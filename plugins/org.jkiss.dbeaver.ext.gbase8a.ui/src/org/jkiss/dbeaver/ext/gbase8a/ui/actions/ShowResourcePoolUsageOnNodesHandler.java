package org.jkiss.dbeaver.ext.gbase8a.ui.actions;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aResourcePool;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aResourcePoolUsage;
import org.jkiss.dbeaver.ext.gbase8a.ui.views.GBase8aShowResourcePoolUsageOnNodesDialog;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseItem;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;


public class ShowResourcePoolUsageOnNodesHandler extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();

            GBase8aResourcePool resourcePool = (GBase8aResourcePool) ((DBNDatabaseItem) element).getObject();
            String vcName = resourcePool.getVcName();
            GBase8aDataSource dataSource = (GBase8aDataSource) ((DBNDatabaseNode) element).getDataSource();
            String name = resourcePool.getName();

            GBase8aShowResourcePoolUsageOnNodesDialog dialog = new GBase8aShowResourcePoolUsageOnNodesDialog(UIUtils.getActiveWorkbenchShell(), dataSource, false);

            List<GBase8aResourcePoolUsage> list_usage = new ArrayList<>();
            String createSql = "show resource pool ";
            if (dataSource.isVCCluster() && !"Default".equalsIgnoreCase(vcName)) {
                createSql = createSql + "usage on nodes where resource_pool_name=? and vc_name=?";
            } else {
                createSql = createSql + "usage on nodes where resource_pool_name=?";
            }
            try (JDBCSession dbsession = DBUtils.openMetaSession(dataSource.getMonitor(), dataSource, "show resource pool usage on nodes");
                 JDBCPreparedStatement stmt = dbsession.prepareStatement(createSql)) {
                stmt.setString(1, name);
                if (dataSource.isVCCluster() && !"Default".equalsIgnoreCase(vcName)) {
                    stmt.setString(2, vcName);
                }
                try (JDBCResultSet result = stmt.executeQuery()) {
                    while (result.next()) {
                        GBase8aResourcePoolUsage usage = new GBase8aResourcePoolUsage();
                        usage.setNodeName(result.getString("node_name"));
                        usage.setVcName(vcName);
                        usage.setResourcePoolname(name);
                        usage.setPriority(result.getString("priority"));
                        usage.setRunning_tasks(result.getString("running_tasks"));
                        usage.setWaiting_tasks(result.getString("waiting_tasks"));
                        usage.setCpu_usage(result.getString("cpu_usage"));
                        usage.setMem_usage(result.getString("mem_usage"));
                        usage.setDisk_usage(result.getString("disk_usage"));
                        usage.setDisk_writeio(result.getString("disk_writeio"));
                        usage.setDisk_readio(result.getString("disk_readio"));
                        list_usage.add(usage);
                    }
                }
            } catch (Exception e) {
                MessageBox box = new MessageBox(UIUtils.getActiveWorkbenchShell(), 1);
                box.setMessage("Error");
                box.setMessage(e.getMessage());
                box.open();
                throw new ExecutionException(e.getMessage());
            }
            dialog.setList_usage(list_usage);
            dialog.open();

            try {
                ((DBNDatabaseNode) element).getParentNode().getParentNode().refreshNode(dataSource.getMonitor(), dataSource);
            } catch (DBException e) {
                throw new ExecutionException(e.getMessage());
            }
        }
        return null;
    }
}
