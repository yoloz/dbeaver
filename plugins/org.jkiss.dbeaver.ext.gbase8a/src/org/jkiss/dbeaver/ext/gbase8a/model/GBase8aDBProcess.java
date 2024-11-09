package org.jkiss.dbeaver.ext.gbase8a.model;

//import cn.gbase.studio.model.DBProcess;

import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;

import java.sql.SQLException;


public class GBase8aDBProcess {//implements DBProcess {
    private String[] filterExtensions = new String[]{"*.csv", "*.txt", "*.xml", "*.xls", "*.xlsx"};

    public String[] getFilterExtensions() {
        return this.filterExtensions;
    }


    public void switchDB(JDBCSession dbsession, String DB) {
    }

    public void switchVC(JDBCSession dbsession, String vc) {
        JDBCStatement st = null;
        try (JDBCPreparedStatement jDBCPreparedStatement = dbsession.prepareStatement("use vc " + vc)) {
            jDBCPreparedStatement.executeStatement();
        } catch (DBCException | SQLException ignore) {
        }
    }

    public String getSql(String db) {
        return "SELECT table_name FROM information_schema.tables where table_schema='" + db + "'";
    }


    public boolean isSystemDatabase(String databaseName) {
        if (databaseName.equalsIgnoreCase("information_schema") || databaseName.equalsIgnoreCase("gbase") ||
                databaseName.equalsIgnoreCase("performance_schema") || databaseName.equalsIgnoreCase("gclusterdb") ||
                databaseName.equalsIgnoreCase("gctmpdb")) {

            return true;
        }

        return false;
    }


    public String getImportSqlHeader(String vcName, String dbName, String tableName) {
        return "insert into `" + vcName + "`.`" + dbName + "`.`" +
                tableName + "`";
    }

    public String getImportSqlHeader(String dbName, String tableName) {
        return "insert into `" + dbName + "`.`" +
                tableName + "`";
    }

    public String getTruncateSql(String vcName, String dbName, String tableName) {
        return "truncate `" + vcName + "`.`" + dbName + "`.`" + tableName + "`";
    }


    public String getTruncateSql(String dbName, String tableName) {
        return "truncate `" + dbName + "`.`" + tableName + "`";
    }


    public String getSql(String vc, String db) {
        return "SELECT table_name FROM information_schema.tables where table_schema='" + db + "' and table_vc='" + vc + "'";
    }
}
