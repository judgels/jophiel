package org.iatoki.judgels.jophiel;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.inject.AbstractModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.iatoki.judgels.AWSFileSystemProvider;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.LocalFileSystemProvider;
import org.iatoki.judgels.jophiel.user.profile.AvatarFileSystemProvider;
import org.iatoki.judgels.play.JudgelsPlayProperties;
import org.iatoki.judgels.play.general.GeneralName;
import org.iatoki.judgels.play.general.GeneralVersion;
import org.iatoki.judgels.play.migration.JudgelsDataMigrator;

public final class JophielModule extends AbstractModule {

    @Override
    public void configure() {
        org.iatoki.judgels.jophiel.BuildInfo$ buildInfo = org.iatoki.judgels.jophiel.BuildInfo$.MODULE$;

        bindConstant().annotatedWith(GeneralName.class).to(buildInfo.name());
        bindConstant().annotatedWith(GeneralVersion.class).to(buildInfo.version());

        // <DEPRECATED>
        Config config = ConfigFactory.load();
        JudgelsPlayProperties.buildInstance(buildInfo.name(), buildInfo.version(), config);
        JophielProperties.buildInstance(config);
        bind(JophielSingletonsBuilder.class).asEagerSingleton();
        // </DEPRECATED>

        bind(JudgelsDataMigrator.class).to(JophielDataMigrator.class);

        bind(FileSystemProvider.class).annotatedWith(AvatarFileSystemProvider.class).toInstance(avatarFileSystemProvider());
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
