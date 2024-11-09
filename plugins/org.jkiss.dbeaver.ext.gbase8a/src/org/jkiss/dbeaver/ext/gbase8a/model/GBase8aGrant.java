package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.access.DBAPrivilegeGrant;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.sql.SQLUtils;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.utils.CommonUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * User privilege grant
 */
public class GBase8aGrant implements DBSObject, DBAPrivilegeGrant {

    public static final Pattern TABLE_GRANT_PATTERN = Pattern.compile("GRANT\\s+(.+)\\s+ON\\s+`?([^`]+)`?\\.`?([^`]+)`?\\s+TO\\s+");
    public static final Pattern GLOBAL_GRANT_PATTERN = Pattern.compile("GRANT\\s+(.+)\\s+ON\\s+(.+)\\s+TO\\s+");

    private final GBase8aUser user;
    private final List<GBase8aPrivilege> privileges;
    @Nullable
    private String vcName;
    @Nullable
    private final String catalogName;
    @Nullable
    private final String tableName;
    private final boolean allPrivileges;
    private boolean grantOption;

    public GBase8aGrant(GBase8aUser user, List<GBase8aPrivilege> privileges, @Nullable String vcName, @Nullable String catalogName,
                        @Nullable String tableName, boolean allPrivileges, boolean grantOption) {
        this.user = user;
        this.privileges = privileges;
        if (vcName != null) {
            this.vcName = vcName.replace("\"", "");
        }
        this.catalogName = catalogName.replace("\"", "");
        this.tableName = tableName.replace("\"", "");
        this.allPrivileges = allPrivileges;
        this.grantOption = grantOption;
    }

    @Nullable
    @Override
    public GBase8aUser getParentObject() {
        return this.user;
    }

    @NotNull
    @Override
    public DBPDataSource getDataSource() {
        return this.user.getDataSource();
    }

    @NotNull
    @Override
    public String getName() {
        return allPrivileges ? "ALL PRIVILEGES" : privileges.toString();
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public boolean isPersisted() {
        return true;
    }

    @Override
    public GBase8aUser getSubject(@NotNull DBRProgressMonitor monitor) {
        return user;
    }

    @Override
    public Object getObject(@NotNull DBRProgressMonitor monitor) throws DBException {
        if (catalogName != null) {
            if (!isAllCatalogs()) {
                GBase8aCatalog catalog = user.getDataSource().getCatalog(catalogName);
                if (catalog != null) {
                    if (!isAllTables()) {
                        GBase8aTable table = catalog.getTable(monitor, tableName);
                        if (table != null) {
                            return table;
                        }
                    }
                }
            }
        }
        String fullName = catalogName + "." + tableName;
        if (vcName != null) {
            fullName = vcName + "." + fullName;
        }
        return fullName;
    }

    @Override
    public GBase8aPrivilege[] getPrivileges() {
        return privileges.toArray(new GBase8aPrivilege[0]);
    }

    @Property(viewable = true, order = 1, name = "privileges")
    public String getPrivilegeNames() {
        return allPrivileges ? "ALL PRIVILEGES" : privileges.toString();
    }

    @Property(viewable = true, order = 3, name = "vc")
    @Nullable
    public String getVcName() {
        return isAllVCs() ? "*" : vcName;
    }

    @Property(viewable = true, order = 3, name = "catalog")
    @Nullable
    public String getCatalog() {
        return isAllCatalogs() ? "*" : catalogName;
    }

    @Property(viewable = true, order = 4, name = "table")
    @Nullable
    public String getTable() {
        return isAllTables() ? "*" : tableName;
    }

    @Override
    public boolean isGranted() {
        return true;
    }

    public boolean isAllCatalogs() {
        return "*".equals(this.catalogName);
    }

    public boolean isAllVCs() {
        return "*".equals(this.vcName);
    }

    public boolean isAllTables() {
        return "*".equals(this.tableName);
    }

    public boolean isAllPrivileges() {
        return this.allPrivileges;
    }

    public void addPrivilege(GBase8aPrivilege privilege) {
        this.privileges.add(privilege);
    }

    public void removePrivilege(GBase8aPrivilege privilege) {
        this.privileges.remove(privilege);
    }

    public boolean isGrantOption() {
        return this.grantOption;
    }

    public void setGrantOption(boolean grantOption) {
        this.grantOption = grantOption;
    }

    public boolean isEmpty() {
        return (this.privileges.isEmpty() && !isAllPrivileges() && !isGrantOption());
    }

    public boolean matches(GBase8aVC vc) {
        return !((vc != null || !isAllVCs()) && (vc == null || !vc.getName().equalsIgnoreCase(this.vcName)));
    }

    public boolean matches(GBase8aCatalog catalog) {
        return (catalog == null && isAllCatalogs())
                || (catalog != null && CommonUtils.isNotEmpty(catalogName) && !isAllCatalogs()
                && SQLUtils.matchesLike(catalog.getName(), catalogName));
    }

    public boolean matches(GBase8aTableBase table) {
        return (table == null && isAllTables()) || (table != null && table.getName().equalsIgnoreCase(tableName));
    }

    public boolean hasNonAdminPrivileges() {
        for (GBase8aPrivilege priv : this.privileges) {
            if (priv.getKind() != GBase8aPrivilege.Kind.ADMIN) {
                return true;
            }
        }
        return false;
    }

    public boolean isStatic() {
        return CommonUtils.isEmpty(catalogName) || "*".equals(catalogName);
    }
}
