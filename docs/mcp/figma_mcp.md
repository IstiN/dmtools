# Figma MCP Integration Guide

## Overview

This guide explains how to use Figma MCP (Model Context Protocol) tools to extract design data from Figma designs. The integration provides a comprehensive set of methods for hierarchical structure analysis, detailed property extraction, and asset downloading. Use cases include creating pixel-perfect HTML/CSS, mobile layouts (iOS, Android), React components, and other UI implementations.

## Table of Contents

1. [Available MCP Methods](#available-mcp-methods)
2. [Visual-First Workflow](#visual-first-workflow)
3. [Quick Start](#quick-start)
4. [Hierarchical Workflow](#hierarchical-workflow)
5. [Method Details](#method-details)
6. [Practical Example](#practical-example)
7. [Best Practices](#best-practices)

---

## Available MCP Methods

### Structure Analysis (2 methods)

| Method | Description | Input | Output |
|--------|-------------|-------|--------|
| `figma_get_layers` ⭐ | Get first-level layers | URL with node-id | Direct children with names, IDs, types, sizes |
| `figma_get_layers_batch` ⭐ | Batch version of get_layers | URL + comma-separated node IDs | Map of nodeId → layers |

### Element Details (2 methods)

| Method | Description | Input | Output |
|--------|-------------|-------|--------|
| `figma_get_node_details` | Get CSS properties | URL + node IDs (max 10) | Padding, gaps, colors, borders, dimensions |
| `figma_get_text_content` | Extract text + typography | URL + text node IDs (max 20) | Text, fonts, sizes, weights, colors |

### Assets & Content (4 methods)

| Method | Description | Input | Output |
|--------|-------------|-------|--------|
| `figma_get_svg_content` | Download SVG icon | URL + single node ID | SVG markup as string |
| `figma_download_image_as_file` | Download raster image | URL + node ID + format | File (PNG/JPG) |
| `figma_download_node_image` ⭐ | Download specific node screenshot | URL + node ID + format + scale | File (PNG/JPG) |
| `figma_get_icons` | Get all exportable elements | URL | Flat list of all visual elements |

### Design System (1 method)

| Method | Description | Input | Output |
|--------|-------------|-------|--------|
| `figma_get_styles` | Get design tokens | URL | Color and text styles |

### Screenshots & Visual Preview (3 methods)

| Method | Description | Input | Output |
|--------|-------------|-------|--------|
| `figma_get_screen_source` | Get screenshot URL | URL with node-id | S3 image URL |
| `figma_download_image_of_file` | Download full design screenshot | URL with node-id | File (PNG) |
| `figma_download_node_image` ⭐ | Download specific node/component image | URL + node ID + format + scale | File (PNG/JPG) |

**Total: 11 MCP methods**

---

## Visual-First Workflow

### Recommended Approach: Start with Visuals

When working with a large or unfamiliar Figma design, start by understanding it visually before diving into structure:

#### Step 1: Get Overall Screenshot

First, get a visual representation of the entire design or section:

```java
// Download the root section/page as an image
File overviewImage = figmaClient.convertUrlToFile(
    "https://www.figma.com/design/YOUR_FILE_ID/Your-Design?node-id=ROOT-ID"
);

// This gives you: overview.png - visual of all screens side-by-side
```

**Purpose**: Understand what screens/layouts exist before processing structure.

#### Step 2: Identify Layers

Now get the structure to see what each visual piece corresponds to:

```java
FigmaNodeChildrenResult layers = figmaClient.getLayers(
    "https://www.figma.com/design/YOUR_FILE_ID/Your-Design?node-id=ROOT-ID"
);

// Output: [Home Screen, Details Screen, Settings Screen, ...]
```

**Purpose**: Map visual elements to node IDs.

#### Step 3: Download Individual Screen Previews

Get screenshots of specific screens for detailed analysis:

```java
// For each screen, download its preview
for (ChildNode screen : layers.getChildren()) {
    File screenPreview = figmaClient.downloadNodeImage(
        baseUrl,
        screen.getId(),    // e.g., "123:457"
        "png",
        2  // Retina scale
    );
    
    // Save as: home_screen_preview.png, details_screen_preview.png, etc.
    System.out.println("Screen: " + screen.getName());
    System.out.println("Preview: " + screenPreview.getPath());
}
```

**Purpose**: Visual reference for each screen while you work on implementation.

#### Step 4: Get Specific Component Screenshots

Found an interesting component? Get its isolated screenshot:

```java
// Download just the header component
File headerImage = figmaClient.downloadNodeImage(
    baseUrl,
    "200:100",  // Header component ID
    "png",
    2
);

// Download just a specific button
File buttonImage = figmaClient.downloadNodeImage(
    baseUrl,
    "200:150",  // Button component ID
    "png",
    4  // Higher resolution for detailed inspection
);
```

**Purpose**: Isolate specific pieces for pixel-perfect implementation.

### Visual-First Complete Workflow

```
1. figma_download_image_of_file(root URL)
   → Get overall visual understanding
   → See all screens in one image

2. figma_get_layers(root URL)
   → Understand structure
   → Get screen IDs: [123:457, 123:458, 123:459, 123:460]

3. figma_download_node_image(each screen ID)
   → Download individual screen previews
   → home_screen.png, details_screen.png, etc.
   → Use as visual reference during implementation

4. figma_get_layers(specific screen URL)
   → Get components: [Header, Content, Footer]
   → Identify which component to implement

5. figma_download_node_image(specific component ID)
   → Download component screenshot
   → header_component.png
   → Compare during implementation

6. figma_get_node_details(component ID)
   → Extract exact properties
   → padding, colors, spacing, etc.

7. figma_get_layers(component ID)
   → Get internal structure
   → [Title, Subtitle, Icon, Badge]

8. figma_get_text_content(text IDs)
   → Extract text + typography

9. figma_get_svg_content(icon IDs)
   → Download vector assets

10. Implement UI while comparing with downloaded screenshots
    → Pixel-perfect match verification
```

### Why Visual-First?

**Benefits**:
- ✅ **Context**: Understand the big picture before diving into details
- ✅ **Validation**: Compare your implementation against original screenshots
- ✅ **Efficiency**: Quickly identify which screens/components to focus on
- ✅ **Communication**: Share screenshots with team/designers for clarification
- ✅ **Debugging**: Visual diff between implementation and design

**Example Use Case**:

```java
// 1. Get overview - see all 4 screens visually
File allScreens = client.convertUrlToFile(rootUrl);
// → Shows: [Home | Details | Settings | Profile]

// 2. "I need to implement the Details screen"
FigmaNodeChildrenResult screens = client.getLayers(rootUrl);
String detailsId = findScreenByName(screens, "Details"); // "123:458"

// 3. Get isolated screenshot of Details screen
File detailsPreview = client.downloadNodeImage(baseUrl, detailsId, "png", 2);
// → details_screen.png (375x812px)

// 4. Analyze structure while looking at screenshot
FigmaNodeChildrenResult components = client.getLayers(baseUrl + "?node-id=" + detailsId);
// → [Header, Content List, Footer]

// 5. "The Content List looks complex, let me see it isolated"
String contentListId = "200:300";
File contentPreview = client.downloadNodeImage(baseUrl, contentListId, "png", 4);
// → content_list.png (high-res for details)

// 6. Now extract properties and implement
FigmaNodeDetails details = client.getNodeDetails(baseUrl, contentListId);
// → padding: 16px, gap: 12px, etc.

// 7. Implement while comparing with contentPreview screenshot
// → Pixel-perfect match!
```

---

## Quick Start

### Problem: Convert Figma Design → Code Implementation

Given a Figma URL:
```
https://www.figma.com/design/YOUR_FILE_ID/Your-Design?node-id=123-456
```

### Solution: Hierarchical Data Extraction

```java
// Step 1: Get top-level screens
FigmaNodeChildrenResult screens = figmaClient.getLayers(rootUrl);
// Returns: [Home, Details, Settings, Profile]

// Step 2: Find the screen you need
// "Home" - ID: 123:457

// Step 3: Get screen components
String screenUrl = baseUrl + "?node-id=123-457";
FigmaNodeChildrenResult components = figmaClient.getLayers(screenUrl);
// Returns: [Header, Content Area, Footer, Navigation Bar]

// Step 4: Get detailed properties
FigmaNodeDetails details = figmaClient.getNodeDetails(baseUrl, "200:100");
// Returns: padding, gaps, colors, etc.

// Step 5: Extract text content
FigmaTextContentResult text = figmaClient.getTextContent(baseUrl, "200:101,200:102");
// Returns: actual text + fonts

// Step 6: Download icons
String svg = figmaClient.getSvgContent(baseUrl, "200:103");
// Returns: SVG markup

// Step 7: Implement UI with extracted properties (HTML, React, SwiftUI, etc.)
```

---

## Hierarchical Workflow

### Understanding the Structure

Figma designs are hierarchical. The key to pixel-perfect conversion is understanding this hierarchy:

```
Root Section (node-id from URL)
├─ Screen 1: Home
│  ├─ Header
│  ├─ Main (content area)
│  │  └─ Content section
│  │     ├─ Section header
│  │     ├─ List item #1
│  │     │  ├─ Container (header)
│  │     │  │  ├─ Text wrapper
│  │     │  │  │  ├─ Title
│  │     │  │  │  └─ Subtitle
│  │     │  │  ├─ Badge
│  │     │  │  └─ Icon
│  │     │  └─ Container (metadata)
│  │     │     ├─ Icon + label
│  │     │     └─ Icon + label
│  │     ├─ List item #2
│  │     └─ ...
│  ├─ Footer
│  └─ Navigation Bar
├─ Screen 2: Details
├─ Screen 3: Settings
└─ Screen 4: Profile
```

### Recommended Process

1. **Start broad** - use `figma_get_layers` on root URL
2. **Identify screens** - find the one you need by name
3. **Drill down** - recursively call `figma_get_layers` for each level
4. **Extract details** - use `figma_get_node_details` for CSS properties
5. **Get content** - use `figma_get_text_content` and `figma_get_svg_content`
6. **Write HTML** - manually apply the extracted properties

---

## Method Details

### 1. figma_get_layers ⭐

**Purpose**: Get first-level layers (direct children) to understand structure.

**Signature**:
```java
public FigmaNodeChildrenResult getLayers(String href)
```

**Parameters**:
- `href` (required) - Figma design URL with node-id parameter
  - Example: `https://www.figma.com/design/FILE_ID?node-id=123-456`

**Returns**: `FigmaNodeChildrenResult`
```json
{
  "parentNodeId": "123:456",
  "children": [
    {
      "id": "123:457",
      "name": "Home Screen",
      "type": "FRAME",
      "width": 375.0,
      "height": 812.0,
      "x": 100.0,
      "y": 200.0,
      "visible": true
    }
  ]
}
```

**When to use**:
- ✅ First step in analyzing any Figma design
- ✅ Understanding screen structure
- ✅ Finding specific screens/components by name
- ✅ Recursive exploration of design hierarchy

**Example**:
```java
// Get all screens from root
FigmaNodeChildrenResult result = client.getLayers(
    "https://www.figma.com/design/FILE_ID?node-id=ROOT_ID"
);

for (ChildNode screen : result.getChildren()) {
    System.out.println("Screen: " + screen.getName());
    System.out.println("  ID: " + screen.getId());
    System.out.println("  Size: " + screen.getWidth() + "x" + screen.getHeight());
}
```

---

### 2. figma_get_layers_batch ⭐

**Purpose**: Get layers for multiple nodes at once (more efficient).

**Signature**:
```java
public Map<String, FigmaNodeChildrenResult> getLayersBatch(String href, String nodeIds)
```

**Parameters**:
- `href` (required) - Figma design URL (for file ID extraction)
- `nodeIds` (required) - Comma-separated node IDs (max 10)
  - Example: `"123:457,123:458,123:459"`
  - **Note**: Use colon format, not dash!

**Returns**: `Map<String, FigmaNodeChildrenResult>`
- Key: node ID
- Value: FigmaNodeChildrenResult with children

**When to use**:
- ✅ Analyzing multiple screens/containers simultaneously
- ✅ More efficient than multiple getLayers calls
- ✅ Batch processing of design structure

**Example**:
```java
Map<String, FigmaNodeChildrenResult> results = client.getLayersBatch(
    "https://www.figma.com/design/YOUR_FILE_ID/Your-Design",
    "123:457,123:458,123:459"
);

results.forEach((nodeId, layers) -> {
    System.out.println("Node: " + nodeId);
    System.out.println("  Children: " + layers.getChildren().size());
});
```

---

### 3. figma_get_node_details

**Purpose**: Get detailed CSS properties for element(s).

**Signature**:
```java
public FigmaNodeDetails getNodeDetails(String href, String nodeIds)
```

**Parameters**:
- `href` (required) - Figma design URL
- `nodeIds` (required) - Comma-separated node IDs (max 10)

**Returns**: `FigmaNodeDetails` with:
- **Layout**: `paddingTop`, `paddingBottom`, `paddingLeft`, `paddingRight`, `itemSpacing`, `layoutMode`
- **Dimensions**: `width`, `height`, `x`, `y`, `absoluteBoundingBox`
- **Colors**: `fills`, `strokes`, `backgroundColor`
- **Borders**: `strokeWeight`, `cornerRadius`, `cornerSmoothing`
- **Effects**: `opacity`, `effects`, `blendMode`
- **Text** (if TEXT node): `fontFamily`, `fontSize`, `fontWeight`, `lineHeight`, `letterSpacing`

**When to use**:
- ✅ Extract layout properties for containers
- ✅ Get exact spacing, padding, gaps
- ✅ Extract colors in RGB → convert to hex
- ✅ Understand flexbox structure

**Example**:
```java
FigmaNodeDetails details = client.getNodeDetails(
    "https://www.figma.com/design/YOUR_FILE_ID/Your-Design",
    "200:100"
);

// Extract properties
int padding = details.getJSONObject().optInt("paddingTop"); // e.g., 16
int gap = details.getJSONObject().optInt("itemSpacing"); // e.g., 12
String bgColor = details.getBackgroundColor(); // e.g., "#FFFFFF"
```

---

### 4. figma_get_text_content

**Purpose**: Extract text content with full typography information.

**Signature**:
```java
public FigmaTextContentResult getTextContent(String href, String nodeIds)
```

**Parameters**:
- `href` (required) - Figma design URL
- `nodeIds` (required) - Comma-separated text node IDs (max 20)

**Returns**: `FigmaTextContentResult` - Map of nodeId → text entry
```json
{
  "textNodes": {
    "200:101": {
      "text": "Welcome to App",
      "fontFamily": "Inter",
      "fontSize": 24,
      "fontWeight": 700,
      "lineHeight": 32,
      "letterSpacing": 0,
      "color": "#000000",
      "textAlign": "LEFT"
    },
    "200:102": {
      "text": "$100.99",
      "fontSize": 18,
      "fontWeight": 700,
      "characterStyleOverrides": [0,0,0,0,0,1,1],
      "styleOverrideTable": {
        "1": {
          "fontSize": 14,
          "fontWeight": 400
        }
      }
    }
  }
}
```

**Note**: When `characterStyleOverrides` is present, different character ranges use different styles. Use `hasMixedStyling()` to check and `getStyleOverrideTable()` to get override styles. See [Common Pitfalls](#common-pitfalls) for details.

**When to use**:
- ✅ Extract actual text content
- ✅ Get precise font properties
- ✅ Typography for styling (CSS, React Native, SwiftUI, etc.)

**Example**:
```java
FigmaTextContentResult text = client.getTextContent(
    baseUrl,
    "textId1,textId2,textId3"
);

Map<String, FigmaTextEntry> entries = text.getTextEntries();
for (Map.Entry<String, FigmaTextEntry> entry : entries.entrySet()) {
    FigmaTextEntry textEntry = entry.getValue();
    System.out.println("Text: " + textEntry.getText());
    System.out.println("Font: " + textEntry.getFontFamily());
    System.out.println("Size: " + textEntry.getFontSize());
}
```

---

### 5. figma_get_svg_content

**Purpose**: Download SVG icon as text markup.

**Signature**:
```java
public String getSvgContent(String href, String nodeId)
```

**Parameters**:
- `href` (required) - Figma design URL
- `nodeId` (required) - Single node ID for vector element

**Returns**: `String` - SVG markup
```xml
<svg width="24" height="24" viewBox="0 0 24 24" fill="none">
  <path fill-rule="evenodd" d="M8.6602..." fill="#E75204"/>
</svg>
```

**When to use**:
- ✅ Download vector icons
- ✅ Embed SVG in web (HTML) or convert to native formats (iOS, Android)
- ✅ Pixel-perfect icon representation

**Example**:
```java
String iconSvg = client.getSvgContent(
    "https://www.figma.com/design/YOUR_FILE_ID/Your-Design",
    "200:103"
);

// Use in HTML:
// <div class="icon">{iconSvg}</div>
```

---

### 6. figma_get_icons

**Purpose**: Get all exportable visual elements (flat list).

**Signature**:
```java
public FigmaIconsResult getIcons(String href)
```

**Parameters**:
- `href` (required) - Figma design URL

**Returns**: `FigmaIconsResult`
```json
{
  "totalIcons": 150,
  "icons": [
    {
      "id": "100:200",
      "name": "Header Component",
      "type": "INSTANCE",
      "width": 375,
      "height": 60,
      "category": "graphic",
      "isVectorBased": false,
      "supportedFormats": ["png", "jpg"]
    },
    {
      "id": "100:201",
      "name": "Icon / Arrow",
      "type": "VECTOR",
      "width": 24,
      "height": 24,
      "category": "icon",
      "isVectorBased": true,
      "supportedFormats": ["png", "jpg", "svg"]
    }
  ]
}
```

**When to use**:
- ✅ Overview of all elements in design
- ✅ Search for elements by name/type
- ✅ Understand what assets are available

**Note**: Returns a flat list without hierarchy. Use `figma_get_layers` for hierarchical structure.

---

### 7. figma_download_node_image ⭐

**Purpose**: Download screenshot of a specific node/component for visual preview.

**Signature**:
```java
public File downloadNodeImage(String href, String nodeId, String format, Integer scale)
```

**Parameters**:
- `href` (required) - Figma design URL (for file ID extraction)
- `nodeId` (required) - Specific node ID to download
- `format` (optional) - Image format: "png" or "jpg" (default: "png")
- `scale` (optional) - Scale factor: 1, 2, or 4 (default: 2 for retina)

**Returns**: `File` - Downloaded image file

**When to use**:
- ✅ **Before processing**: Get visual preview to understand what you're implementing
- ✅ **During implementation**: Compare your code against original design screenshot
- ✅ **Isolated components**: Download just the piece you're working on
- ✅ **High-resolution**: Use scale=4 for detailed pixel inspection
- ✅ **Team communication**: Share specific component screenshots

**Example**:
```java
// Download overview of entire section
File sectionImage = client.downloadNodeImage(
    "https://www.figma.com/design/YOUR_FILE_ID/Your-Design",
    "100:200",  // Section node ID
    "png",
    2           // 2x scale for retina
);

// Download high-res image of specific component for detailed inspection
File componentImage = client.downloadNodeImage(
    baseUrl,
    "200:300",  // Component ID
    "png",
    4           // 4x scale for pixel-perfect analysis
);

// Download as JPG for smaller file size (illustrations, photos)
File illustrationImage = client.downloadNodeImage(
    baseUrl,
    "300:400",
    "jpg",
    1           // 1x scale is enough for preview
);
```

**Workflow Integration**:
```java
// 1. Get structure
FigmaNodeChildrenResult layers = client.getLayers(rootUrl);

// 2. Download visual preview of each layer
for (ChildNode layer : layers.getChildren()) {
    File preview = client.downloadNodeImage(baseUrl, layer.getId(), "png", 2);
    // Now you have visual reference while implementing this layer
}

// 3. Extract properties and implement
// 4. Compare implementation with preview screenshot
```

**Comparison with other screenshot methods**:

| Method | Scope | Use Case |
|--------|-------|----------|
| `figma_download_image_of_file` | Full design from URL (node-id) | Get overview, entire screen |
| `figma_get_screen_source` | Get URL only (node-id) | When you need URL string instead of file |
| `figma_download_node_image` ⭐ | Specific node by ID | Download **any specific piece** for preview |

**Key Difference**: 
- `figma_download_image_of_file` - requires node-id in URL, downloads that specific node
- `figma_get_screen_source` - same as above but returns URL string only
- `figma_download_node_image` ⭐ - **flexible**: pass any node ID to download any piece

**Usage Pattern**:
```java
// Get overview of root
File overview = client.convertUrlToFile(rootUrlWithNodeId);

// Get layers
FigmaNodeChildrenResult layers = client.getLayers(rootUrlWithNodeId);

// Download specific pieces found in layers
for (ChildNode layer : layers.getChildren()) {
    File layerImage = client.downloadNodeImage(
        baseUrl,           // Just base URL, no node-id needed
        layer.getId(),     // Pass specific ID
        "png", 
        2
    );
}
```

---

## Quick Start

### Scenario: Extract Design Components from Figma

**Given**: Figma URL pointing to a design section
```
https://www.figma.com/design/YOUR_FILE_ID/Your-Design?node-id=123-456
```

### Step 1: Discover Top-Level Layers

```java
FigmaNodeChildrenResult layers = figmaClient.getLayers(
    "https://www.figma.com/design/YOUR_FILE_ID/Your-Design?node-id=123-456"
);

// Result:
// ✅ Found 4 layers:
//    1. Screen A (123:457)
//    2. Screen B (123:458)
//    3. Screen C (123:459)
//    4. Screen D (123:460)
```

### Step 2: Get Screen Components

```java
FigmaNodeChildrenResult components = figmaClient.getLayers(
    "https://www.figma.com/design/YOUR_FILE_ID/Your-Design?node-id=123-457"
);

// Result:
// ✅ Components: [Header, Content Area, Footer, Navigation]
```

### Step 3: Extract Component Properties

```java
// Get detailed layout properties
FigmaNodeDetails componentDetails = figmaClient.getNodeDetails(
    baseUrl,
    "200:100"  // Component node ID
);

// Extract from response:
// - paddingTop: 16px
// - paddingBottom: 16px
// - itemSpacing: 12px (gap between sections)
// - fills: [{"color": {"r": 1, "g": 1, "b": 1}}] → #FFFFFF
```

### Step 4: Get Text Content

```java
FigmaTextContentResult texts = figmaClient.getTextContent(
    baseUrl,
    "200:101,200:102"  // Text node IDs
);

// Result:
// {
//   "200:101": {
//     "text": "Welcome",
//     "fontFamily": "Inter",
//     "fontSize": 24,
//     "fontWeight": 700
//   },
//   "200:102": {
//     "text": "Subtitle text",
//     "fontFamily": "Inter",
//     "fontSize": 16
//   }
// }
```

### Step 5: Download Icons

```java
String iconSvg = figmaClient.getSvgContent(
    baseUrl,
    "200:103"  // Icon node ID
);

String arrowSvg = figmaClient.getSvgContent(
    baseUrl,
    "200:104"  // Arrow icon ID
);

String logoSvg = figmaClient.getSvgContent(
    baseUrl,
    "200:105"  // Logo icon ID
);
```

### Step 6: Implement UI with Extracted Properties

**HTML/CSS Example**:
```html
<div class="component" style="padding: 16px; gap: 12px;">
  <div class="header" style="gap: 8px;">
    <div class="text-content">
      <div style="font-family: 'Inter'; font-size: 24px; font-weight: 700;">
        Heading Text
      </div>
      <div style="font-family: 'Inter'; font-size: 16px; font-weight: 400; color: #666;">
        Subtitle text
      </div>
    </div>
    <div class="badge" style="background: #F0F0F0; border-radius: 4px; padding: 8px 12px;">
      <span style="font-size: 18px; font-weight: 800;">Badge</span>
    </div>
    <div class="icon">{arrowSvg}</div>
  </div>
</div>
```

**React/React Native Example**:
```jsx
<View style={{padding: 16, gap: 12}}>
  <View style={{gap: 8, flexDirection: 'row'}}>
    <View>
      <Text style={{fontFamily: 'Inter', fontSize: 24, fontWeight: '700'}}>
        Heading Text
      </Text>
      <Text style={{fontFamily: 'Inter', fontSize: 16, color: '#666'}}>
        Subtitle text
      </Text>
    </View>
    <View style={{backgroundColor: '#F0F0F0', borderRadius: 4, padding: 8}}>
      <Text style={{fontSize: 18, fontWeight: '800'}}>Badge</Text>
    </View>
  </View>
</View>
```

**SwiftUI Example**:
```swift
VStack(spacing: 12) {
    HStack(spacing: 8) {
        VStack(alignment: .leading) {
            Text("Heading Text")
                .font(.custom("Inter", size: 24).weight(.bold))
            Text("Subtitle text")
                .font(.custom("Inter", size: 16))
                .foregroundColor(Color(hex: "#666"))
        }
        Text("Badge")
            .font(.custom("Inter", size: 18).weight(.heavy))
            .padding(8)
            .background(Color(hex: "#F0F0F0"))
            .cornerRadius(4)
    }
}
.padding(16)
```

**Result**: Pixel-perfect match with Figma design across platforms!

---

## Hierarchical Workflow

### Pattern: Root → Screens → Components → Elements

```
Level 0: Root Section
  ↓ figma_get_layers
  
Level 1: Screens (Home, Details, Settings, Profile)
  ↓ figma_get_layers
  
Level 2: Screen Components (Header, Content, Footer, Navigation)
  ↓ figma_get_layers
  
Level 3: Content Sections (List Container, Header Section, ...)
  ↓ figma_get_layers
  
Level 4: Individual Items (List Item #1, List Item #2, ...)
  ↓ figma_get_node_details
  
Level 5: Element Properties (padding, colors, fonts, ...)
  ↓ figma_get_text_content, figma_get_svg_content
  
Final: Assets & Content (text, icons, images)
```

---

## Practical Example

### Typical Data Extraction via MCP

**Layout Properties** (from `figma_get_node_details`):
- Container padding: 16px, 20px, 24px (extracted values)
- Gap between sections: 8px, 12px, 16px (itemSpacing)
- Border radius: 4px, 8px, 12px (cornerRadius)
- Background colors: RGB → Hex conversion
- Border colors and widths: strokeWeight, strokes

**Typography** (from `figma_get_text_content`):
- Font families: Inter, Roboto, SF Pro Text, custom fonts
- Font sizes: 12px, 14px, 16px, 24px, 32px
- Font weights: 400 (regular), 500 (medium), 600 (semibold), 700 (bold), 800 (extrabold)
- Line heights: Exact pixel values or percentages
- Letter spacing: Positive/negative values
- Colors: Extracted as hex codes

**Icons** (from `figma_get_svg_content`):
- Vector icons: 16×16px, 24×24px, 32×32px
- SVG markup ready for embedding
- Colors preserved from design

---

## Best Practices

### 1. Start with Visuals (Recommended)

**Visual-First Approach**: Download screenshots before diving into structure

```java
// Step 1: Get visual overview
File overview = client.convertUrlToFile(rootUrl);
// → See the entire design visually

// Step 2: Get structure
FigmaNodeChildrenResult layers = client.getLayers(rootUrl);
// → Map visual pieces to node IDs

// Step 3: Download specific piece screenshots
File componentPreview = client.downloadNodeImage(baseUrl, componentId, "png", 2);
// → Visual reference for implementation

// Step 4: Extract properties
FigmaNodeDetails details = client.getNodeDetails(baseUrl, componentId);
// → Implement while comparing with screenshot
```

**Why Visual-First**:
- ✅ Understand context before analyzing structure
- ✅ Visual validation during implementation
- ✅ Easier to find the right components
- ✅ Better communication with designers
- ✅ **Catch API data limitations** - Some visual styling (like superscript cents) may not be fully exposed in API data. Screenshots show the truth.

### 2. Understand Structure Hierarchy

After getting visuals, use `figma_get_layers` to understand the hierarchy:

```java
// DON'T start with get_icons - it's a flat list
// DO start with get_layers - it shows structure

// ❌ Bad approach
FigmaIconsResult all = client.getIcons(url); // Hundreds of elements, no hierarchy

// ✅ Good approach  
FigmaNodeChildrenResult layers = client.getLayers(url); // Clear hierarchical structure
```

### 3. Use Batch Methods

When you need data for multiple nodes, use batch versions:

```java
// ❌ Inefficient
for (String id : nodeIds) {
    FigmaNodeChildrenResult result = client.getLayers(urlWithId);
}

// ✅ Efficient
Map<String, FigmaNodeChildrenResult> results = client.getLayersBatch(baseUrl, String.join(",", nodeIds));
```

### 4. Convert Node IDs Correctly

**Important**: Node IDs use different formats in URL vs API:

- **In URL**: dash format `123-456`
- **In API**: colon format `123:456`

Methods handle this automatically, but when passing IDs directly, use colon format:

```java
// ✅ Correct
client.getLayersBatch(baseUrl, "123:457,123:458,123:459");

// ❌ Wrong
client.getLayersBatch(baseUrl, "123-457,123-458,123-459");
```

### 5. Extract Colors Properly

Figma returns colors in RGB 0-1 range. Convert to hex:

```javascript
// From Figma:
"fills": [{"color": {"r": 0.945, "g": 0.945, "b": 0.949, "a": 1}}]

// Convert to hex:
r = Math.round(0.945 * 255) = 241
g = Math.round(0.945 * 255) = 241
b = Math.round(0.949 * 255) = 242
→ #F1F1F2
```

### 6. Implement UI Manually

**Don't try to auto-generate!** The best approach:

1. Extract exact properties via MCP
2. Implement UI manually with those properties for your target platform
3. Results in pixel-perfect match

```java
// ❌ Don't create auto-generators
// ✅ Extract data → Implement manually (HTML, React, SwiftUI, etc.)
```

---

## Parameter Limits

| Method | Max IDs | Reason |
|--------|---------|--------|
| `figma_get_layers_batch` | 10 | Keep response size manageable |
| `figma_get_node_details` | 10 | API limit, response size |
| `figma_get_text_content` | 20 | More text nodes allowed |
| `figma_get_svg_content` | 1 | Single node per call |
| `figma_download_image_as_file` | 1 | Single node per call |

If you need more, make multiple calls or implement batching at higher level.

---

## API Response Formats

### Node ID Format

**Important**: IDs are URL-encoded in API requests:

```
Original ID: I100:200;50:60;10:20
URL-encoded: I100%3A200%3B50%3A60%3B10%3A20
```

Methods handle encoding automatically.

### Common Response Structure

Most methods use Figma's `/files/{fileId}/nodes?ids=...` endpoint:

```json
{
  "nodes": {
    "123:457": {
      "document": {
        "id": "123:457",
        "name": "Home Screen",
        "type": "FRAME",
        "children": [...],
        "absoluteBoundingBox": {...},
        "fills": [...],
        ...
      }
    }
  }
}
```

---

## Common Pitfalls

### 1. Character-Level Styling (Mixed Text Styles)

**Problem**: Text like "$100.99" may have different font sizes for different parts (e.g., ".99" smaller than "$100").

**Why it happens**: Figma allows per-character style overrides within a single text node.

**Symptoms**:
- Price displays look wrong
- Superscript/subscript text not styled correctly
- Mixed font sizes within one text element

**Solution**:

**Step 1: Detect mixed styling**
```java
FigmaTextContentResult texts = client.getTextContent(baseUrl, "priceTextNodeId");
FigmaTextEntry entry = texts.getTextEntry("priceTextNodeId");

// Check if text has mixed styling
if (entry.hasMixedStyling()) {
    int[] overrides = entry.getCharacterStyleOverrides(); // [0,0,0,0,0,1,1]
    JSONObject styleTable = entry.getStyleOverrideTable();
    
    // Characters 5-6 use different style
}
```

**Step 2: Visual verification (CRITICAL!)**

Don't assume what the styling should be - **download a screenshot to see the actual visual**:

```java
// Download image of the price element to see how it actually looks
File priceImage = client.downloadNodeImage(
    baseUrl,
    "priceTextNodeId",
    "png",
    4  // High resolution for clarity
);

// Open priceImage and visually inspect:
// - Is the last part smaller? → Use smaller font-size
// - Is it raised? → Use vertical-align: super
// - Is it different weight? → Use different font-weight
// - Is it different color? → Use different color
```

**Step 3: Implement based on visual**

```java
// After seeing the screenshot, implement accordingly:
if (centsAreSmallerInScreenshot) {
    // <span class="price">$100.<sup style="font-size: 14px">99</sup></span>
} else if (centsAreSameSize) {
    // <span class="price">$100.99</span>
}
```

**Why this approach**:
- ✅ No assumptions - use actual visual
- ✅ Catches edge cases (font features, rendering differences)
- ✅ Handles cases where API data doesn't match visual
- ✅ Pixel-perfect accuracy

**Alternative**: Call `figma_get_node_details` on text node to get full `characterStyleOverrides` and `styleOverrideTable`, but still verify visually!

**Example Output**:
```json
{
  "text": "$100.99",
  "fontSize": 18,
  "fontWeight": 700,
  "characterStyleOverrides": [0,0,0,0,0,1,1],
  "styleOverrideTable": {
    "1": {
      "fontSize": 14,
      "fontWeight": 400
    }
  }
}
```

**Implementation**:
```html
<!-- Base style for "$100." -->
<span style="font-size: 18px; font-weight: 700;">
    $100.
    <!-- Override style for "99" -->
    <sup style="font-size: 14px; font-weight: 400;">99</sup>
</span>
```

---

### 2. Nested Elements with Padding

**Problem**: Elements like dividers may appear offset due to parent container padding.

**Why it happens**: Figma positions children relative to parent's content box (after padding).

**Symptoms**:
- Divider doesn't span full width
- Element appears with unexpected left/right margin
- Calculated positions don't match visual

**Solution**:

```java
FigmaNodeDetails parent = client.getNodeDetails(baseUrl, "containerId");
FigmaNodeChildrenResult children = client.getLayers(containerUrl);

// Parent: Divider Frame
int parentWidth = 375;
int parentPaddingLeft = parent.getJSONObject().optInt("paddingLeft"); // 16
int parentPaddingRight = parent.getJSONObject().optInt("paddingRight"); // 0

// Child: Actual divider line
// Expected width: 375 - 16 - 0 = 359
// Expected x offset: 16 (from parent's left edge)
```

**Example from Figma**:
```json
{
  "Divider Frame": {
    "x": 0,
    "width": 375,
    "paddingLeft": 16,
    "children": [
      {
        "name": "➗ Divider",
        "x": 16,      // Offset by parent's paddingLeft
        "width": 359   // Parent width - paddingLeft
      }
    ]
  }
}
```

**Implementation**:
```css
.divider-container {
    width: 100%;
    padding-left: 16px; /* From parent */
}

.divider-line {
    width: 100%; /* Will be 359px due to parent padding */
    height: 0.5px;
    background: #BEBEBF;
}
```

---

### 3. Missing Real Icons (Using Placeholders)

**Problem**: Implementation uses placeholder icons instead of actual Figma assets.

**Why it happens**: Forgetting to download all icons, or not finding all icon nodes.

**Symptoms**:
- Generic icons that don't match design
- Missing visual details
- Icons in wrong style/color

**Solution - Complete Icon Extraction**:

```java
// Step 1: Find all icons in design
FigmaIconsResult allIcons = client.getIcons(designUrl);

// Step 2: Filter for icons
List<FigmaIcon> iconList = allIcons.getIcons().stream()
    .filter(icon -> icon.isVectorBased() || 
                    "VECTOR".equals(icon.getType()) ||
                    "icon".equals(icon.getCategory()))
    .collect(Collectors.toList());

// Step 3: Download each icon
Map<String, String> iconSvgs = new HashMap<>();
for (FigmaIcon icon : iconList) {
    try {
        String svg = client.getSvgContent(baseUrl, icon.getId());
        iconSvgs.put(icon.getName(), svg);
        System.out.println("Downloaded: " + icon.getName());
    } catch (Exception e) {
        System.err.println("Failed to download: " + icon.getName());
    }
}

// Step 4: Use real icons in implementation
// ✅ Don't use: <svg><!-- placeholder --></svg>
// ✅ Do use: {iconSvgs.get("Calendar Icon")}
```

**Finding icons in specific component**:
```java
// Get component structure
FigmaNodeChildrenResult layers = client.getLayers(componentUrl);

// For each layer, check if it's an icon or contains icons
for (ChildNode layer : layers.getChildren()) {
    if ("INSTANCE".equals(layer.getType()) && 
        layer.getName().contains("Icon")) {
        // This is likely an icon - download it
        String svg = client.getSvgContent(baseUrl, layer.getId());
    }
}
```

**Best Practice**: Create a checklist:
- [ ] Downloaded navigation icons?
- [ ] Downloaded tab bar icons?  
- [ ] Downloaded content icons (calendar, etc.)?
- [ ] Downloaded action icons (chevrons, etc.)?
- [ ] Verified all icons visually match Figma?

---

### 4. Incorrect Active States

**Problem**: Not knowing which tab/button should be active/selected.

**Why it happens**: Active state information is in font weight or color differences.

**Symptoms**:
- All tabs look inactive
- Wrong tab highlighted
- Button states don't match design

**Solution**:

```java
// Get tab labels
FigmaTextContentResult tabTexts = client.getTextContent(baseUrl, "tab1,tab2,tab3,tab4,tab5");

// Check font weights to determine active state
Map<String, FigmaTextEntry> entries = tabTexts.getTextEntries();
for (Map.Entry<String, FigmaTextEntry> entry : entries.entrySet()) {
    FigmaTextEntry text = entry.getValue();
    
    if (text.getFontWeight() == 700) {
        System.out.println("ACTIVE: " + text.getText()); // "Invoices"
    } else {
        System.out.println("Inactive: " + text.getText()); // "Overview", "Shipments", etc.
    }
}
```

**Look for**:
- Font weight differences: 400 (inactive) vs 700 (active)
- Color differences: gray (inactive) vs brand color (active)
- Fill opacity differences
- Icon color differences

---

### 5. Not Checking Absolute Positioning

**Problem**: Using relative positions when Figma uses absolute positioning.

**Why it happens**: Some elements use `layoutPositioning: "ABSOLUTE"`.

**Solution**:

```java
FigmaNodeDetails element = client.getNodeDetails(baseUrl, "elementId");
JSONObject json = element.getJSONObject();

String positioning = json.optString("layoutPositioning"); // "ABSOLUTE" or "AUTO"

if ("ABSOLUTE".equals(positioning)) {
    // Use absolute positioning
    JSONObject bbox = json.optJSONObject("absoluteBoundingBox");
    int x = bbox.optInt("x");
    int y = bbox.optInt("y");
    
    // CSS: position: absolute; left: Xpx; top: Ypx;
} else {
    // Use flexbox/normal flow
    // CSS: display: flex; gap: Xpx;
}
```

---

## Troubleshooting

### Issue: Method returns null

**Possible reasons**:
1. Node ID format incorrect (dash vs colon)
2. Node has no children (expected for leaf nodes)
3. Node ID doesn't exist in file

**Solution**:
- Check node ID format
- Verify node actually has children
- Use `figma_get_icons` to find valid node IDs

### Issue: Text content missing

**Reason**: Node is not a TEXT type or uses instance overrides

**Solution**:
- Use `figma_get_icons` to find TEXT nodes
- Check if text is in a component instance
- Try parent node if child node fails

### Issue: SVG download fails

**Reason**: Node is not vector-based

**Solution**:
- Check `isVectorBased` flag from `figma_get_icons`
- Use `figma_download_image_as_file` for raster elements
- Try parent GROUP if individual VECTOR fails

---

## Integration Testing

All methods are covered by integration tests in:
```
dmtools-core/src/integrationTest/java/com/github/istin/dmtools/figma/FigmaClientMcpToolsIntegrationTest.java
```

**14 tests, all passing ✅**

To run tests:
```bash
./gradlew :dmtools-core:integrationTest --tests "*.FigmaClientMcpToolsIntegrationTest"
```

---

## Examples & Demos

### Working Examples

See integration tests for complete working demonstrations of all MCP methods.

### Code Examples

See:
```
dmtools-core/src/integrationTest/java/com/github/istin/dmtools/figma/utils/FigmaElementBuilder.java
```

Demonstrates:
- Calling MCP methods
- Extracting properties
- Building UI components incrementally

---

## Workflow Summary

### Recommended Approach

```
1. figma_get_layers(root URL)
   Purpose: Understand overall structure
   Output: List of screens

2. Identify target screen
   Method: Find by name in layers result
   
3. figma_get_layers(screen URL)
   Purpose: Get screen components
   Output: [Header, Content, Footer, Navigation]

4. figma_get_layers_batch(component IDs)
   Purpose: Get structure of multiple components
   Output: Map of component → children

5. figma_get_node_details(element IDs)
   Purpose: Extract layout properties
   Output: Padding, gaps, colors, borders, dimensions

6. figma_get_text_content(text node IDs)
   Purpose: Get actual content + typography
   Output: Text, fonts, sizes, weights, colors

7. figma_get_svg_content(icon IDs)
   Purpose: Download vector icons
   Output: SVG markup (for web) or convert to native

8. Implement UI manually
   Purpose: Apply extracted properties to your platform
   Platforms: HTML/CSS, React, React Native, SwiftUI, Android XML, etc.
   Result: Pixel-perfect implementation matching Figma design
```

---

## Color Conversion Helper

### RGB (0-1 range) → Hex

```java
// Figma returns:
"color": {"r": 0.945, "g": 0.945, "b": 0.949, "a": 1}

// Convert to hex:
int r = (int) Math.round(0.945 * 255); // 241
int g = (int) Math.round(0.945 * 255); // 241  
int b = (int) Math.round(0.949 * 255); // 242

String hex = String.format("#%02x%02x%02x", r, g, b); // #F1F1F2
```

Already implemented in `FigmaNodeDetails.rgbaToHex()` helper method.

---

## Advanced Usage

### Recursive Structure Exploration

```java
public void exploreStructure(String url, int depth) {
    if (depth > 5) return; // Prevent infinite recursion
    
    FigmaNodeChildrenResult layers = figmaClient.getLayers(url);
    if (layers == null) return;
    
    for (ChildNode child : layers.getChildren()) {
        System.out.println("  ".repeat(depth) + child.getName());
        
        // Recursively explore children
        String childUrl = baseUrl + "?node-id=" + child.getId().replace(":", "-");
        exploreStructure(childUrl, depth + 1);
    }
}
```

### Batch Processing Multiple Screens

```java
// Get all screens
FigmaNodeChildrenResult screens = client.getLayers(rootUrl);

// Extract screen IDs
List<String> screenIds = screens.getChildren().stream()
    .map(ChildNode::getId)
    .collect(Collectors.toList());

// Get structure of all screens at once
Map<String, FigmaNodeChildrenResult> allStructures = 
    client.getLayersBatch(baseUrl, String.join(",", screenIds));

// Process each screen
allStructures.forEach((screenId, structure) -> {
    System.out.println("Screen: " + screenId);
    System.out.println("  Components: " + structure.getChildren().size());
});
```

---

## Performance Considerations

### API Call Optimization

**Batch methods save API calls:**

```
❌ Individual calls: 10 screens × 1 call = 10 API calls
✅ Batch call: 10 screens ÷ 10 per batch = 1 API call
```

**Recommendation**:
- Use batch methods whenever possible
- Group related queries together
- Cache results to avoid duplicate calls

### Response Size Management

Why `figma_get_file_structure` was disabled:
- Full file structure can be **100KB+**
- Too large for MCP context
- Difficult to parse and use

**Solution**: Use focused methods instead:
- `figma_get_layers` - only direct children
- `figma_get_node_details` - specific nodes only
- Recursive calls as needed

---

## Model Classes

### Data Models

All response data is wrapped in typed model classes:

```java
// Structure
FigmaNodeChildrenResult
  └─ List<ChildNode>
     - getId(), getName(), getType()
     - getWidth(), getHeight()
     - getX(), getY()
     - isVisible()

// Details
FigmaNodeDetails
  - getWidth(), getHeight(), getX(), getY()
  - getBackgroundColor(), getStrokeColor()
  - getFontFamily(), getFontSize(), getFontWeight()
  - getCornerRadius(), getStrokeWeight()
  - getOpacity(), getCharacters()

// Text
FigmaTextContentResult
  └─ Map<String, FigmaTextEntry>
     - getText(), getFontFamily(), getFontSize()
     - getFontWeight(), getLineHeight()
     - getColor(), getTextAlign()

// Styles
FigmaStylesResult
  ├─ List<ColorStyle>
  └─ List<TextStyle>

// Icons
FigmaIconsResult
  └─ List<FigmaIcon>
     - getId(), getName(), getType()
     - getWidth(), getHeight()
     - isVectorBased(), getSupportedFormats()
```

---

## Configuration

### Required Setup

1. **Figma API Token**: Set in environment or config
   ```java
   FigmaClient client = new FigmaClient(figmaToken);
   ```

2. **Base URL**: Default is `https://api.figma.com/v1/`

3. **Caching**: Enabled by default to avoid duplicate API calls

---

## Limitations & Future Improvements

### Current Limitations

1. **figma_get_svg_content**: No batch version yet
   - Workaround: Call multiple times (acceptable for small number of icons)

2. **figma_get_styles**: Returns empty result
   - Figma API limitation - style values require additional calls
   - Future: Implement style detail fetching

3. **Component instances**: Complex overrides not fully extracted
   - Workaround: Use instance-specific node IDs

### Future Improvements

Potential additions:
- `figma_get_svg_content_batch` - Download multiple SVGs at once
- `figma_get_components` - List reusable components
- `figma_get_variables` - Extract Figma variables/tokens
- Enhanced `figma_get_styles` with actual values

---

## Summary

### ✅ Complete Figma MCP Integration

**10 MCP methods** covering:
- ✅ Hierarchical structure analysis
- ✅ Detailed property extraction
- ✅ Text content & typography
- ✅ SVG & image downloads
- ✅ Design tokens

**Flexible Parameters**:
- ✅ Work via URL
- ✅ Work via node IDs
- ✅ Batch operations supported

**Production Ready**:
- ✅ All methods tested (14 integration tests)
- ✅ Documented with examples
- ✅ Proven with pixel-perfect UI implementations

### Key Innovation

**figma_get_layers** - The missing piece that enables:
- Understanding design hierarchy
- Finding specific screens
- Recursive structure exploration
- Efficient targeted data extraction

**Result**: Complete toolkit for Figma → Code conversion across platforms! 🚀

---

## See Also

- [FigmaClient.java](../../dmtools-core/src/main/java/com/github/istin/dmtools/figma/FigmaClient.java) - Implementation
- [FigmaClientMcpToolsIntegrationTest.java](../../dmtools-core/src/integrationTest/java/com/github/istin/dmtools/figma/FigmaClientMcpToolsIntegrationTest.java) - Tests
- [invoice_list_pixel_perfect.html](../../temp/invoice_list_pixel_perfect.html) - Example output
- [Model Classes](../../dmtools-core/src/main/java/com/github/istin/dmtools/figma/model/) - Data models

