package com.github.istin.dmtools.job;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class AbstractJobTest {

    private static class TestJob extends AbstractJob<String> {
        @Override
        public void runJob(String s) throws Exception {

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
        AbstractJob<String> job = new TestJob();
        assertEquals(String.class, job.getParamsClass());
    }

    @Test
    public void testGetTemplateParameterClassThrowsException() {
        class InvalidJob extends AbstractJob {
            @Override
            public void runJob(Object o) throws Exception {

            }
            // No type parameter specified
        }
        InvalidJob job = new InvalidJob();
        assertThrows(IllegalArgumentException.class, job::getParamsClass);
    }
}