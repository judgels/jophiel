package org.iatoki.judgels.jophiel.controllers.api.client.v1;

import org.iatoki.judgels.jophiel.oauth2.AccessToken;
import org.iatoki.judgels.jophiel.user.User;
import org.iatoki.judgels.jophiel.controllers.api.AbstractJophielAPIController;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.UserFindByUsernameAndPasswordRequestV1;
import org.iatoki.judgels.jophiel.controllers.api.object.v1.UserV1;
import org.iatoki.judgels.jophiel.client.ClientService;
import org.iatoki.judgels.jophiel.user.UserService;
import org.iatoki.judgels.play.api.JudgelsAPINotFoundException;
import org.iatoki.judgels.play.api.JudgelsAppClientAPIIdentity;
import play.data.DynamicForm;
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
        DynamicForm dForm = DynamicForm.form().bindFromRequest();

        boolean isLoggedIn = (getCurrentUserJid() != null);
        if (isLoggedIn && dForm.data().containsKey("userJid")) {
            isLoggedIn = getCurrentUserJid().equals(dForm.get("userJid"));
        }

        return okAsJson(isLoggedIn);
    }

    public Result findUserByUsernameAndPassword() {
        authenticateAsJudgelsAppClient(clientService);
        UserFindByUsernameAndPasswordRequestV1 requestBody = parseRequestBody(UserFindByUsernameAndPasswordRequestV1.class);

        if (!userService.userExistsByUsernameAndPassword(requestBody.username, requestBody.password)) {
            throw new JudgelsAPINotFoundException();
        }

        User user = userService.findUserByJid(requestBody.username).get();
        return okAsJson(createUserV1FromUser(user));
    }

    public Result findUserByAccessToken(String accessToken) {
        JudgelsAppClientAPIIdentity identity = authenticateAsJudgelsAppClient(clientService);

        if (!clientService.isAccessTokenValid(accessToken, System.currentTimeMillis())) {
            throw new JudgelsAPINotFoundException();
        }

        AccessToken accessTokenObj = clientService.getAccessTokenByAccessTokenString(accessToken);

        if (!accessTokenObj.getClientJid().equals(identity.getClientJid())) {
            throw new JudgelsAPINotFoundException();
        }

        User user = userService.findUserByJid(accessTokenObj.getUserJid()).get();
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
