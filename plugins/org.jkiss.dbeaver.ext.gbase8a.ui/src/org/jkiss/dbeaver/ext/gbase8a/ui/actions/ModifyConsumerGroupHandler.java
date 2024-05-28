package org.jkiss.dbeaver.ext.gbase8a.ui.actions;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aConsumerGroup;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.ui.views.GBase8aModifyConsumerGroupDialog;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseItem;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class ModifyConsumerGroupHandler extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            GBase8aConsumerGroup group = (GBase8aConsumerGroup) ((DBNDatabaseItem) element).getObject();
            String vcName = ((DBNDatabaseNode) element).getParentNode().getParentNode().getName();
            GBase8aDataSource dataSource = (GBase8aDataSource) ((DBNDatabaseNode) element).getDataSource();
            GBase8aModifyConsumerGroupDialog dialog = new GBase8aModifyConsumerGroupDialog(UIUtils.getActiveWorkbenchShell(), dataSource, group, vcName);
            if (dialog.open() != 0) {
                return null;
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
