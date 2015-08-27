package org.iatoki.judgels.jophiel.models.daos;

import org.iatoki.judgels.jophiel.models.entities.InstitutionModel;
import org.iatoki.judgels.play.models.daos.Dao;

public interface InstitutionDao extends Dao<Long, InstitutionModel> {

    boolean existsByName(String name);

    InstitutionModel findByName(String name);
}
