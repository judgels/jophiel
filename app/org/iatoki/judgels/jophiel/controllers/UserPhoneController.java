package org.iatoki.judgels.jophiel.controllers;

import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserPhone;
import org.iatoki.judgels.jophiel.UserPhoneNotFoundException;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.HasRole;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.forms.UserPhoneCreateForm;
import org.iatoki.judgels.jophiel.services.UserActivityService;
import org.iatoki.judgels.jophiel.services.UserEmailService;
import org.iatoki.judgels.jophiel.services.UserPhoneService;
import org.iatoki.judgels.jophiel.services.UserProfileService;
import org.iatoki.judgels.jophiel.services.UserService;
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
public final class UserPhoneController extends AbstractUserProfileController {

    private final UserService userService;

    @Inject
    public UserPhoneController(UserActivityService userActivityService, UserProfileService userProfileService, UserEmailService userEmailService, UserPhoneService userPhoneService, UserService userService) {
        super(userActivityService, userProfileService, userEmailService, userPhoneService);

        this.userService = userService;
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    @RequireCSRFCheck
    public Result postCreatePhone() {
        User user = userService.findUserByJid(getCurrentUserJid());

        Form<UserPhoneCreateForm> userPhoneCreateForm = Form.form(UserPhoneCreateForm.class).bindFromRequest();

        if (formHasErrors(userPhoneCreateForm)) {
            return showEditProfileWithPhoneCreateForm(user, userPhoneCreateForm);
        }

        UserPhoneCreateForm userPhoneCreateData = userPhoneCreateForm.get();
        if (user.getPhoneJid() == null) {
            userPhoneService.addFirstPhone(getCurrentUserJid(), userPhoneCreateData.phone, getCurrentUserJid());
        } else {
            userPhoneService.addPhone(getCurrentUserJid(), userPhoneCreateData.phone, getCurrentUserJid());
        }

        return redirect(routes.UserProfileController.index());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result makePhonePrimary(long phoneId) throws UserPhoneNotFoundException {
        User user = userService.findUserByJid(getCurrentUserJid());
        UserPhone userPhone = userPhoneService.findPhoneById(phoneId);

        if (!user.getJid().equals(userPhone.getUserJid())) {
            flashError(Messages.get("phone.makePrimary.error.notOwned"));
            return redirect(routes.UserProfileController.index());
        }

        if (user.getEmailJid().equals(userPhone.getJid())) {
            flashError(Messages.get("phone.makePrimary.error.alreadyPrimary"));
            return redirect(routes.UserProfileController.index());
        }

        if (!userPhone.isPhoneVerified()) {
            flashError(Messages.get("phone.makePrimary.error.notVerified"));
            return redirect(routes.UserProfileController.index());
        }

        userPhoneService.makePhonePrimary(user.getJid(), userPhone.getJid(), getCurrentUserJid());

        return redirect(routes.UserProfileController.index());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result deletePhone(long phoneId) throws UserPhoneNotFoundException {
        User user = userService.findUserByJid(getCurrentUserJid());
        UserPhone userPhone = userPhoneService.findPhoneById(phoneId);

        if (!user.getJid().equals(userPhone.getUserJid())) {
            flashError(Messages.get("phone.remove.error.notOwned"));
            return redirect(routes.UserProfileController.index());
        }

        if (user.getEmailJid().equals(userPhone.getJid())) {
            flashError(Messages.get("phone.remove.error.primary"));
            return redirect(routes.UserProfileController.index());
        }

        userPhoneService.removePhone(userPhone.getJid());

        return redirect(routes.UserProfileController.index());
    }
}
