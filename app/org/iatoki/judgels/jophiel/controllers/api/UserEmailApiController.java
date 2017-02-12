package org.iatoki.judgels.jophiel.controllers.api;

import com.google.inject.Inject;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.ApiErrorCodeV1;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.UserEmailV1;
import org.iatoki.judgels.jophiel.user.User;
import org.iatoki.judgels.jophiel.user.UserService;
import org.iatoki.judgels.jophiel.user.profile.email.UserEmail;
import org.iatoki.judgels.jophiel.user.profile.email.UserEmailCreateForm;
import org.iatoki.judgels.jophiel.user.profile.email.UserEmailService;
import org.iatoki.judgels.play.api.JudgelsAPIBadRequestException;
import org.iatoki.judgels.play.api.JudgelsAPINotFoundException;
import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;
import org.iatoki.judgels.play.controllers.apis.JudgelsAPIGuard;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Result;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@JudgelsAPIGuard
public final class UserEmailApiController extends AbstractJudgelsAPIController {
    private final UserEmailService userEmailService;
    private final UserService userService;

    @Inject
    public UserEmailApiController(UserEmailService userEmailService, UserService userService) {
        this.userEmailService = userEmailService;
        this.userService = userService;
    }

    @Transactional
    public Result getAllUserEmail(String userJid) {
        User user = userService.findUserByJid(userJid).orElseThrow(() -> new JudgelsAPINotFoundException(ApiErrorCodeV1.USER_NOT_FOUND));
        Optional<UserEmail> primaryEmail = userEmailService.findEmailByJid(user.getEmailJid());
        List<UserEmail> userEmails = userEmailService.getEmailsByUserJid(user.getEmailJid());

        List<UserEmailV1> userEmailV1 = userEmails.stream()
                .map(x -> createUserEmailV1(x, primaryEmail.map(UserEmail::getEmail).orElse(null)))
                .collect(Collectors.toList());

        return okAsJson(userEmailV1);
    }

    @Transactional
    public Result getUserPrimaryEmail(String userJid) {
        User user = userService.findUserByJid(userJid).orElseThrow(() -> new JudgelsAPINotFoundException(ApiErrorCodeV1.USER_NOT_FOUND));
        UserEmail primaryEmail = userEmailService.findEmailByJid(user.getEmailJid()).get();
        return okAsJson(createUserEmailV1(primaryEmail, primaryEmail.getEmail()));
    }

    @Transactional
    public Result verifyEmail(String emailCode) {
        if (!userEmailService.isEmailCodeValid(emailCode)) {
            throw new JudgelsAPINotFoundException(ApiErrorCodeV1.EMAIL_INVALID_CODE);
        }

        UserEmail userEmail = userEmailService.findEmailByCode(emailCode).get();

        if (!userEmailService.isEmailNotVerified(userEmail.getEmail())) {
            return badRequestAsJson(ApiErrorCodeV1.EMAIL_ALREADY_VERIFIED);
        }

        userEmailService.activateEmail(emailCode, getCurrentUserIpAddress());
        return okJson();
    }

    @Transactional
    public Result createUserEmail(String userJid) {
        User user = userService.findUserByJid(userJid).orElseThrow(() -> new JudgelsAPINotFoundException(ApiErrorCodeV1.USER_NOT_FOUND));

        Form<UserEmailCreateForm> userEmailCreateForm = Form.form(UserEmailCreateForm.class).bindFromRequest();

        if (formHasErrors(userEmailCreateForm)) {
            return badRequestAsJson(ApiErrorCodeV1.INVALID_INPUT_PARAMETER);
        }

        UserEmailCreateForm userEmailCreateData = userEmailCreateForm.get();

        if (userEmailService.isEmailOwned(userEmailCreateData.email)) {
            return badRequestAsJson(ApiErrorCodeV1.EMAIL_ALREADY_OWNED);
        }

        UserEmail userEmail;
        if (user.getEmailJid() == null) {
            userEmail = userEmailService.addFirstEmail(userJid, userEmailCreateData.email, getCurrentUserIpAddress());
        } else {
            userEmail = userEmailService.addEmail(userJid, userEmailCreateData.email, getCurrentUserIpAddress());
        }
        userEmailService.sendEmailVerification(user.getName(), userEmailCreateData.email, getAbsoluteUrl(org.iatoki.judgels.jophiel.user.profile.email.routes.UserEmailController.verifyEmail(userEmailService.getEmailCodeOfUnverifiedEmail(userEmail.getJid()).get())));

        return okJson();
    }

    @Transactional
    public Result makeEmailPrimary(String userJid, String emailJid) {
        User user = userService.findUserByJid(userJid).orElseThrow(() -> new JudgelsAPINotFoundException(ApiErrorCodeV1.USER_NOT_FOUND));
        UserEmail userEmail = userEmailService.findEmailByJid(emailJid).orElseThrow(() -> new JudgelsAPINotFoundException(ApiErrorCodeV1.EMAIL_NOT_FOUND));

        if (!user.getJid().equals(userEmail.getUserJid())) {
            throw new JudgelsAPIBadRequestException(ApiErrorCodeV1.EMAIL_NOT_OWNED);
        }

        if (!userEmail.isEmailVerified()) {
            throw new JudgelsAPIBadRequestException(ApiErrorCodeV1.EMAIL_NOT_VERIFIED);
        }

        userEmailService.makeEmailPrimary(user.getJid(), userEmail.getJid(), getCurrentUserIpAddress());

        return okJson();
    }

    @Transactional
    public Result deleteEmail(String userJid, String emailJid) {
        User user = userService.findUserByJid(userJid).orElseThrow(() -> new JudgelsAPINotFoundException(ApiErrorCodeV1.USER_NOT_FOUND));
        UserEmail userEmail = userEmailService.findEmailByJid(emailJid).orElseThrow(() -> new JudgelsAPINotFoundException(ApiErrorCodeV1.EMAIL_NOT_FOUND));

        if (!user.getJid().equals(userEmail.getUserJid())) {
            throw new JudgelsAPIBadRequestException(ApiErrorCodeV1.EMAIL_NOT_OWNED);
        }

        if (user.getEmailJid().equals(userEmail.getJid())) {
            throw new JudgelsAPIBadRequestException(ApiErrorCodeV1.EMAIL_IS_PRIMARY);
        }

        userEmailService.removeEmail(userEmail.getJid());

        return okJson();
    }

    @Transactional
    public Result resendEmailVerification(String userJid, String emailJid) {
        UserEmail userEmail = userEmailService.findEmailByJid(emailJid).orElseThrow(() -> new JudgelsAPINotFoundException(ApiErrorCodeV1.EMAIL_NOT_FOUND));
        User user = userService.findUserByJid(userJid).orElseThrow(() -> new JudgelsAPINotFoundException(ApiErrorCodeV1.USER_NOT_FOUND));

        if (!user.getJid().equals(userEmail.getUserJid())) {
            throw new JudgelsAPIBadRequestException(ApiErrorCodeV1.EMAIL_NOT_OWNED);
        }

        if (!userEmailService.isEmailNotVerified(userEmail.getJid())) {
            throw new JudgelsAPIBadRequestException(ApiErrorCodeV1.EMAIL_ALREADY_VERIFIED);
        }

        String emailCode = userEmailService.getEmailCodeOfUnverifiedEmail(userEmail.getJid()).get();
        userEmailService.sendEmailVerification(user.getName(), userEmail.getEmail(), getAbsoluteUrl(org.iatoki.judgels.jophiel.user.profile.email.routes.UserEmailController.verifyEmail(emailCode)));

        return okJson();
    }

    @Transactional
    public Result activateEmail(String emailJid) {
        UserEmail userEmail = userEmailService.findEmailByJid(emailJid).orElseThrow(() -> new JudgelsAPINotFoundException(ApiErrorCodeV1.EMAIL_NOT_FOUND));

        if (userEmailService.isEmailOwned(userEmail.getEmail())) {
            throw new JudgelsAPIBadRequestException(ApiErrorCodeV1.EMAIL_ALREADY_OWNED);
        }

        String code = userEmailService.getEmailCodeOfUnverifiedEmail(userEmail.getJid()).get();
        userEmailService.activateEmail(code, getCurrentUserIpAddress());

        return okJson();
    }

    private UserEmailV1 createUserEmailV1(UserEmail userEmail, String primaryEmail) {
        UserEmailV1 result = new UserEmailV1();
        result.email = userEmail.getEmail();
        result.jid = userEmail.getJid();
        result.verified = userEmail.isEmailVerified();
        result.primary = userEmail.getEmail().equals(primaryEmail);

        return result;
    }
}
