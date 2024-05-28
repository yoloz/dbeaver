package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableForeignKey;
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

    public void addColumn(GBase8aTableForeignKeyColumn column) {
        if (this.columns == null) {
            this.columns = new ArrayList<>();
        }
        this.columns.add(column);
    }

    @Override
    public String getFullyQualifiedName(DBPEvaluationContext context) {
        return DBUtils.getFullQualifiedName(getDataSource(), getTable().getContainer(), getTable(), this);
    }

    @Override
    public GBase8aDataSource getDataSource() {
        return getTable().getDataSource();
    }
}
