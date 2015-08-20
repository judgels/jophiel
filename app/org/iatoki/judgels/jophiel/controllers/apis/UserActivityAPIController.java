package org.iatoki.judgels.jophiel.controllers.apis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.codec.binary.Base64;
import org.iatoki.judgels.jophiel.AccessToken;
import org.iatoki.judgels.jophiel.UserActivity;
import org.iatoki.judgels.jophiel.UserInfo;
import org.iatoki.judgels.jophiel.services.ClientService;
import org.iatoki.judgels.jophiel.services.UserActivityService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named
public final class UserActivityAPIController extends AbstractJudgelsAPIController {

    private final ClientService clientService;
    private final UserActivityService userActivityService;
    private final UserService userService;

    @Inject
    public UserActivityAPIController(ClientService clientService, UserActivityService userActivityService, UserService userService) {
        this.clientService = clientService;
        this.userActivityService = userActivityService;
        this.userService = userService;
    }

    @Transactional
    public Result postCreateUserActivity() {
        DynamicForm dForm = DynamicForm.form().bindFromRequest();

        String token;
        if ((request().getHeader("Authorization") != null) && "Bearer".equals(request().getHeader("Authorization").split(" ")[0])) {
            token = new String(Base64.decodeBase64(request().getHeader("Authorization").split(" ")[1]));
        } else {
            token = dForm.get("token");
        }

        if (!clientService.isAccessTokenValid(token)) {
            ObjectNode jsonResponse = Json.newObject();
            jsonResponse.put("success", false);
            jsonResponse.put("error", "invalid_token");
            return unauthorized(jsonResponse);
        }

        AccessToken accessToken = clientService.getAccessTokenByAccessTokenString(token);
        UserInfo user = userService.findUserInfoByJid(accessToken.getUserJid());
        String userActivitiesString = dForm.get("userActivities");
        JsonNode userActivitiesJson = Json.parse(userActivitiesString);
        for (int i = 0; i < userActivitiesJson.size(); ++i) {
            UserActivity userActivity = Json.fromJson(userActivitiesJson.get(i), UserActivity.class);
            userActivityService.createUserActivity(accessToken.getClientJid(), user.getJid(), userActivity.getTime(), userActivity.getLog(), userActivity.getIpAddress());
        }

        ObjectNode jsonResponse = Json.newObject();
        jsonResponse.put("success", true);
        return ok(jsonResponse);
    }
}
