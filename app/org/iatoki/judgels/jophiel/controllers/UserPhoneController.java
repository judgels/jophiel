package org.iatoki.judgels.jophiel.controllers;

import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserPhone;
import org.iatoki.judgels.jophiel.UserPhoneNotFoundException;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.HasRole;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.forms.UserPhoneCreateForm;
import org.iatoki.judgels.jophiel.services.UserPhoneService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
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
public final class UserPhoneController extends AbstractJudgelsController {

    private final UserPhoneService userPhoneService;
    private final UserService userService;

    @Inject
    public UserPhoneController(UserPhoneService userPhoneService, UserService userService) {
        this.userPhoneService = userPhoneService;
        this.userService = userService;
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    @RequireCSRFCheck
    public Result postCreatePhone() {
        Form<UserPhoneCreateForm> userPhoneCreateForm = Form.form(UserPhoneCreateForm.class).bindFromRequest();

        if (formHasErrors(userPhoneCreateForm)) {
            return UserProfileControllerUtils.getInstance().showUpdateProfileWithPhoneCreateForm(userPhoneCreateForm);
        }

        User user = userService.findUserByJid(IdentityUtils.getUserJid());
        UserPhoneCreateForm userPhoneCreateData = userPhoneCreateForm.get();
        if (user.getPhoneJid() == null) {
            userPhoneService.addFirstPhone(IdentityUtils.getUserJid(), userPhoneCreateData.phone);
        } else {
            userPhoneService.addPhone(IdentityUtils.getUserJid(), userPhoneCreateData.phone);
        }

        return redirect(UserProfileControllerUtils.getInstance().getUpdateProfileCall());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result makePhonePrimary(long phoneId) throws UserPhoneNotFoundException {
        User user = userService.findUserByJid(IdentityUtils.getUserJid());
        UserPhone userPhone = userPhoneService.findPhoneById(phoneId);

        if (!user.getJid().equals(userPhone.getUserJid())) {
            flashError(Messages.get("user.phone.makePrimary.error.phoneIsNotOwned"));
            return redirect(UserProfileControllerUtils.getInstance().getUpdateProfileCall());
        }

        if (user.getEmailJid().equals(userPhone.getJid())) {
            flashError(Messages.get("user.phone.makePrimary.error.phoneIsPrimary"));
            return redirect(UserProfileControllerUtils.getInstance().getUpdateProfileCall());
        }

        if (!userPhone.isPhoneVerified()) {
            flashError(Messages.get("user.phone.makePrimary.error.phoneIsNotVerified"));
            return redirect(UserProfileControllerUtils.getInstance().getUpdateProfileCall());
        }

        userPhoneService.makePhonePrimary(user.getJid(), userPhone.getJid());

        return redirect(UserProfileControllerUtils.getInstance().getUpdateProfileCall());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result removePhone(long phoneId) throws UserPhoneNotFoundException {
        User user = userService.findUserByJid(IdentityUtils.getUserJid());
        UserPhone userPhone = userPhoneService.findPhoneById(phoneId);

        if (!user.getJid().equals(userPhone.getUserJid())) {
            flashError(Messages.get("user.phone.remove.error.phoneIsNotOwned"));
            return redirect(UserProfileControllerUtils.getInstance().getUpdateProfileCall());
        }

        if (user.getEmailJid().equals(userPhone.getJid())) {
            flashError(Messages.get("user.phone.remove.error.phoneIsPrimary"));
            return redirect(UserProfileControllerUtils.getInstance().getUpdateProfileCall());
        }

        userPhoneService.removePhone(userPhone.getJid());

        return redirect(UserProfileControllerUtils.getInstance().getUpdateProfileCall());
    }
}
