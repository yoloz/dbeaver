package org.jkiss.dbeaver.ext.gbase8a.ui.views;

import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ui.dialogs.BaseDialog;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class GBase8aCreateResourcePlanDialog extends BaseDialog {
    private final GBase8aDataSource dataSource;
    private String name;
    private String comment;
    private String vcName;

    public GBase8aCreateResourcePlanDialog(Shell parentShell, GBase8aDataSource dataSource, String vcName) {
        super(parentShell, GBase8aMessages.dialog_create_resource_plan_dialog_name, null);
        this.dataSource = dataSource;
        this.vcName = vcName;
    }

    protected Composite createDialogArea(Composite parent) {
        Composite composite = super.createDialogArea(parent);

        Composite group = new Composite(composite, 0);
        group.setLayout(new GridLayout(2, false));
        Label vcNameLabel = new Label(group, 0);
        vcNameLabel.setText(GBase8aMessages.dialog_connection_vc_name);

        Text vcNameText = new Text(group, 0);
        GridData gd_vcNameText = new GridData(16384, 16777216, false, false, 1, 1);
        gd_vcNameText.widthHint = 160;
        vcNameText.setLayoutData(gd_vcNameText);
        vcNameText.setText(this.vcName);
        vcNameText.setEnabled(false);

        Label nameLabel = new Label(group, 0);
        nameLabel.setText(GBase8aMessages.dialog_create_Consumer_Group_name);

        final Text nameText = new Text(group, 2048);
        GridData gd_nameText = new GridData(16384, 16777216, false, false, 1, 1);
        gd_nameText.widthHint = 160;
        nameText.setLayoutData(gd_nameText);
        nameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                GBase8aCreateResourcePlanDialog.this.name = nameText.getText();
                GBase8aCreateResourcePlanDialog.this.getButton(0).setEnabled(!GBase8aCreateResourcePlanDialog.this.name.isEmpty());
            }
        });

        Label commentLabel = new Label(group, 0);
        commentLabel.setText(GBase8aMessages.dialog_create_Consumer_Group_comment);

        final Text commentText = new Text(group, 2048);
        GridData gd_commentText = new GridData(16384, 16777216, false, false, 1, 1);
        gd_commentText.widthHint = 160;
        commentText.setLayoutData(gd_commentText);
        commentText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                GBase8aCreateResourcePlanDialog.this.comment = commentText.getText();
            }
        });
        return composite;
    }

    protected Point getInitialSize() {
        return new Point(280, 200);
    }

    public String getName() {
        return this.name;
    }

    public String getComment() {
        return this.comment;
    }

    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        getButton(0).setEnabled(false);
    }
}