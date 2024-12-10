package com.github.istin.dmtools.metrics;

import com.github.istin.dmtools.report.model.KeyTime;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class RuleTest {

    @Test
    public void testCheckMethod() throws Exception {
        // Create a mock for the Rule interface
        Rule<Object, Object> ruleMock = Mockito.mock(Rule.class);

        // Define the behavior of the mock for the check method
        when(ruleMock.check(any(), any())).thenReturn(Mockito.mock(List.class));

        // Call the check method
        List<KeyTime> result = ruleMock.check(new Object(), new Object());

        // Verify the result is not null
        assertNotNull(result);

        // Verify that the check method was called once
        verify(ruleMock, times(1)).check(any(), any());
    }
}