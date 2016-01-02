package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.jophiel.activity.ActivityLogDao;
import org.iatoki.judgels.jophiel.activity.ActivityLogServiceImpl;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @deprecated Temporary class. Will be restructured when new module system has been finalized.
 */
@Singleton
@Deprecated
public final class JophielSingletonsBuilder {

    @Inject
    public JophielSingletonsBuilder(ActivityLogDao activityLogDao) {
        ActivityLogServiceImpl.buildInstance(activityLogDao);
    }
}
