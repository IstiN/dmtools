package com.github.istin.dmtools.common.model;

import java.util.List;

public interface IFile {

    String getPath();

    String getType();

    boolean isDir();

    String getSelfLink();

    String getFileContent();

    void setFileContent(String fileContent);

    List<ITextMatch> getTextMatches();
}