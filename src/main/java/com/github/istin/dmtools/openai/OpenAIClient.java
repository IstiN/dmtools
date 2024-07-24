package com.github.istin.dmtools.openai;

import com.github.istin.dmtools.ai.ConversationObserver;
import com.github.istin.dmtools.common.networking.GenericRequest;
import com.github.istin.dmtools.common.utils.ImageUtils;
import com.github.istin.dmtools.networking.AbstractRestClient;
import com.github.istin.dmtools.openai.model.AIResponse;
import com.github.istin.dmtools.openai.model.Choice;
import okhttp3.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
        String path = path("openai/deployments/" + model + "/chat/completions");
        logger.info(path);
        GenericRequest postRequest = new GenericRequest(this, path);
//        postRequest.setIgnoreCache(true);

        JSONArray messages = new JSONArray();;
        if (imageFile != null) {
            String extension = ImageUtils.getExtension(imageFile);
            String imageBase64 = ImageUtils.convertToBase64(imageFile, "png");
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
        List<Choice> choices = new AIResponse(response).getChoices();
        String content;
        if (choices.isEmpty()) {
            if (response.contains("error")) {
                logger.error(response);
                content = response;
            } else {
                content = "";
            }
        } else {
            content = choices.get(0).getMessage().getContent();
        }
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

    @Override
    protected @NotNull String buildHashForPostRequest(GenericRequest genericRequest, String url) {
        String adjustedUrl =
                url.replaceAll("gpt-4-0125-preview", "gpt-4-turbo-2024-04-09")
                        .replaceAll("gpt-4-32k", "gpt-4-turbo-2024-04-09")
                        .replaceAll("gpt-4-1106-preview", "gpt-4-turbo-2024-04-09")
                        .replaceAll("gpt-35-turbo", "gpt-4-turbo-2024-04-09")
                ;
        return adjustedUrl + genericRequest.getBody();
    }

}
