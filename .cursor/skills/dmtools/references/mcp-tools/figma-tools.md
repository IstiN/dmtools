# FIGMA MCP Tools

**Total Tools**: 12

## Quick Reference

```bash
# List all figma tools
dmtools list | jq '.tools[] | select(.name | startswith("figma_"))'

# Example usage
dmtools figma_get_screen_source [arguments]
```

## Usage in JavaScript Agents

```javascript
// Direct function calls for figma tools
const result = figma_get_screen_source(...);
const result = figma_download_node_image(...);
const result = figma_download_image_of_file(...);
```

## Available Tools

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `figma_download_image_as_file` | Download image as file by node ID and format. Use this after figma_get_icons to download actual icon files. | `format` (string, **required**)<br>`nodeId` (string, **required**)<br>`href` (string, **required**) |
| `figma_download_image_of_file` | Download image by URL as File type. Converts Figma design URL to downloadable image file. | `href` (string, **required**) |
| `figma_download_node_image` | Download image of specific node/component. Useful for visual preview of design pieces before processing structure. | `format` (string, optional)<br>`scale` (number, optional)<br>`href` (string, **required**)<br>`nodeId` (string, **required**) |
| `figma_get_icons` | Find and extract all exportable visual elements (vectors, shapes, graphics, text) from Figma design by URL. Focuses on actual visual elements to avoid complex component references. | `href` (string, **required**) |
| `figma_get_layers` | Get first-level layers (direct children) to understand structure. Returns layer names, IDs, types, sizes. Essential first step before getting details. | `href` (string, **required**) |
| `figma_get_layers_batch` | Get layers for multiple nodes at once. More efficient for analyzing multiple screens/containers. Returns map of nodeId to layers. | `nodeIds` (string, **required**)<br>`href` (string, **required**) |
| `figma_get_node_children` | Get immediate children IDs and basic info for a node. Non-recursive, returns only direct children. | `href` (string, **required**) |
| `figma_get_node_details` | Get detailed properties for specific node(s) including colors, fonts, text, dimensions, and styles. Returns small focused response. | `nodeIds` (string, **required**)<br>`href` (string, **required**) |
| `figma_get_screen_source` | Get screen source content by URL. Returns the image URL for the specified Figma design node. | `url` (string, **required**) |
| `figma_get_styles` | Get design tokens (colors, text styles) defined in Figma file. | `href` (string, **required**) |
| `figma_get_svg_content` | Get SVG content as text by node ID. Use this after figma_get_icons to get SVG code for vector icons. | `nodeId` (string, **required**)<br>`href` (string, **required**) |
| `figma_get_text_content` | Extract text content from text nodes. Returns map of nodeId to text content. | `nodeIds` (string, **required**)<br>`href` (string, **required**) |

## Detailed Parameter Information

### `figma_download_image_as_file`

Download image as file by node ID and format. Use this after figma_get_icons to download actual icon files.

**Parameters:**

- **`format`** (string) 🔴 Required
  - Export format
  - Example: `png`

- **`nodeId`** (string) 🔴 Required
  - Node ID to export (from figma_get_icons result)
  - Example: `123:456`

- **`href`** (string) 🔴 Required
  - Figma design URL to extract file ID from
  - Example: `https://www.figma.com/file/abc123/Design`

**Example:**
```bash
dmtools figma_download_image_as_file "value" "value"
```

```javascript
// In JavaScript agent
const result = figma_download_image_as_file("format", "nodeId");
```

---

### `figma_download_image_of_file`

Download image by URL as File type. Converts Figma design URL to downloadable image file.

**Parameters:**

- **`href`** (string) 🔴 Required
  - Figma design URL to download as image file
  - Example: `https://www.figma.com/file/abc123/Design?node-id=1%3A2`

**Example:**
```bash
dmtools figma_download_image_of_file "value"
```

```javascript
// In JavaScript agent
const result = figma_download_image_of_file("href");
```

---

### `figma_download_node_image`

Download image of specific node/component. Useful for visual preview of design pieces before processing structure.

**Parameters:**

- **`format`** (string) ⚪ Optional
  - Image format: png or jpg

- **`scale`** (number) ⚪ Optional
  - Scale factor: 1, 2, or 4

- **`href`** (string) 🔴 Required
  - Figma design URL

- **`nodeId`** (string) 🔴 Required
  - Node ID to download

**Example:**
```bash
dmtools figma_download_node_image "value" "value"
```

```javascript
// In JavaScript agent
const result = figma_download_node_image("format", "scale");
```

---

### `figma_get_icons`

Find and extract all exportable visual elements (vectors, shapes, graphics, text) from Figma design by URL. Focuses on actual visual elements to avoid complex component references.

**Parameters:**

- **`href`** (string) 🔴 Required
  - Figma design URL to extract visual elements from
  - Example: `https://www.figma.com/file/abc123/Design`

**Example:**
```bash
dmtools figma_get_icons "value"
```

```javascript
// In JavaScript agent
const result = figma_get_icons("href");
```

---

### `figma_get_layers`

Get first-level layers (direct children) to understand structure. Returns layer names, IDs, types, sizes. Essential first step before getting details.

**Parameters:**

- **`href`** (string) 🔴 Required
  - Figma design URL with node-id

**Example:**
```bash
dmtools figma_get_layers "value"
```

```javascript
// In JavaScript agent
const result = figma_get_layers("href");
```

---

### `figma_get_layers_batch`

Get layers for multiple nodes at once. More efficient for analyzing multiple screens/containers. Returns map of nodeId to layers.

**Parameters:**

- **`nodeIds`** (string) 🔴 Required
  - Comma-separated node IDs (max 10)

- **`href`** (string) 🔴 Required
  - Figma design URL

**Example:**
```bash
dmtools figma_get_layers_batch "value" "value"
```

```javascript
// In JavaScript agent
const result = figma_get_layers_batch("nodeIds", "href");
```

---

### `figma_get_node_children`

Get immediate children IDs and basic info for a node. Non-recursive, returns only direct children.

**Parameters:**

- **`href`** (string) 🔴 Required
  - Figma design URL with node-id

**Example:**
```bash
dmtools figma_get_node_children "value"
```

```javascript
// In JavaScript agent
const result = figma_get_node_children("href");
```

---

### `figma_get_node_details`

Get detailed properties for specific node(s) including colors, fonts, text, dimensions, and styles. Returns small focused response.

**Parameters:**

- **`nodeIds`** (string) 🔴 Required
  - Comma-separated node IDs (max 10)

- **`href`** (string) 🔴 Required
  - Figma design URL

**Example:**
```bash
dmtools figma_get_node_details "value" "value"
```

```javascript
// In JavaScript agent
const result = figma_get_node_details("nodeIds", "href");
```

---

### `figma_get_screen_source`

Get screen source content by URL. Returns the image URL for the specified Figma design node.

**Parameters:**

- **`url`** (string) 🔴 Required
  - Figma design URL with node-id parameter
  - Example: `https://www.figma.com/file/abc123/Design?node-id=1%3A2`

**Example:**
```bash
dmtools figma_get_screen_source "value"
```

```javascript
// In JavaScript agent
const result = figma_get_screen_source("url");
```

---

### `figma_get_styles`

Get design tokens (colors, text styles) defined in Figma file.

**Parameters:**

- **`href`** (string) 🔴 Required
  - Figma design URL

**Example:**
```bash
dmtools figma_get_styles "value"
```

```javascript
// In JavaScript agent
const result = figma_get_styles("href");
```

---

### `figma_get_svg_content`

Get SVG content as text by node ID. Use this after figma_get_icons to get SVG code for vector icons.

**Parameters:**

- **`nodeId`** (string) 🔴 Required
  - Node ID to export as SVG (from figma_get_icons result)
  - Example: `123:456`

- **`href`** (string) 🔴 Required
  - Figma design URL to extract file ID from
  - Example: `https://www.figma.com/file/abc123/Design`

**Example:**
```bash
dmtools figma_get_svg_content "value" "value"
```

```javascript
// In JavaScript agent
const result = figma_get_svg_content("nodeId", "href");
```

---

### `figma_get_text_content`

Extract text content from text nodes. Returns map of nodeId to text content.

**Parameters:**

- **`nodeIds`** (string) 🔴 Required
  - Comma-separated text node IDs (max 20)

- **`href`** (string) 🔴 Required
  - Figma design URL

**Example:**
```bash
dmtools figma_get_text_content "value" "value"
```

```javascript
// In JavaScript agent
const result = figma_get_text_content("nodeIds", "href");
```

---

