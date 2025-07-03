# GitHub Integration Setup Guide

## Overview
This guide will help you set up the GitHub integration for accessing repositories, pull requests, and other GitHub features.

## Prerequisites
- A GitHub account (personal or organization)
- Admin or appropriate permissions for the repositories you want to integrate

## Step 1: Generate a Personal Access Token

1. **Sign in to GitHub**
   - Go to [GitHub.com](https://github.com) and sign in to your account

2. **Navigate to Settings**
   - Click on your profile picture in the top-right corner
   - Select **Settings** from the dropdown menu

3. **Access Developer Settings**
   - Scroll down in the left sidebar
   - Click on **Developer settings**

4. **Create Personal Access Token**
   - Click on **Personal access tokens**
   - Select **Tokens (classic)** or **Fine-grained tokens** (recommended)
   - Click **Generate new token**

5. **Configure Token Settings**
   - **Note**: Give your token a descriptive name (e.g., "DMTools Integration")
   - **Expiration**: Set an appropriate expiration date
   - **Scopes**: Select the following permissions:
     - `repo` - Full control of private repositories
     - `read:org` - Read organization membership
     - `read:user` - Read user profile data
     - `user:email` - Access user email addresses

6. **Generate and Copy Token**
   - Click **Generate token**
   - **Important**: Copy the token immediately and store it securely
   - You won't be able to see it again!

## Step 2: Configure Integration

When setting up the GitHub integration in DMTools, use the following values:

### Required Parameters

1. **GitHub Token**
   - Paste the personal access token you generated
   - Format: `ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`

### Optional Parameters

2. **GitHub Base Path**
   - For GitHub.com: `https://api.github.com` (default)
   - For GitHub Enterprise: `https://your-domain.com/api/v3`

3. **GitHub Workspace**
   - Your GitHub username or organization name
   - Example: `microsoft`, `google`, `your-username`

4. **GitHub Repository**
   - Repository name without the organization prefix
   - Example: If URL is `github.com/microsoft/vscode`, enter `vscode`

5. **GitHub Branch**
   - Default branch to work with
   - Usually `main` or `master`

## Security Considerations

- **Token Security**: Never share your personal access token
- **Scope Limitation**: Only grant the minimum required permissions
- **Token Rotation**: Regularly rotate your tokens for security
- **Organization Policies**: Check if your organization has specific token policies

## Troubleshooting

### Common Issues

1. **Authentication Failed**
   - Verify the token is correct and not expired
   - Check if the token has the required scopes
   - Ensure the token belongs to the correct account

2. **Repository Access Denied**
   - Verify you have access to the repository
   - Check if the repository is private and your token has `repo` scope
   - Confirm the workspace/organization name is correct

3. **Rate Limiting**
   - GitHub has API rate limits
   - Authenticated requests have higher limits
   - Wait if you encounter rate limit errors

### Getting Help

If you encounter issues:
1. Check the GitHub API documentation
2. Verify your token permissions in GitHub settings
3. Contact your system administrator for organization-specific issues

## Additional Resources

- [GitHub Personal Access Tokens Documentation](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token)
- [GitHub API Documentation](https://docs.github.com/en/rest)
- [GitHub Enterprise Server Documentation](https://docs.github.com/en/enterprise-server) 