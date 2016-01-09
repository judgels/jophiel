package org.iatoki.judgels.jophiel.user.profile;

import org.apache.commons.io.FilenameUtils;
import org.iatoki.judgels.jophiel.BasicActivityKeys;
import org.iatoki.judgels.jophiel.activity.UserActivityService;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.HasRole;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.forms.UserProfileSearchForm;
import org.iatoki.judgels.jophiel.user.User;
import org.iatoki.judgels.jophiel.user.UserService;
import org.iatoki.judgels.jophiel.user.profile.email.UserEmailService;
import org.iatoki.judgels.jophiel.user.profile.html.viewProfileView;
import org.iatoki.judgels.jophiel.user.profile.info.UserInfo;
import org.iatoki.judgels.jophiel.user.profile.info.UserInfoEditForm;
import org.iatoki.judgels.jophiel.user.profile.phone.UserPhoneService;
import org.iatoki.judgels.jophiel.user.profile.phone.UserProfileService;
import org.iatoki.judgels.play.template.HtmlTemplate;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.views.html.layouts.messageView;
import play.Logger;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Singleton
public final class UserProfileController extends AbstractUserProfileController {

    private static final String USER = "user";
    private static final String INFO = "info";

    private final UserService userService;

    @Inject
    public UserProfileController(UserActivityService userActivityService, UserProfileService userProfileService, UserEmailService userEmailService, UserPhoneService userPhoneService, UserService userService) {
        super(userActivityService, userProfileService, userEmailService, userPhoneService);

        this.userService = userService;
    }

    @Transactional
    @AddCSRFToken
    public Result index() {
        User user = userService.findUserByJid(getCurrentUserJid());

        return showEditProfile(user);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postEditOwnProfile() {
        User user = userService.findUserByJid(getCurrentUserJid());

        Form<UserProfileEditForm> userProfileEditForm = Form.form(UserProfileEditForm.class).bindFromRequest();

        if (formHasErrors(userProfileEditForm)) {
            Logger.error(userProfileEditForm.errors().toString());

            return showEditProfileWithProfileEditForm(user, userProfileEditForm);
        }

        UserProfileEditForm userProfileEditData = userProfileEditForm.get();

        if (!userProfileEditData.password.isEmpty()) {
            if (!userProfileEditData.password.equals(userProfileEditData.confirmPassword)) {
                userProfileEditForm.reject(Messages.get("basicProfile.error.passwordsDidntMatch"));

                return showEditProfileWithProfileEditForm(user, userProfileEditForm);
            }

            userProfileService.updateProfile(getCurrentUserJid(), userProfileEditData.name, userProfileEditData.showName, userProfileEditData.password, getCurrentUserIpAddress());
        } else {
            userProfileService.updateProfile(getCurrentUserJid(), userProfileEditData.name, userProfileEditData.showName, getCurrentUserIpAddress());
        }

        if (!user.getName().equals(userProfileEditData.name)) {
            session("name", userProfileEditData.name);
        }
        addActivityLog(BasicActivityKeys.EDIT.construct(USER, user.getJid(), user.getUsername()));

        return redirect(routes.UserProfileController.index());
    }

    @BodyParser.Of(value = BodyParser.MultipartFormData.class, maxLength = (2 << 20))
    @Transactional
    @RequireCSRFCheck
    public Result postEditOwnAvatar() {
        User user = userService.findUserByJid(getCurrentUserJid());

        // TODO catch 413 http response
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart avatar = body.getFile("avatar");

        if (avatar == null) {
            Form<UserAvatarForm> userAvatarForm = Form.form(UserAvatarForm.class);

            userAvatarForm.reject(Messages.get("basicProfile.avatar.error.noFile"));
            return showEditProfileWithAvatarForm(user, userAvatarForm);
        }

        String contentType = avatar.getContentType();
        if (!(contentType.equals("image/png") || contentType.equals("image/jpg") || contentType.equals("image/jpeg"))) {
            Form<UserAvatarForm> userAvatarForm = Form.form(UserAvatarForm.class);

            userAvatarForm.reject(Messages.get("basicProfile.avatar.error.notImage"));
            return showEditProfileWithAvatarForm(user, userAvatarForm);
        }

        try {
            String profilePictureName = userProfileService.updateAvatarWithGeneratedFilename(getCurrentUserJid(), avatar.getFile(), FilenameUtils.getExtension(avatar.getFilename()), getCurrentUserIpAddress());
            String profilePictureUrl = userProfileService.getAvatarImageUrlString(profilePictureName);
            try {
                new URL(profilePictureUrl);
                session("avatar", profilePictureUrl);
            } catch (MalformedURLException e) {
                session("avatar", org.iatoki.judgels.jophiel.controllers.api.pub.v1.routes.PublicUserAPIControllerV1.renderAvatarImage(profilePictureName).absoluteURL(request()));
            }
        } catch (IOException e) {
            Form<UserAvatarForm> userAvatarForm = Form.form(UserAvatarForm.class);

            userAvatarForm.reject(Messages.get("basicProfile.avatar.error.cantUpload"));
            return showEditProfileWithAvatarForm(user, userAvatarForm);
        }

        return redirect(routes.UserProfileController.index());
    }

    @Transactional
    public Result postEditOwnInfo() {
        User user = userService.findUserByJid(getCurrentUserJid());

        Form<UserInfoEditForm> userInfoEditForm = Form.form(UserInfoEditForm.class).bindFromRequest();

        if (formHasErrors(userInfoEditForm)) {
            Logger.error(userInfoEditForm.errors().toString());

            return showEditProfileWithInfoEditForm(user, userInfoEditForm);
        }

        UserInfoEditForm userInfoEditData = userInfoEditForm.get();
        userProfileService.upsertInfo(getCurrentUserJid(), userInfoEditData.gender, new Date(JudgelsPlayUtils.parseDate(userInfoEditData.birthDate)), userInfoEditData.streetAddress, userInfoEditData.postalCode, userInfoEditData.institution, userInfoEditData.city, userInfoEditData.provinceOrState, userInfoEditData.country, userInfoEditData.shirtSize, getCurrentUserIpAddress());

        addActivityLog(BasicActivityKeys.EDIT_IN.construct(USER, user.getJid(), user.getUsername(), INFO, user.getJid(), user.getUsername()));

        return redirect(routes.UserProfileController.index());
    }

    @Transactional
    public Result viewProfile(String username) {
        if (!userService.userExistsByUsername(username)) {
            return redirect(routes.UserProfileController.userNotFound());
        }

        User user = userService.findUserByUsername(username);

        UserInfo userInfo = null;
        if (userProfileService.infoExists(user.getJid())) {
            userInfo = userProfileService.findInfo(user.getJid());
        }

        return showViewProfile(user, userInfo);
    }

    @Transactional
    public Result postSearchProfile() {
        Form<UserProfileSearchForm> userProfileSearchForm = Form.form(UserProfileSearchForm.class).bindFromRequest();

        if (formHasErrors(userProfileSearchForm)) {
            return redirect(routes.UserProfileController.userNotFound());
        }

        String username = userProfileSearchForm.get().username;

        return redirect(routes.UserProfileController.viewProfile(username));
    }

    public Result userNotFound() {
        return showUserNotFound();
    }

    private Result showViewProfile(User user, UserInfo userInfo) {
        HtmlTemplate template = getBaseHtmlTemplate();

        template.setContent(viewProfileView.render(user, userInfo));

        return renderTemplate(template, user);
    }

    private Result showUserNotFound() {
        HtmlTemplate template = getBaseHtmlTemplate();

        template.setContent(messageView.render(Messages.get("user.search.notFound")));

        return renderTemplate(template);
    }
}
