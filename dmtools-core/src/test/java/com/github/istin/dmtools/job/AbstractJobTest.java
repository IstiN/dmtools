package com.github.istin.dmtools.job;

import com.github.istin.dmtools.ai.AI;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class AbstractJobTest {

    private static class TestJob extends AbstractJob<String, String> {
        @Override
        public String runJob(String s) throws Exception {
            return "success";
        }

        @Override
        public AI getAi() {
            return null;
        }
        // No additional implementation needed for testing
    }

    @Test
    public void testGetName() {
        TestJob job = new TestJob();
        String expectedName = "TestJob";
        assertEquals(expectedName, job.getName());
    }

    @Test
    public void testGetParamsClass() {
        TestJob job = new TestJob();
        assertEquals(String.class, job.getParamsClass());
    }

    @Test
    public void testGetTemplateParameterClass() {
        AbstractJob<String, String> job = new TestJob();
        assertEquals(String.class, job.getParamsClass());
    }

    @Test
    public void testGetTemplateParameterClassThrowsException() {
        class InvalidJob extends AbstractJob {
            @Override
            public Object runJob(Object o) throws Exception {
                return "success";
            }

            @Override
            public AI getAi() {
                return null;
            }
            // No type parameter specified
        }
        InvalidJob job = new InvalidJob();
        assertThrows(IllegalArgumentException.class, job::getParamsClass);
    }
}