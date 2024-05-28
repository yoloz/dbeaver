package org.jkiss.dbeaver.ext.gbase8a.ui.views;

import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aResourcePoolEvent;
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


public class GBase8aShowResourcePoolEventsDialog extends BaseDialog {
    private final GBase8aDataSource dataSource;
    private Table table;
    private TableViewer tableViewer;
    private List<GBase8aResourcePoolEvent> list_event;

    public GBase8aShowResourcePoolEventsDialog(Shell parentShell, GBase8aDataSource dataSource) {
        super(parentShell, GBase8aMessages.dialog_show_resource_pool_events_title, null);
        this.dataSource = dataSource;
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

        TableColumn tbEventTime = new TableColumn(this.table, 0);
        tbEventTime.setWidth(100);
        tbEventTime.setText(GBase8aMessages.dialog_show_resource_pool_events_event_time);

        TableColumn tbTaskID = new TableColumn(this.table, 0);
        tbTaskID.setWidth(100);
        tbTaskID.setText(GBase8aMessages.dialog_show_resource_pool_events_task_id);

        TableColumn tbStatement = new TableColumn(this.table, 0);
        tbStatement.setWidth(500);
        tbStatement.setText(GBase8aMessages.dialog_show_resource_pool_events_statement);

        TableColumn tbEventType = new TableColumn(this.table, 0);
        tbEventType.setWidth(100);
        tbEventType.setText(GBase8aMessages.dialog_show_resource_pool_events_event_type);

        TableColumn tbEventDesc = new TableColumn(this.table, 0);
        tbEventDesc.setWidth(100);
        tbEventDesc.setText(GBase8aMessages.dialog_show_resource_pool_events_event_desc);

        this.tableViewer.setLabelProvider(new TableViewerLabelProvider());
        this.tableViewer.setContentProvider(new TableViewerContentProvider());
        this.tableViewer.setInput(this.list_event);

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
            return ((List<?>) inputElement).toArray();
        }
    }


    public List<GBase8aResourcePoolEvent> getList_event() {
        return this.list_event;
    }

    public void setList_event(List<GBase8aResourcePoolEvent> list_event) {
        this.list_event = list_event;
    }

    static class TableViewerLabelProvider implements ITableLabelProvider {
        public String getColumnText(Object element, int columnIndex) {
            GBase8aResourcePoolEvent event = (GBase8aResourcePoolEvent) element;
            if (columnIndex == 0) {
                return event.getNodeName();
            }
            if (columnIndex == 1) {
                return event.getVcName();
            }
            if (columnIndex == 2) {
                return event.getResourcePoolName();
            }
            if (columnIndex == 3) {
                return event.getEventTime();
            }
            if (columnIndex == 4) {
                return event.getTaskId();
            }
            if (columnIndex == 5) {
                return event.getStatement();
            }
            if (columnIndex == 6) {
                return event.getEventType();
            }
            if (columnIndex == 7) {
                return event.getEventDesc();
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
