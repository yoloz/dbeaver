package org.jkiss.dbeaver.ext.gbase8a.ui.actions;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aUtils;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseItem;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;


public class DelResourceDirectiveHandler extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof DBNDatabaseItem) {

                String directiveName = ((DBNDatabaseNode) element).getName();
                String vcName = ((DBNDatabaseNode) element).getParentNode().getParentNode().getName();

                GBase8aDataSource dataSource = (GBase8aDataSource) ((DBNDatabaseNode) element).getDataSource();

                if (!MessageDialog.openConfirm(
                        UIUtils.getActiveWorkbenchShell(),
                        GBase8aMessages.gbase8a_handlers_del_handler_confirm_title,
                        GBase8aMessages.gbase8a_handlers_del_handler_confirm_resourcedirective_message)) {
                    return null;
                }
                boolean lastDirectiveFlag;
                try (JDBCSession dbsession = DBUtils.openMetaSession(dataSource.getMonitor(), dataSource, "del consumer group");
                     JDBCStatement stmt = dbsession.createStatement()) {
                    lastDirectiveFlag = GBase8aUtils.isLastDirective(dbsession, vcName, dataSource, directiveName);
                    if (lastDirectiveFlag) {
                        String defaultDirectiveName = GBase8aUtils.getDefaultDirectiveName(dbsession, vcName, dataSource, directiveName);
                        if (!defaultDirectiveName.isEmpty())
                            if (dataSource.isVCCluster()) {
                                stmt.execute("DROP resource directive " + vcName + "." + defaultDirectiveName);
                            } else {
                                stmt.execute("DROP resource directive " + defaultDirectiveName);
                            }
                    }
                    if (dataSource.isVCCluster()) {
                        stmt.execute("DROP resource directive " + vcName + "." + directiveName);
                    } else {
                        stmt.execute("DROP resource directive " + directiveName);
                    }
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
