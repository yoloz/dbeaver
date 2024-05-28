package org.jkiss.dbeaver.ext.gbase8a.ui.actions;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aResourcePool;
import org.jkiss.dbeaver.ext.gbase8a.ui.views.GBase8aRenameResourcePoolDialog;
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


public class RenameResourcePoolHandler extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            GBase8aResourcePool resourcePool = (GBase8aResourcePool) ((DBNDatabaseItem) element).getObject();
            String vcName = ((DBNDatabaseNode) element).getParentNode().getParentNode().getName();
            GBase8aDataSource dataSource = (GBase8aDataSource) ((DBNDatabaseNode) element).getDataSource();
            GBase8aRenameResourcePoolDialog dialog = new GBase8aRenameResourcePoolDialog(UIUtils.getActiveWorkbenchShell(), vcName, resourcePool.getName());
            if (dialog.open() != 0) {
                return null;
            }
            String newName = dialog.getNewName();
            if (!newName.isEmpty()) {
                try (JDBCSession dbsession = DBUtils.openMetaSession(dataSource.getMonitor(), dataSource, "rename resource pool");
                     JDBCStatement stmt = dbsession.createStatement()) {
                    String createSql = "alter resource pool `";
                    if (dataSource.isVCCluster() && !"Default".equalsIgnoreCase(vcName)) {
                        stmt.execute("use vc " + vcName);
                        createSql = createSql + vcName + "`.`" + resourcePool.getName() + "` rename `" + newName + "`";
                    } else {
                        createSql = createSql + resourcePool.getName() + "` rename `" + newName + "`";
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
                ((DBNDatabaseNode) element).getParentNode().getParentNode().getParentNode().refreshNode(dataSource.getMonitor(), dataSource);
            } catch (DBException e) {
                throw new ExecutionException(e.getMessage());
            }
        }

        return null;
    }
}
