package com.github.istin.dmtools.openai.utils;

import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class AIResponseParserTest extends TestCase {

    public void testParseBooleanResponse() {
        try {
            assertTrue(AIResponseParser.parseBooleanResponse("... true ..."));
            assertFalse(AIResponseParser.parseBooleanResponse("... false ..."));

            try {
                AIResponseParser.parseBooleanResponse("... invalid ...");
                fail("Expected IllegalArgumentException for invalid boolean");
            } catch (IllegalArgumentException e) {
                // Expected exception
            }
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    public void testParseResponseAsJSONArray() {
        try {
            String response = "... [\"item1\", \"item2\"] ...";
            JSONArray jsonArray = AIResponseParser.parseResponseAsJSONArray(response);
            assertEquals(2, jsonArray.length());
            assertEquals("item1", jsonArray.getString(0));
            assertEquals("item2", jsonArray.getString(1));

            try {
                AIResponseParser.parseResponseAsJSONArray("invalid response");
                fail("Expected JSONException for invalid JSON array");
            } catch (JSONException e) {
                // Expected exception
            }
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    public void testParseResponseAsJSONObject() {
        try {
            String response = "... {\"key\": \"value\"} ...";
            JSONObject jsonObject = AIResponseParser.parseResponseAsJSONObject(response);
            assertEquals("value", jsonObject.getString("key"));

            try {
                AIResponseParser.parseResponseAsJSONObject("invalid response");
                fail("Expected JSONException for invalid JSON object");
            } catch (JSONException e) {
                // Expected exception
            }
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }
}