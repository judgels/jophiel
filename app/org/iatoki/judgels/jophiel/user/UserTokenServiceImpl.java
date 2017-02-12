package org.iatoki.judgels.jophiel.user;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.iatoki.judgels.jophiel.UserToken;

import java.util.Optional;
import java.util.UUID;

@Singleton
public class UserTokenServiceImpl implements UserTokenService {

    private static final long TOKEN_EXPIRE_TIME = 24 * 60 * 60 * 1000;

    private final UserTokenDao userTokenDao;

    @Inject
    public UserTokenServiceImpl(UserTokenDao userTokenDao) {
        this.userTokenDao = userTokenDao;
    }

    @Override
    public Optional<UserToken> getUserTokenByToken(String token) {
        Optional<UserTokenModel> tokenModel = userTokenDao.getByToken(token);

        return tokenModel.filter(x -> x.expireTime > System.currentTimeMillis())
                .map(x -> new UserToken(x.userJid, x.token));
    }

    @Override
    public UserToken createNewToken(String userJid, String userCreateJid, String userIpAddress) {
        Optional<UserTokenModel> tokenModel = userTokenDao.getByUserJid(userJid);
        tokenModel.ifPresent(userTokenDao::remove);

        UserTokenModel userTokenModel = new UserTokenModel();
        userTokenModel.userJid = userJid;
        userTokenModel.token = UUID.randomUUID().toString();
        userTokenModel.expireTime = System.currentTimeMillis() +TOKEN_EXPIRE_TIME;
        userTokenDao.persist(userTokenModel, userCreateJid, userIpAddress);

        return new UserToken(userTokenModel.userJid, userTokenModel.token);
    }
}
