package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.jophiel.models.daos.ActivityLogDao;
import org.iatoki.judgels.jophiel.services.impls.ActivityLogServiceImpl;
import org.iatoki.judgels.jophiel.services.impls.JophielDataMigrationServiceImpl;
import org.iatoki.judgels.play.AbstractGlobal;
import org.iatoki.judgels.play.services.BaseDataMigrationService;
import play.Application;
import play.inject.Injector;

public final class Global extends AbstractGlobal {

    @Override
    public void onStart(Application application) {
        super.onStart(application);

        buildServices(application.injector());
    }

    private void buildServices(Injector injector) {
        ActivityLogServiceImpl.buildInstance(injector.instanceOf(ActivityLogDao.class));
    }

    @Override
    protected BaseDataMigrationService getDataMigrationService() {
        return new JophielDataMigrationServiceImpl();
    }
}
