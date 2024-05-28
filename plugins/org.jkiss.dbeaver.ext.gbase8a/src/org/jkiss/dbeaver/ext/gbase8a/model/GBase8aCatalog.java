package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aConstants;
import org.jkiss.dbeaver.ext.gbase8a.internal.GBase8aActivator;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBPRefreshableObject;
import org.jkiss.dbeaver.model.DBPSystemObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCObjectCache;
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
import org.jkiss.dbeaver.model.meta.Association;
import org.jkiss.dbeaver.model.struct.rdb.DBSCatalog;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureContainer;
import org.jkiss.utils.CommonUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class GBase8aCatalog implements DBSCatalog, DBPRefreshableObject, DBPSystemObject,DBSObjectContainer {

    Log log = Log.getLog(GBase8aCatalog.class);

    final TableCache tableCache = new TableCache();
    final ProceduresCache proceduresCache = new ProceduresCache();
    final FunctionsCache functionsCache = new FunctionsCache();
    final TriggerCache triggerCache = new TriggerCache();
    final ConstraintCache constraintCache = new ConstraintCache(this.tableCache);
    final IndexCache indexCache = new IndexCache(this.tableCache);
    final FullIndexCache fullIndexCache = new FullIndexCache(this.tableCache);
    final EventCache eventCache = new EventCache();
    final TableSpaceCache tablespaceCache = new TableSpaceCache();

    final GBase8aDataSource dataSource;

    private String name;
    private GBase8aCharset defaultCharset;
    private GBase8aCollation defaultCollation;
    private String sqlPath;
    private boolean persisted;
    private final GBase8aVC vc;

    public GBase8aCatalog(GBase8aDataSource dataSource, ResultSet dbResult, GBase8aVC vc) {
        this.tableCache.setCaseSensitive(false);
        this.dataSource = dataSource;
        this.vc = vc;
        if (dbResult != null) {
            this.name = JDBCUtils.safeGetString(dbResult, "SCHEMA_NAME");
            this.defaultCharset = dataSource.getCharset(JDBCUtils.safeGetString(dbResult, "DEFAULT_CHARACTER_SET_NAME"));
            this.defaultCollation = dataSource.getCollation(JDBCUtils.safeGetString(dbResult, "DEFAULT_COLLATION_NAME"));
            this.sqlPath = JDBCUtils.safeGetString(dbResult, "SQL_PATH");
            this.persisted = true;
        } else {
            this.defaultCharset = dataSource.getCharset("utf8");
            this.defaultCollation = dataSource.getCollation("utf8_general_ci");
            this.sqlPath = "";
            this.persisted = false;
        }
    }

    public DBSObject getParentObject() {
        if (this.vc == null) {
            return this.dataSource.getContainer();
        } else {
            return this.vc.getDataSource().getContainer();
        }
    }

    @NotNull
    public GBase8aDataSource getDataSource() {
        return this.dataSource;
    }

    public String getVcName() {
        return this.vc.getName();
    }

    @Property(viewable = true, order = 1)
    @NotNull
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPersisted() {
        return this.persisted;
    }

    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }

    @Nullable
    public String getDescription() {
        return null;
    }

    @Property(viewable = true, order = 2)
    public String getDefaultCharsets() {
        return this.defaultCharset.toString();
    }

    public GBase8aCharset getDefaultCharset() {
        return this.defaultCharset;
    }

    public void setDefaultCharset(GBase8aCharset defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    @Property(viewable = true, order = 3)
    public String getDefaultCollations() {
        return this.defaultCollation.toString();
    }

    public GBase8aCollation getDefaultCollation() {
        return this.defaultCollation;
    }

    public void setDefaultCollation(GBase8aCollation defaultCollation) {
        this.defaultCollation = defaultCollation;
    }

    @Property(viewable = true, order = 3)
    public String getSqlPath() {
        return this.sqlPath;
    }

    void setSqlPath(String sqlPath) {
        this.sqlPath = sqlPath;
    }

    public TableCache getTableCache() {
        return this.tableCache;
    }

    public ProceduresCache getProceduresCache() {
        return this.proceduresCache;
    }

    public FunctionsCache getFunctionsCache() {
        return this.functionsCache;
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

    @Association
    public Collection<GBase8aTableIndex> getIndexes(DBRProgressMonitor monitor) throws DBException {
        return this.indexCache.getObjects(monitor, this, null);
    }

    @Association
    public Collection<GBase8aTable> getTables(DBRProgressMonitor monitor) throws DBException {
        log.debug("getTables===========================");
        return this.tableCache.getTypedObjects(monitor, this, GBase8aTable.class);
    }

    public GBase8aTable getTable(DBRProgressMonitor monitor, String name) throws DBException {
        return this.tableCache.getObject(monitor, this, name, GBase8aTable.class);
    }

    @Association
    public Collection<GBase8aView> getViews(DBRProgressMonitor monitor) throws DBException {
        return this.tableCache.getTypedObjects(monitor, this, GBase8aView.class);
    }

    @Association
    public Collection<GBase8aProcedure> getProcedures(DBRProgressMonitor monitor) throws DBException {
        return this.proceduresCache.getAllObjects(monitor, this);
    }

    public GBase8aProcedure getProcedure(DBRProgressMonitor monitor, String procName) throws DBException {
        return this.proceduresCache.getObject(monitor, this, procName);
    }

    @Association
    public Collection<GBase8aFunction> getFunctions(DBRProgressMonitor monitor) throws DBException {
        return this.functionsCache.getAllObjects(monitor, this);
    }

    public GBase8aFunction getFunction(DBRProgressMonitor monitor, String procName) throws DBException {
        return this.functionsCache.getObject(monitor, this, procName);
    }

    @Association
    public Collection<GBase8aTrigger> getTriggers(DBRProgressMonitor monitor) throws DBException {
        return this.triggerCache.getAllObjects(monitor, this);
    }

    public GBase8aTrigger getTrigger(DBRProgressMonitor monitor, String name) throws DBException {
        return this.triggerCache.getObject(monitor, this, name);
    }

    @Association
    public Collection<GBase8aEvent> getEvents(DBRProgressMonitor monitor) throws DBException {
        return this.eventCache.getAllObjects(monitor, this);
    }

    @Association
    public Collection<GBase8aTableSpace> getTablespaces(DBRProgressMonitor monitor) throws DBException {
        return this.tablespaceCache.getAllObjects(monitor, this);
    }

    public Collection<GBase8aTableBase> getChildren(@NotNull DBRProgressMonitor monitor) throws DBException {
        return this.tableCache.getAllObjects(monitor, this);
    }

    public GBase8aTableBase getChild(@NotNull DBRProgressMonitor monitor, @NotNull String childName) throws DBException {
        return this.tableCache.getObject(monitor, this, childName);
    }

    @Override
    public Class<? extends DBSObject> getPrimaryChildType(DBRProgressMonitor monitor) throws DBException {
        return null;
    }

    public Class<? extends DBSEntity> getChildType(@NotNull DBRProgressMonitor monitor) throws DBException {
        return GBase8aTable.class;
    }

    public synchronized void cacheStructure(@NotNull DBRProgressMonitor monitor, int scope) throws DBException {
        monitor.subTask("Cache tables");
        this.tableCache.getAllObjects(monitor, this);
        if ((scope & 0x2) != 0) {
            monitor.subTask("Cache table columns");
            this.tableCache.loadChildren(monitor, this, null);
        }
        if ((scope & 0x4) != 0) {
            monitor.subTask("Cache table constraints");
            this.constraintCache.getAllObjects(monitor, this);
        }
    }

    @Override
    public synchronized DBSObject refreshObject(@NotNull DBRProgressMonitor monitor) throws DBException {
        this.tableCache.clearCache();
        this.indexCache.clearCache();
        this.constraintCache.clearCache();
        this.proceduresCache.clearCache();
        this.functionsCache.clearCache();
        this.triggerCache.clearCache();
        return this;
    }

    public boolean isSystem() {
        return !(!"information_schema".equalsIgnoreCase(getName()) &&
                !"gbase".equalsIgnoreCase(getName()) &&
                !"performance_schema".equalsIgnoreCase(getName()) &&
                !"gclusterdb".equalsIgnoreCase(getName()) &&
                !"gctmpdb".equalsIgnoreCase(getName()));
    }

    public String toString() {
        return this.name + " [" + this.dataSource.getContainer().getName() + "]";
    }

    public static class TableCache extends JDBCStructLookupCache<GBase8aCatalog, GBase8aTableBase, GBase8aTableColumn> {

        protected TableCache() {
            super("TABLE_NAME");
        }

        @NotNull
        public JDBCStatement prepareLookupStatement(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner, @Nullable GBase8aTableBase object,
                                                    @Nullable String objectName) throws SQLException {
            boolean useTreeOptimization = GBase8aActivator.getDefault().getPreferenceStore().getBoolean("navigator.use.tree.optimization");
            String sql = "";
            if (useTreeOptimization) {
                sql = "select tbName,'BASE TABLE' as TABLE_TYPE from gbase.table_distribution where dbName='"
                        + DBUtils.getQuotedIdentifier(owner) + "'" + ((
                        object == null && objectName == null) ?
                        "" : (
                        " and tbName LIKE '" + ((object != null) ? object.getName() : objectName) + "'"));
                if (owner.getDataSource().isVCCluster() && !(owner instanceof GBase8aSysCatalog)) {
                    sql = "select tbName,'BASE TABLE' as TABLE_TYPE from gbase.table_distribution where dbName='"
                            + DBUtils.getQuotedIdentifier(owner) + "'" + ((
                            object == null && objectName == null) ?
                            "" : (
                            " and tbName LIKE '" + ((object != null) ? object.getName() : objectName) + "'")) +
                            " and vc_id=(select id from information_schema.vc where name = '" + owner.getVcName() + "') ";
                }
            } else {
                sql = "SHOW FULL TABLES FROM " + DBUtils.getQuotedIdentifier(owner) + ((
                        object == null && objectName == null) ?
                        "" : (
                        " LIKE '" + ((object != null) ? object.getName() : objectName) + "'"));
                if (owner.getDataSource().isVCCluster() && !(owner instanceof GBase8aSysCatalog)) {
                    sql = "SHOW FULL TABLES FROM " + owner.getVcName() + "." + DBUtils.getQuotedIdentifier(owner) + ((
                            object == null && objectName == null) ?
                            "" : (
                            " LIKE '" + ((object != null) ? object.getName() : objectName) + "'"));
                }
            }
            return session.prepareStatement(sql);
        }

        protected GBase8aTableBase fetchObject(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner, @NotNull JDBCResultSet dbResult) {
            String tableType = JDBCUtils.safeGetString(dbResult, "TABLE_TYPE");
            if (tableType != null && tableType.contains("VIEW")) {
                return new GBase8aView(owner, dbResult);
            }
            return new GBase8aTable(owner, dbResult);
        }

        protected JDBCStatement prepareChildrenStatement(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner, @Nullable GBase8aTableBase forTable)
                throws SQLException {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ").append("information_schema.COLUMNS").append(" WHERE ").append("TABLE_SCHEMA").append("=?");
            if (forTable != null) {
                sql.append(" AND ").append("TABLE_NAME").append("=?");
            }
            if (owner.getDataSource().isVCCluster() && !(owner instanceof GBase8aSysCatalog)) {
                sql.append(" AND ").append("TABLE_VC").append("=?");
            }
            sql.append(" ORDER BY ").append("ORDINAL_POSITION");
            JDBCPreparedStatement dbStat = session.prepareStatement(sql.toString());
            dbStat.setString(1, owner.getName());
            int i = 1;
            if (forTable != null) {
                dbStat.setString(2, forTable.getName());
                i = 2;
            }
            if (owner.getDataSource().isVCCluster() && !(owner instanceof GBase8aSysCatalog)) {
                dbStat.setString(i + 1, owner.getVcName());
            }
            return dbStat;
        }

        protected GBase8aTableColumn fetchChild(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner, @NotNull GBase8aTableBase table,
                                                @NotNull JDBCResultSet dbResult) throws SQLException, DBException {
            return new GBase8aTableColumn(table, dbResult);
        }
    }

    static class IndexCache extends JDBCCompositeCache<GBase8aCatalog, GBase8aTable, GBase8aTableIndex, GBase8aTableIndexColumn> {

        protected IndexCache(TableCache tableCache) {
            super(tableCache, GBase8aTable.class, "TABLE_NAME", "INDEX_NAME");
        }

        @NotNull
        protected JDBCStatement prepareObjectsStatement(JDBCSession session, GBase8aCatalog owner, GBase8aTable forTable) throws SQLException {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ").append("information_schema.STATISTICS").append(" WHERE ").append("TABLE_SCHEMA").append("=?");
            if (forTable != null) {
                sql.append(" AND ").append("TABLE_NAME").append("=?");
            }
            if (owner.getDataSource().isVCCluster() && !(owner instanceof GBase8aSysCatalog)) {
                sql.append(" AND ").append("TABLE_VC").append("=?");
            }
            sql.append(" AND index_type='GLOBAL HASH'");
            sql.append(" ORDER BY ").append("INDEX_NAME").append(",").append("SEQ_IN_INDEX");
            JDBCPreparedStatement dbStat = session.prepareStatement(sql.toString());
            dbStat.setString(1, owner.getName());
            int i = 1;
            if (forTable != null) {
                dbStat.setString(2, forTable.getName());
                i = 2;
            }
            if (owner.getDataSource().isVCCluster() && !(owner instanceof GBase8aSysCatalog)) {
                dbStat.setString(i + 1, owner.getVcName());
            }
            return dbStat;
        }

        @Nullable
        protected GBase8aTableIndex fetchObject(JDBCSession session, GBase8aCatalog owner, GBase8aTable parent, String indexName, JDBCResultSet dbResult) throws SQLException, DBException {
            DBSIndexType indexType;
            String indexTypeName = JDBCUtils.safeGetString(dbResult, "INDEX_TYPE");
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
        protected GBase8aTableIndexColumn[] fetchObjectRow(JDBCSession session, GBase8aTable parent, GBase8aTableIndex object, JDBCResultSet dbResult) throws SQLException, DBException {
            int ordinalPosition = JDBCUtils.safeGetInt(dbResult, "SEQ_IN_INDEX");
            String columnName = JDBCUtils.safeGetStringTrimmed(dbResult, "COLUMN_NAME");
            String ascOrDesc = JDBCUtils.safeGetStringTrimmed(dbResult, "COLLATION");
            boolean nullable = "YES".equals(JDBCUtils.safeGetStringTrimmed(dbResult, "NULLABLE"));
            String subPart = JDBCUtils.safeGetStringTrimmed(dbResult, "SUB_PART");
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

        protected void cacheChildren(DBRProgressMonitor monitor, GBase8aTableIndex index, List<GBase8aTableIndexColumn> rows) {
            index.setColumns(rows);
        }
    }

    static class FullIndexCache extends JDBCCompositeCache<GBase8aCatalog, GBase8aTable, GBase8aTableFullIndex, GBase8aTableFullIndexColumn> {

        protected FullIndexCache(TableCache tableCache) {
            super(tableCache, GBase8aTable.class, "TABLE_NAME", "INDEX_NAME");
        }

        @NotNull
        protected JDBCStatement prepareObjectsStatement(JDBCSession session, GBase8aCatalog owner, GBase8aTable forTable) throws SQLException {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ").append("information_schema.STATISTICS").append(" WHERE ").append("TABLE_SCHEMA").append("=?");
            if (forTable != null) {
                sql.append(" AND ").append("TABLE_NAME").append("=?");
            }
            if (owner.getDataSource().isVCCluster() && !(owner instanceof GBase8aSysCatalog)) {
                sql.append(" AND ").append("TABLE_VC").append("=?");
            }
            sql.append(" AND index_type='FULLTEXT'");
            sql.append(" ORDER BY ").append("INDEX_NAME").append(",").append("SEQ_IN_INDEX");
            JDBCPreparedStatement dbStat = session.prepareStatement(sql.toString());
            dbStat.setString(1, owner.getName());
            int i = 1;
            if (forTable != null) {
                dbStat.setString(2, forTable.getName());
                i = 2;
            }
            if (owner.getDataSource().isVCCluster() && !(owner instanceof GBase8aSysCatalog)) {
                dbStat.setString(i + 1, owner.getVcName());
            }
            return dbStat;
        }

        @Nullable
        protected GBase8aTableFullIndex fetchObject(JDBCSession session, GBase8aCatalog owner, GBase8aTable parent, String indexName, JDBCResultSet dbResult) throws SQLException, DBException {
            DBSIndexType indexType;
            String indexTypeName = JDBCUtils.safeGetString(dbResult, "INDEX_TYPE");
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
            int ordinalPosition = JDBCUtils.safeGetInt(dbResult, "SEQ_IN_INDEX");
            String columnName = JDBCUtils.safeGetStringTrimmed(dbResult, "COLUMN_NAME");
            String ascOrDesc = JDBCUtils.safeGetStringTrimmed(dbResult, "COLLATION");
            boolean nullable = "YES".equals(JDBCUtils.safeGetStringTrimmed(dbResult, "NULLABLE"));
            String subPart = JDBCUtils.safeGetStringTrimmed(dbResult, "SUB_PART");
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


    static class ConstraintCache extends JDBCCompositeCache<GBase8aCatalog, GBase8aTable, GBase8aTableConstraint, GBase8aTableConstraintColumn> {
        protected ConstraintCache(TableCache tableCache) {
            super(tableCache, GBase8aTable.class, "TABLE_NAME", "CONSTRAINT_NAME");
        }

        @NotNull
        protected JDBCStatement prepareObjectsStatement(JDBCSession session, GBase8aCatalog owner, GBase8aTable forTable) throws SQLException {
            StringBuilder sql = new StringBuilder(500);
            sql.append("SELECT kc.CONSTRAINT_NAME,kc.TABLE_NAME,kc.COLUMN_NAME,kc.ORDINAL_POSITION\n" +
                    "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE kc WHERE kc.TABLE_SCHEMA=? AND kc.REFERENCED_TABLE_NAME IS NULL");
            if (forTable != null) {
                sql.append(" AND kc.TABLE_NAME=?");
            }
            sql.append("\nORDER BY kc.CONSTRAINT_NAME,kc.ORDINAL_POSITION");
            JDBCPreparedStatement dbStat = session.prepareStatement(sql.toString());
            dbStat.setString(1, owner.getName());
            if (forTable != null) {
                dbStat.setString(2, forTable.getName());
            }
            return dbStat;
        }

        @Nullable
        protected GBase8aTableConstraint fetchObject(JDBCSession session, GBase8aCatalog owner, GBase8aTable parent, String constraintName, JDBCResultSet dbResult) throws SQLException, DBException {
            if (constraintName.equals("PRIMARY")) {
                return new GBase8aTableConstraint(
                        parent, constraintName, null, DBSEntityConstraintType.PRIMARY_KEY, true);
            }
            return new GBase8aTableConstraint(
                    parent, constraintName, null, DBSEntityConstraintType.UNIQUE_KEY, true);
        }

        @Nullable
        protected GBase8aTableConstraintColumn[] fetchObjectRow(JDBCSession session, GBase8aTable parent, GBase8aTableConstraint object, JDBCResultSet dbResult) throws SQLException, DBException {
            String columnName = JDBCUtils.safeGetString(dbResult, "COLUMN_NAME");
            GBase8aTableColumn column = parent.getAttribute(session.getProgressMonitor(), columnName);
            if (column == null) {
                log.warn("Column '" + columnName + "' not found in table '" + parent.getFullyQualifiedName(DBPEvaluationContext.DDL) + "'");
                return null;
            }
            int ordinalPosition = JDBCUtils.safeGetInt(dbResult, "ORDINAL_POSITION");
            return new GBase8aTableConstraintColumn[]{new GBase8aTableConstraintColumn(
                    object,
                    column,
                    ordinalPosition)};
        }

        protected void cacheChildren(DBRProgressMonitor monitor, GBase8aTableConstraint constraint, List<GBase8aTableConstraintColumn> rows) {
            constraint.setColumns(rows);
        }
    }

    static class ProceduresCache extends JDBCStructLookupCache<GBase8aCatalog, GBase8aProcedure, GBase8aProcedureParameter> {
        ProceduresCache() {
            super("PROCEDURE_NAME");
        }

        protected GBase8aProcedure fetchObject(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner, @NotNull JDBCResultSet dbResult) throws SQLException, DBException {
            return new GBase8aProcedure(owner, dbResult);
        }

        protected JDBCStatement prepareChildrenStatement(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner, @Nullable GBase8aProcedure procedure) throws SQLException {
            String catalogName = owner.getName();
            if (owner.getDataSource().isVCCluster() && !(owner instanceof GBase8aSysCatalog)) {
                catalogName = owner.getVcName() + "." + owner.getName();
            }
            return session.getMetaData().getProcedureColumns(
                    catalogName,
                    null,
                    (procedure == null) ? null : procedure.getName(),
                    "%").getSourceStatement();
        }

        protected GBase8aProcedureParameter fetchChild(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner, @NotNull GBase8aProcedure parent, @NotNull JDBCResultSet dbResult) throws SQLException, DBException {
            DBSProcedureParameterKind parameterType;
            String columnName = JDBCUtils.safeGetString(dbResult, "COLUMN_NAME");
            int columnTypeNum = JDBCUtils.safeGetInt(dbResult, "COLUMN_TYPE");
            int valueType = JDBCUtils.safeGetInt(dbResult, "DATA_TYPE");
            String typeName = JDBCUtils.safeGetString(dbResult, "TYPE_NAME");
            int position = JDBCUtils.safeGetInt(dbResult, "ORDINAL_POSITION");
            long columnSize = JDBCUtils.safeGetLong(dbResult, "LENGTH");
            boolean notNull = (JDBCUtils.safeGetInt(dbResult, "NULLABLE") == 0);
            int scale = JDBCUtils.safeGetInt(dbResult, "SCALE");
            int precision = JDBCUtils.safeGetInt(dbResult, "PRECISION");

            switch (columnTypeNum) {
                case 1:
                    parameterType = DBSProcedureParameterKind.IN;
                    break;
                case 2:
                    parameterType = DBSProcedureParameterKind.INOUT;
                    break;
                case 4:
                    parameterType = DBSProcedureParameterKind.OUT;
                    break;
                case 5:
                    parameterType = DBSProcedureParameterKind.RETURN;
                    break;
                case 3:
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
        public JDBCStatement prepareLookupStatement(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner, @Nullable GBase8aProcedure object, @Nullable String objectName) throws SQLException {
            JDBCPreparedStatement dbStat = session.prepareStatement(
                    "SELECT * FROM information_schema.ROUTINES\nWHERE ROUTINE_SCHEMA=?"
                            + ((object == null && objectName == null) ? "" : " AND ROUTINE_NAME=?")
                            + ((owner.getDataSource().isVCCluster() && !(owner instanceof GBase8aSysCatalog)) ? " AND ROUTINE_VC=?" : "") +
                            " AND ROUTINE_TYPE = 'PROCEDURE' " +
                            "\nORDER BY " + "ROUTINE_NAME");
            dbStat.setString(1, owner.getName());
            int i = 1;
            if (object != null || objectName != null) {
                dbStat.setString(2, (object != null) ? object.getName() : objectName);
                i = 2;
            }
            if (owner.getDataSource().isVCCluster() && !(owner instanceof GBase8aSysCatalog)) {
                dbStat.setString(i + 1, owner.getVcName());
            }
            return dbStat;
        }
    }


    static class FunctionsCache extends JDBCStructLookupCache<GBase8aCatalog, GBase8aFunction, GBase8aProcedureParameter> {
        FunctionsCache() {
            super("FUNCTION_NAME");
        }

        protected GBase8aFunction fetchObject(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner, @NotNull JDBCResultSet dbResult) throws SQLException, DBException {
            return new GBase8aFunction(owner, dbResult);
        }

        protected JDBCStatement prepareChildrenStatement(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner, @Nullable GBase8aFunction procedure) throws SQLException {
            String catalogName = owner.getName();
            if (owner.getDataSource().isVCCluster() && !(owner instanceof GBase8aSysCatalog)) {
                catalogName = owner.getVcName() + "." + owner.getName();
            }
            return session.getMetaData().getFunctionColumns(
                    catalogName,
                    null,
                    (procedure == null) ? null : procedure.getName(), "%").getSourceStatement();
        }

        protected GBase8aProcedureParameter fetchChild(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner, @NotNull GBase8aFunction parent, @NotNull JDBCResultSet dbResult) throws SQLException, DBException {
            return null;
        }

        @NotNull
        public JDBCStatement prepareLookupStatement(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner, @Nullable GBase8aFunction object, @Nullable String objectName) throws SQLException {
            JDBCPreparedStatement dbStat = session.prepareStatement(
                    "SELECT * FROM information_schema.ROUTINES\nWHERE ROUTINE_SCHEMA=?"
                            + ((object == null && objectName == null) ? "" : " AND ROUTINE_NAME=?")
                            + ((owner.getDataSource().isVCCluster() && !(owner instanceof GBase8aSysCatalog)) ? " AND ROUTINE_VC=?" : "") +
                            " AND ROUTINE_TYPE = 'FUNCTION' " +
                            "\nORDER BY " + "ROUTINE_NAME");
            dbStat.setString(1, owner.getName());
            int i = 1;
            if (object != null || objectName != null) {
                dbStat.setString(2, (object != null) ? object.getName() : objectName);
                i = 2;
            }
            if (owner.getDataSource().isVCCluster() && !(owner instanceof GBase8aSysCatalog)) {
                dbStat.setString(i + 1, owner.getVcName());
            }

            return dbStat;
        }
    }

    static class TriggerCache extends JDBCObjectCache<GBase8aCatalog, GBase8aTrigger> {
        protected JDBCStatement prepareObjectsStatement(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner) throws SQLException {
            return session.prepareStatement(
                    "SHOW FULL TRIGGERS FROM " + DBUtils.getQuotedIdentifier(owner));
        }

        protected GBase8aTrigger fetchObject(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner, @NotNull JDBCResultSet dbResult) throws SQLException, DBException {
            String tableName = JDBCUtils.safeGetString(dbResult, "TABLE");
            GBase8aTable triggerTable = CommonUtils.isEmpty(tableName) ? null : owner.getTable(session.getProgressMonitor(), tableName);
            return new GBase8aTrigger(owner, triggerTable, dbResult);
        }
    }

    static class EventCache extends JDBCObjectCache<GBase8aCatalog, GBase8aEvent> {
        protected JDBCStatement prepareObjectsStatement(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner) throws SQLException {
            JDBCPreparedStatement dbStat = session.prepareStatement(
                    "SELECT * FROM information_schema.EVENTS WHERE EVENT_SCHEMA=?");
            dbStat.setString(1, DBUtils.getQuotedIdentifier(owner));
            return dbStat;
        }

        protected GBase8aEvent fetchObject(@NotNull JDBCSession session, @NotNull GBase8aCatalog owner, @NotNull JDBCResultSet dbResult) throws SQLException, DBException {
            return new GBase8aEvent(owner, dbResult);
        }
    }

    static class TableSpaceCache extends JDBCObjectCache<GBase8aCatalog, GBase8aTableSpace> {
        protected JDBCStatement prepareObjectsStatement(JDBCSession session, GBase8aCatalog owner) throws SQLException {
            JDBCPreparedStatement dbStat = session.prepareStatement("SELECT * FROM information_schema.TABLESPACES WHERE DB_NAME=?");
            dbStat.setString(1, DBUtils.getQuotedIdentifier(owner));
            return dbStat;
        }

        protected GBase8aTableSpace fetchObject(JDBCSession session, GBase8aCatalog owner, JDBCResultSet resultSet) throws SQLException, DBException {
            return new GBase8aTableSpace(owner, resultSet);
        }
    }

}
