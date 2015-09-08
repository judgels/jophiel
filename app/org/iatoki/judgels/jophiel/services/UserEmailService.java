package org.iatoki.judgels.jophiel.services;

import org.iatoki.judgels.jophiel.UserEmail;
import org.iatoki.judgels.jophiel.UserEmailNotFoundException;

import java.util.List;

public interface UserEmailService {

    boolean isEmailOwnedByUser(String email, String username);

    boolean emailExists(String email);

    boolean isEmailCodeValid(String emailCode);

    boolean isEmailNotVerified(String emailJid);

    String getEmailCodeOfUnverifiedEmail(String emailJid);

    String addFirstEmail(String userJid, String email, String userIpAddress);

    String addEmail(String userJid, String email, String userIpAddress);

    UserEmail findEmailById(long emailId) throws UserEmailNotFoundException;

    UserEmail findEmailByJid(String emailJid);

    List<UserEmail> getEmailsByUserJid(String userJid);

    void makeEmailPrimary(String userJid, String emailJid, String userIpAddress);

    void activateEmail(String emailCode, String userIpAddress);

    void sendEmailVerification(String name, String email, String link);

    void sendRegistrationEmailActivation(String name, String email, String link);

    void sendChangePasswordEmail(String email, String link);

    void removeEmail(String emailJid);
}
