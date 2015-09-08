package org.iatoki.judgels.jophiel.services.impls;

import org.iatoki.judgels.jophiel.UserPhone;
import org.iatoki.judgels.jophiel.models.daos.UserPhoneDao;
import org.iatoki.judgels.jophiel.models.entities.UserPhoneModel;

final class UserPhoneServiceUtils {

    private UserPhoneServiceUtils() {
        // prevent instantiation
    }

    static UserPhone createUserPhoneFromModel(UserPhoneModel userPhoneModel) {
        return new UserPhone(userPhoneModel.id, userPhoneModel.jid, userPhoneModel.userJid, userPhoneModel.phoneNumber, userPhoneModel.phoneNumberVerified);
    }

    static UserPhoneModel persistPhone(UserPhoneDao userPhoneDao, String userJid, String phone, String userIpAddress) {
        UserPhoneModel userPhoneModel = new UserPhoneModel();
        userPhoneModel.phoneNumber = phone;
        userPhoneModel.userJid = userJid;
        userPhoneModel.phoneNumberVerified = false;

        userPhoneDao.persist(userPhoneModel, userJid, userIpAddress);

        return userPhoneModel;
    }
}
