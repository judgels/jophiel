package org.iatoki.judgels.jophiel.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.iatoki.judgels.jophiel.EmailNotVerifiedException;
import org.iatoki.judgels.jophiel.JophielProperties;
import org.iatoki.judgels.jophiel.JophielUtils;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserNotFoundException;
import org.iatoki.judgels.jophiel.forms.ChangePasswordForm;
import org.iatoki.judgels.jophiel.forms.ForgotPasswordForm;
import org.iatoki.judgels.jophiel.forms.LoginForm;
import org.iatoki.judgels.jophiel.forms.RegisterForm;
import org.iatoki.judgels.jophiel.services.ClientService;
import org.iatoki.judgels.jophiel.services.UserAccountService;
import org.iatoki.judgels.jophiel.services.UserActivityService;
import org.iatoki.judgels.jophiel.services.UserEmailService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.jophiel.views.html.account.changePasswordView;
import org.iatoki.judgels.jophiel.views.html.account.forgotPasswordView;
import org.iatoki.judgels.jophiel.views.html.account.loginView;
import org.iatoki.judgels.jophiel.views.html.account.registerView;
import org.iatoki.judgels.jophiel.views.html.account.serviceLoginView;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
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
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Singleton
@Named
public final class UserAccountController extends AbstractJudgelsController {

    private final ClientService clientService;
    private final UserAccountService userAccountService;
    private final UserActivityService userActivityService;
    private final UserEmailService userEmailService;
    private final UserService userService;

    @Inject
    public UserAccountController(ClientService clientService, UserAccountService userAccountService, UserActivityService userActivityService, UserEmailService userEmailService, UserService userService) {
        this.clientService = clientService;
        this.userAccountService = userAccountService;
        this.userActivityService = userActivityService;
        this.userEmailService = userEmailService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result register() {
        if (JophielControllerUtils.getInstance().loggedIn(userService)) {
            return redirect(JophielControllerUtils.getInstance().mainPage());
        }

        Form<RegisterForm> registerForm = Form.form(RegisterForm.class);
        return showRegister(registerForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postRegister() {
        if (JophielControllerUtils.getInstance().loggedIn(userService)) {
            return redirect(JophielControllerUtils.getInstance().mainPage());
        }

        Form<RegisterForm> registerForm = Form.form(RegisterForm.class).bindFromRequest();
        if (formHasErrors(registerForm)) {
            return showRegister(registerForm);
        }

        RegisterForm registerData = registerForm.get();
        if (userService.userExistsByUsername(registerData.username)) {
            registerForm.reject("register.error.usernameExists");
            return showRegister(registerForm);
        }

        if (userEmailService.emailExists(registerData.email)) {
            registerForm.reject("register.error.emailExists");
            return showRegister(registerForm);
        }

        if (!registerData.password.equals(registerData.confirmPassword)) {
            registerForm.reject("register.error.passwordsDidntMatch");
            return showRegister(registerForm);
        }

        if (JophielProperties.getInstance().isRegistrationUsingRecaptcha()) {
            DynamicForm dynamicForm = DynamicForm.form().bindFromRequest();
            F.Promise<JsonNode> jsonPromise = WS.url("https://www.google.com/recaptcha/api/siteverify").setContentType("application/x-www-form-urlencoded").post("secret=" + JophielProperties.getInstance().getRegistrationRecaptchaSecretKey() + "&response=" + dynamicForm.get("g-recaptcha-response") + "&remoteip=" + IdentityUtils.getIpAddress()).map(response -> {
                    return response.asJson();
                });
            JsonNode response = jsonPromise.get(1, TimeUnit.MINUTES);
            boolean valid = response.get("success").asBoolean();
            if (!valid) {
                Logger.error(response.toString());
                registerForm.reject("register.error.invalidRegistration");
                return showRegister(registerForm);
            }
        }

        try {
            String emailCode = userAccountService.registerUser(registerData.username, registerData.name, registerData.email, registerData.password, IdentityUtils.getIpAddress());
            userEmailService.sendRegistrationEmailActivation(registerData.name, registerData.email, routes.UserEmailController.verifyEmail(emailCode).absoluteURL(request(), request().secure()));

            return redirect(routes.UserAccountController.afterRegister(registerData.email));
        } catch (IllegalStateException e) {
            registerForm.reject("register.error.usernameOrEmailExists");
            return showRegister(registerForm);
        }
    }

    public Result afterRegister(String email) {
        LazyHtml content = new LazyHtml(messageView.render(Messages.get("register.activationEmailSentTo") + " " + email + "."));
        content.appendLayout(c -> headingLayout.render(Messages.get("register.successful"), c));
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "After Register");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result forgotPassword() {
        if (JophielControllerUtils.getInstance().loggedIn(userService)) {
            return redirect(JophielControllerUtils.getInstance().mainPage());
        }

        Form<ForgotPasswordForm> forgotPasswordForm = Form.form(ForgotPasswordForm.class);
        return showForgotPassword(forgotPasswordForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postForgotPassword() {
        if (JophielControllerUtils.getInstance().loggedIn(userService)) {
            return redirect(JophielControllerUtils.getInstance().mainPage());
        }

        Form<ForgotPasswordForm> forgotPasswordForm = Form.form(ForgotPasswordForm.class).bindFromRequest();
        if (formHasErrors(forgotPasswordForm)) {
            return showForgotPassword(forgotPasswordForm);
        }

        ForgotPasswordForm forgotPasswordData = forgotPasswordForm.get();
        if (!userService.userExistsByUsername(forgotPasswordData.username)) {
            forgotPasswordForm.reject("forgot_pass.error.usernameNotExists");
            return showForgotPassword(forgotPasswordForm);
        }

        if (!userEmailService.emailExists(forgotPasswordData.email)) {
            forgotPasswordForm.reject("forgot_pass.error.emailNotExists");
            return showForgotPassword(forgotPasswordForm);
        }

        if (!userEmailService.isEmailOwnedByUser(forgotPasswordData.email, forgotPasswordData.username)) {
            forgotPasswordForm.reject("forgot_pass.error.emailIsNotOwnedByUser");
            return showForgotPassword(forgotPasswordForm);
        }

        String forgotPasswordCode = userAccountService.generateForgotPasswordRequest(forgotPasswordData.username, forgotPasswordData.email, IdentityUtils.getIpAddress());
        userEmailService.sendChangePasswordEmail(forgotPasswordData.email, routes.UserAccountController.changePassword(forgotPasswordCode).absoluteURL(request(), request().secure()));

        return redirect(routes.UserAccountController.afterForgotPassword(forgotPasswordData.email));
    }

    public Result afterForgotPassword(String email) {
        LazyHtml content = new LazyHtml(messageView.render(Messages.get("forgotPasswordEmail.changePasswordRequestSentTo") + " " + email + "."));
        content.appendLayout(c -> headingLayout.render(Messages.get("forgotPassword.successful"), c));
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "After Forgot Password");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result changePassword(String code) {
        if (JophielControllerUtils.getInstance().loggedIn(userService)) {
            return redirect(JophielControllerUtils.getInstance().mainPage());
        }

        if (!userAccountService.isValidToChangePassword(code, System.currentTimeMillis())) {
            return notFound();
        }

        Form<ChangePasswordForm> changePasswordForm = Form.form(ChangePasswordForm.class);
        return showChangePassword(changePasswordForm, code);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postChangePassword(String code) {
        if (JophielControllerUtils.getInstance().loggedIn(userService)) {
            return redirect(JophielControllerUtils.getInstance().mainPage());
        }

        if (!userAccountService.isValidToChangePassword(code, System.currentTimeMillis())) {
            return notFound();
        }

        Form<ChangePasswordForm> changePasswordForm = Form.form(ChangePasswordForm.class).bindFromRequest();
        if (formHasErrors(changePasswordForm)) {
            return showChangePassword(changePasswordForm, code);
        }

        ChangePasswordForm changePasswordData = changePasswordForm.get();
        if (!changePasswordData.password.equals(changePasswordData.confirmPassword)) {
            changePasswordForm.reject("change_password.error.passwordsDidntMatch");
            return showChangePassword(changePasswordForm, code);
        }

        userAccountService.processChangePassword(code, changePasswordData.password, IdentityUtils.getIpAddress());

        return redirect(routes.UserAccountController.afterChangePassword());
    }

    public Result afterChangePassword() {
        LazyHtml content = new LazyHtml(messageView.render(Messages.get("changePassword.success") + "."));
        content.appendLayout(c -> headingLayout.render(Messages.get("changePassword.successful"), c));
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "After Change Password");

        return JophielControllerUtils.getInstance().lazyOk(content);
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
        if (JophielControllerUtils.getInstance().loggedIn(userService)) {
            if (continueUrl == null) {
                return redirect(JophielControllerUtils.getInstance().mainPage());
            }
            return redirect(continueUrl);
        }

        Form<LoginForm> loginForm = Form.form(LoginForm.class);
        return showLogin(loginForm, continueUrl);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postServiceLogin(String continueUrl) {
        if (JophielControllerUtils.getInstance().loggedIn(userService)) {
            if (continueUrl == null) {
                return redirect(JophielControllerUtils.getInstance().mainPage());
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
            User user = userAccountService.processLogin(loginData.usernameOrEmail, loginData.password);
            if (user == null) {
                loginForm.reject("login.error.usernameOrEmailOrPasswordInvalid");
                return showLogin(loginForm, continueUrl);
            }

            // TODO add expiry time and remember me options
            session("userJid", user.getJid());
            session("username", user.getUsername());
            session("name", user.getName());
            session("avatar", user.getProfilePictureUrl().toString());
            JophielUtils.saveRoleInSession(user.getRoles());
            JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Logged In.");
            if (continueUrl == null) {
                return redirect(JophielControllerUtils.getInstance().mainPage());
            }

            return redirect(continueUrl);
        } catch (UserNotFoundException e) {
            loginForm.reject("login.error.usernameOrEmailOrPasswordInvalid");
            return showLogin(loginForm, continueUrl);
        } catch (EmailNotVerifiedException e) {
            loginForm.reject("login.error.emailNotVerified");
            return showLogin(loginForm, continueUrl);
        }
    }

    @Transactional
    public Result logout() {
        return serviceLogout(null);
    }

    @Transactional
    public Result serviceLogout(String returnUri) {
        if (IdentityUtils.getUserJid() != null) {
            JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Logout <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");
        }

        session().clear();
        if (returnUri == null) {
            return redirect(routes.UserAccountController.login());
        }

        return redirect(returnUri);
    }

    private Result showRegister(Form<RegisterForm> registerForm) {
        LazyHtml content = new LazyHtml(registerView.render(registerForm));
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Register");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    private Result showForgotPassword(Form<ForgotPasswordForm> forgotPasswordForm) {
        LazyHtml content = new LazyHtml(forgotPasswordView.render(forgotPasswordForm));
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Forgot Password");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    private Result showChangePassword(Form<ChangePasswordForm> changePasswordForm, String code) {
        LazyHtml content = new LazyHtml(changePasswordView.render(changePasswordForm, code));
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Change Password");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    private Result showLogin(Form<LoginForm> loginForm, String continueUrl) {
        LazyHtml content;
        if (continueUrl == null) {
            content = new LazyHtml(loginView.render(loginForm));
        } else {
            content = new LazyHtml(serviceLoginView.render(loginForm, continueUrl));
        }
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Login");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }
}
