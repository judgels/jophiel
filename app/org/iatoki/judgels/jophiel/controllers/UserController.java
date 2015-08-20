package org.iatoki.judgels.jophiel.controllers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.jophiel.JophielUtils;
import org.iatoki.judgels.jophiel.UserInfo;
import org.iatoki.judgels.jophiel.UserNotFoundException;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.Authorized;
import org.iatoki.judgels.jophiel.controllers.securities.HasRole;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.forms.UserCreateForm;
import org.iatoki.judgels.jophiel.forms.UserUpdateForm;
import org.iatoki.judgels.jophiel.services.UserActivityService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.jophiel.views.html.user.createUserView;
import org.iatoki.judgels.jophiel.views.html.user.listUnverifiedUsersView;
import org.iatoki.judgels.jophiel.views.html.user.listUsersView;
import org.iatoki.judgels.jophiel.views.html.user.updateUserView;
import org.iatoki.judgels.jophiel.views.html.user.viewUserView;
import org.iatoki.judgels.play.InternalLink;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.controllers.AbstractJudgelsController;
import org.iatoki.judgels.play.views.html.layouts.headingLayout;
import org.iatoki.judgels.play.views.html.layouts.headingWithActionLayout;
import org.iatoki.judgels.play.views.html.layouts.tabLayout;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Arrays;

@Singleton
@Named
public final class UserController extends AbstractJudgelsController {

    private static final long PAGE_SIZE = 20;

    private final UserActivityService userActivityService;
    private final UserService userService;

    @Inject
    public UserController(UserActivityService userActivityService, UserService userService) {
        this.userActivityService = userActivityService;
        this.userService = userService;
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional
    public Result index() {
        return listUsers(0, "id", "asc", "");
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional
    public Result listUsers(long pageIndex, String orderBy, String orderDir, String filterString) {
        Page<UserInfo> pageOfUsersInfo = userService.getPageOfUsersInfo(pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

        LazyHtml content = new LazyHtml(listUsersView.render(pageOfUsersInfo, orderBy, orderDir, filterString));
        appendTabLayout(content);
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("user.list"), new InternalLink(Messages.get("commons.create"), routes.UserController.createUser()), c));
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content);
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Users");

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Open all users <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional
    public Result viewUnverifiedUsers() {
        return listUnverifiedUsers(0, "id", "asc", "");
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional
    public Result listUnverifiedUsers(long pageIndex, String orderBy, String orderDir, String filterString) {
        Page<UserInfo> pageOfUsersInfo = userService.getPageOfUnverifiedUsersInfo(pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

        LazyHtml content = new LazyHtml(listUnverifiedUsersView.render(pageOfUsersInfo, orderBy, orderDir, filterString));
        appendTabLayout(content);
        content.appendLayout(c -> headingLayout.render(Messages.get("user.unverifiedUsers.list"), c));
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("user.unverifiedUsers"), routes.UserController.viewUnverifiedUsers())
        );
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Users");

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Open unverified users <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional
    public Result viewUser(long userId) throws UserNotFoundException {
        UserInfo user = userService.findUserInfoById(userId);

        LazyHtml content = new LazyHtml(viewUserView.render(user));
        content.appendLayout(c -> headingWithActionLayout.render(Messages.get("user.user") + " #" + userId + ": " + user.getName(), new InternalLink(Messages.get("commons.update"), routes.UserController.updateUser(userId)), c));
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("user.view"), routes.UserController.viewUser(userId))
        );
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "User - View");

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "View user " + user.getUsername() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional
    @AddCSRFToken
    public Result createUser() {
        UserCreateForm userCreateData = new UserCreateForm();
        userCreateData.roles = StringUtils.join(JophielUtils.getDefaultRoles(), ",");
        Form<UserCreateForm> userCreateForm = Form.form(UserCreateForm.class).fill(userCreateData);

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Try to create user <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showCreateUser(userCreateForm);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional
    public Result postCreateUser() {
        Form<UserCreateForm> userCreateForm = Form.form(UserCreateForm.class).bindFromRequest();
        if (formHasErrors(userCreateForm)) {
            return showCreateUser(userCreateForm);
        }

        UserCreateForm userCreateData = userCreateForm.get();
        userService.createUser(userCreateData.username, userCreateData.name, userCreateData.email, userCreateData.password, Arrays.asList(userCreateData.roles.split(",")));

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Create user " + userCreateData.username + ".");

        return redirect(routes.UserController.index());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional
    @AddCSRFToken
    public Result updateUser(long userId) throws UserNotFoundException {
        UserInfo user = userService.findUserInfoById(userId);
        UserUpdateForm userUpdateData = new UserUpdateForm();
        userUpdateData.username = user.getUsername();
        userUpdateData.name = user.getName();
        userUpdateData.email = user.getEmail();
        userUpdateData.roles = StringUtils.join(user.getRoles(), ",");
        Form<UserUpdateForm> userUpdateForm = Form.form(UserUpdateForm.class).fill(userUpdateData);

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Try to update user " + user.getUsername() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return showUpdateUser(userUpdateForm, user);
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional
    @RequireCSRFCheck
    public Result postUpdateUser(long userId) throws UserNotFoundException {
        UserInfo user = userService.findUserInfoById(userId);
        Form<UserUpdateForm> userUpdateForm = Form.form(UserUpdateForm.class).bindFromRequest();

        if (formHasErrors(userUpdateForm)) {
            return showUpdateUser(userUpdateForm, user);
        }

        UserUpdateForm userUpdateData = userUpdateForm.get();
        if (!"".equals(userUpdateData.password)) {
            userService.updateUser(user.getId(), userUpdateData.username, userUpdateData.name, userUpdateData.email, userUpdateData.password, Arrays.asList(userUpdateData.roles.split(",")));
        } else {
            userService.updateUser(user.getId(), userUpdateData.username, userUpdateData.name, userUpdateData.email, Arrays.asList(userUpdateData.roles.split(",")));
        }

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Update user " + user.getUsername() + ".");

        return redirect(routes.UserController.index());
    }

    @Authenticated(value = {LoggedIn.class, HasRole.class})
    @Authorized(value = "admin")
    @Transactional
    public Result deleteUser(long userId) throws UserNotFoundException {
        UserInfo user = userService.findUserInfoById(userId);
        userService.deleteUser(user.getId());

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Delete user " + user.getUsername() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

        return redirect(routes.UserController.index());
    }

    private Result showCreateUser(Form<UserCreateForm> form) {
        LazyHtml content = new LazyHtml(createUserView.render(form));
        content.appendLayout(c -> headingLayout.render(Messages.get("user.create"), c));
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("user.create"), routes.UserController.createUser())
        );
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "Change Password");
        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    private Result showUpdateUser(Form<UserUpdateForm> form, UserInfo user) {
        LazyHtml content = new LazyHtml(updateUserView.render(form, user.getId()));
        content.appendLayout(c -> headingLayout.render(Messages.get("user.user") + " #" + user.getId() + ": " + user.getUsername(), c));
        JophielControllerUtils.getInstance().appendSidebarLayout(content);
        appendBreadcrumbsLayout(content,
                new InternalLink(Messages.get("user.update"), routes.UserController.updateUser(user.getId()))
        );
        JophielControllerUtils.getInstance().appendTemplateLayout(content, "User - Update");
        return JophielControllerUtils.getInstance().lazyOk(content);
    }

    private void appendTabLayout(LazyHtml content) {
        content.appendLayout(c -> tabLayout.render(ImmutableList.of(
                new InternalLink(Messages.get("user.users"), routes.UserController.index()),
                new InternalLink(Messages.get("user.unverifiedUsers"), routes.UserController.viewUnverifiedUsers())
        ), c));
    }

    private void appendBreadcrumbsLayout(LazyHtml content, InternalLink... lastLinks) {
        ImmutableList.Builder<InternalLink> breadcrumbsBuilder = ImmutableList.builder();
        breadcrumbsBuilder.add(new InternalLink(Messages.get("user.users"), routes.UserController.index()));
        breadcrumbsBuilder.add(lastLinks);

        JophielControllerUtils.getInstance().appendBreadcrumbsLayout(content, breadcrumbsBuilder.build());
    }
}
