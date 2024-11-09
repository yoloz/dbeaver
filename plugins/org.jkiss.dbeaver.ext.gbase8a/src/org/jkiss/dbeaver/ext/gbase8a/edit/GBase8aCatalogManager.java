package org.jkiss.dbeaver.ext.gbase8a.edit;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;

import java.util.List;
import java.util.Map;

import org.jkiss.dbeaver.ext.gbase8a.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
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
        return FEATURE_SAVE_IMMEDIATELY;
    }

    @Override
    @Nullable
    public DBSObjectCache<GBase8aDataSource, GBase8aCatalog> getObjectsCache(GBase8aCatalog object) {
        return object.getDataSource().getCatalogCache();
    }

    @Override
    protected GBase8aCatalog createDatabaseObject(DBRProgressMonitor monitor, DBECommandContext context, Object container, Object copyFrom, Map<String, Object> options) throws DBException {
        GBase8aDataSource gBase8aDataSource = (GBase8aDataSource) container;
        return new GBase8aCatalog(gBase8aDataSource, null, gBase8aDataSource.getVcName());
    }

    @Override
    protected void addObjectCreateActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions, ObjectCreateCommand command, Map<String, Object> options) {
        final GBase8aCatalog catalog = command.getObject();
        final StringBuilder script = new StringBuilder("CREATE SCHEMA `" + catalog.getName() + "`");
        appendDatabaseModifiers(catalog, script);
        actions.add(new SQLDatabasePersistAction("Create schema", script.toString()));
    }

    protected void addObjectModifyActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actionList, ObjectChangeCommand command, Map<String, Object> options) {
        final GBase8aCatalog catalog = command.getObject();
        final StringBuilder script = new StringBuilder("ALTER DATABASE `" + catalog.getName() + "`");
        appendDatabaseModifiers(catalog, script);
        actionList.add(
                new SQLDatabasePersistAction("Alter database", script.toString())
        );
    }

    private void appendDatabaseModifiers(GBase8aCatalog catalog, StringBuilder script) {
        if (catalog.getDefaultCharset() != null) {
            script.append("\nDEFAULT CHARACTER SET ").append(catalog.getDefaultCharset().getName());
        }
        if (catalog.getDefaultCollation() != null) {
            script.append("\nDEFAULT COLLATE ").append(catalog.getDefaultCollation().getName());
        }
    }

    @Override
    protected void addObjectDeleteActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions, ObjectDeleteCommand command, Map<String, Object> options) {
        actions.add(new SQLDatabasePersistAction("Drop schema", "DROP SCHEMA `" + command.getObject().getName() + "`"));
    }

    @Override
    public boolean canRenameObject(GBase8aCatalog object) {
        return false;
    }

    @Override
    public void renameObject(@NotNull DBECommandContext commandContext, @NotNull GBase8aCatalog object, @NotNull Map<String, Object> options, @NotNull String newName)
            throws DBException {
        throw new DBException(GBase8aMessages.exception_direct_database_rename);
    }
}