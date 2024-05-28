package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.model.struct.DBSObject;


public abstract class GBase8aInformation implements DBSObject {

    private final GBase8aDataSource dataSource;

    protected GBase8aInformation(GBase8aDataSource dataSource) {
        this.dataSource = dataSource;
    }


    public DBSObject getParentObject() {
        return getDataSource().getContainer();
    }


    @NotNull
    public GBase8aDataSource getDataSource() {
        return this.dataSource;
    }


    public boolean isPersisted() {
        return true;
    }


    public String toString() {
        return getName();
    }
}