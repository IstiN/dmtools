package com.github.istin.dmtools.projectsetup;

import org.junit.Test;

import static org.junit.Assert.*;

public class ProjectSetupAnalysisJobParamsTest {

    @Test
    public void testDefaultConstructor() {
        ProjectSetupAnalysisJobParams params = new ProjectSetupAnalysisJobParams();
        assertNull(params.getProjectKey());
    }

    @Test
    public void testAllArgsConstructor() {
        ProjectSetupAnalysisJobParams params = new ProjectSetupAnalysisJobParams();
        params.setProjectKey("TEST");
        assertEquals("TEST", params.getProjectKey());
    }

    @Test
    public void testProjectKeySetterAndGetter() {
        ProjectSetupAnalysisJobParams params = new ProjectSetupAnalysisJobParams();
        params.setProjectKey("PROJ");
        assertEquals("PROJ", params.getProjectKey());
    }
}
