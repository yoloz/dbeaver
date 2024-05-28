package org.jkiss.dbeaver.ext.gbase8a.ui.config;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTable;
import org.jkiss.dbeaver.ext.gbase8a.ui.views.GBase8aTableDialog;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEObjectConfigurator;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.ui.UITask;
import org.jkiss.dbeaver.ui.UIUtils;

import java.util.Map;

/**
 * @author yolo
 */
public class GBase8aTableConfigurator implements DBEObjectConfigurator<GBase8aTable> {
    Log log = Log.getLog(GBase8aTableConfigurator.class);

    @Override
    public GBase8aTable configureObject(DBRProgressMonitor monitor, DBECommandContext commandContext, Object container,
                                        GBase8aTable object, Map<String, Object> options) {
        return new UITask<GBase8aTable>() {
            @Override
            protected GBase8aTable runTask() {
                GBase8aCatalog parent = (GBase8aCatalog) container;
                try {
                    GBase8aTable table = new GBase8aTable(parent);
//                    table.setTableName(monitor, (DBSObjectContainer) parent, (JDBCTable) table);
                    GBase8aTable.AdditionalInfo additionalInfo = table.getAdditionalInfo(monitor);
                    additionalInfo.setEngine(parent.getDataSource().getDefaultEngine());
                    additionalInfo.setCharset(parent.getDefaultCharset());
                    additionalInfo.setCollation(parent.getDefaultCollation());
                    GBase8aTableDialog dialog = new GBase8aTableDialog(UIUtils.getActiveWorkbenchShell(), monitor, parent.getDataSource(), true);
                    dialog.setGbase8aTable(table);
                    if (dialog.open() != 0) {
                        return null;
                    }
                    return table;
                } catch (DBException e) {
                    log.error(e);
                }
                return null;
            }
        }.execute();
    }
}
