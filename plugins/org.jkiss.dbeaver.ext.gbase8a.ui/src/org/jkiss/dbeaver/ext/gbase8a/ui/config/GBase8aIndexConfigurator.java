package org.jkiss.dbeaver.ext.gbase8a.ui.config;

import org.eclipse.swt.widgets.Display;
import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTable;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableIndex;
import org.jkiss.dbeaver.ext.gbase8a.ui.views.GBase8aCreateIndexDlg;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEObjectConfigurator;
import org.jkiss.dbeaver.model.impl.DBObjectNameCaseTransformer;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.ui.UITask;

import java.util.Map;

public class GBase8aIndexConfigurator implements DBEObjectConfigurator<GBase8aTableIndex> {

    @Override
    public GBase8aTableIndex configureObject(@NotNull DBRProgressMonitor monitor, @Nullable DBECommandContext commandContext,
                                             @Nullable Object container, @NotNull GBase8aTableIndex index, @NotNull Map<String, Object> options) {
        return UITask.run(() -> {
            GBase8aTable gBase8aTable = (GBase8aTable) container;
            GBase8aCreateIndexDlg dialog = new GBase8aCreateIndexDlg(Display.getCurrent().getActiveShell(),
                    gBase8aTable.getContainer().getVcName(), gBase8aTable.getContainer().getName(), gBase8aTable.getName(),
                    gBase8aTable.getDataSource(), null, monitor);
            if (dialog.open() != 0) {
                return null;
            }
            String indexName = dialog.getIndexName();
            if (indexName == null) {
                return null;
            }
            index.setName(DBObjectNameCaseTransformer.transformObjectName((DBSObject) index, indexName));
            return index;
        });
    }

}
