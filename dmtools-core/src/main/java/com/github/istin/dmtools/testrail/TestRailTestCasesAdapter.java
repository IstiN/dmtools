package com.github.istin.dmtools.testrail;

import com.github.istin.dmtools.ai.agent.TestCaseGeneratorAgent;
import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.qa.CustomTestCasesTrackerParams;
import com.github.istin.dmtools.qa.TestCasesGeneratorParams;
import com.github.istin.dmtools.qa.TestCasesTrackerAdapter;
import com.github.istin.dmtools.testrail.model.TestCase;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TestCasesTrackerAdapter implementation for TestRail.
 * Wraps TestRailClient to provide test case operations needed by TestCasesGenerator.
 * All project/label/type configuration is encapsulated here via TestRailAdapterParams.
 */
public class TestRailTestCasesAdapter implements TestCasesTrackerAdapter {

    private final TestRailClient client;
    private final TestRailAdapterParams config;

    public TestRailTestCasesAdapter(TestRailClient client, CustomTestCasesTrackerParams p) {
        this.client = client;
        this.config = new TestRailAdapterParams(p.getParams());
    }

    @Override
    public List<ITicket> getExistingCases() throws Exception {
        List<ITicket> result = new ArrayList<>();
        for (String projectName : config.getProjectNames()) {
            List<TestCase> cases = client.getAllCases(projectName);
            result.addAll(cases);
        }
        return result;
    }

    @Override
    public List<ITicket> getLinkedCases(String sourceTicketKey) throws Exception {
        List<ITicket> result = new ArrayList<>();
        for (String projectName : config.getProjectNames()) {
            List<TestCase> cases = client.getCasesByRefs(sourceTicketKey, projectName);
            result.addAll(cases);
        }
        return result;
    }

    @Override
    public ITicket createTestCase(TestCaseGeneratorAgent.TestCase testCase,
                                  String sourceTicketKey,
                                  TestCasesGeneratorParams params) throws IOException {
        String projectName = resolveTargetProject();
        String mode = config.getCreationMode();

        String typeId = config.getTypeId();
        if (typeId == null || typeId.isEmpty()) {
            String typeName = config.getTypeName();
            if (typeName != null && !typeName.isEmpty()) {
                typeId = client.resolveTypeIdByName(typeName);
            }
        }

        String labelIds = config.getLabelIds();
        if (labelIds == null || labelIds.isEmpty()) {
            String[] labelNames = config.getLabelNames();
            if (labelNames.length > 0) {
                labelIds = client.resolveLabelIdsByNames(projectName, labelNames);
            }
        }

        String priorityId = convertPriorityToId(testCase.getPriority());
        JSONObject customFields = testCase.getCustomFields() != null ? testCase.getCustomFields() : new JSONObject();

        String response;
        switch (mode.toLowerCase()) {
            case "detailed":
                response = client.createCaseDetailed(
                        projectName,
                        testCase.getSummary(),
                        customFields.optString("custom_preconds", null),
                        customFields.optString("custom_steps", null),
                        customFields.optString("custom_expected", null),
                        priorityId,
                        typeId,
                        sourceTicketKey,
                        labelIds
                );
                break;
            case "steps":
                String stepsJson = customFields.optString("custom_steps_json", null);
                if (stepsJson == null || stepsJson.isEmpty()) {
                    // AI may have used the actual TestRail field name instead of the virtual key
                    JSONArray stepsArray = customFields.optJSONArray("custom_steps_separated");
                    if (stepsArray != null) {
                        stepsJson = stepsArray.toString();
                    }
                }
                if (stepsJson == null || stepsJson.isEmpty()) {
                    // Final fallback: build a single step from description
                    stepsJson = new JSONArray()
                            .put(new JSONObject()
                                    .put("content", testCase.getDescription() != null ? testCase.getDescription() : "")
                                    .put("expected", ""))
                            .toString();
                }
                response = client.createCaseSteps(
                        projectName,
                        testCase.getSummary(),
                        customFields.optString("custom_preconds", null),
                        stepsJson,
                        priorityId,
                        typeId,
                        sourceTicketKey,
                        labelIds
                );
                break;
            default: // "simple" — use createCaseDetailed so typeId/labelIds are always applied
                response = client.createCaseDetailed(
                        projectName,
                        testCase.getSummary(),
                        testCase.getDescription(),
                        null,
                        null,
                        priorityId,
                        typeId,
                        sourceTicketKey,
                        labelIds
                );
                break;
        }
        return client.createTicket(response);
    }

    @Override
    public void linkToSource(String testCaseId, String sourceTicketKey, String relationship) throws IOException {
        String numericId = stripCPrefix(testCaseId);
        client.linkToRequirement(numericId, sourceTicketKey);
    }

    /**
     * Normalizes a raw key returned by the AI to TestRail's canonical "C{id}" format.
     * The AI often returns bare numeric IDs (e.g. {@code 1}) while {@code getKey()} returns {@code "C1"}.
     */
    @Override
    public String normalizeKeyFromAI(String raw) {
        try {
            Integer.parseInt(raw.trim());
            return "C" + raw.trim();
        } catch (NumberFormatException e) {
            return raw;
        }
    }

    // ---- private helpers ----

    private String resolveTargetProject() {
        String target = config.getTargetProject();
        if (target != null && !target.isEmpty()) return target;
        String[] names = config.getProjectNames();
        if (names.length > 0) return names[0];
        throw new IllegalStateException("No TestRail project configured in adapter params");
    }

    /**
     * Converts a priority string to a TestRail priority ID.
     * TestRail default priorities: 1=Low, 2=Medium, 3=High, 4=Critical.
     */
    private String convertPriorityToId(String priority) {
        if (priority == null) return "2";
        switch (priority.trim().toLowerCase()) {
            case "critical":
            case "highest":
                return "4";
            case "high":
                return "3";
            case "low":
            case "lowest":
                return "1";
            default:
                return "2";
        }
    }

    /**
     * Strips the "C" prefix from a TestRail case key (e.g. "C123" -> "123").
     */
    private String stripCPrefix(String testCaseId) {
        if (testCaseId != null && testCaseId.startsWith("C")) {
            return testCaseId.substring(1);
        }
        return testCaseId;
    }
}
