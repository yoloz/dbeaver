package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBDatabaseException;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aConstants;
import org.jkiss.dbeaver.model.DBConstants;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBPReferentialIntegrityController;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCDatabaseMetaData;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCConstants;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCObjectCache;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.meta.IPropertyCacheValidator;
import org.jkiss.dbeaver.model.meta.IPropertyValueValidator;
import org.jkiss.dbeaver.model.meta.LazyProperty;
import org.jkiss.dbeaver.model.meta.PropertyGroup;
import org.jkiss.dbeaver.model.meta.PropertyLength;
import org.jkiss.dbeaver.model.struct.DBSEntityConstrainable;
import org.jkiss.dbeaver.model.struct.DBSEntityConstraintInfo;
import org.jkiss.dbeaver.model.struct.DBSEntityConstraintType;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;
import org.jkiss.dbeaver.model.struct.cache.SimpleObjectCache;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Association;
import org.jkiss.dbeaver.model.meta.IPropertyValueListProvider;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSEntity;
import org.jkiss.dbeaver.model.struct.DBSEntityAssociation;
import org.jkiss.dbeaver.model.struct.DBSEntityConstraint;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.rdb.DBSForeignKeyModifyRule;
import org.jkiss.dbeaver.model.struct.rdb.DBSPartitionContainer;
import org.jkiss.utils.CommonUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GBase8aTable extends GBase8aTableBase
        implements DBPReferentialIntegrityController, DBSPartitionContainer, DBSEntityConstrainable {

    private static final Log log = Log.getLog(GBase8aTable.class);

    private static final String INNODB_COMMENT = "InnoDB free";
    private static final String PARTITIONED_STATUS = "partitioned";

    public static class AdditionalInfo {
        private volatile boolean loaded = false;
        private long autoIncrement;
        private String description;
        private String oldDescription;
        private Date createTime;
        private GBase8aCharset charset;
        private GBase8aCollation collation;
        private GBase8aEngine engine;
        private String tableLimitSize;
        private boolean isRepliacte = false;
        private boolean btnIsCompress = false;
        private boolean oldBtnIsCompress = false;
        private String compressNum = "0";
        private String oldCompressNum = "0";
        private String compressString = "0";
        private String oldCompressString = "0";
        private boolean isNocopies = false;
        private final int hashColIndex = 0;
        private List<String> hashCols;
        private String createUser;
        private String tablespace;
//        private boolean partitioned;

        @Property(viewable = true, editable = true, updatable = true, listProvider = EngineListProvider.class,
                visibleIf = PartitionedTablePropertyValidator.class, order = 3)
        public GBase8aEngine getEngine() {
            return engine;
        }

        @Property(viewable = true, editable = true, updatable = true, visibleIf = PartitionedTablePropertyValidator.class, order = 4)
        public long getAutoIncrement() {
            return autoIncrement;
        }

        @Property(editable = true, updatable = true, listProvider = CharsetListProvider.class, visibleIf = PartitionedTablePropertyValidator.class, order = 5)
        public GBase8aCharset getCharset() {
            return this.charset;
        }

        @Property(editable = true, updatable = true, listProvider = CollationListProvider.class, visibleIf = PartitionedTablePropertyValidator.class, order = 6)
        public GBase8aCollation getCollation() {
            return this.collation;
        }

        @Property(category = DBConstants.CAT_STATISTICS, visibleIf = PartitionedTablePropertyValidator.class, order = 10)
        public String getTableLimitSize() {
            return this.tableLimitSize;
        }

        @Property(viewable = true, editable = true, updatable = true, length = PropertyLength.MULTILINE, visibleIf = PartitionedTablePropertyValidator.class, order = 100)
        public String getDescription() {
            return this.description;
        }

        @Property(visibleIf = PartitionedTablePropertyValidator.class, order = 18)
        public boolean isBtnIsCompress() {
            return this.btnIsCompress;
        }

        @Property(visibleIf = PartitionedTablePropertyValidator.class, order = 19)
        public String getCreateUser() {
            return this.createUser;
        }

        @Property(category = DBConstants.CAT_STATISTICS, visibleIf = PartitionedTablePropertyValidator.class, order = 20)
        public Date getCreateTime() {
            return this.createTime;
        }

        @Property(visibleIf = PartitionedTablePropertyValidator.class, order = 21)
        public boolean isRepliacte() {
            return this.isRepliacte;
        }

        @Property(visibleIf = PartitionedTablePropertyValidator.class, order = 22)
        public String getCompressNum() {
            return this.compressNum;
        }

        @Property(visibleIf = PartitionedTablePropertyValidator.class, order = 23)
        public String getCompressString() {
            return this.compressString;
        }

        @Property(visibleIf = PartitionedTablePropertyValidator.class, order = 24)
        public boolean isNocopies() {
            return this.isNocopies;
        }

//        @Property(viewable = true, visibleIf = PartitionedTablePropertyValidator.class, order = 25)
//        public boolean isPartitioned() {
//            return partitioned;
//        }

        public void setEngine(GBase8aEngine engine) {
            this.engine = engine;
        }

        public void setAutoIncrement(long autoIncrement) {
            this.autoIncrement = autoIncrement;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setCharset(GBase8aCharset charset) {
            this.charset = charset;
            this.collation = (charset == null) ? null : charset.getDefaultCollation();
        }

        public void setCollation(GBase8aCollation collation) {
            this.collation = collation;
        }

        public void setTableLimitSize(String tableLimitSize) {
            this.tableLimitSize = tableLimitSize;
        }

        public void setRepliacte(boolean isRepliacte) {
            this.isRepliacte = isRepliacte;
        }

        public void setBtnIsCompress(boolean btnIsCompress) {
            this.btnIsCompress = btnIsCompress;
        }

        public void setCompressNum(String compressNum) {
            this.compressNum = compressNum;
        }

        public void setCompressString(String compressString) {
            this.compressString = compressString;
        }

        public void setNocopies(boolean isNocopies) {
            this.isNocopies = isNocopies;
        }

        public List<String> getHashCols() {
            return this.hashCols;
        }

        public void setHashCols(List<String> hashCols) {
            this.hashCols = hashCols;
        }

        public String getOldDescription() {
            return this.oldDescription;
        }

        public void setOldDescription(String oldDescription) {
            this.oldDescription = oldDescription;
        }

        public boolean isOldBtnIsCompress() {
            return this.oldBtnIsCompress;
        }

        public void setOldBtnIsCompress(boolean oldBtnIsCompress) {
            this.oldBtnIsCompress = oldBtnIsCompress;
        }

        public String getOldCompressNum() {
            return this.oldCompressNum;
        }

        public void setOldCompressNum(String oldCompressNum) {
            this.oldCompressNum = oldCompressNum;
        }

        public String getOldCompressString() {
            return this.oldCompressString;
        }

        public void setOldCompressString(String oldCompressString) {
            this.oldCompressString = oldCompressString;
        }

        public void setCreateUser(String createUser) {
            this.createUser = createUser;
        }

        public void setTablespace(String tablespace) {
            this.tablespace = tablespace;
        }
    }

    public static class AdditionalInfoValidator implements IPropertyCacheValidator<GBase8aTable> {
        @Override
        public boolean isPropertyCached(GBase8aTable object, Object propertyId) {
            return object.additionalInfo.loaded;
        }
    }

    private final SimpleObjectCache<GBase8aTable, GBase8aTableForeignKey> foreignKeys = new SimpleObjectCache();
    private final PartitionCache partitionCache = new PartitionCache();

    private final AdditionalInfo additionalInfo = new AdditionalInfo();
    private volatile List<GBase8aTableForeignKey> referenceCache;

    @Nullable
    private String disableReferentialIntegrityStatement;
    @Nullable
    private String enableReferentialIntegrityStatement;


    public GBase8aTable(GBase8aCatalog catalog) {
        super(catalog);
    }

    // Copy constructor
    public GBase8aTable(DBRProgressMonitor monitor, GBase8aCatalog catalog, DBSEntity source) throws DBException {
        super(monitor, catalog, source);
        if (source instanceof GBase8aTable) {
            AdditionalInfo sourceAI = ((GBase8aTable) source).getAdditionalInfo(monitor);
            this.additionalInfo.loaded = true;
            this.additionalInfo.description = sourceAI.description;
            this.additionalInfo.charset = sourceAI.charset;
            this.additionalInfo.collation = sourceAI.collation;
            this.additionalInfo.tablespace = sourceAI.tablespace;
            this.additionalInfo.engine = sourceAI.engine;
            this.additionalInfo.tableLimitSize = sourceAI.tableLimitSize;
//            this.additionalInfo.partitioned = sourceAI.partitioned;
            // Copy triggers
            for (GBase8aTrigger srcTrigger : ((GBase8aTable) source).getTriggers(monitor)) {
                GBase8aTrigger trigger = new GBase8aTrigger(catalog, this, srcTrigger);
                getContainer().getTriggerCache().cacheObject(trigger);
            }
            // Copy partitions
            for (GBase8aPartition partition : ((GBase8aTable) source).getPartitionCache().getCachedObjects()) {
                getPartitionCache().cacheObject(new GBase8aPartition(monitor, this, partition, source));
            }
        }
        // Copy constraints
        for (DBSEntityConstraint srcConstr : CommonUtils.safeCollection(source.getConstraints(monitor))) {
            GBase8aTableConstraint constr = new GBase8aTableConstraint(monitor, this, srcConstr);
            getContainer().getConstraintCache().cacheObject(constr);
        }
        // Copy FKs
        List<GBase8aTableForeignKey> fkList = new ArrayList<GBase8aTableForeignKey>();
        for (DBSEntityAssociation srcFK : CommonUtils.safeCollection(source.getAssociations(monitor))) {
            GBase8aTableForeignKey fk = new GBase8aTableForeignKey(monitor, this, srcFK);
            if (fk.getReferencedConstraint() != null) {
                fk.setName(fk.getName() + "_copy"); // Fix FK name - they are unique within schema
                fkList.add(fk);
            } else {
                log.debug("Can't copy association '" + srcFK.getName() + "' - can't find referenced constraint");
            }
        }
        this.foreignKeys.setCache(fkList);
    }

    public GBase8aTable(GBase8aCatalog catalog, ResultSet dbResult) {
        super(catalog, dbResult);
    }

    @PropertyGroup()
    @LazyProperty(cacheValidator = AdditionalInfoValidator.class)
    public AdditionalInfo getAdditionalInfo(DBRProgressMonitor monitor) throws DBCException {
        synchronized (this.additionalInfo) {
            if (!this.additionalInfo.loaded) {
                loadAdditionalInfo(monitor);
            }
            return this.additionalInfo;
        }
    }

    @Override
    public boolean isView() {
        return false;
    }

//    public boolean hasPartitions() {
//        return additionalInfo.partitioned;
//    }

    @Override
    public synchronized Collection<GBase8aTableIndex> getIndexes(DBRProgressMonitor monitor) throws DBException {
        // Read indexes using cache
        return getContainer().getIndexCache().getObjects(monitor, getContainer(), this);
    }

    public GBase8aTableIndex getIndex(DBRProgressMonitor monitor, String name) throws DBException {
        return getContainer().getIndexCache().getObject(monitor, getContainer(), this, name);
    }

    public synchronized Collection<GBase8aTableFullIndex> getFullIndexes(DBRProgressMonitor monitor) throws DBException {
        // Read fullIndexes using cache
        return getContainer().getFullIndexCache().getObjects(monitor, getContainer(), this);
    }

    public GBase8aTableFullIndex getFullIndex(DBRProgressMonitor monitor, String name) throws DBException {
        return getContainer().getFullIndexCache().getObject(monitor, getContainer(), this, name);
    }

    @NotNull
    @Override
    public List<DBSEntityConstraintInfo> getSupportedConstraints() {
        List<DBSEntityConstraintInfo> result = new ArrayList<>();
        result.add(DBSEntityConstraintInfo.of(DBSEntityConstraintType.PRIMARY_KEY, GBase8aTableConstraint.class));
        result.add(DBSEntityConstraintInfo.of(DBSEntityConstraintType.UNIQUE_KEY, GBase8aTableConstraint.class));
        return result;
    }

    @Nullable
    @Override
    public synchronized Collection<GBase8aTableConstraint> getConstraints(@NotNull DBRProgressMonitor monitor) throws DBException {
        return getContainer().getConstraintCache().getObjects(monitor, getContainer(), this);
    }

    public GBase8aTableConstraint getConstraint(DBRProgressMonitor monitor, String ukName) throws DBException {
        return getContainer().getConstraintCache().getObject(monitor, getContainer(), this, ukName);
    }

    @Override
    public Collection<GBase8aTableForeignKey> getReferences(@NotNull DBRProgressMonitor monitor) throws DBException {
        if (referenceCache == null) {
            referenceCache = loadForeignKeys(monitor, true);
        }
        return referenceCache;
    }

    @Override
    public synchronized Collection<GBase8aTableForeignKey> getAssociations(@NotNull DBRProgressMonitor monitor) throws DBException {
        if (!getForeignKeyCache().isFullyCached() && getDataSource().getInfo().supportsReferentialIntegrity() && monitor != null) {
            List<GBase8aTableForeignKey> fkList = loadForeignKeys(monitor, false);
            getForeignKeyCache().setCache(fkList);
        }
        return getForeignKeyCache().getCachedObjects();
    }

    public GBase8aTableForeignKey getAssociation(DBRProgressMonitor monitor, String fkName) throws DBException {
        return DBUtils.findObject(getAssociations(monitor), fkName);
    }

    public DBSObjectCache<GBase8aTable, GBase8aTableForeignKey> getForeignKeyCache() {
        return this.foreignKeys;
    }

    @Nullable
    @Association
    public List<GBase8aTrigger> getTriggers(DBRProgressMonitor monitor) throws DBException {
        List<GBase8aTrigger> triggers = new ArrayList<>();
        for (GBase8aTrigger trigger : getContainer().getTriggerCache().getAllObjects(monitor, getContainer())) {
            if (trigger.getTable() == this) {
                triggers.add(trigger);
            }
        }
        return triggers;
    }

    @Association
    @Override
    public Collection<GBase8aPartition> getPartitions(DBRProgressMonitor monitor) throws DBException {
        return getPartitionCache().getAllObjects(monitor, this);
    }

    public PartitionCache getPartitionCache() {
        return this.partitionCache;
    }

    private void loadAdditionalInfo(DBRProgressMonitor monitor) throws DBCException {
        if (!isPersisted()) {
            this.additionalInfo.loaded = true;
            return;
        }
        try (JDBCSession session = DBUtils.openMetaSession(monitor, this, "Load table status")) {
            String sql = "SELECT * FROM "
                    + GBase8aConstants.META_TABLE_TABLES
                    + " WHERE "
                    + GBase8aConstants.COL_TABLE_SCHEMA
                    + " ='"
                    + DBUtils.getQuotedIdentifier(getContainer())
                    + "' AND "
                    + GBase8aConstants.COL_TABLE_NAME
                    + " ='"
                    + getName()
                    + "' ";
            if (!getContainer().isSystemCatalog()) {
                sql = sql + " AND " + GBase8aConstants.COL_VC_NAME + " ='" + getContainer().getVcName() + "'";
            }
            try (JDBCPreparedStatement dbStat = session.prepareStatement(sql)) {
                try (JDBCResultSet dbResult = dbStat.executeQuery()) {
                    if (dbResult.next()) {
                        fetchAdditionalInfo(dbResult);
                    }
                    this.additionalInfo.loaded = true;
                }
            }
            String sqluser = "SELECT u.user FROM `information_schema`.`tables` t, `gbase`.`user` u WHERE t.TABLE_SCHEMA = '"
                    + DBUtils.getQuotedIdentifier(getContainer())
                    + "' AND t.TABLE_NAME = '"
                    + getName()
                    + "' AND t.owner_uid=u.uid";
            if (!getContainer().isSystemCatalog()) {
                sqluser = sqluser + " and table_vc='" + getContainer().getVcName() + "'";
            }
            try (JDBCPreparedStatement statement = session.prepareStatement(sqluser); JDBCResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    this.additionalInfo.createUser = JDBCUtils.safeGetString(result, "USER");
                }
            } catch (SQLException e) {
                log.error(e);
            }
            getTableMessage(monitor);
        } catch (SQLException | DBException e) {
            throw new DBCException("loadAdditionalInfo", e);
        }
    }

    void fetchAdditionalInfo(JDBCResultSet dbResult) {
        GBase8aDataSource dataSource = getDataSource();
        // filer table description (for INNODB it contains some system information)
        String desc = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_TABLE_COMMENT);
        if (desc != null) {
            if (desc.startsWith(INNODB_COMMENT)) {
                desc = "";
            } else if (!CommonUtils.isEmpty(desc)) {
                int divPos = desc.indexOf("; " + INNODB_COMMENT);
                if (divPos != -1) {
                    desc = desc.substring(0, divPos);
                }
            }
            this.additionalInfo.description = desc;
            this.additionalInfo.oldDescription = desc;
        }
        additionalInfo.engine = dataSource.getEngine(JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_ENGINE));
        additionalInfo.autoIncrement = JDBCUtils.safeGetLong(dbResult, "AUTO_INCREMENT");
        additionalInfo.createTime = JDBCUtils.safeGetTimestamp(dbResult, GBase8aConstants.COL_CREATE_TIME);
        additionalInfo.collation = dataSource.getCollation(JDBCUtils.safeGetString(dbResult, "TABLE_COLLATION"));
        if (additionalInfo.collation != null) {
            additionalInfo.charset = additionalInfo.collation.getCharset();
        }
        this.additionalInfo.tableLimitSize = changeSize(JDBCUtils.safeGetLong(dbResult, "TABLE_LIMIT_STORAGE_SIZE"));
//        String createOptions = JDBCUtils.safeGetString(dbResult, "CREATE_OPTIONS");
//        if (CommonUtils.isNotEmpty(createOptions)) {
//            additionalInfo.partitioned = createOptions.contains(PARTITIONED_STATUS);
//        }
    }

    private String changeSize(long oldSizeNum) {
        String oldSize = "0";
        if (oldSizeNum / 1024L / 1024L / 1024L / 1024L > 0L && oldSizeNum % 256L == 0L) {
            oldSize = oldSizeNum / 1024L / 1024L / 1024L / 1024L + "T";
        } else if (oldSizeNum / 1024L / 1024L / 1024L > 0L && oldSizeNum % 1073741824L == 0L) {
            oldSize = oldSizeNum / 1024L / 1024L / 1024L + "G";
        } else if (oldSizeNum / 1024L / 1024L > 0L && oldSizeNum % 1048576L == 0L) {
            oldSize = oldSizeNum / 1024L / 1024L + "M";
        } else if (oldSizeNum / 1024L > 0L && oldSizeNum % 1024L == 0L) {
            oldSize = oldSizeNum / 1024L + "K";
        } else {
            oldSize = "0";
        }
        return oldSize;
    }

    private List<GBase8aTableForeignKey> loadForeignKeys(DBRProgressMonitor monitor, boolean references) throws DBException {
        List<GBase8aTableForeignKey> fkList = new ArrayList<>();
        if (!isPersisted() || monitor == null) {
            return fkList;
        }
        try (JDBCSession session = DBUtils.openMetaSession(monitor, this, "Load table relations")) {
            Map<String, GBase8aTableForeignKey> fkMap = new HashMap<>();
            Map<String, GBase8aTableConstraint> pkMap = new HashMap<>();
            JDBCDatabaseMetaData metaData = session.getMetaData();
            // Load indexes
            JDBCResultSet dbResult;
            if (references) {
                dbResult = metaData.getExportedKeys(getContainer().getName(), null, getName());
            } else {
                dbResult = metaData.getImportedKeys(getContainer().getName(), null, getName());
            }
            try {
                while (dbResult.next()) {
                    String pkTableCatalog = JDBCUtils.safeGetString(dbResult, JDBCConstants.PKTABLE_CAT);
                    String pkTableName = JDBCUtils.safeGetString(dbResult, JDBCConstants.PKTABLE_NAME);
                    String pkColumnName = JDBCUtils.safeGetString(dbResult, JDBCConstants.PKCOLUMN_NAME);
                    String fkTableCatalog = JDBCUtils.safeGetString(dbResult, JDBCConstants.FKTABLE_CAT);
                    String fkTableName = JDBCUtils.safeGetString(dbResult, JDBCConstants.FKTABLE_NAME);
                    String fkColumnName = JDBCUtils.safeGetString(dbResult, JDBCConstants.FKCOLUMN_NAME);
                    int keySeq = JDBCUtils.safeGetInt(dbResult, JDBCConstants.KEY_SEQ);
                    int updateRuleNum = JDBCUtils.safeGetInt(dbResult, JDBCConstants.UPDATE_RULE);
                    int deleteRuleNum = JDBCUtils.safeGetInt(dbResult, JDBCConstants.DELETE_RULE);
                    String fkName = JDBCUtils.safeGetString(dbResult, JDBCConstants.FK_NAME);
                    String pkName = JDBCUtils.safeGetString(dbResult, JDBCConstants.PK_NAME);

                    DBSForeignKeyModifyRule deleteRule = JDBCUtils.getCascadeFromNum(deleteRuleNum);
                    DBSForeignKeyModifyRule updateRule = JDBCUtils.getCascadeFromNum(updateRuleNum);

                    GBase8aTable pkTable = getDataSource().findTable(monitor, pkTableCatalog, pkTableName);
                    if (pkTable == null) {
                        log.debug("Can't find PK table " + pkTableName);
                        if (references) {
                            continue;
                        }
                    }
                    GBase8aTable fkTable = getDataSource().findTable(monitor, fkTableCatalog, fkTableName);
                    if (fkTable == null) {
                        log.warn("Can't find FK table " + fkTableName);
                        if (!references) {
                            continue;
                        }
                    }
                    GBase8aTableColumn pkColumn = pkTable == null ? null : pkTable.getAttribute(monitor, pkColumnName);
                    if (pkColumn == null) {
                        log.debug("Can't find PK table " + pkTableName + " column " + pkColumnName);
                        if (references) {
                            continue;
                        }
                    }
                    GBase8aTableColumn fkColumn = fkTable == null ? null : fkTable.getAttribute(monitor, fkColumnName);
                    if (fkColumn == null) {
                        log.debug("Can't find FK table " + fkTableName + " column " + fkColumnName);
                        if (!references) {
                            continue;
                        }
                    }

                    // Find PK
                    GBase8aTableConstraint pk = null;
                    if (pkTable != null) {
                        // Find pk based on referenced columns
                        Collection<GBase8aTableConstraint> constraints = pkTable.getConstraints(monitor);
                        if (constraints != null) {
                            for (GBase8aTableConstraint pkConstraint : constraints) {
                                if (pkConstraint.getConstraintType().isUnique() && DBUtils.getConstraintAttribute(monitor, pkConstraint, pkColumn) != null) {
                                    pk = pkConstraint;
                                    if (pkConstraint.getName().equals(pkName))
                                        break;
                                    // If pk name does not match, keep searching (actual pk might not be this one)
                                }
                            }
                        }
                    }
                    if (pk == null && pkTable != null && pkName != null) {
                        // Find pk based on name
                        Collection<GBase8aTableConstraint> constraints = pkTable.getConstraints(monitor);
                        pk = DBUtils.findObject(constraints, pkName);
                        if (pk == null) {
                            log.warn("Unique key '" + pkName + "' not found in table " + pkTable.getFullyQualifiedName(DBPEvaluationContext.DDL));
                        }
                    }
                    if (pk == null && pkTable != null) {
                        log.warn("Can't find primary key for table " + pkTable.getFullyQualifiedName(DBPEvaluationContext.DDL));
                        // Too bad. But we have to create new fake PK for this FK
                        String pkFullName = pkTable.getFullyQualifiedName(DBPEvaluationContext.DDL) + "." + pkName;
                        pk = pkMap.get(pkFullName);
                        if (pk == null) {
                            pk = new GBase8aTableConstraint(pkTable, pkName, null, DBSEntityConstraintType.PRIMARY_KEY, true);
                            pk.addColumn(new GBase8aTableConstraintColumn(pk, pkColumn, keySeq));
                            pkMap.put(pkFullName, pk);
                        }
                    }

                    // Find (or create) FK
                    GBase8aTableForeignKey fk = null;
                    if (references && fkTable != null) {
                        fk = DBUtils.findObject(fkTable.getAssociations(monitor), fkName);
                        if (fk == null) {
                            log.warn("Can't find foreign key '" + fkName + "' for table " + fkTable.getFullyQualifiedName(DBPEvaluationContext.DDL));
                            // No choice, we have to create fake foreign key :(
                        } else {
                            if (!fkList.contains(fk)) {
                                fkList.add(fk);
                            }
                        }
                    }

                    if (fk == null) {
                        fk = fkMap.get(fkName);
                        if (fk == null) {
                            fk = new GBase8aTableForeignKey(fkTable, fkName, null, pk, deleteRule, updateRule, true);
                            fkMap.put(fkName, fk);
                            fkList.add(fk);
                        }
                        GBase8aTableForeignKeyColumn fkColumnInfo = new GBase8aTableForeignKeyColumn(fk, fkColumn, keySeq, pkColumn);
                        if (fk.hasColumn(fkColumnInfo)) {
                            // Known MySQL bug, metaData.getImportedKeys() can return duplicates
                            // https://bugs.mysql.com/bug.php?id=95280
                            log.debug("FK " + fkName + " has already been added, skip");
                        } else {
                            fk.addColumn(fkColumnInfo);
                        }
                    }
                }
            } finally {
                dbResult.close();
            }
            return fkList;
        } catch (SQLException ex) {
            throw new DBDatabaseException(ex, getDataSource());
        }
    }

    @Override
    public String getObjectDefinitionText(DBRProgressMonitor monitor, Map<String, Object> options) throws DBException {
        return getDDL(monitor, options);
    }

    @Override
    public void setObjectDefinitionText(String sourceText) throws DBException {
        throw new DBException("Table DDL is read-only");
    }

    public class PartitionCache extends JDBCObjectCache<GBase8aTable, GBase8aPartition> {
        Map<String, GBase8aPartition> partitionMap;

        public PartitionCache() {
            this.partitionMap = new HashMap<>();
        }

        @NotNull
        @Override
        protected JDBCStatement prepareObjectsStatement(@NotNull JDBCSession session, @NotNull GBase8aTable gBase8aTable) throws SQLException {
            String sql = "SELECT * FROM "
                    + GBase8aConstants.META_TABLE_PARTITIONS
                    + " WHERE TABLE_SCHEMA=? AND TABLE_NAME=? ";
            if (!getContainer().isSystemCatalog()) {
                sql = sql + " and table_vc='" + getContainer().getVcName() + "'";
            }
            sql = sql + " ORDER BY PARTITION_ORDINAL_POSITION,SUBPARTITION_ORDINAL_POSITION";
            JDBCPreparedStatement dbStat = session.prepareStatement(sql);
            dbStat.setString(1, getContainer().getName());
            dbStat.setString(2, getName());
            return dbStat;
        }

        @Override
        protected GBase8aPartition fetchObject(@NotNull JDBCSession session, @NotNull GBase8aTable table,
                                               @NotNull JDBCResultSet dbResult) throws SQLException, DBException {
            String partitionName = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_PARTITION_NAME);
            String subPartitionName = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_SUBPARTITION_NAME);
            if (CommonUtils.isEmpty(partitionName) && CommonUtils.isEmpty(subPartitionName)) {
                // This is default empty info partition for tables without partitions. Do not create it.
                return null;
            }
            if (partitionName == null) {
                partitionName = "PARTITION";
            }
            if (CommonUtils.isEmpty(subPartitionName)) {
                return new GBase8aPartition(table, null, partitionName, dbResult);
            }
            GBase8aPartition parentPartition = this.partitionMap.get(partitionName);
            boolean parentFetched = false;
            if (parentPartition == null) {
                parentPartition = new GBase8aPartition(table, null, partitionName, dbResult);
                this.partitionMap.put(partitionName, parentPartition);
            } else {
                parentFetched = true;
            }
            // create subpartition
            new GBase8aPartition(table, parentPartition, subPartitionName, dbResult);
            if (!parentFetched) {
                return parentPartition;
            }
            return null;
//            return new GBase8aPartition(table, parentPartition, subPartitionName, dbResult);
        }

        @Override
        protected void invalidateObjects(DBRProgressMonitor monitor, GBase8aTable owner, Iterator<GBase8aPartition> objectIter) {
            this.partitionMap = null;
        }
    }

    @Nullable
    @Override
    public String getDescription() {
        return this.additionalInfo.description;
    }

    @Override
    public DBSObject refreshObject(@NotNull DBRProgressMonitor monitor) throws DBException {
        getContainer().getConstraintCache().clearObjectCache(this);
        getContainer().getIndexCache().clearObjectCache(this);
        getContainer().getTriggerCache().clearChildrenOf(this);
        this.referenceCache = null;
        return super.refreshObject(monitor);
    }

    @Override
    public boolean supportsChangingReferentialIntegrity(@NotNull DBRProgressMonitor monitor) {
        return false;
    }

    @Override
    public void enableReferentialIntegrity(@NotNull DBRProgressMonitor monitor, boolean enable) throws DBException {
    }

    @NotNull
    public String getChangeReferentialIntegrityStatement(@NotNull DBRProgressMonitor monitor, boolean enable) {
        return null;
    }

    public static class EngineListProvider implements IPropertyValueListProvider<GBase8aTable> {

        @Override
        public boolean allowCustomValue() {
            return false;
        }

        @Override
        public Object[] getPossibleValues(GBase8aTable object) {
            List<GBase8aEngine> engines = new ArrayList<>();
            for (GBase8aEngine engine : object.getDataSource().getEngines()) {
                if (engine.getSupport() == GBase8aEngine.Support.YES || engine.getSupport() == GBase8aEngine.Support.DEFAULT) {
                    engines.add(engine);
                }
            }
            Collections.sort(engines, DBUtils.nameComparator());
            return engines.toArray((Object[]) new GBase8aEngine[engines.size()]);
        }
    }

    public static class CharsetListProvider implements IPropertyValueListProvider<GBase8aTable> {

        @Override
        public boolean allowCustomValue() {
            return false;
        }

        @Override
        public Object[] getPossibleValues(GBase8aTable object) {
            return object.getDataSource().getCharsets().toArray();
        }
    }

    public static class CollationListProvider implements IPropertyValueListProvider<GBase8aTable> {

        @Override
        public boolean allowCustomValue() {
            return false;
        }

        @Override
        public Object[] getPossibleValues(GBase8aTable object) {
            if (object.additionalInfo.charset == null) {
                return null;
            }
            return object.additionalInfo.charset.getCollations().toArray();
        }
    }


    private void getTableMessage(DBRProgressMonitor monitor) throws DBException {
        String sqlString = getDDL(monitor, Collections.emptyMap());
        String[] sqlArray = sqlString.split("ENGINE=EXPRESS");
        if (sqlArray.length == 1) {
            sqlArray = sqlString.split("ENGINE=GsSYS");
        }

        String str0 = sqlArray[0];

        String regex = "(COMPRESS\\(){1}([015]{1})(\\s*,{1}\\s*)([035]{1})(\\){1})";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str0);

        boolean isCompress = m.find();
        this.additionalInfo.setBtnIsCompress(isCompress);
        this.additionalInfo.setOldBtnIsCompress(isCompress);
        if (isCompress) {
            this.additionalInfo.setCompressNum(m.group(2));
            this.additionalInfo.setCompressString(m.group(4));
            this.additionalInfo.setOldCompressNum(m.group(2));
            this.additionalInfo.setOldCompressString(m.group(4));
        } else {
            this.additionalInfo.setCompressNum("0");
            this.additionalInfo.setCompressString("0");
            this.additionalInfo.setOldCompressNum("0");
            this.additionalInfo.setOldCompressString("0");
        }

        String str1 = sqlArray[1];
        if (str1.contains("gbk")) {
            this.additionalInfo.setCharset(getDataSource().getCharset("gbk"));
        } else if (str1.contains("utf8mb4")) {
            this.additionalInfo.setCharset(getDataSource().getCharset("utf8mb4"));
        } else if (str1.contains("gb18030")) {
            this.additionalInfo.setCharset(getDataSource().getCharset("gb18030"));
        } else {
            this.additionalInfo.setCharset(getDataSource().getCharset("utf8"));
        }

        this.additionalInfo.setNocopies(str1.contains("NOCOPIES"));
        this.additionalInfo.setRepliacte(str1.contains("REPLICATED"));

        regex = "(DISTRIBUTED BY\\(\\s*){1}(\\S+)(\\)){1}";
        String regex1 = "'[^'\\,]+'";
        List<String> hashCols = new ArrayList<String>();
        p = Pattern.compile(regex);
        m = p.matcher(str1);
        boolean isHash = m.find();
        if (isHash) {
            String group = m.group(2);
            p = Pattern.compile(regex1);
            m = p.matcher(group);
            while (m.find()) {
                String col = m.group();
                col = col.replace("'", "");
                hashCols.add(col);
            }
            this.additionalInfo.setHashCols(hashCols);
        }
    }

    public static class PartitionedTablePropertyValidator implements IPropertyValueValidator<GBase8aTable, Object> {
        @Override
        public boolean isValidValue(GBase8aTable object, Object value) throws IllegalArgumentException {
            return !object.isPartition();
        }
    }
}
