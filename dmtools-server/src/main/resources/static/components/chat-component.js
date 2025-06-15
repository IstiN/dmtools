/**
 * DMTools Chat Component - Real API Implementation
 * Connects to the actual chat API endpoints for real AI responses
 */
class DMChatComponent {
    constructor(options = {}) {
        // Store options
        this.options = options;
        this.isMinimized = false;
        this.messages = [];
        this.isLoading = false;
        console.log('DMChatComponent initialized with options:', options);
    }

    useExample(text) {
        console.log('Using example:', text);
        // Find the textarea and set the text
        const container = document.getElementById(this.options.containerId);
        if (container) {
            const textarea = container.querySelector('textarea');
            if (textarea) {
                textarea.value = text;
                textarea.focus();
            }
        }
    }

    toggleMinimize() {
        this.isMinimized = !this.isMinimized;
        console.log('Chat minimized:', this.isMinimized);
        
        const container = document.getElementById(this.options.containerId);
        if (container) {
            const content = container.querySelector('.dm-chat__content');
            if (content) {
                content.style.display = this.isMinimized ? 'none' : 'flex';
            }
        }
    }

    close() {
        console.log('Chat closed');
        const container = document.getElementById(this.options.containerId);
        if (container) {
            container.style.display = 'none';
        }
    }

    show() {
        console.log('Chat shown');
        const container = document.getElementById(this.options.containerId);
        if (container) {
            container.style.display = 'block';
        }
    }

    async sendMessage(text) {
        if (!text || !text.trim()) return;
        
        console.log('Sending message:', text);
        
        const container = document.getElementById(this.options.containerId);
        if (!container) return;
        
        const messagesContainer = container.querySelector('.dm-chat__messages');
        if (!messagesContainer) return;
        
        // Hide empty state if it exists
        const emptyState = messagesContainer.querySelector('.dm-chat__empty-state');
        if (emptyState) {
            emptyState.style.display = 'none';
        }
        
        // Add user message
        this.addMessage(text, 'user');
        
        // Clear input and disable send button
        const textarea = container.querySelector('textarea');
        const sendButton = container.querySelector('.dm-chat__send-btn');
        if (textarea) {
            textarea.value = '';
            textarea.style.height = 'auto';
            textarea.disabled = true;
        }
        if (sendButton) {
            sendButton.disabled = true;
        }
        
        // Show loading indicator
        this.showLoadingIndicator();
        
        try {
            // Prepare chat request
            const chatRequest = {
                messages: [
                    ...this.messages.filter(msg => msg.sender !== 'loading').map(msg => ({
                        role: msg.sender === 'user' ? 'user' : 'assistant',
                        content: msg.text
                    })),
                    {
                        role: 'user',
                        content: text
                    }
                ],
                model: this.options.model || null,
                agentTools: this.options.agentTools || { enabled: true }
            };
            
            // Send request to chat API
            const response = await fetch('/api/v1/chat/completions', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify(chatRequest)
            });
            
            // Remove loading indicator
            this.removeLoadingIndicator();
            
            if (response.ok) {
                const chatResponse = await response.json();
                if (chatResponse.success && chatResponse.content) {
                    this.addMessage(chatResponse.content, 'assistant', chatResponse.source);
                } else {
                    this.addMessage('Sorry, I encountered an error: ' + (chatResponse.error || 'Unknown error'), 'error');
                }
            } else {
                const errorText = await response.text();
                this.addMessage('Sorry, I encountered a network error. Please try again.', 'error');
                console.error('Chat API error:', response.status, errorText);
            }
        } catch (error) {
            // Remove loading indicator
            this.removeLoadingIndicator();
            this.addMessage('Sorry, I encountered an error. Please check your connection and try again.', 'error');
            console.error('Chat error:', error);
        } finally {
            // Re-enable input and send button
            if (textarea) {
                textarea.disabled = false;
                textarea.focus();
            }
            if (sendButton) {
                sendButton.disabled = false;
            }
        }
    }

    showLoadingIndicator() {
        const loadingMessage = 'AI is thinking...';
        this.addMessage(loadingMessage, 'loading');
    }

    removeLoadingIndicator() {
        const container = document.getElementById(this.options.containerId);
        if (!container) return;
        
        const loadingMessages = container.querySelectorAll('.dm-chat__message--loading');
        loadingMessages.forEach(msg => msg.remove());
        
        // Remove from messages array
        this.messages = this.messages.filter(msg => msg.sender !== 'loading');
    }

    addMessage(text, sender, source = null) {
        const container = document.getElementById(this.options.containerId);
        if (!container) return;
        
        const messagesContainer = container.querySelector('.dm-chat__messages');
        if (!messagesContainer) return;
        
        const messageDiv = document.createElement('div');
        messageDiv.className = `dm-chat__message dm-chat__message--${sender}`;
        
        let messageStyle = `
            margin-bottom: 12px;
            padding: 8px 12px;
            border-radius: 12px;
            max-width: 80%;
            word-wrap: break-word;
            white-space: pre-wrap;
        `;
        
        switch (sender) {
            case 'user':
                messageStyle += 'background: #4a6cf7; color: white; margin-left: auto; text-align: right;';
                break;
            case 'assistant':
                messageStyle += 'background: #f1f3f4; color: #333; margin-right: auto;';
                break;
            case 'error':
                messageStyle += 'background: #ffebee; color: #c62828; margin-right: auto; border-left: 3px solid #c62828;';
                break;
            case 'loading':
                messageStyle += 'background: #e3f2fd; color: #1976d2; margin-right: auto; font-style: italic;';
                break;
        }
        
        messageDiv.style.cssText = messageStyle;
        
        // Add source indicator for assistant messages
        if (sender === 'assistant' && source) {
            const sourceIndicator = document.createElement('div');
            sourceIndicator.style.cssText = 'font-size: 0.8em; opacity: 0.7; margin-bottom: 4px;';
            sourceIndicator.textContent = `${source.type}${source.name ? ` (${source.name})` : ''}`;
            messageDiv.appendChild(sourceIndicator);
        }
        
        // Add loading animation for loading messages
        if (sender === 'loading') {
            const dots = document.createElement('span');
            dots.className = 'loading-dots';
            dots.style.cssText = 'animation: loading-dots 1.5s infinite;';
            dots.textContent = text;
            messageDiv.appendChild(dots);
            
            // Add CSS animation if not already present
            if (!document.querySelector('#loading-animation-style')) {
                const style = document.createElement('style');
                style.id = 'loading-animation-style';
                style.textContent = `
                    @keyframes loading-dots {
                        0%, 20% { opacity: 0.3; }
                        50% { opacity: 1; }
                        80%, 100% { opacity: 0.3; }
                    }
                `;
                document.head.appendChild(style);
            }
        } else {
            messageDiv.textContent = text;
        }
        
        messagesContainer.appendChild(messageDiv);
        
        // Scroll to bottom
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
        
        // Store in messages array (except loading messages which are temporary)
        if (sender !== 'loading') {
            this.messages.push({ text, sender, timestamp: new Date(), source });
        }
    }

    setupEventListeners(container) {
        const sendBtn = container.querySelector('.dm-chat__send-btn');
        const textarea = container.querySelector('textarea');
        
        if (sendBtn) {
            sendBtn.addEventListener('click', async (e) => {
                e.preventDefault();
                const text = textarea ? textarea.value.trim() : '';
                if (text && !this.isLoading) {
                    await this.sendMessage(text);
                }
            });
        }
        
        if (textarea) {
            // Auto-resize textarea
            textarea.addEventListener('input', function() {
                this.style.height = 'auto';
                this.style.height = Math.min(this.scrollHeight, 100) + 'px';
            });
            
            // Send on Enter (but not Shift+Enter)
            textarea.addEventListener('keydown', async (e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    const text = textarea.value.trim();
                    if (text && !this.isLoading) {
                        await this.sendMessage(text);
                    }
                }
            });
        }
        
        // Setup minimize and close buttons for floating chat
        const minimizeBtn = container.querySelector('.dm-chat__minimize-btn');
        const closeBtn = container.querySelector('.dm-chat__close-btn');
        
        if (minimizeBtn) {
            minimizeBtn.addEventListener('click', () => this.toggleMinimize());
        }
        
        if (closeBtn) {
            closeBtn.addEventListener('click', () => this.close());
        }
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
                    <div class="dm-chat__messages" style="flex: 1; overflow-y: auto; padding: 16px; min-height: 300px;">
                        <div class="dm-chat__empty-state" style="text-align: center; padding: 20px;">
                            <div class="dm-chat__empty-icon" style="font-size: 2rem; margin-bottom: 12px; color: #666;">
                                <i class="fas fa-comments"></i>
                            </div>
                            <h3 style="margin-bottom: 8px; color: #333;">${options.title || 'AI Assistant'}</h3>
                            <p style="color: #666; margin: 0;">${options.welcomeMessage || 'Hi! I can help you with agents, applications, and general questions about DMTools.'}</p>
                        </div>
                    </div>
                    <div class="dm-chat__input" style="padding: 12px; border-top: 1px solid #eee;">
                        <div class="dm-chat__input-container" style="display: flex; align-items: flex-end; gap: 8px;">
                            <textarea 
                                placeholder="${options.placeholder || 'Type your message...'}" 
                                rows="1"
                                style="flex: 1; padding: 8px 12px; border: 1px solid #ddd; border-radius: 18px; resize: none; font-family: inherit; font-size: 14px; line-height: 1.4; min-height: 36px;"
                            ></textarea>
                            <div class="dm-chat__actions">
                                <button class="dm-chat__send-btn" style="background: #4a6cf7; color: white; border: none; width: 36px; height: 36px; border-radius: 50%; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: background-color 0.2s;">
                                    <i class="fas fa-paper-plane"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
            
            // Setup event listeners
            instance.setupEventListeners(container);
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
            container.style.border = '1px solid #e0e0e0';
            document.body.appendChild(container);
        }
        
        options.containerId = container.id;
        const instance = new DMChatComponent(options);
        container.dmChatComponent = instance;
        
        // Add minimal UI
        container.innerHTML = `
            <div class="dm-chat__header" style="padding: 12px 16px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eee; background: #f8f9fa;">
                <div class="dm-chat__title" style="display: flex; align-items: center; gap: 8px; font-weight: 600; color: #333;">
                    <i class="${options.icon || 'fas fa-comments'}" style="color: #4a6cf7;"></i>
                    <span>${options.title || 'AI Assistant'}</span>
                </div>
                <div class="dm-chat__controls" style="display: flex; gap: 4px;">
                    <button class="dm-chat__minimize-btn" style="background: none; border: none; cursor: pointer; padding: 4px; border-radius: 4px; color: #666;" title="Minimize">
                        <i class="fas fa-minus"></i>
                    </button>
                    <button class="dm-chat__close-btn" style="background: none; border: none; cursor: pointer; padding: 4px; border-radius: 4px; color: #666;" title="Close">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
            </div>
            <div class="dm-chat__content" style="flex: 1; display: flex; flex-direction: column; overflow: hidden;">
                <div class="dm-chat__messages" style="flex: 1; overflow-y: auto; padding: 16px;">
                    <div class="dm-chat__empty-state" style="text-align: center; padding: 20px;">
                        <div class="dm-chat__empty-icon" style="font-size: 2rem; margin-bottom: 12px; color: #666;">
                            <i class="${options.icon || 'fas fa-comments'}"></i>
                        </div>
                        <h3 style="margin-bottom: 8px; color: #333; font-size: 1.1rem;">${options.title || 'AI Assistant'}</h3>
                        <p style="color: #666; margin: 0; font-size: 0.9rem; line-height: 1.4;">${options.welcomeMessage || 'Hi! I can help you with agents, applications, and general questions about DMTools.'}</p>
                    </div>
                </div>
                <div class="dm-chat__input" style="padding: 12px; border-top: 1px solid #eee; background: #f8f9fa;">
                    <div class="dm-chat__input-container" style="display: flex; align-items: flex-end; gap: 8px;">
                        <textarea 
                            placeholder="${options.placeholder || 'Type your message...'}" 
                            rows="1"
                            style="flex: 1; padding: 8px 12px; border: 1px solid #ddd; border-radius: 18px; resize: none; font-family: inherit; font-size: 14px; line-height: 1.4; min-height: 36px; background: white;"
                        ></textarea>
                        <div class="dm-chat__actions">
                            <button class="dm-chat__send-btn" style="background: #4a6cf7; color: white; border: none; width: 36px; height: 36px; border-radius: 50%; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: background-color 0.2s;">
                                <i class="fas fa-paper-plane"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        // Setup event listeners
        instance.setupEventListeners(container);
        
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