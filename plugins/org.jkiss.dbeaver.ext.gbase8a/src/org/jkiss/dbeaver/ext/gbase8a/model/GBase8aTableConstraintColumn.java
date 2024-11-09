package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.impl.struct.AbstractTableConstraint;
import org.jkiss.dbeaver.model.impl.struct.AbstractTableConstraintColumn;
import org.jkiss.dbeaver.model.meta.Property;

public class GBase8aTableConstraintColumn extends AbstractTableConstraintColumn {

    private final AbstractTableConstraint<GBase8aTable, ? extends GBase8aTableConstraintColumn> constraint;
    private final GBase8aTableColumn tableColumn;
    private final int ordinalPosition;

    public GBase8aTableConstraintColumn(AbstractTableConstraint<GBase8aTable, ? extends GBase8aTableConstraintColumn> constraint,
                                        GBase8aTableColumn tableColumn, int ordinalPosition) {
        this.constraint = constraint;
        this.tableColumn = tableColumn;
        this.ordinalPosition = ordinalPosition;
    }

    @Property(viewable = true, order = 1)
    @NotNull
    @Override
    public String getName() {
        return this.tableColumn.getName();
    }


    @NotNull
    @Override
    @Property(viewable = true, order = 2)
    public GBase8aTableColumn getAttribute() {
        return this.tableColumn;
    }


    @Override
    @Property(viewable = false, order = 3)
    public int getOrdinalPosition() {
        return this.ordinalPosition;
    }


    @Nullable
    @Override
    public String getDescription() {
        return this.tableColumn.getDescription();
    }

    @Override
    public AbstractTableConstraint<GBase8aTable, ? extends GBase8aTableConstraintColumn> getParentObject() {
        return this.constraint;
    }

    @NotNull
    @Override
    public GBase8aDataSource getDataSource() {
        return this.constraint.getTable().getDataSource();
    }
}
