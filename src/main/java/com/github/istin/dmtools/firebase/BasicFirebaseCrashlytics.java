package com.github.istin.dmtools.firebase;

import com.github.istin.dmtools.common.utils.PropertyReader;

import java.io.IOException;

public class BasicFirebaseCrashlytics extends FirebaseCrashlytics {

    private static FirebaseConfig DEFAULT_CONFIG;

    private FirebaseConfig config;

    static {
        PropertyReader propertyReader = new PropertyReader();
        DEFAULT_CONFIG = FirebaseConfig.builder()
                .projectId(propertyReader.getFirebaseProjectId())
                .serviceAccountJsonAuth(propertyReader.getFirebaseServiceAccountJsonAuth())
                .build();
    }

    public BasicFirebaseCrashlytics() throws IOException {
        this(DEFAULT_CONFIG);
    }

    public BasicFirebaseCrashlytics(FirebaseConfig config) throws IOException {
        super(config.getProjectId(), config.getServiceAccountJsonAuth());
        this.config = config;
    }

    private static BasicFirebaseCrashlytics instance;

    public static FirebaseCrashlytics getInstance() throws IOException {
        if (instance == null) {
            instance = new BasicFirebaseCrashlytics();
        }
        return instance;
    }

}