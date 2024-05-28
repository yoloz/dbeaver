package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GBase8aEngine extends GBase8aInformation {

    private String name;
    private String description;
    private Support support;
    private boolean supportsTransactions;
    private boolean supportsXA;
    private boolean supportsSavepoints;

    public enum Support {
        YES,
        NO,
        DEFAULT,
        DISABLED;
    }

    public GBase8aEngine(GBase8aDataSource dataSource, ResultSet dbResult) throws SQLException {
        super(dataSource);
        loadInfo(dbResult);
    }

    public GBase8aEngine(GBase8aDataSource dataSource, String name) {
        super(dataSource);
        this.name = name;
    }


    private void loadInfo(ResultSet dbResult) throws SQLException {
        this.name = JDBCUtils.safeGetString(dbResult, "ENGINE");
        this.description = JDBCUtils.safeGetString(dbResult, "COMMENT");
        this.support = Support.valueOf(JDBCUtils.safeGetString(dbResult, "SUPPORT"));
        this.supportsTransactions = "YES".equals(JDBCUtils.safeGetString(dbResult, "TRANSACTIONS"));
        this.supportsXA = "YES".equals(JDBCUtils.safeGetString(dbResult, "XA"));
        this.supportsSavepoints = "YES".equals(JDBCUtils.safeGetString(dbResult, "SAVEPOINTS"));
    }


    @Property(viewable = true, order = 1)
    @NotNull
    public String getName() {
        return this.name;
    }


    @Nullable
    public String getDescription() {
        return this.description;
    }

    @Property(viewable = true, order = 3)
    public Support getSupport() {
        return this.support;
    }


    @Property(viewable = true, order = 4)
    public boolean isSupportsTransactions() {
        return this.supportsTransactions;
    }


    @Property(viewable = true, order = 5)
    public boolean isSupportsXA() {
        return this.supportsXA;
    }


    @Property(viewable = true, order = 6)
    public boolean isSupportsSavepoints() {
        return this.supportsSavepoints;
    }
}