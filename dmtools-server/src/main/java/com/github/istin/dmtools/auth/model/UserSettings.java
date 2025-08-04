package com.github.istin.dmtools.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    private String userId;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_setting_values", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "setting_key")
    @Column(name = "setting_value", columnDefinition = "TEXT")
    private Map<String, String> settings;

    @Column(name = "theme")
    private String theme = "auto"; // auto, light, dark

    @Column(name = "language")
    private String language = "en";

    @Column(name = "timezone")
    private String timezone = "UTC";

    @Column(name = "notifications_enabled")
    private boolean notificationsEnabled = true;

    @Column(name = "email_notifications")
    private boolean emailNotifications = true;
} 