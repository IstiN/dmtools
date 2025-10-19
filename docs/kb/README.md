# Knowledge Base Documentation

Welcome to the DMTools Knowledge Base system documentation. This system transforms unstructured data (chat messages, documents, transcripts) into a structured, searchable knowledge repository using AI.

## Documentation Structure

### 📐 [Architecture](architecture.md)
High-level system design with visual diagrams:
- System overview and data flow
- Processing modes (FULL, PROCESS_ONLY, AGGREGATE_ONLY)
- Core components and their relationships
- Entity relationships and output structure
- Source cleanup architecture
- Performance considerations

**Start here** if you want to understand how the system works.

### 📚 [Usage Examples](usage-examples.md)
Practical examples and common workflows:
- Basic usage patterns
- Processing modes in action
- Source management strategies
- Incremental update workflows
- Advanced features and customization
- Troubleshooting guide
- Best practices

**Start here** if you want to use the system.

## Quick Start

### 1. Build a Knowledge Base

```bash
dmtools kb_build \
  --source_name "my_source" \
  --input_file "/path/to/data.json" \
  --date_time "2024-01-15T10:00:00Z" \
  --output_path "/path/to/kb"
```

### 2. View Results

```
kb/
├── questions/     # Extracted questions
├── answers/       # Extracted answers
├── notes/         # Extracted notes
├── topics/        # Organized by topic
├── people/        # Contributor profiles
├── areas/         # High-level categories
└── stats/         # Statistics and metrics
```

### 3. Query Your Knowledge Base

```bash
# Check source status
dmtools kb_get --source_name "my_source" --output_path "/path/to/kb"

# View statistics
cat /path/to/kb/stats/kb_statistics.json
```

## Key Features

### 🤖 AI-Powered Analysis
- Automatically extracts questions, answers, and notes
- Identifies topics and themes
- Recognizes people and their contributions
- Generates natural language descriptions

### 📊 Structured Organization
- Questions linked to answers
- Content organized by topics and areas
- People profiles with contribution history
- Comprehensive statistics and indexes

### 🔄 Incremental Updates
- Add new data without losing existing content
- Track multiple sources independently
- Merge related information automatically
- Maintain historical context

### 🗑️ Source Management
- Clean and refresh specific sources
- Preserve other sources during cleanup
- Automatic statistics regeneration
- Safe multi-source environments

### ⚡ Performance Optimized
- Parallel processing where possible
- Efficient file scanning and parsing
- Chunking for large inputs
- Batch aggregation

## Use Cases

### Team Knowledge Management
Build a searchable knowledge base from:
- Slack/Teams chat messages
- Email threads
- Meeting transcripts
- Video call recordings

### Documentation
Transform unstructured docs into structured KB:
- Confluence pages
- Wiki articles
- Technical documentation
- Process documents

### Customer Support
Extract knowledge from:
- Support tickets
- Customer conversations
- FAQ responses
- Resolution patterns

### Research & Analysis
Organize information from:
- Interview transcripts
- Research notes
- Literature reviews
- Survey responses

## Architecture at a Glance

```
Input → AI Analysis → Structure Building → AI Aggregation → Statistics
  ↓         ↓              ↓                    ↓              ↓
JSON    Questions      Files Created      Descriptions    Indexes
Text    Answers        Topics Built       Generated       Created
        Notes          People Profiles
```

## Processing Modes

| Mode | AI Analysis | Structure | AI Descriptions | Use Case |
|------|-------------|-----------|-----------------|----------|
| **FULL** | ✅ | ✅ | ✅ | Complete processing (default) |
| **PROCESS_ONLY** | ✅ | ✅ | ❌ | Fast bulk import |
| **AGGREGATE_ONLY** | ❌ | ❌ | ✅ | Generate descriptions only |

## System Requirements

- **Java**: 17 or higher
- **Memory**: 2GB minimum, 4GB recommended
- **Storage**: Depends on data volume (typically 10-50MB per 1000 messages)
- **AI Service**: Dial (Claude) or Gemini API access

## Configuration

Set up environment variables in `dmtools.env`:

```bash
# AI Configuration
DEFAULT_LLM=dial                    # or gemini
DIAL_API_KEY=your_dial_key
GEMINI_API_KEY=your_gemini_key

# KB Configuration
DMTOOLS_KB_OUTPUT_PATH=/path/to/kb  # Default output path
```

## Getting Help

### Documentation
- [Architecture](architecture.md) - System design and diagrams
- [Usage Examples](usage-examples.md) - Practical examples and workflows

### Common Issues

**AI not extracting information?**
- Check AI service configuration
- Verify API keys are set
- Try a different AI model

**Statistics not updating?**
- System auto-regenerates after cleanup
- Use `regenerateStructureFromExistingFiles()` if needed

**Source cleanup deleted wrong files?**
- Always verify source name with `kb_get` first
- Use `clean_source` parameter carefully

## Contributing

The KB system is designed to be extensible:

1. **Custom AI Agents**: Implement new analysis logic
2. **Additional Entity Types**: Extend beyond Q/A/N
3. **Custom Statistics**: Add new metrics
4. **Integration Hooks**: Connect to external systems

See the [Architecture](architecture.md) document for extension points.

## Version History

### v1.7.58 (Current)
- ✅ Source-specific cleanup functionality
- ✅ Automatic statistics regeneration
- ✅ Multi-source safety guarantees
- ✅ Comprehensive test coverage (22 tests)
- ✅ Extra instructions for AI agents
- ✅ Incremental update improvements

### Previous Versions
- v1.7.x: Refactored architecture with utility classes
- v1.6.x: Initial KB implementation
- v1.5.x: AI agent framework

## License

Part of DMTools - see main project LICENSE file.

## Support

For issues, questions, or contributions:
- Create an issue in the DMTools repository
- Contact the development team
- Check the troubleshooting guide in [Usage Examples](usage-examples.md)

---

**Ready to get started?** Check out the [Usage Examples](usage-examples.md) for practical examples!

**Want to understand the system?** Read the [Architecture](architecture.md) document!

