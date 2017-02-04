package org.iatoki.judgels.jophiel.controllers.api.client.v1;

import org.iatoki.judgels.jophiel.controllers.api.AbstractJophielAPIController;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.UserPhoneV1;
import org.iatoki.judgels.jophiel.user.User;
import org.iatoki.judgels.jophiel.user.UserService;
import org.iatoki.judgels.jophiel.user.profile.phone.UserPhone;
import org.iatoki.judgels.jophiel.user.profile.phone.UserPhoneService;
import play.db.jpa.Transactional;
import play.mvc.Result;

import java.util.List;
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
        UserPhone primaryPhone = userPhoneService.findPhoneByJid(user.getPhoneJid());
        List<UserPhone> userPhones = userPhoneService.getPhonesByUserJid(getCurrentUserJid());

        List<UserPhoneV1> userPhonesV1 = userPhones.stream()
                .map(x -> createUserPhoneV1(x, primaryPhone.getPhone()))
                .collect(Collectors.toList());

        return okAsJson(userPhonesV1);
    }

    @Transactional
    public Result getPrimaryPhone() {
        User user = userService.findUserByJid(getCurrentUserJid());
        UserPhone primaryPhone = userPhoneService.findPhoneByJid(user.getPhoneJid());

        return okAsJson(primaryPhone);
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
