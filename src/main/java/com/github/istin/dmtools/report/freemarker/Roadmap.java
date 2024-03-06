package com.github.istin.dmtools.report.freemarker;

import java.util.List;

public class Roadmap {

    private List<Row> rows;

    public Roadmap(List<Row> rows) {
        this.rows = rows;
    }

    public List<Row> getRows() {
        return rows;
    }

    public void setRows(List<Row> rows) {
        this.rows = rows;
    }
}
