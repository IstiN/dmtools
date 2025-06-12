package com.github.istin.dmtools.common.model;

public interface IActivity {

    String getAction();

    IComment getComment();

    IUser getApproval();
}