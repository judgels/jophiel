package org.iatoki.judgels.jophiel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.jophiel.models.entities.UserEmailModel;

import java.util.Collection;
import java.util.List;

public interface UserEmailDao extends Dao<Long, UserEmailModel> {

    boolean existsByEmail(String email);

    boolean existsUnverifiedEmailByUserJid(String userJid);

    boolean existsByEmailCode(String emailCode);

    UserEmailModel findByUserJid(String userJid);

    List<String> getUserJidsByFilter(String filter);

    List<String> getUserJidsWithUnverifiedEmail();

    List<String> getSortedUserJidsByEmail(Collection<String> userJids, String sortBy, String order);

    List<UserEmailModel> getByUserJids(Collection<String> userJidSet, long first, long max);

    UserEmailModel findByEmail(String email);

    UserEmailModel findByEmailCode(String emailCode);
}
