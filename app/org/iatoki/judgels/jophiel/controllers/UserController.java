package org.iatoki.judgels.jophiel.controllers;

import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.jophiel.BasicActivityKeys;
import org.iatoki.judgels.jophiel.JophielUtils;
import org.iatoki.judgels.jophiel.UnverifiedUserEmail;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserEmail;
import org.iatoki.judgels.jophiel.UserInfo;
import org.iatoki.judgels.jophiel.UserNotFoundException;
import org.iatoki.judgels.jophiel.UserPhone;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.Authorized;
import org.iatoki.judgels.jophiel.controllers.securities.HasRole;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.forms.UserCreateForm;
import org.iatoki.judgels.jophiel.forms.UserEditForm;
import org.iatoki.judgels.jophiel.services.UserActivityService;
import org.iatoki.judgels.jophiel.services.UserEmailService;
import org.iatoki.judgels.jophiel.services.UserPhoneService;
import org.iatoki.judgels.jophiel.services.UserProfileService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.jophiel.views.html.profile.viewFullProfileView;
import org.iatoki.judgels.jophiel.views.html.user.createUserView;
import org.iatoki.judgels.jophiel.views.html.user.listUnverifiedUsersView;
import org.iatoki.judgels.jophiel.views.html.user.listUsersView;
import org.iatoki.judgels.jophiel.views.html.user.editUserView;
import org.iatoki.judgels.play.HtmlTemplate;
import org.iatoki.judgels.play.Page;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

@Authenticated(value = {LoggedIn.class, HasRole.class})
@Authorized(value = "admin")
@Singleton
@Named
public final class UserController extends AbstractJophielController {

    private static final long PAGE_SIZE = 20;
    private static final String USER = "user";

    private final UserEmailService userEmailService;
    private final UserPhoneService userPhoneService;
    private final UserProfileService userProfileService;
    private final UserService userService;

    @Inject
    public UserController(UserActivityService userActivityService, UserEmailService userEmailService, UserPhoneService userPhoneService, UserProfileService userProfileService, UserService userService) {
        super(userActivityService);

        this.userEmailService = userEmailService;
        this.userPhoneService = userPhoneService;
        this.userProfileService = userProfileService;
        this.userService = userService;
    }

    @Transactional
    public Result index() {
        return listUsers(0, "id", "asc", "");
    }

    @Transactional
    public Result listUsers(long pageIndex, String orderBy, String orderDir, String filterString) {
        Page<User> pageOfUsers = userService.getPageOfUsers(pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

        return showListUsers(pageOfUsers, orderBy, orderDir, filterString);
    }

    @Transactional
    public Result viewUnverifiedUsers() {
        return listUnverifiedUsers(0, "id", "asc", "");
    }

    @Transactional
    public Result listUnverifiedUsers(long pageIndex, String orderBy, String orderDir, String filterString) {
        Page<UnverifiedUserEmail> pageOfUnverifiedUsersWithEmails = userService.getPageOfUnverifiedUsers(pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);

        return showListUnverifiedUsersWithEmails(pageOfUnverifiedUsersWithEmails, orderBy, orderDir, filterString);
    }

    @Transactional
    public Result viewUser(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);

        UserEmail userPrimaryEmail = null;
        if (user.getEmailJid() != null) {
            userPrimaryEmail = userEmailService.findEmailByJid(user.getEmailJid());
        }
        List<UserEmail> userEmails = userEmailService.getEmailsByUserJid(user.getJid());

        UserPhone userPrimaryPhone = null;
        if (user.getPhoneJid() != null) {
            userPrimaryPhone = userPhoneService.findPhoneByJid(user.getPhoneJid());
        }
        List<UserPhone> userPhones = userPhoneService.getPhonesByUserJid(user.getJid());

        UserInfo userInfo = null;
        if (userProfileService.infoExists(user.getJid())) {
            userInfo = userProfileService.getInfo(user.getJid());
        }

        return showViewUser(user, userPrimaryEmail, userEmails, userPrimaryPhone, userPhones, userInfo);
    }

    @Transactional
    @AddCSRFToken
    public Result createUser() {
        UserCreateForm userCreateData = new UserCreateForm();
        userCreateData.roles = StringUtils.join(JophielUtils.getDefaultRoles(), ",");
        Form<UserCreateForm> userCreateForm = Form.form(UserCreateForm.class).fill(userCreateData);

        return showCreateUser(userCreateForm);
    }

    @Transactional
    public Result postCreateUser() {
        Form<UserCreateForm> userCreateForm = Form.form(UserCreateForm.class).bindFromRequest();
        if (formHasErrors(userCreateForm)) {
            return showCreateUser(userCreateForm);
        }

        UserCreateForm userCreateData = userCreateForm.get();
        User user = userService.createUser(userCreateData.username, userCreateData.name, userCreateData.email, userCreateData.password, Arrays.asList(userCreateData.roles.split(",")), getCurrentUserJid(), getCurrentUserIpAddress());

        addActivityLog(BasicActivityKeys.CREATE.construct(USER, user.getJid(), user.getUsername()));

        return redirect(routes.UserController.index());
    }

    @Transactional
    @AddCSRFToken
    public Result editUser(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);
        UserEditForm userEditData = new UserEditForm();
        userEditData.username = user.getUsername();
        userEditData.name = user.getName();
        userEditData.roles = StringUtils.join(user.getRoles(), ",");
        Form<UserEditForm> userEditForm = Form.form(UserEditForm.class).fill(userEditData);

        return showEditUser(user, userEditForm);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postEditUser(long userId) throws UserNotFoundException {
        User user = userService.findUserById(userId);
        Form<UserEditForm> userEditForm = Form.form(UserEditForm.class).bindFromRequest();

        if (formHasErrors(userEditForm)) {
            return showEditUser(user, userEditForm);
        }

        UserEditForm userEditData = userEditForm.get();
        if (!userEditData.password.isEmpty()) {
            userService.updateUser(user.getJid(), userEditData.username, userEditData.name, userEditData.password, Arrays.asList(userEditData.roles.split(",")), getCurrentUserJid(), getCurrentUserIpAddress());
        } else {
            userService.updateUser(user.getJid(), userEditData.username, userEditData.name, Arrays.asList(userEditData.roles.split(",")), getCurrentUserJid(), getCurrentUserIpAddress());
        }

        addActivityLog(BasicActivityKeys.EDIT.construct(USER, user.getJid(), user.getUsername()));

        return redirect(routes.UserController.index());
    }

    @Override
    protected Result renderTemplate(HtmlTemplate template) {
        template.markBreadcrumbLocation(Messages.get("user.text.users"), routes.UserController.index());

        template.addCategoryTab(Messages.get("user.text.all"), routes.UserController.index());
        template.addCategoryTab(Messages.get("user.text.unverified"), routes.UserController.viewUnverifiedUsers());

        return super.renderTemplate(template);
    }

    protected Result renderTemplate(HtmlTemplate template, User user) {
        template.setMainTitle("#" + user.getId() + ": " + user.getUsername() + " (" + user.getJid() + ")");

        template.markBreadcrumbLocation(user.getUsername(), routes.UserController.viewUser(user.getId()));

        return renderTemplate(template);
    }

    private Result showListUsers(Page<User> pageOfUsers, String orderBy, String orderDir, String filterString) {
        HtmlTemplate template = new HtmlTemplate();

        template.setContent(listUsersView.render(pageOfUsers, orderBy, orderDir, filterString));
        template.setMainTitle(Messages.get("user.text.list"));
        template.addMainButton(Messages.get("user.text.new"), routes.UserController.createUser());

        return renderTemplate(template);
    }

    private Result showListUnverifiedUsersWithEmails(Page<UnverifiedUserEmail> pageOfUnverifiedUsersWithEmails, String orderBy, String orderDir, String filterString) {
        HtmlTemplate template = new HtmlTemplate();

        template.setContent(listUnverifiedUsersView.render(pageOfUnverifiedUsersWithEmails, orderBy, orderDir, filterString));
        template.setMainTitle(Messages.get("user.text.listUnverified"));
        template.markBreadcrumbLocation(Messages.get("user.text.unverified"), routes.UserController.viewUnverifiedUsers());

        return renderTemplate(template);
    }

    private Result showCreateUser(Form<UserCreateForm> userCreateForm) {
        HtmlTemplate template = new HtmlTemplate();

        template.setContent(createUserView.render(userCreateForm));
        template.setMainTitle(Messages.get("user.text.new"));
        template.markBreadcrumbLocation(Messages.get("commons.text.new"), routes.UserController.createUser());

        return renderTemplate(template);
    }

    private Result showViewUser(User user, UserEmail userPrimaryEmail, List<UserEmail> userEmails, UserPhone userPrimaryPhone, List<UserPhone> userPhones, UserInfo userInfo) {
        HtmlTemplate template = new HtmlTemplate();

        template.setContent(viewFullProfileView.render(user, userPrimaryEmail, userEmails, userPrimaryPhone, userPhones, userInfo));
        template.setPageTitle(user.getUsername());

        return renderTemplate(template, user);
    }

    private Result showEditUser(User user, Form<UserEditForm> userEditForm) {
        HtmlTemplate template = new HtmlTemplate();

        template.setContent(editUserView.render(user, userEditForm));
        template.markBreadcrumbLocation(Messages.get("commons.text.edit"), routes.UserController.editUser(user.getId()));
        template.setPageTitle(user.getUsername());

        return renderTemplate(template, user);
    }
}
