package org.jkiss.dbeaver.ext.gbase8a.ui.actions;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseItem;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jkiss.dbeaver.ui.UIUtils;


public class ActiveResourcePlanHandler  extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof DBNDatabaseItem) {
                String resourcePlanName = ((DBNDatabaseNode) element).getName();
                String vcName = ((DBNDatabaseNode) element).getParentNode().getParentNode().getName();
                GBase8aDataSource dataSource = (GBase8aDataSource) ((DBNDatabaseNode) element).getDataSource();
                try (JDBCSession dbsession = DBUtils.openMetaSession(dataSource.getMonitor(), dataSource, "Active resource plan");
                     JDBCStatement stmt = dbsession.createStatement()) {
                    if (!vcName.equalsIgnoreCase("default")) {
                        stmt.execute("active resource plan " + resourcePlanName + " on vc " + vcName);
                    } else {
                        stmt.execute("set global active_resource_plan=" + resourcePlanName);
                    }
                    MessageBox box = new MessageBox(UIUtils.getActiveWorkbenchShell());
                    box.setMessage(GBase8aMessages.active_resourceplan_success);
                    box.open();
                } catch (Exception e) {
                    MessageBox box = new MessageBox(UIUtils.getActiveWorkbenchShell(), 1);
                    box.setMessage("Error");
                    box.setMessage(e.getMessage());
                    box.open();
                    throw new ExecutionException(e.getMessage());
                }
                try {
                    ((DBNDatabaseNode) element).getParentNode().getParentNode().getParentNode().refreshNode(dataSource.getMonitor(), dataSource);
                } catch (DBException e) {
                    throw new ExecutionException(e.getMessage());
                }
            }
        }
        return null;
    }
}