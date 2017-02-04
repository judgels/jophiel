package org.iatoki.judgels.jophiel.user.profile.email;

import com.google.inject.ImplementedBy;

import java.util.List;
import java.util.Optional;

@ImplementedBy(UserEmailServiceImpl.class)
public interface UserEmailService {

    boolean isEmailOwned(String email);

    boolean isEmailOwnedByUser(String email, String username);

    boolean emailExists(String email);

    boolean isEmailCodeValid(String emailCode);

    boolean isEmailNotVerified(String emailJid);

    Optional<String> getEmailCodeOfUnverifiedEmail(String emailJid);

    UserEmail addFirstEmail(String userJid, String email, String userIpAddress);

    UserEmail addEmail(String userJid, String email, String userIpAddress);

    Optional<UserEmail> findEmailById(long emailId);

    Optional<UserEmail> findEmailByJid(String emailJid);

    Optional<UserEmail> findEmailByCode(String emailCode);

    List<UserEmail> getEmailsByUserJid(String userJid);

    void makeEmailPrimary(String userJid, String emailJid, String userIpAddress);

    void activateEmail(String emailCode, String userIpAddress);

    void sendEmailVerification(String name, String email, String link);

    void sendRegistrationEmailActivation(String name, String email, String link);

    void sendChangePasswordEmail(String email, String link);

    void removeEmail(String emailJid);
}
