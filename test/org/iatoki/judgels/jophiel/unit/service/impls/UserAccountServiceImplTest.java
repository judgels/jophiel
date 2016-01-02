package org.iatoki.judgels.jophiel.unit.service.impls;

import org.iatoki.judgels.jophiel.PasswordHash;
import org.iatoki.judgels.jophiel.user.User;
import org.iatoki.judgels.jophiel.user.UserDao;
import org.iatoki.judgels.jophiel.user.UserModel;
import org.iatoki.judgels.jophiel.user.UserNotFoundException;
import org.iatoki.judgels.jophiel.user.account.UserAccountServiceImpl;
import org.iatoki.judgels.jophiel.user.account.UserForgotPasswordDao;
import org.iatoki.judgels.jophiel.user.account.UserForgotPasswordModel;
import org.iatoki.judgels.jophiel.user.profile.email.EmailNotVerifiedException;
import org.iatoki.judgels.jophiel.user.profile.email.UserEmailDao;
import org.iatoki.judgels.jophiel.user.profile.email.UserEmailModel;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.model.AbstractModel;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import play.mvc.Http;

import javax.validation.ConstraintViolationException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by bagus.seto on 6/3/2015.
 */
@PrepareForTest(Http.Context.class)
public class UserAccountServiceImplTest extends PowerMockTestCase {

    @Mock
    private UserDao userDao;
    @Mock
    private UserEmailDao userEmailDao;
    @Mock
    private UserForgotPasswordDao userForgotPasswordDao;

    private UserAccountServiceImpl userAccountService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        userAccountService = new UserAccountServiceImpl(userDao, userEmailDao, userForgotPasswordDao);
    }

    @Test
    public void registerUserNewUserReturnsEmailActivationCode() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String username = "alice123";
        String name = "Alice";
        String email = "alice@email.com";
        String password = "alicepassword";

        String getIpAddress = "10.10.10.10";

        UserModel userModel = new UserModel();
        Mockito.doAnswer(invocation -> {
                UserModel insideUserModel = (UserModel) invocation.getArguments()[0];
                insideUserModel.jid = "JIDU0101";

                userModel.jid = insideUserModel.jid;
                userModel.username = insideUserModel.username;
                userModel.name = insideUserModel.name;
                userModel.password = insideUserModel.password;
                userModel.profilePictureImageName = insideUserModel.profilePictureImageName;
                userModel.roles = insideUserModel.roles;

                persistAbstractModel(userModel, invocation);

                return null;
            }).when(userDao).persist(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        UserEmailModel userEmailModel = new UserEmailModel();
        Mockito.doAnswer(invocation -> {
                UserEmailModel insideUserEmailModel = (UserEmailModel) invocation.getArguments()[0];

                userEmailModel.email = insideUserEmailModel.email;
                userEmailModel.emailCode = insideUserEmailModel.emailCode;
                userEmailModel.emailVerified = insideUserEmailModel.emailVerified;
                userEmailModel.userJid = insideUserEmailModel.userJid;

                persistAbstractModel(userEmailModel, invocation);

                return null;
            }).when(userEmailDao).persist(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        String emailActivationCode = userAccountService.registerUser(username, name, email, password, getIpAddress);

        Assert.assertNotNull(emailActivationCode, "Email activation code must not be null");
        Assert.assertTrue(PasswordHash.validatePassword(password, userModel.password), "Password not hashed");
        Assert.assertEquals("avatar-default.png", userModel.profilePictureImageName, "Profile picture not avatar-default.png");
        Assert.assertEquals("user", userModel.roles, "Roles not user");
        Assert.assertEquals("guest", userModel.userCreate, "UserInfo create must be guest");
        Assert.assertNotNull(userModel.ipCreate, "IP create must not be null");
        Assert.assertEquals("guest", userEmailModel.userCreate, "UserInfo Email create must be guest");
        Assert.assertNotNull(userEmailModel.ipCreate, "UserInfo Email IP create must not be null");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void registerUserNewUserExistingUsernameThrowsIllegalStateException() {
        String username = "alice123";
        String name = "A Lice";
        String email = "a_lice@email.com";
        String password = "a_licepassword";

        String getIpAddress = "10.10.10.10";

        Mockito.doThrow(ConstraintViolationException.class).when(userDao).persist(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        String emailActivationCode = userAccountService.registerUser(username, name, email, password, getIpAddress);

        Assert.fail("Unreachable");
    }

    @Test
    public void forgotPasswordExistingUserReturnsForgotPasswordCode() {
        String username = "alice123";
        String email = "alice@email.com";

        String ipAddress = "10.10.10.10";

        UserModel userModel = new UserModel();
        userModel.jid = "JIDU0101";
        Mockito.when(userDao.findByUsername(username)).thenReturn(userModel);

        UserForgotPasswordModel userForgotPasswordModel = new UserForgotPasswordModel();
        Mockito.doAnswer(invocation -> {
                UserForgotPasswordModel insideUserForgotPasswordModel = (UserForgotPasswordModel) invocation.getArguments()[0];

                userForgotPasswordModel.id = 1L;
                userForgotPasswordModel.userJid = insideUserForgotPasswordModel.userJid;
                userForgotPasswordModel.code = insideUserForgotPasswordModel.code;
                userForgotPasswordModel.used = insideUserForgotPasswordModel.used;

                persistAbstractModel(userForgotPasswordModel, invocation);

                return null;
            }).when(userForgotPasswordDao).persist(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        String forgotPasswordCode = userAccountService.generateForgotPasswordRequest(username, email, "127.0.0.1");

        Assert.assertNotNull(forgotPasswordCode, "Forgot password code must not be null");
        Assert.assertEquals(forgotPasswordCode, userForgotPasswordModel.code, "Forgot password code not equal");
        Assert.assertFalse(userForgotPasswordModel.used, "Forgot password code has been used");
    }

    @Test
    public void existForgotPassByCodeExistingCodeReturnsTrue() {
        String code = "FORGOT_PASS_CODE";

        Mockito.when(userForgotPasswordDao.isForgotPasswordCodeValid(Mockito.eq(code), Mockito.anyInt())).thenReturn(true);

        Assert.assertTrue(userAccountService.isValidToChangePassword(code, 0), "Forgot password code not exist");
    }

    @Test
    public void existForgotPassByCodeNonExistingCodeReturnsFalse() {
        String code = "NOT_FORGOT_PASS_CODE";

        Mockito.when(userForgotPasswordDao.isForgotPasswordCodeValid(Mockito.eq(code), Mockito.anyInt())).thenReturn(false);

        Assert.assertFalse(userAccountService.isValidToChangePassword(code, 0), "Forgot password code exist");
    }

    @Test
    public void changePasswordValidCodePasswordChanged() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String code = "FORGOT_PASS_CODE";
        String newPassword = "alice456";

        String ipAddress = "10.10.10.10";

        UserForgotPasswordModel userForgotPasswordModel = new UserForgotPasswordModel();
        userForgotPasswordModel.userJid = "JIDU0101";
        userForgotPasswordModel.code = code;
        userForgotPasswordModel.used = false;
        userForgotPasswordModel.timeCreate = System.currentTimeMillis();
        Mockito.when(userForgotPasswordDao.findByForgotPasswordCode(code)).thenReturn(userForgotPasswordModel);
        Mockito.doAnswer(invocation -> {
                UserForgotPasswordModel insideUserForgotPasswordModel = (UserForgotPasswordModel) invocation.getArguments()[0];

                editAbstractModel(insideUserForgotPasswordModel, invocation);

                return null;
            }).when(userForgotPasswordDao).edit(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        UserModel userModel = new UserModel();
        userModel.password = "alice123";
        userModel.timeCreate = System.currentTimeMillis();
        Mockito.when(userDao.findByJid(userForgotPasswordModel.userJid)).thenReturn(userModel);
        Mockito.doAnswer(invocation -> {
                UserModel insideUserModel = (UserModel) invocation.getArguments()[0];

                editAbstractModel(insideUserModel, invocation);

                return null;
            }).when(userDao).edit(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        userAccountService.processChangePassword(code, newPassword, "127.0.0.1");

        Assert.assertTrue(userForgotPasswordModel.used, "Forgot password code not used");
        Assert.assertTrue(userForgotPasswordModel.timeUpdate > userForgotPasswordModel.timeCreate, "Forgot password not updated");
        Assert.assertTrue(PasswordHash.validatePassword(newPassword, userModel.password), "UserInfo password not changed");
        Assert.assertTrue(userModel.timeUpdate > userModel.timeCreate, "UserInfo password not updated");
    }

    @Test
    public void loginValidUserByUsernameReturnsUser() throws UserNotFoundException, EmailNotVerifiedException {
        String username = "alice123";
        String password = "alicepassword";

        String ipAddress = "10.10.10.10";

        UserModel userModel = new UserModel();
        userModel.jid = "JIDU0101";
        userModel.username = username;
        userModel.name = "Alice";
        userModel.password = JudgelsPlayUtils.hashSHA256(password);
        userModel.emailJid = "JIDE0101";
        userModel.profilePictureImageName = "avatar-default.png";
        userModel.roles = "user";
        Mockito.when(userDao.existByUsername(username)).thenReturn(true);
        Mockito.when(userDao.findByUsername(username)).thenReturn(userModel);

        UserEmailModel userEmailModel = new UserEmailModel();
        userEmailModel.jid = userModel.emailJid;
        userEmailModel.userJid = userModel.jid;
        userEmailModel.email = "alice@email.com";
        userEmailModel.emailVerified = true;
        Mockito.when(userEmailDao.findByJid(userEmailModel.jid)).thenReturn(userEmailModel);

        Http.Request request = Mockito.mock(Http.Request.class);
        Mockito.when(request.secure()).thenReturn(false);

        Http.Context context = Mockito.mock(Http.Context.class);
        PowerMockito.mockStatic(Http.Context.class);
        Mockito.when(Http.Context.current()).thenReturn(context);
        Mockito.when(context.request()).thenReturn(request);

        User user = userAccountService.processLogin(username, password, ipAddress);

        Assert.assertNotNull(user, "UserInfo must not be null");
        Assert.assertEquals(username, user.getUsername(), "Username not equals");
    }

    @Test
    public void loginValidUserByEmailReturnsUser() throws UserNotFoundException, EmailNotVerifiedException {
        String email = "alice@email.com";
        String password = "alicepassword";

        String ipAddress = "10.10.10.10";

        UserEmailModel userEmailModel = new UserEmailModel();
        userEmailModel.jid = "JIDE0101";
        userEmailModel.userJid = "JIDU0101";
        userEmailModel.email = email;
        userEmailModel.emailVerified = true;
        Mockito.when(userDao.existByUsername(email)).thenReturn(false);
        Mockito.when(userEmailDao.existsByEmail(email)).thenReturn(true);
        Mockito.when(userEmailDao.findByEmail(email)).thenReturn(userEmailModel);

        UserModel userModel = new UserModel();
        userModel.jid = userEmailModel.userJid;
        userModel.username = "alice";
        userModel.name = "Alice";
        userModel.password = JudgelsPlayUtils.hashSHA256(password);
        userModel.profilePictureImageName = "avatar-default.png";
        userModel.emailJid = "JIDE0101";
        userModel.roles = "user";
        Mockito.when(userDao.findByJid(userModel.jid)).thenReturn(userModel);

        Http.Request request = Mockito.mock(Http.Request.class);
        Mockito.when(request.secure()).thenReturn(false);

        Http.Context context = Mockito.mock(Http.Context.class);
        PowerMockito.mockStatic(Http.Context.class);
        Mockito.when(Http.Context.current()).thenReturn(context);
        Mockito.when(context.request()).thenReturn(request);

        User user = userAccountService.processLogin(email, password, ipAddress);

        Assert.assertNotNull(user, "UserInfo must not be null");
        Assert.assertEquals(userEmailModel.jid, user.getEmailJid(), "Email not equals");
    }

    @Test(expectedExceptions = UserNotFoundException.class)
    public void loginInvalidUserThrowsUserNotFoundException() throws UserNotFoundException, EmailNotVerifiedException {
        String username = "bob";
        String password = "bobpassword";

        String ipAddress = "10.10.10.10";

        Mockito.when(userDao.existByUsername(username)).thenReturn(false);
        Mockito.when(userEmailDao.existsByEmail(username)).thenReturn(false);

        User user = userAccountService.processLogin(username, password, ipAddress);

        Assert.fail("Unreachable");
    }

    @Test(expectedExceptions = EmailNotVerifiedException.class)
    public void loginValidUserUnverifiedEmailThrowsEmailNotVerifiedException() throws UserNotFoundException, EmailNotVerifiedException {
        String username = "alice123";
        String password = "alicepassword";

        String ipAddress = "10.10.10.10";

        UserModel userModel = new UserModel();
        userModel.jid = "JIDU0101";
        userModel.username = username;
        userModel.password = JudgelsPlayUtils.hashSHA256(password);
        userModel.emailJid = "JIDE0101";
        Mockito.when(userDao.existByUsername(username)).thenReturn(true);
        Mockito.when(userDao.findByUsername(username)).thenReturn(userModel);

        UserEmailModel userEmailModel = new UserEmailModel();
        userEmailModel.jid = userModel.emailJid;
        userEmailModel.userJid = userModel.jid;
        userEmailModel.emailVerified = false;
        Mockito.when(userEmailDao.findByJid(userModel.emailJid)).thenReturn(userEmailModel);

        User user = userAccountService.processLogin(username, password, ipAddress);

        Assert.fail("Unreachable");
    }

    @Test
    public void loginValidUsernameInvalidPasswordReturnsNull() throws UserNotFoundException, EmailNotVerifiedException {
        String username = "alice123";
        String password = "bobpassword";
        String validPassword = "alicepassword";

        String ipAddress = "10.10.10.10";

        UserModel userModel = new UserModel();
        userModel.jid = "JIDU0101";
        userModel.username = username;
        userModel.password = JudgelsPlayUtils.hashSHA256(validPassword);
        userModel.emailJid = "JIDE0101";
        Mockito.when(userDao.existByUsername(username)).thenReturn(true);
        Mockito.when(userDao.findByUsername(username)).thenReturn(userModel);

        UserEmailModel userEmailModel = new UserEmailModel();
        userEmailModel.jid = "JIDE0101";
        userEmailModel.userJid = userModel.jid;
        Mockito.when(userEmailDao.findByJid(userModel.emailJid)).thenReturn(userEmailModel);

        User user = userAccountService.processLogin(username, password, ipAddress);

        Assert.assertNull(user, "UserInfo not null");
    }

    @Test
    public void updatePasswordValidUserNewPasswordPasswordUpdated() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String userJid = "JIDU0101";
        String newPassword = "newalicepassword";

        String ipAddress = "10.10.10.10";

        UserModel userModel = new UserModel();
        userModel.jid = userJid;
        userModel.password = "alicepassword";
        userModel.userCreate = "guest";
        userModel.timeCreate = System.currentTimeMillis();
        Mockito.when(userDao.findByJid(userJid)).thenReturn(userModel);

        Mockito.doAnswer(invocation -> {
                UserModel insideUserModel = (UserModel) invocation.getArguments()[0];

                editAbstractModel(insideUserModel, invocation);

                return null;
            }).when(userDao).edit(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        userAccountService.updateUserPassword(userJid, newPassword, ipAddress);

        Assert.assertTrue(PasswordHash.validatePassword(newPassword, userModel.password), "Password not changed");
        Assert.assertNotEquals(userModel.userCreate, userModel.userUpdate, "UserInfo update not updated");
        Assert.assertTrue(userModel.timeUpdate > userModel.timeCreate, "Time update not updated");
    }

    private void persistAbstractModel(AbstractModel abstractModel, InvocationOnMock invocation) {
        String user = (String) invocation.getArguments()[1];
        String ipAddress = (String) invocation.getArguments()[2];

        abstractModel.userCreate = user;
        abstractModel.ipCreate = ipAddress;
        abstractModel.timeCreate = System.currentTimeMillis();

        abstractModel.userUpdate = user;
        abstractModel.ipUpdate = ipAddress;
        abstractModel.timeUpdate = abstractModel.timeCreate;
    }

    private void editAbstractModel(AbstractModel abstractModel, InvocationOnMock invocation) {
        String user = (String) invocation.getArguments()[1];
        String ipAddress = (String) invocation.getArguments()[2];

        abstractModel.userUpdate = user;
        abstractModel.ipUpdate = ipAddress;
        abstractModel.timeUpdate = System.currentTimeMillis();
    }
}
