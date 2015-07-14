package org.iatoki.judgels.jophiel;

import org.iatoki.judgels.jophiel.services.impls.JophielDataMigrationServiceImpl;
import play.Application;

public final class Global extends org.iatoki.judgels.play.Global {

    public Global() {
        super(new JophielDataMigrationServiceImpl());
    }

    @Override
    public void onStart(Application application) {
        super.onStart(application);
    }
}
