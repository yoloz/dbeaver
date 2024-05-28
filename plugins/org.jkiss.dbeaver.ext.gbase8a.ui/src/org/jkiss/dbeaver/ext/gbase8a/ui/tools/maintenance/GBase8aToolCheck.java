package org.jkiss.dbeaver.ext.gbase8a.ui.tools.maintenance;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTable;
import org.jkiss.dbeaver.ext.gbase8a.ui.tools.IExternalTool;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.ui.UIUtils;

import java.util.Collection;
import java.util.List;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.utils.CommonUtils;


public class GBase8aToolCheck implements IExternalTool {
    public void execute(IWorkbenchWindow window, IWorkbenchPart activePart, Collection<DBSObject> objects) throws DBException {
        List<GBase8aTable> tables = CommonUtils.filterCollection(objects, GBase8aTable.class);
        if (!tables.isEmpty()) {
            SQLDialog dialog = new SQLDialog(activePart.getSite(), tables);
            dialog.open();
        }
    }

    static class SQLDialog
            extends TableToolDialog {
        private Combo optionCombo;

        public SQLDialog(IWorkbenchPartSite partSite, Collection<GBase8aTable> selectedTables) {
            super(partSite, "Check table(s)", selectedTables);
        }


        protected void generateObjectCommand(List<String> lines, GBase8aTable object) {
            String sql = "CHECK TABLE " + object.getFullyQualifiedName(DBPEvaluationContext.DDL);
            String option = this.optionCombo.getText();
            if (!CommonUtils.isEmpty(option)) sql = sql + " " + option;
            lines.add(sql);
        }


        protected void createControls(Composite parent) {
            Group optionsGroup = UIUtils.createControlGroup(parent, "Options", 1, 0, 0);
            optionsGroup.setLayoutData(new GridData(768));
            this.optionCombo = UIUtils.createLabelCombo(optionsGroup, "Option", 12);
            this.optionCombo.add("");
            this.optionCombo.add("FOR UPGRADE");
            this.optionCombo.add("QUICK");
            this.optionCombo.add("FAST");
            this.optionCombo.add("MEDIUM");
            this.optionCombo.add("EXTENDED");
            this.optionCombo.add("CHANGED");
            this.optionCombo.addSelectionListener(this.SQL_CHANGE_LISTENER);

            createObjectsSelector(parent);
        }
    }
}
