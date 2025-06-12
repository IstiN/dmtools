package com.github.istin.dmtools.auth.service;

import com.github.istin.dmtools.auth.model.UserSettings;
import com.github.istin.dmtools.auth.repository.UserSettingsRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;

    public UserSettingsService(UserSettingsRepository userSettingsRepository) {
        this.userSettingsRepository = userSettingsRepository;
    }

    public UserSettings getUserSettings(String userId) {
        return userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
    }

    public UserSettings saveUserSettings(UserSettings userSettings) {
        return userSettingsRepository.save(userSettings);
    }

    public UserSettings updateUserSetting(String userId, String key, String value) {
        UserSettings settings = getUserSettings(userId);
        if (settings.getSettings() == null) {
            settings.setSettings(new HashMap<>());
        }
        settings.getSettings().put(key, value);
        return userSettingsRepository.save(settings);
    }

    public String getUserSetting(String userId, String key, String defaultValue) {
        UserSettings settings = getUserSettings(userId);
        if (settings.getSettings() != null && settings.getSettings().containsKey(key)) {
            return settings.getSettings().get(key);
        }
        return defaultValue;
    }

    private UserSettings createDefaultSettings(String userId) {
        UserSettings settings = new UserSettings();
        settings.setUserId(userId);
        settings.setSettings(new HashMap<>());
        settings.setTheme("auto");
        settings.setLanguage("en");
        settings.setTimezone("UTC");
        settings.setNotificationsEnabled(true);
        settings.setEmailNotifications(true);
        return userSettingsRepository.save(settings);
    }

    public Map<String, Object> getUserSettingsAsMap(String userId) {
        UserSettings settings = getUserSettings(userId);
        Map<String, Object> result = new HashMap<>();
        
        result.put("theme", settings.getTheme());
        result.put("language", settings.getLanguage());
        result.put("timezone", settings.getTimezone());
        result.put("notificationsEnabled", settings.isNotificationsEnabled());
        result.put("emailNotifications", settings.isEmailNotifications());
        
        if (settings.getSettings() != null) {
            result.put("customSettings", settings.getSettings());
        }
        
        return result;
    }
} 