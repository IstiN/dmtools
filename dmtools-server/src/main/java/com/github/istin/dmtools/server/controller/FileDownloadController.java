package com.github.istin.dmtools.server.controller;

import com.github.istin.dmtools.server.service.FileDownloadService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileDownloadController {

    private final FileDownloadService fileDownloadService;

    public FileDownloadController(FileDownloadService fileDownloadService) {
        this.fileDownloadService = fileDownloadService;
    }

    @GetMapping("/download/{token}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String token) {
        try {
            FileDownloadService.DownloadInfo downloadInfo = fileDownloadService.getDownloadInfo(token);
            if (downloadInfo == null) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = downloadInfo.resource();
            String filename = downloadInfo.filename();
            String mimeType = downloadInfo.mimeType();

            ResponseEntity<Resource> response = ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(resource);
            
            // Clean up file after response is created
            cleanupFileAfterResponse(downloadInfo);
            
            return response;
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private void cleanupFileAfterResponse(FileDownloadService.DownloadInfo downloadInfo) {
        // Schedule file cleanup after response is sent
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Wait 1 second for response to be sent
                if (downloadInfo.resource() instanceof org.springframework.core.io.FileSystemResource fileRes) {
                    java.io.File file = fileRes.getFile();
                    if (file.exists() && file.delete()) {
                        System.out.println("Cleaned up downloaded file: " + file.getName());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error cleaning up downloaded file: " + e.getMessage());
            }
        }).start();
    }
} 