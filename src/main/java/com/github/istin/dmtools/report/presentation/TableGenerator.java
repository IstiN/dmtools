package com.github.istin.dmtools.report.presentation;

import com.github.istin.dmtools.common.model.ITicket;
import com.github.istin.dmtools.report.model.TableData;

import java.util.List;

public interface TableGenerator {
    String generateTable(TableData tableData);
    String generateTable(List<ITicket> tickets);
    String generateTable(List<ITicket> tickets, String[] columns, boolean includeDescription, boolean includeStoryPoints);
}