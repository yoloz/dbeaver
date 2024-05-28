package org.jkiss.dbeaver.ext.gbase8a.edit;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTable;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTrigger;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.edit.SQLDatabasePersistAction;
import org.jkiss.dbeaver.model.impl.sql.edit.struct.SQLTriggerManager;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.util.List;
import java.util.Map;


public class GBase8aTriggerManager extends SQLTriggerManager<GBase8aTrigger, GBase8aTable> {

    @Nullable
    public DBSObjectCache<? extends DBSObject, GBase8aTrigger> getObjectsCache(GBase8aTrigger object) {
        return object.getCatalog().getTriggerCache();
    }


    @Override
    protected GBase8aTrigger createDatabaseObject(DBRProgressMonitor monitor, DBECommandContext context, final Object container, Object copyFrom, Map<String, Object> options) {
        GBase8aTable table = (GBase8aTable) container;
        return new GBase8aTrigger(table.getContainer(), table, "NEW_TRIGGER");
    }

    @Override
    protected void createOrReplaceTriggerQuery(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions, GBase8aTrigger trigger, boolean create) {
        if (trigger.isPersisted()) {
            actions.add(new SQLDatabasePersistAction("Drop trigger", "DROP TRIGGER IF EXISTS " + trigger.getFullyQualifiedName(DBPEvaluationContext.DDL)));
        }
        String ddl = "CREATE TRIGGER " + trigger.getFullyQualifiedName(DBPEvaluationContext.DDL) + "\n" +
                trigger.getActionTiming() + " " + trigger.getManipulationType() + "\n" +
                "ON " + trigger.getTable().getFullyQualifiedName(DBPEvaluationContext.DDL) + " FOR EACH ROW\n" +
                trigger.getBody();
        actions.add(new SQLDatabasePersistAction("Create trigger", ddl, true));
    }
}
