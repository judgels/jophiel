package org.iatoki.judgels.jophiel.services.impls;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.FileSystemProvider;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.JudgelsUtils;
import org.iatoki.judgels.jophiel.config.AvatarFile;
import org.iatoki.judgels.jophiel.models.daos.UserDao;
import org.iatoki.judgels.jophiel.models.entities.UserModel;
import org.iatoki.judgels.jophiel.services.UserProfileService;
import play.mvc.Http;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Singleton
@Named("userProfileService")
public final class UserProfileServiceImpl implements UserProfileService {

    private final UserDao userDao;
    private final FileSystemProvider avatarFileSystemProvider;

    @Inject
    public UserProfileServiceImpl(UserDao userDao, @AvatarFile FileSystemProvider avatarFileSystemProvider) {
        this.userDao = userDao;
        this.avatarFileSystemProvider = avatarFileSystemProvider;
    }

    @PostConstruct
    public  void init() {
        if (!avatarFileSystemProvider.fileExists(ImmutableList.of("avatar-default.png"))) {
            try {
                avatarFileSystemProvider.uploadFileFromStream(ImmutableList.of(), getClass().getResourceAsStream("/public/images/avatar/avatar-default.png"), "avatar-default.png");
                avatarFileSystemProvider.makeFilePublic(ImmutableList.of("avatar-default.png"));
            } catch (IOException e) {
                throw new IllegalStateException("Cannot create default avatar.");
            }
        }
    }

    @Override
    public void updateProfile(String userJid, String name) {
        UserModel userModel = userDao.findByJid(userJid);
        userModel.name = name;

        userDao.edit(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        Http.Context.current().session().put("name", userModel.name);
    }

    @Override
    public void updateProfile(String userJid, String name, String password) {
        UserModel userModel = userDao.findByJid(userJid);
        userModel.name = name;
        userModel.password = JudgelsUtils.hashSHA256(password);

        userDao.edit(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        Http.Context.current().session().put("name", userModel.name);
    }

    @Override
    public String updateProfilePicture(String userJid, File imageFile, String extension) throws IOException {
        String newImageName = IdentityUtils.getUserJid() + "-" + JudgelsUtils.hashMD5(UUID.randomUUID().toString()) + "." + extension;
        List<String> filePath = ImmutableList.of(newImageName);
        avatarFileSystemProvider.uploadFile(ImmutableList.of(), imageFile, newImageName);
        avatarFileSystemProvider.makeFilePublic(filePath);

        UserModel userModel = userDao.findByJid(userJid);
        userModel.profilePictureImageName = newImageName;

        userDao.edit(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        return newImageName;
    }

    @Override
    public String getAvatarImageUrlString(String imageName) {
        return avatarFileSystemProvider.getURL(ImmutableList.of(imageName));
    }

}
