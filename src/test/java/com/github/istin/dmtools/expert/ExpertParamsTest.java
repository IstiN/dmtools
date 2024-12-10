package com.github.istin.dmtools.expert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.json.JSONObject;
import org.json.JSONException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class ExpertParamsTest {

    private ExpertParams expertParams;

    @Before
    public void setUp() {
        expertParams = new ExpertParams();
    }

    @Test
    public void testGetProjectContext() {
        expertParams.setProjectContext("TestProjectContext");
        assertEquals("TestProjectContext", expertParams.getProjectContext());
    }

    @Test
    public void testGetRequest() {
        expertParams.setRequest("TestRequest");
        assertEquals("TestRequest", expertParams.getRequest());
    }

    @Test
    public void testSetProjectContext() {
        expertParams.setProjectContext("NewProjectContext");
        assertEquals("NewProjectContext", expertParams.getProjectContext());
    }

    @Test
    public void testSetRequest() {
        expertParams.setRequest("NewRequest");
        assertEquals("NewRequest", expertParams.getRequest());
    }

    @Test
    public void testGetOutputTypeDefault() {
        assertEquals(ExpertParams.OUTPUT_TYPE_COMMENT, expertParams.getOutputType());
    }

    @Test
    public void testGetOutputType() {
        expertParams.setOutputType(ExpertParams.OUTPUT_TYPE_FIELD);
        assertEquals(ExpertParams.OUTPUT_TYPE_FIELD, expertParams.getOutputType());
    }

    @Test
    public void testSetOutputType() {
        expertParams.setOutputType("CustomOutputType");
        assertEquals("CustomOutputType", expertParams.getOutputType());
    }

    @Test
    public void testGetFieldName() {
        expertParams.setFieldName("TestFieldName");
        assertEquals("TestFieldName", expertParams.getFieldName());
    }

    @Test
    public void testSetFieldName() {
        expertParams.setFieldName("NewFieldName");
        assertEquals("NewFieldName", expertParams.getFieldName());
    }

    @Test
    public void testConstructorWithJsonString() throws JSONException {
        String jsonString = "{\"projectContext\":\"JsonProjectContext\",\"request\":\"JsonRequest\"}";
        ExpertParams params = new ExpertParams(jsonString);
        assertEquals("JsonProjectContext", params.getProjectContext());
        assertEquals("JsonRequest", params.getRequest());
    }

    @Test
    public void testConstructorWithJsonObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("projectContext", "JsonObjectProjectContext");
        jsonObject.put("request", "JsonObjectRequest");
        ExpertParams params = new ExpertParams(jsonObject);
        assertEquals("JsonObjectProjectContext", params.getProjectContext());
        assertEquals("JsonObjectRequest", params.getRequest());
    }
}