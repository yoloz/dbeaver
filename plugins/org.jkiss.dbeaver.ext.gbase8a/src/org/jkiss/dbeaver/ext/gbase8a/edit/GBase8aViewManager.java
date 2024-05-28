package org.jkiss.dbeaver.ext.gbase8a.edit;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableBase;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aView;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.edit.SQLDatabasePersistAction;
import org.jkiss.dbeaver.model.impl.sql.edit.SQLObjectEditor;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.utils.GeneralUtils;
import org.jkiss.utils.CommonUtils;

import java.util.List;
import java.util.Map;


public class GBase8aViewManager extends SQLObjectEditor<GBase8aTableBase, GBase8aCatalog> {

    @Override
    public long getMakerOptions(DBPDataSource dataSource) {
        return 4L;
    }

    @Nullable
    public DBSObjectCache<GBase8aCatalog, GBase8aTableBase> getObjectsCache(GBase8aTableBase object) {
        return object.getContainer().getTableCache();
    }

    protected void validateObjectProperties(SQLObjectEditor<GBase8aTableBase, GBase8aCatalog>.ObjectChangeCommand command) throws DBException {
        GBase8aTableBase object = command.getObject();
        if (CommonUtils.isEmpty(object.getName())) {
            throw new DBException("View name cannot be empty");
        }
        if (CommonUtils.isEmpty(((GBase8aView) object).getAdditionalInfo()
                .getDefinition())) {
            throw new DBException("View definition cannot be empty");
        }
    }

    private void createOrReplaceViewQuery(List<DBEPersistAction> actions, GBase8aView view) {
        StringBuilder decl = new StringBuilder(200);
        GeneralUtils.getDefaultLineSeparator();
        actions.add(new SQLDatabasePersistAction(
                "Drop view", "DROP VIEW " + view.getFullyQualifiedName(DBPEvaluationContext.DDL)));

        decl.append(view.getAdditionalInfo().getDefinition());
        actions.add(new SQLDatabasePersistAction("Create view", decl.toString()));
    }


    private void createViewQuery(List<DBEPersistAction> actions, GBase8aView view) {
        StringBuilder decl = new StringBuilder(200);
        String lineSeparator = GeneralUtils.getDefaultLineSeparator();
        decl.append("CREATE VIEW ").append(view.getFullyQualifiedName(DBPEvaluationContext.DDL)).append(lineSeparator)
                .append("AS ").append(view.getAdditionalInfo().getDefinition());

        actions.add(new SQLDatabasePersistAction("Create view", decl.toString()));
    }

    @Override
    protected GBase8aTableBase createDatabaseObject(DBRProgressMonitor monitor, DBECommandContext context, Object container, Object copyFrom, Map<String, Object> options) throws DBException {
        GBase8aView newCatalog = new GBase8aView((GBase8aCatalog) container);
        newCatalog.setName("NewView");
        return newCatalog;
    }

    @Override
    protected void addObjectCreateActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions, SQLObjectEditor<GBase8aTableBase, GBase8aCatalog>.ObjectCreateCommand command, Map<String, Object> options) throws DBException {
        createOrReplaceViewQuery(actions, (GBase8aView) command.getObject());
    }

    @Override
    protected void addObjectDeleteActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions, SQLObjectEditor<GBase8aTableBase, GBase8aCatalog>.ObjectDeleteCommand command, Map<String, Object> options) throws DBException {
        actions.add(new SQLDatabasePersistAction("Drop view", "DROP VIEW " + command.getObject().getFullyQualifiedName(DBPEvaluationContext.DDL)));
    }
}
