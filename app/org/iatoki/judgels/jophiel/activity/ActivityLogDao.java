package org.iatoki.judgels.jophiel.activity;

import com.google.inject.ImplementedBy;

@ImplementedBy(ActivityLogHibernateDao.class)
public interface ActivityLogDao extends BaseActivityLogDao<ActivityLogModel> {

}
