package org.iatoki.judgels.jophiel.controllers.api.oauth2;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.SerializeException;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;
import org.iatoki.judgels.jophiel.Client;
import org.iatoki.judgels.jophiel.controllers.AbstractJophielController;
import org.iatoki.judgels.jophiel.services.ClientService;
import org.iatoki.judgels.jophiel.services.UserActivityService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.jophiel.views.html.oauth2.serviceAuthView;
import org.iatoki.judgels.play.HtmlTemplate;
import org.iatoki.judgels.play.controllers.ControllerUtils;
import play.Logger;
import play.db.jpa.Transactional;
import play.i18n.Messages;
import play.mvc.Result;

import javax.inject.Inject;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class OAuth2WebAPIController extends AbstractJophielController {

    private final UserService userService;
    private final ClientService clientService;

    @Inject
    public OAuth2WebAPIController(UserActivityService userActivityService, UserService userService, ClientService clientService) {
        super(userActivityService);

        this.userService = userService;
        this.clientService = clientService;
    }

    @Transactional
    public Result auth() {
        if (!isLoggedIn()) {
            return redirect((org.iatoki.judgels.jophiel.controllers.routes.UserAccountController.serviceLogin(ControllerUtils.getCurrentUrl(request()))));
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
            if (clientService.isClientAuthorized(getCurrentUserJid(), clientID.toString(), scopes)) {
                return postAuth(path);
            }

            HtmlTemplate template = new HtmlTemplate();

            template.setContent(serviceAuthView.render(path, client, scopes));
            template.setMainTitle(Messages.get("auth.text.request"));
            template.setSingleColumn();

            return renderTemplate(template);

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

        long expirationTime = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS);

        com.nimbusds.oauth2.sdk.AuthorizationCode authCode = clientService.generateAuthorizationCode(getCurrentUserJid(), client.getJid(), redirectURI.toString(), responseType.toString(), scope.toStringList(), System.currentTimeMillis() + expirationTime, getCurrentUserIpAddress());
        String accessToken = clientService.generateAccessToken(authCode.getValue(), getCurrentUserJid(), clientID.toString(), scope.toStringList(), System.currentTimeMillis() + expirationTime, getCurrentUserIpAddress());
        clientService.generateRefreshToken(authCode.getValue(), getCurrentUserJid(), clientID.toString(), scope.toStringList(), getCurrentUserIpAddress());
        clientService.generateIdToken(authCode.getValue(), getCurrentUserJid(), client.getJid(), nonce, System.currentTimeMillis(), accessToken, System.currentTimeMillis() + expirationTime, getCurrentUserIpAddress());

        URI result;
        try {
            result = new AuthenticationSuccessResponse(redirectURI, authCode, null, null, state).toURI();
        } catch (SerializeException e) {
            Logger.error("Exception when parsing authentication request.", e);
            return redirect(path + "?error=invalid_request");
        }

        return redirect(result.toString());
    }

    private boolean isLoggedIn() {
        return getCurrentUserJid() != null && userService.userExistsByJid(getCurrentUserJid());
    }
}
