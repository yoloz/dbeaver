package org.jkiss.dbeaver.ext.gbase8a.ui.actions;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableFullIndex;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseItem;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;


public class UpdateIndexHandler extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof DBNDatabaseItem) {
                ((DBNDatabaseNode) element).getDataSource();
                GBase8aTableFullIndex fullIndex = (GBase8aTableFullIndex) ((DBNDatabaseItem) element).getObject();
                String updateSql = "update index `" + fullIndex.getName() + "` on " + "`" + fullIndex.getTable().getContainer().getName() + "`.`" + fullIndex.getTable().getName() + "`";
                try (JDBCSession dbsession = DBUtils.openMetaSession(fullIndex.getDataSource().getMonitor(), fullIndex.getDataSource(), "s2");
                     JDBCStatement stmt = dbsession.createStatement()) {
                    stmt.execute(updateSql);
                    MessageDialog.openInformation(Display.getCurrent()
                            .getActiveShell(), GBase8aMessages.dialog_create_index_information, GBase8aMessages.dialog_update_full_index_success);
                } catch (Exception e) {
                    MessageDialog.openError(Display.getCurrent().getActiveShell(),
                            GBase8aMessages.dialog_create_index_information, GBase8aMessages.dialog_update_full_index_fail + e.getMessage());
                }
                try {
                    ((DBNDatabaseItem) element).refreshNode(fullIndex.getDataSource().getMonitor(), fullIndex.getDataSource());

                    ((DBNDatabaseItem) element).getParentNode().getParentNode().getParentNode().getParentNode().refreshNode(fullIndex.getDataSource().getMonitor(), fullIndex.getDataSource());

                    ((DBNDatabaseItem) element).getParentNode().getParentNode().refreshNode(fullIndex.getDataSource().getMonitor(), fullIndex.getDataSource());
                } catch (DBException e) {
                    throw new ExecutionException(e.getMessage());
                }
            }
        }
        return null;
    }
}
