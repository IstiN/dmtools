package com.github.istin.dmtools.di;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.ai.google.BasicGeminiAI;
import com.github.istin.dmtools.ai.js.JSAIClient;
import com.github.istin.dmtools.bridge.DMToolsBridge;
import com.github.istin.dmtools.common.config.ApplicationConfiguration;
import com.github.istin.dmtools.common.utils.SecurityUtils;
import com.github.istin.dmtools.ai.dial.BasicDialAI;
import com.github.istin.dmtools.prompt.PromptManager;
import com.github.istin.dmtools.prompt.IPromptTemplateReader;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import org.json.JSONObject;

import javax.script.ScriptException;
import freemarker.template.TemplateException;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Module
public class AIComponentsModule {

    private static final Logger logger = LogManager.getLogger(AIComponentsModule.class);

    @Provides
    @Singleton
    ConversationObserver provideConversationObserver() {
        return new ConversationObserver();
    }

    @Provides
    @Singleton
    DMToolsBridge provideDMToolsBridge() {
        return DMToolsBridge.withAllPermissions("DefaultBridge");
    }

    @Provides
    @Singleton
    AI provideAI(ConversationObserver observer, ApplicationConfiguration configuration) {
        // 1. Attempt to initialize AI via BasicGeminiAI if GEMINI_API_KEY is configured
        // Skip Gemini if DialAI is explicitly preferred or if Gemini has known issues
        String geminiApiKey = configuration.getGeminiApiKey();
        String dialApiKey = configuration.getDialApiKey();
        
        // Prefer Dial if both are available (to avoid Gemini location restrictions)
        if (dialApiKey != null && !dialApiKey.trim().isEmpty() && !dialApiKey.startsWith("$")) {
            try {
                logger.debug("Attempting to initialize AI via BasicDIAL as DIAL_API_KEY is set...");
                AI dial = new BasicDialAI(observer, configuration);
                logger.debug("BasicDIAL initialized successfully.");
                return dial;
            } catch (Exception e) {
                logger.error("Failed to initialize BasicDIAL, trying fallback options. Error: " + e.getMessage());
            }
        }
        
        // Only try Gemini if Dial is not available
        if (geminiApiKey != null && !geminiApiKey.trim().isEmpty() && !geminiApiKey.startsWith("$")) {
            try {
                logger.debug("Attempting to initialize AI via BasicGeminiAI as GEMINI_API_KEY is set...");
                AI geminiAI = BasicGeminiAI.create(observer, configuration);
                logger.debug("BasicGeminiAI initialized successfully.");
                return geminiAI;
            } catch (Exception e) {
                logger.error("Failed to initialize BasicGeminiAI, falling back. Error: " + e.getMessage());
                // Log details, but don't print stack trace to console in production, use a logger in real app
                // e.printStackTrace();
            }
        }

        // 2. Try to initialize JSAIClient using generic JSAI_SCRIPT_PATH if configured
        String jsScriptPath = configuration.getJsScriptPath();
        if (jsScriptPath != null && !jsScriptPath.trim().isEmpty()) {
            try {
                JSONObject configJson = new JSONObject();
                configJson.put("jsScriptPath", jsScriptPath);
                configJson.put("clientName", configuration.getJsClientName());

                String jsModel = configuration.getJsDefaultModel();
                configJson.put("defaultModel", jsModel != null ? jsModel : configuration.getDialModel());

                String jsBasePath = configuration.getJsBasePath();
                configJson.put("basePath", jsBasePath != null ? jsBasePath : configuration.getDialBathPath());

                JSONObject secretsJson = new JSONObject();
                String[] secretKeys = configuration.getJsSecretsKeys();
                if (secretKeys != null) {
                    for (String key : secretKeys) {
                        String value = configuration.getValue(key.trim());
                        if (value != null) {
                            secretsJson.put(key.trim(), value);
                        }
                    }
                }
                configJson.put("secrets", secretsJson);

                // Log the masked version to protect sensitive information
                JSONObject maskedConfig = SecurityUtils.maskSensitiveInformation(configJson);
                logger.debug("Attempting to initialize generic JSAIClient with config: " + maskedConfig.toString(2));
                AI jsAIClient = new JSAIClient(configJson, observer);
                logger.debug("Generic JSAIClient initialized successfully.");
                return jsAIClient;
            } catch (IOException | ScriptException | TemplateException e) {
                logger.error("Failed to initialize generic JSAIClient from properties, falling back. Error: " + e.getMessage());
                // e.printStackTrace();
            }
        }

        // 3. Fallback to AIProvider.getCustomAI()
        AI customAI = com.github.istin.dmtools.ai.AIProvider.getCustomAI();
        if (customAI != null) {
            logger.debug("Using Custom AI from AIProvider.");
            return customAI;
        }

        // 4. Default fallback to BasicDial
        try {
            logger.debug("Falling back to BasicDIAL.");
            AI dial = new BasicDialAI(observer, configuration);
            logger.debug("BasicDIAL initialized successfully.");
            return dial;
        } catch (IOException e) {
            logger.error("Failed to initialize BasicDIAL: " + e.getMessage());
            throw new RuntimeException("Failed to initialize BasicDIAL and no other AI clients were available.", e);
        }
    }

    @Provides
    @Singleton
    IPromptTemplateReader providePromptTemplateReader() {
        return new PromptManager();
    }

}
