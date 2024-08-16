package com.github.istin.dmtools.gitlab.model;

import com.github.istin.dmtools.common.model.IFile;
import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

public class GitLabFile extends JSONModel implements IFile {

    public GitLabFile() {

    }

    public GitLabFile(String json) throws JSONException {
        super(json);
    }

    public GitLabFile(JSONObject json) {
        super(json);
    }

    @Override
    public String getPath() {
        return getString("path");
    }

    @Override
    public String getType() {
        //tree
        //blob
        return getString("type");
    }

    @Override
    public boolean isDir() {
        return getType().equals("tree");
    }

    @Override
    public String getSelfLink() {
        return getString("url");
    }

    private String fileContent;

    @Override
    public String getFileContent() {
        return fileContent;
    }

    @Override
    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }
}