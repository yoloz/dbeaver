package org.jkiss.dbeaver.ext.gbase8a.edit;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTable;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableBase;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableColumn;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableConstraint;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableForeignKey;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableFullIndex;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableIndex;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEObjectRenamer;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.edit.SQLDatabasePersistAction;
import org.jkiss.dbeaver.model.impl.sql.edit.SQLObjectEditor;
import org.jkiss.dbeaver.model.impl.sql.edit.struct.SQLTableManager;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.utils.CommonUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GBase8aTableManager extends SQLTableManager<GBase8aTableBase, GBase8aCatalog> implements DBEObjectRenamer<GBase8aTableBase> {

    private static final Class<? extends DBSObject>[] CHILD_TYPES = CommonUtils.array(
            GBase8aTableColumn.class,
            GBase8aTableConstraint.class,
            GBase8aTableForeignKey.class,
            GBase8aTableIndex.class);


    @Nullable
    public DBSObjectCache<GBase8aCatalog, GBase8aTableBase> getObjectsCache(GBase8aTableBase object) {
        return object.getContainer().getTableCache();
    }

    protected void addObjectRenameActions(List<DBEPersistAction> actions, SQLObjectEditor<GBase8aTableBase, GBase8aCatalog>.ObjectRenameCommand command) {
        GBase8aDataSource dataSource = command.getObject().getDataSource();
        actions.add(
                new SQLDatabasePersistAction(
                        "Rename table",
                        "RENAME TABLE " + command.getObject().getFullyQualifiedName(DBPEvaluationContext.DDL) +
                                " TO " + DBUtils.getQuotedIdentifier(command.getObject().getContainer()) + "."
                                + DBUtils.getQuotedIdentifier(dataSource, command.getNewName())));
    }


    @NotNull
    public Class<? extends DBSObject>[] getChildTypes() {
        return CHILD_TYPES;
    }


    public Collection<? extends DBSObject> getChildObjects(DBRProgressMonitor monitor, GBase8aTableBase object, Class<? extends DBSObject> childType) throws DBException {
        if (childType == GBase8aTableColumn.class)
            return object.getAttributes(monitor);
        if (childType == GBase8aTableConstraint.class)
            return object.getConstraints(monitor);
        if (childType == GBase8aTableForeignKey.class)
            return object.getAssociations(monitor);
        if (childType == GBase8aTableIndex.class || childType == GBase8aTableFullIndex.class) {
            return object.getIndexes(monitor);
        }
        return null;
    }

    public boolean canEditObject(GBase8aTableBase object) {
        return false;
    }

    @Override
    protected GBase8aTableBase createDatabaseObject(DBRProgressMonitor monitor, DBECommandContext context, Object container, Object copyFrom, Map<String, Object> options) throws DBException {
        GBase8aCatalog gBase8aCatalog = (GBase8aCatalog) container;
        GBase8aTable table = new GBase8aTable(gBase8aCatalog); //$NON-NLS-1$
        setNewObjectName(monitor, gBase8aCatalog, table);
        return table;
    }

    @Override
    public void renameObject(DBECommandContext commandContext, GBase8aTableBase object, Map<String, Object> options, String newName) throws DBException {
        processObjectRename(commandContext, object, options, newName);
    }

    @Override
    public boolean canRenameObject(GBase8aTableBase object) {
        return DBEObjectRenamer.super.canRenameObject(object);
    }
}
