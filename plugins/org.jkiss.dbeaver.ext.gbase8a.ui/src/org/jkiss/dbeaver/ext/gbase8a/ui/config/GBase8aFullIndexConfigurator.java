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

package org.jkiss.dbeaver.ext.gbase8a.ui.config;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aConstants;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableColumn;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableFullIndex;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableFullIndexColumn;
import org.jkiss.dbeaver.ext.gbase8a.ui.internal.GBase8aUIMessages;
import org.jkiss.dbeaver.model.DBPDataKind;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBEObjectConfigurator;
import org.jkiss.dbeaver.model.impl.DBObjectNameCaseTransformer;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSEntityAttribute;
import org.jkiss.dbeaver.ui.UITask;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.editors.object.struct.EditIndexPage;
import org.jkiss.utils.CommonUtils;

import java.util.List;
import java.util.Map;

/**
 * MySQL index configurator
 */
public class GBase8aFullIndexConfigurator implements DBEObjectConfigurator<GBase8aTableFullIndex> {


    @Override
    public GBase8aTableFullIndex configureObject(@NotNull DBRProgressMonitor monitor, @Nullable DBECommandContext commandContext,
                                             @Nullable Object parent, @NotNull GBase8aTableFullIndex index, @NotNull Map<String, Object> options) {
        return UITask.run(() -> {
            GBase8aEditFullIndexPage editPage = new GBase8aEditFullIndexPage(index);
            if (!editPage.edit()) {
                return null;
            }

            StringBuilder idxName = new StringBuilder(64);
            idxName.append(CommonUtils.escapeIdentifier(index.getParentObject().getName()));
            int colIndex = 1;
            for (DBSEntityAttribute tableColumn : editPage.getSelectedAttributes()) {
                if (colIndex == 1) {
                    idxName.append("_").append(CommonUtils.escapeIdentifier(tableColumn.getName())); //$NON-NLS-1$
                }
                Integer length = (Integer) editPage.getAttributeProperty(tableColumn, GBase8aEditFullIndexPage.PROP_LENGTH);
                index.addColumn(
                        new GBase8aTableFullIndexColumn(
                                index,
                                (GBase8aTableColumn) tableColumn,
                                colIndex++,
                                !Boolean.TRUE.equals(editPage.getAttributeProperty(tableColumn, EditIndexPage.PROP_DESC)),
                                false,
                                length == null ? null : String.valueOf(length)));
            }
            idxName.append("_IDX"); //$NON-NLS-1$
            index.setName(DBObjectNameCaseTransformer.transformObjectName(index, idxName.toString()));

            index.setName(idxName.toString());
            index.setIndexType(editPage.getIndexType());
            index.setUnique(editPage.isUnique());

            return index;
        });
    }

    private static class GBase8aEditFullIndexPage extends EditIndexPage {

        public static final String PROP_LENGTH = "length";

        private int lengthColumnIndex;

        GBase8aEditFullIndexPage(GBase8aTableFullIndex index) {
            super(GBase8aUIMessages.edit_index_manager_title, index, List.of(GBase8aConstants.INDEX_TYPE_FULLTEXT));
        }

        @Override
        protected void createAttributeColumns(Table columnsTable) {
            super.createAttributeColumns(columnsTable);

            TableColumn colDesc = UIUtils.createTableColumn(columnsTable, SWT.NONE, GBase8aMessages.table_column_length);
            colDesc.setToolTipText(GBase8aMessages.table_column_length_tooltip);
        }

        @Override
        protected int fillAttributeColumns(DBSEntityAttribute attribute, AttributeInfo attributeInfo, TableItem columnItem) {
            lengthColumnIndex = super.fillAttributeColumns(attribute, attributeInfo, columnItem) + 1;
            Integer length = (Integer) attributeInfo.getProperty(PROP_LENGTH);
            columnItem.setText(lengthColumnIndex, length == null ? "" : length.toString());

            return lengthColumnIndex;
        }

        @Override
        protected Control createCellEditor(Table table, int index, TableItem item, AttributeInfo attributeInfo) {
            if (index == lengthColumnIndex && attributeInfo.getAttribute().getDataKind() == DBPDataKind.STRING) {
                Integer length = (Integer) attributeInfo.getProperty(PROP_LENGTH);
                Spinner spinner = new Spinner(table, SWT.BORDER);
                spinner.setMinimum(0);
                spinner.setMaximum((int) attributeInfo.getAttribute().getMaxLength());
                if (length != null) {
                    spinner.setSelection(length);
                }
                return spinner;
            }
            return super.createCellEditor(table, index, item, attributeInfo);
        }

        @Override
        protected void saveCellValue(Control control, int index, TableItem item, AttributeInfo attributeInfo) {
            if (index == lengthColumnIndex) {
                Spinner spinner = (Spinner) control;
                int length = spinner.getSelection();
                item.setText(index, length <= 0 ? "" : String.valueOf(length));
                if (length <= 0) {
                    attributeInfo.setProperty(PROP_LENGTH, null);
                } else {
                    attributeInfo.setProperty(PROP_LENGTH, length);
                }
            } else {
                super.saveCellValue(control, index, item, attributeInfo);
            }
        }
    }

}
