package org.iatoki.judgels.jophiel.user.profile.phone;

import org.iatoki.judgels.jophiel.user.UserDao;
import org.iatoki.judgels.jophiel.user.UserModel;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public final class UserPhoneServiceImpl implements UserPhoneService {

    private final UserDao userDao;
    private final UserPhoneDao userPhoneDao;

    @Inject
    public UserPhoneServiceImpl(UserDao userDao, UserPhoneDao userPhoneDao) {
        this.userDao = userDao;
        this.userPhoneDao = userPhoneDao;
    }

    @Override
    public Optional<UserPhone> findPhoneById(long phoneId) {
        UserPhoneModel userPhoneModel = userPhoneDao.findById(phoneId);
        if (userPhoneModel == null) {
            return Optional.empty();
        }

        return Optional.of(UserPhoneServiceUtils.createUserPhoneFromModel(userPhoneModel));
    }

    @Override
    public Optional<UserPhone> findPhoneByJid(String phoneJid) {
        Optional<UserPhoneModel> userPhoneModel = Optional.ofNullable(userPhoneDao.findByJid(phoneJid));
        return userPhoneModel.map(UserPhoneServiceUtils::createUserPhoneFromModel);
    }

    @Override
    public List<UserPhone> getPhonesByUserJid(String userJid) {
        List<UserPhoneModel> userPhoneModels = userPhoneDao.getByUserJid(userJid);

        return userPhoneModels.stream().map(m -> UserPhoneServiceUtils.createUserPhoneFromModel(m)).collect(Collectors.toList());
    }

    @Override
    public void makePhonePrimary(String userJid, String phoneJid, String userIpAddress) {
        UserModel userModel = userDao.findByJid(userJid);
        userModel.phoneJid = phoneJid;

        userDao.edit(userModel, userJid, userIpAddress);
    }

    @Override
    public UserPhone addFirstPhone(String userJid, String phone, String userIpAddress) {
        UserPhoneModel userPhoneModel = UserPhoneServiceUtils.persistPhone(userPhoneDao, userJid, phone, userIpAddress);

        makePhonePrimary(userJid, userPhoneModel.jid, userIpAddress);

        return UserPhoneServiceUtils.createUserPhoneFromModel(userPhoneModel);
    }

    @Override
    public UserPhone addPhone(String userJid, String phone, String userIpAddress) {
        UserPhoneModel userPhoneModel = UserPhoneServiceUtils.persistPhone(userPhoneDao, userJid, phone, userIpAddress);

        return UserPhoneServiceUtils.createUserPhoneFromModel(userPhoneModel);
    }

    @Override
    public void removePhone(String phoneJid) {
        UserPhoneModel userPhoneModel = userPhoneDao.findByJid(phoneJid);

        userPhoneDao.remove(userPhoneModel);
    }
}
