package org.iatoki.judgels.jophiel.oauth2;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

@ImplementedBy(AccessTokenHibernateDao.class)
public interface AccessTokenDao extends Dao<Long, AccessTokenModel> {

    boolean existsValidByTokenAndTime(String token, long time);

    AccessTokenModel findByCode(String code);

    AccessTokenModel findByToken(String token);
}
