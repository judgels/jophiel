package org.iatoki.judgels.jophiel.services.impls;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
import org.iatoki.judgels.play.IdentityUtils;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.Page;
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

            clientBuilder.add(createClientFromModel(clientModel, scopeString, redirectURIs));
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

            clientBuilder.add(createClientFromModel(clientModel, scopeString, redirectURIs));
        }

        return clientBuilder.build();
    }

    @Override
    public boolean isClientAuthorized(String jid, List<String> scopes) {
        String userJid = IdentityUtils.getUserJid();
        Collections.sort(scopes);

        return authorizationCodeDao.isAuthorized(jid, userJid, StringUtils.join(scopes, ","));
    }

    @Override
    public boolean isAccessTokenValid(String accessToken) {
        // TODO check for access token expiry
        return accessTokenDao.existsByToken(accessToken);
    }

    @Override
    public boolean clientExistsByJid(String jid) {
        return clientDao.existsByJid(jid);
    }

    @Override
    public boolean clientExistsByName(String name) {
        return clientDao.existsByName(name);
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

        return createClientFromModel(clientModel, scopeString, redirectURIs);
    }

    @Override
    public Client findClientByJid(String clientJid) {
        ClientModel clientModel = clientDao.findByJid(clientJid);

        Set<String> scopeString = ImmutableSet.copyOf(clientModel.scopes.split(","));
        List<RedirectURIModel> redirectURIModels = redirectURIDao.getByClientJid(clientModel.jid);
        List<String> redirectURIs = redirectURIModels.stream().map(r -> r.redirectURI).collect(Collectors.toList());

        return createClientFromModel(clientModel, scopeString, redirectURIs);
    }

    @Override
    public AuthorizationCode generateAuthorizationCode(String clientJid, String uRI, String responseType, List<String> scopes, long expireTime) {
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
        authorizationCodeModel.userJid = IdentityUtils.getUserJid();
        authorizationCodeModel.code = authorizationCode.toString();
        authorizationCodeModel.expireTime = expireTime;
        authorizationCodeModel.redirectURI = uRI;
        authorizationCodeModel.scopes = StringUtils.join(scopes, ",");
        authorizationCodeDao.persist(authorizationCodeModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return authorizationCode;
    }


    @Override
    public String generateAccessToken(String code, String userId, String clientId, List<String> scopes, long expireTime) {
        com.nimbusds.oauth2.sdk.token.AccessToken accessToken = new BearerAccessToken();
        Collections.sort(scopes);

        AccessTokenModel accessTokenModel = new AccessTokenModel();
        accessTokenModel.code = code;
        accessTokenModel.clientJid = clientId;
        accessTokenModel.userJid = userId;
        accessTokenModel.redeemed = false;
        accessTokenModel.expireTime = expireTime;
        accessTokenModel.scopes = StringUtils.join(scopes, ",");
        accessTokenModel.token = accessToken.getValue();

        accessTokenDao.persist(accessTokenModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return accessTokenModel.token;
    }

    @Override
    public void generateRefreshToken(String code, String userId, String clientId, List<String> scopes) {
        com.nimbusds.oauth2.sdk.token.RefreshToken refreshToken = new com.nimbusds.oauth2.sdk.token.RefreshToken();
        Collections.sort(scopes);

        RefreshTokenModel refreshTokenModel = new RefreshTokenModel();
        refreshTokenModel.code = code;
        refreshTokenModel.clientJid = clientId;
        refreshTokenModel.userJid = userId;
        refreshTokenModel.redeemed = false;
        refreshTokenModel.scopes = StringUtils.join(scopes, ",");
        refreshTokenModel.token = refreshToken.getValue();

        refreshTokenDao.persist(refreshTokenModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void generateIdToken(String code, String userId, String clientId, String nonce, long authTime, String accessToken, long expireTime) {
        try {
            byte[] encoded = Base64.decodeBase64(JophielProperties.getInstance().getIdTokenPrivateKey().getBytes("utf-8"));
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPrivateKey privateKey = (RSAPrivateKey) kf.generatePrivate(keySpec);

            JWSSigner signer = new RSASSASigner(privateKey);

            JWTClaimsSet claimsSet = new JWTClaimsSet();
            claimsSet.setSubject(userId);
            claimsSet.setAudience(clientId);
            claimsSet.setIssuer(JophielProperties.getInstance().getJophielBaseUrl());
            claimsSet.setIssueTime(new Date(System.currentTimeMillis()));
            claimsSet.setExpirationTime(new Date(expireTime));
            claimsSet.setClaim("auth_time", authTime);
            claimsSet.setClaim("at_hash", JudgelsPlayUtils.hashMD5(accessToken).substring(accessToken.length() / 2));

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS512), claimsSet);
            signedJWT.sign(signer);

            IdTokenModel idTokenModel = new IdTokenModel();
            idTokenModel.userJid = userId;
            idTokenModel.clientJid = clientId;
            idTokenModel.code = code;
            idTokenModel.redeemed = false;
            idTokenModel.token = signedJWT.serialize();

            idTokenDao.persist(idTokenModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | JOSEException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public org.iatoki.judgels.jophiel.AuthorizationCode findAuthorizationCodeByCode(String code) {
        AuthorizationCodeModel authorizationCodeModel = authorizationCodeDao.findByCode(code);

        return createAuthorizationCodeFromModel(authorizationCodeModel);
    }

    @Override
    public AccessToken regenerateAccessToken(String code, String userId, String clientId, List<String> scopes, long expireTime) {
        com.nimbusds.oauth2.sdk.token.AccessToken accessToken = new BearerAccessToken();
        Collections.sort(scopes);

        AccessTokenModel accessTokenModel = new AccessTokenModel();
        accessTokenModel.code = code;
        accessTokenModel.clientJid = clientId;
        accessTokenModel.userJid = userId;
        accessTokenModel.redeemed = false;
        accessTokenModel.expireTime = expireTime;
        accessTokenModel.scopes = StringUtils.join(scopes, ",");
        accessTokenModel.token = accessToken.getValue();

        accessTokenDao.persist(accessTokenModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return createAccessTokenFromModel(accessTokenModel);
    }

    @Override
    public AccessToken getAccessTokenByAccessTokenString(String accessToken) {
        AccessTokenModel accessTokenModel = accessTokenDao.findByToken(accessToken);

        return createAccessTokenFromModel(accessTokenModel);
    }

    @Override
    public AccessToken getAccessTokenByAuthCode(String authCode) {
        AccessTokenModel accessTokenModel = accessTokenDao.findByCode(authCode);

        return createAccessTokenFromModel(accessTokenModel);
    }

    @Override
    public RefreshToken getRefreshTokenByRefreshTokenString(String refreshToken) {
        RefreshTokenModel refreshTokenModel = refreshTokenDao.findByToken(refreshToken);

        return createRefreshTokenFromModel(refreshTokenModel);
    }

    @Override
    public RefreshToken getRefreshTokenByAuthCode(String authCode) {
        RefreshTokenModel refreshTokenModel = refreshTokenDao.findByCode(authCode);

        return createRefreshTokenFromModel(refreshTokenModel);
    }

    @Override
    public IdToken getIdTokenByAuthCode(String authCode) {
        IdTokenModel idTokenModel = idTokenDao.findByCode(authCode);

        return createIdTokenFromModel(idTokenModel);
    }

    @Override
    public long redeemAccessTokenById(long accessTokenId) {
        AccessTokenModel accessTokenModel = accessTokenDao.findById(accessTokenId);
        if (accessTokenModel.redeemed) {
            throw new RuntimeException();
        }
        accessTokenModel.redeemed = true;

        accessTokenDao.edit(accessTokenModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return TimeUnit.SECONDS.convert((accessTokenModel.expireTime - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
    }

    @Override
    public void redeemRefreshTokenById(long refreshTokenId) {
        RefreshTokenModel refreshTokenModel = refreshTokenDao.findById(refreshTokenId);
        if (refreshTokenModel.redeemed) {
            throw new RuntimeException();
        }
        refreshTokenModel.redeemed = true;

        refreshTokenDao.edit(refreshTokenModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void redeemIdTokenById(long idTokenId) {
        IdTokenModel idTokenModel = idTokenDao.findById(idTokenId);
        if (idTokenModel.redeemed) {
            throw new RuntimeException();
        }
        idTokenModel.redeemed = true;

        idTokenDao.edit(idTokenModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void createClient(String name, String applicationType, List<String> scopes, List<String> redirectURIs) {
        ClientModel clientModel = new ClientModel();
        clientModel.name = name;
        clientModel.secret = JudgelsPlayUtils.generateNewSecret();
        clientModel.applicationType = applicationType;
        List<String> scopeList = scopes.stream().filter(s -> ((s != null) && (Scope.valueOf(s) != null))).collect(Collectors.toList());
        clientModel.scopes = StringUtils.join(scopeList, ",");

        clientDao.persist(clientModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        for (String redirectURI : redirectURIs) {
            RedirectURIModel redirectURIModel = new RedirectURIModel();
            redirectURIModel.redirectURI = redirectURI;
            redirectURIModel.clientJid = clientModel.jid;

            redirectURIDao.persist(redirectURIModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }
    }

    @Override
    public void updateClient(long clientId, String name, List<String> scopes, List<String> redirectURIs) throws ClientNotFoundException {
        ClientModel clientModel = clientDao.findById(clientId);

        if (clientModel == null) {
            throw new ClientNotFoundException("Client Not Found.");
        }

        clientModel.name = name;
        List<String> scopeList = scopes.stream().filter(s -> ((s != null) && (Scope.valueOf(s) != null))).collect(Collectors.toList());
        clientModel.scopes = StringUtils.join(scopeList, ",");

        clientDao.edit(clientModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        List<RedirectURIModel> oldRedirectURIs = redirectURIDao.getByClientJid(clientModel.jid);
        for (RedirectURIModel redirectURIModel : oldRedirectURIs) {
            redirectURIDao.remove(redirectURIModel);
        }

        for (String redirectURI : redirectURIs) {
            RedirectURIModel redirectURIModel = new RedirectURIModel();
            redirectURIModel.redirectURI = redirectURI;
            redirectURIModel.clientJid = clientModel.jid;

            redirectURIDao.persist(redirectURIModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }
    }

    @Override
    public void deleteClient(long clientId) throws ClientNotFoundException {
        ClientModel clientModel = clientDao.findById(clientId);

        if (clientModel == null) {
            throw new ClientNotFoundException("Client Not Found.");
        }

        clientDao.remove(clientModel);
    }

    @Override
    public Page<Client> getPageOfClients(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = clientDao.countByFilters(filterString, ImmutableMap.of(), ImmutableMap.of());
        List<ClientModel> clientModels = clientDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(), ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<Client> clients = Lists.transform(clientModels, m -> createClientFromModel(m, ImmutableSet.copyOf(m.scopes.split(",")), ImmutableList.of()));

        return new Page<>(clients, totalPages, pageIndex, pageSize);
    }

    private Client createClientFromModel(ClientModel clientModel, Set<String> scopeString, List<String> redirectURIs) {
        return new Client(clientModel.id, clientModel.jid, clientModel.name, clientModel.secret, clientModel.applicationType.toString(), scopeString, redirectURIs);
    }

    private org.iatoki.judgels.jophiel.AuthorizationCode createAuthorizationCodeFromModel(AuthorizationCodeModel authorizationCodeModel) {
        return new org.iatoki.judgels.jophiel.AuthorizationCode(authorizationCodeModel.id, authorizationCodeModel.userJid, authorizationCodeModel.clientJid, authorizationCodeModel.code, authorizationCodeModel.redirectURI, authorizationCodeModel.expireTime, authorizationCodeModel.scopes);
    }

    private AccessToken createAccessTokenFromModel(AccessTokenModel accessTokenModel) {
        return new AccessToken(accessTokenModel.id, accessTokenModel.code, accessTokenModel.userJid, accessTokenModel.clientJid, accessTokenModel.token, accessTokenModel.expireTime, accessTokenModel.redeemed, accessTokenModel.scopes);
    }

    private RefreshToken createRefreshTokenFromModel(RefreshTokenModel refreshTokenModel) {
        return new RefreshToken(refreshTokenModel.id, refreshTokenModel.code, refreshTokenModel.userJid, refreshTokenModel.clientJid, refreshTokenModel.token, refreshTokenModel.scopes, refreshTokenModel.redeemed);
    }

    private IdToken createIdTokenFromModel(IdTokenModel idTokenModel) {
        return new IdToken(idTokenModel.id, idTokenModel.code, idTokenModel.userJid, idTokenModel.clientJid, idTokenModel.token, idTokenModel.redeemed);
    }
}
