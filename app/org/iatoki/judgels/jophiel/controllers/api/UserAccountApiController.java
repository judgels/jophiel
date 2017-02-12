package org.iatoki.judgels.jophiel.controllers.api;

import com.google.inject.Inject;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.ApiErrorCodeV1;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.UserV1;
import org.iatoki.judgels.jophiel.user.User;
import org.iatoki.judgels.jophiel.user.UserNotFoundException;
import org.iatoki.judgels.jophiel.user.UserService;
import org.iatoki.judgels.jophiel.user.account.LoginForm;
import org.iatoki.judgels.jophiel.user.account.PasswordChangeApiForm;
import org.iatoki.judgels.jophiel.user.account.PasswordForgotForm;
import org.iatoki.judgels.jophiel.user.account.UserAccountService;
import org.iatoki.judgels.jophiel.user.profile.email.EmailNotVerifiedException;
import org.iatoki.judgels.jophiel.user.profile.email.UserEmailService;
import org.iatoki.judgels.play.api.JudgelsAPIBadRequestException;
import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;
import org.iatoki.judgels.play.controllers.apis.JudgelsAPIGuard;
import play.data.Form;
import play.mvc.Result;

import javax.transaction.Transactional;

import static play.data.Form.form;

@JudgelsAPIGuard
public class UserAccountApiController extends AbstractJudgelsAPIController {

    private final UserAccountService userAccountService;
    private final UserEmailService userEmailService;
    private final UserService userService;

    @Inject
    public UserAccountApiController(UserAccountService userAccountService, UserEmailService userEmailService, UserService userService) {
        this.userAccountService = userAccountService;
        this.userEmailService = userEmailService;
        this.userService = userService;
    }

    @play.db.jpa.Transactional
    public Result authLogin() {
        Form<LoginForm> form = Form.form(LoginForm.class).bindFromRequest();
        if (formHasErrors(form)) {
            throw new JudgelsAPIBadRequestException(ApiErrorCodeV1.INVALID_INPUT_PARAMETER);
        }

        LoginForm loginForm = form.get();
        try {
            User user = userAccountService.processLogin(loginForm.usernameOrEmail, loginForm.password, null);

            if (user == null) {
                return badRequestAsJson(ApiErrorCodeV1.INVALID_PASSWORD);
            } else {
                return okAsJson(createUserV1FromUser(user));
            }
        } catch (EmailNotVerifiedException e) {
            return badRequestAsJson(ApiErrorCodeV1.EMAIL_NOT_VERIFIED);
        } catch (UserNotFoundException e) {
            return badRequestAsJson(ApiErrorCodeV1.INVALID_USERNAME);
        }
    }

    @Transactional
    public Result forgotPassword() {
        Form<PasswordForgotForm> forgotPasswordForm = form(PasswordForgotForm.class).bindFromRequest();
        if (formHasErrors(forgotPasswordForm)) {
            throw new JudgelsAPIBadRequestException(ApiErrorCodeV1.INVALID_INPUT_PARAMETER);
        }

        PasswordForgotForm forgotPasswordData = forgotPasswordForm.get();
        if (!userService.userExistsByUsername(forgotPasswordData.username)) {
            throw new JudgelsAPIBadRequestException(ApiErrorCodeV1.INVALID_USERNAME);
        }

        if (!userEmailService.isEmailOwnedByUser(forgotPasswordData.email, forgotPasswordData.username)) {
            throw new JudgelsAPIBadRequestException(ApiErrorCodeV1.INVALID_EMAIL);
        }

        String forgotPasswordCode = userAccountService.generateForgotPasswordRequest(forgotPasswordData.username, forgotPasswordData.email, getCurrentUserIpAddress());
        userEmailService.sendChangePasswordEmail(forgotPasswordData.email, getAbsoluteUrl(org.iatoki.judgels.jophiel.user.account.routes.UserAccountController.changePassword(forgotPasswordCode)));

        return okJson();
    }

    @Transactional
    public Result changePassword(String code) {
        Form<PasswordChangeApiForm> passwordChangeForm = form(PasswordChangeApiForm.class).bindFromRequest();

        if (!userAccountService.isValidToChangePassword(code, System.currentTimeMillis())) {
            throw new JudgelsAPIBadRequestException(ApiErrorCodeV1.INVALID_FORGOT_PASSWORD_CODE);
        }

        PasswordChangeApiForm passwordChangeData = passwordChangeForm.get();
        userAccountService.processChangePassword(code, passwordChangeData.password, null);

        return okJson();
    }

    private UserV1 createUserV1FromUser(User user) {
        UserV1 responseBody = new UserV1();
        responseBody.jid = user.getJid();
        responseBody.username = user.getUsername();
        if (user.isShowName()) {
            responseBody.name = user.getName();
        }
        responseBody.profilePictureUrl = user.getProfilePictureUrl().toString();
        return responseBody;
    }
}
