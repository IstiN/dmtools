# OpenAI Integration Setup Guide

## Overview
This guide will help you set up the OpenAI integration for accessing GPT models and other AI capabilities.

## Prerequisites
- An OpenAI account
- Valid payment method (for API usage beyond free tier)
- Understanding of OpenAI's usage policies

## Step 1: Create an OpenAI Account

1. **Sign Up**
   - Go to [platform.openai.com](https://platform.openai.com)
   - Click **Sign up** or use your existing account

2. **Verify Your Account**
   - Complete email verification
   - Provide phone number verification if required

3. **Set Up Billing** (if needed)
   - Add a payment method for API usage
   - Set spending limits if desired

## Step 2: Generate an API Key

1. **Navigate to API Keys**
   - Go to [platform.openai.com/api-keys](https://platform.openai.com/api-keys)
   - Sign in if prompted

2. **Create New API Key**
   - Click **Create new secret key**
   - Give your key a descriptive name (e.g., "DMTools Integration")
   - Choose appropriate permissions (usually "All" for general use)

3. **Copy and Store Key**
   - **Important**: Copy the API key immediately
   - Store it securely - you won't be able to see it again
   - Format: `sk-...` (starts with "sk-")

## Step 3: Configure Integration

When setting up the OpenAI integration in DMTools, use the following values:

### Required Parameters

1. **API Key**
   - Paste the API key you generated
   - Format: `sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`

### Optional Parameters

2. **Base Path**
   - Default: `https://api.openai.com/v1`
   - Only change if using a custom endpoint or proxy

3. **Default Model**
   - Recommended: `gpt-4` or `gpt-4-turbo`
   - Budget option: `gpt-3.5-turbo`
   - Latest: `gpt-4o` (if available)

4. **Code AI Model**
   - Recommended: `gpt-4` or `gpt-4-turbo`
   - Specialized: `gpt-4o` (if available)

5. **Test AI Model**
   - Same as default model or specialized testing model

## Step 4: Understanding Models

### Available Models

1. **GPT-4 Models** (Recommended)
   - `gpt-4`: Most capable, higher cost
   - `gpt-4-turbo`: Faster, cost-effective
   - `gpt-4o`: Latest model (if available)

2. **GPT-3.5 Models** (Budget Option)
   - `gpt-3.5-turbo`: Fast and cost-effective
   - Good for simple tasks

### Model Selection Guidelines

- **General Use**: `gpt-4-turbo`
- **Complex Reasoning**: `gpt-4`
- **High Volume/Budget**: `gpt-3.5-turbo`
- **Code Generation**: `gpt-4` or `gpt-4-turbo`

## Step 5: Monitor Usage

1. **Check Usage Dashboard**
   - Go to [platform.openai.com/usage](https://platform.openai.com/usage)
   - Monitor your API usage and costs

2. **Set Limits**
   - Configure monthly spending limits
   - Set up usage alerts

## Security Considerations

- **API Key Security**: Never commit API keys to code repositories
- **Environment Variables**: Store keys in secure environment variables
- **Key Rotation**: Regularly rotate your API keys
- **Principle of Least Privilege**: Use organization keys with limited permissions when possible

## Troubleshooting

### Common Issues

1. **Authentication Failed**
   - Verify the API key is correct and complete
   - Check if the key has been deactivated
   - Ensure you're using the correct key format

2. **Rate Limiting**
   - OpenAI has rate limits based on your tier
   - Wait and retry if you hit limits
   - Consider upgrading your usage tier

3. **Billing Issues**
   - Ensure you have sufficient credits/payment method
   - Check if your account is in good standing
   - Verify billing information is up to date

4. **Model Not Available**
   - Some models require special access
   - Check if the model name is correct
   - Verify your account has access to the requested model

### Error Codes

- **401 Unauthorized**: Invalid API key
- **429 Too Many Requests**: Rate limit exceeded
- **400 Bad Request**: Invalid request format
- **500 Internal Server Error**: OpenAI service issue

## Cost Management

### Pricing Considerations

1. **Token-Based Pricing**
   - Charges based on input and output tokens
   - Different models have different rates

2. **Cost Optimization**
   - Use appropriate models for each task
   - Monitor usage regularly
   - Set spending limits

3. **Free Tier**
   - New accounts often get free credits
   - Check your current balance before heavy usage

## Additional Resources

- [OpenAI API Documentation](https://platform.openai.com/docs)
- [OpenAI Pricing](https://openai.com/pricing)
- [OpenAI Usage Policies](https://openai.com/policies/usage-policies)
- [Model Documentation](https://platform.openai.com/docs/models)

## Example Configuration

```
API Key: sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
Base Path: https://api.openai.com/v1
Default Model: gpt-4-turbo
Code AI Model: gpt-4
Test AI Model: gpt-4-turbo
```

## Best Practices

1. **Start Small**: Begin with simple requests to test the integration
2. **Monitor Costs**: Keep an eye on your usage and costs
3. **Use Appropriate Models**: Don't use expensive models for simple tasks
4. **Handle Errors**: Implement proper error handling for rate limits and failures
5. **Secure Storage**: Never expose API keys in client-side code or public repositories 