package org.iatoki.judgels.jophiel.services.impls;

import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.JudgelsUtils;
import org.iatoki.judgels.jophiel.PasswordHash;
import org.iatoki.judgels.jophiel.commons.exceptions.EmailNotVerifiedException;
import org.iatoki.judgels.jophiel.commons.exceptions.UserNotFoundException;
import org.iatoki.judgels.jophiel.commons.plains.User;
import org.iatoki.judgels.jophiel.models.daos.UserDao;
import org.iatoki.judgels.jophiel.models.daos.UserEmailDao;
import org.iatoki.judgels.jophiel.models.daos.UserForgotPasswordDao;
import org.iatoki.judgels.jophiel.models.domains.UserEmailModel;
import org.iatoki.judgels.jophiel.models.domains.UserForgotPasswordModel;
import org.iatoki.judgels.jophiel.models.domains.UserModel;
import org.iatoki.judgels.jophiel.services.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import play.mvc.Http;

import javax.persistence.NoResultException;
import javax.validation.ConstraintViolationException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.UUID;

@Service("userAccountService")
public final class UserAccountServiceImpl implements UserAccountService {

    @Autowired
    private UserDao userDao;
    @Autowired
    private UserEmailDao userEmailDao;
    @Autowired
    private UserForgotPasswordDao userForgotPasswordDao;


    @Override
    public String registerUser(String username, String name, String email, String password) throws IllegalStateException {
        try {
            UserModel userModel = new UserModel();
            userModel.username = username;
            userModel.name = name;
            userModel.password = PasswordHash.createHash(password);
            userModel.profilePictureImageName = "avatar-default.png";
            userModel.roles = "user";

            userDao.persist(userModel, "guest", IdentityUtils.getIpAddress());

            String emailCode = JudgelsUtils.hashMD5(UUID.randomUUID().toString());
            UserEmailModel emailModel = new UserEmailModel(email, emailCode);
            emailModel.userJid = userModel.jid;

            userEmailDao.persist(emailModel, "guest", IdentityUtils.getIpAddress());

            return emailCode;
        } catch (ConstraintViolationException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String forgotPassword(String username, String email) {
        UserModel userModel = userDao.findByUsername(username);

        String code = JudgelsUtils.hashMD5(UUID.randomUUID().toString());
        UserForgotPasswordModel forgotPasswordModel = new UserForgotPasswordModel();
        forgotPasswordModel.userJid = userModel.jid;
        forgotPasswordModel.code = code;
        forgotPasswordModel.used = false;

        userForgotPasswordDao.persist(forgotPasswordModel, "guest", IdentityUtils.getIpAddress());
        return code;
    }

    @Override
    public boolean isValidToChangePassword(String code, long currentMillis) {
        return userForgotPasswordDao.isCodeValid(code, currentMillis);
    }

    @Override
    public void changePassword(String code, String password) {
        try {
            UserForgotPasswordModel forgotPasswordModel = userForgotPasswordDao.findByCode(code);
            forgotPasswordModel.used = true;

            userForgotPasswordDao.edit(forgotPasswordModel, "guest", IdentityUtils.getIpAddress());

            UserModel userModel = userDao.findByJid(forgotPasswordModel.userJid);
            userModel.password = PasswordHash.createHash(password);

            userDao.edit(userModel, "guest", IdentityUtils.getIpAddress());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public User login(String usernameOrEmail, String password) throws UserNotFoundException, EmailNotVerifiedException {
        try {
            UserModel userModel;
            UserEmailModel emailModel;
            if (userDao.existByUsername(usernameOrEmail)) {
                userModel = userDao.findByUsername(usernameOrEmail);
                emailModel = userEmailDao.findByUserJid(userModel.jid);
            } else if (userEmailDao.isExistByEmail(usernameOrEmail)) {
                emailModel = userEmailDao.findByEmail(usernameOrEmail);
                userModel = userDao.findByJid(emailModel.userJid);
            } else {
                throw new UserNotFoundException();
            }

            if ((userModel.password.contains(":")) && (PasswordHash.validatePassword(password, userModel.password))) {
                if (emailModel.emailVerified) {
                    return createUserFromModels(userModel, emailModel);
                } else {
                    throw new EmailNotVerifiedException();
                }
            } else if (userModel.password.equals(JudgelsUtils.hashSHA256(password))) {
                userModel.password = PasswordHash.createHash(password);

                userDao.edit(userModel, "guest", IdentityUtils.getIpAddress());
                if (emailModel.emailVerified) {
                    return createUserFromModels(userModel, emailModel);
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
    public void updatePassword(String userJid, String password) {
        try {
            UserModel userModel = userDao.findByJid(userJid);
            userModel.password = PasswordHash.createHash(password);

            userDao.edit(userModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }

    private User createUserFromModels(UserModel userModel, UserEmailModel emailModel) {
        return new User(userModel.id, userModel.jid, userModel.username, userModel.name, emailModel.email, getAvatarImageUrl(userModel.profilePictureImageName), Arrays.asList(userModel.roles.split(",")));
    }

    private URL getAvatarImageUrl(String imageName) {
        try {
            return new URL(org.iatoki.judgels.jophiel.controllers.apis.routes.UserAPIController.renderAvatarImage(imageName).absoluteURL(Http.Context.current().request(), Http.Context.current().request().secure()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }


}
