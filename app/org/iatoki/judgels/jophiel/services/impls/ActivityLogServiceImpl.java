package org.iatoki.judgels.jophiel.services.impls;

import org.iatoki.judgels.jophiel.models.daos.ActivityLogDao;
import org.iatoki.judgels.jophiel.models.entities.ActivityLogModel;
import org.iatoki.judgels.jophiel.services.ActivityLogService;

import javax.inject.Inject;
import javax.inject.Named;

@Named("activityLogService")
public final class ActivityLogServiceImpl extends AbstractBaseActivityLogServiceImpl<ActivityLogModel> implements ActivityLogService {

    @Inject
    public ActivityLogServiceImpl(ActivityLogDao activityLogDao) {
        super(activityLogDao);
    }
}
