package com.github.istin.dmtools.common.model;

public interface IComment {
    IUser getAuthor();
    String getBody();

    String getId();
}
