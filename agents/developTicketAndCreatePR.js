/**
 * Develop Ticket and Create PR Action
 * Handles git operations, branch creation, commit, push, and PR creation after cursor agent development
 */

// Import common helper functions
const { extractTicketKey } = require('./common/jiraHelpers.js');

/**
 * Extract issue type prefix from ticket summary
 * Looks for first word in square brackets like [Feature], [Bug], [Enhancement]
 * 
 * @param {string} summary - Ticket summary
 * @returns {string} Lowercase issue type or 'feature' as default
 */
function extractIssueTypePrefix(summary) {
    if (!summary) {
        return 'feature';
    }
    
    // Match first word in square brackets at the beginning
    const match = summary.match(/^\[([^\]]+)\]/);
    if (match && match[1]) {
        // Extract the type, convert to lowercase, and remove any special characters
        return match[1].toLowerCase().replace(/[^a-z0-9]/g, '');
    }
    
    return 'feature';
}

/**
 * Generate unique branch name with collision detection
 * Appends _1, _2, _3 etc. if branch already exists
 * 
 * @param {string} baseType - Issue type prefix (e.g., 'feature')
 * @param {string} ticketKey - Ticket key (e.g., 'DMC-575')
 * @returns {string} Unique branch name
 */
function generateUniqueBranchName(baseType, ticketKey) {
    const baseBranchName = baseType + '/' + ticketKey;
    
    // Check if base branch exists
    try {
        const existingBranches = cli_execute_command({
            command: 'git branch --all --list "*' + baseBranchName + '*"'
        }) || '';
        
        // If no branches exist with this base name, use it
        if (!existingBranches.trim()) {
            return baseBranchName;
        }
        
        // Try with suffixes _1, _2, _3, etc.
        for (let i = 1; i <= 10; i++) {
            const candidateName = baseBranchName + '_' + i;
            if (existingBranches.indexOf(candidateName) === -1) {
                return candidateName;
            }
        }
        
        // Fallback: use timestamp suffix if too many collisions
        const timestamp = Date.now();
        return baseBranchName + '_' + timestamp;
        
    } catch (error) {
        console.warn('Error checking existing branches, using base name:', error);
        return baseBranchName;
    }
}

/**
 * Configure git author for AI Teammate commits
 * 
 * @returns {boolean} True if successful
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
 * Create git branch, stage changes, commit, and push
 * 
 * @param {string} branchName - Branch name to create
 * @param {string} commitMessage - Commit message
 * @returns {Object} Result with success status and branch name
 */
function performGitOperations(branchName, commitMessage) {
    try {
        // Create and checkout new branch
        console.log('Creating branch:', branchName);
        cli_execute_command({
            command: 'git checkout -b ' + branchName
        });
        
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
            console.warn('No changes to commit');
            return {
                success: false,
                error: 'No changes were made by the development process'
            };
        }
        
        // Commit changes
        console.log('Committing changes...');
        cli_execute_command({
            command: 'git commit -m "' + commitMessage.replace(/"/g, '\\"') + '"'
        });
        
        // Push to remote
        console.log('Pushing to remote...');
        cli_execute_command({
            command: 'git push -u origin ' + branchName
        });
        
        console.log('✅ Git operations completed successfully');
        return {
            success: true,
            branchName: branchName
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
 * Create Pull Request using GitHub CLI
 * 
 * @param {string} title - PR title
 * @param {string} body - PR body/description
 * @returns {Object} Result with success status and PR URL
 */
function createPullRequest(title, body) {
    try {
        console.log('Creating Pull Request...');
        
        // Escape special characters in title
        const escapedTitle = title.replace(/"/g, '\\"').replace(/\n/g, ' ');
        
        // Write body to temporary file to avoid shell escaping issues
        const timestamp = Date.now();
        const bodyFilePath = '/tmp/pr_body_' + timestamp + '.md';
        
        // Write body content to file
        file_write({
            filePath: bodyFilePath,
            content: body
        });
        
        console.log('Using temporary file for PR body:', bodyFilePath);
        
        // Create PR using gh CLI with body-file
        const output = cli_execute_command({
            command: 'gh pr create --title "' + escapedTitle + '" --body-file "' + bodyFilePath + '" --base main'
        }) || '';
        
        // Clean up temporary file
        try {
            cli_execute_command({
                command: 'rm -f "' + bodyFilePath + '"'
            });
        } catch (cleanupError) {
            console.warn('Failed to clean up temporary file:', cleanupError);
        }
        
        // Extract PR URL from output
        const urlMatch = output.match(/https:\/\/github\.com\/[^\s]+/);
        const prUrl = urlMatch ? urlMatch[0] : null;
        
        if (!prUrl) {
            console.warn('PR created but could not extract URL from output:', output);
        }
        
        console.log('✅ Pull Request created:', prUrl || '(URL not found in output)');
        
        return {
            success: true,
            prUrl: prUrl,
            output: output
        };
        
    } catch (error) {
        console.error('Failed to create Pull Request:', error);
        return {
            success: false,
            error: error.toString()
        };
    }
}

/**
 * Post comment to Jira ticket with PR details
 * 
 * @param {string} ticketKey - Ticket key
 * @param {string} prUrl - Pull Request URL
 * @param {string} branchName - Git branch name
 */
function postPRCommentToJira(ticketKey, prUrl, branchName) {
    try {
        let comment = 'h3. *Development Completed*\n\n';
        comment += '*Branch:* {code}' + branchName + '{code}\n';
        
        if (prUrl) {
            comment += '*Pull Request:* ' + prUrl + '\n';
        } else {
            comment += '*Pull Request:* Created (check GitHub for URL)\n';
        }
        
        comment += '\nAI Teammate has completed the implementation and created a pull request for review.';
        
        jira_post_comment({
            key: ticketKey,
            comment: comment
        });
        
        console.log('✅ Posted PR comment to', ticketKey);
        
    } catch (error) {
        console.error('Failed to post comment to Jira:', error);
    }
}

/**
 * Post error comment to Jira ticket
 * 
 * @param {string} ticketKey - Ticket key
 * @param {string} stage - Stage where error occurred
 * @param {string} errorMessage - Error message
 */
function postErrorCommentToJira(ticketKey, stage, errorMessage) {
    try {
        let comment = 'h3. *Development Workflow Error*\n\n';
        comment += '*Stage:* ' + stage + '\n';
        comment += '*Error:* {code}' + errorMessage + '{code}\n\n';
        comment += 'Please check the logs for more details and retry the workflow if needed.';
        
        jira_post_comment({
            key: ticketKey,
            comment: comment
        });
        
        console.log('Posted error comment to', ticketKey);
        
    } catch (error) {
        console.error('Failed to post error comment to Jira:', error);
    }
}

/**
 * Main action function - orchestrates the entire workflow
 * 
 * @param {Object} params - Parameters from Teammate job
 * @param {Object} params.ticket - Jira ticket object
 * @param {string} params.response - Response content from cursor agent (development summary)
 * @param {string} params.initiator - Initiator account ID
 * @returns {Object} Result object with success status
 */
function action(params) {
    try {
        const ticketKey = params.ticket.key;
        const ticketSummary = params.ticket.fields.summary;
        const ticketDescription = params.ticket.fields.description || '';
        const developmentSummary = params.response || '';
        
        console.log('Processing development workflow for ticket:', ticketKey);
        console.log('Ticket summary:', ticketSummary);
        
        // Extract issue type prefix from ticket summary
        const issueType = extractIssueTypePrefix(ticketSummary);
        console.log('Extracted issue type:', issueType);
        
        // Configure git author
        if (!configureGitAuthor()) {
            const error = 'Failed to configure git author';
            postErrorCommentToJira(ticketKey, 'Git Configuration', error);
            return {
                success: false,
                error: error
            };
        }
        
        // Generate unique branch name
        const branchName = generateUniqueBranchName(issueType, ticketKey);
        console.log('Using branch name:', branchName);
        
        // Prepare commit message
        const commitMessage = ticketKey + ' ' + ticketSummary;
        
        // Perform git operations
        const gitResult = performGitOperations(branchName, commitMessage);
        if (!gitResult.success) {
            postErrorCommentToJira(ticketKey, 'Git Operations', gitResult.error);
            return {
                success: false,
                error: 'Git operations failed: ' + gitResult.error
            };
        }
        
        // Prepare PR body
        let prBody = ticketDescription;
        if (developmentSummary) {
            prBody += '\n\n## Development Summary\n\n' + developmentSummary;
        }
        
        // Create Pull Request
        const prTitle = ticketKey + ' ' + ticketSummary;
        const prResult = createPullRequest(prTitle, prBody);
        
        if (!prResult.success) {
            postErrorCommentToJira(ticketKey, 'Pull Request Creation', prResult.error);
            return {
                success: false,
                error: 'PR creation failed: ' + prResult.error
            };
        }
        
        // Move ticket to In Review status
        try {
            jira_move_to_status({
                key: ticketKey,
                statusName: 'In Review'
            });
            console.log('✅ Moved ticket to In Review status');
        } catch (error) {
            console.warn('Failed to move ticket to In Review:', error);
        }
        
        // Post comment with PR details
        postPRCommentToJira(ticketKey, prResult.prUrl, branchName);
        
        // Add label to indicate AI development
        try {
            jira_add_label({
                key: ticketKey,
                label: 'ai_developed'
            });
        } catch (error) {
            console.warn('Failed to add ai_developed label:', error);
        }
        
        console.log('✅ Development workflow completed successfully');
        
        return {
            success: true,
            message: 'Ticket ' + ticketKey + ' developed, committed, and PR created',
            branchName: branchName,
            prUrl: prResult.prUrl
        };
        
    } catch (error) {
        console.error('❌ Error in development workflow:', error);
        
        // Try to post error comment to ticket
        try {
            if (params && params.ticket && params.ticket.key) {
                postErrorCommentToJira(params.ticket.key, 'Workflow Execution', error.toString());
            }
        } catch (commentError) {
            console.error('Failed to post error comment:', commentError);
        }
        
        return {
            success: false,
            error: error.toString()
        };
    }
}

