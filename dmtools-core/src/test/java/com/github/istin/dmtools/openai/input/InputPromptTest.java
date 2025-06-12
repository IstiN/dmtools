package com.github.istin.dmtools.openai.input;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class InputPromptTest {

    @Test
    public void testGetInput() {
        String expectedInput = "Test Input";
        InputPrompt inputPrompt = new InputPrompt(expectedInput);
        assertEquals(expectedInput, inputPrompt.getInput());
    }

    @Test
    public void testSetInput() {
        String initialInput = "Initial Input";
        String newInput = "New Input";
        InputPrompt inputPrompt = new InputPrompt(initialInput);
        inputPrompt.setInput(newInput);
        assertEquals(newInput, inputPrompt.getInput());
    }
}