package com.github.istin.dmtools.ai;

import com.github.istin.dmtools.atlassian.jira.model.IssueType;
import com.github.istin.dmtools.atlassian.jira.model.Relationship;
import com.github.istin.dmtools.atlassian.jira.model.Ticket;
import com.github.istin.dmtools.atlassian.jira.utils.IssuesIDsParser;
import com.github.istin.dmtools.ba.UserStoryGeneratorParams;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.model.*;
import com.github.istin.dmtools.common.networking.RestClient;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.common.utils.PropertyReader;
import com.github.istin.dmtools.common.utils.StringUtils;
import com.github.istin.dmtools.dev.UnitTestsGeneratorParams;
import com.github.istin.dmtools.openai.OpenAIClient;
import com.github.istin.dmtools.openai.PromptManager;
import com.github.istin.dmtools.openai.input.*;
import com.github.istin.dmtools.openai.utils.AIResponseParser;
import com.github.istin.dmtools.qa.TestCasesGeneratorParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JAssistant {

    private static final Logger logger = LogManager.getLogger(JAssistant.class);
    public static final String ME = JAssistant.class.getSimpleName();
    public static final String TEST_CASES_COMMENT_PREFIX = "<p>JAI Generated Test Cases: </p>";
    public static final String USER_STORIES_COMMENT_PREFIX = "<p>JAI Generated User Stories: </p>";
    public static final String LABEL_REQUIREMENTS = "requirements";
    public static final String LABEL_TIMELINE = "timeline";
    public static final String LABEL_TEAM_SETUP = "team_setup";

    private static String CODE_AI_MODEL;

    private static String TEST_AI_MODEL;

    static {
        PropertyReader propertyReader = new PropertyReader();
        String codeAIModel = propertyReader.getCodeAIModel();
        String defaultModel = propertyReader.getOpenAIModel();
        if (codeAIModel == null || codeAIModel.isEmpty()) {
            CODE_AI_MODEL = defaultModel;
        } else {
            CODE_AI_MODEL = codeAIModel;
        }

        String testAIModel = propertyReader.getTestAIModel();
        if (testAIModel == null || testAIModel.isEmpty()) {
            TEST_AI_MODEL = defaultModel;
        } else {
            TEST_AI_MODEL = testAIModel;
        }
    }

    private TrackerClient<? extends ITicket> trackerClient;

    private List<SourceCode> sourceCodes;

    private OpenAIClient openAIClient;

    private PromptManager promptManager;

    private ConversationObserver conversationObserver;

    public ConversationObserver getConversationObserver() {
        return conversationObserver;
    }

    public void setConversationObserver(ConversationObserver conversationObserver) {
        this.conversationObserver = conversationObserver;
    }

    public JAssistant(TrackerClient<? extends ITicket> trackerClient, List<SourceCode> sourceCodes, OpenAIClient openAIClient, PromptManager promptManager) {
        this(trackerClient, sourceCodes, openAIClient, promptManager, null);
    }

    public JAssistant(TrackerClient<? extends ITicket> trackerClient, List<SourceCode> sourceCodes, OpenAIClient openAIClient, PromptManager promptManager, ConversationObserver conversationObserver) {
        this.trackerClient = trackerClient;
        this.sourceCodes = sourceCodes;
        this.openAIClient = openAIClient;
        this.promptManager = promptManager;
        this.conversationObserver = conversationObserver;
    }

    public void generateCode(String role, TicketContext ticketContext) throws Exception {
        ITicket ticket = ticketContext.getTicket();

        CodeGeneration codeGeneration = new CodeGeneration(trackerClient.getBasePath(), role, ticketContext);
        codeGeneration.setExtraTickets(ticketContext.getExtraTickets());
        if (!IssueType.isBug(ticket.getIssueType())) {
            List<? extends ITicket> testCases = trackerClient.getTestCases(ticket);
            codeGeneration.setExistingTickets(testCases);
        }

        List<IFile> finalResults = new ArrayList<>();
        for (SourceCode sourceCode : sourceCodes) {
            extractPotentiallyEffectedFiles(role, ticketContext, sourceCode, codeGeneration, ticket, finalResults);
        }

        codeGeneration.setFiles(finalResults);
        String finalAIRequest = promptManager.requestGenerateCodeForTicket(codeGeneration);
        String finalResponse = openAIClient.chat(
                CODE_AI_MODEL,
                finalAIRequest);
        logger.info("----- FINAL AI RESPONSE ---- ");
        logger.info(finalResponse);
        trackerClient.postComment(ticketContext.getTicket().getTicketKey(), "<p>AI Generated Code: </p>" + finalResponse);
        trackerClient.addLabelIfNotExists(ticketContext.getTicket(), "ai_generated_code");

    }

    private void extractPotentiallyEffectedFiles(String role, TicketContext ticketContext, SourceCode sourceCode, CodeGeneration codeGeneration, ITicket ticket, List<IFile> finalResults) throws Exception {
        List<IFile> listOfFiles = sourceCode.getListOfFiles(sourceCode.getDefaultWorkspace(), sourceCode.getDefaultRepository(), sourceCode.getDefaultBranch());
        List<IFile> filesOnly = listOfFiles.stream().filter(file -> !file.isDir()).collect(Collectors.toList());

        JSONArray filePaths = getListOfEffectedFilesFromFiles(codeGeneration, filesOnly);

        List<ICommit> commitsFromBranch = sourceCode.getCommitsFromBranch(sourceCode.getDefaultWorkspace(), sourceCode.getDefaultRepository(), sourceCode.getDefaultBranch(), null, null);
        JSONArray filePathsFromCommits = getListOfEffectedFilesFromCommits(sourceCode, sourceCode.getDefaultWorkspace(), sourceCode.getDefaultRepository(), codeGeneration, commitsFromBranch);
        filePaths.putAll(filePathsFromCommits);
        for (int i = 0; i < filePaths.length(); i++) {
            String path = filePaths.getString(i);
            List<IFile> result = filesOnly.stream().filter(file -> file.getPath().equals(path)).collect(Collectors.toList());
            if (!result.isEmpty()) {
                IFile file = result.get(0);
                file.setFileContent(sourceCode.getFileContent(file.getSelfLink()));

                TicketFilePrompt ticketFilePrompt = new TicketFilePrompt(trackerClient.getBasePath(), role, ticketContext, file);
                ticketFilePrompt.setExtraTickets(ticketContext.getExtraTickets());
                String request = promptManager.validatePotentiallyEffectedFile(ticketFilePrompt);
                boolean isTheFileUsefull = openAIClient.chatAsBoolean(
                        CODE_AI_MODEL,
                        request);
                if (isTheFileUsefull) {
                    finalResults.add(file);
                }
            }
        }
    }

    private JSONArray getListOfEffectedFilesFromFiles(CodeGeneration codeGeneration, List<IFile> filesOnly) throws Exception {
        codeGeneration.setFiles(filesOnly);
        String aiRequest = promptManager.checkPotentiallyEffectedFilesForTicket(codeGeneration);
        JSONArray result = openAIClient.chatAsJSONArray(
                CODE_AI_MODEL,
                aiRequest);
        logger.info(result);
        return result;
    }

    private JSONArray getListOfEffectedFilesFromCommits(SourceCode sourceCode, String workspace, String repository, CodeGeneration codeGeneration, List<ICommit> commits) throws Exception {
        codeGeneration.setCommits(commits);
        String aiRequest = promptManager.checkPotentiallyRelatedCommitsToTicket(codeGeneration);
        JSONArray jsonArray = openAIClient.chatAsJSONArray(
                CODE_AI_MODEL,
                aiRequest);
        logger.info(jsonArray);
        Set<String> filesFromCommits = new HashSet<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            String commit = jsonArray.getString(i);
            IDiffStats commitDiffStat = sourceCode.getCommitDiffStat(workspace, repository, commit);
            List<IChange> changes = commitDiffStat.getChanges();
            for (IChange change : changes) {
                filesFromCommits.add(change.getFilePath());
            }
        }
        JSONArray result = new JSONArray();
        for (String path : filesFromCommits) {
            result.put(path);
        }
        return result;
    }

    private void trackConversation(String author, String text) {
        if (conversationObserver != null) {
            conversationObserver.addMessage(new ConversationObserver.Message(author, text));
        }
    }

    public JSONArray generateUserStories(TicketContext ticketContext, List<? extends ITicket> listOfLinkedUserStories, String projectCode, String issueType, String acceptanceCriteriaField, String relationship, String outputType, String priorities) throws Exception {
        ITicket mainTicket = ticketContext.getTicket();
        String key = mainTicket.getTicketKey();

        if (outputType.equals(UserStoryGeneratorParams.OUTPUT_TYPE_TRACKER_COMMENT)) {
            String message = TrackerClient.Utils.checkCommentStartedWith(trackerClient, mainTicket.getKey(), mainTicket, USER_STORIES_COMMENT_PREFIX);
            if (message != null) {
                return null;
            }
        }

        TicketCreationPrompt userStoryCreationPrompt = new TicketCreationPrompt(trackerClient.getBasePath(), ticketContext, priorities);
        userStoryCreationPrompt.setExistingTickets(listOfLinkedUserStories);

        if (outputType.equals(UserStoryGeneratorParams.OUTPUT_TYPE_TRACKER_COMMENT)) {
            String aiRequest = promptManager.generateUserStoriesAsHTML(userStoryCreationPrompt);
            String response = openAIClient.chat(aiRequest);
            String comment = USER_STORIES_COMMENT_PREFIX + response;
            trackerClient.postComment(key, comment);
        } else {
            String aiRequest = promptManager.generateUserStoriesAsJSONArray(userStoryCreationPrompt);
            JSONArray array = openAIClient.chatAsJSONArray(aiRequest);
            if (!array.isEmpty()) {
                createUserStories(projectCode, issueType, acceptanceCriteriaField, relationship, array, mainTicket);
            } else {
                String aiResponseToUpdateUserStories = promptManager.updateUserStoriesAsJSONArray(userStoryCreationPrompt);
                JSONArray objects = openAIClient.chatAsJSONArray(aiResponseToUpdateUserStories);
                updateUserStories(acceptanceCriteriaField, objects);
                return objects;
            }
        }
        return null;
    }

    private void updateUserStories(String acceptanceCriteriaField, JSONArray objects) throws IOException {
        for (int i = 0; i < objects.length(); i++) {
            JSONObject jsonObject = objects.getJSONObject(i);
            String storyKey = jsonObject.getString("key");
            String summary = jsonObject.getString("summary");
            String description = jsonObject.getString("description");
            String acceptanceCriteria = jsonObject.getString("acceptanceCriteria");
            if (acceptanceCriteriaField == null) {
                description += "\n\n" + acceptanceCriteria;
            }
            if (trackerClient.getTextType() == TrackerClient.TextType.MARKDOWN) {
                description = StringUtils.convertToMarkdown(description);
            }
            String finalDescription = description;
            trackerClient.updateTicket(storyKey, new TrackerClient.FieldsInitializer() {
                @Override
                public void init(TrackerClient.TrackerTicketFields fields) {
                    if (acceptanceCriteriaField != null) {
                        if (trackerClient.getTextType() == TrackerClient.TextType.MARKDOWN) {
                            fields.set(acceptanceCriteriaField, StringUtils.convertToMarkdown(acceptanceCriteria));
                        } else {
                            fields.set(acceptanceCriteriaField, acceptanceCriteria);
                        }
                    }
                    fields.set("description", finalDescription);
                    fields.set("summary", summary);
                }
            });
        }
    }

    private void createUserStories(String projectCode, String issueType, String acceptanceCriteriaField, String relationship, JSONArray array, ITicket mainTicket) throws IOException {
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            String description = jsonObject.getString("description");
            String acceptanceCriteria = jsonObject.getString("acceptanceCriteria");
            if (acceptanceCriteriaField == null) {
                description += "\n\n" + acceptanceCriteria;
            }
            if (trackerClient.getTextType() == TrackerClient.TextType.MARKDOWN) {
                description = StringUtils.convertToMarkdown(description);
            }
            Ticket createdUserStories = new Ticket(trackerClient.createTicketInProject(projectCode, issueType, jsonObject.getString("summary"), description, new TrackerClient.FieldsInitializer() {
                @Override
                public void init(TrackerClient.TrackerTicketFields fields) {
                    fields.set("priority",
                            new JSONObject().put("name", jsonObject.getString("priority"))
                    );
                    fields.set("labels", new JSONArray().put("ai_generated"));
                    if (acceptanceCriteriaField != null) {
                        if (trackerClient.getTextType() == TrackerClient.TextType.MARKDOWN) {
                            fields.set(acceptanceCriteriaField, StringUtils.convertToMarkdown(acceptanceCriteria));
                        } else {
                            fields.set(acceptanceCriteriaField, acceptanceCriteria);
                        }
                    }
                    if (relationship != null && relationship.equalsIgnoreCase("parent")) {
                        if (mainTicket instanceof Ticket) {
                            fields.set("parent", ((Ticket) mainTicket).getJSONObject());
                        }
                    }
                }
            }));
            if (relationship != null && !relationship.equalsIgnoreCase("parent")) {
                trackerClient.linkIssueWithRelationship(mainTicket.getTicketKey(), createdUserStories.getKey(), relationship);
            }
        }
    }

    public void generateTestCases(TicketContext ticketContext, List<? extends ITicket> listOfAllTestCases, String outputType, String testCasesPriorities) throws Exception {
        ITicket mainTicket = ticketContext.getTicket();
        String key = mainTicket.getTicketKey();

        if (outputType.equals(TestCasesGeneratorParams.OUTPUT_TYPE_TRACKER_COMMENT)) {
            String message = TrackerClient.Utils.checkCommentStartedWith(trackerClient, mainTicket.getKey(), mainTicket, TEST_CASES_COMMENT_PREFIX);
            if (message != null) {
                return;
            }
        }

        List<ITicket> finaResults = findAndLinkSimilarTestCasesBySummary(ticketContext, listOfAllTestCases, true);

        TicketCreationPrompt qaTestCasesPrompt = new TicketCreationPrompt(trackerClient.getBasePath(), ticketContext, testCasesPriorities);
        qaTestCasesPrompt.setExistingTickets(finaResults);

        if (outputType.equals(TestCasesGeneratorParams.OUTPUT_TYPE_TRACKER_COMMENT)) {
            String aiRequest = promptManager.requestTestCasesForStoryAsHTML(qaTestCasesPrompt);
            String response = openAIClient.chat(TEST_AI_MODEL, aiRequest);
            String comment = TEST_CASES_COMMENT_PREFIX + response;
            trackerClient.postComment(key, comment);
        } else {
            String aiRequest = promptManager.requestTestCasesForStoryAsJSONArray(qaTestCasesPrompt);
            JSONArray array = openAIClient.chatAsJSONArray(TEST_AI_MODEL, aiRequest);
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                String projectCode = key.split("-")[0];
                String description = jsonObject.getString("description");
                if (trackerClient.getTextType() == TrackerClient.TextType.MARKDOWN) {
                    description = StringUtils.convertToMarkdown(description);
                }
                Ticket createdTestCase = new Ticket(trackerClient.createTicketInProject(projectCode, "Test Case", jsonObject.getString("summary"), description, new TrackerClient.FieldsInitializer() {
                    @Override
                    public void init(TrackerClient.TrackerTicketFields fields) {
                        fields.set("priority",
                                new JSONObject().put("name", jsonObject.getString("priority"))
                        );
                        fields.set("labels", new JSONArray().put("ai_generated"));
                    }
                }));
                trackerClient.linkIssueWithRelationship(mainTicket.getTicketKey(), createdTestCase.getKey(), Relationship.TESTS);
            }
        }
    }

    @NotNull
    public List<ITicket> findAndLinkSimilarTestCasesByDetails(TicketContext ticketContext, List<? extends ITicket> listOfAllTestCases, boolean isLink) throws Exception {
        List<ITicket> finaResults = new ArrayList<>();
        for (ITicket testCase : listOfAllTestCases) {
            SimilarStoriesPrompt similarStoriesPrompt = new SimilarStoriesPrompt(trackerClient.getBasePath(),  "", ticketContext, testCase);

            String chatRequest = promptManager.validateTestCaseRelatedToStory(similarStoriesPrompt);
            String isRelatedToStory = openAIClient.chat(
                    "gpt-35-turbo",
                    chatRequest);
            if (Boolean.parseBoolean(isRelatedToStory)) {
                boolean isConfirmed = openAIClient.chatAsBoolean(
                        TEST_AI_MODEL,
                        chatRequest);
                if (isConfirmed) {
                    finaResults.add(testCase);
                    if (isLink) {
                        trackerClient.linkIssueWithRelationship(ticketContext.getTicket().getTicketKey(), testCase.getKey(), Relationship.TESTS);
                    }
                }
            }
        }
        return finaResults;
    }

    @NotNull
    public List<ITicket> findAndLinkSimilarTestCasesBySummary(TicketContext ticketContext, List<? extends ITicket> listOfAllTestCases, boolean isLink) throws Exception {
        List<ITicket> finaResults = new ArrayList<>();
        int batchSize = 50;
        for (int i = 0; i < listOfAllTestCases.size(); i += batchSize) {
            List<? extends ITicket> batch = listOfAllTestCases.subList(i, Math.min(i + batchSize, listOfAllTestCases.size()));
            TicketBasedPrompt similarStoriesPrompt = new TicketBasedPrompt(trackerClient.getBasePath(), ticketContext);
            similarStoriesPrompt.setExistingTickets(batch);
            String chatRequest = promptManager.validateTestCasesAreRelatedToStory(similarStoriesPrompt);
            JSONArray testCaseKeys = openAIClient.chatAsJSONArray(TEST_AI_MODEL, chatRequest);
            //find relevant test case from batch
            for (int j = 0; j < testCaseKeys.length(); j++) {
                String testCaseKey = testCaseKeys.getString(j);
                ITicket testCase = batch.stream().filter(t -> t.getKey().equals(testCaseKey)).findFirst().orElse(null);
                if (testCase != null) {
                    String doubleCheckRequest = promptManager.validateTestCaseRelatedToStory(new SimilarStoriesPrompt(trackerClient.getBasePath(), "", ticketContext, testCase));
                    boolean isConfirmed = openAIClient.chatAsBoolean(
                            TEST_AI_MODEL,
                            doubleCheckRequest);
                    if (isConfirmed) {
                        finaResults.add(testCase);
                        if (isLink) {
                            trackerClient.linkIssueWithRelationship(ticketContext.getTicket().getTicketKey(), testCase.getKey(), Relationship.TESTS);
                        }
                    }
                }
            }
        }
        return finaResults;
    }

    public @NotNull StringBuilder buildAttachmentsDescription(ITicket ticket) throws Exception {
        List<? extends IAttachment> attachments = ticket.getAttachments();
        StringBuilder attachmentsDescription = new StringBuilder();
        if (!attachments.isEmpty()) {
            for (IAttachment attachment : attachments) {
                if (!RestClient.Impl.getFileImageExtension(attachment.getName()).isEmpty()) {
                    java.io.File pageSnapshot = trackerClient.convertUrlToFile(attachment.getUrl());
                    String extendedDescription = combineTextAndImage(ticket.getTicketTitle() + "\n" + ticket.getTicketDescription() + "\n" + attachmentsDescription, pageSnapshot);
                    attachmentsDescription.append(extendedDescription).append("\n");
                }
            }

        }
        return attachmentsDescription;
    }

    public void generateNiceLookingStoryInGherkinStyleAndPotentialQuestionsToPO(String key) throws Exception {
        ITicket ticket = trackerClient.performTicket(key, trackerClient.getExtendedQueryFields());
        TicketContext ticketContext = new TicketContext(trackerClient, ticket);
        ticketContext.prepareContext();
        String aiRequest = promptManager.requestNiceLookingStoryInGherkinStyleAndPotentialQuestionsToPO(new TicketBasedPrompt(trackerClient.getBasePath(), ticketContext));
        String response = openAIClient.chat(aiRequest);
        response = openAIClient.chat(promptManager.convertToHTML(new InputPrompt(response)));
        trackerClient.postComment(key, "<p>JAI Generated Nice Looking Story In Gherkin Style And Potential Questions To PO: </p>" + response);
    }

    public String checkStoryIsTechnicalOrProduct(ITicket ticket) throws Exception {
        TicketContext ticketContext = new TicketContext(trackerClient, ticket);
        ticketContext.prepareContext();
        String aiRequest = promptManager.checkTaskTechnicalOrProduct(new TicketBasedPrompt(trackerClient.getBasePath(), ticketContext));
        return openAIClient.chat(aiRequest);
    }

    public String chooseFeatureAreaForStory(ToText inputText, String areas) throws Exception {
        String aiRequest = promptManager.checkStoryAreas(new BAStoryAreaPrompt(trackerClient.getBasePath(), inputText, areas));
        return openAIClient.chat(
                aiRequest
        );
    }

    public String whatIsFeatureAreaOfStory(ToText ticket) throws Exception {
        String aiRequest = promptManager.whatIsFeatureAreaOfStory(new TextInputPrompt(trackerClient.getBasePath(), ticket));
        return openAIClient.chat(aiRequest);
    }

    public JSONArray whatIsFeatureAreasOfDataInput(ToText textInput) throws Exception {
        String aiRequest = promptManager.whatIsFeatureAreasOfDataInput(new TextInputPrompt(trackerClient.getBasePath(), textInput));
        return openAIClient.chatAsJSONArray(aiRequest);
    }

    public String buildDetailedPageWithRequirementsForInputData(ToText inputData, String existingContent) throws Exception {
        String aiRequest = promptManager.buildDetailedPageWithRequirementsForInputData(new NiceLookingDocumentationPrompt(trackerClient.getBasePath(), inputData, existingContent));
        return openAIClient.chat(aiRequest);
    }

    public String buildNiceLookingDocumentationForStory(ToText inputData, String existingContent) throws Exception {
        String aiRequest = promptManager.buildNiceLookingDocumentation(new NiceLookingDocumentationPrompt(trackerClient.getBasePath(), inputData, existingContent));
        return openAIClient.chat(aiRequest);
    }

    public String buildDORGenerationForStory(ToText inputData, String existingContent) throws Exception {
        String aiRequest = promptManager.buildDorBasedOnExistingStories(new NiceLookingDocumentationPrompt(trackerClient.getBasePath(), inputData, existingContent));
        return openAIClient.chat(aiRequest);
    }

    public String buildProjectTimeline(ToText input, String existingContent) throws Exception {
        String aiRequest = promptManager.buildProjectTimelinePage(new NiceLookingDocumentationPrompt(trackerClient.getBasePath(), input, existingContent));
        return openAIClient.chat(aiRequest);
    }

    public String buildTeamSetupAndLicenses(ToText input, String existingContent) throws Exception {
        String aiRequest = promptManager.buildTeamSetupAndLicensesPage(new NiceLookingDocumentationPrompt(trackerClient.getBasePath(), input, existingContent));
        return openAIClient.chat(aiRequest);
    }

    public String buildNiceLookingDocumentationForStoryWithTechnicalDetails(ToText input, String existingContent) throws Exception {
        String aiRequest = promptManager.buildNiceLookingDocumentationWithTechnicalDetails(new NiceLookingDocumentationPrompt(trackerClient.getBasePath(), input, existingContent));
        return openAIClient.chat(aiRequest);
    }

    public Double estimateStory(String role, String key, List<? extends ITicket> existingStories, boolean isCheckDetailsOfStory) throws Exception {
        ITicket ticket = trackerClient.performTicket(key, trackerClient.getExtendedQueryFields());
        TicketContext ticketContext = new TicketContext(trackerClient, ticket);
        ticketContext.prepareContext();
        List<ITicket> finalResults = checkSimilarTickets(role, existingStories, isCheckDetailsOfStory, ticketContext);
        String finalAiRequest = promptManager.estimateStory(new SimilarStoriesPrompt(trackerClient.getBasePath(), ticketContext, finalResults));
        String finalEstimations = openAIClient.chat(finalAiRequest);
        return findFirstNumberInTheString(finalEstimations);
    }

    public @NotNull List<ITicket> checkSimilarTickets(String role, List<? extends ITicket> existingTickets, boolean isCheckDetailsOfStory, TicketContext ticketContext) throws Exception {
        SimilarStoriesPrompt similarStoriesHighlevel = new SimilarStoriesPrompt(trackerClient.getBasePath(), ticketContext, existingTickets);
        String aiRequest = promptManager.checkSimilarTickets(similarStoriesHighlevel);
        JSONArray array = openAIClient.chatAsJSONArray(aiRequest);
        List<ITicket> finalResults = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            String similarKey = array.getString(i);
            ITicket similarTicket = trackerClient.performTicket(similarKey, trackerClient.getExtendedQueryFields());
            if (isCheckDetailsOfStory) {
                SimilarStoriesPrompt similarStoriesPrompt = new SimilarStoriesPrompt(trackerClient.getBasePath(), role, ticketContext, similarTicket);
                String chatRequest = promptManager.validateSimilarStory(similarStoriesPrompt);
                boolean isSimilarStory = openAIClient.chatAsBoolean(
                    "gpt-35-turbo",
                        chatRequest);
                if (isSimilarStory) {
                    finalResults.add(similarTicket);
                }
            } else {
                finalResults.add(similarTicket);
            }
        }
        for (ITicket result : finalResults) {
            logger.debug("{} {}", result.getTicketTitle(), result.getWeight());
        }
        return finalResults;
    }

    private static Double findFirstNumberInTheString(String input) {
        Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?");

        // Create a matcher to find matches of the pattern in the input string
        Matcher matcher = pattern.matcher(input);

        // Check if there is at least one match
        if (matcher.find()) {
            // matcher.group() returns the first match found
            String firstNumber = matcher.group();
            return Double.parseDouble(firstNumber);
        } else {
            return null;
        }
    }
    public void reviewPullRequest(SourceCode sourceCode, String role, String workspace, String repository, String pullRequestId, IssuesIDsParser issuesIDsParser) throws Exception {
        IPullRequest pullRequest = sourceCode.pullRequest(workspace, repository, pullRequestId);
        List<String> keys = issuesIDsParser.parseIssues(pullRequest.getTitle(), pullRequest.getSourceBranchName(), pullRequest.getDescription());
        if (keys.isEmpty()) {
            sourceCode.addPullRequestComment(workspace, repository, pullRequestId, "Please use Ticket Number in Title, Description or Branch Name");
            return;
        }
        if (keys.size() > 1) {
            sourceCode.addPullRequestComment(workspace, repository, pullRequestId, "One Pull Request should be related to one ticket.");
            return;
        }

        ITicket ticket = trackerClient.performTicket(keys.get(0), trackerClient.getExtendedQueryFields());
        TicketContext ticketContext = new TicketContext(trackerClient, ticket);
        ticketContext.prepareContext();
        PullRequestReview input = new PullRequestReview(trackerClient.getBasePath(), role, ticketContext);
        input.setTicket(ticket);

        if (!IssueType.isBug(ticket.getIssueType())) {
            List<? extends ITicket> testCases = trackerClient.getTestCases(ticket);
            input.setExistingTickets(testCases);
        }

        input.setRole(role);
        IBody diff = sourceCode.getDiff(workspace, repository, pullRequestId);
        input.setDiff(diff.getBody());

        String request;
        if (!IssueType.isBug(ticket.getIssueType())) {
            request = promptManager.requestDeveloperStoryPullRequestReview(input);
        } else {
            request = promptManager.requestDeveloperBugPullRequestReview(input);
        }
        String response = openAIClient.chat(request);
        logger.info("===== AI Response ======");
        logger.info(response);
        logger.info("===== AI Response ======");
        sourceCode.addPullRequestComment(workspace, repository, pullRequestId, "JAI Review \n\n\n" + response);
    }


    public JSONObject createFeatureAreasTree(String inputAreas) throws Exception {
        String prompt = promptManager.createFeatureAreasTree(new InputPrompt(inputAreas));
        return openAIClient.chatAsJSONObject(prompt);
    }

    public JSONArray cleanFeatureAreas(String inputAreas) throws Exception {
        String prompt = promptManager.cleanFeatureAreas(new InputPrompt(inputAreas));
        JSONArray cleanedAreas = openAIClient.chatAsJSONArray(prompt);

        List<String> list = new ArrayList<>();
        for(int i=0; i < cleanedAreas.length(); i++){
            list.add(cleanedAreas.getString(i));
        }
        Collections.sort(list);

        return new JSONArray(list);
    }

    public void identifyIsContentRelatedToRequirementsAndMarkViaLabel(String prefix, ITicket ticket) throws Exception {
        if (isTicketWasIdentified(prefix, ticket, LABEL_REQUIREMENTS)) {
            return;
        }
        TicketContext ticketContext = new TicketContext(trackerClient, ticket);
        ticketContext.prepareContext();
        String prompt = String.valueOf(promptManager.isContentRelatedToRequirements(new TicketBasedPrompt(trackerClient.getBasePath(), ticketContext)));
        boolean isRequirements = openAIClient.chatAsBoolean(prompt);
        if (isRequirements) {
            trackerClient.addLabelIfNotExists(ticket, prefix + "_" + LABEL_REQUIREMENTS);
        } else {
            trackerClient.addLabelIfNotExists(ticket, prefix + "_not_" + LABEL_REQUIREMENTS);
        }
    }

    private boolean isTicketWasIdentified(String prefix, ITicket ticket, String labelName) {
        return TrackerClient.Utils.isLabelExists(ticket, prefix + "_" + labelName) || TrackerClient.Utils.isLabelExists(ticket, prefix + "_not_" + labelName) ;
    }

    public void identifyIsContentRelatedToTimelineAndMarkViaLabel(String prefix, ITicket ticket) throws Exception {
        if (isTicketWasIdentified(prefix, ticket, LABEL_TIMELINE)) {
            return;
        }

        TicketContext ticketContext = new TicketContext(trackerClient, ticket);
        ticketContext.prepareContext();

        String prompt = String.valueOf(promptManager.isContentRelatedToTimeline(new TicketBasedPrompt(trackerClient.getBasePath(), ticketContext)));
        boolean isTimeline = openAIClient.chatAsBoolean(prompt);
        if (isTimeline) {
            trackerClient.addLabelIfNotExists(ticket, prefix + "_" + LABEL_TIMELINE);
        } else {
            trackerClient.addLabelIfNotExists(ticket, prefix + "_not_" + LABEL_TIMELINE);
        }
    }

    public void identifyIsContentRelatedToTeamSetupAndMarkViaLabel(String prefix, ITicket ticket) throws Exception {
        if (isTicketWasIdentified(prefix, ticket, LABEL_TEAM_SETUP)) {
            return;
        }
        TicketContext ticketContext = new TicketContext(trackerClient, ticket);
        ticketContext.prepareContext();

        String prompt = String.valueOf(promptManager.isContentRelatedToTeamSetup(new TicketBasedPrompt(trackerClient.getBasePath(), ticketContext)));
        boolean isTeamSetup = openAIClient.chatAsBoolean(prompt);
        if (isTeamSetup) {
            trackerClient.addLabelIfNotExists(ticket, prefix + "_" + LABEL_TEAM_SETUP);
        } else {
            trackerClient.addLabelIfNotExists(ticket, prefix + "_not_" + LABEL_TEAM_SETUP);
        }
    }

    public String combineTextAndImage(String text, java.io.File pageSnapshot) throws Exception {
        String prompt = promptManager.combineTextAndImage(new InputPrompt(text));
        return openAIClient.chat("gpt-4-vision-preview", prompt, pageSnapshot);
    }

    public String createSolutionForTicket(TrackerClient trackerClient, String roleSpecific, String projectSpecific, TicketContext ticketContext) throws Exception {
        String prompt = promptManager.saCreateSolutionForTicket(new MultiTicketsPrompt(trackerClient.getBasePath(), roleSpecific, projectSpecific, ticketContext));
        return openAIClient.chat(prompt);
    }

    public String buildJQLForContent(TrackerClient trackerClient, String roleSpecific, String projectSpecific, TicketContext ticketContext) throws Exception {
        String requestToCreateJQL = promptManager.baBuildJqlForRequirementsSearching(new MultiTicketsPrompt(trackerClient.getBasePath(), roleSpecific, projectSpecific, ticketContext));
        String jqlToSearch = openAIClient.chat(requestToCreateJQL);
        logger.info(jqlToSearch);
        return jqlToSearch;
    }

    public boolean baIsTicketRelatedToContent(TrackerClient trackerClient, String roleSpecific, String projectSpecific, TicketContext ticketContext, ITicket content) throws Exception {
        MultiTicketsPrompt multiTicketsPrompt = new MultiTicketsPrompt(trackerClient.getBasePath(), roleSpecific, projectSpecific, ticketContext);
        multiTicketsPrompt.setContent(content);
        String prompt = promptManager.baIsTicketRelatedToContent(multiTicketsPrompt);
        return openAIClient.chatAsBoolean(prompt);
    }

    public String buildPageWithRequirementsForInputData(TicketContext ticketContext, String roleSpecific, String projectSpecific, String existingContent, ITicket content) throws Exception {
        MultiTicketsPrompt multiTicketsPrompt = new MultiTicketsPrompt(trackerClient.getBasePath(), roleSpecific, projectSpecific, ticketContext, existingContent);
        multiTicketsPrompt.setContent(content);
        String aiRequest = promptManager.baCollectRequirementsForTicket(multiTicketsPrompt);
        return openAIClient.chat(aiRequest);
    }

    public List<Diagram> createDiagrams(TicketContext ticketContext, String roleSpecific, String projectSpecific) throws Exception {
        MultiTicketsPrompt multiTicketsPrompt = new MultiTicketsPrompt(trackerClient.getBasePath(), roleSpecific, projectSpecific, ticketContext);
        String aiRequest = promptManager.createDiagrams(multiTicketsPrompt);
        return JSONModel.convertToModels(Diagram.class, openAIClient.chatAsJSONArray(aiRequest));
    }

    public String makeDailyScrumReportOfUserWork(String userName, List<com.github.istin.dmtools.sm.Change> changeList) throws Exception {
        String aiRequest = promptManager.makeDailyScrumReportOfUserWork(new ScrumDailyPrompt(userName, changeList));
        return openAIClient.chat(aiRequest);
    }

    public String makeResponseOnRequest(TicketContext ticketContext, String projectContext, String request) throws Exception {
        ITicket ticket = ticketContext.getTicket();
        List<IComment> comments = (List<IComment>) trackerClient.getComments(ticket.getKey(), ticket);
        ExpertPrompt expertPrompt = new ExpertPrompt(trackerClient.getBasePath(), ticketContext, projectContext, request);
        expertPrompt.setComments(comments);
        String aiRequest = promptManager.askExpert(expertPrompt);
        String chatResponse = openAIClient.chat(aiRequest);
        if (trackerClient.getTextType() == TrackerClient.TextType.MARKDOWN) {
            chatResponse = StringUtils.convertToMarkdown(chatResponse);
        }
        return chatResponse;
    }

    public String generateUnitTest(String fileContent, String className, String packageName, String testTemplate, UnitTestsGeneratorParams params) throws Exception {
        TestGeneration testGeneration = new TestGeneration(fileContent, className, packageName, testTemplate, params);
        String finalAIRequest = promptManager.requestTestGeneration(testGeneration);
        String finalResponse = openAIClient.chat(
                CODE_AI_MODEL,
                finalAIRequest);
        List<String> codeExamples = AIResponseParser.parseCodeExamples(finalResponse);
        return codeExamples.get(0);
    }
}
