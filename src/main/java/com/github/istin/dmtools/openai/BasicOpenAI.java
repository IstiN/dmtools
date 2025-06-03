
package com.github.istin.dmtools.openai;

import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.common.utils.PropertyReader;

import java.io.IOException;

public class BasicOpenAI extends OpenAIClient {
    public static final String BASE_PATH;
    public static final String API_KEY;

    public static final String MODEL;

    static {
        PropertyReader propertyReader = new PropertyReader();
        BASE_PATH = propertyReader.getOpenAIBathPath();
        API_KEY = propertyReader.getOpenAIApiKey();
        MODEL = propertyReader.getOpenAIModel();
    }

    public BasicOpenAI() throws IOException {
        this(null);
    }

    public BasicOpenAI(ConversationObserver conversationObserver) throws IOException {
        super(BASE_PATH, API_KEY, MODEL, conversationObserver);
    }

}