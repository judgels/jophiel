package org.iatoki.judgels.jophiel.unit.service.impls;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.play.models.domains.AbstractModel;
import org.iatoki.judgels.jophiel.UserActivity;
import org.iatoki.judgels.jophiel.models.daos.ClientDao;
import org.iatoki.judgels.jophiel.models.daos.UserActivityDao;
import org.iatoki.judgels.jophiel.models.daos.UserDao;
import org.iatoki.judgels.jophiel.models.entities.ClientModel;
import org.iatoki.judgels.jophiel.models.entities.UserActivityModel;
import org.iatoki.judgels.jophiel.models.entities.UserActivityModel_;
import org.iatoki.judgels.jophiel.models.entities.UserModel;
import org.iatoki.judgels.jophiel.services.impls.UserActivityServiceImpl;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.metamodel.SingularAttribute;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by bagus.seto on 6/5/2015.
 */

public class UserActivityServiceImplTest extends PowerMockTestCase {

    @Mock
    private ClientDao clientDao;
    @Mock
    private UserDao userDao;
    @Mock
    private UserActivityDao userActivityDao;

    private UserActivityServiceImpl userActivityService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        userActivityService = new UserActivityServiceImpl(clientDao, userDao, userActivityDao);
    }

    @Test
    public void pageUserActivities_SingleUser_ReturnsActivitiesForThatUser() {
        long pageIndex = 0;
        long pageSize = 1;
        String orderBy = "id";
        String orderDir = "asc";
        String filterString = "";
        Set<String> clientNames = Arrays.asList("Client 1", "localhost").stream().collect(Collectors.toSet());
        String username = "alice123";

        List<String> clientJids = Arrays.asList("JIDC0001", "localhost");
        Mockito.when(clientDao.findClientJidsByNames(clientNames)).thenReturn(clientJids);

        UserModel userModel = new UserModel();
        userModel.jid = "JIDU0101";
        Mockito.when(userDao.findByUsername(username)).thenReturn(userModel);

        UserActivityModel_.clientJid = Mockito.mock(SingularAttribute.class);
        UserActivityModel_.userCreate = Mockito.mock(SingularAttribute.class);

        UserActivityModel firstUserActivityModel = new UserActivityModel();
        firstUserActivityModel.id = 1L;
        firstUserActivityModel.clientJid = "JIDC0001";
        firstUserActivityModel.log = "Log 1 Client 1";
        firstUserActivityModel.time = 0L;
        firstUserActivityModel.userCreate = "JIDU0101";
        firstUserActivityModel.ipCreate = "10.10.10.10";
        UserActivityModel secondUserActivityModel = new UserActivityModel();
        secondUserActivityModel.id = 1L;
        secondUserActivityModel.clientJid = "localhost"; // non-existent
        secondUserActivityModel.log = "Log 1 localhost";
        secondUserActivityModel.time = 100L;
        secondUserActivityModel.userCreate = "JIDU0101";
        secondUserActivityModel.ipCreate = "10.10.10.10";
        List<UserActivityModel> userActivityModels = Arrays.asList(firstUserActivityModel, secondUserActivityModel);
        Mockito.when(userActivityDao.findSortedByFilters(Mockito.eq(orderBy), Mockito.eq(orderDir), Mockito.eq(filterString), Matchers.<Map<SingularAttribute<? super UserActivityModel, String>, String>> any(), Matchers.<Map<SingularAttribute<? super UserActivityModel, String>, ? extends Collection<String>>> any(), Mockito.eq(pageIndex * pageSize), Mockito.eq(pageSize)))
                .thenReturn(userActivityModels);
        Mockito.when(clientDao.existsByJid(firstUserActivityModel.clientJid)).thenReturn(true);
        Mockito.when(clientDao.existsByJid(secondUserActivityModel.clientJid)).thenReturn(false);

        long totalRow = userActivityModels.size();
        Mockito.when(userActivityDao.countByFilters(Mockito.eq(filterString), Matchers.<Map<SingularAttribute<? super UserActivityModel, String>, String>> any(), Matchers.<Map<SingularAttribute<? super UserActivityModel, String>, Set<String>>> any())).thenReturn(totalRow);

        ClientModel firstClientModel = new ClientModel();
        firstClientModel.jid = "JID0001";
        firstClientModel.name = "Client 1";
        Mockito.when(clientDao.findByJid(firstUserActivityModel.clientJid)).thenReturn(firstClientModel);

        Page<UserActivity> userActivityPage = userActivityService.pageUserActivities(pageIndex, pageSize, orderBy, orderDir, filterString, clientNames, username);

        Assert.assertNotNull(userActivityPage.getData(), "UserInfo activity page data must not be null");
        Assert.assertEquals(totalRow, userActivityPage.getTotalRowsCount(), "UserInfo activity page total rows count not match");

        UserActivity secondElement = userActivityPage.getData().get(1);
        Assert.assertEquals(secondElement.getClientJid(), secondElement.getClientName(), "Second element client name must equals client jid");
    }

    @Test
    public void pageUsersActivities_MultipleUsers_ReturnsActivitiesForSelectedUsers() {
        long pageIndex = 0;
        long pageSize = 1;
        String orderBy = "id";
        String orderDir = "asc";
        String filterString = "";
        Set<String> clientNames = Arrays.asList("Client 1", "localhost").stream().collect(Collectors.toSet());
        Set<String> usernames = Arrays.asList("alice123", "guest").stream().collect(Collectors.toSet());

        List<String> clientJids = Arrays.asList("JIDC0001", "localhost");
        Mockito.when(clientDao.findClientJidsByNames(clientNames)).thenReturn(clientJids);

        List<String> userJids = Arrays.asList("JIDU0101", "guest");
        Mockito.when(userDao.findUserJidsByUsernames(usernames)).thenReturn(userJids);

        UserActivityModel firstUserActivityModel = new UserActivityModel();
        firstUserActivityModel.id = 1L;
        firstUserActivityModel.clientJid = "JIDC0001";
        firstUserActivityModel.log = "Log 1 Client 1";
        firstUserActivityModel.time = 0L;
        firstUserActivityModel.userCreate = "JIDU0101";
        firstUserActivityModel.ipCreate = "10.10.10.10";
        UserActivityModel secondUserActivityModel = new UserActivityModel();
        secondUserActivityModel.id = 1L;
        secondUserActivityModel.clientJid = "localhost"; // non-existent
        secondUserActivityModel.log = "Log 1 localhost";
        secondUserActivityModel.time = 100L;
        secondUserActivityModel.userCreate = "guest";
        secondUserActivityModel.ipCreate = "10.10.10.10";
        List<UserActivityModel> userActivityModels = Arrays.asList(firstUserActivityModel, secondUserActivityModel);
        Mockito.when(userActivityDao.findSortedByFilters(Mockito.eq(orderBy), Mockito.eq(orderDir), Mockito.eq(filterString), Matchers.<Map<SingularAttribute<? super UserActivityModel, String>, String>> any(), Matchers.<Map<SingularAttribute<? super UserActivityModel, String>, ? extends Collection<String>>> any(), Mockito.eq(pageIndex * pageSize), Mockito.eq(pageSize)))
                .thenReturn(userActivityModels);
        Mockito.when(userDao.existsByJid(firstUserActivityModel.userCreate)).thenReturn(true);
        Mockito.when(userDao.existsByJid(secondUserActivityModel.userCreate)).thenReturn(false);
        Mockito.when(clientDao.existsByJid(firstUserActivityModel.clientJid)).thenReturn(true);
        Mockito.when(clientDao.existsByJid(secondUserActivityModel.clientJid)).thenReturn(false);

        UserModel firstUserModel = new UserModel();
        firstUserModel.jid = "JIDU0101";
        firstUserModel.username = "alice123";
        Mockito.when(userDao.findByJid(firstUserActivityModel.userCreate)).thenReturn(firstUserModel);

        ClientModel firstClientModel = new ClientModel();
        firstClientModel.jid = "JIDC0001";
        firstClientModel.name = "Client 1";
        Mockito.when(clientDao.findByJid(firstUserActivityModel.clientJid)).thenReturn(firstClientModel);

        long totalRow = userActivityModels.size();
        Mockito.when(userActivityDao.countByFilters(Mockito.eq(filterString), Matchers.<Map<SingularAttribute<? super UserActivityModel, String>, String>> any(), Matchers.<Map<SingularAttribute<? super UserActivityModel, String>, ? extends Collection<String>>> any())).thenReturn(totalRow);

        Page<UserActivity> userActivityPage = userActivityService.pageUsersActivities(pageIndex, pageSize, orderBy, orderDir, filterString, clientNames, usernames);

        Assert.assertNotNull(userActivityPage.getData(), "UserInfo activity page data must not be null");
        Assert.assertEquals(totalRow, userActivityPage.getTotalRowsCount(), "UserInfo activity page total rows count not match");

        UserActivity secondElement = userActivityPage.getData().get(1);
        Assert.assertEquals(secondElement.getUserJid(), secondElement.getUsername(), "Second element username must equals user jid");
        Assert.assertEquals(secondElement.getClientJid(), secondElement.getClientName(), "Second element client name must equals client jid");
    }

    @Test
    public void createUserActivity_UserActivity_UserActivityPersisted() {
        String clientJid = "localhost";
        String userJid = "guest";
        long time = System.currentTimeMillis();
        String log = "user login";
        String ipAddress = "10.10.10.10";

        UserActivityModel userActivityModel = new UserActivityModel();
        Mockito.doAnswer(invocation -> {
            UserActivityModel insideUserActivityModel = (UserActivityModel)invocation.getArguments()[0];

            userActivityModel.clientJid = insideUserActivityModel.clientJid;
            userActivityModel.time = insideUserActivityModel.time;
            userActivityModel.log = insideUserActivityModel.log;
            persistAbstractModel(userActivityModel, invocation);

            return null;
        }).when(userActivityDao).persist(Mockito.any(), Mockito.anyString(), Mockito.any());

        userActivityService.createUserActivity(clientJid, userJid, time, log, ipAddress);

        Assert.assertEquals(clientJid, userActivityModel.clientJid, "Client jid not equals");
        Assert.assertEquals(time, userActivityModel.time, "Time not equals");
        Assert.assertEquals(log, userActivityModel.log, "Log not equals");
        Assert.assertEquals(userJid, userActivityModel.userCreate, "UserInfo create not equals");
        Assert.assertEquals(ipAddress, userActivityModel.ipCreate, "IP create not equals");
    }

    private void persistAbstractModel(AbstractModel abstractModel, InvocationOnMock invocation) {
        String user = (String) invocation.getArguments()[1];
        String ipAddress = (String) invocation.getArguments()[2];

        abstractModel.userCreate = user;
        abstractModel.ipCreate = ipAddress;
        abstractModel.timeCreate = System.currentTimeMillis();

        abstractModel.userUpdate = user;
        abstractModel.ipUpdate = ipAddress;
        abstractModel.timeUpdate = abstractModel.timeCreate;
    }
}
