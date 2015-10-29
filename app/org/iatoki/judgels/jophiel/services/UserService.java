package org.iatoki.judgels.jophiel.services;

import org.iatoki.judgels.jophiel.UnverifiedUserEmail;
import org.iatoki.judgels.jophiel.User;
import org.iatoki.judgels.jophiel.UserNotFoundException;
import org.iatoki.judgels.play.Page;

import java.util.List;

public interface UserService {

    boolean userExistsByUsername(String username);

    boolean userExistsByUsernameAndPassword(String username, String password);

    boolean userExistsByJid(String userJid);

    List<User> getUsersByTerm(String term);

    Page<User> getPageOfUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<UnverifiedUserEmail> getPageOfUnverifiedUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    User findUserById(long userId) throws UserNotFoundException;

    User findUserByJid(String userJid);

    User findUserByUsername(String username);

    User createUser(String username, String name, String email, String password, List<String> roles, String userJid, String userIpAddress);

    void updateUser(String userJid, String username, String name, List<String> roles, String updaterJid, String updaterIpAddress);

    void updateUser(String userJid, String username, String name, String password, List<String> roles, String updaterJid, String updaterIpAddress);

    void deleteUser(String userJid);
}
