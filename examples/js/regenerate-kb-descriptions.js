/**
 * Regenerate AI descriptions for people and topics
 * 
 * This script regenerates AI-generated descriptions for all people and topics
 * in the knowledge base WITHOUT processing new data.
 * 
 * Usage:
 *   dmtools --job jsrunner --param jsPath=examples/js/regenerate-kb-descriptions.js
 * 
 * Or from your KB directory (e.g., ai.m):
 *   ~/git/dmtools/dmtools-core/dmtools.sh --job jsrunner --param jsPath=regenerate-kb-descriptions.js
 * 
 * Parameters (optional):
 * - kbOutputPath: Override KB path (defaults to DMTOOLS_KB_OUTPUT_PATH env)
 * - smartMode: Only regenerate if Q/A/N changed (default: true)
 * - sourceName: Only regenerate for specific source (default: null = all sources)
 * 
 * Example with custom params:
 *   dmtools --job jsrunner --param jsPath=examples/js/regenerate-kb-descriptions.js \
 *           --job-param kbOutputPath=/custom/path \
 *           --job-param smartMode=false
 */

function action() {
    // Get parameters from job params or use defaults
    const params = (typeof jobParams !== 'undefined') ? jobParams : {};

    // Resolve KB output path
    function getKBOutputPath(jobParams) {
        if (jobParams && jobParams.kbOutputPath) {
            console.log('Using KB path from job params:', jobParams.kbOutputPath);
            return jobParams.kbOutputPath;
        }
        
        // Let KB tools resolve from DMTOOLS_KB_OUTPUT_PATH env var
        console.log('Using default KB path (will be resolved from DMTOOLS_KB_OUTPUT_PATH)');
        return null;
    }

    const kbPath = getKBOutputPath(params);
    const smartMode = params.smartMode !== false ? 'true' : 'false';
    const sourceName = params.sourceName || null;

    console.log('='.repeat(60));
    console.log('KB Descriptions Regeneration');
    console.log('='.repeat(60));
    console.log('KB Path:', kbPath || '(from DMTOOLS_KB_OUTPUT_PATH env)');
    console.log('Smart Mode:', smartMode === 'true' ? 'Yes (only if Q/A/N changed)' : 'No (regenerate all)');
    console.log('Source Filter:', sourceName || 'All sources');
    console.log('='.repeat(60));

    // Call kb_aggregate to regenerate descriptions
    console.log('\nStarting description regeneration...');

    try {
        const result = kb_aggregate({
            source_name: sourceName,
            output_path: kbPath,
            smart_mode: smartMode
        });
        
        console.log('\n' + '='.repeat(60));
        console.log('Regeneration Complete!');
        console.log('='.repeat(60));
        console.log('Result:', result);
        
        // Parse and display summary
        let resultObj = null;
        try {
            resultObj = JSON.parse(result);
            if (resultObj.success) {
                console.log('\n✅ Success!');
                if (resultObj.message) {
                    console.log('Message:', resultObj.message);
                }
                if (resultObj.people_count !== undefined) {
                    console.log('People processed:', resultObj.people_count);
                }
                if (resultObj.topics_count !== undefined) {
                    console.log('Topics processed:', resultObj.topics_count);
                }
            } else {
                console.log('\n❌ Failed:', resultObj.message);
            }
        } catch (parseError) {
            // If result isn't JSON, just show as-is
            console.log('Raw result:', result);
        }
        
        console.log('\n' + '='.repeat(60));
        console.log('Next Steps:');
        console.log('1. Review updated *-desc.md files in people/ and topics/');
        console.log('2. Commit changes: git add -A && git commit -m "Regenerate KB descriptions"');
        console.log('='.repeat(60));
        
        // Return success result for JSRunner
        return {
            success: true,
            message: 'KB descriptions regenerated successfully',
            result: resultObj || result
        };
        
    } catch (error) {
        console.error('\n' + '='.repeat(60));
        console.error('❌ Error during regeneration:');
        console.error('='.repeat(60));
        console.error(error);
        
        // Return error result for JSRunner
        return {
            success: false,
            error: error.toString()
        };
    }
}

