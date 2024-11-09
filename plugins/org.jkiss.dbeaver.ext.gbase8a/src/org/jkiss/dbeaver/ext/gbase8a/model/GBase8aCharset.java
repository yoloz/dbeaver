package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aConstants;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.meta.PropertyLength;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class GBase8aCharset extends GBase8aInformation {

    private String name;
    private String description;
    private int maxLength;
    private final List<GBase8aCollation> collations = new ArrayList<>();


    public GBase8aCharset(GBase8aDataSource dataSource, ResultSet dbResult) throws SQLException {
        super(dataSource);
        loadInfo(dbResult);
    }


    private void loadInfo(ResultSet dbResult) throws SQLException {
        this.name = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_CHARSET);
        this.description = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_DESCRIPTION);
        this.maxLength = JDBCUtils.safeGetInt(dbResult, GBase8aConstants.COL_MAX_LEN);
    }


    void addCollation(GBase8aCollation collation) {
        this.collations.add(collation);
        this.collations.sort(DBUtils.nameComparator());
    }


    @NotNull
    @Override
    @Property(viewable = true, order = 1)
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


    @Nullable
    @Override
    @Property(viewable = true, length = PropertyLength.MULTILINE, order = 100)
    public String getDescription() {
        return this.description;
    }
}
