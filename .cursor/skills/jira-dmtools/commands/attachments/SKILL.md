---
name: jira-dmtools-attachments
description: Use when the user needs to attach a file to a JIRA ticket or download an attachment. Covers jira_attach_file_to_ticket, jira_download_attachment. Run dmtools in the terminal.
---

# JIRA attachments

## When to use this skill

Use when the user wants to:
- Attach a local file to a JIRA ticket (skipped if a file with the same name already exists)
- Download an attachment from JIRA by URL to a local file

## Commands

| Tool | Purpose | Parameters |
|------|---------|------------|
| `jira_attach_file_to_ticket` | Attach file from path | `name`, `ticketKey`, `filePath` (required), `contentType` (optional) |
| `jira_download_attachment` | Download by URL | `href` (required) |

## Steps

1. **Attach:** Have the ticket key, file path (absolute), and display name. Run `dmtools jira_attach_file_to_ticket --data '{"name":"doc.pdf","ticketKey":"PROJ-123","filePath":"/path/to/doc.pdf"}'`. Add `"contentType":"application/pdf"` if needed (default may be image/*).
2. **Download:** Use the attachment href from ticket or search response: `dmtools jira_download_attachment --data '{"href":"https://company.atlassian.net/..."}'`.
3. Parse stdout; check stderr on failure.

## Examples

```bash
# Attach PDF
dmtools jira_attach_file_to_ticket --data '{"name":"spec.pdf","ticketKey":"PROJ-123","filePath":"/tmp/spec.pdf","contentType":"application/pdf"}'

# Attach image (contentType optional)
dmtools jira_attach_file_to_ticket --data '{"name":"screenshot.png","ticketKey":"PROJ-123","filePath":"/tmp/screenshot.png"}'

# Download
dmtools jira_download_attachment --data '{"href":"https://company.atlassian.net/rest/api/3/attachment/12345/content"}'
```

## Notes

- Attach only adds if no attachment with the same name exists on the ticket. Use absolute paths for `filePath`.
- Download href is typically from the ticket's attachment list (Jira REST API or jira_get_ticket with attachments).

## Avoiding malformed API URLs

These commands use `--data` with JSON. Ensure the JSON is valid and passed as a **single argument** (correct shell quoting); bad quoting can cause mangled URLs and 404s.
