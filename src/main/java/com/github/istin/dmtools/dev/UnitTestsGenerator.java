package com.github.istin.dmtools.dev;

import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.ai.JAssistant;
import com.github.istin.dmtools.file.FileContentListener;
import com.github.istin.dmtools.file.SourceCodeReader;
import com.github.istin.dmtools.job.AbstractJob;
import com.github.istin.dmtools.openai.BasicOpenAI;
import com.github.istin.dmtools.openai.PromptManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class UnitTestsGenerator extends AbstractJob<UnitTestsGeneratorParams> {

    @Override
    public void runJob(UnitTestsGeneratorParams params) throws Exception {
        List<String> extensionsList = Arrays.asList(params.getFileExtensions());
        SourceCodeReader sourceCodeReader = new SourceCodeReader(extensionsList);
        ConversationObserver conversationObserver = new ConversationObserver();
        BasicOpenAI openAI = new BasicOpenAI(conversationObserver);
        PromptManager promptManager = new PromptManager();
        JAssistant jAssistant = new JAssistant(null, null, openAI, promptManager);
        sourceCodeReader.readSourceFiles(Paths.get(params.getSrcFolder()), new FileContentListener() {
            @Override
            public void onFileRead(String folderPath, String packageName, String fileName, String fileContent) throws Exception {
                try {
                    String packageFilter = params.getPackageFilter();
                    if (packageFilter != null && packageFilter.equalsIgnoreCase(packageName)) {
                        String baseFileName = fileName.substring(0, fileName.lastIndexOf('.'));
                        String testFileName = baseFileName + "Test.java"; // Assuming Java test files

                        Path testFilePath = Paths.get(params.getRootTestsFolder(), packageName.replace('.', '/'), testFileName);

                        if (!Files.exists(testFilePath)) {
                            createTestFileSkeleton(fileContent, testFilePath, baseFileName, packageName, params, jAssistant);
                        }
                    }
                } catch (IOException e) {
                    // Handle exceptions appropriately
                }
            }
        });
    }

    private void createTestFileSkeleton(String fileContent, Path testFilePath, String className, String packageName, UnitTestsGeneratorParams params, JAssistant jAssistant) throws Exception {
        Files.createDirectories(testFilePath.getParent());
        String testClassContent = generateTestClassSkeleton(fileContent, className, packageName, params, jAssistant);
        if (testClassContent != null) {
            Files.writeString(testFilePath, testClassContent);
            //TODO need to think how to check the test
        }
    }

    private String generateTestClassSkeleton(String fileContent, String className, String packageName, UnitTestsGeneratorParams params, JAssistant jAssistant) throws Exception {
        String testTemplate = params.getTestTemplate();
        if (testTemplate == null || testTemplate.isEmpty()) {
            // Default template if none provided
            return String.format("import org.junit.Test;\n\n" +
                    "public class %sTest {\n\n" +
                    "    @Test\n" +
                    "    public void test() {\n" +
                    "        // TODO: Implement test logic for %s\n" +
                    "    }\n" +
                    "}\n", className, className);
        } else {
            // Use provided template, replace placeholders
            return jAssistant.generateUnitTest(fileContent, className, packageName,
                    testTemplate.replace("${PACKAGE_NAME}", packageName)
                    .replace("${CLASS_NAME}", className)
                    .replace("${TEST_NAME}", className + "Test"), params);
        }
    }

    public static void main(String[] args) throws Exception {
        UnitTestsGeneratorParams params = new UnitTestsGeneratorParams();
        params.setSrcFolder("src/main/java");
        params.setRootTestsFolder("src/test/java");
        params.setFileExtensions(new String[]{".java"});
        params.setPackageFilter("com.github.istin.dmtools.common.utils");
        params.setTestTemplate(
                "package ${PACKAGE_NAME};\n\n" +
                        "import org.junit.Test;\n\n" +
                        "public class ${TEST_NAME} {\n\n" +
                        "    @Test\n" +
                        "    public void test() {\n" +
                        "        // TODO: Implement test logic for ${CLASS_NAME}\n" +
                        "    }\n" +
                        "}"
        );
        params.setTestTemplate(
                "package ${PACKAGE_NAME};\n\n"
        );
        params.setRole("Java Developer");
        params.setRules(readRulesFromFile("jai_config/unit_tests_generation_rules.md"));

        UnitTestsGenerator generator = new UnitTestsGenerator();
        generator.runJob(params);
    }

    private static String readRulesFromFile(String filePath) throws IOException {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            System.err.println("Error reading rules file: " + e.getMessage());
            return ""; // Return an empty string if the file cannot be read
        }
    }
}
