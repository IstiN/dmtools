// Workspace Management Module
class WorkspaceManager {
    constructor() {
        console.log('WorkspaceManager constructor called');
        this.workspaces = [];
        this.initialized = false;
        this.init();
    }

    getAuthHeaders() {
        return {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        };
    }

    async init() {
        console.log('WorkspaceManager.init() called');
        
        // Check if we're on the workspaces page by looking for specific workspace elements
        const isWorkspacePage = this.isWorkspacePage();
        
        if (!isWorkspacePage) {
            console.log('Not on workspaces page, skipping initialization');
            return;
        }
        
        console.log('On workspaces page, initializing workspace manager');
        
        // Bind events first
        this.bindEvents();
        
        // Wait for auth manager to initialize
        const authManager = await this.waitForAuthManager();
        
        console.log('AuthManager available:', !!authManager);
        if (authManager) {
            // Register callback for auth state changes
            authManager.onAuthChange((isAuthenticated, user) => {
                console.log('Auth state changed:', isAuthenticated, user);
                if (isAuthenticated) {
                    this.showWorkspacesInterface();
                    this.loadWorkspaces();
                } else {
                    this.showUnauthorizedState();
                    this.bindUnauthorizedLoginButton();
                }
            });

            // Check current state
            const isAuthenticated = authManager.isAuthenticated();
            const currentUser = authManager.getCurrentUser();
            
            console.log('Current auth state:', {
                isAuthenticated,
                currentUser,
                authManagerInitialized: authManager.initialized
            });
            
            if (isAuthenticated && currentUser) {
                this.showWorkspacesInterface();
                await this.loadWorkspaces();
            } else {
                // Try to reload the current user in case auth state changed
                const refreshedUser = await authManager.loadCurrentUser();
                if (refreshedUser) {
                    console.log('User authenticated after refresh');
                    this.showWorkspacesInterface();
                    await this.loadWorkspaces();
                } else {
                    console.log('User still not authenticated after refresh');
                    this.showUnauthorizedState();
                    this.bindUnauthorizedLoginButton();
                }
            }
        } else {
            console.log('AuthManager not available');
            this.showUnauthorizedState();
            this.bindUnauthorizedLoginButton();
        }
        
        this.initialized = true;
    }

    isWorkspacePage() {
        // Check for workspace elements
        return document.getElementById('workspaces-container') || 
               document.getElementById('loading-state') || 
               document.getElementById('unauthorized-state') ||
               document.getElementById('workspace-tabs-container');
    }

    async waitForAuthManager() {
        console.log('Waiting for AuthManager...');
        
        // If auth manager is already initialized, use it
        if (window.authManager && window.authManager.initialized) {
            console.log('AuthManager already initialized');
            return window.authManager;
        }
        
        // Wait up to 5 seconds for auth manager to be available
        for (let i = 0; i < 50; i++) {
            if (window.authManager && window.authManager.initialized) {
                console.log('AuthManager initialized after', i * 100, 'ms');
                return window.authManager;
            }
            await new Promise(resolve => setTimeout(resolve, 100));
        }
        
        console.log('AuthManager initialization timeout');
        
        // If auth manager exists but not initialized, try to manually initialize it
        if (window.authManager && !window.authManager.initialized) {
            console.log('Trying to manually initialize AuthManager');
            try {
                await window.authManager.loadCurrentUser();
                window.authManager.initialized = true;
                return window.authManager;
            } catch (e) {
                console.error('Failed to initialize AuthManager:', e);
            }
        }
        
        return null;
    }

    showWorkspacesInterface() {
        const loadingState = document.getElementById('loading-state');
        const unauthorizedState = document.getElementById('unauthorized-state');
        const tabsContainer = document.getElementById('workspace-tabs-container');
        const createBtn = document.getElementById('show-create-form');
        
        if (loadingState) loadingState.style.display = 'none';
        if (unauthorizedState) unauthorizedState.style.display = 'none';
        if (tabsContainer) tabsContainer.style.display = 'flex';
        if (createBtn) createBtn.style.display = 'inline-flex';
    }

    showUnauthorizedState() {
        const loadingState = document.getElementById('loading-state');
        const tabsContainer = document.getElementById('workspace-tabs-container');
        const createBtn = document.getElementById('show-create-form');
        const unauthorizedState = document.getElementById('unauthorized-state');
        const workspacesContainer = document.getElementById('workspaces-container');
        const createSection = document.getElementById('create-workspace-section');
        const emptyState = document.getElementById('empty-state');
        const errorState = document.getElementById('error-state');
        
        if (loadingState) loadingState.style.display = 'none';
        if (tabsContainer) tabsContainer.style.display = 'none';
        if (createBtn) createBtn.style.display = 'none';
        if (unauthorizedState) unauthorizedState.style.display = 'block';
        
        // Hide any other workspace-related elements
        if (workspacesContainer) workspacesContainer.style.display = 'none';
        if (createSection) createSection.style.display = 'none';
        if (emptyState) emptyState.style.display = 'none';
        if (errorState) errorState.style.display = 'none';
    }

    getInitials(name) {
        if (!name) return '';
        return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
    }

    async loadWorkspaces() {
        console.log('loadWorkspaces() called');
        try {
            this.showLoadingState();
            const response = await fetch('/api/workspaces', {
                headers: this.getAuthHeaders(),
                credentials: 'include'
            });
            console.log('Workspaces API response:', response.status);
            if (response.ok) {
                this.workspaces = await response.json();
                console.log('Loaded workspaces:', this.workspaces);
                
                // Make sure we're still on the workspaces page before rendering
                if (this.isWorkspacePage()) {
                    this.renderWorkspaces();
                } else {
                    console.log('No longer on workspaces page, skipping render');
                }
            } else {
                console.error('Failed to load workspaces:', response.status);
                if (this.isWorkspacePage()) {
                    this.showErrorState('Failed to load workspaces');
                }
            }
        } catch (error) {
            console.error('Failed to load workspaces:', error);
            if (this.isWorkspacePage()) {
                this.showErrorState('Network error occurred');
            }
        }
    }

    renderWorkspaces() {
        const workspacesContainer = document.getElementById('workspaces-container');
        const emptyState = document.getElementById('empty-state');
        const loadingState = document.getElementById('loading-state');
        const tabsContainer = document.getElementById('workspace-tabs-container');
        
        // Hide loading state
        if (loadingState) loadingState.style.display = 'none';
        
        if (this.workspaces.length === 0) {
            // Show empty state
            if (emptyState) emptyState.style.display = 'block';
            if (workspacesContainer) workspacesContainer.style.display = 'none';
            if (tabsContainer) tabsContainer.style.display = 'none';
            return;
        }
        
        // Show workspaces container
        if (workspacesContainer) workspacesContainer.style.display = 'block';
        if (tabsContainer) tabsContainer.style.display = 'flex';
        if (emptyState) emptyState.style.display = 'none';
        
        // Render tabs
        this.renderWorkspaceTabs();
        
        // Show the first workspace by default
        if (this.workspaces.length > 0) {
            this.switchWorkspace(this.workspaces[0].id, document.querySelector('.workspace-tab'));
        }
    }
    
    bindWorkspaceCardEvents() {
        document.querySelectorAll('.workspace-card').forEach(card => {
            card.addEventListener('click', (e) => {
                if (e.target.closest('button')) return;
                const id = card.dataset.workspaceId;
                this.openWorkspaceModal(id);
            });
        });

        document.querySelectorAll('.share-workspace').forEach(button => {
            button.addEventListener('click', (e) => {
                e.stopPropagation();
                const id = e.currentTarget.dataset.workspaceId;
                this.openWorkspaceModal(id);
            });
        });

        document.querySelectorAll('.delete-workspace').forEach(button => {
            button.addEventListener('click', (e) => {
                e.stopPropagation();
                const id = e.currentTarget.dataset.workspaceId;
                const workspace = this.workspaces.find(ws => ws.id === id);
                if (workspace && confirm(`Are you sure you want to delete "${workspace.name}"? This action cannot be undone.`)) {
                    this.deleteWorkspace(id);
                }
            });
        });
    }
    
    renderWorkspaceTabs() {
        const tabsContainer = document.getElementById('workspace-tabs-container');
        if (!tabsContainer) return;

        // Clear existing tabs
        tabsContainer.innerHTML = '';

        // Add a tab for each workspace
        if (this.workspaces.length > 0) {
            this.workspaces.forEach((workspace, index) => {
                const tab = document.createElement('button');
                tab.className = `workspace-tab ${index === 0 ? 'active' : ''}`;
                tab.innerHTML = `<span>${workspace.name}</span>`;
                tab.onclick = () => this.switchWorkspace(workspace.id, tab);
                tabsContainer.appendChild(tab);
            });
        }
    }

    switchWorkspace(workspaceId, tabElement) {
        // Remove active class from all tabs
        document.querySelectorAll('.workspace-tab').forEach(tab => {
            tab.classList.remove('active');
        });

        // Add active class to selected tab
        if (tabElement) {
            tabElement.classList.add('active');
        }

        // Find the workspace
        const workspace = this.workspaces.find(ws => ws.id === workspaceId);
        if (!workspace) return;

        // Get the container where we'll show workspace content
        const container = document.getElementById('workspaces-container');
        if (!container) return;

        // Generate workspace content with placeholders
        container.innerHTML = this.renderWorkspaceContent(workspace);
        
        // Bind events for the workspace actions
        this.bindWorkspaceActionEvents(workspace);
        
        console.log('Switched to workspace:', workspaceId);
    }
    
    renderWorkspaceContent(workspace) {
        const currentUser = window.authManager ? window.authManager.getCurrentUser() : null;
        
        return `
            <div class="workspace-detail">
                <div class="workspace-header">
                    <div class="workspace-header__info">
                        <div class="workspace-icon large">
                            ${workspace.name.substring(0, 2).toUpperCase()}
                        </div>
                        <div>
                            <h2 class="workspace-title">${workspace.name}</h2>
                            <p class="workspace-description">${workspace.description || 'No description provided.'}</p>
                        </div>
                    </div>
                    <div class="workspace-header__actions">
                        <button class="btn btn-secondary edit-workspace" data-workspace-id="${workspace.id}">
                            <i class="fas fa-edit"></i>
                            Edit
                        </button>
                        <button class="btn btn-secondary share-workspace" data-workspace-id="${workspace.id}">
                            <i class="fas fa-share-alt"></i>
                            Share
                        </button>
                        <button class="btn btn-tertiary delete-workspace" data-workspace-id="${workspace.id}">
                            <i class="fas fa-trash"></i>
                            Delete
                        </button>
                    </div>
                </div>
                
                <div class="workspace-content">
                    <div class="workspace-section">
                        <div class="section-header">
                            <h3 class="section-title">Agents</h3>
                            <button class="btn btn-primary btn-small">
                                <i class="fas fa-plus"></i>
                                Add Agent
                            </button>
                        </div>
                        <div class="workspace-placeholder">
                            <i class="fas fa-robot"></i>
                            <p>No agents added to this workspace yet</p>
                            <button class="btn btn-secondary btn-small">Add your first agent</button>
                        </div>
                    </div>
                    
                    <div class="workspace-section">
                        <div class="section-header">
                            <h3 class="section-title">Applications</h3>
                            <button class="btn btn-primary btn-small">
                                <i class="fas fa-plus"></i>
                                Add Application
                            </button>
                        </div>
                        <div class="workspace-placeholder">
                            <i class="fas fa-th-large"></i>
                            <p>No applications added to this workspace yet</p>
                            <button class="btn btn-secondary btn-small">Add your first application</button>
                        </div>
                    </div>
                    
                    <div class="workspace-section">
                        <div class="section-header">
                            <h3 class="section-title">Integrations</h3>
                            <button class="btn btn-primary btn-small">
                                <i class="fas fa-plus"></i>
                                Add Integration
                            </button>
                        </div>
                        <div class="workspace-placeholder">
                            <i class="fas fa-plug"></i>
                            <p>No integrations configured for this workspace</p>
                            <button class="btn btn-secondary btn-small">Set up your first integration</button>
                        </div>
                    </div>
                    
                    <div class="workspace-section">
                        <div class="section-header">
                            <h3 class="section-title">Members</h3>
                            <button class="btn btn-primary btn-small">
                                <i class="fas fa-user-plus"></i>
                                Invite Member
                            </button>
                        </div>
                        <div class="workspace-members-list">
                            <div class="workspace-member">
                                <div class="member-avatar">
                                    ${this.getInitials(currentUser ? currentUser.givenName : 'U')}
                                </div>
                                <div class="member-info">
                                    <div class="member-name">${currentUser ? currentUser.givenName : 'User'}</div>
                                    <div class="member-role">Owner</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }
    
    bindWorkspaceActionEvents(workspace) {
        // Edit button
        document.querySelectorAll('.edit-workspace').forEach(btn => {
            btn.addEventListener('click', () => {
                // In a real app, this would open an edit form
                alert('Edit functionality would open here');
            });
        });
        
        // Share button
        document.querySelectorAll('.share-workspace').forEach(btn => {
            btn.addEventListener('click', () => {
                // In a real app, this would open a sharing dialog
                alert('Share functionality would open here');
            });
        });
        
        // Delete button
        document.querySelectorAll('.delete-workspace').forEach(btn => {
            btn.addEventListener('click', () => {
                if (confirm(`Are you sure you want to delete "${workspace.name}"? This action cannot be undone.`)) {
                    this.deleteWorkspace(workspace.id);
                }
            });
        });
    }

    renderWorkspaceCard(workspace) {
        const currentUser = window.authManager ? window.authManager.getCurrentUser() : null;
        const userRole = this.getUserRoleInWorkspace(workspace, currentUser);
        
        return `
            <div class="workspace-card" data-workspace-id="${workspace.id}">
                <div class="workspace-card__header">
                    <div class="workspace-icon">
                        ${workspace.name.substring(0, 2).toUpperCase()}
                    </div>
                    <div class="workspace-info">
                        <h3 class="workspace-name">${workspace.name}</h3>
                        <div class="workspace-meta">
                            <span class="workspace-owner">${userRole}</span>
                            <span class="workspace-date">Created ${new Date(workspace.createdAt).toLocaleDateString()}</span>
                        </div>
                    </div>
                </div>
                <div class="workspace-card__body">
                    <p>${workspace.description || 'No description provided.'}</p>
                </div>
                <div class="workspace-card__footer">
                    <div class="workspace-members">
                        <div class="member-avatars">
                            <div class="member-avatar" title="${currentUser ? currentUser.givenName : 'User'}">
                                ${this.getInitials(currentUser ? currentUser.givenName : 'U')}
                            </div>
                        </div>
                        <span>1 member</span>
                    </div>
                    <div class="workspace-actions">
                        <button class="btn btn-icon share-workspace" data-workspace-id="${workspace.id}" title="Share workspace">
                            <i class="fas fa-share-alt"></i>
                        </button>
                        <button class="btn btn-icon delete-workspace" data-workspace-id="${workspace.id}" title="Delete workspace">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
            </div>
        `;
    }

    getUserRoleInWorkspace(workspace, currentUser = null) {
        if (!currentUser) return 'Unknown';
        
        // In a real application, this would check the user's role in the workspace
        // For now, we'll assume the current user is the owner
        return 'Owner';
    }

    bindEvents() {
        // Create workspace form
        const createForm = document.getElementById('create-workspace-form');
        if (createForm) {
            createForm.addEventListener('submit', (e) => this.handleCreateWorkspace(e));
        }
        
        // Show create form button
        const showCreateFormBtn = document.getElementById('show-create-form');
        if (showCreateFormBtn) {
            showCreateFormBtn.addEventListener('click', () => this.showCreateForm());
        }
        
        // Cancel create button
        const cancelCreateBtn = document.getElementById('cancel-create');
        if (cancelCreateBtn) {
            cancelCreateBtn.addEventListener('click', () => this.hideCreateForm());
        }
        
        // Create first workspace button in empty state
        const createFirstBtn = document.getElementById('create-first-workspace');
        if (createFirstBtn) {
            createFirstBtn.addEventListener('click', () => this.showCreateForm());
        }
        
        // Retry load button
        const retryBtn = document.getElementById('retry-load');
        if (retryBtn) {
            retryBtn.addEventListener('click', () => this.loadWorkspaces());
        }
        
        // Close workspace modal
        const closeModalBtn = document.getElementById('close-workspace-modal');
        if (closeModalBtn) {
            closeModalBtn.addEventListener('click', () => {
                const modal = document.getElementById('workspace-modal');
                if (modal) modal.style.display = 'none';
            });
        }
    }

    bindUnauthorizedLoginButton() {
        const loginBtn = document.getElementById('unauthorized-login-btn');
        if (loginBtn) {
            loginBtn.addEventListener('click', () => {
                if (window.authManager) {
                    window.authManager.showLoginModal();
                }
            });
        }
    }

    showLoadingState() {
        const loadingState = document.getElementById('loading-state');
        const workspacesContainer = document.getElementById('workspaces-container');
        const createSection = document.getElementById('create-workspace-section');
        const emptyState = document.getElementById('empty-state');
        const errorState = document.getElementById('error-state');
        
        if (loadingState) loadingState.style.display = 'block';
        if (workspacesContainer) workspacesContainer.style.display = 'none';
        if (createSection) createSection.style.display = 'none';
        if (emptyState) emptyState.style.display = 'none';
        if (errorState) errorState.style.display = 'none';
    }

    showErrorState(message) {
        const loadingState = document.getElementById('loading-state');
        const errorState = document.getElementById('error-state');
        const errorMessage = document.getElementById('error-message');
        
        if (loadingState) loadingState.style.display = 'none';
        if (errorState) errorState.style.display = 'block';
        if (errorMessage) errorMessage.textContent = message || 'An error occurred.';
    }

    showCreateForm() {
        const createSection = document.getElementById('create-workspace-section');
        const workspacesContainer = document.getElementById('workspaces-container');
        const emptyState = document.getElementById('empty-state');
        
        if (createSection) createSection.style.display = 'block';
        if (workspacesContainer) workspacesContainer.style.display = 'none';
        if (emptyState) emptyState.style.display = 'none';
    }

    hideCreateForm() {
        const createSection = document.getElementById('create-workspace-section');
        const workspacesContainer = document.getElementById('workspaces-container');
        const emptyState = document.getElementById('empty-state');
        
        if (createSection) createSection.style.display = 'none';
        if (this.workspaces.length === 0) {
            if (emptyState) emptyState.style.display = 'block';
            if (workspacesContainer) workspacesContainer.style.display = 'none';
        } else {
            if (emptyState) emptyState.style.display = 'none';
            if (workspacesContainer) {
                // This is the main view for a single workspace, which should be a block element.
                workspacesContainer.style.display = 'block';
            }
        }
    }

    async handleCreateWorkspace(e) {
        e.preventDefault();
        
        const nameInput = document.getElementById('workspace-name');
        const descInput = document.getElementById('workspace-description');
        
        if (!nameInput || !nameInput.value.trim()) {
            alert('Please enter a workspace name.');
            return;
        }
        
        const workspaceData = {
            name: nameInput.value.trim(),
            description: descInput ? descInput.value.trim() : ''
        };
        
        try {
            const response = await fetch('/api/workspaces', {
                method: 'POST',
                headers: this.getAuthHeaders(),
                credentials: 'include',
                body: JSON.stringify(workspaceData)
            });
            
            if (response.ok) {
                const newWorkspace = await response.json();
                this.workspaces.push(newWorkspace);
                
                // Reset form
                if (nameInput) nameInput.value = '';
                if (descInput) descInput.value = '';
                
                // Hide the create form
                this.hideCreateForm();
                
                // Render workspace tabs first
                this.renderWorkspaceTabs();
                
                // Show the workspaces container and ensure it's visible
                const workspacesContainer = document.getElementById('workspaces-container');
                if (workspacesContainer) {
                    workspacesContainer.style.display = 'block';
                }
                
                // Add a small delay to ensure DOM updates are processed
                await new Promise(resolve => setTimeout(resolve, 50));
                
                // Switch to the newly created workspace
                const tabElements = document.querySelectorAll('.workspace-tab');
                const lastTab = tabElements[tabElements.length - 1];
                if (lastTab) {
                    this.switchWorkspace(newWorkspace.id, lastTab);
                }
            } else {
                alert('Failed to create workspace. Please try again.');
            }
        } catch (error) {
            console.error('Error creating workspace:', error);
            alert('An error occurred while creating the workspace.');
        }
    }

    async deleteWorkspace(workspaceId) {
        try {
            const response = await fetch(`/api/workspaces/${workspaceId}`, {
                method: 'DELETE',
                headers: this.getAuthHeaders(),
                credentials: 'include'
            });
            
            if (response.ok) {
                // Remove from array
                this.workspaces = this.workspaces.filter(ws => ws.id !== workspaceId);
                this.renderWorkspaces();
            } else {
                alert('Failed to delete workspace. Please try again.');
            }
        } catch (error) {
            console.error('Error deleting workspace:', error);
            alert('An error occurred while deleting the workspace.');
        }
    }

    async openWorkspaceModal(workspaceId) {
        const workspace = this.workspaces.find(ws => ws.id === workspaceId);
        if (!workspace) return;
        
        const modal = document.getElementById('workspace-modal');
        const modalContent = document.getElementById('workspace-modal-content');
        
        if (!modal || !modalContent) return;
        
        // Show loading state in modal
        modalContent.innerHTML = '<div class="workspace-empty-state"><i class="fas fa-spinner fa-spin"></i><h3>Loading workspace details...</h3></div>';
        modal.style.display = 'block';
        
        try {
            // In a real app, we'd fetch more details here
            // For now, just use what we have
            modalContent.innerHTML = this.renderWorkspaceDetails(workspace);
            
            // Bind events for the modal
            this.bindModalEvents(workspace);
            
        } catch (error) {
            console.error('Error loading workspace details:', error);
            modalContent.innerHTML = '<div class="workspace-empty-state"><i class="fas fa-exclamation-triangle" style="color: var(--danger-color);"></i><h3>Failed to load details</h3><p>Please try again later.</p></div>';
        }
    }

    renderWorkspaceDetails(workspace) {
        return `
            <div class="workspace-detail">
                <div class="workspace-detail__header">
                    <div class="workspace-icon large">
                        ${workspace.name.substring(0, 2).toUpperCase()}
                    </div>
                    <div class="workspace-info">
                        <h2 class="workspace-name">${workspace.name}</h2>
                        <div class="workspace-meta">
                            <span class="workspace-date">Created ${new Date(workspace.createdAt).toLocaleDateString()}</span>
                        </div>
                    </div>
                </div>
                
                <div class="workspace-detail__body">
                    <h3>Description</h3>
                    <p>${workspace.description || 'No description provided.'}</p>
                    
                    <h3>Members</h3>
                    <div class="workspace-members-list">
                        <div class="workspace-member">
                            <div class="member-avatar">
                                ${this.getInitials(window.authManager?.getCurrentUser()?.givenName || 'U')}
                            </div>
                            <div class="member-info">
                                <div class="member-name">${window.authManager?.getCurrentUser()?.givenName || 'User'}</div>
                                <div class="member-role">Owner</div>
                            </div>
                        </div>
                    </div>
                    
                    <h3>Settings</h3>
                    <div class="workspace-settings">
                        <button class="btn btn-danger" id="delete-workspace-btn">
                            <i class="fas fa-trash"></i>
                            Delete Workspace
                        </button>
                    </div>
                </div>
            </div>
        `;
    }

    bindModalEvents(workspace) {
        // Delete button in modal
        const deleteBtn = document.getElementById('delete-workspace-btn');
        if (deleteBtn) {
            deleteBtn.addEventListener('click', () => {
                if (confirm(`Are you sure you want to delete "${workspace.name}"? This action cannot be undone.`)) {
                    this.deleteWorkspace(workspace.id);
                    
                    // Close modal
                    const modal = document.getElementById('workspace-modal');
                    if (modal) modal.style.display = 'none';
                }
            });
        }
    }

    // Method to clean up resources when navigating away
    destroy() {
        console.log('WorkspaceManager.destroy() called');
        // Remove event listeners if needed
        this.initialized = false;
    }
}

// Make WorkspaceManager globally available
window.WorkspaceManager = WorkspaceManager; 