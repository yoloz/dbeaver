package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.access.DBAPrivilege;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCDataSource;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;

import java.sql.ResultSet;

public class GBase8aPrivilege implements DBAPrivilege {
    public static final String GRANT_PRIVILEGE = "Grant Option";
    public static final String ALL_PRIVILEGES = "All Privileges";
    private GBase8aDataSource dataSource;
    private String name;
    private static final Log log = Log.getLog(GBase8aPrivilege.class);
    private String context;
    private String comment;
    private Kind kind;

    public enum Kind {
        OBJECTS,
        DDL,
        ADMIN,
        MISC;
    }


    public GBase8aPrivilege(GBase8aDataSource dataSource, ResultSet resultSet) {
        this.dataSource = dataSource;
        this.name = JDBCUtils.safeGetString(resultSet, "privilege");
        this.context = JDBCUtils.safeGetString(resultSet, "context");
        this.comment = JDBCUtils.safeGetString(resultSet, "comment");

        if (this.context.contains("Admin")) {
            this.kind = Kind.ADMIN;
        } else if (this.context.contains("Databases")) {
            this.kind = Kind.DDL;
        } else if (this.context.contains("Tables")) {
            this.kind = Kind.OBJECTS;
        } else {
            this.kind = Kind.MISC;
        }
    }


    public Kind getKind() {
        return this.kind;
    }


    @Property(viewable = true, order = 1)
    @NotNull
    public String getName() {
        return this.name;
    }


    @Property(viewable = true, order = 2)
    public String getContext() {
        return this.context;
    }


    @Nullable
    public String getDescription() {
        return this.comment;
    }


    public DBSObject getParentObject() {
        return (DBSObject) this.dataSource;
    }


    @NotNull
    public JDBCDataSource getDataSource() {
        return this.dataSource;
    }


    public boolean isPersisted() {
        return true;
    }


    public boolean isGrantOption() {
        return this.name.equalsIgnoreCase("Grant Option");
    }
}
