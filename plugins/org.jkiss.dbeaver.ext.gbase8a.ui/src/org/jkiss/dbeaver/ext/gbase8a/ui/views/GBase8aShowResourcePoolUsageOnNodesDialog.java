package org.jkiss.dbeaver.ext.gbase8a.ui.views;

import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aResourcePoolUsage;
import org.jkiss.dbeaver.ui.dialogs.BaseDialog;

import java.util.List;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


public class GBase8aShowResourcePoolUsageOnNodesDialog extends BaseDialog {
    private final GBase8aDataSource dataSource;
    private Table table;
    private TableViewer tableViewer;
    private List<GBase8aResourcePoolUsage> list_usage;
    private final boolean isCoordinator;

    public GBase8aShowResourcePoolUsageOnNodesDialog(Shell parentShell, GBase8aDataSource dataSource, boolean isCoordinator) {
        super(parentShell, GBase8aMessages.dialog_show_resource_pool_usage_on_nodes_title, null);
        this.dataSource = dataSource;
        this.isCoordinator = isCoordinator;
        if (isCoordinator) {
            setTitle(GBase8aMessages.dialog_show_resource_pool_usage_on_coordinators_title);
        }
    }


    protected Composite createDialogArea(Composite parent) {
        Composite composite = super.createDialogArea(parent);

        Composite group = new Composite(composite, 0);
        group.setLayout(new GridLayout(2, false));

        this.tableViewer = new TableViewer(composite, 68352);
        this.table = this.tableViewer.getTable();
        this.table.setLinesVisible(true);
        this.table.setHeaderVisible(true);
        GridData gd_table = new GridData(4, 16777216, true, true, 1, 1);
        gd_table.heightHint = 559;
        gd_table.widthHint = 980;
        this.table.setLayoutData(gd_table);
        this.table.setSize(1010, 85);

        TableColumn tbNodeName = new TableColumn(this.table, 0);
        tbNodeName.setWidth(100);
        tbNodeName.setText(GBase8aMessages.dialog_show_resource_pool_usage_node_name);

        TableColumn tbVcName = new TableColumn(this.table, 0);
        tbVcName.setWidth(100);
        tbVcName.setText(GBase8aMessages.dialog_show_resource_pool_usage_vc_name);

        TableColumn tbResourcePoolName = new TableColumn(this.table, 0);
        tbResourcePoolName.setWidth(100);
        tbResourcePoolName.setText(GBase8aMessages.dialog_show_resource_pool_usage_name);

        TableColumn tbPriority = new TableColumn(this.table, 0);
        tbPriority.setWidth(100);
        tbPriority.setText(GBase8aMessages.dialog_show_resource_pool_usage_priority);

        TableColumn tbRunning_tasks = new TableColumn(this.table, 0);
        tbRunning_tasks.setWidth(100);
        tbRunning_tasks.setText(GBase8aMessages.dialog_show_resource_pool_usage_running_tasks);

        TableColumn tbWaiting_tasks = new TableColumn(this.table, 0);
        tbWaiting_tasks.setWidth(100);
        tbWaiting_tasks.setText(GBase8aMessages.dialog_show_resource_pool_usage_waiting_tasks);

        TableColumn tbCpu_usage = new TableColumn(this.table, 0);
        tbCpu_usage.setWidth(100);
        tbCpu_usage.setText(GBase8aMessages.dialog_show_resource_pool_usage_cpu_usage);

        TableColumn tbMem_usage = new TableColumn(this.table, 0);
        tbMem_usage.setWidth(100);
        tbMem_usage.setText(GBase8aMessages.dialog_show_resource_pool_usage_mem_usage);

        TableColumn tbDisk_usage = new TableColumn(this.table, 0);
        tbDisk_usage.setWidth(100);
        tbDisk_usage.setText(GBase8aMessages.dialog_show_resource_pool_usage_disk_usage);

        TableColumn tbDisk_writeio = new TableColumn(this.table, 0);
        tbDisk_writeio.setWidth(100);
        tbDisk_writeio.setText(GBase8aMessages.dialog_show_resource_pool_usage_disk_writeio);

        TableColumn tbDisk_readio = new TableColumn(this.table, 0);
        tbDisk_readio.setWidth(100);
        tbDisk_readio.setText(GBase8aMessages.dialog_show_resource_pool_usage_disk_readio);

        if (this.isCoordinator) {
            tbCpu_usage.dispose();
            tbMem_usage.dispose();
            tbDisk_usage.dispose();
            tbDisk_writeio.dispose();
            tbDisk_readio.dispose();
        }

        this.tableViewer.setLabelProvider(new TableViewerLabelProvider());
        this.tableViewer.setContentProvider(new TableViewerContentProvider());
        this.tableViewer.setInput(this.list_usage);
        return composite;
    }


    protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
        return null;
    }


    protected void initializeBounds() {
        Composite comp = (Composite) getButtonBar();
        super.createButton(comp, 1, GBase8aMessages.dialog_show_resource_pool_usage_close, false);
        super.initializeBounds();
    }


    static class TableViewerContentProvider implements IStructuredContentProvider {
        public void dispose() {
        }


        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }


        public Object[] getElements(Object inputElement) {
            return ((List) inputElement).toArray();
        }
    }


    public List<GBase8aResourcePoolUsage> getList_usage() {
        return this.list_usage;
    }

    public void setList_usage(List<GBase8aResourcePoolUsage> list_usage) {
        this.list_usage = list_usage;
    }

    static class TableViewerLabelProvider implements ITableLabelProvider {
        public String getColumnText(Object element, int columnIndex) {
            GBase8aResourcePoolUsage usage = (GBase8aResourcePoolUsage) element;
            if (columnIndex == 0) {
                return usage.getNodeName();
            }
            if (columnIndex == 1) {
                return usage.getVcName();
            }
            if (columnIndex == 2) {
                return usage.getResourcePoolname();
            }
            if (columnIndex == 3) {
                return usage.getPriority();
            }
            if (columnIndex == 4) {
                return usage.getRunning_tasks();
            }
            if (columnIndex == 5) {
                return usage.getWaiting_tasks();
            }
            if (columnIndex == 6) {
                return usage.getCpu_usage();
            }
            if (columnIndex == 7) {
                return usage.getMem_usage();
            }
            if (columnIndex == 8) {
                return usage.getDisk_usage();
            }
            if (columnIndex == 9) {
                return usage.getDisk_writeio();
            }
            if (columnIndex == 10) {
                return usage.getDisk_readio();
            }
            return null;
        }


        public void addListener(ILabelProviderListener listener) {
        }


        public void dispose() {
        }


        public boolean isLabelProperty(Object element, String property) {
            return false;
        }


        public void removeListener(ILabelProviderListener listener) {
        }


        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }
}