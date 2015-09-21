package org.iatoki.judgels.jophiel.services.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.jophiel.AccessToken;
import org.iatoki.judgels.jophiel.Client;
import org.iatoki.judgels.jophiel.ClientNotFoundException;
import org.iatoki.judgels.jophiel.IdToken;
import org.iatoki.judgels.jophiel.JophielProperties;
import org.iatoki.judgels.jophiel.RefreshToken;
import org.iatoki.judgels.jophiel.Scope;
import org.iatoki.judgels.jophiel.models.daos.AccessTokenDao;
import org.iatoki.judgels.jophiel.models.daos.AuthorizationCodeDao;
import org.iatoki.judgels.jophiel.models.daos.ClientDao;
import org.iatoki.judgels.jophiel.models.daos.IdTokenDao;
import org.iatoki.judgels.jophiel.models.daos.RedirectURIDao;
import org.iatoki.judgels.jophiel.models.daos.RefreshTokenDao;
import org.iatoki.judgels.jophiel.models.entities.AccessTokenModel;
import org.iatoki.judgels.jophiel.models.entities.AuthorizationCodeModel;
import org.iatoki.judgels.jophiel.models.entities.ClientModel;
import org.iatoki.judgels.jophiel.models.entities.IdTokenModel;
import org.iatoki.judgels.jophiel.models.entities.RedirectURIModel;
import org.iatoki.judgels.jophiel.models.entities.RefreshTokenModel;
import org.iatoki.judgels.jophiel.services.ClientService;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.Page;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
@Named("clientService")
public final class ClientServiceImpl implements ClientService {

    private final AccessTokenDao accessTokenDao;
    private final AuthorizationCodeDao authorizationCodeDao;
    private final ClientDao clientDao;
    private final IdTokenDao idTokenDao;
    private final RedirectURIDao redirectURIDao;
    private final RefreshTokenDao refreshTokenDao;

    @Inject
    public ClientServiceImpl(AccessTokenDao accessTokenDao, AuthorizationCodeDao authorizationCodeDao, ClientDao clientDao, IdTokenDao idTokenDao, RedirectURIDao redirectURIDao, RefreshTokenDao refreshTokenDao) {
        this.accessTokenDao = accessTokenDao;
        this.authorizationCodeDao = authorizationCodeDao;
        this.clientDao = clientDao;
        this.idTokenDao = idTokenDao;
        this.redirectURIDao = redirectURIDao;
        this.refreshTokenDao = refreshTokenDao;
    }

    @Override
    public List<Client> getAllClients() {
        List<ClientModel> clientModels = clientDao.getAll();
        ImmutableList.Builder<Client> clientBuilder = ImmutableList.builder();
        for (ClientModel clientModel : clientModels) {
            Set<String> scopeString = ImmutableSet.copyOf(clientModel.scopes.split(","));
            List<RedirectURIModel> redirectURIModels = redirectURIDao.getByClientJid(clientModel.jid);
            List<String> redirectURIs = redirectURIModels.stream().map(r -> r.redirectURI).collect(Collectors.toList());

            clientBuilder.add(ClientServiceUtils.createClientFromModel(clientModel, scopeString, redirectURIs));
        }

        return clientBuilder.build();
    }

    @Override
    public List<Client> getClientsByTerm(String term) {
        List<ClientModel> clientModels = clientDao.findSortedByFilters("id", "asc", term, 0, -1);
        ImmutableList.Builder<Client> clientBuilder = ImmutableList.builder();

        for (ClientModel clientModel : clientModels) {
            Set<String> scopeString = ImmutableSet.copyOf(clientModel.scopes.split(","));
            List<RedirectURIModel> redirectURIModels = redirectURIDao.getByClientJid(clientModel.jid);
            List<String> redirectURIs = redirectURIModels.stream().map(r -> r.redirectURI).collect(Collectors.toList());

            clientBuilder.add(ClientServiceUtils.createClientFromModel(clientModel, scopeString, redirectURIs));
        }

        return clientBuilder.build();
    }

    @Override
    public boolean isClientAuthorized(String userJid, String clientJid, List<String> scopes) {
        Collections.sort(scopes);

        return authorizationCodeDao.isAuthorized(clientJid, userJid, StringUtils.join(scopes, ","));
    }

    @Override
    public boolean isAccessTokenValid(String accessToken, long time) {
        return accessTokenDao.existsValidByTokenAndTime(accessToken, time);
    }

    @Override
    public boolean clientExistsByJid(String clientJid) {
        return clientDao.existsByJid(clientJid);
    }

    @Override
    public boolean clientExistsByName(String clientName) {
        return clientDao.existsByName(clientName);
    }

    @Override
    public Client findClientById(long clientId) throws ClientNotFoundException {
        ClientModel clientModel = clientDao.findById(clientId);
        if (clientModel == null) {
            throw new ClientNotFoundException("Client not found.");
        }

        Set<String> scopeString = ImmutableSet.copyOf(clientModel.scopes.split(","));
        List<RedirectURIModel> redirectURIModels = redirectURIDao.getByClientJid(clientModel.jid);
        List<String> redirectURIs = redirectURIModels.stream().map(r -> r.redirectURI).collect(Collectors.toList());

        return ClientServiceUtils.createClientFromModel(clientModel, scopeString, redirectURIs);
    }

    @Override
    public Client findClientByJid(String clientJid) {
        ClientModel clientModel = clientDao.findByJid(clientJid);

        Set<String> scopeString = ImmutableSet.copyOf(clientModel.scopes.split(","));
        List<RedirectURIModel> redirectURIModels = redirectURIDao.getByClientJid(clientModel.jid);
        List<String> redirectURIs = redirectURIModels.stream().map(r -> r.redirectURI).collect(Collectors.toList());

        return ClientServiceUtils.createClientFromModel(clientModel, scopeString, redirectURIs);
    }

    @Override
    public AuthorizationCode generateAuthorizationCode(String userJid, String clientJid, String uRI, String responseType, List<String> scopes, long expireTime, String userIpAddress) {
        Collections.sort(scopes);
        ClientModel clientModel = clientDao.findByJid(clientJid);
        List<RedirectURIModel> redirectURIs = redirectURIDao.getByClientJid(clientJid);

        List<String> enabledScopes = Arrays.asList(clientModel.scopes.split(","));
        boolean check = true;
        for (int i = 0; i < scopes.size(); ++i) {
            if (!enabledScopes.contains(scopes.get(i).toUpperCase())) {
                check = false;
                break;
            }
        }

        if (!responseType.equals("code") || (redirectURIs.stream().filter(r -> r.redirectURI.equals(uRI)).count() < 1) || !check) {
            throw new IllegalStateException("Response type, redirect URI, or scope is invalid");
        }

        AuthorizationCode authorizationCode = new AuthorizationCode();

        AuthorizationCodeModel authorizationCodeModel = new AuthorizationCodeModel();
        authorizationCodeModel.clientJid = clientJid;
        authorizationCodeModel.userJid = userJid;
        authorizationCodeModel.code = authorizationCode.toString();
        authorizationCodeModel.expireTime = expireTime;
        authorizationCodeModel.redirectURI = uRI;
        authorizationCodeModel.scopes = StringUtils.join(scopes, ",");
        authorizationCodeDao.persist(authorizationCodeModel, userJid, userIpAddress);

        return authorizationCode;
    }


    @Override
    public String generateAccessToken(String code, String userJid, String clientJid, List<String> scopes, long expireTime, String userIpAddress) {
        com.nimbusds.oauth2.sdk.token.AccessToken accessToken = new BearerAccessToken();
        Collections.sort(scopes);

        AccessTokenModel accessTokenModel = new AccessTokenModel();
        accessTokenModel.code = code;
        accessTokenModel.clientJid = clientJid;
        accessTokenModel.userJid = userJid;
        accessTokenModel.redeemed = false;
        accessTokenModel.expireTime = expireTime;
        accessTokenModel.scopes = StringUtils.join(scopes, ",");
        accessTokenModel.token = accessToken.getValue();

        accessTokenDao.persist(accessTokenModel, userJid, userIpAddress);

        return accessTokenModel.token;
    }

    @Override
    public void generateRefreshToken(String code, String userJid, String clientJid, List<String> scopes, String userIpAddress) {
        com.nimbusds.oauth2.sdk.token.RefreshToken refreshToken = new com.nimbusds.oauth2.sdk.token.RefreshToken();
        Collections.sort(scopes);

        RefreshTokenModel refreshTokenModel = new RefreshTokenModel();
        refreshTokenModel.code = code;
        refreshTokenModel.clientJid = clientJid;
        refreshTokenModel.userJid = userJid;
        refreshTokenModel.redeemed = false;
        refreshTokenModel.scopes = StringUtils.join(scopes, ",");
        refreshTokenModel.token = refreshToken.getValue();

        refreshTokenDao.persist(refreshTokenModel, userJid, userIpAddress);
    }

    @Override
    public void generateIdToken(String code, String userJid, String clientJid, String nonce, long authTime, String accessToken, long expireTime, String userIpAddress) {
        try {
            byte[] encoded = Base64.decodeBase64(JophielProperties.getInstance().getIdTokenPrivateKey().getBytes("utf-8"));
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPrivateKey privateKey = (RSAPrivateKey) kf.generatePrivate(keySpec);

            JWSSigner signer = new RSASSASigner(privateKey);

            JWTClaimsSet claimsSet = new JWTClaimsSet();
            claimsSet.setSubject(userJid);
            claimsSet.setAudience(clientJid);
            claimsSet.setIssuer(JophielProperties.getInstance().getJophielBaseUrl());
            claimsSet.setIssueTime(new Date(System.currentTimeMillis()));
            claimsSet.setExpirationTime(new Date(expireTime));
            claimsSet.setClaim("auth_time", authTime);
            claimsSet.setClaim("at_hash", JudgelsPlayUtils.hashMD5(accessToken).substring(accessToken.length() / 2));

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS512), claimsSet);
            signedJWT.sign(signer);

            IdTokenModel idTokenModel = new IdTokenModel();
            idTokenModel.userJid = userJid;
            idTokenModel.clientJid = clientJid;
            idTokenModel.code = code;
            idTokenModel.redeemed = false;
            idTokenModel.token = signedJWT.serialize();

            idTokenDao.persist(idTokenModel, userJid, userIpAddress);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | JOSEException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public org.iatoki.judgels.jophiel.AuthorizationCode findAuthorizationCodeByCode(String code) {
        AuthorizationCodeModel authorizationCodeModel = authorizationCodeDao.findByCode(code);

        return ClientServiceUtils.createAuthorizationCodeFromModel(authorizationCodeModel);
    }

    @Override
    public AccessToken regenerateAccessToken(String code, String userJid, String clientJid, List<String> scopes, long expireTime, String clientIpAddress) {
        com.nimbusds.oauth2.sdk.token.AccessToken accessToken = new BearerAccessToken();
        Collections.sort(scopes);

        AccessTokenModel accessTokenModel = new AccessTokenModel();
        accessTokenModel.code = code;
        accessTokenModel.clientJid = clientJid;
        accessTokenModel.userJid = userJid;
        accessTokenModel.redeemed = false;
        accessTokenModel.expireTime = expireTime;
        accessTokenModel.scopes = StringUtils.join(scopes, ",");
        accessTokenModel.token = accessToken.getValue();

        accessTokenDao.persist(accessTokenModel, clientJid, clientIpAddress);

        return ClientServiceUtils.createAccessTokenFromModel(accessTokenModel);
    }

    @Override
    public AccessToken getAccessTokenByAccessTokenString(String accessToken) {
        AccessTokenModel accessTokenModel = accessTokenDao.findByToken(accessToken);

        return ClientServiceUtils.createAccessTokenFromModel(accessTokenModel);
    }

    @Override
    public AccessToken getAccessTokenByAuthCode(String authCode) {
        AccessTokenModel accessTokenModel = accessTokenDao.findByCode(authCode);

        return ClientServiceUtils.createAccessTokenFromModel(accessTokenModel);
    }

    @Override
    public RefreshToken getRefreshTokenByRefreshTokenString(String refreshToken) {
        RefreshTokenModel refreshTokenModel = refreshTokenDao.findByToken(refreshToken);

        return ClientServiceUtils.createRefreshTokenFromModel(refreshTokenModel);
    }

    @Override
    public RefreshToken getRefreshTokenByAuthCode(String authCode) {
        RefreshTokenModel refreshTokenModel = refreshTokenDao.findByCode(authCode);

        return ClientServiceUtils.createRefreshTokenFromModel(refreshTokenModel);
    }

    @Override
    public IdToken getIdTokenByAuthCode(String authCode) {
        IdTokenModel idTokenModel = idTokenDao.findByCode(authCode);

        return ClientServiceUtils.createIdTokenFromModel(idTokenModel);
    }

    @Override
    public long redeemAccessTokenById(long accessTokenId, String clientJid, String clientIpAddress) {
        AccessTokenModel accessTokenModel = accessTokenDao.findById(accessTokenId);
        if (accessTokenModel.redeemed) {
            throw new RuntimeException();
        }
        accessTokenModel.redeemed = true;

        accessTokenDao.edit(accessTokenModel, clientJid, clientIpAddress);

        return TimeUnit.SECONDS.convert((accessTokenModel.expireTime - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
    }

    @Override
    public void redeemRefreshTokenById(long refreshTokenId, String clientJid, String clientIpAddress) {
        RefreshTokenModel refreshTokenModel = refreshTokenDao.findById(refreshTokenId);
        if (refreshTokenModel.redeemed) {
            throw new RuntimeException();
        }
        refreshTokenModel.redeemed = true;

        refreshTokenDao.edit(refreshTokenModel, clientJid, clientIpAddress);
    }

    @Override
    public void redeemIdTokenById(long idTokenId, String clientJid, String clientIpAddress) {
        IdTokenModel idTokenModel = idTokenDao.findById(idTokenId);
        if (idTokenModel.redeemed) {
            throw new RuntimeException();
        }
        idTokenModel.redeemed = true;

        idTokenDao.edit(idTokenModel, clientJid, clientIpAddress);
    }

    @Override
    public void createClient(String name, String applicationType, List<String> scopes, List<String> redirectURIs, String userJid, String userIpAddress) {
        ClientModel clientModel = new ClientModel();
        clientModel.name = name;
        clientModel.secret = JudgelsPlayUtils.generateNewSecret();
        clientModel.applicationType = applicationType;
        List<String> scopeList = scopes.stream().filter(s -> ((s != null) && (Scope.valueOf(s) != null))).collect(Collectors.toList());
        clientModel.scopes = StringUtils.join(scopeList, ",");

        clientDao.persist(clientModel, userJid, userIpAddress);

        for (String redirectURI : redirectURIs) {
            RedirectURIModel redirectURIModel = new RedirectURIModel();
            redirectURIModel.redirectURI = redirectURI;
            redirectURIModel.clientJid = clientModel.jid;

            redirectURIDao.persist(redirectURIModel, userJid, userIpAddress);
        }
    }

    @Override
    public void updateClient(String clientJid, String name, List<String> scopes, List<String> redirectURIs, String userJid, String userIpAddress) {
        ClientModel clientModel = clientDao.findByJid(clientJid);

        clientModel.name = name;
        List<String> scopeList = scopes.stream().filter(s -> ((s != null) && (Scope.valueOf(s) != null))).collect(Collectors.toList());
        clientModel.scopes = StringUtils.join(scopeList, ",");

        clientDao.edit(clientModel, userJid, userIpAddress);

        List<RedirectURIModel> oldRedirectURIs = redirectURIDao.getByClientJid(clientModel.jid);
        for (RedirectURIModel redirectURIModel : oldRedirectURIs) {
            redirectURIDao.remove(redirectURIModel);
        }

        for (String redirectURI : redirectURIs) {
            RedirectURIModel redirectURIModel = new RedirectURIModel();
            redirectURIModel.redirectURI = redirectURI;
            redirectURIModel.clientJid = clientModel.jid;

            redirectURIDao.persist(redirectURIModel, userJid, userIpAddress);
        }
    }

    @Override
    public void deleteClient(String clientJid) {
        ClientModel clientModel = clientDao.findByJid(clientJid);

        clientDao.remove(clientModel);
    }

    @Override
    public Page<Client> getPageOfClients(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = clientDao.countByFilters(filterString);
        List<ClientModel> clientModels = clientDao.findSortedByFilters(orderBy, orderDir, filterString, pageIndex * pageSize, pageSize);

        List<Client> clients = Lists.transform(clientModels, m -> ClientServiceUtils.createClientFromModel(m, ImmutableSet.copyOf(m.scopes.split(",")), ImmutableList.of()));

        return new Page<>(clients, totalPages, pageIndex, pageSize);
    }
}
