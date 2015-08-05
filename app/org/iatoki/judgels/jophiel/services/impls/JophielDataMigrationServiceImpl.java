package org.iatoki.judgels.jophiel.services.impls;

import org.iatoki.judgels.play.services.impls.AbstractBaseDataMigrationServiceImpl;

import java.sql.SQLException;

public final class JophielDataMigrationServiceImpl extends AbstractBaseDataMigrationServiceImpl {

    @Override
    protected void onUpgrade(long databaseVersion, long codeDatabaseVersion) throws SQLException {
    }

    @Override
    public long getCodeDataVersion() {
        return 1;
    }
}
