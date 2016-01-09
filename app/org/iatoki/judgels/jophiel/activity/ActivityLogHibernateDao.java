package org.iatoki.judgels.jophiel.activity;

import org.iatoki.judgels.jophiel.models.daos.hibernate.AbstractActivityLogHibernateDao;

import javax.inject.Singleton;

@Singleton
public final class ActivityLogHibernateDao extends AbstractActivityLogHibernateDao<ActivityLogModel> implements ActivityLogDao {

    public ActivityLogHibernateDao() {
        super(ActivityLogModel.class);
    }

    @Override
    public ActivityLogModel createActivityLogModel() {
        return new ActivityLogModel();
    }
}