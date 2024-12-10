package com.github.istin.dmtools.atlassian.confluence.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class AttachmentTest {

    @Test
    public void testDefaultConstructor() {
        Attachment attachment = new Attachment();
        assertNotNull("Attachment object should not be null", attachment);
    }

    @Test
    public void testConstructorWithJSONString() {
        String jsonString = "{\"title\":\"Sample Title\"}";
        try {
            Attachment attachment = new Attachment(jsonString);
            assertNotNull("Attachment object should not be null", attachment);
            assertEquals("Sample Title", attachment.getTitle());
        } catch (JSONException e) {
            fail("JSONException should not be thrown");
        }
    }

    @Test
    public void testConstructorWithJSONObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", "Sample Title");
        Attachment attachment = new Attachment(jsonObject);
        assertNotNull("Attachment object should not be null", attachment);
        assertEquals("Sample Title", attachment.getTitle());
    }

    @Test
    public void testGetTitle() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", "Sample Title");
        Attachment attachment = new Attachment(jsonObject);
        assertEquals("Sample Title", attachment.getTitle());
    }
}