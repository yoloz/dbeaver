package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.sql.ResultSet;
import java.sql.SQLException;


public class GBase8aCollation extends GBase8aInformation {

    private GBase8aCharset charset;
    private int id;
    private String name;
    private boolean isDefault;
    private boolean isCompiled;
    private int sortLength;

    public GBase8aCollation(GBase8aCharset charset, ResultSet dbResult) throws SQLException {
        super(charset.getDataSource());
        this.charset = charset;
        loadInfo(dbResult);
    }


    private void loadInfo(ResultSet dbResult) throws SQLException {
        this.name = JDBCUtils.safeGetString(dbResult, "COLLATION");
        this.id = JDBCUtils.safeGetInt(dbResult, "ID");
        this.isDefault = "Yes".equalsIgnoreCase(JDBCUtils.safeGetString(dbResult, "DEFAULT"));
        this.isCompiled = "Yes".equalsIgnoreCase(JDBCUtils.safeGetString(dbResult, "COMPILED"));
        this.sortLength = JDBCUtils.safeGetInt(dbResult, "SORTLEN");
    }


    @Property(viewable = true, order = 1)
    public GBase8aCharset getCharset() {
        return this.charset;
    }


    @Property(viewable = true, order = 2)
    @NotNull
    public String getName() {
        return this.name;
    }


    @Property(viewable = true, order = 3)
    public int getId() {
        return this.id;
    }


    @Property(viewable = true, order = 4)
    public boolean isDefault() {
        return this.isDefault;
    }


    @Property(viewable = true, order = 5)
    public boolean isCompiled() {
        return this.isCompiled;
    }


    @Property(viewable = true, order = 6)
    public int getSortLength() {
        return this.sortLength;
    }


    @Nullable
    public String getDescription() {
        return null;
    }


    public DBSObject getParentObject() {
        return this.charset;
    }
}