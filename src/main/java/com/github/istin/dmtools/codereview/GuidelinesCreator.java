package com.github.istin.dmtools.codereview;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.code.model.SourceCodeConfig;
import com.github.istin.dmtools.common.model.IComment;
import com.github.istin.dmtools.common.model.IDiffHunk;
import com.github.istin.dmtools.common.model.IPullRequest;
import com.github.istin.dmtools.prompt.IPromptTemplateReader;
import com.github.istin.dmtools.prompt.PromptContext;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GuidelinesCreator {
    @Getter
    private final AI ai;
    private final SourceCode sourceCode;
    private final IPromptTemplateReader promptTemplateReader;

    public GuidelinesCreator(AI ai, SourceCode sourceCode, IPromptTemplateReader promptTemplateReader) {
        this.ai = ai;
        this.sourceCode = sourceCode;
        this.promptTemplateReader = promptTemplateReader;
    }

    private List<IComment> retrieveComments(String workspace, String repoName, Calendar fromDate)
        throws IOException, InterruptedException {
        List<IPullRequest> pullRequests = sourceCode.pullRequests(
            workspace,
            repoName,
            "closed",
            true,
            fromDate
        );

        log.debug("Found {} pull requests from {}", pullRequests.size(), fromDate);
        List<IComment> comments = new ArrayList<>();

        for (IPullRequest pullRequest : pullRequests.reversed()) {
            List<IComment> prComments = sourceCode.pullRequestComments(
                workspace,
                repoName,
                pullRequest.getId().toString());
            log.debug("Found {} comments for pull request '{}'", prComments.size(), pullRequest.getTitle());
            if (!prComments.isEmpty()) {
                comments.addAll(prComments);
            }
            Thread.sleep(100);
        }

        return comments;
    }

    private String generatePullRequestGuide(String promptName, String projectName, List<IComment> comments)
        throws IOException, InterruptedException {

        String result = "";

        int totalSize = comments.size();
        int batchSize = calculateBatchSize(totalSize);
        log.debug("Comments total count: {}, AI process batch size: {}", totalSize, batchSize);

        for (int i = 0; i < totalSize; i += batchSize) {
            var time = System.currentTimeMillis();

            List<String> batch = new ArrayList<>();
            for (int j = i; j < (i + batchSize) && j < totalSize; j++) {
                var comment = "<pull_request_comment>" + comments.get(j).getBody() + "</pull_request_comment>";
                var diff = "<pull_request_diff_hunk>" + ((IDiffHunk) comments.get(j)).getDiffHunk()
                    + "</pull_request_diff_hunk>";

                batch.add("<change_request>" + comment + diff + "</change_request>");
            }

            PromptContext context = new PromptContext(null);
            context.set("existing_guideline", result);
            context.set("change_requests", String.join("\n", batch));
            context.set("project_name", projectName);

            try {
                String prompt = promptTemplateReader.read(promptName, context);
                result = getAi().chat(prompt);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            log.debug("batch was processed by AI, time {} ms", (System.currentTimeMillis() - time));
        }
        return result;
    }

    public void generateCodeReviewGuide(SourceCodeConfig sourceCodeConfig, String projectName,
                                        Date historyStartDate) {
        var prompt_file_name = "agents/create_review_guide";
        var startDate = toCalendar(historyStartDate);
        var workspace = sourceCodeConfig.getWorkspaceName();
        var repository = sourceCodeConfig.getRepoName();
        projectName = projectName != null ? projectName : repository;

        try {

            long time = System.currentTimeMillis();
            List<IComment> comments = retrieveComments(workspace, repository, startDate);
            log.debug("Retrieve comments time: {} ms", (System.currentTimeMillis() - time));

            if (comments.isEmpty()) {
                throw new RuntimeException("Pull request comments weren't found!");
            }

            time = System.currentTimeMillis();
            var guide = generatePullRequestGuide(prompt_file_name, projectName, comments);
            log.debug("Generate guide time: {}", (System.currentTimeMillis() - time));

            // save to file
            Path path = Path.of("src", "main", "resources", "code-review-guides",
                projectName + "-" + LocalDateTime.now() + ".md");
            Files.createDirectories(path.getParent());
            Files.writeString(path, guide);
            log.info("Generated Code Review Guide was saved in file {}", path);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private static Calendar toCalendar(Date localDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(localDate);

        return calendar;
    }

    private static int calculateBatchSize(int totalMessages) {
        int batchCount = (int) Math.round(totalMessages / 40.0D);
        return (int) Math.ceil((double) totalMessages / batchCount);
    }

}
