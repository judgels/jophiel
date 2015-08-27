package org.iatoki.judgels.jophiel.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.SerializeException;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;
import org.iatoki.judgels.jophiel.Client;
import org.iatoki.judgels.jophiel.EmailNotVerifiedException;
import org.iatoki.judgels.jophiel.JophielProperties;
import org.iatoki.judgels.jophiel.JophielUtils;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserNotFoundException;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.HasRole;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
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
import org.iatoki.judgels.jophiel.views.html.account.serviceAuthView;
import org.iatoki.judgels.jophiel.views.html.account.serviceLoginView;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.centerLayout;
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
import java.net.URI;
import java.util.List;
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
        if (userService.existsUserByUsername(registerData.username)) {
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

        boolean valid = true;
        if (JophielProperties.getInstance().isRegistrationUsingRecaptcha()) {
            DynamicForm dynamicForm = DynamicForm.form().bindFromRequest();
            F.Promise<JsonNode> jsonPromise = WS.url("https://www.google.com/recaptcha/api/siteverify").setContentType("application/x-www-registerForm-urlencoded").post("secret=" + JophielProperties.getInstance().getRegistrationRecaptchaSecretKey() + "&response=" + dynamicForm.get("g-recaptcha-response") + "&remoteip=" + IdentityUtils.getIpAddress()).map(response -> {
                    return response.asJson();
                });
            JsonNode response = jsonPromise.get(1, TimeUnit.MINUTES);
            valid = response.get("success").asBoolean();
        }

        if (!valid) {
            registerForm.reject("register.error.invalidRegistration");
            return showRegister(registerForm);
        }

        try {
            String emailCode = userAccountService.registerUser(registerData.username, registerData.name, registerData.email, registerData.password);
            userEmailService.sendRegistrationEmailActivation(registerData.name, registerData.email, org.iatoki.judgels.jophiel.controllers.routes.UserEmailController.verifyEmail(emailCode).absoluteURL(request(), request().secure()));

            return redirect(routes.UserAccountController.afterRegister(registerData.email));
        } catch (IllegalStateException e) {
            registerForm.reject("register.error.usernameOrEmailExists");
            return showRegister(registerForm);
        }
    }

    public Result afterRegister(String email) {
        LazyHtml content = new LazyHtml(messageView.render(Messages.get("register.activationEmailSentTo") + " " + email + "."));
        content.appendLayout(c -> headingLayout.render(Messages.get("register.successful"), c));
        content.appendLayout(c -> centerLayout.render(c));
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
        if (!userService.existsUserByUsername(forgotPasswordData.username)) {
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

        String forgotPasswordCode = userAccountService.generateForgotPasswordRequest(forgotPasswordData.username, forgotPasswordData.email);
        userEmailService.sendChangePasswordEmail(forgotPasswordData.email, routes.UserAccountController.changePassword(forgotPasswordCode).absoluteURL(request(), request().secure()));

        return redirect(routes.UserAccountController.afterForgotPassword(forgotPasswordData.email));
    }

    public Result afterForgotPassword(String email) {
        LazyHtml content = new LazyHtml(messageView.render(Messages.get("forgotPasswordEmail.changePasswordRequestSentTo") + " " + email + "."));
        content.appendLayout(c -> headingLayout.render(Messages.get("forgotPassword.successful"), c));
        content.appendLayout(c -> centerLayout.render(c));
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

        userAccountService.processChangePassword(code, changePasswordData.password);

        return redirect(routes.UserAccountController.afterChangePassword());
    }

    public Result afterChangePassword() {
        LazyHtml content = new LazyHtml(messageView.render(Messages.get("changePassword.success") + "."));
        content.appendLayout(c -> headingLayout.render(Messages.get("changePassword.successful"), c));
        content.appendLayout(c -> centerLayout.render(c));
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

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result logout() {
        return serviceLogout(null);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result serviceLogout(String returnUri) {
        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Logout <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        session().clear();
        if (returnUri == null) {
            return redirect(routes.UserAccountController.login());
        }

        return redirect(returnUri);
    }

    @Transactional
    public Result serviceAuthRequest() {
        if (!JophielControllerUtils.getInstance().loggedIn(userService)) {
            return redirect((routes.UserAccountController.serviceLogin("http" + (request().secure() ? "s" : "") + "://" + request().host() + request().uri())));
        }

        String path = request().uri().substring(request().uri().indexOf("?") + 1);
        try {
            AuthenticationRequest req = AuthenticationRequest.parse(path);
            ClientID clientID = req.getClientID();
            if (!clientService.clientExistsByJid(clientID.toString())) {
                return redirect(path + "?error=unauthorized_client");
            }

            Client client = clientService.findClientByJid(clientID.toString());

            List<String> scopes = req.getScope().toStringList();
            if (clientService.isClientAuthorized(clientID.toString(), scopes)) {
                return postServiceAuthRequest(path);
            }

            LazyHtml content = new LazyHtml(serviceAuthView.render(path, client, scopes));
            content.appendLayout(c -> centerLayout.render(c));
            JophielControllerUtils.getInstance().appendTemplateLayout(content, "Auth");

            JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Try authorize client " + client.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return JophielControllerUtils.getInstance().lazyOk(content);
        } catch (com.nimbusds.oauth2.sdk.ParseException e) {
            Logger.error("Exception when parsing authentication request.", e);
            return redirect(path + "?error=invalid_request");
        }
    }

    @Transactional
    public Result postServiceAuthRequest(String path) {
        AuthenticationRequest authRequest;
        try {
            authRequest = AuthenticationRequest.parse(path);
        } catch (ParseException e) {
            Logger.error("Exception when parsing authentication request.", e);
            return redirect(path + "?error=invalid_request");
        }

        ClientID clientID = authRequest.getClientID();
        if (!clientService.clientExistsByJid(clientID.toString())) {
            return redirect(path + "?error=unauthorized_client");
        }

        Client client = clientService.findClientByJid(clientID.toString());
        URI redirectURI = authRequest.getRedirectionURI();
        ResponseType responseType = authRequest.getResponseType();
        State state = authRequest.getState();
        Scope scope = authRequest.getScope();
        String nonce = (authRequest.getNonce() != null) ? authRequest.getNonce().toString() : "";

        AuthorizationCode authCode = clientService.generateAuthorizationCode(client.getJid(), redirectURI.toString(), responseType.toString(), scope.toStringList(), System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES));
        String accessToken = clientService.generateAccessToken(authCode.getValue(), IdentityUtils.getUserJid(), clientID.toString(), scope.toStringList(), System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES));
        clientService.generateRefreshToken(authCode.getValue(), IdentityUtils.getUserJid(), clientID.toString(), scope.toStringList());
        clientService.generateIdToken(authCode.getValue(), IdentityUtils.getUserJid(), client.getJid(), nonce, System.currentTimeMillis(), accessToken, System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES));

        URI result;
        try {
            result = new AuthenticationSuccessResponse(redirectURI, authCode, null, null, state).toURI();
        } catch (SerializeException e) {
            Logger.error("Exception when parsing authentication request.", e);
            return redirect(path + "?error=invalid_request");
        }

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Authorize client " + client.getName() + ".");

        return redirect(result.toString());
    }

    private Result showRegister(Form<RegisterForm> registerForm) {
        LazyHtml content = new LazyHtml(registerView.render(registerForm));
        content.appendLayout(c -> centerLayout.render(c));
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Register");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    private Result showForgotPassword(Form<ForgotPasswordForm> forgotPasswordForm) {
        LazyHtml content = new LazyHtml(forgotPasswordView.render(forgotPasswordForm));
        content.appendLayout(c -> centerLayout.render(c));
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Forgot Password");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    private Result showChangePassword(Form<ChangePasswordForm> changePasswordForm, String code) {
        LazyHtml content = new LazyHtml(changePasswordView.render(changePasswordForm, code));
        content.appendLayout(c -> centerLayout.render(c));
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
        content.appendLayout(c -> centerLayout.render(c));
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Login");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }
}
