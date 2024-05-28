package org.jkiss.dbeaver.ext.gbase8a.ui;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.swt.widgets.Display;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBIcon;
import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTable;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableFullIndex;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseItem;
import org.jkiss.dbeaver.model.navigator.DBNNode;


public class GBase8aTableTester extends PropertyTester {
    protected static final Log log = Log.getLog(GBase8aTableTester.class);
    public static final String PROP_CANMODIFYTABLE = "canModify8aTable";
    public static final String PROP_CANDROPTABLEDISTRIBUTEDCOLUMN = "canDrop8aTableDistributedColumn";
    public static final String PROP_CANMODIFYTABLELIMITSIZE = "canModify8aTableLimitSize";
    public static final String PROP_CANUPDATEINDEX = "canUpdate8aIndex";
    public static final String PROP_CANIMPORTDATA = "canImport8aData";
    public static final String PROP_EXPORT_OBJECT = "canExportObject";
    public static final String PROP_CANIMPORTDATAFROMDB = "canImport8aDataFromDB";
    public static final String PROP_CANCREATEDB = "canCreatedb";
    public static final String PROP_CANDELETEDB = "canDeletedb";
    public static final String PROP_CANLOADSQL = "canLoadSQL";
    public static final String PROP_CANCREATETABLESPACE = "canCreateTablespace";
    public static final String PROP_CANDELETETABLESPACE = "canDeleteTablespace";
    public static final String PROP_CANMODIFYTABLESPACE = "canModifyTablespace";
    public static final String PROP_CANSETDEFAULTTABLESPACE = "canSetDefaultTablespace";
    public static final String PROP_CANREFRESHTABLESPACE = "canRefreshTablespace";
    public static final String PROP_CREATECONSUMERGROUP = "createconsumergroup";
    public static final String PROP_CREATERESOURCEPOOL = "createresourcepool";
    public static final String PROP_CREATERESOURCEPLAN = "createresourceplan";
    public static final String PROP_DELCONSUMERGROUP = "delconsumergroup";
    public static final String PROP_MODIFYCONSUMERGROUP = "modifyconsumergroup";
    public static final String PROP_DELRESOURCEPOOL = "delresourcepool";
    public static final String PROP_RENAMERESOURCEPOOL = "renameresourcepool";
    public static final String PROP_MODIFYRESOURCEPOOL = "modifyresourcepool";
    public static final String PROP_SHOWRESOURCEPOOLUSAGEONNODES = "showresourcepoolusageonnodes";
    public static final String PROP_SHOWRESOURCEPOOLUSAGEONCOORDINATORS = "showresourcepoolusageoncoordinators";
    public static final String PROP_SHOWRESOURCEPOOLSTATUSONNODES = "showresourcepoolstatusonnodes";
    public static final String PROP_SHOWRESOURCEPOOLSTATUSONCOORDINATORS = "showresourcepoolstatusoncoordinators";
    public static final String PROP_SHOWRESOURCEPOOLEVENTS = "showresourcepoolevents";
    public static final String PROP_DELRESOURCEPLAN = "delresourceplan";
    public static final String PROP_MODIFYRESOURCEPLAN = "modifyresourceplan";
    public static final String PROP_CREATERESOURCEDIRECTIVE = "createresourcedirective";
    public static final String PROP_DELRESOURCEDIRECTIVE = "delresourcedirective";
    public static final String PROP_ACTIVERESOURCEPLAN = "activeresourceplan";
    public static final String PROP_CLOSERESOURCEPLAN = "closeresourceplan";
    public static final String PROP_RESOURCECONFIGMANAGER = "resourceconfigmanager";

    public GBase8aTableTester() {
    }

    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        DBNNode node = (DBNNode) receiver;
        if (receiver == null) {
            return false;
        } else {
            Display display = Display.getCurrent();
            if (display == null) {
                return false;
            } else {
                DBNDatabaseItem item = null;
                if ((property.equals("canExportObject") || property.equals("canImport8aData")) && node instanceof DBNDatabaseItem) {
                    item = (DBNDatabaseItem) node;
                    if ( item.getObject() instanceof GBase8aCatalog) {
                        return true;
                    }
                }

                if (property.equals("canModify8aTable")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getObject() instanceof GBase8aTable;
                    }
                } else if (property.equals("canDrop8aTableDistributedColumn")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getObject() instanceof GBase8aTable;
                    }
                } else if (property.equals("canModify8aTableLimitSize")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getObject() instanceof GBase8aTable;
                    }
                } else if (property.equals("canUpdate8aIndex")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getObject() instanceof GBase8aTableFullIndex;
                    }
                } else if (property.equals("canImport8aData")) {
                    return node.getParentNode() != null && node.getParentNode().getName().equals(GBase8aMessages.tree_user_tables_node_name) && node.getParentNode().getParentNode() != null && node.getParentNode().getParentNode().getParentNode() != null && node.getParentNode().getParentNode().getParentNode().getParentNode().getName().equals(GBase8aMessages.tree_user_databases_node_name) && node instanceof DBNDatabaseItem;
                } else if (property.equals("canCreatedb")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode() != null && node.getParentNode().getName().equals(GBase8aMessages.tree_user_databases_node_name);
                    }
                } else if (property.equals("canDeletedb")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode() != null && node.getParentNode().getParentNode() != null && node.getParentNode().getParentNode().getName().equals(GBase8aMessages.tree_user_databases_node_name);
                    }
                } else if (property.equals("canLoadSQL")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode() != null && node.getParentNode().getParentNode() != null && node.getParentNode().getParentNode().getName().equals(GBase8aMessages.tree_user_databases_node_name);
                    }
                } else if (property.equals("canCreateTablespace")) {
                    if (node.getParentNode() instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node.getParentNode();
                        if ( item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource")  &&  node.getName().equals(GBase8aMessages.tree_user_tablespaces_node_name) && item.getDataSource() instanceof GBase8aDataSource datasource) {
                            return datasource.isSupportTablespace();
                        }
                    }
                } else if (property.equals("canDeleteTablespace")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode().getName().equals(GBase8aMessages.tree_user_tablespaces_node_name) && !item.getName().equalsIgnoreCase("sys_tablespace");
                    }
                } else if (property.equals("canModifyTablespace")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode().getName().equals(GBase8aMessages.tree_user_tablespaces_node_name);
                    }
                } else if (property.equals("canSetDefaultTablespace")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode().getName().equals(GBase8aMessages.tree_user_tablespaces_node_name);
                    }
                } else if (property.equals("canRefreshTablespace")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode().getName().equals(GBase8aMessages.tree_user_tablespaces_node_name);
                    }
                } else if (property.equals("createconsumergroup")) {
                    if (node.getParentNode() instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node.getParentNode();
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getName().equals(GBase8aMessages.tree_resource_consumergroup);
                    }
                } else if (property.equals("createresourcepool")) {
                    if (node.getParentNode() instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node.getParentNode();
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getName().equals(GBase8aMessages.tree_resource_resourcepool);
                    }
                } else if (property.equals("createresourceplan")) {
                    if (node.getParentNode() instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node.getParentNode();
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getName().equals(GBase8aMessages.tree_resource_resourceplan);
                    }
                } else if (property.equals("delconsumergroup")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode() != null && node.getParentNode().getName().equals(GBase8aMessages.tree_resource_consumergroup);
                    }
                } else if (property.equals("modifyconsumergroup")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode() != null && node.getParentNode().getName().equals(GBase8aMessages.tree_resource_consumergroup);
                    }
                } else if (property.equals("delresourcepool")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode() != null && node.getParentNode().getName().equals(GBase8aMessages.tree_resource_resourcepool);
                    }
                } else if (property.equals("renameresourcepool")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode() != null && node.getParentNode().getName().equals(GBase8aMessages.tree_resource_resourcepool);
                    }
                } else if (property.equals("modifyresourcepool")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode() != null && node.getParentNode().getName().equals(GBase8aMessages.tree_resource_resourcepool);
                    }
                } else if (property.equals("showresourcepoolusageonnodes")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode() != null && node.getParentNode().getName().equals(GBase8aMessages.tree_resource_resourcepool);
                    }
                } else if (property.equals("showresourcepoolusageoncoordinators")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode() != null && node.getParentNode().getName().equals(GBase8aMessages.tree_resource_resourcepool);
                    }
                } else if (property.equals("showresourcepoolstatusonnodes")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode() != null && node.getParentNode().getName().equals(GBase8aMessages.tree_resource_resourcepool);
                    }
                } else if (property.equals("showresourcepoolstatusoncoordinators")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode() != null && node.getParentNode().getName().equals(GBase8aMessages.tree_resource_resourcepool);
                    }
                } else if (property.equals("showresourcepoolevents")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode() != null && node.getParentNode().getName().equals(GBase8aMessages.tree_resource_resourcepool);
                    }
                } else if (property.equals("delresourceplan")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode() != null && node.getParentNode().getName().equals(GBase8aMessages.tree_resource_resourceplan);
                    }
                } else if (property.equals("modifyresourceplan")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode() != null && node.getParentNode().getName().equals(GBase8aMessages.tree_resource_resourceplan);
                    }
                } else if (property.equals("createresourcedirective")) {
                    if (node.getParentNode() instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node.getParentNode();
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getName().equals(GBase8aMessages.tree_resource_resourcedirective);
                    }
                } else if (property.equals("delresourcedirective")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode() != null && node.getParentNode().getName().equals(GBase8aMessages.tree_resource_resourcedirective);
                    }
                } else if (property.equals("activeresourceplan")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode() != null && node.getParentNode().getName().equals(GBase8aMessages.tree_resource_resourceplan) && !node.getNodeIcon().equals(DBIcon.TREE_LOCKED);
                    }
                } else if (property.equals("closeresourceplan")) {
                    if (node instanceof DBNDatabaseItem) {
                        item = (DBNDatabaseItem) node;
                        return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode() != null && node.getParentNode().getName().equals(GBase8aMessages.tree_resource_resourceplan) && node.getNodeIcon().equals(DBIcon.TREE_LOCKED);
                    }
                } else if (property.equals("resourceconfigmanager") && node instanceof DBNDatabaseItem) {
                    item = (DBNDatabaseItem) node;
                    return item.getDataSource() != null && item.getDataSource().getClass().toString().contains("GBase8aDataSource") && node.getParentNode() != null && node.getParentNode().getName().equals(GBase8aMessages.tree_resource_node_name);
                }
                return false;
            }
        }
    }
}
