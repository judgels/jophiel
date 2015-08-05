package org.iatoki.judgels.jophiel.services;

import org.iatoki.judgels.jophiel.EmailNotVerifiedException;
import org.iatoki.judgels.jophiel.UserInfo;
import org.iatoki.judgels.jophiel.UserNotFoundException;

import javax.persistence.NoResultException;

public interface UserAccountService {

    String registerUser(String username, String name, String email, String password) throws IllegalStateException;

    String forgotPassword(String username, String email);

    boolean isValidToChangePassword(String code, long currentMillis);

    void changePassword(String code, String password);

    UserInfo login(String usernameOrEmail, String password) throws NoResultException, UserNotFoundException, EmailNotVerifiedException;

    void updatePassword(String userJid, String password);

}
