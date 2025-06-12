package com.github.istin.dmtools.common.model;

import java.util.Calendar;
import java.util.List;

public interface IHistory {

    List<? extends IHistoryItem> getHistoryItems();

    IUser getAuthor();

    Calendar getCreated();

}
