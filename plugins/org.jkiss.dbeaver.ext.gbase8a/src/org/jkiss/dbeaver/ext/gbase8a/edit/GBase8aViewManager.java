package org.jkiss.dbeaver.ext.gbase8a.edit;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableBase;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aView;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.impl.sql.edit.struct.SQLTableManager;
import org.jkiss.dbeaver.model.sql.parser.SQLSemanticProcessor;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.edit.SQLDatabasePersistAction;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.utils.GeneralUtils;
import org.jkiss.utils.CommonUtils;

import java.util.List;
import java.util.Map;


public class GBase8aViewManager extends GBase8aTableManager {

    @Nullable
    @Override
    public DBSObjectCache<GBase8aCatalog, GBase8aTableBase> getObjectsCache(GBase8aTableBase object) {
        return object.getContainer().getTableCache();
    }

    protected void validateObjectProperties(DBRProgressMonitor monitor, ObjectChangeCommand command, Map<String, Object> options) throws DBException {
        GBase8aTableBase object = command.getObject();
        if (CommonUtils.isEmpty(object.getName())) {
            throw new DBException("View name cannot be empty");
        }
        if (CommonUtils.isEmpty(((GBase8aView) object).getAdditionalInfo()
                .getDefinition())) {
            throw new DBException("View definition cannot be empty");
        }
    }

    @Override
    protected String getBaseObjectName() {
        return SQLTableManager.BASE_VIEW_NAME;
    }

    @Override
    protected GBase8aTableBase createDatabaseObject(DBRProgressMonitor monitor, DBECommandContext context, Object container, Object copyFrom, Map<String, Object> options) throws DBException {
        GBase8aCatalog catalog = (GBase8aCatalog) container;
        GBase8aView newView = new GBase8aView(catalog);
        setNewObjectName(monitor, catalog, newView);
        return newView;
    }

    @Override
    protected void addStructObjectCreateActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions, StructCreateCommand command, Map<String, Object> options) {
        createOrReplaceViewQuery(actions, (GBase8aView) command.getObject());
    }

    @Override
    protected void addObjectModifyActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actionList, ObjectChangeCommand command, Map<String, Object> options) {
        createOrReplaceViewQuery(actionList, (GBase8aView) command.getObject());
    }

    @Override
    protected void addObjectDeleteActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions, ObjectDeleteCommand command, Map<String, Object> options) {
        actions.add(new SQLDatabasePersistAction("Drop view", "DROP VIEW " + command.getObject().getFullyQualifiedName(DBPEvaluationContext.DDL)));
    }

    private void createOrReplaceViewQuery(List<DBEPersistAction> actions, GBase8aView view) {
        StringBuilder decl = new StringBuilder(200);
        final String lineSeparator = GeneralUtils.getDefaultLineSeparator();
        String viewDDL = view.getAdditionalInfo().getDefinition();
        if (viewDDL == null) {
            viewDDL = "";
        }
        if (!view.isPersisted() && SQLSemanticProcessor.isSelectQuery(view.getDataSource().getSQLDialect(), viewDDL)) {
            if (view.getDataSource().supportsAlterView()) {
                decl.append("CREATE");
            } else {
                decl.append("CREATE OR REPLACE");
            }
            decl.append(" VIEW ").append(view.getFullyQualifiedName(DBPEvaluationContext.DDL)).append(lineSeparator).append("AS ");
        }

        final GBase8aView.CheckOption checkOption = view.getAdditionalInfo().getCheckOption();
        if (checkOption != null && checkOption != GBase8aView.CheckOption.NONE) {
            if (viewDDL.endsWith(";")) {
                viewDDL = viewDDL.substring(0, viewDDL.length() - 1);
            }
            decl.append(viewDDL).append(lineSeparator).append("WITH ").append(checkOption.getDefinitionName()).append(" CHECK OPTION");
        } else {
            decl.append(viewDDL);
        }

        actions.add(new SQLDatabasePersistAction("Create view", decl.toString()) {
            @Override
            public void beforeExecute(DBCSession session) throws DBCException {
                GBase8aView schemaView;
                try {
                    schemaView = DBUtils.findObject(view.getParentObject().getViews(session.getProgressMonitor()), view.getName());
                } catch (DBException e) {
                    throw new DBCException(e, session.getExecutionContext());
                }
                if (schemaView != view) {
                    throw new DBCException("View with name '" + view.getName() + "' already exists. Choose another name");
                }
                super.beforeExecute(session);
            }
        });
    }
}
