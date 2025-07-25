/**
 * Integration Mapping Logic - Maps user integrations to available job types
 * Flow: User Integrations → Integration Types → Job Requirements → Available Jobs
 */

class IntegrationMapper {
    constructor(baseUrl = 'http://localhost:8080') {
        this.baseUrl = baseUrl;
        this.authHeaders = {
            'Authorization': `Bearer ${this.getAuthToken()}`,
            'Content-Type': 'application/json'
        };
    }

    getAuthToken() {
        // Get token from localStorage, cookies, or wherever it's stored
        return localStorage.getItem('authToken') || '';
    }

    /**
     * Step 1: Get user's configured integrations
     */
    async getUserIntegrations() {
        const response = await fetch(`${this.baseUrl}/api/integrations`, {
            headers: this.authHeaders
        });
        
        if (!response.ok) {
            throw new Error(`Failed to get user integrations: ${response.status}`);
        }
        
        return await response.json();
    }

    /**
     * Step 2: Get all available integration types with categories
     */
    async getIntegrationTypes() {
        const response = await fetch(`${this.baseUrl}/api/integrations/types`, {
            headers: this.authHeaders
        });
        
        if (!response.ok) {
            throw new Error(`Failed to get integration types: ${response.status}`);
        }
        
        return await response.json();
    }

    /**
     * Step 3: Get all available job configurations
     */
    async getJobTypes() {
        const response = await fetch(`${this.baseUrl}/api/v1/jobs/types`, {
            headers: this.authHeaders
        });
        
        if (!response.ok) {
            throw new Error(`Failed to get job types: ${response.status}`);
        }
        
        return await response.json();
    }

    /**
     * Step 4: Map user integrations to categories
     */
    mapIntegrationsToCategories(userIntegrations, integrationTypes) {
        const categoryMap = new Map();
        
        // Create a lookup map: integration type → categories
        const typeToCategories = new Map();
        integrationTypes.forEach(type => {
            typeToCategories.set(type.type, type.categories || []);
        });
        
        // Map user integrations to categories
        userIntegrations.forEach(integration => {
            const categories = typeToCategories.get(integration.type) || [];
            categories.forEach(category => {
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
     * Step 5: Filter jobs based on available categories
     */
    filterAvailableJobs(jobTypes, availableCategories) {
        const availableCategorySet = new Set(availableCategories.keys());
        
        return jobTypes.map(job => {
            const requiredCategories = job.requiredIntegrations || [];
            const optionalCategories = job.optionalIntegrations || [];
            
            // Check if all required integrations are available
            const missingRequired = requiredCategories.filter(
                category => !availableCategorySet.has(category)
            );
            
            // Check available optional integrations
            const availableOptional = optionalCategories.filter(
                category => availableCategorySet.has(category)
            );
            
            return {
                ...job,
                canExecute: missingRequired.length === 0,
                missingRequired,
                availableOptional,
                requiredIntegrationIds: this.getIntegrationIds(requiredCategories, availableCategories),
                optionalIntegrationIds: this.getIntegrationIds(availableOptional, availableCategories)
            };
        });
    }

    /**
     * Helper: Get integration IDs for categories
     */
    getIntegrationIds(categories, availableCategories) {
        const integrationIds = [];
        categories.forEach(category => {
            const integrations = availableCategories.get(category) || [];
            // Pick the first enabled integration for each category
            const enabledIntegration = integrations.find(int => int.enabled);
            if (enabledIntegration) {
                integrationIds.push(enabledIntegration.id);
            }
        });
        return integrationIds;
    }

    /**
     * Main method: Complete integration mapping
     */
    async mapIntegrationsToJobs() {
        try {
            console.log('🔄 Starting integration mapping...');

            // Step 1: Get all data
            const [userIntegrations, integrationTypes, jobTypes] = await Promise.all([
                this.getUserIntegrations(),
                this.getIntegrationTypes(),
                this.getJobTypes()
            ]);

            console.log('📊 Data loaded:', {
                userIntegrations: userIntegrations.length,
                integrationTypes: integrationTypes.length,
                jobTypes: jobTypes.length
            });

            // Step 2: Map integrations to categories
            const availableCategories = this.mapIntegrationsToCategories(
                userIntegrations, 
                integrationTypes
            );

            console.log('🗂️ Available categories:', Array.from(availableCategories.keys()));

            // Step 3: Filter jobs based on available integrations
            const mappedJobs = this.filterAvailableJobs(jobTypes, availableCategories);

            // Step 4: Separate executable and non-executable jobs
            const executableJobs = mappedJobs.filter(job => job.canExecute);
            const blockedJobs = mappedJobs.filter(job => !job.canExecute);

            console.log('✅ Results:', {
                executableJobs: executableJobs.length,
                blockedJobs: blockedJobs.length
            });

            return {
                availableCategories: Object.fromEntries(availableCategories),
                executableJobs,
                blockedJobs,
                summary: {
                    totalJobs: jobTypes.length,
                    executableJobs: executableJobs.length,
                    blockedJobs: blockedJobs.length,
                    availableCategories: availableCategories.size
                }
            };

        } catch (error) {
            console.error('❌ Integration mapping failed:', error);
            throw error;
        }
    }

    /**
     * Get specific job execution parameters
     */
    async getJobExecutionParams(jobType) {
        const mapping = await this.mapIntegrationsToJobs();
        const job = mapping.executableJobs.find(j => j.type === jobType);
        
        if (!job) {
            throw new Error(`Job ${jobType} is not executable or not found`);
        }

        return {
            jobName: job.type,
            requiredIntegrations: job.requiredIntegrationIds,
            optionalIntegrations: job.optionalIntegrationIds,
            params: {} // Will be filled by user input
        };
    }

    /**
     * Example: Check if specific job can be executed
     */
    async canExecuteJob(jobType) {
        const mapping = await this.mapIntegrationsToJobs();
        const job = mapping.executableJobs.find(j => j.type === jobType);
        return !!job;
    }
}

// Usage Examples:

async function example1_BasicMapping() {
    const mapper = new IntegrationMapper();
    
    try {
        const result = await mapper.mapIntegrationsToJobs();
        
        console.log('🎯 Integration Mapping Results:');
        console.log('Available Categories:', Object.keys(result.availableCategories));
        console.log('Executable Jobs:', result.executableJobs.map(j => j.type));
        console.log('Blocked Jobs:', result.blockedJobs.map(j => ({ 
            type: j.type, 
            missing: j.missingRequired 
        })));
        
        return result;
    } catch (error) {
        console.error('❌ Mapping failed:', error.message);
    }
}

async function example2_CheckSpecificJob() {
    const mapper = new IntegrationMapper();
    
    const canExecuteExpert = await mapper.canExecuteJob('Expert');
    console.log('Can execute Expert job:', canExecuteExpert);
    
    if (canExecuteExpert) {
        const execParams = await mapper.getJobExecutionParams('Expert');
        console.log('Expert job execution params:', execParams);
    }
}

async function example3_DetailedJobAnalysis() {
    const mapper = new IntegrationMapper();
    const result = await mapper.mapIntegrationsToJobs();
    
    console.log('\n📋 Detailed Job Analysis:');
    
    result.executableJobs.forEach(job => {
        console.log(`\n✅ ${job.type} (${job.displayName})`);
        console.log(`   Required: ${job.requiredIntegrations.join(', ')}`);
        console.log(`   Optional Available: ${job.availableOptional.join(', ')}`);
        console.log(`   Integration IDs: ${job.requiredIntegrationIds.join(', ')}`);
    });
    
    result.blockedJobs.forEach(job => {
        console.log(`\n❌ ${job.type} (${job.displayName})`);
        console.log(`   Missing: ${job.missingRequired.join(', ')}`);
    });
}

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { IntegrationMapper };
} 