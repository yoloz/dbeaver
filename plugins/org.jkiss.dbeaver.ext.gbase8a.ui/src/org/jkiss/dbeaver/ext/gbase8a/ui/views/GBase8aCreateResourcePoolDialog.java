package org.jkiss.dbeaver.ext.gbase8a.ui.views;

import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aResourcePool;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.dialogs.BaseDialog;

import java.util.ArrayList;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;


public class GBase8aCreateResourcePoolDialog extends BaseDialog {
    Log log = Log.getLog(GBase8aCreateResourcePoolDialog.class);

    private final GBase8aDataSource dataSource;
    private final GBase8aResourcePool resourcePool;
    private final String vcName;
    private String name;
    private String baseOn;
    private String type;
    private String priorityString;
    private String cpu_percentString;
    private String max_memoryString;
    private String max_temp_diskspaceString;
    private String max_disk_spaceString;
    private String max_disk_writeioString;
    private String max_disk_readioString;
    private String max_activetaskString;
    private String task_max_parallel_degreeString;
    private String task_waiting_timeoutString;
    private String task_running_timeoutString;
    private boolean isNew = false;
    private long priority = 0L;
    private long cpu_percent = 0L;
    private long max_memory = 0L;
    private long max_temp_diskspace = 0L;
    private long max_disk_space = 0L;
    private long max_disk_writeio = 0L;
    private long max_disk_readio = 0L;
    private long max_activetask = 0L;
    private long task_max_parallel_degree = 0L;
    private long task_waiting_timeout = 0L;
    private long task_running_timeout = 0L;

    private String[] pools;
    private Text vcNameText;
    private Text nameText;
    private Combo baseOnCombo;
    private Combo typeCombo;
    private Spinner prioritySpinner;
    private Spinner cpu_percentSpinner;
    private Spinner max_memorySpinner;
    private Spinner max_temp_diskspaceSpinner;
    private Spinner max_disk_spaceSpinner;
    private Spinner max_disk_writeioSpinner;
    private Spinner max_disk_readioSpinner;
    private Spinner max_activetaskSpinner;
    private Spinner task_max_parallel_degreeSpinner;
    private Spinner task_waiting_timeoutSpinner;
    private Spinner task_running_timeoutSpinner;

    public GBase8aCreateResourcePoolDialog(Shell parentShell, GBase8aDataSource dataSource, GBase8aResourcePool resourcePool, boolean isNew) {
        super(parentShell, GBase8aMessages.dialog_create_resource_pool_title, null);
        this.dataSource = dataSource;
        this.resourcePool = resourcePool;
        this.vcName = resourcePool.getVcName();
        this.isNew = isNew;
        if (!isNew) {
            setTitle(GBase8aMessages.dialog_modify_resource_pool_title);
        }
    }

    protected Composite createDialogArea(Composite parent) {
        Composite composite = super.createDialogArea(parent);

        Composite group = new Composite(composite, 0);
        group.setLayout(new GridLayout(2, false));

        this.vcNameText = UIUtils.createLabelText(group, GBase8aMessages.dialog_create_resource_pool_vcname, "");
        this.vcNameText.setEnabled(false);
        this.vcNameText.setText(this.vcName);

        this.nameText = UIUtils.createLabelText(group, GBase8aMessages.dialog_create_resource_pool_name, "");

        this.baseOnCombo = UIUtils.createLabelCombo(group, GBase8aMessages.dialog_create_resource_pool_base_on, 8);

        this.typeCombo = UIUtils.createLabelCombo(group, GBase8aMessages.dialog_create_resource_pool_type, 8);
        this.typeCombo.add("static");
        this.typeCombo.add("dynamic");

        this.prioritySpinner = UIUtils.createLabelSpinner(group, GBase8aMessages.dialog_create_resource_pool_priority, 1, 1, 8);

        this.cpu_percentSpinner = UIUtils.createLabelSpinner(group, GBase8aMessages.dialog_create_resource_pool_cpu_percent, 100, 1, 100);

        this.max_memorySpinner = UIUtils.createLabelSpinner(group, GBase8aMessages.dialog_create_resource_pool_max_memory, 0, 0, 2147483647);

        this.max_temp_diskspaceSpinner = UIUtils.createLabelSpinner(group, GBase8aMessages.dialog_create_resource_pool_max_temp_diskspace, 0, 0, 2147483647);

        this.max_disk_spaceSpinner = UIUtils.createLabelSpinner(group, GBase8aMessages.dialog_create_resource_pool_max_disk_space, 0, 0, 2147483647);

        this.max_disk_writeioSpinner = UIUtils.createLabelSpinner(group, GBase8aMessages.dialog_create_resource_pool_max_disk_writeio, 0, 0, 2147483647);

        this.max_disk_readioSpinner = UIUtils.createLabelSpinner(group, GBase8aMessages.dialog_create_resource_pool_max_disk_readio, 0, 0, 2147483647);

        this.max_activetaskSpinner = UIUtils.createLabelSpinner(group, GBase8aMessages.dialog_create_resource_pool_max_activetask, 20, 0, 2147483647);

        this.task_max_parallel_degreeSpinner = UIUtils.createLabelSpinner(group, GBase8aMessages.dialog_create_resource_pool_task_max_parallel_degree, 16, 0, 2147483647);

        this.task_waiting_timeoutSpinner = UIUtils.createLabelSpinner(group, GBase8aMessages.dialog_create_resource_pool_task_waiting_timeout, 2592000, 0, 2147483647);

        this.task_running_timeoutSpinner = UIUtils.createLabelSpinner(group, GBase8aMessages.dialog_create_resource_pool_task_running_timeout, 2592000, 0, 2147483647);

        this.typeCombo.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                if ("dynamic".equals(GBase8aCreateResourcePoolDialog.this.typeCombo.getText())) {
                    GBase8aCreateResourcePoolDialog.this.baseOnCombo.setEnabled(true);
                    GBase8aCreateResourcePoolDialog.this.prioritySpinner.setEnabled(true);
                    GBase8aCreateResourcePoolDialog.this.max_activetaskSpinner.setEnabled(true);
                    GBase8aCreateResourcePoolDialog.this.task_max_parallel_degreeSpinner.setEnabled(true);
                    GBase8aCreateResourcePoolDialog.this.task_waiting_timeoutSpinner.setEnabled(true);
                    GBase8aCreateResourcePoolDialog.this.task_running_timeoutSpinner.setEnabled(true);
                } else {
                    GBase8aCreateResourcePoolDialog.this.baseOnCombo.setEnabled(false);
                    GBase8aCreateResourcePoolDialog.this.baseOnCombo.select(0);
                    GBase8aCreateResourcePoolDialog.this.prioritySpinner.setEnabled(false);
                    GBase8aCreateResourcePoolDialog.this.max_activetaskSpinner.setEnabled(false);
                    GBase8aCreateResourcePoolDialog.this.task_max_parallel_degreeSpinner.setEnabled(false);
                    GBase8aCreateResourcePoolDialog.this.task_waiting_timeoutSpinner.setEnabled(false);
                    GBase8aCreateResourcePoolDialog.this.task_running_timeoutSpinner.setEnabled(false);
                }
            }
        });
        init();
        return composite;
    }

    public String getName() {
        return this.name;
    }

    public String getBaseOn() {
        return this.baseOn;
    }

    public String getType() {
        return this.type;
    }

    public long getPriority() {
        return this.priority;
    }

    public long getCpu_percent() {
        return this.cpu_percent;
    }

    public long getMax_memory() {
        return this.max_memory;
    }

    public long getMax_temp_diskspace() {
        return this.max_temp_diskspace;
    }

    public long getMax_disk_space() {
        return this.max_disk_space;
    }

    public long getMax_disk_writeio() {
        return this.max_disk_writeio;
    }

    public long getMax_disk_readio() {
        return this.max_disk_readio;
    }

    public long getMax_activetask() {
        return this.max_activetask;
    }

    public long getTask_max_parallel_degree() {
        return this.task_max_parallel_degree;
    }

    public long getTask_waiting_timeout() {
        return this.task_waiting_timeout;
    }

    public long getTask_running_timeout() {
        return this.task_running_timeout;
    }

    private void init() {
        this.vcNameText.setEnabled(false);
        this.vcNameText.setText(this.vcName);
        ArrayList<String> poolList = new ArrayList<String>();

        String vcId = this.dataSource.getActiveVC().getId();
        String sql = "";
        if (this.dataSource.isVCCluster() && !"Default".equalsIgnoreCase(this.vcName)) {
            sql = "select resource_pool_name from gbase.resource_pool where resource_pool_type=1 and vc_id = '" + vcId + "'";
        } else {
            sql = "select resource_pool_name from gbase.resource_pool where resource_pool_type=1";
        }
        try (JDBCSession dbsession = DBUtils.openMetaSession(this.dataSource.getMonitor(), this.dataSource, "queryResourceDirective");
             JDBCStatement stmt = dbsession.createStatement();
             JDBCResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                poolList.add(rs.getString(1));
            }
            poolList.add(0, "");
            this.pools = new String[poolList.size()];
            poolList.toArray(this.pools);
        } catch (Exception e) {
            log.error(e);
        }
        this.baseOnCombo.setItems(this.pools);
        if (this.isNew) {
            this.typeCombo.select(0);
            this.baseOnCombo.select(0);
            this.baseOnCombo.setEnabled(false);
        } else {
            this.name = this.resourcePool.getName();
            this.nameText.setText(this.name);
            this.nameText.setEnabled(false);

            this.type = this.resourcePool.getType();
            this.typeCombo.setText(this.type);
            this.typeCombo.setEnabled(false);

            this.baseOn = this.resourcePool.getBaseOn();
            this.baseOnCombo.setText(this.baseOn);
            this.baseOnCombo.setEnabled(false);

            this.priority = this.resourcePool.getPriority();
            this.prioritySpinner.setSelection(Integer.parseInt(Long.toString(this.priority)));

            this.cpu_percent = this.resourcePool.getCpu_percent();
            this.cpu_percentSpinner.setSelection(Integer.parseInt(Long.toString(this.cpu_percent)));

            this.max_memory = this.resourcePool.getMax_memory();
            this.max_memorySpinner.setSelection(Integer.parseInt(Long.toString(this.max_memory)));

            this.max_temp_diskspace = this.resourcePool.getMax_temp_diskspace();
            this.max_temp_diskspaceSpinner.setSelection(Integer.parseInt(Long.toString(this.max_temp_diskspace)));

            this.max_disk_space = this.resourcePool.getMax_disk_space();
            this.max_disk_spaceSpinner.setSelection(Integer.parseInt(Long.toString(this.max_disk_space)));

            this.max_disk_writeio = this.resourcePool.getMax_disk_writeio();
            this.max_disk_writeioSpinner.setSelection(Integer.parseInt(Long.toString(this.max_disk_writeio)));

            this.max_disk_readio = this.resourcePool.getMax_disk_readio();
            this.max_disk_readioSpinner.setSelection(Integer.parseInt(Long.toString(this.max_disk_readio)));

            if ("dynamic".equals(this.type)) {
                this.max_activetask = this.resourcePool.getMax_activetask();
                this.max_activetaskSpinner.setSelection(Integer.parseInt(Long.toString(this.max_activetask)));

                this.task_max_parallel_degree = this.resourcePool.getTask_max_parallel_degree();
                this.task_max_parallel_degreeSpinner.setSelection(Integer.parseInt(Long.toString(this.task_max_parallel_degree)));

                this.task_waiting_timeout = this.resourcePool.getTask_waiting_timeout();
                this.task_waiting_timeoutSpinner.setSelection(Integer.parseInt(Long.toString(this.task_waiting_timeout)));

                this.task_running_timeout = this.resourcePool.getTask_running_timeout();
                this.task_running_timeoutSpinner.setSelection(Integer.parseInt(Long.toString(this.task_running_timeout)));
            }
        }
    }


    protected void buttonPressed(int buttonId) {
        if (buttonId == 0) {
            if (this.isNew) {
                this.name = this.nameText.getText().trim();
                this.type = this.typeCombo.getText().trim();
                this.baseOn = this.baseOnCombo.getText().trim();
                if (this.name.isEmpty() || (
                        "dynamic".equals(this.type) && this.baseOn.isEmpty())) {
                    UIUtils.showMessageBox(null, GBase8aMessages.message_information_title, GBase8aMessages.dialog_create_resource_pool_error, 2);
                    return;
                }
            }
            this.priorityString = this.prioritySpinner.getText().trim();
            this.cpu_percentString = this.cpu_percentSpinner.getText().trim();
            this.max_memoryString = this.max_memorySpinner.getText().trim();
            this.max_temp_diskspaceString = this.max_temp_diskspaceSpinner.getText().trim();
            this.max_disk_spaceString = this.max_disk_spaceSpinner.getText().trim();
            this.max_disk_writeioString = this.max_disk_writeioSpinner.getText().trim();
            this.max_disk_readioString = this.max_disk_readioSpinner.getText().trim();
            this.max_activetaskString = this.max_activetaskSpinner.getText().trim();
            this.task_max_parallel_degreeString = this.task_max_parallel_degreeSpinner.getText().trim();
            this.task_waiting_timeoutString = this.task_waiting_timeoutSpinner.getText().trim();
            this.task_running_timeoutString = this.task_running_timeoutSpinner.getText().trim();
            if (this.cpu_percentString.isEmpty() ||
                    this.max_memoryString.isEmpty() ||
                    this.max_temp_diskspaceString.isEmpty() ||
                    this.max_disk_spaceString.isEmpty() ||
                    this.max_disk_writeioString.isEmpty() ||
                    this.max_disk_readioString.isEmpty()) {
                UIUtils.showMessageBox(null, GBase8aMessages.message_information_title, GBase8aMessages.dialog_create_resource_pool_error, 2);
                return;
            }
            if (!this.priorityString.isEmpty()) {
                this.priority = Integer.parseInt(this.priorityString);
            } else {
                this.priority = 0L;
            }
            this.cpu_percent = Long.parseLong(this.cpu_percentString);
            this.max_memory = Long.parseLong(this.max_memoryString);
            this.max_temp_diskspace = Long.parseLong(this.max_temp_diskspaceString);
            this.max_disk_space = Long.parseLong(this.max_disk_spaceString);
            this.max_disk_writeio = Long.parseLong(this.max_disk_writeioString);
            this.max_disk_readio = Long.parseLong(this.max_disk_readioString);
            if ("dynamic".equals(this.type)) {
                if (!this.max_activetaskString.isEmpty()) {
                    this.max_activetask = Long.parseLong(this.max_activetaskString);
                }
                if (!this.task_max_parallel_degreeString.isEmpty()) {
                    this.task_max_parallel_degree = Long.parseLong(this.task_max_parallel_degreeString);
                }
                if (!this.task_waiting_timeoutString.isEmpty()) {
                    this.task_waiting_timeout = Long.parseLong(this.task_waiting_timeoutString);
                }
                if (!this.task_running_timeoutString.isEmpty()) {
                    this.task_running_timeout = Long.parseLong(this.task_running_timeoutString);
                }
            }
            if (this.cpu_percent == 0L ||
                    this.max_memory == 0L ||
                    this.max_temp_diskspace == 0L ||
                    this.max_disk_space == 0L ||
                    this.max_disk_writeio == 0L ||
                    this.max_disk_readio == 0L) {
                UIUtils.showMessageBox(null, GBase8aMessages.message_information_title, GBase8aMessages.dialog_create_resource_pool_error2, 2);

                return;
            }
        }
        super.buttonPressed(buttonId);
    }
}