package org.iatoki.judgels.jophiel.user.account;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.jophiel.user.User;
import org.iatoki.judgels.jophiel.user.UserNotFoundException;
import org.iatoki.judgels.jophiel.user.profile.email.EmailNotVerifiedException;

import javax.persistence.NoResultException;

@ImplementedBy(UserAccountServiceImpl.class)
public interface UserAccountService {

    String registerUser(String username, String name, String email, String password, String ipAddress) throws IllegalStateException;

    String generateForgotPasswordRequest(String username, String email, String ipAddress);

    boolean isValidToChangePassword(String code, long currentMillis);

    void processChangePassword(String code, String password, String ipAddress);

    User processLogin(String usernameOrEmail, String password, String ipAddress) throws NoResultException, UserNotFoundException, EmailNotVerifiedException;

    void updateUserPassword(String userJid, String password, String ipAddress);
}
