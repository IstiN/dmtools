package com.github.istin.dmtools.common.kb.params;

import com.github.istin.dmtools.common.kb.model.KBProcessingMode;
import lombok.Data;

/**
 * Parameters for KBOrchestrator main agent
 */
@Data
public class KBOrchestratorParams {
    private String sourceName;
    private String inputFile;
    private String dateTime;
    private String outputPath;
    
    /**
     * Processing mode: FULL (default), PROCESS_ONLY, or AGGREGATE_ONLY
     */
    private KBProcessingMode processingMode = KBProcessingMode.FULL;
    
    /**
     * Optional extra instructions for KBAnalysisAgent
     * Will be injected into the analysis prompt
     */
    private String analysisExtraInstructions;
    
    /**
     * Optional extra instructions for KBAggregationAgent
     * Will be injected into the aggregation prompt
     */
    private String aggregationExtraInstructions;
    
    /**
     * Optional extra instructions for KBQuestionAnswerMappingAgent
     * Will be injected into the Q&A mapping prompt
     */
    private String qaMappingExtraInstructions;

    /**
     * When true, orchestrator cleans the output directory before processing.
     * When false, existing files remain (useful for incremental updates and manual inspection).
     * Default: false (incremental mode)
     */
    private boolean cleanOutput = false;
}


