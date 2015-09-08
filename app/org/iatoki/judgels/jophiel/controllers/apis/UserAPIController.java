package org.iatoki.judgels.jophiel.controllers.apis;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.iatoki.judgels.AutoComplete;
import org.iatoki.judgels.jophiel.AccessToken;
import org.iatoki.judgels.jophiel.AuthorizationCode;
import org.iatoki.judgels.jophiel.Client;
import org.iatoki.judgels.jophiel.IdToken;
import org.iatoki.judgels.jophiel.PublicUser;
import org.iatoki.judgels.jophiel.RefreshToken;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserEmail;
import org.iatoki.judgels.jophiel.UserInfo;
import org.iatoki.judgels.jophiel.UserPhone;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.services.ClientService;
import org.iatoki.judgels.jophiel.services.UserEmailService;
import org.iatoki.judgels.jophiel.services.UserPhoneService;
import org.iatoki.judgels.jophiel.services.UserProfileService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.controllers.apis.AbstractJudgelsAPIController;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Result;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
@Named
public final class UserAPIController extends AbstractJudgelsAPIController {

    private final ClientService clientService;
    private final UserEmailService userEmailService;
    private final UserPhoneService userPhoneService;
    private final UserProfileService userProfileService;
    private final UserService userService;

    @Inject
    public UserAPIController(ClientService clientService, UserEmailService userEmailService, UserPhoneService userPhoneService, UserProfileService userProfileService, UserService userService) {
        this.clientService = clientService;
        this.userEmailService = userEmailService;
        this.userPhoneService = userPhoneService;
        this.userProfileService = userProfileService;
        this.userService = userService;
    }

    public Result loggedIn() {
        response().setHeader("Access-Control-Allow-Origin", "*");
        response().setContentType("application/javascript");

        DynamicForm dForm = DynamicForm.form().bindFromRequest();
        String callback = dForm.get("callback");

        ObjectNode jsonResponse = Json.newObject();
        if (IdentityUtils.getUserJid() != null) {
            jsonResponse.put("loggedIn", true);
        } else {
            jsonResponse.put("loggedIn", false);
        }

        return ok(createJsonPResponse(callback, jsonResponse.toString()));
    }

    public Result preUserAutocompleteList() {
        setAccessControlOrigin("*", "GET", TimeUnit.SECONDS.convert(5, TimeUnit.MINUTES));
        return ok();
    }

    @Authenticated(LoggedIn.class)
    @Transactional(readOnly = true)
    public Result userAutoCompleteList() {
        response().setHeader("Access-Control-Allow-Origin", "*");
        response().setContentType("application/javascript");

        DynamicForm dForm = DynamicForm.form().bindFromRequest();
        String callback = dForm.get("callback");

        String term = dForm.get("term");
        List<User> users = userService.getUsersByTerm(term);
        ImmutableList.Builder<AutoComplete> autoCompleteBuilder = ImmutableList.builder();
        for (User user : users) {
            String display = user.getUsername();
            if (user.isShowName()) {
                display += " (" + user.getName() + ")";
            }
            autoCompleteBuilder.add(new AutoComplete(user.getJid(), user.getUsername(), display));
        }

        return ok(createJsonPResponse(callback, Json.toJson(autoCompleteBuilder.build()).toString()));
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

        if (!clientService.isAccessTokenValid(token)) {
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

    @Transactional(readOnly = true)
    public Result verifyUsername() {
        UsernamePasswordCredentials credentials = JudgelsPlayUtils.parseBasicAuthFromRequest(request());

        if (credentials == null) {
            response().setHeader("WWW-Authenticate", "Basic realm=\"" + request().host() + "\"");
            return unauthorized();
        }

        String clientJid = credentials.getUserName();
        String clientSecret = credentials.getPassword();

        DynamicForm dForm = DynamicForm.form().bindFromRequest();
        if ((clientJid == null) || !clientService.clientExistsByJid(clientJid)) {
            ObjectNode jsonResponse = Json.newObject();
            jsonResponse.put("error", "invalid_client");
            return badRequest(jsonResponse);
        }

        Client client = clientService.findClientByJid(clientJid);
        if (!client.getSecret().equals(clientSecret)) {
            ObjectNode jsonResponse = Json.newObject();
            jsonResponse.put("error", "unauthorized_client");
            return unauthorized(jsonResponse);
        }

        String username = dForm.get("username");

        if (!userService.userExistsByUsername(username)) {
            ObjectNode result = Json.newObject();
            result.put("success", false);

            return ok(result);
        }

        User user = userService.findUserByUsername(username);
        ObjectNode jsonResponse = Json.newObject();
        jsonResponse.put("success", true);
        jsonResponse.put("jid", user.getJid());

        return ok(jsonResponse);
    }

    @Transactional(readOnly = true)
    public Result userInfoByUserJid() {
        UsernamePasswordCredentials credentials = JudgelsPlayUtils.parseBasicAuthFromRequest(request());

        if (credentials == null) {
            response().setHeader("WWW-Authenticate", "Basic realm=\"" + request().host() + "\"");
            return unauthorized();
        }

        String clientJid = credentials.getUserName();
        String clientSecret = credentials.getPassword();

        DynamicForm dForm = DynamicForm.form().bindFromRequest();
        if ((clientJid == null) || !clientService.clientExistsByJid(clientJid)) {
            ObjectNode jsonResponse = Json.newObject();
            jsonResponse.put("error", "invalid_client");
            return badRequest(jsonResponse);
        }

        Client client = clientService.findClientByJid(clientJid);
        if (!client.getSecret().equals(clientSecret)) {
            ObjectNode jsonResponse = Json.newObject();
            jsonResponse.put("error", "unauthorized_client");
            return unauthorized(jsonResponse);
        }

        String userJid = dForm.get("userJid");
        if (!userService.userExistsByJid(userJid)) {
            ObjectNode jsonResponse = Json.newObject();
            jsonResponse.put("error", "invalid_user");
            return unauthorized(jsonResponse);
        }

        PublicUser publicUser = userService.findPublicUserByJid(userJid);

        return ok(Json.toJson(publicUser));
    }

    public Result renderAvatarImage(String imageName) {
        response().setHeader("Cache-Control", "no-transform,public,max-age=300,s-maxage=900");

        String avatarURL = userProfileService.getAvatarImageUrlString(imageName);
        try {
            new URL(avatarURL);
            return temporaryRedirect(avatarURL);
        } catch (MalformedURLException e) {
            File avatarFile = new File(avatarURL);
            if (!avatarFile.exists()) {
                return notFound();
            }

            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
            response().setHeader("Last-Modified", sdf.format(new Date(avatarFile.lastModified())));

            if (!request().hasHeader("If-Modified-Since")) {
                try {
                    BufferedImage in = ImageIO.read(avatarFile);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    String type = FilenameUtils.getExtension(avatarFile.getAbsolutePath());

                    ImageIO.write(in, type, baos);
                    return ok(baos.toByteArray()).as("image/" + type);
                } catch (IOException e2) {
                    return internalServerError();
                }
            }

            try {
                Date lastUpdate = sdf.parse(request().getHeader("If-Modified-Since"));
                if (avatarFile.lastModified() <= lastUpdate.getTime()) {
                    return status(304);
                }

                BufferedImage in = ImageIO.read(avatarFile);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                String type = FilenameUtils.getExtension(avatarFile.getAbsolutePath());
                ImageIO.write(in, type, baos);

                return ok(baos.toByteArray()).as("image/" + type);
            } catch (ParseException | IOException e2) {
                throw new RuntimeException(e2);
            }
        }
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
