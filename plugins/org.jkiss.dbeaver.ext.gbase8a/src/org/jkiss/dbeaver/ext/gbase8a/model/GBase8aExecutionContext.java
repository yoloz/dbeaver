package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aUtils;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCExecutionContextDefaults;
import org.jkiss.dbeaver.model.exec.DBCExecutionPurpose;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCRemoteInstance;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.rdb.DBSSchema;
import org.jkiss.utils.CommonUtils;

/**
 * @author yolo
 */
public class GBase8aExecutionContext extends JDBCExecutionContext implements DBCExecutionContextDefaults<GBase8aCatalog, DBSSchema> {
    private static final Log log = Log.getLog(GBase8aExecutionContext.class);

    public GBase8aExecutionContext(JDBCRemoteInstance instance, String purpose) {
        super(instance, purpose);
    }

    @Override
    public GBase8aCatalog getDefaultCatalog() {
        GBase8aDataSource gBase8aDataSource = ((GBase8aDataSource) getDataSource());
        String activeName = gBase8aDataSource.getActiveCatalogName();
        GBase8aCatalog gBase8aCatalog = gBase8aDataSource.getSysCatalog(activeName);
        if (gBase8aCatalog == null) {
            gBase8aDataSource.getActiveVC().getCatalog(activeName);
        }
        return gBase8aCatalog;
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
        try {
            ((GBase8aDataSource) getDataSource()).getActiveVC().setDefaultObject(monitor, this, catalog);
        } catch (DBException e) {
            throw new DBCException("setDefaultCatalog", e);
        }
    }

    @Override
    public void setDefaultSchema(DBRProgressMonitor monitor, DBSSchema schema) throws DBCException {

    }

    @Override
    public boolean refreshDefaults(DBRProgressMonitor monitor, boolean useBootstrapSettings) throws DBException {
        GBase8aDataSource gBase8aDataSource = (GBase8aDataSource) getDataSource();
        String oldCatalogName = gBase8aDataSource.getActiveCatalogName();
        try (JDBCSession session = openSession(monitor, DBCExecutionPurpose.UTIL, "refresh catalog")) {
            String newCatalogName = GBase8aUtils.determineCurrentDatabase(session);
            if (!CommonUtils.equalObjects(newCatalogName, oldCatalogName)) {
                GBase8aCatalog newCatalog = gBase8aDataSource.getActiveVC().getCatalog(newCatalogName);
                if (newCatalog != null) {
                    setDefaultCatalog(monitor, newCatalog, null);
                    return true;
                }
            }
        }
        return false;
    }
}
