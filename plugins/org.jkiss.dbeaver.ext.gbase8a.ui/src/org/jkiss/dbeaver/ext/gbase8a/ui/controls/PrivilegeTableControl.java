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
package org.jkiss.dbeaver.ext.gbase8a.ui.controls;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aConstants;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aPrivilege;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aGrant;
import org.jkiss.dbeaver.ext.gbase8a.ui.internal.GBase8aUIMessages;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.controls.CustomCheckboxCellEditor;
import org.jkiss.dbeaver.ui.controls.ListContentProvider;
import org.jkiss.dbeaver.ui.controls.ViewerColumnController;
import org.jkiss.utils.ArrayUtils;
import org.jkiss.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Privilege table control
 */
public class PrivilegeTableControl extends Composite {

    private boolean isStatic;

    private TableViewer tableViewer;
    private ViewerColumnController<Object, Object> columnsController;
    private Table privTable;

    private List<GBase8aPrivilege> privileges;
    private List<GBase8aObjectPrivilege> currentPrivileges = new ArrayList<>();

    public PrivilegeTableControl(Composite parent, String title, boolean isStatic) {
        super(parent, SWT.NONE);
        this.isStatic = isStatic;
        GridLayout gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.verticalSpacing = 0;
        gl.horizontalSpacing = 0;
        setLayout(gl);

        Composite privsGroup = UIUtils.createControlGroup(this, title, 1, GridData.FILL_BOTH, 0);
        GridData gd = (GridData) privsGroup.getLayoutData();
        gd.horizontalSpan = 2;

        tableViewer = new TableViewer(privsGroup, SWT.BORDER | SWT.UNDERLINE_SINGLE | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);

        privTable = tableViewer.getTable();
        privTable.setHeaderVisible(true);
        privTable.setLinesVisible(true);
        gd = new GridData(GridData.FILL_BOTH);
        gd.minimumWidth = 300;
        privTable.setLayoutData(gd);

        columnsController = new ViewerColumnController<>("GBase8aPrivilegesEditor", tableViewer); //$NON-NLS-1$

        columnsController.addColumn(GBase8aUIMessages.controls_privilege_table_column_privilege_name, GBase8aUIMessages.controls_privilege_table_column_privilege_name_tip, SWT.LEFT, true, true, new CellLabelProvider() {
            @Override
            public void update(ViewerCell cell) {
                Object element = cell.getElement();
                if (element instanceof GBase8aObjectPrivilege) {
                    cell.setText(((GBase8aObjectPrivilege) element).privilege.getName());
                }
            }
        });

        columnsController.addBooleanColumn(GBase8aUIMessages.controls_privilege_table_column_privilege_status, GBase8aUIMessages.controls_privilege_table_column_privilege_status_tip, SWT.CENTER, true, true, item -> {
            if (item instanceof GBase8aObjectPrivilege) {
                return ((GBase8aObjectPrivilege) item).enabled;
            }
            return false;
        }, new EditingSupport(tableViewer) {
            @Override
            protected CellEditor getCellEditor(Object element) {
                return new CustomCheckboxCellEditor(tableViewer.getTable(), true);
            }

            @Override
            protected boolean canEdit(Object element) {
                return true;
            }

            @Override
            protected Object getValue(Object element) {
                if (element instanceof GBase8aObjectPrivilege) {
                    return ((GBase8aObjectPrivilege) element).enabled;
                }
                return false;
            }

            @Override
            protected void setValue(Object element, Object value) {
                if (element instanceof GBase8aObjectPrivilege) {
                    GBase8aObjectPrivilege elementPriv = (GBase8aObjectPrivilege) element;
                    if (elementPriv.enabled != Boolean.TRUE.equals(value)) { // handle double click on the box cell
                        elementPriv.enabled = Boolean.TRUE.equals(value);
                        boolean withGrantOption = false;
                        if (elementPriv.enabled && elementPriv.privilege.getName().equals(GBase8aConstants.PRIVILEGE_GRANT_OPTION_NAME)) {
                            withGrantOption = true;
                        }
                        notifyPrivilegeCheck(elementPriv.privilege, elementPriv.enabled, withGrantOption);
                    }
                }
            }
        });

        columnsController.addColumn(GBase8aUIMessages.controls_privilege_table_column_privilege_description, GBase8aUIMessages.controls_privilege_table_column_privilege_description_tip, SWT.LEFT, true, true, new CellLabelProvider() {
            @Override
            public void update(ViewerCell cell) {
                Object element = cell.getElement();
                if (element instanceof GBase8aObjectPrivilege) {
                    cell.setText(((GBase8aObjectPrivilege) element).privilege.getDescription());
                }
            }
        });

        columnsController.createColumns(false);

        tableViewer.setContentProvider(new ListContentProvider());

        Composite buttonsPanel = UIUtils.createComposite(privsGroup, 3);
        buttonsPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        UIUtils.createPushButton(buttonsPanel, GBase8aUIMessages.controls_privilege_table_push_button_check_all, null, new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (GBase8aObjectPrivilege userPrivilege : CommonUtils.safeCollection(currentPrivileges)) {
                    if (!userPrivilege.enabled) {
                        userPrivilege.enabled = true;
                        notifyPrivilegeCheck(userPrivilege.privilege, true, false);
                    }
                }
                drawColumns(currentPrivileges);
            }
        });
        UIUtils.createPushButton(buttonsPanel, GBase8aUIMessages.controls_privilege_table_push_button_clear_all, null, new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (GBase8aObjectPrivilege userPrivilege : CommonUtils.safeCollection(currentPrivileges)) {
                    if (userPrivilege.enabled) {
                        userPrivilege.enabled = false;
                        notifyPrivilegeCheck(userPrivilege.privilege, false, false);
                    }
                }
                drawColumns(currentPrivileges);
            }
        });
    }

    private void notifyPrivilegeCheck(GBase8aPrivilege privilege, boolean checked, boolean withGrantOption) {
        Event event = new Event();
        event.detail = withGrantOption ? 2 : checked ? 1 : 0;
        event.widget = this;
        event.data = privilege;
        super.notifyListeners(SWT.Modify, event);
    }

    public void fillPrivileges(List<GBase8aPrivilege> privs) {
        this.privileges = privs;
        boolean hasGrantOption = false;
        for (GBase8aPrivilege privilege : privileges) {
            if (privilege.getName().equalsIgnoreCase(GBase8aConstants.PRIVILEGE_GRANT_OPTION_NAME)) {
                hasGrantOption = true;
                break;
            }
        }
        if (!hasGrantOption) {
            // Add "With Grant Option" manually. We will use this option to expand grant statements on the "WITH GRANT STATEMENT" string
            GBase8aDataSource dataSource = null;
            if (!CommonUtils.isEmpty(privileges)) {
                dataSource = (GBase8aDataSource) privileges.get(0).getDataSource();
            }
            privileges.add(new GBase8aPrivilege(
                    dataSource,
                    GBase8aConstants.PRIVILEGE_GRANT_OPTION_NAME,
                    "Databases,Tables,Functions,Procedures",
                    "To give to other users those privileges you possess",
                    GBase8aPrivilege.Kind.DDL));
        }
    }

    public void fillGrants(List<GBase8aGrant> grants, boolean editable) {
        // Other Privileges table must be disabled if table is in focus
        privTable.setEnabled(editable);
        fillGrants(grants);
    }

    public void fillGrants(List<GBase8aGrant> grants) {
        if (CommonUtils.isEmpty(privileges)) {
            return;
        }

        currentPrivileges = new ArrayList<>();

        if (CommonUtils.isEmpty(grants)) {
            // Create simple privileges list
            for (GBase8aPrivilege privilege : privileges) {
                currentPrivileges.add(new GBase8aObjectPrivilege(privilege, false));
            }
            drawColumns(currentPrivileges);
            return;
        }

        boolean privilegeEnabled;
        for (GBase8aPrivilege privilege : privileges) {
            privilegeEnabled = false;
            for (GBase8aGrant grant : grants) {
                if (isStatic && !grant.isStatic()) {
                    continue;
                }
                if (privilege.getName().equalsIgnoreCase(GBase8aConstants.PRIVILEGE_GRANT_OPTION_NAME)) {
                    if (grant.isGrantOption()) {
                        // WITH GRANT OPTION is enabled only in this case
                        privilegeEnabled = true;
                        break;
                    }
                } else if (grant.isAllPrivileges() || ArrayUtils.contains(grant.getPrivileges(), privilege)) {
                    privilegeEnabled = true;
                    break;
                }
            }
            if (privilegeEnabled) {
                currentPrivileges.add(new GBase8aObjectPrivilege(privilege, true));
            } else {
                currentPrivileges.add(new GBase8aObjectPrivilege(privilege, false));
            }
        }

        drawColumns(currentPrivileges);
    }

    private void drawColumns(List<?> objects) {
        tableViewer.setInput(objects);
        tableViewer.refresh();
        columnsController.repackColumns();
    }

    public void checkPrivilege(GBase8aPrivilege privilege, boolean grant) {
        for (GBase8aObjectPrivilege basePrivilege : currentPrivileges) {
            if (basePrivilege.privilege == privilege) {
                basePrivilege.enabled = grant;
            }
        }
        drawColumns(currentPrivileges);
    }

    private class GBase8aObjectPrivilege {

        private GBase8aPrivilege privilege;
        private boolean enabled;

        GBase8aObjectPrivilege(GBase8aPrivilege privilege, boolean enabled) {
            this.privilege = privilege;
            this.enabled = enabled;
        }
    }

}
