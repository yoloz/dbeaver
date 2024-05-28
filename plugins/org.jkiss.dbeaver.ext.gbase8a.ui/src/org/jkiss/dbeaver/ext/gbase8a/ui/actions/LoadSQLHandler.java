//package org.jkiss.dbeaver.ext.gbase8a.ui.actions;
//
//import org.jkiss.dbeaver.ui.UIUtils;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
//import org.jkiss.dbeaver.ext.gbase8a.ui.views.GBase8aLoadSQLDialog;
//import org.jkiss.dbeaver.model.navigator.DBNDatabaseItem;
//import org.eclipse.core.commands.AbstractHandler;
//import org.eclipse.core.commands.ExecutionEvent;
//import org.eclipse.core.commands.ExecutionException;
//import org.eclipse.jface.viewers.ISelection;
//import org.eclipse.jface.viewers.IStructuredSelection;
//import org.eclipse.ui.handlers.HandlerUtil;
//
//public class LoadSQLHandler extends AbstractHandler {
//    public Object execute(ExecutionEvent event) throws ExecutionException {
//        ISelection selection = HandlerUtil.getCurrentSelection(event);
//        if (selection instanceof IStructuredSelection) {
//            Object element = ((IStructuredSelection) selection).getFirstElement();
//            if (element instanceof DBNDatabaseItem) {
//                GBase8aCatalog gbase8aCatalog = (GBase8aCatalog) ((DBNDatabaseItem) element).getObject();
//                GBase8aDataSource dataSource = gbase8aCatalog.getDataSource();
//                GBase8aLoadSQLDialog dialog = new GBase8aLoadSQLDialog(UIUtils.getActiveWorkbenchShell(), dataSource, gbase8aCatalog.getVcName(), gbase8aCatalog.getName());
//                if (dialog.open() != 0) {
//                    return null;
//                }
//            }
//        }
//        return null;
//    }
//}