package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.impl.struct.AbstractTableIndexColumn;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableIndexColumn;

public class GBase8aTableIndexColumn extends AbstractTableIndexColumn {

    private GBase8aTableIndex index;
    private GBase8aTableColumn tableColumn;
    private int ordinalPosition;
    private boolean ascending;
    private boolean nullable;
    private String subPart;

    public GBase8aTableIndexColumn(GBase8aTableIndex index, GBase8aTableColumn tableColumn, int ordinalPosition,
                                   boolean ascending, boolean nullable, String subPart) {
        this.index = index;
        this.tableColumn = tableColumn;
        this.ordinalPosition = ordinalPosition;
        this.ascending = ascending;
        this.nullable = nullable;
        this.subPart = subPart;
    }

    GBase8aTableIndexColumn(DBRProgressMonitor monitor, GBase8aTableIndex toIndex, DBSTableIndexColumn source) throws DBException {
        this.index = toIndex;
        if (source.getTableColumn() != null) {
            this.tableColumn = toIndex.getTable().getAttribute(monitor, source.getTableColumn().getName());
        }
        this.ordinalPosition = source.getOrdinalPosition();
        this.ascending = source.isAscending();
        if (source instanceof GBase8aTableIndexColumn) {
            this.nullable = ((GBase8aTableIndexColumn) source).nullable;
            this.subPart = ((GBase8aTableIndexColumn) source).subPart;
        }
    }

    @NotNull
    @Override
    public GBase8aTableIndex getIndex() {
        return this.index;
    }

    @Property(viewable = true, order = 1)
    @NotNull
    @Override
    public String getName() {
        return this.tableColumn.getName();
    }

    @Nullable
    @Override
    @Property(viewable = true, order = 2)
    public GBase8aTableColumn getTableColumn() {
        return this.tableColumn;
    }

    @Property(viewable = false, order = 3)
    public int getOrdinalPosition() {
        return this.ordinalPosition;
    }


    @Property(viewable = true, order = 4)
    @Override
    public boolean isAscending() {
        return this.ascending;
    }


    @Property(viewable = true, order = 5)
    public boolean isNullable() {
        return this.nullable;
    }

    @Property(viewable = true, order = 6)
    public String getSubPart() {
        return this.subPart;
    }

    @Nullable
    @Override
    public String getDescription() {
        return tableColumn.getDescription();
    }

    @Override
    public GBase8aTableIndex getParentObject() {
        return index;
    }

    @NotNull
    @Override
    public GBase8aDataSource getDataSource() {
        return this.index.getDataSource();
    }
}