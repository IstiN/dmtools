package com.github.istin.dmtools.atlassian.bitbucket.model;

import com.github.istin.dmtools.atlassian.common.model.Assignee;
import com.github.istin.dmtools.common.model.JSONModel;
import com.github.istin.dmtools.common.utils.DateUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class Commit extends JSONModel {

    public Commit() {
    }

    public Commit(String json) throws JSONException {
        super(json);
    }

    public Commit(JSONObject json) {
        super(json);
    }

    public String getId() {
        return getString("id");
    }
    public String getMessage() {
        return getString("message");
    }

    public Assignee getAuthor() {
        JSONObject authorObject = getJSONObject("author");
        if (authorObject != null) {
            JSONObject userObject = authorObject.getJSONObject("user");
            if (userObject != null) {
                return new Assignee(userObject);
            }
        }
        return getModel(Assignee.class, "author");
    }

    public Long getCommiterTimestamp() {
        Long committerTimestamp = getLong("committerTimestamp");
        if (committerTimestamp == null) {
                String dateString = getString("date"); {
                return DateUtils.parseBitbucketDate(dateString).getTime();
            }
        }
        return committerTimestamp;
    }

    public Calendar getCommitterDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getCommiterTimestamp());
        return calendar;
    }

}