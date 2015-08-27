package org.iatoki.judgels.jophiel.models.daos;

import org.iatoki.judgels.jophiel.models.entities.ProvinceModel;
import org.iatoki.judgels.play.models.daos.Dao;

public interface ProvinceDao extends Dao<Long, ProvinceModel> {

    boolean existsByName(String name);

    ProvinceModel findByName(String name);
}
