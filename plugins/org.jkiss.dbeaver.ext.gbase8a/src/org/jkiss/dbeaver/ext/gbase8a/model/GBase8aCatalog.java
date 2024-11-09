package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aConstants;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBPRefreshableObject;
import org.jkiss.dbeaver.model.DBPSaveableObject;
import org.jkiss.dbeaver.model.DBPSystemObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCConstants;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCObjectCache;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCObjectLookupCache;
import org.jkiss.dbeaver.model.meta.IPropertyValueListProvider;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSEntity;
import org.jkiss.dbeaver.model.struct.DBSEntityConstraintType;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSObjectContainer;
import org.jkiss.dbeaver.model.struct.rdb.DBSIndexType;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureParameterKind;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCCompositeCache;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCStructLookupCache;
import org.jkiss.dbeaver.model.struct.rdb.DBSCatalog;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureContainer;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureType;
import org.jkiss.utils.CommonUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GBase8aCatalog
 */
public class GBase8aCatalog implements
        DBSCatalog, DBPSaveableObject, DBPRefreshableObject, DBPSystemObject,
        DBSProcedureContainer, DBSObjectContainer {

    private final Log log = Log.getLog(GBase8aCatalog.class);

    final TableCache tableCache = new TableCache() {
        protected void detectCaseSensitivity(DBSObject object) {
            this.setCaseSensitive(!getDataSource().getSQLDialect().useCaseInsensitiveNameLookup());
        }
    };
    final ProceduresCache proceduresCache = new ProceduresCache();
    //    final FunctionsCache functionsCache = new FunctionsCache();
    final EventCache eventCache = new EventCache();
    final TableSpaceCache tablespaceCache = new TableSpaceCache();

    // container type is table
    final TriggerCache triggerCache = new TriggerCache();
    final ConstraintCache constraintCache = new ConstraintCache(this.tableCache);
    final IndexCache indexCache = new IndexCache(this.tableCache);
    final FullIndexCache fullIndexCache = new FullIndexCache(this.tableCache);

    final GBase8aDataSource dataSource;
    final String sqlPath;
    final String vcName;  // system database vcName is empty

    private String name;
    private GBase8aCharset defaultCharset;
    private GBase8aCollation defaultCollation;
    private boolean persisted;

    public GBase8aCatalog(GBase8aDataSource dataSource, ResultSet dbResult, String vcName) {
        this.tableCache.setCaseSensitive(false);
        this.dataSource = dataSource;
        if (dbResult != null) {
            this.vcName = JDBCUtils.safeGetString(dbResult, "VC_NAME");
            this.name = JDBCUtils.safeGetString(dbResult, "SCHEMA_NAME");
            this.defaultCharset = dataSource.getCharset(JDBCUtils.safeGetString(dbResult, "DEFAULT_CHARACTER_SET_NAME"));
            this.defaultCollation = dataSource.getCollation(JDBCUtils.safeGetString(dbResult, "DEFAULT_COLLATION_NAME"));
            this.sqlPath = JDBCUtils.safeGetString(dbResult, "SQL_PATH");
            this.persisted = true;
        } else {
            this.vcName = vcName;
            this.defaultCharset = dataSource.getCharset("utf8");
            this.defaultCollation = dataSource.getCollation("utf8_general_ci");
            this.sqlPath = "";
            this.persisted = false;
        }
    }

    @Override
    public DBSObject getParentObject() {
        return this.dataSource.getContainer();
    }

    @NotNull
    @Override
    public GBase8aDataSource getDataSource() {
        return this.dataSource;
    }

    @Nullable
    public String getVcName() {
        return this.vcName;
    }

    @Property(viewable = true, order = 1)
    @NotNull
    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isPersisted() {
        return this.persisted;
    }

    @Override
    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }

    @Nullable
    public String getDescription() {
        return null;
    }

    public void setDefaultCharset(GBase8aCharset defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    public GBase8aCharset getDefaultCharset() {
        return this.defaultCharset;
    }

    public GBase8aCollation getDefaultCollation() {
        return this.defaultCollation;
    }

    public void setDefaultCollation(GBase8aCollation defaultCollation) {
        this.defaultCollation = defaultCollation;
    }

    @Property(viewable = true, order = 2)
    public String getSqlPath() {
        return this.sqlPath;
    }

    public TableCache getTableCache() {
        return this.tableCache;
    }

    public ProceduresCache getProceduresCache() {
        return this.proceduresCache;
    }

    public TriggerCache getTriggerCache() {
        return this.triggerCache;
    }

    public ConstraintCache getConstraintCache() {
        return this.constraintCache;
    }

    public IndexCache getIndexCache() {
        return this.indexCache;
    }

    public FullIndexCache getFullIndexCache() {
        return this.fullIndexCache;
    }

    public EventCache getEventCache() {
        return this.eventCache;
    }

    public TableSpaceCache getTablespaceCache() {
        return this.tablespaceCache;
    }

    public Collection<GBase8aTable> getTables(DBRProgressMonitor monitor) throws DBException {
        return getTableCache().getTypedObjects(monitor, this, GBase8aTable.class);
    }

    public GBase8aTable getTable(DBRProgressMonitor monitor, String name) throws DBException {
        return getTableCache().getObject(monitor, this, name, GBase8aTable.class);
    }

    public Collection<GBase8aView> getViews(DBRProgressMonitor monitor) throws DBException {
        return getTableCache().getTypedObjects(monitor, this, GBase8aView.class);
    }

    public GBase8aView getView(DBRProgressMonitor monitor, String name) throws DBException {
        return getTableCache().getObject(monitor, this, name, GBase8aView.class);
    }

    public Collection<GBase8aProcedure> getProcedures(DBRProgressMonitor monitor) throws DBException {
        return getProceduresCache().getAllObjects(monitor, this);
    }

    public Collection<GBase8aProcedure> getProceduresOnly(DBRProgressMonitor monitor) throws DBException {
        return getProcedures(monitor)
                .stream()
                .filter(proc -> proc.getProcedureType() == DBSProcedureType.PROCEDURE)
                .collect(Collectors.toList());
    }

    public Collection<GBase8aProcedure> getFunctionsOnly(DBRProgressMonitor monitor) throws DBException {
        return getProcedures(monitor)
                .stream()
                .filter(proc -> proc.getProcedureType() == DBSProcedureType.FUNCTION)
                .collect(Collectors.toList());
    }

    @Override
    public GBase8aProcedure getProcedure(DBRProgressMonitor monitor, String procName) throws DBException {
        return getProceduresCache().getObject(monitor, this, procName);
    }

    public Collection<GBase8aEvent> getEvents(DBRProgressMonitor monitor) throws DBException {
        return getEventCache().getAllObjects(monitor, this);
    }

    public Collection<GBase8aTableSpace> getTablespaces(DBRProgressMonitor monitor) throws DBException {
        return getTablespaceCache().getAllObjects(monitor, this);
    }

    @Override
    public Collection<GBase8aTable> getChildren(@NotNull DBRProgressMonitor monitor) throws DBException {
        return getTables(monitor);
    }

    @Override
    public GBase8aTable getChild(@NotNull DBRProgressMonitor monitor, @NotNull String childName) throws DBException {
        return getTable(monitor, childName);
    }

    @NotNull
    @Override
    public Class<? extends DBSEntity> getPrimaryChildType(@Nullable DBRProgressMonitor monitor) throws DBException {
        return GBase8aTable.class;
    }

    @Override
    public synchronized void cacheStructure(@NotNull DBRProgressMonitor monitor, int scope) throws DBException {
        monitor.subTask("Cache tables");
        getTableCache().getAllObjects(monitor, this);
        if ((scope & STRUCT_ATTRIBUTES) != 0) {
            monitor.subTask("Cache table columns");
            getTableCache().loadChildren(monitor, this, null);
        }
        if ((scope & STRUCT_ASSOCIATIONS) != 0) {
            monitor.subTask("Cache table constraints");
            getConstraintCache().getAllObjects(monitor, this);
        }
    }

    @Override
    public synchronized DBSObject refreshObject(@NotNull DBRProgressMonitor monitor) throws DBException {
        getTableCache().clearCache();
        getIndexCache().clearCache();
        getConstraintCache().clearCache();
        getProceduresCache().clearCache();
//        getFunctionsCache().clearCache();
        getTriggerCache().clearCache();
        getEventCache().clearCache();
        return this;
    }

    @Override
    public boolean isSystem() {
        return isSystemCatalog();
    }

    public boolean isSystemCatalog() {
        return GBase8aConstants.INFO_SCHEMA_NAME.equalsIgnoreCase(getName()) ||
                GBase8aConstants.GBASE8A_SCHEMA_NAME.equalsIgnoreCase(getName()) ||
                GBase8aConstants.PERFORMANCE_SCHEMA.equalsIgnoreCase(getName()) ||
                GBase8aConstants.GCLUSTERDB.equalsIgnoreCase(getName()) ||
                GBase8aConstants.GCTEMDB.equalsIgnoreCase(getName());
    }

    @Override
    public String toString() {
        if (getVcName() != null && !getVcName().isEmpty()) {
            return getVcName() + "." + getName() + " [" + getDataSource().getContainer().getName() + "]";
        }
        return getName() + " [" + getDataSource().getContainer().getName() + "]";
    }

    public static class TableCache extends JDBCStructLookupCache<GBase8aCatalog, GBase8aTableBase, GBase8aTableColumn> {

        protected TableCache() {
            super(JDBCConstants.TABLE_NAME);
        }

        @NotNull
        @Override
        public JDBCStatement prepareLookupStatement(@NotNull JDBCSession session,
                                                    @NotNull GBase8aCatalog owner,
                                                    @Nullable GBase8aTableBase object,
                                                    @Nullable String objectName) throws SQLException {
            boolean useTreeOptimization = session.getDataSource().getContainer().getPreferenceStore().getBoolean("navigator.use.tree.optimization");
            String sql = "";
            if (useTreeOptimization) {
                sql = "select tbName,'BASE TABLE' as TABLE_TYPE from gbase.table_distribution where dbName='"
                        + DBUtils.getQuotedIdentifier(owner)
                        + "'"
                        + ((object == null && objectName == null) ? "" : (" and tbName LIKE '"
                        + ((object != null) ? object.getName() : objectName)
                        + "'"));
                if (!owner.isSystemCatalog()) {
                    sql = "select tbName,'BASE TABLE' as TABLE_TYPE from gbase.table_distribution where dbName='"
                            + DBUtils.getQuotedIdentifier(owner)
                            + "'"
                            + ((object == null && objectName == null) ? "" : (" and tbName LIKE '"
                            + ((object != null) ? object.getName() : objectName)
                            + "'"))
                            + " and vc_id=(select id from information_schema.vc where name = '"
                            + owner.getVcName()
                            + "') ";
                }
            } else {
                sql = "SHOW FULL TABLES FROM "
                        + DBUtils.getQuotedIdentifier(owner)
                        + ((object == null && objectName == null) ? "" : (" LIKE '"
                        + ((object != null) ? object.getName() : objectName)
                        + "'"));
                if (!owner.isSystemCatalog()) {
                    sql = "SHOW FULL TABLES FROM "
                            + owner.getVcName()
                            + "."
                            + DBUtils.getQuotedIdentifier(owner)
                            + ((object == null && objectName == null) ? "" : (" LIKE '"
                            + ((object != null) ? object.getName() : objectName)
                            + "'"));
                }
            }
            return session.prepareStatement(sql);
        }

        @Override
        protected GBase8aTableBase fetchObject(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner, @NotNull JDBCResultSet dbResult) {
            String tableType = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_TABLE_TYPE);
            if (tableType != null && tableType.contains("VIEW")) {
                return new GBase8aView(owner, dbResult);
            }
            return new GBase8aTable(owner, dbResult);
        }

        @Override
        protected JDBCStatement prepareChildrenStatement(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner, @Nullable GBase8aTableBase forTable)
                throws SQLException {
            StringBuilder sql = new StringBuilder().append("SELECT * FROM ")
                    .append(GBase8aConstants.META_TABLE_COLUMNS)
                    .append(" WHERE ")
                    .append(GBase8aConstants.COL_TABLE_SCHEMA)
                    .append("=?");
            if (forTable != null) {
                sql.append(" AND ").append(GBase8aConstants.COL_TABLE_NAME).append("=?");
            }
            if (!owner.isSystemCatalog()) {
                sql.append(" AND ").append(GBase8aConstants.COL_VC_NAME).append("=?");
            }
            sql.append(" ORDER BY ORDINAL_POSITION");
            JDBCPreparedStatement dbStat = session.prepareStatement(sql.toString());
            dbStat.setString(1, owner.getName());
            int i = 1;
            if (forTable != null) {
                dbStat.setString(2, forTable.getName());
                i = 2;
            }
            if (!owner.isSystemCatalog()) {
                dbStat.setString(i + 1, owner.getVcName());
            }
            return dbStat;
        }

        @Override
        protected GBase8aTableColumn fetchChild(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner, @NotNull GBase8aTableBase table,
                                                @NotNull JDBCResultSet dbResult) throws SQLException, DBException {
            return new GBase8aTableColumn(table, dbResult);
        }
    }

    /**
     * Index cache implementation
     */
    public static class IndexCache extends JDBCCompositeCache<GBase8aCatalog, GBase8aTable, GBase8aTableIndex, GBase8aTableIndexColumn> {

        IndexCache(TableCache tableCache) {
            super(tableCache, GBase8aTable.class, GBase8aConstants.COL_TABLE_NAME, GBase8aConstants.COL_INDEX_NAME);
        }

        @NotNull
        @Override
        protected JDBCStatement prepareObjectsStatement(JDBCSession session, GBase8aCatalog owner, GBase8aTable forTable) throws SQLException {
            StringBuilder sql = new StringBuilder()
                    .append("SELECT * FROM ")
                    .append(GBase8aConstants.META_TABLE_STATISTICS)
                    .append(" WHERE ").append(GBase8aConstants.COL_TABLE_SCHEMA)
                    .append("=?");
            if (forTable != null) {
                sql.append(" AND ").append(GBase8aConstants.COL_TABLE_NAME).append("=?");
            }
            if (!owner.isSystemCatalog()) {
                sql.append(" AND ").append(GBase8aConstants.COL_VC_NAME).append("=?");
            }
            sql.append(" AND index_type !='FULLTEXT' ORDER BY ")
                    .append(GBase8aConstants.COL_TABLE_NAME).append(",")
                    .append(GBase8aConstants.COL_INDEX_NAME).append(",")
                    .append(GBase8aConstants.COL_SEQ_IN_INDEX);
            JDBCPreparedStatement dbStat = session.prepareStatement(sql.toString());
            dbStat.setString(1, owner.getName());
            int i = 1;
            if (forTable != null) {
                dbStat.setString(2, forTable.getName());
                i = 2;
            }
            if (!owner.isSystemCatalog()) {
                dbStat.setString(i + 1, owner.getVcName());
            }
            return dbStat;
        }

        @Nullable
        @Override
        protected GBase8aTableIndex fetchObject(JDBCSession session, GBase8aCatalog owner, GBase8aTable parent,
                                                String indexName, JDBCResultSet dbResult) throws SQLException, DBException {
            DBSIndexType indexType;
            String indexTypeName = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_INDEX_TYPE);
            if (GBase8aConstants.INDEX_TYPE_BTREE.getId().equals(indexTypeName)) {
                indexType = GBase8aConstants.INDEX_TYPE_BTREE;
            } else if (GBase8aConstants.INDEX_TYPE_FULLTEXT.getId().equals(indexTypeName)) {
                indexType = GBase8aConstants.INDEX_TYPE_FULLTEXT;
            } else if (GBase8aConstants.INDEX_TYPE_HASH.getId().equals(indexTypeName)) {
                indexType = GBase8aConstants.INDEX_TYPE_HASH;
            } else if (GBase8aConstants.INDEX_TYPE_RTREE.getId().equals(indexTypeName)) {
                indexType = GBase8aConstants.INDEX_TYPE_RTREE;
            } else {
                indexType = DBSIndexType.OTHER;
            }
            return new GBase8aTableIndex(
                    parent,
                    indexName,
                    indexType,
                    dbResult);
        }

        @Nullable
        @Override
        protected GBase8aTableIndexColumn[] fetchObjectRow(JDBCSession session, GBase8aTable parent,
                                                           GBase8aTableIndex object, JDBCResultSet dbResult) throws SQLException, DBException {
            int ordinalPosition = JDBCUtils.safeGetInt(dbResult, GBase8aConstants.COL_SEQ_IN_INDEX);
            String columnName = JDBCUtils.safeGetStringTrimmed(dbResult, GBase8aConstants.COL_COLUMN_NAME);
            String ascOrDesc = JDBCUtils.safeGetStringTrimmed(dbResult, GBase8aConstants.COL_COLLATION);
            boolean nullable = "YES".equals(JDBCUtils.safeGetStringTrimmed(dbResult, GBase8aConstants.COL_NULLABLE));
            String subPart = JDBCUtils.safeGetStringTrimmed(dbResult, GBase8aConstants.COL_SUB_PART);
            GBase8aTableColumn tableColumn = (columnName == null) ? null : parent.getAttribute(session.getProgressMonitor(), columnName);
            if (tableColumn == null) {
                log.debug("Column '" + columnName + "' not found in table '" + parent.getName() + "' for index '" + object.getName() + "'");
                return null;
            }
            return new GBase8aTableIndexColumn[]{new GBase8aTableIndexColumn(
                    object,
                    tableColumn,
                    ordinalPosition,
                    "A".equalsIgnoreCase(ascOrDesc),
                    nullable,
                    subPart)};
        }

        @Override
        protected void cacheChildren(DBRProgressMonitor monitor, GBase8aTableIndex index, List<GBase8aTableIndexColumn> rows) {
            index.setColumns(rows);
        }
    }

    public static class FullIndexCache extends JDBCCompositeCache<GBase8aCatalog, GBase8aTable, GBase8aTableFullIndex, GBase8aTableFullIndexColumn> {

        protected FullIndexCache(TableCache tableCache) {
            super(tableCache, GBase8aTable.class, GBase8aConstants.COL_TABLE_NAME, GBase8aConstants.COL_INDEX_NAME);
        }

        @NotNull
        @Override
        protected JDBCStatement prepareObjectsStatement(JDBCSession session, GBase8aCatalog owner, GBase8aTable forTable) throws SQLException {
            StringBuilder sql = new StringBuilder()
                    .append("SELECT * FROM ")
                    .append(GBase8aConstants.META_TABLE_STATISTICS)
                    .append(" WHERE ").append(GBase8aConstants.COL_TABLE_SCHEMA)
                    .append("=?");
            if (forTable != null) {
                sql.append(" AND ").append(GBase8aConstants.COL_TABLE_NAME).append("=?");
            }
            if (!owner.isSystemCatalog()) {
                sql.append(" AND ").append(GBase8aConstants.COL_VC_NAME).append("=?");
            }
            sql.append(" AND index_type='FULLTEXT' ORDER BY ")
                    .append(GBase8aConstants.COL_TABLE_NAME).append(",")
                    .append(GBase8aConstants.COL_INDEX_NAME).append(",")
                    .append(GBase8aConstants.COL_SEQ_IN_INDEX);
            JDBCPreparedStatement dbStat = session.prepareStatement(sql.toString());
            dbStat.setString(1, owner.getName());
            int i = 1;
            if (forTable != null) {
                dbStat.setString(2, forTable.getName());
                i = 2;
            }
            if (!owner.isSystemCatalog()) {
                dbStat.setString(i + 1, owner.getVcName());
            }
            return dbStat;
        }

        @Nullable
        protected GBase8aTableFullIndex fetchObject(JDBCSession session, GBase8aCatalog owner, GBase8aTable parent, String indexName, JDBCResultSet dbResult) throws SQLException, DBException {
            DBSIndexType indexType;
            String indexTypeName = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_INDEX_TYPE);
            if (GBase8aConstants.INDEX_TYPE_BTREE.getId().equals(indexTypeName)) {
                indexType = GBase8aConstants.INDEX_TYPE_BTREE;
            } else if (GBase8aConstants.INDEX_TYPE_FULLTEXT.getId().equals(indexTypeName)) {
                indexType = GBase8aConstants.INDEX_TYPE_FULLTEXT;
            } else if (GBase8aConstants.INDEX_TYPE_HASH.getId().equals(indexTypeName)) {
                indexType = GBase8aConstants.INDEX_TYPE_HASH;
            } else if (GBase8aConstants.INDEX_TYPE_RTREE.getId().equals(indexTypeName)) {
                indexType = GBase8aConstants.INDEX_TYPE_RTREE;
            } else {
                indexType = DBSIndexType.OTHER;
            }
            return new GBase8aTableFullIndex(
                    parent,
                    indexName,
                    indexType,
                    dbResult);
        }

        @Nullable
        protected GBase8aTableFullIndexColumn[] fetchObjectRow(JDBCSession session, GBase8aTable parent, GBase8aTableFullIndex object, JDBCResultSet dbResult) throws SQLException, DBException {
            int ordinalPosition = JDBCUtils.safeGetInt(dbResult, GBase8aConstants.COL_SEQ_IN_INDEX);
            String columnName = JDBCUtils.safeGetStringTrimmed(dbResult, GBase8aConstants.COL_COLUMN_NAME);
            String ascOrDesc = JDBCUtils.safeGetStringTrimmed(dbResult, GBase8aConstants.COL_COLLATION);
            boolean nullable = "YES".equals(JDBCUtils.safeGetStringTrimmed(dbResult, GBase8aConstants.COL_NULLABLE));
            String subPart = JDBCUtils.safeGetStringTrimmed(dbResult, GBase8aConstants.COL_SUB_PART);
            GBase8aTableColumn tableColumn = (columnName == null) ? null : parent.getAttribute(session.getProgressMonitor(), columnName);
            if (tableColumn == null) {
                log.debug("Column '" + columnName + "' not found in table '" + parent.getName() + "' for index '" + object.getName() + "'");
                return null;
            }
            return new GBase8aTableFullIndexColumn[]{new GBase8aTableFullIndexColumn(
                    object,
                    tableColumn,
                    ordinalPosition,
                    "A".equalsIgnoreCase(ascOrDesc),
                    nullable,
                    subPart)};
        }

        protected void cacheChildren(DBRProgressMonitor monitor, GBase8aTableFullIndex index, List<GBase8aTableFullIndexColumn> rows) {
            index.setColumns(rows);
        }
    }

    /**
     * Constraint cache implementation
     */
    public static class ConstraintCache extends JDBCCompositeCache<GBase8aCatalog, GBase8aTable, GBase8aTableConstraint, GBase8aTableConstraintColumn> {
        protected ConstraintCache(TableCache tableCache) {
            super(tableCache, GBase8aTable.class, GBase8aConstants.COL_TABLE_NAME, GBase8aConstants.COL_CONSTRAINT_NAME);
        }

        @NotNull
        @Override
        protected JDBCStatement prepareObjectsStatement(JDBCSession session, GBase8aCatalog owner, GBase8aTable forTable) throws SQLException {
            StringBuilder sql = new StringBuilder()
                    .append("SELECT CONSTRAINT_NAME,TABLE_NAME,COLUMN_NAME,ORDINAL_POSITION FROM ")
                    .append(GBase8aConstants.META_TABLE_KEY_COLUMN_USAGE)
                    .append(" WHERE ")
                    .append(GBase8aConstants.COL_TABLE_SCHEMA)
                    .append("=? AND REFERENCED_TABLE_NAME IS NULL ");
            if (forTable != null) {
                sql.append(" AND TABLE_NAME=?");
            }
            if (!owner.isSystemCatalog()) {
                sql.append(" AND ").append(GBase8aConstants.COL_VC_NAME).append("=?");
            }
            sql.append(" ORDER BY CONSTRAINT_NAME,ORDINAL_POSITION");
            JDBCPreparedStatement dbStat = session.prepareStatement(sql.toString());
            dbStat.setString(1, owner.getName());
            int i = 2;
            if (forTable != null) {
                dbStat.setString(2, forTable.getName());
                i = 3;
            }
            if (!owner.isSystemCatalog()) {
                dbStat.setString(i, owner.getVcName());
            }
            return dbStat;
        }

        @Nullable
        @Override
        protected GBase8aTableConstraint fetchObject(JDBCSession session, GBase8aCatalog owner, GBase8aTable parent,
                                                     String constraintName, JDBCResultSet dbResult) throws SQLException, DBException {
            if (constraintName.equals(GBase8aConstants.INDEX_PRIMARY)) {
                return new GBase8aTableConstraint(parent, constraintName, null, DBSEntityConstraintType.PRIMARY_KEY, true);
            }
            return new GBase8aTableConstraint(parent, constraintName, null, DBSEntityConstraintType.UNIQUE_KEY, true);
        }

        @Nullable
        @Override
        protected GBase8aTableConstraintColumn[] fetchObjectRow(JDBCSession session, GBase8aTable parent,
                                                                GBase8aTableConstraint object, JDBCResultSet dbResult)
                throws SQLException, DBException {
            String columnName = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_COLUMN_NAME);
            GBase8aTableColumn column = parent.getAttribute(session.getProgressMonitor(), columnName);
            if (column == null) {
                log.warn("Column '" + columnName + "' not found in table '" + parent.getFullyQualifiedName(DBPEvaluationContext.DDL) + "'");
                return null;
            }
            int ordinalPosition = JDBCUtils.safeGetInt(dbResult, GBase8aConstants.COL_ORDINAL_POSITION);
            return new GBase8aTableConstraintColumn[]{new GBase8aTableConstraintColumn(
                    object,
                    column,
                    ordinalPosition)};
        }

        @Override
        protected void cacheChildren(DBRProgressMonitor monitor, GBase8aTableConstraint constraint, List<GBase8aTableConstraintColumn> rows) {
            constraint.setAttributeReferences(rows);
        }
    }

    /**
     * Procedures cache implementation
     */
    public static class ProceduresCache extends JDBCStructLookupCache<GBase8aCatalog, GBase8aProcedure, GBase8aProcedureParameter> {
        ProceduresCache() {
            super(JDBCConstants.PROCEDURE_NAME);
        }

        @Override
        protected GBase8aProcedure fetchObject(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner,
                                               @NotNull JDBCResultSet dbResult) throws SQLException, DBException {
            return new GBase8aProcedure(owner, dbResult);
        }

        @Override
        protected JDBCStatement prepareChildrenStatement(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner,
                                                         @Nullable GBase8aProcedure procedure) throws SQLException {
            String catalogName = DBUtils.getQuotedIdentifier(owner);
            if (!owner.isSystemCatalog()) {
                catalogName = owner.getVcName() + "." + catalogName;
            }
            return session.getMetaData().getProcedureColumns(
                    catalogName,
                    null,
                    (procedure == null) ? null : JDBCUtils.escapeWildCards(session, procedure.getName()),
                    "%").getSourceStatement();
        }

        @Override
        protected GBase8aProcedureParameter fetchChild(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner,
                                                       @NotNull GBase8aProcedure parent, @NotNull JDBCResultSet dbResult)
                throws SQLException, DBException {
            DBSProcedureParameterKind parameterType;
            String columnName = JDBCUtils.safeGetString(dbResult, JDBCConstants.COLUMN_NAME);
            int columnTypeNum = JDBCUtils.safeGetInt(dbResult, JDBCConstants.COLUMN_TYPE);
            int valueType = JDBCUtils.safeGetInt(dbResult, JDBCConstants.DATA_TYPE);
            String typeName = JDBCUtils.safeGetString(dbResult, JDBCConstants.TYPE_NAME);
            int position = JDBCUtils.safeGetInt(dbResult, JDBCConstants.ORDINAL_POSITION);
            long columnSize = JDBCUtils.safeGetLong(dbResult, JDBCConstants.LENGTH);
            boolean notNull = (JDBCUtils.safeGetInt(dbResult, JDBCConstants.NULLABLE) == DatabaseMetaData.procedureNoNulls);
            int scale = JDBCUtils.safeGetInt(dbResult, JDBCConstants.SCALE);
            int precision = JDBCUtils.safeGetInt(dbResult, JDBCConstants.PRECISION);
            switch (columnTypeNum) {
                case DatabaseMetaData.procedureColumnIn:
                    parameterType = DBSProcedureParameterKind.IN;
                    break;
                case DatabaseMetaData.procedureColumnInOut:
                    parameterType = DBSProcedureParameterKind.INOUT;
                    break;
                case DatabaseMetaData.procedureColumnOut:
                    parameterType = DBSProcedureParameterKind.OUT;
                    break;
                case DatabaseMetaData.procedureColumnReturn:
                    parameterType = DBSProcedureParameterKind.RETURN;
                    break;
                case DatabaseMetaData.procedureColumnResult:
                    parameterType = DBSProcedureParameterKind.RESULTSET;
                    break;
                default:
                    parameterType = DBSProcedureParameterKind.UNKNOWN;
                    break;
            }
            if (CommonUtils.isEmpty(columnName) && parameterType == DBSProcedureParameterKind.RETURN) {
                columnName = "RETURN";
            }
            return new GBase8aProcedureParameter(
                    parent,
                    columnName,
                    typeName,
                    valueType,
                    position,
                    columnSize,
                    scale, precision, notNull,
                    parameterType);
        }

        @NotNull
        @Override
        public JDBCStatement prepareLookupStatement(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner,
                                                    @Nullable GBase8aProcedure object, @Nullable String objectName) throws SQLException {
            JDBCPreparedStatement dbStat = session.prepareStatement(
                    "SELECT * FROM "
                            + GBase8aConstants.META_TABLE_ROUTINES
                            + " WHERE "
                            + GBase8aConstants.COL_ROUTINE_SCHEMA
                            + "=?"
                            + ((object == null && objectName == null) ? "" : " AND " + GBase8aConstants.COL_ROUTINE_NAME + "=?")
                            + (!owner.isSystemCatalog() ? " AND " + GBase8aConstants.COL_ROUTINE_VC + "=?" : "")
                            + " ORDER BY "
                            + GBase8aConstants.COL_ROUTINE_NAME);
            dbStat.setString(1, owner.getName());
            int i = 2;
            if (object != null || objectName != null) {
                dbStat.setString(2, (object != null) ? object.getName() : objectName);
                i = 3;
            }
            if (!owner.isSystemCatalog()) {
                dbStat.setString(i, owner.getVcName());
            }
            return dbStat;
        }
    }

//    public static class FunctionsCache extends JDBCStructLookupCache<GBase8aCatalog, GBase8aFunction, GBase8aFunctionParameter> {
//        FunctionsCache() {
//            super(JDBCConstants.FUNCTION_NAME);
//        }
//
//        protected GBase8aFunction fetchObject(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner, @NotNull JDBCResultSet dbResult) throws SQLException, DBException {
//            return new GBase8aFunction(owner, dbResult);
//        }
//
//        protected JDBCStatement prepareChildrenStatement(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner, @Nullable GBase8aFunction function) throws SQLException {
//            String catalogName = DBUtils.getQuotedIdentifier(owner);
//            if (!owner.isSystemCatalog()) {
//                catalogName = owner.getVcName() + "." + catalogName;
//            }
//            return session.getMetaData().getFunctionColumns(
//                    catalogName,
//                    null,
//                    (function == null) ? null : JDBCUtils.escapeWildCards(session, function.getName()),
//                    "%").getSourceStatement();
//        }
//
//        protected GBase8aFunctionParameter fetchChild(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner, @NotNull GBase8aFunction parent, @NotNull JDBCResultSet dbResult) throws SQLException, DBException {
//            DBSProcedureParameterKind parameterType;
//            String columnName = JDBCUtils.safeGetString(dbResult, JDBCConstants.COLUMN_NAME);
//            int columnTypeNum = JDBCUtils.safeGetInt(dbResult, JDBCConstants.COLUMN_TYPE);
//            int valueType = JDBCUtils.safeGetInt(dbResult, JDBCConstants.DATA_TYPE);
//            String typeName = JDBCUtils.safeGetString(dbResult, JDBCConstants.TYPE_NAME);
//            int position = JDBCUtils.safeGetInt(dbResult, JDBCConstants.ORDINAL_POSITION);
//            long columnSize = JDBCUtils.safeGetLong(dbResult, JDBCConstants.LENGTH);
//            boolean notNull = (JDBCUtils.safeGetInt(dbResult, JDBCConstants.NULLABLE) == DatabaseMetaData.procedureNoNulls);
//            int scale = JDBCUtils.safeGetInt(dbResult, JDBCConstants.SCALE);
//            int precision = JDBCUtils.safeGetInt(dbResult, JDBCConstants.PRECISION);
//            switch (columnTypeNum) {
//                case DatabaseMetaData.procedureColumnIn:
//                    parameterType = DBSProcedureParameterKind.IN;
//                    break;
//                case DatabaseMetaData.procedureColumnInOut:
//                    parameterType = DBSProcedureParameterKind.INOUT;
//                    break;
//                case DatabaseMetaData.procedureColumnOut:
//                    parameterType = DBSProcedureParameterKind.OUT;
//                    break;
//                case DatabaseMetaData.procedureColumnReturn:
//                    parameterType = DBSProcedureParameterKind.RETURN;
//                    break;
//                case DatabaseMetaData.procedureColumnResult:
//                    parameterType = DBSProcedureParameterKind.RESULTSET;
//                    break;
//                default:
//                    parameterType = DBSProcedureParameterKind.UNKNOWN;
//                    break;
//            }
//            if (CommonUtils.isEmpty(columnName) && parameterType == DBSProcedureParameterKind.RETURN) {
//                columnName = "RETURN";
//            }
//            return new GBase8aFunctionParameter(
//                    parent,
//                    columnName,
//                    typeName,
//                    valueType,
//                    position,
//                    columnSize,
//                    scale, precision, notNull,
//                    parameterType);
//        }
//
//        @NotNull
//        public JDBCStatement prepareLookupStatement(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner, @Nullable GBase8aFunction object, @Nullable String objectName) throws SQLException {
//            JDBCPreparedStatement dbStat = session.prepareStatement(
//                    "SELECT * FROM "
//                            + GBase8aConstants.META_TABLE_ROUTINES
//                            + " WHERE "
//                            + GBase8aConstants.COL_ROUTINE_SCHEMA
//                            + "=?"
//                            + ((object == null && objectName == null) ? "" : " AND " + GBase8aConstants.COL_ROUTINE_NAME + "=?")
//                            + (!owner.isSystemCatalog() ? " AND " + GBase8aConstants.COL_ROUTINE_VC + "=?" : "") +
//                            " AND "
//                            + GBase8aConstants.COL_ROUTINE_TYPE
//                            + " ='FUNCTION' ORDER BY "
//                            + GBase8aConstants.COL_ROUTINE_NAME);
//            dbStat.setString(1, owner.getName());
//            int i = 2;
//            if (object != null || objectName != null) {
//                dbStat.setString(2, (object != null) ? object.getName() : objectName);
//                i = 3;
//            }
//            if (!owner.isSystemCatalog()) {
//                dbStat.setString(i, owner.getVcName());
//            }
//            return dbStat;
//        }
//    }

    public static class TriggerCache extends JDBCObjectLookupCache<GBase8aCatalog, GBase8aTrigger> {

        @Override
        protected GBase8aTrigger fetchObject(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner,
                                             @NotNull JDBCResultSet dbResult) throws SQLException, DBException {
            String tableName = JDBCUtils.safeGetString(dbResult, "TABLE");
            GBase8aTable triggerTable = CommonUtils.isEmpty(tableName) ? null : owner.getTable(session.getProgressMonitor(), tableName);
            return new GBase8aTrigger(owner, triggerTable, dbResult);
        }

        @Override
        public JDBCStatement prepareLookupStatement(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner,
                                                    @Nullable GBase8aTrigger object, @Nullable String objectName) throws SQLException {
            String catalogName = DBUtils.getQuotedIdentifier(owner);
            if (!owner.isSystemCatalog()) {
                catalogName = owner.getVcName() + "." + catalogName;
            }
            return session.prepareStatement("SHOW FULL TRIGGERS FROM " + catalogName);
        }

    }

    public static class EventCache extends JDBCObjectCache<GBase8aCatalog, GBase8aEvent> {

        @Override
        protected JDBCStatement prepareObjectsStatement(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner) throws SQLException {
            JDBCPreparedStatement dbStat = session.prepareStatement("SELECT * FROM information_schema.EVENTS WHERE EVENT_SCHEMA=?"
                    + (owner.isSystemCatalog() ? "" : " AND EVENT_VC=?"));
            dbStat.setString(1, DBUtils.getQuotedIdentifier(owner));
            if (!owner.isSystemCatalog()) {
                dbStat.setString(2, owner.getVcName());
            }
            return dbStat;
        }

        protected GBase8aEvent fetchObject(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner, @NotNull JDBCResultSet dbResult) throws SQLException, DBException {
            return new GBase8aEvent(owner, dbResult);
        }
    }

    public static class TableSpaceCache extends JDBCObjectCache<GBase8aCatalog, GBase8aTableSpace> {

        @Override
        protected JDBCStatement prepareObjectsStatement(JDBCSession session, GBase8aCatalog owner) throws SQLException {
            JDBCPreparedStatement dbStat = session.prepareStatement("SELECT * FROM information_schema.TABLESPACES WHERE DB_NAME=?"
                    + (owner.isSystemCatalog() ? "" : " AND VC_NAME=?"));
            dbStat.setString(1, DBUtils.getQuotedIdentifier(owner));
            if (!owner.isSystemCatalog()) {
                dbStat.setString(2, owner.getVcName());
            }
            return dbStat;
        }

        protected GBase8aTableSpace fetchObject(JDBCSession session, GBase8aCatalog owner, JDBCResultSet resultSet) throws SQLException, DBException {
            return new GBase8aTableSpace(owner, resultSet);
        }
    }


    public static class CharsetListProvider implements IPropertyValueListProvider<GBase8aCatalog> {
        @Override
        public boolean allowCustomValue() {
            return false;
        }

        @Override
        public Object[] getPossibleValues(GBase8aCatalog object) {
            return object.getDataSource().getCharsets().toArray();
        }
    }

    public static class CollationListProvider implements IPropertyValueListProvider<GBase8aCatalog> {
        @Override
        public boolean allowCustomValue() {
            return false;
        }

        @Override
        public Object[] getPossibleValues(GBase8aCatalog object) {
            if (object.defaultCharset == null) {
                return null;
            } else {
                return object.defaultCharset.getCollations().toArray();
            }
        }
    }

}
