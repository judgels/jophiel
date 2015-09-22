package org.iatoki.judgels.jophiel.services.impls;

import org.iatoki.judgels.jophiel.UserPhone;
import org.iatoki.judgels.jophiel.UserPhoneNotFoundException;
import org.iatoki.judgels.jophiel.models.daos.UserDao;
import org.iatoki.judgels.jophiel.models.daos.UserPhoneDao;
import org.iatoki.judgels.jophiel.models.entities.UserModel;
import org.iatoki.judgels.jophiel.models.entities.UserPhoneModel;
import org.iatoki.judgels.jophiel.services.UserPhoneService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Named("userPhoneService")
public final class UserPhoneServiceImpl implements UserPhoneService {

    private final UserDao userDao;
    private final UserPhoneDao userPhoneDao;

    @Inject
    public UserPhoneServiceImpl(UserDao userDao, UserPhoneDao userPhoneDao) {
        this.userDao = userDao;
        this.userPhoneDao = userPhoneDao;
    }

    @Override
    public UserPhone findPhoneById(long phoneId) throws UserPhoneNotFoundException {
        UserPhoneModel userPhoneModel = userPhoneDao.findById(phoneId);

        if (userPhoneModel == null) {
            throw new UserPhoneNotFoundException("User Phone Not Found.");
        }

        return UserPhoneServiceUtils.createUserPhoneFromModel(userPhoneModel);
    }

    @Override
    public UserPhone findPhoneByJid(String phoneJid) {
        return UserPhoneServiceUtils.createUserPhoneFromModel(userPhoneDao.findByJid(phoneJid));
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
