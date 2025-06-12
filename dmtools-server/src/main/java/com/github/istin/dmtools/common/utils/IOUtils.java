package com.github.istin.dmtools.common.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class IOUtils {

    public static File multipartToFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }
        try {
            File file = File.createTempFile("temp_", "_" + multipartFile.getOriginalFilename());
            try (OutputStream os = new FileOutputStream(file)) {
                os.write(multipartFile.getBytes());
            }
            return file;
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert MultipartFile to File", e);
        }
    }

    public static void deleteFiles(List<File> files) {
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file != null && file.exists()) {
                if (!file.delete()) {
                    System.err.println("Failed to delete temporary file: " + file.getAbsolutePath());
                }
            }
        }
    }
} 