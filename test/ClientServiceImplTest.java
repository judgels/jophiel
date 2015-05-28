import com.nimbusds.oauth2.sdk.AuthorizationCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.jophiel.commons.exceptions.ClientNotFoundException;
import org.iatoki.judgels.jophiel.commons.plains.Client;
import org.iatoki.judgels.jophiel.models.daos.AccessTokenDao;
import org.iatoki.judgels.jophiel.models.daos.AuthorizationCodeDao;
import org.iatoki.judgels.jophiel.models.daos.ClientDao;
import org.iatoki.judgels.jophiel.models.daos.IdTokenDao;
import org.iatoki.judgels.jophiel.models.daos.RedirectURIDao;
import org.iatoki.judgels.jophiel.models.daos.RefreshTokenDao;
import org.iatoki.judgels.jophiel.models.domains.AuthorizationCodeModel;
import org.iatoki.judgels.jophiel.models.domains.ClientModel;
import org.iatoki.judgels.jophiel.models.domains.RedirectURIModel;
import org.iatoki.judgels.jophiel.services.impls.ClientServiceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by bagus.seto on 5/25/2015.
 */
@PrepareForTest(IdentityUtils.class)
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
                new Client(1L, "JID0001", "Client 1", "secret1", "Web Server", Arrays.asList("user").stream().collect(Collectors.toSet()), Arrays.asList("http://client1/verify")),
                new Client(2L, "JID0002", "Client 2", "secret2", "Web Server", Arrays.asList("user").stream().collect(Collectors.toSet()), Arrays.asList("http://client2/verify"))
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
                new Client(1L, "JID0001", "Client 1", "secret1", "Web Server", Arrays.asList("user").stream().collect(Collectors.toSet()), Arrays.asList("http://client1/verify")),
                new Client(2L, "JID0002", "Client 2", "secret2", "Web Server", Arrays.asList("user").stream().collect(Collectors.toSet()), Arrays.asList("http://client2/verify"))
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
        String containedTerm = "user";
        List<Client> expected = Arrays.asList(
                new Client(1L, "JID0001", "Alice", "alice secret", "Web Server", Arrays.asList("user").stream().collect(Collectors.toSet()), Arrays.asList("http://alice.com/verify")),
                new Client(2L, "JID0002", "Bob", "bob secret", "Web Server", Arrays.asList("admin", "user").stream().collect(Collectors.toSet()), Arrays.asList("http://bob.com/verify"))
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
        List<String> scopes1 = Arrays.asList("user");
        String scopes1String = StringUtils.join(scopes1, ",");

        Mockito.when(IdentityUtils.getUserJid()).thenReturn(userJid1);
        Mockito.when(authorizationCodeDao.checkIfAuthorized(clientJid1, userJid1, scopes1String)).thenReturn(true);

        Assert.assertTrue(clientService.isClientAuthorized(clientJid1, scopes1), "Client is not authorized");
    }

    @Test
    public void isClientAuthorized_UnauthorizedClient_ReturnsFalse() {
        String clientJid1 = "JIDC0000";
        String userJid1 = "JIDU0100";
        List<String> scopes1 = Arrays.asList("user");
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
        Client existingClient = new Client(existingClientId, "JIDC1010", "Alice", "alice secret", "Web Server", Arrays.asList("user").stream().collect(Collectors.toSet()), Arrays.asList("http://alice.com/verify"));

        ClientModel clientModelFromExistingClient = createClientModelFromClient(existingClient);
        Mockito.when(clientDao.findById(existingClientId)).thenReturn(clientModelFromExistingClient);

        List<RedirectURIModel> redirectURIModelsFromExistingClient = createRedirectURIModel(1L, existingClient.getRedirectURIs(), existingClient.getJid());
        Mockito.when(redirectURIDao.findByClientJid(existingClient.getJid())).thenReturn(redirectURIModelsFromExistingClient);

        Client result = clientService.findClientById(existingClientId);
        Assert.assertTrue(clientIsEqual(existingClient, result), "Client find by Id not equal");
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
        Client existingClient = new Client(10L, existingClientJid, "Alice", "alice secret", "Web Server", Arrays.asList("user").stream().collect(Collectors.toSet()), Arrays.asList("http://alice.com/verify"));

        ClientModel clientModelFromExistingClient = createClientModelFromClient(existingClient);
        Mockito.when(clientDao.findByJid(existingClientJid)).thenReturn(clientModelFromExistingClient);

        List<RedirectURIModel> redirectUriModelsFromExistingClient = createRedirectURIModel(1L, existingClient.getRedirectURIs(), existingClient.getJid());
        Mockito.when(redirectURIDao.findByClientJid(existingClient.getJid())).thenReturn(redirectUriModelsFromExistingClient);

        Client result = clientService.findClientByJid(existingClientJid);
        Assert.assertTrue(clientIsEqual(existingClient, result), "Client find by Jid not equal");
    }

    @Test(expectedExceptions = ClientNotFoundException.class)
    public void findClientByJid_NonExistingClientJid_Returns() throws ClientNotFoundException {
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
        List<String> scopes = Arrays.asList("USER");
        long expireTime = 600L; // 10 minutes

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
        authorizationCodeModel.clientJid = validClient.getJid();
        authorizationCodeModel.userJid = getUserJid;
        authorizationCodeModel.code = "";
        authorizationCodeModel.expireTime = expireTime;
        authorizationCodeModel.redirectURI = redirectURI;
        authorizationCodeModel.scopes = StringUtils.join(scopes, ",");

        Mockito.doAnswer(invocation -> {
            AuthorizationCodeModel acm = authorizationCodeModel;
            String userJid = (String)invocation.getArguments()[1];
            String ipAddress = (String)invocation.getArguments()[2];

            acm.timeCreate = System.currentTimeMillis();
            acm.userCreate = userJid;
            acm.ipCreate = ipAddress;
            acm.timeUpdate = acm.timeCreate;
            acm.userUpdate = userJid;
            acm.ipUpdate = ipAddress;

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
        List<String> scopes = Arrays.asList("USER");
        long expireTime = 600L; // 10 minutes

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

    private void assertClientListEquals(List<Client> expected, List<Client> result) {
        Assert.assertEquals(expected.size(), result.size(), "Result size not equal to expected size");
        Assert.assertTrue(IntStream.range(0, expected.size())
                .mapToObj(i -> clientIsEqual(expected.get(i), result.get(i)))
                .allMatch(b -> b), "Some element are not equal");
    }

    private boolean clientIsEqual(Client a, Client b) {
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
