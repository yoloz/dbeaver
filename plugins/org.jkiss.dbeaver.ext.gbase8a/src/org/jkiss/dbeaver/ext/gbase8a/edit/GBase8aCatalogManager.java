package org.jkiss.dbeaver.ext.gbase8a.edit;

import org.jkiss.code.Nullable;

import java.util.List;
import java.util.Map;

import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aVC;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.impl.edit.SQLDatabasePersistAction;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.edit.DBEObjectRenamer;
import org.jkiss.dbeaver.model.impl.sql.edit.SQLObjectEditor;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;
import org.jkiss.dbeaver.DBException;


public class GBase8aCatalogManager extends SQLObjectEditor<GBase8aCatalog, GBase8aDataSource> implements DBEObjectRenamer<GBase8aCatalog> {

    @Override
    public long getMakerOptions(DBPDataSource dataSource) {
        return 1L;
    }

    @Override
    @Nullable
    public DBSObjectCache<GBase8aVC, GBase8aCatalog> getObjectsCache(GBase8aCatalog object) {
        return object.getDataSource().getActiveVC().getCatalogCache();
    }

    @Override
    public void renameObject(DBECommandContext commandContext, GBase8aCatalog object, Map<String, Object> options, String newName) throws DBException {
        throw new DBException("Direct database rename is not yet implemented in GBase8a. You should use export/import functions for that.");
    }

    @Override
    protected GBase8aCatalog createDatabaseObject(DBRProgressMonitor monitor, DBECommandContext context, Object container, Object copyFrom, Map<String, Object> options) throws DBException {
//        return new UITask<GBase8aCatalog>() {
//            protected GBase8aCatalog runTask() {
//                GBase8aCreateDatabaseDialog dialog = new GBase8aCreateDatabaseDialog(UIUtils.getActiveWorkbenchShell(), (GBase8aDataSource) container);
//                if (dialog.open() != 0) {
//                    return null;
//                }
//                String schemaName = dialog.getName();
//                GBase8aCatalog newCatalog = new GBase8aCatalog((GBase8aDataSource) container, null, null);
//                newCatalog.setName(schemaName);
//                return newCatalog;
//            }
//        }.execute();
        GBase8aCatalog newCatalog = new GBase8aCatalog((GBase8aDataSource) container, null, null);
        newCatalog.setName("newCatalog");
        return newCatalog;
    }

    @Override
    protected void addObjectCreateActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions,
                                          SQLObjectEditor<GBase8aCatalog, GBase8aDataSource>.ObjectCreateCommand command, Map<String, Object> options) throws DBException {
        GBase8aCatalog catalog = (GBase8aCatalog)command.getObject();
        StringBuilder script = new StringBuilder("CREATE SCHEMA `" + catalog.getName() + "`");
        if (catalog.getDefaultCharset() != null) {
            script.append("\nDEFAULT CHARACTER SET ").append(catalog.getDefaultCharset().getName());
        }

        if (catalog.getDefaultCollation() != null) {
            script.append("\nDEFAULT COLLATE ").append(catalog.getDefaultCollation().getName());
        }

        actions.add(new SQLDatabasePersistAction("Create schema", script.toString()));
    }

    @Override
    protected void addObjectDeleteActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions,
                                          SQLObjectEditor<GBase8aCatalog, GBase8aDataSource>.ObjectDeleteCommand command, Map<String, Object> options) throws DBException {
        actions.add(new SQLDatabasePersistAction("Drop schema", "DROP SCHEMA `" + ((GBase8aCatalog)command.getObject()).getName() + "`"));
    }
}