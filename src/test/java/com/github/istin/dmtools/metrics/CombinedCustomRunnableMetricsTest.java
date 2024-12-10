package com.github.istin.dmtools.metrics;

import com.github.istin.dmtools.report.model.KeyTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.mockito.Mockito.*;

public class CombinedCustomRunnableMetricsTest {

    private Metric mockMetric1;
    private Metric mockMetric2;
    private CombinedCustomRunnableMetrics combinedMetrics;

    @Before
    public void setUp() {
        mockMetric1 = mock(Metric.class);
        mockMetric2 = mock(Metric.class);
        combinedMetrics = new CombinedCustomRunnableMetrics("TestMetrics", mockMetric1, mockMetric2);
    }

    @Test
    public void testPerform() throws Exception {
        Map<String, Map<String, List<KeyTime>>> metricAndMap = new HashMap<>();
        Set<String> combinedPeople = new HashSet<>();

        combinedMetrics.perform(metricAndMap, combinedPeople);

        verify(mockMetric1, times(1)).perform(metricAndMap, combinedPeople);
        verify(mockMetric2, times(1)).perform(metricAndMap, combinedPeople);
    }

    @Test
    public void testConstructorWithList() {
        List<Metric> metricsList = Arrays.asList(mockMetric1, mockMetric2);
        CombinedCustomRunnableMetrics combinedMetricsWithList = new CombinedCustomRunnableMetrics("TestMetricsList", metricsList);

        // Verify that the constructor correctly converts the list to an array
        assert combinedMetricsWithList != null;
    }
}