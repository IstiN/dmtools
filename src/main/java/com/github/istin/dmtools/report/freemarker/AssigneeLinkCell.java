package com.github.istin.dmtools.report.freemarker;

import com.github.istin.dmtools.atlassian.common.model.Assignee;

public class AssigneeLinkCell extends GenericCell {

    public AssigneeLinkCell(Assignee assignee) {
        super(assignee == null ? "&nbsp;" : "<a href=\"mailto:"+ assignee.getEmailAddress() +"\">" + assignee.getDisplayName() + "</a>");
    }
}
