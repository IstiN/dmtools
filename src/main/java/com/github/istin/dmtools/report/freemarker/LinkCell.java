package com.github.istin.dmtools.report.freemarker;

public class LinkCell extends GenericCell {

    public LinkCell(String text, String link) {
        super("<a href=\""+ link +"\">" + text + "</a>");
    }
}
