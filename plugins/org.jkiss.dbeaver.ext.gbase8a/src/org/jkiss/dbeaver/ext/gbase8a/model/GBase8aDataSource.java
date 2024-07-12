package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aConstants;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aDataSourceProvider;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aUtils;
import org.jkiss.dbeaver.ext.gbase8a.model.plan.GBase8aPlanAnalyser;
import org.jkiss.dbeaver.ext.gbase8a.model.session.GBase8aSessionManager;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.DBPErrorAssistant;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.admin.sessions.DBAServerSessionManager;
import org.jkiss.dbeaver.model.connection.DBPConnectionConfiguration;
import org.jkiss.dbeaver.model.connection.DBPDriver;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.exec.DBCExecutionPurpose;
import org.jkiss.dbeaver.model.exec.DBCQueryTransformType;
import org.jkiss.dbeaver.model.exec.DBCQueryTransformer;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.exec.plan.DBCQueryPlanner;
import org.jkiss.dbeaver.model.gis.GisConstants;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCRemoteInstance;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCBasicDataTypeCache;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCObjectCache;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCDataType;
import org.jkiss.dbeaver.model.impl.sql.QueryTransformerLimit;
import org.jkiss.dbeaver.model.meta.Association;
import org.jkiss.dbeaver.model.net.DBWHandlerConfiguration;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.VoidProgressMonitor;
import org.jkiss.dbeaver.model.sql.SQLState;
import org.jkiss.dbeaver.model.struct.DBSDataType;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSObjectFilter;
import org.jkiss.dbeaver.model.struct.DBSStructureAssistant;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCDataSource;
import org.jkiss.dbeaver.model.gis.SpatialDataProvider;
import org.jkiss.utils.CommonUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GBase8aDataSource extends JDBCDataSource {

    private static final Log log = Log.getLog(GBase8aDataSource.class);

    private final JDBCBasicDataTypeCache<GBase8aDataSource, JDBCDataType> dataTypeCache;

    private List<GBase8aEngine> engines = new ArrayList<>();
    private final VCCache vcCache = new VCCache();
    private List<GBase8aVC> vcs = new ArrayList<>();
    private final SysCatalogCache sysCatalogCache = new SysCatalogCache();
    private List<GBase8aPrivilege> privileges;
    private List<GBase8aUser> users = new ArrayList<>();
    private List<GBase8aCharset> charsets = new ArrayList<>();
    private Map<String, GBase8aCollation> collations = new LinkedHashMap<>();
    private String activeCatalogName;
    private boolean isrefresh = false;
    private DBRProgressMonitor monitor;
    private final GBase8aDBProcess gbase8aDBProcess = new GBase8aDBProcess();

    private boolean showLog = false;
    private static final String DEFAULT = "Default";

    //    private String isVCCluster = "";
    private final Pattern ERROR_POSITION_PATTERN;

    public GBase8aDataSource(@NotNull DBRProgressMonitor monitor, DBPDataSourceContainer container) throws DBException {
        super(monitor, container, new GBase8aDialect());
        this.ERROR_POSITION_PATTERN = Pattern.compile(" at line ([0-9]+)");
        this.dataTypeCache = new JDBCBasicDataTypeCache<>(this);
        this.monitor = Objects.requireNonNullElseGet(monitor, VoidProgressMonitor::new);
    }

    public String getActiveCatalogName() {
        return this.activeCatalogName;
    }

    @Override
    public Object getDataSourceFeature(String featureId) {
        switch (featureId) {
            case DBPDataSource.FEATURE_MAX_STRING_LENGTH:
                if (isServerVersionAtLeast(5, 0)) {
                    return 65535;
                } else {
                    return 255;
                }
            case DBPDataSource.FEATURE_LIMIT_AFFECTS_DML:
                return true;
        }
        return super.getDataSourceFeature(featureId);
    }

    @Override
    protected Map<String, String> getInternalConnectionProperties(DBRProgressMonitor monitor, DBPDriver driver, JDBCExecutionContext context, String purpose, DBPConnectionConfiguration connectionInfo)
            throws DBCException {
        Map<String, String> props = new LinkedHashMap<>(GBase8aDataSourceProvider.getConnectionsProps());
        DBWHandlerConfiguration sslConfig = getContainer().getActualConnectionConfiguration().getHandler("gbase8a_ssl");
        if (sslConfig != null && sslConfig.isEnabled()) {
            try {
                initSSL(monitor, props, sslConfig);
            } catch (Exception e) {
                throw new DBCException("Error configuring SSL certificates", e);
            }
        } else {
            props.put("useSSL", "false");
        }
        return props;
    }


    private void initSSL(DBRProgressMonitor monitor, Map<String, String> props, DBWHandlerConfiguration sslConfig) throws Exception {
        monitor.subTask("Install SSL certificates");
        props.put("useSSL", "true");
        props.put("requireSSL", "true");
        System.setProperty("javax.net.ssl.keyStore", CommonUtils.notEmpty((String) sslConfig.getProperties().get("javax.net.ssl.keyStore")));
        System.setProperty("javax.net.ssl.keyStorePassword", CommonUtils.notEmpty((String) sslConfig.getProperties().get("javax.net.ssl.keyStorePassword")));
        System.setProperty("javax.net.ssl.trustStore", CommonUtils.notEmpty((String) sslConfig.getProperties().get("javax.net.ssl.trustStore")));
        System.setProperty("javax.net.ssl.trustStorePassword", CommonUtils.notEmpty((String) sslConfig.getProperties().get("javax.net.ssl.trustStorePassword")));
    }

    @Override
    protected JDBCExecutionContext createExecutionContext(JDBCRemoteInstance instance, String type) {
        return new GBase8aExecutionContext(instance, type);
    }

    public String[] getTableTypes() {
        return GBase8aConstants.TABLE_TYPES;
    }

    public SysCatalogCache getSysCatalogCache() {
        return this.sysCatalogCache;
    }

    public Collection<GBase8aCatalog> getCatalogs() {
        return getActiveVC().getCatalogs(null);
    }

    @Association
    public Collection<GBase8aVC> getVcs(DBRProgressMonitor monitor) {
        log.debug("getVcs===================");
        if (isVCCluster()) {
            return this.vcCache.getCachedObjects();
        }
        return this.vcs;
    }

    public GBase8aVC getVc(String childName) {
        if (isVCCluster()) {
            return this.vcCache.getCachedObject(childName);
        }
        return this.vcs.get(0);
    }

    public GBase8aVC getActiveVC() {
        if (isVCCluster()) {
            try (JDBCSession session = DBUtils.openMetaSession(this.monitor, this, "select vc()");
                 JDBCPreparedStatement dbStat = session.prepareStatement("select vc()");
                 JDBCResultSet dbResult = dbStat.executeQuery()) {
                if (dbResult.next()) {
                    String id = dbResult.getString(1);
                    if (getVcs(null) != null) {
                        for (GBase8aVC vc : getVcs(null)) {
                            if (vc.getId().equals(id)) {
                                return vc;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                log.error(ex);
            }
        } else if (getVcs(null) != null) {
            for (GBase8aVC vc : getVcs(null)) {
                if (vc.getName().equals(DEFAULT)) {
                    return vc;
                }
            }
        }
        return null;
    }

    public boolean isVCCluster() {
        try (JDBCSession session = DBUtils.openMetaSession(this.monitor, this, "isVCCluster");
             JDBCStatement stmt = session.createStatement();
             JDBCResultSet resultSet = stmt.executeQuery("select vc()")) {
            return true;
        } catch (Exception e) {
            log.error(e);
            return false;
        }
    }

    public GBase8aSysCatalog getSysCatalog(String name) {
        return this.sysCatalogCache.getCachedObject(name);
    }

    @Association
    public Collection<GBase8aSysCatalog> getSysCatalogs(DBRProgressMonitor monitor) {
        log.debug("getSysCatalogs=====================");
        List<GBase8aSysCatalog> list = new ArrayList<>();
        try (JDBCSession dbsession = DBUtils.openMetaSession(monitor, this, "");
             JDBCPreparedStatement stmt = (JDBCPreparedStatement) this.sysCatalogCache.prepareObjectsStatement(dbsession, this);
             JDBCResultSet resultSet = stmt.executeQuery()) {
            while (resultSet.next()) {
                log.debug("getSysCatalogs=====================");
                list.add(new GBase8aSysCatalog(this, resultSet));
            }
        } catch (SQLException | DBCException e) {
            log.error(e);
        }
        return list;
    }

    public void initialize(@NotNull DBRProgressMonitor monitor) throws DBException {
        if (!this.isrefresh) {
            super.initialize(monitor);
        }
        this.dataTypeCache.getAllObjects(monitor, this);
        try (JDBCSession dbsession = DBUtils.openMetaSession(monitor, this, "Load basic datasource metadata");
             JDBCStatement stmt = dbsession.createStatement()) {
            try (JDBCResultSet dbResult = stmt.executeQuery("SHOW ENGINES")) {
                while (dbResult.next()) {
                    GBase8aEngine engine = new GBase8aEngine(this, dbResult);
                    this.engines.add(engine);
                }
            }
            try (JDBCResultSet dbResult = stmt.executeQuery("SHOW CHARSET")) {
                while (dbResult.next()) {
                    GBase8aCharset charset = new GBase8aCharset(this, dbResult);
                    this.charsets.add(charset);
                }
//                Collections.sort(this.charsets, DBUtils.nameComparator());
            }
            try (JDBCResultSet dbResult = stmt.executeQuery("SHOW COLLATION")) {
                while (dbResult.next()) {
                    String charsetName = JDBCUtils.safeGetString(dbResult, "CHARSET");
                    GBase8aCharset charset = getCharset(charsetName);
                    if (charset == null) {
                        log.warn("Charset '" + charsetName + "' not found.");
                        continue;
                    }
                    GBase8aCollation collation = new GBase8aCollation(charset, dbResult);
                    this.collations.put(collation.getName(), collation);
                    charset.addCollation(collation);
                }
            }
            this.activeCatalogName = GBase8aUtils.determineCurrentDatabase(dbsession);
            if (isVCCluster()) {
                this.vcCache.getAllObjects(monitor, this);
            } else {
                this.vcs = new ArrayList<>();
                GBase8aVC vc = new GBase8aVC(this, DEFAULT, null);
                this.vcs.add(vc);
            }
            this.sysCatalogCache.getAllObjects(monitor, this);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public DBSObject refreshObject(@NotNull DBRProgressMonitor monitor) throws DBException {
        super.refreshObject(monitor);
        this.engines.clear();
        this.vcCache.clearCache();
        this.sysCatalogCache.clearCache();
        this.users.clear();
        this.charsets.clear();
        this.collations.clear();
        this.activeCatalogName = null;
        refreshTreeObject(monitor);
        return this;
    }

    @Override
    public Collection<? extends GBase8aCatalog> getChildren(@NotNull DBRProgressMonitor monitor) {
        return getActiveVC().getCatalogs(monitor);
    }

    @Override
    public GBase8aCatalog getChild(@NotNull DBRProgressMonitor monitor, @NotNull String childName) {
        return getActiveVC().getCatalog(childName);
    }


    @Override
    public Class<? extends DBSObject> getPrimaryChildType(DBRProgressMonitor monitor) throws DBException {
        return GBase8aCatalog.class;
    }

    @Override
    public void cacheStructure(DBRProgressMonitor monitor, int scope) throws DBException {

    }

    @Override
    protected Connection openConnection(@NotNull DBRProgressMonitor monitor, @Nullable JDBCExecutionContext context, @NotNull String purpose) throws DBCException {
        Connection GBase8aConnection = super.openConnection(monitor, context, purpose);
        try {
            GBase8aConnection.setClientInfo("ApplicationName", DBUtils.getClientApplicationName(getContainer(), context, purpose));
        } catch (Throwable e) {
            throw new DBCException("openConnection", e);
        }
        return GBase8aConnection;
    }

    @Association
    public List<GBase8aUser> getUsers(DBRProgressMonitor monitor) throws DBException {
        if (this.users == null || this.users.isEmpty()) {
            this.users = loadUsers(monitor);
        }
        return this.users;
    }

    public GBase8aUser getUser(DBRProgressMonitor monitor, String name) throws DBException {
        return DBUtils.findObject(getUsers(monitor), name);
    }

    private List<GBase8aUser> loadUsers(DBRProgressMonitor monitor) throws DBException {
        List<GBase8aUser> list = new ArrayList<>();
        try (JDBCSession session = DBUtils.openMetaSession(monitor, this, "Load users");
             JDBCPreparedStatement dbStat = session.prepareStatement("SELECT * FROM gbase.user ORDER BY user");
             JDBCResultSet resultSet = dbStat.executeQuery()) {
            while (resultSet.next()) {
                list.add(new GBase8aUser(this, resultSet));
            }
        } catch (SQLException ex) {
            throw new DBException("", ex);
        }
        return list;
    }

    @Association
    public List<GBase8aEngine> getEngines(DBRProgressMonitor monitor) {
        return this.engines;
    }

    public GBase8aEngine getEngine(String name) {
        return DBUtils.findObject(this.engines, name);
    }

    public GBase8aEngine getDefaultEngine() {
        for (GBase8aEngine engine : this.engines) {
            if (engine.getSupport() == GBase8aEngine.Support.DEFAULT) return engine;
        }
        return null;
    }

    @Association
    public Collection<GBase8aCharset> getCharsets(DBRProgressMonitor monitor) {
        return this.charsets;
    }

    public GBase8aCharset getCharset(String name) {
        for (GBase8aCharset charset : this.charsets) {
            if (charset.getName().equals(name)) {
                return charset;
            }
        }
        return null;
    }

    public GBase8aCollation getCollation(String name) {
        return this.collations.get(name);
    }

    @Association
    public Collection<GBase8aCollation> getCollations(DBRProgressMonitor monitor) {
        return this.collations.values();
    }

    @Association
    public List<GBase8aPrivilege> getPrivileges(DBRProgressMonitor monitor) throws DBException {
        if (this.privileges == null) {
            this.privileges = loadPrivileges(monitor);
        }
        return this.privileges;
    }

    public List<GBase8aPrivilege> getPrivilegesByKind(DBRProgressMonitor monitor, GBase8aPrivilege.Kind kind) throws DBException {
        List<GBase8aPrivilege> privs = new ArrayList<>();
        for (GBase8aPrivilege priv : getPrivileges(monitor)) {
            if (priv.getKind() == kind) privs.add(priv);
        }
        return privs;
    }

    public GBase8aPrivilege getPrivilege(DBRProgressMonitor monitor, String name) throws DBException {
        return DBUtils.findObject(getPrivileges(monitor), name);
    }

    private List<GBase8aPrivilege> loadPrivileges(DBRProgressMonitor monitor) throws DBException {
        List<GBase8aPrivilege> list = new ArrayList<>();
        try (JDBCSession session = DBUtils.openMetaSession(monitor, this, "Load privileges");
             JDBCPreparedStatement dbStat = session.prepareStatement("SHOW PRIVILEGES");
             JDBCResultSet resultSet = dbStat.executeQuery()) {
            while (resultSet.next()) {
                list.add(new GBase8aPrivilege(this, resultSet));
            }
        } catch (SQLException ex) {
            throw new DBException("Load privileges failed", ex);
        }
        return list;
    }

    @Association
    public List<GBase8aParameter> getSessionStatus(DBRProgressMonitor monitor) throws DBException {
        log.debug("getSessionStatus===================");
        return this.loadParameters(monitor, true, false);
    }

    @Association
    public List<GBase8aParameter> getGlobalStatus(DBRProgressMonitor monitor) throws DBException {
        log.debug("getGlobalStatus===================");
        return this.loadParameters(monitor, true, true);
    }

    @Association
    public List<GBase8aParameter> getSessionVariables(DBRProgressMonitor monitor) throws DBException {
        return this.loadParameters(monitor, false, false);
    }

    @Association
    public List<GBase8aParameter> getGlobalVariables(DBRProgressMonitor monitor) throws DBException {
        return this.loadParameters(monitor, false, true);
    }

    public List<GBase8aDataSource> getInformation() {
        return Collections.singletonList(this);
    }

    private List<GBase8aParameter> loadParameters(DBRProgressMonitor monitor, boolean status, boolean global) throws DBException {

        try (JDBCSession session = DBUtils.openMetaSession(monitor, this, "Load status")) {

            try (JDBCPreparedStatement dbStat = session.prepareStatement("SHOW " + (global ? "GLOBAL " : "") + (status ? "STATUS" : "VARIABLES"))) {

                try (JDBCResultSet dbResult = dbStat.executeQuery()) {
                    List<GBase8aParameter> parameters = new ArrayList<>();
                    while (dbResult.next()) {
                        GBase8aParameter parameter = new GBase8aParameter(this, JDBCUtils.safeGetString(dbResult, "variable_name"),
                                JDBCUtils.safeGetString(dbResult, "value"));
                        parameters.add(parameter);
                    }
                    return parameters;
                }
            }
        } catch (SQLException ex) {
            throw new DBException("Load status failed", ex);
        }
    }

    public DBCQueryTransformer createQueryTransformer(@NotNull DBCQueryTransformType type) {
        if (type == DBCQueryTransformType.RESULT_SET_LIMIT) {
            return new QueryTransformerLimit();
        }
        if (type == DBCQueryTransformType.FETCH_ALL_TABLE) {
            return new QueryTransformerFetchAll();
        }
        return super.createQueryTransformer(type);
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == DBSStructureAssistant.class) {
            return adapter.cast(new GBase8aStructureAssistant(this));
        } else if (adapter == DBAServerSessionManager.class) {
            return adapter.cast(new GBase8aSessionManager(this));
        } else if (adapter == SpatialDataProvider.class) {
            return adapter.cast(new SpatialDataProvider() {
                @Override
                public boolean isFlipCoordinates() {
                    return false;
                }

                @Override
                public int getDefaultSRID() {
                    return GisConstants.SRID_4326;
                }
            });
        } else if (adapter == DBCQueryPlanner.class) {
            return adapter.cast(new GBase8aPlanAnalyser(this));
        }
        return super.getAdapter(adapter);
    }

    @Override
    public Collection<? extends DBSDataType> getLocalDataTypes() {
        return dataTypeCache.getCachedObjects();
    }

    @Override
    public DBSDataType getLocalDataType(String typeName) {
        return dataTypeCache.getCachedObject(typeName);
    }

    @Override
    public DBSDataType getLocalDataType(int typeID) {
        return dataTypeCache.getCachedObject(typeID);
    }


    public void refreshTreeObject(@NotNull DBRProgressMonitor monitor) throws DBException {
        this.isrefresh = true;
        initialize(monitor);
    }


    public GBase8aCatalog getDefaultObject() {
        if (getActiveVC() != null) {
            GBase8aCatalog catalog = null;
            try {
                catalog = getActiveVC().getDefaultObject();
            } catch (Exception e) {
                log.error(e);
            }
            return catalog;
        }
        return null;
    }

    public void useDatabase(DBRProgressMonitor monitor, JDBCExecutionContext context, GBase8aCatalog catalog) throws DBCException {
        if (catalog == null) {
            log.debug("Null current database");
            return;
        }
        JDBCSession session = context.openSession(monitor, DBCExecutionPurpose.UTIL, "Set active catalog");
        if (!catalog.getVcName().equals(getActiveVC().getName())) {
            try (JDBCPreparedStatement dbStat = session.prepareStatement("use vc " + catalog.getVcName())) {
                dbStat.execute();
            } catch (SQLException e) {
                throw new DBCException("useDatabase", e);
            }
        }
        try (JDBCPreparedStatement dbStat = session.prepareStatement("use " + DBUtils.getQuotedIdentifier(catalog))) {
            dbStat.execute();
        } catch (SQLException e) {
            throw new DBCException("useDatabase", e);
        } finally {
            session.close();
        }
    }


    @Nullable
    @Override
    public ErrorPosition[] getErrorPosition(@NotNull DBRProgressMonitor monitor, @NotNull DBCExecutionContext context, @NotNull String query, @NotNull Throwable error) {
        String message = error.getMessage();
        if (!CommonUtils.isEmpty(message)) {
            Matcher matcher = this.ERROR_POSITION_PATTERN.matcher(message);
            if (matcher.find()) {
                DBPErrorAssistant.ErrorPosition pos = new DBPErrorAssistant.ErrorPosition();
                pos.line = Integer.parseInt(matcher.group(1)) - 1;
                return new DBPErrorAssistant.ErrorPosition[]{pos};
            }
        }
        return null;
    }

    @NotNull
    public GBase8aDataSource getDataSource() {
        return this;
    }


    static class VCCache extends JDBCObjectCache<GBase8aDataSource, GBase8aVC> {

        protected JDBCStatement prepareObjectsStatement(JDBCSession session, GBase8aDataSource owner) throws SQLException {
            return session.prepareStatement("show vcs");
        }

        protected GBase8aVC fetchObject(JDBCSession session, GBase8aDataSource owner, JDBCResultSet resultSet) throws SQLException {
            String name = resultSet.getString("name");
            String id = resultSet.getString("id");
            return new GBase8aVC(owner, name, id);
        }
    }

    public static class SysCatalogCache extends JDBCObjectCache<GBase8aDataSource, GBase8aSysCatalog> {

        protected JDBCStatement prepareObjectsStatement(@NotNull JDBCSession session, @NotNull GBase8aDataSource owner) throws SQLException {
            StringBuilder catalogQuery = new StringBuilder("SELECT * FROM information_schema.SCHEMATA where schema_name in('information_schema','performance_schema','gbase','gctmpdb')");
            DBSObjectFilter catalogFilters = owner.getContainer().getObjectFilter(GBase8aSysCatalog.class, null, false);
            if (catalogFilters != null) {
                JDBCUtils.appendFilterClause(catalogQuery, catalogFilters, "SCHEMA_NAME", true);
            }
            JDBCPreparedStatement dbStat = session.prepareStatement(catalogQuery.toString());
            if (catalogFilters != null) {
                JDBCUtils.setFilterParameters(dbStat, 1, catalogFilters);
            }
            return dbStat;
        }

        protected GBase8aSysCatalog fetchObject(@NotNull JDBCSession session, @NotNull GBase8aDataSource owner, @NotNull JDBCResultSet resultSet) {
            return new GBase8aSysCatalog(owner, resultSet);
        }
    }

    public boolean isMariaDB() {
        return "org.mariadb.jdbc.Driver".equals(getContainer().getDriver().getDriverClassName());
    }

    @Override
    public ErrorType discoverErrorType(@NotNull Throwable error) {
        if (isMariaDB()) {
            if ("08".equals(SQLState.getStateFromException(error))) {
                return DBPErrorAssistant.ErrorType.CONNECTION_LOST;
            }
        }
        return super.discoverErrorType(error);
    }


    public DBRProgressMonitor getMonitor() {
        return this.monitor;
    }

    public void setMonitor(DBRProgressMonitor monitor) {
        this.monitor = monitor;
    }


    public GBase8aDBProcess getDbProcess() {
        return this.gbase8aDBProcess;
    }

    public boolean isShowLog() throws DBCException {
        this.showLog = false;
        try (JDBCSession session = DBUtils.openMetaSession(this.monitor, this, "select version");
             JDBCPreparedStatement dbStat = session.prepareStatement("select version()");
             JDBCResultSet dbResult = dbStat.executeQuery()) {
            if (dbResult.next()) {
                String version = dbResult.getString(1);
                if (version.compareTo("8.6.2.14") >= 0) {
                    this.showLog = true;
                }
            }
        } catch (Exception ex) {
            log.error(ex);
        }
        return this.showLog;
    }

    public void setShowLog(boolean showLog) {
        this.showLog = showLog;
    }

    public boolean isSupportTablespace() {
        boolean supportTableSpace = false;
        try (JDBCSession session = DBUtils.openMetaSession(this.monitor, this, "support tablespace");
             JDBCPreparedStatement dbStat = session.prepareStatement("select 1 from information_schema.tablespaces");
             JDBCResultSet dbResult = dbStat.executeQuery()) {
            if (dbResult.next()) {
                supportTableSpace = true;
            }
        } catch (Exception exception) {
            log.error(exception);
        }
        return supportTableSpace;
    }

    public boolean isSupportPartition() throws DBCException {
        boolean supportTableSpace = false;
        try (JDBCSession session = DBUtils.openMetaSession(this.monitor, this, "support partition");
             JDBCPreparedStatement dbStat = session.prepareStatement("select 1 from information_schema.partitions");
             JDBCResultSet dbResult = dbStat.executeQuery()) {
            if (dbResult.next()) {
                supportTableSpace = true;
            }
        } catch (Exception exception) {
            log.error(exception);
        }
        return supportTableSpace;
    }
}
