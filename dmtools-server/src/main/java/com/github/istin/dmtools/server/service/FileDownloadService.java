package com.github.istin.dmtools.server.service;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class FileDownloadService {

    private final Map<String, DownloadInfo> downloadTokens = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public FileDownloadService() {
        // Запускаем очистку каждые 5 минут
        scheduler.scheduleAtFixedRate(this::cleanupExpiredTokens, 5, 5, TimeUnit.MINUTES);
    }

    public String createDownloadToken(File file) throws IOException {
        String token = UUID.randomUUID().toString();
        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        DownloadInfo downloadInfo = new DownloadInfo(
                new FileSystemResource(file),
                file.getName(),
                mimeType,
                Instant.now().plusSeconds(15 * 60) // 15 минут
        );

        downloadTokens.put(token, downloadInfo);
        return token;
    }

    public DownloadInfo getDownloadInfo(String token) {
        DownloadInfo downloadInfo = downloadTokens.get(token);
        if (downloadInfo == null || downloadInfo.expiresAt().isBefore(Instant.now())) {
            // Clean up expired file if it exists
            if (downloadInfo != null) {
                cleanupFile(downloadInfo);
            }
            downloadTokens.remove(token);
            return null;
        }

        // Remove token after first use (file will be cleaned up after download)
        downloadTokens.remove(token);
        return downloadInfo;
    }

    private void cleanupExpiredTokens() {
        Instant now = Instant.now();
        downloadTokens.entrySet().removeIf(entry -> {
            if (entry.getValue().expiresAt().isBefore(now)) {
                cleanupFile(entry.getValue());
                return true;
            }
            return false;
        });
    }

    private void cleanupFile(DownloadInfo downloadInfo) {
        try {
            if (downloadInfo.resource() instanceof FileSystemResource fileRes) {
                File file = fileRes.getFile();
                if (file.exists() && file.delete()) {
                    System.out.println("Cleaned up expired file: " + file.getName());
                }
            }
        } catch (Exception e) {
            System.err.println("Error cleaning up file: " + e.getMessage());
        }
    }

    public record DownloadInfo(Resource resource, String filename, String mimeType, Instant expiresAt) {}
} 