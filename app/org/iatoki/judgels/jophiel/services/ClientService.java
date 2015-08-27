package org.iatoki.judgels.jophiel.services;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.jophiel.AccessToken;
import org.iatoki.judgels.jophiel.Client;
import org.iatoki.judgels.jophiel.ClientNotFoundException;
import org.iatoki.judgels.jophiel.IdToken;
import org.iatoki.judgels.jophiel.RefreshToken;

import java.util.List;

public interface ClientService {

    List<Client> getAllClients();

    List<Client> getClientsByTerm(String term);

    boolean isClientAuthorized(String jid, List<String> scopes);

    boolean isAccessTokenValid(String accessToken);

    boolean clientExistsByJid(String jid);

    boolean clientExistsByName(String name);

    Client findClientById(long clientId) throws ClientNotFoundException;

    Client findClientByJid(String clientJid);

    AuthorizationCode generateAuthorizationCode(String clientJid, String uRI, String responseType, List<String> scopes, long expireTime);

    String generateAccessToken(String code, String userId, String clientId, List<String> scopes, long expireTime);

    void generateRefreshToken(String code, String userId, String clientId, List<String> scopes);

    void generateIdToken(String code, String userId, String clientId, String nonce, long authTime, String accessToken, long expireTime);

    org.iatoki.judgels.jophiel.AuthorizationCode findAuthorizationCodeByCode(String code);

    AccessToken regenerateAccessToken(String code, String userId, String clientId, List<String> scopes, long expireTime);

    AccessToken getAccessTokenByAccessTokenString(String accessToken);

    AccessToken getAccessTokenByAuthCode(String authCode);

    RefreshToken getRefreshTokenByRefreshTokenString(String refreshToken);

    RefreshToken getRefreshTokenByAuthCode(String authCode);

    IdToken getIdTokenByAuthCode(String authCode);

    long redeemAccessTokenById(long accessTokenId);

    void redeemRefreshTokenById(long refreshTokenId);

    void redeemIdTokenById(long idTokenId);

    void createClient(String name, String applicationType, List<String> scopes, List<String> redirectURIs);

    void updateClient(long clientId, String name, List<String> scopes, List<String> redirectURIs) throws ClientNotFoundException;

    void deleteClient(long clientId) throws ClientNotFoundException;

    Page<Client> getPageOfClients(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);
}
