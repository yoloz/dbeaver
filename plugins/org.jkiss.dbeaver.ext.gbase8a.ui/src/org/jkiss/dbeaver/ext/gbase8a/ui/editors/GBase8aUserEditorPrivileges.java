package org.jkiss.dbeaver.ext.gbase8a.ui.editors;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.ui.controls.PrivilegeTableControl;
import org.jkiss.dbeaver.ext.gbase8a.edit.GBase8aCommandGrantPrivilege;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aGrant;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aPrivilege;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableBase;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aUser;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aVC;
import org.jkiss.dbeaver.model.DBIcon;
import org.jkiss.dbeaver.model.edit.DBECommandReflector;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.load.DatabaseLoadService;
import org.jkiss.dbeaver.ui.DBeaverIcons;
import org.jkiss.dbeaver.ui.LoadingJob;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.controls.ProgressPageControl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;


public class GBase8aUserEditorPrivileges extends GBase8aUserEditorAbstract {
    private static final Log log = Log.getLog(GBase8aUserEditorPrivileges.class);
    private PageControl pageControl;
    private Table vcsTable;
    private Table catalogsTable;
    private Table tablesTable;
    private boolean isLoaded = false;
    private GBase8aVC selectedVC;
    private GBase8aCatalog selectedCatalog;
    private GBase8aTableBase selectedTable;
    private PrivilegeTableControl tablePrivilegesTable;
    private PrivilegeTableControl otherPrivilegesTable;
    private volatile List<GBase8aGrant> grants;
    private Font boldFont;


    public void createPartControl(Composite parent) {
        this.boldFont = UIUtils.makeBoldFont(parent.getFont());
        this.pageControl = new PageControl(parent);
        Composite container = UIUtils.createPlaceholder(this.pageControl, 2, 5);
        GridData gd = new GridData(1808);
        container.setLayoutData(gd);
        Composite leftPane = UIUtils.createPlaceholder(container, 2);
        leftPane.setLayoutData(new GridData(1808));
        leftPane.setLayout(new GridLayout(3, true));
        Composite tablesGroup = UIUtils.createControlGroup(leftPane, GBase8aMessages.editors_user_editor_privileges_group_vcs, 1, 1808, 0);
        this.vcsTable = new Table(tablesGroup, 2820);
        this.vcsTable.setHeaderVisible(true);
        gd = new GridData(1808);
        this.vcsTable.setLayoutData(gd);
        this.vcsTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                int selIndex = GBase8aUserEditorPrivileges.this.vcsTable.getSelectionIndex();
                if (selIndex <= 0) {
                    GBase8aUserEditorPrivileges.this.selectedVC = null;
                } else {
                    GBase8aUserEditorPrivileges.this.selectedVC = (GBase8aVC) GBase8aUserEditorPrivileges.this.vcsTable.getItem(selIndex).getData();
                }

                GBase8aUserEditorPrivileges.this.showVcCatalogs();
                GBase8aUserEditorPrivileges.this.showCatalogTables();
                GBase8aUserEditorPrivileges.this.showGrants();
            }
        });
        UIUtils.createTableColumn(this.vcsTable, 16384, GBase8aMessages.editors_user_editor_privileges_column_vc);
        TableItem item = new TableItem(this.vcsTable, 0);
        item.setText("% (All)");
        item.setImage(DBeaverIcons.getImage(DBIcon.TREE_SCHEMA_UTIL));
        Iterator var7 = this.getDatabaseObject().getDataSource().getVcs(null).iterator();

        while (var7.hasNext()) {
            GBase8aVC vc = (GBase8aVC) var7.next();
            TableItem item1 = new TableItem(this.vcsTable, 0);
            item1.setText(vc.getName());
            item1.setImage(DBeaverIcons.getImage(DBIcon.TREE_SCHEMA_UTIL));
            item1.setData(vc);
        }

        UIUtils.packColumns(this.vcsTable);
        tablesGroup = UIUtils.createControlGroup(leftPane, GBase8aMessages.editors_user_editor_privileges_group_catalogs, 1, 1808, 0);
        this.catalogsTable = new Table(tablesGroup, 2820);
        this.catalogsTable.setHeaderVisible(true);
        gd = new GridData(1808);
        this.catalogsTable.setLayoutData(gd);
        this.catalogsTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                int selIndex = GBase8aUserEditorPrivileges.this.catalogsTable.getSelectionIndex();
                if (selIndex <= 0) {
                    GBase8aUserEditorPrivileges.this.selectedCatalog = null;
                } else {
                    GBase8aUserEditorPrivileges.this.selectedCatalog = (GBase8aCatalog) GBase8aUserEditorPrivileges.this.catalogsTable.getItem(selIndex).getData();
                }

                GBase8aUserEditorPrivileges.this.showCatalogTables();
                GBase8aUserEditorPrivileges.this.showGrants();
            }
        });
        UIUtils.createTableColumn(this.catalogsTable, 16384, GBase8aMessages.editors_user_editor_privileges_column_catalog);
        item = new TableItem(this.catalogsTable, 0);
        item.setText("% (All)");
        item.setImage(DBeaverIcons.getImage(DBIcon.TREE_DATABASE));
        var7 = this.getDatabaseObject().getDataSource().getCatalogs().iterator();

        while (var7.hasNext()) {
            GBase8aCatalog catalog = (GBase8aCatalog) var7.next();
            item = new TableItem(this.catalogsTable, 0);
            item.setText(catalog.getName());
            item.setImage(DBeaverIcons.getImage(DBIcon.TREE_DATABASE));
            item.setData(catalog);
        }

        UIUtils.packColumns(this.catalogsTable);
        tablesGroup = UIUtils.createControlGroup(leftPane, GBase8aMessages.editors_user_editor_privileges_group_tables, 1, 1808, 0);
        this.tablesTable = new Table(tablesGroup, 2820);
        this.tablesTable.setHeaderVisible(true);
        gd = new GridData(1808);
        this.tablesTable.setLayoutData(gd);
        this.tablesTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                int selIndex = GBase8aUserEditorPrivileges.this.tablesTable.getSelectionIndex();
                if (selIndex <= 0) {
                    GBase8aUserEditorPrivileges.this.selectedTable = null;
                } else {
                    GBase8aUserEditorPrivileges.this.selectedTable = (GBase8aTableBase) GBase8aUserEditorPrivileges.this.tablesTable.getItem(selIndex).getData();
                }

                GBase8aUserEditorPrivileges.this.showGrants();
            }
        });
        UIUtils.createTableColumn(this.tablesTable, 16384, GBase8aMessages.editors_user_editor_privileges_column_table);
        UIUtils.packColumns(this.tablesTable);
        Composite ph = UIUtils.createPlaceholder(container, 1);
        ph.setLayoutData(new GridData(1808));
        this.tablePrivilegesTable = new PrivilegeTableControl(ph, GBase8aMessages.editors_user_editor_privileges_control_table_privileges);
        gd = new GridData(1808);
        this.tablePrivilegesTable.setLayoutData(gd);
        this.otherPrivilegesTable = new PrivilegeTableControl(ph, GBase8aMessages.editors_user_editor_privileges_control_other_privileges);
        gd = new GridData(1808);
        this.otherPrivilegesTable.setLayoutData(gd);
        this.vcsTable.setSelection(0);
        this.catalogsTable.setSelection(0);
        this.showVcCatalogs();
        this.showCatalogTables();
        this.pageControl.createProgressPanel();
        parent.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                UIUtils.dispose(GBase8aUserEditorPrivileges.this.boldFont);
            }
        });
        this.addGrantListener(this.tablePrivilegesTable);
        this.addGrantListener(this.otherPrivilegesTable);
    }

    private void addGrantListener(final PrivilegeTableControl privTable) {
        privTable.addListener(24, new Listener() {
            public void handleEvent(Event event) {
                final GBase8aPrivilege privilege = (GBase8aPrivilege) event.data;
                final boolean isGrant = event.detail == 1;
                final GBase8aVC curVC = GBase8aUserEditorPrivileges.this.selectedVC;
                final GBase8aCatalog curCatalog = GBase8aUserEditorPrivileges.this.selectedCatalog;
                final GBase8aTableBase curTable = GBase8aUserEditorPrivileges.this.selectedTable;
                GBase8aUserEditorPrivileges.this.updateLocalData(privilege, isGrant, curVC, curCatalog, curTable);
                GBase8aUserEditorPrivileges.this.addChangeCommand(new GBase8aCommandGrantPrivilege(GBase8aUserEditorPrivileges.this.getDatabaseObject(), isGrant, curVC, curCatalog, curTable, privilege), new DBECommandReflector<GBase8aUser, GBase8aCommandGrantPrivilege>() {
                    public void redoCommand(GBase8aCommandGrantPrivilege GBaseDataCommandGrantPrivilege) {
                        if (!privTable.isDisposed() && curVC == GBase8aUserEditorPrivileges.this.selectedVC && curCatalog == GBase8aUserEditorPrivileges.this.selectedCatalog && curTable == GBase8aUserEditorPrivileges.this.selectedTable) {
                            privTable.checkPrivilege(privilege, isGrant);
                        }

                        GBase8aUserEditorPrivileges.this.updateLocalData(privilege, isGrant, curVC, curCatalog, curTable);
                    }

                    public void undoCommand(GBase8aCommandGrantPrivilege GBaseDataCommandGrantPrivilege) {
                        if (!privTable.isDisposed() && curVC == GBase8aUserEditorPrivileges.this.selectedVC && curCatalog == GBase8aUserEditorPrivileges.this.selectedCatalog && curTable == GBase8aUserEditorPrivileges.this.selectedTable) {
                            privTable.checkPrivilege(privilege, !isGrant);
                        }

                        GBase8aUserEditorPrivileges.this.updateLocalData(privilege, !isGrant, curVC, curCatalog, curTable);
                    }
                });
            }
        });
    }

    private void updateLocalData(GBase8aPrivilege privilege, boolean isGrant, GBase8aVC curVC, GBase8aCatalog curCatalog, GBase8aTableBase curTable) {
        this.getDatabaseObject().clearGrantsCache();
        boolean found = false;
        for (GBase8aGrant grant : this.grants) {
            if (grant.matches(curVC) && grant.matches(curCatalog) && grant.matches(curTable)) {
                if (privilege.isGrantOption()) {
                    grant.setGrantOption(isGrant);
                } else if (isGrant) {
                    if (!grant.getPrivileges().contains(privilege)) {
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
            List<GBase8aPrivilege> privileges = new ArrayList();
            if (!privilege.isGrantOption()) {
                privileges.add(privilege);
            }

            GBase8aGrant grant = new GBase8aGrant(this.getDatabaseObject(), privileges, curVC == null ? "*" : curVC.getName(), curCatalog == null ? "*" : curCatalog.getName(), curTable == null ? "*" : curTable.getName(), false, privilege.isGrantOption());
            this.grants.add(grant);
        }

        this.highlightVcs();
        this.highlightCatalogs();
        this.highlightTables();
    }

    private void showVcCatalogs() {
        LoadingJob.createService(new DatabaseLoadService<>(GBase8aMessages.editors_user_editor_privileges_service_load_catalogs,
                this.getExecutionContext()) {
            public Collection<GBase8aCatalog> evaluate(DBRProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                return GBase8aUserEditorPrivileges.this.selectedVC == null ? Collections.emptyList() : GBase8aUserEditorPrivileges.this.selectedVC.getCatalogs(null);
            }
        }, this.pageControl.createCatalogsLoadVisualizer()).schedule();
    }

    private void showCatalogTables() {
        LoadingJob.createService(new DatabaseLoadService<>(GBase8aMessages.editors_user_editor_privileges_service_load_tables, this.getExecutionContext()) {
            public Collection<GBase8aTableBase> evaluate(DBRProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                if (GBase8aUserEditorPrivileges.this.selectedCatalog == null) {
                    return Collections.emptyList();
                } else {
                    try {
                        return GBase8aUserEditorPrivileges.this.selectedCatalog.getTableCache().getAllObjects(monitor, GBase8aUserEditorPrivileges.this.selectedCatalog);
                    } catch (DBException e) {
                        GBase8aUserEditorPrivileges.log.error(e);
                    }
                }
                return null;
            }
        }, this.pageControl.createTablesLoadVisualizer()).schedule();
    }

    private void showGrants() {
        if (this.grants != null) {
            List<GBase8aGrant> curGrants = new ArrayList();

            for (GBase8aGrant grant : this.grants) {
                if (grant.matches(this.selectedCatalog) && grant.matches(this.selectedTable)) {
                    curGrants.add(grant);
                }
            }

            this.tablePrivilegesTable.fillGrants(curGrants);
            this.otherPrivilegesTable.fillGrants(curGrants);
        }
    }

    public synchronized void activatePart() {
        if (!this.isLoaded) {
            this.isLoaded = true;
            LoadingJob.createService(new DatabaseLoadService<>(GBase8aMessages.editors_user_editor_privileges_service_load_privileges, this.getExecutionContext()) {
                public List<GBase8aPrivilege> evaluate(DBRProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        List<GBase8aPrivilege> privList = GBase8aUserEditorPrivileges.this.getDatabaseObject().getDataSource().getPrivileges(monitor);
                        Iterator<GBase8aPrivilege> iterator = privList.iterator();

                        while (iterator.hasNext()) {
                            GBase8aPrivilege priv = iterator.next();
                            if (priv.getName().equalsIgnoreCase("connect")) {
                                iterator.remove();
                            }

                            if (priv.getName().equalsIgnoreCase("defaultvc")) {
                                iterator.remove();
                            }

                            if (priv.getName().equalsIgnoreCase("file")) {
                                iterator.remove();
                            }
                        }

                        return privList;
                    } catch (DBException e) {
                        throw new InvocationTargetException(e);
                    }
                }
            }, this.pageControl.createPrivilegesLoadVisualizer()).schedule();
        }
    }

    protected PageControl getPageControl() {
        return this.pageControl;
    }

    protected void processGrants(List<GBase8aGrant> grantsTmp) {
        this.grants = new ArrayList(grantsTmp);

        this.grants.removeIf(grant -> !grant.isAllPrivileges() && !grant.hasNonAdminPrivileges());

        this.highlightCatalogs();
        this.showGrants();
        this.showCatalogTables();
    }

    private void highlightVcs() {
        if (this.vcsTable != null && !this.vcsTable.isDisposed()) {
            TableItem[] var4;
            int var3 = (var4 = this.vcsTable.getItems()).length;

            for (int var2 = 0; var2 < var3; ++var2) {
                TableItem item = var4[var2];
                GBase8aVC vc = (GBase8aVC) item.getData();
                item.setFont(null);
                if (this.grants != null) {

                    for (GBase8aGrant grant : this.grants) {
                        if (grant.matches(vc) && !grant.isEmpty()) {
                            item.setFont(this.boldFont);
                            break;
                        }
                    }
                }
            }
        }

    }

    private void highlightCatalogs() {
        if (this.catalogsTable != null && !this.catalogsTable.isDisposed()) {
            TableItem[] var4;
            int var3 = (var4 = this.catalogsTable.getItems()).length;

            for (int var2 = 0; var2 < var3; ++var2) {
                TableItem item = var4[var2];
                GBase8aCatalog catalog = (GBase8aCatalog) item.getData();
                item.setFont(null);
                if (this.grants != null) {

                    for (GBase8aGrant grant : this.grants) {
                        if (grant.matches(catalog) && !grant.isEmpty()) {
                            item.setFont(this.boldFont);
                            break;
                        }
                    }
                }
            }
        }

    }

    private void highlightTables() {
        if (this.tablesTable != null && !this.tablesTable.isDisposed()) {
            TableItem[] var4;
            int var3 = (var4 = this.tablesTable.getItems()).length;

            for (int var2 = 0; var2 < var3; ++var2) {
                TableItem item = var4[var2];
                GBase8aTableBase table = (GBase8aTableBase) item.getData();
                item.setFont(null);
                if (this.grants != null) {

                    for (GBase8aGrant grant : this.grants) {
                        if (grant.matches(this.selectedCatalog) && grant.matches(table) && !grant.isEmpty()) {
                            item.setFont(this.boldFont);
                            break;
                        }
                    }
                }
            }
        }

    }

    public RefreshResult refreshPart(Object source, boolean force) {
        this.showVcCatalogs();
        this.showCatalogTables();
        this.showGrants();
        return RefreshResult.REFRESHED;
    }

    private class PageControl extends GBase8aUserEditorAbstract.UserPageControl {
        public PageControl(Composite parent) {
            super(parent);
        }

        public ProgressPageControl.ProgressVisualizer<Collection<GBase8aCatalog>> createCatalogsLoadVisualizer() {
            return new ProgressPageControl.ProgressVisualizer<>() {
                public void completeLoading(Collection<GBase8aCatalog> catalogs) {
                    super.completeLoading(catalogs);
                    if (!GBase8aUserEditorPrivileges.this.catalogsTable.isDisposed()) {
                        GBase8aUserEditorPrivileges.this.catalogsTable.removeAll();
                        TableItem item = new TableItem(GBase8aUserEditorPrivileges.this.catalogsTable, 0);
                        item.setText("% (All)");
                        item.setImage(DBeaverIcons.getImage(DBIcon.TREE_TABLE));

                        for (GBase8aCatalog catalog : catalogs) {
                            TableItem itemx = new TableItem(GBase8aUserEditorPrivileges.this.catalogsTable, 0);
                            itemx.setText(catalog.getName());
                            itemx.setImage(DBeaverIcons.getImage(DBIcon.TREE_DATABASE));
                            itemx.setData(catalog);
                        }

                        GBase8aUserEditorPrivileges.this.highlightCatalogs();
                        UIUtils.packColumns(GBase8aUserEditorPrivileges.this.catalogsTable);
                    }
                }
            };
        }

        public ProgressPageControl.ProgressVisualizer<Collection<GBase8aTableBase>> createTablesLoadVisualizer() {
            return new ProgressPageControl.ProgressVisualizer<>() {
                public void completeLoading(Collection<GBase8aTableBase> tables) {
                    super.completeLoading(tables);
                    if (!GBase8aUserEditorPrivileges.this.tablesTable.isDisposed()) {
                        GBase8aUserEditorPrivileges.this.tablesTable.removeAll();
                        TableItem item = new TableItem(GBase8aUserEditorPrivileges.this.tablesTable, 0);
                        item.setText("% (All)");
                        item.setImage(DBeaverIcons.getImage(DBIcon.TREE_TABLE));

                        for (GBase8aTableBase table : tables) {
                            TableItem itemx = new TableItem(GBase8aUserEditorPrivileges.this.tablesTable, 0);
                            itemx.setText(table.getName());
                            itemx.setImage(DBeaverIcons.getImage(table.isView() ? DBIcon.TREE_VIEW : DBIcon.TREE_TABLE));
                            itemx.setData(table);
                        }

                        GBase8aUserEditorPrivileges.this.highlightTables();
                        UIUtils.packColumns(GBase8aUserEditorPrivileges.this.tablesTable);
                    }
                }
            };
        }

        public ProgressPageControl.ProgressVisualizer<List<GBase8aPrivilege>> createPrivilegesLoadVisualizer() {
            return new ProgressPageControl.ProgressVisualizer<>() {
                public void completeLoading(List<GBase8aPrivilege> privs) {
                    super.completeLoading(privs);
                    List<GBase8aPrivilege> otherPrivs = new ArrayList();
                    List<GBase8aPrivilege> tablePrivs = new ArrayList();

                    for (GBase8aPrivilege priv : privs) {
                        if (priv.getKind() != GBase8aPrivilege.Kind.ADMIN) {
                            if (priv.getContext().contains("Table")) {
                                tablePrivs.add(priv);
                            } else {
                                otherPrivs.add(priv);
                            }
                        }
                    }

                    GBase8aUserEditorPrivileges.this.tablePrivilegesTable.fillPrivileges(tablePrivs);
                    GBase8aUserEditorPrivileges.this.otherPrivilegesTable.fillPrivileges(otherPrivs);
                    GBase8aUserEditorPrivileges.this.loadGrants();
                }
            };
        }
    }
}
