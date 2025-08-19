package com.github.istin.dmtools.auth.model.mcp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents MCP content structure for responses.
 */
public class McpContent {
    
    public static class TextContent {
        private final String type = "text";
        private final String text;
        
        public TextContent(String text) {
            this.text = text;
        }
        
        public JSONObject toJson() {
            return new JSONObject()
                    .put("type", type)
                    .put("text", text);
        }
    }
    
    public static class FileContent {
        private final String downloadUrl;
        private final String filename;
        private final String mimeType;
        private final String expiresIn;
        
        public FileContent(String downloadUrl, String filename, String mimeType, String expiresIn) {
            this.downloadUrl = downloadUrl;
            this.filename = filename;
            this.mimeType = mimeType;
            this.expiresIn = expiresIn;
        }
        
        public JSONObject toJson() {
            return new JSONObject()
                    .put("downloadUrl", downloadUrl)
                    .put("filename", filename)
                    .put("mimeType", mimeType)
                    .put("expiresIn", expiresIn);
        }
    }
    
    private final List<Object> contents = new ArrayList<>();
    
    public McpContent addText(String text) {
        contents.add(new TextContent(text));
        return this;
    }
    
    public McpContent addFile(String downloadUrl, String filename, String mimeType, String expiresIn) {
        contents.add(new FileContent(downloadUrl, filename, mimeType, expiresIn));
        return this;
    }
    
    public JSONArray toJsonArray() {
        JSONArray array = new JSONArray();
        for (Object content : contents) {
            if (content instanceof TextContent) {
                array.put(((TextContent) content).toJson());
            } else if (content instanceof FileContent) {
                array.put(((FileContent) content).toJson());
            }
        }
        return array;
    }
    
    public static McpContent text(String text) {
        return new McpContent().addText(text);
    }
    
    public static McpContent file(String downloadUrl, String filename, String mimeType, String expiresIn) {
        return new McpContent().addFile(downloadUrl, filename, mimeType, expiresIn);
    }
}

