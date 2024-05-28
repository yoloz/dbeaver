package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.Nullable;

import java.util.List;
import java.util.regex.Pattern;


public class GBase8aGrant {

    public static final Pattern TABLE_GRANT_PATTERN = Pattern.compile("GRANT\\s+(.+)\\s+ON\\s+`?([^`]+)`?\\.`?([^`]+)`?\\s+TO\\s+");
    public static final Pattern GLOBAL_GRANT_PATTERN = Pattern.compile("GRANT\\s+(.+)\\s+ON\\s+(.+)\\s+TO\\s+");

    private GBase8aUser user;

    private List<GBase8aPrivilege> privileges;
    @Nullable
    private String vcName;
    @Nullable
    private String catalogName;
    @Nullable
    private String tableName;
    private boolean allPrivileges;
    private boolean grantOption;

    public GBase8aGrant(GBase8aUser user, List<GBase8aPrivilege> privileges, @Nullable String vcName, @Nullable String catalogName, @Nullable String tableName, boolean allPrivileges, boolean grantOption) {
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


    public GBase8aUser getUser() {
        return this.user;
    }


    public List<GBase8aPrivilege> getPrivileges() {
        return this.privileges;
    }


    public boolean isAllCatalogs() {
        return "*".equals(this.catalogName);
    }


    public boolean isAllVCs() {
        return "*".equals(this.vcName);
    }


    @Nullable
    public String getCatalog() {
        return this.catalogName;
    }


    @Nullable
    public String getTable() {
        return this.tableName;
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
        return !((catalog != null || !isAllCatalogs()) && (catalog == null || !catalog.getName().equalsIgnoreCase(this.catalogName)));
    }


    public boolean matches(GBase8aTableBase table) {
        return !((table != null || !isAllTables()) && (table == null || !table.getName().equalsIgnoreCase(this.tableName)));
    }


    public boolean hasNonAdminPrivileges() {
        for (GBase8aPrivilege priv : this.privileges) {
            if (priv.getKind() != GBase8aPrivilege.Kind.ADMIN) {
                return true;
            }
        }
        return false;
    }
}
