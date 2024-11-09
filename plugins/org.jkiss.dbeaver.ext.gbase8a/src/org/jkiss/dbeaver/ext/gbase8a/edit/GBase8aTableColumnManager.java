package org.jkiss.dbeaver.ext.gbase8a.edit;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aConstants;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTable;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableBase;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableColumn;
import org.jkiss.dbeaver.model.DBPDataKind;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEObjectRenamer;
import org.jkiss.dbeaver.model.edit.DBEObjectReorderer;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.impl.edit.SQLDatabasePersistAction;
import org.jkiss.dbeaver.model.sql.SQLUtils;
import org.jkiss.dbeaver.model.struct.DBSDataType;
import org.jkiss.dbeaver.model.struct.DBSEntityAttribute;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.edit.DBECommandAbstract;
import org.jkiss.dbeaver.model.impl.sql.edit.struct.SQLTableColumnManager;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.utils.CommonUtils;

import java.sql.Types;
import java.util.List;
import java.util.Map;

public class GBase8aTableColumnManager extends SQLTableColumnManager<GBase8aTableColumn, GBase8aTableBase>
        implements DBEObjectRenamer<GBase8aTableColumn>, DBEObjectReorderer<GBase8aTableColumn> {

    private final ColumnModifier<GBase8aTableColumn> GBaseDataDataTypeModifier = (monitor, column, sql, command) -> {
        sql.append(' ');
        String fullTypeName = column.getFullTypeName();
        String typeName = column.getTypeName();
        if (!fullTypeName.contains("(") && (typeName.equalsIgnoreCase(GBase8aConstants.TYPE_VARCHAR))) {
            sql.append(typeName);
            String modifiers = SQLUtils.getColumnTypeModifiers(column.getDataSource(), column, typeName, column.getDataKind());
            if (modifiers != null) {
                sql.append(modifiers);
            }
        } else {
            sql.append(fullTypeName);
        }
    };

    private final ColumnModifier<GBase8aTableColumn> CharsetModifier = (monitor, column, sql, command) -> {
        if (column.getDataKind() == DBPDataKind.STRING && column.getCharset() != null) {
            sql.append(" CHARACTER SET ").append(column.getCharset().getName());
        }
    };

    private final ColumnModifier<GBase8aTableColumn> CollationModifier = (monitor, column, sql, command) -> {
        if (column.getDataKind() == DBPDataKind.STRING && column.getCollation() != null) {
            sql.append(" COLLATE ").append(column.getCollation().getName());
        }
    };

    @Nullable
    @Override
    public DBSObjectCache<? extends DBSObject, GBase8aTableColumn> getObjectsCache(GBase8aTableColumn object) {
        return object.getParentObject().getContainer().getTableCache().getChildrenCache(object.getParentObject());
    }

    protected ColumnModifier[] getSupportedModifiers(GBase8aTableColumn column, Map<String, Object> options) {
        return new ColumnModifier[]{GBaseDataDataTypeModifier, CharsetModifier, CollationModifier, DefaultModifier, NullNotNullModifier};
    }

    @Override
    public StringBuilder getNestedDeclaration(@NotNull DBRProgressMonitor monitor, @NotNull GBase8aTableBase owner, @NotNull DBECommandAbstract<GBase8aTableColumn> command, @NotNull Map<String, Object> options) {
        StringBuilder decl = super.getNestedDeclaration(monitor, owner, command, options);
        GBase8aTableColumn column = command.getObject();
        if (!CommonUtils.isEmpty(column.getExtraInfo())) {
            decl.append(" ").append(column.getExtraInfo());
        }
        if (!CommonUtils.isEmpty(column.getComment())) {
            decl.append(" COMMENT ").append(SQLUtils.quoteString(column, column.getComment()));
        }
        return decl;
    }

    @Override
    protected GBase8aTableColumn createDatabaseObject(final DBRProgressMonitor monitor, final DBECommandContext context,
                                                      final Object container, Object copyFrom, Map<String, Object> options) throws DBException {
        GBase8aTable table = (GBase8aTable) container;
        GBase8aTableColumn column;
        if (copyFrom instanceof DBSEntityAttribute) {
            column = new GBase8aTableColumn(table, (DBSEntityAttribute) copyFrom);
        } else {
            column = new GBase8aTableColumn(table);
            DBSDataType columnType = findBestDataType(table, "varchar");
            column.setName(getNewColumnName(monitor, context, table));
            final String typeName = columnType == null ? "integer" : columnType.getName().toLowerCase();
            column.setTypeName(typeName);
            column.setMaxLength(columnType != null && columnType.getDataKind() == DBPDataKind.STRING ? 100 : 0);
            column.setValueType(columnType == null ? Types.INTEGER : columnType.getTypeID());
            column.setOrdinalPosition(table.getCachedAttributes().size() + 1);
            if (columnType != null && columnType.getDataKind() == DBPDataKind.STRING) {
                column.setFullTypeName(typeName + "(" + column.getMaxLength() + ")");
            } else {
                column.setFullTypeName(typeName);
            }
        }
        return column;
    }

    @Override
    protected void addObjectModifyActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actionList, ObjectChangeCommand command, Map<String, Object> options) {
        final GBase8aTableColumn column = command.getObject();
        actionList.add(new SQLDatabasePersistAction("Modify column", "ALTER TABLE "
                + column.getTable().getFullyQualifiedName(DBPEvaluationContext.DDL)
                + " MODIFY COLUMN "
                + getNestedDeclaration(monitor, column.getTable(), command, options)));
    }

    @Override
    public void renameObject(@NotNull DBECommandContext commandContext, @NotNull GBase8aTableColumn object, @NotNull Map<String, Object> options, @NotNull String newName) throws DBException {
        processObjectRename(commandContext, object, options, newName);
    }

    @Override
    protected void addObjectRenameActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions, ObjectRenameCommand command, Map<String, Object> options) {
        final GBase8aTableColumn column = command.getObject();
        actions.add(new SQLDatabasePersistAction("Rename column", "ALTER TABLE "
                + column.getTable().getFullyQualifiedName(DBPEvaluationContext.DDL)
                + " CHANGE "
                + DBUtils.getQuotedIdentifier(column.getDataSource(), command.getOldName())
                + " "
                + getNestedDeclaration(monitor, column.getTable(), command, options)));
    }

    @Override
    protected void addObjectReorderActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions, ObjectReorderCommand command, Map<String, Object> options) {
        final GBase8aTableColumn column = command.getObject();
        String order = "FIRST";
        if (column.getOrdinalPosition() > 0) {
            for (GBase8aTableColumn col : command.getObject().getTable().getCachedAttributes()) {
                if (col.getOrdinalPosition() == column.getOrdinalPosition() - 1) {
                    order = "AFTER " + DBUtils.getQuotedIdentifier(col);
                    break;
                }
            }
        }
        actions.add(new SQLDatabasePersistAction("Reorder column", "ALTER TABLE "
                + column.getTable().getFullyQualifiedName(DBPEvaluationContext.DDL)
                + " CHANGE "
                + DBUtils.getQuotedIdentifier(command.getObject())
                + " "
                + getNestedDeclaration(monitor, column.getTable(), command, options) + " " + order));
    }

    public int getMinimumOrdinalPosition(GBase8aTableColumn object) {
        return 1;
    }

    public int getMaximumOrdinalPosition(GBase8aTableColumn object) {
        return object.getTable().getCachedAttributes().size();
    }


    public void setObjectOrdinalPosition(DBECommandContext commandContext, GBase8aTableColumn object, List<GBase8aTableColumn> siblingObjects, int newPosition) throws DBException {
        processObjectReorder(commandContext, object, siblingObjects, newPosition);
    }

//
//    public boolean canEditObject(GBase8aTableColumn object) {
//        return false;
//    }
//
//    @Override
//    public boolean canRenameObject(GBase8aTableColumn object) {
//        return DBEObjectRenamer.super.canRenameObject(object);
//    }
}
