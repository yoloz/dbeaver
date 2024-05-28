package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class GBase8aCharset extends GBase8aInformation {
    private String name;
    private String description;
    private int maxLength;
    private List<GBase8aCollation> collations = new ArrayList<GBase8aCollation>();


    public GBase8aCharset(GBase8aDataSource dataSource, ResultSet dbResult) throws SQLException {
        super(dataSource);
        loadInfo(dbResult);
    }


    private void loadInfo(ResultSet dbResult) throws SQLException {
        this.name = JDBCUtils.safeGetString(dbResult, "CHARSET");
        this.description = JDBCUtils.safeGetString(dbResult, "DESCRIPTION");
        this.maxLength = JDBCUtils.safeGetInt(dbResult, "MAXLEN");
    }


    void addCollation(GBase8aCollation collation) {
        this.collations.add(collation);
        Collections.sort(this.collations, DBUtils.nameComparator());
    }


    @Property(viewable = true, order = 1)
    @NotNull
    public String getName() {
        return this.name;
    }


    public List<GBase8aCollation> getCollations() {
        return this.collations;
    }


    @Property(viewable = true, order = 2)
    public GBase8aCollation getDefaultCollation() {
        for (GBase8aCollation collation : this.collations) {
            if (collation.isDefault()) {
                return collation;
            }
        }
        return null;
    }

    public GBase8aCollation getCollation(String name) {
        for (GBase8aCollation collation : this.collations) {
            if (collation.getName().equals(name)) {
                return collation;
            }
        }
        return null;
    }


    @Property(viewable = true, order = 3)
    public int getMaxLength() {
        return this.maxLength;
    }


    @Property(viewable = true, order = 100)
    @Nullable
    public String getDescription() {
        return this.description;
    }
}
