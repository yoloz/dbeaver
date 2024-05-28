package org.jkiss.dbeaver.ext.gbase8a.model.session;

import org.jkiss.dbeaver.model.admin.sessions.AbstractServerSession;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;

import java.sql.ResultSet;


public class GBase8aSession extends AbstractServerSession {

    private long pid;
    private String user;
    private String host;
    private String db;
    private String command;
    private long time;
    private String state;
    private String info;

    public GBase8aSession(ResultSet dbResult) {
        this.pid = JDBCUtils.safeGetLong(dbResult, "id");
        this.user = JDBCUtils.safeGetString(dbResult, "user");
        this.host = JDBCUtils.safeGetString(dbResult, "host");
        this.db = JDBCUtils.safeGetString(dbResult, "db");
        this.command = JDBCUtils.safeGetString(dbResult, "command");
        this.time = JDBCUtils.safeGetLong(dbResult, "time");
        this.state = JDBCUtils.safeGetString(dbResult, "state");
        this.info = JDBCUtils.safeGetString(dbResult, "info");
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
    public String getCommand() {
        return this.command;
    }


    @Property(viewable = true, order = 6)
    public long getTime() {
        return this.time;
    }


    @Property(viewable = true, order = 7)
    public String getState() {
        return this.state;
    }


    public String getActiveQuery() {
        return this.info;
    }


    public String toString() {
        return String.valueOf(this.pid) + "@" + this.db;
    }
}
