package org.iatoki.judgels.jophiel.user.profile.email;

import org.iatoki.judgels.jophiel.activity.BasicActivityKeys;
import org.iatoki.judgels.jophiel.activity.UserActivityService;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.Authorized;
import org.iatoki.judgels.jophiel.controllers.securities.HasRole;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.user.User;
import org.iatoki.judgels.jophiel.user.UserService;
import org.iatoki.judgels.jophiel.user.account.html.activationView;
import org.iatoki.judgels.jophiel.user.profile.AbstractUserProfileController;
import org.iatoki.judgels.jophiel.user.profile.phone.UserPhoneService;
import org.iatoki.judgels.jophiel.user.profile.phone.UserProfileService;
import org.iatoki.judgels.play.template.HtmlTemplate;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class UserEmailController extends AbstractUserProfileController {

    private static final String USER = "user";
    private static final String EMAIL = "email";

    private final UserService userService;

    @Inject
    public UserEmailController(UserActivityService userActivityService, UserProfileService userProfileService, UserEmailService userEmailService, UserPhoneService userPhoneService, UserService userService) {
        super(userActivityService, userProfileService, userEmailService, userPhoneService);

        this.userService = userService;
    }


    @Transactional
    public Result verifyEmail(String emailCode) {
        if (!userEmailService.isEmailCodeValid(emailCode)) {
            return notFound();
        }

        UserEmail userEmail = userEmailService.findEmailByCode(emailCode);

        if (userEmailService.isEmailOwned(userEmail.getEmail())) {
            flashError(Messages.get("email.verify.error.emailOwned"));
            userEmailService.removeEmail(userEmail.getJid());

            return redirect(org.iatoki.judgels.jophiel.user.profile.routes.UserProfileController.index());
        }

        userEmailService.activateEmail(emailCode, getCurrentUserIpAddress());

        return showAfterActivateEmail();
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    @RequireCSRFCheck
    public Result postCreateEmail() {
        User user = userService.findUserByJid(getCurrentUserJid());

        Form<UserEmailCreateForm> userEmailCreateForm = Form.form(UserEmailCreateForm.class).bindFromRequest();

        if (formHasErrors(userEmailCreateForm)) {
            return showEditProfileWithEmailCreateForm(user, userEmailCreateForm);
        }

        UserEmailCreateForm userEmailCreateData = userEmailCreateForm.get();

        if (userEmailService.isEmailOwned(userEmailCreateData.email)) {
            flashError(Messages.get("email.create.error.emailOwned"));

            return redirect(org.iatoki.judgels.jophiel.user.profile.routes.UserProfileController.index());
        }

        UserEmail userEmail;
        if (user.getEmailJid() == null) {
            userEmail = userEmailService.addFirstEmail(getCurrentUserJid(), userEmailCreateData.email, getCurrentUserIpAddress());
        } else {
            userEmail = userEmailService.addEmail(getCurrentUserJid(), userEmailCreateData.email, getCurrentUserIpAddress());
        }
        userEmailService.sendEmailVerification(user.getName(), userEmailCreateData.email, getAbsoluteUrl(routes.UserEmailController.verifyEmail(userEmailService.getEmailCodeOfUnverifiedEmail(userEmail.getJid()))));

        flashInfo(Messages.get("email.verify.text.verificationSentTo", userEmailCreateData.email));

        addActivityLog(BasicActivityKeys.ADD_IN.construct(USER, user.getJid(), user.getUsername(), EMAIL, userEmail.getJid(), userEmail.getEmail()));

        return redirect(org.iatoki.judgels.jophiel.user.profile.routes.UserProfileController.index());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result makeEmailPrimary(long emailId) throws UserEmailNotFoundException {
        User user = userService.findUserByJid(getCurrentUserJid());
        UserEmail userEmail = userEmailService.findEmailById(emailId);

        if (!user.getJid().equals(userEmail.getUserJid())) {
            flashError(Messages.get("email.makePrimary.error.notOwned"));
            return redirect(org.iatoki.judgels.jophiel.user.profile.routes.UserProfileController.index());
        }

        if (user.getEmailJid().equals(userEmail.getJid())) {
            flashError(Messages.get("email.makePrimary.error.alreadyPrimary"));
            return redirect(org.iatoki.judgels.jophiel.user.profile.routes.UserProfileController.index());
        }

        if (!userEmail.isEmailVerified()) {
            flashError(Messages.get("email.makePrimary.error.notVerified"));
            return redirect(org.iatoki.judgels.jophiel.user.profile.routes.UserProfileController.index());
        }

        userEmailService.makeEmailPrimary(user.getJid(), userEmail.getJid(), getCurrentUserIpAddress());

        return redirect(org.iatoki.judgels.jophiel.user.profile.routes.UserProfileController.index());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result deleteEmail(long emailId) throws UserEmailNotFoundException {
        User user = userService.findUserByJid(getCurrentUserJid());
        UserEmail userEmail = userEmailService.findEmailById(emailId);

        if (!user.getJid().equals(userEmail.getUserJid())) {
            flashError(Messages.get("email.remove.error.notOwned"));
            return redirect(org.iatoki.judgels.jophiel.user.profile.routes.UserProfileController.index());
        }

        if (user.getEmailJid().equals(userEmail.getJid())) {
            flashError(Messages.get("email.remove.error.primary"));
            return redirect(org.iatoki.judgels.jophiel.user.profile.routes.UserProfileController.index());
        }

        userEmailService.removeEmail(userEmail.getJid());

        addActivityLog(BasicActivityKeys.REMOVE_FROM.construct(USER, user.getJid(), user.getUsername(), EMAIL, userEmail.getJid(), userEmail.getEmail()));

        return redirect(org.iatoki.judgels.jophiel.user.profile.routes.UserProfileController.index());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional(readOnly = true)
    public Result resendEmailVerification(long emailId) throws UserEmailNotFoundException {
        UserEmail userEmail = userEmailService.findEmailById(emailId);
        User user = userService.findUserByJid(userEmail.getUserJid());

        if (userEmailService.isEmailOwned(userEmail.getEmail())) {
            flashError(Messages.get("email.resend.error.emailOwned"));
            userEmailService.removeEmail(userEmail.getJid());

            return redirect(org.iatoki.judgels.jophiel.user.profile.routes.UserProfileController.index());
        }

        if (!userEmailService.isEmailNotVerified(userEmail.getJid())) {
            flashError(Messages.get("email.resend.error.alreadyVerified"));
            return redirect(org.iatoki.judgels.jophiel.user.routes.UserController.viewUnverifiedUsers());
        }

        String emailCode = userEmailService.getEmailCodeOfUnverifiedEmail(userEmail.getJid());
        userEmailService.sendEmailVerification(user.getName(), userEmail.getEmail(), getAbsoluteUrl(routes.UserEmailController.verifyEmail(emailCode)));

        flashInfo(Messages.get("email.verify.text.verificationSentTo") + " " + userEmail.getEmail() + ".");

        return redirect(org.iatoki.judgels.jophiel.user.routes.UserController.viewUnverifiedUsers());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional
    public Result activateEmail(long emailId) throws UserEmailNotFoundException {
        UserEmail userEmail = userEmailService.findEmailById(emailId);

        if (userEmailService.isEmailOwned(userEmail.getEmail())) {
            flashError(Messages.get("email.activate.error.emailOwned"));
            userEmailService.removeEmail(userEmail.getJid());

            return redirect(org.iatoki.judgels.jophiel.user.profile.routes.UserProfileController.index());
        }

        if (!userEmailService.isEmailNotVerified(userEmail.getJid())) {
            flashError(Messages.get("email.activate.error.alreadyVerified"));
            return redirect(org.iatoki.judgels.jophiel.user.routes.UserController.viewUnverifiedUsers());
        }

        String code = userEmailService.getEmailCodeOfUnverifiedEmail(userEmail.getJid());
        userEmailService.activateEmail(code, getCurrentUserIpAddress());

        return redirect(org.iatoki.judgels.jophiel.user.routes.UserController.viewUnverifiedUsers());
    }

    private Result showAfterActivateEmail() {
        HtmlTemplate template = getBaseHtmlTemplate();

        template.setContent(activationView.render());
        template.setMainTitle(Messages.get("activation.text.successful"));

        return renderTemplate(template);
    }
}
