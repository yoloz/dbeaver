package org.jkiss.dbeaver.ext.gbase8a.edit;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aFunction;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.edit.SQLDatabasePersistAction;
import org.jkiss.dbeaver.model.impl.sql.edit.SQLObjectEditor;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.utils.CommonUtils;

import java.util.List;
import java.util.Map;


public class GBase8aFunctionManager extends SQLObjectEditor<GBase8aFunction, GBase8aCatalog> {

    @Override
    public long getMakerOptions(DBPDataSource dataSource) {
        return 4L;
    }

    @Nullable
    public DBSObjectCache<GBase8aCatalog, GBase8aFunction> getObjectsCache(GBase8aFunction object) {
        return object.getContainer().getFunctionsCache();
    }

    protected void validateObjectProperties(SQLObjectEditor<GBase8aFunction, GBase8aCatalog>.ObjectChangeCommand command) throws DBException {
        if (CommonUtils.isEmpty(command.getObject().getName())) {
            throw new DBException("Function name cannot be empty");
        }
        if (CommonUtils.isEmpty(command.getObject().getDeclaration())) {
            throw new DBException("Function body cannot be empty");
        }
    }

    private void createOrReplaceFunctionQuery(List<DBEPersistAction> actions, GBase8aFunction function) {
        actions.add(new SQLDatabasePersistAction("Drop function", "DROP " + function.getProcedureType() + " IF EXISTS " + function.getFullyQualifiedName(DBPEvaluationContext.DDL)));
        actions.add(new SQLDatabasePersistAction("Create function", function.getDeclaration(), true));
    }

    private void createFunctionQuery(List<DBEPersistAction> actions, GBase8aFunction function) {
        actions.add(new SQLDatabasePersistAction("Create procedure", function.getDeclaration(), true));
    }

    private String getFullName(String porcname) {
        String fullName = "";
        String[] names = porcname.split("\\.");
        byte b;
        int i;
        String[] arrayOfString1;
        for (i = (arrayOfString1 = names).length, b = 0; b < i; ) {
            String name = arrayOfString1[b];
            fullName = fullName + "`" + name + "`.";
            b++;
        }
        fullName = fullName.substring(0, fullName.length() - 1);
        return fullName;
    }

    @Override
    protected GBase8aFunction createDatabaseObject(DBRProgressMonitor monitor, DBECommandContext context, Object container, Object copyFrom, Map<String, Object> options) throws DBException {
        GBase8aFunction gBase8aFunction = new GBase8aFunction((GBase8aCatalog) container);
        gBase8aFunction.setName("NEW_FUNCTION");
        return gBase8aFunction;
    }

    @Override
    protected void addObjectCreateActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions, SQLObjectEditor<GBase8aFunction, GBase8aCatalog>.ObjectCreateCommand command, Map<String, Object> options) throws DBException {
        createOrReplaceFunctionQuery(actions, command.getObject());
    }

    @Override
    protected void addObjectDeleteActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions, SQLObjectEditor<GBase8aFunction, GBase8aCatalog>.ObjectDeleteCommand command, Map<String, Object> options) throws DBException {
        actions.add(new SQLDatabasePersistAction("Drop function", "DROP " + command.getObject().getProcedureType() + " " + getFullName(command.getObject().getFullyQualifiedName(DBPEvaluationContext.DDL))));
    }
}
