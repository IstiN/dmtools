package com.github.istin.dmtools.atlassian.bitbucket.model;

import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class File extends JSONModel {

    public File() {
    }

    public File(String json) throws JSONException {
        super(json);
    }

    public File(JSONObject json) {
        super(json);
    }

    public String getPath() {
        return getString("path");
    }

    public String getType() {
        //commit_directory
        //commit_file
        return getString("type");
    }

    public boolean isDir() {
        return getType().equals("commit_directory");
    }

    public String getSelfLink() {
        return getJSONObject("links").getJSONObject("self").getString("href");
    }

    private String fileContent;

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }
}