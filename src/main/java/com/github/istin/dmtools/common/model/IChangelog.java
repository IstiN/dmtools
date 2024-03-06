package com.github.istin.dmtools.common.model;

import java.util.List;

public interface IChangelog {

    List<? extends IHistory> getHistories();

}
