package org.iatoki.judgels.jophiel.models.daos;

import org.iatoki.judgels.play.models.daos.Dao;
import org.iatoki.judgels.jophiel.models.entities.IdTokenModel;

public interface IdTokenDao extends Dao<Long, IdTokenModel> {

    IdTokenModel findByCode(String code);

}
