package org.jkiss.dbeaver.ext.gbase8a.ui.actions;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aUtils;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.ui.views.GBase8aCreateResourceDirectiveDialog;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jkiss.dbeaver.ui.UIUtils;


public class CreateResourceDirectiveHandler extends AbstractHandler {
    Log log = Log.getLog(CreateResourceDirectiveHandler.class);

    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            String vcName = ((DBNDatabaseNode) element).getParentNode().getName();
            GBase8aDataSource dataSource = (GBase8aDataSource) ((DBNDatabaseNode) element).getDataSource();
            String activeResourcePlanId = "";
            try (JDBCSession session = DBUtils.openMetaSession(dataSource.getMonitor(), dataSource, "Load basic datasource metadata")) {
                activeResourcePlanId = GBase8aUtils.getActiveResourcePlanId(session, vcName, dataSource);
            } catch (DBCException e1) {
                log.error(e1);
            }
            if (activeResourcePlanId != null && !activeResourcePlanId.isEmpty() && !activeResourcePlanId.equals("0")) {
                MessageBox box = new MessageBox(UIUtils.getActiveWorkbenchShell(), 8);
                box.setMessage("Warning");
                box.setMessage(GBase8aMessages.dialog_create_resource_directive_can_not_active);
                box.open();
                throw new ExecutionException(GBase8aMessages.dialog_create_resource_directive_can_not_active);
            }
            GBase8aCreateResourceDirectiveDialog dialog = new GBase8aCreateResourceDirectiveDialog(UIUtils.getActiveWorkbenchShell(), dataSource, vcName);
            if (dialog.open() != 0) {
                return null;
            }
            String directiveName = dialog.getName();
            if (!directiveName.isEmpty()) {
                try (JDBCSession dbsession = DBUtils.openMetaSession(dataSource.getMonitor(), dataSource, "create directive group");
                     JDBCStatement stmt = dbsession.createStatement()) {
                    if (dataSource.isVCCluster()) {
                        stmt.execute("use vc " + vcName);
                    }
                    String createSql = "";
                    if (dataSource.isVCCluster()) {
                        createSql = "create resource directive " + vcName + "." + directiveName;
                    } else {
                        createSql = "create resource directive " + directiveName;
                    }
                    String plan = dialog.getPlanName();
                    String group = dialog.getGroupName();
                    String pool = dialog.getPoolName();
                    String comment = dialog.getComment();
                    createSql = createSql + "(plan_name='" + plan + "',";
                    createSql = createSql + "group_name='" + group + "',";
                    createSql = createSql + "pool_name='" + pool + "'";
                    if (comment != null && !comment.trim().equals("")) {
                        createSql = createSql + " ,comment='" + comment.trim() + "')";
                    } else {
                        createSql = createSql + ")";
                    }
                    stmt.execute(createSql);
                    if (!GBase8aUtils.isHasDefaultDirective(dbsession, vcName, dataSource, plan)) {
                        group = "default_consumer_group";
                        if (dataSource.isVCCluster()) {
                            createSql = "create resource directive " + vcName + "." + directiveName + "_default";
                        } else {
                            createSql = "create resource directive " + directiveName + "_default";
                        }
                        createSql = createSql + "(plan_name='" + plan + "',";
                        createSql = createSql + "group_name='" + group + "',";
                        createSql = createSql + "pool_name='" + pool + "'";
                        if (comment != null && !comment.trim().equals("")) {
                            createSql = createSql + " ,comment='" + comment.trim() + "')";
                        } else {
                            createSql = createSql + ")";
                        }
                        stmt.execute(createSql);
                    }
                } catch (Exception e) {
                    MessageBox box = new MessageBox(UIUtils.getActiveWorkbenchShell(), 1);
                    box.setMessage("Error");
                    box.setMessage(e.getMessage());
                    box.open();
                    throw new ExecutionException(e.getMessage());
                }
            }
            try {
                ((DBNDatabaseNode) element).getParentNode().getParentNode().refreshNode(dataSource.getMonitor(), dataSource);
            } catch (DBException e) {
                throw new ExecutionException(e.getMessage());
            }
        }
        return null;
    }
}
