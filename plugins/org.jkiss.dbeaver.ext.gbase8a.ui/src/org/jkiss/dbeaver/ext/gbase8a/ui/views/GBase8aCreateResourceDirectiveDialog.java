package org.jkiss.dbeaver.ext.gbase8a.ui.views;

import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.ui.dialogs.BaseDialog;

import java.util.ArrayList;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class GBase8aCreateResourceDirectiveDialog extends BaseDialog {

    Log log = Log.getLog(GBase8aCreateResourceDirectiveDialog.class);

    private final GBase8aDataSource dataSource;
    private String name;
    private String comment;
    private final String vcName;
    private String planName;
    private String groupName;
    private String poolName;
    private Combo planCombo;
    private Combo groupCombo;
    private Combo poolCombo;
    private String[] plans;
    private String[] groups;
    private String[] pools;

    public GBase8aCreateResourceDirectiveDialog(Shell parentShell, GBase8aDataSource dataSource, String vcName) {
        super(parentShell, GBase8aMessages.dialog_create_resource_directive_dialog_name, null);
        this.dataSource = dataSource;
        this.vcName = vcName;
    }


    protected Composite createDialogArea(Composite parent) {
        init();
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
        nameLabel.setText(GBase8aMessages.dialog_create_resource_directive_name);

        final Text nameText = new Text(group, 2048);
        GridData gd_nameText = new GridData(16384, 16777216, false, false, 1, 1);
        gd_nameText.widthHint = 160;
        nameText.setLayoutData(gd_nameText);
        nameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                GBase8aCreateResourceDirectiveDialog.this.name = nameText.getText();
                GBase8aCreateResourceDirectiveDialog.this.getButton(0).setEnabled(!GBase8aCreateResourceDirectiveDialog.this.name.isEmpty());
            }
        });

        Label plannameLabel = new Label(group, 0);
        plannameLabel.setText(GBase8aMessages.dialog_create_resource_directive_plan_name);

        this.planCombo = new Combo(group, 2056);
        this.planCombo.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        this.planCombo.setItems(this.plans);
        this.planCombo.select(0);
        this.planCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                GBase8aCreateResourceDirectiveDialog.this.planName = GBase8aCreateResourceDirectiveDialog.this.planCombo.getText();
            }
        });

        Label groupnameLabel = new Label(group, 0);
        groupnameLabel.setText(GBase8aMessages.dialog_create_resource_directive_group_name);

        this.groupCombo = new Combo(group, 2056);
        this.groupCombo.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        this.groupCombo.setItems(this.groups);
        this.groupCombo.select(0);
        this.groupCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                GBase8aCreateResourceDirectiveDialog.this.groupName = GBase8aCreateResourceDirectiveDialog.this.groupCombo.getText();
            }
        });

        Label poolnameLabel = new Label(group, 0);
        poolnameLabel.setText(GBase8aMessages.dialog_create_resource_directive_pool_name);

        this.poolCombo = new Combo(group, 2056);
        this.poolCombo.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        this.poolCombo.setItems(this.pools);
        this.poolCombo.select(0);
        this.poolCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                GBase8aCreateResourceDirectiveDialog.this.poolName = GBase8aCreateResourceDirectiveDialog.this.poolCombo.getText();
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
                GBase8aCreateResourceDirectiveDialog.this.comment = commentText.getText();
            }
        });
        this.planName = this.planCombo.getText();
        this.poolName = this.poolCombo.getText();
        this.groupName = this.groupCombo.getText();
        return composite;
    }

    protected Point getInitialSize() {
        return new Point(480, 600);
    }

    public String getName() {
        return this.name;
    }

    public String getComment() {
        return this.comment;
    }


    public String getPlanName() {
        return this.planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPoolName() {
        return this.poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }


    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        getButton(0).setEnabled(false);
    }

    private void init() {
        ArrayList<String> planList = new ArrayList<>();
        ArrayList<String> groupList = new ArrayList<>();
        ArrayList<String> poolList = new ArrayList<>();
        String sql = "";
        try (JDBCSession dbsession = DBUtils.openMetaSession(this.dataSource.getMonitor(), this.dataSource, "queryResourceDirective");
             JDBCStatement stmt = dbsession.createStatement()) {
            if (this.dataSource.isVCCluster()) {
                sql = "select resource_plan_name from gbase.resource_plan t1,information_schema.vc t2 where t1.vc_id=t2.id and t2.name='" + this.vcName + "'";
            } else {
                sql = "select resource_plan_name from gbase.resource_plan ";
            }
            try (JDBCResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    planList.add(rs.getString(1));
                }
            }
            if (this.dataSource.isVCCluster()) {
                sql = "select resource_pool_name from gbase.resource_pool t1,information_schema.vc t2 where t1.vc_id=t2.id and t2.name='" + this.vcName + "'";
            } else {
                sql = "select resource_pool_name from gbase.resource_pool";
            }
            try (JDBCResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    poolList.add(rs.getString(1));
                }
            }
            if (this.dataSource.isVCCluster()) {
                sql = "select consumer_group_name from gbase.consumer_group t1,information_schema.vc t2 where t1.vc_id=t2.id and t2.name='" + this.vcName + "'";
            } else {
                sql = "select consumer_group_name from gbase.consumer_group";
            }
            try (JDBCResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    groupList.add(rs.getString(1));
                }
            }
            this.plans = new String[planList.size()];
            planList.toArray(this.plans);
            this.groups = new String[groupList.size()];
            groupList.toArray(this.groups);
            this.pools = new String[poolList.size()];
            poolList.toArray(this.pools);
        } catch (Exception e) {
            log.error(e);
        }
    }
}