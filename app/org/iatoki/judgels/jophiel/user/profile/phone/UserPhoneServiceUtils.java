package org.iatoki.judgels.jophiel.user.profile.phone;

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
