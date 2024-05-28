//package org.jkiss.dbeaver.ext.gbase8a.ui.tools;
//
//import org.jkiss.dbeaver.DBException;
//import org.jkiss.dbeaver.core.GBaseDataUI;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableBase;
//import org.jkiss.dbeaver.model.DBIcon;
//import org.jkiss.dbeaver.model.DBPImage;
//import org.jkiss.dbeaver.model.DBUtils;
//import org.jkiss.dbeaver.model.runtime.AbstractJob;
//import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
//import org.jkiss.dbeaver.model.struct.DBSObject;
//import org.jkiss.dbeaver.ui.GBaseDataIcons;
//import org.jkiss.dbeaver.ui.UIUtils;
//import org.jkiss.dbeaver.ui.controls.CustomSashForm;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import org.eclipse.core.runtime.IStatus;
//import org.eclipse.core.runtime.Status;
//import org.eclipse.swt.events.SelectionAdapter;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Event;
//import org.eclipse.swt.widgets.Group;
//import org.eclipse.swt.widgets.Listener;
//import org.eclipse.swt.widgets.Table;
//import org.eclipse.swt.widgets.TableItem;
//
//class GBase8aExportWizardPageObjects extends GBase8aWizardPageSettings<GBase8aExportWizard> {
//    private Map<GBase8aCatalog, Set<GBase8aTableBase>> checkedObjects = new HashMap<GBase8aCatalog, Set<GBase8aTableBase>>();
//    private Table catalogTable;
//    private Table tablesTable;
//    private GBase8aCatalog curCatalog;
//
//    protected GBase8aExportWizardPageObjects(GBase8aExportWizard wizard) {
//        super(wizard, "Schemas/tables");
//        setTitle("Choose objects to export");
//        setDescription("Schemas/tables/views which will be exported");
//    }
//
//
//    public boolean isPageComplete() {
//        return super.isPageComplete();
//    }
//
//
//    public void createControl(Composite parent) {
//        Composite composite = UIUtils.createPlaceholder(parent, 1);
//
//        Group objectsGroup = UIUtils.createControlGroup(composite, GBase8aMessages.tools_db_export_wizard_page_settings_group_objects, 1, 768, 0);
//        objectsGroup.setLayoutData(new GridData(1808));
//
//        CustomSashForm customSashForm = new CustomSashForm((Composite) objectsGroup, 512);
//        customSashForm.setLayoutData(new GridData(1808));
//
//        this.catalogTable = new Table((Composite) customSashForm, 2080);
//        this.catalogTable.addListener(13, new Listener() {
//            public void handleEvent(Event event) {
//                TableItem item = (TableItem) event.item;
//                GBase8aCatalog catalog = (GBase8aCatalog) item.getData();
//                if (event.detail == 32) {
//                    GBase8aExportWizardPageObjects.this.catalogTable.select(GBase8aExportWizardPageObjects.this.catalogTable.indexOf(item));
//                    GBase8aExportWizardPageObjects.this.checkedObjects.remove(catalog);
//                }
//                GBase8aExportWizardPageObjects.this.loadTables(catalog);
//                GBase8aExportWizardPageObjects.this.updateState();
//            }
//        });
//        GridData gd = new GridData(1808);
//        gd.heightHint = 50;
//        this.catalogTable.setLayoutData(gd);
//
//        this.tablesTable = new Table((Composite) customSashForm, 2080);
//        gd = new GridData(1808);
//        gd.heightHint = 50;
//        this.tablesTable.setLayoutData(gd);
//        this.tablesTable.addListener(13, new Listener() {
//            public void handleEvent(Event event) {
//                if (event.detail == 32) {
//                    GBase8aExportWizardPageObjects.this.updateCheckedTables();
//                    GBase8aExportWizardPageObjects.this.updateState();
//                }
//            }
//        });
//
//        final Button exportViewsCheck = UIUtils.createCheckbox((Composite) objectsGroup, "Show views", false);
//        exportViewsCheck.addSelectionListener((SelectionListener) new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                ((GBase8aExportWizard) GBase8aExportWizardPageObjects.this.wizard).showViews = exportViewsCheck.getSelection();
//                GBase8aExportWizardPageObjects.this.loadTables((GBase8aCatalog) null);
//            }
//        });
//
//        GBase8aDataSource dataSource = null;
//        Set<GBase8aCatalog> activeCatalogs = new LinkedHashSet<GBase8aCatalog>();
//        for (DBSObject object : ((GBase8aExportWizard) this.wizard).getDatabaseObjects()) {
//            if (object instanceof GBase8aCatalog) {
//                activeCatalogs.add((GBase8aCatalog) object);
//                dataSource = ((GBase8aCatalog) object).getDataSource();
//                continue;
//            }
//            if (object instanceof GBase8aTableBase) {
//                GBase8aCatalog catalog = (GBase8aCatalog) ((GBase8aTableBase) object).getContainer();
//                dataSource = catalog.getDataSource();
//                activeCatalogs.add(catalog);
//                Set<GBase8aTableBase> tables = this.checkedObjects.get(catalog);
//                if (tables == null) {
//                    tables = new HashSet<GBase8aTableBase>();
//                    this.checkedObjects.put(catalog, tables);
//                }
//                tables.add((GBase8aTableBase) object);
//                if (((GBase8aTableBase) object).isView()) {
//                    ((GBase8aExportWizard) this.wizard).showViews = true;
//                    exportViewsCheck.setSelection(true);
//                }
//                continue;
//            }
//            if (object.getDataSource() instanceof GBase8aDataSource) {
//                dataSource = (GBase8aDataSource) object.getDataSource();
//            }
//        }
//        if (dataSource != null) {
//            boolean tablesLoaded = false;
//            for (GBase8aCatalog catalog : dataSource.getCatalogs()) {
//                TableItem item = new TableItem(this.catalogTable, 0);
//                item.setImage(GBaseDataIcons.getImage((DBPImage) DBIcon.TREE_DATABASE));
//                item.setText(0, catalog.getName());
//                item.setData(catalog);
//                if (activeCatalogs.contains(catalog)) {
//                    item.setChecked(true);
//                    this.catalogTable.select(this.catalogTable.indexOf(item));
//                    if (!tablesLoaded) {
//                        loadTables(catalog);
//                        tablesLoaded = true;
//                    }
//                }
//            }
//        }
//        updateState();
//        setControl((Control) composite);
//    }
//
//    private void updateCheckedTables() {
//        Set<GBase8aTableBase> checkedTables = new HashSet<GBase8aTableBase>();
//        TableItem[] tableItems = this.tablesTable.getItems();
//        byte b;
//        int i;
//        TableItem[] arrayOfTableItem1;
//        for (i = (arrayOfTableItem1 = tableItems).length, b = 0; b < i; ) {
//            TableItem item = arrayOfTableItem1[b];
//            if (item.getChecked())
//                checkedTables.add((GBase8aTableBase) item.getData());
//            b++;
//        }
//
//        TableItem catalogItem = this.catalogTable.getItem(this.catalogTable.getSelectionIndex());
//        catalogItem.setChecked(!checkedTables.isEmpty());
//        if (checkedTables.isEmpty() || checkedTables.size() == tableItems.length) {
//            this.checkedObjects.remove(this.curCatalog);
//        } else {
//            this.checkedObjects.put(this.curCatalog, checkedTables);
//        }
//    }
//
//    private boolean isChecked(GBase8aCatalog catalog) {
//        byte b;
//        int i;
//        TableItem[] arrayOfTableItem;
//        for (i = (arrayOfTableItem = this.catalogTable.getItems()).length, b = 0; b < i; ) {
//            TableItem item = arrayOfTableItem[b];
//            if (item.getData() == catalog)
//                return item.getChecked();
//            b++;
//        }
//
//        return false;
//    }
//
//    private void loadTables(GBase8aCatalog catalog) {
//        if (catalog != null) {
//            this.curCatalog = catalog;
//        }
//        if (this.curCatalog == null) {
//            return;
//        }
//        final boolean isCatalogChecked = isChecked(this.curCatalog);
//        final Set<GBase8aTableBase> checkedObjects = this.checkedObjects.get(this.curCatalog);
//        (new AbstractJob("Load '" + this.curCatalog.getName() + "' tables") {
//
//
//            protected IStatus run(DBRProgressMonitor monitor) {
//                try {
//                    final List<GBase8aTableBase> objects = new ArrayList<GBase8aTableBase>();
//                    objects.addAll(GBase8aExportWizardPageObjects.this.curCatalog.getTables(monitor));
//                    if (((GBase8aExportWizard) GBase8aExportWizardPageObjects.this.wizard).showViews) {
//                        objects.addAll(GBase8aExportWizardPageObjects.this.curCatalog.getViews(monitor));
//                    }
//                    Collections.sort(objects, DBUtils.nameComparator());
//                    GBaseDataUI.syncExec(new Runnable() {
//                        public void run() {
//                            (GBase8aExportWizardPageObjects. null.access$0(GBase8aExportWizardPageObjects. null. this)).
//                            tablesTable.removeAll();
//                            for (GBase8aTableBase table : objects) {
//                                TableItem item = new TableItem((GBase8aExportWizardPageObjects.
//                                null.access$0(GBase8aExportWizardPageObjects. null. this)).tablesTable, 0);
//                                item.setImage(GBaseDataIcons.getImage(table.isView() ? (DBPImage) DBIcon.TREE_VIEW : (DBPImage) DBIcon.TREE_TABLE));
//                                item.setText(0, table.getName());
//                                item.setData(table);
//                                item.setChecked((isCatalogChecked && (checkedObjects == null || checkedObjects.contains(table))));
//                            }
//                        }
//                    });
//                } catch (DBException e) {
//                    UIUtils.showErrorDialog(null, "Table list", "Can't read table list", (Throwable) e);
//                }
//                return Status.OK_STATUS;
//            }
//        }).schedule();
//    }
//
//    public void saveState() {
//        ((GBase8aExportWizard) this.wizard).objects.clear();
//        byte b;
//        int i;
//        TableItem[] arrayOfTableItem;
//        for (i = (arrayOfTableItem = this.catalogTable.getItems()).length, b = 0; b < i; ) {
//            TableItem item = arrayOfTableItem[b];
//            if (item.getChecked()) {
//                GBase8aCatalog catalog = (GBase8aCatalog) item.getData();
//                GBase8aDatabaseExportInfo info = new GBase8aDatabaseExportInfo(catalog, this.checkedObjects.get(catalog));
//                ((GBase8aExportWizard) this.wizard).objects.add(info);
//            }
//            b++;
//        }
//
//    }
//
//    private void updateState() {
//        boolean complete = false;
//        if (!this.checkedObjects.isEmpty())
//            complete = true;
//        byte b;
//        int i;
//        TableItem[] arrayOfTableItem;
//        for (i = (arrayOfTableItem = this.catalogTable.getItems()).length, b = 0; b < i; ) {
//            TableItem item = arrayOfTableItem[b];
//            if (item.getChecked()) {
//                complete = true;
//                break;
//            }
//            b++;
//        }
//
//        setPageComplete(complete);
//    }
//}
