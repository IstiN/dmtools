package com.github.istin.dmtools.openai;

import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.common.networking.GenericRequest;
import com.github.istin.dmtools.common.utils.ImageUtils;
import com.github.istin.dmtools.networking.AbstractRestClient;
import com.github.istin.dmtools.openai.model.AIResponse;
import okhttp3.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class OpenAIClient extends AbstractRestClient {

    private static final Logger logger = LogManager.getLogger(OpenAIClient.class);

    public static final String API_VERSION = "2023-07-01-preview";

    private String model;

    public ConversationObserver getConversationObserver() {
        return conversationObserver;
    }

    public void setConversationObserver(ConversationObserver conversationObserver) {
        this.conversationObserver = conversationObserver;
    }

    private ConversationObserver conversationObserver;

    public OpenAIClient(String basePath, String authorization, String model) throws IOException {
        this(basePath, authorization, model, null);
    }

    public OpenAIClient(String basePath, String authorization, String model, ConversationObserver conversationObserver) throws IOException {
        super(basePath, authorization);
        this.model = model;
        this.conversationObserver = conversationObserver;
        setCachePostRequestsEnabled(true);
    }

    public String getName() {
        return model;
    }

    @Override
    public String path(String path) {
        return getBasePath() + path + "?api-version=" + API_VERSION;
    }

    @Override
    public Request.Builder sign(Request.Builder builder) {
        return builder
                .header("api-key", authorization)
                .header("Content-Type", "application/json");
    }

    @Override
    public int getTimeout() {
        return 700;
    }

    public String chat(String message) throws Exception {
        return chat(model, message);
    }

    public String chat(String model, String message, File imageFile) throws Exception {
        logger.info("-------- message to ai --------");
        logger.info(message);
        logger.info("-------- start chat ai --------");
        if (conversationObserver != null) {
            conversationObserver.addMessage(new ConversationObserver.Message("DMTools", message));
        }
        GenericRequest postRequest = new GenericRequest(this, path("openai/deployments/" + model + "/chat/completions"));
//        postRequest.setIgnoreCache(true);

        JSONArray messages = new JSONArray();;
        if (imageFile != null) {
            String extension = ImageUtils.getExtension(imageFile);
            String imageBase64 = ImageUtils.convertToBase64(imageFile, extension);
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", new JSONArray()
                            .put(new JSONObject()
                                    .put("type", "text")
                                    .put("text", message)
                            )
                            .put(new JSONObject()
                                    .put("type", "image_url")
                                    .put("image_url", new JSONObject()
                                            .put("url", "data:image/"+extension+";base64," + imageBase64))
                            )
                    ));
        } else {
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", message));
        }
        postRequest.setBody(new JSONObject()
                .put("temperature", 0.1)
                .put("messages", messages).toString());
        String response = post(postRequest);
        logger.info(response);
        String content = new AIResponse(response).getChoices().get(0).getMessage().getContent();
        if (conversationObserver != null) {
            conversationObserver.addMessage(new ConversationObserver.Message(model, content));
        }
        logger.info("-------- ai response --------");
        logger.info(content);
        logger.info("-------- end chat ai --------");
        return content;
    }

    public String chat(String model, String message) throws Exception {
        return chat(model, message, null);
    }

}