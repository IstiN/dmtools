document.addEventListener('DOMContentLoaded', () => {
    const saveButton = document.getElementById('saveSettingsButton');
    const expertsContainer = document.getElementById('expertsContainer');
    const addExpertButton = document.getElementById('addExpertButton');
    const tgConfluencePagesContainer = document.getElementById('tgConfluencePagesContainer');
    const addTGConfluencePageButton = document.getElementById('addTGConfluencePageButton');
    const usgConfluencePagesContainer = document.getElementById('usgConfluencePagesContainer');
    const addUSGConfluencePageButton = document.getElementById('addUSGConfluencePageButton');

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

    const createExpertRow = (expert = { label: '', projectContext: '', confluencePages: [] }) => {
        const expertDiv = document.createElement('div');
        expertDiv.className = 'expert-row';

        expertDiv.innerHTML = `
            <label>Expert Label</label>
            <input type="text" class="expert-label" placeholder="Enter expert label" value="${expert.label || ''}">
            <label>Project Context</label>
            <input type="text" class="expert-project-context" placeholder="Enter project context" value="${expert.projectContext || ''}">
            <label>Confluence Pages</label>
            <div class="expert-confluence-pages"></div>
            <button type="button" class="add-confluence-page add-button">Add Confluence Page</button>
            <button type="button" class="remove-button">Remove Expert</button>
        `;

        const confluencePagesContainer = expertDiv.querySelector('.expert-confluence-pages');
        expert.confluencePages.forEach(page => {
            confluencePagesContainer.appendChild(createConfluencePageRow(page, confluencePagesContainer));
        });

        expertDiv.querySelector('.add-confluence-page').addEventListener('click', () => {
            confluencePagesContainer.appendChild(createConfluencePageRow('', confluencePagesContainer));
        });

        expertDiv.querySelector('.remove-button').addEventListener('click', () => {
            expertsContainer.removeChild(expertDiv);
        });

        return expertDiv;
    };

    const saveSettings = () => {
        const experts = Array.from(expertsContainer.children).map(expertDiv => ({
            label: expertDiv.querySelector('.expert-label').value,
            projectContext: expertDiv.querySelector('.expert-project-context').value,
            confluencePages: Array.from(expertDiv.querySelectorAll('.confluence-page-input')).map(input => input.value)
        }));

        const settings = {
            token: document.getElementById('token').value,
            basePath: document.getElementById('basePath').value,
            gitlabProjectId: document.getElementById('gitlabProjectId').value,
            jiraInitiator: document.getElementById('jiraInitiator').value,
            experts: experts,
            existingTestCasesJql: document.getElementById('existingTestCasesJql').value,
            relatedTestCasesRules: document.getElementById('relatedTestCasesRules').value,
            testCasesPriorities: document.getElementById('testCasesPriorities').value,
            testCaseIssueType: document.getElementById('testCaseIssueType').value,
            outputType: document.getElementById('outputType').value,
            tgConfluencePages: Array.from(tgConfluencePagesContainer.querySelectorAll('.confluence-page-input')).map(input => input.value),
            usgExistingUserStoriesJql: document.getElementById('usgExistingUserStoriesJql').value,
            usgPriorities: document.getElementById('usgPriorities').value,
            usgProjectCode: document.getElementById('usgProjectCode').value,
            usgIssueType: document.getElementById('usgIssueType').value,
            usgAcceptanceCriteriaField: document.getElementById('usgAcceptanceCriteriaField').value,
            usgParentField: document.getElementById('usgParentField').value,
            usgRelationship: document.getElementById('usgRelationship').value,
            usgOutputType: document.getElementById('usgOutputType').value,
            usgConfluencePages: Array.from(usgConfluencePagesContainer.querySelectorAll('.confluence-page-input')).map(input => input.value)
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

                // Load experts
                expertsContainer.innerHTML = '';
                const experts = result.settings.experts || [{
                    label: 'Default Expert',
                    projectContext: result.settings.projectContext || '',
                    confluencePages: result.settings.confluencePages || []
                }];
                experts.forEach(expert => {
                    expertsContainer.appendChild(createExpertRow(expert));
                });

                document.getElementById('existingTestCasesJql').value = result.settings.existingTestCasesJql || '';
                document.getElementById('relatedTestCasesRules').value = result.settings.relatedTestCasesRules || '';
                document.getElementById('testCasesPriorities').value = result.settings.testCasesPriorities || '';
                document.getElementById('testCaseIssueType').value = result.settings.testCaseIssueType || '';
                document.getElementById('outputType').value = result.settings.outputType || 'creation';

                // Load TestCasesGenerator confluence pages
                tgConfluencePagesContainer.innerHTML = '';
                (result.settings.tgConfluencePages || []).forEach(page => {
                    tgConfluencePagesContainer.appendChild(createConfluencePageRow(page, tgConfluencePagesContainer));
                });

                // Load UserStoryGenerator settings
                document.getElementById('usgExistingUserStoriesJql').value = result.settings.usgExistingUserStoriesJql || '';
                document.getElementById('usgPriorities').value = result.settings.usgPriorities || '';
                document.getElementById('usgProjectCode').value = result.settings.usgProjectCode || '';
                document.getElementById('usgIssueType').value = result.settings.usgIssueType || '';
                document.getElementById('usgAcceptanceCriteriaField').value = result.settings.usgAcceptanceCriteriaField || '';
                document.getElementById('usgParentField').value = result.settings.usgParentField || '';
                document.getElementById('usgRelationship').value = result.settings.usgRelationship || '';
                document.getElementById('usgOutputType').value = result.settings.usgOutputType || 'trackerComment';

                // Load UserStoryGenerator confluence pages
                usgConfluencePagesContainer.innerHTML = '';
                (result.settings.usgConfluencePages || []).forEach(page => {
                    usgConfluencePagesContainer.appendChild(createConfluencePageRow(page, usgConfluencePagesContainer));
                });
            }
        });
    };

    addExpertButton.addEventListener('click', () => {
        expertsContainer.appendChild(createExpertRow());
    });

    addTGConfluencePageButton.addEventListener('click', () => {
        tgConfluencePagesContainer.appendChild(createConfluencePageRow('', tgConfluencePagesContainer));
    });

    addUSGConfluencePageButton.addEventListener('click', () => {
        usgConfluencePagesContainer.appendChild(createConfluencePageRow('', usgConfluencePagesContainer));
    });

    saveButton.addEventListener('click', saveSettings);

    // Load settings on page load
    loadSettings();
});