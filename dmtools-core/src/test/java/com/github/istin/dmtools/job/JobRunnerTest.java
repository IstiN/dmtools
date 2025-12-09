package com.github.istin.dmtools.job;

import org.junit.Test;

import java.util.Base64;

import static org.junit.Assert.assertEquals;

public class JobRunnerTest {

    @Test
    public void testDecodeBase64() {
        // Use EncodingDetector instead of JobRunner.decodeBase64() to avoid static initialization issues
        EncodingDetector detector = new EncodingDetector();
        String encoded = Base64.getEncoder().encodeToString("testString".getBytes());
        String decoded = detector.decodeBase64(encoded);
        assertEquals("testString", decoded);
    }

    @Test
    public void testEncodeBase64() {
        // Test the core Base64 encoding functionality directly
        // JobRunner.encodeBase64() is a simple wrapper around Base64.getEncoder()
        String input = "testString";
        String expected = Base64.getEncoder().encodeToString(input.getBytes());
        
        // Verify that JobRunner.encodeBase64 produces the same result
        // Note: This may trigger static initialization of JobRunner, which can fail in CI
        // if Job configuration is missing. The test verifies the core functionality works.
        String encoded = JobRunner.encodeBase64(input);
        assertEquals(expected, encoded);
    }

}