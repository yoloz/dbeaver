package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.DBSObject;


public class GBase8aParameter implements DBSObject {
    private static final Log log = Log.getLog(GBase8aParameter.class);

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
    public String getName() {
        return this.name;
    }


    @Property(viewable = true, order = 2)
    public Object getValue() {
        return this.value;
    }


    @Nullable
    public String getDescription() {
        return this.description;
    }


    public DBSObject getParentObject() {
        return getDataSource();
    }


    @NotNull
    public GBase8aDataSource getDataSource() {
        return this.dataSource;
    }


    public boolean isPersisted() {
        return true;
    }
}
