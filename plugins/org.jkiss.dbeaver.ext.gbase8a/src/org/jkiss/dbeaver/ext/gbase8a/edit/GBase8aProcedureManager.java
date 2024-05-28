package org.jkiss.dbeaver.ext.gbase8a.edit;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aProcedure;
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


public class GBase8aProcedureManager extends SQLObjectEditor<GBase8aProcedure, GBase8aCatalog> {

    @Override
    public long getMakerOptions(DBPDataSource dataSource) {
        return 4L;
    }

    @Nullable
    public DBSObjectCache<GBase8aCatalog, GBase8aProcedure> getObjectsCache(GBase8aProcedure object) {
        return object.getContainer().getProceduresCache();
    }

    protected void validateObjectProperties(SQLObjectEditor<GBase8aProcedure, GBase8aCatalog>.ObjectChangeCommand command) throws DBException {
        if (CommonUtils.isEmpty(command.getObject().getName())) {
            throw new DBException("Procedure name cannot be empty");
        }
        if (CommonUtils.isEmpty(command.getObject().getDeclaration())) {
            throw new DBException("Procedure body cannot be empty");
        }
    }

    @Override
    protected GBase8aProcedure createDatabaseObject(DBRProgressMonitor monitor, DBECommandContext context, Object container, Object copyFrom, Map<String, Object> options) throws DBException {
        GBase8aProcedure gBase8aProcedure = new GBase8aProcedure((GBase8aCatalog) container);
        gBase8aProcedure.setName("NEW_PROCEDURE");
        return gBase8aProcedure;
    }

    @Override
    protected void addObjectCreateActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions, SQLObjectEditor<GBase8aProcedure, GBase8aCatalog>.ObjectCreateCommand command, Map<String, Object> options) throws DBException {
        createOrReplaceProcedureQuery(actions, command.getObject());
    }

    @Override
    protected void addObjectDeleteActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions, SQLObjectEditor<GBase8aProcedure, GBase8aCatalog>.ObjectDeleteCommand command, Map<String, Object> options) throws DBException {
        actions.add(new SQLDatabasePersistAction("Drop procedure", "DROP " + command.getObject().getProcedureType() + " " + getFullName(command.getObject().getFullyQualifiedName(DBPEvaluationContext.DDL))));
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

    private void createOrReplaceProcedureQuery(List<DBEPersistAction> actions, GBase8aProcedure procedure) {
        actions.add(new SQLDatabasePersistAction("Drop procedure", "DROP " + procedure.getProcedureType() + " IF EXISTS " + procedure.getFullyQualifiedName(DBPEvaluationContext.DDL)));
        actions.add(new SQLDatabasePersistAction("Create procedure", procedure.getDeclaration(), true));
    }

    private void createProcedureQuery(List<DBEPersistAction> actions, GBase8aProcedure procedure) {
        actions.add(new SQLDatabasePersistAction("Create procedure", procedure.getDeclaration(), true));
    }
}
