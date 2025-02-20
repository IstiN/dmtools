package com.github.istin.dmtools.ai.agent;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class SummaryContextAgentTest {

    SummaryContextAgent agent;

    @Before
    public void setUp() throws Exception {
        agent = new SummaryContextAgent();
    }

    @Test
    public void testDemoPageSetValue() throws Exception {
        String result = agent.run(new SummaryContextAgent.Params(
                "Some task Details",
                "any raw data to assess\n"
        ));
        assertFalse(result.isEmpty());
    }

}
