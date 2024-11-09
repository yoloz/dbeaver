/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2024 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ext.gbase8a.edit;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aEvent;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aExecutionContext;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBPScriptObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEPersistAction;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.impl.edit.SQLDatabasePersistAction;
import org.jkiss.dbeaver.model.impl.sql.edit.SQLObjectEditor;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GBase8aEventManager extends SQLObjectEditor<GBase8aEvent, GBase8aCatalog> {

    public DBSObjectCache<GBase8aCatalog, GBase8aEvent> getObjectsCache(GBase8aEvent object) {
        return object.getCatalog().getEventCache();
    }

    @Override
    public long getMakerOptions(DBPDataSource dataSource) {
        return FEATURE_EDITOR_ON_CREATE;
    }

    @Nullable
    @Override
    protected GBase8aEvent createDatabaseObject(DBRProgressMonitor monitor, DBECommandContext context, Object container, Object copyFrom, Map<String, Object> options) {
        return new GBase8aEvent((GBase8aCatalog) container, "NewEvent");
    }

    @Override
    protected void addObjectCreateActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions, ObjectCreateCommand command, Map<String, Object> options) {
        final GBase8aEvent event = command.getObject();
        final StringBuilder script = new StringBuilder();
        try {
            script.append(event.getObjectDefinitionText(monitor, options));
        } catch (DBException e) {
            log.error(e);
        }

        makeEventActions(actions, executionContext, event, false, script.toString());
    }

    @Override
    protected void addObjectModifyActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actionList, ObjectChangeCommand command, Map<String, Object> options) {
        final GBase8aEvent event = command.getObject();
        final StringBuilder script = new StringBuilder();
        options = new LinkedHashMap<>(options);
        options.put(DBPScriptObject.OPTION_OBJECT_ALTER, true);
        try {
            script.append(event.getObjectDefinitionText(monitor, options));
        } catch (DBException e) {
            log.error(e);
        }
        String ddlText = script.toString();
        if (ddlText.startsWith("CREATE ") || ddlText.startsWith("create ")) {
            ddlText = "ALTER " + ddlText.substring(7);
        }

        makeEventActions(actionList, executionContext, event, true, ddlText);
    }

    private void makeEventActions(List<DBEPersistAction> actionList, DBCExecutionContext executionContext, GBase8aEvent event, boolean alter, String ddlText) {
        GBase8aCatalog curCatalog = ((GBase8aExecutionContext) executionContext).getDefaultCatalog();
        if (curCatalog != event.getCatalog()) {
            actionList.add(new SQLDatabasePersistAction("Set current schema ", "USE " + DBUtils.getQuotedIdentifier(event.getCatalog()), false));
        }
        actionList.add(new SQLDatabasePersistAction(alter ? "Alter event" : "Create event", ddlText));
        if (curCatalog != null && curCatalog != event.getCatalog()) {
            actionList.add(new SQLDatabasePersistAction("Set current schema ", "USE " + DBUtils.getQuotedIdentifier(curCatalog), false));
        }
    }

    @Override
    protected void addObjectDeleteActions(DBRProgressMonitor monitor, DBCExecutionContext executionContext, List<DBEPersistAction> actions, ObjectDeleteCommand command, Map<String, Object> options) {
        actions.add(new SQLDatabasePersistAction("Drop event", "DROP EVENT " + command.getObject().getFullyQualifiedName(DBPEvaluationContext.DDL)));
    }

}
