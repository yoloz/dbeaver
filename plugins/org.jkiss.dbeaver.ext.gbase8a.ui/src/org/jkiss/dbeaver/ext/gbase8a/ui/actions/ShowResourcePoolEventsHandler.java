package org.jkiss.dbeaver.ext.gbase8a.ui.actions;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aResourcePool;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aResourcePoolEvent;
import org.jkiss.dbeaver.ext.gbase8a.ui.views.GBase8aShowResourcePoolEventsDialog;
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


public class ShowResourcePoolEventsHandler
        extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            GBase8aResourcePool resourcePool = (GBase8aResourcePool) ((DBNDatabaseItem) element).getObject();
            String vcName = resourcePool.getVcName();
            GBase8aDataSource dataSource = (GBase8aDataSource) ((DBNDatabaseNode) element).getDataSource();
            String name = resourcePool.getName();
            GBase8aShowResourcePoolEventsDialog dialog = new GBase8aShowResourcePoolEventsDialog(UIUtils.getActiveWorkbenchShell(), dataSource);

            List<GBase8aResourcePoolEvent> list_event = new ArrayList<>();
            String createSql = "show resource pool events ";
            if (dataSource.isVCCluster() && !"Default".equalsIgnoreCase(vcName)) {
                createSql = createSql + "where resource_pool_name=? and vc_name=?";
            } else {
                createSql = createSql + "where resource_pool_name=?";
            }
            try (JDBCSession dbsession = DBUtils.openMetaSession(dataSource.getMonitor(), dataSource, "show resource pool events");
                 JDBCPreparedStatement stmt = dbsession.prepareStatement(createSql)) {
                stmt.setString(1, name);
                if (dataSource.isVCCluster() && !"Default".equalsIgnoreCase(vcName)) {
                    stmt.setString(2, vcName);
                }
                try (JDBCResultSet result = stmt.executeQuery()) {
                    while (result.next()) {
                        GBase8aResourcePoolEvent resourcePoolevent = new GBase8aResourcePoolEvent();
                        resourcePoolevent.setNodeName(result.getString("node_name"));
                        resourcePoolevent.setVcName(vcName);
                        resourcePoolevent.setResourcePoolName(name);
                        resourcePoolevent.setEventTime(result.getString("event_time"));
                        resourcePoolevent.setTaskId(result.getString("task_id"));
                        resourcePoolevent.setStatement(result.getString("statement"));
                        resourcePoolevent.setEventType(result.getString("event_type"));
                        resourcePoolevent.setEventDesc(result.getString("event_description"));
                        list_event.add(resourcePoolevent);
                    }
                }
            } catch (Exception e) {
                MessageBox box = new MessageBox(UIUtils.getActiveWorkbenchShell(), 1);
                box.setMessage("Error");
                box.setMessage(e.getMessage());
                box.open();
                throw new ExecutionException(e.getMessage());
            }
            dialog.setList_event(list_event);
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
