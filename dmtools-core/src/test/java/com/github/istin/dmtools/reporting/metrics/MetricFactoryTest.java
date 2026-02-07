package com.github.istin.dmtools.reporting.metrics;

import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.metrics.Metric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MetricFactory
 * Tests metric creation from configuration
 */
@ExtendWith(MockitoExtension.class)
class MetricFactoryTest {

    @Mock
    private TrackerClient mockTrackerClient;

    @Mock
    private SourceCode mockSourceCode;

    private MetricFactory factory;

    @BeforeEach
    void setUp() {
        factory = new MetricFactory(mockTrackerClient, mockSourceCode);
    }

    @Test
    void testCreateTicketMovedToStatusRule_singleStatus() throws Exception {
        // Given: Configuration for TicketMovedToStatusRule with single status
        Map<String, Object> params = new HashMap<>();
        params.put("statuses", List.of("Done"));
        params.put("label", "Completed Tickets");
        params.put("isWeight", true);

        // When: Create metric
        Metric metric = factory.createMetric("TicketMovedToStatusRule", params, "tracker");

        // Then: Metric should be created with correct properties
        assertNotNull(metric, "Metric should not be null");
        assertEquals("Completed Tickets", metric.getName(), "Metric name should match label");
        assertTrue(metric.isWeight(), "Metric should have isWeight=true");
        assertNotNull(metric.getRule(), "Metric should have a rule");
    }

    @Test
    void testCreateTicketMovedToStatusRule_multipleStatuses() throws Exception {
        // Given: Configuration with multiple statuses
        Map<String, Object> params = new HashMap<>();
        params.put("statuses", List.of("In Progress", "Pull Request", "Review", "Done"));
        params.put("label", "All Active Statuses");
        params.put("isWeight", false);

        // When
        Metric metric = factory.createMetric("TicketMovedToStatusRule", params, "tracker");

        // Then
        assertNotNull(metric);
        assertEquals("All Active Statuses", metric.getName());
        assertFalse(metric.isWeight());
        assertNotNull(metric.getRule());
    }

    @Test
    void testCreateBugsCreatorsRule() throws Exception {
        // Given: Configuration for BugsCreatorsRule
        Map<String, Object> params = new HashMap<>();
        params.put("project", "DMC");
        params.put("label", "Bugs Created");
        params.put("isWeight", false);

        // When
        Metric metric = factory.createMetric("BugsCreatorsRule", params, "tracker");

        // Then
        assertNotNull(metric);
        assertEquals("Bugs Created", metric.getName());
        assertFalse(metric.isWeight());
        assertNotNull(metric.getRule());
    }

    @Test
    void testCreatePullRequestsMetricSource() throws Exception {
        // Given: Configuration for PullRequestsMetricSource
        Map<String, Object> params = new HashMap<>();
        params.put("workspace", "IstiN");
        params.put("repository", "dmtools");
        params.put("since", "2024-01-01");
        params.put("label", "Pull Requests");
        params.put("isWeight", false);
        params.put("isPersonalized", true);

        // When
        Metric metric = factory.createMetric("PullRequestsMetricSource", params, "pullRequests");

        // Then
        assertNotNull(metric);
        assertEquals("Pull Requests", metric.getName());
        assertFalse(metric.isWeight());
        assertTrue(metric.isPersonalized());
        assertNotNull(metric.getSourceCollector());
    }

    @Test
    void testCreateMetric_unknownDataSourceType_shouldThrowException() {
        // Given: Unknown data source type
        Map<String, Object> params = new HashMap<>();
        params.put("label", "Unknown Metric");

        // When/Then: Should throw IllegalArgumentException
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> factory.createMetric("SomeMetric", params, "unknownSource")
        );

        assertTrue(exception.getMessage().contains("Unknown data source type"));
    }

    @Test
    void testCreateMetric_unknownTrackerRule_shouldThrowException() {
        // Given: Unknown tracker rule name
        Map<String, Object> params = new HashMap<>();
        params.put("label", "Unknown Rule");

        // When/Then: Should throw IllegalArgumentException
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> factory.createMetric("UnknownRule", params, "tracker")
        );

        assertTrue(exception.getMessage().contains("Unknown tracker rule"));
    }

    @Test
    void testCreateMetric_statusesAsArray() throws Exception {
        // Given: Statuses as array (from JSON deserialization)
        Map<String, Object> params = new HashMap<>();
        params.put("statuses", new String[]{"Done", "Closed"});
        params.put("label", "Finished");
        params.put("isWeight", true);

        // When
        Metric metric = factory.createMetric("TicketMovedToStatusRule", params, "tracker");

        // Then
        assertNotNull(metric);
        assertEquals("Finished", metric.getName());
        assertTrue(metric.isWeight());
    }

    @Test
    void testCreateMetric_defaultValues() throws Exception {
        // Given: Minimal configuration (using defaults)
        Map<String, Object> params = new HashMap<>();
        params.put("statuses", List.of("Done"));
        // No label - should use metric name
        // No isWeight - should default to false

        // When
        Metric metric = factory.createMetric("TicketMovedToStatusRule", params, "tracker");

        // Then
        assertNotNull(metric);
        // Label defaults to metric name
        assertEquals("TicketMovedToStatusRule", metric.getName());
        // isWeight defaults to false
        assertFalse(metric.isWeight());
    }

    @Test
    void testCreateMetric_withLabelOverride() throws Exception {
        // Given: Configuration with custom label
        Map<String, Object> params = new HashMap<>();
        params.put("statuses", List.of("Done"));
        params.put("label", "Custom Label");
        params.put("isWeight", true);

        // When
        Metric metric = factory.createMetric("TicketMovedToStatusRule", params, "tracker");

        // Then
        assertEquals("Custom Label", metric.getName(), "Label should override metric name");
    }

    @Test
    void testCreateMetric_pullRequests_requiresSourceCode() {
        // Given: PullRequestsMetricSource without SourceCode configured
        MetricFactory factoryWithoutSourceCode = new MetricFactory(mockTrackerClient, null);
        Map<String, Object> params = new HashMap<>();
        params.put("label", "PRs");

        // When/Then: Should throw exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> factoryWithoutSourceCode.createMetric("PullRequestsMetricSource", params, "pullRequests")
        );

        assertTrue(exception.getMessage().contains("SourceCode is not configured"));
    }

    @Test
    void testParseDateParam_validDate() throws Exception {
        // This is testing internal behavior - we verify it works via PullRequestsMetricSource
        Map<String, Object> params = new HashMap<>();
        params.put("workspace", "test");
        params.put("repository", "repo");
        params.put("since", "2024-01-01");
        params.put("label", "PRs");
        params.put("isWeight", false);
        params.put("isPersonalized", false);

        // When: Create metric with date parameter
        Metric metric = factory.createMetric("PullRequestsMetricSource", params, "pullRequests");

        // Then: Should not throw exception - date was parsed successfully
        assertNotNull(metric);
    }
}
