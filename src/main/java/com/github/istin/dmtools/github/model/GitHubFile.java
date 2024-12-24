package com.github.istin.dmtools.github.model;

import com.github.istin.dmtools.common.model.IFile;
import com.github.istin.dmtools.common.model.ITextMatch;
import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class GitHubFile extends JSONModel implements IFile {

    public GitHubFile() {

    }

    public GitHubFile(String json) throws JSONException {
        super(json);
    }

    public GitHubFile(JSONObject json) {
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

    @Override
    public List<ITextMatch> getTextMatches() {
        return getModels(GitHubTextMatch.class, "text_matches");
    }
}