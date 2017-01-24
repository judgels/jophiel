package org.iatoki.judgels.jophiel.user;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.iatoki.judgels.jophiel.UserToken;

import java.util.Optional;
import java.util.UUID;

@Singleton
public class UserTokenServiceImpl implements UserTokenService {

    private final UserTokenDao userTokenDao;

    @Inject
    public UserTokenServiceImpl(UserTokenDao userTokenDao) {
        this.userTokenDao = userTokenDao;
    }

    @Override
    public Optional<UserToken> getUserTokenByToken(String token) {
        Optional<UserTokenModel> tokenModel = userTokenDao.getByToken(token);

        return tokenModel.map(x -> new UserToken(x.userJid, x.token));
    }

    @Override
    public UserToken createNewToken(String userJid, String userCreateJid, String userIpAddress) {
        Optional<UserTokenModel> tokenModel = userTokenDao.getByUserJid(userJid);
        tokenModel.ifPresent(userTokenDao::remove);

        UserTokenModel userTokenModel = new UserTokenModel();
        userTokenModel.userJid = userJid;
        userTokenModel.token = UUID.randomUUID().toString();
        userTokenDao.persist(userTokenModel, userCreateJid, userIpAddress);

        return new UserToken(userTokenModel.userJid, userTokenModel.token);
    }
}
