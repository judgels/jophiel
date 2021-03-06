package org.iatoki.judgels.jophiel.user.profile.email;

import org.iatoki.judgels.jophiel.JophielProperties;
import org.iatoki.judgels.jophiel.user.UserDao;
import org.iatoki.judgels.jophiel.user.UserModel;
import org.iatoki.judgels.play.JudgelsPlayProperties;
import play.i18n.Messages;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public final class UserEmailServiceImpl implements UserEmailService {

    private final MailerClient mailerClient;
    private final UserDao userDao;
    private final UserEmailDao userEmailDao;

    @Inject
    public UserEmailServiceImpl(MailerClient mailerClient, UserDao userDao, UserEmailDao userEmailDao) {
        this.mailerClient = mailerClient;
        this.userDao = userDao;
        this.userEmailDao = userEmailDao;
    }

    @Override
    public boolean isEmailOwned(String email) {
        return userEmailDao.existsVerifiedEmail(email);
    }

    @Override
    public boolean isEmailOwnedByUser(String email, String username) {
        UserModel userModel = userDao.findByUsername(username);
        UserEmailModel emailModel = userEmailDao.findByEmail(email);

        return emailModel.userJid.equals(userModel.jid) && emailModel.emailVerified;
    }

    @Override
    public boolean emailExists(String email) {
        return userEmailDao.existsByEmail(email);
    }

    @Override
    public boolean isEmailCodeValid(String emailCode) {
        if (!userEmailDao.existsByEmailCode(emailCode)) {
            return false;
        }

        UserEmailModel emailModel = userEmailDao.findByEmailCode(emailCode);
        return !emailModel.emailVerified;
    }

    @Override
    public boolean isEmailNotVerified(String emailJid) {
        return userEmailDao.existsUnverifiedEmailByJid(emailJid);
    }

    @Override
    public String getEmailCodeOfUnverifiedEmail(String emailJid) {
        UserEmailModel userEmailModel = userEmailDao.findByJid(emailJid);
        return userEmailModel.emailCode;
    }

    @Override
    public UserEmail addFirstEmail(String userJid, String email, String userIpAddress) {
        UserEmailModel userEmailModel = UserEmailServiceUtils.persistEmail(userEmailDao, userJid, email, userIpAddress);

        makeEmailPrimary(userJid, userEmailModel.jid, userIpAddress);

        return UserEmailServiceUtils.createUserEmailFromModel(userEmailModel);
    }

    @Override
    public UserEmail addEmail(String userJid, String email, String userIpAddress) {
        UserEmailModel userEmailModel = UserEmailServiceUtils.persistEmail(userEmailDao, userJid, email, userIpAddress);

        return UserEmailServiceUtils.createUserEmailFromModel(userEmailModel);
    }

    @Override
    public UserEmail findEmailById(long emailId) throws UserEmailNotFoundException {
        UserEmailModel userEmailModel = userEmailDao.findById(emailId);

        if (userEmailModel == null) {
            throw new UserEmailNotFoundException("User Email Not Found.");
        }

        return UserEmailServiceUtils.createUserEmailFromModel(userEmailModel);
    }

    @Override
    public UserEmail findEmailByJid(String emailJid) {
        return UserEmailServiceUtils.createUserEmailFromModel(userEmailDao.findByJid(emailJid));
    }

    @Override
    public UserEmail findEmailByCode(String emailCode) {
        UserEmailModel emailModel = userEmailDao.findByEmailCode(emailCode);

        return UserEmailServiceUtils.createUserEmailFromModel(emailModel);
    }

    @Override
    public List<UserEmail> getEmailsByUserJid(String userJid) {
        List<UserEmailModel> userEmailModels = userEmailDao.getByUserJid(userJid);

        return userEmailModels
                .stream()
                .map(UserEmailServiceUtils::createUserEmailFromModel)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<UserEmail>> getEmailsMapByUserJids(Collection<String> userJids) {
        List<UserEmailModel> userEmailModels = userEmailDao.getByUserJids(userJids);

        return userEmailModels
                .stream()
                .map(UserEmailServiceUtils::createUserEmailFromModel)
                .collect(Collectors.groupingBy(UserEmail::getUserJid));
    }

    @Override
    public void makeEmailPrimary(String userJid, String emailJid, String userIpAddress) {
        UserModel userModel = userDao.findByJid(userJid);
        userModel.emailJid = emailJid;

        userDao.edit(userModel, userJid, userIpAddress);
    }

    @Override
    public void activateEmail(String emailCode, String userIpAddress) {
        UserEmailModel emailModel = userEmailDao.findByEmailCode(emailCode);
        emailModel.emailVerified = true;

        userEmailDao.edit(emailModel, emailModel.userJid, userIpAddress);
    }

    @Override
    public void sendEmailVerification(String name, String email, String link) {
        Email mail = new Email();
        mail.setSubject(Messages.get("email.verify.email.subject", JudgelsPlayProperties.getInstance().getAppCopyright()));
        mail.setFrom(JophielProperties.getInstance().getNoreplyName() + " <" + JophielProperties.getInstance().getNoreplyEmail() + ">");
        mail.addTo(name + " <" + email + ">");
        mail.setBodyHtml(Messages.get("email.verify.email.body", JudgelsPlayProperties.getInstance().getAppCopyright(), link));
        mailerClient.send(mail);
    }

    @Override
    public void sendRegistrationEmailActivation(String name, String email, String link) {
        Email mail = new Email();
        mail.setSubject(Messages.get("register.email.subject", JudgelsPlayProperties.getInstance().getAppCopyright()));
        mail.setFrom(JophielProperties.getInstance().getNoreplyName() + " <" + JophielProperties.getInstance().getNoreplyEmail() + ">");
        mail.addTo(name + " <" + email + ">");
        mail.setBodyHtml(Messages.get("register.email.body", JudgelsPlayProperties.getInstance().getAppCopyright(), link));
        mailerClient.send(mail);
    }

    @Override
    public void sendChangePasswordEmail(String email, String link) {
        Email mail = new Email();
        mail.setSubject(Messages.get("forgotPassword.email.subject", JudgelsPlayProperties.getInstance().getAppCopyright()));
        mail.setFrom(JophielProperties.getInstance().getNoreplyName() + " <" + JophielProperties.getInstance().getNoreplyEmail() + ">");
        mail.addTo(email);
        mail.setBodyHtml(Messages.get("forgotPassword.email.body", JudgelsPlayProperties.getInstance().getAppCopyright(), link));
        mailerClient.send(mail);
    }

    @Override
    public void removeEmail(String emailJid) {
        UserEmailModel userEmailModel = userEmailDao.findByJid(emailJid);

        userEmailDao.remove(userEmailModel);
    }
}
