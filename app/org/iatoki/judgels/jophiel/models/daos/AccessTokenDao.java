package org.iatoki.judgels.jophiel.models.daos;

import org.iatoki.judgels.commons.models.daos.interfaces.Dao;
import org.iatoki.judgels.jophiel.models.entities.AccessTokenModel;

public interface AccessTokenDao extends Dao<Long, AccessTokenModel> {

    boolean existsByToken(String token);

    AccessTokenModel findByCode(String code);

    AccessTokenModel findByToken(String token);

}
