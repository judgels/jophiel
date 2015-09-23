package org.iatoki.judgels.jophiel.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.iatoki.judgels.jophiel.EmailNotVerifiedException;
import org.iatoki.judgels.jophiel.JophielActivityKeys;
import org.iatoki.judgels.jophiel.JophielProperties;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserNotFoundException;
import org.iatoki.judgels.jophiel.forms.PasswordChangeForm;
import org.iatoki.judgels.jophiel.forms.PasswordForgotForm;
import org.iatoki.judgels.jophiel.forms.LoginForm;
import org.iatoki.judgels.jophiel.forms.RegisterForm;
import org.iatoki.judgels.jophiel.services.UserAccountService;
import org.iatoki.judgels.jophiel.services.UserActivityService;
import org.iatoki.judgels.jophiel.services.UserEmailService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.jophiel.views.html.account.changePasswordView;
import org.iatoki.judgels.jophiel.views.html.account.forgotPasswordView;
import org.iatoki.judgels.jophiel.views.html.account.loginView;
import org.iatoki.judgels.jophiel.views.html.account.registerView;
import org.iatoki.judgels.jophiel.views.html.account.serviceLoginView;
import org.iatoki.judgels.play.HtmlTemplate;
import org.iatoki.judgels.play.views.html.layouts.messageView;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.libs.F;
import play.libs.ws.WS;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Singleton
@Named
public final class UserAccountController extends AbstractJophielController {

    private final UserService userService;
    private final UserAccountService userAccountService;
    private final UserEmailService userEmailService;

    @Inject
    public UserAccountController(UserActivityService userActivityService, UserService userService, UserAccountService userAccountService, UserEmailService userEmailService) {
        super(userActivityService);

        this.userService = userService;
        this.userAccountService = userAccountService;
        this.userEmailService = userEmailService;
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result register() {
        if (isLoggedIn()) {
            return redirect(getMainPage());
        }

        Form<RegisterForm> registerForm = Form.form(RegisterForm.class);
        return showRegister(registerForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postRegister() {
        if (isLoggedIn()) {
            return redirect(getMainPage());
        }

        Form<RegisterForm> registerForm = Form.form(RegisterForm.class).bindFromRequest();
        if (formHasErrors(registerForm)) {
            return showRegister(registerForm);
        }

        RegisterForm registerData = registerForm.get();
        if (userService.userExistsByUsername(registerData.username)) {
            registerForm.reject(Messages.get("register.error.usernameExists"));
            return showRegister(registerForm);
        }

        if (userEmailService.emailExists(registerData.email)) {
            registerForm.reject(Messages.get("register.error.emailExists"));
            return showRegister(registerForm);
        }

        if (!registerData.password.equals(registerData.confirmPassword)) {
            registerForm.reject(Messages.get("register.error.passwordsDidntMatch"));
            return showRegister(registerForm);
        }

        if (JophielProperties.getInstance().isRegistrationUsingRecaptcha()) {
            DynamicForm dynamicForm = DynamicForm.form().bindFromRequest();
            F.Promise<JsonNode> jsonPromise = WS.url("https://www.google.com/recaptcha/api/siteverify")
                    .setContentType("application/x-www-form-urlencoded")
                    .post("secret=" + JophielProperties.getInstance().getRegistrationRecaptchaSecretKey() + "&response=" + dynamicForm.get("g-recaptcha-response") + "&remoteip=" + getCurrentUserIpAddress())
                    .map(response -> response.asJson());
            JsonNode response = jsonPromise.get(1, TimeUnit.MINUTES);
            boolean valid = response.get("success").asBoolean();
            if (!valid) {
                Logger.error(response.toString());
                registerForm.reject(Messages.get("register.error.captchaInvalid"));
                return showRegister(registerForm);
            }
        }

        try {
            String emailCode = userAccountService.registerUser(registerData.username, registerData.name, registerData.email, registerData.password, getCurrentUserIpAddress());
            userEmailService.sendRegistrationEmailActivation(registerData.name, registerData.email, getAbsoluteUrl(routes.UserEmailController.verifyEmail(emailCode)));

            return showAfterRegister(registerData.email);
        } catch (IllegalStateException e) {
            registerForm.reject(Messages.get("register.error.usernameOrEmailExists"));
            return showRegister(registerForm);
        }
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result forgotPassword() {
        if (isLoggedIn()) {
            return redirect(getMainPage());
        }

        Form<PasswordForgotForm> forgotPasswordForm = Form.form(PasswordForgotForm.class);
        return showForgotPassword(forgotPasswordForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postForgotPassword() {
        if (isLoggedIn()) {
            return redirect(getMainPage());
        }

        Form<PasswordForgotForm> forgotPasswordForm = Form.form(PasswordForgotForm.class).bindFromRequest();
        if (formHasErrors(forgotPasswordForm)) {
            return showForgotPassword(forgotPasswordForm);
        }

        PasswordForgotForm forgotPasswordData = forgotPasswordForm.get();
        if (!userService.userExistsByUsername(forgotPasswordData.username)) {
            forgotPasswordForm.reject(Messages.get("forgotPassword.error.usernameNotExists"));
            return showForgotPassword(forgotPasswordForm);
        }

        if (!userEmailService.emailExists(forgotPasswordData.email)) {
            forgotPasswordForm.reject(Messages.get("forgotPassword.error.emailInvalid"));
            return showForgotPassword(forgotPasswordForm);
        }

        if (!userEmailService.isEmailOwnedByUser(forgotPasswordData.email, forgotPasswordData.username)) {
            forgotPasswordForm.reject(Messages.get("forgotPassword.error.emailInvalid"));
            return showForgotPassword(forgotPasswordForm);
        }

        String forgotPasswordCode = userAccountService.generateForgotPasswordRequest(forgotPasswordData.username, forgotPasswordData.email, getCurrentUserIpAddress());
        userEmailService.sendChangePasswordEmail(forgotPasswordData.email, getAbsoluteUrl(routes.UserAccountController.changePassword(forgotPasswordCode)));

        return showAfterForgotPassword(forgotPasswordData.email);
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result changePassword(String code) {
        if (isLoggedIn()) {
            return redirect(getMainPage());
        }

        if (!userAccountService.isValidToChangePassword(code, System.currentTimeMillis())) {
            return notFound();
        }

        Form<PasswordChangeForm> changePasswordForm = Form.form(PasswordChangeForm.class);
        return showChangePassword(changePasswordForm, code);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postChangePassword(String code) {
        if (isLoggedIn()) {
            return redirect(getMainPage());
        }

        if (!userAccountService.isValidToChangePassword(code, System.currentTimeMillis())) {
            return notFound();
        }

        Form<PasswordChangeForm> changePasswordForm = Form.form(PasswordChangeForm.class).bindFromRequest();
        if (formHasErrors(changePasswordForm)) {
            return showChangePassword(changePasswordForm, code);
        }

        PasswordChangeForm changePasswordData = changePasswordForm.get();
        if (!changePasswordData.password.equals(changePasswordData.confirmPassword)) {
            changePasswordForm.reject(Messages.get("forgotPassword.error.passwordsDidntMatch"));
            return showChangePassword(changePasswordForm, code);
        }

        userAccountService.processChangePassword(code, changePasswordData.password, getCurrentUserIpAddress());

        return showAfterChangePassword();
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result login() {
        return serviceLogin(null);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postLogin() {
        return postServiceLogin(null);
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result serviceLogin(String continueUrl) {
        if (isLoggedIn()) {
            if (continueUrl == null) {
                return redirect(getMainPage());
            }
            return redirect(continueUrl);
        }

        Form<LoginForm> loginForm = Form.form(LoginForm.class);
        return showLogin(loginForm, continueUrl);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postServiceLogin(String continueUrl) {
        if (isLoggedIn()) {
            if (continueUrl == null) {
                return redirect(getMainPage());
            }
            return redirect(continueUrl);
        }

        Form<LoginForm> loginForm = Form.form(LoginForm.class).bindFromRequest();
        if (formHasErrors(loginForm)) {
            Logger.error(loginForm.errors().toString());
            return showLogin(loginForm, continueUrl);
        }

        try {
            LoginForm loginData = loginForm.get();
            User user = userAccountService.processLogin(loginData.usernameOrEmail, loginData.password, getCurrentUserIpAddress());
            if (user == null) {
                loginForm.reject(Messages.get("login.error.invalid"));
                return showLogin(loginForm, continueUrl);
            }

            // TODO add expiry time and remember me options
            session("userJid", user.getJid());
            session("username", user.getUsername());
            session("name", user.getName());
            session("avatar", user.getProfilePictureUrl().toString());
            setCurrentUserRoles(user.getRoles());

            addActivityLog(JophielActivityKeys.LOGIN.construct());
            if (continueUrl == null) {
                return redirect(getMainPage());
            }

            return redirect(continueUrl);
        } catch (UserNotFoundException e) {
            loginForm.reject(Messages.get("login.error.invalid"));
            return showLogin(loginForm, continueUrl);
        } catch (EmailNotVerifiedException e) {
            loginForm.reject(Messages.get("login.error.emailNotVerified"));
            return showLogin(loginForm, continueUrl);
        }
    }

    @Transactional
    public Result logout() {
        return serviceLogout(null);
    }

    @Transactional
    public Result serviceLogout(String returnUri) {
        addActivityLog(JophielActivityKeys.LOGOUT.construct());
        session().clear();

        if (returnUri == null) {
            return redirect(routes.UserAccountController.login());
        }

        return redirect(returnUri);
    }

    private Result showRegister(Form<RegisterForm> registerForm) {
        HtmlTemplate template = new HtmlTemplate();

        template.setContent(registerView.render(registerForm));
        template.setMainTitle(Messages.get("register.text.register"));

        return renderTemplate(template);
    }

    private Result showAfterRegister(String email) {
        HtmlTemplate template = new HtmlTemplate();

        template.setContent(messageView.render(Messages.get("register.text.activationEmailSentTo", email)));
        template.setMainTitle(Messages.get("register.text.successful"));

        return renderTemplate(template);
    }

    private Result showForgotPassword(Form<PasswordForgotForm> passwordForgotForm) {
        HtmlTemplate template = new HtmlTemplate();

        template.setContent(forgotPasswordView.render(passwordForgotForm));
        template.setMainTitle(Messages.get("forgotPassword.text.forgotPassword"));

        return renderTemplate(template);
    }

    private Result showAfterForgotPassword(String email) {
        HtmlTemplate template = new HtmlTemplate();

        template.setContent(messageView.render(Messages.get("forgotPassword.text.emailSentTo", email)));
        template.setMainTitle(Messages.get("forgotPassword.text.requestSent"));

        return renderTemplate(template);
    }

    private Result showChangePassword(Form<PasswordChangeForm> passwordChangeForm, String code) {
        HtmlTemplate template = new HtmlTemplate();

        template.setContent(changePasswordView.render(passwordChangeForm, code));
        template.setMainTitle(Messages.get("forgotPassword.text.changePassword"));

        return renderTemplate(template);
    }

    private Result showAfterChangePassword() {
        HtmlTemplate template = new HtmlTemplate();

        template.setContent(messageView.render(Messages.get("forgotPassword.text.canNowLogin")));
        template.setMainTitle(Messages.get("forgotPassword.text.changePasswordSuccessful"));

        return renderTemplate(template);
    }

    private Result showLogin(Form<LoginForm> loginForm, String continueUrl) {
        HtmlTemplate template = new HtmlTemplate();

        if (continueUrl == null) {
            template.setContent(loginView.render(loginForm));
        } else {
            template.setContent(serviceLoginView.render(loginForm, continueUrl));
        }

        template.setMainTitle(Messages.get("login.text.logIn"));

        return renderTemplate(template);
    }

    private boolean isLoggedIn() {
        return getCurrentUserJid() != null && userService.userExistsByJid(getCurrentUserJid());
    }
}
