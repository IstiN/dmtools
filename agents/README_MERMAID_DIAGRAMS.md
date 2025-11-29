# Mermaid Diagrams Generator Agent

This agent generates Mermaid diagrams for Confluence pages and all their children recursively.

## Overview

The agent processes a specified Confluence page and generates Mermaid diagrams for:
- The main page
- All child pages (recursively)
- Stores diagrams in a hierarchical file structure matching the page hierarchy

## Configuration

### JSON Configuration File

The agent configuration is in `agents/mermaid_diagrams_generator.json`:

```json
{
  "name" : "Teammate",
  "params" : {
    "metadata" : {
      "contextId" : "mermaid_diagrams_generator"
    },
    "agentParams" : {
      "aiRole" : "Mermaid Diagram Generator",
      "instructions" : [
        "Generate Mermaid diagrams for Confluence pages and their children recursively.",
        "The agent processes the specified Confluence page and all its child pages to create visual Mermaid diagrams.",
        "Diagrams are stored in a hierarchical file structure matching the page hierarchy."
      ]
    },
    "skipAIProcessing" : true,
    "preJSAction" : "agents/js/checkWipLabel.js",
    "postJSAction" : "agents/js/generateMermaidDiagrams.js",
    "jobParams" : {
      "confluenceUrl" : "https://dmtools.atlassian.net/wiki/spaces/AINA/pages/11665522/Templates",
      "storagePath" : "./mermaid-diagrams"
    }
  }
}
```

### Parameters

- **`confluenceUrl`** (optional): The Confluence page URL to process. Defaults to the Templates page.
  - Format: `https://dmtools.atlassian.net/wiki/spaces/{SPACE}/pages/{PAGE_ID}/{TITLE}`
  - Example: `https://dmtools.atlassian.net/wiki/spaces/AINA/pages/11665522/Templates`

- **`storagePath`** (optional): Directory path where diagrams will be stored. Defaults to `./mermaid-diagrams`.
  - Diagrams are stored in: `{storagePath}/confluence/{SPACE}/{PAGE_ID}/{PAGE_TITLE}.mmd`

## How It Works

1. **URL Parsing**: Extracts space key, page ID, and page name from the Confluence URL
2. **Pattern Building**: Creates an include pattern for recursive processing: `{SPACE}/pages/{PAGE_ID}/{PAGE_NAME}/**`
3. **Confluence Access**: Gets Confluence instance via `BasicConfluence.getInstance()`
4. **Diagram Generation**: Uses `MermaidIndex` to:
   - Process the page and all children recursively
   - Generate Mermaid diagrams using `MermaidDiagramGeneratorAgent`
   - Store diagrams in the file system
   - Skip pages that haven't been modified since last generation (caching)

## Usage

### Via Agent Configuration

1. Use the configuration file `agents/mermaid_diagrams_generator.json`
2. Customize `jobParams.confluenceUrl` and `jobParams.storagePath` as needed
3. Run the agent through the DMTools agent system

### Via Direct Script Execution

The script can also be used directly as a postJSAction in any agent configuration:

```json
{
  "postJSAction": "agents/js/generateMermaidDiagrams.js",
  "jobParams": {
    "confluenceUrl": "https://dmtools.atlassian.net/wiki/spaces/AINA/pages/11665522/Templates",
    "storagePath": "./mermaid-diagrams"
  }
}
```

## Output Structure

Diagrams are stored in a hierarchical structure:

```
{storagePath}/
└── confluence/
    └── {SPACE}/
        └── {PAGE_ID}/
            └── {PAGE_TITLE}.mmd
```

Example:
```
./mermaid-diagrams/
└── confluence/
    └── AINA/
        └── 11665522/
            └── Templates.mmd
        └── 11665523/
            └── Child Page 1.mmd
        └── 11665524/
            └── Child Page 2.mmd
```

## Features

- ✅ **Recursive Processing**: Automatically processes all child pages
- ✅ **Caching**: Skips pages that haven't been modified (based on last modified date)
- ✅ **Hierarchical Storage**: Maintains page hierarchy in file structure
- ✅ **Error Handling**: Comprehensive error handling and logging
- ✅ **Configurable**: Customizable URL and storage path

## Requirements

- Confluence must be configured in DMTools (credentials, base path, etc.)
- The specified Confluence page must be accessible
- Write permissions to the storage path directory

## Example

To generate diagrams for the Templates page and all its children:

```json
{
  "jobParams": {
    "confluenceUrl": "https://dmtools.atlassian.net/wiki/spaces/AINA/pages/11665522/Templates",
    "storagePath": "./mermaid-diagrams"
  }
}
```

This will:
1. Process the Templates page (ID: 11665522)
2. Recursively process all child pages
3. Generate Mermaid diagrams for each page
4. Store them in `./mermaid-diagrams/confluence/AINA/{PAGE_ID}/{PAGE_TITLE}.mmd`

## Troubleshooting

### Confluence Not Configured

If you see: `"Confluence is not configured. Please configure Confluence credentials."`

- Ensure Confluence credentials are set in DMTools configuration
- Check that `BasicConfluence.getInstance()` returns a valid instance

### Invalid URL Format

If you see: `"Invalid Confluence URL format"`

- Ensure the URL follows the format: `https://.../wiki/spaces/{SPACE}/pages/{PAGE_ID}/{TITLE}`
- The URL must include the space key and page ID

### Java Bridge Errors

If you see: `"Failed to access Java classes"`

- Ensure the JavaScript bridge has access to Java classes
- Check that `MermaidIndex` and `MermaidDiagramGeneratorAgent` are available
