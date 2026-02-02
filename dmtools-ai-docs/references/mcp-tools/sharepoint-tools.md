# SHAREPOINT MCP Tools

**Total Tools**: 2

## Quick Reference

```bash
# List all sharepoint tools
dmtools list | jq '.tools[] | select(.name | startswith("sharepoint_"))'

# Example usage
dmtools sharepoint_get_drive_item [arguments]
```

## Usage in JavaScript Agents

```javascript
// Direct function calls for sharepoint tools
const result = sharepoint_get_drive_item(...);
const result = sharepoint_download_file(...);
```

## Available Tools

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `sharepoint_download_file` | Download a file from a SharePoint sharing URL to a local file path. Works with OneDrive and SharePoint sharing links. | `sharingUrl` (string, **required**)<br>`outputPath` (string, **required**) |
| `sharepoint_get_drive_item` | Get DriveItem information from a SharePoint sharing URL. Returns metadata about the shared file including download URLs. | `sharingUrl` (string, **required**) |

## Detailed Parameter Information

### `sharepoint_download_file`

Download a file from a SharePoint sharing URL to a local file path. Works with OneDrive and SharePoint sharing links.

**Parameters:**

- **`sharingUrl`** (string) 🔴 Required
  - SharePoint sharing URL
  - Example: `https://company-my.sharepoint.com/:v:/p/user/EabcdefGHIJ...`

- **`outputPath`** (string) 🔴 Required
  - Local file path to save to
  - Example: `/tmp/recording.mp4`

**Example:**
```bash
dmtools sharepoint_download_file "value" "value"
```

```javascript
// In JavaScript agent
const result = sharepoint_download_file("sharingUrl", "outputPath");
```

---

### `sharepoint_get_drive_item`

Get DriveItem information from a SharePoint sharing URL. Returns metadata about the shared file including download URLs.

**Parameters:**

- **`sharingUrl`** (string) 🔴 Required
  - SharePoint sharing URL
  - Example: `https://company-my.sharepoint.com/:v:/p/user/EabcdefGHIJ...`

**Example:**
```bash
dmtools sharepoint_get_drive_item "value"
```

```javascript
// In JavaScript agent
const result = sharepoint_get_drive_item("sharingUrl");
```

---

