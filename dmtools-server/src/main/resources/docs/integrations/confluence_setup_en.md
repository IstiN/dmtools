# Confluence Integration Setup Guide

## Overview
This guide will help you set up the Confluence integration for accessing pages, spaces, and other Confluence features.

## Prerequisites
- A Confluence account (Atlassian Cloud or Server)
- Appropriate permissions for the spaces you want to access

## Step 1: Generate an API Token

### For Confluence Cloud (Atlassian Cloud)

1. **Sign in to Atlassian**
   - Go to [id.atlassian.com](https://id.atlassian.com) and sign in

2. **Navigate to Account Settings**
   - Click on your profile picture
   - Select **Manage account**

3. **Create API Token**
   - Go to the **Security** tab
   - Click **Create and manage API tokens**
   - Click **Create API token**

4. **Configure Token**
   - **Label**: Give your token a descriptive name (e.g., "DMTools Confluence Integration")
   - Click **Create**

5. **Copy Token**
   - **Important**: Copy the token immediately and store it securely
   - You won't be able to see it again!

### For Confluence Server/Data Center

1. **Use Username and Password**
   - You can use your regular Confluence username and password
   - Or create a dedicated service account

2. **Check with Administrator**
   - Confirm authentication method with your Confluence administrator
   - Some instances may require specific authentication setup

## Step 2: Find Your Confluence Instance URL

1. **Confluence Cloud URL Format**
   - Format: `https://your-domain.atlassian.net/wiki`
   - Example: `https://mycompany.atlassian.net/wiki`

2. **Confluence Server URL Format**
   - Format: `https://confluence.your-company.com`
   - Example: `https://confluence.mycompany.com`

## Step 3: Configure DMTools Integration

When setting up the Confluence integration in DMTools, use the following values:

### Required Parameters

1. **Confluence Base Path**
   - Your complete Confluence instance URL
   - Examples:
     - Cloud: `https://mycompany.atlassian.net/wiki`
     - Server: `https://confluence.mycompany.com`

### Authentication Methods (Choose One)

#### **Method 1: Separate Email, API Token, and Auth Type (Recommended)**

2. **Email Address**
   - Your Confluence account email address
   - Example: `john.doe@company.com`

3. **API Token**
   - Your Confluence API token (no manual encoding required)
   - Example: `ATTxxxxxxxxxxxxxxxx`

4. **Authentication Type**
   - Choose the authentication method:
     - **Basic**: For traditional username:password or email:token authentication (default)
     - **Bearer**: For standalone API tokens or PAT tokens

*Note: The system automatically handles authentication formatting internally.*

#### **Method 2: Legacy Base64 Token**

2. **Login/Password Token (Legacy)**
   - **For Cloud**: Use your email and API token in format `base64(email:token)`
     - Example: `am9obi5kb2VAY29tcGFueS5jb206QVRUeHh4eHh4eHh4eHh4eHh4`
   - **For Server**: Use your username and password in format `base64(username:password)`
     - Example: `am9obmRvZTpteXBhc3N3b3Jk`

*Note: This method requires manual base64 encoding of `email:token` or `username:password`.*

### Optional Parameters

5. **Default Space**
   - The space key for your default Confluence space
   - Example: `TEAM` or `DEV`
   - Used as default when no space is specified

6. **GraphQL Path**
   - Usually leave as default unless using custom GraphQL endpoint
   - Default: `/graphql`

## Step 4: Test Connection

After configuration:
1. Use the test connection feature
2. Verify you can access your spaces
3. Check that pages are loading correctly

## Authentication Examples

### Basic Authentication (Default)
- **Email**: `developer@company.com`
- **API Token**: `ATATT3xFfGF0a1b2c3d4e5f6g7h8i9j0`
- **Auth Type**: `Basic`
- **Result**: System creates `Authorization: Basic base64(email:token)`

### Bearer Authentication
- **Email**: `developer@company.com` (optional, not used)
- **API Token**: `ATATT3xFfGF0a1b2c3d4e5f6g7h8i9j0`
- **Auth Type**: `Bearer`
- **Result**: System creates `Authorization: Bearer token`

### Legacy Method
- **Legacy Token**: `ZGV2ZWxvcGVyQGNvbXBhbnkuY29tOkFUQVRUM3hGZkdGMGExYjJjM2Q0ZTVmNmc3aDhpOWowCg==`
- **Result**: System uses `Authorization: Basic legacy-token`

## Security Considerations

- **Token Security**: Never share your API token
- **Email Protection**: Keep your email address secure when used in authentication
- **Account Permissions**: Use an account with minimal required permissions
- **Token Rotation**: Regularly rotate your API tokens
- **Auth Type Selection**: Use Basic for most cases, Bearer for PAT tokens

## Troubleshooting

### Common Issues

1. **Authentication Failed**
   - Verify your email and API token are correct
   - Check if the token is expired
   - Ensure the base path URL is correct
   - Verify the authentication type is appropriate for your token

2. **Permission Denied**
   - Verify you have access to the Confluence spaces
   - Check if your account has the required permissions
   - Contact your Confluence administrator

3. **Invalid URL**
   - Ensure the base path includes the full URL with protocol
   - For Cloud instances, make sure to include `/wiki` in the path
   - Verify the domain name is correct
   - Check if your instance is accessible from your network

4. **Authentication Type Issues**
   - Try switching between Basic and Bearer authentication types
   - Basic auth is recommended for most email:token combinations
   - Bearer auth is for standalone tokens or PAT tokens

### Migration from Legacy Method

If you're currently using the legacy base64 token method:

1. **Keep existing setup working**: Your current configuration will continue to work
2. **Optional upgrade**: You can switch to the new method for easier management
3. **New setup steps**:
   - Extract email and token from your current base64 string
   - Configure separate Email and API Token fields
   - Set Auth Type to "Basic"
   - Remove the legacy token field

## Support

If you encounter issues:
1. Check the connection test results
2. Verify your Confluence permissions
3. Contact your system administrator
4. Check DMTools logs for detailed error messages

---

**Note**: This integration supports both Atlassian Cloud and Server/Data Center instances. Choose the appropriate authentication method based on your setup and security requirements. 