package org.iatoki.judgels.jophiel.client;

import org.iatoki.judgels.jophiel.oauth2.AccessToken;
import org.iatoki.judgels.jophiel.oauth2.AccessTokenModel;
import org.iatoki.judgels.jophiel.oauth2.AuthorizationCode;
import org.iatoki.judgels.jophiel.oauth2.AuthorizationCodeModel;
import org.iatoki.judgels.jophiel.oauth2.IdToken;
import org.iatoki.judgels.jophiel.oauth2.IdTokenModel;
import org.iatoki.judgels.jophiel.oauth2.RefreshToken;
import org.iatoki.judgels.jophiel.oauth2.RefreshTokenModel;

import java.util.List;
import java.util.Set;

final class ClientServiceUtils {

    private ClientServiceUtils() {
        // prevent instantiation
    }

    static Client createClientFromModel(ClientModel clientModel, Set<String> scopeString, List<String> redirectURIs) {
        return new Client(clientModel.id, clientModel.jid, clientModel.name, clientModel.secret, clientModel.applicationType.toString(), scopeString, redirectURIs);
    }

    static AuthorizationCode createAuthorizationCodeFromModel(AuthorizationCodeModel authorizationCodeModel) {
        return new AuthorizationCode(authorizationCodeModel.id, authorizationCodeModel.userJid, authorizationCodeModel.clientJid, authorizationCodeModel.code, authorizationCodeModel.redirectURI, authorizationCodeModel.expireTime, authorizationCodeModel.scopes);
    }

    static AccessToken createAccessTokenFromModel(AccessTokenModel accessTokenModel) {
        return new AccessToken(accessTokenModel.id, accessTokenModel.code, accessTokenModel.userJid, accessTokenModel.clientJid, accessTokenModel.token, accessTokenModel.expireTime, accessTokenModel.redeemed, accessTokenModel.scopes);
    }

    static RefreshToken createRefreshTokenFromModel(RefreshTokenModel refreshTokenModel) {
        return new RefreshToken(refreshTokenModel.id, refreshTokenModel.code, refreshTokenModel.userJid, refreshTokenModel.clientJid, refreshTokenModel.token, refreshTokenModel.scopes, refreshTokenModel.redeemed);
    }

    static IdToken createIdTokenFromModel(IdTokenModel idTokenModel) {
        return new IdToken(idTokenModel.id, idTokenModel.code, idTokenModel.userJid, idTokenModel.clientJid, idTokenModel.token, idTokenModel.redeemed);
    }
}
