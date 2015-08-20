package org.iatoki.judgels.jophiel.services;

public interface UserEmailService {

    boolean isEmailOwnedByUser(String email, String username);

    boolean emailExists(String email);

    boolean isEmailCodeValid(String emailCode);

    boolean isEmailNotVerified(String userJid);

    String getEmailCodeOfUnverifiedEmail(String userJid);

    void activateEmail(String emailCode);

    void sendActivationEmail(String name, String email, String link);

    void sendChangePasswordEmail(String email, String link);
}
