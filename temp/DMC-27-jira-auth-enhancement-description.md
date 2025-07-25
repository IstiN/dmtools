# Enhanced Jira Authentication Configuration

**What it does**: Simplifies Jira integration configuration by allowing users to provide email and API token separately instead of requiring manual base64 encoding
**Target Users**: Developers, project managers, and system administrators setting up Jira integrations
**Business Value**: Reduces configuration complexity, minimizes setup errors, and improves user experience while maintaining backward compatibility

## 🏆 Current Status
* ✅ **Solution Designed**: Requirements analyzed and implementation approach defined
* 🔄 **In Development**: Implementation pending based on DMC-27 specifications
* 📋 **Backlog Status**: Currently in backlog awaiting development

## 🎪 How It Works

### User Journey
1. **Discovery**: Users access Jira integration configuration through the DMTools server web interface
2. **Configuration**: Users can now choose between two authentication methods:
   - **New Method**: Provide email and API token separately (recommended)
   - **Legacy Method**: Continue using base64-encoded email:token combination
3. **Usage**: Application automatically handles authentication encoding internally
4. **Management**: Configuration can be updated through the web interface or environment variables

### Key Capabilities
* **Separate Email/Token Input**: Users can provide email and API token in separate fields
* **Automatic Base64 Encoding**: System internally handles the `base64(email:token)` encoding
* **Backward Compatibility**: Existing `JIRA_LOGIN_PASS_TOKEN` configurations continue to work
* **Configuration Flexibility**: Support for both web UI and environment variable configuration

## ✅ Acceptance Criteria (To Be Implemented)

### Core Functionality
* ✅ **Separate Input Fields**: Add new configuration parameters `JIRA_EMAIL` and `JIRA_API_TOKEN`
* ✅ **Automatic Encoding**: System automatically combines email and token with base64 encoding
* ✅ **Priority Logic**: Use separate email/token if provided, otherwise fall back to existing token property
* ✅ **Backward Compatibility**: Existing `JIRA_LOGIN_PASS_TOKEN` configurations remain functional

### User Experience
* ✅ **Intuitive Configuration**: Users no longer need to understand base64 encoding
* ✅ **Clear Documentation**: Updated setup guides explain both authentication methods
* ✅ **Validation**: Proper validation of email format and token requirements
* ✅ **Error Handling**: Clear error messages for authentication failures

### Technical Requirements
* ✅ **Property Resolution**: Implement priority-based property resolution logic
* ✅ **Security**: Ensure sensitive data handling for both methods
* ✅ **Integration Testing**: Verify compatibility with existing Jira configurations
* ✅ **Documentation Updates**: Update all relevant configuration documentation

## 🏗️ Technical Architecture

### System Components
* **Backend**: Enhanced property resolution in `PropertyReader` and configuration classes
* **Server**: Updated integration configuration endpoints and validation
* **Configuration**: New properties added to Jira integration configuration schema
* **State Management**: Maintained through existing configuration management system

### Key Files and Locations
* **Main Implementation**: 
  - `dmtools-core/src/main/java/com/github/istin/dmtools/common/utils/PropertyReader.java`
  - `dmtools-core/src/main/java/com/github/istin/dmtools/common/config/JiraConfiguration.java`
* **Server Integration**: 
  - `dmtools-server/src/main/java/com/github/istin/dmtools/auth/service/IntegrationService.java`
  - `dmtools-server/src/main/resources/integrations/jira.json`
* **Configuration Schema**: 
  - `dmtools-server/src/main/resources/integrations/jira.json`
* **Documentation**: 
  - `dmtools-server/src/main/resources/docs/integrations/jira_setup_en.md`

### Current Authentication Flow
```
User Input (Current):
┌─────────────────────────────────┐
│ JIRA_LOGIN_PASS_TOKEN           │
│ base64(email:token)             │
└─────────────────────────────────┘
           │
           ▼
    ┌─────────────┐
    │ JiraClient  │
    │ Constructor │
    └─────────────┘
           │
           ▼
    ┌─────────────┐
    │ HTTP Auth   │
    │ Header      │
    └─────────────┘
```

### Enhanced Authentication Flow (Proposed)
```
User Input Options:
┌─────────────────┐  OR  ┌─────────────────────────────────┐
│ JIRA_EMAIL      │      │ JIRA_LOGIN_PASS_TOKEN           │
│ JIRA_API_TOKEN  │      │ base64(email:token) [Legacy]    │
└─────────────────┘      └─────────────────────────────────┘
         │                           │
         ▼                           │
┌─────────────────┐                  │
│ Auto Base64     │                  │
│ Encoding        │                  │
│ email:token     │                  │
└─────────────────┘                  │
         │                           │
         └─────────────┬─────────────┘
                       ▼
                ┌─────────────┐
                │ JiraClient  │
                │ Constructor │
                └─────────────┘
                       │
                       ▼
                ┌─────────────┐
                │ HTTP Auth   │
                │ Header      │
                └─────────────┘
```

## 📊 Architecture Diagram

```mermaid
graph TD
    A[User Configuration] --> B{Configuration Method}
    B -->|New Method| C[JIRA_EMAIL + JIRA_API_TOKEN]
    B -->|Legacy Method| D[JIRA_LOGIN_PASS_TOKEN]
    
    C --> E[Property Reader]
    D --> E
    
    E --> F{Priority Resolution}
    F -->|Email & Token Available| G[Auto Base64 Encoding]
    F -->|Only Token Available| H[Use Existing Token]
    
    G --> I[base64(email:token)]
    H --> I
    
    I --> J[JiraClient Constructor]
    J --> K[HTTP Authentication]
    K --> L[Jira API]
    
    style C fill:#e1f5fe
    style G fill:#e8f5e8
    style I fill:#fff3e0
```

**Diagram Explanation**:
- **Configuration Method**: Users can choose between new separated inputs or legacy combined token
- **Property Reader**: Enhanced to handle both configuration methods with priority logic
- **Auto Base64 Encoding**: Internal handling eliminates user complexity
- **Priority Resolution**: Smart fallback ensures backward compatibility

## 🔧 Configuration & Setup

### Environment Requirements
* **Development**: Existing DMTools development environment
* **Production**: No additional dependencies required
* **Dependencies**: Uses existing base64 encoding utilities and HTTP authentication framework

### New Configuration Properties
* **Property Name**: `JIRA_EMAIL` - User's email address for Jira authentication
* **Property Name**: `JIRA_API_TOKEN` - Jira API token (separate from email)
* **Backward Compatibility**: `JIRA_LOGIN_PASS_TOKEN` - Existing base64-encoded email:token

### Priority Resolution Logic
1. **Primary**: If both `JIRA_EMAIL` and `JIRA_API_TOKEN` are provided, use them
2. **Fallback**: If `JIRA_LOGIN_PASS_TOKEN` is provided, use existing logic
3. **Validation**: Ensure at least one valid authentication method is configured

## 📝 User Documentation

### Getting Started (New Method)
1. **Access**: Navigate to Jira integration configuration in DMTools
2. **Email Setup**: Enter your Jira account email address
3. **Token Setup**: Enter your Jira API token (no encoding required)
4. **Test Connection**: Verify authentication works correctly

### Getting Started (Legacy Method)
1. **Access**: Navigate to Jira integration configuration in DMTools
2. **Token Setup**: Provide pre-encoded `base64(email:token)` value
3. **Test Connection**: Verify authentication works correctly

### Migration Guide
* **Existing Users**: No action required - current configurations will continue working
* **New Users**: Recommended to use the new separated email/token method
* **Migration Path**: Users can optionally migrate to the new method for easier management

### Troubleshooting
* **Authentication Failures**: Verify email format and token validity
* **Configuration Issues**: Check that either new method or legacy method is properly configured
* **Priority Conflicts**: Ensure only one authentication method is active

## 🔐 Security & Compliance

### Data Handling
* **Email Storage**: Email addresses handled as non-sensitive configuration data
* **Token Security**: API tokens treated as sensitive data with appropriate protection
* **Encoding Security**: Base64 encoding performed securely in memory
* **Backward Compatibility**: No changes to existing security model

### Access Control
* **Configuration Access**: Restricted to users with integration management permissions
* **Token Visibility**: Sensitive tokens masked in UI displays
* **Audit Trail**: Configuration changes logged for security tracking

## 🚀 Future Enhancements

### Planned Improvements
* **OAuth2 Support**: Consider adding OAuth2 authentication as an alternative
* **Token Rotation**: Automated token rotation and expiration handling
* **Multi-Instance Support**: Enhanced support for multiple Jira instances
* **Configuration Templates**: Pre-configured templates for common setups

### Known Limitations
* **Token Management**: Users still responsible for token lifecycle management
* **Email Validation**: Basic email format validation (not full verification)
* **Legacy Dependency**: Maintains dependency on base64 encoding for Jira API compatibility

## 🔗 Related Documentation

### Technical References
* **Current Implementation**: `BasicJiraClient.java` and `PropertyReader.java`
* **Configuration Schema**: `jira.json` integration definition
* **Setup Documentation**: `jira_setup_en.md` user guide

### Related Features
* **Integration Management System**: DMC-12 (Parent Epic)
* **Server Authentication**: OAuth2 and JWT authentication systems
* **Configuration Management**: Environment-based configuration system

### Implementation Files
* **Core Configuration**: 
  - `dmtools-core/src/main/java/com/github/istin/dmtools/common/utils/PropertyReader.java`
  - `dmtools-core/src/main/java/com/github/istin/dmtools/common/config/JiraConfiguration.java`
* **Server Integration**: 
  - `dmtools-server/src/main/java/com/github/istin/dmtools/auth/service/IntegrationService.java`
* **Configuration Schema**: 
  - `dmtools-server/src/main/resources/integrations/jira.json` 