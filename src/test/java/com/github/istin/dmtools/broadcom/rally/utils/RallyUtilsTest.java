package com.github.istin.dmtools.broadcom.rally.utils;


import com.github.istin.dmtools.common.model.IHistoryItem;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class RallyUtilsTest {


    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testConvertRevisionDescriptionToHistoryItems() {
        List<IHistoryItem> results = RallyUtils.convertRevisionDescriptionToHistoryItems("SCHEDULE STATE changed from [Completed] to [Accepted], READY changed from [true] to [false], FLOW STATE CHANGED DATE changed from [Wed Dec 27 04:03:08 MST 2023] to [Thu Jan 04 01:29:50 MST 2024], FLOW STATE changed from [In Acceptance] to [Done], ACCEPTED DATE added [Thu Jan 04 01:29:50 MST 2024]");
        for (IHistoryItem historyItem : results) {
            System.out.println(historyItem.getField() + " " + historyItem.getFromAsString() + " " + historyItem.getToAsString());
        }
    }

    @Test
    public void testConvertRevisionDescriptionWithBlockedReasonToHistoryItems() {
        List<IHistoryItem> results = RallyUtils.convertRevisionDescriptionToHistoryItems("BLOCKED REASON changed from [Some blocked description] to [Some another blocker description], ITERATION changed from [24Q1 Sprint 3] to [24Q1 Sprint 4]");
        for (IHistoryItem historyItem : results) {
            System.out.println(historyItem.getField() + " : " + historyItem.getFromAsString() + " => " + historyItem.getToAsString());
        }
    }
}