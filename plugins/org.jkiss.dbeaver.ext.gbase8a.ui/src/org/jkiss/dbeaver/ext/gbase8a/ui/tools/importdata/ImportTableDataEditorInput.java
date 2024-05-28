//package org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata;
//
//import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
//import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
//import org.eclipse.jface.resource.ImageDescriptor;
//import org.eclipse.ui.IEditorInput;
//import org.eclipse.ui.IPersistableElement;
//
//
//public class ImportTableDataEditorInput implements IEditorInput {
//    private String name = GBase8aMessages.import_properties_import_importdata;
//
//    private String dbname;
//
//    private String tableName;
//
//    private GBase8aDataSource dataSource;
//
//    private DBNDatabaseNode dbnDatabaseNode;
//    private String vcName;
//
//    public Object getAdapter(Class adapter) {
//        return null;
//    }
//
//
//    public boolean exists() {
//        return false;
//    }
//
//
//    public ImageDescriptor getImageDescriptor() {
//        return null;
//    }
//
//
//    public String getName() {
//        return this.name;
//    }
//
//
//    public IPersistableElement getPersistable() {
//        return null;
//    }
//
//
//    public String getToolTipText() {
//        return this.name;
//    }
//
//    public String getDbname() {
//        return this.dbname;
//    }
//
//    public void setDbname(String dbname) {
//        this.dbname = dbname;
//    }
//
//    public GBase8aDataSource getDataSource() {
//        return this.dataSource;
//    }
//
//    public void setDataSource(GBase8aDataSource dataSource) {
//        this.dataSource = dataSource;
//    }
//
//    public DBNDatabaseNode getDBNDatabaseNode() {
//        return this.dbnDatabaseNode;
//    }
//
//    public void setDBNDatabaseNode(DBNDatabaseNode dbnDatabaseNode) {
//        this.dbnDatabaseNode = dbnDatabaseNode;
//    }
//
//    public String getTableName() {
//        return this.tableName;
//    }
//
//    public void setTableName(String tableName) {
//        this.tableName = tableName;
//    }
//
//    public String getVcName() {
//        return this.vcName;
//    }
//
//    public void setVcName(String vcName) {
//        this.vcName = vcName;
//    }
//}
