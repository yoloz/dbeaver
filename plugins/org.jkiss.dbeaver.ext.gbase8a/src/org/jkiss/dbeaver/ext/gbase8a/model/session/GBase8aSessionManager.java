package org.jkiss.dbeaver.ext.gbase8a.model.session;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.admin.sessions.DBAServerSessionManager;
import org.jkiss.dbeaver.model.admin.sessions.DBAServerSessionManagerSQL;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.utils.CommonUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * GBase8a session manager
 */
public class GBase8aSessionManager implements DBAServerSessionManager<GBase8aSession>, DBAServerSessionManagerSQL {

    public static final String PROP_KILL_QUERY = "killQuery";
    public static final String OPTION_HIDE_SLEEPING = "hideSleeping";
    private final GBase8aDataSource dataSource;

    public GBase8aSessionManager(GBase8aDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @NotNull
    @Override
    public DBPDataSource getDataSource() {
        return this.dataSource;
    }

    @NotNull
    @Override
    public Collection<GBase8aSession> getSessions(@NotNull DBCSession session, @NotNull Map<String, Object> options) throws DBException {
        boolean hideSleeping = CommonUtils.getOption(options, OPTION_HIDE_SLEEPING);

        try (JDBCPreparedStatement dbStat = ((JDBCSession) session).prepareStatement(generateSessionReadQuery(options));
             JDBCResultSet dbResult = dbStat.executeQuery()) {
            List<GBase8aSession> list = new ArrayList<>();
            while (dbResult.next()) {
                GBase8aSession sessionInfo = new GBase8aSession(dbResult, options);
                if (hideSleeping && "Sleep".equals(sessionInfo.getCommand())) {
                    continue;
                }
                list.add(sessionInfo);
            }
            return list;
        } catch (SQLException e) {
            throw new DBException("SHOW FULL PROCESSLIST failed", e);
        }
    }

    @Override
    public void alterSession(DBCSession session, GBase8aSession sessionType, Map<String, Object> options) throws DBException {
        try (JDBCPreparedStatement dbStat = ((JDBCSession) session).prepareStatement(
                Boolean.TRUE.equals(options.get(PROP_KILL_QUERY)) ? (
                        "KILL QUERY " + sessionType.getPid()) : (
                        "KILL CONNECTION " + sessionType.getPid()))) {
            dbStat.execute();
        } catch (SQLException e) {
            throw new DBException("alter session failed", e);
        }
    }

    @Override
    public boolean canGenerateSessionReadQuery() {
        return true;
    }

    @Override
    public String generateSessionReadQuery(Map<String, Object> options) {
        return "SHOW FULL PROCESSLIST";
    }
}
