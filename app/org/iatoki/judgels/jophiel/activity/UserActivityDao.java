package org.iatoki.judgels.jophiel.activity;

import com.google.inject.ImplementedBy;
import org.iatoki.judgels.play.model.Dao;

@ImplementedBy(UserActivityHibernateDao.class)
public interface UserActivityDao extends Dao<Long, UserActivityModel> {

}
