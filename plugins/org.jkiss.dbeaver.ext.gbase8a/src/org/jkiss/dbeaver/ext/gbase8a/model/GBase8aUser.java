package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBPQualifiedObject;
import org.jkiss.dbeaver.model.DBPRefreshableObject;
import org.jkiss.dbeaver.model.DBPSaveableObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.access.DBARole;
import org.jkiss.dbeaver.model.access.DBAUser;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.utils.CommonUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

public class GBase8aUser implements DBAUser, DBARole, DBPRefreshableObject, DBPSaveableObject, DBPQualifiedObject {
    private static final Log log = Log.getLog(GBase8aUser.class);

    private final GBase8aDataSource dataSource;

    private String userName;

    private String host;

    private String passwordHash;
    private String sslType;
    private byte[] sslCipher;
    private byte[] x509Issuer;
    private byte[] x509Subject;
    private int maxQuestions;
    private int maxUpdates;
    private int maxConnections;
    private int maxUserConnections;
    private List<GBase8aGrant> grants;
    private boolean persisted;

    public GBase8aUser(GBase8aDataSource dataSource, ResultSet resultSet) {
        this.dataSource = dataSource;
        if (resultSet != null) {
            this.persisted = true;
            this.userName = CommonUtils.trim(JDBCUtils.safeGetString(resultSet, "user"));
            this.host = CommonUtils.trim(JDBCUtils.safeGetString(resultSet, "host"));
            this.passwordHash = JDBCUtils.safeGetString(resultSet, "password");

            this.sslType = JDBCUtils.safeGetString(resultSet, "ssl_type");
            this.sslCipher = JDBCUtils.safeGetBytes(resultSet, "ssl_cipher");
            this.x509Issuer = JDBCUtils.safeGetBytes(resultSet, "x509_issuer");
            this.x509Subject = JDBCUtils.safeGetBytes(resultSet, "x509_subject");

            this.maxQuestions = JDBCUtils.safeGetInt(resultSet, "max_questions");
            this.maxUpdates = JDBCUtils.safeGetInt(resultSet, "max_updates");
            this.maxConnections = JDBCUtils.safeGetInt(resultSet, "max_connections");
            this.maxUserConnections = JDBCUtils.safeGetInt(resultSet, "max_user_connections");
        } else {
            this.persisted = false;
            this.userName = "newuser";
            this.host = "%";
        }
    }


    @NotNull
    @Override
    @Property(viewable = true, order = 1)
    public String getName() {
        return userName + "@" + host;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFullName() {
        return "'" + userName + "'@'" + host + "'";
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public DBSObject getParentObject() {
        return dataSource.getContainer();
    }

    @NotNull
    @Override
    public GBase8aDataSource getDataSource() {
        return dataSource;
    }

    @Override
    public boolean isPersisted() {
        return persisted;
    }

    @Override
    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
        DBUtils.fireObjectUpdate(this);
    }

    @Property(viewable = true, order = 2)
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void clearGrantsCache() {
        this.grants = null;
    }

    public List<GBase8aGrant> getGrants(DBRProgressMonitor monitor) throws DBException {
        if (this.grants != null) {
            return this.grants;
        }
        if (!isPersisted()) {
            this.grants = new ArrayList<>();
            return this.grants;
        }
        try (JDBCSession session = DBUtils.openMetaSession(monitor, getDataSource(), "Read user privileges");
             JDBCPreparedStatement dbStat = session.prepareStatement("SHOW GRANTS FOR " + getFullName());
             JDBCResultSet dbResult = dbStat.executeQuery()) {
            List<GBase8aGrant> grants = new ArrayList<>();
            while (dbResult.next()) {
                List<GBase8aPrivilege> privileges = new ArrayList<>();
                boolean allPrivilegesFlag = false;
                boolean grantOption = false;
                String vc = null;
                String catalog = null;
                String table = null;
                String grantString = CommonUtils.notEmpty(JDBCUtils.safeGetString(dbResult, 1)).toUpperCase(Locale.ENGLISH);
                if (grantString.contains(" WITH GRANT OPTION")) {
                    grantOption = true;
                }
                Matcher matcher = GBase8aGrant.TABLE_GRANT_PATTERN.matcher(grantString);
                String privString;
                if (matcher.find()) {
                    privString = matcher.group(1);
                    catalog = matcher.group(2);
                    if (catalog.contains(".")) {
                        vc = catalog.split("[.]")[0];
                        catalog = catalog.split("[.]")[1];
                    }
                    table = matcher.group(3);
                } else {
                    matcher = GBase8aGrant.GLOBAL_GRANT_PATTERN.matcher(grantString);
                    if (!matcher.find()) {
                        log.warn("Can't parse GRANT string: " + grantString);
                        continue;
                    }
                    privString = matcher.group(1);
                }
                StringTokenizer st = new StringTokenizer(privString, ",");
                while (st.hasMoreTokens()) {
                    String privName = CommonUtils.trim(st.nextToken());
                    if (privName.equalsIgnoreCase("All Privileges")) {
                        allPrivilegesFlag = true;
                    } else {
                        GBase8aPrivilege priv = getDataSource().getPrivilege(monitor, privName);
                        if (priv == null) {
                            log.warn("Can't find privilege '" + privName + "'");
                        } else {
                            privileges.add(priv);
                        }
                    }
                }
                grants.add(new GBase8aGrant(this, privileges, vc, catalog, table, allPrivilegesFlag, grantOption));
            }
            this.grants = grants;
            return this.grants;
        } catch (SQLException e) {
            throw new DBException("Read user privileges failed", e);
        }
    }

    @Property(viewable = true, order = 20)
    public String getSslType() {
        return sslType;
    }

    void setSslType(String sslType) {
        this.sslType = sslType;
    }

    @Property(viewable = true, order = 21)
    public byte[] getSslCipher() {
        return sslCipher;
    }

    void setSslCipher(byte[] sslCipher) {
        this.sslCipher = sslCipher;
    }

    public byte[] getX509Issuer() {
        return x509Issuer;
    }

    void setX509Issuer(byte[] x509Issuer) {
        this.x509Issuer = x509Issuer;
    }

    public byte[] getX509Subject() {
        return x509Subject;
    }

    void setX509Subject(byte[] x509Subject) {
        this.x509Subject = x509Subject;
    }

    @Property(viewable = true, order = 22)
    public int getMaxQuestions() {
        return maxQuestions;
    }

    public void setMaxQuestions(int maxQuestions) {
        this.maxQuestions = maxQuestions;
    }

    @Property(viewable = true, order = 23)
    public int getMaxUpdates() {
        return maxUpdates;
    }

    public void setMaxUpdates(int maxUpdates) {
        this.maxUpdates = maxUpdates;
    }

    @Property(viewable = true, order = 24)
    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    @Property(viewable = true, order = 25)
    public int getMaxUserConnections() {
        return maxUserConnections;
    }

    public void setMaxUserConnections(int maxUserConnections) {
        this.maxUserConnections = maxUserConnections;
    }

    @Override
    public DBSObject refreshObject(@NotNull DBRProgressMonitor monitor) throws DBException {
        grants = null;
        return this;
    }

    @NotNull
    @Override
    public String getFullyQualifiedName(DBPEvaluationContext context) {
        return DBUtils.getQuotedIdentifier(dataSource, userName, false, true) + "@"
                + DBUtils.getQuotedIdentifier(dataSource, host, false, true);
    }
}