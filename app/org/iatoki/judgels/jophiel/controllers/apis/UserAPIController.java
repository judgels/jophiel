package org.iatoki.judgels.jophiel.controllers.apis;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.iatoki.judgels.commons.AutoComplete;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.JudgelsUtils;
import org.iatoki.judgels.jophiel.AccessToken;
import org.iatoki.judgels.jophiel.AuthorizationCode;
import org.iatoki.judgels.jophiel.Client;
import org.iatoki.judgels.jophiel.IdToken;
import org.iatoki.judgels.jophiel.RefreshToken;
import org.iatoki.judgels.jophiel.UserInfo;
import org.iatoki.judgels.jophiel.controllers.securities.Authenticated;
import org.iatoki.judgels.jophiel.controllers.securities.LoggedIn;
import org.iatoki.judgels.jophiel.services.ClientService;
import org.iatoki.judgels.jophiel.services.UserProfileService;
import org.iatoki.judgels.jophiel.services.UserService;
import play.data.DynamicForm;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
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
public final class UserAPIController extends Controller {

    private final ClientService clientService;
    private final UserService userService;
    private final UserProfileService userProfileService;

    @Inject
    public UserAPIController(ClientService clientService, UserService userService, UserProfileService userProfileService) {
        this.clientService = clientService;
        this.userService = userService;
        this.userProfileService = userProfileService;
    }

    public Result preUserAutocompleteList() {
        response().setHeader("Access-Control-Allow-Origin", "*");       // Need to add the correct domain in here!!
        response().setHeader("Access-Control-Allow-Methods", "GET");    // Only allow POST
        response().setHeader("Access-Control-Max-Age", "300");          // Cache response for 5 minutes
        response().setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");         // Ensure this header is also allowed!
        return ok();
    }

    @Authenticated(LoggedIn.class)
    @Transactional(readOnly = true)
    public Result userAutoCompleteList() {
        response().setHeader("Access-Control-Allow-Origin", "*");
        response().setContentType("application/javascript");

        DynamicForm form = DynamicForm.form().bindFromRequest();
        UserInfo user = userService.findUserByUserJid(IdentityUtils.getUserJid());
        String term = form.get("term");
        List<UserInfo> users = userService.findAllUserByTerm(term);
        ImmutableList.Builder<AutoComplete> responseBuilder = ImmutableList.builder();

        for (UserInfo user1 : users) {
            responseBuilder.add(new AutoComplete(user1.getJid(), user1.getUsername(), user1.getUsername() + " (" + user1.getName() + ")"));
        }

        String callback = form.get("callback");

        return ok(callback + "(" + Json.toJson(responseBuilder.build()).toString() + ")");
    }

    @Transactional
    public Result token() {
        DynamicForm form = DynamicForm.form().bindFromRequest();
        String grantType = form.get("grant_type");

        if ("authorization_code".equals(grantType)) {
            return processTokenAuthCodeRequest(form);
        } else if ("refresh_token".equals(grantType)) {
            return processTokenRefreshTokenRequest(form);
        } else {
            ObjectNode node = Json.newObject();
            node.put("error", "invalid_grant");
            return badRequest(node);
        }
    }

    @Transactional(readOnly = true)
    public Result userInfo() {
        DynamicForm form = DynamicForm.form().bindFromRequest();
        String token;
        if ((request().getHeader("Authorization") != null) && ("Bearer".equals(request().getHeader("Authorization").split(" ")[0]))) {
            token = new String(Base64.decodeBase64(request().getHeader("Authorization").split(" ")[1]));
        } else {
            token = form.get("token");
        }

        if (clientService.isValidAccessTokenExist(token)) {
            AccessToken accessToken = clientService.findAccessTokenByAccessToken(token);
            UserInfo user = userService.findUserByUserJid(accessToken.getUserJid());
            ObjectNode result = Json.newObject();
            result.put("sub", user.getJid());
            result.put("name", user.getName());
            result.put("preferred_username", user.getUsername());
            result.put("email", user.getEmail());
            result.put("picture", user.getProfilePictureUrl().toString());
            return ok(result);
        } else {
            ObjectNode node = Json.newObject();
            node.put("error", "invalid_token");
            return unauthorized(node);
        }
    }

    @Transactional(readOnly = true)
    public Result verifyUsername() {
        UsernamePasswordCredentials credentials = JudgelsUtils.parseBasicAuthFromRequest(request());

        if (credentials != null) {
            String clientJid = credentials.getUserName();
            String clientSecret = credentials.getPassword();

            DynamicForm form = DynamicForm.form().bindFromRequest();
            if ((clientJid != null) && (clientService.clientExistByClientJid(clientJid))) {
                Client client = clientService.findClientByJid(clientJid);
                if (client.getSecret().equals(clientSecret)) {
                    String username = form.get("username");

                    if (userService.existByUsername(username)) {
                        UserInfo user1 = userService.findUserByUsername(username);

                        ObjectNode objectNode = Json.newObject();
                        objectNode.put("success", true);
                        objectNode.put("jid", user1.getJid());

                        return ok(objectNode);
                    } else {
                        ObjectNode objectNode = Json.newObject();
                        objectNode.put("success", false);

                        return ok(objectNode);
                    }
                } else {
                    ObjectNode node = Json.newObject();
                    node.put("error", "unauthorized_client");
                    return unauthorized(node);
                }
            } else {
                ObjectNode node = Json.newObject();
                node.put("error", "invalid_client");
                return badRequest(node);
            }
        } else {
            response().setHeader("WWW-Authenticate", "Basic realm=\"" + request().host() + "\"");
            return unauthorized();
        }
    }

    @Transactional(readOnly = true)
    public Result userInfoByUserJid() {
        UsernamePasswordCredentials credentials = JudgelsUtils.parseBasicAuthFromRequest(request());

        if (credentials != null) {
            String clientJid = credentials.getUserName();
            String clientSecret = credentials.getPassword();

            DynamicForm form = DynamicForm.form().bindFromRequest();
            if ((clientJid != null) && (clientService.clientExistByClientJid(clientJid))) {
                Client client = clientService.findClientByJid(clientJid);
                if (client.getSecret().equals(clientSecret)) {
                    String userJid = form.get("userJid");
                    if (userService.existsByUserJid(userJid)) {
                        UserInfo response = userService.findPublicUserByUserJid(userJid);

                        return ok(Json.toJson(response));
                    } else {
                        ObjectNode node = Json.newObject();
                        node.put("error", "invalid_user");
                        return unauthorized(node);
                    }
                } else {
                    ObjectNode node = Json.newObject();
                    node.put("error", "unauthorized_client");
                    return unauthorized(node);
                }
            } else {
                ObjectNode node = Json.newObject();
                node.put("error", "invalid_client");
                return badRequest(node);
            }
        } else {
            response().setHeader("WWW-Authenticate", "Basic realm=\"" + request().host() + "\"");
            return unauthorized();
        }
    }

    public Result renderAvatarImage(String imageName) {
        response().setHeader("Cache-Control", "no-transform,public,max-age=300,s-maxage=900");

        String avatarURL = userProfileService.getAvatarImageUrlString(imageName);
        try {
            new URL(avatarURL);
            return temporaryRedirect(avatarURL);
        } catch (MalformedURLException e) {
            File avatarFile = new File(avatarURL);
            if (avatarFile.exists()) {
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
                response().setHeader("Last-Modified", sdf.format(new Date(avatarFile.lastModified())));

                if (request().hasHeader("If-Modified-Since")) {
                    try {
                        Date lastUpdate = sdf.parse(request().getHeader("If-Modified-Since"));
                        if (avatarFile.lastModified() > lastUpdate.getTime()) {
                            BufferedImage in = ImageIO.read(avatarFile);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();

                            String type = FilenameUtils.getExtension(avatarFile.getAbsolutePath());

                            ImageIO.write(in, type, baos);
                            return ok(baos.toByteArray()).as("image/" + type);
                        } else {
                            return status(304);
                        }
                    } catch (ParseException | IOException e2) {
                        throw new RuntimeException(e2);
                    }
                } else {
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
            } else {
                return notFound();
            }
        }
    }

    private Result processTokenAuthCodeRequest(DynamicForm form) {
        String code = form.get("code");
        String redirectUri = form.get("redirect_uri");
        AuthorizationCode authorizationCode = clientService.findAuthorizationCodeByCode(code);

        if ((authorizationCode.getRedirectURI().equals(redirectUri)) && (!authorizationCode.isExpired())) {
            String scope = form.get("scope");
            UsernamePasswordCredentials credentials = JudgelsUtils.parseBasicAuthFromRequest(request());

            if (credentials != null) {
                String clientJid = credentials.getUserName();
                String clientSecret = credentials.getPassword();

                if ((clientJid != null) && (clientService.clientExistByClientJid(clientJid))) {
                    // TODO check if auth code is expired
                    Client client = clientService.findClientByJid(clientJid);
                    if ((client.getSecret().equals(clientSecret)) && (authorizationCode.getClientJid().equals(client.getJid()))) {
                        Set<String> addedSet = Arrays.asList(scope.split(" ")).stream()
                                .filter(s -> (!"".equals(s)) && (!client.getScopes().contains(StringUtils.upperCase(s))))
                                .collect(Collectors.toSet());
                        if (addedSet.isEmpty()) {
                            ObjectNode result = Json.newObject();
                            AccessToken accessToken = clientService.findAccessTokenByCode(code);
                            if (!accessToken.isRedeemed()) {
                                result.put("access_token", accessToken.getToken());
                                if (client.getScopes().contains("OFFLINE_ACCESS")) {
                                    RefreshToken refreshToken = clientService.findRefreshTokenByCode(code);
                                    result.put("refresh_token", refreshToken.getToken());
                                    clientService.redeemRefreshTokenById(refreshToken.getId());
                                }
                                if (client.getScopes().contains("OPENID")) {
                                    IdToken idToken = clientService.findIdTokenByCode(code);
                                    result.put("id_token", idToken.getToken());
                                    clientService.redeemIdTokenById(idToken.getId());
                                }
                                result.put("token_type", "Bearer");
                                result.put("expire_in", clientService.redeemAccessTokenById(accessToken.getId()));
                                return ok(result);
                            } else {
                                ObjectNode node = Json.newObject();
                                node.put("error", "invalid_client");
                                return badRequest(node);
                            }
                        } else {
                            ObjectNode node = Json.newObject();
                            node.put("error", "invalid_scope");
                            return badRequest(node);
                        }
                    } else {
                        ObjectNode node = Json.newObject();
                        node.put("error", "unauthorized_client");
                        return unauthorized(node);
                    }
                } else {
                    ObjectNode node = Json.newObject();
                    node.put("error", "invalid_client");
                    return badRequest(node);
                }
            } else {
                response().setHeader("WWW-Authenticate", "Basic realm=\"" + request().host() + "\"");
                return unauthorized();
            }
        } else {
            ObjectNode node = Json.newObject();
            node.put("error", "invalid_request");
            return badRequest(node);
        }
    }

    private Result processTokenRefreshTokenRequest(DynamicForm form) {
        String refreshToken = form.get("refresh_token");

        RefreshToken refreshToken1 = clientService.findRefreshTokenByRefreshToken(refreshToken);
        if ((refreshToken1.getToken().equals(refreshToken)) && (refreshToken1.isRedeemed())) {
            UsernamePasswordCredentials credentials = JudgelsUtils.parseBasicAuthFromRequest(request());

            if (credentials != null) {
                String clientJid = credentials.getUserName();
                String clientSecret = credentials.getPassword();
                if ((clientJid != null) && (clientService.clientExistByClientJid(clientJid))) {
                    Client client = clientService.findClientByJid(clientJid);
                    if ((client.getSecret().equals(clientSecret)) && (refreshToken1.getClientJid().equals(client.getJid()))) {
                        ObjectNode result = Json.newObject();
                        if (refreshToken1.isRedeemed()) {
                            AccessToken accessToken = clientService.regenerateAccessToken(refreshToken1.getCode(), refreshToken1.getUserJid(), refreshToken1.getClientJid(), Arrays.asList(refreshToken1.getScopes().split(",")), System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES));
                            result.put("access_token", accessToken.getToken());
                            if (client.getScopes().contains("OPENID")) {
                                IdToken idToken = clientService.findIdTokenByCode(refreshToken1.getCode());
                                result.put("id_token", idToken.getToken());
                                clientService.redeemIdTokenById(idToken.getId());
                            }
                            result.put("token_type", "Bearer");
                            result.put("expire_in", clientService.redeemAccessTokenById(accessToken.getId()));
                            return ok(result);
                        } else {
                            ObjectNode node = Json.newObject();
                            node.put("error", "invalid_client");
                            return badRequest(node);
                        }
                    } else {
                        ObjectNode node = Json.newObject();
                        node.put("error", "unauthorized_client");
                        return unauthorized(node);
                    }
                } else {
                    ObjectNode node = Json.newObject();
                    node.put("error", "invalid_client");
                    return badRequest(node);
                }
            } else {
                response().setHeader("WWW-Authenticate", "Basic realm=\"" + request().host() + "\"");
                return unauthorized();
            }
        } else {
            ObjectNode node = Json.newObject();
            node.put("error", "invalid_request");
            return badRequest(node);
        }
    }
}
