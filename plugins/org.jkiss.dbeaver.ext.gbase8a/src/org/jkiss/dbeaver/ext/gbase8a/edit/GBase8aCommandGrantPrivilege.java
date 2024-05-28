package org.jkiss.dbeaver.ext.gbase8a.edit;

import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aPrivilege;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableBase;
import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aUser;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aVC;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.edit.DBECommand;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.impl.edit.DBECommandAbstract;
import org.jkiss.dbeaver.model.impl.edit.SQLDatabasePersistAction;

import java.util.Collection;
import java.util.Map;

public class GBase8aCommandGrantPrivilege extends DBECommandAbstract<GBase8aUser> {
    private final boolean grant;
    private final GBase8aVC vc;
    private final GBase8aCatalog schema;
    private final GBase8aTableBase table;
    private final GBase8aPrivilege privilege;

    public GBase8aCommandGrantPrivilege(GBase8aUser user, boolean grant, GBase8aVC vc, GBase8aCatalog schema, GBase8aTableBase table, GBase8aPrivilege privilege) {
        super(user, grant ? GBase8aMessages.edit_command_grant_privilege_action_grant_privilege : GBase8aMessages.edit_command_grant_privilege_name_revoke_privilege);
        this.vc = vc;
        this.grant = grant;
        this.schema = schema;
        this.table = table;
        this.privilege = privilege;
    }

    @Override
    public void updateModel() {
        getObject().clearGrantsCache();
    }

    public DBEPersistAction[] getPersistActions() {
        String privName = this.privilege.getName();
        String grantScript = "GRANT " + privName +
                " ON " + getObjectName() +
                " TO " + getObject().getFullName();
        String revokeScript = "REVOKE " + privName +
                " ON " + getObjectName() +
                " FROM " + getObject().getFullName();
        return new DBEPersistAction[]{
                new SQLDatabasePersistAction(
                        GBase8aMessages.edit_command_grant_privilege_action_grant_privilege,
                        this.grant ? grantScript : revokeScript)
        };
    }

    @Override
    public DBECommand<?> merge(DBECommand<?> prevCommand, Map<Object, Object> userParams) {
        if (prevCommand instanceof GBase8aCommandGrantPrivilege prevGrant) {
            if (prevGrant.schema == this.schema && prevGrant.table == this.table && prevGrant.privilege == this.privilege) {
                if (prevGrant.grant == this.grant) {
                    return prevCommand;
                }
                return null;
            }
        }
        return super.merge(prevCommand, userParams);
    }

    private String getObjectName() {
        String star = "*";
        String objName = null;
        if (this.privilege.getDataSource() instanceof GBase8aDataSource datasource) {
            Collection<GBase8aVC> vcs = datasource.getVcs(null);
            boolean isdefaultVc = false;
            if (vcs.size() == 1) {
                GBase8aVC vc = (GBase8aVC) vcs.toArray()[0];
                if (vc.getName().equalsIgnoreCase("vcname000001")) {
                    isdefaultVc = true;
                }
            }
            if (datasource.isVCCluster() && !isdefaultVc) {
                if (this.vc == null) {
                    objName = star + ".";
                } else {
                    objName = DBUtils.getQuotedIdentifier(this.vc) + ".";
                }
            } else {
                objName = "";
            }
            if (this.schema == null) {
                objName = objName + star + ".";
            } else {
                objName = objName + DBUtils.getQuotedIdentifier(this.schema) + ".";
            }
            if (this.table == null) {
                objName = objName + star;
            } else {
                objName = objName + DBUtils.getQuotedIdentifier((DBSObject) this.table);
            }
        }
        return objName;
    }
}
