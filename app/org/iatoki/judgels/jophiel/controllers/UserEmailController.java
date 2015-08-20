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
    @Authorized(value = "admin")
    @Transactional(readOnly = true)
    public Result resendEmailVerification(long userId) throws UserNotFoundException {
        UserInfo user = userService.findUserInfoById(userId);
        if (!userEmailService.isEmailNotVerified(user.getJid())) {
            return forbidden();
        }

        String code = userEmailService.getEmailCodeOfUnverifiedEmail(user.getJid());
        userEmailService.sendActivationEmail(user.getName(), user.getEmail(), org.iatoki.judgels.jophiel.controllers.routes.UserEmailController.verifyEmail(code).absoluteURL(request(), request().secure()));

        return redirect(routes.UserController.viewUnverifiedUsers());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional
    public Result activateMainEmail(long userId) throws UserNotFoundException {
        UserInfo user = userService.findUserInfoById(userId);
        if (userEmailService.emailExists(user.getEmail()) && !userEmailService.isEmailNotVerified(user.getJid())) {
            return forbidden();
        }

        String code = userEmailService.getEmailCodeOfUnverifiedEmail(user.getJid());
        userEmailService.activateEmail(code);

        return redirect(routes.UserController.viewUnverifiedUsers());
    }
}
