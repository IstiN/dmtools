# KB Fixes - Testing Instructions

## Summary of Fixes

### 1. ✅ Areas preserve contributors and topics from previous sources
**Problem**: When processing `session_2`, the `ai` area was losing contributors and topics from `session_1`.
**Fix**: `buildAreaStructure` now reads existing area files and merges contributors/topics from all sources (similar to how `buildTopicFiles` works).
**File**: `KBStructureBuilder.java`

### 2. ✅ Fixed double quotes in timestamps
**Problem**: `created` timestamps were showing as `created: ""2025-10-24T15:41:32.083142Z""` (double quotes).
**Fix**: `extractFromFrontmatter` now removes surrounding quotes when extracting values.
**File**: `KBStructureBuilder.java`

### 3. ✅ People profiles now show Q/A/N links
**Problem**: Person profile files showed only summary numbers (e.g., "Questions asked: 1") but no links to actual questions/answers/notes.
**Fix**: `KBOrchestrator` now collects `PersonContributions` from `analysisResult` and passes them to `buildStructure`, which populates the "Questions Asked", "Answers Provided", and "Notes Contributed" sections with links.
**Files**: `KBOrchestrator.java`, `KBStructureManager.java`, `KBStructureBuilder.java`

### 4. ✅ Added parameter to skip AI description generation
**Feature**: You can now skip AI description generation by passing `generate_descriptions=false` to `kb_process_inbox`.
**File**: `KBTools.java`
**Usage**: See below.

## Testing Instructions

### Step 1: Backup and clean
```bash
cd /Users/Uladzimir_Klyshevich/git/ai.m

# Backup current state
git status
git stash  # If you have uncommitted changes

# Clean generated KB content (keep inbox/raw for re-processing)
rm -rf people/ topics/ areas/ answers/ questions/ notes/ stats/ INDEX.md
```

### Step 2: Copy session_1 only
```bash
# Ensure temp folder has session_1 and session_2
ls /Users/Uladzimir_Klyshevich/git/ai.m/temp/

# Copy session_1 to inbox/raw
mkdir -p inbox/raw
cp -r /Users/Uladzimir_Klyshevich/git/ai.m/temp/session_1 inbox/raw/

# Clean analyzed tracking (force reprocessing)
rm -rf inbox/analyzed
```

### Step 3: Process session_1 WITHOUT descriptions
```bash
# Run with descriptions DISABLED to speed up testing
~/git/dmtools/dmtools-core/dmtools.sh \
  --job-config teams-kb-processor.json \
  --generate-descriptions false

# Or via direct KB tool:
# dmtools kb_process_inbox output_path=/Users/Uladzimir_Klyshevich/git/ai.m generate_descriptions=false
```

**Expected results**:
- ✅ `areas/ai/ai.md` should exist with contributors from session_1
- ✅ `topics/*.md` files should exist
- ✅ `people/*/*.md` files should exist with Q/A/N links (NOT just numbers)
- ✅ `created` timestamps should have format: `created: "2025-10-24T...Z"` (NOT `created: ""2025-10-24T...""`)
- ✅ NO `-desc.md` files (descriptions disabled)

### Step 4: Verify people profiles have links
```bash
# Check a person profile
cat people/Maksim_Karaban/Maksim_Karaban.md

# Should see sections like:
# ## Questions Asked
# - [[../../questions/q_0001|q_0001]] - 2025-10-24
#
# NOT just:
# - Questions asked: 1
```

### Step 5: Copy session_2 on top
```bash
# Add session_2
cp -r /Users/Uladzimir_Klyshevich/git/ai.m/temp/session_2 inbox/raw/

# Process again WITHOUT descriptions
~/git/dmtools/dmtools-core/dmtools.sh \
  --job-config teams-kb-processor.json \
  --generate-descriptions false
```

**Expected results**:
- ✅ `areas/ai/ai.md` should have contributors from BOTH sessions:
  - `contributors: ["Maksim Karaban", "Tom Bradley", "Uladzimir Klyshevich", "Aliaksandr Raukuts", "Aliaksandr Tarasevich"]`
  - `sources: ["session_1", "session_2"]`
- ✅ Topics from both sessions should be merged
- ✅ `created` timestamps should NOT change (preserved from first run)
- ✅ NO double quotes in timestamps
- ✅ New people (`Aliaksandr_Tarasevich`) should have Q/A/N links

### Step 6: Check specific fixes

#### Fix 1: Areas preserve data
```bash
cat areas/ai/ai.md | grep -A2 "contributors:"
# Should show ALL contributors from both sessions

cat areas/ai/ai.md | grep -A10 "## Topics"
# Should show topics from BOTH sessions
```

#### Fix 2: No double quotes
```bash
grep "created:" topics/*.md | grep '""'
# Should return EMPTY (no double quotes)
```

#### Fix 3: People profiles have links
```bash
cat people/Aliaksandr_Tarasevich/Aliaksandr_Tarasevich.md
# Should see "## Questions Asked" with links like:
# - [[../../questions/q_0012|q_0012]] - 2025-10-24
```

### Step 7: Test WITH descriptions (optional)
```bash
# Clean up
rm -rf inbox/analyzed people/*/\*-desc.md topics/\*-desc.md areas/\*/\*-desc.md

# Run with descriptions ENABLED
~/git/dmtools/dmtools-core/dmtools.sh \
  --job-config teams-kb-processor.json \
  --generate-descriptions true

# Or with smart aggregation:
~/git/dmtools/dmtools-core/dmtools.sh \
  --job-config teams-kb-processor.json \
  --generate-descriptions true \
  --smart-aggregation true
```

**Expected**:
- ✅ All `-desc.md` files should be generated
- ✅ All other fixes should still apply

## Configuration Options

### In job config (teams-kb-processor.json)
```json
{
  "name": "jsrunner",
  "params": {
    "jsPath": "teams-kb-inbox-processor.js",
    "jobParams": {
      "chatName": "ai.m",
      "generateDescriptions": false,
      "smartAggregation": true
    }
  }
}
```

### In JavaScript (teams-kb-inbox-processor.js)
```javascript
const inboxResult = kb_process_inbox({
    output_path: kbPath,
    generate_descriptions: 'false',  // Skip descriptions
    smart_aggregation: 'true'         // Use smart regeneration
});
```

### Via CLI
```bash
dmtools kb_process_inbox \
  output_path=/path/to/kb \
  generate_descriptions=false \
  smart_aggregation=true
```

## Summary

All 4 issues are fixed:
1. ✅ Areas accumulate contributors/topics (not overwrite)
2. ✅ No double quotes in timestamps
3. ✅ People profiles show Q/A/N links
4. ✅ Can skip description generation with `generate_descriptions=false`

Unit tests: ✅ All passing
Build: ✅ Successful

Ready for real-world testing with session_1 and session_2!

