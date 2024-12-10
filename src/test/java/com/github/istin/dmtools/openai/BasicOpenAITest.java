package com.github.istin.dmtools.openai;

import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.common.utils.PropertyReader;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BasicOpenAITest {

    private static PropertyReader propertyReaderMock;

    @BeforeClass
    public static void setUpClass() {
        propertyReaderMock = mock(PropertyReader.class);
        when(propertyReaderMock.getOpenAIBathPath()).thenReturn("mockBasePath");
        when(propertyReaderMock.getOpenAIApiKey()).thenReturn("mockApiKey");
        when(propertyReaderMock.getOpenAIModel()).thenReturn("mockModel");
    }

    @Test
    public void testStaticInitialization() {
        assertNotNull(BasicOpenAI.BASE_PATH);
        assertNotNull(BasicOpenAI.API_KEY);
        assertNotNull(BasicOpenAI.MODEL);
    }

    @Test
    public void testConstructorWithoutObserver() throws IOException {
        BasicOpenAI basicOpenAI = new BasicOpenAI();
        assertNotNull(basicOpenAI);
    }

    @Test
    public void testConstructorWithObserver() throws IOException {
        ConversationObserver observer = mock(ConversationObserver.class);
        BasicOpenAI basicOpenAI = new BasicOpenAI(observer);
        assertNotNull(basicOpenAI);
    }
}