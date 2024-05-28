package org.jkiss.dbeaver.ext.gbase8a.ui.config;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableColumn;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableConstraint;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableConstraintColumn;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEObjectConfigurator;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSEntityAttribute;
import org.jkiss.dbeaver.ui.UITask;
import org.jkiss.dbeaver.ui.editors.object.struct.EditConstraintPage;

import java.util.Map;

/**
 * @author yolo
 */
public class GBase8aConstraintConfigurator implements DBEObjectConfigurator<GBase8aTableConstraint> {

    @Override
    public GBase8aTableConstraint configureObject(@NotNull DBRProgressMonitor monitor, @Nullable DBECommandContext commandContext, @Nullable Object parent, @NotNull GBase8aTableConstraint constraint, @NotNull Map<String, Object> options) {
        return UITask.run(() -> {
            EditConstraintPage editPage = new EditConstraintPage(
                    GBase8aMessages.edit_constraint_manager_title,
                    constraint);
            if (!editPage.edit()) {
                return null;
            }
            constraint.setName(editPage.getConstraintName());
            constraint.setConstraintType(editPage.getConstraintType());
            int colIndex = 1;
            for (DBSEntityAttribute tableColumn : editPage.getSelectedAttributes()) {
                constraint.addColumn(
                        new GBase8aTableConstraintColumn(
                                constraint,
                                (GBase8aTableColumn) tableColumn,
                                colIndex++));
            }
            return constraint;
        });
    }
}
