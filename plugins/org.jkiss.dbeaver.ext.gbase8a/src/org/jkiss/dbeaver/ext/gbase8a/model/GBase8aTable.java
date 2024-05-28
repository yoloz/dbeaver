package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCObjectCache;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.meta.IPropertyCacheValidator;
import org.jkiss.dbeaver.model.meta.LazyProperty;
import org.jkiss.dbeaver.model.meta.PropertyGroup;
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
import org.jkiss.dbeaver.model.struct.rdb.DBSTable;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableIndex;
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

public class GBase8aTable extends GBase8aTableBase {

    private static final Log log = Log.getLog(GBase8aTable.class);

    private static final String INNODB_COMMENT = "InnoDB free";

    @Override
    public String getObjectDefinitionText(DBRProgressMonitor monitor, Map<String, Object> options) throws DBException {
        return getDDL(monitor, options);
    }

    public static class AdditionalInfo {
        private volatile boolean loaded = false;
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

        @Property(viewable = true, editable = false, updatable = false, listProvider = EngineListProvider.class, order = 3)
        public String getEngine() {
            return this.engine.getName();
        }

        @Property(viewable = false, editable = false, updatable = false, listProvider = CharsetListProvider.class, order = 5)
        public String getCharset() {
            return this.charset.getName();
        }

        @Property(viewable = false, editable = false, updatable = false, listProvider = CollationListProvider.class, order = 6)
        public String getCollation() {
            return this.collation.getName();
        }


        @Property(viewable = true, editable = false, updatable = false, order = 17)
        public String getTableLimitSize() {
            return this.tableLimitSize;
        }

        @Property(viewable = true, editable = false, updatable = false, order = 100)
        public String getDescription() {
            return this.description;
        }

        @Property(viewable = false, order = 19)
        public String getCreateUser() {
            return this.createUser;
        }

        @Property(viewable = false, order = 20)
        public Date getCreateTime() {
            return this.createTime;
        }

        public void setEngine(GBase8aEngine engine) {
            this.engine = engine;
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

        @Property(viewable = true, editable = false, updatable = false, order = 21)
        public boolean isRepliacte() {
            return this.isRepliacte;
        }

        public void setRepliacte(boolean isRepliacte) {
            this.isRepliacte = isRepliacte;
        }

        @Property(viewable = true, editable = false, updatable = false, order = 18)
        public boolean isBtnIsCompress() {
            return this.btnIsCompress;
        }

        public void setBtnIsCompress(boolean btnIsCompress) {
            this.btnIsCompress = btnIsCompress;
        }

        @Property(viewable = true, editable = false, updatable = false, order = 19)
        public String getCompressNum() {
            return this.compressNum;
        }

        public void setCompressNum(String compressNum) {
            this.compressNum = compressNum;
        }

        @Property(viewable = true, editable = false, updatable = false, order = 20)
        public String getCompressString() {
            return this.compressString;
        }

        public void setCompressString(String compressString) {
            this.compressString = compressString;
        }

        @Property(viewable = true, editable = false, updatable = false, order = 22)
        public boolean isNocopies() {
            return this.isNocopies;
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
        public boolean isPropertyCached(GBase8aTable object, Object propertyId) {
            return object.additionalInfo.loaded;
        }
    }

    private final SimpleObjectCache<GBase8aTable, GBase8aTableForeignKey> foreignKeys = new SimpleObjectCache();
    private final PartitionCache partitionCache = new PartitionCache();

    private final AdditionalInfo additionalInfo = new AdditionalInfo();

    public GBase8aTable(GBase8aCatalog catalog) {
        super(catalog);
    }

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
            for (GBase8aTrigger srcTrigger : ((GBase8aTable) source).getTriggers(monitor)) {
                GBase8aTrigger trigger = new GBase8aTrigger(catalog, this, srcTrigger);
                getContainer().triggerCache.cacheObject(trigger);
            }
            for (GBase8aPartition partition : ((GBase8aTable) source).partitionCache.getCachedObjects()) {
                this.partitionCache.cacheObject(new GBase8aPartition(monitor, this, partition));
            }
        }
        if (source instanceof DBSTable) {
            for (DBSTableIndex srcIndex : CommonUtils.safeCollection(((DBSTable) source).getIndexes(monitor))) {
                if (srcIndex instanceof GBase8aTableIndex && srcIndex.getName().equals("PRIMARY")) {
                    continue;
                }
                GBase8aTableIndex index = new GBase8aTableIndex(monitor, this, srcIndex);
                getContainer().indexCache.cacheObject(index);
            }

            for (DBSTableIndex srcIndex : CommonUtils.safeCollection(((DBSTable) source).getIndexes(monitor))) {
                if (srcIndex instanceof GBase8aTableFullIndex && srcIndex.getName().equals("PRIMARY")) {
                    continue;
                }
                GBase8aTableFullIndex index = new GBase8aTableFullIndex(monitor, this, srcIndex);
                getContainer().fullIndexCache.cacheObject(index);
            }
        }

        for (DBSEntityConstraint srcConstr : CommonUtils.safeCollection(source.getConstraints(monitor))) {
            GBase8aTableConstraint constr = new GBase8aTableConstraint(monitor, this, srcConstr);
            getContainer().constraintCache.cacheObject(constr);
        }

        List<GBase8aTableForeignKey> fkList = new ArrayList<GBase8aTableForeignKey>();
        for (DBSEntityAssociation srcFK : CommonUtils.safeCollection(source.getAssociations(monitor))) {
            GBase8aTableForeignKey fk = new GBase8aTableForeignKey(monitor, this, srcFK);
            if (fk.getReferencedConstraint() != null) {
                fk.setName(fk.getName() + "_copy");
                fkList.add(fk);
                continue;
            }
            log.debug("Can't copy association '" + srcFK.getName() + "' - can't find referenced constraint");
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

    public boolean isView() {
        return false;
    }

    @Association
    public synchronized Collection<GBase8aTableIndex> getIndexes(DBRProgressMonitor monitor) throws DBException {
        return getContainer().indexCache.getObjects(monitor, getContainer(), this);
    }

    @Association
    public synchronized Collection<GBase8aTableFullIndex> getFullIndexes(DBRProgressMonitor monitor) throws DBException {
        return getContainer().fullIndexCache.getObjects(monitor, getContainer(), this);
    }

    @Nullable
    @Association
    public synchronized Collection<GBase8aTableConstraint> getConstraints(@NotNull DBRProgressMonitor monitor) throws DBException {
        return getContainer().constraintCache.getObjects(monitor, getContainer(), this);
    }

    public GBase8aTableConstraint getConstraint(DBRProgressMonitor monitor, String ukName) throws DBException {
        return getContainer().constraintCache.getObject(monitor, getContainer(), this, ukName);
    }

    @Association
    public Collection<GBase8aTableForeignKey> getReferences(@NotNull DBRProgressMonitor monitor) throws DBException {
        return null;
    }

    public synchronized Collection<GBase8aTableForeignKey> getAssociations(@NotNull DBRProgressMonitor monitor) throws DBException {
        return this.foreignKeys.getCachedObjects();
    }

    public GBase8aTableForeignKey getAssociation(DBRProgressMonitor monitor, String fkName) throws DBException {
        return DBUtils.findObject(getAssociations(monitor), fkName);
    }

    public DBSObjectCache<GBase8aTable, GBase8aTableForeignKey> getForeignKeyCache() {
        return this.foreignKeys;
    }

    @Association
    public List<GBase8aTrigger> getTriggers(DBRProgressMonitor monitor) throws DBException {
        List<GBase8aTrigger> triggers = new ArrayList<>();
        for (GBase8aTrigger trigger : getContainer().triggerCache.getAllObjects(monitor, getContainer())) {
            if (trigger.getTable() == this) {
                triggers.add(trigger);
            }
        }
        return triggers;
    }

    @Association
    public Collection<GBase8aPartition> getPartitions(DBRProgressMonitor monitor) throws DBException {
        return this.partitionCache.getAllObjects(monitor, this);
    }

    public PartitionCache getPartitionCache() {
        return this.partitionCache;
    }

    private void loadAdditionalInfo(DBRProgressMonitor monitor) throws DBCException {
        if (!isPersisted()) {
            this.additionalInfo.loaded = true;
            return;
        }
        GBase8aDataSource dataSource = getDataSource();

        try (JDBCSession session = DBUtils.openMetaSession(monitor, dataSource, "Load table status")) {
            String sql = "SELECT t.* FROM `information_schema`.`tables` t WHERE t.TABLE_SCHEMA = '" + DBUtils.getQuotedIdentifier(getContainer()) + "' AND t.TABLE_NAME = '" + getName() + "' ";
            if (dataSource.isVCCluster() && !(getContainer() instanceof GBase8aSysCatalog)) {
                sql = sql + " and table_vc='" + getContainer().getVcName() + "'";
            }
            try (JDBCPreparedStatement dbStat = session.prepareStatement(sql)) {
                try (JDBCResultSet dbResult = dbStat.executeQuery()) {
                    if (dbResult.next()) {
                        String desc = JDBCUtils.safeGetString(dbResult, "TABLE_COMMENT");
                        if (desc != null) {
                            if (desc.startsWith("InnoDB free")) {
                                desc = "";
                            } else if (!CommonUtils.isEmpty(desc)) {
                                int divPos = desc.indexOf("; InnoDB free");
                                if (divPos != -1) {
                                    desc = desc.substring(0, divPos);
                                }
                            }
                            this.additionalInfo.description = desc;
                            this.additionalInfo.oldDescription = desc;
                        }
                        this.additionalInfo.engine = dataSource.getEngine(JDBCUtils.safeGetString(dbResult, "ENGINE"));
                        this.additionalInfo.createTime = JDBCUtils.safeGetTimestamp(dbResult, "CREATE_TIME");
                        this.additionalInfo.collation = dataSource.getCollation(JDBCUtils.safeGetString(dbResult, "COLLATION"));
                        if (this.additionalInfo.collation != null) {
                            this.additionalInfo.charset = this.additionalInfo.collation.getCharset();
                        }
                        this.additionalInfo.tableLimitSize = changeSize(JDBCUtils.safeGetLong(dbResult, "LIMIT_STORAGE_SIZE"));
                    }
                    this.additionalInfo.loaded = true;
                }
            }
            String sqluser = "SELECT u.user FROM `information_schema`.`tables` t, `gbase`.`user` u WHERE t.TABLE_SCHEMA = '" + DBUtils.getQuotedIdentifier(getContainer()) + "' AND t.TABLE_NAME = '" + getName() + "' AND t.owner_uid=u.uid";
            if (dataSource.isVCCluster() && !(getContainer() instanceof GBase8aSysCatalog)) {
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

    public void setObjectDefinitionText(String sourceText) throws DBException {
        throw new DBException("Table DDL is read-only");
    }

    public class PartitionCache extends JDBCObjectCache<GBase8aTable, GBase8aPartition> {
        Map<String, GBase8aPartition> partitionMap;

        public PartitionCache() {
            this.partitionMap = new HashMap<String, GBase8aPartition>();
        }

        protected JDBCStatement prepareObjectsStatement(@NotNull JDBCSession session, @NotNull GBase8aTable gBase8aTable) throws SQLException {
            String sql = "SELECT * FROM information_schema.PARTITIONS WHERE TABLE_SCHEMA=? AND TABLE_NAME=? ";
            if (gBase8aTable.getDataSource().isVCCluster() && !(GBase8aTable.this.getContainer() instanceof GBase8aSysCatalog)) {
                sql = sql + " and table_vc='" + GBase8aTable.this.getContainer().getVcName() + "'";
            }
            sql = sql + " ORDER BY PARTITION_ORDINAL_POSITION,SUBPARTITION_ORDINAL_POSITION";
            JDBCPreparedStatement dbStat = session.prepareStatement(sql);
            dbStat.setString(1, GBase8aTable.this.getContainer().getName());
            dbStat.setString(2, GBase8aTable.this.getName());
            return dbStat;
        }

        protected GBase8aPartition fetchObject(@NotNull JDBCSession session, @NotNull GBase8aTable table, @NotNull JDBCResultSet dbResult) throws SQLException, DBException {
            String partitionName = JDBCUtils.safeGetString(dbResult, "PARTITION_NAME");
            if (partitionName == null) {
                partitionName = "PARTITION";
            }
            String subPartitionName = JDBCUtils.safeGetString(dbResult, "SUBPARTITION_NAME");
            if (CommonUtils.isEmpty(subPartitionName)) {
                return new GBase8aPartition(table, null, partitionName, dbResult);
            }
            GBase8aPartition parentPartition = this.partitionMap.get(partitionName);
            if (parentPartition == null) {
                parentPartition = new GBase8aPartition(table, null, partitionName, dbResult);
                this.partitionMap.put(partitionName, parentPartition);
            }
            return new GBase8aPartition(table, parentPartition, subPartitionName, dbResult);
        }

        protected void invalidateObjects(DBRProgressMonitor monitor, GBase8aTable owner, Iterator<GBase8aPartition> objectIter) {
            this.partitionMap = null;
        }
    }

    @Nullable
    public String getDescription() {
        return this.additionalInfo.description;
    }

    public DBSObject refreshObject(@NotNull DBRProgressMonitor monitor) throws DBException {
        getContainer().constraintCache.clearObjectCache(this);
        getContainer().indexCache.clearObjectCache(this);
        getContainer().triggerCache.clearChildrenOf(this);

        return super.refreshObject(monitor);
    }

    public static class EngineListProvider implements IPropertyValueListProvider<GBase8aTable> {
        public boolean allowCustomValue() {
            return false;
        }

        public Object[] getPossibleValues(GBase8aTable object) {
            List<GBase8aEngine> engines = new ArrayList<GBase8aEngine>();
            for (GBase8aEngine engine : object.getDataSource().getEngines(null)) {
                if (engine.getSupport() == GBase8aEngine.Support.YES || engine.getSupport() == GBase8aEngine.Support.DEFAULT) {
                    engines.add(engine);
                }
            }
            Collections.sort(engines, DBUtils.nameComparator());
            return engines.toArray((Object[]) new GBase8aEngine[engines.size()]);
        }
    }

    public static class CharsetListProvider
            implements IPropertyValueListProvider<GBase8aTable> {
        public boolean allowCustomValue() {
            return false;
        }

        public Object[] getPossibleValues(GBase8aTable object) {
            return object.getDataSource().getCharsets(null).toArray();
        }
    }

    public static class CollationListProvider
            implements IPropertyValueListProvider<GBase8aTable> {
        public boolean allowCustomValue() {
            return false;
        }

        public Object[] getPossibleValues(GBase8aTable object) {
            if (object.additionalInfo.charset == null) {
                return null;
            }
            return object.additionalInfo.charset.getCollations().toArray();
        }
    }

//    public Collection<? extends DBSLocation> getLocation(DBRProgressMonitor monitor) throws DBException {
//        return null;
//    }
//
//
//    public Collection<? extends DBSTableIndex> getStorages(DBRProgressMonitor monitor) throws DBException {
//        return null;
//    }
//
//
//    public Collection<? extends DBSTableIndex> getDesigns(DBRProgressMonitor monitor) throws DBException {
//        return null;
//    }

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
}
