package com.github.istin.dmtools.server.config;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.ai.agent.*;
import com.github.istin.dmtools.context.ContextOrchestrator;
import com.github.istin.dmtools.presentation.PresentationMakerOrchestrator;
import com.github.istin.dmtools.prompt.IPromptTemplateReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

    // Agent beans - creating instances directly
     @Bean
     public BusinessAreaAssessmentAgent businessAreaAssessmentAgent() {
         BusinessAreaAssessmentAgent agent = new BusinessAreaAssessmentAgent();
         // Note: Dependency injection will be handled by the agent's internal DI mechanism
         return agent;
     }

    @Bean
    public PresentationSlideFormatterAgent presentationSlideFormatterAgent() {
        return new PresentationSlideFormatterAgent();
    }

    @Bean
    public PresentationContentGeneratorAgent presentationContentGeneratorAgent() {
        return new PresentationContentGeneratorAgent();
    }

    @Bean
    public TaskProgressAgent taskProgressAgent(AI ai, IPromptTemplateReader promptTemplateReader) {
        return new TaskProgressAgent(ai, promptTemplateReader);
    }

    @Bean
    public TeamAssistantAgent teamAssistantAgent() {
        return new TeamAssistantAgent();
    }

    @Bean
    public GenericRequestAgent genericRequestAgent() {
        return new GenericRequestAgent();
    }

    @Bean
    public KeywordGeneratorAgent keywordGeneratorAgent() {
        return new KeywordGeneratorAgent();
    }

    @Bean
    public AutomationTestingGeneratorAgent automationTestingGeneratorAgent(AI ai, IPromptTemplateReader promptTemplateReader) {
        return new AutomationTestingGeneratorAgent(ai, promptTemplateReader);
    }

    @Bean
    public TestCaseVisualizerAgent testCaseVisualizerAgent() {
        return new TestCaseVisualizerAgent();
    }

    @Bean
    public RelatedTestCaseAgent relatedTestCaseAgent() {
        return new RelatedTestCaseAgent();
    }

    @Bean
    public SourceImpactAssessmentAgent sourceImpactAssessmentAgent(AI ai, IPromptTemplateReader promptTemplateReader) {
        return new SourceImpactAssessmentAgent(ai, promptTemplateReader);
    }

    @Bean
    public TestCaseGeneratorAgent testCaseGeneratorAgent() {
        return new TestCaseGeneratorAgent();
    }

    @Bean
    public ContentMergeAgent contentMergeAgent() {
        return new ContentMergeAgent();
    }

    @Bean
    public SearchResultsAssessmentAgent searchResultsAssessmentAgent() {
        return new SearchResultsAssessmentAgent();
    }

    @Bean
    public TaskExecutionAgent taskExecutionAgent(AI ai, IPromptTemplateReader promptTemplateReader) {
        return new TaskExecutionAgent(ai, promptTemplateReader);
    }

    @Bean
    public SnippetExtensionAgent snippetExtensionAgent() {
        return new SnippetExtensionAgent();
    }

    @Bean
    public SummaryContextAgent summaryContextAgent(AI ai, IPromptTemplateReader promptTemplateReader) {
        return new SummaryContextAgent(ai, promptTemplateReader);
    }

    @Bean
    public RelatedTestCasesAgent relatedTestCasesAgent() {
        return new RelatedTestCasesAgent();
    }

    @Bean
    public JSBridgeScriptGeneratorAgent jsBridgeScriptGeneratorAgent() {
        return new JSBridgeScriptGeneratorAgent();
    }

    @Bean
    public RequestDecompositionAgent requestDecompositionAgent(AI ai, IPromptTemplateReader promptTemplateReader) {
        return new RequestDecompositionAgent(ai, promptTemplateReader);
    }

    // Orchestrator beans
    @Bean
    public ContextOrchestrator contextOrchestrator(SummaryContextAgent summaryContextAgent, ContentMergeAgent contentMergeAgent) {
        return new ContextOrchestrator(summaryContextAgent, contentMergeAgent);
    }

    // Commented out beans for missing classes
    @Bean
    public PresentationMakerOrchestrator presentationMakerOrchestrator() {
        return new PresentationMakerOrchestrator();
    }


    // Note: Search orchestrators require dependencies, will be created when needed
    // or configured separately

    @Bean
    public ToolSelectorAgent toolSelectorAgent() {
        return new ToolSelectorAgent();
    }
} 