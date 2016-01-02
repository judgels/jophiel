package org.iatoki.judgels.jophiel.user.profile.phone;

import org.iatoki.judgels.jophiel.BasicActivityKeys;
import org.iatoki.judgels.jophiel.activity.UserActivityService;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.HasRole;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.user.User;
import org.iatoki.judgels.jophiel.user.UserService;
import org.iatoki.judgels.jophiel.user.profile.AbstractUserProfileController;
import org.iatoki.judgels.jophiel.user.profile.email.UserEmailService;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class UserPhoneController extends AbstractUserProfileController {

    private static final String USER = "user";
    private static final String PHONE = "phone";

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
        UserPhone userPhone;
        if (user.getPhoneJid() == null) {
            userPhone = userPhoneService.addFirstPhone(getCurrentUserJid(), userPhoneCreateData.phone, getCurrentUserJid());
        } else {
            userPhone = userPhoneService.addPhone(getCurrentUserJid(), userPhoneCreateData.phone, getCurrentUserJid());
        }

        addActivityLog(BasicActivityKeys.ADD_IN.construct(USER, user.getJid(), user.getUsername(), PHONE, userPhone.getJid(), userPhone.getPhone()));

        return redirect(org.iatoki.judgels.jophiel.user.profile.routes.UserProfileController.index());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result makePhonePrimary(long phoneId) throws UserPhoneNotFoundException {
        User user = userService.findUserByJid(getCurrentUserJid());
        UserPhone userPhone = userPhoneService.findPhoneById(phoneId);

        if (!user.getJid().equals(userPhone.getUserJid())) {
            flashError(Messages.get("phone.makePrimary.error.notOwned"));
            return redirect(org.iatoki.judgels.jophiel.user.profile.routes.UserProfileController.index());
        }

        if (user.getEmailJid().equals(userPhone.getJid())) {
            flashError(Messages.get("phone.makePrimary.error.alreadyPrimary"));
            return redirect(org.iatoki.judgels.jophiel.user.profile.routes.UserProfileController.index());
        }

        if (!userPhone.isPhoneVerified()) {
            flashError(Messages.get("phone.makePrimary.error.notVerified"));
            return redirect(org.iatoki.judgels.jophiel.user.profile.routes.UserProfileController.index());
        }

        userPhoneService.makePhonePrimary(user.getJid(), userPhone.getJid(), getCurrentUserJid());

        return redirect(org.iatoki.judgels.jophiel.user.profile.routes.UserProfileController.index());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result deletePhone(long phoneId) throws UserPhoneNotFoundException {
        User user = userService.findUserByJid(getCurrentUserJid());
        UserPhone userPhone = userPhoneService.findPhoneById(phoneId);

        if (!user.getJid().equals(userPhone.getUserJid())) {
            flashError(Messages.get("phone.remove.error.notOwned"));
            return redirect(org.iatoki.judgels.jophiel.user.profile.routes.UserProfileController.index());
        }

        if (user.getEmailJid().equals(userPhone.getJid())) {
            flashError(Messages.get("phone.remove.error.primary"));
            return redirect(org.iatoki.judgels.jophiel.user.profile.routes.UserProfileController.index());
        }

        userPhoneService.removePhone(userPhone.getJid());

        addActivityLog(BasicActivityKeys.REMOVE_FROM.construct(USER, user.getJid(), user.getUsername(), PHONE, userPhone.getJid(), userPhone.getPhone()));

        return redirect(org.iatoki.judgels.jophiel.user.profile.routes.UserProfileController.index());
    }
}
