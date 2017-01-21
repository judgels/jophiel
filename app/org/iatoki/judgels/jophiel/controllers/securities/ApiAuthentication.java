package org.iatoki.judgels.jophiel.controllers.securities;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import org.iatoki.judgels.jophiel.user.UserTokenDao;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

public class ApiAuthentication extends Security.Authenticator {

    private UserTokenDao userTokenDao;

    @Inject
    public ApiAuthentication(UserTokenDao userTokenDao) {
        this.userTokenDao = userTokenDao;
    }

    @Override
    public String getUsername(Http.Context context) {
        String token = context.request().getHeader("AuthorizationToken");

        if (token != null) {
            return userTokenDao.getUserJidByToken(token);
        } else {
            return null;
        }
    }

    @Override
    public Result onUnauthorized(Http.Context context) {
        ObjectNode result = Json.newObject();
        result.put("status", "Invalid Authorization Header");

        return unauthorized(result);
    }
}
