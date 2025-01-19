package com.github.istin.dmtools;

import com.github.istin.dmtools.ai.agent.TestCaseGeneratorAgent;
import com.github.istin.dmtools.common.utils.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class TestCaseGeneratorAgentTest {

    TestCaseGeneratorAgent agent;

    @Before
    public void setUp() throws Exception {
        agent = new TestCaseGeneratorAgent();
    }

    @Test
    public void testDemoPageSetValue() throws Exception {
        List<TestCaseGeneratorAgent.TestCase> result = agent.run(new TestCaseGeneratorAgent.Params(
                "Critical, Major, Minor",
                "",
                "There is share button via clicking that app must to share app url to available services on mobile device",
                ""
        ));
        for (TestCaseGeneratorAgent.TestCase testCase : result) {
            System.out.println(StringUtils.convertToMarkdown(testCase.getDescription()));
        }
    }

}
