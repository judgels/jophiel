package org.iatoki.judgels.jophiel.services.impls;

import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.iatoki.judgels.play.JidService;
import org.iatoki.judgels.play.services.impls.AbstractBaseDataMigrationServiceImpl;
import play.db.jpa.JPA;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class JophielDataMigrationServiceImpl extends AbstractBaseDataMigrationServiceImpl {

    @Override
    public long getCodeDataVersion() {
        return 2;
    }

    @Override
    protected void onUpgrade(long databaseVersion, long codeDatabaseVersion) throws SQLException {
        if (databaseVersion < 2) {
            migrateV1toV2();
        }
    }

    private void migrateV1toV2() throws SQLException {
        SessionImpl session = (SessionImpl) JPA.em().unwrap(Session.class);
        Connection connection = session.getJdbcConnectionAccess().obtainConnection();

        String userTable = "jophiel_user";
        String emailTable = "jophiel_user_email";

        Statement statement = connection.createStatement();
        try {
            statement.execute("ALTER TABLE " + userTable + " ADD showName BIT(1);");
        } catch (SQLException e) {
            // ignore
        }

        String emailQuery = "SELECT * FROM " + emailTable + "";
        ResultSet resultSet = statement.executeQuery(emailQuery);
        while (resultSet.next()) {
            String emailJid = JidService.getInstance().generateNewJid("USEE").toString();
            long id = resultSet.getLong("id");
            String userJid = resultSet.getString("userJid");
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + emailTable + " SET jid= ? WHERE id=" + id + ";");
            preparedStatement.setString(1, emailJid);
            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement("UPDATE " + userTable + " SET emailJid= ? WHERE jid=\"" + userJid + "\";");
            preparedStatement.setString(1, emailJid);
            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement("UPDATE " + userTable + " SET showName= ? WHERE jid=\"" + userJid + "\";");
            preparedStatement.setBoolean(1, true);
            preparedStatement.executeUpdate();
        }

        statement.close();
    }
}
