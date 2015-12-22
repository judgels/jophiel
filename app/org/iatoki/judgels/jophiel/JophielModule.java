package org.iatoki.judgels.jophiel;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.iatoki.judgels.AWSFileSystemProvider;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.LocalFileSystemProvider;
import org.iatoki.judgels.jophiel.config.AvatarFileSystemProvider;
import org.iatoki.judgels.play.JudgelsPlayProperties;
import org.iatoki.judgels.play.config.AbstractJudgelsPlayModule;
import org.iatoki.judgels.play.general.GeneralName;
import org.iatoki.judgels.play.general.GeneralVersion;
import org.iatoki.judgels.play.migration.BaseDataMigrationService;

public class JophielModule extends AbstractJudgelsPlayModule {

    @Override
    protected void manualBinding() {
        org.iatoki.judgels.jophiel.BuildInfo$ buildInfo = org.iatoki.judgels.jophiel.BuildInfo$.MODULE$;

        bindConstant().annotatedWith(GeneralName.class).to(buildInfo.name());
        bindConstant().annotatedWith(GeneralVersion.class).to(buildInfo.version());

        // <DEPRECATED>
        JudgelsPlayProperties.buildInstance(buildInfo.name(), buildInfo.version(), ConfigFactory.load());
        Config config = ConfigFactory.load();
        JophielProperties.buildInstance(config);
        // </DEPRECATED>

        bind(BaseDataMigrationService.class).to(JophielDataMigrationServiceImpl.class);

        bind(FileSystemProvider.class).annotatedWith(AvatarFileSystemProvider.class).toInstance(avatarFileSystemProvider());
    }

    @Override
    protected String getDaosImplPackage() {
        return "org.iatoki.judgels.jophiel.models.daos.hibernate";
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
