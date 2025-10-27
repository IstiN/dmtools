/**
 * CLI MCP Tool Examples
 * 
 * These examples demonstrate how to use the cli_execute_command MCP tool
 * from JavaScript post-action functions in Expert and Teammate jobs.
 * 
 * The cli_execute_command tool allows executing CLI commands (git, gh, dmtools, npm, yarn, 
 * docker, kubectl, terraform, ansible, aws, gcloud, az) from JavaScript automation workflows.
 * 
 * Security Features:
 * - Command whitelisting (only pre-approved commands allowed)
 * - Working directory boundary enforcement
 * - Sensitive data masking in logs
 * - Environment variable inheritance from workflow context
 * - Automatic dmtools.env file loading
 */

// Example 1: Git Status - Check repository status
// Usage: Get current git status to verify uncommitted changes
function exampleGitStatus() {
    try {
        const output = cli_execute_command({
            command: "git status"
        });
        console.log("Git status output:", output);
        return output;
    } catch (error) {
        console.error("Failed to execute git status:", error);
        throw error;
    }
}

// Example 2: Git Commit - Make automated commit
// Usage: Commit changes made by automation workflow
function exampleGitCommit(message) {
    try {
        // Sanitize message to prevent command injection issues
        // Escape special shell characters: ", `, \, $
        const sanitizedMessage = message.replace(/(["`\\$])/g, '\\$1');
        
        const output = cli_execute_command({
            command: `git commit -m "${sanitizedMessage}"`
        });
        console.log("Git commit output:", output);
        return output;
    } catch (error) {
        console.error("Failed to commit changes:", error);
        throw error;
    }
}

// Example 3: Git Push - Push changes to remote
// Usage: Push committed changes to GitHub
function exampleGitPush(branch) {
    try {
        const branchName = branch || "main";
        const output = cli_execute_command({
            command: `git push origin ${branchName}`
        });
        console.log("Git push output:", output);
        return output;
    } catch (error) {
        console.error("Failed to push changes:", error);
        throw error;
    }
}

// Example 4: GitHub PR Creation - Create pull request via gh CLI
// Usage: Create pull request from automation workflow
function exampleCreatePullRequest(title, body, base) {
    try {
        const baseBranch = base || "main";
        const output = cli_execute_command({
            command: `gh pr create --title "${title}" --body "${body}" --base ${baseBranch}`
        });
        console.log("GitHub PR created:", output);
        return output;
    } catch (error) {
        console.error("Failed to create pull request:", error);
        throw error;
    }
}

// Example 5: DMTools CLI - Get Jira ticket via dmtools CLI
// Usage: Retrieve Jira ticket information using dmtools command line
function exampleDMToolsGetTicket(ticketKey) {
    try {
        const output = cli_execute_command({
            command: `dmtools jira_get_ticket ${ticketKey}`
        });
        console.log("DMTools output:", output);
        return output;
    } catch (error) {
        console.error("Failed to execute dmtools command:", error);
        throw error;
    }
}

// Example 6: Custom Working Directory - Execute command in specific directory
// Usage: Run commands in different directory (e.g., subdirectory or specific repository path)
function exampleCustomWorkingDirectory(directory, command) {
    try {
        const output = cli_execute_command({
            command: command,
            workingDirectory: directory
        });
        console.log(`Command output from ${directory}:`, output);
        return output;
    } catch (error) {
        console.error("Failed to execute command in custom directory:", error);
        throw error;
    }
}

// Example 7: NPM Commands - Run npm scripts
// Usage: Execute npm commands like install, build, test
function exampleNpmInstall() {
    try {
        const output = cli_execute_command({
            command: "npm install"
        });
        console.log("NPM install output:", output);
        return output;
    } catch (error) {
        console.error("Failed to run npm install:", error);
        throw error;
    }
}

// Example 8: Docker Commands - Docker operations
// Usage: Execute docker commands for container management
function exampleDockerPs() {
    try {
        const output = cli_execute_command({
            command: "docker ps -a"
        });
        console.log("Docker containers:", output);
        return output;
    } catch (error) {
        console.error("Failed to list docker containers:", error);
        throw error;
    }
}

// Example 9: Git Branch Operations - Create and checkout branch
// Usage: Create feature branch for automated changes
function exampleGitBranch(branchName) {
    try {
        // Create and checkout new branch
        const output = cli_execute_command({
            command: `git checkout -b ${branchName}`
        });
        console.log("Git branch created:", output);
        return output;
    } catch (error) {
        console.error("Failed to create git branch:", error);
        throw error;
    }
}

// Example 10: Complete Workflow - Full automation workflow
// Usage: End-to-end workflow combining multiple commands
function exampleCompleteWorkflow(ticketKey, branchName, commitMessage) {
    try {
        // Step 1: Create feature branch
        console.log("Step 1: Creating feature branch...");
        exampleGitBranch(branchName);
        
        // Step 2: Get ticket information
        console.log("Step 2: Fetching ticket information...");
        const ticketInfo = exampleDMToolsGetTicket(ticketKey);
        
        // Step 3: Make some changes (example: update file with ticket info)
        // ... your automation logic here ...
        
        // Step 4: Stage changes
        console.log("Step 3: Staging changes...");
        cli_execute_command({ command: "git add ." });
        
        // Step 5: Commit changes
        console.log("Step 4: Committing changes...");
        exampleGitCommit(commitMessage);
        
        // Step 6: Push to remote
        console.log("Step 5: Pushing to remote...");
        exampleGitPush(branchName);
        
        // Step 7: Create pull request
        console.log("Step 6: Creating pull request...");
        exampleCreatePullRequest(
            `Automated update for ${ticketKey}`,
            `This PR was automatically created by DMTools automation for ticket ${ticketKey}`,
            "main"
        );
        
        console.log("Workflow completed successfully!");
        return { success: true, ticketKey, branchName };
        
    } catch (error) {
        console.error("Workflow failed:", error);
        throw error;
    }
}

// Example 11: Error Handling - Proper error handling pattern
// Usage: Handle command execution failures gracefully
function exampleErrorHandling(command) {
    try {
        const output = cli_execute_command({ command });
        console.log("Command succeeded:", output);
        return { success: true, output };
    } catch (error) {
        console.error("Command failed:", error);
        // Log error but don't fail entire workflow
        return { success: false, error: error.message };
    }
}

// Example 12: Conditional Execution - Execute commands based on conditions
// Usage: Run commands only when certain conditions are met
function exampleConditionalExecution() {
    try {
        // Check if there are uncommitted changes
        const status = cli_execute_command({ command: "git status --porcelain" });
        
        if (status.trim().length > 0) {
            console.log("Uncommitted changes detected, committing...");
            cli_execute_command({ command: "git add ." });
            cli_execute_command({ command: "git commit -m 'Automated update'" });
            console.log("Changes committed successfully");
            return true;
        } else {
            console.log("No uncommitted changes detected");
            return false;
        }
    } catch (error) {
        console.error("Failed to check and commit changes:", error);
        throw error;
    }
}

// Export examples for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        exampleGitStatus,
        exampleGitCommit,
        exampleGitPush,
        exampleCreatePullRequest,
        exampleDMToolsGetTicket,
        exampleCustomWorkingDirectory,
        exampleNpmInstall,
        exampleDockerPs,
        exampleGitBranch,
        exampleCompleteWorkflow,
        exampleErrorHandling,
        exampleConditionalExecution
    };
}


