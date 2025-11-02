# MCP (Model Context Protocol) Documentation

This directory contains documentation for MCP tool integrations.

## Available Integrations

### Figma MCP Integration

**File**: [figma_mcp.md](./figma_mcp.md)

**Description**: Complete guide for using Figma MCP tools to extract design data and create pixel-perfect UI implementations (HTML/CSS, React, React Native, SwiftUI, Android, etc.).

**Key Features**:
- Hierarchical structure analysis
- Property extraction (CSS, typography, colors)
- Asset downloading (SVG, PNG, JPG)
- Batch operations for efficiency
- URL and node ID support

**Methods**: 10 MCP tools covering:
- Structure: `figma_get_layers`, `figma_get_layers_batch`
- Details: `figma_get_node_details`, `figma_get_text_content`
- Assets: `figma_get_svg_content`, `figma_download_image_as_file`, `figma_get_icons`
- Design Tokens: `figma_get_styles`
- Screenshots: `figma_get_screen_source`, `figma_download_image_of_file`

**Use Cases**:
- HTML/CSS web layouts
- React/React Native mobile apps
- SwiftUI iOS applications
- Android XML layouts
- Any UI platform that needs Figma design properties

**Examples**:
- [Integration Tests](../../dmtools-core/src/integrationTest/java/com/github/istin/dmtools/figma/FigmaClientMcpToolsIntegrationTest.java) - 14 tests demonstrating usage

---

## Quick Links

- **Figma Integration**: [figma_mcp.md](./figma_mcp.md) - Complete guide
- **Source Code**: [FigmaClient.java](../../dmtools-core/src/main/java/com/github/istin/dmtools/figma/FigmaClient.java)
- **Tests**: [FigmaClientMcpToolsIntegrationTest.java](../../dmtools-core/src/integrationTest/java/com/github/istin/dmtools/figma/FigmaClientMcpToolsIntegrationTest.java)
- **Models**: [figma/model/](../../dmtools-core/src/main/java/com/github/istin/dmtools/figma/model/)

---

## Future Integrations

This directory will contain documentation for other MCP integrations as they are added:
- Jira MCP
- Confluence MCP
- Teams MCP
- And more...

---

## Contributing

When adding new MCP integrations:

1. Create a new `{service}_mcp.md` file in this directory
2. Follow the structure used in `figma_mcp.md`:
   - Overview of available methods
   - Quick start guide
   - Detailed method documentation
   - Practical examples
   - Best practices
   - Troubleshooting
3. Add integration tests
4. Update this README

---

## MCP Protocol

MCP (Model Context Protocol) tools are annotated with `@MCPTool` and automatically discovered by the MCP annotation processor during compilation.

### How it works:

1. Add `@MCPTool` annotation to public methods
2. Define `@MCPParam` for parameters
3. Compile - annotation processor generates MCP infrastructure
4. Tools are automatically exposed via MCP server

### Example:

```java
@MCPTool(
    name = "figma_get_layers",
    description = "Get first-level layers to understand structure",
    integration = "figma",
    category = "structure_analysis"
)
public FigmaNodeChildrenResult getLayers(
    @MCPParam(name = "href", description = "Figma design URL", required = true) 
    String href
) throws Exception {
    // Implementation
}
```

See [figma_mcp.md](./figma_mcp.md) for complete examples.

