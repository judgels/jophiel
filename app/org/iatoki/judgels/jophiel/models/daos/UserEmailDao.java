package org.iatoki.judgels.jophiel.models.daos;

import org.iatoki.judgels.jophiel.models.entities.UserEmailModel;
import org.iatoki.judgels.play.models.daos.JudgelsDao;

import java.util.Collection;
import java.util.List;

public interface UserEmailDao extends JudgelsDao<UserEmailModel> {

    boolean existsByEmail(String email);

    boolean existsUnverifiedEmailByJid(String jid);

    boolean existsByEmailCode(String emailCode);

    List<UserEmailModel> getByUserJid(String userJid);

    List<String> getUserJidsByFilter(String filter);

    List<String> getUserJidsWithUnverifiedEmail();

    List<String> getSortedUserJidsByEmail(Collection<String> userJids, String sortBy, String order);

    List<UserEmailModel> getByUserJids(Collection<String> userJidSet, long first, long max);

    UserEmailModel findByEmail(String email);

    UserEmailModel findByEmailCode(String emailCode);
}
