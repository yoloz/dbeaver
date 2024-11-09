package org.jkiss.dbeaver.ext.gbase8a.model.session;

import org.jkiss.dbeaver.model.admin.sessions.AbstractServerSession;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;

import java.sql.ResultSet;
import java.util.Map;

/**
 * GBase8a session
 */
public class GBase8aSession extends AbstractServerSession {

    private final long pid;
    private final String user;
    private final String host;
    private final String vc;
    private final String db;
    private final String command;
    private final long time;
    private final String state;
    private final String info;

    public GBase8aSession(ResultSet dbResult, Map<String, Object> options) {
        this.pid = JDBCUtils.safeGetLong(dbResult, "ID");
        this.user = JDBCUtils.safeGetString(dbResult, "USER");
        this.host = JDBCUtils.safeGetString(dbResult, "HOST");
        this.vc = JDBCUtils.safeGetString(dbResult, "VC");
        this.db = JDBCUtils.safeGetString(dbResult, "DB");
        this.command = JDBCUtils.safeGetString(dbResult, "COMMAND");
        this.time = JDBCUtils.safeGetLong(dbResult, "TIME");
        this.state = JDBCUtils.safeGetString(dbResult, "STATE");
        this.info = JDBCUtils.safeGetString(dbResult, "INFO");
    }


    @Property(viewable = true, order = 1)
    public long getPid() {
        return this.pid;
    }


    @Property(viewable = true, order = 2)
    public String getUser() {
        return this.user;
    }


    @Property(viewable = true, order = 3)
    public String getHost() {
        return this.host;
    }


    @Property(viewable = true, order = 4)
    public String getDb() {
        return this.db;
    }

    @Property(viewable = true, order = 5)
    public String getVc() {
        return this.vc;
    }

    @Property(viewable = true, order = 6)
    public String getCommand() {
        return this.command;
    }


    @Property(viewable = true, order = 7)
    public long getTime() {
        return this.time;
    }


    @Property(viewable = true, order = 8)
    public String getState() {
        return this.state;
    }

    @Override
    @Property(viewable = true, order = 9, name = "activeQuery")
    public String getActiveQuery() {
        return this.info;
    }

    @Override
    public String toString() {
        if (this.vc == null) {
            return this.pid + "@" + this.db;
        } else {
            return this.pid + "@" + this.vc + "." + this.db;
        }
    }
}
