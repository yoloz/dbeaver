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
import org.jkiss.dbeaver.model.DBConstants;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBPScriptObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEObjectRenamer;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.messages.ModelMessages;
import org.jkiss.dbeaver.model.sql.SQLUtils;
import org.jkiss.dbeaver.model.struct.DBSEntity;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.edit.SQLDatabasePersistAction;
import org.jkiss.dbeaver.model.impl.sql.edit.struct.SQLTableManager;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableIndex;
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

    @Override
    public long getMakerOptions(DBPDataSource dataSource) {
        return super.getMakerOptions(dataSource) | FEATURE_SUPPORTS_COPY;
    }

    @Nullable
    public DBSObjectCache<GBase8aCatalog, GBase8aTableBase> getObjectsCache(GBase8aTableBase object) {
        return object.getContainer().getTableCache();
    }

    @Override
    protected GBase8aTableBase createDatabaseObject(DBRProgressMonitor monitor, DBECommandContext context, Object container, Object copyFrom, Map<String, Object> options) throws DBException {
        final GBase8aTable table;
        GBase8aCatalog catalog = (GBase8aCatalog) container;
        if (copyFrom instanceof DBSEntity) {
            table = new GBase8aTable(monitor, catalog, (DBSEntity) copyFrom);
            table.setName(getNewChildName(monitor, catalog, ((DBSEntity) copyFrom).getName()));
        } else if (copyFrom == null) {
            table = new GBase8aTable(catalog);
            setNewObjectName(monitor, catalog, table);

            final GBase8aTable.AdditionalInfo additionalInfo = table.getAdditionalInfo(monitor);
            additionalInfo.setEngine(catalog.getDataSource().getDefaultEngine());
            additionalInfo.setCharset(catalog.getDefaultCharset());
            additionalInfo.setCollation(catalog.getDefaultCollation());
        } else {
            throw new DBException("Can't create GBase8a table from '" + copyFrom + "'");
        }
        return table;
    }

    @Override
    protected void addObjectModifyActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actionList, ObjectChangeCommand command, Map<String, Object> options) {
        StringBuilder query = new StringBuilder("ALTER TABLE ");
        query.append(command.getObject().getFullyQualifiedName(DBPEvaluationContext.DDL)).append(" ");
        appendTableModifiers(monitor, command.getObject(), command, query, true);
        actionList.add(new SQLDatabasePersistAction(query.toString()));
    }

    @Override
    protected void addStructObjectCreateActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions, StructCreateCommand command, Map<String, Object> options) throws DBException {

        if (CommonUtils.getOption(options, DBPScriptObject.OPTION_INCLUDE_OBJECT_DROP)) {
            final GBase8aTableBase table = command.getObject();
            final String tableName = DBUtils.getEntityScriptName(table, options);
            actions.add(0, new SQLDatabasePersistAction(ModelMessages.model_jdbc_create_new_table, "DROP TABLE IF EXISTS " + tableName));
        }
        super.addStructObjectCreateActions(monitor, executionContext, actions, command, options);
    }

    @Override
    protected void appendTableModifiers(DBRProgressMonitor monitor, GBase8aTableBase tableBase, NestedObjectCommand tableProps, StringBuilder ddl, boolean alter) {
        if (tableBase instanceof GBase8aTable table) {
            try {
                final GBase8aDataSource dataSource = table.getDataSource();
                final GBase8aTable.AdditionalInfo additionalInfo = table.getAdditionalInfo(monitor);
                if ((!table.isPersisted() || tableProps.getProperty("engine") != null) && additionalInfo.getEngine() != null) {
                    ddl.append("\nENGINE=").append(additionalInfo.getEngine().getName());
                }
                if (dataSource.supportsCharsets()
                        && (!table.isPersisted() || tableProps.getProperty("charset") != null)
                        && additionalInfo.getCharset() != null) {
                    ddl.append("\nDEFAULT CHARSET=").append(additionalInfo.getCharset().getName());
                }
                if (dataSource.supportsCollations()
                        && (!table.isPersisted() || tableProps.getProperty("collation") != null)
                        && additionalInfo.getCollation() != null) {
                    ddl.append("\nCOLLATE=").append(additionalInfo.getCollation().getName());
                }
                if ((!table.isPersisted() && table.getDescription() != null) || tableProps.hasProperty(DBConstants.PROP_ID_DESCRIPTION)) {
                    ddl.append("\nCOMMENT=").append(SQLUtils.quoteString(table, CommonUtils.notEmpty(table.getDescription())));
                }
                if ((!table.isPersisted() || tableProps.getProperty("autoIncrement") != null)
                        && additionalInfo.getAutoIncrement() > 0) {
                    ddl.append("\nAUTO_INCREMENT=").append(additionalInfo.getAutoIncrement());
                }
            } catch (DBCException e) {
                log.error(e);
            }
        }
    }

    @Override
    protected boolean isIncludeIndexInDDL(DBRProgressMonitor monitor, DBSTableIndex index) throws DBException {
        return !((GBase8aTableIndex) index).isUniqueKeyIndex(monitor) && super.isIncludeIndexInDDL(monitor, index);
    }

    @Override
    protected void addObjectRenameActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions, ObjectRenameCommand command, Map<String, Object> options) {
        final GBase8aDataSource dataSource = command.getObject().getDataSource();
        boolean alterTable = dataSource.supportsAlterTableRenameSyntax();
        actions.add(new SQLDatabasePersistAction(
                "Rename table",
                (alterTable ? "ALTER" : "RENAME")
                        + " TABLE "
                        + DBUtils.getQuotedIdentifier(command.getObject().getContainer())
                        + "."
                        + DBUtils.getQuotedIdentifier(dataSource, command.getOldName())
                        + (alterTable ? " RENAME" : "")
                        + " TO "
                        + DBUtils.getQuotedIdentifier(command.getObject().getContainer())
                        + "."
                        + DBUtils.getQuotedIdentifier(dataSource, command.getNewName())));
    }

    @NotNull
    @Override
    public Class<? extends DBSObject>[] getChildTypes() {
        return CHILD_TYPES;
    }


    @Override
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

//    public boolean canEditObject(GBase8aTableBase object) {
//        return false;
//    }


    @Override
    public void renameObject(@NotNull DBECommandContext commandContext, @NotNull GBase8aTableBase object, @NotNull Map<String, Object> options, @NotNull String newName) throws DBException {
        processObjectRename(commandContext, object, options, newName);
    }

//    @Override
//    public boolean canRenameObject(GBase8aTableBase object) {
//        return DBEObjectRenamer.super.canRenameObject(object);
//    }
}
