package com.github.istin.dmtools.common.kb.tool;

import com.github.istin.dmtools.common.kb.SourceConfigManager;
import com.github.istin.dmtools.common.kb.agent.KBOrchestrator;
import com.github.istin.dmtools.common.kb.model.KBProcessingMode;
import com.github.istin.dmtools.common.kb.model.KBResult;
import com.github.istin.dmtools.common.kb.model.SourceConfig;
import com.github.istin.dmtools.common.kb.model.SourceInfo;
import com.github.istin.dmtools.common.kb.params.KBOrchestratorParams;
import com.github.istin.dmtools.common.utils.PropertyReader;
import com.github.istin.dmtools.mcp.MCPParam;
import com.github.istin.dmtools.mcp.MCPTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MCP Tools for Knowledge Base operations.
 * Provides CLI interface for building and querying indexed knowledge bases.
 */
@Singleton
public class KBTools {
    
    private static final Logger logger = LogManager.getLogger(KBTools.class);
    
    protected final KBOrchestrator orchestrator;
    protected final PropertyReader propertyReader;
    protected final SourceConfigManager sourceConfigManager;
    
    @Inject
    public KBTools(
            KBOrchestrator orchestrator,
            PropertyReader propertyReader,
            SourceConfigManager sourceConfigManager
    ) {
        this.orchestrator = orchestrator;
        this.propertyReader = propertyReader;
        this.sourceConfigManager = sourceConfigManager;
        logger.info("KBTools initialized");
    }
    
    /**
     * Get last sync date for a knowledge base source.
     * 
     * @param sourceName Name of the data source (e.g., "teams_chat", "slack_general")
     * @param outputPath Optional path to KB directory. If not provided, uses DMTOOLS_KB_OUTPUT_PATH env var or current directory
     * @return Last sync date in ISO 8601 format, or "Source not found" message
     */
    @MCPTool(
        name = "kb_get",
        description = "Get last sync date for a knowledge base source. Returns ISO 8601 date string or 'Source not found' message.",
        integration = "kb"
    )
    public String kbGet(
            @MCPParam(
                name = "source_name",
                description = "Name of the data source (e.g., 'teams_chat', 'slack_general')",
                required = true,
                example = "teams_chat"
            ) String sourceName,
            @MCPParam(
                name = "output_path",
                description = "Optional path to KB directory. Defaults to DMTOOLS_KB_OUTPUT_PATH env var or current directory",
                required = false,
                example = "/path/to/knowledge-base"
            ) String outputPath
    ) {
        try {
            Path kbPath = resolveOutputPath(outputPath);
            logger.info("Checking source '{}' in KB at: {}", sourceName, kbPath);
            
            SourceConfig config = sourceConfigManager.loadConfig(kbPath);
            SourceInfo info = config.getSources().get(sourceName);
            
            if (info == null) {
                return "Source not found: " + sourceName;
            }
            
            return info.getLastSyncDate();
            
        } catch (Exception e) {
            logger.error("Error getting source info for '{}': {}", sourceName, e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * Build or update knowledge base from input file.
     * 
     * @param sourceName Name of the data source
     * @param inputFile Path to input file (JSON or text format)
     * @param dateTime Sync date/time in ISO 8601 format
     * @param outputPath Optional path to KB directory. If not provided, uses DMTOOLS_KB_OUTPUT_PATH env var or current directory
     * @return JSON string with build results including counts and status
     */
    @MCPTool(
        name = "kb_build",
        description = "Build or update knowledge base from input file. Processes chat messages, documentation, or any text data to create indexed, searchable knowledge base. Returns JSON with statistics.",
        integration = "kb"
    )
    public String kbBuild(
            @MCPParam(
                name = "source_name",
                description = "Name of the data source (e.g., 'teams_chat', 'slack_general')",
                required = true,
                example = "teams_chat"
            ) String sourceName,
            @MCPParam(
                name = "input_file",
                description = "Path to input file (JSON or text format). Can be JSON array of messages or plain text.",
                required = true,
                example = "/path/to/messages.json"
            ) String inputFile,
            @MCPParam(
                name = "date_time",
                description = "Sync date/time in ISO 8601 format (e.g., '2024-10-10T12:00:00Z')",
                required = true,
                example = "2024-10-10T12:00:00Z"
            ) String dateTime,
            @MCPParam(
                name = "output_path",
                description = "Optional path to KB directory. Defaults to DMTOOLS_KB_OUTPUT_PATH env var or current directory",
                required = false,
                example = "/path/to/knowledge-base"
            ) String outputPath,
            @MCPParam(
                name = "clean_source",
                description = "Optional. If true, removes all existing Q/A/N from this source before processing. Use for content refresh (e.g., Confluence pages).",
                required = false,
                example = "true"
            ) String cleanSource
    ) {
        try {
            // Validate input file exists
            Path inputPath = Paths.get(inputFile);
            if (!Files.exists(inputPath)) {
                return "{\"success\": false, \"message\": \"Input file not found: " + inputFile + "\"}";
            }
            
            Path kbPath = resolveOutputPath(outputPath);
            logger.info("Building KB from source '{}' at: {}", sourceName, kbPath);
            logger.info("Input file: {}", inputFile);
            logger.info("Sync date: {}", dateTime);
            
            // Create orchestrator params
            KBOrchestratorParams params = new KBOrchestratorParams();
            params.setSourceName(sourceName);
            params.setInputFile(inputFile);
            params.setDateTime(dateTime);
            params.setOutputPath(kbPath.toString());
            params.setProcessingMode(KBProcessingMode.FULL);
            
            // Set clean source flag if provided
            if (cleanSource != null && cleanSource.equalsIgnoreCase("true")) {
                params.setCleanSourceBeforeProcessing(true);
                logger.info("Clean source mode enabled - will remove existing Q/A/N from source '{}'", sourceName);
            }
            
            // Run orchestrator
            KBResult result = orchestrator.run(params);
            
            // Convert result to JSON
            return formatResult(result);
            
        } catch (Exception e) {
            logger.error("Error building KB for source '{}': {}", sourceName, e.getMessage(), e);
            return "{\"success\": false, \"message\": \"Error: " + escapeJson(e.getMessage()) + "\"}";
        }
    }
    
    /**
     * Process input file without generating AI descriptions (fast mode).
     * 
     * @param sourceName Name of the data source
     * @param inputFile Path to input file (JSON or text format)
     * @param dateTime Sync date/time in ISO 8601 format
     * @param outputPath Optional path to KB directory. If not provided, uses DMTOOLS_KB_OUTPUT_PATH env var or current directory
     * @return JSON string with build results including counts and status
     */
    @MCPTool(
        name = "kb_process",
        description = "Process input file and build KB structure WITHOUT generating AI descriptions (fast mode). Use this for bulk data processing. Run kb_aggregate later to generate descriptions.",
        integration = "kb"
    )
    public String kbProcess(
            @MCPParam(
                name = "source_name",
                description = "Name of the data source (e.g., 'teams_chat', 'slack_general')",
                required = true,
                example = "teams_chat"
            ) String sourceName,
            @MCPParam(
                name = "input_file",
                description = "Path to input file (JSON or text format). Can be JSON array of messages or plain text.",
                required = true,
                example = "/path/to/messages.json"
            ) String inputFile,
            @MCPParam(
                name = "date_time",
                description = "Sync date/time in ISO 8601 format (e.g., '2024-10-10T12:00:00Z')",
                required = true,
                example = "2024-10-10T12:00:00Z"
            ) String dateTime,
            @MCPParam(
                name = "output_path",
                description = "Optional path to KB directory. Defaults to DMTOOLS_KB_OUTPUT_PATH env var or current directory",
                required = false,
                example = "/path/to/knowledge-base"
            ) String outputPath,
            @MCPParam(
                name = "clean_source",
                description = "Optional. If true, removes all existing Q/A/N from this source before processing. Use for content refresh (e.g., Confluence pages).",
                required = false,
                example = "true"
            ) String cleanSource
    ) {
        try {
            // Validate input file exists
            Path inputPath = Paths.get(inputFile);
            if (!Files.exists(inputPath)) {
                return "{\"success\": false, \"message\": \"Input file not found: " + inputFile + "\"}";
            }
            
            Path kbPath = resolveOutputPath(outputPath);
            logger.info("Processing KB (PROCESS_ONLY mode) from source '{}' at: {}", sourceName, kbPath);
            logger.info("Input file: {}", inputFile);
            logger.info("Sync date: {}", dateTime);
            
            // Create orchestrator params
            KBOrchestratorParams params = new KBOrchestratorParams();
            params.setSourceName(sourceName);
            params.setInputFile(inputFile);
            params.setDateTime(dateTime);
            params.setOutputPath(kbPath.toString());
            params.setProcessingMode(KBProcessingMode.PROCESS_ONLY);
            
            // Set clean source flag if provided
            if (cleanSource != null && cleanSource.equalsIgnoreCase("true")) {
                params.setCleanSourceBeforeProcessing(true);
                logger.info("Clean source mode enabled - will remove existing Q/A/N from source '{}'", sourceName);
            }
            
            // Run orchestrator
            KBResult result = orchestrator.run(params);
            
            // Convert result to JSON
            return formatResult(result);
            
        } catch (Exception e) {
            logger.error("Error processing KB for source '{}': {}", sourceName, e.getMessage(), e);
            return "{\"success\": false, \"message\": \"Error: " + escapeJson(e.getMessage()) + "\"}";
        }
    }
    
    /**
     * Generate AI descriptions for existing KB structure (without processing new data).
     * 
     * @param sourceName Name of the data source (required for proper tagging)
     * @param outputPath Optional path to KB directory. If not provided, uses DMTOOLS_KB_OUTPUT_PATH env var or current directory
     * @return JSON string with build results including counts and status
     */
    @MCPTool(
        name = "kb_aggregate",
        description = "Generate AI descriptions for existing KB structure WITHOUT processing new data. Use this after kb_process to add AI-generated descriptions for people and topics.",
        integration = "kb"
    )
    public String kbAggregate(
            @MCPParam(
                name = "source_name",
                description = "Name of the data source (required for proper tagging)",
                required = true,
                example = "teams_chat"
            ) String sourceName,
            @MCPParam(
                name = "output_path",
                description = "Optional path to KB directory. Defaults to DMTOOLS_KB_OUTPUT_PATH env var or current directory",
                required = false,
                example = "/path/to/knowledge-base"
            ) String outputPath
    ) {
        try {
            Path kbPath = resolveOutputPath(outputPath);
            logger.info("Generating AI descriptions (AGGREGATE_ONLY mode) for KB at: {}", kbPath);
            
            // Create orchestrator params
            KBOrchestratorParams params = new KBOrchestratorParams();
            params.setSourceName(sourceName);  // Can be null for all sources
            params.setOutputPath(kbPath.toString());
            params.setProcessingMode(KBProcessingMode.AGGREGATE_ONLY);
            
            // Run orchestrator
            KBResult result = orchestrator.run(params);
            
            // Convert result to JSON
            return formatResult(result);
            
        } catch (Exception e) {
            logger.error("Error aggregating KB: {}", e.getMessage(), e);
            return "{\"success\": false, \"message\": \"Error: " + escapeJson(e.getMessage()) + "\"}";
        }
    }
    
    /**
     * Resolve output path based on priority:
     * 1. Provided outputPath parameter
     * 2. DMTOOLS_KB_OUTPUT_PATH environment variable
     * 3. Current working directory
     */
    private Path resolveOutputPath(String outputPath) {
        if (outputPath != null && !outputPath.trim().isEmpty()) {
            return Paths.get(outputPath.trim()).toAbsolutePath().normalize();
        }
        
        String envPath = propertyReader.getValue("DMTOOLS_KB_OUTPUT_PATH");
        if (envPath != null && !envPath.trim().isEmpty()) {
            return Paths.get(envPath.trim()).toAbsolutePath().normalize();
        }
        
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }
    
    /**
     * Format KBResult as JSON string
     */
    private String formatResult(KBResult result) {
        return String.format(
                "{\"success\": %s, \"message\": \"%s\", " +
                "\"topics\": %d, \"areas\": %d, \"questions\": %d, \"answers\": %d, \"notes\": %d, \"people\": %d}",
                result.isSuccess(),
                escapeJson(result.getMessage()),
                result.getTopicsCount(),
                result.getAreasCount(),
                result.getQuestionsCount(),
                result.getAnswersCount(),
                result.getNotesCount(),
                result.getPeopleCount()
        );
    }
    
    /**
     * Scan inbox and process all unprocessed files automatically.
     * 
     * Scans inbox/raw/[source]/ subdirectories for files and processes any that haven't been processed yet.
     * Files are processed in place (no copy) - only analyzed tracking files are created.
     * 
     * @param outputPath Optional path to KB directory. If not provided, uses DMTOOLS_KB_OUTPUT_PATH env var or current directory
     * @return JSON string with processing results including processed and skipped files
     */
    @MCPTool(
        name = "kb_process_inbox",
        description = "Scan inbox/raw/ folders and process all unprocessed files automatically. Files are processed in place. Returns JSON with processed and skipped file details.",
        integration = "kb"
    )
    public String kbProcessInbox(
            @MCPParam(
                name = "output_path",
                description = "Optional path to KB directory. Defaults to DMTOOLS_KB_OUTPUT_PATH env var or current directory",
                required = false,
                example = "/path/to/knowledge-base"
            ) String outputPath
    ) {
        try {
            Path kbPath = resolveOutputPath(outputPath);
            Path inboxRawPath = kbPath.resolve("inbox/raw");
            Path inboxAnalyzedPath = kbPath.resolve("inbox/analyzed");
            
            logger.info("Processing inbox at: {}", kbPath);
            logger.info("Scanning inbox/raw path: {}", inboxRawPath);
            
            // Check if inbox/raw exists
            if (!Files.exists(inboxRawPath)) {
                logger.warn("Inbox raw path does not exist: {}", inboxRawPath);
                return "{\"success\": true, \"message\": \"No inbox/raw directory found\", \"processed\": [], \"skipped\": []}";
            }
            
            List<ProcessedFile> processedFiles = new ArrayList<>();
            List<SkippedFile> skippedFiles = new ArrayList<>();
            
            // Scan source folders in inbox/raw/
            try (DirectoryStream<Path> sourceFolders = Files.newDirectoryStream(inboxRawPath, Files::isDirectory)) {
                for (Path sourceFolder : sourceFolders) {
                    String sourceName = sourceFolder.getFileName().toString();
                    logger.info("Scanning source folder: {}", sourceName);
                    
                    Path analyzedSourcePath = inboxAnalyzedPath.resolve(sourceName);
                    
                    // Scan files in source folder
                    try (DirectoryStream<Path> files = Files.newDirectoryStream(sourceFolder, Files::isRegularFile)) {
                        for (Path file : files) {
                            String fileName = file.getFileName().toString();
                            String baseFileName = fileName.replaceAll("\\.[^.]+$", ""); // Remove extension
                            
                            // Check if file has been analyzed
                            Path analyzedFile = analyzedSourcePath.resolve(baseFileName + "_analyzed.json");
                            
                            if (Files.exists(analyzedFile)) {
                                logger.debug("Skipping already processed file: {}/{}", sourceName, fileName);
                                skippedFiles.add(new SkippedFile(sourceName, fileName, "Already processed"));
                                continue;
                            }
                            
                            // Process the file in place
                            logger.info("Processing file: {}/{}", sourceName, fileName);
                            
                            try {
                                // Create orchestrator params - process file in place
                                KBOrchestratorParams params = new KBOrchestratorParams();
                                params.setSourceName(sourceName);
                                params.setInputFile(file.toString()); // Use file in place
                                params.setDateTime(Instant.now().toString()); // Current timestamp
                                params.setOutputPath(kbPath.toString());
                                params.setProcessingMode(KBProcessingMode.FULL);
                                
                                // Run orchestrator
                                KBResult result = orchestrator.run(params);
                                
                                if (result.isSuccess()) {
                                    processedFiles.add(new ProcessedFile(
                                        sourceName,
                                        fileName,
                                        result.getQuestionsCount(),
                                        result.getAnswersCount(),
                                        result.getNotesCount()
                                    ));
                                    logger.info("✓ Successfully processed: {}/{} (Q:{}, A:{}, N:{})", 
                                        sourceName, fileName, 
                                        result.getQuestionsCount(), 
                                        result.getAnswersCount(), 
                                        result.getNotesCount());
                                } else {
                                    skippedFiles.add(new SkippedFile(sourceName, fileName, result.getMessage()));
                                    logger.warn("✗ Processing failed: {}/{} - {}", sourceName, fileName, result.getMessage());
                                }
                                
                            } catch (Exception e) {
                                String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                                skippedFiles.add(new SkippedFile(sourceName, fileName, "Error: " + errorMsg));
                                logger.error("Error processing file {}/{}: {}", sourceName, fileName, errorMsg, e);
                            }
                        }
                    }
                }
            }
            
            // Build result JSON
            StringBuilder jsonResult = new StringBuilder();
            jsonResult.append("{\"success\": true, \"message\": \"Processed ")
                     .append(processedFiles.size())
                     .append(" files, skipped ")
                     .append(skippedFiles.size())
                     .append(" files\", ");
            
            // Add processed files
            jsonResult.append("\"processed\": [");
            for (int i = 0; i < processedFiles.size(); i++) {
                if (i > 0) jsonResult.append(", ");
                ProcessedFile pf = processedFiles.get(i);
                jsonResult.append("{\"source\": \"").append(escapeJson(pf.source))
                         .append("\", \"file\": \"").append(escapeJson(pf.file))
                         .append("\", \"questions\": ").append(pf.questions)
                         .append(", \"answers\": ").append(pf.answers)
                         .append(", \"notes\": ").append(pf.notes)
                         .append("}");
            }
            jsonResult.append("], ");
            
            // Add skipped files
            jsonResult.append("\"skipped\": [");
            for (int i = 0; i < skippedFiles.size(); i++) {
                if (i > 0) jsonResult.append(", ");
                SkippedFile sf = skippedFiles.get(i);
                jsonResult.append("{\"source\": \"").append(escapeJson(sf.source))
                         .append("\", \"file\": \"").append(escapeJson(sf.file))
                         .append("\", \"reason\": \"").append(escapeJson(sf.reason))
                         .append("\"}");
            }
            jsonResult.append("]}");
            
            logger.info("Inbox processing completed: {} processed, {} skipped", 
                       processedFiles.size(), skippedFiles.size());
            
            return jsonResult.toString();
            
        } catch (Exception e) {
            logger.error("Error processing inbox: {}", e.getMessage(), e);
            return "{\"success\": false, \"message\": \"Error: " + escapeJson(e.getMessage()) + "\"}";
        }
    }
    
    /**
     * Helper class for tracking processed files
     */
    private static class ProcessedFile {
        final String source;
        final String file;
        final int questions;
        final int answers;
        final int notes;
        
        ProcessedFile(String source, String file, int questions, int answers, int notes) {
            this.source = source;
            this.file = file;
            this.questions = questions;
            this.answers = answers;
            this.notes = notes;
        }
    }
    
    /**
     * Helper class for tracking skipped files
     */
    private static class SkippedFile {
        final String source;
        final String file;
        final String reason;
        
        SkippedFile(String source, String file, String reason) {
            this.source = source;
            this.file = file;
            this.reason = reason;
        }
    }
    
    /**
     * Escape special characters for JSON string
     */
    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}

