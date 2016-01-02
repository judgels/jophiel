package org.iatoki.judgels.jophiel.controllers.api;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.iatoki.judgels.jophiel.oauth2.AccessToken;
import org.iatoki.judgels.jophiel.user.User;
import org.iatoki.judgels.jophiel.api.JophielUserAPIIdentity;
import org.iatoki.judgels.jophiel.client.ClientService;
import org.iatoki.judgels.jophiel.user.UserService;
import org.iatoki.judgels.play.apis.JudgelsAPIUnauthorizedException;
import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;

public abstract class AbstractJophielAPIController extends AbstractJudgelsAPIController {

    protected static JophielUserAPIIdentity authenticateAsJophielUser(ClientService clientService, UserService userService) {
        if (!request().hasHeader("Authorization")) {
            throw new JudgelsAPIUnauthorizedException("Basic/OAuth2 authentication required.");
        }

        String[] authorization = request().getHeader("Authorization").split(" ");

        if (authorization.length != 2) {
            throw new JudgelsAPIUnauthorizedException("Basic/OAuth2 authentication required.");
        }

        String method = authorization[0];
        String credentialsString = authorization[1];

        if ("Basic".equals(method)) {
            String decodedCredentialsString = new String(Base64.decodeBase64(credentialsString));
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(decodedCredentialsString);

            String username = credentials.getUserName();
            String password = credentials.getPassword();

            if (!userService.userExistsByUsernameAndPassword(username, password)) {
                throw new JudgelsAPIUnauthorizedException("Bad credentials.");
            }

            User user = userService.findUserByUsername(username);
            return new JophielUserAPIIdentity(user.getJid(), user.getUsername());
        } else if ("Bearer".equals(method)) {
            String accessToken = new String(Base64.decodeBase64(credentialsString));

            AccessToken token = clientService.getAccessTokenByAccessTokenString(accessToken);
            User user = userService.findUserByJid(token.getUserJid());
            return new JophielUserAPIIdentity(user.getJid(), user.getUsername());
        } else {
            throw new JudgelsAPIUnauthorizedException("Basic/OAuth2 authentication required.");
        }
    }

    protected static String getCurrentUserJid() {
        return session("userJid");
    }
}
