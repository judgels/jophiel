package org.iatoki.judgels.jophiel.controllers.api.client.v1;

import org.iatoki.judgels.jophiel.controllers.api.AbstractJophielAPIController;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.UserEmailV1;
import org.iatoki.judgels.jophiel.user.User;
import org.iatoki.judgels.jophiel.user.UserService;
import org.iatoki.judgels.jophiel.user.profile.email.*;
import play.data.Form;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Result;

import java.util.List;
import java.util.stream.Collectors;

public class ClientUserEmailAPIControllerV1 extends AbstractJophielAPIController {

    private final UserEmailService userEmailService;
    private final UserService userService;

    public ClientUserEmailAPIControllerV1(UserEmailService userEmailService, UserService userService) {
        this.userEmailService = userEmailService;
        this.userService = userService;
    }

    @Transactional
    public Result getAllUserEmail() {
        User user = userService.findUserByJid(getCurrentUserJid());
        UserEmail primaryEmail = userEmailService.findEmailByJid(user.getEmailJid());
        List<UserEmail> userEmails = userEmailService.getEmailsByUserJid(user.getEmailJid());

        List<UserEmailV1> userEmailV1 = userEmails.stream()
                .map(x -> createUserEmailV1(x, primaryEmail.getEmail()))
                .collect(Collectors.toList());

        return okAsJson(userEmailV1);
    }

    @Transactional
    public Result getPrimaryEmail() {
        User user = userService.findUserByJid(getCurrentUserJid());
        UserEmail primaryEmail = userEmailService.findEmailByJid(user.getPhoneJid());
        return okAsJson(createUserEmailV1(primaryEmail, primaryEmail.getEmail()));
    }

    @Transactional
    public Result verifyEmail(String emailCode) {
        if (!userEmailService.isEmailCodeValid(emailCode)) {
            return notFoundAsJson(Messages.get("api.email.invalid_code"));
        }

        UserEmail userEmail = userEmailService.findEmailByCode(emailCode);

        if (!userEmailService.isEmailNotVerified(userEmail.getEmail())) {
            return badRequestAsJson(Messages.get("api.email.already_verified"));
        }

        userEmailService.activateEmail(emailCode, getCurrentUserIpAddress());
        return okJson();
    }

    @Transactional
    public Result createEmail() {
        User user = userService.findUserByJid(getCurrentUserJid());

        Form<UserEmailCreateForm> userEmailCreateForm = Form.form(UserEmailCreateForm.class).bindFromRequest();

        if (formHasErrors(userEmailCreateForm)) {
            return badRequestAsJson(Messages.get("api.param.invalid"));
        }

        UserEmailCreateForm userEmailCreateData = userEmailCreateForm.get();

        if (userEmailService.isEmailOwned(userEmailCreateData.email)) {
            return badRequestAsJson("api.email.aready_owned");
        }

        UserEmail userEmail;
        if (user.getEmailJid() == null) {
            userEmail = userEmailService.addFirstEmail(getCurrentUserJid(), userEmailCreateData.email, getCurrentUserIpAddress());
        } else {
            userEmail = userEmailService.addEmail(getCurrentUserJid(), userEmailCreateData.email, getCurrentUserIpAddress());
        }
        userEmailService.sendEmailVerification(user.getName(), userEmailCreateData.email, getAbsoluteUrl(org.iatoki.judgels.jophiel.user.profile.email.routes.UserEmailController.verifyEmail(userEmailService.getEmailCodeOfUnverifiedEmail(userEmail.getJid()))));

        return okJson();
    }

    @Transactional
    public Result makeEmailPrimary(String emailJid) {
        User user = userService.findUserByJid(getCurrentUserJid());
        UserEmail userEmail = userEmailService.findEmailByJid(emailJid);

        if (!user.getJid().equals(userEmail.getUserJid())) {
            return unauthorizeddAsJson(Messages.get("api.email.now_owned"));
        }

        if (!userEmail.isEmailVerified()) {
            return badRequestAsJson(Messages.get("api.email.not_verified"));
        }

        userEmailService.makeEmailPrimary(user.getJid(), userEmail.getJid(), getCurrentUserIpAddress());

        return okJson();
    }

    @Transactional
    public Result deleteEmail(String emailJid) {
        User user = userService.findUserByJid(getCurrentUserJid());
        UserEmail userEmail = userEmailService.findEmailByJid(emailJid);

        if (!user.getJid().equals(userEmail.getUserJid())) {
            return unauthorizeddAsJson(Messages.get("api.email.now_owned"));
        }

        if (user.getEmailJid().equals(userEmail.getJid())) {
            return badRequestAsJson(Messages.get("api.email.primary"));
        }

        userEmailService.removeEmail(userEmail.getJid());

        return okJson();
    }

    @Transactional
    public Result resendEmailVerification(String emailJid) {
        UserEmail userEmail = userEmailService.findEmailByJid(emailJid);
        User user = userService.findUserByJid(userEmail.getUserJid());

        if (!userEmailService.isEmailNotVerified(userEmail.getJid())) {
            return badRequestAsJson("api.email.already_verified");
        }

        String emailCode = userEmailService.getEmailCodeOfUnverifiedEmail(userEmail.getJid());
        userEmailService.sendEmailVerification(user.getName(), userEmail.getEmail(), getAbsoluteUrl(org.iatoki.judgels.jophiel.user.profile.email.routes.UserEmailController.verifyEmail(emailCode)));

        return okJson();
    }

    @Transactional
    public Result activateEmail(String emailJid) {
        UserEmail userEmail = userEmailService.findEmailByJid(emailJid);

        if (userEmailService.isEmailOwned(userEmail.getEmail())) {
            return badRequestAsJson(Messages.get("api.email.already_owned"));
        }

        String code = userEmailService.getEmailCodeOfUnverifiedEmail(userEmail.getJid());
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
