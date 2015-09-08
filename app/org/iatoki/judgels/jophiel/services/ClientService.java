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

    boolean isClientAuthorized(String clientJid, List<String> scopes);

    boolean isAccessTokenValid(String accessToken);

    boolean clientExistsByJid(String clientJid);

    boolean clientExistsByName(String clientName);

    Client findClientById(long clientId) throws ClientNotFoundException;

    Client findClientByJid(String clientJid);

    AuthorizationCode generateAuthorizationCode(String userJid, String clientJid, String uRI, String responseType, List<String> scopes, long expireTime, String userIpAddress);

    String generateAccessToken(String code, String userJid, String clientJid, List<String> scopes, long expireTime, String userIpAddress);

    void generateRefreshToken(String code, String userJid, String clientJid, List<String> scopes, String userIpAddress);

    void generateIdToken(String code, String userJid, String clientJid, String nonce, long authTime, String accessToken, long expireTime, String userIpAddress);

    org.iatoki.judgels.jophiel.AuthorizationCode findAuthorizationCodeByCode(String code);

    AccessToken regenerateAccessToken(String code, String userJid, String clientJid, List<String> scopes, long expireTime, String clientIpAddress);

    AccessToken getAccessTokenByAccessTokenString(String accessToken);

    AccessToken getAccessTokenByAuthCode(String authCode);

    RefreshToken getRefreshTokenByRefreshTokenString(String refreshToken);

    RefreshToken getRefreshTokenByAuthCode(String authCode);

    IdToken getIdTokenByAuthCode(String authCode);

    long redeemAccessTokenById(long accessTokenId, String clientJid, String clientIpAddress);

    void redeemRefreshTokenById(long refreshTokenId, String clientJid, String clientIpAddress);

    void redeemIdTokenById(long idTokenId, String clientJid, String clientIpAddress);

    void createClient(String name, String applicationType, List<String> scopes, List<String> redirectURIs, String userJid, String userIpAddress);

    void updateClient(String clientJid, String name, List<String> scopes, List<String> redirectURIs, String userJid, String userIpAddress);

    void deleteClient(String clientJid);

    Page<Client> getPageOfClients(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);
}
