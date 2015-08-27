package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.jophiel.controllers.UserProfileControllerUtils;
import org.iatoki.judgels.jophiel.services.UserEmailService;
import org.iatoki.judgels.jophiel.services.UserPhoneService;
import org.iatoki.judgels.jophiel.services.UserProfileService;
import org.iatoki.judgels.jophiel.services.UserService;
import org.iatoki.judgels.jophiel.services.impls.JophielDataMigrationServiceImpl;
import org.iatoki.judgels.play.AbstractGlobal;
import org.iatoki.judgels.play.services.BaseDataMigrationService;
import play.Application;
import play.inject.Injector;

public final class Global extends AbstractGlobal {

    @Override
    public void onStart(Application application) {
        super.onStart(application);

        buildUtils(application.injector());
    }

    private void buildUtils(Injector injector) {
        UserProfileControllerUtils.buildInstance(injector.instanceOf(UserEmailService.class), injector.instanceOf(UserPhoneService.class), injector.instanceOf(UserProfileService.class), injector.instanceOf(UserService.class));
    }

    @Override
    protected BaseDataMigrationService getDataMigrationService() {
        return new JophielDataMigrationServiceImpl();
    }
}
