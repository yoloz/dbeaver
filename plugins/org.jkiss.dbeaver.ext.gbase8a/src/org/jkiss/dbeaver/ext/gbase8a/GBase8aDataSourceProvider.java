package org.jkiss.dbeaver.ext.gbase8a;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.DatabaseURL;
import org.jkiss.dbeaver.model.connection.DBPConnectionConfiguration;
import org.jkiss.dbeaver.model.connection.DBPDriver;
import org.jkiss.dbeaver.model.connection.DBPDriverConfigurationType;
import org.jkiss.dbeaver.model.connection.DBPNativeClientLocation;
import org.jkiss.dbeaver.model.connection.DBPNativeClientLocationManager;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCDataSourceProvider;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.utils.GeneralUtils;
import org.jkiss.utils.CommonUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GBase8aDataSourceProvider extends JDBCDataSourceProvider implements DBPNativeClientLocationManager {

    private static final Map<String, String> connectionsProps = new HashMap<>();

    static {
        connectionsProps.put("zeroDateTimeBehavior", "convertToNull");
        connectionsProps.put("characterEncoding", GeneralUtils.UTF8_ENCODING);
        connectionsProps.put("tinyInt1isBit", "false");
    }

    public static Map<String, String> getConnectionsProps() {
        return connectionsProps;
    }

    protected String getConnectionPropertyDefaultValue(String name, String value) {
        String ovrValue = connectionsProps.get(name);
        return (ovrValue != null) ? ovrValue : super.getConnectionPropertyDefaultValue(name, value);
    }

    @Override
    public long getFeatures() {
        return FEATURE_CATALOGS;
    }


    public String getConnectionURL(DBPDriver driver, DBPConnectionConfiguration connectionInfo) {
        if (connectionInfo.getConfigurationType() == DBPDriverConfigurationType.URL) {
            return connectionInfo.getUrl();
        }
        if (driver.isSampleURLApplicable()) {
            return DatabaseURL.generateUrlByTemplate(driver, connectionInfo);
        }
        StringBuilder url = new StringBuilder();
        if (driver.getSampleURL().contains(":yzsec")) {
            url.append("jdbc:yzsec://");
        } else {
            url.append("jdbc:gbase://");
        }
        url.append(connectionInfo.getHostName());
        if (!CommonUtils.isEmpty(connectionInfo.getHostPort())) {
            url.append(":").append(connectionInfo.getHostPort());
        }
        url.append("/");
        if (!CommonUtils.isEmpty(connectionInfo.getDatabaseName())) {
            url.append(connectionInfo.getDatabaseName());
        }
        String vcNameStr = connectionInfo.getProperty("vcName");
        if (vcNameStr != null && !vcNameStr.isEmpty()) {
            url.append("?").append("vcName=").append(vcNameStr);
        }
        return url.toString();
    }


    @NotNull
    @Override
    public DBPDataSource openDataSource(@NotNull DBRProgressMonitor monitor, @NotNull DBPDataSourceContainer container)
            throws DBException{
        return new GBase8aDataSource(monitor, container);
    }

    //Native Client

    @Override
    public boolean supportsNativeClients() {
        return false;
    }

    @Override
    public List<DBPNativeClientLocation> findLocalClientLocations() {
        return Collections.emptyList();
    }

    @Override
    public DBPNativeClientLocation getDefaultLocalClientLocation() {
        return null;
    }

    @Override
    public String getProductName(DBPNativeClientLocation location) {
        return "GBase8a";
    }

    @Override
    public String getProductVersion(DBPNativeClientLocation location) {
        return "";
    }


}
