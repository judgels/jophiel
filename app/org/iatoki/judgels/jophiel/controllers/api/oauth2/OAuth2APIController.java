package org.iatoki.judgels.jophiel.controllers.api.oauth2;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.iatoki.judgels.jophiel.oauth2.AccessToken;
import org.iatoki.judgels.jophiel.oauth2.AuthorizationCode;
import org.iatoki.judgels.jophiel.client.Client;
import org.iatoki.judgels.jophiel.oauth2.IdToken;
import org.iatoki.judgels.jophiel.oauth2.RefreshToken;
import org.iatoki.judgels.jophiel.user.User;
import org.iatoki.judgels.jophiel.user.profile.email.UserEmail;
import org.iatoki.judgels.jophiel.user.profile.info.UserInfo;
import org.iatoki.judgels.jophiel.user.profile.phone.UserPhone;
import org.iatoki.judgels.jophiel.controllers.api.AbstractJophielAPIController;
import org.iatoki.judgels.jophiel.client.ClientService;
import org.iatoki.judgels.jophiel.user.profile.email.UserEmailService;
import org.iatoki.judgels.jophiel.user.profile.phone.UserPhoneService;
import org.iatoki.judgels.jophiel.user.profile.phone.UserProfileService;
import org.iatoki.judgels.jophiel.user.UserService;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
@Named
public final class OAuth2APIController extends AbstractJophielAPIController {

    private final ClientService clientService;
    private final UserEmailService userEmailService;
    private final UserPhoneService userPhoneService;
    private final UserProfileService userProfileService;
    private final UserService userService;

    @Inject
    public OAuth2APIController(ClientService clientService, UserEmailService userEmailService, UserPhoneService userPhoneService, UserProfileService userProfileService, UserService userService) {
        this.clientService = clientService;
        this.userEmailService = userEmailService;
        this.userPhoneService = userPhoneService;
        this.userProfileService = userProfileService;
        this.userService = userService;
    }

    @Transactional
    public Result token() {
        DynamicForm dForm = DynamicForm.form().bindFromRequest();
        String grantType = dForm.get("grant_type");

        if ("authorization_code".equals(grantType)) {
            return processTokenAuthCodeRequest(dForm);
        } else if ("refresh_token".equals(grantType)) {
            return processTokenRefreshTokenRequest(dForm);
        } else {
            ObjectNode jsonResponse = Json.newObject();
            jsonResponse.put("error", "invalid_grant");
            return badRequest(jsonResponse);
        }
    }

    @Transactional(readOnly = true)
    public Result userInfo() {
        DynamicForm dForm = DynamicForm.form().bindFromRequest();
        String token;
        if ((request().getHeader("Authorization") != null) && "Bearer".equals(request().getHeader("Authorization").split(" ")[0])) {
            token = new String(Base64.decodeBase64(request().getHeader("Authorization").split(" ")[1]));
        } else {
            token = dForm.get("token");
        }

        if (!clientService.isAccessTokenValid(token, System.currentTimeMillis())) {
            ObjectNode jsonResponse = Json.newObject();
            jsonResponse.put("error", "invalid_token");
            return unauthorized(jsonResponse);
        }

        AccessToken accessToken = clientService.getAccessTokenByAccessTokenString(token);
        User user = userService.findUserByJid(accessToken.getUserJid()).get();
        UserEmail userEmail = userEmailService.findEmailByJid(user.getEmailJid()).get();
        Optional<UserPhone> userPhone = user.getPhoneJid().flatMap(userPhoneService::findPhoneByJid);
        Optional<UserInfo> userInfo = userProfileService.findInfo(user.getJid());

        ObjectNode jsonResponse = Json.newObject();
        jsonResponse.put("sub", user.getJid());
        if (user.isShowName()) {
            jsonResponse.put("name", user.getName());
        }
        jsonResponse.put("preferred_username", user.getUsername());
        jsonResponse.put("picture", user.getProfilePictureUrl().toString());
        if (userEmail != null) {
            jsonResponse.put("email", userEmail.getEmail());
            jsonResponse.put("email_verified", userEmail.isEmailVerified());
        }
        userPhone.ifPresent(p -> {
            jsonResponse.put("phone_number", p.getPhone());
            jsonResponse.put("phone_number_verified", p.isPhoneVerified());
        });
        userInfo.ifPresent(info -> {
            jsonResponse.put("gender", info.getGender());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            jsonResponse.put("birthdate", simpleDateFormat.format(info.getBirthDate()));
            ObjectNode jsonAddress = Json.newObject();
            jsonAddress.put("street_address", info.getStreetAddress());
            jsonAddress.put("locality", info.getCity());
            jsonAddress.put("region", info.getProvinceOrState());
            jsonAddress.put("postal_code", info.getPostalCode());
            jsonAddress.put("country", info.getCountry());
            jsonResponse.set("address", jsonAddress);
        });

        return ok(jsonResponse);
    }

    private Result processTokenAuthCodeRequest(DynamicForm dForm) {
        String authCode = dForm.get("code");
        String redirectUri = dForm.get("redirect_uri");
        AuthorizationCode authorizationCode = clientService.findAuthorizationCodeByCode(authCode);

        if (authorizationCode.isExpired() || !authorizationCode.getRedirectURI().equals(redirectUri)) {
            ObjectNode jsonResponse = Json.newObject();
            jsonResponse.put("error", "invalid_request");
            return badRequest(jsonResponse);
        }

        String scope = dForm.get("scope");
        UsernamePasswordCredentials credentials = JudgelsPlayUtils.parseBasicAuthFromRequest(request());

        if (credentials == null) {
            response().setHeader("WWW-Authenticate", "Basic realm=\"" + request().host() + "\"");
            return unauthorized();
        }

        String clientJid = credentials.getUserName();
        String clientSecret = credentials.getPassword();

        if ((clientJid == null) || !clientService.clientExistsByJid(clientJid)) {
            ObjectNode jsonResponse = Json.newObject();
            jsonResponse.put("error", "invalid_client");
            return badRequest(jsonResponse);
        }

        Client client = clientService.findClientByJid(clientJid);
        if (!client.getSecret().equals(clientSecret) || !authorizationCode.getClientJid().equals(client.getJid())) {
            ObjectNode jsonResponse = Json.newObject();
            jsonResponse.put("error", "unauthorized_client");
            return unauthorized(jsonResponse);
        }

        Set<String> addedSet = Arrays.asList(scope.split(" ")).stream()
                .filter(s -> (!s.isEmpty()) && (!client.getScopes().contains(StringUtils.upperCase(s))))
                .collect(Collectors.toSet());
        if (!addedSet.isEmpty()) {
            ObjectNode jsonResponse = Json.newObject();
            jsonResponse.put("error", "invalid_scope");
            return badRequest(jsonResponse);
        }

        AccessToken accessToken = clientService.getAccessTokenByAuthCode(authCode);
        if (accessToken.isRedeemed()) {
            ObjectNode jsonResponse = Json.newObject();
            jsonResponse.put("error", "invalid_client");
            return badRequest(jsonResponse);
        }

        ObjectNode jsonResponse = Json.newObject();
        if (client.getScopes().contains("OFFLINE_ACCESS")) {
            RefreshToken refreshToken = clientService.getRefreshTokenByAuthCode(authCode);
            jsonResponse.put("refresh_token", refreshToken.getToken());
            clientService.redeemRefreshTokenById(refreshToken.getId(), client.getJid(), getCurrentUserIpAddress());
        }
        if (client.getScopes().contains("OPENID")) {
            IdToken idToken = clientService.getIdTokenByAuthCode(authCode);
            jsonResponse.put("id_token", idToken.getToken());
            clientService.redeemIdTokenById(idToken.getId(), client.getJid(), getCurrentUserIpAddress());
        }
        jsonResponse.put("access_token", accessToken.getToken());
        jsonResponse.put("token_type", "Bearer");
        jsonResponse.put("expire_in", clientService.redeemAccessTokenById(accessToken.getId(), client.getJid(), getCurrentUserIpAddress()));
        return ok(jsonResponse);
    }

    private Result processTokenRefreshTokenRequest(DynamicForm dForm) {
        String refreshTokenString = dForm.get("refresh_token");

        RefreshToken refreshToken = clientService.getRefreshTokenByRefreshTokenString(refreshTokenString);
        if (!refreshToken.getToken().equals(refreshTokenString) || !refreshToken.isRedeemed()) {
            ObjectNode result = Json.newObject();
            result.put("error", "invalid_request");

            return badRequest(result);
        }

        UsernamePasswordCredentials credentials = JudgelsPlayUtils.parseBasicAuthFromRequest(request());

        if (credentials == null) {
            response().setHeader("WWW-Authenticate", "Basic realm=\"" + request().host() + "\"");
            return unauthorized();
        }

        String clientJid = credentials.getUserName();
        String clientSecret = credentials.getPassword();
        if ((clientJid == null) || !clientService.clientExistsByJid(clientJid)) {
            ObjectNode jsonResponse = Json.newObject();
            jsonResponse.put("error", "invalid_client");
            return badRequest(jsonResponse);
        }

        Client client = clientService.findClientByJid(clientJid);
        if (!client.getSecret().equals(clientSecret) || !refreshToken.getClientJid().equals(client.getJid())) {
            ObjectNode jsonResponse = Json.newObject();
            jsonResponse.put("error", "unauthorized_client");
            return unauthorized(jsonResponse);
        }

        if (!refreshToken.isRedeemed()) {
            ObjectNode jsonResponse = Json.newObject();
            jsonResponse.put("error", "invalid_client");
            return badRequest(jsonResponse);
        }

        AccessToken newAccessToken = clientService.regenerateAccessToken(refreshToken.getCode(), refreshToken.getUserJid(), refreshToken.getClientJid(), Arrays.asList(refreshToken.getScopes().split(",")), System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES), getCurrentUserIpAddress());
        ObjectNode jsonResponse = Json.newObject();
        if (client.getScopes().contains("OPENID")) {
            IdToken idToken = clientService.getIdTokenByAuthCode(refreshToken.getCode());
            jsonResponse.put("id_token", idToken.getToken());
            clientService.redeemIdTokenById(idToken.getId(), client.getJid(), getCurrentUserIpAddress());
        }
        jsonResponse.put("access_token", newAccessToken.getToken());
        jsonResponse.put("token_type", "Bearer");
        jsonResponse.put("expire_in", clientService.redeemAccessTokenById(newAccessToken.getId(), client.getJid(), getCurrentUserIpAddress()));
        return ok(jsonResponse);
    }
}
