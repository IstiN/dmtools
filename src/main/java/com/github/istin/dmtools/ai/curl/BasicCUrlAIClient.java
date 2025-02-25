package com.github.istin.dmtools.ai.curl;

import com.github.istin.dmtools.ai.AI;
import com.github.istin.dmtools.common.utils.PropertyReader;

import java.io.IOException;

public class BasicCUrlAIClient extends CUrlAIClient {

    private static CurlAIConfig DEFAULT_CONFIG;

    private CurlAIConfig config;

    static {
        PropertyReader propertyReader = new PropertyReader();
        DEFAULT_CONFIG = CurlAIConfig.builder()
                .model(propertyReader.getCurlAIModel())
                .basePath(propertyReader.getCurlAIBathPath())
                .auth(propertyReader.getCurlAIAuth())
                .curlUrlTemplate(propertyReader.getCurlAIUrlTemplate())
                .curlBodyTemplate(propertyReader.getCurlAIBodyTemplate())
                .curlBodyTemplateWithImage(propertyReader.getCurlAIBodyTemplateWithImage())
                .curlAiResponseJsonPath(propertyReader.getCurlAiResponseJsonPath())
                .build();
    }


    public BasicCUrlAIClient() throws IOException {
        this(DEFAULT_CONFIG);
    }

    public BasicCUrlAIClient(CurlAIConfig config) throws IOException {
        super(config.getBasePath(), config.getAuth(), config.getCurlUrlTemplate(), config.getCurlBodyTemplate(), config.getCurlBodyTemplateWithImage(), config.getCurlAiResponseJsonPath(), config.getModel());
        this.config = config;
    }


    private static BasicCUrlAIClient instance;

    public static AI getInstance() throws IOException {
        String curlUrlTemplate = DEFAULT_CONFIG.getCurlUrlTemplate();
        if (curlUrlTemplate == null || curlUrlTemplate.isEmpty()) {
            return null;
        }
        if (instance == null) {
            instance = new BasicCUrlAIClient();
        }
        return instance;
    }

}