package com.github.istin.dmtools.metrics;

import com.github.istin.dmtools.metrics.source.SourceCollector;
import com.github.istin.dmtools.report.model.KeyTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MetricTest {

    private Metric metric;
    private TrackerRule mockRule;
    private SourceCollector mockSourceCollector;
    private KeyTime mockKeyTime;

    @Before
    public void setUp() {
        mockRule = mock(TrackerRule.class);
        mockSourceCollector = mock(SourceCollector.class);
        mockKeyTime = mock(KeyTime.class);
        metric = new Metric("TestMetric", true, true, mockRule, mockSourceCollector);
    }

    @Test
    public void testGetName() {
        assertEquals("TestMetric", metric.getName());
    }

    @Test
    public void testGetRule() {
        assertEquals(mockRule, metric.getRule());
    }

    @Test
    public void testIsWeight() {
        assertTrue(metric.isWeight());
    }

    @Test
    public void testSetWeight() {
        metric.setWeight(false);
        assertFalse(metric.isWeight());
    }

    @Test
    public void testGetSourceCollector() {
        assertEquals(mockSourceCollector, metric.getSourceCollector());
    }

    @Test
    public void testPerform() throws Exception {
        List<KeyTime> keyTimes = Collections.singletonList(mockKeyTime);
        when(mockSourceCollector.performSourceCollection(true, "TestMetric")).thenReturn(keyTimes);
        when(mockKeyTime.getWho()).thenReturn("TestOwner");

        Map<String, Map<String, List<KeyTime>>> metricAndMap = new HashMap<>();
        Set<String> combinedPeople = new HashSet<>();

        metric.perform(metricAndMap, combinedPeople);

        assertTrue(metricAndMap.containsKey("TestMetric"));
        assertTrue(metricAndMap.get("TestMetric").containsKey("TestOwner"));
        assertEquals(1, metricAndMap.get("TestMetric").get("TestOwner").size());
        assertTrue(combinedPeople.contains("TestOwner"));
    }

    @Test
    public void testAddKeyTime() {
        Map<String, Map<String, List<KeyTime>>> metricAndMap = new HashMap<>();
        Set<String> combinedPeople = new HashSet<>();
        when(mockKeyTime.getWho()).thenReturn("TestOwner");

        metric.addKeyTime(metricAndMap, combinedPeople, mockKeyTime, "TestOwner");

        assertTrue(metricAndMap.containsKey("TestMetric"));
        assertTrue(metricAndMap.get("TestMetric").containsKey("TestOwner"));
        assertEquals(1, metricAndMap.get("TestMetric").get("TestOwner").size());
        assertTrue(combinedPeople.contains("TestOwner"));
    }
}