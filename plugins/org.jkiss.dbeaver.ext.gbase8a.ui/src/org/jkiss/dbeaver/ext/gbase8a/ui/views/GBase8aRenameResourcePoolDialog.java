package org.jkiss.dbeaver.ext.gbase8a.ui.views;

import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ui.UIUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class GBase8aRenameResourcePoolDialog extends Dialog {
    private Label label_name;
    private Text textName;
    private final String vcName;
    private final String name;
    private String newName;

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(GBase8aMessages.dialog_rename_resource_pool_title + this.vcName + "." + this.name);
    }

    public GBase8aRenameResourcePoolDialog(Shell parentShell, String vcName, String name) {
        super(parentShell);
        this.vcName = vcName;
        this.name = name;
    }

    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(3, false));

        this.label_name = new Label(container, 0);
        this.label_name.setLayoutData(new GridData(4, 16777216, false,
                false, 1, 1));
        this.label_name.setText(GBase8aMessages.dialog_create_resource_pool_name);

        this.textName = new Text(container, 2048);
        GridData gd = new GridData(4, 16777216, true, false, 1, 1);
        gd.widthHint = 200;
        this.textName.setLayoutData(gd);
        this.textName.setText(this.name);
        return container;
    }


    protected void buttonPressed(int buttonId) {
        if (buttonId == 0) {
            this.newName = this.textName.getText().trim();
            if (this.newName.isEmpty()) {
                UIUtils.showMessageBox(null, GBase8aMessages.message_information_title, GBase8aMessages.dialog_rename_resource_pool_error, 2);
                return;
            }
            if (this.newName.equalsIgnoreCase(this.name)) {
                UIUtils.showMessageBox(null, GBase8aMessages.message_information_title, GBase8aMessages.dialog_rename_resource_pool_error2, 2);
                return;
            }
        }
        super.buttonPressed(buttonId);
    }

    public String getNewName() {
        return this.newName;
    }
}