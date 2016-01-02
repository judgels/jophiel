package org.iatoki.judgels.jophiel.user;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.JudgelsDao;

import java.util.Collection;
import java.util.List;

@ImplementedBy(UserHibernateDao.class)
public interface UserDao extends JudgelsDao<UserModel> {

    boolean existByUsername(String username);

    List<String> getJidsByUsernames(Collection<String> usernames);

    List<String> getJidsByFilter(String filter);

    List<String> getSortedJidsByOrder(Collection<String> userJids, String sortBy, String order);

    List<UserModel> getByJids(Collection<String> userJids, long first, long max);

    UserModel findByUsername(String username);
}
