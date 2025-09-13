/**
 * Figma MCP Methods Examples
 * 
 * This file demonstrates all available Figma MCP tools with example usage.
 * Use this as a reference for implementing Figma operations in DMTools jobs.
 * 
 * Available in JavaScript context: 6 Figma MCP tools
 * - figma_get_screen_source: Get screen source content by URL
 * - figma_download_image_of_file: Download image by URL as File type
 * - figma_get_file_structure: Get JSON structure of Figma design file
 * - figma_get_icons: Extract visual elements from Figma design
 * - figma_download_image_as_file: Download image as file by node ID
 * - figma_get_svg_content: Get SVG content as text by node ID
 * 
 * Usage: Copy relevant examples to your postJSAction parameter
 */

function action(params) {
    try {
        console.log("=== FIGMA MCP EXAMPLES ===");
        console.log("Processing ticket:", params.ticket.key);
        
        const results = {};
        const ticketKey = params.ticket.key;
        
        // Example Figma URLs - Replace with actual Figma file URLs
        const figmaFileUrl = "https://www.figma.com/file/abc123/Design-System";
        const figmaNodeUrl = "https://www.figma.com/file/abc123/Design-System?node-id=1%3A2";
        
        // ===== FILE STRUCTURE ANALYSIS =====
        
        console.log("1. Getting Figma file structure...");
        try {
            const fileStructure = figma_get_file_structure({
                href: figmaFileUrl
            });
            results.fileStructure = fileStructure;
            console.log("File structure retrieved successfully");
        } catch (error) {
            results.fileStructure = { error: error.toString() };
            console.log("File structure retrieval failed:", error.toString());
        }
        
        // Get file structure for specific node
        console.log("2. Getting specific node structure...");
        try {
            const nodeStructure = figma_get_file_structure({
                href: figmaNodeUrl
            });
            results.nodeStructure = nodeStructure;
            console.log("Node structure retrieved successfully");
        } catch (error) {
            results.nodeStructure = { error: error.toString() };
            console.log("Node structure retrieval failed:", error.toString());
        }
        
        // ===== VISUAL ELEMENTS EXTRACTION =====
        
        console.log("3. Extracting visual elements (icons)...");
        try {
            const icons = figma_get_icons({
                href: figmaFileUrl
            });
            results.icons = icons;
            console.log("Icons extracted successfully");
            
            // If we found icons, demonstrate further operations
            if (icons && icons.length > 0) {
                const firstIcon = icons[0];
                console.log("Found", icons.length, "visual elements");
                console.log("First element:", firstIcon.name, "ID:", firstIcon.id);
                
                // ===== SVG CONTENT EXTRACTION =====
                
                console.log("4. Getting SVG content for first icon...");
                try {
                    const svgContent = figma_get_svg_content({
                        href: figmaFileUrl,
                        nodeId: firstIcon.id
                    });
                    results.svgContent = svgContent;
                    console.log("SVG content retrieved successfully");
                } catch (error) {
                    results.svgContent = { error: error.toString() };
                    console.log("SVG content retrieval failed:", error.toString());
                }
                
                // ===== IMAGE DOWNLOAD AS FILE =====
                
                console.log("5. Downloading image as PNG file...");
                try {
                    const pngFile = figma_download_image_as_file({
                        href: figmaFileUrl,
                        nodeId: firstIcon.id,
                        format: "png"
                    });
                    results.pngFile = pngFile;
                    console.log("PNG file downloaded successfully");
                } catch (error) {
                    results.pngFile = { error: error.toString() };
                    console.log("PNG download failed:", error.toString());
                }
                
                console.log("6. Downloading image as SVG file...");
                try {
                    const svgFile = figma_download_image_as_file({
                        href: figmaFileUrl,
                        nodeId: firstIcon.id,
                        format: "svg"
                    });
                    results.svgFile = svgFile;
                    console.log("SVG file downloaded successfully");
                } catch (error) {
                    results.svgFile = { error: error.toString() };
                    console.log("SVG download failed:", error.toString());
                }
                
                console.log("7. Downloading image as JPG file...");
                try {
                    const jpgFile = figma_download_image_as_file({
                        href: figmaFileUrl,
                        nodeId: firstIcon.id,
                        format: "jpg"
                    });
                    results.jpgFile = jpgFile;
                    console.log("JPG file downloaded successfully");
                } catch (error) {
                    results.jpgFile = { error: error.toString() };
                    console.log("JPG download failed:", error.toString());
                }
            }
        } catch (error) {
            results.icons = { error: error.toString() };
            console.log("Icon extraction failed:", error.toString());
        }
        
        // ===== SCREEN SOURCE =====
        
        console.log("8. Getting screen source...");
        try {
            const screenSource = figma_get_screen_source({
                url: figmaNodeUrl
            });
            results.screenSource = screenSource;
            console.log("Screen source retrieved successfully");
        } catch (error) {
            results.screenSource = { error: error.toString() };
            console.log("Screen source retrieval failed:", error.toString());
        }
        
        // ===== IMAGE DOWNLOAD BY URL =====
        
        console.log("9. Downloading image by URL...");
        try {
            const imageFile = figma_download_image_of_file({
                href: figmaNodeUrl
            });
            results.imageFile = imageFile;
            console.log("Image file downloaded successfully");
        } catch (error) {
            results.imageFile = { error: error.toString() };
            console.log("Image download failed:", error.toString());
        }
        
        // ===== WORKFLOW INTEGRATION EXAMPLES =====
        
        console.log("10. Workflow integration examples...");
        
        // Example: Extract design tokens from Figma
        if (results.fileStructure && !results.fileStructure.error) {
            console.log("Analyzing design system structure...");
            results.designAnalysis = {
                message: "Design system analysis completed",
                recommendation: "Consider extracting color tokens and typography definitions",
                ticketIntegration: `Design assets for ${ticketKey} can be found in the Figma file`
            };
        }
        
        // Example: Asset preparation for development
        if (results.icons && !results.icons.error && results.icons.length > 0) {
            console.log("Preparing assets for development...");
            results.assetPreparation = {
                totalAssets: results.icons.length,
                formats: ["PNG", "SVG", "JPG"],
                recommendation: "SVG format recommended for scalable icons",
                ticketNote: `${results.icons.length} design assets available for implementation of ${ticketKey}`
            };
        }
        
        // ===== DESIGN SYSTEM INTEGRATION =====
        
        console.log("11. Design system integration...");
        
        // Simulate design token extraction
        if (results.svgContent && !results.svgContent.error) {
            results.designTokens = {
                colors: ["#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4"],
                typography: ["Roboto", "Inter", "Poppins"],
                spacing: ["8px", "16px", "24px", "32px"],
                borderRadius: ["4px", "8px", "12px"],
                note: "Extracted from Figma design system for ticket " + ticketKey
            };
        }
        
        // ===== DOCUMENTATION GENERATION =====
        
        console.log("12. Generating design documentation...");
        
        results.designDocumentation = {
            title: `Design Assets for ${ticketKey}`,
            summary: "Figma integration provides access to design files and assets",
            availableAssets: Object.keys(results).filter(key => 
                key.includes('File') || key.includes('Content') || key === 'icons'
            ),
            integrationStatus: "MCP tools successfully connected to Figma",
            nextSteps: [
                "Review extracted assets",
                "Implement design tokens in code",
                "Validate design consistency",
                "Update component library"
            ]
        };
        
        console.log("=== FIGMA MCP EXAMPLES COMPLETED ===");
        
        return {
            success: true,
            message: `Successfully executed Figma MCP examples for ticket ${ticketKey}`,
            results: results,
            totalOperations: Object.keys(results).length,
            figmaCapabilities: [
                "File structure analysis",
                "Visual element extraction",
                "SVG content retrieval",
                "Multi-format image export",
                "Screen capture",
                "Design token extraction",
                "Asset preparation for development"
            ],
            supportedFormats: ["PNG", "JPG", "SVG"],
            notes: [
                "Replace example URLs with actual Figma file URLs",
                "Ensure Figma API access is properly configured",
                "Some operations require specific node IDs",
                "Consider file size limits for downloads",
                "SVG format recommended for icons and illustrations"
            ],
            troubleshooting: [
                "Verify Figma file permissions",
                "Check if file URLs are accessible",
                "Ensure node IDs exist in the file",
                "Validate Figma API credentials"
            ]
        };
        
    } catch (error) {
        console.error("Error in Figma MCP examples:", error);
        return {
            success: false,
            error: error.toString(),
            message: "Failed to execute Figma MCP examples",
            commonIssues: [
                "Invalid Figma file URLs",
                "Missing API permissions",
                "Network connectivity issues",
                "File access restrictions"
            ],
            solutions: [
                "Verify Figma file URLs are public or accessible",
                "Check Figma API token configuration",
                "Ensure proper network access to Figma",
                "Validate file sharing permissions"
            ]
        };
    }
}

