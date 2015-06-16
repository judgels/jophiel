package org.iatoki.judgels.jophiel.services;

import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jophiel.UserInfo;
import org.iatoki.judgels.jophiel.UserNotFoundException;

import java.util.List;

public interface UserService {

    boolean existByUsername(String username);

    boolean existsByUserJid(String userJid);

    List<UserInfo> findAllUserByTerm(String term);

    Page<UserInfo> pageUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<UserInfo> pageUnverifiedUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    UserInfo findUserById(long userId) throws UserNotFoundException;

    UserInfo findUserByUserJid(String userJid);

    UserInfo findPublicUserByUserJid(String userJid);

    UserInfo findUserByUsername(String username);

    void createUser(String username, String name, String email, String password, List<String> roles);

    void updateUser(long userId, String username, String name, String email, List<String> roles) throws UserNotFoundException;

    void updateUser(long userId, String username, String name, String email, String password, List<String> roles)  throws UserNotFoundException;

    void deleteUser(long userId);
}
