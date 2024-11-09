package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aConstants;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.utils.CommonUtils;

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
        this.name = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_ENGINE_NAME);
        this.description = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_COMMENT);
        this.support = CommonUtils.valueOf(
                GBase8aEngine.Support.class,
                JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_ENGINE_SUPPORT),
                GBase8aEngine.Support.YES);
        this.supportsTransactions = "YES".equals(JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_ENGINE_SUPPORT_TXN));
        this.supportsXA = "YES".equals(JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_ENGINE_SUPPORT_XA));
        this.supportsSavepoints = "YES".equals(JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_ENGINE_SUPPORT_SAVEPOINTS));
    }


    @Property(viewable = true, order = 1)
    @NotNull
    @Override
    public String getName() {
        return this.name;
    }

    @Nullable
    @Override
    public String getDescription() {
        return this.description;
    }

    @Property(viewable = true, order = 2)
    public Support getSupport() {
        return this.support;
    }


    @Property(viewable = true, order = 3)
    public boolean isSupportsTransactions() {
        return this.supportsTransactions;
    }


    @Property(viewable = true, order = 4)
    public boolean isSupportsXA() {
        return this.supportsXA;
    }


    @Property(viewable = true, order = 5)
    public boolean isSupportsSavepoints() {
        return this.supportsSavepoints;
    }
}