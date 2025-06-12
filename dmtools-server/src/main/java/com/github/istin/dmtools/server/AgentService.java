package com.github.istin.dmtools.server;

import com.github.istin.dmtools.ai.agent.*;
import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.context.ContextOrchestrator;
import com.github.istin.dmtools.dto.AgentExecutionRequest;
import com.github.istin.dmtools.dto.AgentExecutionResponse;
import com.github.istin.dmtools.dto.AgentInfo;
import com.github.istin.dmtools.dto.AgentListResponse;
// import com.github.istin.dmtools.orchestrator.confluence.ConfluenceSearchOrchestrator;
// import com.github.istin.dmtools.orchestrator.context.ContextOrchestrator;
// import com.github.istin.dmtools.orchestrator.jira.TrackerSearchOrchestrator;
// import com.github.istin.dmtools.orchestrator.test.RelatedTestCasesOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.istin.dmtools.presentation.PresentationMakerOrchestrator;
import com.github.istin.dmtools.search.ConfluenceSearchOrchestrator;
import com.github.istin.dmtools.search.TrackerSearchOrchestrator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AgentService {

    private static final Logger logger = LogManager.getLogger(AgentService.class);

    @Autowired
    private ObjectMapper objectMapper;

    // Agent beans - these will be injected by Spring from DI configuration
    @Autowired(required = false)
    private BusinessAreaAssessmentAgent businessAreaAssessmentAgent;
    
    @Autowired(required = false)
    private PresentationSlideFormatterAgent presentationSlideFormatterAgent;
    
    @Autowired(required = false)
    private PresentationContentGeneratorAgent presentationContentGeneratorAgent;
    
    @Autowired(required = false)
    private TaskProgressAgent taskProgressAgent;
    
    @Autowired(required = false)
    private TeamAssistantAgent teamAssistantAgent;
    
    @Autowired(required = false)
    private KeywordGeneratorAgent keywordGeneratorAgent;
    
    @Autowired(required = false)
    private AutomationTestingGeneratorAgent automationTestingGeneratorAgent;
    
    @Autowired(required = false)
    private TestCaseVisualizerAgent testCaseVisualizerAgent;
    
    @Autowired(required = false)
    private RelatedTestCaseAgent relatedTestCaseAgent;
    
    @Autowired(required = false)
    private SourceImpactAssessmentAgent sourceImpactAssessmentAgent;
    
    @Autowired(required = false)
    private TestCaseGeneratorAgent testCaseGeneratorAgent;
    
    @Autowired(required = false)
    private ContentMergeAgent contentMergeAgent;
    
    @Autowired(required = false)
    private SearchResultsAssessmentAgent searchResultsAssessmentAgent;
    
    @Autowired(required = false)
    private TaskExecutionAgent taskExecutionAgent;
    
    @Autowired(required = false)
    private SnippetExtensionAgent snippetExtensionAgent;
    
    @Autowired(required = false)
    private SummaryContextAgent summaryContextAgent;
    
    @Autowired(required = false)
    private RelatedTestCasesAgent relatedTestCasesAgent;

    @Autowired(required = false)
    private JSBridgeScriptGeneratorAgent jsBridgeScriptGeneratorAgent;

//     Orchestrator beans - temporarily disabled since classes don't exist
     @Autowired(required = false)
     private ContextOrchestrator contextOrchestrator;
    
     @Autowired(required = false)
     private PresentationMakerOrchestrator presentationMakerOrchestrator;
    
     @Autowired(required = false)
     private ConfluenceSearchOrchestrator confluenceSearchOrchestrator; // Temporarily disabled
    
     @Autowired(required = false)
     private TrackerSearchOrchestrator trackerSearchOrchestrator;

    public List<String> getAvailableAgents() {
        List<String> agents = new ArrayList<>();
        if (businessAreaAssessmentAgent != null) agents.add("BusinessAreaAssessmentAgent");
        if (presentationSlideFormatterAgent != null) agents.add("PresentationSlideFormatterAgent");
        if (presentationContentGeneratorAgent != null) agents.add("PresentationContentGeneratorAgent");
        if (taskProgressAgent != null) agents.add("TaskProgressAgent");
        if (teamAssistantAgent != null) agents.add("TeamAssistantAgent");
        if (keywordGeneratorAgent != null) agents.add("KeywordGeneratorAgent");
        if (automationTestingGeneratorAgent != null) agents.add("AutomationTestingGeneratorAgent");
        if (testCaseVisualizerAgent != null) agents.add("TestCaseVisualizerAgent");
        if (relatedTestCaseAgent != null) agents.add("RelatedTestCaseAgent");
        if (sourceImpactAssessmentAgent != null) agents.add("SourceImpactAssessmentAgent");
        if (testCaseGeneratorAgent != null) agents.add("TestCaseGeneratorAgent");
        if (contentMergeAgent != null) agents.add("ContentMergeAgent");
        if (searchResultsAssessmentAgent != null) agents.add("SearchResultsAssessmentAgent");
        if (taskExecutionAgent != null) agents.add("TaskExecutionAgent");
        if (snippetExtensionAgent != null) agents.add("SnippetExtensionAgent");
        if (summaryContextAgent != null) agents.add("SummaryContextAgent");
        if (relatedTestCasesAgent != null) agents.add("RelatedTestCasesAgent");
        if (jsBridgeScriptGeneratorAgent != null) agents.add("JSBridgeScriptGeneratorAgent");
        return agents;
    }

    public List<String> getAvailableOrchestrators() {
        List<String> orchestrators = new ArrayList<>();
         if (contextOrchestrator != null) orchestrators.add("ContextOrchestrator");
         if (presentationMakerOrchestrator != null) orchestrators.add("PresentationMakerOrchestrator");
         if (confluenceSearchOrchestrator != null) orchestrators.add("ConfluenceSearchOrchestrator"); // Temporarily disabled
         if (trackerSearchOrchestrator != null) orchestrators.add("TrackerSearchOrchestrator");
        return orchestrators;
    }

    public List<AgentInfo> getDetailedAgents() {
        List<AgentInfo> detailedAgents = new ArrayList<>();
        
        if (businessAreaAssessmentAgent != null) {
            detailedAgents.add(createBusinessAreaAssessmentAgentInfo());
        }
        if (presentationSlideFormatterAgent != null) {
            detailedAgents.add(createPresentationSlideFormatterAgentInfo());
        }
        if (presentationContentGeneratorAgent != null) {
            detailedAgents.add(createPresentationContentGeneratorAgentInfo());
        }
        if (taskProgressAgent != null) {
            detailedAgents.add(createTaskProgressAgentInfo());
        }
        if (teamAssistantAgent != null) {
            detailedAgents.add(createTeamAssistantAgentInfo());
        }
        if (keywordGeneratorAgent != null) {
            detailedAgents.add(createKeywordGeneratorAgentInfo());
        }
        if (automationTestingGeneratorAgent != null) {
            detailedAgents.add(createAutomationTestingGeneratorAgentInfo());
        }
        if (testCaseVisualizerAgent != null) {
            detailedAgents.add(createTestCaseVisualizerAgentInfo());
        }
        if (relatedTestCaseAgent != null) {
            detailedAgents.add(createRelatedTestCaseAgentInfo());
        }
        if (sourceImpactAssessmentAgent != null) {
            detailedAgents.add(createSourceImpactAssessmentAgentInfo());
        }
        if (testCaseGeneratorAgent != null) {
            detailedAgents.add(createTestCaseGeneratorAgentInfo());
        }
        if (contentMergeAgent != null) {
            detailedAgents.add(createContentMergeAgentInfo());
        }
        if (searchResultsAssessmentAgent != null) {
            detailedAgents.add(createSearchResultsAssessmentAgentInfo());
        }
        if (taskExecutionAgent != null) {
            detailedAgents.add(createTaskExecutionAgentInfo());
        }
        if (snippetExtensionAgent != null) {
            detailedAgents.add(createSnippetExtensionAgentInfo());
        }
        if (summaryContextAgent != null) {
            detailedAgents.add(createSummaryContextAgentInfo());
        }
        if (relatedTestCasesAgent != null) {
            detailedAgents.add(createRelatedTestCasesAgentInfo());
        }
        if (jsBridgeScriptGeneratorAgent != null) {
            detailedAgents.add(createJSBridgeScriptGeneratorAgentInfo());
        }
        
        return detailedAgents;
    }

    public List<AgentInfo> getDetailedOrchestrators() {
        List<AgentInfo> detailedOrchestrators = new ArrayList<>();
        
         if (contextOrchestrator != null) {
             detailedOrchestrators.add(createContextOrchestratorInfo());
         }
         if (presentationMakerOrchestrator != null) {
             detailedOrchestrators.add(createPresentationMakerOrchestratorInfo());
         }
         if (confluenceSearchOrchestrator != null) {
             detailedOrchestrators.add(createConfluenceSearchOrchestratorInfo());
         } // Temporarily disabled
         if (trackerSearchOrchestrator != null) {
             detailedOrchestrators.add(createTrackerSearchOrchestratorInfo());
         }
        
        return detailedOrchestrators;
    }

    public AgentListResponse getAgentsAndOrchestrators(boolean detailed) {
        if (detailed) {
            return AgentListResponse.detailed(getDetailedAgents(), getDetailedOrchestrators());
        } else {
            return AgentListResponse.simple(getAvailableAgents(), getAvailableOrchestrators());
        }
    }

    @SuppressWarnings("unchecked")
    public AgentExecutionResponse executeAgent(AgentExecutionRequest request) {
        try {
            logger.info("Executing agent: {}", request.getAgentName());
            
            IAgent<?, ?> agent = getAgentByName(request.getAgentName());
            if (agent == null) {
                return AgentExecutionResponse.error(request.getAgentName(), 
                    "Agent not found: " + request.getAgentName(), "agent");
            }

            // Convert parameters to appropriate type and execute
            Object params = convertParameters(request.getAgentName(), request.getParameters());
            Object result = ((IAgent<Object, Object>) agent).run(params);
            
            logger.info("Agent {} executed successfully", request.getAgentName());
            return AgentExecutionResponse.success(request.getAgentName(), result, "agent");
            
        } catch (Exception e) {
            logger.error("Error executing agent: {}", request.getAgentName(), e);
            return AgentExecutionResponse.error(request.getAgentName(), 
                "Failed to execute agent: " + e.getMessage(), "agent");
        }
    }

    public AgentExecutionResponse executeOrchestrator(AgentExecutionRequest request) {
        try {
            logger.info("Executing orchestrator: {}", request.getAgentName());
            
            Object result = executeOrchestratorByName(request.getAgentName(), request.getParameters());
            if (result == null) {
                return AgentExecutionResponse.error(request.getAgentName(), 
                    "Orchestrator not found: " + request.getAgentName(), "orchestrator");
            }
            
            logger.info("Orchestrator {} executed successfully", request.getAgentName());
            return AgentExecutionResponse.success(request.getAgentName(), result, "orchestrator");
            
        } catch (Exception e) {
            logger.error("Error executing orchestrator: {}", request.getAgentName(), e);
            return AgentExecutionResponse.error(request.getAgentName(), 
                "Failed to execute orchestrator: " + e.getMessage(), "orchestrator");
        }
    }

    private IAgent<?, ?> getAgentByName(String agentName) {
        return switch (agentName) {
            case "BusinessAreaAssessmentAgent" -> businessAreaAssessmentAgent;
            case "PresentationSlideFormatterAgent" -> presentationSlideFormatterAgent;
            case "PresentationContentGeneratorAgent" -> presentationContentGeneratorAgent;
            case "TaskProgressAgent" -> taskProgressAgent;
            case "TeamAssistantAgent" -> teamAssistantAgent;
            case "KeywordGeneratorAgent" -> keywordGeneratorAgent;
            case "AutomationTestingGeneratorAgent" -> automationTestingGeneratorAgent;
            case "TestCaseVisualizerAgent" -> testCaseVisualizerAgent;
            case "RelatedTestCaseAgent" -> relatedTestCaseAgent;
            case "SourceImpactAssessmentAgent" -> sourceImpactAssessmentAgent;
            case "TestCaseGeneratorAgent" -> testCaseGeneratorAgent;
            case "ContentMergeAgent" -> contentMergeAgent;
            case "SearchResultsAssessmentAgent" -> searchResultsAssessmentAgent;
            case "TaskExecutionAgent" -> taskExecutionAgent;
            case "SnippetExtensionAgent" -> snippetExtensionAgent;
            case "SummaryContextAgent" -> summaryContextAgent;
            case "RelatedTestCasesAgent" -> relatedTestCasesAgent;
            case "JSBridgeScriptGeneratorAgent" -> jsBridgeScriptGeneratorAgent;
            default -> null;
        };
    }

    private Object executeOrchestratorByName(String orchestratorName, Map<String, Object> parameters) throws Exception {
        return switch (orchestratorName) {
            case "ContextOrchestrator" -> {
                 if (contextOrchestrator != null) {
                     // For demonstration - orchestrators might need specific method calls
                     yield "ContextOrchestrator executed with parameters: " + parameters;
                 }
                yield null;
            }
            case "PresentationMakerOrchestrator" -> {
                 if (presentationMakerOrchestrator != null) {
                     // Convert parameters to PresentationMakerOrchestrator.Params
                     PresentationMakerOrchestrator.Params params = convertToPresentationParams(parameters);
                     yield presentationMakerOrchestrator.createPresentation(params);
                 }
                yield null;
            }
            // Temporarily disabled due to ClassCastException
            case "ConfluenceSearchOrchestrator" -> {
                if (confluenceSearchOrchestrator != null) {
                    // Extract search parameters
                    String task = (String) parameters.get("task");
                    String blacklist = (String) parameters.getOrDefault("blacklist", "");
                    int itemsLimit = (Integer) parameters.getOrDefault("itemsLimit", 10);
                    int iterations = (Integer) parameters.getOrDefault("iterations", 1);
                    yield confluenceSearchOrchestrator.run(task, blacklist, itemsLimit, iterations);
                }
                yield null;
            }
            case "TrackerSearchOrchestrator" -> {
                 if (trackerSearchOrchestrator != null) {
                     // Extract search parameters
                     String task = (String) parameters.get("task");
                     String blacklist = (String) parameters.getOrDefault("blacklist", "");
                     int itemsLimit = (Integer) parameters.getOrDefault("itemsLimit", 10);
                     int iterations = (Integer) parameters.getOrDefault("iterations", 1);
                     yield trackerSearchOrchestrator.run(task, blacklist, itemsLimit, iterations);
                 }
                yield null;
            }
            default -> null;
        };
    }

    private Object convertParameters(String agentName, Map<String, Object> parameters) {
        try {
            IAgent<?, ?> agent = getAgentByName(agentName);
            if (agent != null) {
                Class<?> paramsClass = agent.getParamsClass();
                if (paramsClass != null) {
                    return objectMapper.convertValue(parameters, paramsClass);
                }
            }
            logger.warn("Could not determine parameter class for agent {}, using raw map.", agentName);
            return parameters;
        } catch (Exception e) {
            logger.warn("Failed to convert parameters for agent {}, using raw parameters. Error: {}", agentName, e.getMessage());
            return parameters;
        }
    }

     private PresentationMakerOrchestrator.Params convertToPresentationParams(Map<String, Object> parameters) {
         // Convert generic Map to specific Params object using ObjectMapper
         return objectMapper.convertValue(parameters, PresentationMakerOrchestrator.Params.class);
     }

    // Private methods for creating detailed agent info
    private AgentInfo createBusinessAreaAssessmentAgentInfo() {
        return AgentInfo.agent("BusinessAreaAssessmentAgent", 
                "Analyzes and assesses business domain and requirements based on story description",
                "Analysis")
            .withParameters(List.of(
                new AgentInfo.ParameterInfo("storyDescription", "String", 
                    "Description of the business story or requirement to analyze", 
                    true, null, null, "User authentication feature for mobile app")
            ))
            .withReturnInfo(new AgentInfo.ReturnInfo("String", 
                "Business area name or category", null, "User Management"));
    }

    private AgentInfo createPresentationSlideFormatterAgentInfo() {
        return AgentInfo.agent("PresentationSlideFormatterAgent",
                "Formats and structures presentation slides with proper layout and design",
                "Presentation")
            .withParameters(List.of(
                new AgentInfo.ParameterInfo("slidesContent", "JSONArray",
                    "JSON array of slides to be formatted",
                    true, null, null, "[{\"title\": \"Slide 1\", \"content\": \"...\"}]")
            ))
            .withReturnInfo(new AgentInfo.ReturnInfo("JSONArray",
                "Formatted presentation slides as a JSON array", null, "[{\"title\": \"Formatted Slide 1\", \"content\": \"...\"}]"));
    }

    private AgentInfo createPresentationContentGeneratorAgentInfo() {
        return AgentInfo.agent("PresentationContentGeneratorAgent",
                "Generates comprehensive presentation content based on topic and audience",
                "Presentation")
            .withParameters(List.of(
                new AgentInfo.ParameterInfo("topic", "String",
                    "Main topic of the presentation",
                    true, null, null, "AI Integration in Enterprise"),
                new AgentInfo.ParameterInfo("audience", "String",
                    "Target audience for the presentation",
                    true, null, null, "Technical Team"),
                new AgentInfo.ParameterInfo("userRequest", "String",
                    "Specific user requirements or requests",
                    false, null, null, "Include technical implementation details"),
                new AgentInfo.ParameterInfo("additionalData", "String",
                    "Additional context or data to include",
                    false, null, null, "Recent project statistics")
            ))
            .withReturnInfo(new AgentInfo.ReturnInfo("JSONArray",
                "Array of slides with content", null, "[{\"title\": \"Slide 1\", \"content\": \"...\"}]"));
    }

    private AgentInfo createTaskProgressAgentInfo() {
        return AgentInfo.agent("TaskProgressAgent",
                "Tracks and reports on task progress with detailed step analysis",
                "Project Management")
            .withParameters(List.of(
                new AgentInfo.ParameterInfo("steps", "String",
                    "JSON string describing the task steps",
                    true, null, null, "[{\"id\": 1, \"name\": \"Setup\", \"status\": \"completed\"}]"),
                new AgentInfo.ParameterInfo("previousAssessment", "String",
                    "Previous progress assessment",
                    false, null, null, "Last week: 60% complete"),
                new AgentInfo.ParameterInfo("requireExplanation", "Boolean",
                    "Whether to include detailed explanation",
                    false, false, null, "true"),
                new AgentInfo.ParameterInfo("files", "List<File>",
                    "Related files for analysis",
                    false, null, null, null)
            ))
            .withReturnInfo(new AgentInfo.ReturnInfo("JSONObject",
                "Progress report with completed steps, next steps, and explanation",
                Map.of("completedSteps", "JSONArray", "nextSteps", "JSONArray", "explanation", "String"),
                "{\"completedSteps\": [...], \"nextSteps\": [...], \"explanation\": \"...\"}"));
    }

    private AgentInfo createTeamAssistantAgentInfo() {
        return AgentInfo.agent("TeamAssistantAgent",
                "Provides comprehensive team coordination and assistance based on decomposed requests",
                "Team Management")
            .withParameters(List.of(
                new AgentInfo.ParameterInfo("request", "RequestDecompositionAgent.Result",
                    "Decomposed user request with structured information",
                    true, null, null, null),
                new AgentInfo.ParameterInfo("files", "List<File>",
                    "Related files for context",
                    false, null, null, null),
                new AgentInfo.ParameterInfo("chunks", "List<ChunkPreparation.Chunk>",
                    "Text chunks for processing",
                    false, null, null, null),
                new AgentInfo.ParameterInfo("chunksProcessingTimeout", "Long",
                    "Timeout for chunk processing in milliseconds",
                    false, 0L, null, "30000")
            ))
            .withReturnInfo(new AgentInfo.ReturnInfo("String",
                "HTML formatted team assistance response", null, "<html>...</html>"));
    }

    private AgentInfo createKeywordGeneratorAgentInfo() {
        return AgentInfo.agent("KeywordGeneratorAgent",
                "Generates relevant keywords for search optimization and content discovery",
                "Search & Keywords")
            .withParameters(List.of(
                new AgentInfo.ParameterInfo("searchContext", "String",
                    "Context for keyword generation",
                    true, null, null, "Software development documentation"),
                new AgentInfo.ParameterInfo("task", "String",
                    "Specific task or goal for keyword generation",
                    true, null, null, "Find API integration guides"),
                new AgentInfo.ParameterInfo("blacklist", "String",
                    "Keywords to exclude from results",
                    false, null, null, "deprecated,old,legacy")
            ))
            .withReturnInfo(new AgentInfo.ReturnInfo("JSONArray",
                "Array of generated keywords", null, "[\"API\", \"integration\", \"REST\", \"GraphQL\"]"));
    }

    private AgentInfo createAutomationTestingGeneratorAgentInfo() {
        return AgentInfo.agent("AutomationTestingGeneratorAgent",
                "Generates automated test scripts and scenarios",
                "Testing")
            .withParameters(List.of(
                new AgentInfo.ParameterInfo("testScenario", "String",
                    "Description of the test scenario to automate",
                    true, null, null, "User login functionality"),
                new AgentInfo.ParameterInfo("framework", "String",
                    "Testing framework to use",
                    false, "selenium", List.of("selenium", "cypress", "playwright", "appium"), "selenium"),
                new AgentInfo.ParameterInfo("language", "String",
                    "Programming language for tests",
                    false, "java", List.of("java", "javascript", "python", "c#"), "java")
            ))
            .withReturnInfo(new AgentInfo.ReturnInfo("JSONObject",
                "Generated test scripts and configuration", null, "{\"scripts\": [...], \"config\": {...}}"));
    }

    private AgentInfo createTestCaseVisualizerAgentInfo() {
        return AgentInfo.agent("TestCaseVisualizerAgent",
                "Creates visual representations of test cases and coverage",
                "Testing")
            .withParameters(List.of(
                new AgentInfo.ParameterInfo("testCases", "String",
                    "JSON string containing test cases to visualize",
                    true, null, null, "[{\"id\": 1, \"name\": \"Login Test\", \"status\": \"passed\"}]"),
                new AgentInfo.ParameterInfo("visualizationType", "String",
                    "Type of visualization to create",
                    false, "flowchart", List.of("flowchart", "coverage", "matrix", "timeline"), "flowchart")
            ))
            .withReturnInfo(new AgentInfo.ReturnInfo("String",
                "Visualization markup (Mermaid, HTML, or SVG)", null, "graph TD\nA[Start] --> B[Login]"));
    }

    private AgentInfo createRelatedTestCaseAgentInfo() {
        return AgentInfo.agent("RelatedTestCaseAgent",
                "Finds and suggests related test cases based on functionality",
                "Testing")
            .withParameters(List.of(
                new AgentInfo.ParameterInfo("baseTestCase", "String",
                    "Primary test case to find relations for",
                    true, null, null, "User login with valid credentials"),
                new AgentInfo.ParameterInfo("context", "String",
                    "Additional context for finding relations",
                    false, null, null, "Authentication system")
            ))
            .withReturnInfo(new AgentInfo.ReturnInfo("JSONArray",
                "Array of related test cases", null, "[{\"name\": \"Invalid login\", \"relation\": \"negative_case\"}]"));
    }

    private AgentInfo createSourceImpactAssessmentAgentInfo() {
        return AgentInfo.agent("SourceImpactAssessmentAgent",
                "Analyzes the impact of source code changes on the system",
                "Analysis")
            .withParameters(List.of(
                new AgentInfo.ParameterInfo("sourceType", "String",
                    "Type of source being analyzed",
                    true, null, List.of("java", "javascript", "python", "api", "database"), "java"),
                new AgentInfo.ParameterInfo("task", "String",
                    "Description of the change or task",
                    true, null, null, "Modify user authentication method")
            ))
            .withReturnInfo(new AgentInfo.ReturnInfo("Boolean",
                "True if the change has significant impact, false otherwise", null, "true"));
    }

    private AgentInfo createTestCaseGeneratorAgentInfo() {
        return AgentInfo.agent("TestCaseGeneratorAgent",
                "Generates comprehensive test cases for given functionality",
                "Testing")
            .withParameters(List.of(
                new AgentInfo.ParameterInfo("userInput", "String",
                    "Description of functionality to test",
                    true, null, null, "User registration process"),
                new AgentInfo.ParameterInfo("requestType", "String",
                    "Type of test generation request",
                    false, "manual_execution", List.of("manual_execution", "automated", "api_testing"), "manual_execution"),
                new AgentInfo.ParameterInfo("testLevel", "String",
                    "Testing level",
                    false, "integration", List.of("unit", "integration", "system", "acceptance"), "integration")
            ))
            .withReturnInfo(new AgentInfo.ReturnInfo("JSONObject",
                "Generated test cases with scenarios and expected results",
                Map.of("testCases", "JSONArray", "coverage", "String", "metadata", "JSONObject"),
                "{\"testCases\": [...], \"coverage\": \"authentication_flow\"}"));
    }

    private AgentInfo createContentMergeAgentInfo() {
        return AgentInfo.agent("ContentMergeAgent",
                "Merges and consolidates content from multiple sources",
                "Content Management")
            .withParameters(List.of(
                new AgentInfo.ParameterInfo("task", "String",
                    "Description of the merge task",
                    true, null, null, "Combine documentation from multiple sources"),
                new AgentInfo.ParameterInfo("sourceContent", "String",
                    "Original content to merge with",
                    true, null, null, "# API Documentation\n..."),
                new AgentInfo.ParameterInfo("newContent", "String",
                    "New content to merge",
                    true, null, null, "# Additional API Examples\n..."),
                new AgentInfo.ParameterInfo("contentType", "String",
                    "Type of content being merged",
                    false, "text", List.of("html", "markdown", "text", "mermaid"), "markdown")
            ))
            .withReturnInfo(new AgentInfo.ReturnInfo("String",
                "Merged content in the specified format", null, "# Combined Documentation\n..."));
    }

    private AgentInfo createSearchResultsAssessmentAgentInfo() {
        return AgentInfo.agent("SearchResultsAssessmentAgent",
                "Evaluates and ranks search results based on relevance",
                "Search & Analysis")
            .withParameters(List.of(
                new AgentInfo.ParameterInfo("sourceType", "String",
                    "Type of source being searched",
                    true, null, List.of("jira", "confluence", "files", "api"), "confluence"),
                new AgentInfo.ParameterInfo("keyField", "String",
                    "Key field to extract from results",
                    true, null, List.of("key", "id", "path", "textMatch"), "key"),
                new AgentInfo.ParameterInfo("taskDescription", "String",
                    "Description of the search task",
                    true, null, null, "Find API documentation"),
                new AgentInfo.ParameterInfo("searchResults", "String",
                    "JSON string containing search results",
                    true, null, null, "[{\"key\": \"DOC-123\", \"title\": \"...\"}]")
            ))
            .withReturnInfo(new AgentInfo.ReturnInfo("JSONArray",
                "Ranked and filtered search results", null, "[{\"key\": \"DOC-123\", \"relevance\": 0.95}]"));
    }

    private AgentInfo createTaskExecutionAgentInfo() {
        return AgentInfo.agent("TaskExecutionAgent",
                "Executes and manages complex task workflows",
                "Task Management")
            .withParameters(List.of(
                new AgentInfo.ParameterInfo("taskDescription", "String",
                    "Detailed description of the task to execute",
                    true, null, null, "Setup CI/CD pipeline for new microservice")
            ))
            .withReturnInfo(new AgentInfo.ReturnInfo("JSONObject",
                "Execution steps and known data",
                Map.of("steps", "JSONArray", "knownData", "JSONObject"),
                "{\"steps\": [...], \"knownData\": {...}}"));
    }

    private AgentInfo createSnippetExtensionAgentInfo() {
        return AgentInfo.agent("SnippetExtensionAgent",
                "Extends code snippets with additional context and documentation",
                "Code Enhancement")
            .withParameters(List.of(
                new AgentInfo.ParameterInfo("codeSnippet", "String",
                    "Code snippet to extend",
                    true, null, null, "public void login(String username, String password) { ... }"),
                new AgentInfo.ParameterInfo("language", "String",
                    "Programming language of the snippet",
                    false, "java", List.of("java", "javascript", "python", "csharp"), "java"),
                new AgentInfo.ParameterInfo("extensionType", "String",
                    "Type of extension to add",
                    false, "documentation", List.of("documentation", "tests", "examples", "validation"), "documentation")
            ))
            .withReturnInfo(new AgentInfo.ReturnInfo("String",
                "Extended code snippet with additional context", null, "/** Login method... */\npublic void login(...)"));
    }

    private AgentInfo createSummaryContextAgentInfo() {
        return AgentInfo.agent("SummaryContextAgent",
                "Creates contextual summaries of documents and data",
                "Content Analysis")
            .withParameters(List.of(
                new AgentInfo.ParameterInfo("content", "String",
                    "Content to summarize",
                    true, null, null, "Long document text..."),
                new AgentInfo.ParameterInfo("summaryType", "String",
                    "Type of summary to create",
                    false, "executive", List.of("executive", "technical", "bullet_points", "abstract"), "executive"),
                new AgentInfo.ParameterInfo("maxLength", "Integer",
                    "Maximum length of summary in words",
                    false, 200, null, "150")
            ))
            .withReturnInfo(new AgentInfo.ReturnInfo("String",
                "Contextual summary of the content", null, "Executive Summary: This document describes..."));
    }

    private AgentInfo createRelatedTestCasesAgentInfo() {
        return AgentInfo.agent("RelatedTestCasesAgent",
                "Advanced discovery of related test cases with correlation analysis",
                "Testing")
            .withParameters(List.of(
                new AgentInfo.ParameterInfo("primaryTestCase", "String",
                    "Primary test case for finding relations",
                    true, null, null, "User authentication flow"),
                new AgentInfo.ParameterInfo("testSuite", "String",
                    "Existing test suite to search in",
                    false, null, null, "Authentication test suite"),
                new AgentInfo.ParameterInfo("correlationLevel", "String",
                    "Level of correlation to find",
                    false, "medium", List.of("low", "medium", "high"), "medium")
            ))
            .withReturnInfo(new AgentInfo.ReturnInfo("JSONArray",
                "Advanced related test cases with correlation scores",
                null, "[{\"name\": \"Password reset\", \"correlation\": 0.85, \"type\": \"functional_related\"}]"));
    }

    private AgentInfo createJSBridgeScriptGeneratorAgentInfo() {
        return AgentInfo.agent("JSBridgeScriptGeneratorAgent",
                "Generates executable JavaScript scripts that interact with DMToolsBridge API for various automation tasks",
                "JavaScript Generation")
            .withParameters(List.of(
                new AgentInfo.ParameterInfo("task", "String",
                    "Description of what the JavaScript should accomplish",
                    true, null, null, "Create a presentation about quarterly results using bridge API"),
                new AgentInfo.ParameterInfo("apiDescription", "String",
                    "Dynamic description of available DMToolsBridge API methods",
                    true, null, null, "Auto-generated from DMToolsBridge"),
                new AgentInfo.ParameterInfo("jsFramework", "String",
                    "Target JavaScript framework or environment",
                    false, "vanilla", List.of("vanilla", "node", "browser"), "vanilla"),
                new AgentInfo.ParameterInfo("outputFormat", "String",
                    "Expected output format for the generated script",
                    false, "function", List.of("function", "module", "complete script"), "function"),
                new AgentInfo.ParameterInfo("additionalRequirements", "String",
                    "Any special requirements or constraints for the script",
                    false, null, null, "Include error handling and logging"),
                new AgentInfo.ParameterInfo("files", "List<File>",
                    "Related files for context (optional)",
                    false, null, null, null)
            ))
            .withReturnInfo(new AgentInfo.ReturnInfo("String",
                "Generated JavaScript code that uses DMToolsBridge API methods", 
                null, 
                "function createPresentation(bridge, topic) { bridge.jsLogInfo('Creating presentation for: ' + topic); ... }"));
    }

    // Orchestrator info methods
    private AgentInfo createContextOrchestratorInfo() {
        return AgentInfo.orchestrator("ContextOrchestrator",
                "Manages and processes contextual information across multiple sources",
                "Context Management")
            .withParameters(List.of(
                new AgentInfo.ParameterInfo("contextSources", "List<String>",
                    "List of context sources to process",
                    true, null, null, "[\"confluence\", \"jira\", \"files\"]"),
                new AgentInfo.ParameterInfo("processingMode", "String",
                    "Mode of context processing",
                    false, "comprehensive", List.of("basic", "comprehensive", "focused"), "comprehensive")
            ))
            .withReturnInfo(new AgentInfo.ReturnInfo("JSONObject",
                "Processed contextual information", null, "{\"context\": {...}, \"sources\": [...]}"));
    }

    private AgentInfo createPresentationMakerOrchestratorInfo() {
        return AgentInfo.orchestrator("PresentationMakerOrchestrator",
                "End-to-end presentation creation workflow from topic to formatted slides",
                "Presentation Workflow")
            .withParameters(List.of(
                new AgentInfo.ParameterInfo("topic", "String",
                    "Main presentation topic",
                    true, null, null, "Q4 Financial Results"),
                new AgentInfo.ParameterInfo("audience", "String",
                    "Target audience",
                    true, null, null, "Board of Directors"),
                new AgentInfo.ParameterInfo("presenterName", "String",
                    "Name of the presenter",
                    false, null, null, "John Smith"),
                new AgentInfo.ParameterInfo("presenterTitle", "String",
                    "Title of the presenter",
                    false, null, null, "CFO"),
                new AgentInfo.ParameterInfo("slideCount", "Integer",
                    "Preferred number of slides",
                    false, 10, null, "15"),
                new AgentInfo.ParameterInfo("format", "String",
                    "Output format",
                    false, "html", List.of("html", "pptx", "pdf"), "html")
            ))
            .withReturnInfo(new AgentInfo.ReturnInfo("JSONObject",
                "Complete presentation with formatted slides",
                Map.of("slides", "JSONArray", "metadata", "JSONObject", "format", "String"),
                "{\"slides\": [...], \"metadata\": {...}}"));
    }

    private AgentInfo createConfluenceSearchOrchestratorInfo() {
        return AgentInfo.orchestrator("ConfluenceSearchOrchestrator",
                "Advanced Confluence content search and analysis workflow",
                "Search Workflow")
            .withParameters(List.of(
                new AgentInfo.ParameterInfo("task", "String",
                    "Search task description",
                    true, null, null, "Find API documentation for user service"),
                new AgentInfo.ParameterInfo("blacklist", "String",
                    "Content to exclude from search",
                    false, "", null, "deprecated,old"),
                new AgentInfo.ParameterInfo("itemsLimit", "Integer",
                    "Maximum number of results",
                    false, 10, null, "20"),
                new AgentInfo.ParameterInfo("iterations", "Integer",
                    "Number of search iterations",
                    false, 1, null, "3")
            ))
            .withReturnInfo(new AgentInfo.ReturnInfo("JSONObject",
                "Search results with analysis and recommendations",
                Map.of("results", "JSONArray", "analysis", "JSONObject", "recommendations", "JSONArray"),
                "{\"results\": [...], \"analysis\": {...}}"));
    }

    private AgentInfo createTrackerSearchOrchestratorInfo() {
        return AgentInfo.orchestrator("TrackerSearchOrchestrator",
                "Advanced ticket and issue tracking search workflow",
                "Issue Tracking Workflow")
            .withParameters(List.of(
                new AgentInfo.ParameterInfo("task", "String",
                    "Issue search task description",
                    true, null, null, "Find all bugs related to authentication"),
                new AgentInfo.ParameterInfo("blacklist", "String",
                    "Issues to exclude",
                    false, "", null, "closed,wont-fix"),
                new AgentInfo.ParameterInfo("itemsLimit", "Integer",
                    "Maximum number of issues to retrieve",
                    false, 10, null, "25"),
                new AgentInfo.ParameterInfo("iterations", "Integer",
                    "Number of search iterations for refinement",
                    false, 1, null, "2")
            ))
            .withReturnInfo(new AgentInfo.ReturnInfo("JSONObject",
                "Issue search results with categorization and priority analysis",
                Map.of("issues", "JSONArray", "categorization", "JSONObject", "priorities", "JSONArray"),
                "{\"issues\": [...], \"categorization\": {...}}"));
    }
} 