package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBDatabaseException;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ModelPreferences;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aConstants;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aDataSourceProvider;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aUtils;
import org.jkiss.dbeaver.ext.gbase8a.model.plan.GBase8aPlanAnalyser;
import org.jkiss.dbeaver.ext.gbase8a.model.session.GBase8aSessionManager;
import org.jkiss.dbeaver.model.DBPDataKind;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.DBPDataSourceInfo;
import org.jkiss.dbeaver.model.DBPErrorAssistant;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.admin.sessions.DBAServerSessionManager;
import org.jkiss.dbeaver.model.connection.DBPConnectionConfiguration;
import org.jkiss.dbeaver.model.connection.DBPDriver;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.exec.DBCQueryTransformType;
import org.jkiss.dbeaver.model.exec.DBCQueryTransformer;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCDatabaseMetaData;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.exec.plan.DBCQueryPlanner;
import org.jkiss.dbeaver.model.gis.GisConstants;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCConstants;
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
import org.jkiss.dbeaver.model.sql.SQLDialect;
import org.jkiss.dbeaver.model.struct.DBSDataType;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSObjectFilter;
import org.jkiss.dbeaver.model.struct.DBSStructureAssistant;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCDataSource;
import org.jkiss.dbeaver.model.gis.SpatialDataProvider;
import org.jkiss.dbeaver.model.struct.rdb.DBSIndexType;
import org.jkiss.utils.CommonUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GBase8aDataSource extends JDBCDataSource {

    private static final Log log = Log.getLog(GBase8aDataSource.class);

    private final JDBCBasicDataTypeCache<GBase8aDataSource, JDBCDataType> dataTypeCache;

    private final CatalogCache catalogCache = new CatalogCache() {
        @Override
        protected void detectCaseSensitivity(DBSObject object) {
            setCaseSensitive(!getDataSource().getSQLDialect().useCaseInsensitiveNameLookup());
        }
    };
    private List<GBase8aEngine> engines;
    private List<GBase8aPrivilege> privileges;
    private List<GBase8aUser> users;
    private List<GBase8aCharset> charsets;
    private Map<String, GBase8aCollation> collations;
    private String defaultCharset, defaultCollation;
    private int lowerCaseTableNames = 1;

    private String vcID;
    private String vcName;

//    private final VCCache vcCache = new VCCache();
//    private boolean isVCluster = false;
//    private GBase8aVC onlySingleVC;

    private final DBRProgressMonitor monitor;

    public GBase8aDataSource(DBRProgressMonitor monitor, DBPDataSourceContainer container) throws DBException {
        this(monitor, container, new GBase8aDialect());
    }

    public GBase8aDataSource(DBRProgressMonitor monitor, DBPDataSourceContainer container, SQLDialect dialect)
            throws DBException {
        super(monitor, container, dialect);
        this.dataTypeCache = new JDBCBasicDataTypeCache<>(this);
        this.monitor = monitor; //Objects.requireNonNullElseGet(monitor, VoidProgressMonitor::new);
    }

    public void initialize(@NotNull DBRProgressMonitor monitor) throws DBException {
        super.initialize(monitor);
        this.dataTypeCache.getAllObjects(monitor, this);
        try (JDBCSession session = DBUtils.openMetaSession(monitor, this, "Load basic datasource metadata")) {
            //Read VC
            try (JDBCPreparedStatement dbStat = session.prepareStatement("SELECT ID,NAME FROM information_schema.VC WHERE ID=(SELECT VC())")) {
                try (JDBCResultSet dbResult = dbStat.executeQuery()) {
                    if (dbResult.next()) {
                        this.vcID = dbResult.getString("ID");
                        this.vcName = dbResult.getString("NAME");
                    }
                }
            } catch (SQLException ex) {
                throw new DBException(ex.getMessage());
            }
            // Read engines
            engines = new ArrayList<>();
            try (JDBCPreparedStatement dbStat = session.prepareStatement("SHOW ENGINES")) {
                try (JDBCResultSet dbResult = dbStat.executeQuery()) {
                    while (dbResult.next()) {
                        GBase8aEngine engine = new GBase8aEngine(this, dbResult);
                        this.engines.add(engine);
                    }
                }
            } catch (SQLException ex) {
                // Engines are not supported. Shame on it. Leave this list empty
            }
            // Read charsets and collations
            charsets = new ArrayList<>();
            try (JDBCPreparedStatement dbStat = session.prepareStatement("SHOW CHARSET")) {
                try (JDBCResultSet dbResult = dbStat.executeQuery()) {
                    while (dbResult.next()) {
                        GBase8aCharset charset = new GBase8aCharset(this, dbResult);
                        this.charsets.add(charset);
                    }
                } catch (SQLException ex) {
                    // Engines are not supported. Shame on it. Leave this list empty
                }
            } catch (SQLException ex) {
                // Engines are not supported. Shame on it. Leave this list empty
            }
            charsets.sort(DBUtils.nameComparator());
            collations = new LinkedHashMap<>();
            try (JDBCPreparedStatement dbStat = session.prepareStatement("SHOW COLLATION")) {
                try (JDBCResultSet dbResult = dbStat.executeQuery()) {
                    while (dbResult.next()) {
                        String charsetName = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_CHARSET);
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
            } catch (SQLException ex) {
                // Engines are not supported. Shame on it. Leave this list empty
            }
            try (JDBCPreparedStatement dbStat = session.prepareStatement("SELECT @@GLOBAL.character_set_server,@@GLOBAL.collation_server")) {
                try (JDBCResultSet dbResult = dbStat.executeQuery()) {
                    if (dbResult.next()) {
                        defaultCharset = JDBCUtils.safeGetString(dbResult, 1);
                        defaultCollation = JDBCUtils.safeGetString(dbResult, 2);
                    }
                }
            } catch (Throwable ex) {
                log.debug("Error reading default server charset/collation", ex);
            }

            try (JDBCPreparedStatement dbStat = session.prepareStatement("SHOW VARIABLES LIKE 'lower_case_table_names'")) {
                try (JDBCResultSet dbResult = dbStat.executeQuery()) {
                    if (dbResult.next()) {
                        lowerCaseTableNames = JDBCUtils.safeGetInt(dbResult, 2);
                    }
                }
            } catch (Throwable ex) {
                log.debug("Error reading default server charset/collation", ex);
            }

            // Read catalogs：system and user vc
            catalogCache.getAllObjects(monitor, this);
        }
    }

    public DBSObject refreshObject(@NotNull DBRProgressMonitor monitor) throws DBException {
        super.refreshObject(monitor);
        this.engines = null;
        this.catalogCache.clearCache();
        this.users = null;
        this.initialize(monitor);
        return this;
    }

    public String getVcID() {
        return this.vcID;
    }

    public String getVcName() {
        return this.vcName;
    }

    public DBRProgressMonitor getMonitor() {
        return monitor;
    }

    public String getCurrentCatalog() throws DBCException {
        JDBCSession session = DBUtils.openMetaSession(monitor, this, "Query current database");
        return GBase8aUtils.determineCurrentDatabase(session);
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

    int getLowerCaseTableNames() {
        return lowerCaseTableNames;
    }

    @Override
    protected Map<String, String> getInternalConnectionProperties(
            @NotNull DBRProgressMonitor monitor,
            @NotNull DBPDriver driver,
            @NotNull JDBCExecutionContext context,
            @NotNull String purpose,
            @NotNull DBPConnectionConfiguration connectionInfo) throws DBCException {
        Map<String, String> props = new LinkedHashMap<>(GBase8aDataSourceProvider.getConnectionsProps());
        DBWHandlerConfiguration sslConfig = getContainer().getActualConnectionConfiguration().getHandler(GBase8aConstants.HANDLER_SSL);
        if (sslConfig != null && sslConfig.isEnabled()) {
            try {
                monitor.subTask("Install SSL certificates");
                props.put("useSSL", "true");
                props.put("requireSSL", "true");
                System.setProperty("javax.net.ssl.keyStore", CommonUtils.notEmpty((String) sslConfig.getProperties().get("javax.net.ssl.keyStore")));
                System.setProperty("javax.net.ssl.keyStorePassword", CommonUtils.notEmpty((String) sslConfig.getProperties().get("javax.net.ssl.keyStorePassword")));
                System.setProperty("javax.net.ssl.trustStore", CommonUtils.notEmpty((String) sslConfig.getProperties().get("javax.net.ssl.trustStore")));
                System.setProperty("javax.net.ssl.trustStorePassword", CommonUtils.notEmpty((String) sslConfig.getProperties().get("javax.net.ssl.trustStorePassword")));
            } catch (Exception e) {
                throw new DBCException("Error configuring SSL certificates", e);
            }
        } else {
            props.put("useSSL", "false");
        }
        return props;
    }

    @Override
    protected DBPDataSourceInfo createDataSourceInfo(DBRProgressMonitor monitor, @NotNull JDBCDatabaseMetaData metaData) {
        return new Gbase8aDataSourceInfo(metaData);
    }

    @Override
    protected JDBCExecutionContext createExecutionContext(JDBCRemoteInstance instance, String type) {
        return new GBase8aExecutionContext(instance, type);
    }

    protected void initializeContextState(@NotNull DBRProgressMonitor monitor, @NotNull JDBCExecutionContext context,
                                          JDBCExecutionContext initFrom) throws DBException {
        if (initFrom != null && !context.getDataSource().getContainer().isConnectionReadOnly()) {
            GBase8aCatalog object = ((GBase8aExecutionContext) initFrom).getDefaultCatalog();
            if (object != null) {
                ((GBase8aExecutionContext) context).setCurrentDatabase(monitor, object);
            }
        } else {
            ((GBase8aExecutionContext) context).refreshDefaults(monitor, true);
        }
    }

    public String[] getTableTypes() {
        return GBase8aConstants.TABLE_TYPES;
    }

    public CatalogCache getCatalogCache() {
        return catalogCache;
    }

    public Collection<GBase8aCatalog> getCatalogs() throws DBException {
        Collection<GBase8aCatalog> sysList = catalogCache.getCachedObjects();
        return sysList;
    }

    public GBase8aCatalog getCatalog(String name) {
        return catalogCache.getCachedObject(name);
    }

    GBase8aTable findTable(DBRProgressMonitor monitor, String catalogName, String tableName) throws DBException {
        if (CommonUtils.isEmpty(catalogName)) {
            return null;
        }
        GBase8aCatalog catalog = getCatalog(catalogName);
        if (catalog == null) {
            throw new DBCException("Schema " + catalogName + " not found");
        }
        return catalog.getTable(monitor, tableName);
    }

    @Override
    public Collection<? extends GBase8aCatalog> getChildren(@NotNull DBRProgressMonitor monitor) throws DBException {
        return getCatalogs();
    }

    @Override
    public GBase8aCatalog getChild(@NotNull DBRProgressMonitor monitor, @NotNull String childName) throws DBException {
        return getCatalog(childName);
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
        if (!getContainer().getPreferenceStore().getBoolean(ModelPreferences.META_CLIENT_NAME_DISABLE)) {
            // Provide client info
            try {
                GBase8aConnection.setClientInfo(JDBCConstants.APPLICATION_NAME_CLIENT_PROPERTY, DBUtils.getClientApplicationName(getContainer(), context, purpose));
            } catch (Throwable e) {
                // just ignore
                log.debug(e);
            }
        }
        return GBase8aConnection;
    }

    public List<GBase8aUser> getUsers(DBRProgressMonitor monitor) throws DBException {
        if (this.users == null) {
            this.users = loadUsers(monitor);
        }
        return this.users;
    }

    public GBase8aUser getUser(DBRProgressMonitor monitor, String name) throws DBException {
        return DBUtils.findObject(getUsers(monitor), name);
    }

    private List<GBase8aUser> loadUsers(DBRProgressMonitor monitor) throws DBException {
        try (JDBCSession session = DBUtils.openMetaSession(monitor, this, "Load users");
             JDBCPreparedStatement dbStat = session.prepareStatement("SELECT * FROM gbase.user ORDER BY user");
             JDBCResultSet resultSet = dbStat.executeQuery()) {
            List<GBase8aUser> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(new GBase8aUser(this, resultSet));
            }
            return list;
        } catch (SQLException ex) {
            throw new DBDatabaseException(ex, this);
        }
    }

    public List<GBase8aEngine> getEngines() {
        return this.engines;
    }

    public GBase8aEngine getEngine(String name) {
        return DBUtils.findObject(this.engines, name);
    }

    public GBase8aEngine getDefaultEngine() {
        for (GBase8aEngine engine : this.engines) {
            if (engine.getSupport() == GBase8aEngine.Support.DEFAULT) {
                return engine;
            }
        }
        return null;
    }

    public Collection<GBase8aCharset> getCharsets() {
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

    public Collection<GBase8aCollation> getCollations(DBRProgressMonitor monitor) {
        return this.collations.values();
    }

    public GBase8aCharset getDefaultCharset() {
        return getCharset(defaultCharset);
    }

    public GBase8aCollation getDefaultCollation() {
        return getCollation(defaultCollation);
    }

    public List<GBase8aPrivilege> getPrivileges(DBRProgressMonitor monitor) throws DBException {
        if (this.privileges == null) {
            this.privileges = loadPrivileges(monitor);
        }
        return this.privileges;
    }

    public List<GBase8aPrivilege> getPrivilegesByKind(DBRProgressMonitor monitor, GBase8aPrivilege.Kind kind) throws
            DBException {
        List<GBase8aPrivilege> privs = new ArrayList<>();
        for (GBase8aPrivilege priv : getPrivileges(monitor)) {
            if (priv.getKind() == kind) privs.add(priv);
        }
        return privs;
    }

    public GBase8aPrivilege getPrivilege(DBRProgressMonitor monitor, String name) throws DBException {
        return DBUtils.findObject(getPrivileges(monitor), name, true);
    }

    private List<GBase8aPrivilege> loadPrivileges(DBRProgressMonitor monitor) throws DBException {
        try (JDBCSession session = DBUtils.openMetaSession(monitor, this, "Load privileges");
             JDBCPreparedStatement dbStat = session.prepareStatement("SHOW PRIVILEGES");
             JDBCResultSet resultSet = dbStat.executeQuery()) {
            List<GBase8aPrivilege> list = new ArrayList<>();
            while (resultSet.next()) {
                String context = JDBCUtils.safeGetString(resultSet, "context");
                if (CommonUtils.isEmpty(context)) {
                    log.debug("Skip privilege with an empty context.");
                    continue;
                }
                list.add(new GBase8aPrivilege(this, context, resultSet));
            }
            return list;
        } catch (SQLException ex) {
            throw new DBException("Load privileges failed", ex);
        }
    }

    public List<GBase8aParameter> getSessionStatus(DBRProgressMonitor monitor) throws DBException {
        return loadParameters(monitor, true, false);
    }

    public List<GBase8aParameter> getGlobalStatus(DBRProgressMonitor monitor) throws DBException {
        return this.loadParameters(monitor, true, true);
    }

    public List<GBase8aParameter> getSessionVariables(DBRProgressMonitor monitor) throws DBException {
        return this.loadParameters(monitor, false, false);
    }

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
                        GBase8aParameter parameter = new GBase8aParameter(this,
                                JDBCUtils.safeGetString(dbResult, "variable_name"),
                                JDBCUtils.safeGetString(dbResult, "value"));
                        parameters.add(parameter);
                    }
                    return parameters;
                }
            }
        } catch (SQLException ex) {
            throw new DBDatabaseException(ex, this);
        }
    }

    @Override
    public DBCQueryTransformer createQueryTransformer(@NotNull DBCQueryTransformType type) {
        if (type == DBCQueryTransformType.RESULT_SET_LIMIT) {
            return new QueryTransformerLimit();
        }
        if (type == DBCQueryTransformType.FETCH_ALL_TABLE) {
            return new QueryTransformerFetchAll(this);
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

    @Override
    public String getDefaultDataTypeName(DBPDataKind dataKind) {
        switch (dataKind) {
            case BOOLEAN:
                return "TINYINT(1)";
            case NUMERIC:
                return "BIGINT";
            case DATETIME:
                return "TIMESTAMP";
            case BINARY:
                return "BINARY";
            case CONTENT:
                return "LONGBLOB";
            case ROWID:
                return "BINARY";
            default:
                return "VARCHAR";
        }
    }

    @NotNull
    @Override
    public GBase8aDataSource getDataSource() {
        return this;
    }

    static class VCCache extends JDBCObjectCache<GBase8aDataSource, GBase8aVC> {
        protected JDBCStatement prepareObjectsStatement(JDBCSession session, GBase8aDataSource owner) throws SQLException {
            return session.prepareStatement("SHOW VCS");
        }

        protected GBase8aVC fetchObject(JDBCSession session, GBase8aDataSource owner, JDBCResultSet resultSet) throws SQLException, DBException {
            String name = resultSet.getString("name");
            String id = resultSet.getString("id");
            return new GBase8aVC(owner, name, id);
        }
    }

    static class CatalogCache extends JDBCObjectCache<GBase8aDataSource, GBase8aCatalog> {
        @Override
        protected JDBCStatement prepareObjectsStatement(@NotNull JDBCSession session, @NotNull GBase8aDataSource owner) throws SQLException {
            StringBuilder catalogQuery = new StringBuilder("SELECT * FROM ")
                    .append(GBase8aConstants.META_TABLE_SCHEMATA)
                    .append(" WHERE VC_NAME='' OR VC_NAME='")
                    .append(owner.vcName).append("'");
            DBSObjectFilter catalogFilters = owner.getContainer().getObjectFilter(GBase8aCatalog.class, null, false);
            if (catalogFilters != null) {
                JDBCUtils.appendFilterClause(catalogQuery, catalogFilters, "SCHEMA_NAME", true);
            }
            JDBCPreparedStatement dbStat = session.prepareStatement(catalogQuery.toString());
            if (catalogFilters != null) {
                JDBCUtils.setFilterParameters(dbStat, 1, catalogFilters);
            }
            return dbStat;
        }

        protected GBase8aCatalog fetchObject(@NotNull JDBCSession session, @NotNull GBase8aDataSource owner, @NotNull JDBCResultSet resultSet) {
            return new GBase8aCatalog(owner, resultSet, null);
        }
    }

    @Override
    public ErrorType discoverErrorType(@NotNull Throwable error) {
        return super.discoverErrorType(error);
    }

    private final Pattern ERROR_POSITION_PATTERN = Pattern.compile(" at line ([0-9]+)");

    @Nullable
    @Override
    public ErrorPosition[] getErrorPosition(@NotNull DBRProgressMonitor monitor, @NotNull DBCExecutionContext context,
                                            @NotNull String query, @NotNull Throwable error) {
        String message = error.getMessage();
        if (!CommonUtils.isEmpty(message)) {
            Matcher matcher = ERROR_POSITION_PATTERN.matcher(message);
            if (matcher.find()) {
                DBPErrorAssistant.ErrorPosition pos = new DBPErrorAssistant.ErrorPosition();
                pos.line = Integer.parseInt(matcher.group(1)) - 1;
                return new ErrorPosition[]{pos};
            }
        }
        return null;
    }

    @Override
    protected void fillConnectionProperties(DBPConnectionConfiguration connectionInfo, Properties connectProps) {
        super.fillConnectionProperties(connectionInfo, connectProps);
    }


    public boolean supportsUserManagement() {
        return CommonUtils.getBoolean(getContainer().getDriver().getDriverParameter("supports-users"), true);
    }

    public boolean supportsEvents() {
        return CommonUtils.getBoolean(getContainer().getDriver().getDriverParameter("supports-events"), true);
    }

    public boolean supportsAlterView() {
        return CommonUtils.getBoolean(getContainer().getDriver().getDriverParameter("supports-alter-view"), false);
    }

    /**
     * Returns true if table/catalog triggers are supported.
     */
    @Association
    public boolean supportsTriggers() {
        return CommonUtils.getBoolean(getContainer().getDriver().getDriverParameter("supports-triggers"), true);
    }

    /**
     * Returns true if the charsets information is supported. Ex. for table creation.
     */
    @Association
    public boolean supportsCharsets() {
        return CommonUtils.getBoolean(getContainer().getDriver().getDriverParameter("supports-charsets"), true);
    }

    /**
     * Returns true if the collation information is supported. Ex. for table creation.
     */
    @Association
    public boolean supportsCollations() {
        return CommonUtils.getBoolean(getContainer().getDriver().getDriverParameter("supports-collations"), true);
    }

    /**
     * Returns true if different rename table syntax is used
     */
    public boolean supportsAlterTableRenameSyntax() {
        return false;
    }

    /**
     * Return true if WHERE condition can be added for SHOW DATABASES statement
     */
    public boolean supportsConditionForShowDatabasesStatement() {
        return true;
    }

    /**
     * Returns list of supported index types
     */
    public List<DBSIndexType> supportedIndexTypes() {
        return Arrays.asList(GBase8aConstants.INDEX_TYPE_BTREE,
                GBase8aConstants.INDEX_TYPE_FULLTEXT,
                GBase8aConstants.INDEX_TYPE_HASH,
                GBase8aConstants.INDEX_TYPE_RTREE);
    }

}
