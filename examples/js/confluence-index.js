/**
 * Confluence Mermaid Index
 * 
 * Automated processor for indexing Confluence content into Mermaid diagrams.
 * Fetches content from Confluence based on include/exclude patterns, generates
 * Mermaid diagrams, and stores them in a hierarchical file structure.
 * 
 * Usage:
 * - Via JSRunner job with integration, storagePath, include, and exclude parameters
 * - Uses MCP tool mermaid_index_generate for diagram generation
 * 
 * @param {Object} params - Job parameters
 * @param {Object} params.jobParams - Job configuration
 * @param {string} params.jobParams.integration - Integration name (e.g., "confluence")
 * @param {string} params.jobParams.storagePath - Base path for storing diagrams
 * @param {string[]} params.jobParams.include - Optional: Array of include patterns
 * @param {string[]} params.jobParams.exclude - Optional: Array of exclude patterns
 */

/**
 * Main action function
 * Orchestrates the Mermaid indexing workflow:
 * 1. Extract job parameters
 * 2. Call MCP tool mermaid_index_generate
 * 3. Return result
 * 
 * @param {Object} params - Job parameters
 * @returns {Object} Result with success status and details
 */
function action(params) {
    try {
        console.log('=== Confluence Mermaid Index ===');
        
        // Get job parameters
        const jobParams = params.jobParams || {};
        const integration = jobParams.integration || 'confluence';
        const storagePath = jobParams.storagePath;
        const include = jobParams.include || [];
        const exclude = jobParams.exclude || [];
        
        console.log('Job parameters:', JSON.stringify(jobParams));
        
        // Validate required parameters
        if (!storagePath) {
            return {
                success: false,
                error: 'storagePath parameter is required'
            };
        }
        
        console.log('Integration:', integration);
        console.log('Storage path:', storagePath);
        console.log('Include patterns:', include);
        console.log('Exclude patterns:', exclude);
        
        // Use MCP tool for Mermaid index generation
        // This uses the MCP architecture similar to confluence, jira, kb tools
        try {
            console.log('Using MCP tool: mermaid_index_generate');
            
            // Call MCP tool
            const result = mermaid_index_generate({
                integration: integration,
                storage_path: storagePath,
                include_patterns: include,
                exclude_patterns: exclude
            });
            
            // Parse result (MCP tool returns JSON string)
            let resultObj;
            try {
                resultObj = typeof result === 'string' ? JSON.parse(result) : result;
            } catch (e) {
                // If result is not JSON, treat as success message
                resultObj = { success: true, message: result };
            }
            
            if (resultObj.success) {
                console.log('✅ Mermaid indexing completed successfully');
                return {
                    success: true,
                    message: resultObj.message || 'Mermaid indexing completed successfully',
                    integration: integration,
                    storagePath: storagePath,
                    includePatterns: include.length,
                    excludePatterns: exclude.length
                };
            } else {
                console.error('❌ Mermaid indexing failed:', resultObj.error);
                return {
                    success: false,
                    error: resultObj.error || 'Unknown error'
                };
            }
            
        } catch (mcpError) {
            console.error('MCP tool error:', mcpError);
            return {
                success: false,
                error: 'Failed to execute MCP tool: ' + mcpError.toString()
            };
        }
        
    } catch (error) {
        console.error('❌ Mermaid indexing failed:', error);
        return {
            success: false,
            error: error.toString()
        };
    }
}
