package org.iatoki.judgels.jophiel.controllers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FilenameUtils;
import org.iatoki.judgels.jophiel.JophielUtils;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserInfo;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.HasRole;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.forms.SearchProfileForm;
import org.iatoki.judgels.jophiel.forms.UserAvatarForm;
import org.iatoki.judgels.jophiel.forms.UserInfoUpsertForm;
import org.iatoki.judgels.jophiel.forms.UserProfileUpdateForm;
import org.iatoki.judgels.jophiel.services.UserActivityService;
import org.iatoki.judgels.jophiel.services.UserProfileService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.jophiel.views.html.profile.viewProfileView;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.messageView;
import org.iatoki.judgels.play.views.html.layouts.tabLayout;
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
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

@Singleton
@Named
public final class UserProfileController extends AbstractJudgelsController {

    private final UserActivityService userActivityService;
    private final UserProfileService userProfileService;
    private final UserService userService;

    @Inject
    public UserProfileController(UserActivityService userActivityService, UserProfileService userProfileService, UserService userService) {
        this.userActivityService = userActivityService;
        this.userProfileService = userProfileService;
        this.userService = userService;
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    @AddCSRFToken
    public Result updateProfile() {
        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Try to update profile <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return UserProfileControllerUtils.getInstance().showUpdateProfile();
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    @RequireCSRFCheck
    public Result postUpdateProfile() {
        Form<UserProfileUpdateForm> userProfileUpdateForm = Form.form(UserProfileUpdateForm.class).bindFromRequest();
        User user = userService.findUserByJid(IdentityUtils.getUserJid());

        if (formHasErrors(userProfileUpdateForm)) {
            Logger.error(userProfileUpdateForm.errors().toString());

            return UserProfileControllerUtils.getInstance().showUpdateProfileWithProfileUpdateForm(userProfileUpdateForm);
        }

        UserProfileUpdateForm userProfileUpdateData = userProfileUpdateForm.get();

        if (!userProfileUpdateData.password.isEmpty()) {
            if (!userProfileUpdateData.password.equals(userProfileUpdateData.confirmPassword)) {
                userProfileUpdateForm.reject("profile.error.passwordsDidntMatch");

                return UserProfileControllerUtils.getInstance().showUpdateProfileWithProfileUpdateForm(userProfileUpdateForm);
            }

            userProfileService.updateProfile(IdentityUtils.getUserJid(), userProfileUpdateData.name, userProfileUpdateData.showName, userProfileUpdateData.password, IdentityUtils.getIpAddress());
        } else {
            userProfileService.updateProfile(IdentityUtils.getUserJid(), userProfileUpdateData.name, userProfileUpdateData.showName, IdentityUtils.getIpAddress());
        }
        session("name", userProfileUpdateData.name);

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Update profile.");

        return redirect(UserProfileControllerUtils.getInstance().getUpdateProfileCall());
    }

    @BodyParser.Of(value = BodyParser.MultipartFormData.class, maxLength = (2 << 20))
    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    @RequireCSRFCheck
    public Result postAvatar() {
        // TODO catch 413 http response
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart avatar = body.getFile("avatar");
        User user = userService.findUserByJid(IdentityUtils.getUserJid());

        if (avatar == null) {
            Form<UserAvatarForm> userAvatarForm = Form.form(UserAvatarForm.class);

            userAvatarForm.reject("profile.error.not_picture");
            return UserProfileControllerUtils.getInstance().showUpdateProfileWithAvatarForm(userAvatarForm);
        }

        String contentType = avatar.getContentType();
        if (!(contentType.equals("image/png") || contentType.equals("image/jpg") || contentType.equals("image/jpeg"))) {
            Form<UserAvatarForm> userAvatarForm = Form.form(UserAvatarForm.class);

            userAvatarForm.reject("error.profile.not_picture");
            return UserProfileControllerUtils.getInstance().showUpdateProfileWithAvatarForm(userAvatarForm);
        }

        try {
            String profilePictureName = userProfileService.updateAvatarWithGeneratedFilename(IdentityUtils.getUserJid(), avatar.getFile(), FilenameUtils.getExtension(avatar.getFilename()));
            String profilePictureUrl = userProfileService.getAvatarImageUrlString(profilePictureName);
            try {
                new URL(profilePictureUrl);
                session("avatar", profilePictureUrl.toString());
            } catch (MalformedURLException e) {
                session("avatar", org.iatoki.judgels.jophiel.controllers.apis.routes.UserAPIController.renderAvatarImage(profilePictureName).absoluteURL(request()));
            }
        } catch (IOException e) {
            Form<UserAvatarForm> userAvatarForm = Form.form(UserAvatarForm.class);

            userAvatarForm.reject("profile.error.cantUpload");
            return UserProfileControllerUtils.getInstance().showUpdateProfileWithAvatarForm(userAvatarForm);
        }

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Update avatar.");

        return redirect(UserProfileControllerUtils.getInstance().getUpdateProfileCall());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result postUpdateInfo() {
        Form<UserInfoUpsertForm> userInfoUpsertForm = Form.form(UserInfoUpsertForm.class).bindFromRequest();

        if (formHasErrors(userInfoUpsertForm)) {
            Logger.error(userInfoUpsertForm.errors().toString());

            return UserProfileControllerUtils.getInstance().showUpdateProfileWithInfoUpsertForm(userInfoUpsertForm);
        }

        UserInfoUpsertForm userInfoUpsertData = userInfoUpsertForm.get();
        userProfileService.upsertInfo(IdentityUtils.getUserJid(), userInfoUpsertData.gender, new Date(JudgelsPlayUtils.parseDate(userInfoUpsertData.birthDate)), userInfoUpsertData.streetAddress, userInfoUpsertData.postalCode, userInfoUpsertData.institution, userInfoUpsertData.city, userInfoUpsertData.provinceOrState, userInfoUpsertData.country, userInfoUpsertData.shirtSize, IdentityUtils.getIpAddress());

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Update info.");

        return redirect(UserProfileControllerUtils.getInstance().getUpdateProfileCall());
    }

    @Transactional
    public Result viewProfile(String username) {
        if (!userService.userExistsByUsername(username)) {
            return redirect(routes.UserProfileController.userNotFound());
        }

        User user = userService.findUserByUsername(username);

        UserInfo userInfo = null;
        if (userProfileService.infoExists(user.getJid())) {
            userInfo = userProfileService.getInfo(user.getJid());
        }

        LazyHtml content = new LazyHtml(viewProfileView.render(user, userInfo));
        if (IdentityUtils.getUserJid() != null) {
            if (JophielUtils.hasRole("admin")) {
                content.appendLayout(c -> tabLayout.render(ImmutableList.of(new InternalLink(Messages.get("profile.profile"), routes.UserProfileController.viewProfile(username)), new InternalLink(Messages.get("profile.activities"), routes.UserActivityController.viewUserActivities(username))), c));
            }
            content.appendLayout(c -> headingLayout.render(user.getUsername(), c));
            JophielControllerUtils.getInstance().appendSidebarLayout(content);
            JophielControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("profile.profile"), routes.UserProfileController.viewProfile(username))
            ));

            JophielControllerUtils.getInstance().addActivityLog(userActivityService, "View user profile " + user.getUsername() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");
        } else {
            content.appendLayout(c -> headingLayout.render(user.getUsername(), c));
            JophielControllerUtils.getInstance().appendSidebarLayout(content);
        }
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Profile");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    @Transactional
    public Result postViewProfile() {
        Form<SearchProfileForm> searchProfileForm = Form.form(SearchProfileForm.class).bindFromRequest();

        if (formHasErrors(searchProfileForm)) {
            return redirect(routes.UserProfileController.userNotFound());
        }

        String username = searchProfileForm.get().username;

        if (!userService.userExistsByUsername(username)) {
            return redirect(routes.UserProfileController.userNotFound());
        }

        User user = userService.findUserByUsername(username);

        UserInfo userInfo = null;
        if (userProfileService.infoExists(user.getJid())) {
            userInfo = userProfileService.getInfo(user.getJid());
        }

        LazyHtml content = new LazyHtml(viewProfileView.render(user, userInfo));
        if (IdentityUtils.getUserJid() != null) {
            if (JophielUtils.hasRole("admin")) {
                content.appendLayout(c -> tabLayout.render(ImmutableList.of(new InternalLink(Messages.get("profile.profile"), routes.UserProfileController.viewProfile(username)), new InternalLink(Messages.get("profile.activities"), routes.UserActivityController.viewUserActivities(username))), c));
            }
            content.appendLayout(c -> headingLayout.render(user.getUsername(), c));
            JophielControllerUtils.getInstance().appendSidebarLayout(content);
            JophielControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("profile.profile"), routes.UserProfileController.viewProfile(username))
            ));

            JophielControllerUtils.getInstance().addActivityLog(userActivityService, "View user profile " + user.getUsername() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");
        } else {
            content.appendLayout(c -> headingLayout.render(user.getUsername(), c));
            JophielControllerUtils.getInstance().appendSidebarLayout(content);
        }
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Profile");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    public Result userNotFound() {
        LazyHtml content = new LazyHtml(messageView.render(Messages.get("user.search.notFound")));
        content.appendLayout(c -> headingLayout.render(Messages.get("user.search"), c));
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "User not Found");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }
}
