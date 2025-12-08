/**
 * Generate Mermaid Diagrams Agent
 * 
 * Generates Mermaid diagrams for Confluence pages recursively.
 * Uses MermaidIndex to process pages and their children.
 * 
 * This script can be used as a postJSAction in agent configurations.
 * It processes the specified Confluence page and all its children recursively.
 * 
 * @param {Object} params - Agent parameters
 * @param {Object} params.ticket - Jira ticket information (optional)
 * @param {string} params.response - AI response (not used in this agent)
 * @param {string} params.initiator - Initiator account ID
 * @param {Object} params.metadata - Metadata with contextId
 * @param {Object} params.jobParams - Job parameters
 * @param {string} params.jobParams.confluenceUrl - Confluence page URL (optional, defaults to Templates page)
 * @param {string} params.jobParams.storagePath - Storage path for diagrams (optional, defaults to ./mermaid-diagrams)
 * @returns {Object} Result with success status
 */
function action(params) {
    try {
        console.log('=== Generate Mermaid Diagrams Agent ===');
        
        // Get job parameters - can be from params.jobParams or parsed from string
        let jobParams = params.jobParams || {};
        if (typeof jobParams === 'string') {
            try {
                jobParams = JSON.parse(jobParams);
            } catch (e) {
                console.warn('Could not parse jobParams as JSON, using as-is');
            }
        }
        
        // Default to Templates page if not specified
        const confluenceUrl = jobParams.confluenceUrl || 
            'https://dmtools.atlassian.net/wiki/spaces/AINA/pages/11665522/Templates';
        
        // Default storage path
        const storagePath = jobParams.storagePath || './mermaid-diagrams';
        
        console.log('Confluence URL:', confluenceUrl);
        console.log('Storage path:', storagePath);
        
        // Extract page ID and space from URL
        // Format: https://dmtools.atlassian.net/wiki/spaces/{SPACE}/pages/{PAGE_ID}/{TITLE}
        const urlMatch = confluenceUrl.match(/\/spaces\/([^\/]+)\/pages\/(\d+)(?:\/([^\/\?]+))?/);
        if (!urlMatch) {
            return {
                success: false,
                error: 'Invalid Confluence URL format. Expected: https://.../wiki/spaces/{SPACE}/pages/{PAGE_ID}/{TITLE}'
            };
        }
        
        const spaceKey = urlMatch[1];
        const pageId = urlMatch[2];
        const pageName = urlMatch[3] || 'Templates'; // Extract from URL or use default
        
        console.log('Space:', spaceKey);
        console.log('Page ID:', pageId);
        console.log('Page Name:', pageName);
        
        // Build include pattern for recursive children
        // Format: {SPACE}/pages/{PAGE_ID}/{PAGE_NAME}/**
        const includePattern = `${spaceKey}/pages/${pageId}/${pageName}/**`;
        
        console.log('Include pattern:', includePattern);
        
        try {
            // Access Java classes via bridge
            const MermaidIndex = Java.type('com.github.istin.dmtools.index.mermaid.MermaidIndex');
            const MermaidDiagramGeneratorAgent = Java.type('com.github.istin.dmtools.ai.agent.MermaidDiagramGeneratorAgent');
            const BasicConfluence = Java.type('com.github.istin.dmtools.atlassian.confluence.BasicConfluence');
            
            // Get or create Confluence instance
            let confluence = null;
            try {
                // Try to get BasicConfluence instance
                confluence = BasicConfluence.getInstance();
                if (confluence === null) {
                    console.warn('BasicConfluence.getInstance() returned null, Confluence may not be configured');
                    return {
                        success: false,
                        error: 'Confluence is not configured. Please configure Confluence credentials.'
                    };
                }
                console.log('✅ Confluence instance obtained successfully');
            } catch (e) {
                console.error('Failed to get Confluence instance:', e);
                return {
                    success: false,
                    error: 'Failed to get Confluence instance: ' + e.toString()
                };
            }
            
            // Create diagram generator agent
            const diagramGenerator = new MermaidDiagramGeneratorAgent();
            console.log('✅ Diagram generator agent created');
            
            // Create MermaidIndex instance
            const mermaidIndex = new MermaidIndex(
                'confluence',
                storagePath,
                [includePattern],
                [],
                confluence,
                diagramGenerator
            );
            console.log('✅ MermaidIndex instance created');
            
            // Execute indexing
            console.log('Starting Mermaid diagram generation...');
            console.log('This will process the page and all its children recursively.');
            mermaidIndex.index();
            
            console.log('✅ Mermaid diagram generation completed successfully');
            
            return {
                success: true,
                message: `Mermaid diagrams generated successfully for ${confluenceUrl} and all children`,
                storagePath: storagePath,
                includePattern: includePattern,
                spaceKey: spaceKey,
                pageId: pageId,
                pageName: pageName
            };
            
        } catch (javaError) {
            console.error('Java bridge error:', javaError);
            console.error('Stack trace:', javaError.stack);
            return {
                success: false,
                error: 'Failed to access Java classes: ' + javaError.toString()
            };
        }
        
    } catch (error) {
        console.error('❌ Mermaid diagram generation failed:', error);
        console.error('Stack trace:', error.stack);
        return {
            success: false,
            error: error.toString()
        };
    }
}
