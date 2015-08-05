package org.iatoki.judgels.jophiel;

import com.amazonaws.services.s3.model.Region;
import com.typesafe.config.Config;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class JophielProperties {

    private static JophielProperties INSTANCE;

    private final Config config;

    private String jophielBaseUrl;
    private File jophielBaseDataDir;

    private List<String> jophielClientLabels;
    private List<String> jophielClientTargets;

    private String idTokenPrivateKey;

    private String noreplyName;
    private String noreplyEmail;

    private boolean registrationUsingRecaptcha;
    private String registrationRecaptchaSiteKey;
    private String registrationRecaptchaSecretKey;

    private boolean avatarUsingAWSS3;
    private File avatarLocalDir;
    private boolean avatarAWSUsingKeys;
    private String avatarAWSAccessKey;
    private String avatarAWSSecretKey;
    private String avatarAWSS3BucketName;
    private Region avatarAWSS3BucketRegion;
    private String avatarAWSCloudFrontUrl;

    public String getJophielBaseUrl() {
        return jophielBaseUrl;
    }

    public List<String> getJophielClientLabels() {
        return jophielClientLabels;
    }

    public List<String> getJophielClientTargets() {
        return jophielClientTargets;
    }

    public String getIdTokenPrivateKey() {
        return idTokenPrivateKey;
    }

    public String getNoreplyName() {
        return noreplyName;
    }

    public String getNoreplyEmail() {
        return noreplyEmail;
    }

    public boolean isAvatarUsingAWSS3() {
        return avatarUsingAWSS3;
    }

    public File getAvatarLocalDir() {
        if (isAvatarUsingAWSS3()) {
            throw new UnsupportedOperationException("Avatar is using AWS S3");
        }
        return avatarLocalDir;
    }

    public String getAvatarAWSAccessKey() {
        if (!isAvatarUsingAWSS3()) {
            throw new UnsupportedOperationException("Avatar is not using AWS S3");
        }
        return avatarAWSAccessKey;
    }

    public String getAvatarAWSSecretKey() {
        if (!isAvatarUsingAWSS3()) {
            throw new UnsupportedOperationException("Avatar is not using AWS S3");
        }
        return avatarAWSSecretKey;
    }

    public String getAvatarAWSS3BucketName() {
        if (!isAvatarUsingAWSS3()) {
            throw new UnsupportedOperationException("Avatar is not using AWS S3");
        }
        return avatarAWSS3BucketName;
    }

    public Region getAvatarAWSS3BucketRegion() {
        if (!isAvatarUsingAWSS3()) {
            throw new UnsupportedOperationException("Avatar is not using AWS S3");
        }
        return avatarAWSS3BucketRegion;
    }

    public boolean isAvatarAWSUsingKeys() {
        if (!isAvatarUsingAWSS3()) {
            throw new UnsupportedOperationException("Avatar is not using AWS S3");
        }
        return avatarAWSUsingKeys;
    }

    public String getAvatarAWSCloudFrontUrl() {
        if (!isAvatarUsingAWSS3()) {
            throw new UnsupportedOperationException("Avatar is not using AWS S3");
        }
        return avatarAWSCloudFrontUrl;
    }

    public boolean isRegistrationUsingRecaptcha() {
        return registrationUsingRecaptcha;
    }

    public String getRegistrationRecaptchaSiteKey() {
        if (!isRegistrationUsingRecaptcha()) {
            throw new UnsupportedOperationException("Registration is not using Recaptcha");
        }
        return registrationRecaptchaSiteKey;
    }

    public String getRegistrationRecaptchaSecretKey() {
        if (!isRegistrationUsingRecaptcha()) {
            throw new UnsupportedOperationException("Registration is not using Recaptcha");
        }
        return registrationRecaptchaSecretKey;
    }

    private JophielProperties(Config config) {
        this.config = config;
    }

    public static synchronized void buildInstance(Config config) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException("JophielProperties instance has already been built");
        }

        INSTANCE = new JophielProperties(config);
        INSTANCE.build();
    }

    public static JophielProperties getInstance() {
        if (INSTANCE == null) {
            throw new UnsupportedOperationException("JophielProperties instance has not been built");
        }
        return INSTANCE;
    }

    private void build() {
        jophielBaseUrl = requireStringValue("jophiel.baseUrl");
        jophielBaseDataDir = requireDirectoryValue("jophiel.baseDataDir");
        jophielClientLabels = requireStringListValue("jophiel.client.labels");
        jophielClientTargets = requireStringListValue("jophiel.client.targets");

        idTokenPrivateKey = requireStringValue("jophiel.idToken.key.private");

        noreplyName = requireStringValue("noreply.name");
        noreplyEmail = requireStringValue("noreply.email");

        requireStringValue("play.mailer.host");
        requireIntegerValue("play.mailer.port");
        requireStringValue("play.mailer.ssl");
        requireStringValue("play.mailer.user");
        requireStringValue("play.mailer.password");

        avatarUsingAWSS3 = requireBooleanValue("aws.avatar.s3.use");

        if (avatarUsingAWSS3) {
            avatarAWSUsingKeys = requireBooleanValue("aws.avatar.key.use");
            avatarAWSAccessKey = requireStringValue("aws.avatar.key.access");
            avatarAWSSecretKey = requireStringValue("aws.avatar.key.secret");
            avatarAWSS3BucketName = requireStringValue("aws.avatar.s3.bucket.name");
            avatarAWSS3BucketRegion = Region.fromValue(requireStringValue("aws.avatar.s3.bucket.regionId"));
            avatarAWSCloudFrontUrl = requireStringValue("aws.avatar.cloudFront.baseUrl");
        } else {
            try {
                avatarLocalDir = new File(jophielBaseDataDir, "avatar");
                FileUtils.forceMkdir(avatarLocalDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        registrationUsingRecaptcha = requireBooleanValue("recaptcha.registration.use");

        if (registrationUsingRecaptcha) {
            registrationRecaptchaSiteKey = requireStringValue("recaptcha.registration.key.site");
            registrationRecaptchaSecretKey = requireStringValue("recaptcha.registration.key.secret");
        }
    }

    private String getStringValue(String key) {
        if (!config.hasPath(key)) {
            return null;
        }
        return config.getString(key);
    }

    private String requireStringValue(String key) {
        return config.getString(key);
    }

    private Integer getIntegerValue(String key) {
        if (!config.hasPath(key)) {
            return null;
        }
        return config.getInt(key);
    }

    private int requireIntegerValue(String key) {
        return config.getInt(key);
    }

    private Boolean getBooleanValue(String key) {
        if (!config.hasPath(key)) {
            return null;
        }
        return config.getBoolean(key);
    }

    private boolean requireBooleanValue(String key) {
        return config.getBoolean(key);
    }

    private File requireDirectoryValue(String key) {
        String filename = config.getString(key);

        File dir = new File(filename);
        if (!dir.isDirectory()) {
            throw new IllegalStateException("Directory " + dir.getAbsolutePath() + " does not exist");
        }
        return dir;
    }

    private List<String> requireStringListValue(String key) {
        return config.getStringList(key);
    }
}
