package org.iatoki.judgels.jophiel.services.impls;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.JudgelsUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.models.domains.AbstractModel;
import org.iatoki.judgels.jophiel.JophielProperties;
import org.iatoki.judgels.jophiel.commons.exceptions.ClientNotFoundException;
import org.iatoki.judgels.jophiel.commons.plains.AccessToken;
import org.iatoki.judgels.jophiel.commons.plains.Client;
import org.iatoki.judgels.jophiel.commons.plains.IdToken;
import org.iatoki.judgels.jophiel.commons.plains.RefreshToken;
import org.iatoki.judgels.jophiel.models.daos.AccessTokenDao;
import org.iatoki.judgels.jophiel.models.daos.AuthorizationCodeDao;
import org.iatoki.judgels.jophiel.models.daos.ClientDao;
import org.iatoki.judgels.jophiel.models.daos.IdTokenDao;
import org.iatoki.judgels.jophiel.models.daos.RedirectURIDao;
import org.iatoki.judgels.jophiel.models.daos.RefreshTokenDao;
import org.iatoki.judgels.jophiel.models.domains.AccessTokenModel;
import org.iatoki.judgels.jophiel.models.domains.AuthorizationCodeModel;
import org.iatoki.judgels.jophiel.models.domains.ClientModel;
import org.iatoki.judgels.jophiel.models.domains.IdTokenModel;
import org.iatoki.judgels.jophiel.models.domains.RedirectURIModel;
import org.iatoki.judgels.jophiel.models.domains.RefreshTokenModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by bagus.seto on 5/25/2015.
 */
@PrepareForTest({IdentityUtils.class, JophielProperties.class, JudgelsUtils.class})
public class ClientServiceImplTest extends PowerMockTestCase {
    @Mock
    ClientDao clientDao;
    @Mock
    RedirectURIDao redirectURIDao;
    @Mock
    AuthorizationCodeDao authorizationCodeDao;
    @Mock
    AccessTokenDao accessTokenDao;
    @Mock
    RefreshTokenDao refreshTokenDao;
    @Mock
    IdTokenDao idTokenDao;

    @InjectMocks
    ClientServiceImpl clientService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(IdentityUtils.class);
    }

    @Test
    public void findAll_EmptyData_ReturnsEmptyList() {
        List<Client> result = clientService.findAll();
        Assert.assertEquals(Collections.emptyList(), result, "Result should be an empty list");
    }

    @Test
    public void findAll_SomeData_ReturnsTheSameList() {
        List<Client> expected = Arrays.asList(
                new Client(1L, "JID0001", "Client 1", "secret1", "Web Server", Arrays.asList("OPENID").stream().collect(Collectors.toSet()), Arrays.asList("http://client1/verify")),
                new Client(2L, "JID0002", "Client 2", "secret2", "Web Server", Arrays.asList("OPENID").stream().collect(Collectors.toSet()), Arrays.asList("http://client2/verify"))
        );

        List<ClientModel> clientDaoFindAllResult = expected.stream().map(client ->
                createClientModelFromClient(client))
                .collect(Collectors.toList());
        Mockito.when(clientDao.findAll()).thenReturn(clientDaoFindAllResult);

        mockRedirectURIDaoFromClients(expected);

        List<Client> result = clientService.findAll();
        assertClientListEquals(expected, result);
    }

    @Test
    public void findAllClientByTerm_EmptyStringTerm_ReturnsAllClient() {
        String emptyStringTerm = "";
        List<Client> expected = Arrays.asList(
                new Client(1L, "JID0001", "Client 1", "secret1", "Web Server", Arrays.asList("OPENID").stream().collect(Collectors.toSet()), Arrays.asList("http://client1/verify")),
                new Client(2L, "JID0002", "Client 2", "secret2", "Web Server", Arrays.asList("OPENID").stream().collect(Collectors.toSet()), Arrays.asList("http://client2/verify"))
        );

        List<ClientModel> clientDaoFindAllClientByTermResult = expected.stream().map(client ->
                createClientModelFromClient(client))
                .collect(Collectors.toList());
        Mockito.when(clientDao.findSortedByFilters(Mockito.anyString(), Mockito.anyString(), Mockito.eq(emptyStringTerm), Mockito.anyMap(), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(clientDaoFindAllClientByTermResult);

        mockRedirectURIDaoFromClients(expected);

        List<Client> result = clientService.findAllClientByTerm(emptyStringTerm);
        assertClientListEquals(expected, result);
    }

    @Test
    public void findAllClientByTerm_SomeTermNotContainedInClients_ReturnsEmptyList() {
        String randomTerm = "asdfasdf";
        Mockito.when(clientDao.findSortedByFilters(Mockito.anyString(), Mockito.anyString(), Mockito.eq(randomTerm), Mockito.anyMap(), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(Collections.emptyList());

        List<Client> result = clientService.findAllClientByTerm(randomTerm);
        Assert.assertEquals(Collections.emptyList(), result, "Result should be an empty list");
    }

    @Test
    public void findAllClientByTerm_SomeTermContainedInClients_ReturnsAllClientsThatMatch() {
        String containedTerm = "OPENID";
        List<Client> expected = Arrays.asList(
                new Client(1L, "JID0001", "Alice", "alice secret", "Web Server", Arrays.asList("OPENID").stream().collect(Collectors.toSet()), Arrays.asList("http://alice.com/verify")),
                new Client(2L, "JID0002", "Bob", "bob secret", "Web Server", Arrays.asList("OPENID", "OFFLINE_ACCESS").stream().collect(Collectors.toSet()), Arrays.asList("http://bob.com/verify"))
        );

        List<ClientModel> clientModelsFromExpected = expected.stream().map(client ->
                createClientModelFromClient(client))
                .collect(Collectors.toList());
        Mockito.when(clientDao.findSortedByFilters(Mockito.anyString(), Mockito.anyString(), Mockito.eq(containedTerm), Mockito.anyMap(), Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(clientModelsFromExpected);

        mockRedirectURIDaoFromClients(expected);

        List<Client> result = clientService.findAllClientByTerm(containedTerm);
        assertClientListEquals(expected, result);
    }

    @Test
    public void isClientAuthorized_AuthorizedClient_ReturnsTrue() {
        String clientJid1 = "JIDC0001";
        String userJid1 = "JIDU0010";
        List<String> scopes1 = Arrays.asList("OPENID");
        String scopes1String = StringUtils.join(scopes1, ",");

        Mockito.when(IdentityUtils.getUserJid()).thenReturn(userJid1);
        Mockito.when(authorizationCodeDao.checkIfAuthorized(clientJid1, userJid1, scopes1String)).thenReturn(true);

        Assert.assertTrue(clientService.isClientAuthorized(clientJid1, scopes1), "Client is not authorized");
    }

    @Test
    public void isClientAuthorized_UnauthorizedClient_ReturnsFalse() {
        String clientJid1 = "JIDC0000";
        String userJid1 = "JIDU0100";
        List<String> scopes1 = Arrays.asList("OPENID");
        String scopes1String = StringUtils.join(scopes1, ",");

        Mockito.when(IdentityUtils.getUserJid()).thenReturn(userJid1);
        Mockito.when(authorizationCodeDao.checkIfAuthorized(clientJid1, userJid1, scopes1String)).thenReturn(false);

        Assert.assertFalse(clientService.isClientAuthorized(clientJid1, scopes1), "Client is authorized");
    }

    @Test
    public void isValidAccessTokenExist_ValidAccessToken_ReturnsTrue() {
        String validAccessToken = "validaccesstoken";
        Mockito.when(accessTokenDao.existsByToken(validAccessToken)).thenReturn(true);

        Assert.assertTrue(clientService.isValidAccessTokenExist(validAccessToken), "Access token invalid");
    }

    @Test
    public void isValidAccessTokenExist_InvalidAccessToken_ReturnsFalse() {
        String invalidAccessToken = "invalidaccesstoken";
        Mockito.when(accessTokenDao.existsByToken(invalidAccessToken)).thenReturn(false);

        Assert.assertFalse(clientService.isValidAccessTokenExist(invalidAccessToken), "Access token valid");
    }

    @Test
    public void clientExistByClientJid_ExistingClientJid_ReturnsTrue() {
        String existingClientJid = "JIDC1234";
        Mockito.when(clientDao.existsByJid(existingClientJid)).thenReturn(true);

        Assert.assertTrue(clientService.clientExistByClientJid(existingClientJid), "Client JID not exist");
    }

    @Test
    public void clientExistByClientJid_NonExistingClientJid_ReturnsFalse() {
        String nonExistingClientJid = "JIDCABCDEF";
        Mockito.when(clientDao.existsByJid(nonExistingClientJid)).thenReturn(false);

        Assert.assertFalse(clientService.clientExistByClientJid(nonExistingClientJid), "Client JID exist");
    }

    @Test
    public void clientExistByClientName_ExistingClientName_ReturnsTrue() {
        String existingClientName = "Alice";
        Mockito.when(clientDao.existByName(existingClientName)).thenReturn(true);

        Assert.assertTrue(clientService.clientExistByClientName(existingClientName), "Client name not exist");
    }

    @Test
    public void clientExistByClientName_NonExistingClientName_ReturnsFalse() {
        String nonExistingClientName = "Not Alice";
        Mockito.when(clientDao.existByName(nonExistingClientName)).thenReturn(false);

        Assert.assertFalse(clientService.clientExistByClientName(nonExistingClientName), "Client name not exist");
    }

    @Test
    public void findClientById_ExistingClientId_ReturnsClient() throws ClientNotFoundException {
        long existingClientId = 10L;
        Client existingClient = new Client(existingClientId, "JIDC1010", "Alice", "alice secret", "Web Server", Arrays.asList("OPENID").stream().collect(Collectors.toSet()), Arrays.asList("http://alice.com/verify"));

        ClientModel clientModelFromExistingClient = createClientModelFromClient(existingClient);
        Mockito.when(clientDao.findById(existingClientId)).thenReturn(clientModelFromExistingClient);

        List<RedirectURIModel> redirectURIModelsFromExistingClient = createRedirectURIModel(1L, existingClient.getRedirectURIs(), existingClient.getJid());
        Mockito.when(redirectURIDao.findByClientJid(existingClient.getJid())).thenReturn(redirectURIModelsFromExistingClient);

        Client result = clientService.findClientById(existingClientId);
        Assert.assertTrue(clientIsEquals(existingClient, result), "Client find by Id not equal");
    }

    @Test(expectedExceptions = ClientNotFoundException.class)
    public void findClientById_NonExistingClientId_ThrowsClientNotFoundException() throws ClientNotFoundException {
        long nonExistingClientId = -10000L;

        Mockito.when(clientDao.findById(nonExistingClientId)).thenReturn(null);

        Client result = clientService.findClientById(nonExistingClientId);
        Assert.fail("Unreachable");
    }

    @Test
    public void findClientByJid_ExistingClientJid_ReturnsClient() {
        String existingClientJid = "JIDC1010";
        Client existingClient = new Client(10L, existingClientJid, "Alice", "alice secret", "Web Server", Arrays.asList("OPENID").stream().collect(Collectors.toSet()), Arrays.asList("http://alice.com/verify"));

        ClientModel clientModelFromExistingClient = createClientModelFromClient(existingClient);
        Mockito.when(clientDao.findByJid(existingClientJid)).thenReturn(clientModelFromExistingClient);

        List<RedirectURIModel> redirectUriModelsFromExistingClient = createRedirectURIModel(1L, existingClient.getRedirectURIs(), existingClient.getJid());
        Mockito.when(redirectURIDao.findByClientJid(existingClient.getJid())).thenReturn(redirectUriModelsFromExistingClient);

        Client result = clientService.findClientByJid(existingClientJid);
        Assert.assertTrue(clientIsEquals(existingClient, result), "Client find by Jid not equal");
    }

    @Test(expectedExceptions = ClientNotFoundException.class)
    public void findClientByJid_NonExistingClientJid_ThrowsClientNotFoundException() throws ClientNotFoundException {
        String nonExistingClientJid = "JIDC9999";

        Mockito.when(clientDao.findByJid(nonExistingClientJid)).thenReturn(null);

        Client result = clientService.findClientByJid(nonExistingClientJid);
        Assert.fail("Unreachable");
    }

    @Test
    public void generateAuthorizationCode_ValidClient_ReturnsAuthorizationCode() {
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
        Mockito.when(redirectURIDao.findByClientJid(validClient.getJid())).thenReturn(redirectURIModelsFromValidClient);

        Mockito.when(IdentityUtils.getUserJid()).thenReturn(getUserJid);
        Mockito.when(IdentityUtils.getIpAddress()).thenReturn(getIpAddress);

        AuthorizationCodeModel authorizationCodeModel = new AuthorizationCodeModel();

        Mockito.doAnswer(invocation -> {
            AbstractModel acm = authorizationCodeModel;

            persistAbstractModel(acm, invocation);

            return null;
        }).when(authorizationCodeDao).persist(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        AuthorizationCode authorizationCode = clientService.generateAuthorizationCode(clientJid, redirectURI, responseType, scopes, expireTime);

        Assert.assertNotNull(authorizationCode, "Authorization code null");
        Assert.assertNotNull(authorizationCodeModel.userCreate, "User Create must not be null");
        Assert.assertNotNull(authorizationCodeModel.ipCreate, "IP Create must not be null");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void generateAuthorizationCode_InvalidClient_ThrowsIllegalStateException() {
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
        Mockito.when(redirectURIDao.findByClientJid(validClient.getJid())).thenReturn(redirectURIModelsFromClient);

        Mockito.when(IdentityUtils.getUserJid()).thenReturn(getUserJid);
        Mockito.when(IdentityUtils.getIpAddress()).thenReturn(getIpAddress);

        AuthorizationCode authorizationCode = clientService.generateAuthorizationCode(clientJid, redirectUri, responseType, scopes, expireTime);

        Assert.fail("Unreachable");
    }

    @Test
    public void generateAccessToken_ValidAuthorizationCode_ReturnsAccessToken() {
        String code = "THIS_IS_AUTHORIZATION_CODE";
        String userId = "JIDU0101";
        String clientId = "JIDC1010";
        List<String> scopes = Arrays.asList("OPENID");
        long expireTime = 600L;

        String getUserJid = userId;
        String getIpAddress = "10.10.10.10";

        Mockito.when(IdentityUtils.getUserJid()).thenReturn(getUserJid);
        Mockito.when(IdentityUtils.getIpAddress()).thenReturn(getIpAddress);

        AccessTokenModel accessTokenModel = new AccessTokenModel();

        Mockito.doAnswer(invocation -> {
            AbstractModel atm = accessTokenModel;

            persistAbstractModel(atm, invocation);

            return null;
        }).when(accessTokenDao).persist(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        String token = clientService.generateAccessToken(code, userId, clientId, scopes, expireTime);

        Assert.assertNotNull(token, "Token can not be null");
        Assert.assertNotNull(accessTokenModel.userCreate, "User create must not be null");
        Assert.assertNotNull(accessTokenModel.ipCreate, "IP create must not be null");
    }

    @Test
    public void generateRefreshToken_ValidAuthorizationCode_RefreshTokenModelPersisted() {
        String code = "THIS_IS_AUTHORIZATION_CODE";
        String userId = "JIDU0101";
        String clientId = "JIDC1010";
        List<String> scopes = Arrays.asList("OPENID");

        String getUserJid = userId;
        String getIpAddress = "10.10.10.10";

        Mockito.when(IdentityUtils.getUserJid()).thenReturn(getUserJid);
        Mockito.when(IdentityUtils.getIpAddress()).thenReturn(getIpAddress);

        RefreshTokenModel refreshTokenModel = new RefreshTokenModel();

        Mockito.doAnswer(invocation -> {
            AbstractModel rtm = refreshTokenModel;

            persistAbstractModel(rtm, invocation);

            return null;
        }).when(refreshTokenDao).persist(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        clientService.generateRefreshToken(code, userId, clientId, scopes);

        Assert.assertNotNull(refreshTokenModel.userCreate, "User Create must not be null");
        Assert.assertNotNull(refreshTokenModel.ipCreate, "IP Create must not be null");
    }

    @Test
    public void generateIdToken_ValidAccessToken_IdTokenModelPersisted() throws UnsupportedEncodingException {
        String code = "THIS_IS_AUTHORIZATION_CODE";
        String userId = "JIDU0101";
        String clientId = "JIDC1010";
        String nonce = "THIS_IS_NONCE";
        long authTime = 0L;
        String accessToken = "THIS_IS_ACCESS_TOKEN";
        long expireTime = 600L;

        String idTokenPrivateKey = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDCdoHMwrsIiggV6hp7Yf4FZaKqkAeHuk5WAbBzuIDB40gQKKimwfKk+yaR6UKOOduGM3k4eDbaZy3n8NCkWnAvVIwt4rus7LhDhVUNrJGQU9BdK59x+wvhUtMcE2eP0V3hjeJmqzhoJxqLIAcnksU2Z3mmAkgbXecV16fCgo8G1Ny+Ai+FY2ZefRK+LF0u9rGQx5tA6XuQOUWvPJb45YlzmEDLwEMw7nOqwnnN6mSj9cKVfDX33ayvZY0aenEn7SMtrAkia5gBKGKDfN2KECX6OD9joatmNW0b+z9RtAXJvrWtkXhGaZR9+YBLBITllAtgkWMLWCCnDDOM4lNLoj9XAgMBAAECggEACPCz1Psa6DCYYJGLuCJwMEVU7iyC/B13noKjXx6bZM6TMJL99fSyuB0Hz+t+cNV+HzRcnVkBhJb7yE8M+JFj2Pk1HKLw5+lWK1yE5YUKiC0iRjZMNUxKZoiNRhwqRbVlcIo6X2f9xuQNV1oYmhwoTvEA6b3vHLr7dcidYNbpxnGMQZs035um6zShIFNqrmM4poQZZE9NbltOX1k/qxD0+OAAuemU3Y7WzH1XvTwXy7qU8O0PCktTe+QBSJZUPxy1nZwKbF1vdad39KfCjvxemkdUdzuPvlMfi+dsDXjAz71ukUO0r1+4n+l9DYOI8Pq6oI5ZGcwmz5B/Fd8RpPb2gQKBgQDy1o9HCnkL4rw3Wg6hkM46dlPPT7Mm5p+GrNbRxd6bX0wRpXivcasT60u4UZnG7gVVjpqour6tbyRaVNr5F6Cxg6YXDnZKwa8Jz64oUduQqMw7FvGtBG8+NR/26wI53Xoe1nq50ugkq3V3l9TtW9p0ccrsELP7Nu6Fmd4aa9AMFwKBgQDNALqptObo+2jODiuU4+w4wt/hUZa0BbmhjkhJNVpczZvUlXkLtMCq1ESxH4wWzRpBvIlcWpKnSyxzuFD5rtjqHh1kqVbFjQ2k0hRGs5S2vT+aC5oTH4M92nRPCZbWq+26jSVcvAgFj+S6MSOofMDYVOfM3dEKhzNKVsChjGsuwQKBgBCccrKWWc9hVCSpKWUN5b2ECJmexw97KSBqREuXMHIKY8a1PfsqWFyFdOmH03ATKhQ/K/8svwxYFPGE6nGtlxVtfvgGyjq04wdVyIEDkHRlx4qnOCLwsbdcpPIcA0v4BXmEjGKXtb+EZwWmQi92YAwlGI9rWRRvHoPPEa1XAKVDAoGALWgf8D71dl1ZVWqmFJB3Xgsr84hSzQUHnNUbBbwfi7au8WM6MHGUy0HBBUpriRFc43qTIjWdjhiEfA0zQlqMCS8qa4VmhtM7VmqBuzdDlUZNtB0lv16XfzfH00nYcywZt9xTjjrHvBOnIeaIc2VOgZwsy5/GEYLoxWp5uE6V3wECgYALHhV4lk4bH1Gm2S7Od8yPix62dbwoFMjfFiI4Y3dCu7Um93MS34OSWo2pixb9w+1Y/ZNNfrq+tEhUSsJKd3MvE8oskUR4bo4yMQJZC1+FSNUpehjz1Z9XiqJMpsl9GGYXo+nzU27PwlZdorgd8uiH30sNLcm9VG3e72hbQ0EpmQ==";
        String getUserJid = userId;
        String getIpAddress = "10.10.10.10";

        PowerMockito.mockStatic(JophielProperties.class);
        JophielProperties jophielProperties = Mockito.mock(JophielProperties.class);
        Mockito.when(JophielProperties.getInstance()).thenReturn(jophielProperties);
        Mockito.when(jophielProperties.getIdTokenPrivateKey()).thenReturn(idTokenPrivateKey);
        Mockito.when(jophielProperties.getJophielBaseUrl()).thenReturn("http://jophiel.com");

        Mockito.when(IdentityUtils.getUserJid()).thenReturn(getUserJid);
        Mockito.when(IdentityUtils.getIpAddress()).thenReturn(getIpAddress);

        IdTokenModel idTokenModel = new IdTokenModel();

        Mockito.doAnswer(invocation -> {
            AbstractModel itm = idTokenModel;

            persistAbstractModel(itm, invocation);

            return null;
        }).when(idTokenDao).persist(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        clientService.generateIdToken(code, userId, clientId, nonce, authTime, accessToken, expireTime);

        Assert.assertNotNull(idTokenModel.userCreate, "User Create must not be null");
        Assert.assertNotNull(idTokenModel.ipCreate, "IP Create must not be null");
    }

    @Test
    public void findByAuthorizationCodeByCode_ValidCode_ReturnsAuthorizationCode() {
        String validCode = "THIS_IS_AUTHORIZATION_CODE";

        AuthorizationCodeModel authorizationCodeModel = new AuthorizationCodeModel();
        authorizationCodeModel.id = 1L;
        authorizationCodeModel.clientJid = "JIDC1010";
        authorizationCodeModel.userJid = "JIDU0101";
        authorizationCodeModel.code = validCode;
        authorizationCodeModel.expireTime = 600L;
        authorizationCodeModel.redirectURI = "http://alice.com/verify";
        authorizationCodeModel.scopes = StringUtils.join(Arrays.asList("OPENID"), ",");
        org.iatoki.judgels.jophiel.commons.plains.AuthorizationCode authorizationCode = new org.iatoki.judgels.jophiel.commons.plains.AuthorizationCode(authorizationCodeModel);

        Mockito.when(authorizationCodeDao.findByCode(validCode)).thenReturn(authorizationCodeModel);

        org.iatoki.judgels.jophiel.commons.plains.AuthorizationCode result = clientService.findAuthorizationCodeByCode(validCode);

        Assert.assertTrue(authorizationCodeIsEquals(authorizationCode, result), "Authorization code not equals");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void findByAuthorizationCodeByCode_InvalidCode_ThrowsNullPointerException() {
        String invalidCode = "THIS_IS_NOT_AUTHORIZATION_CODE";

        Mockito.when(authorizationCodeDao.findByCode(invalidCode)).thenReturn(null);

        org.iatoki.judgels.jophiel.commons.plains.AuthorizationCode result = clientService.findAuthorizationCodeByCode(invalidCode);

        Assert.fail("Unreachable");
    }

    @Test
    public void regenerateAccessToken_ValidAuthorizationCode_ReturnsNewAccessToken() {
        String code = "THIS_IS_AUTHORIZATION_CODE";
        String userId = "JIDU0101";
        String clientId = "JIDC1010";
        List<String> scopes = Arrays.asList("OPENID");
        long expireTime = 600L;

        String getUserJid = userId;
        String getIpAddress = "10.10.10.10";

        Mockito.when(IdentityUtils.getUserJid()).thenReturn(getUserJid);
        Mockito.when(IdentityUtils.getIpAddress()).thenReturn(getIpAddress);

        AccessTokenModel accessTokenModel = new AccessTokenModel();

        Mockito.doAnswer(invocation -> {
            AbstractModel atm = accessTokenModel;
            AccessTokenModel accessTokenModel1 = (AccessTokenModel) invocation.getArguments()[0];

            accessTokenModel.redeemed = accessTokenModel1.redeemed;
            persistAbstractModel(atm, invocation);

            return null;
        }).when(accessTokenDao).persist(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        AccessToken accessToken = clientService.regenerateAccessToken(code, userId, clientId, scopes, expireTime);

        Assert.assertFalse(accessToken.isRedeemed(), "Access token is redeemed");
        Assert.assertNotNull(accessTokenModel.userCreate, "User create must not be null");
        Assert.assertNotNull(accessTokenModel.ipCreate, "IP create must not be null");

    }

    @Test
    public void findAccessTokenByAccessToken_ValidToken_ReturnsAccessToken() {
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
        AccessToken accessToken = new AccessToken(accessTokenModel);

        Mockito.when(accessTokenDao.findByToken(validToken)).thenReturn(accessTokenModel);

        AccessToken result = clientService.findAccessTokenByAccessToken(validToken);

        Assert.assertTrue(accessTokenIsEquals(accessToken, result), "Access token not equals");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void findAccessTokenByAccessToken_InvalidToken_ThrowsNullPointerException() {
        String invalidToken = "THIS_IS_NOT_ACCESS_TOKEN";

        Mockito.when(accessTokenDao.findByToken(invalidToken)).thenReturn(null);

        AccessToken result = clientService.findAccessTokenByAccessToken(invalidToken);

        Assert.fail("Unreachable");
    }

    @Test
    public void findAccessTokenByCode_ValidCode_ReturnsAccessToken() {
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
        AccessToken accessToken = new AccessToken(accessTokenModel);

        Mockito.when(accessTokenDao.findByCode(validCode)).thenReturn(accessTokenModel);

        AccessToken result = clientService.findAccessTokenByCode(validCode);

        Assert.assertTrue(accessTokenIsEquals(accessToken, result), "Access token not equals");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void findAccessTokenByCode_InvalidCode_ThrowsNullPointerException() {
        String invalidCode = "THIS_IS_INVALID_AUTHORIZATION_CODE";

        Mockito.when(accessTokenDao.findByCode(invalidCode)).thenReturn(null);

        AccessToken result = clientService.findAccessTokenByCode(invalidCode);

        Assert.fail("Unreachable");
    }

    @Test
    public void findRefreshTokenByRefreshToken_ValidRefreshToken_ReturnsRefreshToken() {
        String validRefreshToken = "THIS_IS_REFRESH_TOKEN";

        RefreshTokenModel refreshTokenModel = new RefreshTokenModel();
        refreshTokenModel.id = 1L;
        refreshTokenModel.code = "THIS_IS_AUTHORIZATION_CODE";
        refreshTokenModel.clientJid = "JIDC1010";
        refreshTokenModel.userJid = "JIDU0101";
        refreshTokenModel.redeemed = false;
        refreshTokenModel.scopes = StringUtils.join(Arrays.asList("OPENID"), ",");
        refreshTokenModel.token = validRefreshToken;
        RefreshToken refreshToken = new RefreshToken(refreshTokenModel);

        Mockito.when(refreshTokenDao.findByToken(validRefreshToken)).thenReturn(refreshTokenModel);

        RefreshToken result = clientService.findRefreshTokenByRefreshToken(validRefreshToken);

        Assert.assertTrue(refreshTokenIsEquals(refreshToken, result), "Refresh token not equals");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void findRefreshTokenByRefreshToken_InvalidRefreshToken_ThrowsNullPointerException() {
        String invalidRefreshToken = "THIS_IS_NOT_REFRESH_TOKEN";

        Mockito.when(refreshTokenDao.findByToken(invalidRefreshToken)).thenReturn(null);

        RefreshToken result = clientService.findRefreshTokenByRefreshToken(invalidRefreshToken);

        Assert.fail("Unreachable");
    }

    @Test
    public void findRefreshTokenByCode_ValidCode_ReturnsRefreshToken() {
        String validCode = "THIS_IS_AUTHORIZATION_CODE";

        RefreshTokenModel refreshTokenModel = new RefreshTokenModel();
        refreshTokenModel.id = 1L;
        refreshTokenModel.code = validCode;
        refreshTokenModel.clientJid = "JIDC1010";
        refreshTokenModel.userJid = "JID0101";
        refreshTokenModel.redeemed = false;
        refreshTokenModel.scopes = StringUtils.join(Arrays.asList("OPENID"), ",");
        refreshTokenModel.token = "THIS_IS_REFRESH_TOKEN";
        RefreshToken refreshToken = new RefreshToken(refreshTokenModel);

        Mockito.when(refreshTokenDao.findByCode(validCode)).thenReturn(refreshTokenModel);

        RefreshToken result = clientService.findRefreshTokenByCode(validCode);

        Assert.assertTrue(refreshTokenIsEquals(refreshToken, result), "Refresh token not equals");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void findRefreshTokenByCode_InvalidCode_ThrowsNullPointerException() {
        String invalidCode = "THIS_IS_NOT_AUTHORIZATION_CODE";

        Mockito.when(refreshTokenDao.findByCode(invalidCode)).thenReturn(null);

        RefreshToken result = clientService.findRefreshTokenByCode(invalidCode);

        Assert.fail("Unreachable");
    }

    @Test
    public void findIdTokenByCode_ValidCode_ReturnsIdToken() {
        String validCode = "THIS_IS_AUTHORIZATION_CODE";

        IdTokenModel idTokenModel = new IdTokenModel();
        idTokenModel.id = 1L;
        idTokenModel.clientJid = "JIDC1010";
        idTokenModel.userJid = "JIDC0101";
        idTokenModel.code = validCode;
        idTokenModel.redeemed = false;
        idTokenModel.token = "THIS_IS_ID_TOKEN";
        IdToken idToken = new IdToken(idTokenModel);

        Mockito.when(idTokenDao.findByCode(validCode)).thenReturn(idTokenModel);

        IdToken result = clientService.findIdTokenByCode(validCode);

        Assert.assertTrue(idTokenIsEquals(idToken, result), "Id token not equals");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void findIdTokenByCode_InvalidCode_ThrowsNullPointerException() {
        String invalidCode = "THIS_IS_NOT_AUTHORIZATION_CODE";

        Mockito.when(idTokenDao.findByCode(invalidCode)).thenReturn(null);

        IdToken idToken = clientService.findIdTokenByCode(invalidCode);

        Assert.fail("Unreachable");
    }

    @Test
    public void redeemAccessTokenById_UnredeemedAccessToken_ReturnRemainingTime() {
        long tokenId = 1L;

        AccessTokenModel accessTokenModel = new AccessTokenModel();
        accessTokenModel.id = tokenId;
        accessTokenModel.redeemed = false;
        accessTokenModel.expireTime = System.currentTimeMillis() + (60L * 60L * 1L); // one hour from now
        accessTokenModel.timeCreate = System.currentTimeMillis();

        String getUserJid = "JIDU0101";
        String getIpAddress = "10.10.10.10";

        Mockito.when(IdentityUtils.getUserJid()).thenReturn(getUserJid);
        Mockito.when(IdentityUtils.getIpAddress()).thenReturn(getIpAddress);

        Mockito.when(accessTokenDao.findById(tokenId)).thenReturn(accessTokenModel);
        Mockito.doAnswer(invocation -> {
            AccessTokenModel accessTokenModel1 = (AccessTokenModel) invocation.getArguments()[0];
            editAbstractModel(accessTokenModel1, invocation);

            return null;
        }).when(accessTokenDao).edit(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        long remainingTime = clientService.redeemAccessTokenById(tokenId);

        Assert.assertTrue(remainingTime <= (60L * 60L * 1L), "Remaining cannot greater than 3600");
        Assert.assertTrue(accessTokenModel.redeemed, "Access token has not been redeemed");
        Assert.assertTrue(accessTokenModel.timeUpdate > accessTokenModel.timeCreate, "Time update not updated");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void redeemAccessTokenById_RedeemedAccessToken_ThrowsRuntimeException() {
        long tokenId = 1L;

        AccessTokenModel accessTokenModel = new AccessTokenModel();
        accessTokenModel.id = tokenId;
        accessTokenModel.redeemed = true;
        accessTokenModel.expireTime = System.currentTimeMillis() + (60L * 60L * 1L); // one hour from now

        Mockito.when(accessTokenDao.findById(tokenId)).thenReturn(accessTokenModel);

        long remainingTime = clientService.redeemAccessTokenById(tokenId);

        Assert.fail("Unreachable");
    }

    @Test
    public void redeemRefreshTokenById_UnredeemedRefreshToken_RefreshTokenRedeemed() {
        long tokenId = 1L;

        RefreshTokenModel refreshTokenModel = new RefreshTokenModel();
        refreshTokenModel.id = tokenId;
        refreshTokenModel.redeemed = false;
        refreshTokenModel.timeCreate = System.currentTimeMillis();

        String getUserJid = "JIDU0101";
        String getIpAddress = "10.10.10.10";

        Mockito.when(IdentityUtils.getUserJid()).thenReturn(getUserJid);
        Mockito.when(IdentityUtils.getIpAddress()).thenReturn(getIpAddress);

        Mockito.when(refreshTokenDao.findById(tokenId)).thenReturn(refreshTokenModel);
        Mockito.doAnswer(invocation -> {
            RefreshTokenModel refreshTokenModel1 = (RefreshTokenModel) invocation.getArguments()[0];
            editAbstractModel(refreshTokenModel1, invocation);

            return null;
        }).when(refreshTokenDao).edit(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        clientService.redeemRefreshTokenById(tokenId);

        Assert.assertTrue(refreshTokenModel.redeemed, "Refresh token has not been redeemed");
        Assert.assertTrue(refreshTokenModel.timeUpdate > refreshTokenModel.timeCreate, "Time update not updated");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void redeemRefreshTokenById_RedeemedRefreshToken_ThrowsRuntimeException() {
        long tokenId = 1L;

        RefreshTokenModel refreshTokenModel = new RefreshTokenModel();
        refreshTokenModel.id = tokenId;
        refreshTokenModel.redeemed = true;

        Mockito.when(refreshTokenDao.findById(tokenId)).thenReturn(refreshTokenModel);

        clientService.redeemRefreshTokenById(tokenId);

        Assert.fail("Unreachable");
    }

    @Test
    public void redeemIdTokenById_UnredeemedIdToken_IdTokenRedeemed() {
        long tokenId = 1L;

        IdTokenModel idTokenModel = new IdTokenModel();
        idTokenModel.id = tokenId;
        idTokenModel.redeemed = false;
        idTokenModel.timeCreate = System.currentTimeMillis();

        String getUserJid = "JIDU0101";
        String getIpAddress = "10.10.10.10";

        Mockito.when(IdentityUtils.getUserJid()).thenReturn(getUserJid);
        Mockito.when(IdentityUtils.getIpAddress()).thenReturn(getIpAddress);

        Mockito.when(idTokenDao.findById(tokenId)).thenReturn(idTokenModel);
        Mockito.doAnswer(invocation -> {
            IdTokenModel idTokenModel1 = (IdTokenModel) invocation.getArguments()[0];
            editAbstractModel(idTokenModel1, invocation);

            return null;
        }).when(idTokenDao).edit(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        clientService.redeemIdTokenById(tokenId);

        Assert.assertTrue(idTokenModel.redeemed, "Id token has not been redeemed");
        Assert.assertTrue(idTokenModel.timeUpdate > idTokenModel.timeCreate, "Time update not updated");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void redeemIdTokenById_RedeemedIdToken_ThrowsRuntimeException() {
        long tokenId = 1L;

        IdTokenModel idTokenModel = new IdTokenModel();
        idTokenModel.id = 1L;
        idTokenModel.redeemed = true;

        Mockito.when(idTokenDao.findById(tokenId)).thenReturn(idTokenModel);

        clientService.redeemIdTokenById(tokenId);

        Assert.fail("Unreachable");
    }

    @Test
    public void createClient_CreateClientParameters_ClientPersisted() {
        String name = "Alice";
        String applicationType = "Web Server";
        List<String> scopes = Arrays.asList("OPENID", "OFFLINE_ACCESS");
        List<String> redirectURIs = Arrays.asList("http://alice.com/verify");

        String clientJid = "JIDC1010";
        String newSecret = "THIS_IS_NEW_SECRET";

        String getUserJid = "JIDU0101";
        String getIpAddress = "10.10.10.10";

        PowerMockito.mockStatic(JudgelsUtils.class);
        Mockito.when(JudgelsUtils.generateNewSecret()).thenReturn(newSecret);

        Mockito.when(IdentityUtils.getUserJid()).thenReturn(getUserJid);
        Mockito.when(IdentityUtils.getIpAddress()).thenReturn(getIpAddress);

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

        clientService.createClient(name, applicationType, scopes, redirectURIs);

        Assert.assertEquals(clientJid, clientModel.jid, "Client model JID mismatch");
        Assert.assertEquals(newSecret, clientModel.secret, "Client model secret mismatch");
        Mockito.verify(redirectURIDao, Mockito.times(redirectURIs.size())).persist(Mockito.any(), Mockito.anyString(), Mockito.anyString());
        Assert.assertNotNull(clientModel.userCreate, "User create must not be null");
        Assert.assertNotNull(clientModel.ipCreate, "IP create must not be null");
    }

    @Test
    public void updateClient_UpdateClientParameters_ClientUpdatePersisted() {
        long clientId = 1L;
        String name = "Bob";
        List<String> scopes = Arrays.asList("OPENID");
        String scopesString = StringUtils.join(scopes, ",");
        List<String> redirectURIs = Arrays.asList("http://bob.com/verify", "http://bobu.com/verify");

        String getUserJid = "JIDU0101";
        String getIpAddress = "10.10.10.10";

        ClientModel clientModel = new ClientModel();
        clientModel.jid = "JIDC1010";
        clientModel.id = clientId;
        clientModel.name = "Alice";
        clientModel.scopes = StringUtils.join(Arrays.asList("OPENID", "OFFLINE_ACCESS"), ",");
        clientModel.timeCreate = System.currentTimeMillis();

        List<String> oldRedirectUris = Arrays.asList("http://alice.com/verify");
        List<RedirectURIModel> oldRedirectURIModels = createRedirectURIModel(1L, oldRedirectUris, clientModel.jid);

        Mockito.when(IdentityUtils.getUserJid()).thenReturn(getUserJid);
        Mockito.when(IdentityUtils.getIpAddress()).thenReturn(getIpAddress);

        Mockito.when(clientDao.findById(clientId)).thenReturn(clientModel);
        Mockito.when(redirectURIDao.findByClientJid(clientModel.jid)).thenReturn(oldRedirectURIModels);
        Mockito.doAnswer(invocation -> {
            ClientModel clientModel1 = (ClientModel) invocation.getArguments()[0];
            editAbstractModel(clientModel1, invocation);

            return null;
        }).when(clientDao).edit(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        clientService.updateClient(clientId, name, scopes, redirectURIs);

        Assert.assertEquals(name, clientModel.name, "Client name not changed");
        Assert.assertEquals(scopesString, clientModel.scopes, "Client scopes not changed");
        Mockito.verify(redirectURIDao, Mockito.times(oldRedirectURIModels.size())).remove(Mockito.any());
        Mockito.verify(redirectURIDao, Mockito.times(redirectURIs.size())).persist(Mockito.any(), Mockito.anyString(), Mockito.anyString());
        Assert.assertTrue(clientModel.timeUpdate > clientModel.timeCreate, "Time update not updated");
    }

    @Test
    public void deleteClient_ExistingClient_ExistingClientRemoved() {
        long clientId = 1L;

        ClientModel clientModel = new ClientModel();

        Mockito.when(clientDao.findById(clientId)).thenReturn(clientModel);

        clientService.deleteClient(clientId);

        Mockito.verify(clientDao, Mockito.times(1)).remove(clientModel);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void deleteClient_NonExistingClient_ThrowsIllegalArgumentException() {
        long clientId = -1L;

        Mockito.when(clientDao.findById(clientId)).thenReturn(null);
        Mockito.doThrow(IllegalArgumentException.class).when(clientDao).remove(null);

        clientService.deleteClient(clientId);

        Assert.fail("Unreachable");
    }

    @Test
    public void pageClients_PageClientsParameter_ReturnsPagedClients() {
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
        Mockito.when(clientDao.findSortedByFilters(Mockito.eq(orderBy), Mockito.eq(orderDir), Mockito.eq(filterString), Mockito.anyMap(), Mockito.eq(pageIndex * pageSize), Mockito.eq(pageSize)))
                .thenReturn(clientModels);

        long totalRows = clientModels.size();
        Mockito.when(clientDao.countByFilters(Mockito.eq(filterString), Mockito.anyMap())).thenReturn(totalRows);

        Page<Client> clientPage = clientService.pageClients(pageIndex, pageSize, orderBy, orderDir, filterString);

        Assert.assertNotNull(clientPage.getData(), "Page data must not be null");
        Assert.assertEquals(clientModels.size(), clientPage.getTotalRowsCount(), "Page total rows count not equal");
    }

    @Test
    public void pageClients_OtherPageClientsParameter_ReturnsEmptyPagedClient() {
        long pageIndex = 5L;
        long pageSize = 10L;
        String orderBy = "id";
        String orderDir = "asc";
        String filterString = "asdfasdf";

        List<ClientModel> clientModels = Arrays.asList();
        Mockito.when(clientDao.findSortedByFilters(Mockito.eq(orderBy), Mockito.eq(orderDir), Mockito.eq(filterString), Mockito.anyMap(), Mockito.eq(pageIndex * pageSize), Mockito.eq(pageSize)))
                .thenReturn(clientModels);

        long totalRows = clientModels.size();
        Mockito.when(clientDao.countByFilters(Mockito.eq(filterString), Mockito.anyMap())).thenReturn(totalRows);

        Page<Client> clientPage = clientService.pageClients(pageIndex, pageSize, orderBy, orderDir, filterString);

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

    private boolean authorizationCodeIsEquals(org.iatoki.judgels.jophiel.commons.plains.AuthorizationCode a, org.iatoki.judgels.jophiel.commons.plains.AuthorizationCode b) {
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
            Mockito.when(redirectURIDao.findByClientJid(client.getJid())).thenReturn(createRedirectURIModel(redirectURIId, client.getRedirectURIs(), client.getJid()));
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
}
