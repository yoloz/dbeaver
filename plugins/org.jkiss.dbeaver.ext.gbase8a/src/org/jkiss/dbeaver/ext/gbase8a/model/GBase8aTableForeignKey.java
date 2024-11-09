package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableForeignKey;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSEntityAssociation;
import org.jkiss.dbeaver.model.struct.DBSEntityAttributeRef;
import org.jkiss.dbeaver.model.struct.DBSEntityReferrer;
import org.jkiss.dbeaver.model.struct.rdb.DBSForeignKeyModifyRule;

import java.util.ArrayList;
import java.util.List;

public class GBase8aTableForeignKey extends JDBCTableForeignKey<GBase8aTable, GBase8aTableForeignKeyColumn, GBase8aTableConstraint> {

    private List<GBase8aTableForeignKeyColumn> columns;

    public GBase8aTableForeignKey(GBase8aTable table, String name, String remarks, GBase8aTableConstraint referencedKey,
                                  DBSForeignKeyModifyRule deleteRule, DBSForeignKeyModifyRule updateRule, boolean persisted) {
        super(table, name, remarks, referencedKey, deleteRule, updateRule, persisted);
    }

    public GBase8aTableForeignKey(DBRProgressMonitor monitor, GBase8aTable table, DBSEntityAssociation source) throws DBException {
        super(monitor, table, source, false);
        if (source instanceof DBSEntityReferrer) {
            List<? extends DBSEntityAttributeRef> columns = ((DBSEntityReferrer) source).getAttributeReferences(monitor);
            if (columns != null) {
                this.columns = new ArrayList<>(columns.size());
                for (DBSEntityAttributeRef srcCol : columns) {
                    if (srcCol instanceof GBase8aTableForeignKeyColumn fkCol) {
                        this.columns.add(new GBase8aTableForeignKeyColumn(
                                this,
                                table.getAttribute(monitor, fkCol.getName()),
                                fkCol.getOrdinalPosition(),
                                table.getAttribute(monitor, fkCol.getReferencedColumn().getName())));
                    }
                }
            }
        }
    }

    @Override
    public List<GBase8aTableForeignKeyColumn> getAttributeReferences(DBRProgressMonitor monitor) {
        return this.columns;
    }

    @Override
    public void setAttributeReferences(List<GBase8aTableForeignKeyColumn> gBase8aTableForeignKeyColumns) throws DBException {
        this.columns = gBase8aTableForeignKeyColumns;
    }

    @NotNull
    @Override
    @Property(viewable = true, editable = true, updatable = true, listProvider = ConstraintModifyRuleListProvider.class, order = 5)
    public DBSForeignKeyModifyRule getDeleteRule() {
        return super.getDeleteRule();
    }

    @NotNull
    @Override
    @Property(viewable = true, editable = true, updatable = true, listProvider = ConstraintModifyRuleListProvider.class, order = 6)
    public DBSForeignKeyModifyRule getUpdateRule() {
        return super.getUpdateRule();
    }


    public void addColumn(GBase8aTableForeignKeyColumn column) {
        if (this.columns == null) {
            this.columns = new ArrayList<>();
        }
        this.columns.add(column);
    }

    public boolean hasColumn(GBase8aTableForeignKeyColumn column) {
        if (columns != null) {
            String columnName = column.getName();
            String refName = column.getReferencedColumn().getName();
            for (GBase8aTableForeignKeyColumn col : columns) {
                if (columnName.equals(col.getName()) &&
                        refName.equals(col.getReferencedColumn().getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @NotNull
    @Override
    public String getFullyQualifiedName(DBPEvaluationContext context) {
        return DBUtils.getFullQualifiedName(getDataSource(), getTable().getContainer(), getTable(), this);
    }

    @NotNull
    @Override
    public GBase8aDataSource getDataSource() {
        return getTable().getDataSource();
    }
}
