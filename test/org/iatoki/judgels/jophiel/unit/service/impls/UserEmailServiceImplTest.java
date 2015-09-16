package org.iatoki.judgels.jophiel.unit.service.impls;

import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.JudgelsPlayProperties;
import org.iatoki.judgels.jophiel.JophielProperties;
import org.iatoki.judgels.jophiel.models.daos.UserDao;
import org.iatoki.judgels.jophiel.models.daos.UserEmailDao;
import org.iatoki.judgels.jophiel.models.entities.UserEmailModel;
import org.iatoki.judgels.jophiel.models.entities.UserModel;
import org.iatoki.judgels.jophiel.services.impls.UserEmailServiceImpl;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import play.i18n.Messages;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;
import play.libs.mailer.MailerPlugin;

/**
 * Created by bagus.seto on 6/9/2015.
 */
@PrepareForTest({IdentityUtils.class, JophielProperties.class, JudgelsPlayProperties.class, MailerPlugin.class, Messages.class})
public class UserEmailServiceImplTest extends PowerMockTestCase {

    @Mock
    private UserDao userDao;
    @Mock
    private UserEmailDao userEmailDao;
    @Mock
    private MailerClient mailerClient;

    private UserEmailServiceImpl userEmailService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(IdentityUtils.class);
        PowerMockito.mockStatic(JudgelsPlayProperties.class);
        PowerMockito.mockStatic(JophielProperties.class);
        PowerMockito.mockStatic(Messages.class);

        userEmailService = new UserEmailServiceImpl(mailerClient, userDao, userEmailDao);
    }

    @Test
    public void isEmailOwnedByUserEmailOwnedByUserReturnsTrue() {
        String email = "alice@email.com";
        String username = "alice123";
        String userJid = "JIDU0101";

        UserModel userModel = new UserModel();
        userModel.jid = userJid;
        Mockito.when(userDao.findByUsername(username)).thenReturn(userModel);

        UserEmailModel userEmailModel = new UserEmailModel();
        userEmailModel.userJid = userJid;
        Mockito.when(userEmailDao.findByEmail(email)).thenReturn(userEmailModel);

        Assert.assertTrue(userEmailService.isEmailOwnedByUser(email, username), "Email not owned by user");
    }

    @Test
    public void isEmailOwnedByUserEmailNotOwnedByUserReturnsFalse() {
        String email = "bob@email.com";
        String username = "alice123";
        String userJid = "JIDU0101";
        String emailJid = "JIDU0111";

        UserModel userModel = new UserModel();
        userModel.jid = userJid;
        Mockito.when(userDao.findByUsername(username)).thenReturn(userModel);

        UserEmailModel userEmailModel = new UserEmailModel();
        userEmailModel.userJid = emailJid;
        Mockito.when(userEmailDao.findByEmail(email)).thenReturn(userEmailModel);

        Assert.assertFalse(userEmailService.isEmailOwnedByUser(email, username), "Email owned by user");
    }

    @Test
    public void existByEmailExistingEmailReturnsTrue() {
        String existingEmail = "alice@email.com";
        Mockito.when(userEmailDao.existsByEmail(existingEmail)).thenReturn(true);

        Assert.assertTrue(userEmailService.emailExists(existingEmail), "Email not exists");
    }

    @Test
    public void existByEmailNonExistingEmailReturnsFalse() {
        String nonExistingEmail = "alice_email";
        Mockito.when(userEmailDao.existsByEmail(nonExistingEmail)).thenReturn(false);

        Assert.assertFalse(userEmailService.emailExists(nonExistingEmail), "Email exists");
    }

    @Test
    public void activateEmailExistingUnverifiedEmailCodeEmailVerified() {
        String emailCode = "UNVERIFIED_EMAIL_CODE";
        Mockito.when(userEmailDao.existsByEmailCode(emailCode)).thenReturn(true);

        String getIpAddress = "10.10.10.10";
        Mockito.when(IdentityUtils.getIpAddress()).thenReturn(getIpAddress);

        UserEmailModel userEmailModel = new UserEmailModel();
        userEmailModel.emailVerified = false;
        userEmailModel.userJid = "JID0101";
        userEmailModel.userCreate = "guest";
        userEmailModel.timeCreate = System.currentTimeMillis();
        Mockito.when(userEmailDao.findByEmailCode(emailCode)).thenReturn(userEmailModel);

        Mockito.doAnswer(invocation -> {
                UserEmailModel insideUserEmailModel = (UserEmailModel) invocation.getArguments()[0];
                String userJid = (String) invocation.getArguments()[1];

                insideUserEmailModel.userUpdate = userJid;
                insideUserEmailModel.timeUpdate = System.currentTimeMillis();

                return null;
            }).when(userEmailDao).edit(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        userEmailService.activateEmail(emailCode, getIpAddress);

        Assert.assertTrue(userEmailModel.emailVerified, "Invalid email code or email has been activated");
        Assert.assertNotEquals(userEmailModel.userCreate, userEmailModel.userUpdate, "UserInfo update not updated");
        Assert.assertTrue(userEmailModel.timeUpdate > userEmailModel.timeCreate, "Time update ot updated");
    }

    @Test
    public void isEmailCodeValidExistingVerifiedEmailCodeReturnsFalse() {
        String emailCode = "VERIFIED_EMAIL_CODE";
        Mockito.when(userEmailDao.existsByEmailCode(emailCode)).thenReturn(true);

        UserEmailModel userEmailModel = new UserEmailModel();
        userEmailModel.emailVerified = true;
        Mockito.when(userEmailDao.findByEmailCode(emailCode)).thenReturn(userEmailModel);

        Assert.assertFalse(userEmailService.isEmailCodeValid(emailCode), "Email code has not been activated");
    }

    @Test
    public void isEmailCodeValidNonExistingEmailCodeReturnsFalse() {
        String emailCode = "INVALID_EMAIL_CODE";
        Mockito.when(userEmailDao.existsByEmailCode(emailCode)).thenReturn(false);

        Assert.assertFalse(userEmailService.isEmailCodeValid(emailCode), "Email code is valid");
    }

    @Test
    public void isEmailNotVerifiedUnverifiedUserReturnsTrue() {
        String unverifiedUserJid = "JIDU0101";
        Mockito.when(userEmailDao.existsUnverifiedEmailByJid(unverifiedUserJid)).thenReturn(true);

        Assert.assertTrue(userEmailService.isEmailNotVerified(unverifiedUserJid), "UserInfo is verified");
    }

    @Test
    public void isEmailNotVerifiedVerifiedUserReturnsFalse() {
        String verifiedUserJid = "JIDU1111";
        Mockito.when(userEmailDao.existsUnverifiedEmailByJid(verifiedUserJid)).thenReturn(false);

        Assert.assertFalse(userEmailService.isEmailNotVerified(verifiedUserJid), "UserInfo is not verified");
    }

    @Test
    public void getEmailCodeOfUnverifiedEmailUnverifiedUserReturnsEmailCode() {
        String emailJid = "JIDU0101";

        UserEmailModel userEmailModel = new UserEmailModel();
        userEmailModel.jid = emailJid;
        userEmailModel.emailCode = "JIDU0101_EMAIL_CODE";
        Mockito.when(userEmailDao.findByJid(emailJid)).thenReturn(userEmailModel);

        String emailCode = userEmailService.getEmailCodeOfUnverifiedEmail(emailJid);

        Assert.assertNotNull(emailCode, "Email code must not be null");
    }

    @Test
    public void sendActivationEmailUnverifiedUserActivationEmailSent() {
        String name = "Alice";
        String email = "alice@email.com";
        String link = "https://link.to/activate/email";

        JudgelsPlayProperties judgelsPlayProperties = Mockito.mock(JudgelsPlayProperties.class);
        Mockito.when(JudgelsPlayProperties.getInstance()).thenReturn(judgelsPlayProperties);
        Mockito.when(judgelsPlayProperties.getAppCopyright()).thenReturn("copyright");

        JophielProperties jophielProperties = Mockito.mock(JophielProperties.class);
        Mockito.when(JophielProperties.getInstance()).thenReturn(jophielProperties);
        Mockito.when(jophielProperties.getNoreplyName()).thenReturn("No reply");
        Mockito.when(jophielProperties.getNoreplyEmail()).thenReturn("no-reply@email.com");

        Mockito.when(Messages.get("registrationEmail.userRegistration")).thenReturn("user registration");
        Mockito.when(Messages.get("registrationEmail.thankYou")).thenReturn("thank you");
        Mockito.when(Messages.get("registrationEmail.pleaseActivate")).thenReturn("please activate");

        Email sentMail = new Email();
        Mockito.when(mailerClient.send(Mockito.any())).thenAnswer(invocation -> {
                Email insideSentMail = (Email) invocation.getArguments()[0];
                sentMail.setSubject(insideSentMail.getSubject());
                sentMail.setFrom(insideSentMail.getFrom());
                sentMail.setTo(insideSentMail.getTo());
                sentMail.setBodyHtml(insideSentMail.getBodyHtml());

                return null;
            });
        userEmailService.sendRegistrationEmailActivation(name, email, link);

        Assert.assertNotEquals("", sentMail.getSubject(), "Subject must not be empty");
        Assert.assertNotEquals("", sentMail.getFrom(), "From must not be empty");
        Assert.assertEquals(1, sentMail.getTo().size(), "To must be one element");
        Assert.assertTrue(sentMail.getBodyHtml().contains(link), "Body not contains link");
    }

    @Test
    public void sendChangePasswordEmailExistingUserChangePasswordEmailSent() {
        String email = "alice@email.com";
        String link = "https://link.to/change/password";

        JudgelsPlayProperties judgelsPlayProperties = Mockito.mock(JudgelsPlayProperties.class);
        Mockito.when(JudgelsPlayProperties.getInstance()).thenReturn(judgelsPlayProperties);
        Mockito.when(judgelsPlayProperties.getAppCopyright()).thenReturn("copyright");

        JophielProperties jophielProperties = Mockito.mock(JophielProperties.class);
        Mockito.when(JophielProperties.getInstance()).thenReturn(jophielProperties);
        Mockito.when(jophielProperties.getNoreplyName()).thenReturn("No reply");
        Mockito.when(jophielProperties.getNoreplyEmail()).thenReturn("no-reply@email.com");

        Mockito.when(Messages.get("forgotPasswordEmail.forgotPassword")).thenReturn("forgot password");
        Mockito.when(Messages.get("registrationEmail.request")).thenReturn("request");
        Mockito.when(Messages.get("registrationEmail.changePassword")).thenReturn("change password");

        Email sentMail = new Email();
        Mockito.when(mailerClient.send(Mockito.any())).thenAnswer(invocation -> {
                Email insideSentMail = (Email) invocation.getArguments()[0];
                sentMail.setSubject(insideSentMail.getSubject());
                sentMail.setFrom(insideSentMail.getFrom());
                sentMail.setTo(insideSentMail.getTo());
                sentMail.setBodyHtml(insideSentMail.getBodyHtml());

                return null;
            });
        userEmailService.sendChangePasswordEmail(email, link);

        Assert.assertNotEquals("", sentMail.getSubject(), "Subject must not be empty");
        Assert.assertNotEquals("", sentMail.getFrom(), "From must not be empty");
        Assert.assertEquals(1, sentMail.getTo().size(), "To must be one element");
        Assert.assertTrue(sentMail.getBodyHtml().contains(link), "Body not contains link");
    }
}
