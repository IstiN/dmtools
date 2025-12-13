/**
 * Preprocess Xray Test Cases - Handle Preconditions with Temporary IDs
 * 
 * This function processes test cases to handle preconditions that use temporary IDs
 * (e.g., @precondition-1). It creates actual Precondition issues and replaces
 * temporary IDs with real ticket keys.
 * 
 * @param {Object} params - Parameters object containing:
 *   - newTestCases: Array of test case objects with customFields.preconditions
 *   - ticket: Main ticket context
 *   - jobParams: Job parameters
 * @returns {Array} Modified array of test cases with temporary IDs replaced
 */
function action(params) {
    try {
        console.log("Starting Xray test cases preprocessing for preconditions");
        
        if (!params.newTestCases || !Array.isArray(params.newTestCases)) {
            console.warn("No newTestCases provided or invalid format");
            return params.newTestCases || [];
        }
        
        const newTestCases = params.newTestCases;
        const ticket = params.ticket || {};
        const jobParams = params.jobParams || {};
        
        // Extract project code from ticket key (e.g., "TP-1309" -> "TP")
        const projectCode = ticket.key ? ticket.key.split("-")[0] : (jobParams.inputJql ? extractProjectFromJql(jobParams.inputJql) : null);
        if (!projectCode) {
            console.error("Cannot determine project code from ticket or params");
            return newTestCases;
        }
        
        console.log("Using project code:", projectCode);
        
        // Step 1: Collect all preconditions with temporary IDs
        const preconditionMap = new Map(); // tempId -> precondition object
        const preconditionKeys = new Set(); // Set of all precondition keys (temp IDs and real keys)
        
        for (const testCase of newTestCases) {
            if (!testCase.customFields || !testCase.customFields.preconditions) {
                continue;
            }
            
            let preconditions = testCase.customFields.preconditions;
            
            // Handle preconditions as string (JSON) - parse it
            if (typeof preconditions === 'string') {
                try {
                    preconditions = JSON.parse(preconditions);
                } catch (e) {
                    console.warn(`Failed to parse preconditions as JSON for test case: ${testCase.summary}`, e);
                    continue;
                }
            }
            
            if (!Array.isArray(preconditions)) {
                continue;
            }
            
            for (const precondition of preconditions) {
                if (typeof precondition === 'string') {
                    // Existing precondition key - no processing needed
                    preconditionKeys.add(precondition);
                } else if (typeof precondition === 'object' && precondition !== null) {
                    const key = precondition.key;
                    if (key && key.startsWith('@')) {
                        // Temporary ID - collect for creation
                        if (!preconditionMap.has(key)) {
                            preconditionMap.set(key, precondition);
                        }
                        preconditionKeys.add(key);
                    } else if (key) {
                        // Real key - no processing needed
                        preconditionKeys.add(key);
                    }
                }
            }
        }
        
        console.log(`Found ${preconditionMap.size} unique preconditions with temporary IDs`);
        console.log(`Found ${preconditionKeys.size - preconditionMap.size} existing precondition references`);
        
        // Step 2: Create Precondition issues for temporary IDs
        const tempIdToRealKey = new Map(); // tempId -> realKey
        
        for (const [tempId, precondition] of preconditionMap.entries()) {
            try {
                console.log(`Creating precondition for ${tempId}: ${precondition.summary || 'No summary'}`);
                
                // Prepare steps JSON string if steps are provided
                let stepsJson = null;
                if (precondition.steps && Array.isArray(precondition.steps)) {
                    stepsJson = JSON.stringify(precondition.steps);
                }
                
                // Create precondition via MCP tool
                const createdKey = jira_xray_create_precondition({
                    project: projectCode,
                    summary: precondition.summary || "Precondition",
                    description: precondition.description || "",
                    steps: stepsJson
                });
                
                if (createdKey) {
                    tempIdToRealKey.set(tempId, createdKey);
                    console.log(`✅ Created precondition ${tempId} -> ${createdKey}`);
                } else {
                    console.error(`❌ Failed to create precondition for ${tempId}`);
                }
            } catch (error) {
                console.error(`Error creating precondition ${tempId}:`, error);
                // Continue with other preconditions
            }
        }
        
        // Step 3: Replace temporary IDs with real keys in all test cases
        const processedTestCases = newTestCases.map(testCase => {
            if (!testCase.customFields || !testCase.customFields.preconditions) {
                return testCase;
            }
            
            let preconditions = testCase.customFields.preconditions;
            
            // Handle preconditions as string (JSON) - parse it
            if (typeof preconditions === 'string') {
                try {
                    preconditions = JSON.parse(preconditions);
                } catch (e) {
                    console.warn(`Failed to parse preconditions as JSON for test case: ${testCase.summary}`, e);
                    return testCase;
                }
            }
            
            if (!Array.isArray(preconditions)) {
                return testCase;
            }
            
            // Create a copy of the test case
            const processedTestCase = JSON.parse(JSON.stringify(testCase));
            
            // Replace temporary IDs in preconditions array
            const updatedPreconditions = preconditions.map(precondition => {
                if (typeof precondition === 'string') {
                    // Check if it's a temporary ID
                    if (precondition.startsWith('@')) {
                        const realKey = tempIdToRealKey.get(precondition);
                        if (realKey) {
                            console.log(`Replacing ${precondition} with ${realKey} in test case: ${testCase.summary}`);
                            return realKey; // Return just the key string
                        } else {
                            console.warn(`No real key found for ${precondition}, keeping original`);
                            return precondition; // Keep original if creation failed
                        }
                    }
                    // Already a real key, keep as is
                    return precondition;
                } else if (typeof precondition === 'object' && precondition !== null) {
                    const key = precondition.key;
                    if (key && key.startsWith('@')) {
                        // Temporary ID - replace with real key
                        const realKey = tempIdToRealKey.get(key);
                        if (realKey) {
                            console.log(`Replacing ${key} with ${realKey} in test case: ${testCase.summary}`);
                            return realKey; // Return just the key string
                        } else {
                            console.warn(`No real key found for ${key}, keeping original`);
                            return precondition; // Keep original if creation failed
                        }
                    } else {
                        // Already a real key in object format, extract key
                        return key || precondition;
                    }
                }
                return precondition;
            });
            
            // Update preconditions in customFields (keep as array, not string)
            processedTestCase.customFields.preconditions = updatedPreconditions;
            
            return processedTestCase;
        });
        
        console.log(`✅ Preprocessing completed. Processed ${processedTestCases.length} test cases`);
        console.log(`   Created ${tempIdToRealKey.size} preconditions`);
        
        // Convert to plain JavaScript objects and return as JSON string for proper serialization
        // This ensures the result is properly converted back to Java JSONArray
        const result = processedTestCases.map(tc => {
            const obj = {
                priority: tc.priority,
                summary: tc.summary,
                description: tc.description,
                customFields: tc.customFields || {}
            };
            // Ensure customFields are plain objects
            if (obj.customFields.steps && typeof obj.customFields.steps === 'string') {
                // Keep steps as string if it's already a string
            } else if (obj.customFields.steps) {
                obj.customFields.steps = JSON.stringify(obj.customFields.steps);
            }
            if (obj.customFields.preconditions && Array.isArray(obj.customFields.preconditions)) {
                // Preconditions should be array of strings after replacement
                obj.customFields.preconditions = obj.customFields.preconditions;
            }
            return obj;
        });
        
        return result;
        
    } catch (error) {
        console.error("Error in preprocessing:", error);
        // Return original test cases on error
        return params.newTestCases || [];
    }
}

/**
 * Extract project code from JQL query
 * @param {string} jql - JQL query string
 * @returns {string|null} Project code or null
 */
function extractProjectFromJql(jql) {
    if (!jql) return null;
    
    // Try to extract from "project = TP" or "key in (TP-123)"
    const projectMatch = jql.match(/project\s*=\s*(\w+)/i);
    if (projectMatch) {
        return projectMatch[1];
    }
    
    const keyMatch = jql.match(/key\s+in\s*\((\w+)-\d+\)/i);
    if (keyMatch) {
        return keyMatch[1];
    }
    
    return null;
}

