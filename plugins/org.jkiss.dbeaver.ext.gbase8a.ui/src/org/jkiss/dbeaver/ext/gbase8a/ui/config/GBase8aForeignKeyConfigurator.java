package org.jkiss.dbeaver.ext.gbase8a.ui.config;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableConstraint;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableForeignKey;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableForeignKeyColumn;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEObjectConfigurator;
import org.jkiss.dbeaver.model.impl.sql.edit.struct.SQLForeignKeyManager;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.rdb.DBSForeignKeyModifyRule;
import org.jkiss.dbeaver.ui.UITask;
import org.jkiss.dbeaver.ui.editors.object.struct.EditForeignKeyPage;
import org.jkiss.utils.CommonUtils;

import java.util.Map;

/**
 * @author yolo
 */
public class GBase8aForeignKeyConfigurator implements DBEObjectConfigurator<GBase8aTableForeignKey> {

    @Override
    public GBase8aTableForeignKey configureObject(@NotNull DBRProgressMonitor monitor, @Nullable DBECommandContext commandContext, @Nullable Object table, @NotNull GBase8aTableForeignKey foreignKey, @NotNull Map<String, Object> options) {
        return UITask.run(() -> {
            EditForeignKeyPage editPage = new EditForeignKeyPage(
                    GBase8aMessages.edit_foreign_key_manager_title,
                    foreignKey,
                    new DBSForeignKeyModifyRule[]{
                            DBSForeignKeyModifyRule.NO_ACTION,
                            DBSForeignKeyModifyRule.CASCADE, DBSForeignKeyModifyRule.RESTRICT,
                            DBSForeignKeyModifyRule.SET_NULL,
                            DBSForeignKeyModifyRule.SET_DEFAULT},
                    options);
            editPage.setSupportsCustomName(true);
            if (!editPage.edit()) {
                return null;
            }
            foreignKey.setReferencedConstraint((GBase8aTableConstraint) editPage.getUniqueConstraint());
//            foreignKey.setName(DBObjectNameCaseTransformer.transformObjectName(foreignKey,
//                    CommonUtils.escapeIdentifier(((GBase8aTable) table).getName()) + "_" +
//                            CommonUtils.escapeIdentifier(editPage.getUniqueConstraint().getParentObject().getName()) + "_FK"));
            String customName = editPage.getName();
            if (CommonUtils.isNotEmpty(customName)) {
                foreignKey.setName(customName);
            } else {
                SQLForeignKeyManager.updateForeignKeyName(monitor, foreignKey);
            }
            foreignKey.setDeleteRule(editPage.getOnDeleteRule());
            int colIndex = 1;
            for (EditForeignKeyPage.FKColumnInfo tableColumn : editPage.getColumns()) {
                foreignKey.addColumn(
                        new GBase8aTableForeignKeyColumn(
                                foreignKey,
                                tableColumn.getOwnColumn(),
                                colIndex++,
                                tableColumn.getRefColumn()));
            }
            SQLForeignKeyManager.updateForeignKeyName(monitor, foreignKey);
            return foreignKey;
        });
    }
}
