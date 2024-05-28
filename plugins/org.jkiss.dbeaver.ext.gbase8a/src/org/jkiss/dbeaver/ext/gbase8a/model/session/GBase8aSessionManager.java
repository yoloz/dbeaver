package org.jkiss.dbeaver.ext.gbase8a.model.session;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.admin.sessions.DBAServerSessionManager;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public class GBase8aSessionManager implements DBAServerSessionManager<GBase8aSession> {

    public static final String PROP_KILL_QUERY = "killQuery";
    private final GBase8aDataSource dataSource;

    public GBase8aSessionManager(GBase8aDataSource dataSource) {
        this.dataSource = dataSource;
    }


    public DBPDataSource getDataSource() {
        return this.dataSource;
    }

    public Collection<GBase8aSession> getSessions(DBCSession session, Map<String, Object> options) throws DBException {
        List<GBase8aSession> list = new ArrayList<>();
        try (JDBCPreparedStatement dbStat = ((JDBCSession) session).prepareStatement("SHOW FULL PROCESSLIST")) {
            JDBCResultSet dbResult = dbStat.executeQuery();
            while (dbResult.next()) {
                list.add(new GBase8aSession(dbResult));
            }
        } catch (SQLException e) {
            throw new DBException(e, session.getDataSource());
        }
        return list;
    }


    public void alterSession(DBCSession session, GBase8aSession sessionType, Map<String, Object> options) throws DBException {
        try (JDBCPreparedStatement dbStat = ((JDBCSession) session).prepareStatement(
                Boolean.TRUE.equals(options.get("killQuery")) ? (
                        "KILL QUERY " + sessionType.getPid()) : (
                        "KILL CONNECTION " + sessionType.getPid()))) {
            dbStat.execute();
        } catch (SQLException e) {
            throw new DBException(e, session.getDataSource());
        }
    }
}
