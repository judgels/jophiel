package org.iatoki.judgels.jophiel.user;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.jophiel.PasswordHash;
import org.iatoki.judgels.jophiel.user.profile.email.UnverifiedUserEmail;
import org.iatoki.judgels.jophiel.user.profile.email.UserEmailDao;
import org.iatoki.judgels.jophiel.user.profile.email.UserEmailModel;
import org.iatoki.judgels.play.JudgelsPlayUtils;
import org.iatoki.judgels.play.Page;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public final class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final UserEmailDao userEmailDao;

    @Inject
    public UserServiceImpl(UserDao userDao, UserEmailDao userEmailDao) {
        this.userDao = userDao;
        this.userEmailDao = userEmailDao;
    }

    @Override
    public boolean userExistsByUsername(String username) {
        return userDao.existByUsername(username);
    }

    @Override
    public boolean userExistsByUsernameAndPassword(String username, String password) {
        if (!userDao.existByUsername(username)) {
            return false;
        }

        UserModel userModel = userDao.findByUsername(username);
        if (userModel.password.contains(":")) {
            try {
                return PasswordHash.validatePassword(password, userModel.password);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                return false;
            }
        } else {
            return userModel.password.equals(JudgelsPlayUtils.hashSHA256(password));
        }
    }

    @Override
    public boolean userExistsByJid(String userJid) {
        return userDao.existsByJid(userJid);
    }

    @Override
    public List<User> getUsersByUsernames(Collection<String> usernames) {
        List<UserModel> userModels =  userDao.findSortedByFiltersIn("id", "asc", "", ImmutableMap.of(UserModel_.username, usernames), 0, -1);

        return userModels.stream().map(m -> UserServiceUtils.createUserFromModel(m)).collect(Collectors.toList());
    }

    @Override
    public List<User> getUsersByTerm(String term) {
        List<UserModel> userModels = userDao.findSortedByFilters("id", "asc", term, 0, -1);
        ImmutableList.Builder<User> userBuilder = ImmutableList.builder();

        for (UserModel userModel : userModels) {
            userBuilder.add(UserServiceUtils.createUserFromModel(userModel));
        }

        return userBuilder.build();
    }

    @Override
    public Page<User> getPageOfUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        List<String> userUserJid = userDao.getJidsByFilter(filterString);
        List<String> emailUserJid = userEmailDao.getUserJidsByFilter(filterString);

        ImmutableSet.Builder<String> setBuilder = ImmutableSet.builder();
        setBuilder.addAll(userUserJid);
        setBuilder.addAll(emailUserJid);

        ImmutableSet<String> userJidSet = setBuilder.build();
        long totalRow = userJidSet.size();
        ImmutableList.Builder<User> listBuilder = ImmutableList.builder();

        if (totalRow > 0) {
            List<String> sortedUserJids;
            if (orderBy.equals("email")) {
                sortedUserJids = userEmailDao.getSortedUserJidsByEmail(userJidSet, orderBy, orderDir);
            } else {
                sortedUserJids = userDao.getSortedJidsByOrder(userJidSet, orderBy, orderDir);
            }

            List<UserModel> userModels = userDao.getByJids(sortedUserJids, pageIndex * pageSize, pageSize);

            for (int i = 0; i < userModels.size(); ++i) {
                UserModel userModel = userModels.get(i);
                listBuilder.add(UserServiceUtils.createUserFromModel(userModel));
            }
        }

        return new Page<>(listBuilder.build(), totalRow, pageIndex, pageSize);
    }

    @Override
    public Page<UnverifiedUserEmail> getPageOfUnverifiedUsers(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        List<String> unverifiedEmailUserJids = userEmailDao.getUserJidsWithUnverifiedEmail();
        List<String> userUserJids = userDao.getJidsByFilter(filterString);
        List<String> emailUserJids = userEmailDao.getUserJidsByFilter(filterString);

        userUserJids.retainAll(unverifiedEmailUserJids);
        emailUserJids.retainAll(unverifiedEmailUserJids);

        ImmutableSet.Builder<String> setBuilder = ImmutableSet.builder();
        setBuilder.addAll(userUserJids);
        setBuilder.addAll(emailUserJids);

        ImmutableSet<String> userJidSet = setBuilder.build();
        long totalRow = userJidSet.size();
        ImmutableList.Builder<UnverifiedUserEmail> listBuilder = ImmutableList.builder();

        if (totalRow > 0) {
            List<String> sortedUserJid;
            if (orderBy.equals("email")) {
                sortedUserJid = userEmailDao.getSortedUserJidsByEmail(userJidSet, orderBy, orderDir);
            } else {
                sortedUserJid = userDao.getSortedJidsByOrder(userJidSet, orderBy, orderDir);
            }

            List<UserModel> userModels = userDao.getByJids(sortedUserJid, pageIndex * pageSize, pageSize);
            List<UserEmailModel> emailModels = userEmailDao.getByUserJids(sortedUserJid, pageIndex * pageSize, pageSize);

            for (int i = 0; i < userModels.size(); ++i) {
                UserModel userModel = userModels.get(i);
                UserEmailModel emailModel = emailModels.get(i);
                listBuilder.add(new UnverifiedUserEmail(emailModel.id, emailModel.jid, userModel.username, emailModel.email, emailModel.emailVerified));
            }
        }

        return new Page<>(listBuilder.build(), totalRow, pageIndex, pageSize);
    }

    @Override
    public User findUserById(long userId) throws UserNotFoundException {
        UserModel userModel = userDao.findById(userId);
        if (userModel == null) {
            throw new UserNotFoundException("User not found.");
        }

        User user = UserServiceUtils.createUserFromModel(userModel);

        return user;
    }

    @Override
    public User findUserByJid(String userJid) {
        UserModel userModel = userDao.findByJid(userJid);

        return UserServiceUtils.createUserFromModel(userModel);
    }

    @Override
    public User findUserByUsername(String username) {
        UserModel userModel = userDao.findByUsername(username);

        return UserServiceUtils.createUserFromModel(userModel);
    }

    @Override
    public User createUser(String username, String name, String email, String password, List<String> roles, String userJid, String userIpAddress) {
        UserModel userModel = new UserModel();
        userModel.username = username;
        userModel.name = name;

        try {
            userModel.password = PasswordHash.createHash(password);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }

        userModel.profilePictureImageName = "avatar-default.png";
        userModel.roles = StringUtils.join(roles, ",");

        userDao.persist(userModel, userJid, userIpAddress);

        UserEmailModel emailModel = new UserEmailModel();
        emailModel.email = email;
        emailModel.emailVerified = true;
        emailModel.userJid = userModel.jid;

        userEmailDao.persist(emailModel, userJid, userIpAddress);

        userModel.emailJid = emailModel.jid;

        userDao.edit(userModel, userJid, userIpAddress);

        return UserServiceUtils.createUserFromModel(userModel);
    }

    @Override
    public void updateUser(String userJid, String username, String name, List<String> roles, String updaterJid, String updaterIpAddress) {
        UserModel userModel = userDao.findByJid(userJid);

        userModel.username = username;
        userModel.name = name;
        userModel.roles = StringUtils.join(roles, ",");

        userDao.edit(userModel, updaterJid, updaterIpAddress);
    }

    @Override
    public void updateUser(String userJid, String username, String name, String password, List<String> roles, String updaterJid, String updaterIpAddress) {
        UserModel userModel = userDao.findByJid(userJid);

        userModel.username = username;
        userModel.name = name;

        try {
            userModel.password = PasswordHash.createHash(password);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }

        userModel.roles = StringUtils.join(roles, ",");

        userDao.edit(userModel, updaterJid, updaterIpAddress);
    }

    @Override
    public void deleteUser(String userJid) {
        UserModel userModel = userDao.findByJid(userJid);

        userDao.remove(userModel);
    }
}
