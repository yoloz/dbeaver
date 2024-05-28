package org.jkiss.dbeaver.ext.gbase8a.ui.views;

import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCharset;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCollation;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.dialogs.BaseDialog;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class GBase8aCreateDatabaseDialog extends BaseDialog {
    public static final String DEFAULT_CHARSET_NAME = "utf8";
    private final GBase8aDataSource dataSource;
    private String name;
    private GBase8aCharset charset;
    private GBase8aCollation collation;
    private String sql;
    private Text nameText;
    private Text tablespaceText;
    private Combo comboUnit1;
    private Combo comboUnit2;
    private Text segSizeText;
    private Text maxSizeText;
    private String[] units = new String[]{"K", "M", "G"};

    public GBase8aCreateDatabaseDialog(Shell parentShell, GBase8aDataSource dataSource) {
        super(parentShell, GBase8aMessages.dialog_create_Database_name, null);
        this.dataSource = dataSource;
    }

    protected Composite createDialogArea(Composite parent) {
        init();
        Composite composite = super.createDialogArea(parent);
        Composite group = new Composite(composite, 0);
        group.setLayout(new GridLayout(2, false));
        this.nameText = UIUtils.createLabelText(group, GBase8aMessages.dialog_create_Database_db_name, "");
        this.nameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                GBase8aCreateDatabaseDialog.this.name = GBase8aCreateDatabaseDialog.this.nameText.getText();
                GBase8aCreateDatabaseDialog.this.getButton(0).setEnabled(!GBase8aCreateDatabaseDialog.this.name.isEmpty());
            }
        });
        return composite;
    }

    public String getName() {
        return this.name;
    }

    public String getSQL() {
        return this.sql;
    }

    private void init() {
        if (!this.dataSource.isSupportTablespace()) {
            this.tablespaceText.setEnabled(false);
        }

        this.segSizeText.setEnabled(false);
        this.comboUnit1.setEnabled(false);
        this.maxSizeText.setEnabled(false);
        this.comboUnit2.setEnabled(false);
    }

    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        this.getButton(0).setEnabled(false);
    }

    protected void buttonPressed(int buttonId) {
        if (buttonId == 0) {
            this.sql = "create database `" + this.name + "`";
        }

        super.buttonPressed(buttonId);
    }
}