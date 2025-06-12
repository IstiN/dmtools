/**
 * DMTools Chat Component
 * A reusable chat interface that can be embedded in different parts of the application
 */
class DMChatComponent {
    constructor(options = {}) {
        // Default configuration
        this.config = {
            containerId: options.containerId || 'dm-chat-container',
            apiBaseUrl: options.apiBaseUrl || window.location.origin,
            mode: options.mode || 'embedded', // 'embedded', 'floating', 'fullscreen'
            enableFileAttachments: options.enableFileAttachments !== false,
            enableHealthCheck: options.enableHealthCheck !== false,
            enableMcpTools: options.enableMcpTools || false, // Enable MCP tools integration
            placeholder: options.placeholder || 'Type your message...',
            welcomeMessage: options.welcomeMessage || 'Hi! How can I help you today?',
            title: options.title || 'AI Assistant',
            icon: options.icon || 'fas fa-comments',
            theme: options.theme || 'default', // 'default', 'compact', 'minimal'
            height: options.height || '400px',
            maxHeight: options.maxHeight || '600px',
            position: options.position || 'bottom-right', // for floating mode
            context: options.context || 'general', // 'general', 'presentation', 'support', 'development'
            examples: options.examples || null, // Custom examples array
            emptyStateConfig: options.emptyStateConfig || null, // Custom empty state configuration
            callbacks: {
                onMessage: options.onMessage || null,
                onResponse: options.onResponse || null,
                onError: options.onError || null,
                onHealthCheck: options.onHealthCheck || null,
                ...options.callbacks
            },
            apiHandler: options.apiHandler || null, // Custom API handler function
        };

        // State
        this.conversationHistory = [];
        this.attachedFiles = [];
        this.isInitialized = false;
        this.isMinimized = false;

        // Initialize
        this.init();
    }

    async init() {
        if (this.isInitialized) return;

        try {
            await this.createChatInterface();
            await this.setupEventListeners();
            
            if (this.config.enableHealthCheck) {
                await this.checkAPIHealth();
            }

            this.isInitialized = true;
            this.log('Chat component initialized successfully');
        } catch (error) {
            this.handleError('Failed to initialize chat component', error);
        }
    }

    async createChatInterface() {
        const container = document.getElementById(this.config.containerId);
        if (!container) {
            throw new Error(`Container with ID '${this.config.containerId}' not found`);
        }

        const chatHTML = this.generateChatHTML();
        container.innerHTML = chatHTML;

        // Apply theme-specific classes
        container.className = `dm-chat dm-chat--${this.config.mode} dm-chat--${this.config.theme}`;
        
        if (this.config.mode === 'floating') {
            container.classList.add(`dm-chat--${this.config.position}`);
            container.style.position = 'fixed';
            container.style.zIndex = '1000';
        }

        // Set height for embedded mode
        if (this.config.mode === 'embedded') {
            const messagesContainer = container.querySelector('.dm-chat__messages');
            if (messagesContainer) {
                messagesContainer.style.height = this.config.height;
                messagesContainer.style.maxHeight = this.config.maxHeight;
            }
        }
    }

    generateChatHTML() {
        const headerHTML = this.config.mode === 'floating' ? `
            <div class="dm-chat__header">
                <div class="dm-chat__title">
                    <i class="${this.config.icon}"></i>
                    <span>${this.config.title}</span>
                </div>
                <div class="dm-chat__controls">
                    <button class="dm-chat__minimize-btn" onclick="this.parentElement.parentElement.parentElement.dmChatComponent.toggleMinimize()">
                        <i class="fas fa-minus"></i>
                    </button>
                    <button class="dm-chat__close-btn" onclick="this.parentElement.parentElement.parentElement.dmChatComponent.close()">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
            </div>
        ` : '';

        return `
            ${headerHTML}
            <div class="dm-chat__content">
                <div class="dm-chat__messages" id="${this.config.containerId}-messages">
                    <div class="dm-chat__empty-state" id="${this.config.containerId}-empty-state">
                        <div class="dm-chat__empty-icon">
                            <i class="${this.config.icon}"></i>
                        </div>
                        <h3>${this.getContextualWelcomeMessage().title}</h3>
                        <p class="dm-chat__empty-message">${this.getContextualWelcomeMessage().message}</p>
                        ${this.getContextualWelcomeMessage().subtitle ? `<p class="dm-chat__empty-subtitle">${this.getContextualWelcomeMessage().subtitle}</p>` : ''}
                        ${this.generateExamplesHTML()}
                    </div>
                </div>
                ${this.config.enableFileAttachments ? '<div class="dm-chat__attachment-display" id="' + this.config.containerId + '-attachments"></div>' : ''}
                <div class="dm-chat__input">
                    <div class="dm-chat__input-container">
                        <textarea 
                            id="${this.config.containerId}-input" 
                            placeholder="${this.config.placeholder}" 
                            rows="1"
                        ></textarea>
                        <div class="dm-chat__actions">
                            ${this.config.enableFileAttachments ? `<button class="dm-chat__attach-btn" id="${this.config.containerId}-attach-btn"><i class="fas fa-paperclip"></i></button>` : ''}
                            <button class="dm-chat__send-btn" id="${this.config.containerId}-send-btn">
                                <i class="fas fa-paper-plane"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    generateExamplesHTML() {
        // Use custom examples if provided, otherwise use context-specific examples
        let examples = this.config.examples;
        
        if (!examples) {
            const contextExamples = {
                presentation: [
                    "Create a Q4 sales presentation with charts",
                    "Generate project status report for stakeholders",
                    "Make a team performance dashboard",
                    "Build a business strategy overview",
                    "Design a product launch presentation"
                ],
                support: [
                    "How do I configure JIRA integration?",
                    "Help with setting up automated reports",
                    "Troubleshoot API connection issues",
                    "Guide me through user management",
                    "Explain the workflow automation features"
                ],
                development: [
                    "Generate unit tests for my code",
                    "Review code quality and suggest improvements",
                    "Help with API documentation",
                    "Create deployment scripts",
                    "Analyze code performance issues"
                ],
                general: [
                    "💬 Ask me anything about DMTools",
                    "📊 Help with presentations and reports", 
                    "🤖 Automate your workflow tasks"
                ]
            };
            
            examples = contextExamples[this.config.context] || contextExamples.general;
        }
        
        return `
            <div class="dm-chat__examples">
                <div class="dm-chat__examples-header">
                    <span>Try asking:</span>
                </div>
                ${examples.map((example, index) => `
                    <div class="dm-chat__example-item" onclick="this.closest('.dm-chat').dmChatComponent.useExample('${this.escapeHtml(example.replace(/[💬📊🤖]/g, '').trim())}')">${example}</div>
                `).join('')}
            </div>
        `;
    }

    getContextualWelcomeMessage() {
        if (this.config.emptyStateConfig?.welcomeMessage) {
            return this.config.emptyStateConfig;
        }

        const contextMessages = {
            presentation: {
                title: "Presentation Assistant",
                message: "I'll help you create professional presentations from your data. Describe what you need - sales reports, project updates, team dashboards, or custom presentations.",
                subtitle: "I can analyze your JIRA projects, Confluence data, and generate beautiful charts and metrics automatically."
            },
            support: {
                title: "DMTools Support",
                message: "I'm here to help you get the most out of DMTools. Ask about features, troubleshooting, or configuration guidance.",
                subtitle: "Whether you're setting up integrations or need help with automation, I've got you covered."
            },
            development: {
                title: "Development Assistant",
                message: "I can help with code reviews, testing, documentation, and development workflows within DMTools.",
                subtitle: "From generating tests to analyzing performance, I'm your coding companion."
            },
            general: {
                title: this.config.title,
                message: this.config.welcomeMessage,
                subtitle: "I'm powered by AI and integrated with your DMTools workspace to provide intelligent assistance."
            }
        };

        return contextMessages[this.config.context] || contextMessages.general;
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    async setupEventListeners() {
        const container = document.getElementById(this.config.containerId);
        
        // Store reference to this component instance
        container.dmChatComponent = this;

        const input = container.querySelector(`#${this.config.containerId}-input`);
        const sendBtn = container.querySelector(`#${this.config.containerId}-send-btn`);
        const attachBtn = container.querySelector(`#${this.config.containerId}-attach-btn`);

        // Auto-resize textarea
        input.addEventListener('input', () => this.autoResizeTextarea(input));

        // Send message on Enter (without Shift)
        input.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                this.sendMessage();
            }
        });

        // Send button click
        sendBtn.addEventListener('click', () => this.sendMessage());

        // File attachment button
        if (attachBtn) {
            attachBtn.addEventListener('click', () => this.attachFiles());
            attachBtn.addEventListener('contextmenu', (e) => this.showAttachmentMenu(e));
        }
    }

    autoResizeTextarea(textarea) {
        textarea.style.height = 'auto';
        textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px';
    }

    useExample(text) {
        const input = document.querySelector(`#${this.config.containerId}-input`);
        input.value = text;
        input.focus();
        this.autoResizeTextarea(input);
    }

    async sendMessage() {
        const input = document.querySelector(`#${this.config.containerId}-input`);
        const userText = input.value.trim();
        
        if (!userText && this.attachedFiles.length === 0) return;

        // Clear input
        const messageText = userText || "📎 File attachment";
        input.value = '';
        this.autoResizeTextarea(input);

        // Add user message
        this.appendMessage(messageText, 'user', '', this.attachedFiles.map(f => f.name));

        // Call callback
        if (this.config.callbacks.onMessage) {
            this.config.callbacks.onMessage(messageText, this.attachedFiles);
        }

        // Show thinking indicator
        this.showTypingIndicator();

        // Use custom API handler if provided, otherwise use default
        if (this.config.apiHandler && typeof this.config.apiHandler === 'function') {
            try {
                const responseContent = await this.config.apiHandler(messageText, this.attachedFiles);
                this.hideTypingIndicator();
                
                // The response from the custom handler is passed to the onResponse callback
                if (this.config.callbacks.onResponse) {
                    this.config.callbacks.onResponse({ success: true, content: responseContent });
                }

                // We do not append an assistant message here, as the onResponse callback handles it
                // We also do not push to conversationHistory here, assuming the custom handler's flow manages state.

            } catch (error) {
                this.hideTypingIndicator();
                this.appendMessage(`❌ Sorry, I encountered an error: ${error.message}`, 'assistant');
                this.handleError('Custom API handler error', error);
            }
        } else {
            // Default chat API logic
            try {
                // Add to conversation history
                this.conversationHistory.push({
                    role: "user",
                    content: messageText
                });

                const response = await this.sendChatToAPI(messageText);
                
                this.hideTypingIndicator();

                if (response.success) {
                    this.conversationHistory.push({
                        role: "assistant",
                        content: response.content
                    });
                    
                    this.appendMessage(response.content, 'assistant');
                    
                    // Call callback
                    if (this.config.callbacks.onResponse) {
                        this.config.callbacks.onResponse(response);
                    }
                } else {
                    throw new Error(response.error || 'Unknown error');
                }
            } catch (error) {
                this.hideTypingIndicator();
                this.appendMessage(`❌ Sorry, I encountered an error: ${error.message}`, 'assistant');
                this.handleError('Chat API error', error);
            }
        }
    }

    appendMessage(text, sender, type = '', attachments = null) {
        const messagesContainer = document.querySelector(`#${this.config.containerId}-messages`);
        const emptyState = document.querySelector(`#${this.config.containerId}-empty-state`);
        
        // Hide empty state
        if (emptyState) {
            emptyState.style.display = 'none';
        }

        const messageDiv = document.createElement('div');
        messageDiv.className = `dm-chat__message dm-chat__message--${sender}`;

        const avatar = document.createElement('div');
        avatar.className = 'dm-chat__avatar';
        
        const messageContent = document.createElement('div');
        messageContent.className = 'dm-chat__message-content';

        if (sender === 'user') {
            avatar.textContent = 'You';
            // Apply Markdown to user messages
            if (typeof marked !== 'undefined' && marked.parse) {
                messageContent.innerHTML = marked.parse(text);
            } else {
                messageContent.textContent = text;
                console.warn('marked.js not loaded. Markdown will not be rendered for user message.');
            }
            
            if (attachments && attachments.length > 0) {
                const attachmentInfo = document.createElement('div');
                attachmentInfo.className = 'dm-chat__message-attachments';
                attachmentInfo.innerHTML = `
                    <div class="dm-chat__attachment-indicator">
                        <i class="fas fa-paperclip"></i>
                        ${attachments.length} file${attachments.length > 1 ? 's' : ''} attached
                    </div>
                `;
                messageContent.appendChild(attachmentInfo);
            }
        } else {
            avatar.innerHTML = '<i class="fas fa-robot"></i>';
            
            if (type === 'thinking') {
                messageContent.innerHTML = '<i class="fas fa-spinner fa-spin"></i> ' + text;
            } else {
                // Handle different message types for assistant
                if (type === 'image') {
                    messageContent.innerHTML = `<img src="${text}" alt="User Uploaded Image" style="max-width: 100%; border-radius: 4px; cursor: pointer;" onclick="this.closest('.dm-chat').dmChatComponent.openImageModal('${text}')">`;
                } else if (type === 'file' && attachments) {
                    messageContent.textContent = text; // Assuming 'text' is a simple file descriptor
                } else if (type === 'progress') {
                    messageContent.innerHTML = text; // For progress bars or rich HTML content
                } else {
                    // Default handling for assistant text messages - now with Markdown
                    if (typeof marked !== 'undefined' && marked.parse) {
                        messageContent.innerHTML = marked.parse(text);
                    } else {
                        // Fallback if marked.js is not loaded
                        messageContent.textContent = text;
                        console.warn('marked.js not loaded. Markdown will not be rendered for assistant message.');
                    }
                }
            }
        }

        messageDiv.appendChild(avatar);
        messageDiv.appendChild(messageContent);
        messagesContainer.appendChild(messageDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    showTypingIndicator() {
        this.appendMessage('Thinking...', 'assistant', 'thinking');
    }

    hideTypingIndicator() {
        const messagesContainer = document.querySelector(`#${this.config.containerId}-messages`);
        const thinkingMessage = messagesContainer.querySelector('.fa-spinner');
        if (thinkingMessage && thinkingMessage.closest('.dm-chat__message')) {
            thinkingMessage.closest('.dm-chat__message').remove();
        }
    }

    async sendChatToAPI(userMessage) {
        try {
            if (this.attachedFiles.length > 0) {
                return await this.sendChatWithFiles(userMessage);
            } else {
                return await this.sendChatJSON(userMessage);
            }
        } catch (error) {
            throw error;
        }
    }

    async sendChatJSON(userMessage) {
        const requestData = {
            messages: this.conversationHistory
        };

        // Add MCP tools configuration if enabled
        if (this.config.enableMcpTools) {
            requestData.agentTools = {
                enabled: true
            };
        }

        const response = await fetch(`${this.config.apiBaseUrl}/api/v1/chat/completions`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestData)
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    }

    async sendChatWithFiles(userMessage) {
        const requestData = {
            messages: this.conversationHistory
        };

        // Add MCP tools configuration if enabled
        if (this.config.enableMcpTools) {
            requestData.agentTools = {
                enabled: true
            };
        }

        const formData = new FormData();
        formData.append('chatRequest', JSON.stringify(requestData));
        
        for (let i = 0; i < this.attachedFiles.length; i++) {
            formData.append('files', this.attachedFiles[i]);
        }

        const response = await fetch(`${this.config.apiBaseUrl}/api/v1/chat/completions-with-files`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();
        this.clearAttachments();
        return result;
    }

    // File attachment methods
    attachFiles() {
        if (!this.config.enableFileAttachments) return;

        if (this.attachedFiles.length > 0) {
            this.showAttachmentOptions();
        } else {
            this.selectFiles();
        }
    }

    selectFiles() {
        const fileInput = document.createElement('input');
        fileInput.type = 'file';
        fileInput.multiple = true;
        fileInput.accept = '.pdf,.doc,.docx,.txt,.csv,.xlsx,.xls,.jpg,.jpeg,.png,.gif,.bmp,.tiff,.webp';
        
        fileInput.onchange = (event) => {
            const files = Array.from(event.target.files);
            if (files.length > 0) {
                this.attachedFiles = files;
                this.updateAttachmentDisplay();
                this.showNotification(`${files.length} file${files.length > 1 ? 's' : ''} attached`, 'success');
            }
        };
        
        fileInput.click();
    }

    clearAttachments() {
        this.attachedFiles = [];
        this.updateAttachmentDisplay();
    }

    updateAttachmentDisplay() {
        const attachBtn = document.querySelector(`#${this.config.containerId}-attach-btn`);
        const attachmentDisplay = document.querySelector(`#${this.config.containerId}-attachments`);
        
        if (!attachBtn || !attachmentDisplay) return;

        if (this.attachedFiles.length > 0) {
            attachBtn.classList.add('dm-chat__attach-btn--has-files');
            attachBtn.title = `${this.attachedFiles.length} file(s) attached`;
            
            attachmentDisplay.innerHTML = `
                <div class="dm-chat__attachment-list">
                    ${this.attachedFiles.map((file, index) => `
                        <div class="dm-chat__attachment-item">
                            <i class="${this.getFileIcon(file.name)}"></i>
                            <span class="dm-chat__attachment-name">${file.name}</span>
                            <span class="dm-chat__attachment-size">(${this.formatFileSize(file.size)})</span>
                            <button class="dm-chat__remove-attachment" onclick="this.closest('.dm-chat').dmChatComponent.removeAttachment(${index})">
                                <i class="fas fa-times"></i>
                            </button>
                        </div>
                    `).join('')}
                </div>
            `;
            attachmentDisplay.style.display = 'block';
        } else {
            attachBtn.classList.remove('dm-chat__attach-btn--has-files');
            attachBtn.title = 'Attach files';
            attachmentDisplay.style.display = 'none';
        }
    }

    removeAttachment(index) {
        this.attachedFiles.splice(index, 1);
        this.updateAttachmentDisplay();
    }

    getFileIcon(filename) {
        const extension = filename.split('.').pop().toLowerCase();
        const iconMap = {
            'pdf': 'fas fa-file-pdf',
            'doc': 'fas fa-file-word',
            'docx': 'fas fa-file-word',
            'txt': 'fas fa-file-alt',
            'csv': 'fas fa-file-csv',
            'xlsx': 'fas fa-file-excel',
            'xls': 'fas fa-file-excel',
            'jpg': 'fas fa-file-image',
            'jpeg': 'fas fa-file-image',
            'png': 'fas fa-file-image',
            'gif': 'fas fa-file-image',
            'bmp': 'fas fa-file-image',
            'tiff': 'fas fa-file-image',
            'webp': 'fas fa-file-image'
        };
        return iconMap[extension] || 'fas fa-file';
    }

    formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    showAttachmentOptions() {
        // Simple implementation - you can enhance this
        const action = confirm('You have files attached. Click OK to add more files, Cancel to clear all.');
        if (action) {
            this.selectFiles();
        } else {
            this.clearAttachments();
        }
    }

    showAttachmentMenu(event) {
        event.preventDefault();
        if (this.attachedFiles.length > 0) {
            this.clearAttachments();
        }
    }

    // Utility methods
    async checkAPIHealth() {
        try {
            const response = await fetch(`${this.config.apiBaseUrl}/api/v1/chat/health`);
            const isHealthy = response.ok;
            
            if (this.config.callbacks.onHealthCheck) {
                this.config.callbacks.onHealthCheck(isHealthy);
            }

            if (isHealthy) {
                this.log('Chat service is healthy');
            } else {
                this.log('Chat service health check failed', 'warn');
            }
        } catch (error) {
            this.log('Could not connect to chat service', 'error');
            if (this.config.callbacks.onHealthCheck) {
                this.config.callbacks.onHealthCheck(false);
            }
        }
    }

    showNotification(message, type = 'info') {
        // Simple notification - can be enhanced
        console.log(`[${type.toUpperCase()}] ${message}`);
        
        // You can implement a more sophisticated notification system here
        const notification = document.createElement('div');
        notification.className = `dm-chat__notification dm-chat__notification--${type}`;
        notification.textContent = message;
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 12px 16px;
            border-radius: 8px;
            color: white;
            background-color: ${type === 'success' ? '#10b981' : type === 'error' ? '#ef4444' : '#3b82f6'};
            z-index: 10000;
        `;
        
        document.body.appendChild(notification);
        
        setTimeout(() => {
            notification.remove();
        }, 3000);
    }

    handleError(message, error) {
        this.log(message, 'error', error);
        if (this.config.callbacks.onError) {
            this.config.callbacks.onError(message, error);
        }
    }

    log(message, level = 'info', error = null) {
        const prefix = '[DMChatComponent]';
        switch (level) {
            case 'error':
                console.error(prefix, message, error);
                break;
            case 'warn':
                console.warn(prefix, message);
                break;
            default:
                console.log(prefix, message);
        }
    }

    // Public API methods
    toggleMinimize() {
        const container = document.getElementById(this.config.containerId);
        const content = container.querySelector('.dm-chat__content');
        
        this.isMinimized = !this.isMinimized;
        
        if (this.isMinimized) {
            content.style.display = 'none';
            container.classList.add('dm-chat--minimized');
        } else {
            content.style.display = 'block';
            container.classList.remove('dm-chat--minimized');
        }
    }

    close() {
        const container = document.getElementById(this.config.containerId);
        container.style.display = 'none';
    }

    show() {
        const container = document.getElementById(this.config.containerId);
        container.style.display = 'block';
    }

    destroy() {
        const container = document.getElementById(this.config.containerId);
        if (container && container.dmChatComponent) {
            delete container.dmChatComponent;
        }
        // Additional cleanup can be added here
    }

    // Static factory methods
    static createEmbedded(containerId, options = {}) {
        return new DMChatComponent({
            containerId,
            mode: 'embedded',
            ...options
        });
    }

    static createFloating(options = {}) {
        return new DMChatComponent({
            containerId: 'dm-chat-floating',
            mode: 'floating',
            ...options
        });
    }

    static createFullscreen(containerId, options = {}) {
        return new DMChatComponent({
            containerId,
            mode: 'fullscreen',
            theme: 'minimal',
            ...options
        });
    }
}

// Make it available globally
window.DMChatComponent = DMChatComponent; 