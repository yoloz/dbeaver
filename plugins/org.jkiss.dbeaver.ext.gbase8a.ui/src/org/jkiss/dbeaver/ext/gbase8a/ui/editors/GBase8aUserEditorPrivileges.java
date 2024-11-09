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
package org.jkiss.dbeaver.ext.gbase8a.ui.editors;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aGrant;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aPrivilege;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableBase;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aUser;
import org.jkiss.dbeaver.ext.gbase8a.ui.controls.PrivilegeTableControl;
import org.jkiss.dbeaver.ext.gbase8a.ui.config.GBase8aCommandGrantPrivilege;
import org.jkiss.dbeaver.ext.gbase8a.ui.internal.GBase8aUIMessages;
import org.jkiss.dbeaver.model.DBIcon;
import org.jkiss.dbeaver.model.edit.DBECommandReflector;
import org.jkiss.dbeaver.model.navigator.DBNEvent;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.load.DatabaseLoadService;
import org.jkiss.dbeaver.ui.DBeaverIcons;
import org.jkiss.dbeaver.ui.LoadingJob;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.controls.CustomSashForm;
import org.jkiss.utils.ArrayUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.*;

/**
 * GBase8aUserEditorPrivileges
 */
public class GBase8aUserEditorPrivileges extends GBase8aUserEditorAbstract {
    private static final Log log = Log.getLog(GBase8aUserEditorPrivileges.class);

    private PageControl pageControl;
    private Table catalogsTable;
    private Table tablesTable;

    private boolean isLoaded = false;
    private GBase8aCatalog selectedCatalog;
    private GBase8aTableBase selectedTable;
    private PrivilegeTableControl tablePrivilegesTable;
    private PrivilegeTableControl otherPrivilegesTable;
    private volatile List<GBase8aGrant> grants;

    private Font boldFont;

    @Override
    public void createPartControl(Composite parent) {
        boldFont = UIUtils.makeBoldFont(parent.getFont());

        pageControl = new PageControl(parent);

        GridData gd = new GridData(GridData.FILL_BOTH);
        CustomSashForm sash = new CustomSashForm(pageControl, SWT.HORIZONTAL);
        sash.setLayoutData(gd);

        Composite leftPane = UIUtils.createPlaceholder(sash, 2);
        leftPane.setLayoutData(new GridData(GridData.FILL_BOTH));
        leftPane.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
        {
            Composite catalogGroup = UIUtils.createControlGroup(leftPane, GBase8aUIMessages.editors_user_editor_privileges_group_catalogs, 1, GridData.FILL_BOTH, 0);

            catalogsTable = new Table(catalogGroup, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
            catalogsTable.setHeaderVisible(true);
            gd = new GridData(GridData.FILL_BOTH);
            catalogsTable.setLayoutData(gd);
            catalogsTable.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    int selIndex = catalogsTable.getSelectionIndex();
                    if (selIndex <= 0) {
                        selectedCatalog = null;
                    } else {
                        selectedCatalog = (GBase8aCatalog) catalogsTable.getItem(selIndex).getData();
                    }
                    showCatalogTables();
                    showGrants();
                }
            });
            UIUtils.createTableColumn(catalogsTable, SWT.LEFT, GBase8aUIMessages.editors_user_editor_privileges_column_catalog);
            {
                TableItem item = new TableItem(catalogsTable, SWT.NONE);
                item.setText("% (All)"); //$NON-NLS-1$
                item.setImage(DBeaverIcons.getImage(DBIcon.TREE_DATABASE));
            }
            try {
                for (GBase8aCatalog catalog : getDatabaseObject().getDataSource().getCatalogs()) {
                    TableItem item = new TableItem(catalogsTable, SWT.NONE);
                    item.setText(catalog.getName());
                    item.setImage(DBeaverIcons.getImage(DBIcon.TREE_DATABASE));
                    item.setData(catalog);
                }
            } catch (DBException e) {
                log.error("getUserCatalogs fail", e);
            }

            UIUtils.packColumns(catalogsTable);
        }

        {
            Composite tablesGroup = UIUtils.createControlGroup(leftPane, GBase8aUIMessages.editors_user_editor_privileges_group_tables, 1, GridData.FILL_BOTH, 0);

            tablesTable = new Table(tablesGroup, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
            tablesTable.setHeaderVisible(true);
            gd = new GridData(GridData.FILL_BOTH);
            tablesTable.setLayoutData(gd);
            tablesTable.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    int selIndex = tablesTable.getSelectionIndex();
                    if (selIndex <= 0) {
                        selectedTable = null;
                    } else {
                        selectedTable = (GBase8aTableBase) tablesTable.getItem(selIndex).getData();
                    }
                    showGrants();
                }
            });
            UIUtils.createTableColumn(tablesTable, SWT.LEFT, GBase8aUIMessages.editors_user_editor_privileges_column_table);
            UIUtils.packColumns(tablesTable);
        }
        Composite ph = UIUtils.createPlaceholder(sash, 1);
        ph.setLayoutData(new GridData(GridData.FILL_BOTH));

        tablePrivilegesTable = new PrivilegeTableControl(ph, GBase8aUIMessages.editors_user_editor_privileges_control_table_privileges, false);
        gd = new GridData(GridData.FILL_BOTH);
        tablePrivilegesTable.setLayoutData(gd);

        otherPrivilegesTable = new PrivilegeTableControl(ph, GBase8aUIMessages.editors_user_editor_privileges_control_other_privileges, false);
        gd = new GridData(GridData.FILL_BOTH);
        otherPrivilegesTable.setLayoutData(gd);

        sash.setSashBorders(new boolean[]{false, false});

        catalogsTable.setSelection(0);
        showCatalogTables();

        pageControl.createProgressPanel();

        parent.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                UIUtils.dispose(boldFont);
            }
        });

        addGrantListener(tablePrivilegesTable);
        addGrantListener(otherPrivilegesTable);
    }

    private void addGrantListener(final PrivilegeTableControl privTable) {
        privTable.addListener(SWT.Modify, new Listener() {
            @Override
            public void handleEvent(Event event) {
                final GBase8aPrivilege privilege = (GBase8aPrivilege) event.data;
                final boolean isGrant = event.detail >= 1;
                final boolean withGrantOption = event.detail == 2;
                final GBase8aCatalog curCatalog = selectedCatalog;
                final GBase8aTableBase curTable = selectedTable;
                updateLocalData(privilege, isGrant, withGrantOption, curCatalog, curTable);

                // Add command
                addChangeCommand(
                        new GBase8aCommandGrantPrivilege(
                                getDatabaseObject(),
                                isGrant,
                                withGrantOption,
                                curCatalog,
                                curTable,
                                privilege),
                        new DBECommandReflector<GBase8aUser, GBase8aCommandGrantPrivilege>() {
                            @Override
                            public void redoCommand(GBase8aCommandGrantPrivilege mySQLCommandGrantPrivilege) {
                                if (!privTable.isDisposed() && curCatalog == selectedCatalog && curTable == selectedTable) {
                                    privTable.checkPrivilege(privilege, isGrant);
                                }
                                updateLocalData(privilege, isGrant, withGrantOption, curCatalog, curTable);
                            }

                            @Override
                            public void undoCommand(GBase8aCommandGrantPrivilege mySQLCommandGrantPrivilege) {
                                if (!privTable.isDisposed() && curCatalog == selectedCatalog && curTable == selectedTable) {
                                    privTable.checkPrivilege(privilege, !isGrant);
                                }
                                updateLocalData(privilege, !isGrant, !withGrantOption, curCatalog, curTable);
                            }
                        });
            }
        });
    }

    private void updateLocalData(GBase8aPrivilege privilege, boolean isGrant, boolean withGrantOption, GBase8aCatalog curCatalog, GBase8aTableBase curTable) {
        // Modify local grants (and clear grants cache in user objects)
        getDatabaseObject().clearGrantsCache();
        boolean found = false;
        for (GBase8aGrant grant : grants) {
            if (grant.matches(curCatalog) && grant.matches(curTable)) {
                //if (privilege.isGrantOption()) {
                grant.setGrantOption(withGrantOption);
                //} else
                if (isGrant) {
                    if (!ArrayUtils.contains(grant.getPrivileges(), privilege)) {
                        grant.addPrivilege(privilege);
                    }
                } else {
                    grant.removePrivilege(privilege);
                }
                found = true;
                break;
            }
        }
        if (!found) {
            List<GBase8aPrivilege> privileges = new ArrayList<>();
            if (!privilege.isGrantOption()) {
                privileges.add(privilege);
            }
            GBase8aGrant grant = new GBase8aGrant(
                    getDatabaseObject(),
                    privileges,
                    curCatalog == null ? "*" : curCatalog.getVcName(),
                    curCatalog == null ? "*" : curCatalog.getName(), //$NON-NLS-1$
                    curTable == null ? "*" : curTable.getName(), //$NON-NLS-1$
                    false,
                    withGrantOption);
            grants.add(grant);
        }
        highlightCatalogs();
        highlightTables();
    }

    private void showCatalogTables() {
        LoadingJob.createService(
                new DatabaseLoadService<>(GBase8aUIMessages.editors_user_editor_privileges_service_load_tables, getExecutionContext()) {
                    @Override
                    public Collection<GBase8aTableBase> evaluate(DBRProgressMonitor monitor)
                            throws InvocationTargetException, InterruptedException {
                        if (selectedCatalog == null) {
                            return Collections.emptyList();
                        }
                        try {
                            return selectedCatalog.getTableCache().getAllObjects(monitor, selectedCatalog);
                        } catch (DBException e) {
                            log.error(e);
                        }
                        return null;
                    }
                },
                pageControl.createTablesLoadVisualizer()).schedule();
    }

    private void showGrants() {
        if (grants == null) {
            return;
        }
        List<GBase8aGrant> curGrants = new ArrayList<>();
        for (GBase8aGrant grant : grants) {
            if (grant.matches(selectedCatalog) && grant.matches(selectedTable)) {
                curGrants.add(grant);
            }
        }
        tablePrivilegesTable.fillGrants(curGrants);
        if (selectedTable == null) {
            otherPrivilegesTable.fillGrants(curGrants, true);
        } else {
            // Privilege table will be grayed. No grants for this table
            otherPrivilegesTable.fillGrants(new ArrayList<>(), false);
        }
    }

    @Override
    public synchronized void activatePart() {
        if (isLoaded) {
            return;
        }
        isLoaded = true;
        LoadingJob.createService(
                        new DatabaseLoadService<>(GBase8aUIMessages.editors_user_editor_privileges_service_load_privileges, getExecutionContext()) {
                            @Override
                            public List<GBase8aPrivilege> evaluate(DBRProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                                try {
                                    return getDatabaseObject().getDataSource().getPrivileges(monitor);
                                } catch (DBException e) {
                                    throw new InvocationTargetException(e);
                                }
                            }
                        },
                        pageControl.createPrivilegesLoadVisualizer()).schedule();
    }

    @Override
    protected PageControl getPageControl() {
        return pageControl;
    }

    @Override
    protected void processGrants(List<GBase8aGrant> grantsTmp) {
        this.grants = new ArrayList<>(grantsTmp);
        for (Iterator<GBase8aGrant> i = grants.iterator(); i.hasNext(); ) {
            GBase8aGrant grant = i.next();
            if (!grant.isAllPrivileges() && !grant.hasNonAdminPrivileges() && !grant.isGrantOption()) {
                i.remove();
            }
        }
        highlightCatalogs();

        showGrants();
        showCatalogTables();
    }

    private void highlightCatalogs() {
        // Highlight granted catalogs
        if (catalogsTable != null && !catalogsTable.isDisposed()) {
            for (TableItem item : catalogsTable.getItems()) {
                GBase8aCatalog catalog = (GBase8aCatalog) item.getData();
                item.setFont(null);
                if (grants != null) {
                    for (GBase8aGrant grant : grants) {
                        if (grant.matches(catalog) && !grant.isEmpty()) {
                            item.setFont(boldFont);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void highlightTables() {
        if (tablesTable != null && !tablesTable.isDisposed()) {
            for (TableItem item : tablesTable.getItems()) {
                GBase8aTableBase table = (GBase8aTableBase) item.getData();
                item.setFont(null);
                if (grants != null) {
                    for (GBase8aGrant grant : grants) {
                        if (grant.matches(selectedCatalog) && grant.matches(table) && !grant.isEmpty()) {
                            item.setFont(boldFont);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public RefreshResult refreshPart(Object source, boolean force) {
        if (force || (source instanceof DBNEvent && ((DBNEvent) source).getSource() == DBNEvent.UPDATE_ON_SAVE)
                || !isLoaded) {
            isLoaded = false;
            activatePart();
            return RefreshResult.REFRESHED;
        }
        return RefreshResult.IGNORED;
    }

    private class PageControl extends UserPageControl {
        public PageControl(Composite parent) {
            super(parent);
        }

        public ProgressVisualizer<Collection<GBase8aTableBase>> createTablesLoadVisualizer() {
            return new ProgressVisualizer<>() {
                @Override
                public void completeLoading(Collection<GBase8aTableBase> tables) {
                    super.completeLoading(tables);
                    if (tablesTable.isDisposed()) {
                        return;
                    }
                    tablesTable.removeAll();
                    {
                        TableItem item = new TableItem(tablesTable, SWT.NONE);
                        item.setText("% (All)"); //$NON-NLS-1$
                        item.setImage(DBeaverIcons.getImage(DBIcon.TREE_TABLE));
                    }
                    if (tables != null) {
                        for (GBase8aTableBase table : tables) {
                            TableItem item = new TableItem(tablesTable, SWT.NONE);
                            item.setText(table.getName());
                            item.setImage(DBeaverIcons.getImage(table.isView() ? DBIcon.TREE_VIEW : DBIcon.TREE_TABLE));
                            item.setData(table);
                        }
                        highlightTables();
                    }
                    UIUtils.packColumns(tablesTable);
                }
            };
        }

        public ProgressVisualizer<List<GBase8aPrivilege>> createPrivilegesLoadVisualizer() {
            return new ProgressVisualizer<>() {
                @Override
                public void completeLoading(List<GBase8aPrivilege> privs) {
                    super.completeLoading(privs);
                    List<GBase8aPrivilege> otherPrivs = new ArrayList<>();
                    List<GBase8aPrivilege> tablePrivs = new ArrayList<>();
                    for (GBase8aPrivilege priv : privs) {
                        if (priv.getKind() == GBase8aPrivilege.Kind.ADMIN) {
                            continue;
                        }
                        if (priv.getContext().contains("Table")) {
                            tablePrivs.add(priv);
                        } else {
                            otherPrivs.add(priv);
                        }
                    }
                    tablePrivilegesTable.fillPrivileges(tablePrivs);
                    otherPrivilegesTable.fillPrivileges(otherPrivs);
                    loadGrants();
                }
            };
        }

    }


}