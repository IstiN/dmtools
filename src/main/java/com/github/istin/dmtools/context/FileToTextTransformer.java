package com.github.istin.dmtools.context;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class FileToTextTransformer {

    private static final Set<String> BINARY_EXTENSIONS = Set.of(
            "pdf", "csv", "doc", "docx", "xls", "xlsx",
            "jpg", "jpeg", "png", "gif", "bmp", "tiff", "webp"
    );

    public static String transform(File file) throws IOException {
        if (file == null || !file.exists()) {
            return null;
        }

        String fileName = file.getName().toLowerCase();

        // Return null for binary files
        if (BINARY_EXTENSIONS.stream().anyMatch(ext -> fileName.endsWith("." + ext))) {
            return null;
        }

        // Read text files
        return file.getName() + "\n" + FileUtils.readFileToString(file);
    }

}
