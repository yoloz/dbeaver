package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableConstraint;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSEntityAttributeRef;
import org.jkiss.dbeaver.model.struct.DBSEntityConstraint;
import org.jkiss.dbeaver.model.struct.DBSEntityConstraintType;
import org.jkiss.dbeaver.model.struct.DBSEntityReferrer;

import java.util.ArrayList;
import java.util.List;

public class GBase8aTableConstraint extends JDBCTableConstraint<GBase8aTable, GBase8aTableConstraintColumn> {

    private List<GBase8aTableConstraintColumn> columns;

    public GBase8aTableConstraint(GBase8aTable table, String name, String remarks, DBSEntityConstraintType constraintType, boolean persisted) {
        super(table, name, remarks, constraintType, persisted);
    }

    protected GBase8aTableConstraint(DBRProgressMonitor monitor, GBase8aTable table, DBSEntityConstraint source) throws DBException {
        super(table, source, false);
        if (source instanceof DBSEntityReferrer) {
            List<? extends DBSEntityAttributeRef> columns = ((DBSEntityReferrer) source).getAttributeReferences(monitor);
            if (columns != null) {
                this.columns = new ArrayList<>(columns.size());
                for (DBSEntityAttributeRef col : columns) {
                    if (col.getAttribute() != null) {
                        GBase8aTableColumn ownCol = table.getAttribute(monitor, col.getAttribute().getName());
                        this.columns.add(new GBase8aTableConstraintColumn(this, ownCol, col.getAttribute().getOrdinalPosition()));
                    }
                }
            }
        }
    }

    @Override
    public List<GBase8aTableConstraintColumn> getAttributeReferences(DBRProgressMonitor monitor) throws DBException {
        return this.columns;
    }

    @Override
    public void setAttributeReferences(List<GBase8aTableConstraintColumn> gBase8aTableConstraintColumns) throws DBException {
        this.columns = gBase8aTableConstraintColumns;
    }

    @Override
    public String getFullyQualifiedName(DBPEvaluationContext context) {
        return DBUtils.getFullQualifiedName(getDataSource(), getTable().getContainer(), getTable(), this);
    }

    @Override
    public DBPDataSource getDataSource() {
        return getTable().getDataSource();
    }

    public void addColumn(GBase8aTableConstraintColumn column) {
        if (this.columns == null) {
            this.columns = new ArrayList<>();
        }
        this.columns.add(column);
    }

    void setColumns(List<GBase8aTableConstraintColumn> columns) {
        this.columns = columns;
    }


}
