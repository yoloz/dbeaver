package org.jkiss.dbeaver.ext.gbase8a.ui.actions;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ext.gbase8a.ui.editors.GBaseupExportObjectEditorInput;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
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


public class ExportObjectHandler extends AbstractHandler {
    Log log = Log.getLog(ExportObjectHandler.class);

    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof DBNDatabaseItem) {
                ((DBNDatabaseNode) element).getDataSource();
                GBase8aCatalog gc = (GBase8aCatalog) ((DBNDatabaseItem) element).getObject();
                GBase8aDataSource dataSource = gc.getDataSource();
                String vcName = ((DBNDatabaseItem) element).getParentNode().getName();
                if (!vcName.equalsIgnoreCase("default")) {
                    try (JDBCSession dbsession = DBUtils.openMetaSession(dataSource.getMonitor(), dataSource, "use vc");
                         JDBCStatement stmt = dbsession.createStatement()) {
                        if (dataSource.isVCCluster()) {
                            stmt.execute("use vc " + vcName);
                        }
                    } catch (Exception e) {
                        MessageBox box = new MessageBox(UIUtils.getActiveWorkbenchShell(), 1);
                        box.setMessage("Error");
                        box.setMessage(e.getMessage());
                        box.open();
                        throw new ExecutionException(e.getMessage());
                    }
                    GBaseupExportObjectEditorInput input = new GBaseupExportObjectEditorInput(gc.getDataSource());
                    input.setDbname(((DBNDatabaseNode) element).getNodeFullName().split(":")[0]);
                    try {
                        UIUtils.getActiveWorkbenchWindow().getActivePage().openEditor(input, "cn.gbase.studio.ext.gbase8a.editors.GBaseupExportObjectEditor");
                    } catch (Exception e) {
                       log.error(e);
                    }
                    try {
                        ((DBNDatabaseItem) element).refreshNode(gc.getDataSource().getMonitor(), gc.getDataSource());
                        ((DBNDatabaseItem) element).getParentNode().getParentNode().getParentNode().getParentNode().refreshNode(gc.getDataSource().getMonitor(), gc.getDataSource());
                        ((DBNDatabaseItem) element).getParentNode().getParentNode().refreshNode(gc.getDataSource().getMonitor(), gc.getDataSource());
                    } catch (DBException e) {
                        throw new ExecutionException(e.getMessage());
                    }
                }
            }
        }
        return null;
    }
}
