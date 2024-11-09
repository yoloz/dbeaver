package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aUtils;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.connection.DBPConnectionBootstrap;
import org.jkiss.dbeaver.model.dpi.DPIContainer;
import org.jkiss.dbeaver.model.exec.DBCCachedContextDefaults;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCExecutionContextDefaults;
import org.jkiss.dbeaver.model.exec.DBCExecutionPurpose;
import org.jkiss.dbeaver.model.exec.DBCFeatureNotSupportedException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCRemoteInstance;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.rdb.DBSSchema;
import org.jkiss.utils.CommonUtils;

import java.sql.SQLException;


/**
 * @author yolo
 */
public class GBase8aExecutionContext extends JDBCExecutionContext implements DBCExecutionContextDefaults<GBase8aCatalog, DBSSchema> {

    private static final Log log = Log.getLog(GBase8aExecutionContext.class);

    private String activeDatabaseName;

    public GBase8aExecutionContext(JDBCRemoteInstance instance, String purpose) {
        super(instance, purpose);
    }

    @DPIContainer
    @NotNull
    @Override
    public GBase8aDataSource getDataSource() {
        return (GBase8aDataSource) super.getDataSource();
    }

    @NotNull
    @Override
    public GBase8aExecutionContext getContextDefaults() {
        return this;
    }

    @Override
    public GBase8aCatalog getDefaultCatalog() {
        return CommonUtils.isEmpty(activeDatabaseName) ? null : getDataSource().getCatalog(activeDatabaseName);
    }

    @Override
    public DBSSchema getDefaultSchema() {
        return null;
    }

    @Override
    public boolean supportsCatalogChange() {
        return true;
    }

    @Override
    public boolean supportsSchemaChange() {
        return false;
    }

    @Override
    public void setDefaultCatalog(DBRProgressMonitor monitor, GBase8aCatalog catalog, DBSSchema schema) throws DBCException {
        if (activeDatabaseName != null && activeDatabaseName.equals(catalog.getName())) {
            return;
        }
        final GBase8aCatalog oldActiveDatabase = getDefaultCatalog();

        // MySQL driver bug - it doesn't ley to change active schema in read-only mode (#9167)
        boolean connectionReadOnly = isConnectionReadOnly(monitor);
        if (connectionReadOnly) {
            setConnectionReadOnly(monitor, false);
        }
        try {
            if (!setCurrentDatabase(monitor, catalog)) {
                return;
            }
        } finally {
            if (connectionReadOnly) {
                setConnectionReadOnly(monitor, true);
            }
        }
        activeDatabaseName = catalog.getName();

        // Send notifications
        DBUtils.fireObjectSelectionChange(oldActiveDatabase, catalog, this);
    }

    private void setConnectionReadOnly(DBRProgressMonitor monitor, boolean readOnly) {
        try {
            getConnection(monitor).setReadOnly(readOnly);
        } catch (Exception e) {
            log.debug(e);
        }
    }

    private boolean isConnectionReadOnly(DBRProgressMonitor monitor) {
        try {
            return getConnection(monitor).isReadOnly();
        } catch (Exception e) {
            log.debug(e);
            return false;
        }
    }

    @Override
    public void setDefaultSchema(DBRProgressMonitor monitor, DBSSchema schema) throws DBCException {
        throw new DBCFeatureNotSupportedException();
    }

    @Override
    public boolean refreshDefaults(DBRProgressMonitor monitor, boolean useBootstrapSettings) throws DBException {
        // Check default active schema
        try (JDBCSession session = openSession(monitor, DBCExecutionPurpose.META, "Query active database")) {
            if (useBootstrapSettings) {
                DBPConnectionBootstrap bootstrap = getBootstrapSettings();
                if (!CommonUtils.isEmpty(bootstrap.getDefaultCatalogName())) {
                    setCurrentDatabaseName(monitor, bootstrap.getDefaultCatalogName());
                }
            }
            activeDatabaseName = GBase8aUtils.determineCurrentDatabase(session);
        } catch (DBException e) {
            throw new DBCException(e, this);
        }
        return true;
    }

    boolean setCurrentDatabase(DBRProgressMonitor monitor, GBase8aCatalog object) throws DBCException {
        if (object == null) {
            log.debug("Null current database");
            return false;
        }
        String databaseName = object.getName();
        return setCurrentDatabaseName(monitor, databaseName);
    }

    private boolean setCurrentDatabaseName(DBRProgressMonitor monitor, String databaseName) throws DBCException {
        try (JDBCSession session = openSession(monitor, DBCExecutionPurpose.UTIL, "Set active catalog")) {
            //  在 GBase JDBC 中，不要执行“use database”来试图改变当前Statement 使用的数据库，因为驱动不会发现你已经切换了数据库
            // 正确做法是通过 Connection.setCatalog()方式切换数据库，且只能在Statement 创建之前改变数据库，一旦Statement创建了就不能修改其使用的数据库。
            try {
                session.getOriginal().setCatalog(DBUtils.getQuotedIdentifier(getDataSource(), databaseName));
            } catch (SQLException e) {
                throw new DBCException(e, session.getExecutionContext());
            }
            this.activeDatabaseName = databaseName;
            return true;
        }
    }

    @NotNull
    @Override
    public DBCCachedContextDefaults getCachedDefault() {
        return new DBCCachedContextDefaults(activeDatabaseName, null);
    }
}
