/**
 * Preprocess Xray Cucumber Test Cases - Handle Gherkin, Datasets, and Preconditions
 * 
 * This function processes Cucumber test cases to:
 * 1. Validate Gherkin syntax and dataset structure
 * 2. Ensure parameter consistency between Gherkin and Dataset
 * 3. Handle preconditions with temporary IDs (create and replace)
 * 
 * @param {Object} params - Parameters object containing:
 *   - newTestCases: Array of test case objects with customFields.gherkin and customFields.dataset
 *   - ticket: Main ticket context
 *   - jobParams: Job parameters
 * @returns {Array} Modified array of test cases with validated Gherkin/Dataset and replaced temp IDs
 */
function action(params) {
    try {
        console.log("Starting Xray Cucumber test cases preprocessing");
        
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
        
        // Step 1: Validate and process Cucumber-specific fields
        const validatedTestCases = newTestCases.map((testCase, index) => {
            console.log(`Processing test case ${index + 1}: ${testCase.summary || 'No summary'}`);
            
            if (!testCase.customFields) {
                return testCase;
            }
            
            const gherkin = testCase.customFields.gherkin;
            let dataset = testCase.customFields.dataset;
            
            // Parse dataset if it's a string
            if (typeof dataset === 'string') {
                try {
                    dataset = JSON.parse(dataset);
                } catch (e) {
                    console.error(`Failed to parse dataset JSON for test case: ${testCase.summary}`, e);
                    dataset = null;
                }
            }
            
            // Validate Gherkin + Dataset consistency
            if (gherkin && dataset) {
                const validation = validateGherkinDatasetConsistency(gherkin, dataset, testCase.summary);
                if (!validation.valid) {
                    console.warn(`⚠️ Validation issues for ${testCase.summary}:`, validation.errors.join(", "));
                } else {
                    console.log(`✅ Gherkin and Dataset are consistent for ${testCase.summary}`);
                }
            }
            
            // Ensure dataset is kept as object (will be stringified later)
            if (dataset && typeof dataset === 'object') {
                testCase.customFields.dataset = dataset;
            }
            
            return testCase;
        });
        
        // Step 2: Handle Preconditions (same logic as manual tests)
        const preconditionMap = new Map(); // tempId -> precondition object
        const preconditionKeys = new Set();
        
        for (const testCase of validatedTestCases) {
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
                    preconditionKeys.add(precondition);
                } else if (typeof precondition === 'object' && precondition !== null) {
                    const key = precondition.key;
                    if (key && key.startsWith('@')) {
                        if (!preconditionMap.has(key)) {
                            preconditionMap.set(key, precondition);
                        }
                        preconditionKeys.add(key);
                    } else if (key) {
                        preconditionKeys.add(key);
                    }
                }
            }
        }
        
        console.log(`Found ${preconditionMap.size} unique preconditions with temporary IDs`);
        
        // Step 3: Create Precondition issues
        const tempIdToRealKey = new Map();
        
        for (const [tempId, precondition] of preconditionMap.entries()) {
            try {
                console.log(`Creating precondition for ${tempId}: ${precondition.summary || 'No summary'}`);
                
                let stepsJson = null;
                if (precondition.steps && Array.isArray(precondition.steps)) {
                    stepsJson = JSON.stringify(precondition.steps);
                }
                
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
            }
        }
        
        // Step 4: Replace temporary IDs and prepare final structure
        const processedTestCases = validatedTestCases.map(testCase => {
            if (!testCase.customFields) {
                return testCase;
            }
            
            // Create a copy
            const processedTestCase = JSON.parse(JSON.stringify(testCase));
            
            // Replace precondition temp IDs
            if (processedTestCase.customFields.preconditions) {
                let preconditions = processedTestCase.customFields.preconditions;
                
                if (typeof preconditions === 'string') {
                    try {
                        preconditions = JSON.parse(preconditions);
                    } catch (e) {
                        console.warn(`Failed to parse preconditions for final replacement: ${testCase.summary}`);
                        return processedTestCase;
                    }
                }
                
                if (Array.isArray(preconditions)) {
                    const updatedPreconditions = preconditions.map(precondition => {
                        if (typeof precondition === 'string' && precondition.startsWith('@')) {
                            return tempIdToRealKey.get(precondition) || precondition;
                        } else if (typeof precondition === 'object' && precondition !== null) {
                            const key = precondition.key;
                            if (key && key.startsWith('@')) {
                                return tempIdToRealKey.get(key) || key;
                            }
                            return key || precondition;
                        }
                        return precondition;
                    });
                    
                    processedTestCase.customFields.preconditions = updatedPreconditions;
                }
            }
            
            return processedTestCase;
        });
        
        console.log(`✅ Cucumber preprocessing completed. Processed ${processedTestCases.length} test cases`);
        console.log(`   Created ${tempIdToRealKey.size} preconditions`);
        
        // Convert to final format
        const result = processedTestCases.map(tc => {
            const obj = {
                priority: tc.priority,
                summary: tc.summary,
                description: tc.description,
                customFields: tc.customFields || {}
            };
            
            // Ensure gherkin is string
            if (obj.customFields.gherkin) {
                if (typeof obj.customFields.gherkin !== 'string') {
                    obj.customFields.gherkin = String(obj.customFields.gherkin);
                }
            }
            
            // Ensure dataset is stringified JSON
            if (obj.customFields.dataset) {
                if (typeof obj.customFields.dataset === 'object') {
                    obj.customFields.dataset = JSON.stringify(obj.customFields.dataset);
                } else if (typeof obj.customFields.dataset !== 'string') {
                    obj.customFields.dataset = String(obj.customFields.dataset);
                }
            }
            
            // Ensure preconditions is array of strings
            if (obj.customFields.preconditions && Array.isArray(obj.customFields.preconditions)) {
                obj.customFields.preconditions = obj.customFields.preconditions;
            }
            
            return obj;
        });
        
        return result;
        
    } catch (error) {
        console.error("Error in Cucumber preprocessing:", error);
        return params.newTestCases || [];
    }
}

/**
 * Validate consistency between Gherkin and Dataset
 * @param {string} gherkin - Gherkin scenario text
 * @param {Object} dataset - Dataset object with parameters and rows
 * @param {string} testName - Test case name for logging
 * @returns {Object} Validation result with valid flag and errors array
 */
function validateGherkinDatasetConsistency(gherkin, dataset, testName) {
    const errors = [];
    
    if (!dataset || !dataset.parameters || !dataset.rows) {
        errors.push("Dataset must have 'parameters' and 'rows' fields");
        return { valid: false, errors };
    }
    
    // Extract parameters from Gherkin (look for <parameter> placeholders)
    const gherkinParamMatches = gherkin.match(/<(\w+)>/g);
    const gherkinParams = gherkinParamMatches ? 
        [...new Set(gherkinParamMatches.map(p => p.slice(1, -1)))] : [];
    
    // Extract parameters from Dataset
    const datasetParams = dataset.parameters.map(p => p.name);
    
    // Check if all Gherkin parameters are in Dataset
    for (const gherkinParam of gherkinParams) {
        if (!datasetParams.includes(gherkinParam)) {
            errors.push(`Gherkin parameter "<${gherkinParam}>" not found in dataset`);
        }
    }
    
    // Check if all Dataset parameters are used in Gherkin (warning only)
    for (const datasetParam of datasetParams) {
        if (!gherkinParams.includes(datasetParam)) {
            console.warn(`Dataset parameter "${datasetParam}" not used in Gherkin for ${testName}`);
        }
    }
    
    // Validate rows structure
    for (let i = 0; i < dataset.rows.length; i++) {
        const row = dataset.rows[i];
        
        if (!row.hasOwnProperty('order')) {
            errors.push(`Row ${i} missing 'order' field`);
        }
        
        if (!row.Values || !Array.isArray(row.Values)) {
            errors.push(`Row ${i} missing 'Values' array`);
            continue;
        }
        
        if (row.Values.length !== dataset.parameters.length) {
            errors.push(`Row ${i} has ${row.Values.length} values but ${dataset.parameters.length} parameters defined`);
        }
    }
    
    return {
        valid: errors.length === 0,
        errors
    };
}

/**
 * Extract project code from JQL query
 * @param {string} jql - JQL query string
 * @returns {string|null} Project code or null
 */
function extractProjectFromJql(jql) {
    if (!jql) return null;
    
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
