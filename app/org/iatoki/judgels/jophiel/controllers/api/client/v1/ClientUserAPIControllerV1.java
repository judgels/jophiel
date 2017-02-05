package org.iatoki.judgels.jophiel.controllers.api.client.v1;

import org.iatoki.judgels.jophiel.controllers.api.object.v1.ApiErrorCodeV1;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.UserInfoV1;
import org.iatoki.judgels.jophiel.oauth2.AccessToken;
import org.iatoki.judgels.jophiel.user.User;
import org.iatoki.judgels.jophiel.controllers.api.AbstractJophielAPIController;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.UserFindByUsernameAndPasswordRequestV1;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.UserV1;
import org.iatoki.judgels.jophiel.client.ClientService;
import org.iatoki.judgels.jophiel.user.UserService;
import org.iatoki.judgels.jophiel.user.profile.UserProfileEditApiForm;
import org.iatoki.judgels.jophiel.user.profile.email.UnverifiedUserEmail;
import org.iatoki.judgels.jophiel.user.profile.info.UserInfo;
import org.iatoki.judgels.jophiel.user.profile.info.UserInfoEditForm;
import org.iatoki.judgels.jophiel.user.profile.phone.UserProfileService;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.api.JudgelsAPINotFoundException;
import org.iatoki.judgels.play.api.JudgelsAppClientAPIIdentity;
import play.data.DynamicForm;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Date;
import java.util.Optional;

public final class ClientUserAPIControllerV1 extends AbstractJophielAPIController {

    private final UserProfileService userProfileService;
    private final UserService userService;
    private final ClientService clientService;

    @Inject
    public ClientUserAPIControllerV1(UserProfileService userProfileService, UserService userService, ClientService clientService) {
        this.userProfileService = userProfileService;
        this.userService = userService;
        this.clientService = clientService;
    }

    public Result isLoggedIn() {
        DynamicForm dForm = DynamicForm.form().bindFromRequest();

        boolean isLoggedIn = (getCurrentUserJid() != null);
        if (isLoggedIn && dForm.data().containsKey("userJid")) {
            isLoggedIn = getCurrentUserJid().equals(dForm.get("userJid"));
        }

        return okAsJson(isLoggedIn);
    }

    public Result findUserByUsernameAndPassword() {
        authenticateAsJudgelsAppClient(clientService);
        UserFindByUsernameAndPasswordRequestV1 requestBody = parseRequestBody(UserFindByUsernameAndPasswordRequestV1.class);

        if (!userService.userExistsByUsernameAndPassword(requestBody.username, requestBody.password)) {
            throw new JudgelsAPINotFoundException();
        }

        User user = userService.findUserByJid(requestBody.username);
        return okAsJson(createUserV1FromUser(user));
    }

    public Result findUserByAccessToken(String accessToken) {
        JudgelsAppClientAPIIdentity identity = authenticateAsJudgelsAppClient(clientService);

        if (!clientService.isAccessTokenValid(accessToken, System.currentTimeMillis())) {
            throw new JudgelsAPINotFoundException();
        }

        AccessToken accessTokenObj = clientService.getAccessTokenByAccessTokenString(accessToken);

        if (!accessTokenObj.getClientJid().equals(identity.getClientJid())) {
            throw new JudgelsAPINotFoundException();
        }

        User user = userService.findUserByJid(accessTokenObj.getUserJid());
        return okAsJson(createUserV1FromUser(user));
    }

    @Transactional
    public Result getUserList(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        Page<User> pageOfUsers = userService.getPageOfUsers(pageIndex, pageSize, orderBy, orderDir, filterString);
        return okAsJson(pageOfUsers);
    }

    @Transactional
    public Result getUnverifiedUserList(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        Page<UnverifiedUserEmail> pageOfUnverifiedUsersWithEmails = userService.getPageOfUnverifiedUsers(pageIndex, pageSize, orderBy, orderDir, filterString);
        return okAsJson(pageOfUnverifiedUsersWithEmails);
    }

    @Transactional
    public Result getUserInfo() {
        Optional<UserInfo> userInfo = userProfileService.findInfo(getCurrentUserJid());
        if (userInfo.isPresent()) {
            return okAsJson(createUserInfoV1(userInfo.get()));
        } else {
            return okAsJson(new UserInfoV1());
        }
    }

    @Transactional
    public Result editUserInfo() {
        Form<UserInfoEditForm> userInfoEditForm = Form.form(UserInfoEditForm.class).bindFromRequest();

        if (formHasErrors(userInfoEditForm)) {
            return badRequestAsJson(ApiErrorCodeV1.INVALID_INPUT_PARAMETER);
        }

        UserInfoEditForm userInfoEditData = userInfoEditForm.get();
        userProfileService.upsertInfo(getCurrentUserJid(), userInfoEditData.gender, new Date(JudgelsPlayUtils.parseDate(userInfoEditData.birthDate)), userInfoEditData.streetAddress, userInfoEditData.postalCode, userInfoEditData.institution, userInfoEditData.city, userInfoEditData.provinceOrState, userInfoEditData.country, userInfoEditData.shirtSize, getCurrentUserIpAddress());

        return okJson();
    }

    @Transactional
    public Result editUserProfile() {
        Form<UserProfileEditApiForm> userProfileEditForm = Form.form(UserProfileEditApiForm.class).bindFromRequest();

        if (formHasErrors(userProfileEditForm)) {
            return badRequestAsJson(ApiErrorCodeV1.INVALID_INPUT_PARAMETER);
        }

        UserProfileEditApiForm userProfileEditData = userProfileEditForm.get();

        if (!userProfileEditData.password.isEmpty()) {
            userProfileService.updateProfile(getCurrentUserJid(), userProfileEditData.name, userProfileEditData.showName, userProfileEditData.password, getCurrentUserIpAddress());
        } else {
            userProfileService.updateProfile(getCurrentUserJid(), userProfileEditData.name, userProfileEditData.showName, getCurrentUserIpAddress());
        }

        return okJson();
    }

    private UserV1 createUserV1FromUser(User user) {
        UserV1 responseBody = new UserV1();
        responseBody.jid = user.getJid();
        responseBody.username = user.getUsername();
        if (user.isShowName()) {
            responseBody.name = user.getName();
        }
        responseBody.profilePictureUrl = user.getProfilePictureUrl().toString();
        return responseBody;
    }

    private UserInfoV1 createUserInfoV1(UserInfo userInfo) {
        UserInfoV1 result = new UserInfoV1();
        result.id = userInfo.getId();
        result.jid = userInfo.getUserJid();
        result.gender = userInfo.getGender();
        result.birthDate = userInfo.getBirthDate();
        result.streetAddress = userInfo.getStreetAddress();
        result.postalCode = userInfo.getPostalCode();
        result.institution = userInfo.getInstitution();
        result.city = userInfo.getCity();
        result.provinceOrState = userInfo.getProvinceOrState();
        result.country = userInfo.getCountry();
        result.shirtSize = userInfo.getShirtSize();

        return result;
    }
}
