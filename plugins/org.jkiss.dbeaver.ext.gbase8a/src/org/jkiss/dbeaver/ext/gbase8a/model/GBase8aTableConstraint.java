package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBPScriptObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableConstraint;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSEntityAttributeRef;
import org.jkiss.dbeaver.model.struct.DBSEntityConstraint;
import org.jkiss.dbeaver.model.struct.DBSEntityConstraintType;
import org.jkiss.dbeaver.model.struct.DBSEntityReferrer;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBStructUtils;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GBase8aTableConstraint extends JDBCTableConstraint<GBase8aTable, GBase8aTableConstraintColumn> implements DBPScriptObject, DBSObject, GBase8aSourceObject {

    private List<GBase8aTableConstraintColumn> columns = new ArrayList<>();

    public GBase8aTableConstraint(GBase8aTable table, String name, String remarks, DBSEntityConstraintType constraintType, boolean persisted) {
        super(table, name, remarks, constraintType, persisted);
    }

    protected GBase8aTableConstraint(DBRProgressMonitor monitor, GBase8aTable table, DBSEntityConstraint source) throws DBException {
        super(table, source, false);
        if (source instanceof DBSEntityReferrer) {
            List<? extends DBSEntityAttributeRef> columns = ((DBSEntityReferrer) source).getAttributeReferences(monitor);
            if (columns != null) {
                this.columns.clear();
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
    public void addAttributeReference(DBSTableColumn column) throws DBException {
        this.columns.add(new GBase8aTableConstraintColumn(this, (GBase8aTableColumn) column, columns.size()));
    }

    @Override
    public void setAttributeReferences(List<GBase8aTableConstraintColumn> columns) {
        this.columns.clear();
        this.columns.addAll(columns);
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

    public void addColumn(GBase8aTableConstraintColumn column) {
        if (this.columns == null) {
            this.columns = new ArrayList<>();
        }
        this.columns.add(column);
    }

    @Override
    public String getObjectDefinitionText(DBRProgressMonitor monitor, Map<String, Object> options) throws DBException {
        return DBStructUtils.generateObjectDDL(monitor, this, options, false);
    }

    @Override
    public void setObjectDefinitionText(String paramString) throws DBException {
        throw new DBException("Constraints DDL is read-only");
    }
}
