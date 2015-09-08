package org.iatoki.judgels.jophiel.controllers;

import com.google.common.collect.ImmutableList;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserEmail;
import org.iatoki.judgels.jophiel.UserInfo;
import org.iatoki.judgels.jophiel.UserInfoGenders;
import org.iatoki.judgels.jophiel.UserPhone;
import org.iatoki.judgels.jophiel.forms.UserEmailCreateForm;
import org.iatoki.judgels.jophiel.forms.UserInfoUpsertForm;
import org.iatoki.judgels.jophiel.forms.UserPhoneCreateForm;
import org.iatoki.judgels.jophiel.forms.UserAvatarForm;
import org.iatoki.judgels.jophiel.forms.UserProfileUpdateForm;
import org.iatoki.judgels.jophiel.services.UserEmailService;
import org.iatoki.judgels.jophiel.services.UserPhoneService;
import org.iatoki.judgels.jophiel.services.UserProfileService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.jophiel.views.html.profile.updateProfileView;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import play.api.mvc.Call;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Result;

import java.util.List;

public final class UserProfileControllerUtils {

    private static UserProfileControllerUtils instance;

    private final UserEmailService userEmailService;
    private final UserPhoneService userPhoneService;
    private final UserProfileService userProfileService;
    private final UserService userService;

    private UserProfileControllerUtils(UserEmailService userEmailService, UserPhoneService userPhoneService, UserProfileService userProfileService, UserService userService) {
        this.userEmailService = userEmailService;
        this.userPhoneService = userPhoneService;
        this.userProfileService = userProfileService;
        this.userService = userService;
    }

    public static synchronized void buildInstance(UserEmailService userEmailService, UserPhoneService userPhoneService, UserProfileService userProfileService, UserService userService) {
        if (instance != null) {
            throw new UnsupportedOperationException("UserProfileControllerUtils instance has already been built");
        }
        instance = new UserProfileControllerUtils(userEmailService, userPhoneService, userProfileService, userService);
    }

    static UserProfileControllerUtils getInstance() {
        if (instance == null) {
            throw new UnsupportedOperationException("UserProfileControllerUtils instance has not been built");
        }
        return instance;
    }

    Call getUpdateProfileCall() {
        return routes.UserProfileController.updateProfile();
    }

    Result showUpdateProfileWithAvatarForm(Form<UserAvatarForm> userAvatarForm) {
        User user = userService.findUserByJid(IdentityUtils.getUserJid());

        Form<UserProfileUpdateForm> userProfileUpdateForm = prepareProfileUpdateForm(user);
        Form<UserEmailCreateForm> userEmailCreateForm = Form.form(UserEmailCreateForm.class);
        Form<UserPhoneCreateForm> userPhoneCreateForm = Form.form(UserPhoneCreateForm.class);
        Form<UserInfoUpsertForm> userInfoUpsertForm = prepareInfoUpsertForm(user.getJid());

        return showUpdateProfileWithContact(user, userProfileUpdateForm, userAvatarForm, userEmailCreateForm, userPhoneCreateForm, userInfoUpsertForm);
    }

    Result showUpdateProfileWithInfoUpsertForm(Form<UserInfoUpsertForm> userInfoUpsertForm) {
        User user = userService.findUserByJid(IdentityUtils.getUserJid());

        Form<UserProfileUpdateForm> userProfileUpdateForm = prepareProfileUpdateForm(user);
        Form<UserAvatarForm> userAvatarForm = Form.form(UserAvatarForm.class);
        Form<UserEmailCreateForm> userEmailCreateForm = Form.form(UserEmailCreateForm.class);
        Form<UserPhoneCreateForm> userPhoneCreateForm = Form.form(UserPhoneCreateForm.class);

        return showUpdateProfileWithContact(user, userProfileUpdateForm, userAvatarForm, userEmailCreateForm, userPhoneCreateForm, userInfoUpsertForm);
    }

    Result showUpdateProfileWithProfileUpdateForm(Form<UserProfileUpdateForm> userProfileUpdateForm) {
        User user = userService.findUserByJid(IdentityUtils.getUserJid());

        Form<UserAvatarForm> userAvatarForm = Form.form(UserAvatarForm.class);
        Form<UserEmailCreateForm> userEmailCreateForm = Form.form(UserEmailCreateForm.class);
        Form<UserPhoneCreateForm> userPhoneCreateForm = Form.form(UserPhoneCreateForm.class);
        Form<UserInfoUpsertForm> userInfoUpsertForm = prepareInfoUpsertForm(user.getJid());

        return showUpdateProfileWithContact(user, userProfileUpdateForm, userAvatarForm, userEmailCreateForm, userPhoneCreateForm, userInfoUpsertForm);
    }

    Result showUpdateProfileWithPhoneCreateForm(Form<UserPhoneCreateForm> userPhoneCreateForm) {
        User user = userService.findUserByJid(IdentityUtils.getUserJid());

        Form<UserProfileUpdateForm> userProfileUpdateForm = prepareProfileUpdateForm(user);
        Form<UserAvatarForm> userAvatarForm = Form.form(UserAvatarForm.class);
        Form<UserEmailCreateForm> userEmailCreateForm = Form.form(UserEmailCreateForm.class);
        Form<UserInfoUpsertForm> userInfoUpsertForm = prepareInfoUpsertForm(user.getJid());

        return showUpdateProfileWithContact(user, userProfileUpdateForm, userAvatarForm, userEmailCreateForm, userPhoneCreateForm, userInfoUpsertForm);
    }

    Result showUpdateProfileWithEmailCreateForm(Form<UserEmailCreateForm> userEmailCreateForm) {
        User user = userService.findUserByJid(IdentityUtils.getUserJid());

        Form<UserProfileUpdateForm> userProfileUpdateForm = prepareProfileUpdateForm(user);
        Form<UserAvatarForm> userAvatarForm = Form.form(UserAvatarForm.class);
        Form<UserPhoneCreateForm> userPhoneCreateForm = Form.form(UserPhoneCreateForm.class);
        Form<UserInfoUpsertForm> userInfoUpsertForm = prepareInfoUpsertForm(user.getJid());

        return showUpdateProfileWithContact(user, userProfileUpdateForm, userAvatarForm, userEmailCreateForm, userPhoneCreateForm, userInfoUpsertForm);
    }

    Result showUpdateProfile() {
        User user = userService.findUserByJid(IdentityUtils.getUserJid());

        Form<UserProfileUpdateForm> userProfileUpdateForm = prepareProfileUpdateForm(user);
        Form<UserAvatarForm> userAvatarForm = Form.form(UserAvatarForm.class);
        Form<UserEmailCreateForm> userEmailCreateForm = Form.form(UserEmailCreateForm.class);
        Form<UserPhoneCreateForm> userPhoneCreateForm = Form.form(UserPhoneCreateForm.class);
        Form<UserInfoUpsertForm> userInfoUpsertForm = prepareInfoUpsertForm(user.getJid());

        return showUpdateProfileWithContact(user, userProfileUpdateForm, userAvatarForm, userEmailCreateForm, userPhoneCreateForm, userInfoUpsertForm);
    }

    private Result showUpdateProfile(Form<UserProfileUpdateForm> userProfileUpdateForm, Form<UserAvatarForm> userAvatarForm, Form<UserEmailCreateForm> userEmailCreateForm, UserEmail primaryEmail, List<UserEmail> userEmails, Form<UserPhoneCreateForm> userPhoneCreateForm, UserPhone primaryPhone, List<UserPhone> userPhones, Form<UserInfoUpsertForm> userInfoUpsertForm) {
        LazyHtml content = new LazyHtml(updateProfileView.render(userProfileUpdateForm, userAvatarForm, userEmailCreateForm, primaryEmail, userEmails, userPhoneCreateForm, primaryPhone, userPhones, userInfoUpsertForm));
        content.appendLayout(c -> headingLayout.render(Messages.get("profile.profile"), c));
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        JophielControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                new InternalLink(Messages.get("profile.profile"), routes.UserProfileController.updateProfile())
        ));
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Profile");
        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    private Form<UserProfileUpdateForm> prepareProfileUpdateForm(User user) {
        Form<UserProfileUpdateForm> userProfileUpdateForm = Form.form(UserProfileUpdateForm.class);
        UserProfileUpdateForm userProfileUpdateData = new UserProfileUpdateForm();
        userProfileUpdateData.name = user.getName();
        userProfileUpdateData.showName = user.isShowName();

        userProfileUpdateForm = userProfileUpdateForm.fill(userProfileUpdateData);

        return userProfileUpdateForm;
    }

    private Form<UserInfoUpsertForm> prepareInfoUpsertForm(String userJid) {
        Form<UserInfoUpsertForm> userInfoUpsertForm = Form.form(UserInfoUpsertForm.class);

        UserInfoUpsertForm userInfoUpsertData = new UserInfoUpsertForm();
        if (userProfileService.infoExists(IdentityUtils.getUserJid())) {
            UserInfo userInfo = userProfileService.getInfo(IdentityUtils.getUserJid());
            userInfoUpsertData.birthDate = JudgelsPlayUtils.formatDate(userInfo.getBirthDate());
            userInfoUpsertData.city = userInfo.getCity();
            userInfoUpsertData.country = userInfo.getCountry();
            userInfoUpsertData.gender = userInfo.getGender();
            userInfoUpsertData.institution = userInfo.getInstitution();
            userInfoUpsertData.postalCode = userInfo.getPostalCode();
            userInfoUpsertData.provinceOrState = userInfo.getProvinceOrState();
            userInfoUpsertData.shirtSize = userInfo.getShirtSize();
            userInfoUpsertData.streetAddress = userInfo.getStreetAddress();
        } else {
            userInfoUpsertData.birthDate = JudgelsPlayUtils.formatDate(0);
            userInfoUpsertData.gender = UserInfoGenders.DO_NOT_WANT_TO_STATE.name();
        }
        userInfoUpsertForm = userInfoUpsertForm.fill(userInfoUpsertData);

        return userInfoUpsertForm;
    }

    private Result showUpdateProfileWithContact(User user, Form<UserProfileUpdateForm> userProfileUpdateForm, Form<UserAvatarForm> userAvatarForm, Form<UserEmailCreateForm> userEmailCreateForm, Form<UserPhoneCreateForm> userPhoneCreateForm, Form<UserInfoUpsertForm> userInfoUpsertForm) {
        UserEmail userPrimaryEmail = null;
        if (user.getEmailJid() != null) {
            userPrimaryEmail = userEmailService.findEmailByJid(user.getEmailJid());
        }

        UserPhone userPrimaryPhone = null;
        if (user.getPhoneJid() != null) {
            userPrimaryPhone = userPhoneService.findPhoneByJid(user.getPhoneJid());
        }

        return showUpdateProfile(userProfileUpdateForm, userAvatarForm, userEmailCreateForm, userPrimaryEmail, userEmailService.getEmailsByUserJid(user.getJid()), userPhoneCreateForm, userPrimaryPhone, userPhoneService.getPhonesByUserJid(user.getJid()), userInfoUpsertForm);
    }
}
