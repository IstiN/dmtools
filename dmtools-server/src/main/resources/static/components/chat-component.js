/**
 * DMTools Chat Component Mock Implementation
 * A simplified version that provides the minimum functionality needed for the UI to work without errors
 */
class DMChatComponent {
    constructor(options = {}) {
        // Store options
        this.options = options;
        this.isMinimized = false;
        console.log('DMChatComponent initialized with options:', options);
    }

    useExample(text) {
        console.log('Using example:', text);
    }

    toggleMinimize() {
        this.isMinimized = !this.isMinimized;
        console.log('Chat minimized:', this.isMinimized);
    }

    close() {
        console.log('Chat closed');
    }

    show() {
        console.log('Chat shown');
    }

    /**
     * Creates an embedded chat component
     * @param {string} containerId - ID of the container element
     * @param {Object} options - Configuration options
     * @returns {DMChatComponent} Chat component instance
     */
    static createEmbedded(containerId, options = {}) {
        options.containerId = containerId;
        options.mode = 'embedded';
        const instance = new DMChatComponent(options);
        
        // Add instance to container for reference
        const container = document.getElementById(containerId);
        if (container) {
            container.dmChatComponent = instance;
            
            // Add minimal UI
            container.innerHTML = `
                <div class="dm-chat__content">
                    <div class="dm-chat__messages">
                        <div class="dm-chat__empty-state">
                            <div class="dm-chat__empty-icon">
                                <i class="fas fa-comments"></i>
                            </div>
                            <h3>${options.title || 'AI Assistant'}</h3>
                            <p>${options.welcomeMessage || 'Chat functionality is currently in mock mode.'}</p>
                        </div>
                    </div>
                    <div class="dm-chat__input">
                        <div class="dm-chat__input-container">
                            <textarea 
                                placeholder="${options.placeholder || 'Type your message...'}" 
                                rows="1"
                            ></textarea>
                            <div class="dm-chat__actions">
                                <button class="dm-chat__send-btn">
                                    <i class="fas fa-paper-plane"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }
        
        return instance;
    }

    /**
     * Creates a floating chat component
     * @param {Object} options - Configuration options
     * @returns {DMChatComponent} Chat component instance
     */
    static createFloating(options = {}) {
        options.mode = 'floating';
        
        // Create container if it doesn't exist
        let container = document.getElementById('dm-chat-floating');
        if (!container) {
            container = document.createElement('div');
            container.id = 'dm-chat-floating';
            container.className = 'dm-chat dm-chat--floating dm-chat--bottom-right';
            container.style.position = 'fixed';
            container.style.zIndex = '1000';
            container.style.bottom = '20px';
            container.style.right = '20px';
            container.style.width = '350px';
            container.style.height = '500px';
            container.style.borderRadius = '12px';
            container.style.boxShadow = '0 5px 40px rgba(0, 0, 0, 0.16)';
            container.style.overflow = 'hidden';
            container.style.display = 'flex';
            container.style.flexDirection = 'column';
            container.style.background = 'white';
            document.body.appendChild(container);
        }
        
        options.containerId = container.id;
        const instance = new DMChatComponent(options);
        container.dmChatComponent = instance;
        
        // Add minimal UI
        container.innerHTML = `
            <div class="dm-chat__header" style="padding: 12px 16px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eee;">
                <div class="dm-chat__title">
                    <i class="${options.icon || 'fas fa-comments'}"></i>
                    <span>${options.title || 'AI Assistant'}</span>
                </div>
                <div class="dm-chat__controls">
                    <button class="dm-chat__minimize-btn" style="background: none; border: none; cursor: pointer; margin-right: 8px;">
                        <i class="fas fa-minus"></i>
                    </button>
                    <button class="dm-chat__close-btn" style="background: none; border: none; cursor: pointer;">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
            </div>
            <div class="dm-chat__content" style="flex: 1; display: flex; flex-direction: column; overflow: hidden;">
                <div class="dm-chat__messages" style="flex: 1; overflow-y: auto; padding: 16px;">
                    <div class="dm-chat__empty-state" style="text-align: center; padding: 20px;">
                        <div class="dm-chat__empty-icon" style="font-size: 2rem; margin-bottom: 12px;">
                            <i class="${options.icon || 'fas fa-comments'}"></i>
                        </div>
                        <h3 style="margin-bottom: 8px;">${options.title || 'AI Assistant'}</h3>
                        <p>${options.welcomeMessage || 'Chat functionality is currently in mock mode.'}</p>
                    </div>
                </div>
                <div class="dm-chat__input" style="padding: 12px; border-top: 1px solid #eee;">
                    <div class="dm-chat__input-container" style="display: flex; align-items: center;">
                        <textarea 
                            placeholder="${options.placeholder || 'Type your message...'}" 
                            rows="1"
                            style="flex: 1; padding: 8px 12px; border: 1px solid #ddd; border-radius: 18px; resize: none;"
                        ></textarea>
                        <div class="dm-chat__actions" style="margin-left: 8px;">
                            <button class="dm-chat__send-btn" style="background: #4a6cf7; color: white; border: none; width: 32px; height: 32px; border-radius: 50%; cursor: pointer;">
                                <i class="fas fa-paper-plane"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        // Add event listeners
        const minimizeBtn = container.querySelector('.dm-chat__minimize-btn');
        const closeBtn = container.querySelector('.dm-chat__close-btn');
        
        if (minimizeBtn) {
            minimizeBtn.addEventListener('click', () => instance.toggleMinimize());
        }
        
        if (closeBtn) {
            closeBtn.addEventListener('click', () => instance.close());
        }
        
        return instance;
    }

    /**
     * Creates a fullscreen chat component
     * @param {string} containerId - ID of the container element
     * @param {Object} options - Configuration options
     * @returns {DMChatComponent} Chat component instance
     */
    static createFullscreen(containerId, options = {}) {
        options.containerId = containerId;
        options.mode = 'fullscreen';
        return DMChatComponent.createEmbedded(containerId, options);
    }
}

// Make it available globally
window.DMChatComponent = DMChatComponent; 