package com.github.istin.dmtools.ai;

import com.github.istin.dmtools.atlassian.bitbucket.Bitbucket;
import com.github.istin.dmtools.atlassian.bitbucket.model.File;
import com.github.istin.dmtools.atlassian.bitbucket.model.PullRequest;
import com.github.istin.dmtools.atlassian.jira.model.IssueType;
import com.github.istin.dmtools.atlassian.jira.utils.IssuesIDsParser;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.openai.OpenAIClient;
import com.github.istin.dmtools.openai.PromptManager;
import com.github.istin.dmtools.openai.input.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JAssistant {

    private static final Logger logger = LogManager.getLogger(JAssistant.class);
    public static final String ME = JAssistant.class.getSimpleName();
    public static final String TEST_CASES_COMMENT_PREFIX = "<p>JAI Generated Test Cases: </p>";

    private TrackerClient<? extends ITicket> trackerClient;

    private Bitbucket sourceCode;

    private OpenAIClient openAIClient;

    private PromptManager promptManager;

    private ConversationObserver conversationObserver;

    public ConversationObserver getConversationObserver() {
        return conversationObserver;
    }

    public void setConversationObserver(ConversationObserver conversationObserver) {
        this.conversationObserver = conversationObserver;
    }

    public JAssistant(TrackerClient<? extends ITicket> trackerClient, Bitbucket sourceCode, OpenAIClient openAIClient, PromptManager promptManager) {
        this(trackerClient, sourceCode, openAIClient, promptManager, null);
    }

    public JAssistant(TrackerClient<? extends ITicket> trackerClient, Bitbucket sourceCode, OpenAIClient openAIClient, PromptManager promptManager, ConversationObserver conversationObserver) {
        this.trackerClient = trackerClient;
        this.sourceCode = sourceCode;
        this.openAIClient = openAIClient;
        this.promptManager = promptManager;
        this.conversationObserver = conversationObserver;
    }

    public void generateCode(String role, String key, String workspace, String repository, String branchName) throws Exception {
        ITicket ticket = trackerClient.performTicket(key, trackerClient.getExtendedQueryFields());
        CodeGeneration codeGeneration = new CodeGeneration(role, ticket);
        if (!IssueType.isBug(ticket.getIssueType())) {
            List<? extends ITicket> testCases = trackerClient.getTestCases(ticket);
            codeGeneration.setTestCases(testCases);
        }

        List<File> listOfFiles = sourceCode.getListOfFiles(workspace, repository, branchName);
        List<File> filesOnly = listOfFiles.stream().filter(file -> !file.isDir()).collect(Collectors.toList());
        codeGeneration.setFiles(filesOnly);
        String aiRequest = promptManager.checkPotentiallyEffectedFilesForTicket(codeGeneration);
        String response = openAIClient.chat(aiRequest);
        logger.info(response);
        JSONArray objects = new JSONArray(response);
        List<File> finalResults = new ArrayList<>();
        for (int i = 0; i < objects.length(); i++) {
            String path = objects.getString(i);
            List<File> result = filesOnly.stream().filter(file -> file.getPath().equals(path)).collect(Collectors.toList());
            if (!result.isEmpty()) {
                File file = result.get(0);
                file.setFileContent(sourceCode.getFileContent(file.getSelfLink()));

                String request = promptManager.validatePotentiallyEffectedFile(new TicketFilePrompt(role, ticket, file));
                String isTheFileUsefull = openAIClient.chat("gpt-35-turbo", request);
                if (Boolean.parseBoolean(isTheFileUsefull)) {
                    finalResults.add(file);
                }
            }
        }

        codeGeneration.setFiles(finalResults);
        String finalAIRequest = promptManager.requestGenerateCodeForTicket(codeGeneration);
        String finalResponse = openAIClient.chat(finalAIRequest);
        logger.info("----- FINAL AI RESPONSE ---- ");
        logger.info(finalResponse);
        String htmlConversation = promptManager.convertToHTML(new InputPrompt(finalResponse));
        finalResponse = openAIClient.chat(htmlConversation);
        trackerClient.postComment(key, "<p>JAI Generated Code: </p>" + finalResponse);
    }

    private void trackConversation(String author, String text) {
        if (conversationObserver != null) {
            conversationObserver.addMessage(new ConversationObserver.Message(author, text));
        }
    }

    public void generateTestCases(String key) throws Exception {
        ITicket ticket = trackerClient.performTicket(key, trackerClient.getExtendedQueryFields());
        String message = TrackerClient.Utils.checkCommentStartedWith(trackerClient, ticket.getKey(), ticket, TEST_CASES_COMMENT_PREFIX);
        if (message != null) {
            return;
        }

        String aiRequest = promptManager.requestTestCasesForStory(new TicketBasedPrompt(ticket));
        String response = openAIClient.chat(aiRequest);
        trackerClient.postComment(key, TEST_CASES_COMMENT_PREFIX + response);
    }

    public void generateNiceLookingStoryInGherkinStyleAndPotentialQuestionsToPO(String key) throws Exception {
        ITicket ticket = trackerClient.performTicket(key, trackerClient.getExtendedQueryFields());
        String aiRequest = promptManager.requestNiceLookingStoryInGherkinStyleAndPotentialQuestionsToPO(new TicketBasedPrompt(ticket));
        String response = openAIClient.chat(aiRequest);
        response = openAIClient.chat(promptManager.convertToHTML(new InputPrompt(response)));
        trackerClient.postComment(key, "<p>JAI Generated Nice Looking Story In Gherkin Style And Potential Questions To PO: </p>" + response);
    }

    public String checkStoryIsTechnicalOrProduct(ITicket ticket) throws Exception {
        String aiRequest = promptManager.checkTaskTechnicalOrProduct(new TicketBasedPrompt(ticket));
        return openAIClient.chat(aiRequest);
    }

    public String checkStoryAreas(ITicket ticket, String[] areas) throws Exception {
        String aiRequest = promptManager.checkStoryAreas(new BAStoryAreaPrompt(ticket, areas));
        return openAIClient.chat(aiRequest);
    }

    public String buildNiceLookingDocumentationForStory(ITicket ticket, String existingContent) throws Exception {
        String aiRequest = promptManager.buildNiceLookingDocumentation(new NiceLookingDocumentationPrompt(ticket, existingContent));
        return openAIClient.chat(aiRequest);
    }

    public Double estimateStory(String role, String key, List<? extends ITicket> existingStories, boolean isCheckDetailsOfStory) throws Exception {
        ITicket ticket = trackerClient.performTicket(key, trackerClient.getExtendedQueryFields());
        String aiRequest = promptManager.checkSimilarStories(new BASimilarStoriesPrompt(ticket, existingStories));
        String response = openAIClient.chat(aiRequest);
        JSONArray array = new JSONArray(response);
        List<ITicket> finalResults = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            String similarKey = array.getString(i);
            ITicket similarTicket = trackerClient.performTicket(similarKey, trackerClient.getExtendedQueryFields());
            if (isCheckDetailsOfStory) {
                BASimilarStoriesPrompt baSimilarStoriesPrompt = new BASimilarStoriesPrompt(role, ticket, similarTicket);
                String chatRequest = promptManager.validateSimilarStory(baSimilarStoriesPrompt);
                String isSimilarStory = openAIClient.chat(
                    "gpt-35-turbo",
                        chatRequest);
                if (Boolean.parseBoolean(isSimilarStory)) {
                    finalResults.add(similarTicket);
                }
            } else {
                finalResults.add(similarTicket);
            }
        }
        for (ITicket result : finalResults) {
            logger.log(Level.DEBUG,result.getTicketTitle() + " " + result.getWeight());
        }
        String finalAiRequest = promptManager.estimateStory(new BASimilarStoriesPrompt(ticket, finalResults));
        String finalEstimations = openAIClient.chat(finalAiRequest);
        return findFirstNumberInTheString(finalEstimations);
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
    public void reviewPullRequest(String role, String workspace, String repository, String pullRequestId, IssuesIDsParser issuesIDsParser) throws Exception {
        PullRequest pullRequest = sourceCode.pullRequest(workspace, repository, pullRequestId);
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
        PullRequestReview input = new PullRequestReview(role, ticket);
        input.setTicket(ticket);

        if (!IssueType.isBug(ticket.getIssueType())) {
            List<? extends ITicket> testCases = trackerClient.getTestCases(ticket);
            input.setTestCases(testCases);
        }

        input.setRole(role);
        String diff = sourceCode.getDiff(workspace, repository, pullRequestId);
        input.setDiff(diff);

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


}
