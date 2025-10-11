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
}


