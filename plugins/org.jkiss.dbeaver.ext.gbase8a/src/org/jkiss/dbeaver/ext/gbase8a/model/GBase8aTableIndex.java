package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBPNamedObject2;
import org.jkiss.dbeaver.model.DBPRefreshableObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableIndex;
import org.jkiss.dbeaver.model.meta.Association;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.rdb.DBSIndexType;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableIndex;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableIndexColumn;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


public class GBase8aTableIndex extends JDBCTableIndex<GBase8aCatalog, GBase8aTable> implements DBPNamedObject2, DBPRefreshableObject {

    private final boolean nonUnique;
    private String additionalInfo;
    private final String indexComment;
    private long cardinality;
    private List<GBase8aTableIndexColumn> columns;

    public GBase8aTableIndex(GBase8aTable table, boolean nonUnique, String indexName, DBSIndexType indexType, String comment, boolean persisted) {
        super(table.getContainer(), table, indexName, indexType, persisted);
        this.nonUnique = nonUnique;
        this.indexComment = comment;
    }

    GBase8aTableIndex(DBRProgressMonitor monitor, GBase8aTable table, DBSTableIndex source) throws DBException {
        super(table.getContainer(), table, source, false);
        this.nonUnique = !source.isUnique();
        this.indexComment = source.getDescription();
        if (source instanceof GBase8aTableIndex) {
            this.cardinality = ((GBase8aTableIndex) source).cardinality;
            this.additionalInfo = ((GBase8aTableIndex) source).additionalInfo;
        }
        List<? extends DBSTableIndexColumn> columns = source.getAttributeReferences(monitor);
        if (columns != null) {
            this.columns = new ArrayList<GBase8aTableIndexColumn>(columns.size());
            for (DBSTableIndexColumn sourceColumn : columns) {
                this.columns.add(new GBase8aTableIndexColumn(monitor, this, sourceColumn));
            }
        }
    }

    public GBase8aTableIndex(GBase8aTable parent, String indexName, DBSIndexType indexType, ResultSet dbResult) {
        super(parent.getContainer(), parent, indexName, indexType, true);
        this.nonUnique = (JDBCUtils.safeGetInt(dbResult, "NON_UNIQUE") != 0);
        this.cardinality = JDBCUtils.safeGetLong(dbResult, "cardinality");
        this.indexComment = JDBCUtils.safeGetString(dbResult, "INDEX_COMMENT");
        this.additionalInfo = JDBCUtils.safeGetString(dbResult, "COMMENT");
    }

    @NotNull
    public GBase8aDataSource getDataSource() {
        return getTable().getDataSource();
    }

    @Property(viewable = true, order = 5)
    public boolean isUnique() {
        return !this.nonUnique;
    }

    @Property(viewable = true, order = 100)
    @Nullable
    public String getDescription() {
        return this.indexComment;
    }

    @Property(viewable = true, order = 20)
    public long getCardinality() {
        return this.cardinality;
    }

    @Property(viewable = false, order = 30)
    public String getAdditionalInfo() {
        return this.additionalInfo;
    }

    @Association
    public List<GBase8aTableIndexColumn> getAttributeReferences(DBRProgressMonitor monitor) {
        return this.columns;
    }

    public GBase8aTableIndexColumn getColumn(String columnName) {
        return DBUtils.findObject(this.columns, columnName);
    }

    void setColumns(List<GBase8aTableIndexColumn> columns) {
        this.columns = columns;
    }

    public void addColumn(GBase8aTableIndexColumn column) {
        if (this.columns == null) {
            this.columns = new ArrayList<>();
        }
        this.columns.add(column);
    }

    @NotNull
    public String getFullyQualifiedName(DBPEvaluationContext context) {
        return DBUtils.getFullQualifiedName(getDataSource(), (getTable()).getContainer(), this);
    }

    public DBSObject refreshObject(DBRProgressMonitor monitor) throws DBException {
        return null;
    }
}
