package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.model.DBPSystemInfoObject;
import org.jkiss.dbeaver.model.struct.DBSObject;

/**
 * GBase8a informational object
 */
public abstract class GBase8aInformation implements DBSObject, DBPSystemInfoObject {

    private final GBase8aDataSource dataSource;

    protected GBase8aInformation(GBase8aDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public DBSObject getParentObject() {
        return getDataSource().getContainer();
    }


    @NotNull
    @Override
    public GBase8aDataSource getDataSource() {
        return this.dataSource;
    }

    @Override
    public boolean isPersisted() {
        return true;
    }

    @Override
    public String toString() {
        return getName();
    }
}