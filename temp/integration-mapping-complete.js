/**
 * Complete Integration Mapping Logic for DMTools
 * Maps User Integrations → Categories → Job Requirements
 * 
 * Usage:
 * const mapper = new IntegrationMapper(bearerToken);
 * const result = await mapper.getAvailableJobsForUser();
 */

class IntegrationMapper {
    constructor(bearerToken) {
        this.bearerToken = bearerToken;
        this.baseUrl = 'http://localhost:8080';
    }

    /**
     * Step 1: Get user's configured integrations (with categories)
     */
    async getUserIntegrations() {
        const response = await fetch(`${this.baseUrl}/api/integrations`, {
            headers: {
                'Authorization': `Bearer ${this.bearerToken}`,
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error(`Failed to fetch user integrations: ${response.statusText}`);
        }
        
        return await response.json();
    }

    /**
     * Step 2: Get all available integration types (with categories)
     */
    async getIntegrationTypes() {
        const response = await fetch(`${this.baseUrl}/api/integrations/types`, {
            headers: {
                'Authorization': `Bearer ${this.bearerToken}`,
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error(`Failed to fetch integration types: ${response.statusText}`);
        }
        
        return await response.json();
    }

    /**
     * Step 3: Get all available job configurations (with requirements)
     */
    async getJobTypes() {
        const response = await fetch(`${this.baseUrl}/api/v1/jobs/types`, {
            headers: {
                'Authorization': `Bearer ${this.bearerToken}`,
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error(`Failed to fetch job types: ${response.statusText}`);
        }
        
        return await response.json();
    }

    /**
     * Step 4: Create category mapping from user integrations
     */
    mapUserIntegrationsToCategories(userIntegrations) {
        const categoryMap = new Map();
        
        userIntegrations.forEach(integration => {
            // Each integration now includes categories from the enhanced endpoint
            (integration.categories || []).forEach(category => {
                if (!categoryMap.has(category)) {
                    categoryMap.set(category, []);
                }
                categoryMap.get(category).push({
                    id: integration.id,
                    name: integration.name,
                    type: integration.type,
                    enabled: integration.enabled
                });
            });
        });
        
        return categoryMap;
    }

    /**
     * Step 5: Check if user can execute a specific job
     */
    canUserExecuteJob(jobType, userCategoryMap) {
        const requiredCategories = jobType.requiredIntegrations || [];
        const optionalCategories = jobType.optionalIntegrations || [];
        
        // Check if all required categories are satisfied
        const missingRequired = requiredCategories.filter(category => 
            !userCategoryMap.has(category) || 
            userCategoryMap.get(category).length === 0
        );
        
        // Get available optional categories
        const availableOptional = optionalCategories.filter(category => 
            userCategoryMap.has(category) && 
            userCategoryMap.get(category).length > 0
        );

        return {
            canExecute: missingRequired.length === 0,
            missingRequired,
            availableOptional,
            requiredCategories,
            optionalCategories
        };
    }

    /**
     * Step 6: Get all available jobs for the user
     */
    async getAvailableJobsForUser() {
        try {
            // Fetch all data in parallel
            const [userIntegrations, integrationTypes, jobTypes] = await Promise.all([
                this.getUserIntegrations(),
                this.getIntegrationTypes(),
                this.getJobTypes()
            ]);

            // Create category mapping
            const userCategoryMap = this.mapUserIntegrationsToCategories(userIntegrations);

            // Analyze each job
            const jobAnalysis = jobTypes.map(jobType => {
                const analysis = this.canUserExecuteJob(jobType, userCategoryMap);
                
                return {
                    job: jobType,
                    ...analysis,
                    // Add integration details for each required category
                    requiredIntegrationDetails: analysis.requiredCategories.map(category => ({
                        category,
                        available: userCategoryMap.has(category),
                        integrations: userCategoryMap.get(category) || []
                    })),
                    // Add integration details for each optional category
                    optionalIntegrationDetails: analysis.optionalCategories.map(category => ({
                        category,
                        available: userCategoryMap.has(category),
                        integrations: userCategoryMap.get(category) || []
                    }))
                };
            });

            return {
                userIntegrations,
                userCategoryMap: Object.fromEntries(userCategoryMap),
                availableJobs: jobAnalysis.filter(job => job.canExecute),
                unavailableJobs: jobAnalysis.filter(job => !job.canExecute),
                allJobs: jobAnalysis,
                summary: {
                    totalJobs: jobTypes.length,
                    availableJobs: jobAnalysis.filter(job => job.canExecute).length,
                    userIntegrations: userIntegrations.length,
                    userCategories: Array.from(userCategoryMap.keys())
                }
            };
        } catch (error) {
            console.error('Error mapping integrations to jobs:', error);
            throw error;
        }
    }

    /**
     * Helper: Get integrations for a specific category
     */
    getIntegrationsForCategory(category, userCategoryMap) {
        return userCategoryMap.get(category) || [];
    }

    /**
     * Helper: Get missing categories for a job
     */
    getMissingCategoriesForJob(jobType, userCategoryMap) {
        const required = jobType.requiredIntegrations || [];
        return required.filter(category => 
            !userCategoryMap.has(category) || 
            userCategoryMap.get(category).length === 0
        );
    }

    /**
     * Helper: Create integration selection for job execution
     */
    createIntegrationSelectionForJob(jobType, userCategoryMap) {
        const requiredCategories = jobType.requiredIntegrations || [];
        const optionalCategories = jobType.optionalIntegrations || [];
        
        const integrationSelection = {};
        
        // Map required categories to specific integrations
        requiredCategories.forEach(category => {
            const integrations = this.getIntegrationsForCategory(category, userCategoryMap);
            if (integrations.length > 0) {
                // Use the first available integration (or implement selection logic)
                integrationSelection[category] = integrations[0].id;
            }
        });
        
        // Optionally map optional categories
        optionalCategories.forEach(category => {
            const integrations = this.getIntegrationsForCategory(category, userCategoryMap);
            if (integrations.length > 0) {
                integrationSelection[category] = integrations[0].id;
            }
        });
        
        return integrationSelection;
    }
}

// Example Usage:
async function demonstrateIntegrationMapping() {
    const bearerToken = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJpc3RpbjIwMDdAZ21haWwuY29tIiwidXNlcklkIjoiVmxhZGltaXIgS2x5c2hldmljaCIsImlhdCI6MTc1MzAyODEzOSwiZXhwIjoxNzUzMTE0NTM5fQ.31SfBNkoPlfGB2Aq9RlXoYDQs-DAFsY59cQEAXIaRK0';
    
    const mapper = new IntegrationMapper(bearerToken);
    
    try {
        const result = await mapper.getAvailableJobsForUser();
        
        console.log('📊 Integration Mapping Results:');
        console.log(`📝 Total Jobs: ${result.summary.totalJobs}`);
        console.log(`✅ Available Jobs: ${result.summary.availableJobs}`);
        console.log(`🔗 User Integrations: ${result.summary.userIntegrations}`);
        console.log(`🏷️  User Categories: ${result.summary.userCategories.join(', ')}`);
        
        console.log('\n🎯 Available Jobs:');
        result.availableJobs.forEach(job => {
            console.log(`- ${job.job.displayName} (${job.job.type})`);
            console.log(`  Required: ${job.requiredCategories.join(', ')}`);
            console.log(`  Optional: ${job.optionalCategories.join(', ')}`);
        });
        
        console.log('\n❌ Unavailable Jobs:');
        result.unavailableJobs.forEach(job => {
            console.log(`- ${job.job.displayName} (${job.job.type})`);
            console.log(`  Missing: ${job.missingRequired.join(', ')}`);
        });
        
        return result;
    } catch (error) {
        console.error('❌ Error:', error);
    }
}

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { IntegrationMapper, demonstrateIntegrationMapping };
}

// Example API calls demonstrating the complete flow:
const API_EXAMPLES = {
    // 1. Get user integrations with categories
    getUserIntegrations: `
curl 'http://localhost:8080/api/integrations' \\
  -H 'Authorization: Bearer YOUR_TOKEN' | jq '.[] | {name, type, categories}'
`,
    
    // 2. Get integration types with categories  
    getIntegrationTypes: `
curl 'http://localhost:8080/api/integrations/types' \\
  -H 'Authorization: Bearer YOUR_TOKEN' | jq '.[] | {type, categories}'
`,
    
    // 3. Get job types with requirements
    getJobTypes: `
curl 'http://localhost:8080/api/v1/jobs/types' \\
  -H 'Authorization: Bearer YOUR_TOKEN' | jq '.[] | {type, requiredIntegrations, optionalIntegrations}'
`,
    
    // 4. Execute a job with specific integrations
    executeJob: `
curl -X POST 'http://localhost:8080/api/v1/jobs/execute' \\
  -H 'Authorization: Bearer YOUR_TOKEN' \\
  -H 'Content-Type: application/json' \\
  -d '{
    "jobName": "Expert",
    "requiredIntegrations": ["uuid-jira", "uuid-openai", "uuid-confluence"],
    "params": {"request": "Analyze this ticket"}
  }'
`
};

console.log('🚀 Integration Mapping System Ready!');
console.log('📚 API Examples:', API_EXAMPLES); 