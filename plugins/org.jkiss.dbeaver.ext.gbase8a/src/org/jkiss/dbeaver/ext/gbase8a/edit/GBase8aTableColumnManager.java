package org.jkiss.dbeaver.ext.gbase8a.edit;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableBase;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableColumn;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEObjectRenamer;
import org.jkiss.dbeaver.model.edit.DBEObjectReorderer;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.edit.DBECommandAbstract;
import org.jkiss.dbeaver.model.impl.sql.edit.struct.SQLTableColumnManager;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.utils.CommonUtils;

import java.util.List;
import java.util.Map;

public class GBase8aTableColumnManager extends SQLTableColumnManager<GBase8aTableColumn, GBase8aTableBase>
        implements DBEObjectRenamer<GBase8aTableColumn>, DBEObjectReorderer<GBase8aTableColumn> {

    protected final SQLTableColumnManager.ColumnModifier<GBase8aTableColumn> GBaseDataDataTypeModifier;
    protected final SQLTableColumnManager.ColumnModifier<GBase8aTableColumn> CharsetModifier;
    protected final SQLTableColumnManager.ColumnModifier<GBase8aTableColumn> CollationModifier;

    public GBase8aTableColumnManager() {
        this.GBaseDataDataTypeModifier = (monitor, column, sql, command) -> sql.append(' ').append(column.getFullTypeName());

        this.CharsetModifier = (monitor, column, sql, command) -> {
            if (column.getCharset() != null) {
                sql.append(" CHARACTER SET ").append(column.getCharset().getName());
            }
        };

        this.CollationModifier = (monitor, column, sql, command) -> {
            if (column.getCollation() != null) {
                sql.append(" COLLATE ").append(column.getCollation().getName());
            }
        };
    }


    @Nullable
    public DBSObjectCache<? extends DBSObject, GBase8aTableColumn> getObjectsCache(GBase8aTableColumn object) {
        return object.getParentObject().getContainer().getTableCache().getChildrenCache(object.getParentObject());
    }

    protected SQLTableColumnManager.ColumnModifier[] getSupportedModifiers(GBase8aTableColumn column) {
        return new SQLTableColumnManager.ColumnModifier[]{this.GBaseDataDataTypeModifier, this.CharsetModifier, this.CollationModifier, this.DefaultModifier, this.NullNotNullModifier};
    }

    public StringBuilder getNestedDeclaration(DBRProgressMonitor monitor, GBase8aTableBase owner, DBECommandAbstract<GBase8aTableColumn> command, Map<String, Object> options) {
        StringBuilder decl = super.getNestedDeclaration(monitor, owner, command, options);
        GBase8aTableColumn column = command.getObject();
        if (!CommonUtils.isEmpty(column.getExtraInfo())) {
            decl.append(" ").append(column.getExtraInfo());
        }
        if (!CommonUtils.isEmpty(column.getComment())) {
            decl.append(" COMMENT '").append(escapeComment(column.getComment())).append("'");
        }
        return decl;
    }

    private String escapeComment(String comment) {
        return comment.replace("'", "\\'");
    }

//    protected void addObjectModifyActions(List<DBEPersistAction> actionList, SQLObjectEditor<GBase8aTableColumn, GBase8aTableBase>.ObjectChangeCommand command) {
//        GBase8aTableColumn column = command.getObject();
//        actionList.add(
//                new SQLDatabasePersistAction(
//                        "Modify column",
//                        "ALTER TABLE " + column.getTable().getFullyQualifiedName(DBPEvaluationContext.DDL) + " MODIFY COLUMN " + getNestedDeclaration(column.getTable(), command)));
//    }
//
//
//    protected void addObjectRenameActions(List<DBEPersistAction> actions, SQLObjectEditor<GBase8aTableColumn, GBase8aTableBase>.ObjectRenameCommand command) {
//        GBase8aTableColumn column = command.getObject();
//        actions.add(
//                new SQLDatabasePersistAction(
//                        "Rename column",
//                        "ALTER TABLE " + column.getTable().getFullyQualifiedName(DBPEvaluationContext.DDL) + " CHANGE " +
//                                DBUtils.getQuotedIdentifier(column.getDataSource(), command.getOldName()) + " " +
//                                getNestedDeclaration(column.getTable(), command)));
//    }
//
//    protected void addObjectReorderActions(List<DBEPersistAction> actions, SQLObjectEditor<GBase8aTableColumn, GBase8aTableBase>.ObjectReorderCommand command) {
//        GBase8aTableColumn column = command.getObject();
//        String order = "FIRST";
//        if (column.getOrdinalPosition() > 0) {
//            for (GBase8aTableColumn col : command.getObject().getTable().getCachedAttributes()) {
//                if (col.getOrdinalPosition() == column.getOrdinalPosition() - 1) {
//                    order = "AFTER " + DBUtils.getQuotedIdentifier(col);
//                    break;
//                }
//            }
//        }
//        actions.add(
//                new SQLDatabasePersistAction(
//                        "Reorder column",
//                        "ALTER TABLE " + column.getTable().getFullyQualifiedName(DBPEvaluationContext.DDL) + " CHANGE " +
//                                DBUtils.getQuotedIdentifier(command.getObject()) + " " +
//                                getNestedDeclaration(column.getTable(), command) + " " + order));
//    }


    public int getMinimumOrdinalPosition(GBase8aTableColumn object) {
        return 1;
    }

    public int getMaximumOrdinalPosition(GBase8aTableColumn object) {
        return object.getTable().getCachedAttributes().size();
    }


    public void setObjectOrdinalPosition(DBECommandContext commandContext, GBase8aTableColumn object, List<GBase8aTableColumn> siblingObjects, int newPosition) throws DBException {
        processObjectReorder(commandContext, object, siblingObjects, newPosition);
    }


    public boolean canEditObject(GBase8aTableColumn object) {
        return false;
    }

    @Override
    protected GBase8aTableColumn createDatabaseObject(DBRProgressMonitor monitor, DBECommandContext context, Object container, Object copyFrom, Map<String, Object> options) throws DBException {
        return null;
    }

    @Override
    public void renameObject(DBECommandContext commandContext, GBase8aTableColumn object, Map<String, Object> options, String newName) throws DBException {
        processObjectRename(commandContext, object, options, newName);
    }

    @Override
    public boolean canRenameObject(GBase8aTableColumn object) {
        return DBEObjectRenamer.super.canRenameObject(object);
    }
}
