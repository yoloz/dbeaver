package org.jkiss.dbeaver.ext.gbase8a;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.DatabaseURL;
import org.jkiss.dbeaver.model.connection.DBPConnectionConfiguration;
import org.jkiss.dbeaver.model.connection.DBPDriver;
import org.jkiss.dbeaver.model.connection.DBPNativeClientLocation;
import org.jkiss.dbeaver.model.connection.DBPNativeClientLocationManager;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCDataSourceProvider;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.utils.CommonUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GBase8aDataSourceProvider extends JDBCDataSourceProvider implements DBPNativeClientLocationManager {

    private static final Log log = Log.getLog(GBase8aDataSourceProvider.class);

    private static final String REGISTRY_ROOT_GBASE8A_32 = "SOFTWARE\\GBASE AB";
    private static final String REGISTRY_ROOT_GBASE8A_64 = "SOFTWARE\\Wow6432Node\\GBASE AB";
    private static final String REGISTRY_ROOT_MARIADB = "SOFTWARE\\Monty Program AB";
    private static final String SERER_LOCATION_KEY = "Location";
    private static final String INSTALLDIR_KEY = "INSTALLDIR";
//    private static Map<String, GBase8aServerHome> localServers = null;


    private static final Map<String, String> connectionsProps = new HashMap<>();


    static {
        connectionsProps.put("zeroDateTimeBehavior", "convertToNull");
        connectionsProps.put("characterEncoding", "UTF-8");
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
    public long getFeatures()
    {
        return FEATURE_CATALOGS;
    }


    public String getConnectionURL(DBPDriver driver, DBPConnectionConfiguration connectionInfo) {
        return DatabaseURL.generateUrlByTemplate(connectionInfo.getUrl(), connectionInfo);
//        StringBuilder url = new StringBuilder();
//        url.append("jdbc:yzsec://");
//        if (connectionInfo.getHostName().contains(":")) {
//            url.append("(");
//            url.append(connectionInfo.getHostName());
//            url.append(")");
//        } else {
//            url.append(connectionInfo.getHostName());
//        }
//        if (!CommonUtils.isEmpty(connectionInfo.getHostPort())) {
//            url.append(":").append(connectionInfo.getHostPort());
//        }
//        url.append("/");
//        if (!CommonUtils.isEmpty(connectionInfo.getDatabaseName())) {
//            url.append(connectionInfo.getDatabaseName());
//        }
//        return url.toString();
    }


    @NotNull
    public DBPDataSource openDataSource(@NotNull DBRProgressMonitor monitor, @NotNull DBPDataSourceContainer container) throws DBException {
        return new GBase8aDataSource(monitor, container);
    }


    /*public static synchronized void findLocalClients() {
        if (localServers != null) { return;
        }
        localServers = new LinkedHashMap<>();
        String path = System.getenv("PATH");
        if (path != null) {
            byte b;
            int i;
            String[] arrayOfString;
            for (i = (arrayOfString = path.split(File.pathSeparator)).length, b = 0; b < i; ) {
                String token = arrayOfString[b];
                token = CommonUtils.removeTrailingSlash(token);
                File gbase8aFile = new File(token, GBase8aUtils.getGBase8aConsoleBinaryName());
                if (gbase8aFile.exists()) {
                    File binFolder = gbase8aFile.getAbsoluteFile().getParentFile();
                    if (binFolder.getName().equalsIgnoreCase("bin")) {
                        String homeId = CommonUtils.removeTrailingSlash(binFolder.getParentFile().getAbsolutePath());
                        localServers.put(homeId, new GBase8aServerHome(homeId, null));
                    }
                }
                b++;
            }

        }
        if (RuntimeUtils.isWindows()) {
            try {
                LocalSystemRegistry.Registry registry = LocalSystemRegistry.getInstance();
                String registryRoot = RuntimeUtils.isOSArchAMD64() ? "SOFTWARE\\Wow6432Node\\GBASE AB" : "SOFTWARE\\GBASE AB";
                if (registry.registryKeyExists("HKEY_LOCAL_MACHINE", registryRoot)) {
                    String[] list1 = registry.registryGetKeys("HKEY_LOCAL_MACHINE", registryRoot);
                    if (list1 != null) {
                        for (String homeKey : list1) {
                            Map<String, Object> valuesMap = registry.registryGetValues("HKEY_LOCAL_MACHINE", registryRoot + "\\" + homeKey);
                            if (valuesMap != null) {
                                for (String key : valuesMap.keySet()) {
                                    if ("Location".equalsIgnoreCase(key)) {
                                        String serverPath = CommonUtils.removeTrailingSlash(CommonUtils.toString(valuesMap.get(key)));
                                        localServers.put(serverPath, new GBase8aServerHome(serverPath, homeKey));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (registry.registryKeyExists("HKEY_LOCAL_MACHINE", "SOFTWARE\\Monty Program AB")) {
                    String[] homeKeys = registry.registryGetKeys("HKEY_LOCAL_MACHINE", "SOFTWARE\\Monty Program AB");
                    if (homeKeys != null) {
                        for (String homeKey : homeKeys) {
                            Map<String, Object> valuesMap = registry.registryGetValues("HKEY_LOCAL_MACHINE", "SOFTWARE\\Monty Program AB\\" + homeKey);
                            if (valuesMap != null) {
                                for (String key : valuesMap.keySet()) {
                                    if ("INSTALLDIR".equalsIgnoreCase(key)) {
                                        String serverPath = CommonUtils.removeTrailingSlash(CommonUtils.toString(valuesMap.get(key)));
                                        localServers.put(serverPath, new GBase8aServerHome(serverPath, homeKey));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                log.warn("Error reading Windows registry", e);
            }
        }
    }*/

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
        return "";
    }

    @Override
    public String getProductVersion(DBPNativeClientLocation location) {
        return "";
    }

    @Override
    public boolean supportsNativeClients() {
        return false;
    }

}
