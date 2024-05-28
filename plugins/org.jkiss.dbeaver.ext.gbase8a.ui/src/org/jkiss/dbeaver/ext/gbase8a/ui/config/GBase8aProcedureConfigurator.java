package org.jkiss.dbeaver.ext.gbase8a.ui.config;

import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aProcedure;
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
public class GBase8aProcedureConfigurator implements DBEObjectConfigurator<GBase8aProcedure> {

    @Override
    public GBase8aProcedure configureObject(DBRProgressMonitor monitor, DBECommandContext commandContext, Object container,
                                           GBase8aProcedure object, Map<String, Object> options) {
        return new UITask<GBase8aProcedure>() {
            @Override
            protected GBase8aProcedure runTask() {
                GBase8aCreateProcedurePage editPage = new GBase8aCreateProcedurePage((DBSObjectContainer) object, DBSProcedureType.PROCEDURE.name());
                if (!editPage.edit()) {
                    return null;
                }
                GBase8aProcedure newProcedure = new GBase8aProcedure((GBase8aCatalog) container);
                newProcedure.setProcedureType(editPage.getProcedureType());
                newProcedure.setName(editPage.getProcedureName());
                return newProcedure;
            }
        }.execute();
    }
}
