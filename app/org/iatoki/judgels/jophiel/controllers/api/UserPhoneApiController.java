package org.iatoki.judgels.jophiel.controllers.api;

import com.google.inject.Inject;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.ApiErrorCodeV1;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.UserPhoneV1;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.user.User;
import org.iatoki.judgels.jophiel.user.UserService;
import org.iatoki.judgels.jophiel.user.profile.phone.UserPhone;
import org.iatoki.judgels.jophiel.user.profile.phone.UserPhoneCreateForm;
import org.iatoki.judgels.jophiel.user.profile.phone.UserPhoneService;
import org.iatoki.judgels.play.api.JudgelsAPIBadRequestException;
import org.iatoki.judgels.play.api.JudgelsAPINotFoundException;
import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;
import org.iatoki.judgels.play.controllers.apis.JudgelsAPIGuard;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Result;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@JudgelsAPIGuard
public final class UserPhoneApiController extends AbstractJudgelsAPIController {

    private final UserPhoneService userPhoneService;
    private final UserService userService;

    @Inject
    public UserPhoneApiController(UserPhoneService userPhoneService, UserService userService) {
        this.userPhoneService = userPhoneService;
        this.userService = userService;
    }

    @Transactional
    public Result getAllUserPhone(String userJid) {
        User user = userService.findUserByJid(userJid).orElseThrow(() -> new JudgelsAPINotFoundException(ApiErrorCodeV1.USER_NOT_FOUND));
        Optional<UserPhone> primaryPhone = user.getPhoneJid().flatMap(userPhoneService::findPhoneByJid);
        List<UserPhone> userPhones = userPhoneService.getPhonesByUserJid(userJid);

        List<UserPhoneV1> userPhonesV1 = userPhones.stream()
                .map(x -> createUserPhoneV1(x, primaryPhone.map(UserPhone::getPhone).orElse(null)))
                .collect(Collectors.toList());

        return okAsJson(userPhonesV1);
    }

    @Transactional
    public Result getUserPrimaryPhone(String userJid) {
        User user = userService.findUserByJid(userJid).orElseThrow(() -> new JudgelsAPINotFoundException(ApiErrorCodeV1.USER_NOT_FOUND));
        Optional<UserPhone> primaryPhone = user.getPhoneJid().flatMap(userPhoneService::findPhoneByJid);

        if (primaryPhone.isPresent()) {
            return okAsJson(primaryPhone);
        } else {
            return notFoundAsJson(ApiErrorCodeV1.PHONE_NOT_FOUND);
        }
    }

    @Authenticated
    public Result createPhone(String userJid) {
        User user = userService.findUserByJid(userJid).orElseThrow(() -> new JudgelsAPINotFoundException(ApiErrorCodeV1.USER_NOT_FOUND));

        Form<UserPhoneCreateForm> userPhoneCreateForm = Form.form(UserPhoneCreateForm.class).bindFromRequest();

        if (formHasErrors(userPhoneCreateForm)) {
            return badRequestAsJson(ApiErrorCodeV1.INVALID_INPUT_PARAMETER);
        }

        UserPhoneCreateForm userPhoneCreateData = userPhoneCreateForm.get();
        if (user.getPhoneJid() == null) {
            userPhoneService.addFirstPhone(userJid, userPhoneCreateData.phone, userJid);
        } else {
            userPhoneService.addPhone(userJid, userPhoneCreateData.phone, userJid);
        }

        return okJson();
    }

    @Transactional
    public Result makePhonePrimary(String userJid, String phoneJid) {
        User user = userService.findUserByJid(userJid).orElseThrow(() -> new JudgelsAPINotFoundException(ApiErrorCodeV1.USER_NOT_FOUND));
        UserPhone userPhone = userPhoneService.findPhoneByJid(phoneJid).orElseThrow(() -> new JudgelsAPINotFoundException(ApiErrorCodeV1.PHONE_NOT_FOUND));

        if (!user.getJid().equals(userPhone.getUserJid())) {
            throw new JudgelsAPIBadRequestException(ApiErrorCodeV1.PHONE_NOT_OWNED);
        }

        if (!userPhone.isPhoneVerified()) {
            throw new JudgelsAPIBadRequestException(ApiErrorCodeV1.PHONE_NOT_VERIFIED);
        }

        userPhoneService.makePhonePrimary(user.getJid(), userPhone.getJid(), userJid);

        return okJson();
    }

    @Transactional
    public Result deleteUserPhone(String userJid, String phoneJid) {
        User user = userService.findUserByJid(userJid).orElseThrow(() -> new JudgelsAPINotFoundException(ApiErrorCodeV1.USER_NOT_FOUND));
        UserPhone userPhone = userPhoneService.findPhoneByJid(phoneJid).orElseThrow(() -> new JudgelsAPINotFoundException(ApiErrorCodeV1.PHONE_NOT_FOUND));

        if (!user.getJid().equals(userPhone.getUserJid())) {
            throw new JudgelsAPIBadRequestException(ApiErrorCodeV1.PHONE_NOT_OWNED);
        }

        if (user.getEmailJid().equals(userPhone.getJid())) {
            throw new JudgelsAPIBadRequestException(ApiErrorCodeV1.PHONE_IS_PRIMARY);
        }

        userPhoneService.removePhone(userPhone.getJid());

        return okJson();
    }

    private UserPhoneV1 createUserPhoneV1(UserPhone userPhone, String primaryPhone) {
        UserPhoneV1 result = new UserPhoneV1();
        result.jid = userPhone.getJid();
        result.phone = userPhone.getPhone();
        result.verified = userPhone.isPhoneVerified();
        result.primary = userPhone.getPhone().equals(primaryPhone);

        return result;
    }
}
