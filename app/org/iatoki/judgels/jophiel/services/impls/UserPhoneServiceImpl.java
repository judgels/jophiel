package org.iatoki.judgels.jophiel.services.impls;

import org.iatoki.judgels.jophiel.UserPhone;
import org.iatoki.judgels.jophiel.UserPhoneNotFoundException;
import org.iatoki.judgels.jophiel.models.daos.UserDao;
import org.iatoki.judgels.jophiel.models.daos.UserPhoneDao;
import org.iatoki.judgels.jophiel.models.entities.UserModel;
import org.iatoki.judgels.jophiel.models.entities.UserPhoneModel;
import org.iatoki.judgels.jophiel.services.UserPhoneService;
import org.iatoki.judgels.play.IdentityUtils;

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

        return createFromModel(userPhoneModel);
    }

    @Override
    public UserPhone findPhoneByJid(String phoneJid) {
        return createFromModel(userPhoneDao.findByJid(phoneJid));
    }

    @Override
    public List<UserPhone> getPhonesByUserJid(String userJid) {
        List<UserPhoneModel> userPhoneModels = userPhoneDao.getByUserJid(userJid);

        return userPhoneModels.stream().map(m -> createFromModel(m)).collect(Collectors.toList());
    }

    @Override
    public void makePhonePrimary(String userJid, String phoneJid) {
        UserModel userModel = userDao.findByJid(userJid);
        userModel.phoneJid = phoneJid;

        userDao.edit(userModel, userJid, IdentityUtils.getIpAddress());
    }

    @Override
    public void addFirstPhone(String userJid, String phone) {
        UserPhoneModel userPhoneModel = persistPhone(userJid, phone);

        makePhonePrimary(userJid, userPhoneModel.jid);
    }

    @Override
    public void addPhone(String userJid, String phone) {
        persistPhone(userJid, phone);
    }

    @Override
    public void removePhone(String phoneJid) {
        UserPhoneModel userPhoneModel = userPhoneDao.findByJid(phoneJid);

        userPhoneDao.remove(userPhoneModel);
    }

    private UserPhone createFromModel(UserPhoneModel userPhoneModel) {
        return new UserPhone(userPhoneModel.id, userPhoneModel.jid, userPhoneModel.userJid, userPhoneModel.phoneNumber, userPhoneModel.phoneNumberVerified);
    }

    private UserPhoneModel persistPhone(String userJid, String phone) {
        UserPhoneModel userPhoneModel = new UserPhoneModel();
        userPhoneModel.phoneNumber = phone;
        userPhoneModel.userJid = userJid;
        userPhoneModel.phoneNumberVerified = false;

        userPhoneDao.persist(userPhoneModel, userJid, IdentityUtils.getIpAddress());

        return userPhoneModel;
    }
}
