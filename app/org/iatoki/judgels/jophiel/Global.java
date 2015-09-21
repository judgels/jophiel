package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.jophiel.services.impls.JophielDataMigrationServiceImpl;
import org.iatoki.judgels.play.AbstractGlobal;
import org.iatoki.judgels.play.services.BaseDataMigrationService;

public final class Global extends AbstractGlobal {

    @Override
    protected BaseDataMigrationService getDataMigrationService() {
        return new JophielDataMigrationServiceImpl();
    }
}
