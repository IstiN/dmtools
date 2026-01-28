/**
 * Jira Mermaid Index
 * 
 * Automated processor for indexing Jira tickets into Mermaid diagrams.
 * Fetches tickets from Jira based on JQL query, generates Mermaid diagrams,
 * and stores them in a hierarchical file structure.
 * 
 * Usage:
 * - Via JSRunner job with integration, storagePath, include (JQL), customFields, and includeComments parameters
 * - Uses MCP tool mermaid_index_generate for diagram generation
 * 
 * @param {Object} params - Job parameters
 * @param {Object} params.jobParams - Job configuration
 * @param {string} params.jobParams.integration - Integration name ("jira" or "jira_xray")
 * @param {string} params.jobParams.storagePath - Base path for storing diagrams
 * @param {string[]} params.jobParams.include - Array with JQL query (e.g., ["project = PROJ AND status = Open"])
 * @param {string[]} params.jobParams.exclude - Optional: Array of exclude patterns (not used for Jira)
 * @param {string[]} params.jobParams.customFields - Optional: Array of custom field names to include
 * @param {boolean} params.jobParams.includeComments - Optional: Whether to include comments (default: false)
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
        console.log('=== Jira Mermaid Index ===');
        
        // Get job parameters
        // Parse from JSON string to ensure we get a plain JavaScript object
        // This handles GraalVM polyglot objects that may not support direct property access
        let jobParams = params.jobParams || {};
        try {
            const jobParamsJson = JSON.stringify(jobParams);
            jobParams = JSON.parse(jobParamsJson);
        } catch (e) {
            console.warn('Failed to parse jobParams as JSON, using original:', e);
        }
        
        const integration = jobParams.integration || 'jira';
        const storagePath = jobParams.storagePath;
        
        console.log('Job parameters:', JSON.stringify(jobParams));
        
        // Helper function to convert values to JavaScript arrays
        function toArray(value) {
            if (Array.isArray(value)) {
                return value;
            }
            if (value === undefined || value === null) {
                return [];
            }
            // Try to convert polyglot list/array to JavaScript array
            try {
                // If it has array elements (GraalVM polyglot), convert it
                if (value.length !== undefined && typeof value.length === 'number') {
                    const result = [];
                    for (let i = 0; i < value.length; i++) {
                        result.push(value[i]);
                    }
                    return result;
                }
                // If it's iterable, use spread operator
                if (Symbol.iterator in value) {
                    return Array.from(value);
                }
                // Otherwise, wrap in array
                return [value];
            } catch (e) {
                console.warn('Failed to convert to array:', e);
                return [];
            }
        }
        
        // Support both 'include' and 'include_patterns' for backward compatibility
        // Try both dot notation and bracket notation
        let include = jobParams['include_patterns'] || jobParams.include_patterns || jobParams['include'] || jobParams.include;
        include = toArray(include);
        
        // Support both 'exclude' and 'exclude_patterns' for backward compatibility
        let exclude = jobParams['exclude_patterns'] || jobParams.exclude_patterns || jobParams['exclude'] || jobParams.exclude;
        exclude = toArray(exclude);
        
        // Get customFields and includeComments
        let customFields = jobParams['custom_fields'] || jobParams.custom_fields || jobParams['customFields'] || jobParams.customFields;
        customFields = toArray(customFields);
        
        let includeComments = jobParams['include_comments'] || jobParams.include_comments || jobParams['includeComments'] || jobParams.includeComments || false;
        
        // Validate required parameters
        if (!storagePath) {
            return {
                success: false,
                error: 'storagePath parameter is required'
            };
        }
        
        if (include.length === 0) {
            return {
                success: false,
                error: 'include parameter is required (should contain JQL query)'
            };
        }
        
        console.log('Integration:', integration);
        console.log('Storage path:', storagePath);
        console.log('JQL query:', JSON.stringify(include));
        console.log('Custom fields:', JSON.stringify(customFields));
        console.log('Include comments:', includeComments);
        
        // Use MCP tool for Mermaid index generation
        // This uses the MCP architecture similar to confluence, jira, kb tools
        try {
            console.log('Using MCP tool: mermaid_index_generate');
            
            // Prepare parameters for MCP tool
            const mcpParams = {
                integration: integration,
                storage_path: storagePath,
                include_patterns: include,
                exclude_patterns: exclude
            };
            
            // Add optional parameters only if they are provided
            if (customFields.length > 0) {
                mcpParams.custom_fields = customFields;
            }
            if (includeComments) {
                mcpParams.include_comments = includeComments;
            }
            
            // Call MCP tool
            const result = mermaid_index_generate(mcpParams);
            
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
                    jqlQuery: include[0],
                    customFields: customFields.length,
                    includeComments: includeComments
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


