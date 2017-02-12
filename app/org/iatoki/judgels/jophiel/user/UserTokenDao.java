package org.iatoki.judgels.jophiel.user;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

import java.util.Optional;

@ImplementedBy(UserTokenHibernateDao.class)
public interface UserTokenDao extends Dao<Long, UserTokenModel> {

    Optional<UserTokenModel> getByToken(String token);

    Optional<UserTokenModel> getByUserJid(String userJid);
}
