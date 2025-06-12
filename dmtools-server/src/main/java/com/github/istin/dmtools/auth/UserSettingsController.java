package com.github.istin.dmtools.auth;

import com.github.istin.dmtools.auth.model.UserSettings;
import com.github.istin.dmtools.auth.service.UserSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class UserSettingsController {

    private final UserSettingsService userSettingsService;

    public UserSettingsController(UserSettingsService userSettingsService) {
        this.userSettingsService = userSettingsService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserSettings(@PathVariable String userId) {
        try {
            Map<String, Object> settings = userSettingsService.getUserSettingsAsMap(userId);
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get user settings"));
        }
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<?> updateUserSettings(@PathVariable String userId, 
                                               @RequestBody Map<String, Object> settingsUpdate) {
        try {
            UserSettings settings = userSettingsService.getUserSettings(userId);
            
            // Update basic settings
            if (settingsUpdate.containsKey("theme")) {
                settings.setTheme((String) settingsUpdate.get("theme"));
            }
            if (settingsUpdate.containsKey("language")) {
                settings.setLanguage((String) settingsUpdate.get("language"));
            }
            if (settingsUpdate.containsKey("timezone")) {
                settings.setTimezone((String) settingsUpdate.get("timezone"));
            }
            if (settingsUpdate.containsKey("notificationsEnabled")) {
                settings.setNotificationsEnabled((Boolean) settingsUpdate.get("notificationsEnabled"));
            }
            if (settingsUpdate.containsKey("emailNotifications")) {
                settings.setEmailNotifications((Boolean) settingsUpdate.get("emailNotifications"));
            }
            
            // Update custom settings
            if (settingsUpdate.containsKey("customSettings")) {
                @SuppressWarnings("unchecked")
                Map<String, String> customSettings = (Map<String, String>) settingsUpdate.get("customSettings");
                if (settings.getSettings() == null) {
                    settings.setSettings(customSettings);
                } else {
                    settings.getSettings().putAll(customSettings);
                }
            }
            
            UserSettings savedSettings = userSettingsService.saveUserSettings(settings);
            return ResponseEntity.ok(userSettingsService.getUserSettingsAsMap(savedSettings.getUserId()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to update user settings"));
        }
    }

    @PutMapping("/user/{userId}/{key}")
    public ResponseEntity<?> updateUserSetting(@PathVariable String userId, 
                                              @PathVariable String key, 
                                              @RequestBody Map<String, String> body) {
        try {
            String value = body.get("value");
            userSettingsService.updateUserSetting(userId, key, value);
            return ResponseEntity.ok(Map.of("success", true, "key", key, "value", value));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to update setting"));
        }
    }

    @GetMapping("/user/{userId}/{key}")
    public ResponseEntity<?> getUserSetting(@PathVariable String userId, 
                                           @PathVariable String key,
                                           @RequestParam(required = false, defaultValue = "") String defaultValue) {
        try {
            String value = userSettingsService.getUserSetting(userId, key, defaultValue);
            return ResponseEntity.ok(Map.of("key", key, "value", value));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to get setting"));
        }
    }
} 