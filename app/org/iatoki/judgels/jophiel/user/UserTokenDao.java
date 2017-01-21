package org.iatoki.judgels.jophiel.user;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

@ImplementedBy(UserTokenHibernateDao.class)
public interface UserTokenDao extends Dao<Long, UserTokenModel> {

    String getUserJidByToken(String token);
}
