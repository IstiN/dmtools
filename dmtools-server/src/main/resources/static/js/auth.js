// Unified Authentication Manager
class AuthManager {
    constructor() {
        this.currentUser = null;
        this.loginModal = null;
        this.onAuthChangeCallbacks = [];
        this.initialized = false;
        this.init();
    }

    async init() {
        // Setup event listeners first
        this.setupEventListeners();
        
        // Load user and update UI
        await this.loadCurrentUser();
        this.initialized = true;
        this.updateUI();
    }

    // Load current user from server
    async loadCurrentUser() {
        try {
            console.log('AuthManager.loadCurrentUser() called');
            const response = await fetch('/api/auth/user', {
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                credentials: 'include'
            });
            
            if (response.ok) {
                const userData = await response.json();
                console.log('Auth user data received:', userData);
                
                // Check if user is actually authenticated
                if (userData.authenticated === false) {
                    console.log('User not authenticated according to response');
                    this.currentUser = null;
                } else if (userData.id && userData.email) {
                    console.log('User authenticated:', userData.email);
                    this.currentUser = userData;
                } else {
                    console.log('Invalid user data received');
                    this.currentUser = null;
                }
            } else {
                console.log('Auth API response not OK:', response.status);
                this.currentUser = null;
            }
            
            // Notify about auth state change
            this.notifyAuthChange();
            
            return this.currentUser;
        } catch (error) {
            console.error('Error loading current user:', error);
            this.currentUser = null;
            return null;
        }
    }

    // Check if user is authenticated
    isAuthenticated() {
        return this.currentUser !== null;
    }

    // Get current user
    getCurrentUser() {
        return this.currentUser;
    }

    // Update UI based on authentication state
    updateUI() {
        if (!this.initialized) {
            console.log('AuthManager not initialized, skipping UI update');
            return; // Don't update UI until fully initialized
        }

        console.log('updateUIForLoggedInUser: Checking authentication status...');
        
        if (this.isAuthenticated() && this.currentUser) {
            console.log('updateUIForLoggedInUser: User is authenticated. Updating UI.');
            // Update login button to show user info
            this.updateLoginButton(this.currentUser);
            
            // Show app layout and hide landing page if on index page
            const landingPage = document.getElementById('landing-page');
            const appLayout = document.getElementById('app-layout');
            
            if (landingPage && appLayout) {
                landingPage.style.display = 'none';
                appLayout.style.display = 'flex';
            }
        } else {
            console.log('updateUIForLoggedInUser: User is not authenticated.');
            // Reset login button
            this.updateLoginButton(null);
            
            // Show landing page and hide app layout if on index page
            const landingPage = document.getElementById('landing-page');
            const appLayout = document.getElementById('app-layout');
            
            if (landingPage && appLayout) {
                landingPage.style.display = 'block';
                appLayout.style.display = 'none';
            }
        }
    }
    
    // Update login button based on auth state
    updateLoginButton(user) {
        const loginBtn = document.querySelector('.btn-login');
        if (!loginBtn) return;
        
        if (user) {
            console.log('updateLoginButton: Updating UI for logged-in user:', user.givenName);
            loginBtn.innerHTML = `
                <div class="user-avatar-small">${user.givenName.substring(0, 1).toUpperCase()}</div>
                <span class="login-text">${user.givenName}</span>
            `;
            loginBtn.classList.add('logged-in');
        } else {
            loginBtn.innerHTML = `
                <i class="fas fa-sign-in-alt"></i>
                Login
            `;
            loginBtn.classList.remove('logged-in');
        }
    }

    // Create user profile UI
    createUserProfile() {
        const navActions = document.querySelector('.nav-actions');
        const loginBtn = document.querySelector('.btn-login');
        
        if (navActions && this.currentUser) {
            // Remove any existing user profile first
            const existingProfile = document.querySelector('.user-profile');
            if (existingProfile) {
                existingProfile.remove();
            }

            const userProfile = document.createElement('div');
            userProfile.className = 'user-profile';
            
            const displayName = this.currentUser.name || this.currentUser.email || 'User';
            const avatarSrc = this.currentUser.pictureUrl || '/img/default-avatar.svg';
            
            userProfile.innerHTML = `
                <div class="user-info">
                    <img src="${avatarSrc}" 
                         alt="User Avatar" class="user-avatar"
                         onerror="this.src='/img/default-avatar.svg'">
                    <span class="user-name">${displayName}</span>
                </div>
                <button class="btn btn-icon logout-btn" title="Logout">
                    <i class="fas fa-sign-out-alt"></i>
                </button>
            `;
            
            // Insert before login button or at the end
            if (loginBtn) {
                navActions.insertBefore(userProfile, loginBtn);
            } else {
                navActions.appendChild(userProfile);
            }

            // Add logout functionality
            const logoutBtn = userProfile.querySelector('.logout-btn');
            logoutBtn.addEventListener('click', () => this.logout());
        }
    }

    // Update existing user profile
    updateUserProfile(userProfile) {
        if (!this.currentUser) return;

        const avatar = userProfile.querySelector('.user-avatar');
        const userName = userProfile.querySelector('.user-name');

        if (avatar) {
            avatar.src = this.currentUser.pictureUrl || '/img/default-avatar.svg';
        }
        if (userName) {
            userName.textContent = this.currentUser.name || this.currentUser.email;
        }

        userProfile.style.display = 'flex';
    }

    // Setup event listeners
    setupEventListeners() {
        // Use event delegation for login button click
        document.body.addEventListener('click', (e) => {
            // Check if the clicked element or its parent is the login button
            const loginBtn = e.target.closest('.btn-login');
            if (loginBtn && !document.querySelector('.user-profile')) {
                // Only handle if user is not logged in (no user profile exists)
                e.preventDefault();
                e.stopPropagation();
                this.showLoginModal();
            }
        });
    }

    // Show login modal
    showLoginModal() {
        console.log('AUTH - showLoginModal called');
        if (this.loginModal) {
            console.log('AUTH - Modal already exists, showing it');
            this.loginModal.style.display = 'flex';
            this.loginModal.classList.add('show');
            return;
        }

        console.log('AUTH - Creating new login modal');
        // Create login modal
        this.loginModal = document.createElement('div');
        this.loginModal.className = 'login-modal';
        this.loginModal.id = 'loginModal';
        this.loginModal.innerHTML = `
            <div class="login-modal-content">
                <button class="login-modal-close">
                    <i class="fas fa-times"></i>
                </button>
                
                <div class="login-provider-selector">
                    <div class="login-header">
                        <h2>Welcome Back</h2>
                        <p>Choose your preferred login method</p>
                    </div>
                    
                    <div class="login-providers">
                        <a href="/oauth2/authorization/google" class="login-provider-btn login-provider-btn--google">
                            <i class="fab fa-google login-provider-icon"></i>
                            Continue with Google
                        </a>
                        
                        <a href="/oauth2/authorization/microsoft" class="login-provider-btn login-provider-btn--microsoft">
                            <i class="fab fa-microsoft login-provider-icon"></i>
                            Continue with Microsoft
                        </a>
                        
                        <a href="/oauth2/authorization/github" class="login-provider-btn login-provider-btn--github">
                            <i class="fab fa-github login-provider-icon"></i>
                            Continue with GitHub
                        </a>
                        
                        <div class="login-divider">
                            <span>or</span>
                        </div>
                        
                        <button class="login-provider-btn login-provider-btn--demo demo-login-btn">
                            <i class="fas fa-user login-provider-icon"></i>
                            Demo Login (testuser)
                        </button>
                    </div>
                    
                    <div class="login-footer">
                        <p>By continuing, you agree to our <a href="#">Terms of Service</a> and <a href="#">Privacy Policy</a></p>
                    </div>
                </div>
            </div>
        `;

        document.body.appendChild(this.loginModal);

        // Setup modal event listeners
        const closeBtn = this.loginModal.querySelector('.login-modal-close');
        const demoLoginBtn = this.loginModal.querySelector('.demo-login-btn');

        closeBtn.addEventListener('click', () => this.hideLoginModal());
        demoLoginBtn.addEventListener('click', () => this.loginWithDemo());

        // Close on background click
        this.loginModal.addEventListener('click', (e) => {
            if (e.target === this.loginModal) {
                this.hideLoginModal();
            }
        });

        console.log('AUTH - Setting modal display to flex and adding show class');
        this.loginModal.style.display = 'flex';
        // Add show class after a brief delay to trigger animation
        setTimeout(() => {
            this.loginModal.classList.add('show');
        }, 10);
    }

    // Hide login modal
    hideLoginModal() {
        if (this.loginModal) {
            this.loginModal.classList.remove('show');
            // Wait for animation to complete before hiding
            setTimeout(() => {
                this.loginModal.style.display = 'none';
            }, 300);
        }
    }

    // Demo login
    async loginWithDemo() {
        try {
            const response = await fetch('/api/auth/local-login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ username: 'testuser', password: 'secret123' })
            });

            if (response.ok) {
                console.log('✅ AUTH - Demo login successful, loading current user');
                await this.loadCurrentUser();
                console.log('✅ AUTH - Current user after login:', this.currentUser);
                this.updateUI();
                this.hideLoginModal();
            } else {
                console.error('❌ AUTH - Demo login failed');
                alert('Demo login failed. Please try again.');
            }
        } catch (error) {
            console.error('❌ AUTH - Demo login error:', error);
            alert('Network error occurred during login.');
        }
    }

    // Alias for compatibility
    loginAsDemo() {
        return this.loginWithDemo();
    }

    // Logout
    async logout() {
        try {
            const response = await fetch('/api/auth/logout', {
                method: 'POST',
                credentials: 'include'
            });

            // Clear user state regardless of response
            this.currentUser = null;
            
            // Clear all cookies
            this.clearAuthCookies();
            
            // Remove user profile
            const userProfile = document.querySelector('.user-profile');
            if (userProfile) {
                userProfile.remove();
            }

            this.updateUI();

        } catch (error) {
            // Still clear local state
            this.currentUser = null;
            this.clearAuthCookies();
            this.updateUI();
        }
    }

    // Clear authentication cookies
    clearAuthCookies() {
        // Clear JWT cookie
        document.cookie = 'jwt=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
        document.cookie = 'JSESSIONID=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    }

    // Register callback for auth state changes
    onAuthChange(callback) {
        this.onAuthChangeCallbacks.push(callback);
    }

    // Remove callback
    offAuthChange(callback) {
        const index = this.onAuthChangeCallbacks.indexOf(callback);
        if (index > -1) {
            this.onAuthChangeCallbacks.splice(index, 1);
        }
    }

    // Notify all registered callbacks about auth state change
    notifyAuthChange() {
        // Notify callbacks about auth state change
        this.onAuthChangeCallbacks.forEach(callback => {
            try {
                callback(this.isAuthenticated(), this.currentUser);
            } catch (error) {
                console.error('Auth callback error:', error);
            }
        });
    }
}

// Global instance
window.authManager = new AuthManager();

// Export for modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = AuthManager;
} 