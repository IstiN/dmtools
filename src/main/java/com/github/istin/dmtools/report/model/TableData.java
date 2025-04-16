package com.github.istin.dmtools.report.model;

import java.util.ArrayList;
import java.util.List;

public class TableData {
    private final String title;
    private final List<String> headers;
    private final List<List<String>> rows;
    private final String description;

    public TableData(String title, List<String> headers) {
        this(title, headers, null);
    }

    public TableData(String title, List<String> headers, String description) {
        this.title = title;
        this.headers = headers;
        this.description = description;
        this.rows = new ArrayList<>();
    }

    public void addRow(List<String> row) {
        if (row.size() != headers.size()) {
            throw new IllegalArgumentException("Row size must match header size");
        }
        rows.add(row);
    }

    public String getTitle() {
        return title;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public String getDescription() {
        return description;
    }
}