package org.jkiss.dbeaver.ext.gbase8a.ui.actions;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableSpace;
import org.jkiss.dbeaver.ext.gbase8a.ui.views.GBase8aCreateTableSpaceDialog;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseItem;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;


public class ModifyTableSpaceHandler extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof DBNDatabaseItem) {
                GBase8aTableSpace gbase8aTableSpace = (GBase8aTableSpace) ((DBNDatabaseItem) element).getObject();
                String vcName = ((DBNDatabaseItem) element).getParentNode().getParentNode().getParentNode().getName();
                GBase8aDataSource dataSource = (GBase8aDataSource) gbase8aTableSpace.getDataSource();
                GBase8aCreateTableSpaceDialog dialog = new GBase8aCreateTableSpaceDialog(UIUtils.getActiveWorkbenchShell(), gbase8aTableSpace, false);
                if (dialog.open() != 0) {
                    return null;
                }
                String sql = dialog.getSQL();
                if (sql != null && !sql.isEmpty()) {
                    try (JDBCSession dbsession = DBUtils.openMetaSession(dataSource.getMonitor(), (DBPDataSource) dataSource, "create tablespace");
                         JDBCStatement stmt = dbsession.createStatement()) {
                        if (dataSource.isVCCluster()) {
                            stmt.execute("use vc " + vcName);
                        }
                        stmt.execute(sql);
                    } catch (Exception e) {
                        MessageBox box = new MessageBox(UIUtils.getActiveWorkbenchShell(), 1);
                        box.setMessage("Error");
                        box.setMessage(e.getMessage());
                        box.open();
                        throw new ExecutionException(e.getMessage());
                    }
                }
                try {
                    if (((DBNDatabaseItem) element).getParentNode() != null && (
                            (DBNDatabaseItem) element).getParentNode().getParentNode() != null && (
                            (DBNDatabaseItem) element).getParentNode().getParentNode().getParentNode() != null && (
                            (DBNDatabaseItem) element).getParentNode().getParentNode().getParentNode().getParentNode() != null) {
                        ((DBNDatabaseItem) element).getParentNode().getParentNode().getParentNode().getParentNode().refreshNode(dataSource.getMonitor(), dataSource);
                    }
                } catch (DBException e) {
                    throw new ExecutionException(e.getMessage());
                }
            }
        }
        return null;
    }
}
