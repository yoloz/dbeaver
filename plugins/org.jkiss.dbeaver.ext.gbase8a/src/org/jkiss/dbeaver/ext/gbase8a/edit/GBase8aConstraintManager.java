package org.jkiss.dbeaver.ext.gbase8a.edit;

import org.jkiss.code.Nullable;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTable;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableConstraint;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.impl.sql.edit.struct.SQLConstraintManager;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;
import org.jkiss.dbeaver.model.struct.DBSEntityConstraintType;

import java.util.Map;

public class GBase8aConstraintManager extends SQLConstraintManager<GBase8aTableConstraint, GBase8aTable> {

    @Nullable
    @SuppressWarnings("unchecked")
    public DBSObjectCache<GBase8aCatalog, GBase8aTableConstraint> getObjectsCache(GBase8aTableConstraint object) {
        return object.getTable().getContainer().getConstraintCache();
    }

    @Override
    protected String getDropConstraintPattern(GBase8aTableConstraint constraint) {
        if (constraint.getConstraintType() == DBSEntityConstraintType.PRIMARY_KEY) {
            return "ALTER TABLE %TABLE% DROP PRIMARY KEY";
        }
        return "ALTER TABLE %TABLE% DROP KEY %CONSTRAINT%";
    }

    @Override
    protected GBase8aTableConstraint createDatabaseObject(DBRProgressMonitor monitor, DBECommandContext context, Object container,
                                                          Object copyFrom, Map<String, Object> options) throws DBException {
        return new GBase8aTableConstraint(
                (GBase8aTable) container,
                "",
                null,
                DBSEntityConstraintType.UNIQUE_KEY,
                false);
    }
}
