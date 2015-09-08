package org.iatoki.judgels.jophiel.services;

import org.iatoki.judgels.jophiel.EmailNotVerifiedException;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserNotFoundException;

import javax.persistence.NoResultException;

public interface UserAccountService {

    String registerUser(String username, String name, String email, String password, String ipAddress) throws IllegalStateException;

    String generateForgotPasswordRequest(String username, String email, String ipAddress);

    boolean isValidToChangePassword(String code, long currentMillis);

    void processChangePassword(String code, String password, String ipAddress);

    User processLogin(String usernameOrEmail, String password) throws NoResultException, UserNotFoundException, EmailNotVerifiedException;

    void updateUserPassword(String userJid, String password, String ipAddress);
}
