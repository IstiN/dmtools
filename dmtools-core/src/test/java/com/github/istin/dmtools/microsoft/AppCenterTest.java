package com.github.istin.dmtools.microsoft;

import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;

public class AppCenterTest {

    private AppCenter appCenter;

    @Before
    public void setUp() {
        appCenter = new AppCenter("testOrg", "testToken");
    }


    @Test
    public void testConvertGroupName() throws UnsupportedEncodingException {
        String encodedName = AppCenter.convertGroupName("test group");
        assertEquals("test%20group", encodedName);
    }

    @Test
    public void testMembersUrl() throws UnsupportedEncodingException {
        String url = appCenter.membersUrl("test group");
        assertEquals("https://api.appcenter.ms/v0.1/orgs/testOrg/distribution_groups/test%20group/members", url);
    }

    @Test
    public void testMembersUrlWithApp() throws UnsupportedEncodingException {
        String url = appCenter.membersUrl("test group", "testApp");
        assertEquals("https://api.appcenter.ms/v0.1/apps/testOrg/testApp/distribution_groups/test%20group/members", url);
    }

    @Test
    public void testAppsUrl() throws UnsupportedEncodingException {
        String url = appCenter.appsUrl("test group");
        assertEquals("https://api.appcenter.ms/v0.1/orgs/testOrg/distribution_groups/test%20group/apps", url);
    }

    @Test
    public void testConvertVersionLinksToDownloadLinks() {
        String input = "https://appcenter.ms/orgs/testOrg/apps/testApp/distribute/releases/1";
        String expected = "https://install.appcenter.ms/orgs/testOrg/apps/testApp/releases/1";
        String result = appCenter.convertVersionLinksToDownloadLinks(input);
        assertEquals(expected, result);
    }

    @Test
    public void testGetBaseUrlWebsite() {
        assertEquals("https://appcenter.ms/orgs/testOrg/apps/", appCenter.getBaseUrlWebsite());
    }

    @Test
    public void testGetBaseUrlInstallWebsite() {
        assertEquals("https://install.appcenter.ms/orgs/testOrg/apps/", appCenter.getBaseUrlInstallWebsite());
    }
}