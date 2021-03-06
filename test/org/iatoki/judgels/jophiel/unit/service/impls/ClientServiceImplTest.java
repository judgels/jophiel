package org.iatoki.judgels.jophiel.unit.service.impls;

import com.google.common.collect.ImmutableList;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.iatoki.judgels.jophiel.JophielProperties;
import org.iatoki.judgels.jophiel.client.Client;
import org.iatoki.judgels.jophiel.client.ClientDao;
import org.iatoki.judgels.jophiel.client.ClientModel;
import org.iatoki.judgels.jophiel.client.ClientNotFoundException;
import org.iatoki.judgels.jophiel.client.ClientServiceImpl;
import org.iatoki.judgels.jophiel.oauth2.AccessToken;
import org.iatoki.judgels.jophiel.oauth2.AccessTokenDao;
import org.iatoki.judgels.jophiel.oauth2.AccessTokenModel;
import org.iatoki.judgels.jophiel.oauth2.AuthorizationCodeDao;
import org.iatoki.judgels.jophiel.oauth2.AuthorizationCodeModel;
import org.iatoki.judgels.jophiel.oauth2.IdToken;
import org.iatoki.judgels.jophiel.oauth2.IdTokenDao;
import org.iatoki.judgels.jophiel.oauth2.IdTokenModel;
import org.iatoki.judgels.jophiel.oauth2.RedirectURIDao;
import org.iatoki.judgels.jophiel.oauth2.RedirectURIModel;
import org.iatoki.judgels.jophiel.oauth2.RefreshToken;
import org.iatoki.judgels.jophiel.oauth2.RefreshTokenDao;
import org.iatoki.judgels.jophiel.oauth2.RefreshTokenModel;
import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.model.AbstractModel;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.metamodel.SingularAttribute;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by bagus.seto on 5/25/2015.
 */
@PrepareForTest(JophielProperties.class)
@PowerMockIgnore("javax.crypto.*")
public class ClientServiceImplTest extends PowerMockTestCase {

    @Mock
    private ClientDao clientDao;
    @Mock
    private RedirectURIDao redirectURIDao;
    @Mock
    private AuthorizationCodeDao authorizationCodeDao;
    @Mock
    private AccessTokenDao accessTokenDao;
    @Mock
    private RefreshTokenDao refreshTokenDao;
    @Mock
    private IdTokenDao idTokenDao;

    private ClientServiceImpl clientService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        clientService = new ClientServiceImpl(accessTokenDao, authorizationCodeDao, clientDao, idTokenDao, redirectURIDao, refreshTokenDao);
    }

    @Test
    public void findAllEmptyDataReturnsEmptyList() {
        List<Client> result = clientService.getAllClients();
        Assert.assertEquals(Collections.emptyList(), result, "Result should be an empty list");
    }

    @Test
    public void findAllSomeDataReturnsTheSameList() {
        List<Client> expected = Arrays.asList(
                new Client(1L, "JID0001", "Client 1", "secret1", "Web Server", Arrays.asList("OPENID").stream().collect(Collectors.toSet()), Arrays.asList("http://client1/verify")),
                new Client(2L, "JID0002", "Client 2", "secret2", "Web Server", Arrays.asList("OPENID").stream().collect(Collectors.toSet()), Arrays.asList("http://client2/verify"))
        );

        List<ClientModel> clientDaoFindAllResult = expected.stream().map(client ->
                createClientModelFromClient(client))
                .collect(Collectors.toList());
        Mockito.when(clientDao.getAll()).thenReturn(clientDaoFindAllResult);

        mockRedirectURIDaoFromClients(expected);

        List<Client> result = clientService.getAllClients();
        assertClientListEquals(expected, result);
    }

    @Test
    public void findAllClientByTermEmptyStringTermReturnsAllClient() {
        String emptyStringTerm = "";
        List<Client> expected = Arrays.asList(
                new Client(1L, "JID0001", "Client 1", "secret1", "Web Server", Arrays.asList("OPENID").stream().collect(Collectors.toSet()), Arrays.asList("http://client1/verify")),
                new Client(2L, "JID0002", "Client 2", "secret2", "Web Server", Arrays.asList("OPENID").stream().collect(Collectors.toSet()), Arrays.asList("http://client2/verify"))
        );

        List<ClientModel> clientDaoFindAllClientByTermResult = expected.stream().map(client ->
                createClientModelFromClient(client))
                .collect(Collectors.toList());
        Mockito.when(clientDao.findSortedByFilters(Mockito.anyString(), Mockito.anyString(), Mockito.eq(emptyStringTerm), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(clientDaoFindAllClientByTermResult);

        mockRedirectURIDaoFromClients(expected);

        List<Client> result = clientService.getClientsByTerm(emptyStringTerm);
        assertClientListEquals(expected, result);
    }

    @Test
    public void findAllClientByTermSomeTermNotContainedInClientsReturnsEmptyList() {
        String randomTerm = "asdfasdf";
        Mockito.when(clientDao.findSortedByFilters(Mockito.anyString(), Mockito.anyString(), Mockito.eq(randomTerm), Matchers.<Map<SingularAttribute<? super ClientModel, ? extends Object>, String>>any(), Matchers.<Map<SingularAttribute<? super ClientModel, String>, Set<String>>>any(), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(Collections.emptyList());

        List<Client> result = clientService.getClientsByTerm(randomTerm);
        Assert.assertEquals(Collections.emptyList(), result, "Result should be an empty list");
    }

    @Test
    public void findAllClientByTermSomeTermContainedInClientsReturnsAllClientsThatMatch() {
        String containedTerm = "OPENID";
        List<Client> expected = Arrays.asList(
                new Client(1L, "JID0001", "Alice", "alice secret", "Web Server", Arrays.asList("OPENID").stream().collect(Collectors.toSet()), Arrays.asList("http://alice.com/verify")),
                new Client(2L, "JID0002", "Bob", "bob secret", "Web Server", Arrays.asList("OPENID", "OFFLINEACCESS").stream().collect(Collectors.toSet()), Arrays.asList("http://bob.com/verify"))
        );

        List<ClientModel> clientModelsFromExpected = expected.stream().map(client ->
                createClientModelFromClient(client))
                .collect(Collectors.toList());
        Mockito.when(clientDao.findSortedByFilters(Mockito.anyString(), Mockito.anyString(), Mockito.eq(containedTerm), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(clientModelsFromExpected);

        mockRedirectURIDaoFromClients(expected);

        List<Client> result = clientService.getClientsByTerm(containedTerm);
        assertClientListEquals(expected, result);
    }

    @Test
    public void isClientAuthorizedAuthorizedClientReturnsTrue() {
        String clientJid1 = "JIDC0001";
        String userJid1 = "JIDU0010";
        List<String> scopes1 = Arrays.asList("OPENID");
        String scopes1String = StringUtils.join(scopes1, ",");

        Mockito.when(authorizationCodeDao.isAuthorized(clientJid1, userJid1, scopes1String)).thenReturn(true);

        Assert.assertTrue(clientService.isClientAuthorized(userJid1, clientJid1, scopes1), "Client is not authorized");
    }

    @Test
    public void isClientAuthorizedUnauthorizedClientReturnsFalse() {
        String clientJid1 = "JIDC0000";
        String userJid1 = "JIDU0100";
        List<String> scopes1 = Arrays.asList("OPENID");
        String scopes1String = StringUtils.join(scopes1, ",");

        Mockito.when(authorizationCodeDao.isAuthorized(clientJid1, userJid1, scopes1String)).thenReturn(false);

        Assert.assertFalse(clientService.isClientAuthorized(userJid1, clientJid1, scopes1), "Client is authorized");
    }

    @Test
    public void isValidAccessTokenExistValidAccessTokenReturnsTrue() {
        String validAccessToken = "validaccesstoken";
        long time = System.currentTimeMillis();
        Mockito.when(accessTokenDao.existsValidByTokenAndTime(validAccessToken, time)).thenReturn(true);

        Assert.assertTrue(clientService.isAccessTokenValid(validAccessToken, time), "Access token invalid");
    }

    @Test
    public void isValidAccessTokenExistInvalidAccessTokenReturnsFalse() {
        String invalidAccessToken = "invalidaccesstoken";
        long time = System.currentTimeMillis();
        Mockito.when(accessTokenDao.existsValidByTokenAndTime(invalidAccessToken, time)).thenReturn(false);

        Assert.assertFalse(clientService.isAccessTokenValid(invalidAccessToken, time), "Access token valid");
    }

    @Test
    public void clientExistByClientJidExistingClientJidReturnsTrue() {
        String existingClientJid = "JIDC1234";
        Mockito.when(clientDao.existsByJid(existingClientJid)).thenReturn(true);

        Assert.assertTrue(clientService.clientExistsByJid(existingClientJid), "Client JID not exist");
    }

    @Test
    public void clientExistByClientJidNonExistingClientJidReturnsFalse() {
        String nonExistingClientJid = "JIDCABCDEF";
        Mockito.when(clientDao.existsByJid(nonExistingClientJid)).thenReturn(false);

        Assert.assertFalse(clientService.clientExistsByJid(nonExistingClientJid), "Client JID exist");
    }

    @Test
    public void clientExistByClientNameExistingClientNameReturnsTrue() {
        String existingClientName = "Alice";
        Mockito.when(clientDao.existsByName(existingClientName)).thenReturn(true);

        Assert.assertTrue(clientService.clientExistsByName(existingClientName), "Client name not exist");
    }

    @Test
    public void clientExistByClientNameNonExistingClientNameReturnsFalse() {
        String nonExistingClientName = "Not Alice";
        Mockito.when(clientDao.existsByName(nonExistingClientName)).thenReturn(false);

        Assert.assertFalse(clientService.clientExistsByName(nonExistingClientName), "Client name not exist");
    }

    @Test
    public void findClientByIdExistingClientIdReturnsClient() throws ClientNotFoundException {
        long existingClientId = 10L;
        Client existingClient = new Client(existingClientId, "JIDC1010", "Alice", "alice secret", "Web Server", Arrays.asList("OPENID").stream().collect(Collectors.toSet()), Arrays.asList("http://alice.com/verify"));

        ClientModel clientModelFromExistingClient = createClientModelFromClient(existingClient);
        Mockito.when(clientDao.findById(existingClientId)).thenReturn(clientModelFromExistingClient);

        List<RedirectURIModel> redirectURIModelsFromExistingClient = createRedirectURIModel(1L, existingClient.getRedirectURIs(), existingClient.getJid());
        Mockito.when(redirectURIDao.getByClientJid(existingClient.getJid())).thenReturn(redirectURIModelsFromExistingClient);

        Client result = clientService.findClientById(existingClientId);
        Assert.assertTrue(clientIsEquals(existingClient, result), "Client find by Id not equal");
    }

    @Test
    public void findClientByJidExistingClientJidReturnsClient() {
        String existingClientJid = "JIDC1010";
        Client existingClient = new Client(10L, existingClientJid, "Alice", "alice secret", "Web Server", Arrays.asList("OPENID").stream().collect(Collectors.toSet()), Arrays.asList("http://alice.com/verify"));

        ClientModel clientModelFromExistingClient = createClientModelFromClient(existingClient);
        Mockito.when(clientDao.findByJid(existingClientJid)).thenReturn(clientModelFromExistingClient);

        List<RedirectURIModel> redirectUriModelsFromExistingClient = createRedirectURIModel(1L, existingClient.getRedirectURIs(), existingClient.getJid());
        Mockito.when(redirectURIDao.getByClientJid(existingClient.getJid())).thenReturn(redirectUriModelsFromExistingClient);

        Client result = clientService.findClientByJid(existingClientJid);
        Assert.assertTrue(clientIsEquals(existingClient, result), "Client find by Jid not equal");
    }

    @Test
    public void generateAuthorizationCodeValidClientReturnsAuthorizationCode() {
        String clientJid = "JIDC1010";
        String redirectURI = "http://alice.com/verify";
        String responseType = "code";
        List<String> scopes = Arrays.asList("OPENID");
        long expireTime = 600L;

        Client validClient = new Client(10L, clientJid, "Alice", "alice secret", "Web Server", scopes.stream().collect(Collectors.toSet()), Arrays.asList(redirectURI));

        String getUserJid = "JIDU0101";
        String getIpAddress = "10.10.10.10";

        ClientModel clientModelFromValidClient = createClientModelFromClient(validClient);
        Mockito.when(clientDao.findByJid(validClient.getJid())).thenReturn(clientModelFromValidClient);

        List<RedirectURIModel> redirectURIModelsFromValidClient = createRedirectURIModel(1L, validClient.getRedirectURIs(), validClient.getJid());
        Mockito.when(redirectURIDao.getByClientJid(validClient.getJid())).thenReturn(redirectURIModelsFromValidClient);

        AuthorizationCodeModel authorizationCodeModel = new AuthorizationCodeModel();

        Mockito.doAnswer(invocation -> {
                AbstractModel acm = authorizationCodeModel;

                persistAbstractModel(acm, invocation);

                return null;
            }).when(authorizationCodeDao).persist(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        AuthorizationCode authorizationCode = clientService.generateAuthorizationCode(getUserJid, clientJid, redirectURI, responseType, scopes, expireTime, getIpAddress);

        Assert.assertNotNull(authorizationCode, "Authorization code null");
        Assert.assertNotNull(authorizationCodeModel.userCreate, "UserInfo Create must not be null");
        Assert.assertNotNull(authorizationCodeModel.ipCreate, "IP Create must not be null");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void generateAuthorizationCodeInvalidClientThrowsIllegalStateException() {
        String clientJid = "JIDC1010";
        String redirectUri = "http://bob.com/verify";
        String responseType = "code";
        List<String> scopes = Arrays.asList("OPENID");
        long expireTime = 600L;

        Client validClient = new Client(10L, clientJid, "Alice", "alice secret", "Web Server", scopes.stream().collect(Collectors.toSet()), Arrays.asList("http://alice.com/verify"));

        String getUserJid = "JIDU0101";
        String getIpAddress = "10.10.10.10";

        ClientModel clientModelFromValidClient = createClientModelFromClient(validClient);
        Mockito.when(clientDao.findByJid(validClient.getJid())).thenReturn(clientModelFromValidClient);

        List<RedirectURIModel> redirectURIModelsFromClient = createRedirectURIModel(1L, validClient.getRedirectURIs(), validClient.getJid());
        Mockito.when(redirectURIDao.getByClientJid(validClient.getJid())).thenReturn(redirectURIModelsFromClient);

        AuthorizationCode authorizationCode = clientService.generateAuthorizationCode(getUserJid, clientJid, redirectUri, responseType, scopes, expireTime, getIpAddress);

        Assert.fail("Unreachable");
    }

    @Test
    public void generateAccessTokenValidAuthorizationCodeReturnsAccessToken() {
        String code = "THIS_IS_AUTHORIZATION_CODE";
        String userJid = "JIDU0101";
        String clientJid = "JIDC1010";
        List<String> scopes = Arrays.asList("OPENID");
        long expireTime = 600L;

        String getIpAddress = "10.10.10.10";

        AccessTokenModel accessTokenModel = new AccessTokenModel();

        Mockito.doAnswer(invocation -> {
                AbstractModel atm = accessTokenModel;

                persistAbstractModel(atm, invocation);

                return null;
            }).when(accessTokenDao).persist(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        String token = clientService.generateAccessToken(code, userJid, clientJid, scopes, expireTime, getIpAddress);

        Assert.assertNotNull(token, "Token can not be null");
        Assert.assertNotNull(accessTokenModel.userCreate, "UserInfo create must not be null");
        Assert.assertNotNull(accessTokenModel.ipCreate, "IP create must not be null");
    }

    @Test
    public void generateRefreshTokenValidAuthorizationCodeRefreshTokenModelPersisted() {
        String code = "THIS_IS_AUTHORIZATION_CODE";
        String userJid = "JIDU0101";
        String clientJid = "JIDC1010";
        List<String> scopes = Arrays.asList("OPENID");

        String getIpAddress = "10.10.10.10";

        RefreshTokenModel refreshTokenModel = new RefreshTokenModel();

        Mockito.doAnswer(invocation -> {
                AbstractModel rtm = refreshTokenModel;

                persistAbstractModel(rtm, invocation);

                return null;
            }).when(refreshTokenDao).persist(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        clientService.generateRefreshToken(code, userJid, clientJid, scopes, getIpAddress);

        Assert.assertNotNull(refreshTokenModel.userCreate, "UserInfo Create must not be null");
        Assert.assertNotNull(refreshTokenModel.ipCreate, "IP Create must not be null");
    }

    @Test
    public void generateIdTokenValidAccessTokenIdTokenModelPersisted() throws UnsupportedEncodingException {
        String code = "THIS_IS_AUTHORIZATION_CODE";
        String userJid = "JIDU0101";
        String clientJid = "JIDC1010";
        String nonce = "THIS_IS_NONCE";
        long authTime = 0L;
        String accessToken = "THIS_IS_ACCESS_TOKEN";
        long expireTime = 600L;

        String idTokenPrivateKey = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDCdoHMwrsIiggV6hp7Yf4FZaKqkAeHuk5WAbBzuIDB40gQKKimwfKk+yaR6UKOOduGM3k4eDbaZy3n8NCkWnAvVIwt4rus7LhDhVUNrJGQU9BdK59x+wvhUtMcE2eP0V3hjeJmqzhoJxqLIAcnksU2Z3mmAkgbXecV16fCgo8G1Ny+Ai+FY2ZefRK+LF0u9rGQx5tA6XuQOUWvPJb45YlzmEDLwEMw7nOqwnnN6mSj9cKVfDX33ayvZY0aenEn7SMtrAkia5gBKGKDfN2KECX6OD9joatmNW0b+z9RtAXJvrWtkXhGaZR9+YBLBITllAtgkWMLWCCnDDOM4lNLoj9XAgMBAAECggEACPCz1Psa6DCYYJGLuCJwMEVU7iyC/B13noKjXx6bZM6TMJL99fSyuB0Hz+t+cNV+HzRcnVkBhJb7yE8M+JFj2Pk1HKLw5+lWK1yE5YUKiC0iRjZMNUxKZoiNRhwqRbVlcIo6X2f9xuQNV1oYmhwoTvEA6b3vHLr7dcidYNbpxnGMQZs035um6zShIFNqrmM4poQZZE9NbltOX1k/qxD0+OAAuemU3Y7WzH1XvTwXy7qU8O0PCktTe+QBSJZUPxy1nZwKbF1vdad39KfCjvxemkdUdzuPvlMfi+dsDXjAz71ukUO0r1+4n+l9DYOI8Pq6oI5ZGcwmz5B/Fd8RpPb2gQKBgQDy1o9HCnkL4rw3Wg6hkM46dlPPT7Mm5p+GrNbRxd6bX0wRpXivcasT60u4UZnG7gVVjpqour6tbyRaVNr5F6Cxg6YXDnZKwa8Jz64oUduQqMw7FvGtBG8+NR/26wI53Xoe1nq50ugkq3V3l9TtW9p0ccrsELP7Nu6Fmd4aa9AMFwKBgQDNALqptObo+2jODiuU4+w4wt/hUZa0BbmhjkhJNVpczZvUlXkLtMCq1ESxH4wWzRpBvIlcWpKnSyxzuFD5rtjqHh1kqVbFjQ2k0hRGs5S2vT+aC5oTH4M92nRPCZbWq+26jSVcvAgFj+S6MSOofMDYVOfM3dEKhzNKVsChjGsuwQKBgBCccrKWWc9hVCSpKWUN5b2ECJmexw97KSBqREuXMHIKY8a1PfsqWFyFdOmH03ATKhQ/K/8svwxYFPGE6nGtlxVtfvgGyjq04wdVyIEDkHRlx4qnOCLwsbdcpPIcA0v4BXmEjGKXtb+EZwWmQi92YAwlGI9rWRRvHoPPEa1XAKVDAoGALWgf8D71dl1ZVWqmFJB3Xgsr84hSzQUHnNUbBbwfi7au8WM6MHGUy0HBBUpriRFc43qTIjWdjhiEfA0zQlqMCS8qa4VmhtM7VmqBuzdDlUZNtB0lv16XfzfH00nYcywZt9xTjjrHvBOnIeaIc2VOgZwsy5/GEYLoxWp5uE6V3wECgYALHhV4lk4bH1Gm2S7Od8yPix62dbwoFMjfFiI4Y3dCu7Um93MS34OSWo2pixb9w+1Y/ZNNfrq+tEhUSsJKd3MvE8oskUR4bo4yMQJZC1+FSNUpehjz1Z9XiqJMpsl9GGYXo+nzU27PwlZdorgd8uiH30sNLcm9VG3e72hbQ0EpmQ==";
        String getIpAddress = "10.10.10.10";

        PowerMockito.mockStatic(JophielProperties.class);
        JophielProperties jophielProperties = Mockito.mock(JophielProperties.class);
        Mockito.when(JophielProperties.getInstance()).thenReturn(jophielProperties);
        Mockito.when(jophielProperties.getIdTokenPrivateKey()).thenReturn(idTokenPrivateKey);
        Mockito.when(jophielProperties.getJophielBaseUrl()).thenReturn("http://jophiel.com");

        IdTokenModel idTokenModel = new IdTokenModel();

        Mockito.doAnswer(invocation -> {
                AbstractModel itm = idTokenModel;

                persistAbstractModel(itm, invocation);

                return null;
            }).when(idTokenDao).persist(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        clientService.generateIdToken(code, userJid, clientJid, nonce, authTime, accessToken, expireTime, getIpAddress);

        Assert.assertNotNull(idTokenModel.userCreate, "UserInfo Create must not be null");
        Assert.assertNotNull(idTokenModel.ipCreate, "IP Create must not be null");
    }

    @Test
    public void findByAuthorizationCodeByCodeValidCodeReturnsAuthorizationCode() {
        String validCode = "THIS_IS_AUTHORIZATION_CODE";

        AuthorizationCodeModel authorizationCodeModel = new AuthorizationCodeModel();
        authorizationCodeModel.id = 1L;
        authorizationCodeModel.clientJid = "JIDC1010";
        authorizationCodeModel.userJid = "JIDU0101";
        authorizationCodeModel.code = validCode;
        authorizationCodeModel.expireTime = 600L;
        authorizationCodeModel.redirectURI = "http://alice.com/verify";
        authorizationCodeModel.scopes = StringUtils.join(Arrays.asList("OPENID"), ",");
        org.iatoki.judgels.jophiel.oauth2.AuthorizationCode authorizationCode = createAuthorizationCodeFromModel(authorizationCodeModel);

        Mockito.when(authorizationCodeDao.findByCode(validCode)).thenReturn(authorizationCodeModel);

        org.iatoki.judgels.jophiel.oauth2.AuthorizationCode result = clientService.findAuthorizationCodeByCode(validCode);

        Assert.assertTrue(authorizationCodeIsEquals(authorizationCode, result), "Authorization code not equals");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void findByAuthorizationCodeByCodeInvalidCodeThrowsNullPointerException() {
        String invalidCode = "THIS_IS_NOT_AUTHORIZATION_CODE";

        Mockito.when(authorizationCodeDao.findByCode(invalidCode)).thenReturn(null);

        org.iatoki.judgels.jophiel.oauth2.AuthorizationCode result = clientService.findAuthorizationCodeByCode(invalidCode);

        Assert.fail("Unreachable");
    }

    @Test
    public void regenerateAccessTokenValidAuthorizationCodeReturnsNewAccessToken() {
        String code = "THIS_IS_AUTHORIZATION_CODE";
        String userJid = "JIDU0101";
        String clientJid = "JIDC1010";
        List<String> scopes = Arrays.asList("OPENID");
        long expireTime = 600L;

        String getIpAddress = "10.10.10.10";

        AccessTokenModel accessTokenModel = new AccessTokenModel();

        Mockito.doAnswer(invocation -> {
                AbstractModel atm = accessTokenModel;
                AccessTokenModel accessTokenModel1 = (AccessTokenModel) invocation.getArguments()[0];

                accessTokenModel.redeemed = accessTokenModel1.redeemed;
                persistAbstractModel(atm, invocation);

                return null;
            }).when(accessTokenDao).persist(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        AccessToken accessToken = clientService.regenerateAccessToken(code, userJid, clientJid, scopes, expireTime, getIpAddress);

        Assert.assertFalse(accessToken.isRedeemed(), "Access token is redeemed");
        Assert.assertNotNull(accessTokenModel.userCreate, "UserInfo create must not be null");
        Assert.assertNotNull(accessTokenModel.ipCreate, "IP create must not be null");

    }

    @Test
    public void findAccessTokenByAccessTokenValidTokenReturnsAccessToken() {
        String validToken = "THIS_IS_ACCESS_TOKEN";

        AccessTokenModel accessTokenModel = new AccessTokenModel();
        accessTokenModel.id = 1L;
        accessTokenModel.code = "THIS_IS_AUTHORIZATION_CODE";
        accessTokenModel.clientJid = "JIDC1010";
        accessTokenModel.userJid = "JIDU0101";
        accessTokenModel.redeemed = false;
        accessTokenModel.expireTime = 600L;
        accessTokenModel.scopes = StringUtils.join(Arrays.asList("OPENID"), ",");
        accessTokenModel.token = validToken;
        AccessToken accessToken = createAccessTokenFromModel(accessTokenModel);

        Mockito.when(accessTokenDao.findByToken(validToken)).thenReturn(accessTokenModel);

        AccessToken result = clientService.getAccessTokenByAccessTokenString(validToken);

        Assert.assertTrue(accessTokenIsEquals(accessToken, result), "Access token not equals");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void findAccessTokenByAccessTokenInvalidTokenThrowsNullPointerException() {
        String invalidToken = "THIS_IS_NOT_ACCESS_TOKEN";

        Mockito.when(accessTokenDao.findByToken(invalidToken)).thenReturn(null);

        AccessToken result = clientService.getAccessTokenByAccessTokenString(invalidToken);

        Assert.fail("Unreachable");
    }

    @Test
    public void findAccessTokenByCodeValidCodeReturnsAccessToken() {
        String validCode = "THIS_IS_AUTHORIZATION_CODE";

        AccessTokenModel accessTokenModel = new AccessTokenModel();
        accessTokenModel.id = 1L;
        accessTokenModel.code = validCode;
        accessTokenModel.clientJid = "JIDC1010";
        accessTokenModel.userJid = "JIDU0101";
        accessTokenModel.redeemed = false;
        accessTokenModel.expireTime = 600L;
        accessTokenModel.scopes = StringUtils.join(Arrays.asList("OPENID"), ",");
        accessTokenModel.token = "THIS_IS_ACCESS_TOKEN";
        AccessToken accessToken = createAccessTokenFromModel(accessTokenModel);

        Mockito.when(accessTokenDao.findByCode(validCode)).thenReturn(accessTokenModel);

        AccessToken result = clientService.getAccessTokenByAuthCode(validCode);

        Assert.assertTrue(accessTokenIsEquals(accessToken, result), "Access token not equals");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void findAccessTokenByCodeInvalidCodeThrowsNullPointerException() {
        String invalidCode = "THIS_IS_INVALID_AUTHORIZATION_CODE";

        Mockito.when(accessTokenDao.findByCode(invalidCode)).thenReturn(null);

        AccessToken result = clientService.getAccessTokenByAuthCode(invalidCode);

        Assert.fail("Unreachable");
    }

    @Test
    public void findRefreshTokenByRefreshTokenValidRefreshTokenReturnsRefreshToken() {
        String validRefreshToken = "THIS_IS_REFRESH_TOKEN";

        RefreshTokenModel refreshTokenModel = new RefreshTokenModel();
        refreshTokenModel.id = 1L;
        refreshTokenModel.code = "THIS_IS_AUTHORIZATION_CODE";
        refreshTokenModel.clientJid = "JIDC1010";
        refreshTokenModel.userJid = "JIDU0101";
        refreshTokenModel.redeemed = false;
        refreshTokenModel.scopes = StringUtils.join(Arrays.asList("OPENID"), ",");
        refreshTokenModel.token = validRefreshToken;
        RefreshToken refreshToken = createRefreshTokenFromModel(refreshTokenModel);

        Mockito.when(refreshTokenDao.findByToken(validRefreshToken)).thenReturn(refreshTokenModel);

        RefreshToken result = clientService.getRefreshTokenByRefreshTokenString(validRefreshToken);

        Assert.assertTrue(refreshTokenIsEquals(refreshToken, result), "Refresh token not equals");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void findRefreshTokenByRefreshTokenInvalidRefreshTokenThrowsNullPointerException() {
        String invalidRefreshToken = "THIS_IS_NOT_REFRESH_TOKEN";

        Mockito.when(refreshTokenDao.findByToken(invalidRefreshToken)).thenReturn(null);

        RefreshToken result = clientService.getRefreshTokenByRefreshTokenString(invalidRefreshToken);

        Assert.fail("Unreachable");
    }

    @Test
    public void findRefreshTokenByCodeValidCodeReturnsRefreshToken() {
        String validCode = "THIS_IS_AUTHORIZATION_CODE";

        RefreshTokenModel refreshTokenModel = new RefreshTokenModel();
        refreshTokenModel.id = 1L;
        refreshTokenModel.code = validCode;
        refreshTokenModel.clientJid = "JIDC1010";
        refreshTokenModel.userJid = "JID0101";
        refreshTokenModel.redeemed = false;
        refreshTokenModel.scopes = StringUtils.join(Arrays.asList("OPENID"), ",");
        refreshTokenModel.token = "THIS_IS_REFRESH_TOKEN";
        RefreshToken refreshToken = createRefreshTokenFromModel(refreshTokenModel);

        Mockito.when(refreshTokenDao.findByCode(validCode)).thenReturn(refreshTokenModel);

        RefreshToken result = clientService.getRefreshTokenByAuthCode(validCode);

        Assert.assertTrue(refreshTokenIsEquals(refreshToken, result), "Refresh token not equals");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void findRefreshTokenByCodeInvalidCodeThrowsNullPointerException() {
        String invalidCode = "THIS_IS_NOT_AUTHORIZATION_CODE";

        Mockito.when(refreshTokenDao.findByCode(invalidCode)).thenReturn(null);

        RefreshToken result = clientService.getRefreshTokenByAuthCode(invalidCode);

        Assert.fail("Unreachable");
    }

    @Test
    public void findIdTokenByCodeValidCodeReturnsIdToken() {
        String validCode = "THIS_IS_AUTHORIZATION_CODE";

        IdTokenModel idTokenModel = new IdTokenModel();
        idTokenModel.id = 1L;
        idTokenModel.clientJid = "JIDC1010";
        idTokenModel.userJid = "JIDC0101";
        idTokenModel.code = validCode;
        idTokenModel.redeemed = false;
        idTokenModel.token = "THIS_IS_ID_TOKEN";
        IdToken idToken = createIdTokenFromModel(idTokenModel);

        Mockito.when(idTokenDao.findByCode(validCode)).thenReturn(idTokenModel);

        IdToken result = clientService.getIdTokenByAuthCode(validCode);

        Assert.assertTrue(idTokenIsEquals(idToken, result), "Id token not equals");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void findIdTokenByCodeInvalidCodeThrowsNullPointerException() {
        String invalidCode = "THIS_IS_NOT_AUTHORIZATION_CODE";

        Mockito.when(idTokenDao.findByCode(invalidCode)).thenReturn(null);

        IdToken idToken = clientService.getIdTokenByAuthCode(invalidCode);

        Assert.fail("Unreachable");
    }

    @Test
    public void redeemAccessTokenByIdUnredeemedAccessTokenReturnRemainingTime() {
        long tokenId = 1L;

        AccessTokenModel accessTokenModel = new AccessTokenModel();
        accessTokenModel.id = tokenId;
        accessTokenModel.redeemed = false;
        accessTokenModel.expireTime = System.currentTimeMillis() + (60L * 60L * 1L); // one hour from now
        accessTokenModel.timeCreate = System.currentTimeMillis();

        String getClientJid = "JIDC0101";
        String getIpAddress = "10.10.10.10";

        Mockito.when(accessTokenDao.findById(tokenId)).thenReturn(accessTokenModel);
        Mockito.doAnswer(invocation -> {
                AccessTokenModel accessTokenModel1 = (AccessTokenModel) invocation.getArguments()[0];
                editAbstractModel(accessTokenModel1, invocation);

                return null;
            }).when(accessTokenDao).edit(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        long remainingTime = clientService.redeemAccessTokenById(tokenId, getClientJid, getIpAddress);

        Assert.assertTrue(remainingTime <= (60L * 60L * 1L), "Remaining cannot greater than 3600");
        Assert.assertTrue(accessTokenModel.redeemed, "Access token has not been redeemed");
        Assert.assertTrue(accessTokenModel.timeUpdate >= accessTokenModel.timeCreate, "Time update not updated");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void redeemAccessTokenByIdRedeemedAccessTokenThrowsRuntimeException() {
        long tokenId = 1L;

        AccessTokenModel accessTokenModel = new AccessTokenModel();
        accessTokenModel.id = tokenId;
        accessTokenModel.redeemed = true;
        accessTokenModel.expireTime = System.currentTimeMillis() + (60L * 60L * 1L); // one hour from now

        String getClientJid = "JIDC0101";
        String getIpAddress = "10.10.10.10";

        Mockito.when(accessTokenDao.findById(tokenId)).thenReturn(accessTokenModel);

        long remainingTime = clientService.redeemAccessTokenById(tokenId, getClientJid, getIpAddress);

        Assert.fail("Unreachable");
    }

    @Test
    public void redeemRefreshTokenByIdUnredeemedRefreshTokenRefreshTokenRedeemed() {
        long tokenId = 1L;

        RefreshTokenModel refreshTokenModel = new RefreshTokenModel();
        refreshTokenModel.id = tokenId;
        refreshTokenModel.redeemed = false;
        refreshTokenModel.timeCreate = System.currentTimeMillis();

        String getClientJid = "JIDC0101";
        String getIpAddress = "10.10.10.10";

        Mockito.when(refreshTokenDao.findById(tokenId)).thenReturn(refreshTokenModel);
        Mockito.doAnswer(invocation -> {
                RefreshTokenModel refreshTokenModel1 = (RefreshTokenModel) invocation.getArguments()[0];
                editAbstractModel(refreshTokenModel1, invocation);

                return null;
            }).when(refreshTokenDao).edit(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        clientService.redeemRefreshTokenById(tokenId, getClientJid, getIpAddress);

        Assert.assertTrue(refreshTokenModel.redeemed, "Refresh token has not been redeemed");
        Assert.assertTrue(refreshTokenModel.timeUpdate >= refreshTokenModel.timeCreate, "Time update not updated");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void redeemRefreshTokenByIdRedeemedRefreshTokenThrowsRuntimeException() {
        long tokenId = 1L;

        RefreshTokenModel refreshTokenModel = new RefreshTokenModel();
        refreshTokenModel.id = tokenId;
        refreshTokenModel.redeemed = true;

        String getClientJid = "JIDC0101";
        String getIpAddress = "10.10.10.10";

        Mockito.when(refreshTokenDao.findById(tokenId)).thenReturn(refreshTokenModel);

        clientService.redeemRefreshTokenById(tokenId, getClientJid, getIpAddress);

        Assert.fail("Unreachable");
    }

    @Test
    public void redeemIdTokenByIdUnredeemedIdTokenIdTokenRedeemed() {
        long tokenId = 1L;

        IdTokenModel idTokenModel = new IdTokenModel();
        idTokenModel.id = tokenId;
        idTokenModel.redeemed = false;
        idTokenModel.timeCreate = System.currentTimeMillis();

        String getClientJid = "JIDC0101";
        String getIpAddress = "10.10.10.10";

        Mockito.when(idTokenDao.findById(tokenId)).thenReturn(idTokenModel);
        Mockito.doAnswer(invocation -> {
                IdTokenModel idTokenModel1 = (IdTokenModel) invocation.getArguments()[0];
                editAbstractModel(idTokenModel1, invocation);

                return null;
            }).when(idTokenDao).edit(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        clientService.redeemIdTokenById(tokenId, getClientJid, getIpAddress);

        Assert.assertTrue(idTokenModel.redeemed, "Id token has not been redeemed");
        Assert.assertTrue(idTokenModel.timeUpdate >= idTokenModel.timeCreate, "Time update not updated");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void redeemIdTokenByIdRedeemedIdTokenThrowsRuntimeException() {
        long tokenId = 1L;

        IdTokenModel idTokenModel = new IdTokenModel();
        idTokenModel.id = 1L;
        idTokenModel.redeemed = true;

        String getClientJid = "JIDC0101";
        String getIpAddress = "10.10.10.10";

        Mockito.when(idTokenDao.findById(tokenId)).thenReturn(idTokenModel);

        clientService.redeemIdTokenById(tokenId, getClientJid, getIpAddress);

        Assert.fail("Unreachable");
    }

    @Test
    public void createClientCreateClientParametersClientPersisted() {
        String name = "Alice";
        String applicationType = "Web Server";
        List<String> scopes = Arrays.asList("OPENID", "OFFLINE_ACCESS");
        List<String> redirectURIs = Arrays.asList("http://alice.com/verify");

        String clientJid = "JIDC1010";
        String newSecret = "THIS_IS_NEW_SECRET";

        String getUserJid = "JIDU0101";
        String getIpAddress = "10.10.10.10";

        ClientModel clientModel = new ClientModel();

        Mockito.doAnswer(invocation -> {
                ClientModel clientModel1 = (ClientModel) invocation.getArguments()[0];
                persistAbstractModel(clientModel, invocation);

                clientModel.jid = clientJid;
                clientModel.name = clientModel1.name;
                clientModel.secret = clientModel1.secret;
                clientModel.applicationType = clientModel1.applicationType;
                clientModel.scopes = clientModel1.scopes;

                return null;
            }).when(clientDao).persist(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        clientService.createClient(name, applicationType, scopes, redirectURIs, getUserJid, getIpAddress);

        Assert.assertEquals(clientJid, clientModel.jid, "Client model JID mismatch");
        Mockito.verify(redirectURIDao, Mockito.times(redirectURIs.size())).persist(Mockito.any(), Mockito.anyString(), Mockito.anyString());
        Assert.assertNotNull(clientModel.userCreate, "UserInfo create must not be null");
        Assert.assertNotNull(clientModel.ipCreate, "IP create must not be null");
    }

    @Test
    public void updateClientUpdateClientParametersClientUpdatePersisted() throws ClientNotFoundException {
        long clientId = 1L;
        String clientJid = "JIDC0101";
        String name = "Bob";
        List<String> scopes = Arrays.asList("OPENID");
        String scopesString = StringUtils.join(scopes, ",");
        List<String> redirectURIs = Arrays.asList("http://bob.com/verify", "http://bobu.com/verify");

        String getUserJid = "JIDU0101";
        String getIpAddress = "10.10.10.10";

        ClientModel clientModel = new ClientModel();
        clientModel.jid = clientJid;
        clientModel.id = clientId;
        clientModel.name = "Alice";
        clientModel.scopes = StringUtils.join(Arrays.asList("OPENID", "OFFLINE_ACCESS"), ",");
        clientModel.timeCreate = System.currentTimeMillis();

        List<String> oldRedirectUris = Arrays.asList("http://alice.com/verify");
        List<RedirectURIModel> oldRedirectURIModels = createRedirectURIModel(1L, oldRedirectUris, clientModel.jid);

        Mockito.when(clientDao.findByJid(clientJid)).thenReturn(clientModel);
        Mockito.when(redirectURIDao.getByClientJid(clientModel.jid)).thenReturn(oldRedirectURIModels);
        Mockito.doAnswer(invocation -> {
                ClientModel clientModel1 = (ClientModel) invocation.getArguments()[0];
                editAbstractModel(clientModel1, invocation);

                return null;
            }).when(clientDao).edit(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        clientService.updateClient(clientJid, name, scopes, redirectURIs, getUserJid, getIpAddress);

        Assert.assertEquals(name, clientModel.name, "Client name not changed");
        Assert.assertEquals(scopesString, clientModel.scopes, "Client scopes not changed");
        Mockito.verify(redirectURIDao, Mockito.times(oldRedirectURIModels.size())).remove(Mockito.any());
        Mockito.verify(redirectURIDao, Mockito.times(redirectURIs.size())).persist(Mockito.any(), Mockito.anyString(), Mockito.anyString());
        Assert.assertTrue(clientModel.timeUpdate >= clientModel.timeCreate, "Time update not updated");
    }

    @Test
    public void deleteClientExistingClientExistingClientRemoved() throws ClientNotFoundException {
        String clientJid = "JIDC0101";

        ClientModel clientModel = new ClientModel();

        Mockito.when(clientDao.findByJid(clientJid)).thenReturn(clientModel);

        clientService.deleteClient(clientJid);

        Mockito.verify(clientDao, Mockito.times(1)).remove(clientModel);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void deleteClientNonExistingClientThrowsClientNotFoundException() throws IllegalArgumentException {
        String clientJid = "JIDC0101";

        Mockito.when(clientDao.findByJid(clientJid)).thenReturn(null);
        Mockito.doThrow(IllegalArgumentException.class).when(clientDao).remove(null);

        clientService.deleteClient(clientJid);

        Assert.fail("Unreachable");
    }

    @Test
    public void pageClientsPageClientsParameterReturnsPagedClients() {
        long pageIndex = 0L;
        long pageSize = 10L;
        String orderBy = "id";
        String orderDir = "asc";
        String filterString = "";

        ClientModel firstClientModel = new ClientModel();
        firstClientModel.id = 1L;
        firstClientModel.jid = "JIDC0001";
        firstClientModel.name = "Client 1";
        firstClientModel.secret = "CLIENT_1_SECRET";
        firstClientModel.applicationType = "Web Server";
        firstClientModel.scopes = "OPENID";
        ClientModel secondClientModel = new ClientModel();
        secondClientModel.id = 2L;
        secondClientModel.jid = "JIDC0002";
        secondClientModel.name = "Client 2";
        secondClientModel.secret = "CLIENT_2_SECRET";
        secondClientModel.applicationType = "Web Server";
        secondClientModel.scopes = "OPENID,OFFLINE_ACCESS";
        List<ClientModel> clientModels = Arrays.asList(firstClientModel, secondClientModel);
        Mockito.when(clientDao.findSortedByFilters(Mockito.eq(orderBy), Mockito.eq(orderDir), Mockito.eq(filterString), Mockito.eq(pageIndex * pageSize), Mockito.eq(pageSize)))
                .thenReturn(clientModels);

        long totalRows = clientModels.size();
        Mockito.when(clientDao.countByFilters(Mockito.eq(filterString))).thenReturn(totalRows);

        Page<Client> clientPage = clientService.getPageOfClients(pageIndex, pageSize, orderBy, orderDir, filterString);

        Assert.assertNotNull(clientPage.getData(), "Page data must not be null");
        Assert.assertEquals(clientModels.size(), clientPage.getTotalRowsCount(), "Page total rows count not equal");
    }

    @Test
    public void pageClientsOtherPageClientsParameterReturnsEmptyPagedClient() {
        long pageIndex = 5L;
        long pageSize = 10L;
        String orderBy = "id";
        String orderDir = "asc";
        String filterString = "asdfasdf";

        List<ClientModel> clientModels = ImmutableList.of();
        Mockito.when(clientDao.findSortedByFilters(Mockito.eq(orderBy), Mockito.eq(orderDir), Mockito.eq(filterString), Matchers.<Map<SingularAttribute<? super ClientModel, ? extends Object>, String>>any(), Matchers.<Map<SingularAttribute<? super ClientModel, String>, Set<String>>>any(), Mockito.eq(pageIndex * pageSize), Mockito.eq(pageSize)))
                .thenReturn(clientModels);

        long totalRows = clientModels.size();
        Mockito.when(clientDao.countByFilters(Mockito.eq(filterString), Matchers.<Map<SingularAttribute<? super ClientModel, ? extends Object>, String>>any(), Matchers.<Map<SingularAttribute<? super ClientModel, String>, Set<String>>>any())).thenReturn(totalRows);

        Page<Client> clientPage = clientService.getPageOfClients(pageIndex, pageSize, orderBy, orderDir, filterString);

        Assert.assertNotNull(clientPage.getData(), "Page data must not be null");
        Assert.assertEquals(clientModels.size(), clientPage.getTotalRowsCount(), "Page total rows count not equal");
    }

    private boolean idTokenIsEquals(IdToken a, IdToken b) {
        EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(a.getId(), b.getId());
        equalsBuilder.append(a.getClientJid(), b.getClientJid());
        equalsBuilder.append(a.getUserJid(), b.getUserJid());
        equalsBuilder.append(a.getCode(), b.getCode());
        equalsBuilder.append(a.isRedeemed(), b.isRedeemed());
        equalsBuilder.append(a.getToken(), b.getToken());

        return equalsBuilder.isEquals();
    }

    private boolean refreshTokenIsEquals(RefreshToken a, RefreshToken b) {
        EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(a.getId(), b.getId());
        equalsBuilder.append(a.getCode(), b.getCode());
        equalsBuilder.append(a.getClientJid(), b.getClientJid());
        equalsBuilder.append(a.getUserJid(), b.getUserJid());
        equalsBuilder.append(a.isRedeemed(), b.isRedeemed());
        equalsBuilder.append(a.getScopes(), b.getScopes());
        equalsBuilder.append(a.getToken(), b.getToken());

        return equalsBuilder.isEquals();
    }

    private boolean accessTokenIsEquals(AccessToken a, AccessToken b) {
        EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(a.getId(), b.getId());
        equalsBuilder.append(a.getCode(), b.getCode());
        equalsBuilder.append(a.getClientJid(), b.getClientJid());
        equalsBuilder.append(a.getUserJid(), b.getUserJid());
        equalsBuilder.append(a.isRedeemed(), b.isRedeemed());
        equalsBuilder.append(a.getExpireTime(), b.getExpireTime());
        equalsBuilder.append(a.getScopes(), b.getScopes());
        equalsBuilder.append(a.getToken(), b.getToken());

        return equalsBuilder.isEquals();
    }

    private boolean authorizationCodeIsEquals(org.iatoki.judgels.jophiel.oauth2.AuthorizationCode a, org.iatoki.judgels.jophiel.oauth2.AuthorizationCode b) {
        EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(a.getId(), b.getId());
        equalsBuilder.append(a.getClientJid(), b.getClientJid());
        equalsBuilder.append(a.getUserJid(), b.getUserJid());
        equalsBuilder.append(a.getCode(), b.getCode());
        equalsBuilder.append(a.getExpireTime(), b.getExpireTime());
        equalsBuilder.append(a.getRedirectURI(), b.getRedirectURI());
        equalsBuilder.append(a.getScopes(), b.getScopes());

        return equalsBuilder.isEquals();
    }

    private void editAbstractModel(AbstractModel abstractModel, InvocationOnMock invocation) {
        String userJid = (String) invocation.getArguments()[1];
        String ipAddress = (String) invocation.getArguments()[2];

        abstractModel.timeUpdate = System.currentTimeMillis();
        abstractModel.userUpdate = userJid;
        abstractModel.ipUpdate = ipAddress;
    }

    private void persistAbstractModel(AbstractModel abstractModel, InvocationOnMock invocation) {
        String userJid = (String) invocation.getArguments()[1];
        String ipAddress = (String) invocation.getArguments()[2];

        abstractModel.timeCreate = System.currentTimeMillis();
        abstractModel.userCreate = userJid;
        abstractModel.ipCreate = ipAddress;
        abstractModel.timeUpdate = abstractModel.timeCreate;
        abstractModel.userUpdate = userJid;
        abstractModel.ipUpdate = ipAddress;
    }

    private void assertClientListEquals(List<Client> expected, List<Client> result) {
        Assert.assertEquals(expected.size(), result.size(), "Result size not equal to expected size");
        Assert.assertTrue(IntStream.range(0, expected.size())
                .mapToObj(i -> clientIsEquals(expected.get(i), result.get(i)))
                .allMatch(b -> b), "Some element are not equal");
    }

    private boolean clientIsEquals(Client a, Client b) {
        EqualsBuilder eb = new EqualsBuilder();
        eb.append(a.getId(), b.getId());
        eb.append(a.getJid(), b.getJid());
        eb.append(a.getName(), b.getName());
        eb.append(a.getSecret(), b.getSecret());
        eb.append(a.getApplicationType(), b.getApplicationType());
        eb.append(a.getScopes(), b.getScopes());
        eb.append(a.getRedirectURIs(), b.getRedirectURIs());

        return eb.isEquals();
    }

    private ClientModel createClientModelFromClient(Client client) {
        ClientModel clientModel = new ClientModel();
        clientModel.id = client.getId();
        clientModel.jid = client.getJid();
        clientModel.name = client.getName();
        clientModel.applicationType = client.getApplicationType();
        clientModel.secret = client.getSecret();
        clientModel.scopes = StringUtils.join(client.getScopes(), ",");

        return clientModel;
    }

    private void mockRedirectURIDaoFromClients(List<Client> clients) {
        long redirectURIId = 1L;
        for (Client client : clients) {
            Mockito.when(redirectURIDao.getByClientJid(client.getJid())).thenReturn(createRedirectURIModel(redirectURIId, client.getRedirectURIs(), client.getJid()));
            redirectURIId += client.getRedirectURIs().size();
        }
    }

    private List<RedirectURIModel> createRedirectURIModel(long id, List<String> redirectUris, String jid) {
        List<RedirectURIModel> redirectURIModelList = IntStream.range(0, redirectUris.size()).mapToObj(i -> {
                RedirectURIModel redirectURIModel = new RedirectURIModel();
                redirectURIModel.id = id + i;
                redirectURIModel.redirectURI = redirectUris.get(i);
                redirectURIModel.clientJid = jid;
                return redirectURIModel;
            }).collect(Collectors.toList());

        return redirectURIModelList;
    }


    private Client createClientFromModel(ClientModel clientModel, Set<String> scopeString, List<String> redirectURIs) {
        return new Client(clientModel.id, clientModel.jid, clientModel.name, clientModel.secret, clientModel.applicationType, scopeString, redirectURIs);
    }

    private org.iatoki.judgels.jophiel.oauth2.AuthorizationCode createAuthorizationCodeFromModel(AuthorizationCodeModel authorizationCodeModel) {
        return new org.iatoki.judgels.jophiel.oauth2.AuthorizationCode(authorizationCodeModel.id, authorizationCodeModel.userJid, authorizationCodeModel.clientJid, authorizationCodeModel.code, authorizationCodeModel.redirectURI, authorizationCodeModel.expireTime, authorizationCodeModel.scopes);
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
