package org.jkiss.dbeaver.ext.gbase8a.ui.actions;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aSysCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTable;
import org.jkiss.dbeaver.ext.gbase8a.ui.views.GBase8aModifyTableLimitSizeDialog;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseItem;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;

import java.sql.SQLException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.handlers.HandlerUtil;


public class ModifyTableLimitSizeHandler extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        String oldSize;
        String newSize;
        long oldSizeNum = 0L;

        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof DBNDatabaseItem) {
                DBPDataSource dbpds = ((DBNDatabaseNode) element).getDataSource();
                GBase8aTable gt = (GBase8aTable) ((DBNDatabaseItem) element).getObject();
                String dbName = gt.getContainer().getName();
                String tbName = gt.getName();
                String sql = "select TABLE_LIMIT_STORAGE_SIZE,TABLE_STORAGE_SIZE from information_schema.tables where table_schema='" +
                        dbName + "' and table_name='" + tbName + "'";
                if (gt.getDataSource().isVCCluster() && !(gt.getContainer() instanceof GBase8aSysCatalog)) {
                    sql = sql + " and table_vc='" + gt.getContainer().getVcName() + "'";
                }
                try (JDBCSession dbsession = DBUtils.openMetaSession(((GBase8aDataSource) dbpds).getMonitor(), gt.getDataSource(), GBase8aMessages.message_query_sql);
                     JDBCStatement stmt = dbsession.createStatement();
                     JDBCResultSet rs = stmt.executeQuery(sql)) {
                    if (rs.next()) {
                        oldSizeNum = rs.getLong("TABLE_LIMIT_STORAGE_SIZE");
                        rs.getLong("TABLE_STORAGE_SIZE");
                    }
                    if (oldSizeNum / 1024L / 1024L / 1024L / 1024L > 0L &&
                            oldSizeNum % 256L == 0L) {
                        oldSize = oldSizeNum / 1024L / 1024L / 1024L / 1024L + "T";
                    } else if (oldSizeNum / 1024L / 1024L / 1024L > 0L &&
                            oldSizeNum % 1073741824L == 0L) {
                        oldSize = oldSizeNum / 1024L / 1024L / 1024L + "G";
                    } else if (oldSizeNum / 1024L / 1024L > 0L &&
                            oldSizeNum % 1048576L == 0L) {
                        oldSize = oldSizeNum / 1024L / 1024L + "M";
                    } else if (oldSizeNum / 1024L > 0L && oldSizeNum % 1024L == 0L) {
                        oldSize = oldSizeNum / 1024L + "K";
                    } else {
                        oldSize = "0";
                    }
                    GBase8aModifyTableLimitSizeDialog dialog = new GBase8aModifyTableLimitSizeDialog(UIUtils.getActiveWorkbenchShell(), oldSize, dbName, tbName);
                    if (dialog.open() != 0) {
                        return null;
                    }
                    newSize = dialog.getTableSize();
                    sql = "alter table `";
                    if (gt.getDataSource().isVCCluster() && !(gt.getContainer() instanceof GBase8aSysCatalog)) {
                        sql = sql + gt.getContainer().getVcName() + "`.`";
                    }
                    sql = sql + dbName + "`.`" + tbName + "` limit_storage_size = " + newSize + ";";
                    try (JDBCStatement stmt1 = dbsession.createStatement()) {
                        stmt1.execute(sql);
                        UIUtils.showMessageBox(null, GBase8aMessages.message_information_title, NLS.bind(GBase8aMessages.dialog_modify_table_limit_size_ok, newSize.toUpperCase()), 2);
                    }
                    ((DBNDatabaseItem) element).refreshNode(gt.getDataSource().getMonitor(), gt.getDataSource());
                    ((DBNDatabaseItem) element).getParentNode().getParentNode().getParentNode().getParentNode().refreshNode(gt.getDataSource().getMonitor(), gt.getDataSource());
                    ((DBNDatabaseItem) element).getParentNode().getParentNode().refreshNode(gt.getDataSource().getMonitor(), gt.getDataSource());
                } catch (DBException | SQLException e) {
                    throw new ExecutionException(e.getMessage());
                }
            }
        }
        return null;
    }
}
