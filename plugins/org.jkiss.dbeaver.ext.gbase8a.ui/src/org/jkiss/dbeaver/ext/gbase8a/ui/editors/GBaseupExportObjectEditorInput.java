package org.jkiss.dbeaver.ext.gbase8a.ui.editors;

import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;


public class GBaseupExportObjectEditorInput implements IEditorInput {
    private String name = GBase8aMessages.editors_export_object_title;

    private String dbname;

    private GBase8aDataSource dataSource;

    public GBaseupExportObjectEditorInput(GBase8aDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public GBase8aDataSource getDataSource() {
        return this.dataSource;
    }

    public void setDataSource(GBase8aDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Object getAdapter(Class adapter) {
        return null;
    }

    public boolean exists() {
        return false;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return this.name;
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public String getToolTipText() {
        return "";
    }

    public String getDbname() {
        return this.dbname;
    }

    public void setDbname(String dbname) {
        this.dbname = dbname;
    }
}
