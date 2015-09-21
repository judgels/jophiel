package org.iatoki.judgels.jophiel.services.impls;

import org.iatoki.judgels.jophiel.JophielProperties;
import org.iatoki.judgels.jophiel.UserEmail;
import org.iatoki.judgels.jophiel.UserEmailNotFoundException;
import org.iatoki.judgels.jophiel.models.daos.UserDao;
import org.iatoki.judgels.jophiel.models.daos.UserEmailDao;
import org.iatoki.judgels.jophiel.models.entities.UserEmailModel;
import org.iatoki.judgels.jophiel.models.entities.UserModel;
import org.iatoki.judgels.jophiel.services.UserEmailService;
import org.iatoki.judgels.play.JudgelsPlayProperties;
import play.i18n.Messages;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Named("userEmailService")
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
    public boolean isEmailOwnedByUser(String email, String username) {
        UserModel userModel = userDao.findByUsername(username);
        UserEmailModel emailModel = userEmailDao.findByEmail(email);

        return (emailModel.userJid.equals(userModel.jid));
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
    public String addFirstEmail(String userJid, String email, String userIpAddress) {
        UserEmailModel userEmailModel = UserEmailServiceUtils.persistEmail(userEmailDao, userJid, email, userIpAddress);

        makeEmailPrimary(userJid, userEmailModel.jid, userIpAddress);

        return userEmailModel.emailCode;
    }

    @Override
    public String addEmail(String userJid, String email, String userIpAddress) {
        UserEmailModel userEmailModel = UserEmailServiceUtils.persistEmail(userEmailDao, userJid, email, userIpAddress);

        return userEmailModel.emailCode;
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
    public List<UserEmail> getEmailsByUserJid(String userJid) {
        List<UserEmailModel> userEmailModels = userEmailDao.getByUserJid(userJid);

        return userEmailModels.stream().map(m -> UserEmailServiceUtils.createUserEmailFromModel(m)).collect(Collectors.toList());
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
