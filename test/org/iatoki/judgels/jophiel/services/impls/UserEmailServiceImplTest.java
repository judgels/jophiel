package org.iatoki.judgels.jophiel.services.impls;

import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.JudgelsProperties;
import org.iatoki.judgels.jophiel.JophielProperties;
import org.iatoki.judgels.jophiel.models.daos.UserDao;
import org.iatoki.judgels.jophiel.models.daos.UserEmailDao;
import org.iatoki.judgels.jophiel.models.domains.UserEmailModel;
import org.iatoki.judgels.jophiel.models.domains.UserModel;
import org.mockito.InjectMocks;
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
import play.libs.mailer.MailerPlugin;

/**
 * Created by bagus.seto on 6/9/2015.
 */
@PrepareForTest({IdentityUtils.class, JophielProperties.class, JudgelsProperties.class, MailerPlugin.class, Messages.class})
public class UserEmailServiceImplTest extends PowerMockTestCase {

    @Mock
    UserDao userDao;
    @Mock
    UserEmailDao userEmailDao;

    @InjectMocks
    UserEmailServiceImpl userEmailService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(IdentityUtils.class);
        PowerMockito.mockStatic(JudgelsProperties.class);
        PowerMockito.mockStatic(JophielProperties.class);
        PowerMockito.mockStatic(MailerPlugin.class);
        PowerMockito.mockStatic(Messages.class);
    }

    @Test
    public void isEmailOwnedByUser_EmailOwnedByUser_ReturnsTrue() {
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
    public void isEmailOwnedByUser_EmailNotOwnedByUser_ReturnsFalse() {
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
    public void existByEmail_ExistingEmail_ReturnsTrue() {
        String existingEmail = "alice@email.com";
        Mockito.when(userEmailDao.isExistByEmail(existingEmail)).thenReturn(true);

        Assert.assertTrue(userEmailService.existByEmail(existingEmail), "Email not exists");
    }

    @Test
    public void existByEmail_NonExistingEmail_ReturnsFalse() {
        String nonExistingEmail = "alice_email";
        Mockito.when(userEmailDao.isExistByEmail(nonExistingEmail)).thenReturn(false);

        Assert.assertFalse(userEmailService.existByEmail(nonExistingEmail), "Email exists");
    }

    @Test
    public void activateEmail_ExistingUnverifiedEmailCode_ReturnsTrue() {
        String emailCode = "UNVERIFIED_EMAIL_CODE";
        Mockito.when(userEmailDao.isExistByCode(emailCode)).thenReturn(true);

        String getIpAddress = "10.10.10.10";
        Mockito.when(IdentityUtils.getIpAddress()).thenReturn(getIpAddress);

        UserEmailModel userEmailModel = new UserEmailModel();
        userEmailModel.emailVerified = false;
        userEmailModel.userJid = "JID0101";
        userEmailModel.userCreate = "guest";
        userEmailModel.timeCreate = System.currentTimeMillis();
        Mockito.when(userEmailDao.findByCode(emailCode)).thenReturn(userEmailModel);

        Mockito.doAnswer(invocation -> {
            UserEmailModel insideUserEmailModel = (UserEmailModel) invocation.getArguments()[0];
            String userJid = (String) invocation.getArguments()[1];

            insideUserEmailModel.userUpdate = userJid;
            insideUserEmailModel.timeUpdate = System.currentTimeMillis();

            return null;
        }).when(userEmailDao).edit(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        Assert.assertTrue(userEmailService.activateEmail(emailCode), "Invalid email code or email has been activated");
        Assert.assertNotEquals(userEmailModel.userCreate, userEmailModel.userUpdate, "User update not updated");
        Assert.assertTrue(userEmailModel.timeUpdate > userEmailModel.timeCreate, "Time update ot updated");
    }

    @Test
    public void activateEmail_ExistingVerifiedEmailCode_ReturnsFalse() {
        String emailCode = "VERIFIED_EMAIL_CODE";
        Mockito.when(userEmailDao.isExistByCode(emailCode)).thenReturn(true);

        UserEmailModel userEmailModel = new UserEmailModel();
        userEmailModel.emailVerified = true;
        Mockito.when(userEmailDao.findByCode(emailCode)).thenReturn(userEmailModel);

        Assert.assertFalse(userEmailService.activateEmail(emailCode), "Email code has not been activated");
    }

    @Test
    public void activateEmail_NonExistingEmailCode_ReturnsFalse() {
        String emailCode = "INVALID_EMAIL_CODE";
        Mockito.when(userEmailDao.isExistByCode(emailCode)).thenReturn(false);

        Assert.assertFalse(userEmailService.activateEmail(emailCode), "Email code is valid");
    }

    @Test
    public void isEmailNotVerified_UnverifiedUser_ReturnsTrue() {
        String unverifiedUserJid = "JIDU0101";
        Mockito.when(userEmailDao.isExistNotVerifiedByUserJid(unverifiedUserJid)).thenReturn(true);

        Assert.assertTrue(userEmailService.isEmailNotVerified(unverifiedUserJid), "User is verified");
    }

    @Test
    public void isEmailNotVerified_VerifiedUser_ReturnsFalse() {
        String verifiedUserJid = "JIDU1111";
        Mockito.when(userEmailDao.isExistNotVerifiedByUserJid(verifiedUserJid)).thenReturn(false);

        Assert.assertFalse(userEmailService.isEmailNotVerified(verifiedUserJid), "User is not verified");
    }

    @Test
    public void getEmailCodeOfUnverifiedEmail_UnverifiedUser_ReturnsEmailCode() {
        String userJid = "JIDU0101";

        UserEmailModel userEmailModel = new UserEmailModel();
        userEmailModel.emailCode = "JIDU0101_EMAIL_CODE";
        Mockito.when(userEmailDao.findByUserJid(userJid)).thenReturn(userEmailModel);

        String emailCode = userEmailService.getEmailCodeOfUnverifiedEmail(userJid);

        Assert.assertNotNull(emailCode, "Email code must not be null");
    }

    @Test
    public void sendActivationEmail_UnverifiedUser_ActivationEmailSent() {
        String name = "Alice";
        String email = "alice@email.com";
        String link = "https://link.to/activate/email";

        JudgelsProperties judgelsProperties = Mockito.mock(JudgelsProperties.class);
        Mockito.when(JudgelsProperties.getInstance()).thenReturn(judgelsProperties);
        Mockito.when(judgelsProperties.getAppCopyright()).thenReturn("copyright");

        JophielProperties jophielProperties = Mockito.mock(JophielProperties.class);
        Mockito.when(JophielProperties.getInstance()).thenReturn(jophielProperties);
        Mockito.when(jophielProperties.getNoreplyName()).thenReturn("No reply");
        Mockito.when(jophielProperties.getNoreplyEmail()).thenReturn("no-reply@email.com");

        Mockito.when(Messages.get("registrationEmail.userRegistration")).thenReturn("user registration");
        Mockito.when(Messages.get("registrationEmail.thankYou")).thenReturn("thank you");
        Mockito.when(Messages.get("registrationEmail.pleaseActivate")).thenReturn("please activate");

        Email sentMail = new Email();
        Mockito.when(MailerPlugin.send(Mockito.any())).thenAnswer(invocation -> {
            Email insideSentMail = (Email) invocation.getArguments()[0];
            sentMail.setSubject(insideSentMail.getSubject());
            sentMail.setFrom(insideSentMail.getFrom());
            sentMail.setTo(insideSentMail.getTo());
            sentMail.setBodyHtml(insideSentMail.getBodyHtml());

            return null;
        });
        userEmailService.sendActivationEmail(name, email, link);

        Assert.assertNotEquals("", sentMail.getSubject(), "Subject must not be empty");
        Assert.assertNotEquals("", sentMail.getFrom(), "From must not be empty");
        Assert.assertEquals(1, sentMail.getTo().size(), "To must be one element");
        Assert.assertTrue(sentMail.getBodyHtml().contains(link), "Body not contains link");
    }

    @Test
    public void sendChangePasswordEmail_ExistingUser_ChangePasswordEmailSent() {
        String email = "alice@email.com";
        String link = "https://link.to/change/password";

        JudgelsProperties judgelsProperties = Mockito.mock(JudgelsProperties.class);
        Mockito.when(JudgelsProperties.getInstance()).thenReturn(judgelsProperties);
        Mockito.when(judgelsProperties.getAppCopyright()).thenReturn("copyright");

        JophielProperties jophielProperties = Mockito.mock(JophielProperties.class);
        Mockito.when(JophielProperties.getInstance()).thenReturn(jophielProperties);
        Mockito.when(jophielProperties.getNoreplyName()).thenReturn("No reply");
        Mockito.when(jophielProperties.getNoreplyEmail()).thenReturn("no-reply@email.com");

        Mockito.when(Messages.get("forgotPasswordEmail.forgotPassword")).thenReturn("forgot password");
        Mockito.when(Messages.get("registrationEmail.request")).thenReturn("request");
        Mockito.when(Messages.get("registrationEmail.changePassword")).thenReturn("change password");

        Email sentMail = new Email();
        Mockito.when(MailerPlugin.send(Mockito.any())).thenAnswer(invocation -> {
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
