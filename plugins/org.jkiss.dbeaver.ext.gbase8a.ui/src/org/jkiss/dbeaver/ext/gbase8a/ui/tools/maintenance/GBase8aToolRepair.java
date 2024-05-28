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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.utils.CommonUtils;


public class GBase8aToolRepair implements IExternalTool {
    public void execute(IWorkbenchWindow window, IWorkbenchPart activePart, Collection<DBSObject> objects) throws DBException {
        List<GBase8aTable> tables = CommonUtils.filterCollection(objects, GBase8aTable.class);
        if (!tables.isEmpty()) {
            SQLDialog dialog = new SQLDialog(activePart.getSite(), tables);
            dialog.open();
        }
    }

    static class SQLDialog
            extends TableToolDialog {
        private Button quickCheck;
        private Button extendedCheck;
        private Button frmCheck;

        public SQLDialog(IWorkbenchPartSite partSite, Collection<GBase8aTable> selectedTables) {
            super(partSite, "Repair table(s)", selectedTables);
        }


        protected void generateObjectCommand(List<String> lines, GBase8aTable object) {
            String sql = "REPAIR TABLE " + object.getFullyQualifiedName(DBPEvaluationContext.DDL);
            if (this.quickCheck.getSelection()) sql = sql + " QUICK";
            if (this.extendedCheck.getSelection()) sql = sql + " EXTENDED";
            if (this.frmCheck.getSelection()) sql = sql + " USE_FRM";
            lines.add(sql);
        }


        protected void createControls(Composite parent) {
            Group optionsGroup = UIUtils.createControlGroup(parent, "Options", 1, 0, 0);
            optionsGroup.setLayoutData(new GridData(768));
            this.quickCheck = UIUtils.createCheckbox(optionsGroup, "Quick", false);
            this.quickCheck.addSelectionListener(this.SQL_CHANGE_LISTENER);
            this.extendedCheck = UIUtils.createCheckbox(optionsGroup, "Extended", false);
            this.extendedCheck.addSelectionListener(this.SQL_CHANGE_LISTENER);
            this.frmCheck = UIUtils.createCheckbox(optionsGroup, "Use FRM", false);
            this.frmCheck.addSelectionListener(this.SQL_CHANGE_LISTENER);

            createObjectsSelector(parent);
        }
    }
}
