package org.jkiss.dbeaver.ext.gbase8a.ui.views;

import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aVC;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.ui.dialogs.BaseDialog;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class GBase8aResourceConfigManagerDialog extends BaseDialog {
    private final GBase8aDataSource dataSource;
    private String name;
    private String comment;
    private final GBase8aVC gbase8aVC;
    private String monit_record_interval_old_value;
    private String monit_record_old_value;
    private String monit_record_interval_value;
    private String monit_record_value;
    private Text recordIntervalText;
    private Text monitRecordText;

    public GBase8aResourceConfigManagerDialog(Shell parentShell, GBase8aDataSource dataSource, GBase8aVC gbase8aVC) {
        super(parentShell, GBase8aMessages.dialog_resource_config_manager_dialog_name, null);
        this.dataSource = dataSource;
        this.gbase8aVC = gbase8aVC;
    }

    protected Composite createDialogArea(Composite parent) {
        Composite composite = super.createDialogArea(parent);

        Composite group = new Composite(composite, 0);
        group.setLayout(new GridLayout(2, false));

        Label keyLabel = new Label(group, 0);
        keyLabel.setText(GBase8aMessages.dialog_resource_config_manager_dialog_config);

        Label valueLabel = new Label(group, 0);
        GridData gd_vcNameText = new GridData(16384, 16777216, false, false, 1, 1);
        gd_vcNameText.widthHint = 160;
        valueLabel.setLayoutData(gd_vcNameText);
        valueLabel.setText(GBase8aMessages.dialog_resource_config_manager_dialog_value);

        Label nameLabel = new Label(group, 0);
        nameLabel.setText("gbase_resource_monit_record_interval");

        this.recordIntervalText = new Text(group, 2048);
        GridData gd_nameText = new GridData(16384, 16777216, false, false, 1, 1);
        gd_nameText.widthHint = 160;
        this.recordIntervalText.setLayoutData(gd_nameText);

        Label commentLabel = new Label(group, 0);
        commentLabel.setText("gbase_resource_monit_record");

        this.monitRecordText = new Text(group, 2048);
        GridData gd_commentText = new GridData(16384, 16777216, false, false, 1, 1);
        gd_commentText.widthHint = 160;
        this.monitRecordText.setLayoutData(gd_commentText);

        initData();

        return composite;
    }


    private void initData() {
        String sql = "select * from gbase.resource_config ";
        if (this.dataSource.isVCCluster()) {
            sql = sql + " where vc_id='" + this.gbase8aVC.getId() + "'";
        }
        try(JDBCSession dbsession = DBUtils.openMetaSession(this.dataSource.getMonitor(), this.dataSource, "modify consumer group");
            JDBCStatement dbStat = dbsession.createStatement();
            JDBCResultSet resultSet = dbStat.executeQuery(sql)) {
            while (resultSet.next()) {
                String configName = resultSet.getString("config_name");
                if (configName != null && configName.equals("gbase_resource_monit_record_interval")) {
                    this.monit_record_interval_old_value = resultSet.getString("config_int_value");
                }
                if (configName != null && configName.equals("gbase_resource_monit_record")) {
                    this.monit_record_old_value = resultSet.getString("config_int_value");
                }
            }
        } catch (Exception e) {
            MessageDialog.openError(Display.getCurrent().getActiveShell(),
                    GBase8aMessages.dialog_modify_Consumer_dialog_name, GBase8aMessages.dialog_modify_Consumer_dialog_failed + e.getMessage());
        }
        if (this.monit_record_interval_old_value != null) {
            this.recordIntervalText.setText(this.monit_record_interval_old_value);
        }
        if (this.monit_record_old_value != null) {
            this.monitRecordText.setText(this.monit_record_old_value);
        }
    }

    protected void buttonPressed(int buttonId) {
        if (buttonId == 0) {
            this.monit_record_interval_value = this.recordIntervalText.getText().trim();
            this.monit_record_value = this.monitRecordText.getText().trim();
            try (JDBCSession dbsession = DBUtils.openMetaSession(this.dataSource.getMonitor(), this.dataSource, "modify consumer group");
            JDBCStatement stmt = dbsession.createStatement()){
                if (this.monit_record_interval_value != null && !this.monit_record_interval_value.equals(this.monit_record_interval_old_value)) {
                    String recordIntervalSql = "alter resource config ";
                    if (this.dataSource.isVCCluster()) {
                        recordIntervalSql = recordIntervalSql + this.gbase8aVC.getName() + ".";
                    }
                    recordIntervalSql = recordIntervalSql + "gbase_resource_monit_record_interval=" + this.monit_record_interval_value;
                    stmt.execute(recordIntervalSql);
                }
                if (this.monit_record_value != null && !this.monit_record_value.equals(this.monit_record_old_value)) {
                    String recordSql = "alter resource config ";
                    if (this.dataSource.isVCCluster()) {
                        recordSql = recordSql + this.gbase8aVC.getName() + ".";
                    }
                    recordSql = recordSql + "gbase_resource_monit_record=" + this.monit_record_value;
                    stmt.execute(recordSql);
                }
                MessageDialog.openInformation(Display.getCurrent()
                        .getActiveShell(), GBase8aMessages.dialog_resource_config_manager_dialog_modify, GBase8aMessages.dialog_resource_config_manager_dialog_success);
                close();
            } catch (Exception e) {
                MessageDialog.openError(Display.getCurrent().getActiveShell(),
                        GBase8aMessages.dialog_resource_config_manager_dialog_modify, GBase8aMessages.dialog_resource_config_manager_dialog_failed + e.getMessage());
            }
        } else if (buttonId == 1) {
            close();
        }
    }

    protected Point getInitialSize() {
        return new Point(450, 200);
    }

    public String getName() {
        return this.name;
    }

    public String getComment() {
        return this.comment;
    }


    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
    }
}