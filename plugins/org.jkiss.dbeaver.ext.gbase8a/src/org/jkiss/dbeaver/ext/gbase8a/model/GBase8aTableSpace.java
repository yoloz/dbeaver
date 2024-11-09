package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aConstants;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPRefreshableObject;
import org.jkiss.dbeaver.model.DBPSaveableObject;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GBase8aTableSpace implements DBSObject, DBPRefreshableObject, DBPSaveableObject {
    private final GBase8aDataSource dataSource;
    private final GBase8aCatalog catalog;
    private String name;
    private String path;
    private boolean isDefault;
    private String segsize;
    private String maxsize;
    private String usedsize;
    private String freesize;

    public GBase8aTableSpace(GBase8aCatalog catalog, ResultSet dbResult) throws SQLException {
        this.catalog = catalog;
        this.dataSource = catalog.getDataSource();
        this.name = JDBCUtils.safeGetString(dbResult, "TABLESPACE_NAME");
        this.path = JDBCUtils.safeGetString(dbResult, "TABLESPACE_PATH");
        this.isDefault = JDBCUtils.safeGetString(dbResult, "IS_DEFAULT").equalsIgnoreCase("yes");
        this.segsize = changeSize(JDBCUtils.safeGetString(dbResult, "SEG_SIZE"));
        this.maxsize = changeSize(JDBCUtils.safeGetString(dbResult, "MAX_SIZE"));
        this.usedsize = changeSize(JDBCUtils.safeGetString(dbResult, "USED_SIZE"));
        this.freesize = changeSize(JDBCUtils.safeGetString(dbResult, "FREE_SIZE"));
    }

    public GBase8aTableSpace(GBase8aCatalog catalog) {
        this.catalog = catalog;
        this.dataSource = catalog.getDataSource();
    }

    @Property(viewable = true, editable = false, updatable = false, order = 11)
    public String getName() {
        return this.name;
    }

    @Property(viewable = true, editable = false, updatable = false, order = 22)
    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Property(viewable = true, editable = false, updatable = false, order = 33)
    public boolean isDefault() {
        return this.isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Property(viewable = true, editable = false, updatable = false, order = 44)
    public String getSegsize() {
        return this.segsize;
    }

    public void setSegsize(String segsize) {
        this.segsize = segsize;
    }

    @Property(viewable = true, editable = false, updatable = false, order = 55)
    public String getMaxsize() {
        return this.maxsize;
    }

    @Property(viewable = true, editable = false, updatable = false, order = 66)
    public String getUsedsize() {
        return this.usedsize;
    }

    @Property(viewable = true, editable = false, updatable = false, order = 77)
    public String getFreesize() {
        return this.freesize;
    }

    public GBase8aCatalog getCatalog() {
        return this.catalog;
    }

    public void setMaxsize(String maxsize) {
        this.maxsize = maxsize;
    }

    public void setName(String name) {
        this.name = name;
    }


    public boolean isPersisted() {
        return false;
    }


    public void setPersisted(boolean persisted) {
    }


    public DBSObject refreshObject(DBRProgressMonitor monitor) throws DBException {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public DBSObject getParentObject() {
        return this.dataSource;
    }

    @Override
    public DBPDataSource getDataSource() {
        return this.dataSource;
    }

    private String changeSize(String sizeNum) {
        String oldSize = "0";
        if (sizeNum.equalsIgnoreCase("default")) {
            return sizeNum;
        }
        long oldSizeNum = Long.parseLong(sizeNum);
        if (oldSizeNum / 1024L / 1024L / 1024L / 1024L > 0L && oldSizeNum % 256L == 0L) {
            oldSize = oldSizeNum / 1024L / 1024L / 1024L / 1024L + "T";
        } else if (oldSizeNum / 1024L / 1024L / 1024L > 0L && oldSizeNum % 1073741824L == 0L) {
            oldSize = oldSizeNum / 1024L / 1024L / 1024L + "G";
        } else if (oldSizeNum / 1024L / 1024L > 0L && oldSizeNum % 1048576L == 0L) {
            oldSize = oldSizeNum / 1024L / 1024L + "M";
        } else if (oldSizeNum / 1024L > 0L && oldSizeNum % 1024L == 0L) {
            oldSize = oldSizeNum / 1024L + "K";
        } else {
            oldSize = "0";
        }
        return oldSize;
    }
}