package com.github.istin.dmtools.codereview;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.json.JSONObject;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.code.model.SourceCodeConfig;
import com.github.istin.dmtools.common.model.ICommit;
import com.github.istin.dmtools.di.DaggerCodeReviewAgentComponent;
import com.github.istin.dmtools.di.SourceCodeFactory;
import com.github.istin.dmtools.job.AbstractJob;
import com.github.istin.dmtools.prompt.IPromptTemplateReader;
import com.github.istin.dmtools.prompt.PromptContext;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CodeReviewAgent extends AbstractJob<CodeReviewAgentParams> {

    private static final Pattern DIFF_START_LINE_NUMBER_PATTERN = Pattern.compile("\\+(\\d+)");
    private static final Pattern NEW_FILE_PATH_PATTERN = Pattern.compile("diff.* b/(.*)$");
    private static final Pattern GET_LINE_NUMBERS_PATTERN = Pattern.compile("^(\\d+)-?(\\d*)$");

    @Inject
    @Getter
    AI ai;

    @Inject
    SourceCodeFactory sourceCodeFactory;

    @Inject
    IPromptTemplateReader promptTemplateReader;

    public CodeReviewAgent() {
                DaggerCodeReviewAgentComponent.create().inject(this);
    }

    @Override
    public void runJob(CodeReviewAgentParams codeReviewAgentParams) throws Exception {
        // params
        SourceCodeConfig sourceCodeConfig = codeReviewAgentParams.getSourceCodeConfig();
        String pullRequestId = codeReviewAgentParams.getPullRequestId();

        // artifacts
        SourceCode sourceCode = sourceCodeFactory.createSourceCodes(sourceCodeConfig.getType().toString());

        doCodeReview(sourceCode, sourceCodeConfig.getWorkspaceName(), sourceCodeConfig.getRepoName(), pullRequestId,
            getGuideline());
    }

    private void doCodeReview(SourceCode sourceCode, String workspace, String repository, String pullRequestId,
                              String guideline) throws IOException {
        var commits = sourceCode.getCommitsFromPullRequest(workspace, repository, pullRequestId);
        log.info("Found {} commits to review in {} PR", commits.size(), pullRequestId);

        commits.forEach(commit -> {
            try {
                doCodeReviewForCommit(sourceCode, workspace, repository, pullRequestId, commit, guideline);
            } catch (IOException e) {
                log.error("Error occurred during code review for the commit {} (will be skipped):\n{}", commit,
                    e.toString());
            }
        });
    }

    private void doCodeReviewForCommit(SourceCode sourceCode, String workspace, String repository, String pullRequestId,
                                       ICommit commit,
                                       String guideline) throws IOException {
        var commitId = commit.getHash();
        log.info("Doing code review for commit {}", commitId);

        var commitDiff = sourceCode.getCommitDiff(workspace, repository, commitId);
        var diffs = commitDiff.getBody();

        var diffPerFile = diffs.split("(?=diff --git)");
        log.info("Found {} diff files", diffPerFile.length);

        Arrays.stream(diffPerFile).forEach(diff -> {
            var diffLines = diff.split("\n");
            var filePath = getNewFilePath(diffLines[0]);

            try {
                var jsonResult = getCodeReviewForDiffFile(diffLines, guideline);

                var listOfComments = jsonResult.getJSONArray("review_comments");
                listOfComments.iterator().forEachRemaining(o -> {
                    var jsonObject = (JSONObject) o;
                    var lines = jsonObject.getString("lines");
                    var comment = jsonObject.getString("comment");
                    var line = getLine(lines);
                    var startLine = getStartLine(lines);

                    // send a comment to GitHub
                    try {
                        var res = sourceCode.addPullRequestReviewComment(workspace, repository, pullRequestId, commitId,
                            filePath,
                            line, startLine, comment);
                        log.debug("PullRequest review comment added, res: {}", res);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                });

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });

    }

    private JSONObject getCodeReviewForDiffFile(String[] diffLines, String guideline)
        throws Exception {

        var diffWithCodeNumbers = putLineNumbersToCode(diffLines);

        PromptContext context = new PromptContext(null);
        context.set("guideline", guideline);
        context.set("input", diffWithCodeNumbers);

        String prompt = promptTemplateReader.read("developer_code_review_expert", context);

        log.debug("Prompt:\n{}", prompt);
        var result = AI.Utils.chatAsJSONObject(ai, prompt);
        log.debug("Result:\n{}", result);

        return result;
    }

    private static String putLineNumbersToCode(String[] diffLines) {
        int startLineNumber = 0;
        boolean needToMarkLineNumbers = false;
        for (int i = 0; i < diffLines.length; i++) {
            var line = diffLines[i];

            if (needToMarkLineNumbers) {
                if (line.startsWith("-")) {
                    diffLines[i] = "00\t" + line;
                } else {
                    diffLines[i] = startLineNumber++ + "\t" + line;
                }
            }

            if (line.startsWith("@@")) {
                startLineNumber = getStartLineNumber(line);
                needToMarkLineNumbers = true;
            }
        }

        return String.join("\n", diffLines);
    }

    private static int getStartLineNumber(String line) {
        Matcher matcher = DIFF_START_LINE_NUMBER_PATTERN.matcher(line);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        } else {
            throw new RuntimeException("Invalid diff header: " + line);
        }
    }

    private static String getNewFilePath(String line) {
        Matcher matcher = NEW_FILE_PATH_PATTERN.matcher(line);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new RuntimeException("Invalid diff command: " + line);
        }
    }

    private static Integer getLine(String lines) {
        Matcher matcher = GET_LINE_NUMBERS_PATTERN.matcher(lines);

        if (matcher.find()) {
            var first = matcher.group(1);
            var second = matcher.group(2);
            return second != null && !second.isBlank() ? Integer.parseInt(second) : Integer.parseInt(first);
        } else {
            throw new RuntimeException("Invalid lines: " + lines);
        }
    }

    private static Integer getStartLine(String lines) {
        Matcher matcher = GET_LINE_NUMBERS_PATTERN.matcher(lines);

        if (matcher.find()) {
            var first = matcher.group(1);
            var second = matcher.group(2);
            return second != null && !second.isBlank() ? Integer.parseInt(first) : null;
        } else {
            throw new RuntimeException("Invalid lines: " + lines);
        }
    }

    // Just mocked Guideline, @todo need to clarify a good way to obtain the real Guide
    private static String getGuideline() {
        return "# Comprehensive Code Review Guidelines\n"
            + "\n"
            + "## 1. Coding Standards\n"
            + "### General Guidelines\n"
            + "- Use descriptive and meaningful names for variables, methods, and classes.\n"
            + "- Follow consistent naming conventions (e.g., `camelCase` for variables and methods, `PascalCase` for classes).\n"
            + "- Avoid abbreviations unless widely understood (e.g., `URL`, `ID`).\n"
            + "- Adhere to the project's code style guide for indentation, whitespace, and formatting.\n"
            + "- Use comments to explain complex or non-obvious code sections.\n"
            + "\n"
            + "### Example\n"
            + "**Problem:**  \n"
            + "```java\n"
            + "private Flux<String> buffers;\n"
            + "```\n"
            + "**Feedback:** Consider using `List<String>` instead of `Flux<String>` for better clarity and flexibility in handling the data.\n"
            + "\n"
            + "**Solution:**  \n"
            + "```java\n"
            + "private List<String> buffers;\n"
            + "```\n"
            + "\n"
            + "---\n"
            + "\n"
            + "## 2. Performance Optimization\n"
            + "### Best Practices\n"
            + "- Avoid unnecessary object creation or method calls within loops.\n"
            + "- Use efficient data structures (e.g., `HashMap` for fast lookups).\n"
            + "- Optimize reactive pipelines by minimizing the creation of intermediate objects.\n"
            + "- Prefer batch processing over single-item operations when handling large datasets.\n"
            + "\n"
            + "### Example\n"
            + "**Problem:**  \n"
            + "Using `concatMap` repeatedly in a reactive pipeline can lead to inefficiencies.  \n"
            + "**Feedback:** Accumulating writes instead of composing with a new operator for every write can improve performance.\n"
            + "\n"
            + "**Solution:**  \n"
            + "Refactor to use `Flux.fromIterable` with `concatWith` for better efficiency:\n"
            + "```java\n"
            + "Flux.fromIterable(buffers).concatWith(anotherPublisher);\n"
            + "```\n"
            + "\n"
            + "---\n"
            + "\n"
            + "## 3. Security Considerations\n"
            + "### Best Practices\n"
            + "- Validate and sanitize all user inputs to prevent injection attacks.\n"
            + "- Use secure APIs and libraries for cryptographic operations.\n"
            + "- Avoid hardcoding sensitive information (e.g., passwords, API keys) in the codebase.\n"
            + "- Implement proper access controls and authentication mechanisms.\n"
            + "\n"
            + "### Example\n"
            + "**Problem:**  \n"
            + "Using `Environment` directly without considering its global impact.  \n"
            + "**Feedback:** If modifying `Environment`, ensure it does not affect external usage. Consider creating a copy.\n"
            + "\n"
            + "**Solution:**  \n"
            + "```java\n"
            + "Environment copiedEnvironment = new StandardEnvironment();\n"
            + "copiedEnvironment.setIgnoreUnresolvableNestedPlaceholders(true);\n"
            + "```\n"
            + "\n"
            + "---\n"
            + "\n"
            + "## 4. Maintainability\n"
            + "### Codebase Organization\n"
            + "- Follow modular design principles to ensure code is reusable and maintainable.\n"
            + "- Group related classes and files logically (e.g., by feature or functionality).\n"
            + "- Avoid large, monolithic classes; break them into smaller, focused components.\n"
            + "\n"
            + "### Documentation\n"
            + "- Document all public methods and classes with clear descriptions of their purpose and usage.\n"
            + "- Maintain up-to-date API documentation for external integrations.\n"
            + "- Use inline comments sparingly to explain non-obvious code.\n"
            + "\n"
            + "### Example\n"
            + "**Problem:**  \n"
            + "The `FluxWriter` class lacks sufficient documentation for its methods.  \n"
            + "**Feedback:** Add Javadoc comments to improve clarity for future developers.\n"
            + "\n"
            + "**Solution:**  \n"
            + "```java\n"
            + "/**\n"
            + " * Writes a Flux of data buffers to the output stream.\n"
            + " * Used for rendering progressive output in a MustacheView.\n"
            + " */\n"
            + "class FluxWriter extends Writer {\n"
            + "    // Implementation\n"
            + "}\n"
            + "```\n"
            + "\n"
            + "---\n"
            + "\n"
            + "## 5. Testing Guidelines\n"
            + "### Best Practices\n"
            + "- Ensure sufficient test coverage for all critical paths and edge cases.\n"
            + "- Use modular test design to isolate functionality and dependencies.\n"
            + "- Write proper assertions to verify code correctness and expected behavior.\n"
            + "- Use parameterized tests for scenarios with multiple input variations.\n"
            + "\n"
            + "### Example\n"
            + "**Problem:**  \n"
            + "Tests failing due to `WebTestClient` ignoring the base path.  \n"
            + "**Feedback:** Ensure the base path is correctly configured in the test setup.\n"
            + "\n"
            + "**Solution:**  \n"
            + "```java\n"
            + "webTestClient = WebTestClient.bindToApplicationContext(context)\n"
            + "                             .baseUrl(\"/management\")\n"
            + "                             .build();\n"
            + "```\n"
            + "\n"
            + "---\n"
            + "\n"
            + "## 6. Branch and Pull Request Workflow\n"
            + "### Branch Naming Conventions\n"
            + "- Use descriptive names for branches (e.g., `feature/add-user-auth`, `bugfix/fix-null-pointer`).\n"
            + "- Include the ticket number or issue ID in the branch name if applicable.\n"
            + "\n"
            + "### Pull Request Metadata\n"
            + "- Provide a clear and concise title summarizing the changes.\n"
            + "- Include a detailed description of the changes, the problem being solved, and any relevant context.\n"
            + "- Reference related issues or tickets.\n"
            + "\n"
            + "### Handling Merge Conflicts\n"
            + "- Resolve conflicts locally before pushing changes.\n"
            + "- Test the merged code thoroughly to ensure no regressions.\n"
            + "- Avoid force-pushing unless absolutely necessary.\n"
            + "\n"
            + "---\n"
            + "\n"
            + "## 7. API Design and Validation\n"
            + "### Best Practices\n"
            + "- Guard endpoints with proper authentication and authorization mechanisms.\n"
            + "- Validate all inputs to ensure they meet expected formats and constraints.\n"
            + "- Ensure consistent response formats across all endpoints.\n"
            + "- Maintain up-to-date API documentation for external consumers.\n"
            + "\n"
            + "### Example\n"
            + "**Problem:**  \n"
            + "Endpoints lack input validation, leading to potential errors.  \n"
            + "**Feedback:** Add validation logic to ensure inputs are sanitized.\n"
            + "\n"
            + "**Solution:**  \n"
            + "```java\n"
            + "@PostMapping(\"/create\")\n"
            + "public ResponseEntity<?> create(@Valid @RequestBody CreateRequest request) {\n"
            + "    // Implementation\n"
            + "}\n"
            + "```\n"
            + "\n"
            + "---\n"
            + "\n"
            + "## 8. Metadata and Configurations\n"
            + "### Best Practices\n"
            + "- Use environment-specific configuration files to manage settings.\n"
            + "- Avoid hardcoding values; use placeholders and externalized configurations.\n"
            + "- Document all configuration options and their expected values.\n"
            + "\n"
            + "### Example\n"
            + "**Problem:**  \n"
            + "Hardcoded values in the configuration file.  \n"
            + "**Feedback:** Use placeholders to allow flexibility across environments.\n"
            + "\n"
            + "**Solution:**  \n"
            + "```properties\n"
            + "management.endpoints.web.base-path=${BASE_PATH:/management}\n"
            + "```\n"
            + "\n"
            + "---\n"
            + "\n"
            + "## 9. Additional Notes from Pull Request Feedback\n"
            + "### Testing with Testcontainers\n"
            + "- Use `@Testcontainers` JUnit5 extension for managing containers in tests.\n"
            + "- For Spring Boot, manage Testcontainers as Spring beans using configuration classes.\n"
            + "- Ensure proper lifecycle management of containers, especially when used with Spring's `TestContext` framework.\n"
            + "\n"
            + "### Example\n"
            + "**Problem:**  \n"
            + "Incorrect lifecycle management of Testcontainers in Spring Boot tests.  \n"
            + "**Feedback:** Use Spring-managed beans for better lifecycle control.\n"
            + "\n"
            + "**Solution:**  \n"
            + "```java\n"
            + "@Configuration\n"
            + "public class TestContainerConfig {\n"
            + "    @Bean\n"
            + "    public GenericContainer<?> myContainer() {\n"
            + "        return new GenericContainer<>(\"my-image\").withExposedPorts(8080);\n"
            + "    }\n"
            + "}\n"
            + "```\n"
            + "\n"
            + "---\n"
            + "\n"
            + "By adhering to these guidelines, developers can ensure code quality, maintainability, and security while fostering collaboration and efficiency in the development process.";
    }
}
