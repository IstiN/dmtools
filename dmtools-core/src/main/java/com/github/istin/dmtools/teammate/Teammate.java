package com.github.istin.dmtools.teammate;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.ai.ChunkPreparation;
import com.github.istin.dmtools.ai.TicketContext;
import com.github.istin.dmtools.ai.agent.GenericRequestAgent;
import com.github.istin.dmtools.ai.agent.RequestDecompositionAgent;
import com.github.istin.dmtools.ai.agent.SourceImpactAssessmentAgent;
import com.github.istin.dmtools.atlassian.confluence.Confluence;
import com.github.istin.dmtools.atlassian.jira.JiraClient;
import com.github.istin.dmtools.atlassian.jira.model.Fields;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.config.ApplicationConfiguration;
import com.github.istin.dmtools.common.model.IAttachment;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.common.utils.StringUtils;
import com.github.istin.dmtools.context.ContextOrchestrator;
import com.github.istin.dmtools.context.UriToObject;
import com.github.istin.dmtools.context.UriToObjectFactory;
import com.github.istin.dmtools.di.AIAgentsModule;
import com.github.istin.dmtools.di.DaggerTeammateComponent;
import com.github.istin.dmtools.di.ServerManagedIntegrationsModule;
import com.github.istin.dmtools.di.TeammateComponent;
import com.github.istin.dmtools.expert.ExpertParams;
import com.github.istin.dmtools.job.*;
import com.github.istin.dmtools.prompt.IPromptTemplateReader;
import com.github.istin.dmtools.search.CodebaseSearchOrchestrator;
import com.github.istin.dmtools.search.ConfluenceSearchOrchestrator;
import com.github.istin.dmtools.search.TrackerSearchOrchestrator;
import com.google.gson.annotations.SerializedName;
import dagger.Component;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Teammate extends AbstractJob<Teammate.TeammateParams, List<ResultItem>> {

    private static final Logger logger = LogManager.getLogger(Teammate.class);

    @Getter
    @Setter
    public static class TeammateParams extends JobTrackerParams<RequestDecompositionAgent.Result> {

        @SerializedName("hooksAsContext")
        private String[] hooksAsContext;

        @SerializedName("cliCommands")
        private String[] cliCommands;

        @SerializedName("skipAIProcessing")
        private boolean skipAIProcessing = false;

    }

    @Inject
    TrackerClient<? extends ITicket> trackerClient;

    @Inject
    Confluence confluence;

    @Inject
    @Getter
    AI ai;

    @Inject
    IPromptTemplateReader promptTemplateReader;

    @Inject
    List<SourceCode> sourceCodes;

    @Inject
    SourceImpactAssessmentAgent sourceImpactAssessmentAgent;

    @Inject
    RequestDecompositionAgent requestDecompositionAgent;

    @Inject
    GenericRequestAgent genericRequestAgent;

    @Inject
    ApplicationConfiguration configuration;

    List<CodebaseSearchOrchestrator> listOfCodebaseSearchOrchestrator = new ArrayList<>();

    @Inject
    ConfluenceSearchOrchestrator confluenceSearchOrchestrator; // Temporarily disabled

    @Inject
    TrackerSearchOrchestrator trackerSearchOrchestrator;

    @Inject
    ContextOrchestrator contextOrchestrator;

    @Inject
    UriToObjectFactory uriToObjectFactory;

    // JavaScript bridge is now inherited from AbstractJob

    private static TeammateComponent teammateComponent;

    /**
     * Server-managed Dagger component that uses pre-resolved integrations
     * Includes ServerManagedIntegrationsModule for integrations and AIAgentsModule for agents
     */
    @Singleton
    @Component(modules = {ServerManagedIntegrationsModule.class, AIAgentsModule.class})
    public interface ServerManagedExpertComponent {
        void inject(Teammate expert);
    }

    /**
     * Creates a new Expert instance with the default configuration
     */
    public Teammate() {
        this(null);
    }

    /**
     * Creates a new Expert instance with the provided configuration
     *
     * @param configuration The application configuration to use
     */
    public Teammate(ApplicationConfiguration configuration) {
        // Don't initialize here - will be done in initializeForMode based on execution mode
    }

    @Override
    protected void initializeStandalone() {
        logger.info("Initializing Teammate in STANDALONE mode using TeammateComponent with BasicGeminiAI");
        
        // Use existing Dagger component for standalone mode
        if (teammateComponent == null) {
            logger.info("Creating new DaggerTeammateComponent for standalone mode");
            teammateComponent = DaggerTeammateComponent.create();
        }
        
        logger.info("Injecting dependencies using TeammateComponent");
        teammateComponent.inject(this);
        
        logger.info("Teammate standalone initialization completed - AI type: {}", 
                   (ai != null ? ai.getClass().getSimpleName() : "null"));
        
        // TeamAssistantAgent is now automatically injected by Dagger
    }

    @Override
    protected void initializeServerManaged(JSONObject resolvedIntegrations) {
        logger.info("Initializing Teammate in SERVER_MANAGED mode using ServerManagedIntegrationsModule");
        logger.info("Resolved integrations: {}", 
                   (resolvedIntegrations != null ? resolvedIntegrations.length() + " integrations" : "null"));
        
        // Create dynamic component with pre-resolved integrations
        try {
            logger.info("Creating ServerManagedIntegrationsModule with resolved credentials");
            ServerManagedIntegrationsModule module = new ServerManagedIntegrationsModule(resolvedIntegrations);
            
            logger.info("Building ServerManagedExpertComponent for Teammate");
            ServerManagedExpertComponent component = DaggerTeammate_ServerManagedExpertComponent.builder()
                    .serverManagedIntegrationsModule(module)
                    .build();
            
            logger.info("Injecting dependencies using ServerManagedExpertComponent");
            component.inject(this);
            
            logger.info("Teammate server-managed initialization completed - AI type: {}", 
                       (ai != null ? ai.getClass().getSimpleName() : "null"));
            
            // TeamAssistantAgent is now automatically injected by Dagger with server-managed dependencies
        } catch (Exception e) {
            logger.error("Failed to initialize Teammate in server-managed mode: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Teammate in server-managed mode", e);
        }
    }

    @Override
    protected List<ResultItem> runJobImpl(TeammateParams expertParams) throws Exception {
        ExpertParams.OutputType outputType = expertParams.getOutputType();
        String initiator = expertParams.getInitiator();
        String inputJQL = expertParams.getInputJql();
        String fieldName = expertParams.getFieldName();

        // Use injected UriToObjectFactory to create URI processing sources
        List<? extends UriToObject> uriProcessingSources;
        try {
            uriProcessingSources = uriToObjectFactory.createUriProcessingSources();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create URI processing sources", e);
        }

        RequestDecompositionAgent.Result inputParams = expertParams.getAgentParams();
        inputParams.setAiRole(extractIfNeeded(inputParams.getAiRole()));
        inputParams.setInstructions(new String[] {extractIfNeeded(inputParams.getInstructions())});
        inputParams.setFormattingRules(extractIfNeeded(inputParams.getFormattingRules()));
        inputParams.setFewShots(extractIfNeeded(inputParams.getFewShots()));
        inputParams.setQuestions(new String[] {extractIfNeeded(inputParams.getQuestions())});
        inputParams.setTasks(new String[] {extractIfNeeded(inputParams.getTasks())});

        contextOrchestrator.processUrisInContent(inputParams.getKnownInfo(), uriProcessingSources, 2);
        String processedKnownInfo = contextOrchestrator.summarize().toString();
        inputParams.setKnownInfo(processedKnownInfo);
        contextOrchestrator.clear();

        List<ResultItem> results = new ArrayList<>();
        trackerClient.searchAndPerform(ticket -> {
            long overallStart = System.currentTimeMillis();
            logger.info("Processing ticket: {}", ticket.getKey());
            
            // Create and prepare ticket context
            TicketContext ticketContext = new TicketContext(trackerClient, ticket);
            ticketContext.prepareContext(true);
            
            // Get attachments and convert to text
            List<? extends IAttachment> attachments = ticket.getAttachments();
            String ticketText = ticketContext.toText();
            
            // Process content with ContextOrchestrator
            long contextStart = System.currentTimeMillis();
            contextOrchestrator.processFullContent(ticket.getKey(), ticketText, (UriToObject) trackerClient, uriProcessingSources, expertParams.getTicketContextDepth());
            
            String textFieldsOnly = trackerClient.getTextFieldsOnly(ticket);
            contextOrchestrator.processUrisInContent(textFieldsOnly, uriProcessingSources, 1);
            contextOrchestrator.processUrisInContent(attachments, uriProcessingSources, 1);
            
            List<ChunkPreparation.Chunk> chunksContext = contextOrchestrator.summarize();
            long contextDuration = System.currentTimeMillis() - contextStart;
            logger.debug("Context processing took {}ms for {}", contextDuration, ticket.getKey());
            
            inputParams.setKnownInfo(inputParams.getKnownInfo() + "\n" + chunksContext.toString());
            contextOrchestrator.clear();
            
            long processingDuration = System.currentTimeMillis() - overallStart;
            logger.debug("Ticket context preparation took {}ms for {}", processingDuration, ticket.getKey());

            // Process hooks as context first
            String[] hooksAsContext = expertParams.getHooksAsContext();
            StringBuilder globalHooksResponses = new StringBuilder();
            if (hooksAsContext != null && sourceCodes != null) {
                for (String hook : hooksAsContext) {
                    for (SourceCode sourceCode : sourceCodes) {
                        try {
                            String response = sourceCode.callHookAndWaitResponse(hook, inputParams.toString());
                            if (response != null) {
                                globalHooksResponses.append("Tools Information (").append(hook).append("):\n");
                                globalHooksResponses.append(response).append("\n\n");
                            }
                        } catch (Exception e) {
                            // Log but don't fail the workflow
                            System.err.println("Failed to call hook: " + hook + ", error: " + e.getMessage());
                        }
                    }
                }
            }

            // Append hooks responses to knownInfo
            if (!globalHooksResponses.isEmpty()) {
                inputParams.setKnownInfo(inputParams.getKnownInfo() + "\n\nAdditional Context:\n" + globalHooksResponses);
            }

            // Process CLI commands if configured
            String[] cliCommands = expertParams.getCliCommands();
            CliExecutionHelper cliHelper = new CliExecutionHelper();
            CliExecutionHelper.CliExecutionResult cliResult = null;
            Path inputContextPath = null;
            
            if (cliCommands != null && cliCommands.length > 0) {
                try {
                    // Create input context for CLI commands
                    inputContextPath = cliHelper.createInputContext(ticket, inputParams.toString(), trackerClient);
                    
                    // Execute CLI commands from project root directory (where cursor-agent can find workspace config)
                    Path projectRoot = Paths.get(System.getProperty("user.dir"));
                    cliResult = cliHelper.executeCliCommandsWithResult(cliCommands, projectRoot);
                    
                    // Append CLI responses to knownInfo if not empty
                    StringBuilder cliResponses = cliResult.getCommandResponses();
                    if (!cliResponses.isEmpty()) {
                        String cliContent = cliResponses.toString();
                        // Include output response if available
                        if (cliResult.hasOutputResponse()) {
                            cliContent += cliResult.getOutputResponse() + "\n\n";
                        }
                        inputParams.setKnownInfo(inputParams.getKnownInfo() + "\n\nCLI Execution Results:\n" + cliContent);
                    }
                    
                } catch (Exception e) {
                    logger.error("Failed to execute CLI commands for ticket {}: {}", ticket.getKey(), e.getMessage(), e);
                    // Create error result for consistent handling below
                    StringBuilder errorResponse = new StringBuilder("CLI Execution Error: ").append(e.getMessage()).append("\n");
                    cliResult = new CliExecutionHelper.CliExecutionResult(errorResponse, null);
                } finally {
                    // Clean up input context
                    if (inputContextPath != null) {
                        cliHelper.cleanupInputContext(inputContextPath);
                    }
                }
            }

            String response;
            if (expertParams.isSkipAIProcessing()) {
                // Skip AI processing and use CLI output response if available
                if (cliResult != null && cliResult.hasOutputResponse()) {
                    response = cliResult.getOutputResponse();
                    logger.info("Using CLI output response as final response for ticket {}", ticket.getKey());
                } else if (cliResult != null) {
                    response = cliResult.getCommandResponses().toString();
                    logger.info("Using CLI execution results as final response for ticket {}", ticket.getKey());
                } else {
                    response = "No CLI commands executed or results available.";
                    logger.info("No CLI results available for ticket {}", ticket.getKey());
                }
            } else {
                // Standard AI processing workflow
                GenericRequestAgent.Params genericRequesAgentParams = new GenericRequestAgent.Params(inputParams, null, null, expertParams.getChunkProcessingTimeoutInMinutes() * 60 * 1000);
                response = genericRequestAgent.run(genericRequesAgentParams);
            }
            js(expertParams.getPostJSAction())
                .mcp(trackerClient, ai, confluence, null) // sourceCode not available in Teammate context
                .withJobContext(expertParams, ticket, response)
                .with(TrackerParams.INITIATOR, initiator)
                .execute();
            if (expertParams.isAttachResponseAsFile()) {
                attachResponse(genericRequestAgent, "_final_answer.txt", response, ticket.getKey(), "text/plain");
            }
            
            // Handle output based on outputType, skip publishing if outputType is 'none'
            if (outputType != Params.OutputType.none) {
                if (outputType == Params.OutputType.field) {
                    if (trackerClient instanceof JiraClient<?>) {
                        String fieldCustomCode = ((JiraClient<?>) trackerClient).getFieldCustomCode(ticket.getTicketKey().split("-")[0], fieldName);
                        String currentFieldValue = ticket.getFields().getString(fieldCustomCode);
                        if (expertParams.getOperationType() == Params.OperationType.Append) {
                            trackerClient.updateTicket(ticket.getTicketKey(), fields -> fields.set(fieldCustomCode, currentFieldValue + "\n\n" + response));
                        } else if (expertParams.getOperationType() == Params.OperationType.Replace) {
                            trackerClient.updateTicket(ticket.getTicketKey(), fields -> fields.set(fieldCustomCode, response));
                        }
                        if (initiator != null && !initiator.isEmpty()) {
                            trackerClient.postComment(ticket.getTicketKey(), trackerClient.tag(initiator) + ", \n\n AI response in '" + fieldName + "' on your request.");
                        }
                    } else {
                        throw new UnsupportedOperationException("the operation to set value to field was tested only with jira client");
                    }
                } else {
                    trackerClient.postCommentIfNotExists(ticket.getTicketKey(), trackerClient.tag(initiator) + ", \n\nAI Response is: \n" + response);
                }
            } else {
                logger.info("Output type is 'none', skipping publishing results for ticket {}", ticket.getKey());
            }
            results.add(new ResultItem(ticket.getTicketKey(), response));
            return false;
        }, inputJQL, trackerClient.getExtendedQueryFields());
        return results;
    }

    private String extractIfNeeded(String... inputArray) throws IOException {
        if (inputArray == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (String input : inputArray) {
            if (!result.isEmpty()) {
                result.append("\n");
            }
            if (input != null && input.startsWith("https://")) {
                String value = confluence.contentByUrl(input).getStorage().getValue();
                if (StringUtils.isConfluenceYamlFormat(value)) {
                    input = StringUtils.extractYamlContentFromConfluence(value);
                } else {
                    input = value;
                }
            }
            result.append(input);
        }
        return result.toString();
    }


    public void attachResponse(Object orchestratorClass, String file, String result, String ticketKey, String contentType) throws IOException {
        String fileNameResult = orchestratorClass.getClass().getSimpleName() + file;
        String[] fields = {Fields.ATTACHMENT, Fields.SUMMARY};
        ITicket t = trackerClient.performTicket(ticketKey, fields);
        List<? extends IAttachment> attachments = t.getAttachments();
        fileNameResult = IAttachment.Utils.generateUniqueFileName(fileNameResult, attachments);

        File tempFileResult = File.createTempFile(fileNameResult, null);

        // Write JSON to file using Commons IO
        FileUtils.writeStringToFile(tempFileResult, result, "UTF-8");

        // Attach file to ticket
        trackerClient.attachFileToTicket(
                ticketKey,
                fileNameResult,
                contentType,
                tempFileResult
        );
        // Clean up temp file
        FileUtils.deleteQuietly(tempFileResult);
    }

}