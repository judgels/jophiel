package org.iatoki.judgels.jophiel.user;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.jophiel.user.profile.email.UnverifiedUserEmail;
import org.iatoki.judgels.play.Page;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@ImplementedBy(UserServiceImpl.class)
public interface UserService {

    boolean userExistsByUsername(String username);

    boolean userExistsByUsernameAndPassword(String username, String password);

    boolean userExistsByJid(String userJid);

    List<User> getUsersByUsernames(Collection<String> usernames);

    List<User> getUsersByTerm(String term);

    Page<User> getPageOfUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Page<UnverifiedUserEmail> getPageOfUnverifiedUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString);

    Optional<User> findUserById(long userId);

    Optional<User> findUserByJid(String userJid);

    Optional<User> findUserByUsername(String username);

    User createUser(String username, String name, String email, String password, List<String> roles, String userJid, String userIpAddress);

    void updateUser(String userJid, String username, String name, List<String> roles, String updaterJid, String updaterIpAddress);

    void updateUser(String userJid, String username, String name, String password, List<String> roles, String updaterJid, String updaterIpAddress);

    void deleteUser(String userJid);
}
