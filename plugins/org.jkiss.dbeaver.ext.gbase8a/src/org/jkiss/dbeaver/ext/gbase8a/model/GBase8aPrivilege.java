package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.access.DBAPrivilege;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCDataSource;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;

import java.sql.ResultSet;

/**
 * GBase8a Privilege
 */
public class GBase8aPrivilege implements DBAPrivilege {

    public static final String GRANT_PRIVILEGE = "Grant Option";
    public static final String ALL_PRIVILEGES = "All Privileges";

    public enum Kind {
        OBJECTS,
        DDL,
        ADMIN,
        MISC
    }

    private GBase8aDataSource dataSource;
    private String name;
    private String context;
    private String comment;
    private Kind kind;

    public GBase8aPrivilege(@NotNull GBase8aDataSource dataSource, @NotNull String context, @NotNull ResultSet resultSet) {
        this.dataSource = dataSource;
        this.name = JDBCUtils.safeGetString(resultSet, "privilege");
        this.context = context;
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

    public GBase8aPrivilege(GBase8aDataSource dataSource, String name, String context, String comment, Kind kind) {
        this.dataSource = dataSource;
        this.name = name;
        this.context = context;
        this.comment = comment;
        this.kind = kind;
    }

    public Kind getKind() {
        return this.kind;
    }


    @NotNull
    @Override
    @Property(viewable = true, order = 1)
    public String getName() {
        return name;
    }

    @Property(viewable = true, order = 2)
    public String getContext() {
        return context;
    }

    @Nullable
    @Override
    public String getDescription() {
        return comment;
    }

    @Override
    public DBSObject getParentObject() {
        return dataSource;
    }

    @NotNull
    @Override
    public JDBCDataSource getDataSource() {
        return dataSource;
    }

    @Override
    public boolean isPersisted() {
        return true;
    }

    public boolean isGrantOption() {
        return name.equalsIgnoreCase(GRANT_PRIVILEGE);
    }

    @Override
    public String toString() {
        return name;
    }

}
