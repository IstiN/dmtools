package com.github.istin.dmtools.metrics;

import com.github.istin.dmtools.atlassian.jira.model.Ticket;
import com.github.istin.dmtools.common.code.SourceCode;
import com.github.istin.dmtools.common.tracker.TrackerClient;
import com.github.istin.dmtools.metrics.source.SourceCollector;
import com.github.istin.dmtools.report.model.KeyTime;
import com.github.istin.dmtools.team.IEmployees;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class CommonSourceCodeMetricTest {

    private CommonSourceCodeMetric commonSourceCodeMetric;
    private SourceCode mockSourceCode;
    private IEmployees mockEmployees;
    private SourceCollector mockSourceCollector;

    @Before
    public void setUp() {
        mockSourceCode = mock(SourceCode.class);
        mockEmployees = mock(IEmployees.class);
        mockSourceCollector = mock(SourceCollector.class);

        commonSourceCodeMetric = new CommonSourceCodeMetric(
                "TestMetric",
                true,
                "TestWorkspace",
                "TestRepo",
                mockSourceCode,
                mockEmployees,
                mockSourceCollector
        );
    }

    @Test
    public void testGetWorkspace() {
        assertEquals("TestWorkspace", commonSourceCodeMetric.getWorkspace());
    }

    @Test
    public void testGetRepo() {
        assertEquals("TestRepo", commonSourceCodeMetric.getRepo());
    }

    @Test
    public void testGetSourceCode() {
        assertEquals(mockSourceCode, commonSourceCodeMetric.getSourceCode());
    }

    @Test
    public void testGetEmployees() {
        assertEquals(mockEmployees, commonSourceCodeMetric.getEmployees());
    }

}