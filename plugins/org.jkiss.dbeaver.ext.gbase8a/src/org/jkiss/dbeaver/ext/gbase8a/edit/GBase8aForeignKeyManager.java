package org.jkiss.dbeaver.ext.gbase8a.edit;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTable;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableConstraint;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableForeignKey;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.sql.edit.struct.SQLForeignKeyManager;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.rdb.DBSForeignKeyModifyRule;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.util.List;
import java.util.Map;

public class GBase8aForeignKeyManager extends SQLForeignKeyManager<GBase8aTableForeignKey, GBase8aTable> {

    @Nullable
    @Override
    public DBSObjectCache<? extends DBSObject, GBase8aTableForeignKey> getObjectsCache(GBase8aTableForeignKey object) {
        return object.getParentObject().getForeignKeyCache();
    }

    @Override
    protected GBase8aTableForeignKey createDatabaseObject(DBRProgressMonitor monitor, DBECommandContext context, Object container,
                                                          Object copyFrom, Map<String, Object> options) throws DBException {
        GBase8aTable table = (GBase8aTable) container;
        GBase8aTableForeignKey foreignKey = new GBase8aTableForeignKey(
                table,
                "",
                null,
                getReferencedKey(monitor, table, GBase8aTableConstraint.class, options),
                DBSForeignKeyModifyRule.NO_ACTION,
                DBSForeignKeyModifyRule.NO_ACTION,
                false);
        foreignKey.setName(getNewConstraintName(monitor, foreignKey));
        return foreignKey;
    }

    @Override
    protected void addObjectModifyActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions, ObjectChangeCommand command, Map<String, Object> options) throws DBException {
        addObjectDeleteActions(monitor, executionContext, actions, new ObjectDeleteCommand(command.getObject(), command.getTitle()), options);
        addObjectCreateActions(monitor, executionContext, actions, makeCreateCommand(command.getObject(), options), options);
    }

    @Override
    protected String getDropForeignKeyPattern(GBase8aTableForeignKey foreignKey) {
        return "ALTER TABLE " + PATTERN_ITEM_TABLE + " DROP FOREIGN KEY " + PATTERN_ITEM_CONSTRAINT;
    }
}
