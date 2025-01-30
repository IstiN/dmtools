document.addEventListener('DOMContentLoaded', function() {
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

    document.getElementById('readUrlButton').addEventListener('click', function() {
        chrome.storage.sync.get(['settings'], (result) => {
            const settings = result.settings || {};
            const userInput = document.getElementById('requestInput').value;
            const jsonData = {
                "name": "Expert",
                "params": {
                    "inputJql": "",
                    "initiator": settings.jiraInitiator,
                    "request": userInput,
                    "projectContext": settings.projectContext,
                    "confluencePages": settings.confluencePages
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
                    "relatedTestCasesRules": settings.relatedTestCasesRules.toString(), // Convert to string
                    "testCasesPriorities": settings.testCasesPriorities,
                    "outputType": settings.outputType,
                    "testCaseIssueType": settings.testCaseIssueType
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

    // Open settings page
    document.getElementById('openSettingsButton').addEventListener('click', function() {
        chrome.runtime.openOptionsPage();
    });
});