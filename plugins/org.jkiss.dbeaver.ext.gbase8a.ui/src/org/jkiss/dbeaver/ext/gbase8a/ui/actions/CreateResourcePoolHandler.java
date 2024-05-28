package org.jkiss.dbeaver.ext.gbase8a.ui.actions;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aResourcePool;
import org.jkiss.dbeaver.ext.gbase8a.ui.views.GBase8aCreateResourcePoolDialog;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jkiss.dbeaver.ui.UIUtils;


public class CreateResourcePoolHandler extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            String vcName = ((DBNDatabaseNode) element).getParentNode().getName();
            GBase8aDataSource dataSource = (GBase8aDataSource) ((DBNDatabaseNode) element).getDataSource();
            GBase8aResourcePool resourcePool = new GBase8aResourcePool(dataSource, null);
            resourcePool.setVcName(vcName);
            GBase8aCreateResourcePoolDialog dialog = new GBase8aCreateResourcePoolDialog(UIUtils.getActiveWorkbenchShell(), dataSource, resourcePool, true);
            if (dialog.open() != 0) {
                return null;
            }
            String name = dialog.getName();
            if (!name.isEmpty()) {
                try (JDBCSession dbsession = DBUtils.openMetaSession(dataSource.getMonitor(), dataSource, "create resource pool");
                     JDBCStatement stmt = dbsession.createStatement()) {
                    String createSql = "create resource pool `";
                    if (dataSource.isVCCluster() && !"Default".equalsIgnoreCase(vcName)) {
                        stmt.execute("use vc " + vcName);
                        createSql = createSql + vcName + "`.`" + name + "` (";
                    } else {
                        createSql = createSql + name + "` (";
                    }
                    long priority = dialog.getPriority();
                    if (priority > 0L) {
                        createSql = createSql + " priority=" + priority + ",";
                    }
                    createSql = createSql + " cpu_percent =" + dialog.getCpu_percent() + ",";
                    createSql = createSql + " max_memory =" + dialog.getMax_memory() + ",";
                    createSql = createSql + " max_temp_diskspace =" + dialog.getMax_temp_diskspace() + ",";
                    createSql = createSql + " max_disk_space=" + dialog.getMax_disk_space() + ",";
                    createSql = createSql + " max_disk_writeio=" + dialog.getMax_disk_writeio() + ",";
                    createSql = createSql + " max_disk_readio=" + dialog.getMax_disk_readio() + ",";
                    String type = dialog.getType();
                    if ("dynamic".equals(type)) {
                        long max_activetask = dialog.getMax_activetask();
                        if (max_activetask > 0L) {
                            createSql = createSql + " max_activetask=" + max_activetask + ",";
                        }
                        long task_max_parallel_degree = dialog.getTask_max_parallel_degree();
                        if (task_max_parallel_degree > 0L) {
                            createSql = createSql + " task_max_parallel_degree=" + task_max_parallel_degree + ",";
                        }
                        long task_waiting_timeout = dialog.getTask_waiting_timeout();
                        if (task_waiting_timeout > 0L) {
                            createSql = createSql + " task_waiting_timeout=" + task_waiting_timeout + ",";
                        }
                        long task_running_timeout = dialog.getTask_running_timeout();
                        if (task_running_timeout > 0L) {
                            createSql = createSql + " task_running_timeout=" + task_running_timeout + ",";
                        }
                    }
                    createSql = createSql.substring(0, createSql.lastIndexOf(","));
                    createSql = createSql + ")";
                    createSql = createSql + " TYPE " + type;
                    String baseOn = dialog.getBaseOn();
                    if (baseOn != null && !baseOn.isEmpty()) {
                        createSql = createSql + " BASE ON " + baseOn;
                    }
                    stmt.execute(createSql);
                } catch (Exception e) {
                    MessageBox box = new MessageBox(UIUtils.getActiveWorkbenchShell(), 1);
                    box.setMessage("Error");
                    box.setMessage(e.getMessage());
                    box.open();
                    throw new ExecutionException(e.getMessage());
                }
            }
            try {
                ((DBNDatabaseNode) element).getParentNode().getParentNode().refreshNode(dataSource.getMonitor(), dataSource);
            } catch (DBException e) {
                throw new ExecutionException(e.getMessage());
            }
        }

        return null;
    }
}
