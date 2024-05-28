package org.jkiss.dbeaver.ext.gbase8a.edit;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTable;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableForeignKey;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.sql.edit.struct.SQLForeignKeyManager;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.rdb.DBSForeignKeyModifyRule;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.util.Map;

public class GBase8aForeignKeyManager extends SQLForeignKeyManager<GBase8aTableForeignKey, GBase8aTable> {

    @Override
    public DBSObjectCache<? extends DBSObject, GBase8aTableForeignKey> getObjectsCache(GBase8aTableForeignKey object) {
        return object.getParentObject().getForeignKeyCache();
    }

    @Override
    protected String getDropForeignKeyPattern(GBase8aTableForeignKey foreignKey) {
        return "ALTER TABLE %TABLE% DROP FOREIGN KEY %CONSTRAINT%";
    }

    @Override
    protected GBase8aTableForeignKey createDatabaseObject(DBRProgressMonitor monitor, DBECommandContext context, Object container,
                                                          Object copyFrom, Map<String, Object> options) throws DBException {
        GBase8aTable table = (GBase8aTable) container;
        return new GBase8aTableForeignKey(
                table,
                "",
                null,
                null,
                DBSForeignKeyModifyRule.NO_ACTION,
                DBSForeignKeyModifyRule.NO_ACTION,
                false);
    }
}
