package org.iatoki.judgels.jophiel.models.daos;

import org.iatoki.judgels.play.models.daos.JudgelsDao;
import org.iatoki.judgels.jophiel.models.entities.UserModel;

import java.util.Collection;
import java.util.List;

public interface UserDao extends JudgelsDao<UserModel> {

    boolean existByUsername(String username);

    List<String> getJidsByUsernames(Collection<String> usernames);

    List<String> getJidsByFilter(String filter);

    List<String> getSortedJidsByOrder(Collection<String> userJids, String sortBy, String order);

    List<UserModel> getByJids(Collection<String> userJids, long first, long max);

    UserModel findByUsername(String username);
}
