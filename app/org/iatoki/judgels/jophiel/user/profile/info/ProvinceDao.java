package org.iatoki.judgels.jophiel.user.profile.info;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

@ImplementedBy(ProvinceHibernateDao.class)
public interface ProvinceDao extends Dao<Long, ProvinceModel> {

    boolean existsByName(String name);

    ProvinceModel findByName(String name);
}
