package org.iatoki.judgels.jophiel.config;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import org.iatoki.judgels.AWSFileSystemProvider;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.LocalFileSystemProvider;
import org.iatoki.judgels.play.config.AbstractJudgelsPlayModule;
import org.iatoki.judgels.jophiel.JophielProperties;

public class JophielModule extends AbstractJudgelsPlayModule {

    @Override
    protected void manualBinding() {
        bind(FileSystemProvider.class).annotatedWith(AvatarFile.class).toInstance(avatarFileSystemProvider());
    }

    @Override
    protected String getDaosImplPackage() {
        return "org.iatoki.judgels.jophiel.models.daos.impls";
    }

    @Override
    protected String getServicesImplPackage() {
        return "org.iatoki.judgels.jophiel.services.impls";
    }

    private JophielProperties jophielProperties() {
        return JophielProperties.getInstance();
    }

    private FileSystemProvider avatarFileSystemProvider() {
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
