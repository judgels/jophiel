package org.iatoki.judgels.jophiel.controllers;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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
import org.iatoki.judgels.jophiel.forms.UserDownloadForm;
import org.iatoki.judgels.jophiel.forms.UserEditForm;
import org.iatoki.judgels.jophiel.services.UserActivityService;
import org.iatoki.judgels.jophiel.services.UserEmailService;
import org.iatoki.judgels.jophiel.services.UserPhoneService;
import org.iatoki.judgels.jophiel.services.UserProfileService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.jophiel.views.html.profile.viewFullProfileView;
import org.iatoki.judgels.jophiel.views.html.user.createUserView;
import org.iatoki.judgels.jophiel.views.html.user.editUserView;
import org.iatoki.judgels.jophiel.views.html.user.listUnverifiedUsersView;
import org.iatoki.judgels.jophiel.views.html.user.listUsersView;
import org.iatoki.judgels.play.HtmlTemplate;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.Page;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @AddCSRFToken
    public Result index() {
        return listUsers(0, "id", "asc", "");
    }

    @Transactional
    @AddCSRFToken
    public Result listUsers(long pageIndex, String orderBy, String orderDir, String filterString) {
        Page<User> pageOfUsers = userService.getPageOfUsers(pageIndex, PAGE_SIZE, orderBy, orderDir, filterString);
        Form<UserDownloadForm> userDownloadForm = Form.form(UserDownloadForm.class);

        return showListUsers(pageOfUsers, userDownloadForm, orderBy, orderDir, filterString);
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
            userInfo = userProfileService.findInfo(user.getJid());
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

    @Transactional
    @RequireCSRFCheck
    public Result postDownloadUsers() {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart file;

        file = body.getFile("users");
        if (file != null) {
            File userFile = file.getFile();
            try {
                String[] usernames = FileUtils.readFileToString(userFile).split("\n");
                List<User> users = userService.getUsersByUsernames(Arrays.asList(usernames));
                Map<String, UserInfo> mapJidToUserInfo = userProfileService.getUsersInfoByUserJids(users.stream().map(User::getJid).collect(Collectors.toList())).stream().collect(Collectors.toMap(i -> i.getUserJid(), i -> i));

                Workbook workbook = generateUserData(users, mapJidToUserInfo);

                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    workbook.write(baos);
                    baos.close();
                    response().setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                    response().setHeader("Content-Disposition", "attachment; filename=\"" + Messages.get("user.text.users") + ".xls\"");
                    return ok(baos.toByteArray());
                } catch (IOException e) {
                    return internalServerError();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

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

    private Result showListUsers(Page<User> pageOfUsers, Form<UserDownloadForm> userDownloadForm, String orderBy, String orderDir, String filterString) {
        HtmlTemplate template = super.getBaseHtmlTemplate();

        template.setContent(listUsersView.render(pageOfUsers, userDownloadForm, orderBy, orderDir, filterString));
        template.setMainTitle(Messages.get("user.text.list"));
        template.addMainButton(Messages.get("user.text.new"), routes.UserController.createUser());

        return renderTemplate(template);
    }

    private Result showListUnverifiedUsersWithEmails(Page<UnverifiedUserEmail> pageOfUnverifiedUsersWithEmails, String orderBy, String orderDir, String filterString) {
        HtmlTemplate template = super.getBaseHtmlTemplate();

        template.setContent(listUnverifiedUsersView.render(pageOfUnverifiedUsersWithEmails, orderBy, orderDir, filterString));
        template.setMainTitle(Messages.get("user.text.listUnverified"));
        template.markBreadcrumbLocation(Messages.get("user.text.unverified"), routes.UserController.viewUnverifiedUsers());

        return renderTemplate(template);
    }

    private Result showCreateUser(Form<UserCreateForm> userCreateForm) {
        HtmlTemplate template = super.getBaseHtmlTemplate();

        template.setContent(createUserView.render(userCreateForm));
        template.setMainTitle(Messages.get("user.text.new"));
        template.markBreadcrumbLocation(Messages.get("commons.text.new"), routes.UserController.createUser());

        return renderTemplate(template);
    }

    private Result showViewUser(User user, UserEmail userPrimaryEmail, List<UserEmail> userEmails, UserPhone userPrimaryPhone, List<UserPhone> userPhones, UserInfo userInfo) {
        HtmlTemplate template = super.getBaseHtmlTemplate();

        template.setContent(viewFullProfileView.render(user, userPrimaryEmail, userEmails, userPrimaryPhone, userPhones, userInfo));
        template.setPageTitle(user.getUsername());

        return renderTemplate(template, user);
    }

    private Result showEditUser(User user, Form<UserEditForm> userEditForm) {
        HtmlTemplate template = super.getBaseHtmlTemplate();

        template.setContent(editUserView.render(user, userEditForm));
        template.markBreadcrumbLocation(Messages.get("commons.text.edit"), routes.UserController.editUser(user.getId()));
        template.setPageTitle(user.getUsername());

        return renderTemplate(template, user);
    }

    private Workbook generateUserData(List<User> users, Map<String, UserInfo> userInfoMap) {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet(Messages.get("user.text.users"));

        int rowNum = 0;
        int cellNum = 0;
        Row row = sheet.createRow(rowNum++);
        Cell cell = row.createCell(cellNum++);
        cell.setCellValue(Messages.get("user.field.username"));
        cell = row.createCell(cellNum++);
        cell.setCellValue(Messages.get("user.field.name"));
        cell = row.createCell(cellNum++);
        cell.setCellValue(Messages.get("info.field.gender"));
        cell = row.createCell(cellNum++);
        cell.setCellValue(Messages.get("info.field.birthDate"));
        cell = row.createCell(cellNum++);
        cell.setCellValue(Messages.get("info.field.streetAddress"));
        cell = row.createCell(cellNum++);
        cell.setCellValue(Messages.get("info.field.postalCode"));
        cell = row.createCell(cellNum++);
        cell.setCellValue(Messages.get("info.field.institution"));
        cell = row.createCell(cellNum++);
        cell.setCellValue(Messages.get("info.field.city"));
        cell = row.createCell(cellNum++);
        cell.setCellValue(Messages.get("info.field.provinceOrState"));
        cell = row.createCell(cellNum++);
        cell.setCellValue(Messages.get("info.field.country"));
        cell = row.createCell(cellNum++);
        cell.setCellValue(Messages.get("info.field.shirtSize"));
        for (User user : users) {
            cellNum = 0;
            row = sheet.createRow(rowNum++);
            cell = row.createCell(cellNum++);
            cell.setCellValue(user.getUsername());
            cell = row.createCell(cellNum++);
            cell.setCellValue(user.getName());
            if (userInfoMap.containsKey(user.getJid())) {
                UserInfo userInfo = userInfoMap.get(user.getJid());
                cell = row.createCell(cellNum++);
                cell.setCellValue(userInfo.getGender());
                cell = row.createCell(cellNum++);
                cell.setCellValue(JudgelsPlayUtils.formatDate(userInfo.getBirthDate()));
                cell = row.createCell(cellNum++);
                cell.setCellValue(userInfo.getStreetAddress());
                cell = row.createCell(cellNum++);
                cell.setCellValue(userInfo.getPostalCode());
                cell = row.createCell(cellNum++);
                cell.setCellValue(userInfo.getInstitution());
                cell = row.createCell(cellNum++);
                cell.setCellValue(userInfo.getCity());
                cell = row.createCell(cellNum++);
                cell.setCellValue(userInfo.getProvinceOrState());
                cell = row.createCell(cellNum++);
                cell.setCellValue(userInfo.getCountry());
                cell = row.createCell(cellNum++);
                cell.setCellValue(userInfo.getShirtSize());
            }
        }

        return workbook;
    }
}
