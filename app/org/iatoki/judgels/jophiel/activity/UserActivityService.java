package org.iatoki.judgels.jophiel.activity;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.Page;

import java.util.Set;

@ImplementedBy(UserActivityServiceImpl.class)
public interface UserActivityService {

    Page<UserActivity> getPageOfUserActivities(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, Set<String> clientsNames, String username);

    Page<UserActivity> getPageOfUsersActivities(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString, Set<String> clientsNames, Set<String> usernames);

    void createUserActivity(String clientJid, String userJid, long time, String log, String ipAddress);
}
