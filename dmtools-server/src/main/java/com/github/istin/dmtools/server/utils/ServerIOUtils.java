package com.github.istin.dmtools.server.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Server-specific IOUtils extensions that require Spring dependencies
 */
public class ServerIOUtils {

    public static File multipartToFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }
        try {
            File file = File.createTempFile("temp_", "_" + multipartFile.getOriginalFilename());
            try (FileOutputStream os = new FileOutputStream(file)) {
                os.write(multipartFile.getBytes());
            }
            return file;
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert MultipartFile to File", e);
        }
    }
}
