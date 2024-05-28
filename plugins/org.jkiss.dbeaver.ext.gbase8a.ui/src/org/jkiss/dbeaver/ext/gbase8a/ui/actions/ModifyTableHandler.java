package org.jkiss.dbeaver.ext.gbase8a.ui.actions;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTable;
import org.jkiss.dbeaver.ext.gbase8a.ui.views.GBase8aTableDialog;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseItem;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;


public class ModifyTableHandler extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof DBNDatabaseItem) {
                DBPDataSource dbpds = ((DBNDatabaseNode) element).getDataSource();
                GBase8aTable gt = (GBase8aTable) ((DBNDatabaseItem) element).getObject();
                GBase8aTableDialog dialog = new GBase8aTableDialog(UIUtils.getActiveWorkbenchShell(), ((GBase8aDataSource) dbpds).getMonitor(), gt.getDataSource(), false);
                dialog.setGbase8aTable(gt);
                dialog.open();
                try {
                    ((DBNDatabaseItem) element).refreshNode(gt.getDataSource().getMonitor(), gt.getDataSource());

                    ((DBNDatabaseItem) element).getParentNode().getParentNode().getParentNode().getParentNode().refreshNode(gt.getDataSource().getMonitor(), gt.getDataSource());

                    ((DBNDatabaseItem) element).getParentNode().getParentNode().refreshNode(gt.getDataSource().getMonitor(), gt.getDataSource());
                } catch (DBException e) {
                    throw new ExecutionException(e.getMessage());
                }
            }
        }
        return null;
    }
}