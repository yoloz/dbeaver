package org.jkiss.dbeaver.ext.gbase8a.ui.actions;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aResourcePlan;
import org.jkiss.dbeaver.ext.gbase8a.ui.views.GBase8aModifyResourcePlanDialog;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseItem;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;


public class ModifyResourcePlanHandler extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            GBase8aResourcePlan group = (GBase8aResourcePlan) ((DBNDatabaseItem) element).getObject();
            String vcName = ((DBNDatabaseNode) element).getParentNode().getParentNode().getName();
            GBase8aDataSource dataSource = (GBase8aDataSource) ((DBNDatabaseNode) element).getDataSource();
            GBase8aModifyResourcePlanDialog dialog = new GBase8aModifyResourcePlanDialog(UIUtils.getActiveWorkbenchShell(), dataSource, group, vcName);
            if (dialog.open() != 0) {
                return null;
            }
            String resourcePlanName = dialog.getName();
            if (!resourcePlanName.isEmpty()) {
                try (JDBCSession dbsession = DBUtils.openMetaSession(dataSource.getMonitor(), dataSource, "modify resource plan");
                     JDBCStatement stmt = dbsession.createStatement()) {
                    String alertSql = "";
                    if (!dialog.getOriginal_name().equals(resourcePlanName)) {
                        if (dataSource.isVCCluster()) {
                            alertSql = "alter resource plan " + vcName + "." + dialog.getOriginal_name() + " rename to " + resourcePlanName;
                        } else {
                            alertSql = "alter resource plan " + dialog.getOriginal_name() + " rename to " + resourcePlanName;
                        }
                        stmt.execute(alertSql);
                    }
                    String comment = dialog.getComment();
                    if (comment != null && !comment.trim().isEmpty()) {
                        if (dataSource.isVCCluster()) {
                            alertSql = "alter resource plan " + vcName + "." + resourcePlanName + " comment='" + comment.trim() + "'";
                        } else {
                            alertSql = "alter resource plan " + resourcePlanName + " comment='" + comment.trim() + "'";
                        }
                        stmt.execute(alertSql);
                    }
                } catch (Exception e) {
                    MessageBox box = new MessageBox(UIUtils.getActiveWorkbenchShell(), 1);
                    box.setMessage("Error");
                    box.setMessage(e.getMessage());
                    box.open();
                    throw new ExecutionException(e.getMessage());
                }
            }
            try {
                ((DBNDatabaseNode) element).getParentNode().getParentNode().getParentNode().refreshNode(dataSource.getMonitor(), dataSource);
            } catch (DBException e) {
                throw new ExecutionException(e.getMessage());
            }
        }
        return null;
    }
}
