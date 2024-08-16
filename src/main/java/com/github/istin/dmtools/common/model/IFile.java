package com.github.istin.dmtools.common.model;

public interface IFile {

    String getPath();

    String getType();

    boolean isDir();

    String getSelfLink();

    String getFileContent();

    void setFileContent(String fileContent);
}