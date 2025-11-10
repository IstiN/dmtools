# Ollama Integration Setup Guide

## Overview
This guide will help you set up the Ollama integration for local AI capabilities. Ollama allows you to run large language models locally on your machine without requiring cloud API keys.

## Prerequisites
- Ollama installed on your local machine or server
- At least one Ollama model downloaded
- Sufficient system resources (RAM and CPU/GPU) for running the model

## Step 1: Install Ollama

### For macOS and Linux

1. **Install Ollama**
   ```bash
   curl -fsSL https://ollama.com/install.sh | sh
   ```

2. **Verify Installation**
   ```bash
   ollama --version
   ```

### For Windows

1. **Download Ollama**
   - Visit [ollama.com](https://ollama.com/download)
   - Download the Windows installer
   - Run the installer and follow the prompts

2. **Verify Installation**
   - Open Command Prompt or PowerShell
   - Run: `ollama --version`

## Step 2: Download a Model

Before using Ollama, you need to download at least one model:

### Popular Models

1. **Llama 3 (Recommended)**
   ```bash
   ollama pull llama3
   ```
   - Good balance of performance and quality
   - Requires ~4GB RAM

2. **Mistral**
   ```bash
   ollama pull mistral
   ```
   - Fast and efficient
   - Requires ~4GB RAM

3. **CodeLlama (For Code Tasks)**
   ```bash
   ollama pull codellama
   ```
   - Optimized for code generation
   - Requires ~4GB RAM

4. **Llama 3.1 (Latest)**
   ```bash
   ollama pull llama3.1
   ```
   - Latest version with improvements
   - Requires ~4GB RAM

### View Available Models
```bash
ollama list
```

## Step 3: Start Ollama Service

Ollama runs as a background service:

### macOS and Linux
```bash
ollama serve
```

The service typically starts automatically after installation and runs on `http://localhost:11434`

### Windows
- Ollama service starts automatically after installation
- Check if it's running by opening `http://localhost:11434` in your browser

### Verify Service is Running
```bash
curl http://localhost:11434/api/tags
```

You should see a JSON response with available models.

## Step 4: Configure Integration in DMTools

When setting up the Ollama integration in DMTools, use the following values:

### Required Parameters

1. **Model** (Required)
   - The name of the Ollama model to use
   - Examples:
     - `llama3`
     - `mistral`
     - `codellama`
     - `llama3.1`
   - **Important**: Model must be pre-pulled using `ollama pull <model>`

### Optional Parameters

2. **Base Path** (Optional)
   - Default: `http://localhost:11434`
   - Change only if:
     - Ollama is running on a different port
     - Ollama is running on a remote server
   - Examples:
     - Local custom port: `http://localhost:8080`
     - Remote server: `http://192.168.1.100:11434`

3. **Context Window Size** (Optional)
   - Default: `16384` tokens
   - Controls how much context the model can remember
   - Higher values:
     - Allow more context in conversations
     - Require more memory
     - May slow down responses
   - Recommended values:
     - Small tasks: `4096`
     - Normal tasks: `16384`
     - Large documents: `32768`

4. **Max Prediction Tokens** (Optional)
   - Default: `-1` (unlimited)
   - Controls maximum length of AI responses
   - Set to a positive number to limit response length
   - Examples:
     - Short answers: `512`
     - Normal answers: `2048`
     - Long answers: `4096`
     - Unlimited: `-1`

## Step 5: Test Connection

After configuration:
1. Use the test connection feature in DMTools
2. Verify you can communicate with Ollama
3. Check that responses are being generated

## Configuration Examples

### Basic Configuration (Local)
```
Model: llama3
Base Path: http://localhost:11434
Context Window Size: 16384
Max Prediction Tokens: -1
```

### Remote Server Configuration
```
Model: mistral
Base Path: http://192.168.1.100:11434
Context Window Size: 8192
Max Prediction Tokens: 2048
```

### Code Generation Configuration
```
Model: codellama
Base Path: http://localhost:11434
Context Window Size: 32768
Max Prediction Tokens: 4096
```

## Performance Considerations

### System Requirements

**Minimum Requirements:**
- RAM: 8GB
- CPU: Modern multi-core processor
- Storage: 5GB for model files

**Recommended Requirements:**
- RAM: 16GB or more
- GPU: NVIDIA GPU with CUDA support (optional, but significantly faster)
- Storage: 10GB for multiple models

### Model Selection

- **Small models** (7B parameters): Faster, less accurate, lower memory usage
- **Medium models** (13B parameters): Balanced performance and quality
- **Large models** (70B+ parameters): Best quality, slower, high memory usage

### GPU Acceleration

Ollama automatically uses GPU if available:
- **NVIDIA GPUs**: Automatically detected and used
- **Apple Silicon (M1/M2/M3)**: Automatically uses Metal
- **AMD GPUs**: Limited support, check Ollama documentation

## Troubleshooting

### Common Issues

1. **Connection Failed**
   - Verify Ollama service is running: `curl http://localhost:11434/api/tags`
   - Check if the base path URL is correct
   - Ensure no firewall is blocking the connection
   - Try restarting Ollama: `ollama serve`

2. **Model Not Found**
   - Verify model is downloaded: `ollama list`
   - Pull the model if missing: `ollama pull <model-name>`
   - Check model name spelling (case-sensitive)

3. **Out of Memory**
   - Use a smaller model (e.g., `mistral` instead of `llama3.1:70b`)
   - Reduce context window size
   - Close other applications
   - Consider upgrading system RAM

4. **Slow Responses**
   - Use a smaller model
   - Reduce context window size
   - Enable GPU acceleration if available
   - Check system resource usage

5. **Service Not Starting**
   - Check if port 11434 is already in use
   - Review Ollama logs for errors
   - Try reinstalling Ollama
   - Check system requirements

### Getting Help

If you encounter issues:
1. Check Ollama documentation: [ollama.com/docs](https://ollama.com/docs)
2. Verify your model is compatible with your system
3. Check Ollama GitHub issues: [github.com/ollama/ollama](https://github.com/ollama/ollama)
4. Review system logs for error messages

## Security Considerations

- **Local Execution**: Ollama runs locally, no data is sent to external servers
- **Network Access**: By default, Ollama only accepts local connections
- **Remote Access**: If exposing Ollama remotely, use proper firewall rules and authentication
- **Model Trust**: Only download models from trusted sources

## Advanced Configuration

### Running Ollama on Custom Port
```bash
OLLAMA_HOST=0.0.0.0:8080 ollama serve
```

### Using GPU Acceleration
Ollama automatically detects and uses available GPUs. To verify:
```bash
ollama run llama3 "Hello" --verbose
```

### Multiple Models
You can have multiple models installed and switch between them:
```bash
ollama pull llama3
ollama pull mistral
ollama pull codellama
```

Then configure different DMTools integrations for each model.

## Additional Resources

- [Ollama Official Website](https://ollama.com)
- [Ollama Documentation](https://ollama.com/docs)
- [Ollama GitHub Repository](https://github.com/ollama/ollama)
- [Available Models Library](https://ollama.com/library)
- [Ollama API Documentation](https://github.com/ollama/ollama/blob/main/docs/api.md)

## Model Recommendations by Use Case

### General Purpose
- **llama3**: Best all-around model
- **mistral**: Fast and efficient alternative

### Code Generation
- **codellama**: Specialized for code tasks
- **deepseek-coder**: Alternative code model

### Long Context
- **llama3.1**: Supports larger context windows
- **mixtral**: Good for long documents

### Fast Responses
- **mistral**: Optimized for speed
- **phi**: Very small and fast
