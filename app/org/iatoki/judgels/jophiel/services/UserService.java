package org.iatoki.judgels.jophiel.services;

import org.iatoki.judgels.jophiel.PublicUser;
import org.iatoki.judgels.jophiel.UnverifiedUserEmail;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserNotFoundException;
import org.iatoki.judgels.play.Page;

import java.util.List;

public interface UserService {

    boolean existsUserByUsername(String username);

    boolean existsUserByJid(String userJid);

    List<User> getUsersByTerm(String term);

    Page<User> getPageOfUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<UnverifiedUserEmail> getPageOfUnverifiedUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    User findUserById(long userId) throws UserNotFoundException;

    User findUserByJid(String userJid);

    PublicUser findPublicUserByJid(String userJid);

    User findUserByUsername(String username);

    void createUser(String username, String name, String email, String password, List<String> roles);

    void updateUser(long userId, String username, String name, String email, List<String> roles) throws UserNotFoundException;

    void updateUser(long userId, String username, String name, String email, String password, List<String> roles)  throws UserNotFoundException;

    void deleteUser(long userId);
}
