package org.iatoki.judgels.jophiel.controllers.api.internal;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.iatoki.judgels.jophiel.controllers.api.AbstractJophielAPIController;
import org.iatoki.judgels.jophiel.user.UserService;
import org.iatoki.judgels.jophiel.user.account.*;
import org.iatoki.judgels.jophiel.user.profile.email.UserEmailService;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Named;

@Singleton
@Named
public class InternalUserAccountApiController extends AbstractJophielAPIController {

    private final UserAccountService userAccountService;
    private final UserEmailService userEmailService;
    private final UserService userService;

    @Inject
    public InternalUserAccountApiController(UserAccountService userAccountService, UserEmailService userEmailService, UserService userService) {
        this.userAccountService = userAccountService;
        this.userEmailService = userEmailService;
        this.userService = userService;
    }

    public Result forgotPassword() {
        Form<PasswordForgotForm> forgotPasswordForm = Form.form(PasswordForgotForm.class).bindFromRequest();
        if (formHasErrors(forgotPasswordForm)) {
            return badRequestAsJson(Messages.get("api.param.invalid"));
        }

        PasswordForgotForm forgotPasswordData = forgotPasswordForm.get();
        if (!userService.userExistsByUsername(forgotPasswordData.username)) {
            return badRequestAsJson(Messages.get("forgotPassword.error.usernameNotExists"));
        }

        if (!userEmailService.isEmailOwnedByUser(forgotPasswordData.email, forgotPasswordData.username)) {
            return badRequestAsJson("forgotPassword.error.emailInvalid");
        }

        String forgotPasswordCode = userAccountService.generateForgotPasswordRequest(forgotPasswordData.username, forgotPasswordData.email, getCurrentUserIpAddress());
        userEmailService.sendChangePasswordEmail(forgotPasswordData.email, getAbsoluteUrl(org.iatoki.judgels.jophiel.user.account.routes.UserAccountController.changePassword(forgotPasswordCode)));

        return okJson();
    }

    public Result changePassword(String code) {
        Form<PasswordChangeApiForm> passwordChangeForm = Form.form(PasswordChangeApiForm.class).bindFromRequest();

        if (!userAccountService.isValidToChangePassword(code, System.currentTimeMillis())) {
            return badRequestAsJson(Messages.get("forgotPassword.code.invalid"));
        }

        PasswordChangeApiForm passwordChangeData = passwordChangeForm.get();
        userAccountService.processChangePassword(code, passwordChangeData.password, getCurrentUserJid());

        return okJson();
    }
}
