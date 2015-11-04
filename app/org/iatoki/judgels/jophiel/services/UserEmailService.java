package org.iatoki.judgels.jophiel.services;

import org.iatoki.judgels.jophiel.UserEmail;
import org.iatoki.judgels.jophiel.UserEmailNotFoundException;

import java.util.List;

public interface UserEmailService {

    boolean isEmailOwned(String email);

    boolean isEmailOwnedByUser(String email, String username);

    boolean emailExists(String email);

    boolean isEmailCodeValid(String emailCode);

    boolean isEmailNotVerified(String emailJid);

    String getEmailCodeOfUnverifiedEmail(String emailJid);

    UserEmail addFirstEmail(String userJid, String email, String userIpAddress);

    UserEmail addEmail(String userJid, String email, String userIpAddress);

    UserEmail findEmailById(long emailId) throws UserEmailNotFoundException;

    UserEmail findEmailByJid(String emailJid);

    UserEmail findEmailByCode(String emailCode);

    List<UserEmail> getEmailsByUserJid(String userJid);

    void makeEmailPrimary(String userJid, String emailJid, String userIpAddress);

    void activateEmail(String emailCode, String userIpAddress);

    void sendEmailVerification(String name, String email, String link);

    void sendRegistrationEmailActivation(String name, String email, String link);

    void sendChangePasswordEmail(String email, String link);

    void removeEmail(String emailJid);
}
