package org.iatoki.judgels.jophiel.controllers;

import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.centerLayout;
import org.iatoki.judgels.jophiel.UserInfo;
import org.iatoki.judgels.jophiel.UserNotFoundException;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.Authorized;
import org.iatoki.judgels.jophiel.controllers.securities.HasRole;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.services.UserEmailService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.jophiel.views.html.account.activationView;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named
public final class UserEmailController extends AbstractJudgelsController {

    private final UserService userService;
    private final UserEmailService userEmailService;

    @Inject
    public UserEmailController(UserService userService, UserEmailService userEmailService) {
        this.userService = userService;
        this.userEmailService = userEmailService;
    }

    @Transactional
    public Result verifyEmail(String emailCode) {
        if (userEmailService.activateEmail(emailCode)) {
            LazyHtml content = new LazyHtml(activationView.render());
            content.appendLayout(c -> centerLayout.render(c));
            ControllerUtils.getInstance().appendTemplateLayout(content, "Verify Email");
            return ControllerUtils.getInstance().lazyOk(content);
        } else {
            return notFound();
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    @Transactional(readOnly = true)
    public Result resendEmailVerification(long userId) throws UserNotFoundException {
        UserInfo user = userService.findUserById(userId);
        if (userEmailService.isEmailNotVerified(user.getJid())) {
            String code = userEmailService.getEmailCodeOfUnverifiedEmail(user.getJid());
            userEmailService.sendActivationEmail(user.getName(), user.getEmail(), org.iatoki.judgels.jophiel.controllers.routes.UserEmailController.verifyEmail(code).absoluteURL(request(), request().secure()));

            return redirect(routes.UserController.viewUnverifiedUsers());
        } else {
            return forbidden();
        }
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = {"admin"})
    @Transactional
    public Result activateMainEmail(long userId) throws UserNotFoundException {
        UserInfo user = userService.findUserById(userId);
        if (userEmailService.isEmailNotVerified(user.getJid())) {
            String code = userEmailService.getEmailCodeOfUnverifiedEmail(user.getJid());
            userEmailService.activateEmail(code);

            return redirect(routes.UserController.viewUnverifiedUsers());
        } else {
            return forbidden();
        }
    }
}
