document.addEventListener('DOMContentLoaded', function() {
    const expertSelect = document.getElementById('expertSelect');

    // Load experts into select
    chrome.storage.sync.get(['settings'], (result) => {
        if (result.settings && result.settings.experts) {
            expertSelect.innerHTML = '<option value="">Select Expert</option>';
            result.settings.experts.forEach(expert => {
                const option = document.createElement('option');
                option.value = expert.label;
                option.textContent = expert.label || 'Unnamed Expert';
                expertSelect.appendChild(option);
            });
        }
    });

    const triggerPipeline = (params, jsonData) => {
        chrome.storage.sync.get(['settings'], (result) => {
            if (!result.settings) {
                alert('Settings not found. Please configure the settings.');
                return;
            }

            const { token, basePath, gitlabProjectId } = result.settings;

            chrome.tabs.query({ active: true, currentWindow: true }, function(tabs) {
                const activeTab = tabs[0];
                const activeTabUrl = activeTab.url;
                const ticketIdMatch = activeTabUrl.match(/browse\/([A-Z]+-\d+)/);
                if (ticketIdMatch && ticketIdMatch[1]) {
                    const ticketId = ticketIdMatch[1];

                    const finalJsonData = JSON.stringify({
                        ...jsonData,
                        params: {
                            ...jsonData.params,
                            inputJql: "key = " + ticketId
                        }
                    });

                    const urlEncodedJsonData = encodeURIComponent(finalJsonData);

                    const postBody = {
                        "token": token,
                        "ref": "main",
                        "variables": {
                            "JOB_PARAMS": urlEncodedJsonData
                        }
                    };

                    fetch(`${basePath}/api/v4/projects/${gitlabProjectId}/trigger/pipeline`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(postBody)
                    })
                        .then(response => response.json())
                        .then(data => {
                            console.log('Success:', data);
                            alert('Pipeline triggered successfully.');
                        })
                        .catch(error => {
                            console.error('Error:', error);
                            alert('Error triggering pipeline.');
                        });
                } else {
                    alert("No ticket ID found in URL.");
                }
            });
        });
    };

    document.getElementById('askExpertButton').addEventListener('click', function() {
        const selectedExpertLabel = expertSelect.value;
        if (!selectedExpertLabel) {
            alert('Please select an expert');
            return;
        }

        chrome.storage.sync.get(['settings'], (result) => {
            const settings = result.settings || {};
            const selectedExpert = settings.experts.find(e => e.label === selectedExpertLabel);

            if (!selectedExpert) {
                alert('Expert configuration not found');
                return;
            }

            const userInput = document.getElementById('requestInput').value;
            const jsonData = {
                "name": "Expert",
                "params": {
                    "inputJql": "",
                    "initiator": settings.jiraInitiator,
                    "request": userInput,
                    "projectContext": selectedExpert.projectContext,
                    "confluencePages": selectedExpert.confluencePages,
                    "isCodeAsSource": selectedExpert.isCodeAsSource || false,
                    "isConfluenceAsSource": selectedExpert.isConfluenceAsSource || false,
                    "isTrackerAsSource": selectedExpert.isTrackerAsSource || false,
                    "filesLimit": selectedExpert.filesLimit || 5,
                    "filesIterations": selectedExpert.filesIterations || 1,
                    "confluenceLimit": selectedExpert.confluenceLimit || 5,
                    "confluenceIterations": selectedExpert.confluenceIterations || 1,
                    "trackerLimit": selectedExpert.trackerLimit || 5,
                    "trackerIterations": selectedExpert.trackerIterations || 1,
                    "searchOrchestratorType": selectedExpert.searchOrchestratorType || "BULK",
                    "source_code_config": selectedExpert.source_code_config || []
                }
            };
            triggerPipeline({}, jsonData);
        });
    });

    document.getElementById('generateTestCasesButton').addEventListener('click', function() {
        chrome.storage.sync.get(['settings'], (result) => {
            const settings = result.settings || {};
            const jsonData = {
                "name": "TestCasesGenerator",
                "params": {
                    "inputJql": "",
                    "initiator": settings.jiraInitiator,
                    "existingTestCasesJql": settings.existingTestCasesJql,
                    "confluencePages": settings.tgConfluencePages,
                    "relatedTestCasesRules": settings.relatedTestCasesRules.toString(),
                    "testCasesPriorities": settings.testCasesPriorities,
                    "outputType": settings.outputType,
                    "testCaseIssueType": settings.testCaseIssueType
                }
            };
            triggerPipeline({}, jsonData);
        });
    });

    document.getElementById('generateUserStoriesButton').addEventListener('click', function() {
        chrome.storage.sync.get(['settings'], (result) => {
            const settings = result.settings || {};
            const jsonData = {
                "name": "UserStoryGenerator",
                "params": {
                    "inputJql": "",
                    "initiator": settings.jiraInitiator,
                    "existingUserStoriesJql": settings.usgExistingUserStoriesJql,
                    "confluencePages": settings.usgConfluencePages,
                    "priorities": settings.usgPriorities,
                    "projectCode": settings.usgProjectCode,
                    "issueType": settings.usgIssueType,
                    "acceptanceCriteriaField": settings.usgAcceptanceCriteriaField,
                    "parentField": settings.usgParentField,
                    "relationship": settings.usgRelationship,
                    "outputType": settings.usgOutputType
                }
            };
            triggerPipeline({}, jsonData);
        });
    });

    document.getElementById('alertTextContentButton').addEventListener('click', function() {
        chrome.tabs.query({ active: true, currentWindow: true }, function(tabs) {
            const activeTab = tabs[0];
            chrome.scripting.executeScript({
                target: { tabId: activeTab.id },
                func: () => document.body.innerText
            }, (results) => {
                if (results && results[0] && results[0].result) {
                    alert(results[0].result);
                } else {
                    alert("No text content found.");
                }
            });
        });
    });

    document.getElementById('openSettingsButton').addEventListener('click', function() {
        chrome.runtime.openOptionsPage();
    });
});