package org.jkiss.dbeaver.ext.gbase8a.ui.config;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTrigger;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEObjectConfigurator;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSEntityType;
import org.jkiss.dbeaver.ui.UITask;
import org.jkiss.dbeaver.ui.editors.object.struct.EntityEditPage;

import java.util.Map;


public class GBase8aTriggerConfigurator implements DBEObjectConfigurator<GBase8aTrigger> {

    @Override
    public GBase8aTrigger configureObject(@NotNull DBRProgressMonitor monitor, @Nullable DBECommandContext commandContext, @Nullable Object container, @NotNull GBase8aTrigger newTrigger, @NotNull Map<String, Object> options) {
        return UITask.run(() -> {
            EntityEditPage editPage = new EntityEditPage(newTrigger.getDataSource(), DBSEntityType.TRIGGER);
            if (!editPage.edit()) {
                return null;
            }
            newTrigger.setName(editPage.getEntityName());
            newTrigger.setObjectDefinitionText("TRIGGER " + editPage.getEntityName() + "\n" + //$NON-NLS-1$ //$NON-NLS-2$
                    "BEGIN\n" + //$NON-NLS-1$
                    "END;"); //$NON-NLS-1$
            return newTrigger;
        });
    }

}

