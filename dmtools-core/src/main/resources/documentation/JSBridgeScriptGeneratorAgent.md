# JSBridgeScriptGeneratorAgent Documentation

## Overview

The `JSBridgeScriptGeneratorAgent` is an AI-powered agent that generates executable JavaScript scripts optimized for **GraalJS execution** within the DMTools ecosystem. This agent leverages the DMToolsBridge API to create automation scripts, presentation generators, reporting tools, and more that run efficiently in the GraalJS polyglot environment.

## Key Features

- **GraalJS Optimized**: All generated scripts are optimized for GraalJS execution environment
- **Dynamic API Description**: Automatically generates API documentation from the DMToolsBridge class using reflection
- **Permission-Based Filtering**: Only includes API methods that match the specified permissions
- **Multiple Output Formats**: Supports function, module, and complete script formats
- **Polyglot Environment**: Works seamlessly with Java-JavaScript interop via GraalJS
- **Error Handling**: Generated scripts include comprehensive error handling and logging
- **Type Safety**: Validates that generated scripts only use available API methods

## GraalJS Environment

Since GraalJS is always available in the DMTools environment, the generated scripts can take advantage of:

- **Java-JavaScript Interop**: Direct access to Java objects and methods through the bridge
- **Polyglot Context**: Seamless integration with Java services and APIs
- **Performance**: Optimized execution with JIT compilation when available
- **Host Access**: Controlled access to Java classes and methods via `@HostAccess.Export`

### GraalJS Performance Optimization

To optimize GraalJS performance in your environment, consider:

```bash
# Enable JVMCI for better performance
-XX:+EnableJVMCI

# Disable interpreter-only warnings if performance is acceptable
-Dpolyglot.engine.WarnInterpreterOnly=false

# Redirect GraalJS logs to file
-Dpolyglot.log.file=/path/to/graal.log
```

## Agent Parameters

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| `task` | String | Yes | Description of what the JavaScript should accomplish | "Create a presentation about quarterly results" |
| `apiDescription` | String | Yes | Dynamic description of available DMToolsBridge API methods | Auto-generated from reflection |
| `jsFramework` | String | No | Target JavaScript framework ("vanilla", "node", "graaljs") | "graaljs" |
| `outputFormat` | String | No | Expected output format ("function", "module", "complete script") | "function" |
| `additionalRequirements` | String | No | Special requirements or constraints | "Include error handling and logging" |
| `files` | List<File> | No | Related files for context | null |

## Helper Methods

The `JSBridgeScriptGeneratorAgentHelper` class provides convenient factory methods:

### createParamsWithFullAPI()
Creates parameters with access to all DMToolsBridge API methods, optimized for GraalJS.

```java
JSBridgeScriptGeneratorAgent.Params params = JSBridgeScriptGeneratorAgentHelper.createParamsWithFullAPI(
    "Create a comprehensive utility script for GraalJS execution",
    "graaljs",
    "complete script", 
    "Optimize for GraalJS polyglot environment",
    Collections.emptyList()
);
```

### createPresentationParams()
Creates parameters optimized for presentation-related JavaScript generation in GraalJS.

```java
JSBridgeScriptGeneratorAgent.Params params = JSBridgeScriptGeneratorAgentHelper.createPresentationParams(
    "Generate a presentation about project status using GraalJS bridge",
    "graaljs",
    "function",
    "Leverage Java-JavaScript interop for optimal performance",
    Collections.emptyList()
);
```

### createReportingParams()
Creates parameters optimized for reporting and analytics scripts in GraalJS.

```java
JSBridgeScriptGeneratorAgent.Params params = JSBridgeScriptGeneratorAgentHelper.createReportingParams(
    "Create a project status report using GraalJS polyglot capabilities",
    "graaljs", 
    "module",
    "Use GraalJS async capabilities and Java interop",
    Collections.emptyList()
);
```

### createHttpClientParams()
Creates parameters for HTTP client operations optimized for GraalJS.

```java
JSBridgeScriptGeneratorAgent.Params params = JSBridgeScriptGeneratorAgentHelper.createHttpClientParams(
    "Make API requests using GraalJS HTTP bridge capabilities",
    "graaljs",
    "complete script",
    "Leverage polyglot environment for optimal HTTP handling",
    Collections.emptyList()
);
```

## Usage Examples

### Basic Usage with GraalJS Optimization

```java
// Create the agent
JSBridgeScriptGeneratorAgent agent = new JSBridgeScriptGeneratorAgent();

// Create parameters for GraalJS-optimized presentation generation
JSBridgeScriptGeneratorAgent.Params params = JSBridgeScriptGeneratorAgentHelper.createPresentationParams(
    "Create a JavaScript function that generates a quarterly results presentation optimized for GraalJS",
    "graaljs",
    "function", 
    "Include comprehensive error handling and leverage Java-JavaScript interop",
    Collections.emptyList()
);

// Generate the script
String generatedScript = agent.run(params);
System.out.println(generatedScript);
```

### Integration with JSPresentationMakerBridge (GraalJS)

```java
// Generate a script specifically for JSPresentationMakerBridge running in GraalJS
JSBridgeScriptGeneratorAgent.Params params = JSBridgeScriptGeneratorAgentHelper.createPresentationParams(
    "Create a JavaScript function named 'generatePresentationJs' optimized for GraalJS execution",
    "graaljs",
    "function",
    "Must work with JSPresentationMakerBridge in GraalJS polyglot context",
    Collections.emptyList()
);

String generatedScript = agent.run(params);

// Save the script to a file and use with JSPresentationMakerBridge
File jsFile = new File("generated_presentation_graaljs.js");
Files.write(jsFile.toPath(), generatedScript.getBytes());

JSONObject config = new JSONObject();
config.put("jsScriptPath", jsFile.getAbsolutePath());
config.put("clientName", "GraalJSGeneratedScript");

JSPresentationMakerBridge bridge = new JSPresentationMakerBridge(config);
JSONObject result = bridge.createPresentation("{\"topic\":\"Q4 Results\"}");
```

## Generated Script Examples

### GraalJS-Optimized Presentation Function

```javascript
function createOptimizedPresentation(bridge, topic, content) {
    try {
        // Leverage GraalJS polyglot capabilities
        bridge.jsLogInfo('Starting GraalJS-optimized presentation creation for topic: ' + topic);
        
        // Direct Java object interaction via GraalJS
        const requestData = bridge.createRequestDataJson(
            'Generate presentation slides for: ' + topic,
            content || 'Default presentation content'
        );
        
        // Use GraalJS efficient object handling
        const orchestratorParams = {
            topic: topic,
            audience: 'General audience',
            requestDataList: [JSON.parse(requestData)],
            assistantName: 'GraalJS AI Assistant',
            presenterName: 'Script Generator',
            summarySlideRequest: 'Create a summary slide with key points'
        };
        
        // Polyglot method invocation
        const presentationJson = bridge.invokePresentationOrchestrator(
            JSON.stringify(orchestratorParams)
        );
        
        // GraalJS-optimized presentation rendering
        bridge.drawHtmlPresentation(topic, presentationJson);
        bridge.jsLogInfo('GraalJS presentation created successfully');
        
        return JSON.parse(presentationJson);
        
    } catch (error) {
        // GraalJS error handling with full stack trace
        bridge.jsLogErrorWithException('GraalJS presentation error: ' + error.message, error.stack);
        throw error;
    }
}
```

### GraalJS Reporting Module with Java Interop

```javascript
// GraalJS module optimized for polyglot environment
function generateAdvancedProjectReport(bridge, projectKey, startDate) {
    try {
        bridge.jsLogInfo('Starting GraalJS project report generation for: ' + projectKey);
        
        // Direct Java object access via GraalJS
        const trackerClient = bridge.getTrackerClientInstance();
        
        // GraalJS-optimized configuration object
        const reportConfig = {
            reportName: `GraalJS Project Report - ${projectKey}`,
            reportDescription: `Generated via GraalJS polyglot environment`,
            includeBugs: true,
            includeStories: true,
            includeEpics: true,
            optimizedForGraalJS: true
        };
        
        // Efficient string interpolation in GraalJS
        const jql = `project = ${projectKey} AND created >= ${startDate}`;
        const tableTypes = JSON.stringify(['SUMMARY', 'DETAILED', 'TIMELINE', 'GRAALJS_OPTIMIZED']);
        
        // Polyglot method call with Java object parameters
        const reportResult = bridge.generateCustomProjectReport(
            trackerClient,
            reportConfig,
            jql,
            startDate,
            tableTypes
        );
        
        bridge.jsLogInfo('GraalJS project report generated successfully');
        return reportResult;
        
    } catch (error) {
        // Enhanced error reporting for GraalJS environment
        const errorDetails = {
            message: error.message,
            stack: error.stack,
            graalJSContext: 'project-reporting',
            timestamp: new Date().toISOString()
        };
        
        bridge.jsLogErrorWithException(
            'GraalJS reporting error: ' + JSON.stringify(errorDetails), 
            error.stack
        );
        throw error;
    }
}

// GraalJS module export
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { generateAdvancedProjectReport };
}
```

## GraalJS-Specific Features

### Java-JavaScript Interop Optimization

Generated scripts can leverage:
- Direct access to Java objects and methods through the bridge
- Efficient type conversion between Java and JavaScript
- Polyglot value sharing and method invocation
- Host access annotations for secure Java integration

### Performance Considerations

1. **Object Reuse**: Generated scripts reuse bridge objects efficiently
2. **Memory Management**: Proper cleanup of polyglot values
3. **Method Caching**: Cache frequently used bridge methods
4. **Batch Operations**: Group bridge calls when possible

### Error Handling in GraalJS

```javascript
// Enhanced GraalJS error handling pattern
function graalJSErrorHandler(bridge, operation, error) {
    const graalJSError = {
        operation: operation,
        message: error.message,
        stack: error.stack,
        polyglotContext: true,
        timestamp: new Date().toISOString()
    };
    
    bridge.jsLogErrorWithException(
        `GraalJS Error in ${operation}: ${JSON.stringify(graalJSError)}`,
        error.stack
    );
}
```

## Integration Testing with GraalJS

The agent includes GraalJS-specific integration tests:

- **testGraalJSPresentationScript()**: Tests GraalJS-optimized presentation generation
- **testGraalJSReportingScript()**: Tests polyglot reporting capabilities
- **testJavaInteropValidation()**: Validates Java-JavaScript interop functionality
- **testGraalJSPerformance()**: Performance testing in GraalJS environment

## Best Practices for GraalJS

1. **Use Polyglot Features**: Leverage direct Java object access when possible
2. **Optimize Bridge Calls**: Minimize bridge method invocations for better performance
3. **Handle Type Conversion**: Be aware of Java-JavaScript type conversions
4. **Error Context**: Include GraalJS context information in error messages
5. **Memory Efficiency**: Properly manage polyglot value lifecycle

## Security Considerations in GraalJS

- Scripts run in controlled GraalJS polyglot context
- Host access is restricted through `@HostAccess.Export` annotations
- Bridge permissions control Java method access
- No direct file system or network access without explicit permissions
- Polyglot sandbox provides additional security layer

## Future Enhancements

- **TypeScript Generation**: Support for TypeScript with GraalJS type definitions
- **GraalJS Profiling**: Integration with GraalJS profiling tools
- **Advanced Polyglot Features**: Leverage more GraalJS polyglot capabilities
- **Performance Metrics**: Built-in performance monitoring for generated scripts
- **Hot Reload**: Dynamic script reloading in GraalJS environment
- **Native Image Support**: Optimization for GraalVM native image compilation 