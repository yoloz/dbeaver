package org.jkiss.dbeaver.ext.gbase8a.edit;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;

import org.jkiss.dbeaver.ext.gbase8a.GBase8aConstants;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTable;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableConstraint;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.impl.edit.DBECommandAbstract;
import org.jkiss.dbeaver.model.impl.sql.edit.struct.SQLConstraintManager;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;
import org.jkiss.dbeaver.model.struct.DBSEntityConstraintType;

import java.util.Map;

/**
 * GBase8a constraint manager
 */
public class GBase8aConstraintManager extends SQLConstraintManager<GBase8aTableConstraint, GBase8aTable> {

    @Nullable
    @Override
    public DBSObjectCache<GBase8aCatalog, GBase8aTableConstraint> getObjectsCache(GBase8aTableConstraint object) {
        return object.getTable().getContainer().getConstraintCache();
    }

    @Override
    protected GBase8aTableConstraint createDatabaseObject(
            DBRProgressMonitor monitor, DBECommandContext context, final Object container,
            Object from, Map<String, Object> options) {
        return new GBase8aTableConstraint(
                (GBase8aTable) container,
                "NewConstraint",
                null,
                DBSEntityConstraintType.PRIMARY_KEY,
                false);
    }

    @NotNull
    @Override
    protected String getAddConstraintTypeClause(GBase8aTableConstraint constraint) {
        if (constraint.getConstraintType() == DBSEntityConstraintType.UNIQUE_KEY) {
            return GBase8aConstants.CONSTRAINT_UNIQUE;
        }
        return super.getAddConstraintTypeClause(constraint);
    }

    @Override
    protected String getDropConstraintPattern(GBase8aTableConstraint constraint) {
        if (constraint.getConstraintType() == DBSEntityConstraintType.PRIMARY_KEY) {
            return "ALTER TABLE " + PATTERN_ITEM_TABLE + " DROP PRIMARY KEY";
        }
        return "ALTER TABLE " + PATTERN_ITEM_TABLE + " DROP KEY " + PATTERN_ITEM_CONSTRAINT;
    }

    @Override
    protected void appendConstraintDefinition(StringBuilder decl, DBECommandAbstract<GBase8aTableConstraint> command) {
        super.appendConstraintDefinition(decl, command);
    }
}
