# UriToObjectFactory Dependency Injection Improvements

## Overview

The `UriToObjectFactory` has been enhanced to support proper dependency injection for both standalone and server-managed execution modes. This improvement ensures that jobs running in server-managed mode use the properly resolved integrations instead of falling back to Basic* instances.

## Problem Solved

**Before**: The `UriToObjectFactory` and `Expert` class manually created instances using `BasicJiraClient.getInstance()`, `BasicConfluence.getInstance()`, etc., even in server-managed mode where proper integrations were already resolved and injected.

**After**: The `UriToObjectFactory` is now properly dependency-injected and uses the correct integration instances based on the execution mode.

## Architecture Changes

### 1. Enhanced UriToObjectFactory

```java
public class UriToObjectFactory {
    // Injected instances for server-managed mode
    private final TrackerClient<? extends ITicket> trackerClient;
    private final Confluence confluence;
    private final SourceCodeFactory sourceCodeFactory;

    @Inject
    public UriToObjectFactory(TrackerClient<? extends ITicket> trackerClient, 
                             Confluence confluence, 
                             SourceCodeFactory sourceCodeFactory) {
        // Server-managed mode constructor
    }

    public UriToObjectFactory() {
        // Standalone mode constructor (backward compatibility)
    }
}
```

### 2. Dagger Module Configuration

#### ServerManagedIntegrationsModule
- Provides `UriToObjectFactory` with server-managed integrations
- Uses integrations resolved from job configuration credentials
- Logs: `🔧 [ServerManagedIntegrationsModule] Creating UriToObjectFactory with server-managed integrations`

#### ConfluenceModule (Standalone)
- Provides `UriToObjectFactory` for standalone mode
- Uses Basic* instances for backward compatibility
- Logs: `🔧 [ConfluenceModule] Creating UriToObjectFactory for standalone mode`

### 3. Expert Class Integration

**Before:**
```java
// Manual creation in Expert.runJobImpl()
List<UriToObject> uriProcessingSources = new ArrayList<>();
if (trackerClient instanceof UriToObject) {
    uriProcessingSources.add((UriToObject) trackerClient);
}
// ... manual logic for each integration type
```

**After:**
```java
@Inject
UriToObjectFactory uriToObjectFactory;

// In runJobImpl()
List<? extends UriToObject> uriProcessingSources;
try {
    uriProcessingSources = uriToObjectFactory.createUriProcessingSources(expertParams.getSourceCodeConfig());
} catch (IOException e) {
    throw new RuntimeException("Failed to create URI processing sources", e);
}
```

## Execution Mode Behavior

### Standalone Mode
- Uses `ConfluenceModule` provider
- Falls back to `BasicJiraClient.getInstance()`, `BasicConfluence.getInstance()`, etc.
- Maintains full backward compatibility

### Server-Managed Mode
- Uses `ServerManagedIntegrationsModule` provider
- Utilizes properly resolved integrations from job configuration
- Ensures consistency with other injected dependencies

## Benefits

1. **Consistency**: Both execution modes use the same factory pattern
2. **Proper Dependency Injection**: Server-managed jobs use resolved integrations
3. **Maintainability**: Centralized URI processing source creation
4. **Extensibility**: Easy to add new URI processors through DI
5. **Debugging**: Clear log messages show which mode is being used

## Testing

The improvements can be tested by:

1. **Server-Managed Mode**: Execute saved job configurations through the UI or API
2. **Standalone Mode**: Run jobs directly using the existing standalone execution
3. **Log Verification**: Check server logs for factory creation messages

### Expected Log Messages

**Server-Managed Mode:**
```
🔧 [ServerManagedIntegrationsModule] Creating UriToObjectFactory with server-managed integrations
```

**Standalone Mode:**
```
🔧 [ConfluenceModule] Creating UriToObjectFactory for standalone mode
```

## Implementation Details

### Files Modified

1. `dmtools-core/src/main/java/com/github/istin/dmtools/context/UriToObjectFactory.java`
   - Added dependency injection support
   - Maintained backward compatibility

2. `dmtools-core/src/main/java/com/github/istin/dmtools/di/ServerManagedIntegrationsModule.java`
   - Added UriToObjectFactory provider for server-managed mode

3. `dmtools-core/src/main/java/com/github/istin/dmtools/di/ConfluenceModule.java`
   - Added UriToObjectFactory provider for standalone mode

4. `dmtools-core/src/main/java/com/github/istin/dmtools/expert/Expert.java`
   - Injected UriToObjectFactory
   - Replaced manual URI processing source creation

### Dagger Component Configuration

- **ExpertComponent**: Uses `ConfluenceModule` for standalone mode
- **ServerManagedExpertComponent**: Uses `ServerManagedIntegrationsModule` for server-managed mode

## Backward Compatibility

The improvements are fully backward compatible:
- Existing standalone code continues to work unchanged
- Default constructor still available for manual instantiation
- No breaking changes to public APIs

## Future Enhancements

1. Add support for GitHub and Figma integrations in server-managed mode
2. Implement caching for URI processing sources
3. Add configuration options for URI processing behavior
4. Extend logging and monitoring capabilities 