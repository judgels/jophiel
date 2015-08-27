package org.iatoki.judgels.jophiel.services.impls;

import org.iatoki.judgels.jophiel.EmailNotVerifiedException;
import org.iatoki.judgels.jophiel.PasswordHash;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserNotFoundException;
import org.iatoki.judgels.jophiel.models.daos.UserDao;
import org.iatoki.judgels.jophiel.models.daos.UserEmailDao;
import org.iatoki.judgels.jophiel.models.daos.UserForgotPasswordDao;
import org.iatoki.judgels.jophiel.models.entities.UserEmailModel;
import org.iatoki.judgels.jophiel.models.entities.UserForgotPasswordModel;
import org.iatoki.judgels.jophiel.models.entities.UserModel;
import org.iatoki.judgels.jophiel.services.UserAccountService;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolationException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.UUID;

@Singleton
@Named("userAccountService")
public final class UserAccountServiceImpl implements UserAccountService {

    private final UserDao userDao;
    private final UserEmailDao userEmailDao;
    private final UserForgotPasswordDao userForgotPasswordDao;

    @Inject
    public UserAccountServiceImpl(UserDao userDao, UserEmailDao userEmailDao, UserForgotPasswordDao userForgotPasswordDao) {
        this.userDao = userDao;
        this.userEmailDao = userEmailDao;
        this.userForgotPasswordDao = userForgotPasswordDao;
    }

    @Override
    public String registerUser(String username, String name, String email, String password) throws IllegalStateException {
        UserModel userModel = new UserModel();
        userModel.username = username;
        userModel.name = name;
        userModel.showName = true;

        try {
            userModel.password = PasswordHash.createHash(password);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }

        userModel.profilePictureImageName = "avatar-default.png";
        userModel.roles = "user";

        try {
            userDao.persist(userModel, "guest", IdentityUtils.getIpAddress());
        } catch (ConstraintViolationException e) {
            throw new IllegalStateException(e);
        }

        String emailCode = JudgelsPlayUtils.hashMD5(UUID.randomUUID().toString());
        UserEmailModel emailModel = new UserEmailModel();
        emailModel.email = email;
        emailModel.emailCode = emailCode;
        emailModel.userJid = userModel.jid;

        userEmailDao.persist(emailModel, "guest", IdentityUtils.getIpAddress());

        userModel.emailJid = emailModel.jid;

        userDao.edit(userModel, "guest", IdentityUtils.getIpAddress());

        return emailCode;
    }

    @Override
    public String generateForgotPasswordRequest(String username, String email) {
        UserModel userModel = userDao.findByUsername(username);

        String code = JudgelsPlayUtils.hashMD5(UUID.randomUUID().toString());
        UserForgotPasswordModel forgotPasswordModel = new UserForgotPasswordModel();
        forgotPasswordModel.userJid = userModel.jid;
        forgotPasswordModel.code = code;
        forgotPasswordModel.used = false;

        userForgotPasswordDao.persist(forgotPasswordModel, "guest", IdentityUtils.getIpAddress());
        return code;
    }

    @Override
    public boolean isValidToChangePassword(String code, long currentMillis) {
        return userForgotPasswordDao.isForgotPasswordCodeValid(code, currentMillis);
    }

    @Override
    public void processChangePassword(String code, String password) {
        UserForgotPasswordModel forgotPasswordModel = userForgotPasswordDao.findByForgotPasswordCode(code);
        forgotPasswordModel.used = true;

        userForgotPasswordDao.edit(forgotPasswordModel, "guest", IdentityUtils.getIpAddress());

        UserModel userModel = userDao.findByJid(forgotPasswordModel.userJid);

        try {
            userModel.password = PasswordHash.createHash(password);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }

        userDao.edit(userModel, "guest", IdentityUtils.getIpAddress());
    }

    @Override
    public User processLogin(String usernameOrEmail, String password) throws UserNotFoundException, EmailNotVerifiedException {
        try {
            UserModel userModel;
            UserEmailModel emailModel;
            if (userDao.existByUsername(usernameOrEmail)) {
                userModel = userDao.findByUsername(usernameOrEmail);
                emailModel = userEmailDao.findByJid(userModel.emailJid);
            } else if (userEmailDao.existsByEmail(usernameOrEmail)) {
                emailModel = userEmailDao.findByEmail(usernameOrEmail);
                userModel = userDao.findByJid(emailModel.userJid);
            } else {
                throw new UserNotFoundException();
            }

            if (userModel.password.contains(":") && PasswordHash.validatePassword(password, userModel.password)) {
                if (emailModel.emailVerified) {
                    return createUserFromModels(userModel);
                } else {
                    throw new EmailNotVerifiedException();
                }
            } else if (userModel.password.equals(JudgelsPlayUtils.hashSHA256(password))) {
                userModel.password = PasswordHash.createHash(password);

                userDao.edit(userModel, "guest", IdentityUtils.getIpAddress());
                if (emailModel.emailVerified) {
                    return createUserFromModels(userModel);
                } else {
                    throw new EmailNotVerifiedException();
                }
            } else {
                return null;
            }
        } catch (NoResultException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new UserNotFoundException();
        }
    }

    @Override
    public void updateUserPassword(String userJid, String password) {
        UserModel userModel = userDao.findByJid(userJid);

        try {
            userModel.password = PasswordHash.createHash(password);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }

        userDao.edit(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    private User createUserFromModels(UserModel userModel) {
        return new User(userModel.id, userModel.jid, userModel.username, userModel.name, userModel.emailJid, userModel.phoneJid, userModel.showName, getAvatarImageUrl(userModel.profilePictureImageName), Arrays.asList(userModel.roles.split(",")));
    }

    private URL getAvatarImageUrl(String imageName) {
        try {
            return new URL(org.iatoki.judgels.jophiel.controllers.apis.routes.UserAPIController.renderAvatarImage(imageName).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
