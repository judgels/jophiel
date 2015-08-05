package org.iatoki.judgels.jophiel;

import com.typesafe.config.Config;

import java.io.File;

public final class JophielTestProperties {

    private static JophielTestProperties INSTANCE;

    private String testUsername;
    private String testPassword;

    private JophielTestProperties() {

    }

    public String getTestUsername() {
        return testUsername;
    }

    public String getTestPassword() {
        return testPassword;
    }

    public static synchronized void buildInstance(Config config) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException("JophielTestProperties instance has already been built");
        }

        INSTANCE = new JophielTestProperties();
        INSTANCE.build(config);
    }

    public static JophielTestProperties getInstance() {
        if (INSTANCE == null) {
            throw new UnsupportedOperationException("JophielTestProperties instance has not been built");
        }
        return INSTANCE;
    }

    private void build(Config config) {
        testUsername = requireStringValue(config, "test.username");
        testPassword = requireStringValue(config, "test.password");
    }

    private String getStringValue(Config config, String key) {
        if (!config.hasPath(key)) {
            return null;
        }
        return config.getString(key);
    }

    private String requireStringValue(Config config, String key) {
        return config.getString(key);
    }

    private Integer getIntegerValue(Config config, String key) {
        if (!config.hasPath(key)) {
            return null;
        }
        return config.getInt(key);
    }

    private int requireIntegerValue(Config config, String key) {
        return config.getInt(key);
    }

    private Boolean getBooleanValue(Config config, String key) {
        if (!config.hasPath(key)) {
            return null;
        }
        return config.getBoolean(key);
    }

    private boolean requireBooleanValue(Config config, String key) {
        return config.getBoolean(key);
    }

    private File requireDirectoryValue(Config config, String key) {
        String filename = config.getString(key);

        File dir = new File(filename);
        if (!dir.isDirectory()) {
            throw new IllegalStateException("Directory " + dir.getAbsolutePath() + " does not exist");
        }
        return dir;
    }
}
