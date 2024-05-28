package org.jkiss.dbeaver.ext.gbase8a.ui.actions;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aSysCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTable;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseItem;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
import org.jkiss.dbeaver.ui.UIUtils;

import java.sql.SQLException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.handlers.HandlerUtil;


public class DropTableDistributedColumnHandler extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        String newSize = "0";
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof DBNDatabaseItem) {
                DBPDataSource dbpds = ((DBNDatabaseNode) element).getDataSource();
                GBase8aTable gt = (GBase8aTable) ((DBNDatabaseItem) element).getObject();
                String vcName = gt.getContainer().getVcName();
                String dbName = gt.getContainer().getName();
                String tbName = gt.getName();
                String sql = null;
                if (UIUtils.confirmAction(GBase8aMessages.drop_table_distributed_column_confirm_title, GBase8aMessages.drop_table_distributed_column_confirm_message)) {
                    try (JDBCSession dbsession = DBUtils.openMetaSession(((GBase8aDataSource) dbpds).getMonitor(), gt.getDataSource(),
                            GBase8aMessages.message_query_sql);
                         JDBCStatement stmt = dbsession.createStatement()) {
                        sql = "drop distributed column on `";
                        if (gt.getDataSource().isVCCluster() && !(gt.getContainer() instanceof GBase8aSysCatalog)) {
                            sql = sql + vcName + "`.`";
                        }
                        sql = sql + dbName + "`.`" + tbName +
                                "`";
                        stmt.execute(sql);
                        UIUtils.showMessageBox(null, GBase8aMessages.message_information_title, NLS.bind(GBase8aMessages.dialog_drop_table_distributed_column_ok, newSize.toUpperCase()), 2);
                        stmt.close();
                        ((DBNDatabaseItem) element).refreshNode(gt.getDataSource().getMonitor(), gt.getDataSource());
                        ((DBNDatabaseItem) element).getParentNode().getParentNode().getParentNode().getParentNode().refreshNode(gt.getDataSource().getMonitor(), gt.getDataSource());
                        ((DBNDatabaseItem) element).getParentNode().getParentNode().refreshNode(gt.getDataSource().getMonitor(), gt.getDataSource());
                    } catch (DBException | SQLException e) {
                        throw new ExecutionException(e.getMessage());
                    }
                }
            }
        }
        return null;
    }
}
