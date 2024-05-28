package org.jkiss.dbeaver.ext.gbase8a.ui.views;

import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ui.UIUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class GBase8aModifyTableLimitSizeDialog extends Dialog {
    private Label label_size;
    private Text textSize;
    private final String oldSize;
    private String tableSize;
    private final String databaseName;
    private final String tableName;
    private Label label;

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(GBase8aMessages.dialog_modify_table_limit_size_title + this.databaseName + "." + this.tableName);
    }

    public GBase8aModifyTableLimitSizeDialog(Shell parentShell, String oldSize, String databaseName, String tableName) {
        super(parentShell);
        this.oldSize = oldSize;
        this.databaseName = databaseName;
        this.tableName = tableName;
    }

    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(3, false));
        this.label_size = new Label(container, 0);
        this.label_size.setLayoutData(new GridData(4, 16777216, false,
                false, 1, 1));
        this.label_size.setText(GBase8aMessages.dialog_create_table_limit_size);

        this.textSize = new Text(container, 2048);
        GridData gd = new GridData(4, 16777216, true, false, 1, 1);
        gd.widthHint = 200;
        this.textSize.setLayoutData(gd);
        this.textSize.setText(this.oldSize);

        Label label_unit = new Label(container, 0);
        label_unit.setLayoutData(new GridData(4, 16777216, false,
                false, 1, 1));
        label_unit.setText(GBase8aMessages.dialog_create_table_limit_size_unit);

        this.label = new Label(container, 258);
        this.label.setLayoutData(new GridData(4, 16777216, true, false, 3,
                1));
        return container;
    }


    protected void buttonPressed(int buttonId) {
        if (buttonId == 0) {
            this.tableSize = this.textSize.getText().trim().toUpperCase();

            if (this.tableSize.equals(this.oldSize.toUpperCase())) {
                UIUtils.showMessageBox(null, GBase8aMessages.message_information_title, GBase8aMessages.dialog_modify_table_limit_size_no_change, 2);
                super.buttonPressed(buttonId);
            }
            if (!checkTableSize(this.tableSize)) {
                UIUtils.showMessageBox(null, GBase8aMessages.message_information_title, GBase8aMessages.dialog_modify_table_limit_size_error, 2);
                this.textSize.selectAll();
                this.textSize.setFocus();
                return;
            }
        }
        super.buttonPressed(buttonId);
    }


    private boolean checkTableSize(String tableSize) {
        Pattern p = Pattern.compile("\\d+[KMGT]{1}");
        if (tableSize == null || tableSize.isEmpty() || tableSize.equals("0")) {
            tableSize = "0";
            return true;
        }
        Matcher m = p.matcher(tableSize);
        return m.matches();
    }

    public String getTableSize() {
        return this.tableSize;
    }
}