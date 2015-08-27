package org.iatoki.judgels.jophiel.controllers;

import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserEmail;
import org.iatoki.judgels.jophiel.UserEmailNotFoundException;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.Authorized;
import org.iatoki.judgels.jophiel.controllers.securities.HasRole;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.forms.UserEmailCreateForm;
import org.iatoki.judgels.jophiel.services.UserEmailService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.jophiel.views.html.account.activationView;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.centerLayout;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named
public final class UserEmailController extends AbstractJudgelsController {

    private final UserEmailService userEmailService;
    private final UserService userService;

    @Inject
    public UserEmailController(UserEmailService userEmailService, UserService userService) {
        this.userEmailService = userEmailService;
        this.userService = userService;
    }

    @Transactional
    public Result verifyEmail(String emailCode) {
        if (!userEmailService.isEmailCodeValid(emailCode)) {
            return notFound();
        }

        userEmailService.activateEmail(emailCode);

        LazyHtml content = new LazyHtml(activationView.render());
        content.appendLayout(c -> centerLayout.render(c));
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Verify Email");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    @RequireCSRFCheck
    public Result postCreateEmail() {
        Form<UserEmailCreateForm> userEmailCreateForm = Form.form(UserEmailCreateForm.class).bindFromRequest();

        if (formHasErrors(userEmailCreateForm)) {
            return UserProfileControllerUtils.getInstance().showUpdateProfileWithEmailCreateForm(userEmailCreateForm);
        }

        UserEmailCreateForm userEmailCreateData = userEmailCreateForm.get();

        User user = userService.findUserByJid(IdentityUtils.getUserJid());
        String emailCode;
        if (user.getEmailJid() == null) {
            emailCode = userEmailService.addFirstEmail(IdentityUtils.getUserJid(), userEmailCreateData.email);
        } else {
            emailCode = userEmailService.addEmail(IdentityUtils.getUserJid(), userEmailCreateData.email);
        }
        userEmailService.sendEmailVerification(user.getName(), userEmailCreateData.email, routes.UserEmailController.verifyEmail(emailCode).absoluteURL(request(), request().secure()));

        flashInfo(Messages.get("user.email.verificationEmailSentTo") + " " + userEmailCreateData.email + ".");

        return redirect(UserProfileControllerUtils.getInstance().getUpdateProfileCall());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result makeEmailPrimary(long emailId) throws UserEmailNotFoundException {
        User user = userService.findUserByJid(IdentityUtils.getUserJid());
        UserEmail userEmail = userEmailService.findEmailById(emailId);

        if (!user.getJid().equals(userEmail.getUserJid())) {
            flashError(Messages.get("user.email.makePrimary.error.emailIsNotOwned"));
            return redirect(UserProfileControllerUtils.getInstance().getUpdateProfileCall());
        }

        if (user.getEmailJid().equals(userEmail.getJid())) {
            flashError(Messages.get("user.email.makePrimary.error.emailIsPrimary"));
            return redirect(UserProfileControllerUtils.getInstance().getUpdateProfileCall());
        }

        if (!userEmail.isEmailVerified()) {
            flashError(Messages.get("user.email.makePrimary.error.emailIsNotVerified"));
            return redirect(UserProfileControllerUtils.getInstance().getUpdateProfileCall());
        }

        userEmailService.makeEmailPrimary(user.getJid(), userEmail.getJid());

        return redirect(UserProfileControllerUtils.getInstance().getUpdateProfileCall());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result removeEmail(long emailId) throws UserEmailNotFoundException {
        User user = userService.findUserByJid(IdentityUtils.getUserJid());
        UserEmail userEmail = userEmailService.findEmailById(emailId);

        if (!user.getJid().equals(userEmail.getUserJid())) {
            flashError(Messages.get("user.email.remove.error.emailIsNotOwned"));
            return redirect(UserProfileControllerUtils.getInstance().getUpdateProfileCall());
        }

        if (user.getEmailJid().equals(userEmail.getJid())) {
            flashError(Messages.get("user.email.remove.error.emailIsPrimary"));
            return redirect(UserProfileControllerUtils.getInstance().getUpdateProfileCall());
        }

        userEmailService.removeEmail(userEmail.getJid());

        return redirect(UserProfileControllerUtils.getInstance().getUpdateProfileCall());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional(readOnly = true)
    public Result resendEmailVerification(long emailId) throws UserEmailNotFoundException {
        UserEmail userEmail = userEmailService.findEmailById(emailId);
        User user = userService.findUserByJid(userEmail.getUserJid());

        if (!userEmailService.isEmailNotVerified(userEmail.getJid())) {
            flashError(Messages.get("user.email.resend.error.emailIsAlreadyVerified"));
            return redirect(routes.UserController.viewUnverifiedUsers());
        }

        String emailCode = userEmailService.getEmailCodeOfUnverifiedEmail(userEmail.getJid());
        userEmailService.sendEmailVerification(user.getName(), userEmail.getEmail(), routes.UserEmailController.verifyEmail(emailCode).absoluteURL(request(), request().secure()));

        flashInfo(Messages.get("user.email.verificationEmailSentTo") + " " + userEmail.getEmail() + ".");

        return redirect(routes.UserController.viewUnverifiedUsers());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional
    public Result activateEmail(long emailId) throws UserEmailNotFoundException {
        UserEmail userEmail = userEmailService.findEmailById(emailId);

        if (!userEmailService.isEmailNotVerified(userEmail.getJid())) {
            flashError(Messages.get("user.email.activate.error.emailIsAlreadyVerified"));
            return redirect(routes.UserController.viewUnverifiedUsers());
        }

        String code = userEmailService.getEmailCodeOfUnverifiedEmail(userEmail.getJid());
        userEmailService.activateEmail(code);

        return redirect(routes.UserController.viewUnverifiedUsers());
    }
}
