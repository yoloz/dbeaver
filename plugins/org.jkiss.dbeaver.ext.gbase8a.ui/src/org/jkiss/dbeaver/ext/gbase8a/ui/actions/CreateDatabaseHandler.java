package org.jkiss.dbeaver.ext.gbase8a.ui.actions;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aVC;
import org.jkiss.dbeaver.ext.gbase8a.ui.views.GBase8aCreateDatabaseDialog;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseItem;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jkiss.dbeaver.ui.UIUtils;


public class CreateDatabaseHandler extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof DBNDatabaseItem) {
                GBase8aVC gbase8aVC = (GBase8aVC) ((DBNDatabaseItem) element).getObject();
                GBase8aDataSource dataSource = (GBase8aDataSource) gbase8aVC.getDataSource();
                GBase8aCreateDatabaseDialog dialog = new GBase8aCreateDatabaseDialog(UIUtils.getActiveWorkbenchShell(), dataSource);
                if (dialog.open() != 0) {
                    return null;
                }
                String sql = dialog.getSQL();
                if (sql != null && !sql.isEmpty()) {
                    try (JDBCSession dbsession = DBUtils.openMetaSession(dataSource.getMonitor(), dataSource, "create database");
                         JDBCStatement stmt = dbsession.createStatement()) {
                        if (dataSource.isVCCluster()) {
                            stmt.execute("use vc " + gbase8aVC.getName());
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
                    ((DBNDatabaseItem) element).getParentNode().refreshNode(dataSource.getMonitor(), gbase8aVC.getDataSource());
                } catch (DBException e) {
                    throw new ExecutionException(e.getMessage());
                }
            }
        }
        return null;
    }
}