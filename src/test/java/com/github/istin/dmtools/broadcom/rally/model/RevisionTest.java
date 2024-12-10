package com.github.istin.dmtools.broadcom.rally.model;

import com.github.istin.dmtools.broadcom.rally.utils.RallyUtils;
import com.github.istin.dmtools.common.model.IHistoryItem;
import com.github.istin.dmtools.common.model.IUser;
import com.github.istin.dmtools.common.utils.DateUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

public class RevisionTest {

    private Revision revision;
    private JSONObject mockJsonObject;

    @Before
    public void setUp() throws JSONException {
        mockJsonObject = mock(JSONObject.class);
        when(mockJsonObject.getString(anyString())).thenReturn("mockedString");
        revision = new Revision(mockJsonObject);
    }

    @Test
    public void testGetDescription() {
        String description = revision.getDescription();
        assertEquals("mockedString", description);
    }

    @Test
    public void testGetCreationDate() {
        Date mockDate = new Date();
        mockStatic(DateUtils.class);
        when(DateUtils.parseRallyDate(anyString())).thenReturn(mockDate);

        Date creationDate = revision.getCreationDate();
        assertEquals(mockDate, creationDate);

        verifyStatic(DateUtils.class);
        DateUtils.parseRallyDate(anyString());
    }


    @Test
    public void testGetHistoryItems() {
        List<IHistoryItem> mockHistoryItems = mock(List.class);
        mockStatic(RallyUtils.class);
        when(RallyUtils.convertRevisionDescriptionToHistoryItems(anyString())).thenReturn(mockHistoryItems);

        List<? extends IHistoryItem> historyItems = revision.getHistoryItems();
        assertEquals(mockHistoryItems, historyItems);

        verifyStatic(RallyUtils.class);
        RallyUtils.convertRevisionDescriptionToHistoryItems(anyString());
    }

}