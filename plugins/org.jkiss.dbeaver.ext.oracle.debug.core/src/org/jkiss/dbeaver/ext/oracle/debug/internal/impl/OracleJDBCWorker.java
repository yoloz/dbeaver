package org.jkiss.dbeaver.ext.oracle.debug.internal.impl;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jkiss.dbeaver.debug.DBGEvent;
import org.jkiss.dbeaver.model.runtime.AbstractJob;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.utils.GeneralUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author yolo
 */
public class OracleJDBCWorker extends AbstractJob {

    private final OracleDebugSession debugSession;
    private final String sql;
    private final DBGEvent before;
    private final DBGEvent after;

    public OracleJDBCWorker(OracleDebugSession session, String name, String sqlCommand, DBGEvent begin, DBGEvent end) {
        super(name);
        this.debugSession = session;
        this.sql = sqlCommand;
        this.before = begin;
        this.after = end;
    }

    @Override
    protected IStatus run(DBRProgressMonitor monitor) {
        monitor.beginTask("Execute async job", 1);
        try {
            Connection connection = debugSession.targetSessionContext.getConnection(monitor);
            if (connection == null || connection.isClosed()) {
                return Status.warning("Connection is not exist.");
            }
            monitor.subTask(sql);
            try (Statement stmt = connection.createStatement()) {
//                debugSession.fireEvent(before);
                stmt.execute(sql);
//                debugSession.fireEvent(after);
                return Status.OK_STATUS;
            }
        } catch (SQLException e) {
            return GeneralUtils.makeExceptionStatus(String.format("Failed to execute %s", sql), e);
        } finally {
            monitor.done();
        }
    }
}