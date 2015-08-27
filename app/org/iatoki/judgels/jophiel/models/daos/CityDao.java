package org.iatoki.judgels.jophiel.models.daos;

import org.iatoki.judgels.jophiel.models.entities.CityModel;
import org.iatoki.judgels.play.models.daos.Dao;

public interface CityDao extends Dao<Long, CityModel> {

    boolean existsByName(String name);

    CityModel findByName(String name);
}
