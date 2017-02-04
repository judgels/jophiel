package org.iatoki.judgels.jophiel.user.profile;

import org.iatoki.judgels.jophiel.AbstractJophielController;
import org.iatoki.judgels.jophiel.activity.UserActivityService;
import org.iatoki.judgels.jophiel.user.User;
import org.iatoki.judgels.jophiel.user.profile.email.UserEmail;
import org.iatoki.judgels.jophiel.user.profile.email.UserEmailCreateForm;
import org.iatoki.judgels.jophiel.user.profile.email.UserEmailService;
import org.iatoki.judgels.jophiel.user.profile.html.editProfileView;
import org.iatoki.judgels.jophiel.user.profile.info.UserInfo;
import org.iatoki.judgels.jophiel.user.profile.info.UserInfoEditForm;
import org.iatoki.judgels.jophiel.user.profile.info.UserInfoGenders;
import org.iatoki.judgels.jophiel.user.profile.phone.UserPhone;
import org.iatoki.judgels.jophiel.user.profile.phone.UserPhoneCreateForm;
import org.iatoki.judgels.jophiel.user.profile.phone.UserPhoneService;
import org.iatoki.judgels.jophiel.user.profile.phone.UserProfileService;
import org.iatoki.judgels.play.template.HtmlTemplate;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Result;

import java.util.List;
import java.util.Optional;

public abstract class AbstractUserProfileController extends AbstractJophielController {

    protected final UserProfileService userProfileService;
    protected final UserEmailService userEmailService;
    protected final UserPhoneService userPhoneService;

    protected AbstractUserProfileController(UserActivityService userActivityService, UserProfileService userProfileService, UserEmailService userEmailService, UserPhoneService userPhoneService) {
        super(userActivityService);

        this.userProfileService = userProfileService;
        this.userEmailService = userEmailService;
        this.userPhoneService = userPhoneService;
    }

    protected Result renderTemplate(HtmlTemplate template, User user) {
        template.markBreadcrumbLocation(Messages.get("profile.text.profile"), routes.UserProfileController.index());
        template.setMainTitle(Messages.get("profile.text.of", user.getUsername()));

        return renderTemplate(template);
    }

    protected Result showEditProfileWithAvatarForm(User user, Form<UserAvatarForm> avatarForm) {
        Form<UserProfileEditForm> profileEditForm = prepareProfileEditForm(user);
        Form<UserEmailCreateForm> emailCreateForm = Form.form(UserEmailCreateForm.class);
        Form<UserPhoneCreateForm> phoneCreateForm = Form.form(UserPhoneCreateForm.class);
        Form<UserInfoEditForm> infoEditForm = prepareInfoEditForm(user.getJid());

        return showEditProfile(user, profileEditForm, avatarForm, emailCreateForm, phoneCreateForm, infoEditForm);
    }

    protected Result showEditProfileWithInfoEditForm(User user, Form<UserInfoEditForm> infoEditForm) {
        Form<UserProfileEditForm> profileEditForm = prepareProfileEditForm(user);
        Form<UserAvatarForm> avatarForm = Form.form(UserAvatarForm.class);
        Form<UserEmailCreateForm> emailCreateForm = Form.form(UserEmailCreateForm.class);
        Form<UserPhoneCreateForm> phoneCreateForm = Form.form(UserPhoneCreateForm.class);

        return showEditProfile(user, profileEditForm, avatarForm, emailCreateForm, phoneCreateForm, infoEditForm);
    }

    protected Result showEditProfileWithProfileEditForm(User user, Form<UserProfileEditForm> profileEditForm) {
        Form<UserAvatarForm> avatarForm = Form.form(UserAvatarForm.class);
        Form<UserEmailCreateForm> emailCreateForm = Form.form(UserEmailCreateForm.class);
        Form<UserPhoneCreateForm> phoneCreateForm = Form.form(UserPhoneCreateForm.class);
        Form<UserInfoEditForm> infoEditForm = prepareInfoEditForm(user.getJid());

        return showEditProfile(user, profileEditForm, avatarForm, emailCreateForm, phoneCreateForm, infoEditForm);
    }

    protected Result showEditProfileWithPhoneCreateForm(User user, Form<UserPhoneCreateForm> phoneCreateForm) {
        Form<UserProfileEditForm> profileEditForm = prepareProfileEditForm(user);
        Form<UserAvatarForm> avatarForm = Form.form(UserAvatarForm.class);
        Form<UserEmailCreateForm> emailCreateForm = Form.form(UserEmailCreateForm.class);
        Form<UserInfoEditForm> infoEditForm = prepareInfoEditForm(user.getJid());

        return showEditProfile(user, profileEditForm, avatarForm, emailCreateForm, phoneCreateForm, infoEditForm);
    }

    protected Result showEditProfileWithEmailCreateForm(User user, Form<UserEmailCreateForm> emailCreateForm) {
        Form<UserProfileEditForm> profileEditForm = prepareProfileEditForm(user);
        Form<UserAvatarForm> avatarForm = Form.form(UserAvatarForm.class);
        Form<UserPhoneCreateForm> phoneCreateForm = Form.form(UserPhoneCreateForm.class);
        Form<UserInfoEditForm> infoEditForm = prepareInfoEditForm(user.getJid());

        return showEditProfile(user, profileEditForm, avatarForm, emailCreateForm, phoneCreateForm, infoEditForm);
    }

    protected Form<UserProfileEditForm> prepareProfileEditForm(User user) {
        Form<UserProfileEditForm> profileEditForm = Form.form(UserProfileEditForm.class);
        UserProfileEditForm userProfileEditData = new UserProfileEditForm();
        userProfileEditData.name = user.getName();
        userProfileEditData.showName = user.isShowName();

        profileEditForm = profileEditForm.fill(userProfileEditData);

        return profileEditForm;
    }

    private Form<UserInfoEditForm> prepareInfoEditForm(String userJid) {
        Form<UserInfoEditForm> infoEditForm = Form.form(UserInfoEditForm.class);

        UserInfoEditForm infoEditDta = new UserInfoEditForm();
        if (userProfileService.infoExists(userJid)) {
            UserInfo userInfo = userProfileService.findInfo(getCurrentUserJid());
            infoEditDta.birthDate = JudgelsPlayUtils.formatDate(userInfo.getBirthDate());
            infoEditDta.city = userInfo.getCity();
            infoEditDta.country = userInfo.getCountry();
            infoEditDta.gender = userInfo.getGender();
            infoEditDta.institution = userInfo.getInstitution();
            infoEditDta.postalCode = userInfo.getPostalCode();
            infoEditDta.provinceOrState = userInfo.getProvinceOrState();
            infoEditDta.shirtSize = userInfo.getShirtSize();
            infoEditDta.streetAddress = userInfo.getStreetAddress();
        } else {
            infoEditDta.birthDate = JudgelsPlayUtils.formatDate(0);
            infoEditDta.gender = UserInfoGenders.DO_NOT_WANT_TO_STATE.name();
        }
        infoEditForm = infoEditForm.fill(infoEditDta);

        return infoEditForm;
    }

    protected Result showEditProfile(User user) {
        Form<UserProfileEditForm> profileEditForm = prepareProfileEditForm(user);
        Form<UserAvatarForm> avatarForm = Form.form(UserAvatarForm.class);
        Form<UserEmailCreateForm> emailCreateForm = Form.form(UserEmailCreateForm.class);
        Form<UserPhoneCreateForm> phoneCreateForm = Form.form(UserPhoneCreateForm.class);
        Form<UserInfoEditForm> infoEditForm = prepareInfoEditForm(user.getJid());

        return showEditProfile(user, profileEditForm, avatarForm, emailCreateForm, phoneCreateForm, infoEditForm);
    }

    protected Result showEditProfile(User user, Form<UserProfileEditForm> profileEditForm, Form<UserAvatarForm> avatarForm, Form<UserEmailCreateForm> emailCreateForm, Form<UserPhoneCreateForm> phoneCreateForm, Form<UserInfoEditForm> infoEditForm) {
        UserEmail userPrimaryEmail = null;
        userPrimaryEmail = userEmailService.findEmailByJid(user.getEmailJid()).get();
        List<UserEmail> userEmails = userEmailService.getEmailsByUserJid(user.getJid());

        Optional<UserPhone> userPrimaryPhone = user.getPhoneJid().flatMap(userPhoneService::findPhoneByJid);
        List<UserPhone> userPhones = userPhoneService.getPhonesByUserJid(user.getJid());

        return showEditProfile(user, profileEditForm, avatarForm, emailCreateForm, userPrimaryEmail, userEmails, phoneCreateForm, userPrimaryPhone.orElse(null), userPhones, infoEditForm);
    }

    private Result showEditProfile(User user, Form<UserProfileEditForm> profileEditForm, Form<UserAvatarForm> avatarForm, Form<UserEmailCreateForm> emailCreateForm, UserEmail primaryEmail, List<UserEmail> userEmails, Form<UserPhoneCreateForm> phoneCreateForm, UserPhone primaryPhone, List<UserPhone> userPhones, Form<UserInfoEditForm> infoEditForm) {
        HtmlTemplate template = getBaseHtmlTemplate();

        template.setContent(editProfileView.render(profileEditForm, avatarForm, user.getProfilePictureUrl().toString(), emailCreateForm, primaryEmail, userEmails, phoneCreateForm, primaryPhone, userPhones, infoEditForm));

        return renderTemplate(template, user);
    }
}
