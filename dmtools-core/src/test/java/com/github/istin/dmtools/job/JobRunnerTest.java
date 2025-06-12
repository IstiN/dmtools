package com.github.istin.dmtools.job;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Base64;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class JobRunnerTest {

    @Test
    public void testDecodeBase64() {
        String encoded = Base64.getEncoder().encodeToString("testString".getBytes());
        String decoded = JobRunner.decodeBase64(encoded);
        assertEquals("testString", decoded);
    }

    @Test
    public void testEncodeBase64() {
        String encoded = JobRunner.encodeBase64("testString");
        String expected = Base64.getEncoder().encodeToString("testString".getBytes());
        assertEquals(expected, encoded);
    }

}