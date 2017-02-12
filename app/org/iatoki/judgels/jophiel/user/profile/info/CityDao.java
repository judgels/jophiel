package org.iatoki.judgels.jophiel.user.profile.info;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

import java.util.Optional;

@ImplementedBy(CityHibernateDao.class)
public interface CityDao extends Dao<Long, CityModel> {

    boolean existsByName(String name);

    Optional<CityModel> findByName(String name);
}
