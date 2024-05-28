package org.jkiss.dbeaver.ext.gbase8a.ui.views;

import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableSpace;
import org.jkiss.dbeaver.ui.dialogs.BaseDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class GBase8aCreateTableSpaceDialog extends BaseDialog {
    private final GBase8aTableSpace gbase8aTableSpace;
    private String name = "";
    private String path = "";

    private String sql;

    private boolean isNew = false;
    private Text nameText;
    private Text pathText;
    private Combo comboUnit1;
    private Combo comboUnit2;
    private Text segSizeText;
    private Text maxSizeText;
    private final String[] units = new String[]{"K", "M", "G"};

    public GBase8aCreateTableSpaceDialog(Shell parentShell, GBase8aTableSpace gbase8aTableSpace, boolean isNew) {
        super(parentShell, GBase8aMessages.dialog_create_tablespace_create_name, null);
        this.gbase8aTableSpace = gbase8aTableSpace;
        this.isNew = isNew;
        if (!isNew) {
            setTitle(GBase8aMessages.dialog_create_tablespace_modify_name);
        }
    }


    protected Composite createDialogArea(Composite parent) {
        Composite composite = super.createDialogArea(parent);

        Composite group = new Composite(composite, 0);
        group.setLayout(new GridLayout(3, false));

        Label lblName = new Label(group, 0);
        GridData gd_lblName = new GridData(4, 16777216, false, false, 1, 1);
        lblName.setLayoutData(gd_lblName);
        lblName.setText(GBase8aMessages.dialog_create_tablespace_tablespace_name);

        this.nameText = new Text(group, 2048);
        GridData gd_textName = new GridData(4, 16777216, true, false, 2, 1);
        gd_textName.widthHint = 200;
        this.nameText.setLayoutData(gd_textName);

        Label lblPath = new Label(group, 0);
        GridData gd_lblPath = new GridData(4, 16777216, false, false, 1, 1);
        lblPath.setLayoutData(gd_lblPath);
        lblPath.setText(GBase8aMessages.dialog_create_tablespace_path);

        this.pathText = new Text(group, 2048);
        GridData gd_textTableSpace = new GridData(4, 16777216, true, false, 2, 1);
        this.pathText.setLayoutData(gd_textTableSpace);

        Label lblSegSize = new Label(group, 0);
        GridData gd_lblSegSize = new GridData(4, 16777216, false, false, 1, 1);
        lblSegSize.setLayoutData(gd_lblSegSize);
        lblSegSize.setText(GBase8aMessages.dialog_create_tablespace_segsize);

        this.segSizeText = new Text(group, 2048);
        this.segSizeText.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));

        this.comboUnit1 = new Combo(group, 8);
        this.comboUnit1.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        this.comboUnit1.setItems(this.units);
        this.comboUnit1.select(0);

        Label lblMaxSize = new Label(group, 0);
        GridData gd_lblMaxSize = new GridData(4, 16777216, false, false, 1, 1);
        lblMaxSize.setLayoutData(gd_lblMaxSize);
        lblMaxSize.setText(GBase8aMessages.dialog_create_tablespace_maxsize);

        this.maxSizeText = new Text(group, 2048);
        this.maxSizeText.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));

        this.comboUnit2 = new Combo(group, 8);
        this.comboUnit2.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        this.comboUnit2.setItems(this.units);
        this.comboUnit2.select(0);

        init();
        return composite;
    }

    private void init() {
        if (!this.isNew) {
            this.nameText.setEnabled(false);
            this.pathText.setEnabled(false);
            this.segSizeText.setEnabled(false);
            this.comboUnit1.setEnabled(false);
            this.maxSizeText.setEnabled(true);
            this.comboUnit2.setEnabled(true);

            this.nameText.setText(this.gbase8aTableSpace.getName());
            this.pathText.setText(this.gbase8aTableSpace.getPath());
            String segSize = this.gbase8aTableSpace.getSegsize();
            if (!"default".equalsIgnoreCase(this.gbase8aTableSpace.getSegsize())) {
                String unit = segSize.substring(segSize.length() - 1);
                String segNum = segSize.substring(0, segSize.length() - 1);
                this.segSizeText.setText(segNum);
                this.comboUnit1.setText(unit.toUpperCase());
            }
            String maxSize = this.gbase8aTableSpace.getMaxsize();
            if (!"default".equalsIgnoreCase(this.gbase8aTableSpace.getMaxsize())) {
                String unit = maxSize.substring(maxSize.length() - 1);
                String maxNum = maxSize.substring(0, maxSize.length() - 1);
                this.maxSizeText.setText(maxNum);
                this.comboUnit2.setText(unit.toUpperCase());
            }
        }
    }

    public String getName() {
        return this.name;
    }

    public String getSQL() {
        return this.sql;
    }


    protected void buttonPressed(int buttonId) {
        if (buttonId == 0) {
            this.name = this.nameText.getText().trim();
            this.path = this.pathText.getText().trim();
            if (this.name.isEmpty()) {
                MessageDialog.openError(getShell(), GBase8aMessages.message_error_title, GBase8aMessages.dialog_create_tablespace_error1);
                return;
            }
            if (this.path.isEmpty()) {
                MessageDialog.openError(getShell(), GBase8aMessages.message_error_title, GBase8aMessages.dialog_create_tablespace_error2);
                return;
            }
            if (this.isNew) {
                this.sql = "create tablespace `" + this.gbase8aTableSpace.getCatalog().getName() + "`.`" + this.name + "`";
                if (!this.pathText.getText().trim().isEmpty()) {
                    this.sql = this.sql + " DATADIR '" + this.pathText.getText().trim() + "'";
                    if (!this.segSizeText.getText().trim().isEmpty()) {
                        this.sql = this.sql + " SEGSIZE " + this.segSizeText.getText().trim() + this.comboUnit1.getText();
                    }
                    if (!this.maxSizeText.getText().trim().isEmpty()) {
                        this.sql = this.sql + " MAXSIZE " + this.maxSizeText.getText().trim() + this.comboUnit2.getText();
                    }
                }
            } else {
                if (this.maxSizeText.getText().trim().isEmpty()) {
                    MessageDialog.openError(getShell(), GBase8aMessages.message_error_title, GBase8aMessages.dialog_create_tablespace_error3);
                    return;
                }
                this.sql = "alter tablespace `" + this.gbase8aTableSpace.getCatalog().getName() + "`.`" + this.gbase8aTableSpace.getName() + "`";
                this.sql = this.sql + " MAXSIZE " + this.maxSizeText.getText().trim() + this.comboUnit2.getText();
            }
        }
        super.buttonPressed(buttonId);
    }
}