/**
 * Comprehensive Confluence MCP Methods Examples
 * 
 * This file demonstrates all available Confluence MCP tools with example usage.
 * Use this as a reference for implementing Confluence operations in DMTools jobs.
 * 
 * Available in JavaScript context: 16 Confluence MCP tools
 * Usage: Copy relevant examples to your postJSAction parameter
 */

function action(params) {
    try {
        console.log("=== CONFLUENCE MCP EXAMPLES ===");
        console.log("Processing ticket:", params.ticket.key);
        
        const results = {};
        const ticketKey = params.ticket.key;
        const testSpaceKey = "TEST"; // Change to your test space
        
        // ===== USER PROFILE =====
        
        // Get current user profile
        console.log("1. Getting current user profile...");
        const currentUser = confluence_get_current_user_profile({
            random_string: "dummy" // Required parameter for no-param tools
        });
        results.currentUser = currentUser;
        
        // Get user profile by ID
        if (currentUser && currentUser.accountId) {
            console.log("2. Getting user profile by ID...");
            const userProfile = confluence_get_user_profile_by_id({
                userId: currentUser.accountId
            });
            results.userProfile = userProfile;
        }
        
        // ===== CONTENT SEARCH =====
        
        // Search content by text
        console.log("3. Searching content by text...");
        const searchResults = confluence_search_content_by_text({
            query: ticketKey, // Search for our ticket key
            limit: 10
        });
        results.searchResults = searchResults;
        
        // Find content by title and space
        console.log("4. Finding content by title and space...");
        const contentByTitle = confluence_find_content_by_title_and_space({
            title: "Home", // Common page title
            space: testSpaceKey
        });
        results.contentByTitle = contentByTitle;
        
        // Find content by title (default space)
        console.log("5. Finding content by title...");
        const contentByTitleDefault = confluence_find_content({
            title: "Home"
        });
        results.contentByTitleDefault = contentByTitleDefault;
        
        // Get content by title (default space)
        console.log("6. Getting content by title...");
        const contentByTitleGet = confluence_content_by_title({
            title: "Home"
        });
        results.contentByTitleGet = contentByTitleGet;
        
        // ===== CONTENT RETRIEVAL =====
        
        let testPageId = null;
        let testParentId = null;
        
        // Get content by title and space (detailed)
        console.log("7. Getting content by title and space...");
        const detailedContent = confluence_content_by_title_and_space({
            title: "Home",
            space: testSpaceKey
        });
        results.detailedContent = detailedContent;
        
        if (detailedContent && detailedContent.id) {
            testPageId = detailedContent.id;
            testParentId = detailedContent.id; // Use as parent for new pages
            
            // Get content by ID
            console.log("8. Getting content by ID...");
            const contentById = confluence_content_by_id({
                contentId: testPageId
            });
            results.contentById = contentById;
            
            // Get content attachments
            console.log("9. Getting content attachments...");
            const attachments = confluence_get_content_attachments({
                contentId: testPageId
            });
            results.attachments = attachments;
            
            // Get children by ID
            console.log("10. Getting children by ID...");
            const childrenById = confluence_get_children_by_id({
                contentId: testPageId
            });
            results.childrenById = childrenById;
        }
        
        // Get children by name
        console.log("11. Getting children by name...");
        const childrenByName = confluence_get_children_by_name({
            spaceKey: testSpaceKey,
            contentName: "Home"
        });
        results.childrenByName = childrenByName;
        
        // ===== CONTENT RETRIEVAL BY URLS =====
        
        // Get contents by URLs (example with multiple URLs)
        console.log("12. Getting contents by URLs...");
        const urlContents = confluence_contents_by_urls({
            urlStrings: [
                `https://your-confluence-domain.atlassian.net/wiki/spaces/${testSpaceKey}/pages/123456/Home`,
                // Add more URLs as needed
            ]
        });
        results.urlContents = urlContents;
        
        // ===== CONTENT CREATION =====
        
        if (testParentId) {
            // Create new page
            console.log("13. Creating new page...");
            const newPageTitle = `JavaScript MCP Example - ${ticketKey} - ${new Date().toISOString()}`;
            const newPageBody = `
                <h1>JavaScript MCP Example Page</h1>
                <p>This page was created by JavaScript MCP example to demonstrate Confluence integration.</p>
                <p><strong>Related Ticket:</strong> ${ticketKey}</p>
                <p><strong>Created:</strong> ${new Date().toISOString()}</p>
                
                <h2>Ticket Information</h2>
                <ul>
                    <li><strong>Key:</strong> ${params.ticket.key}</li>
                    <li><strong>Title:</strong> ${params.ticket.title || 'N/A'}</li>
                    <li><strong>Status:</strong> ${params.ticket.status || 'N/A'}</li>
                </ul>
                
                <h2>MCP Integration Test</h2>
                <p>This page demonstrates successful integration between DMTools JavaScript bridge and Confluence MCP tools.</p>
            `;
            
            const newPage = confluence_create_page({
                title: newPageTitle,
                parentId: testParentId,
                body: newPageBody,
                space: testSpaceKey
            });
            results.newPage = newPage;
            
            // ===== CONTENT UPDATE =====
            
            if (newPage && newPage.id) {
                // Update page
                console.log("14. Updating page...");
                const updatedBody = newPageBody + `
                    <h2>Update Information</h2>
                    <p><strong>Updated:</strong> ${new Date().toISOString()}</p>
                    <p>This content was added during the update operation.</p>
                `;
                
                const updateResult = confluence_update_page({
                    contentId: newPage.id,
                    title: newPageTitle + " (Updated)",
                    parentId: testParentId,
                    body: updatedBody,
                    space: testSpaceKey
                });
                results.updateResult = updateResult;
                
                // Update page with history comment
                console.log("15. Updating page with history...");
                const historyUpdateBody = updatedBody + `
                    <h2>History Update</h2>
                    <p>This update includes a history comment for tracking changes.</p>
                `;
                
                const historyUpdate = confluence_update_page_with_history({
                    contentId: newPage.id,
                    title: newPageTitle + " (History Update)",
                    parentId: testParentId,
                    body: historyUpdateBody,
                    space: testSpaceKey,
                    historyComment: `Updated by JavaScript MCP example for ticket ${ticketKey}`
                });
                results.historyUpdate = historyUpdate;
            }
            
            // ===== FIND OR CREATE =====
            
            // Find or create page
            console.log("16. Find or create page...");
            const findOrCreateTitle = `JavaScript MCP Persistent Page - ${testSpaceKey}`;
            const findOrCreateBody = `
                <h1>Persistent Test Page</h1>
                <p>This page is created once and then found on subsequent runs.</p>
                <p><strong>Last accessed:</strong> ${new Date().toISOString()}</p>
                <p><strong>From ticket:</strong> ${ticketKey}</p>
            `;
            
            const findOrCreateResult = confluence_find_or_create({
                title: findOrCreateTitle,
                parentId: testParentId,
                body: findOrCreateBody
            });
            results.findOrCreateResult = findOrCreateResult;
        }
        
        console.log("=== CONFLUENCE MCP EXAMPLES COMPLETED ===");
        
        return {
            success: true,
            message: `Successfully executed Confluence MCP examples for ticket ${ticketKey}`,
            results: results,
            totalOperations: Object.keys(results).length,
            notes: [
                "Some operations may fail if the test space doesn't exist",
                "Update testSpaceKey variable to match your Confluence space",
                "Created pages will remain in Confluence - clean up as needed"
            ]
        };
        
    } catch (error) {
        console.error("Error in Confluence MCP examples:", error);
        return {
            success: false,
            error: error.toString(),
            message: "Failed to execute Confluence MCP examples",
            troubleshooting: [
                "Check if Confluence is properly configured",
                "Verify space permissions",
                "Ensure test space exists",
                "Check authentication credentials"
            ]
        };
    }
}

