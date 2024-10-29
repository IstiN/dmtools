package com.github.istin.dmtools.atlassian.jira.model;

import com.github.istin.dmtools.atlassian.common.model.Assignee;
import com.github.istin.dmtools.common.model.IComment;
import com.github.istin.dmtools.common.model.IUser;
import com.github.istin.dmtools.common.model.JSONModel;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Comment extends JSONModel implements IComment {

    private static final String ID = "id";
    private static final String AUTHOR = "author";
    public static final String BODY = "body";


    public Comment() {
    }

    public Comment(String json) throws JSONException {
        super(json);
    }

    public Comment(JSONObject json) {
        super(json);
    }


    @Override
    public String getId() {
        return getString(ID);
    }

    @Override
    public Date getCreated() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getBody() {
        return getString(BODY);
    }

    @Override
    public IUser getAuthor() {
        return getModel(Assignee.class, AUTHOR);
    }

}