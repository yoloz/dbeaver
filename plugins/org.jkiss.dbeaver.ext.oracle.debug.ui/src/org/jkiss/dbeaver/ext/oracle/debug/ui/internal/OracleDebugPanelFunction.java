/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2024 DBeaver Corp and others
 * Copyright (C) 2017-2018 Alexander Fedorov (alexander.fedorov@jkiss.org)
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

package org.jkiss.dbeaver.ext.oracle.debug.ui.internal;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.debug.ui.DBGConfigurationPanel;
import org.jkiss.dbeaver.debug.ui.DBGConfigurationPanelContainer;
import org.jkiss.dbeaver.ext.oracle.debug.OracleDebugConstants;
import org.jkiss.dbeaver.ext.oracle.debug.core.OracleSqlDebugCore;
import org.jkiss.dbeaver.ext.oracle.debug.internal.impl.OracleDebugVariable;
import org.jkiss.dbeaver.ext.oracle.model.OracleProcedureArgument;
import org.jkiss.dbeaver.ext.oracle.model.OracleProcedureStandalone;
import org.jkiss.dbeaver.model.DBIcon;
import org.jkiss.dbeaver.model.DBInfoUtils;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
import org.jkiss.dbeaver.model.navigator.DBNModel;
import org.jkiss.dbeaver.model.navigator.DBNNode;
import org.jkiss.dbeaver.model.runtime.VoidProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSInstance;
import org.jkiss.dbeaver.model.struct.DBSObjectContainer;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureParameter;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.jkiss.dbeaver.ui.DBeaverIcons;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.controls.CSmartCombo;
import org.jkiss.dbeaver.ui.controls.CSmartSelector;
import org.jkiss.dbeaver.ui.controls.CustomTableEditor;
import org.jkiss.utils.CommonUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

public class OracleDebugPanelFunction implements DBGConfigurationPanel {

    private static final int PARAMETERS_TABLE_MAX_HEIGHT = 150;

    private DBGConfigurationPanelContainer container;
    private CSmartCombo<OracleProcedureStandalone> functionCombo;
//    private Text functionText;

    private OracleProcedureStandalone selectedFunction;
    private final Map<DBSProcedureParameter, String> parameterValues = new HashMap<>();
    private Table parametersTable;

    @Override
    public void createPanel(Composite parent, DBGConfigurationPanelContainer container) {
        this.container = container;
        createFunctionGroup(parent);
        createParametersGroup(parent);
    }

    private void createFunctionGroup(Composite parent) {
        Group functionGroup = UIUtils.createControlGroup(parent, "Function", 2, GridData.VERTICAL_ALIGN_BEGINNING, SWT.DEFAULT);
//        functionText = UIUtils.createLabelText(functionGroup, "Function", "", SWT.READ_ONLY);
        UIUtils.createControlLabel(functionGroup, "Function");
        functionCombo = new CSmartSelector<>(functionGroup, SWT.BORDER | SWT.DOWN | SWT.READ_ONLY, new LabelProvider() {
            @Override
            public Image getImage(Object element) {
                return DBeaverIcons.getImage(DBIcon.TREE_PROCEDURE);
            }

            @Override
            public String getText(Object element) {
                if (element == null) {
                    return "N/A";
                }
                return ((OracleProcedureStandalone) element).getFullyQualifiedName(DBPEvaluationContext.UI);
            }
        }) {
            @Override
            protected void dropDown(boolean drop) {
                if (drop) {
                    DBNModel navigatorModel = DBWorkbench.getPlatform().getNavigatorModel();
                    DBNDatabaseNode dsNode = navigatorModel.getNodeByObject(container.getDataSource());
                    if (dsNode != null) {
                        DBNNode curNode = selectedFunction == null ? null : navigatorModel.getNodeByObject(selectedFunction);
                        DBNNode node = DBWorkbench.getPlatformUI().selectObject(
                                parent.getShell(),
                                "Select function to debug",
                                dsNode,
                                curNode,
                                new Class[]{DBSInstance.class, DBSObjectContainer.class, OracleProcedureStandalone.class},
                                new Class[]{OracleProcedureStandalone.class}, null);
                        if (node instanceof DBNDatabaseNode && ((DBNDatabaseNode) node).getObject() instanceof OracleProcedureStandalone) {
                            functionCombo.removeAll();
                            selectedFunction = (OracleProcedureStandalone) ((DBNDatabaseNode) node).getObject();
                            functionCombo.addItem(selectedFunction);
                            functionCombo.select(selectedFunction);
                            updateParametersTable();
                            container.updateDialogState();
                        }
                        parametersTable.setEnabled(selectedFunction != null);
                    }
                }
            }
        };
        functionCombo.addItem(null);
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.widthHint = UIUtils.getFontHeight(functionCombo) * 40 + 10;
        functionCombo.setLayoutData(gd);
    }

    private void createParametersGroup(Composite parent) {
        Group composite = UIUtils.createControlGroup(parent, "Function parameters", 2, GridData.FILL_BOTH, SWT.DEFAULT);
        parametersTable = new Table(composite, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.minimumHeight = PARAMETERS_TABLE_MAX_HEIGHT;
        parametersTable.setLayoutData(gd);
        parametersTable.setHeaderVisible(true);
        parametersTable.setLinesVisible(true);
        parametersTable.addListener(SWT.Resize, arg0 -> {
            Point size = parametersTable.getSize();
            if (size.y > PARAMETERS_TABLE_MAX_HEIGHT) {
                parametersTable.setSize(size.x, PARAMETERS_TABLE_MAX_HEIGHT);
            }
        });

        final TableColumn nameColumn = UIUtils.createTableColumn(parametersTable, SWT.LEFT, "Name");
        nameColumn.setWidth(100);
        final TableColumn valueColumn = UIUtils.createTableColumn(parametersTable, SWT.LEFT, "Value");
        valueColumn.setWidth(120);
        final TableColumn typeColumn = UIUtils.createTableColumn(parametersTable, SWT.LEFT, "Type");
        typeColumn.setWidth(140);
        final TableColumn kindColumn = UIUtils.createTableColumn(parametersTable, SWT.LEFT, "Kind");
        kindColumn.setWidth(40);

        new CustomTableEditor(parametersTable) {
            {
                firstTraverseIndex = 1;
                lastTraverseIndex = 1;
                editOnEnter = false;
            }

            @Override
            protected Control createEditor(Table table, int index, TableItem item) {
                if (index != 1) {
                    return null;
                }
                DBSProcedureParameter param = (DBSProcedureParameter) item.getData();
                Text editor = new Text(table, SWT.BORDER);
                editor.setText(CommonUtils.toString(parameterValues.get(param), ""));
                editor.selectAll();
                return editor;
            }

            @Override
            protected void saveEditorValue(Control control, int index, TableItem item) {
                DBSProcedureParameter param = (DBSProcedureParameter) item.getData();
                String newValue = ((Text) control).getText();
                item.setText(1, newValue);
                parameterValues.put(param, newValue);
                container.updateDialogState();
            }
        };
    }

    @Override
    public void loadConfiguration(DBPDataSourceContainer dataSource, Map<String, Object> configuration) {
        String functionName = CommonUtils.toString(configuration.get(OracleDebugConstants.ATTR_FUNCTION_NAME));
        if (functionName != null && dataSource != null) {
            try {
                container.getRunnableContext().run(true, true, monitor -> {
                    try {
                        selectedFunction = OracleSqlDebugCore.resolveFunction(monitor, dataSource, configuration);
                    } catch (DBException e) {
                        throw new InvocationTargetException(e);
                    }
                });
                container.setWarningMessage(null);
            } catch (InvocationTargetException e) {
                container.setWarningMessage(e.getTargetException().getMessage());
            } catch (InterruptedException e) {
                // ignore
            }
        }
        if (selectedFunction != null) {
            updateParametersTable();
        }
        parametersTable.setEnabled(selectedFunction != null);
        if (selectedFunction != null) {
//            functionText.setText(selectedFunction.getFullyQualifiedName(null));
            functionCombo.addItem(selectedFunction);
            functionCombo.select(selectedFunction);
        }
    }

    private void updateParametersTable() {
        parametersTable.removeAll();
        parameterValues.clear();
        if (selectedFunction != null) {
            try {
                List<OracleProcedureArgument> parameters = (List<OracleProcedureArgument>) selectedFunction.getParameters(new VoidProgressMonitor());
                for (OracleProcedureArgument param : parameters) {
                    parameterValues.put(param, null);
                }
                if (!parameterValues.isEmpty()) {
                    int counter = 0;
                    for (DBSProcedureParameter param : parameterValues.keySet()) {
                        TableItem item = new TableItem(parametersTable, SWT.NONE, counter);
                        item.setData(param);
                        item.setImage(DBeaverIcons.getImage(DBIcon.TREE_ATTRIBUTE));
                        item.setText(0, param.getName());
                        Object value = parameterValues.get(param);
                        item.setText(1, CommonUtils.toString(value, ""));
                        item.setText(2, param.getParameterType().getFullTypeName());
                        item.setText(3, param.getParameterKind().getTitle());
                        counter++;
                    }
                }
                parametersTable.select(0);
                container.setWarningMessage(null);
            } catch (DBException e) {
                container.setWarningMessage(e.getMessage());
            }
        }
    }

    @Override
    public void saveConfiguration(DBPDataSourceContainer dataSource, Map<String, Object> configuration) {
        if (selectedFunction != null && !parameterValues.isEmpty()) {
            TableItem[] items = parametersTable.getItems();
            List<OracleDebugVariable> list = new ArrayList<>(items.length);
            for (TableItem item : items) {
                OracleDebugVariable debugVariable = new OracleDebugVariable();
                debugVariable.setName(item.getText(0));
                debugVariable.setVal(item.getText(1));
                debugVariable.setDataType(item.getText(2));
                debugVariable.setKind(item.getText(3));
                list.add(debugVariable);
            }
            configuration.put(OracleDebugConstants.ATTR_FUNCTION_ARGUMENTS, DBInfoUtils.SECRET_GSON.toJson(list));
        } else {
            configuration.remove(OracleDebugConstants.ATTR_FUNCTION_NAME);
            configuration.remove(OracleDebugConstants.ATTR_SCHEMA_NAME);
            configuration.remove(OracleDebugConstants.ATTR_FUNCTION_ARGUMENTS);
        }
    }

    @Override
    public boolean isValid() {
        return selectedFunction != null;
    }
}
