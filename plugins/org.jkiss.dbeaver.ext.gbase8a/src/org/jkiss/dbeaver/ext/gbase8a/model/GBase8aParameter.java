package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.DBSObject;


public class GBase8aParameter implements DBSObject {

    private final GBase8aDataSource dataSource;
    private final String name;
    private Object value;
    private String description;

    public GBase8aParameter(GBase8aDataSource dataSource, String name, Object value) {
        this.dataSource = dataSource;
        this.name = name;
        this.value = value;
    }


    @Property(viewable = true, order = 1)
    @NotNull
    @Override
    public String getName() {
        return this.name;
    }


    @Property(viewable = true, order = 2)
    public Object getValue() {
        return this.value;
    }


    @Nullable
    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public DBSObject getParentObject() {
        return getDataSource();
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
}
