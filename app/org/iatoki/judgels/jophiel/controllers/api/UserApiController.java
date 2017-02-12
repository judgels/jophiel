package org.iatoki.judgels.jophiel.controllers.api;

import org.iatoki.judgels.jophiel.client.ClientService;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.ApiErrorCodeV1;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.UserInfoV1;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.UserV1;
import org.iatoki.judgels.jophiel.profile.UserProfileSearchForm;
import org.iatoki.judgels.jophiel.user.User;
import org.iatoki.judgels.jophiel.user.UserService;
import org.iatoki.judgels.jophiel.user.profile.UserProfileEditApiForm;
import org.iatoki.judgels.jophiel.user.profile.info.UserInfo;
import org.iatoki.judgels.jophiel.user.profile.info.UserInfoEditForm;
import org.iatoki.judgels.jophiel.user.profile.phone.UserProfileService;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.api.JudgelsAPIBadRequestException;
import org.iatoki.judgels.play.api.JudgelsAPINotFoundException;
import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;
import org.iatoki.judgels.play.controllers.apis.JudgelsAPIGuard;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Date;
import java.util.Optional;

@JudgelsAPIGuard
public final class UserApiController extends AbstractJudgelsAPIController{
    private final UserProfileService userProfileService;
    private final UserService userService;
    private final ClientService clientService;

    @Inject
    public UserApiController(UserProfileService userProfileService, UserService userService, ClientService clientService) {
        this.userProfileService = userProfileService;
        this.userService = userService;
        this.clientService = clientService;
    }

    @Transactional
    public Result getUser(String userJid) {
        User user = userService.findUserByJid(userJid).orElseThrow(() -> new JudgelsAPINotFoundException(ApiErrorCodeV1.USER_NOT_FOUND));
        return okAsJson(createUserV1FromUser(user));
    }

    @Transactional
    public Result getUserInfo(String userJid) {
        if (userService.userExistsByJid(userJid)) throw new JudgelsAPINotFoundException(ApiErrorCodeV1.USER_NOT_FOUND);
        Optional<UserInfo> userInfo = userProfileService.findInfo(userJid);
        return okAsJson(userInfo.isPresent() ? createUserInfoV1(userInfo.get()) : new UserInfoV1());
    }

    @Transactional
    public Result editUserInfo(String userJid) {
        if (userService.userExistsByJid(userJid)) throw new JudgelsAPINotFoundException(ApiErrorCodeV1.USER_NOT_FOUND);
        Form<UserInfoEditForm> userInfoEditForm = Form.form(UserInfoEditForm.class).bindFromRequest();
        if (formHasErrors(userInfoEditForm)) {
            throw new JudgelsAPIBadRequestException(ApiErrorCodeV1.INVALID_INPUT_PARAMETER);
        }

        UserInfoEditForm userInfoEditData = userInfoEditForm.get();
        userProfileService.upsertInfo(userJid, userInfoEditData.gender, new Date(JudgelsPlayUtils.parseDate(userInfoEditData.birthDate)), userInfoEditData.streetAddress, userInfoEditData.postalCode, userInfoEditData.institution, userInfoEditData.city, userInfoEditData.provinceOrState, userInfoEditData.country, userInfoEditData.shirtSize, getCurrentUserIpAddress());

        return okJson();
    }

    @Transactional
    public Result editUserProfile(String userJid) {
        if (userService.userExistsByJid(userJid)) throw new JudgelsAPINotFoundException(ApiErrorCodeV1.USER_NOT_FOUND);
        Form<UserProfileEditApiForm> userProfileEditForm = Form.form(UserProfileEditApiForm.class).bindFromRequest();

        if (formHasErrors(userProfileEditForm)) {
            throw new JudgelsAPIBadRequestException(ApiErrorCodeV1.INVALID_INPUT_PARAMETER);
        }

        UserProfileEditApiForm userProfileEditData = userProfileEditForm.get();

        if (!userProfileEditData.password.isEmpty()) {
            userProfileService.updateProfile(userJid, userProfileEditData.name, userProfileEditData.showName, userProfileEditData.password, getCurrentUserIpAddress());
        } else {
            userProfileService.updateProfile(userJid, userProfileEditData.name, userProfileEditData.showName, getCurrentUserIpAddress());
        }

        return okJson();
    }

    @Transactional
    public Result searchUser(String username) {
        User user = userService.findUserByUsername(username).orElseThrow(() -> new JudgelsAPINotFoundException(ApiErrorCodeV1.USER_NOT_FOUND));

        return okAsJson(createUserV1FromUser(user));
    }

    @Transactional
    public Result searchUserInfo(String username) {
        User user = userService.findUserByUsername(username).orElseThrow(() -> new JudgelsAPINotFoundException(ApiErrorCodeV1.USER_NOT_FOUND));
        Optional<UserInfo> userInfo = userProfileService.findInfo(user.getJid());

        return okAsJson(userInfo.isPresent() ? createUserInfoV1(userInfo.get()) : new UserInfoV1());
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
