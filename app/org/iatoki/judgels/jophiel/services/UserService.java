package org.iatoki.judgels.jophiel.services;

import org.iatoki.judgels.play.Page;
import org.iatoki.judgels.jophiel.UserInfo;
import org.iatoki.judgels.jophiel.UserNotFoundException;

import java.util.List;

public interface UserService {

    boolean existsUserByUsername(String username);

    boolean existsUserByJid(String userJid);

    List<UserInfo> getUsersInfoByTerm(String term);

    Page<UserInfo> getPageOfUsersInfo(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<UserInfo> getPageOfUnverifiedUsersInfo(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    UserInfo findUserInfoById(long userId) throws UserNotFoundException;

    UserInfo findUserInfoByJid(String userJid);

    UserInfo findPublicUserInfoByJid(String userJid);

    UserInfo findUserInfoByUsername(String username);

    void createUser(String username, String name, String email, String password, List<String> roles);

    void updateUser(long userId, String username, String name, String email, List<String> roles) throws UserNotFoundException;

    void updateUser(long userId, String username, String name, String email, String password, List<String> roles)  throws UserNotFoundException;

    void deleteUser(long userId);
}
