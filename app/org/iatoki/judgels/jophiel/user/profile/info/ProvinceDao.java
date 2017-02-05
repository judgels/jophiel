package org.iatoki.judgels.jophiel.user.profile.info;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

import java.util.Optional;

@ImplementedBy(ProvinceHibernateDao.class)
public interface ProvinceDao extends Dao<Long, ProvinceModel> {

    boolean existsByName(String name);

    Optional<ProvinceModel> findByName(String name);
}
