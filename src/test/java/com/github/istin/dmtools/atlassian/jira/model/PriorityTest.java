package com.github.istin.dmtools.atlassian.jira.model;

import org.json.JSONObject;
import org.json.JSONException;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class PriorityTest {

    @Test
    public void testConstructorWithJSONString() throws JSONException {
        String jsonString = "{\"name\":\"High\",\"id\":\"1\"}";
        Priority priority = new Priority(jsonString);
        assertNotNull(priority);
        assertEquals("High", priority.getName());
        assertEquals(1, priority.getId());
    }

    @Test
    public void testConstructorWithJSONObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "Medium");
        jsonObject.put("id", "2");
        Priority priority = new Priority(jsonObject);
        assertNotNull(priority);
        assertEquals("Medium", priority.getName());
        assertEquals(2, priority.getId());
    }

    @Test
    public void testGetName() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "Low");
        Priority priority = new Priority(jsonObject);
        assertEquals("Low", priority.getName());
    }

    @Test
    public void testGetId() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", "3");
        Priority priority = new Priority(jsonObject);
        assertEquals(3, priority.getId());
    }

    @Test
    public void testCreatePostObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", "4");
        Priority priority = new Priority(jsonObject);
        JSONObject postObject = priority.createPostObject();
        assertNotNull(postObject);
        assertEquals("4", postObject.getString("id"));
    }
}