package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableConstraint;
import org.jkiss.dbeaver.model.impl.struct.AbstractTableConstraint;
import org.jkiss.dbeaver.model.impl.struct.AbstractTableConstraintColumn;
import org.jkiss.dbeaver.model.meta.Property;

public class GBase8aTableConstraintColumn extends AbstractTableConstraintColumn {

    private final AbstractTableConstraint<GBase8aTable, GBase8aTableConstraintColumn> constraint;
    private final GBase8aTableColumn tableColumn;
    private final int ordinalPosition;

    public GBase8aTableConstraintColumn(JDBCTableConstraint<GBase8aTable, GBase8aTableConstraintColumn> constraint,
                                        GBase8aTableColumn tableColumn, int ordinalPosition) {
        this.constraint = constraint;
        this.tableColumn = tableColumn;
        this.ordinalPosition = ordinalPosition;
    }

    @NotNull
    public String getName() {
        return this.tableColumn.getName();
    }


    @Property(id = "name", viewable = true, order = 1)
    @NotNull
    public GBase8aTableColumn getAttribute() {
        return this.tableColumn;
    }


    @Property(viewable = false, order = 2)
    public int getOrdinalPosition() {
        return this.ordinalPosition;
    }


    @Nullable
    public String getDescription() {
        return this.tableColumn.getDescription();
    }


    public AbstractTableConstraint<GBase8aTable, GBase8aTableConstraintColumn> getParentObject() {
        return this.constraint;
    }

    @NotNull
    public GBase8aDataSource getDataSource() {
        return this.constraint.getTable().getDataSource();
    }
}
