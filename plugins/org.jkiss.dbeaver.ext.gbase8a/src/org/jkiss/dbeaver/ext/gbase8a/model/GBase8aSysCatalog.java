package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.sql.ResultSet;

public class GBase8aSysCatalog extends GBase8aCatalog {

    public GBase8aSysCatalog(GBase8aDataSource dataSource, ResultSet dbResult) {
        super(dataSource, dbResult, null);
    }

    public DBSObject getParentObject() {
        return this.dataSource.getContainer();
    }

    @NotNull
    public GBase8aDataSource getDataSource() {
        return this.dataSource;
    }
}
