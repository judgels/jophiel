package org.iatoki.judgels.jophiel.unit.service.impls;

import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.JudgelsUtils;
import org.iatoki.judgels.play.models.domains.AbstractModel;
import org.iatoki.judgels.jophiel.EmailNotVerifiedException;
import org.iatoki.judgels.jophiel.PasswordHash;
import org.iatoki.judgels.jophiel.UserInfo;
import org.iatoki.judgels.jophiel.UserNotFoundException;
import org.iatoki.judgels.jophiel.models.daos.UserDao;
import org.iatoki.judgels.jophiel.models.daos.UserEmailDao;
import org.iatoki.judgels.jophiel.models.daos.UserForgotPasswordDao;
import org.iatoki.judgels.jophiel.models.entities.UserEmailModel;
import org.iatoki.judgels.jophiel.models.entities.UserForgotPasswordModel;
import org.iatoki.judgels.jophiel.models.entities.UserModel;
import org.iatoki.judgels.jophiel.services.impls.UserAccountServiceImpl;
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
@PrepareForTest({IdentityUtils.class, Http.Context.class})
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

        PowerMockito.mockStatic(IdentityUtils.class);

        userAccountService = new UserAccountServiceImpl(userDao, userEmailDao, userForgotPasswordDao);
    }

    @Test
    public void registerUser_NewUser_ReturnsEmailActivationCode() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String username = "alice123";
        String name = "Alice";
        String email = "alice@email.com";
        String password = "alicepassword";

        String getIpAddress = "10.10.10.10";
        Mockito.when(IdentityUtils.getIpAddress()).thenReturn(getIpAddress);

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

        String emailActivationCode = userAccountService.registerUser(username, name, email, password);

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
    public void registerUser_NewUserExistingUsername_ThrowsIllegalStateException() {
        String username = "alice123";
        String name = "A Lice";
        String email = "a_lice@email.com";
        String password = "a_licepassword";

        String getIpAddress = "10.10.10.10";
        Mockito.when(IdentityUtils.getIpAddress()).thenReturn(getIpAddress);

        Mockito.doThrow(ConstraintViolationException.class).when(userDao).persist(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        String emailActivationCode = userAccountService.registerUser(username, name, email, password);

        Assert.fail("Unreachable");
    }

    @Test
    public void forgotPassword_ExistingUser_ReturnsForgotPasswordCode() {
        String username = "alice123";
        String email = "alice@email.com";

        String getIpAddress = "10.10.10.10";
        Mockito.when(IdentityUtils.getIpAddress()).thenReturn(getIpAddress);

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

        String forgotPasswordCode = userAccountService.forgotPassword(username, email);

        Assert.assertNotNull(forgotPasswordCode, "Forgot password code must not be null");
        Assert.assertEquals(forgotPasswordCode, userForgotPasswordModel.code, "Forgot password code not equal");
        Assert.assertFalse(userForgotPasswordModel.used, "Forgot password code has been used");
    }

    @Test
    public void existForgotPassByCode_ExistingCode_ReturnsTrue() {
        String code = "FORGOT_PASS_CODE";

        Mockito.when(userForgotPasswordDao.isCodeValid(Mockito.eq(code), Mockito.anyInt())).thenReturn(true);

        Assert.assertTrue(userAccountService.isValidToChangePassword(code, 0), "Forgot password code not exist");
    }

    @Test
    public void existForgotPassByCode_NonExistingCode_ReturnsFalse() {
        String code = "NOT_FORGOT_PASS_CODE";

        Mockito.when(userForgotPasswordDao.isCodeValid(Mockito.eq(code), Mockito.anyInt())).thenReturn(false);

        Assert.assertFalse(userAccountService.isValidToChangePassword(code, 0), "Forgot password code exist");
    }

    @Test
    public void changePassword_ValidCode_PasswordChanged() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String code = "FORGOT_PASS_CODE";
        String newPassword = "alice456";

        String getIpAddress = "10.10.10.10";
        Mockito.when(IdentityUtils.getIpAddress()).thenReturn(getIpAddress);

        UserForgotPasswordModel userForgotPasswordModel = new UserForgotPasswordModel();
        userForgotPasswordModel.userJid = "JIDU0101";
        userForgotPasswordModel.code = code;
        userForgotPasswordModel.used = false;
        userForgotPasswordModel.timeCreate = System.currentTimeMillis();
        Mockito.when(userForgotPasswordDao.findByCode(code)).thenReturn(userForgotPasswordModel);
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

        userAccountService.changePassword(code, newPassword);

        Assert.assertTrue(userForgotPasswordModel.used, "Forgot password code not used");
        Assert.assertTrue(userForgotPasswordModel.timeUpdate > userForgotPasswordModel.timeCreate, "Forgot password not updated");
        Assert.assertTrue(PasswordHash.validatePassword(newPassword, userModel.password), "UserInfo password not changed");
        Assert.assertTrue(userModel.timeUpdate > userModel.timeCreate, "UserInfo password not updated");
    }

    @Test
    public void login_ValidUserByUsername_ReturnsUser() throws UserNotFoundException, EmailNotVerifiedException {
        String username = "alice123";
        String password = "alicepassword";

        UserModel userModel = new UserModel();
        userModel.jid = "JIDU0101";
        userModel.username = username;
        userModel.name = "Alice";
        userModel.password = JudgelsUtils.hashSHA256(password);
        userModel.profilePictureImageName = "avatar-default.png";
        userModel.roles = "user";
        Mockito.when(userDao.existByUsername(username)).thenReturn(true);
        Mockito.when(userDao.findByUsername(username)).thenReturn(userModel);

        UserEmailModel userEmailModel = new UserEmailModel();
        userEmailModel.userJid = userModel.jid;
        userEmailModel.email = "alice@email.com";
        userEmailModel.emailVerified = true;
        Mockito.when(userEmailDao.findByUserJid(userEmailModel.userJid)).thenReturn(userEmailModel);

        Http.Request request = Mockito.mock(Http.Request.class);
        Mockito.when(request.secure()).thenReturn(false);

        Http.Context context = Mockito.mock(Http.Context.class);
        PowerMockito.mockStatic(Http.Context.class);
        Mockito.when(Http.Context.current()).thenReturn(context);
        Mockito.when(context.request()).thenReturn(request);

        UserInfo user = userAccountService.login(username, password);

        Assert.assertNotNull(user, "UserInfo must not be null");
        Assert.assertEquals(username, user.getUsername(), "Username not equals");
    }

    @Test
    public void login_ValidUserByEmail_ReturnsUser() throws UserNotFoundException, EmailNotVerifiedException {
        String email = "alice@email.com";
        String password = "alicepassword";

        UserEmailModel userEmailModel = new UserEmailModel();
        userEmailModel.userJid = "JIDU0101";
        userEmailModel.email = email;
        userEmailModel.emailVerified = true;
        Mockito.when(userDao.existByUsername(email)).thenReturn(false);
        Mockito.when(userEmailDao.isExistByEmail(email)).thenReturn(true);
        Mockito.when(userEmailDao.findByEmail(email)).thenReturn(userEmailModel);

        UserModel userModel = new UserModel();
        userModel.jid = userEmailModel.userJid;
        userModel.username = "alice";
        userModel.name = "Alice";
        userModel.password = JudgelsUtils.hashSHA256(password);
        userModel.profilePictureImageName = "avatar-default.png";
        userModel.roles = "user";
        Mockito.when(userDao.findByJid(userModel.jid)).thenReturn(userModel);

        Http.Request request = Mockito.mock(Http.Request.class);
        Mockito.when(request.secure()).thenReturn(false);

        Http.Context context = Mockito.mock(Http.Context.class);
        PowerMockito.mockStatic(Http.Context.class);
        Mockito.when(Http.Context.current()).thenReturn(context);
        Mockito.when(context.request()).thenReturn(request);

        UserInfo user = userAccountService.login(email, password);

        Assert.assertNotNull(user, "UserInfo must not be null");
        Assert.assertEquals(email, user.getEmail(), "Email not equals");
    }

    @Test(expectedExceptions = UserNotFoundException.class)
    public void login_InvalidUser_ThrowsUserNotFoundException() throws UserNotFoundException, EmailNotVerifiedException {
        String username = "bob";
        String password = "bobpassword";

        Mockito.when(userDao.existByUsername(username)).thenReturn(false);
        Mockito.when(userEmailDao.isExistByEmail(username)).thenReturn(false);

        UserInfo user = userAccountService.login(username, password);

        Assert.fail("Unreachable");
    }

    @Test(expectedExceptions = EmailNotVerifiedException.class)
    public void login_ValidUserUnverifiedEmail_ThrowsEmailNotVerifiedException() throws UserNotFoundException, EmailNotVerifiedException {
        String username = "alice123";
        String password = "alicepassword";

        UserModel userModel = new UserModel();
        userModel.jid = "JIDU0101";
        userModel.username = username;
        userModel.password = JudgelsUtils.hashSHA256(password);
        Mockito.when(userDao.existByUsername(username)).thenReturn(true);
        Mockito.when(userDao.findByUsername(username)).thenReturn(userModel);

        UserEmailModel userEmailModel = new UserEmailModel();
        userEmailModel.userJid = userModel.jid;
        userEmailModel.emailVerified = false;
        Mockito.when(userEmailDao.findByUserJid(userEmailModel.userJid)).thenReturn(userEmailModel);

        UserInfo user = userAccountService.login(username, password);

        Assert.fail("Unreachable");
    }

    @Test
    public void login_ValidUsernameInvalidPassword_ReturnsNull() throws UserNotFoundException, EmailNotVerifiedException {
        String username = "alice123";
        String password = "bobpassword";
        String validPassword = "alicepassword";

        UserModel userModel = new UserModel();
        userModel.jid = "JIDU0101";
        userModel.username = username;
        userModel.password = JudgelsUtils.hashSHA256(validPassword);
        Mockito.when(userDao.existByUsername(username)).thenReturn(true);
        Mockito.when(userDao.findByUsername(username)).thenReturn(userModel);

        UserEmailModel userEmailModel = new UserEmailModel();
        userEmailModel.userJid = userModel.jid;
        Mockito.when(userEmailDao.findByUserJid(userEmailModel.userJid)).thenReturn(userEmailModel);

        UserInfo user = userAccountService.login(username, password);

        Assert.assertNull(user, "UserInfo not null");
    }

    @Test
    public void updatePassword_ValidUserNewPassword_PasswordUpdated() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String userJid = "JIDU0101";
        String newPassword = "newalicepassword";

        String getUserJid = userJid;
        String getIpAddress = "10.10.10.10";
        Mockito.when(IdentityUtils.getUserJid()).thenReturn(getUserJid);
        Mockito.when(IdentityUtils.getIpAddress()).thenReturn(getIpAddress);

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

        userAccountService.updatePassword(userJid, newPassword);

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
