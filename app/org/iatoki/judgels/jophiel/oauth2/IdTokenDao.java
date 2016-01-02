package org.iatoki.judgels.jophiel.oauth2;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

@ImplementedBy(IdTokenHibernateDao.class)
public interface IdTokenDao extends Dao<Long, IdTokenModel> {

    IdTokenModel findByCode(String code);
}
