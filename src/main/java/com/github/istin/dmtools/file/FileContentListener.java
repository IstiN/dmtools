package com.github.istin.dmtools.file;

public interface FileContentListener {
    void onFileRead(String folderPath, String packageName, String fileName, String fileContent) throws Exception;
}
