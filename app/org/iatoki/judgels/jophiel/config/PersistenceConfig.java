package org.iatoki.judgels.jophiel.config;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.iatoki.judgels.commons.AWSFileSystemProvider;
import org.iatoki.judgels.commons.FileSystemProvider;
import org.iatoki.judgels.commons.JudgelsProperties;
import org.iatoki.judgels.commons.LocalFileSystemProvider;
import org.iatoki.judgels.jophiel.JophielProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Deny Prasetyo
 * 26 May 2015
 * Principal Software Development Engineer
 * GDP Labs
 * deny.prasetyo@gdplabs.id
 */

@Configuration
@ComponentScan({
        "org.iatoki.judgels.jophiel.models.daos",
        "org.iatoki.judgels.jophiel.services"
})
public class PersistenceConfig {

    @Bean
    public JudgelsProperties judgelsProperties() {
        org.iatoki.judgels.jophiel.BuildInfo$ buildInfo = org.iatoki.judgels.jophiel.BuildInfo$.MODULE$;
        JudgelsProperties.buildInstance(buildInfo.name(), buildInfo.version(), ConfigFactory.load());
        return JudgelsProperties.getInstance();
    }

    @Bean
    public JophielProperties jophielProperties() {
        Config config = ConfigFactory.load();
        JophielProperties.buildInstance(config);
        return JophielProperties.getInstance();
    }

    @Bean
    public FileSystemProvider fileSystemProvider() {
        FileSystemProvider avatarProvider;
        if (jophielProperties().isAvatarUsingAWSS3()) {
            AmazonS3Client s3Client;
            if (jophielProperties().isAvatarAWSUsingKeys()) {
                s3Client = new AmazonS3Client(new BasicAWSCredentials(jophielProperties().getAvatarAWSAccessKey(), jophielProperties().getAvatarAWSSecretKey()));
            } else {
                s3Client = new AmazonS3Client();
            }
            avatarProvider = new AWSFileSystemProvider(s3Client, jophielProperties().getAvatarAWSS3BucketName(), jophielProperties().getAvatarAWSCloudFrontUrl(), jophielProperties().getAvatarAWSS3BucketRegion());
        } else {
            avatarProvider = new LocalFileSystemProvider(jophielProperties().getAvatarLocalDir());
        }

        return avatarProvider;
    }

}
