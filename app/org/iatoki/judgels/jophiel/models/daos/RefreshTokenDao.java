package org.iatoki.judgels.jophiel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.jophiel.models.entities.RefreshTokenModel;

public interface RefreshTokenDao extends Dao<Long, RefreshTokenModel> {

    RefreshTokenModel findByCode(String code);

    RefreshTokenModel findByToken(String token);
}
