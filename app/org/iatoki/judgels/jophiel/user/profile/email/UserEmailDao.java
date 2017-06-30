package org.iatoki.judgels.jophiel.user.profile.email;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.JudgelsDao;

import java.util.Collection;
import java.util.List;

@ImplementedBy(UserEmailHibernateDao.class)
public interface UserEmailDao extends JudgelsDao<UserEmailModel> {

    boolean existsByEmail(String email);

    boolean existsVerifiedEmail(String email);

    boolean existsUnverifiedEmailByJid(String jid);

    boolean existsByEmailCode(String emailCode);

    List<UserEmailModel> getByUserJid(String userJid);

    List<String> getUserJidsByFilter(String filter);

    List<String> getUserJidsWithUnverifiedEmail();

    List<String> getSortedUserJidsByEmail(Collection<String> userJids, String sortBy, String order, long first, long max);

    List<UserEmailModel> getByUserJids(Collection<String> userJidSet);

    UserEmailModel findByEmail(String email);

    UserEmailModel findByEmailCode(String emailCode);
}
