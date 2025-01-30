document.addEventListener('DOMContentLoaded', () => {
    const saveButton = document.getElementById('saveSettingsButton');
    const expertConfluencePagesContainer = document.getElementById('expertConfluencePagesContainer');
    const addExpertConfluencePageButton = document.getElementById('addExpertConfluencePageButton');
    const tgConfluencePagesContainer = document.getElementById('tgConfluencePagesContainer');
    const addTGConfluencePageButton = document.getElementById('addTGConfluencePageButton');

    // Function to create a Confluence Page input row
    const createConfluencePageRow = (value = '', container) => {
        const row = document.createElement('div');
        row.className = 'confluence-page-row';

        const input = document.createElement('input');
        input.type = 'text';
        input.className = 'confluence-page-input';
        input.placeholder = 'Enter Confluence page URL';
        input.value = value;

        const removeButton = document.createElement('button');
        removeButton.className = 'remove-button';
        removeButton.innerText = 'X';
        removeButton.addEventListener('click', () => {
            container.removeChild(row);
        });

        row.appendChild(input);
        row.appendChild(removeButton);

        return row;
    };

    // Rest of the JavaScript remains the same
    const saveSettings = () => {
        const settings = {
            token: document.getElementById('token').value,
            basePath: document.getElementById('basePath').value,
            gitlabProjectId: document.getElementById('gitlabProjectId').value,
            jiraInitiator: document.getElementById('jiraInitiator').value,
            projectContext: document.getElementById('projectContext').value,
            confluencePages: Array.from(expertConfluencePagesContainer.children).map(row => row.querySelector('input').value),
            existingTestCasesJql: document.getElementById('existingTestCasesJql').value,
            relatedTestCasesRules: document.getElementById('relatedTestCasesRules').value,
            testCasesPriorities: document.getElementById('testCasesPriorities').value,
            testCaseIssueType: document.getElementById('testCaseIssueType').value,
            outputType: document.getElementById('outputType').value,
            tgConfluencePages: Array.from(tgConfluencePagesContainer.children).map(row => row.querySelector('input').value)
        };

        chrome.storage.sync.set({ settings }, () => {
            alert('Settings saved');
        });
    };

    const loadSettings = () => {
        chrome.storage.sync.get(['settings'], (result) => {
            if (result.settings) {
                document.getElementById('token').value = result.settings.token || '';
                document.getElementById('basePath').value = result.settings.basePath || '';
                document.getElementById('gitlabProjectId').value = result.settings.gitlabProjectId || '';
                document.getElementById('jiraInitiator').value = result.settings.jiraInitiator || '';
                document.getElementById('projectContext').value = result.settings.projectContext || '';
                const expertConfluencePages = result.settings.confluencePages || [];
                expertConfluencePagesContainer.innerHTML = '';
                expertConfluencePages.forEach(page => {
                    expertConfluencePagesContainer.appendChild(createConfluencePageRow(page, expertConfluencePagesContainer));
                });
                document.getElementById('existingTestCasesJql').value = result.settings.existingTestCasesJql || '';
                document.getElementById('relatedTestCasesRules').value = result.settings.relatedTestCasesRules || '';
                document.getElementById('testCasesPriorities').value = result.settings.testCasesPriorities || '';
                document.getElementById('testCaseIssueType').value = result.settings.testCaseIssueType || '';
                document.getElementById('outputType').value = result.settings.outputType || 'creation';
                const tgConfluencePages = result.settings.tgConfluencePages || [];
                tgConfluencePagesContainer.innerHTML = '';
                tgConfluencePages.forEach(page => {
                    tgConfluencePagesContainer.appendChild(createConfluencePageRow(page, tgConfluencePagesContainer));
                });
            }
        });
    };

    addExpertConfluencePageButton.addEventListener('click', () => {
        expertConfluencePagesContainer.appendChild(createConfluencePageRow('', expertConfluencePagesContainer));
    });

    addTGConfluencePageButton.addEventListener('click', () => {
        tgConfluencePagesContainer.appendChild(createConfluencePageRow('', tgConfluencePagesContainer));
    });

    saveButton.addEventListener('click', saveSettings);

    // Load settings on page load
    loadSettings();
});