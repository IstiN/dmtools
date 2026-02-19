package com.github.istin.dmtools.qa;

import com.github.istin.dmtools.ai.agent.TestCaseGeneratorAgent;
import com.github.istin.dmtools.common.model.ITicket;

import java.io.IOException;
import java.util.List;

/**
 * Adapter interface for test cases tracker operations in TestCasesGenerator.
 * Implementations allow using a separate system (e.g., TestRail) for test case
 * read/create/link operations while the source stories remain in Jira/ADO.
 *
 * <p>The adapter owns all knowledge of its own configuration (project names,
 * creation mode, labels, etc.). The generator passes only what it naturally
 * knows: the source ticket key and the AI-generated test case data.</p>
 */
public interface TestCasesTrackerAdapter {

    /**
     * Fetch all test cases from the adapter's configured projects.
     */
    List<ITicket> getExistingCases() throws Exception;

    /**
     * Get cases already linked to a source ticket key.
     */
    List<ITicket> getLinkedCases(String sourceTicketKey) throws Exception;

    /**
     * Create a single test case and return the created ITicket.
     *
     * @param testCase        AI-generated test case data
     * @param sourceTicketKey source story key used for initial linking (e.g. "PROJ-123")
     * @param params          generator params
     */
    ITicket createTestCase(TestCaseGeneratorAgent.TestCase testCase,
                           String sourceTicketKey,
                           TestCasesGeneratorParams params) throws IOException;

    /**
     * Link a test case to a source story/requirement.
     */
    void linkToSource(String testCaseId, String sourceTicketKey, String relationship) throws IOException;

    /**
     * Normalize a raw key returned by the AI to the adapter's canonical key format.
     * Default implementation returns the value unchanged (suitable for Jira-style keys).
     * Override for trackers whose AI responses differ from {@code ITicket.getKey()},
     * e.g. TestRail where the AI returns {@code 1} but {@code getKey()} returns {@code "C1"}.
     */
    default String normalizeKeyFromAI(String raw) {
        return raw;
    }
}
