/**
 * Confluence Mermaid Index
 * 
 * Automated processor for indexing Confluence content into Mermaid diagrams.
 * Fetches content from Confluence based on include/exclude patterns, generates
 * Mermaid diagrams, and stores them in a hierarchical file structure.
 * 
 * Usage:
 * - Via JSRunner job with integration, storagePath, include, and exclude parameters
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
 * 2. Create MermaidIndex instance
 * 3. Execute indexing
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
        
        // Access Java classes via bridge
        // Note: This assumes MermaidIndex and related classes are exposed via the JS bridge.
        // If not available, register them in the bridge configuration (see 'src/main/resources/bridge-config.json' or the BridgeModule in 'com.github.istin.dmtools.bridge.BridgeModule').
        // For example, refer to the documentation in 'docs/bridge-setup.md' for instructions.
        
        try {
            // Try to access Java classes
            const MermaidIndex = Java.type('com.github.istin.dmtools.index.mermaid.MermaidIndex');
            const MermaidDiagramGeneratorAgent = Java.type('com.github.istin.dmtools.ai.agent.MermaidDiagramGeneratorAgent');
            
            // Get Confluence instance from bridge (should be available via mcpWithKB)
            // For now, we'll need to get it from the bridge context
            // This may need to be adjusted based on how the bridge exposes Confluence
            
            // Create diagram generator agent
            const diagramGenerator = new MermaidDiagramGeneratorAgent();
            
            // Get Confluence - this should be available in the bridge context
            // If not directly available, we may need to inject it differently
            let confluence = null;
            try {
                // Try to get from global context if available
                if (confluence !== null) {
                    // Use existing confluence instance
                } else {
                    // Try to get from Java bridge
                    // This approach may need adjustment based on actual bridge implementation
                    console.warn('Confluence instance not directly available, may need bridge configuration');
                }
            } catch (e) {
                console.warn('Could not get Confluence instance:', e.message);
            }
            
            // Create MermaidIndex instance
            // Note: If Confluence is not available here, we may need to pass it differently
            // For now, we'll create the index and let it handle the Confluence requirement
            const mermaidIndex = new MermaidIndex(
                integration,
                storagePath,
                include,
                exclude,
                confluence,
                diagramGenerator
            );
            
            // Execute indexing
            console.log('Starting Mermaid indexing...');
            mermaidIndex.index();
            
            console.log('✅ Mermaid indexing completed successfully');
            
            return {
                success: true,
                message: 'Mermaid indexing completed successfully',
                integration: integration,
                storagePath: storagePath
            };
            
        } catch (javaError) {
            console.error('Java bridge error:', javaError);
            return {
                success: false,
                error: 'Failed to access Java classes: ' + javaError.toString()
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
