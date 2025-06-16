// DMTools SPA Navigation Module
class SPANavigator {
    constructor() {
        this.currentPage = window.location.pathname;
        this.pageScripts = new Map();
        this.initialized = false;
        this.init();
    }

    init() {
        // Set up event listeners that will work when sidebar appears
        document.addEventListener('click', (e) => {
            const link = e.target.closest('.sidebar-nav__link');
            if (link && link.getAttribute('href')) {
                e.preventDefault();
                const url = link.getAttribute('href');
                this.navigateTo(url);
            }
        });

        // Handle browser back/forward buttons
        window.addEventListener('popstate', (e) => {
            this.loadPage(window.location.pathname, false);
        });

        // Register page-specific scripts
        this.registerPageScripts();
        
        // Mark as initialized
        this.initialized = true;
    }

    async navigateTo(url) {
        if (url === this.currentPage) return;
        
        await this.loadPage(url, true);
    }

    async loadPage(url, pushState = true) {
        try {
            // Show loading state
            const mainContent = document.querySelector('.app-layout__main');
            if (!mainContent) {
                // If no app layout, fallback to regular navigation
                window.location.href = url;
                return;
            }

            // Fetch the new page
            const response = await fetch(url);
            if (!response.ok) throw new Error('Failed to load page');

            const html = await response.text();
            const parser = new DOMParser();
            const doc = parser.parseFromString(html, 'text/html');

            // Extract the main content
            const newMain = doc.querySelector('.app-layout__main');
            if (!newMain) throw new Error('No main content found');

            // Update the main content
            mainContent.innerHTML = newMain.innerHTML;

            // Update page title
            const newTitle = doc.querySelector('title');
            if (newTitle) document.title = newTitle.textContent;

            // Update active state in sidebar
            this.updateActiveNavLink(url);

            // Update browser history
            if (pushState) {
                window.history.pushState({}, '', url);
            }

            // Update current page
            this.currentPage = url;

            // Run page-specific initialization
            this.initializePage(url);

        } catch (error) {
            console.error('Navigation error:', error);
            // Fallback to regular navigation
            window.location.href = url;
        }
    }

    updateActiveNavLink(url) {
        // Remove all active states
        document.querySelectorAll('.sidebar-nav__link').forEach(link => {
            link.classList.remove('active');
        });

        // Add active state to current link
        const activeLink = document.querySelector(`.sidebar-nav__link[href="${url}"]`);
        if (activeLink) {
            activeLink.classList.add('active');
        }
    }

    registerPageScripts() {
        // Register initialization functions for each page
        this.pageScripts.set('/', () => this.initHomePage());
        this.pageScripts.set('/index.html', () => this.initHomePage());
        this.pageScripts.set('/workspaces.html', () => this.initWorkspacesPage());
        this.pageScripts.set('/agents.html', () => this.initAgentsPage());
        this.pageScripts.set('/applications.html', () => this.initApplicationsPage());
    }

    initializePage(url) {
        const initFn = this.pageScripts.get(url);
        if (initFn) {
            initFn();
        }
    }

    initWorkspacesPage() {
        console.log('Initializing workspaces page...');
        
        // Check if WorkspaceManager class exists
        if (typeof WorkspaceManager === 'undefined') {
            console.error('WorkspaceManager class not found! Make sure workspace-manager.js is loaded.');
            return;
        }
        
        // If there's an existing instance, destroy it first to avoid duplicates
        if (window.workspaceManager && typeof window.workspaceManager.destroy === 'function') {
            console.log('Destroying existing WorkspaceManager instance');
            window.workspaceManager.destroy();
        }
        
        // Create a new instance
        console.log('Creating new WorkspaceManager instance');
        window.workspaceManager = new WorkspaceManager();
    }

    initAgentsPage() {
        // Any agents page specific initialization
        console.log('Agents page initialized');
    }

    initApplicationsPage() {
        // Any applications page specific initialization
        console.log('Applications page initialized');
    }

    initHomePage() {
        console.log('Initializing home page...');
        // Any home page specific initialization
    }
}

// Theme Manager (shared across all pages)
class ThemeManager {
    constructor() {
        this.init();
    }

    init() {
        const themeToggle = document.getElementById('theme-toggle');
        const themeIcon = document.getElementById('theme-icon');
        const currentTheme = localStorage.getItem('theme') || 'light';

        // Apply saved theme
        if (currentTheme === 'dark') {
            document.body.classList.add('dark-theme');
            if (themeIcon) themeIcon.className = 'fas fa-sun';
        }

        // Theme toggle handler
        if (themeToggle) {
            themeToggle.addEventListener('click', () => {
                document.body.classList.toggle('dark-theme');
                const isDark = document.body.classList.contains('dark-theme');
                if (themeIcon) {
                    themeIcon.className = isDark ? 'fas fa-sun' : 'fas fa-moon';
                }
                localStorage.setItem('theme', isDark ? 'dark' : 'light');
            });
        }
    }
}

// Global function for login button click
window.handleLoginClick = function() {
    if (window.authManager) {
        window.authManager.showLoginModal();
    } else {
        // If authManager isn't ready yet, wait a bit and try again
        const checkInterval = setInterval(() => {
            if (window.authManager) {
                clearInterval(checkInterval);
                window.authManager.showLoginModal();
            }
        }, 100);
        
        // Give up after 3 seconds
        setTimeout(() => clearInterval(checkInterval), 3000);
    }
};

// Initialize on DOM ready
document.addEventListener('DOMContentLoaded', () => {
    // Initialize theme manager
    window.themeManager = new ThemeManager();
    
    // Initialize SPA navigator
    window.spaNavigator = new SPANavigator();
}); 