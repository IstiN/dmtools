# Jira Integration Setup Guide

## Overview
This guide will help you set up the Jira integration for accessing issues, projects, and other Jira features.

## Prerequisites
- A Jira account (Atlassian Cloud or Server)
- Appropriate permissions for the projects you want to access

## Step 1: Generate an API Token

### For Jira Cloud (Atlassian Cloud)

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
   - **Label**: Give your token a descriptive name (e.g., "DMTools Integration")
   - Click **Create**

5. **Copy Token**
   - **Important**: Copy the token immediately and store it securely
   - You won't be able to see it again!

### For Jira Server/Data Center

1. **Use Username and Password**
   - You can use your regular Jira username and password
   - Or create a dedicated service account

2. **Check with Administrator**
   - Confirm authentication method with your Jira administrator
   - Some instances may require specific authentication setup

## Step 2: Find Your Jira Instance URL

1. **Jira Cloud URL Format**
   - Format: `https://your-domain.atlassian.net`
   - Example: `https://mycompany.atlassian.net`

2. **Jira Server URL Format**
   - Format: `https://your-jira-server.com`
   - Example: `https://jira.mycompany.com`

## Step 3: Configure Integration

When setting up the Jira integration in DMTools, use the following values:

### Required Parameters

1. **Jira Base Path**
   - Your complete Jira instance URL
   - Examples:
     - Cloud: `https://mycompany.atlassian.net`
     - Server: `https://jira.mycompany.com`

2. **Login/Password Token**
   - **For Cloud**: Use your email and API token in format `email:token`
     - Example: `john.doe@company.com:ATTxxxxxxxxxxxxxxxx`
   - **For Server**: Use your username and password in format `username:password`
     - Example: `johndoe:mypassword`

### Optional Parameters

3. **Auth Type**
   - Usually leave as default or set to `Basic`
   - Check with your administrator for specific requirements

## Step 4: Test Connection

After configuration:
1. Use the test connection feature
2. Verify you can access your projects
3. Check that issues are loading correctly

## Security Considerations

- **Token Security**: Never share your API token
- **Email Protection**: Keep your email address secure when used in authentication
- **Account Permissions**: Use an account with minimal required permissions
- **Token Rotation**: Regularly rotate your API tokens

## Troubleshooting

### Common Issues

1. **Authentication Failed**
   - Verify your email and API token are correct
   - Check if the token is expired
   - Ensure the base path URL is correct

2. **Permission Denied**
   - Verify you have access to the Jira projects
   - Check if your account has the required permissions
   - Contact your Jira administrator

3. **Invalid URL**
   - Ensure the base path includes the full URL with protocol
   - Verify the domain name is correct
   - Check if your instance is accessible from your network

4. **Rate Limiting**
   - Jira has API rate limits
   - Wait if you encounter rate limit errors
   - Consider using a dedicated service account

### Getting Help

If you encounter issues:
1. Check the Jira API documentation
2. Verify your API token in Atlassian account settings
3. Contact your Jira administrator for server-specific issues

## Required Permissions

Your Jira account should have the following permissions:
- **Browse Projects**: View projects and issues
- **View Issues**: Access issue details
- **Edit Issues**: Modify issues (if needed)
- **Create Issues**: Create new issues (if needed)

## Additional Resources

- [Atlassian API Tokens Documentation](https://support.atlassian.com/atlassian-account/docs/manage-api-tokens-for-your-atlassian-account/)
- [Jira REST API Documentation](https://developer.atlassian.com/cloud/jira/platform/rest/v3/)
- [Jira Server API Documentation](https://docs.atlassian.com/software/jira/docs/api/REST/)

## Examples

### Cloud Configuration
```
Base Path: https://mycompany.atlassian.net
Token: john.doe@company.com:ATTxxxxxxxxxxxxxxxx
Auth Type: Basic
```

### Server Configuration
```
Base Path: https://jira.mycompany.com
Token: johndoe:mypassword
Auth Type: Basic
``` 