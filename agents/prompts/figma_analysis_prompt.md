Your task is to analyse a Figma design file and produce a structured design analysis. Read all files in the `input` folder:

- `request.md` — ticket details describing what needs to be built or analysed
- `comments.md` *(if present)* — prior decisions and context
- `figma_snapshot.json` — full Figma file structure + render URLs for every top-level frame

**Step 1 — Visualise the screens**

For each top-level frame in `figma_snapshot.json`, use its `renderUrl` to view the rendered image:
```
dmtools gemini_ai_chat_with_files --data '{"message": "Describe what you see in this screen", "filePaths": ["<renderUrl>"]}'
```
If a render URL is an https link (not a local path), download it first:
```
dmtools figma_download_image_of_file --data '{"href": "<renderUrl>"}'
```

**Step 2 — Analyse structure**

From `figma_snapshot.json → fileStructure`, extract:
- Screen/frame names and their hierarchy
- Component types present (buttons, forms, navigation, cards, modals, etc.)
- Text content and labels
- Colour and style tokens if present

**Step 3 — Write findings**

Write `outputs/design_analysis.md` covering:
1. **Screens overview** — list of all screens/frames with purpose
2. **UI components inventory** — reusable components identified
3. **User flows** — how screens connect
4. **Design tokens** — colours, typography, spacing patterns observed
5. **Implementation notes** — anything a developer needs to know (edge cases, responsive behaviour, states)
6. **Open questions** — anything ambiguous or missing in the design

Keep it concise — no filler, no restating the obvious. Every point must be actionable.
