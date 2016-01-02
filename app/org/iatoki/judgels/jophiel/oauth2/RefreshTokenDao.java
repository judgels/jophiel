package org.iatoki.judgels.jophiel.oauth2;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

@ImplementedBy(RefreshTokenHibernateDao.class)
public interface RefreshTokenDao extends Dao<Long, RefreshTokenModel> {

    RefreshTokenModel findByCode(String code);

    RefreshTokenModel findByToken(String token);
}
