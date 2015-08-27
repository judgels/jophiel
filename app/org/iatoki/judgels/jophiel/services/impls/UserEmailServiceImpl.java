package org.iatoki.judgels.jophiel.services.impls;

import org.iatoki.judgels.jophiel.UserEmail;
import org.iatoki.judgels.jophiel.UserEmailNotFoundException;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.JudgelsPlayProperties;
import org.iatoki.judgels.jophiel.JophielProperties;
import org.iatoki.judgels.jophiel.models.daos.UserDao;
import org.iatoki.judgels.jophiel.models.daos.UserEmailDao;
import org.iatoki.judgels.jophiel.models.entities.UserEmailModel;
import org.iatoki.judgels.jophiel.models.entities.UserModel;
import org.iatoki.judgels.jophiel.services.UserEmailService;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import play.i18n.Messages;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
@Named("userEmailService")
public final class UserEmailServiceImpl implements UserEmailService {

    private final UserDao userDao;
    private final UserEmailDao userEmailDao;
    private final MailerClient mailerClient;

    @Inject
    public UserEmailServiceImpl(UserDao userDao, UserEmailDao userEmailDao, MailerClient mailerClient) {
        this.userDao = userDao;
        this.userEmailDao = userEmailDao;
        this.mailerClient = mailerClient;
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
    public String addFirstEmail(String userJid, String email) {
        UserEmailModel userEmailModel = persistEmail(userJid, email);

        makeEmailPrimary(userJid, userEmailModel.jid);

        return userEmailModel.emailCode;
    }

    @Override
    public String addEmail(String userJid, String email) {
        UserEmailModel userEmailModel = persistEmail(userJid, email);

        return userEmailModel.emailCode;
    }

    @Override
    public UserEmail findEmailById(long emailId) throws UserEmailNotFoundException {
        UserEmailModel userEmailModel = userEmailDao.findById(emailId);

        if (userEmailModel == null) {
            throw new UserEmailNotFoundException("User Email Not Found.");
        }

        return createFromModel(userEmailModel);
    }

    @Override
    public UserEmail findEmailByJid(String emailJid) {
        return createFromModel(userEmailDao.findByJid(emailJid));
    }

    @Override
    public List<UserEmail> getEmailsByUserJid(String userJid) {
        List<UserEmailModel> userEmailModels = userEmailDao.getByUserJid(userJid);

        return userEmailModels.stream().map(m -> createFromModel(m)).collect(Collectors.toList());
    }

    @Override
    public void makeEmailPrimary(String userJid, String emailJid) {
        UserModel userModel = userDao.findByJid(userJid);
        userModel.emailJid = emailJid;

        userDao.edit(userModel, userJid, IdentityUtils.getIpAddress());
    }

    @Override
    public void activateEmail(String emailCode) {
        UserEmailModel emailModel = userEmailDao.findByEmailCode(emailCode);
        emailModel.emailVerified = true;

        userEmailDao.edit(emailModel, emailModel.userJid, IdentityUtils.getIpAddress());
    }

    @Override
    public void sendEmailVerification(String name, String email, String link) {
        Email mail = new Email();
        mail.setSubject(JudgelsPlayProperties.getInstance().getAppCopyright() + " " + Messages.get("email.verification"));
        mail.setFrom(JophielProperties.getInstance().getNoreplyName() + " <" + JophielProperties.getInstance().getNoreplyEmail() + ">");
        mail.addTo(name + " <" + email + ">");
        mail.setBodyHtml("<p>" + Messages.get("email.pleaseVerify") + " <a href='" + link + "'>here</a>.</p>");
        mailerClient.send(mail);
    }

    @Override
    public void sendRegistrationEmailActivation(String name, String email, String link) {
        Email mail = new Email();
        mail.setSubject(JudgelsPlayProperties.getInstance().getAppCopyright() + " " + Messages.get("registrationEmail.userRegistration"));
        mail.setFrom(JophielProperties.getInstance().getNoreplyName() + " <" + JophielProperties.getInstance().getNoreplyEmail() + ">");
        mail.addTo(name + " <" + email + ">");
        mail.setBodyHtml("<p>" + Messages.get("registrationEmail.thankYou") + " " + JudgelsPlayProperties.getInstance().getAppCopyright() + ".</p><p>" + Messages.get("registrationEmail.pleaseActivate") + " <a href='" + link + "'>here</a>.</p>");
        mailerClient.send(mail);
    }

    @Override
    public void sendChangePasswordEmail(String email, String link) {
        Email mail = new Email();
        mail.setSubject(JudgelsPlayProperties.getInstance().getAppCopyright() + " " + Messages.get("forgotPasswordEmail.forgotPassword"));
        mail.setFrom(JophielProperties.getInstance().getNoreplyName() + " <" + JophielProperties.getInstance().getNoreplyEmail() + ">");
        mail.addTo(email);
        mail.setBodyHtml("<p>" + Messages.get("forgotPasswordEmail.request") + " " + JudgelsPlayProperties.getInstance().getAppCopyright() + ".</p><p>" + Messages.get("forgotPasswordEmail.changePassword") + " <a href='" + link + "'>here</a>.</p>");
        mailerClient.send(mail);
    }

    @Override
    public void removeEmail(String emailJid) {
        UserEmailModel userEmailModel = userEmailDao.findByJid(emailJid);

        userEmailDao.remove(userEmailModel);
    }

    private UserEmail createFromModel(UserEmailModel userEmailModel) {
        return new UserEmail(userEmailModel.id, userEmailModel.jid, userEmailModel.userJid, userEmailModel.email, userEmailModel.emailVerified);
    }

    private UserEmailModel persistEmail(String userJid, String email) {
        UserEmailModel userEmailModel = new UserEmailModel();
        userEmailModel.email = email;
        userEmailModel.userJid = userJid;
        userEmailModel.emailVerified = false;
        userEmailModel.emailCode = JudgelsPlayUtils.hashMD5(UUID.randomUUID().toString());

        userEmailDao.persist(userEmailModel, userJid, IdentityUtils.getIpAddress());

        return userEmailModel;
    }
}
