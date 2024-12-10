package com.github.istin.dmtools.common.utils;

import org.junit.Test;
import org.mockito.Mockito;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class StringUtilsTest {

    @Test
    public void testExtractUrls() {
        String text = "Check out this link: https://example.com and this one: http://example.org";
        List<String> urls = StringUtils.extractUrls(text);
        assertEquals(2, urls.size());
        assertTrue(urls.contains("https://example.com"));
        assertTrue(urls.contains("http://example.org"));
    }

    @Test
    public void testExtractUrlsWithNullInput() {
        List<String> urls = StringUtils.extractUrls(null);
        assertNotNull(urls);
        assertTrue(urls.isEmpty());
    }

    @Test
    public void testConvertToMarkdown() {
        String htmlInput = "<p>This is <strong>bold</strong> and <em>italic</em> text.</p>";
        String expectedMarkdown = "This is *bold* and _italic_ text.";
        String markdown = StringUtils.convertToMarkdown(htmlInput);
        assertEquals(expectedMarkdown, markdown);
    }

    @Test
    public void testConcatenate() {
        String result = StringUtils.concatenate(", ", "one", "two", "three");
        assertEquals("one, two, three", result);
    }

    @Test
    public void testConcatenateWithSingleValue() {
        String result = StringUtils.concatenate(", ", "single");
        assertEquals("single", result);
    }


    @Test
    public void testTransformJSONToTextWithArray() {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put("item1");
        jsonArray.put("item2");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("items", jsonArray);

        StringBuilder textBuilder = new StringBuilder();
        StringUtils.transformJSONToText(textBuilder, jsonObject, false);

        String expectedText = "items: [item1, item2]\n";
        assertEquals(expectedText, textBuilder.toString());
    }
}