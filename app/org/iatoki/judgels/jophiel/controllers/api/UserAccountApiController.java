package org.iatoki.judgels.jophiel.controllers.api;

import com.google.inject.Inject;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.ApiErrorCodeV1;
import org.iatoki.judgels.jophiel.user.UserService;
import org.iatoki.judgels.jophiel.user.account.PasswordChangeApiForm;
import org.iatoki.judgels.jophiel.user.account.PasswordForgotForm;
import org.iatoki.judgels.jophiel.user.account.UserAccountService;
import org.iatoki.judgels.jophiel.user.profile.email.UserEmailService;
import org.iatoki.judgels.play.api.JudgelsAPIBadRequestException;
import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;
import org.iatoki.judgels.play.controllers.apis.JudgelsAPIGuard;
import play.data.Form;
import play.mvc.Result;

import javax.transaction.Transactional;

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

    @Transactional
    public Result forgotPassword() {
        Form<PasswordForgotForm> forgotPasswordForm = Form.form(PasswordForgotForm.class).bindFromRequest();
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
        Form<PasswordChangeApiForm> passwordChangeForm = Form.form(PasswordChangeApiForm.class).bindFromRequest();

        if (!userAccountService.isValidToChangePassword(code, System.currentTimeMillis())) {
            throw new JudgelsAPIBadRequestException(ApiErrorCodeV1.INVALID_FORGOT_PASSWORD_CODE);
        }

        PasswordChangeApiForm passwordChangeData = passwordChangeForm.get();
        userAccountService.processChangePassword(code, passwordChangeData.password, null);

        return okJson();
    }
}
