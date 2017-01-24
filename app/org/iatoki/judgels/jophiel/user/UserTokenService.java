package org.iatoki.judgels.jophiel.user;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.jophiel.UserToken;

import java.util.Optional;

@ImplementedBy(UserTokenServiceImpl.class)
public interface UserTokenService {

    Optional<UserToken> getUserTokenByToken(String token);

    UserToken createNewToken(String userJid, String userCreateJid, String userIpAddress);
}
