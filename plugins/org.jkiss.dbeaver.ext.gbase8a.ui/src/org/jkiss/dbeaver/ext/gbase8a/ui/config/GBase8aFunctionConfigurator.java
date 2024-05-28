package org.jkiss.dbeaver.ext.gbase8a.ui.config;

import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aFunction;
import org.jkiss.dbeaver.ext.gbase8a.ui.views.GBase8aCreateProcedurePage;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEObjectConfigurator;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObjectContainer;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureType;
import org.jkiss.dbeaver.ui.UITask;

import java.util.Map;

/**
 * @author yolo
 */
public class GBase8aFunctionConfigurator implements DBEObjectConfigurator<GBase8aFunction> {

    @Override
    public GBase8aFunction configureObject(DBRProgressMonitor monitor, DBECommandContext commandContext, Object container,
                                           GBase8aFunction object, Map<String, Object> options) {
        return new UITask<GBase8aFunction>() {
            @Override
            protected GBase8aFunction runTask() {
                GBase8aCreateProcedurePage editPage = new GBase8aCreateProcedurePage((DBSObjectContainer) object, DBSProcedureType.FUNCTION.name());
                if (!editPage.edit()) {
                    return null;
                }
                GBase8aFunction newProcedure = new GBase8aFunction((GBase8aCatalog) container);
                newProcedure.setProcedureType(editPage.getProcedureType());
                newProcedure.setName(editPage.getProcedureName());
                return newProcedure;
            }
        }.execute();
    }
}
