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

    const createSourceCodeConfigRow = (config = {}) => {
        const row = document.createElement('div');
        row.className = 'source-code-config-row';

        row.innerHTML = `
            <input type="text" class="branch-name" placeholder="Branch Name" value="${config.branch_name || ''}">
            <input type="text" class="repo-name" placeholder="Repository Name" value="${config.repo_name || ''}">
            <input type="text" class="workspace-name" placeholder="Workspace Name" value="${config.workspace_name || ''}">
            <select class="repo-type">
                <option value="GITHUB" ${config.type === 'GITHUB' ? 'selected' : ''}>GitHub</option>
                <option value="BITBUCKET" ${config.type === 'BITBUCKET' ? 'selected' : ''}>Bitbucket</option>
                <option value="GITLAB" ${config.type === 'GITLAB' ? 'selected' : ''}>GitLab</option>
            </select>
            <input type="password" class="auth" placeholder="Authentication" value="${config.auth || ''}">
            <input type="text" class="path" placeholder="Path" value="${config.path || ''}">
            <input type="text" class="api-version" placeholder="API Version" value="${config.api_version || ''}">
            <button type="button" class="remove-button">X</button>
        `;

        row.querySelector('.remove-button').addEventListener('click', () => {
            row.parentElement.removeChild(row);
        });

        return row;
    };

    const createExpertRow = (expert = {
        label: '',
        projectContext: '',
        confluencePages: [],
        isCodeAsSource: false,
        isConfluenceAsSource: false,
        isTrackerAsSource: false,
        filesLimit: 5,
        filesIterations: 1,
        confluenceLimit: 5,
        confluenceIterations: 1,
        trackerLimit: 5,
        trackerIterations: 1,
        searchOrchestratorType: 'BULK',
        source_code_config: []
    }) => {
        const expertDiv = document.createElement('div');
        expertDiv.className = 'expert-row';

        expertDiv.innerHTML = `
            <label>Expert Label</label>
            <input type="text" class="expert-label" placeholder="Enter expert label" value="${expert.label || ''}">

            <label>Project Context</label>
            <input type="text" class="expert-project-context" placeholder="Enter project context" value="${expert.projectContext || ''}">

            <div class="checkbox-group">
                <label>
                    <input type="checkbox" class="is-code-source" ${expert.isCodeAsSource ? 'checked' : ''}>
                    Use Code as Source
                </label>
                <label>
                    <input type="checkbox" class="is-confluence-source" ${expert.isConfluenceAsSource ? 'checked' : ''}>
                    Use Confluence as Source
                </label>
                <label>
                    <input type="checkbox" class="is-tracker-source" ${expert.isTrackerAsSource ? 'checked' : ''}>
                    Use Tracker as Source
                </label>
            </div>

            <div class="limits-group">
                <label>Files Limit</label>
                <input type="number" class="files-limit" value="${expert.filesLimit || 5}" min="1">
                <label>Files Iterations</label>
                <input type="number" class="files-iterations" value="${expert.filesIterations || 1}" min="1">

                <label>Confluence Limit</label>
                <input type="number" class="confluence-limit" value="${expert.confluenceLimit || 5}" min="1">
                <label>Confluence Iterations</label>
                <input type="number" class="confluence-iterations" value="${expert.confluenceIterations || 1}" min="1">

                <label>Tracker Limit</label>
                <input type="number" class="tracker-limit" value="${expert.trackerLimit || 5}" min="1">
                <label>Tracker Iterations</label>
                <input type="number" class="tracker-iterations" value="${expert.trackerIterations || 1}" min="1">

                <label>Search Orchestrator Type</label>
                <select class="search-orchestrator-type">
                    <option value="BULK" ${expert.searchOrchestratorType === 'BULK' ? 'selected' : ''}>BULK</option>
                    <option value="ONE_BY_ONE" ${expert.searchOrchestratorType === 'ONE_BY_ONE' ? 'selected' : ''}>ONE_BY_ONE</option>
                </select>
            </div>

            <div class="source-code-config">
                <h4>Source Code Configuration</h4>
                <div class="source-code-entries"></div>
                <button type="button" class="add-source-config add-button">Add Source Configuration</button>
            </div>

            <label>Confluence Pages</label>
            <div class="expert-confluence-pages"></div>
            <button type="button" class="add-confluence-page add-button">Add Confluence Page</button>
            <button type="button" class="remove-button">Remove Expert</button>
        `;

        const sourceCodeEntriesContainer = expertDiv.querySelector('.source-code-entries');

        if (expert.source_code_config && expert.source_code_config.length > 0) {
            expert.source_code_config.forEach(config => {
                sourceCodeEntriesContainer.appendChild(createSourceCodeConfigRow(config));
            });
        }

        expertDiv.querySelector('.add-source-config').addEventListener('click', () => {
            sourceCodeEntriesContainer.appendChild(createSourceCodeConfigRow());
        });

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
            confluencePages: Array.from(expertDiv.querySelectorAll('.confluence-page-input')).map(input => input.value),
            isCodeAsSource: expertDiv.querySelector('.is-code-source').checked,
            isConfluenceAsSource: expertDiv.querySelector('.is-confluence-source').checked,
            isTrackerAsSource: expertDiv.querySelector('.is-tracker-source').checked,
            filesLimit: parseInt(expertDiv.querySelector('.files-limit').value) || 5,
            filesIterations: parseInt(expertDiv.querySelector('.files-iterations').value) || 1,
            confluenceLimit: parseInt(expertDiv.querySelector('.confluence-limit').value) || 5,
            confluenceIterations: parseInt(expertDiv.querySelector('.confluence-iterations').value) || 1,
            trackerLimit: parseInt(expertDiv.querySelector('.tracker-limit').value) || 5,
            trackerIterations: parseInt(expertDiv.querySelector('.tracker-iterations').value) || 1,
            searchOrchestratorType: expertDiv.querySelector('.search-orchestrator-type').value,
            source_code_config: Array.from(expertDiv.querySelectorAll('.source-code-config-row')).map(row => ({
                branch_name: row.querySelector('.branch-name').value,
                repo_name: row.querySelector('.repo-name').value,
                workspace_name: row.querySelector('.workspace-name').value,
                type: row.querySelector('.repo-type').value,
                auth: row.querySelector('.auth').value,
                path: row.querySelector('.path').value,
                api_version: row.querySelector('.api-version').value
            }))
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

                expertsContainer.innerHTML = '';
                const experts = result.settings.experts || [{
                    label: 'Default Expert',
                    projectContext: result.settings.projectContext || '',
                    confluencePages: result.settings.confluencePages || [],
                    isCodeAsSource: false,
                    isConfluenceAsSource: true,
                    isTrackerAsSource: false,
                    filesLimit: 5,
                    filesIterations: 1,
                    confluenceLimit: 5,
                    confluenceIterations: 1,
                    trackerLimit: 5,
                    trackerIterations: 1,
                    searchOrchestratorType: 'BULK',
                    source_code_config: []
                }];
                experts.forEach(expert => {
                    expertsContainer.appendChild(createExpertRow(expert));
                });

                document.getElementById('existingTestCasesJql').value = result.settings.existingTestCasesJql || '';
                document.getElementById('relatedTestCasesRules').value = result.settings.relatedTestCasesRules || '';
                document.getElementById('testCasesPriorities').value = result.settings.testCasesPriorities || '';
                document.getElementById('testCaseIssueType').value = result.settings.testCaseIssueType || '';
                document.getElementById('outputType').value = result.settings.outputType || 'creation';

                tgConfluencePagesContainer.innerHTML = '';
                (result.settings.tgConfluencePages || []).forEach(page => {
                    tgConfluencePagesContainer.appendChild(createConfluencePageRow(page, tgConfluencePagesContainer));
                });

                document.getElementById('usgExistingUserStoriesJql').value = result.settings.usgExistingUserStoriesJql || '';
                document.getElementById('usgPriorities').value = result.settings.usgPriorities || '';
                document.getElementById('usgProjectCode').value = result.settings.usgProjectCode || '';
                document.getElementById('usgIssueType').value = result.settings.usgIssueType || '';
                document.getElementById('usgAcceptanceCriteriaField').value = result.settings.usgAcceptanceCriteriaField || '';
                document.getElementById('usgParentField').value = result.settings.usgParentField || '';
                document.getElementById('usgRelationship').value = result.settings.usgRelationship || '';
                document.getElementById('usgOutputType').value = result.settings.usgOutputType || 'trackerComment';

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

    loadSettings();
});