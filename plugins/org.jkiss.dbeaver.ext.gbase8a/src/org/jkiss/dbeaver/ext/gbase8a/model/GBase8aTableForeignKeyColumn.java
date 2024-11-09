package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableForeignKeyColumn;


public class GBase8aTableForeignKeyColumn extends GBase8aTableConstraintColumn implements DBSTableForeignKeyColumn {

    private final GBase8aTableColumn referencedColumn;

    public GBase8aTableForeignKeyColumn(GBase8aTableForeignKey constraint, GBase8aTableColumn tableColumn,
                                        int ordinalPosition, GBase8aTableColumn referencedColumn) {
        super(constraint, tableColumn, ordinalPosition);
        this.referencedColumn = referencedColumn;
    }

    @Override
    @Property(id = "reference", viewable = true, order = 4)
    public GBase8aTableColumn getReferencedColumn() {
        return referencedColumn;
    }
}
