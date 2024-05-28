//package org.jkiss.dbeaver.ext.gbase8a.ui.actions;
//
//import org.jkiss.dbeaver.DBException;
//import org.jkiss.dbeaver.Log;
//import org.jkiss.dbeaver.core.CoreMessages;
//import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
//import org.jkiss.dbeaver.ui.UIUtils;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
//import org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata.ImportTableDataEditorInput;
//import org.jkiss.dbeaver.model.navigator.DBNDatabaseItem;
//import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
//import org.eclipse.core.commands.AbstractHandler;
//import org.eclipse.core.commands.ExecutionEvent;
//import org.eclipse.core.commands.ExecutionException;
//import org.eclipse.jface.viewers.ISelection;
//import org.eclipse.jface.viewers.IStructuredSelection;
//import org.eclipse.ui.IEditorInput;
//import org.eclipse.ui.handlers.HandlerUtil;
//
//public class ImportDataHandler extends AbstractHandler {
//    Log log = Log.getLog(ImportDataHandler.class);
//
//    public Object execute(ExecutionEvent event) throws ExecutionException {
//        ISelection selection = HandlerUtil.getCurrentSelection(event);
//        GBase8aCatalog gc = null;
//        if (selection instanceof IStructuredSelection) {
//            Object element = ((IStructuredSelection) selection).getFirstElement();
//            if (element instanceof DBNDatabaseItem &&
//                    (((DBNDatabaseNode) element).getParentNode().getName().equals(CoreMessages.dialog_connection_wizard_final_filter_tables)
//                            || ((DBNDatabaseNode) element).getParentNode().getName().equals(GBase8aMessages.tree_user_tables_node_name))) {
//                gc = (GBase8aCatalog) ((DBNDatabaseItem) element).getObject().getParentObject();
//                ImportTableDataEditorInput input = new ImportTableDataEditorInput();
//                input.setVcName(((DBNDatabaseNode) element).getParentNode().getParentNode().getParentNode().getName());
//                input.setDbname(((DBNDatabaseNode) element).getParentNode().getParentNode().getName());
//                input.setDataSource(gc.getDataSource());
//                input.setDBNDatabaseNode((DBNDatabaseNode) ((DBNDatabaseNode) element).getParentNode().getParentNode());
//                input.setTableName(((DBNDatabaseNode) element).getName());
//                try {
//                    UIUtils.getActiveWorkbenchWindow().getActivePage().openEditor(input, "cn.gbase.studio.ext.gbase8a.tools.importdata.ImportTableDataEditor");
//                } catch (Exception e) {
//                    log.error(e);
//                }
//            } else {
//                gc = (GBase8aCatalog) ((DBNDatabaseItem) element).getObject();
//                ImportTableDataEditorInput input = new ImportTableDataEditorInput();
//                input.setVcName(((DBNDatabaseNode) element).getParentNode().getName());
//                input.setDbname(((DBNDatabaseNode) element).getName());
//                input.setDataSource(gc.getDataSource());
//                input.setDBNDatabaseNode((DBNDatabaseNode) element);
//                try {
//                    UIUtils.getActiveWorkbenchWindow().getActivePage().openEditor(input, "cn.gbase.studio.ext.gbase8a.tools.importdata.ImportTableDataEditor");
//                } catch (Exception e) {
//                    log.error(e);
//                }
//            }
//            try {
//                ((DBNDatabaseItem) element).refreshNode(gc.getDataSource().getMonitor(), gc.getDataSource());
//                ((DBNDatabaseItem) element).getParentNode().getParentNode().getParentNode().getParentNode().refreshNode(gc.getDataSource().getMonitor(), gc.getDataSource());
//                ((DBNDatabaseItem) element).getParentNode().getParentNode().refreshNode(gc.getDataSource().getMonitor(), gc.getDataSource());
//            } catch (DBException e) {
//                throw new ExecutionException(e.getMessage());
//            }
//        }
//        return null;
//    }
//}
