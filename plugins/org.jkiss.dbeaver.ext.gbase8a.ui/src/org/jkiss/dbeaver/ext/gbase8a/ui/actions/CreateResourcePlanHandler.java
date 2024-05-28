package org.jkiss.dbeaver.ext.gbase8a.ui.actions;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.ui.views.GBase8aCreateResourcePlanDialog;
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


public class CreateResourcePlanHandler extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            String vcName = ((DBNDatabaseNode) element).getParentNode().getName();
            GBase8aDataSource dataSource = (GBase8aDataSource) ((DBNDatabaseNode) element).getDataSource();
            GBase8aCreateResourcePlanDialog dialog = new GBase8aCreateResourcePlanDialog(UIUtils.getActiveWorkbenchShell(), dataSource, vcName);
            if (dialog.open() != 0) {
                return null;
            }
            String planName = dialog.getName();
            if (!planName.isEmpty()) {
                try (JDBCSession dbsession = DBUtils.openMetaSession(dataSource.getMonitor(), dataSource, "create resource plan");
                     JDBCStatement stmt = dbsession.createStatement()) {
                    if (dataSource.isVCCluster()) {
                        stmt.execute("use vc " + vcName);
                    }
                    String createSql = "";
                    if (dataSource.isVCCluster()) {
                        createSql = "create resource plan " + vcName + "." + planName;
                    } else {
                        createSql = "create resource plan " + planName;
                    }
                    String comment = dialog.getComment();
                    if (comment != null && !comment.trim().isEmpty()) {
                        createSql = createSql + " comment='" + comment.trim() + "'";
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
