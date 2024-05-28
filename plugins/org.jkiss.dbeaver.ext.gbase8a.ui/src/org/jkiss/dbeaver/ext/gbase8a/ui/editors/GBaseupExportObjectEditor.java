package org.jkiss.dbeaver.ext.gbase8a.ui.editors;

import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.gbase8a.ui.GBase8aIEUtil;
import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aIEClusterObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;


public class GBaseupExportObjectEditor extends EditorPart {
    Log log = Log.getLog(GBaseupExportObjectEditor.class);

    public static final String ID = "org.jkiss.dbeaver.ext.gbase8a.ui.editors.GBaseupExportObjectEditor";
    private Composite container;
    private Table table;
    private TableViewer tableViewer;
    private Text txtAddr;
    private Combo databaseCombo;
    private String[] filterExpressions;
    private JDBCSession session;
    private String fFilterPath;
    private Button btnSingle;
    private List<GBase8aIEClusterObject> clusterObjectList = new ArrayList();
    private String curSelDatabase;
    private String fileAddr = "";
    private boolean exportSingleFile;
    private List<GBase8aIEClusterObject> selectedObjectList = new ArrayList();
    private boolean isSuccess = true;
    private String errorMessage = "";
    private String DBName = "";
    private GBase8aDataSource datasource;

    public GBaseupExportObjectEditor() {
    }

    public void createPartControl(Composite parent) {
        GBaseupExportObjectEditorInput editorInput = (GBaseupExportObjectEditorInput) this.getEditorInput();
        this.datasource = editorInput.getDataSource();
        try {
            this.session = DBUtils.openMetaSession(this.datasource.getMonitor(), this.datasource, "Load table status");
        } catch (DBCException e) {
            log.error(e);
        }
        this.DBName = editorInput.getDbname();
        this.initGUI(parent);
        this.initDatabaseCombo();
        this.addDatabaseComboSelListener();
    }

    private void initGUI(Composite parent) {
        this.container = new Composite(parent, 0);
        this.container.setLayout(new GridLayout(1, false));
        this.databaseCombo = new Combo(this.container, 8);
        this.databaseCombo.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        this.tableViewer = new TableViewer(this.container, 67586);
        this.table = this.tableViewer.getTable();
        this.table.setLayoutData(new GridData(4, 4, true, true, 1, 1));
        TableColumn tableColumnName = new TableColumn(this.table, 0);
        tableColumnName.setText(GBase8aMessages.editors_export_object_title_name);
        TableColumn tableCoumnType = new TableColumn(this.table, 0);
        tableCoumnType.setText(GBase8aMessages.editors_export_object_title_type);
        this.table.setLinesVisible(true);
        this.table.setHeaderVisible(true);
        TableLayout tableLayout = new TableLayout();

        for (int i = 0; i < 2; ++i) {
            tableLayout.addColumnData(new ColumnWeightData(1, 100, true));
        }

        this.table.setLayout(tableLayout);
        this.tableViewer.setLabelProvider(new LProvider());
        this.tableViewer.setContentProvider(new IStructuredContentProvider() {
            public Object[] getElements(Object input) {
                return GBaseupExportObjectEditor.this.clusterObjectList.toArray();
            }

            public void dispose() {
            }

            public void inputChanged(Viewer viewer, Object arg1, Object arg2) {
            }
        });
        Composite cmpExport = new Composite(this.container, 0);
        cmpExport.setLayout(new GridLayout(3, false));
        cmpExport.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        Group grpFile = new Group(cmpExport, 0);
        grpFile.setLayoutData(new GridData(4, 16777216, true, false, 3, 1));
        grpFile.setText(GBase8aMessages.editors_export_object_title_file_item);
        grpFile.setBounds(0, 0, 70, 84);
        grpFile.setLayout(new GridLayout(1, false));
        this.btnSingle = new Button(grpFile, 16);
        this.btnSingle.setSelection(true);
        this.btnSingle.setText(GBase8aMessages.editors_export_object_title_all_export_one_file);
        Button btnPerfile = new Button(grpFile, 16);
        btnPerfile.setText(GBase8aMessages.editors_export_object_title_one_export_one_file);
        Label lblAddr = new Label(cmpExport, 0);
        lblAddr.setBounds(0, 0, 61, 17);
        lblAddr.setText(GBase8aMessages.editors_export_object_title_export_path);
        this.txtAddr = new Text(cmpExport, 2048);
        this.txtAddr.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        this.txtAddr.setBounds(0, 0, 73, 23);
        Button btnBrowse = new Button(cmpExport, 0);
        btnBrowse.setText(GBase8aMessages.editors_export_object_title_find);
        new Label(cmpExport, 0);
        btnBrowse.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(GBaseupExportObjectEditor.this.container.getShell(), 8194);
                String[] filterExtensions = new String[]{"*.sql"};
                dialog.setFilterExtensions(filterExtensions);
                dialog.setText(GBase8aMessages.editors_export_object_title_save_as);
                dialog.setFilterPath(GBaseupExportObjectEditor.this.fFilterPath);
                dialog.open();
                String[] names = dialog.getFileNames();
                if (names != null && names.length > 0) {
                    GBaseupExportObjectEditor.this.fFilterPath = dialog.getFilterPath();
                    GBaseupExportObjectEditor.this.txtAddr.setText(GBaseupExportObjectEditor.this.fFilterPath + File.separator + names[0]);
                }

            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        Button btnExport = new Button(cmpExport, 0);
        btnExport.setText(GBase8aMessages.editors_export_object_title);
        btnExport.setLayoutData(new GridData(131072, 16777216, false, false, 2, 1));
        btnExport.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                TableItem[] tableItem = GBaseupExportObjectEditor.this.table.getSelection();
                if (GBaseupExportObjectEditor.this.table.getSelectionCount() < 1) {
                    MessageDialog.openWarning(GBaseupExportObjectEditor.this.container.getShell(), GBase8aMessages.message_information_title, GBase8aMessages.editors_export_object_select_object);
                } else {
                    GBaseupExportObjectEditor.this.fileAddr = GBaseupExportObjectEditor.this.txtAddr.getText().trim();
                    if (!GBase8aIEUtil.isFilePathValid(GBaseupExportObjectEditor.this.fileAddr)) {
                        MessageDialog.openWarning(GBaseupExportObjectEditor.this.container.getShell(), GBase8aMessages.message_information_title, GBaseupExportObjectEditor.this.fileAddr + GBase8aMessages.editors_export_object_select_correct_path);
                    } else {
                        int i = 0;

                        int len;
                        for (len = tableItem.length; i < len; ++i) {
                            String tableName = ((GBase8aIEClusterObject) tableItem[i].getData()).getObjectName().trim();
                            String type = ((GBase8aIEClusterObject) tableItem[i].getData()).getType();
                            boolean isObjExist = false;

                            try {
                                isObjExist = GBase8aIEUtil.isObjectExist(GBaseupExportObjectEditor.this.curSelDatabase, tableName, GBaseupExportObjectEditor.this.session, type);
                            } catch (Exception er) {
                                MessageDialog.openInformation(GBaseupExportObjectEditor.this.container.getShell(),
                                        GBase8aMessages.message_information_title, GBase8aMessages.editors_export_object_export_error + er.getMessage());
                                return;
                            }

                            if (!isObjExist) {
                                MessageDialog.openWarning(GBaseupExportObjectEditor.this.container.getShell(), GBase8aMessages.message_information_title, GBase8aMessages.editors_export_object_select_select_object + tableName.toUpperCase() + GBase8aMessages.editors_export_object_select_had_delete);

                                try {
                                    GBaseupExportObjectEditor.this.databaseComboChange(GBaseupExportObjectEditor.this.curSelDatabase);
                                } catch (Exception er) {
                                    log.error(er);
                                }
                                return;
                            }
                        }
                        GBaseupExportObjectEditor.this.selectedObjectList.clear();
                        i = 0;

                        for (len = tableItem.length; i < len; ++i) {
                            GBase8aIEClusterObject culsterObject = (GBase8aIEClusterObject) tableItem[i].getData();
                            GBaseupExportObjectEditor.this.selectedObjectList.add(culsterObject);
                        }

                        File file = new File(GBaseupExportObjectEditor.this.fileAddr);
                        if (file.exists()) {
                            boolean confirm = MessageDialog.openConfirm(GBaseupExportObjectEditor.this.container.getShell(), GBase8aMessages.editors_export_object_title_confirm, GBase8aMessages.editors_export_object_title_file + GBaseupExportObjectEditor.this.fileAddr + GBase8aMessages.editors_export_object_title_had_cover);
                            if (!confirm) {
                                return;
                            }
                        }

                        GBaseupExportObjectEditor.this.exportSingleFile = GBaseupExportObjectEditor.this.btnSingle.getSelection();

                        try {
                            IRunnableWithProgress ldp = new IRunnableWithProgress() {
                                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                                    monitor.setTaskName("exporting...");
                                    monitor.beginTask("exporting...", -1);

                                    try {
                                        GBaseupExportObjectEditor.this.exportObjectData();
                                        GBaseupExportObjectEditor.this.isSuccess = true;
                                    } catch (Exception var6) {
                                        Exception e = var6;
                                        GBaseupExportObjectEditor.this.isSuccess = false;
                                        GBaseupExportObjectEditor.this.errorMessage = e.getMessage();
                                    } finally {
                                        monitor.done();
                                    }

                                }
                            };
                            ProgressMonitorDialog pg = new ProgressMonitorDialog(GBaseupExportObjectEditor.this.container.getParent().getShell());
                            pg.run(true, true, ldp);
                            if (GBaseupExportObjectEditor.this.isSuccess) {
                                MessageDialog.openInformation(GBaseupExportObjectEditor.this.container.getParent().getShell(), GBase8aMessages.message_information_title, GBase8aMessages.editors_export_object_export_success);
                            } else if (!GBaseupExportObjectEditor.this.errorMessage.isEmpty()) {
                                MessageDialog.openInformation(GBaseupExportObjectEditor.this.container.getParent().getShell(), GBase8aMessages.message_information_title, GBase8aMessages.editors_export_object_export_error + GBaseupExportObjectEditor.this.errorMessage);
                            }
                        } catch (Exception er) {
                            MessageDialog.openWarning(GBaseupExportObjectEditor.this.container.getParent().getShell(), GBase8aMessages.message_error_title, GBase8aMessages.editors_export_object_server_error);
                        }

                    }
                }
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
    }

    private void initDatabaseCombo() {
        try {
            Collection<GBase8aCatalog> databases = this.datasource.getCatalogs();
            this.databaseCombo.removeAll();
            int count = 0;
            int databaseIndex = 0;

            for (Iterator<GBase8aCatalog> var5 = databases.iterator(); var5.hasNext(); ++count) {
                GBase8aCatalog node = var5.next();
                String catalog = node.getName();
                this.databaseCombo.add(catalog);
                String dbName = this.DBName;

                try {
                    dbName = this.DBName.split("\\.")[1];
                } catch (Exception var8) {
                }

                if (catalog.equalsIgnoreCase(dbName)) {
                    databaseIndex = count;
                }
            }

            this.databaseCombo.select(databaseIndex);
            this.databaseComboChange(this.databaseCombo.getItem(this.databaseCombo.getSelectionIndex()));
        } catch (Exception er) {
            MessageDialog.openInformation(this.container.getParent().getShell(), GBase8aMessages.message_information_title,
                    GBase8aMessages.editors_export_object_get_data_fail + er.getMessage());
        }

    }

    private void addDatabaseComboSelListener() {
        this.databaseCombo.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                String selDBName = GBaseupExportObjectEditor.this.databaseCombo.getItem(GBaseupExportObjectEditor.this.databaseCombo.getSelectionIndex());

                try {
                    GBaseupExportObjectEditor.this.databaseComboChange(selDBName);
                } catch (Exception er) {
                    MessageDialog.openInformation(GBaseupExportObjectEditor.this.container.getParent().getShell(),
                            GBase8aMessages.message_information_title, GBase8aMessages.editors_export_object_get_table_fail + er.getMessage());
                }

            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
    }

    private void databaseComboChange(String selDBName) throws Exception {
        try (JDBCStatement statement = this.session.createStatement()) {
            statement.execute("use " + selDBName + ";");
        }
        this.curSelDatabase = selDBName;
        this.clusterObjectList.clear();
        GBase8aIEUtil.getClusterTableNames(selDBName, this.session, this.clusterObjectList);
        GBase8aIEUtil.getClusterViewNames(selDBName, this.session, this.clusterObjectList);
        GBase8aIEUtil.getClusterProcedureNames(selDBName, this.session, this.clusterObjectList);
        GBase8aIEUtil.getClusterFunctionNames(selDBName, this.session, this.clusterObjectList);
        this.tableViewer.setInput(this.clusterObjectList);
    }

    private void exportObjectData() throws Exception {
        int i;
        int size;
        GBase8aIEClusterObject culsterObject;
        String createFunctionScript;
        if (this.exportSingleFile) {
            StringBuilder writeSqlBuffer = new StringBuilder("");
            i = 0;

            for (size = this.selectedObjectList.size(); i < size; ++i) {
                culsterObject = (GBase8aIEClusterObject) this.selectedObjectList.get(i);
                if (culsterObject.getType().equals("TABLE")) {
                    createFunctionScript = GBase8aIEUtil.genScriptOfTable(this.curSelDatabase, culsterObject.getObjectName(), this.session);
                    writeSqlBuffer.append(createFunctionScript);
                    writeSqlBuffer.append(";");
                    writeSqlBuffer.append("\n");
                } else if (culsterObject.getType().equals("VIEW")) {
                    createFunctionScript = GBase8aIEUtil.genScriptOfView(this.curSelDatabase, culsterObject.getObjectName(), this.session);
                    writeSqlBuffer.append(createFunctionScript);
                    writeSqlBuffer.append(";");
                    writeSqlBuffer.append("\n");
                } else if (culsterObject.getType().equals("PROCEDURE")) {
                    createFunctionScript = GBase8aIEUtil.genScriptOfProcedure(this.curSelDatabase, culsterObject.getObjectName(), this.session);
                    writeSqlBuffer.append(createFunctionScript);
                    writeSqlBuffer.append("\n");
                } else if (culsterObject.getType().equals("FUNCTION")) {
                    createFunctionScript = GBase8aIEUtil.genScriptOfFunction(this.curSelDatabase, culsterObject.getObjectName(), this.session);
                    writeSqlBuffer.append(createFunctionScript);
                    writeSqlBuffer.append("\n");
                }
            }

            GBase8aIEUtil.writeFile(writeSqlBuffer.toString(), this.fileAddr, "UTF-8");
        } else {
            String filePath = this.fileAddr.substring(0, this.fileAddr.lastIndexOf(File.separator));
            i = 0;

            for (size = this.selectedObjectList.size(); i < size; ++i) {
                culsterObject = (GBase8aIEClusterObject) this.selectedObjectList.get(i);
                if (culsterObject.getType().equals("TABLE")) {
                    createFunctionScript = GBase8aIEUtil.genScriptOfTable(this.curSelDatabase, culsterObject.getObjectName(), this.session);
                    GBase8aIEUtil.writeFile(createFunctionScript, filePath + "/" + culsterObject.getObjectName() + ".sql", "UTF-8");
                } else if (culsterObject.getType().equals("VIEW")) {
                    createFunctionScript = GBase8aIEUtil.genScriptOfView(this.curSelDatabase, culsterObject.getObjectName(), this.session);
                    GBase8aIEUtil.writeFile(createFunctionScript, filePath + "/" + culsterObject.getObjectName() + ".sql", "UTF-8");
                } else if (culsterObject.getType().equals("PROCEDURE")) {
                    createFunctionScript = GBase8aIEUtil.genScriptOfProcedure(this.curSelDatabase, culsterObject.getObjectName(), this.session);
                    GBase8aIEUtil.writeFile(createFunctionScript, filePath + "/" + culsterObject.getObjectName() + ".sql", "UTF-8");
                } else if (culsterObject.getType().equals("FUNCTION")) {
                    createFunctionScript = GBase8aIEUtil.genScriptOfFunction(this.curSelDatabase, culsterObject.getObjectName(), this.session);
                    GBase8aIEUtil.writeFile(createFunctionScript, filePath + "/" + culsterObject.getObjectName() + ".sql", "UTF-8");
                }
            }
        }

    }

    public void doSave(IProgressMonitor monitor) {
    }

    public void doSaveAs() {
    }

    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        this.setSite(site);
        this.setInput(input);
        this.setPartName(input.getName());
    }

    public boolean isDirty() {
        return false;
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void setFocus() {
        this.container.setFocus();
    }

    public void setTitleToolTip(String toolTipName) {
        super.setTitleToolTip(toolTipName);
    }

    public String getTitleToolTip() {
        return this.getTitle();
    }

    private class LProvider extends LabelProvider implements ITableLabelProvider {
        private LProvider() {
        }

        public Image getColumnImage(Object arg0, int arg1) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            GBase8aIEClusterObject object = (GBase8aIEClusterObject) element;
            if (columnIndex == 0) {
                return object.getObjectName();
            } else {
                return columnIndex == 1 ? object.getType() : "";
            }
        }
    }

}