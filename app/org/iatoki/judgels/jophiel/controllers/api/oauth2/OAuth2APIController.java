package org.iatoki.judgels.jophiel.controllers.api.oauth2;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.SerializeException;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.iatoki.judgels.jophiel.AccessToken;
import org.iatoki.judgels.jophiel.AuthorizationCode;
import org.iatoki.judgels.jophiel.Client;
import org.iatoki.judgels.jophiel.IdToken;
import org.iatoki.judgels.jophiel.RefreshToken;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserEmail;
import org.iatoki.judgels.jophiel.UserInfo;
import org.iatoki.judgels.jophiel.UserPhone;
import org.iatoki.judgels.jophiel.controllers.JophielControllerUtils;
import org.iatoki.judgels.jophiel.controllers.routes;
import org.iatoki.judgels.jophiel.services.ClientService;
import org.iatoki.judgels.jophiel.services.UserActivityService;
import org.iatoki.judgels.jophiel.services.UserEmailService;
import org.iatoki.judgels.jophiel.services.UserPhoneService;
import org.iatoki.judgels.jophiel.services.UserProfileService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.jophiel.views.html.oauth2.serviceAuthView;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.LazyHtml;
import org.iatoki.judgels.play.controllers.ControllerUtils;
import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;
import org.iatoki.judgels.play.views.html.layouts.centerLayout;
import play.Logger;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
@Named
public final class OAuth2APIController extends AbstractJudgelsAPIController {

    private final ClientService clientService;
    private final UserEmailService userEmailService;
    private final UserPhoneService userPhoneService;
    private final UserProfileService userProfileService;
    private final UserService userService;
    private final UserActivityService userActivityService;

    @Inject
    public OAuth2APIController(ClientService clientService, UserEmailService userEmailService, UserPhoneService userPhoneService, UserProfileService userProfileService, UserService userService, UserActivityService userActivityService) {
        this.clientService = clientService;
        this.userEmailService = userEmailService;
        this.userPhoneService = userPhoneService;
        this.userProfileService = userProfileService;
        this.userService = userService;
        this.userActivityService = userActivityService;
    }

    @Transactional
    public Result auth() {
        if (!JophielControllerUtils.getInstance().loggedIn(userService)) {
            return redirect((routes.UserAccountController.serviceLogin(ControllerUtils.getCurrentUrl(request()))));
        }

        String path = request().uri().substring(request().uri().indexOf("?") + 1);
        try {
            AuthenticationRequest req = AuthenticationRequest.parse(path);
            ClientID clientID = req.getClientID();
            if (!clientService.clientExistsByJid(clientID.toString())) {
                return redirect(path + "?error=unauthorized_client");
            }

            Client client = clientService.findClientByJid(clientID.toString());

            List<String> scopes = req.getScope().toStringList();
            if (clientService.isClientAuthorized(clientID.toString(), scopes)) {
                return postAuth(path);
            }

            LazyHtml content = new LazyHtml(serviceAuthView.render(path, client, scopes));
            content.appendLayout(c -> centerLayout.render(c));
            JophielControllerUtils.getInstance().appendTemplateLayout(content, "Auth");

            JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Try authorize client " + client.getName() + " <a href=\"" + "http://" + Http.Context.current().request().host() + Http.Context.current().request().uri() + "\">link</a>.");

            return JophielControllerUtils.getInstance().lazyOk(content);
        } catch (com.nimbusds.oauth2.sdk.ParseException e) {
            Logger.error("Exception when parsing authentication request.", e);
            return redirect(path + "?error=invalid_request");
        }
    }

    @Transactional
    public Result postAuth(String path) {
        AuthenticationRequest authRequest;
        try {
            authRequest = AuthenticationRequest.parse(path);
        } catch (ParseException e) {
            Logger.error("Exception when parsing authentication request.", e);
            return redirect(path + "?error=invalid_request");
        }

        ClientID clientID = authRequest.getClientID();
        if (!clientService.clientExistsByJid(clientID.toString())) {
            return redirect(path + "?error=unauthorized_client");
        }

        Client client = clientService.findClientByJid(clientID.toString());
        URI redirectURI = authRequest.getRedirectionURI();
        ResponseType responseType = authRequest.getResponseType();
        State state = authRequest.getState();
        Scope scope = authRequest.getScope();
        String nonce = (authRequest.getNonce() != null) ? authRequest.getNonce().toString() : "";

        com.nimbusds.oauth2.sdk.AuthorizationCode authCode = clientService.generateAuthorizationCode(IdentityUtils.getUserJid(), client.getJid(), redirectURI.toString(), responseType.toString(), scope.toStringList(), System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES), IdentityUtils.getIpAddress());
        String accessToken = clientService.generateAccessToken(authCode.getValue(), IdentityUtils.getUserJid(), clientID.toString(), scope.toStringList(), System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES), IdentityUtils.getIpAddress());
        clientService.generateRefreshToken(authCode.getValue(), IdentityUtils.getUserJid(), clientID.toString(), scope.toStringList(), IdentityUtils.getIpAddress());
        clientService.generateIdToken(authCode.getValue(), IdentityUtils.getUserJid(), client.getJid(), nonce, System.currentTimeMillis(), accessToken, System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES), IdentityUtils.getIpAddress());

        URI result;
        try {
            result = new AuthenticationSuccessResponse(redirectURI, authCode, null, null, state).toURI();
        } catch (SerializeException e) {
            Logger.error("Exception when parsing authentication request.", e);
            return redirect(path + "?error=invalid_request");
        }

        JophielControllerUtils.getInstance().addActivityLog(userActivityService, "Authorize client " + client.getName() + ".");

        return redirect(result.toString());
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
        User user = userService.findUserByJid(accessToken.getUserJid());
        UserEmail userEmail = null;
        if (user.getEmailJid() != null) {
            userEmail = userEmailService.findEmailByJid(user.getEmailJid());
        }
        UserPhone userPhone = null;
        if (user.getPhoneJid() != null) {
            userPhone = userPhoneService.findPhoneByJid(user.getPhoneJid());
        }
        UserInfo userInfo = null;
        if (userProfileService.infoExists(user.getJid())) {
            userInfo = userProfileService.getInfo(user.getJid());
        }

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
        if (userPhone != null) {
            jsonResponse.put("phone_number", userPhone.getPhone());
            jsonResponse.put("phone_number_verified", userPhone.isPhoneVerified());
        }
        if (userInfo != null) {
            jsonResponse.put("gender", userInfo.getGender());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            jsonResponse.put("birthdate", simpleDateFormat.format(userInfo.getBirthDate()));
            ObjectNode jsonAddress = Json.newObject();
            jsonAddress.put("street_address", userInfo.getStreetAddress());
            jsonAddress.put("locality", userInfo.getCity());
            jsonAddress.put("region", userInfo.getProvinceOrState());
            jsonAddress.put("postal_code", userInfo.getPostalCode());
            jsonAddress.put("country", userInfo.getCountry());
            jsonResponse.set("address", jsonAddress);
        }

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
                .filter(s -> (!"".equals(s)) && (!client.getScopes().contains(StringUtils.upperCase(s))))
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
            clientService.redeemRefreshTokenById(refreshToken.getId(), client.getJid(), IdentityUtils.getIpAddress());
        }
        if (client.getScopes().contains("OPENID")) {
            IdToken idToken = clientService.getIdTokenByAuthCode(authCode);
            jsonResponse.put("id_token", idToken.getToken());
            clientService.redeemIdTokenById(idToken.getId(), client.getJid(), IdentityUtils.getIpAddress());
        }
        jsonResponse.put("access_token", accessToken.getToken());
        jsonResponse.put("token_type", "Bearer");
        jsonResponse.put("expire_in", clientService.redeemAccessTokenById(accessToken.getId(), client.getJid(), IdentityUtils.getIpAddress()));
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

        AccessToken newAccessToken = clientService.regenerateAccessToken(refreshToken.getCode(), refreshToken.getUserJid(), refreshToken.getClientJid(), Arrays.asList(refreshToken.getScopes().split(",")), System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES), IdentityUtils.getIpAddress());
        ObjectNode jsonResponse = Json.newObject();
        if (client.getScopes().contains("OPENID")) {
            IdToken idToken = clientService.getIdTokenByAuthCode(refreshToken.getCode());
            jsonResponse.put("id_token", idToken.getToken());
            clientService.redeemIdTokenById(idToken.getId(), client.getJid(), IdentityUtils.getIpAddress());
        }
        jsonResponse.put("access_token", newAccessToken.getToken());
        jsonResponse.put("token_type", "Bearer");
        jsonResponse.put("expire_in", clientService.redeemAccessTokenById(newAccessToken.getId(), client.getJid(), IdentityUtils.getIpAddress()));
        return ok(jsonResponse);
    }
}
