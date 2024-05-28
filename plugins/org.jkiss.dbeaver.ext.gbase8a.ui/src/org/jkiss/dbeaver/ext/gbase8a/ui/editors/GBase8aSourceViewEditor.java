package org.jkiss.dbeaver.ext.gbase8a.ui.editors;

import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aSourceObject;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.ui.editors.sql.SQLSourceViewer;


public class GBase8aSourceViewEditor extends SQLSourceViewer<GBase8aSourceObject> {

    protected boolean isReadOnly() {
        return false;
    }

    protected void setSourceText(DBRProgressMonitor monitor, String sourceText) {
        getInputPropertySource().setPropertyValue(monitor, "objectDefinitionText", sourceText);
    }
}
