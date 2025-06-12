package com.github.istin.dmtools.atlassian.confluence.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class StorageTest {

    @Test
    public void testDefaultConstructor() {
        Storage storage = new Storage();
        assertNotNull(storage);
    }

    @Test
    public void testConstructorWithJSONString() {
        String jsonString = "{\"value\":\"testValue\"}";
        try {
            Storage storage = new Storage(jsonString);
            assertNotNull(storage);
            assertEquals("testValue", storage.getValue());
        } catch (JSONException e) {
            fail("JSONException should not be thrown");
        }
    }

    @Test
    public void testConstructorWithJSONObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("value", "testValue");
        Storage storage = new Storage(jsonObject);
        assertNotNull(storage);
        assertEquals("testValue", storage.getValue());
    }

    @Test
    public void testGetValue() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("value", "testValue");
        Storage storage = new Storage(jsonObject);
        assertEquals("testValue", storage.getValue());
    }
}