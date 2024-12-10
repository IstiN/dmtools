package com.github.istin.dmtools.atlassian.jira.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ComponentTest {

    private Component component;

    @Before
    public void setUp() {
        component = new Component();
    }

    @Test
    public void testGetId() {
        component.set("id", "123");
        assertEquals("123", component.getId());
    }

    @Test
    public void testGetName() {
        component.set("name", "TestComponent");
        assertEquals("TestComponent", component.getName());
    }

    @Test
    public void testSetName() {
        component.setName("NewName");
        assertEquals("NewName", component.getName());
    }

    @Test
    public void testConstructorWithJSONString() throws JSONException {
        String jsonString = "{\"id\":\"456\",\"name\":\"JSONComponent\"}";
        Component jsonComponent = new Component(jsonString);
        assertEquals("456", jsonComponent.getId());
        assertEquals("JSONComponent", jsonComponent.getName());
    }

    @Test
    public void testConstructorWithJSONObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", "789");
        jsonObject.put("name", "JSONObjectComponent");
        Component jsonComponent = new Component(jsonObject);
        assertEquals("789", jsonComponent.getId());
        assertEquals("JSONObjectComponent", jsonComponent.getName());
    }

    @Test
    public void testConstructorWithInvalidJSONString() {
        String invalidJsonString = "invalid json";
        assertThrows(JSONException.class, () -> new Component(invalidJsonString));
    }
}