package org.iatoki.judgels.jophiel.controllers.api.client.v1;

import org.iatoki.judgels.jophiel.controllers.api.AbstractJophielAPIController;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.ApiErrorCodeV1;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.UserPhoneV1;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.user.User;
import org.iatoki.judgels.jophiel.user.UserService;
import org.iatoki.judgels.jophiel.user.profile.phone.UserPhone;
import org.iatoki.judgels.jophiel.user.profile.phone.UserPhoneCreateForm;
import org.iatoki.judgels.jophiel.user.profile.phone.UserPhoneService;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Result;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class ClientUserPhoneAPIControllerV1 extends AbstractJophielAPIController {

    private final UserPhoneService userPhoneService;
    private final UserService userService;

    public ClientUserPhoneAPIControllerV1(UserPhoneService userPhoneService, UserService userService) {
        this.userPhoneService = userPhoneService;
        this.userService = userService;
    }

    @Transactional
    public Result getAllUserPhone() {
        User user = userService.findUserByJid(getCurrentUserJid());
        Optional<UserPhone> primaryPhone = userPhoneService.findPhoneByJid(user.getPhoneJid());
        List<UserPhone> userPhones = userPhoneService.getPhonesByUserJid(getCurrentUserJid());

        List<UserPhoneV1> userPhonesV1 = userPhones.stream()
                .map(x -> createUserPhoneV1(x, primaryPhone.map(UserPhone::getPhone).orElse(null)))
                .collect(Collectors.toList());

        return okAsJson(userPhonesV1);
    }

    @Transactional
    public Result getPrimaryPhone() {
        User user = userService.findUserByJid(getCurrentUserJid());
        Optional<UserPhone> primaryPhone = userPhoneService.findPhoneByJid(user.getPhoneJid());

        if (primaryPhone.isPresent()) {
            return okAsJson(primaryPhone);
        } else {
            return notFoundAsJson(ApiErrorCodeV1.PHONE_NOT_FOUND);
        }
    }

    @Authenticated
    public Result createPhone() {
        User user = userService.findUserByJid(getCurrentUserJid());

        Form<UserPhoneCreateForm> userPhoneCreateForm = Form.form(UserPhoneCreateForm.class).bindFromRequest();

        if (formHasErrors(userPhoneCreateForm)) {
            return badRequestAsJson(ApiErrorCodeV1.INVALID_INPUT_PARAMETER);
        }

        UserPhoneCreateForm userPhoneCreateData = userPhoneCreateForm.get();
        if (user.getPhoneJid() == null) {
            userPhoneService.addFirstPhone(getCurrentUserJid(), userPhoneCreateData.phone, getCurrentUserJid());
        } else {
            userPhoneService.addPhone(getCurrentUserJid(), userPhoneCreateData.phone, getCurrentUserJid());
        }

        return okJson();
    }

    @Transactional
    public Result makePhonePrimary(String phoneJid) {
        User user = userService.findUserByJid(getCurrentUserJid());
        Optional<UserPhone> userPhoneOpt = userPhoneService.findPhoneByJid(phoneJid);
        if (!userPhoneOpt.isPresent()) {
            return notFoundAsJson(ApiErrorCodeV1.PHONE_NOT_FOUND);
        }
        UserPhone userPhone = userPhoneOpt.get();

        if (!user.getJid().equals(userPhone.getUserJid())) {
            return unauthorizeddAsJson(ApiErrorCodeV1.PHONE_NOT_OWNED);
        }

        if (!userPhone.isPhoneVerified()) {
            return badRequestAsJson(ApiErrorCodeV1.PHONE_NOT_VERIFIED);
        }

        userPhoneService.makePhonePrimary(user.getJid(), userPhone.getJid(), getCurrentUserJid());

        return okJson();
    }

    @Transactional
    public Result deletePhone(String phoneJid) {
        User user = userService.findUserByJid(getCurrentUserJid());
        Optional<UserPhone> userPhoneOpt = userPhoneService.findPhoneByJid(phoneJid);
        if (!userPhoneOpt.isPresent()) {
            return notFoundAsJson(ApiErrorCodeV1.PHONE_NOT_FOUND);
        }
        UserPhone userPhone = userPhoneOpt.get();

        if (!user.getJid().equals(userPhone.getUserJid())) {
            return unauthorizeddAsJson(ApiErrorCodeV1.PHONE_NOT_OWNED);
        }

        if (user.getEmailJid().equals(userPhone.getJid())) {
            return badRequestAsJson(ApiErrorCodeV1.PHONE_IS_PRIMARY);
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
