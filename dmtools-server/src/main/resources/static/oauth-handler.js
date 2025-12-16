/**
 * OAuth Handler - Reusable module for handling OAuth authentication flows
 * 
 * Features:
 * - Automatic detection and handling of OAuth redirects (code/state parameters)
 * - Token exchange and storage
 * - Authentication state management
 * - OAuth flow initiation
 * - Session storage for tokens with expiration
 */

class OAuthHandler {
    constructor(options = {}) {
        this.options = {
            tokenStorageKey: 'dmtools_oauth_token',
            stateStorageKey: 'dmtools_oauth_state',
            autoHandleRedirect: true,
            redirectUrlCleanup: true,
            onTokenReceived: null,
            onAuthError: null,
            onAuthSuccess: null,
            debug: false,
            ...options
        };
        
        // Initialize
        this.init();
    }
    
    init() {
        if (this.options.autoHandleRedirect) {
            this.handleOAuthRedirect();
        }
    }
    
    /**
     * Check if current URL contains OAuth redirect parameters
     */
    isOAuthRedirect() {
        const urlParams = new URLSearchParams(window.location.search);
        const code = urlParams.get('code');
        const state = urlParams.get('state');
        return !!(code && state);
    }
    
    /**
     * Automatically handle OAuth redirect if present
     */
    async handleOAuthRedirect() {
        if (!this.isOAuthRedirect()) {
            this.log('No OAuth redirect detected');
            return;
        }
        
        this.log('OAuth redirect detected, processing...');
        
        const urlParams = new URLSearchParams(window.location.search);
        const code = urlParams.get('code');
        const state = urlParams.get('state');
        const error = urlParams.get('error');
        const errorDescription = urlParams.get('error_description');
        
        if (error) {
            this.log('OAuth error received:', error, errorDescription);
            if (this.options.onAuthError) {
                this.options.onAuthError(error, errorDescription);
            }
            return;
        }
        
        try {
            // Exchange code for token
            await this.exchangeCodeForToken(code, state);
            
            // Clean up URL if requested
            if (this.options.redirectUrlCleanup) {
                this.cleanupRedirectUrl();
            }
            
        } catch (error) {
            this.log('Error handling OAuth redirect:', error);
            if (this.options.onAuthError) {
                this.options.onAuthError('exchange_failed', error.message);
            }
        }
    }
    
    /**
     * Exchange authorization code for access token
     */
    async exchangeCodeForToken(code, state) {
        this.log('Exchanging code for token...', { code: code.substring(0, 10) + '...', state });
        
        try {
            const response = await fetch('/api/oauth-proxy/exchange', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    code: code,
                    state: state
                })
            });
            
            const data = await response.json();
            
            if (response.ok) {
                this.log('Token exchange successful');
                
                // Store token with expiration
                this.storeToken(data);
                
                // Notify success
                if (this.options.onTokenReceived) {
                    this.options.onTokenReceived(data);
                }
                if (this.options.onAuthSuccess) {
                    this.options.onAuthSuccess(data);
                }
                
                return data;
            } else {
                throw new Error(`Token exchange failed: ${data.error || data.message || 'Unknown error'}`);
            }
            
        } catch (error) {
            this.log('Token exchange error:', error);
            throw error;
        }
    }
    
    /**
     * Initiate OAuth flow
     */
    async initiateOAuth(provider = 'google', options = {}) {
        const config = {
            provider: provider,
            client_redirect_uri: options.redirectUri || window.location.href.split('?')[0],
            client_type: options.clientType || 'web',
            environment: options.environment || 'prod',
            ...options
        };
        
        this.log('Initiating OAuth flow:', config);
        
        try {
            const response = await fetch('/api/oauth-proxy/initiate', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(config)
            });
            
            const data = await response.json();
            
            if (response.ok) {
                this.log('OAuth initiation successful', data);
                
                // Store state for verification
                sessionStorage.setItem(this.options.stateStorageKey, data.state);
                
                // Redirect to OAuth provider
                window.location.href = data.auth_url;
                
                return data;
            } else {
                throw new Error(`OAuth initiation failed: ${data.error || data.message || 'Unknown error'}`);
            }
            
        } catch (error) {
            this.log('OAuth initiation error:', error);
            throw error;
        }
    }
    
    /**
     * Store token in localStorage with expiration
     */
    storeToken(tokenData) {
        const expirationTime = Date.now() + (tokenData.expires_in * 1000);
        const refreshExpirationTime = tokenData.refresh_expires_in 
            ? Date.now() + (tokenData.refresh_expires_in * 1000)
            : null;
        
        const tokenInfo = {
            access_token: tokenData.access_token,
            refresh_token: tokenData.refresh_token || null,
            token_type: tokenData.token_type || 'Bearer',
            expires_at: expirationTime,
            expires_in: tokenData.expires_in,
            refresh_expires_at: refreshExpirationTime,
            refresh_expires_in: tokenData.refresh_expires_in || null,
            stored_at: Date.now()
        };
        
        localStorage.setItem(this.options.tokenStorageKey, JSON.stringify(tokenInfo));
        this.log('Token stored with expiration:', new Date(expirationTime));
        if (refreshExpirationTime) {
            this.log('Refresh token stored with expiration:', new Date(refreshExpirationTime));
        }
    }
    
    /**
     * Get stored token if valid, or attempt to refresh if expired
     */
    async getStoredToken() {
        try {
            const tokenStr = localStorage.getItem(this.options.tokenStorageKey);
            if (!tokenStr) {
                return null;
            }
            
            const tokenInfo = JSON.parse(tokenStr);
            
            // Check if access token is expired (with 5 minute buffer)
            const now = Date.now();
            const bufferTime = 5 * 60 * 1000; // 5 minutes
            
            if (tokenInfo.expires_at && now > (tokenInfo.expires_at - bufferTime)) {
                this.log('Access token expired, attempting refresh...');
                
                // Try to refresh if refresh token is available and not expired
                if (tokenInfo.refresh_token) {
                    const refreshExpired = tokenInfo.refresh_expires_at && now > tokenInfo.refresh_expires_at;
                    if (!refreshExpired) {
                        try {
                            const refreshed = await this.refreshAccessToken(tokenInfo.refresh_token);
                            if (refreshed) {
                                this.log('Token refreshed successfully');
                                // Return the newly stored token by reading from localStorage
                                const refreshedTokenStr = localStorage.getItem(this.options.tokenStorageKey);
                                if (refreshedTokenStr) {
                                    return JSON.parse(refreshedTokenStr);
                                }
                            }
                        } catch (error) {
                            this.log('Token refresh failed:', error);
                        }
                    } else {
                        this.log('Refresh token expired');
                    }
                }
                
                // If refresh failed or no refresh token, clear and return null
                this.clearToken();
                return null;
            }
            
            return tokenInfo;
            
        } catch (error) {
            this.log('Error getting stored token:', error);
            this.clearToken();
            return null;
        }
    }
    
    /**
     * Synchronous version of getStoredToken (does not attempt refresh)
     */
    getStoredTokenSync() {
        try {
            const tokenStr = localStorage.getItem(this.options.tokenStorageKey);
            if (!tokenStr) {
                return null;
            }
            
            const tokenInfo = JSON.parse(tokenStr);
            
            // Check if token is expired (with 5 minute buffer)
            const now = Date.now();
            const bufferTime = 5 * 60 * 1000; // 5 minutes
            
            if (tokenInfo.expires_at && now > (tokenInfo.expires_at - bufferTime)) {
                return null;
            }
            
            return tokenInfo;
            
        } catch (error) {
            this.log('Error getting stored token:', error);
            return null;
        }
    }
    
    /**
     * Refresh access token using refresh token
     */
    async refreshAccessToken(refreshToken) {
        this.log('Refreshing access token...');
        
        try {
            const response = await fetch('/api/auth/refresh', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    refreshToken: refreshToken
                })
            });
            
            const data = await response.json();
            
            if (response.ok) {
                this.log('Token refresh successful');
                
                // Store new tokens (with rotation - new refresh token included)
                this.storeToken(data);
                
                // Notify success if callback is set
                if (this.options.onTokenReceived) {
                    this.options.onTokenReceived(data);
                }
                
                return data;
            } else {
                throw new Error(`Token refresh failed: ${data.error || data.message || 'Unknown error'}`);
            }
            
        } catch (error) {
            this.log('Token refresh error:', error);
            throw error;
        }
    }
    
    /**
     * Get authorization header value (async - may trigger token refresh)
     */
    async getAuthHeader() {
        const token = await this.getStoredToken();
        if (!token) {
            return null;
        }
        return `${token.token_type} ${token.access_token}`;
    }
    
    /**
     * Get authorization header value (synchronous - does not refresh)
     */
    getAuthHeaderSync() {
        const token = this.getStoredTokenSync();
        if (!token) {
            return null;
        }
        return `${token.token_type} ${token.access_token}`;
    }
    
    /**
     * Check if user is authenticated (synchronous check)
     */
    isAuthenticated() {
        return !!this.getStoredTokenSync();
    }
    
    /**
     * Clear stored token
     */
    clearToken() {
        localStorage.removeItem(this.options.tokenStorageKey);
        sessionStorage.removeItem(this.options.stateStorageKey);
        this.log('Token cleared');
    }
    
    /**
     * Test authentication by calling user info endpoint
     */
    async testAuthentication() {
        const authHeader = await this.getAuthHeader();
        if (!authHeader) {
            throw new Error('No authentication token available');
        }
        
        this.log('Testing authentication...');
        
        try {
            const response = await fetch('/api/auth/user', {
                method: 'GET',
                headers: {
                    'Authorization': authHeader,
                    'Content-Type': 'application/json'
                }
            });
            
            const data = await response.json();
            
            if (response.ok) {
                this.log('Authentication test successful', data);
                return data;
            } else {
                throw new Error(`Authentication test failed: ${data.error || 'Unknown error'}`);
            }
            
        } catch (error) {
            this.log('Authentication test error:', error);
            throw error;
        }
    }
    
    /**
     * Make authenticated API request (automatically refreshes token if needed)
     */
    async authenticatedFetch(url, options = {}) {
        const authHeader = await this.getAuthHeader();
        if (!authHeader) {
            throw new Error('No authentication token available');
        }
        
        const headers = {
            'Content-Type': 'application/json',
            'Authorization': authHeader,
            ...options.headers
        };
        
        return fetch(url, {
            ...options,
            headers
        });
    }
    
    /**
     * Clean up redirect URL parameters
     */
    cleanupRedirectUrl() {
        if (window.history && window.history.replaceState) {
            const cleanUrl = window.location.protocol + "//" + window.location.host + window.location.pathname;
            window.history.replaceState({}, document.title, cleanUrl);
            this.log('URL cleaned up');
        }
    }
    
    /**
     * Add authentication UI to existing button or create new one
     */
    addAuthButton(containerId, options = {}) {
        const container = document.getElementById(containerId);
        if (!container) {
            this.log('Container not found:', containerId);
            return;
        }
        
        const buttonConfig = {
            text: 'Authenticate with Google',
            className: 'btn-primary oauth-btn',
            style: 'margin: 5px;',
            provider: 'google',
            ...options
        };
        
        // Create button
        const button = document.createElement('button');
        button.textContent = buttonConfig.text;
        button.className = buttonConfig.className;
        button.style.cssText = buttonConfig.style;
        
        // Add click handler
        button.onclick = () => {
            this.initiateOAuth(buttonConfig.provider, buttonConfig);
        };
        
        container.appendChild(button);
        this.log('Auth button added to container:', containerId);
        
        return button;
    }
    
    /**
     * Get authentication status summary
     */
    getAuthStatus() {
        const token = this.getStoredTokenSync();
        if (!token) {
            return {
                authenticated: false,
                token: null,
                expiresAt: null,
                timeRemaining: null,
                hasRefreshToken: false
            };
        }
        
        const timeRemaining = token.expires_at - Date.now();
        const refreshTimeRemaining = token.refresh_expires_at 
            ? token.refresh_expires_at - Date.now()
            : null;
        
        return {
            authenticated: true,
            token: token.access_token.substring(0, 10) + '...',
            expiresAt: new Date(token.expires_at),
            timeRemaining: Math.max(0, Math.floor(timeRemaining / 1000)), // seconds
            timeRemainingFormatted: this.formatTimeRemaining(timeRemaining),
            hasRefreshToken: !!token.refresh_token,
            refreshExpiresAt: token.refresh_expires_at ? new Date(token.refresh_expires_at) : null,
            refreshTimeRemaining: refreshTimeRemaining ? Math.max(0, Math.floor(refreshTimeRemaining / 1000)) : null
        };
    }
    
    /**
     * Format time remaining in human readable format
     */
    formatTimeRemaining(milliseconds) {
        if (milliseconds <= 0) return 'Expired';
        
        const seconds = Math.floor(milliseconds / 1000);
        const minutes = Math.floor(seconds / 60);
        const hours = Math.floor(minutes / 60);
        
        if (hours > 0) {
            return `${hours}h ${minutes % 60}m`;
        } else if (minutes > 0) {
            return `${minutes}m ${seconds % 60}s`;
        } else {
            return `${seconds}s`;
        }
    }
    
    /**
     * Logout user
     */
    async logout() {
        this.log('Logging out...');
        
        try {
            // Clear local token first
            this.clearToken();
            
            // Call server logout endpoint
            await fetch('/logout', {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                }
            });
            
            this.log('Logout successful');
            
        } catch (error) {
            this.log('Logout error:', error);
            // Still clear local token even if server call fails
            this.clearToken();
        }
    }
    
    /**
     * Debug logging
     */
    log(...args) {
        if (this.options.debug) {
            console.log('[OAuthHandler]', ...args);
        }
    }
}

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = OAuthHandler;
}

// Also expose globally for easy usage
window.OAuthHandler = OAuthHandler; 