/**
 * Teams KB Inbox Processor
 * 
 * Automated processor for Teams messages and manual KB inbox files.
 * Fetches Teams messages since last sync, saves to inbox, processes all unprocessed files,
 * and commits changes to git.
 * 
 * Usage:
 * - Via JSRunner job with chatName parameter for Teams processing
 * - Manual files: drop into inbox/raw/[source_name]/ and run without chatName
 * 
 * @param {Object} params - Job parameters
 * @param {Object} params.jobParams - Job configuration
 * @param {string} params.jobParams.chatName - Optional: Teams chat name to fetch messages from
 * @param {string} params.jobParams.kbOutputPath - Optional: KB output path (defaults to DMTOOLS_KB_OUTPUT_PATH env)
 */

/**
 * Sanitize chat name to create valid source name
 * Replaces non-alphanumeric characters with underscore and converts to lowercase
 * 
 * @param {string} chatName - Original chat name
 * @returns {string} Sanitized source name
 */
function sanitizeChatName(chatName) {
    if (!chatName) {
        return '';
    }
    // Replace all non-alphanumeric characters with underscore, convert to lowercase
    return chatName.toLowerCase().replace(/[^a-z0-9]+/g, '_');
}

/**
 * Resolve KB output path from parameters or environment variable
 * Priority: jobParams.kbOutputPath > DMTOOLS_KB_OUTPUT_PATH env > current directory
 * 
 * @param {Object} jobParams - Job parameters
 * @returns {string} Resolved KB output path
 */
function getKBOutputPath(jobParams) {
    // Check job params first
    if (jobParams && jobParams.kbOutputPath) {
        console.log('Using KB path from job params:', jobParams.kbOutputPath);
        return jobParams.kbOutputPath;
    }
    
    // Try to get from environment (note: depends on how env vars are exposed)
    // In dmtools, KB tools use DMTOOLS_KB_OUTPUT_PATH
    // For now, return empty string to let KB tools resolve it
    console.log('Using default KB path (will be resolved by KB tools)');
    return '';
}

/**
 * Configure git author for automated commits
 * Sets user name and email for AI Teammate
 * 
 * @returns {boolean} True if successful, false otherwise
 */
function configureGitAuthor() {
    try {
        cli_execute_command({
            command: 'git config user.name "AI Teammate"'
        });
        
        cli_execute_command({
            command: 'git config user.email "agent.ai.native@gmail.com"'
        });
        
        console.log('✅ Configured git author as AI Teammate');
        return true;
        
    } catch (error) {
        console.error('Failed to configure git author:', error);
        return false;
    }
}

/**
 * Fetch Teams messages since last sync and save to inbox
 * Gets last sync date from KB, fetches new messages, saves to inbox/raw/[source]/
 * For first-time processing (no last sync), uses teams_messages with limit:0, sorting:'asc' and batches of 100
 * For incremental sync, uses teams_messages_since with sorting:'desc' (newest first)
 * 
 * @param {string} chatName - Teams chat name
 * @param {string} kbPath - KB output path
 * @returns {Object} Result with success status and details
 */
function fetchAndSaveTeamsMessages(chatName, kbPath) {
    try {
        console.log('Fetching Teams messages for chat:', chatName);
        
        // Sanitize chat name to create source name
        const sourceName = sanitizeChatName(chatName);
        console.log('Source name:', sourceName);
        
        // Get last sync date for this source
        const lastSyncResult = kb_get({
            source_name: sourceName,
            output_path: kbPath
        });
        
        console.log('Last sync result:', lastSyncResult);
        
        let sinceDate = null;
        let isFirstSync = false;
        
        if (lastSyncResult && !lastSyncResult.startsWith('Source not found') && !lastSyncResult.startsWith('Error')) {
            sinceDate = lastSyncResult;
            console.log('Fetching messages since:', sinceDate);
        } else {
            console.log('No previous sync found, fetching messages with ASC sorting for batching');
            isFirstSync = true;
        }
        
        // Fetch Teams messages
        let messagesJson;
        if (sinceDate) {
            // Incremental sync: DESC order (newest first), single file
            messagesJson = teams_messages_since({
                chatName: chatName,
                sinceDate: sinceDate,
                sorting: 'desc'
            });
        } else {
            // First sync: ASC order (oldest first) with limit 0 (all messages)
            messagesJson = teams_messages({
                chatName: chatName,
                limit: 0,
                sorting: 'asc'
            });
        }
        
        if (!messagesJson) {
            console.log('No messages fetched from Teams');
            return {
                success: true,
                messageCount: 0,
                message: 'No new messages'
            };
        }
        
        // Parse messages (already comes as JSON string)
        let messages;
        let messageCount = 0;
        try {
            messages = JSON.parse(messagesJson);
            messageCount = Array.isArray(messages) ? messages.length : 0;
        } catch (e) {
            console.warn('Could not parse messages JSON to count:', e);
            return {
                success: false,
                error: 'Failed to parse messages: ' + e
            };
        }
        
        console.log('Fetched', messageCount, 'messages from Teams');
        
        if (messageCount === 0) {
            console.log('No new messages to process');
            return {
                success: true,
                messageCount: 0,
                message: 'No new messages'
            };
        }
        
        let filesWritten = 0;
        let totalSaved = 0;
        
        // For first sync with ASC order: split into batches of 100
        if (isFirstSync && messageCount > 100) {
            console.log('First sync detected - splitting', messageCount, 'messages into batches of 100');
            
            const batchSize = 100;
            const batchCount = Math.ceil(messageCount / batchSize);
            
            for (let i = 0; i < batchCount; i++) {
                const start = i * batchSize;
                const end = Math.min(start + batchSize, messageCount);
                const batch = messages.slice(start, end);
                
                // Generate timestamp and batch number for filename
                const timestamp = Date.now() + i; // Increment timestamp slightly to avoid collisions
                const fileName = timestamp + '-batch-' + (i + 1) + '-of-' + batchCount + '-messages.json';
                
                // Build inbox path
                const inboxPath = kbPath ? kbPath + '/inbox/raw/' + sourceName + '/' + fileName
                                         : 'inbox/raw/' + sourceName + '/' + fileName;
                
                console.log('Saving batch', (i + 1), 'of', batchCount, '(' + batch.length, 'messages) to:', inboxPath);
                
                // Save batch to inbox using file_write
                const batchJson = JSON.stringify(batch, null, 2);
                const writeResult = file_write({
                    path: inboxPath,
                    content: batchJson
                });
                
                if (!writeResult) {
                    throw new Error('Failed to write batch ' + (i + 1) + ' file');
                }
                
                filesWritten++;
                totalSaved += batch.length;
            }
            
            console.log('✅ Saved', totalSaved, 'messages in', filesWritten, 'batches to inbox');
            
        } else {
            // Incremental sync or small first sync: single file
            const timestamp = Date.now();
            const fileName = timestamp + '-messages.json';
            
            // Build inbox path
            const inboxPath = kbPath ? kbPath + '/inbox/raw/' + sourceName + '/' + fileName
                                     : 'inbox/raw/' + sourceName + '/' + fileName;
            
            console.log('Saving', messageCount, 'messages to:', inboxPath);
            
            // Save messages to inbox using file_write
            const writeResult = file_write({
                path: inboxPath,
                content: messagesJson
            });
            
            if (!writeResult) {
                throw new Error('Failed to write messages file');
            }
            
            filesWritten = 1;
            totalSaved = messageCount;
            console.log('✅ Saved', messageCount, 'messages to inbox');
        }
        
        return {
            success: true,
            messageCount: totalSaved,
            filesWritten: filesWritten,
            message: 'Saved ' + totalSaved + ' messages in ' + filesWritten + ' file(s)'
        };
        
    } catch (error) {
        console.error('Failed to fetch and save Teams messages:', error);
        return {
            success: false,
            error: error.toString()
        };
    }
}

/**
 * Commit and push changes to git
 * Stages all changes, commits with descriptive message, and pushes to origin
 * 
 * @param {string} commitMessage - Commit message
 * @returns {Object} Result with success status
 */
function commitAndPush(commitMessage) {
    try {
        // Stage all changes
        console.log('Staging changes...');
        cli_execute_command({
            command: 'git add .'
        });
        
        // Check if there are changes to commit
        const statusOutput = cli_execute_command({
            command: 'git status --porcelain'
        });
        
        if (!statusOutput || !statusOutput.trim()) {
            console.log('No changes to commit');
            return {
                success: true,
                message: 'No changes to commit'
            };
        }
        
        // Commit changes
        console.log('Committing changes...');
        cli_execute_command({
            command: 'git commit -m "' + commitMessage.replace(/"/g, '\\"') + '"'
        });
        
        // Get current branch
        const currentBranch = cli_execute_command({
            command: 'git branch --show-current'
        }).trim();
        
        // Push to remote
        console.log('Pushing to origin/' + currentBranch + '...');
        cli_execute_command({
            command: 'git push origin ' + currentBranch
        });
        
        console.log('✅ Git operations completed successfully');
        return {
            success: true,
            message: 'Committed and pushed changes'
        };
        
    } catch (error) {
        console.error('Git operations failed:', error);
        return {
            success: false,
            error: error.toString()
        };
    }
}

/**
 * Main action function
 * Orchestrates the entire workflow:
 * 1. Get KB path
 * 2. Configure git author
 * 3. Fetch Teams messages (if chatName provided)
 * 4. Process inbox (all unprocessed files)
 * 5. Commit and push changes
 * 
 * @param {Object} params - Job parameters
 * @returns {Object} Result with success status and details
 */
function action(params) {
    try {
        console.log('=== Teams KB Inbox Processor ===');
        
        // Get job parameters
        const jobParams = params.jobParams || {};
        const chatName = jobParams.chatName;
        
        console.log('Job parameters:', JSON.stringify(jobParams));
        
        // Get KB output path
        const kbPath = getKBOutputPath(jobParams);
        console.log('KB output path:', kbPath || '(default)');
        
        // Configure git author
        if (!configureGitAuthor()) {
            return {
                success: false,
                error: 'Failed to configure git author'
            };
        }
        
        let teamsMessageCount = 0;
        let teamsSourceName = '';
        
        // Fetch Teams messages if chatName is provided
        if (chatName) {
            console.log('Fetching Teams messages for chat:', chatName);
            const teamsResult = fetchAndSaveTeamsMessages(chatName, kbPath);
            
            if (!teamsResult.success) {
                console.warn('Failed to fetch Teams messages:', teamsResult.error);
                // Continue anyway to process any manually dropped files
            } else {
                teamsMessageCount = teamsResult.messageCount;
                teamsSourceName = sanitizeChatName(chatName);
                console.log('Teams messages saved:', teamsMessageCount);
            }
        } else {
            console.log('No chatName provided, skipping Teams message fetch');
        }
        
        // Process inbox - scans and processes all unprocessed files
        console.log('Processing inbox...');
        const inboxResult = kb_process_inbox({
            output_path: kbPath,
            generate_descriptions: jobParams.generateDescriptions !== false ? 'true' : 'false',
            smart_aggregation: jobParams.smartAggregation !== false ? 'true' : 'false'
        });
        
        console.log('Inbox processing result:', inboxResult);
        
        // Parse inbox result
        let processedCount = 0;
        let skippedCount = 0;
        let processedSources = [];
        
        try {
            const result = JSON.parse(inboxResult);
            if (result.success) {
                processedCount = result.processed ? result.processed.length : 0;
                skippedCount = result.skipped ? result.skipped.length : 0;
                
                // Extract unique source names
                if (result.processed) {
                    const sources = new Set();
                    result.processed.forEach(function(item) {
                        sources.add(item.source);
                    });
                    processedSources = Array.from(sources);
                }
                
                console.log('Processed', processedCount, 'files from', processedSources.length, 'sources');
                console.log('Skipped', skippedCount, 'files');
            } else {
                console.warn('Inbox processing failed:', result.message);
            }
        } catch (e) {
            console.error('Failed to parse inbox result:', e);
        }
        
        // Commit and push if there are changes
        if (processedCount > 0 || teamsMessageCount > 0) {
            const commitMessage = 'KB: Processed ' + processedCount + ' files from ' + 
                                 processedSources.length + ' sources' +
                                 (teamsMessageCount > 0 ? ' (including ' + teamsMessageCount + ' Teams messages from ' + teamsSourceName + ')' : '');
            
            console.log('Committing changes...');
            const gitResult = commitAndPush(commitMessage);
            
            if (!gitResult.success) {
                console.error('Git operations failed:', gitResult.error);
                return {
                    success: false,
                    error: 'Git operations failed: ' + gitResult.error,
                    processed: processedCount,
                    skipped: skippedCount
                };
            }
        } else {
            console.log('No files processed, skipping git commit');
        }
        
        console.log('✅ Workflow completed successfully');
        
        return {
            success: true,
            message: 'Processed ' + processedCount + ' files, skipped ' + skippedCount + ' files',
            teamsMessages: teamsMessageCount,
            processed: processedCount,
            skipped: skippedCount,
            sources: processedSources
        };
        
    } catch (error) {
        console.error('❌ Workflow failed:', error);
        return {
            success: false,
            error: error.toString()
        };
    }
}

