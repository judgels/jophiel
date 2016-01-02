package org.iatoki.judgels.jophiel.user.account;

import org.iatoki.judgels.jophiel.PasswordHash;
import org.iatoki.judgels.jophiel.user.User;
import org.iatoki.judgels.jophiel.user.UserDao;
import org.iatoki.judgels.jophiel.user.UserModel;
import org.iatoki.judgels.jophiel.user.UserNotFoundException;
import org.iatoki.judgels.jophiel.user.UserServiceUtils;
import org.iatoki.judgels.jophiel.user.profile.email.EmailNotVerifiedException;
import org.iatoki.judgels.jophiel.user.profile.email.UserEmailDao;
import org.iatoki.judgels.jophiel.user.profile.email.UserEmailModel;
import org.iatoki.judgels.play.JudgelsPlayUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolationException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

@Singleton
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
    public String registerUser(String username, String name, String email, String password, String ipAddress) throws IllegalStateException {
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
            userDao.persist(userModel, "guest", ipAddress);
        } catch (ConstraintViolationException e) {
            throw new IllegalStateException(e);
        }

        String emailCode = JudgelsPlayUtils.hashMD5(UUID.randomUUID().toString());
        UserEmailModel emailModel = new UserEmailModel();
        emailModel.email = email;
        emailModel.emailCode = emailCode;
        emailModel.userJid = userModel.jid;

        userEmailDao.persist(emailModel, "guest", ipAddress);

        userModel.emailJid = emailModel.jid;

        userDao.edit(userModel, "guest", ipAddress);

        return emailCode;
    }

    @Override
    public String generateForgotPasswordRequest(String username, String email, String ipAddress) {
        UserModel userModel = userDao.findByUsername(username);

        String code = JudgelsPlayUtils.hashMD5(UUID.randomUUID().toString());
        UserForgotPasswordModel forgotPasswordModel = new UserForgotPasswordModel();
        forgotPasswordModel.userJid = userModel.jid;
        forgotPasswordModel.code = code;
        forgotPasswordModel.used = false;

        userForgotPasswordDao.persist(forgotPasswordModel, "guest", ipAddress);
        return code;
    }

    @Override
    public boolean isValidToChangePassword(String code, long currentMillis) {
        return userForgotPasswordDao.isForgotPasswordCodeValid(code, currentMillis);
    }

    @Override
    public void processChangePassword(String code, String password, String ipAddress) {
        UserForgotPasswordModel forgotPasswordModel = userForgotPasswordDao.findByForgotPasswordCode(code);
        forgotPasswordModel.used = true;

        userForgotPasswordDao.edit(forgotPasswordModel, "guest", ipAddress);

        UserModel userModel = userDao.findByJid(forgotPasswordModel.userJid);

        try {
            userModel.password = PasswordHash.createHash(password);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }

        userDao.edit(userModel, "guest", ipAddress);
    }

    @Override
    public User processLogin(String usernameOrEmail, String password, String ipAddress) throws UserNotFoundException, EmailNotVerifiedException {
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
                    return UserServiceUtils.createUserFromModel(userModel);
                } else {
                    throw new EmailNotVerifiedException();
                }
            } else if (userModel.password.equals(JudgelsPlayUtils.hashSHA256(password))) {
                userModel.password = PasswordHash.createHash(password);

                userDao.edit(userModel, "guest", ipAddress);
                if (emailModel.emailVerified) {
                    return UserServiceUtils.createUserFromModel(userModel);
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
    public void updateUserPassword(String userJid, String password, String ipAddress) {
        UserModel userModel = userDao.findByJid(userJid);

        try {
            userModel.password = PasswordHash.createHash(password);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }

        userDao.edit(userModel, userJid, ipAddress);
    }
}
