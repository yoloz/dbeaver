package org.jkiss.dbeaver.ext.gbase8a.ui.actions;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseItem;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;


public class DeleteDatabaseHandler extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof DBNDatabaseItem) {


                GBase8aCatalog gbase8aCatalog = (GBase8aCatalog) ((DBNDatabaseItem) element).getObject();
                GBase8aDataSource dataSource = gbase8aCatalog.getDataSource();

                if (!MessageDialog.openConfirm(
                        UIUtils.getActiveWorkbenchShell(),
                        GBase8aMessages.gbase8a_handlers_del_handler_confirm_title,
                        GBase8aMessages.gbase8a_handlers_del_handler_confirm_message)) {
                    return null;
                }
                try (JDBCSession dbsession = DBUtils.openMetaSession(dataSource.getMonitor(), dataSource, "create database");
                     JDBCStatement stmt = dbsession.createStatement()) {
                    if (dataSource.isVCCluster()) {
                        stmt.execute("use vc " + gbase8aCatalog.getVcName());
                    }
                    stmt.execute("DROP SCHEMA `" + gbase8aCatalog.getName() + "`");
                } catch (Exception e) {
                    MessageBox box = new MessageBox(UIUtils.getActiveWorkbenchShell(), 1);
                    box.setMessage("Error");
                    box.setMessage(e.getMessage());
                    box.open();
                    throw new ExecutionException(e.getMessage());
                }
                try {
                    ((DBNDatabaseItem) element).getParentNode().getParentNode().refreshNode(dataSource.getMonitor(), dataSource);
                } catch (DBException e) {
                    throw new ExecutionException(e.getMessage());
                }
            }
        }
        return null;
    }
}
