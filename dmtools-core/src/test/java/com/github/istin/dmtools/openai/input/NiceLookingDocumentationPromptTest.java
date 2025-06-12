package com.github.istin.dmtools.openai.input;

import com.github.istin.dmtools.common.model.ToText;
import com.github.istin.dmtools.common.utils.HtmlCleaner;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class NiceLookingDocumentationPromptTest {

    private NiceLookingDocumentationPrompt prompt;
    private ToText mockToText;
    private String basePath = "base/path";
    private String existingContent = "<html>Existing Content</html>";

    @Before
    public void setUp() {
        mockToText = mock(ToText.class);
        prompt = new NiceLookingDocumentationPrompt(basePath, mockToText, existingContent);
    }

    @Test
    public void testGetExistingContent() {
        String expectedContent = HtmlCleaner.cleanAllHtmlTags(basePath, existingContent);
        String actualContent = prompt.getExistingContent();
        assertEquals(expectedContent, actualContent);
    }

    @Test
    public void testConstructorInitialization() {
        assertEquals(HtmlCleaner.cleanAllHtmlTags(basePath, existingContent), prompt.getExistingContent());
    }
}