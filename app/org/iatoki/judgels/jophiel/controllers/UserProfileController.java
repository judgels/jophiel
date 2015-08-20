package org.iatoki.judgels.jophiel.controllers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FilenameUtils;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.centerLayout;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.tabLayout;
import org.iatoki.judgels.jophiel.UserInfo;
import org.iatoki.judgels.jophiel.forms.UserProfileForm;
import org.iatoki.judgels.jophiel.forms.UserProfilePictureForm;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.Authorized;
import org.iatoki.judgels.jophiel.controllers.securities.HasRole;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.services.UserActivityService;
import org.iatoki.judgels.jophiel.services.UserProfileService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.jophiel.views.html.profile.editProfileView;
import org.iatoki.judgels.jophiel.views.html.profile.serviceEditProfileView;
import org.iatoki.judgels.jophiel.views.html.profile.viewProfileView;
import play.Logger;
import play.data.Form;
import play.db.jpa.Transactional;
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
    public Result profile() {
        return serviceProfile(null);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result postProfile() {
        return postServiceProfile(null);
    }

    @Authenticated({LoggedIn.class, HasRole.class})
    @Authorized("admin")
    @Transactional
    public Result viewProfile(String username) {
        if (!userService.existsUserByUsername(username)) {
            return notFound();
        }

        UserInfo userInfo = userService.findUserInfoByUsername(username);

        LazyHtml content = new LazyHtml(viewProfileView.render(userInfo));
        if (IdentityUtils.getUserJid() != null) {
            content.appendLayout(c -> tabLayout.render(ImmutableList.of(new InternalLink(Messages.get("profile.profile"), routes.UserProfileController.viewProfile(username)), new InternalLink(Messages.get("profile.activities"), routes.UserActivityController.viewUserActivities(username))), c));
            content.appendLayout(c -> headingLayout.render(userInfo.getUsername(), c));
            JophielControllerUtils.getInstance().appendSidebarLayout(content);
            JophielControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("profile.profile"), routes.UserProfileController.viewProfile(username))
            ));
        } else {
            content.appendLayout(c -> headingLayout.render(userInfo.getUsername(), c));
            content.appendLayout(c -> centerLayout.render(c));
        }
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Profile");

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "View userInfo profile " + userInfo.getUsername() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    @BodyParser.Of(value = BodyParser.MultipartFormData.class, maxLength = (2 << 20))
    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result postAvatar() {
        return postServiceAvatar(null);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result serviceProfile(String continueUrl) {
        Form<UserProfileForm> userProfileForm = Form.form(UserProfileForm.class);
        Form<UserProfilePictureForm> userProfilePictureForm = Form.form(UserProfilePictureForm.class);

        UserInfo user = userService.findUserInfoByJid(IdentityUtils.getUserJid());
        UserProfileForm userProfileData = new UserProfileForm();
        userProfileData.name = user.getName();

        userProfileForm = userProfileForm.fill(userProfileData);

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Try to update profile <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showProfile(userProfileForm, userProfilePictureForm, continueUrl);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result postServiceProfile(String continueUrl) {
        Form<UserProfileForm> userProfileForm = Form.form(UserProfileForm.class).bindFromRequest();

        if (formHasErrors(userProfileForm)) {
            Form<UserProfilePictureForm> userProfilePictureForm = Form.form(UserProfilePictureForm.class);
            Logger.error(userProfileForm.errors().toString());
            return showProfile(userProfileForm, userProfilePictureForm, continueUrl);
        }

        UserProfileForm userProfileData = userProfileForm.get();

        if (!"".equals(userProfileData.password)) {
            if (!userProfileData.password.equals(userProfileData.confirmPassword)) {
                Form<UserProfilePictureForm> userProfilePictureForm = Form.form(UserProfilePictureForm.class);
                userProfileForm.reject("profile.error.passwordsDidntMatch");
                return showProfile(userProfileForm, userProfilePictureForm, continueUrl);
            }

            userProfileService.updateProfile(IdentityUtils.getUserJid(), userProfileData.name, userProfileData.password);
        } else {
            userProfileService.updateProfile(IdentityUtils.getUserJid(), userProfileData.name);
        }

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Update profile.");

        if (continueUrl == null) {
            return redirect(routes.UserProfileController.profile());
        }

        return redirect(routes.UserProfileController.serviceProfile(continueUrl));
    }

    @BodyParser.Of(value = BodyParser.MultipartFormData.class, maxLength = (2 << 20))
    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Transactional
    public Result postServiceAvatar(String continueUrl) {
        // TODO catch 413 http response
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart avatar = body.getFile("avatar");

        if (avatar == null) {
            Form<UserProfileForm> userProfileForm = Form.form(UserProfileForm.class);
            Form<UserProfilePictureForm> userProfilePictureForm = Form.form(UserProfilePictureForm.class);
            userProfilePictureForm.reject("profile.error.not_picture");
            return showProfile(userProfileForm, userProfilePictureForm, continueUrl);
        }

        String contentType = avatar.getContentType();
        if (!(contentType.equals("image/png") || contentType.equals("image/jpg") || contentType.equals("image/jpeg"))) {
            Form<UserProfileForm> userProfileForm = Form.form(UserProfileForm.class);
            Form<UserProfilePictureForm> userProfilePictureForm = Form.form(UserProfilePictureForm.class);
            userProfilePictureForm.reject("error.profile.not_picture");
            return showProfile(userProfileForm, userProfilePictureForm, continueUrl);
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

            JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Update avatar.");

            if (continueUrl == null) {
                return redirect(routes.UserProfileController.profile());
            }

            return redirect(routes.UserProfileController.serviceProfile(continueUrl));
        } catch (IOException e) {
            Form<UserProfileForm> userProfileForm = Form.form(UserProfileForm.class);
            Form<UserProfilePictureForm> userProfilePictureForm = Form.form(UserProfilePictureForm.class);
            userProfilePictureForm.reject("profile.error.cantUpload");
            return showProfile(userProfileForm, userProfilePictureForm, continueUrl);
        }
    }

    private Result showProfile(Form<UserProfileForm> userProfileForm, Form<UserProfilePictureForm> userProfilePictureForm, String continueUrl) {
        LazyHtml content;
        if (continueUrl == null) {
            content = new LazyHtml(editProfileView.render(userProfileForm, userProfilePictureForm));
        } else {
            content = new LazyHtml(serviceEditProfileView.render(userProfileForm, userProfilePictureForm, continueUrl));
        }
        content.appendLayout(c -> headingLayout.render(Messages.get("profile.profile"), c));
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        if (continueUrl == null) {
            JophielControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("profile.profile"), routes.UserProfileController.profile())
            ));
        } else {
            JophielControllerUtils.getInstance().appendBreadcrumbsLayout(content, ImmutableList.of(
                    new InternalLink(Messages.get("profile.profile"), routes.UserProfileController.serviceProfile(continueUrl))
            ));
        }
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Profile");
        return JophielControllerUtils.getInstance().lazyOk(content);
    }
}
