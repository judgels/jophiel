package org.iatoki.judgels.jophiel.controllers.apis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.codec.binary.Base64;
import org.iatoki.judgels.jophiel.commons.plains.AccessToken;
import org.iatoki.judgels.jophiel.commons.plains.User;
import org.iatoki.judgels.jophiel.commons.plains.UserActivity;
import org.iatoki.judgels.jophiel.services.ClientService;
import org.iatoki.judgels.jophiel.services.UserActivityService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

@Component
public final class UserActivityAPIController extends Controller {

    @Autowired
    private ClientService clientService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserActivityService userActivityService;


    @Transactional
    public Result postCreateUserActivity() {
        DynamicForm form = DynamicForm.form().bindFromRequest();
        String token;
        if ((request().getHeader("Authorization") != null) && ("Bearer".equals(request().getHeader("Authorization").split(" ")[0]))) {
            token = new String(Base64.decodeBase64(request().getHeader("Authorization").split(" ")[1]));
        } else {
            token = form.get("token");
        }

        if (clientService.isValidAccessTokenExist(token)) {
            AccessToken accessToken = clientService.findAccessTokenByAccessToken(token);

            User user = userService.findUserByUserJid(accessToken.getUserJid());
            String userActivitiesString = form.get("userActivities");
            JsonNode jsonNode = Json.parse(userActivitiesString);
            for (int i = 0; i < jsonNode.size(); ++i) {
                UserActivity userActivity = Json.fromJson(jsonNode.get(i), UserActivity.class);
                userActivityService.createUserActivity(accessToken.getClientJid(), user.getJid(), userActivity.getTime(), userActivity.getLog(), userActivity.getIpAddress());
            }

            ObjectNode result = Json.newObject();
            result.put("success", true);
            return ok(result);
        } else {
            ObjectNode result = Json.newObject();
            result.put("success", false);
            result.put("error", "invalid_token");
            return unauthorized(result);
        }
    }

}
