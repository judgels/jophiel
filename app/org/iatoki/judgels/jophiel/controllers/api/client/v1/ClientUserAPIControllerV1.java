package org.iatoki.judgels.jophiel.controllers.api.client.v1;

import org.iatoki.judgels.jophiel.AccessToken;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.controllers.api.AbstractJophielAPIController;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.UserV1;
import org.iatoki.judgels.jophiel.services.ClientService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.apis.JudgelsAPINotFoundException;
import org.iatoki.judgels.play.apis.JudgelsAppClientAPIIdentity;
import play.mvc.Result;

import javax.inject.Inject;

public final class ClientUserAPIControllerV1 extends AbstractJophielAPIController {

    private final UserService userService;
    private final ClientService clientService;

    @Inject
    public ClientUserAPIControllerV1(UserService userService, ClientService clientService) {
        this.userService = userService;
        this.clientService = clientService;
    }

    public Result isLoggedIn() {
        return okAsJson(IdentityUtils.getUserJid() != null);
    }

    public Result findUserByUsernameAndPassword(String username, String password) {
        authenticateAsJudgelsAppClient(clientService);

        if (!userService.userExistsByUsernameAndPassword(username, password)) {
            throw new JudgelsAPINotFoundException();
        }

        User user = userService.findUserByJid(username);
        return okAsJson(createUserV1FromUser(user));
    }

    public Result findUserByAccessToken(String accessToken) {
        JudgelsAppClientAPIIdentity identity = authenticateAsJudgelsAppClient(clientService);

        if (!clientService.isAccessTokenValid(accessToken)) {
            throw new JudgelsAPINotFoundException();
        }

        AccessToken accessTokenObj = clientService.getAccessTokenByAccessTokenString(accessToken);

        if (!accessTokenObj.getClientJid().equals(identity.getClientJid())) {
            throw new JudgelsAPINotFoundException();
        }

        User user = userService.findUserByJid(accessTokenObj.getUserJid());
        return okAsJson(createUserV1FromUser(user));
    }

    private UserV1 createUserV1FromUser(User user) {
        UserV1 responseBody = new UserV1();
        responseBody.jid = user.getJid();
        responseBody.username = user.getUsername();
        if (user.isShowName()) {
            responseBody.name = user.getName();
        }
        responseBody.profilePictureUrl = user.getProfilePictureUrl().toString();
        return responseBody;
    }
}
