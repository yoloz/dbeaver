package org.jkiss.dbeaver.ext.gbase8a.edit;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aConstants;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTable;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableFullIndex;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableIndexColumn;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEObjectRenamer;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.impl.edit.SQLDatabasePersistAction;
import org.jkiss.dbeaver.model.impl.sql.edit.struct.SQLIndexManager;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.rdb.DBSIndexType;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableIndexColumn;

import org.jkiss.utils.CommonUtils;

import java.util.List;
import java.util.Map;


public class GBase8aFullIndexManager extends SQLIndexManager<GBase8aTableFullIndex, GBase8aTable> implements DBEObjectRenamer<GBase8aTableFullIndex> {

    @Nullable
    @Override
    public DBSObjectCache<GBase8aCatalog, GBase8aTableFullIndex> getObjectsCache(GBase8aTableFullIndex object) {
        return object.getTable().getContainer().getFullIndexCache();
    }

    @Override
    protected GBase8aTableFullIndex createDatabaseObject(DBRProgressMonitor monitor, DBECommandContext context,
                                                         final Object container, Object from, Map<String, Object> options) {
        GBase8aTable table = (GBase8aTable) container;
        return new GBase8aTableFullIndex(
                table,
                false,
                null,
                DBSIndexType.OTHER,
                null,
                false);
    }

    protected void appendIndexType(GBase8aTableFullIndex index, StringBuilder decl) {
        DBSIndexType indexType = index.getIndexType();
        if (indexType != GBase8aConstants.INDEX_TYPE_FULLTEXT) {
            decl.append(" USING ").append(indexType.getId());
        }
    }

    protected void appendIndexModifiers(GBase8aTableFullIndex index, StringBuilder decl) {
        if (index.getIndexType() == GBase8aConstants.INDEX_TYPE_FULLTEXT) {
            decl.append(" FULLTEXT");
        } else {
            super.appendIndexModifiers(index, decl);
        }
    }

    protected void appendIndexColumnModifiers(DBRProgressMonitor monitor, StringBuilder decl, DBSTableIndexColumn indexColumn) {
        final String subPart = ((GBase8aTableIndexColumn) indexColumn).getSubPart();
        if (!CommonUtils.isEmpty(subPart)) {
            decl.append(" (").append(subPart).append(")");
        }
        if (!indexColumn.isAscending()) {
            decl.append(" DESC");
        }
    }

    @Override
    protected void addObjectModifyActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actionList, ObjectChangeCommand command, Map<String, Object> options) throws DBException {
        addObjectDeleteActions(monitor, executionContext, actionList, new ObjectDeleteCommand(command.getObject(), command.getTitle()), options);
        addObjectCreateActions(monitor, executionContext, actionList, makeCreateCommand(command.getObject(), options), options);
    }

    @Override
    public void renameObject(@NotNull DBECommandContext commandContext, @NotNull GBase8aTableFullIndex object, @NotNull Map<String, Object> options, @NotNull String newName) throws DBException {
        processObjectRename(commandContext, object, options, newName);
    }

    @Override
    protected void addObjectRenameActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions, ObjectRenameCommand command, Map<String, Object> options) {
        final GBase8aDataSource dataSource = command.getObject().getDataSource();
        actions.add(
                new SQLDatabasePersistAction(
                        "Rename table",
                        "ALTER TABLE " + command.getObject().getTable().getFullyQualifiedName(DBPEvaluationContext.DDL) +
                                "\nRENAME INDEX " + DBUtils.getQuotedIdentifier(dataSource, command.getOldName()) +
                                " TO " + DBUtils.getQuotedIdentifier(dataSource, command.getNewName()))
        );
    }

}
