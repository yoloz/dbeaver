//package org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata;
//
//import org.jkiss.dbeaver.Log;
//import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDBProcess;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTable;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableColumn;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aVC;
//import org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata.model.ColumnModel;
//import org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata.model.DateFormatModel;
//import org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata.model.FieldModel;
//import org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata.model.TablePreviewModel;
//import org.jkiss.dbeaver.model.DBPDataSource;
//import org.jkiss.dbeaver.model.DBUtils;
//import org.jkiss.dbeaver.model.exec.DBCException;
//import org.jkiss.dbeaver.model.exec.DBCResultSet;
//import org.jkiss.dbeaver.model.exec.DBCStatement;
//import org.jkiss.dbeaver.model.exec.DBCStatementType;
//import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
//import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
//import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
//import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
//import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
//import org.jkiss.dbeaver.model.runtime.VoidProgressMonitor;
//import de.kupzog.ktable.KTable;
//import de.kupzog.ktable.KTableModel;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.InputStreamReader;
//import java.lang.reflect.InvocationTargetException;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Iterator;
//
//import org.eclipse.core.filesystem.EFS;
//import org.eclipse.core.filesystem.IFileStore;
//import org.eclipse.core.runtime.IPath;
//import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.core.runtime.Path;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.jface.dialogs.ProgressMonitorDialog;
//import org.eclipse.jface.viewers.ArrayContentProvider;
//import org.eclipse.jface.viewers.ComboViewer;
//import org.eclipse.jface.viewers.IBaseLabelProvider;
//import org.eclipse.jface.viewers.IContentProvider;
//import org.eclipse.jface.viewers.LabelProvider;
//import org.eclipse.jface.viewers.ListViewer;
//import org.eclipse.swt.custom.SashForm;
//import org.eclipse.swt.events.ModifyEvent;
//import org.eclipse.swt.events.ModifyListener;
//import org.eclipse.swt.events.SelectionAdapter;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.events.SelectionListener;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Combo;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Control;
//import org.eclipse.swt.widgets.FileDialog;
//import org.eclipse.swt.widgets.Group;
//import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.Layout;
//import org.eclipse.swt.widgets.List;
//import org.eclipse.swt.widgets.Spinner;
//import org.eclipse.swt.widgets.TabFolder;
//import org.eclipse.swt.widgets.TabItem;
//import org.eclipse.swt.widgets.Text;
//import org.eclipse.swt.widgets.ToolBar;
//import org.eclipse.swt.widgets.ToolItem;
//import org.eclipse.ui.IEditorInput;
//import org.eclipse.ui.IEditorSite;
//import org.eclipse.ui.IWorkbench;
//import org.eclipse.ui.IWorkbenchPartSite;
//import org.eclipse.ui.IWorkbenchWindow;
//import org.eclipse.ui.PartInitException;
//import org.eclipse.ui.PlatformUI;
//import org.eclipse.ui.part.EditorPart;
//
//
//public class ImportTableDataEditor extends EditorPart {
//    Log log = Log.getLog(ImportTableDataEditor.class);
//
//    public static final String ID = "org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata.ImportTableDataEditor";
//    private Composite container;
//    private GBase8aDataSource dataSource;
//    private DBNDatabaseNode dbnDatabaseNode;
//    private GBase8aDBProcess dbProcess;
//    private String fileType;
//    private String vcName;
//    private String dbname;
//    private String tableName;
//    private int databaseIndex = 0;
//
//
//    private final String filePathSQL = "";
//
//
//    private final long fileWarningSize = 500L;
//
//    private String[] tables;
//
//    private Text txtCSVFile;
//
//    private Text txtEnclose;
//
//    private Text txtFeildTerminate;
//
//    private Text txtLineTerminate;
//
//    private Spinner spnColNum;
//
//    private Spinner spnRowNum;
//
//    private Button btnTitle;
//
//    private Combo cmbCharset;
//    private ListViewer listViewerField;
//    private List listField;
//    private Combo cmbVC;
//    private Combo cmbDb;
//    private Combo cmbTb;
//    private Spinner spnCommit;
//    private Button btnTruncate;
//    private Button autoMatchButton;
//    private ListViewer listViewerColumn;
//    private List listColumn;
//    private Combo cmbField;
//    private ComboViewer comboViewerField;
//    private Combo cmbFieldtype;
//    private ComboViewer comboViewerDateformat;
//    private Combo cmbDateformat;
//    private KTable tablePreview;
//    private TablePreviewModel tablePreviewModel;
//    private Button btnCSVImportToDb;
//    private Text txtCSVImportResult;
//    private String filePathCSV = "";
//
//    private String fFilterPath = "";
//
//    private final ParseOption parseOption = new ParseOption();
//
//    private DataParser dataParser = null;
//
//    private String[] fieldHeaders = new String[1];
//
//    private HashMap<Integer, FieldModel> fieldMap = new HashMap<>();
//
//    private final String[] fieldTypes = new String[]{"String",
//            "Number", "Date"};
//
//    private ArrayList<FieldModel> fieldList = new ArrayList<>();
//
//    private java.util.List<String[]> dataList = null;
//
//    private final int previewLine = 100;
//
//    private String oldDBName = "";
//    JDBCSession dbsession;
//    private GBase8aVC vc = null;
//    private DBRProgressMonitor monitor = null;
//    public static ArrayList<DateFormatModel> dateFormatList = new ArrayList<DateFormatModel>();
//
//    static {
//        dateFormatList.add(new DateFormatModel("yyyy-mm-dd", "yyyy-MM-dd",
//                "%Y-%m-%d"));
//        dateFormatList.add(new DateFormatModel("yyyy-mm-dd hh24:mi:ss",
//                "yyyy-MM-dd HH:mm:ss", "%Y-%m-%d %H:%M:%S"));
//        dateFormatList.add(new DateFormatModel("yyyy-mm-dd hh:mi:ss",
//                "yyyy-MM-dd KK:mm:ss", "%Y-%m-%d %I:%M:%S"));
//        dateFormatList.add(new DateFormatModel("yyyy/mm/dd", "yyyy/MM/dd",
//                "%Y/%m/%d"));
//        dateFormatList.add(new DateFormatModel("yyyy/mm/dd hh24:mi:ss",
//                "yyyy/MM/dd HH:mm:ss", "%Y/%m/%d %H:%M:%S"));
//        dateFormatList.add(new DateFormatModel("yyyy/mm/dd hh:mi:ss",
//                "yyyy/MM/dd KK:mm:ss", "%Y/%m/%d %I:%M:%S"));
//        dateFormatList.add(new DateFormatModel("yyyymmdd", "yyyyMMdd",
//                "%Y%m%d"));
//        dateFormatList.add(new DateFormatModel("yyyymmddhh24miss",
//                "yyyyMMddHHmmss", "%Y%m%d%H%M%S"));
//        dateFormatList.add(new DateFormatModel("yyyymmddhhmiss",
//                "yyyyMMddKKmmss", "%Y%m%d%I%M%S"));
//    }
//
//
//    public void doSave(IProgressMonitor monitor) {
//    }
//
//
//    public void doSaveAs() {
//    }
//
//
//    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
//        setSite(site);
//        setInput(input);
//        setPartName(input.getName());
//        this.vcName = ((ImportTableDataEditorInput) input).getVcName();
//        this.dbname = ((ImportTableDataEditorInput) input).getDbname();
//        this.dataSource = ((ImportTableDataEditorInput) input).getDataSource();
//        this.dbnDatabaseNode = ((ImportTableDataEditorInput) input).getDBNDatabaseNode();
//        this.tableName = ((ImportTableDataEditorInput) input).getTableName();
//        this.oldDBName = this.dbname;
//        this.parseOption.setOldDBName(this.oldDBName);
//        try {
//            this.dbsession = DBUtils.openUtilSession(new VoidProgressMonitor(), this.dataSource, this.dbname);
//        } catch (DBCException e) {
//            throw new PartInitException("", e);
//        }
//        this.dbProcess = this.dataSource.getDbProcess();
//        this.vc = this.dataSource.getVc(this.vcName);
//        this.monitor = this.dataSource.getMonitor();
//    }
//
//
//    public boolean isDirty() {
//        return false;
//    }
//
//
//    public boolean isSaveAsAllowed() {
//        return false;
//    }
//
//
//    public void createPartControl(Composite parent) {
//        this.container = new Composite(parent, 0);
//        this.container.setLayout(new GridLayout(1, false));
//
//        TabFolder tabFolder = new TabFolder(this.container, 0);
//        tabFolder.setLayoutData(new GridData(4, 4, true, true, 1,
//                1));
//
//        TabItem tbtmCSV = new TabItem(tabFolder, 0);
//        tbtmCSV.setText(GBase8aMessages.import_properties_import_file);
//
//        Composite cmpCSV = new Composite(tabFolder, 0);
//        tbtmCSV.setControl(cmpCSV);
//        cmpCSV.setLayout(new GridLayout(1, false));
//
//        Composite cmpCSVToolBar = new Composite(cmpCSV, 0);
//        cmpCSVToolBar.setLayout(new GridLayout(1, false));
//        cmpCSVToolBar.setLayoutData(new GridData(4, 4, true,
//                false, 1, 1));
//
//        ToolBar toolBar = new ToolBar(cmpCSVToolBar, 8519680);
//        toolBar.setLayoutData(new GridData(4, 16777216, true, false,
//                1, 1));
//
//        ToolItem tltmOpenfile = new ToolItem(toolBar, 0);
//        tltmOpenfile.setImage(SWTResourceManager.getImage(
//                ImportTableDataEditor.class, "/icons/open.gif"));
//        tltmOpenfile.setText(GBase8aMessages.import_properties_open);
//        tltmOpenfile.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                FileDialog dialog = new FileDialog(ImportTableDataEditor.this.container.getShell(),
//                        4098);
//                String[] filterExtensions = ImportTableDataEditor.this.dbProcess.getFilterExtensions();
//
//                dialog.setFilterExtensions(filterExtensions);
//                dialog.setText(GBase8aMessages.import_properties_openfile);
//                dialog.setFilterPath(ImportTableDataEditor.this.fFilterPath);
//                dialog.open();
//                String name = dialog.getFileName();
//
//                ImportTableDataEditor.this.fileType = name.substring(name.lastIndexOf(".") + 1);
//                ImportTableDataEditor.this.txtCSVFile.setText("");
//                if (ImportTableDataEditor.this.fileType.equalsIgnoreCase("TXT")) {
//                    if (ImportTableDataEditor.this.txtLineTerminate.getText().isEmpty()) {
//                        MessageDialog.openWarning(ImportTableDataEditor.this.container.getShell(),
//                                GBase8aMessages.import_properties_tip, GBase8aMessages.import_properties_txt_notnull);
//                        ImportTableDataEditor.this.setOptionEnable(true);
//                        return;
//                    }
//                } else {
//                    ImportTableDataEditor.this.txtEnclose.setText("");
//                    ImportTableDataEditor.this.txtFeildTerminate.setText("");
//                    ImportTableDataEditor.this.txtLineTerminate.setText("");
//                }
//
//                ImportTableDataEditor.this.setOptionEnable(!ImportTableDataEditor.this.fileType.equalsIgnoreCase("XLS") && !ImportTableDataEditor.this.fileType.equalsIgnoreCase("XLSX"));
//                if (name != null) {
//                    ImportTableDataEditor.this.fFilterPath = dialog.getFilterPath();
//
//                    IFileStore fileStore = EFS.getLocalFileSystem().getStore(
//                            new Path(ImportTableDataEditor.this.fFilterPath));
//                    fileStore = fileStore.getChild(name);
//
//                    if (!fileStore.fetchInfo().isDirectory() &&
//                            fileStore.fetchInfo().exists()) {
//                        ImportTableDataEditor.this.filePathCSV = fileStore.toURI().getPath();
//                        try {
//                            if (!ImportTableDataEditor.this.fileType.equalsIgnoreCase("XLS") && !ImportTableDataEditor.this.fileType.equalsIgnoreCase("XLSX")) {
//                                String filePreviewStr = ImportTableDataEditor.this.readCsvFilePreviewToString();
//                                ImportTableDataEditor.this.txtCSVFile.setText(filePreviewStr);
//                            }
//                        } catch (Exception e1) {
//                            log.error(e1);
//                        }
//                        ImportTableDataEditor.this.parseOption.setEnclose(ImportTableDataEditor.this.txtEnclose.getText());
//                        ImportTableDataEditor.this.parseOption.setFieldTerminate(ImportTableDataEditor.this.txtFeildTerminate.getText());
//                        ImportTableDataEditor.this.parseOption.setLineTerminate(ImportTableDataEditor.this.txtLineTerminate.getText());
//
//                        ImportTableDataEditor.this.parseOption.setPreviewLine(100);
//                        ImportTableDataEditor.this.dataParser = new DataParser();
//
//                        if (ImportTableDataEditor.this.fileType != null && ImportTableDataEditor.this.fileType.equalsIgnoreCase("SHP")) {
//                            ImportTableDataEditor.this.autoMatchButton.setSelection(false);
//                        }
//
//
//                        try {
//                            ImportTableDataEditor.this.dataList = ImportTableDataEditor.this.dataParser.parseToList(ImportTableDataEditor.this.filePathCSV,
//                                    ImportTableDataEditor.this.parseOption, 100, ImportTableDataEditor.this.container);
//                        } catch (Exception e1) {
//                            MessageDialog.openWarning(ImportTableDataEditor.this.container.getShell(),
//                                    GBase8aMessages.import_properties_tip, e1.getMessage());
//                            return;
//                        }
//                        int colNum = ImportTableDataEditor.this.dataParser.getColSize();
//                        int rowNum = (ImportTableDataEditor.this.dataParser.getRowSize() > 100) ? 100 :
//                                ImportTableDataEditor.this.dataParser.getRowSize();
//
//                        ImportTableDataEditor.this.fieldList = new ArrayList();
//
//                        if (ImportTableDataEditor.this.parseOption.isHasHeader()) {
//                            ImportTableDataEditor.this.fieldHeaders = ImportTableDataEditor.this.dataParser.getHeaders();
//                            for (int i = 0; i < ImportTableDataEditor.this.fieldHeaders.length; i++) {
//                                FieldModel fieldModel = new FieldModel();
//                                fieldModel.setFieldIndex(i + 1);
//                                fieldModel.setFieldName(ImportTableDataEditor.this.fieldHeaders[i]);
//                                ImportTableDataEditor.this.fieldList.add(fieldModel);
//                            }
//                        } else {
//                            ImportTableDataEditor.this.fieldHeaders = new String[ImportTableDataEditor.this.dataParser.getColSize()];
//                            for (int i = 0; i < ImportTableDataEditor.this.dataParser.getColSize(); i++) {
//                                ImportTableDataEditor.this.fieldHeaders[i] = "Field" + (i + 1);
//                                FieldModel fieldModel = new FieldModel();
//                                fieldModel.setFieldIndex(i + 1);
//                                fieldModel.setFieldName(ImportTableDataEditor.this.fieldHeaders[i]);
//                                ImportTableDataEditor.this.fieldList.add(fieldModel);
//                            }
//                        }
//
//                        ImportTableDataEditor.this.listViewerField.setInput(ImportTableDataEditor.this.fieldList);
//                        ImportTableDataEditor.this.listViewerColumn.setInput(ImportTableDataEditor.this.fieldList);
//
//
//                        int colSize = ImportTableDataEditor.this.dataParser.getColSize();
//                        ImportTableDataEditor.this.spnColNum.setMaximum(colSize);
//                        ImportTableDataEditor.this.spnColNum.setMinimum((colSize > 0) ? 1 : 0);
//                        ImportTableDataEditor.this.spnColNum.setSelection(colSize);
//
//                        int rowSize = ImportTableDataEditor.this.dataParser.getRowSize();
//                        ImportTableDataEditor.this.spnRowNum.setMaximum(rowSize);
//                        ImportTableDataEditor.this.spnRowNum.setMinimum((rowSize > 0) ? 1 : 0);
//                        ImportTableDataEditor.this.spnRowNum.setSelection(rowSize);
//
//                        ImportTableDataEditor.this.parseOption.setColumnNum(colSize);
//                        ImportTableDataEditor.this.parseOption.setRowNum(rowSize);
//
//
//                        ImportTableDataEditor.this.cmbField.deselectAll();
//                        ImportTableDataEditor.this.cmbFieldtype.deselectAll();
//                        ImportTableDataEditor.this.cmbDateformat.deselectAll();
//                        ImportTableDataEditor.this.cmbDateformat.setEnabled(false);
//                        ImportTableDataEditor.this.listField.deselectAll();
//                        ImportTableDataEditor.this.listColumn.deselectAll();
//
//
//                        ImportTableDataEditor.this.fieldMap.clear();
//
//
//                        ImportTableDataEditor.this.tablePreviewModel = new TablePreviewModel(ImportTableDataEditor.this.dataList,
//                                ImportTableDataEditor.this.fieldHeaders, rowNum, colNum);
//                        ImportTableDataEditor.this.tablePreview.setModel(ImportTableDataEditor.this.tablePreviewModel);
//
//
//                        ImportTableDataEditor.this.btnTruncate.setSelection(false);
//                        ImportTableDataEditor.this.btnCSVImportToDb.setEnabled(false);
//
//
//                        ImportTableDataEditor.this.txtCSVImportResult.setText("");
//
//                        ImportTableDataEditor.this.cmbTb.setEnabled(true);
//                    }
//
//
//                    if (ImportTableDataEditor.this.autoMatchButton.getSelection()) {
//                        for (int i = 0; i < (ImportTableDataEditor.this.listColumn.getItems()).length && i < ImportTableDataEditor.this.comboViewerField.getCombo().getItemCount(); i++) {
//                            ColumnModel columnModel = (ColumnModel) ImportTableDataEditor.this.comboViewerField
//                                    .getElementAt(i);
//                            String columnLabel = columnModel.getColumnName() + " (" +
//                                    columnModel.getTypeName() + ")";
//                            ImportTableDataEditor.this.cmbField.setText(columnLabel);
//
//                            FieldModel fieldModel = ImportTableDataEditor.this.fieldList.get(i);
//                            fieldModel.setColumnModel(columnModel);
//
//                            String fieldType = DataImporter.dataTypeMap.get(columnModel
//                                    .getTypeName().toLowerCase());
//                            if (fieldType != null) {
//                                fieldModel.setFieldType(fieldType);
//                            }
//
//
//                            ImportTableDataEditor.this.fieldMap.put(Integer.valueOf(i), fieldModel);
//                            ImportTableDataEditor.this.listViewerColumn.setInput(ImportTableDataEditor.this.fieldList);
//                            ImportTableDataEditor.this.listColumn.select(i);
//
//                            ImportTableDataEditor.this.listColumn.showSelection();
//
//                            ImportTableDataEditor.this.btnCSVImportToDb.setEnabled((ImportTableDataEditor.this.fieldMap.size() > 0));
//                            ImportTableDataEditor.this.refreshListColumn();
//                        }
//                    }
//                }
//            }
//        });
//
//        SashForm sashForm = new SashForm(cmpCSV, 512);
//        sashForm.setLayoutData(new GridData(4, 4, true, true, 1,
//                1));
//
//        TabFolder tabFolderCSV = new TabFolder(sashForm, 0);
//
//        TabItem tbtmFile = new TabItem(tabFolderCSV, 0);
//        tbtmFile.setText(GBase8aMessages.import_properties_from_txt);
//
//        Composite cmpFile = new Composite(tabFolderCSV, 0);
//        tbtmFile.setControl(cmpFile);
//        cmpFile.setLayout(new GridLayout(1, false));
//
//        Group grpFileData = new Group(cmpFile, 0);
//        grpFileData.setText(GBase8aMessages.import_properties_file_data);
//        grpFileData.setLayoutData(new GridData(4, 4, true, true,
//                1, 1));
//        grpFileData.setLayout(new GridLayout(1, false));
//
//        this.txtCSVFile = new Text(grpFileData, 2826);
//
//        this.txtCSVFile.setLayoutData(new GridData(4, 4, true, true,
//                1, 1));
//
//        Group grpOption = new Group(cmpFile, 0);
//        grpOption.setText(GBase8aMessages.import_properties_set);
//        grpOption.setLayout(new GridLayout(2, false));
//        grpOption.setLayoutData(new GridData(4, 4, true, false,
//                1, 1));
//
//        Group grpNormal = new Group(grpOption, 0);
//        grpNormal.setLayoutData(new GridData(16384, 4, false, true,
//                1, 1));
//        grpNormal.setText(GBase8aMessages.import_properties_general);
//        grpNormal.setLayout(new GridLayout(2, false));
//
//        Label lblEnclose = new Label(grpNormal, 0);
//        lblEnclose.setText(GBase8aMessages.import_properties_enclose);
//        this.txtEnclose = new Text(grpNormal, 2048);
//        this.txtEnclose.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
//        this.txtEnclose.setFocus();
//
//        Label lblCloseIn = new Label(grpNormal, 0);
//        lblCloseIn.setText(GBase8aMessages.import_properties_field_terminate);
//        this.txtFeildTerminate = new Text(grpNormal, 2048);
//        this.txtFeildTerminate.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
//
//        Label lblLineTerminate = new Label(grpNormal, 0);
//        lblLineTerminate.setText(GBase8aMessages.import_properties_line_terminate);
//        this.txtLineTerminate = new Text(grpNormal, 2048);
//        this.txtLineTerminate.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
//
//        Label lblColNum = new Label(grpNormal, 0);
//        lblColNum.setText(GBase8aMessages.import_properties_column_num);
//
//        this.spnColNum = new Spinner(grpNormal, 2048);
//        this.spnColNum.setLayoutData(new GridData(4, 16777216, true, false,
//                1, 1));
//        this.spnColNum.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                super.widgetSelected(e);
//                int colNum = ImportTableDataEditor.this.spnColNum.getSelection();
//                int listFieldNum = ImportTableDataEditor.this.listField.getItemCount();
//                if (colNum < listFieldNum) {
//                    ImportTableDataEditor.this.listField.remove(colNum, listFieldNum - 1);
//                    ImportTableDataEditor.this.listColumn.remove(colNum, listFieldNum - 1);
//                    ImportTableDataEditor.this.fieldList.get(listFieldNum - 1).setColumnModel(null);
//                    ImportTableDataEditor.this.fieldList.get(listFieldNum - 1).setShow(false);
//                    ImportTableDataEditor.this.fieldMap.remove(Integer.valueOf(listFieldNum - 1));
//                } else if (colNum > listFieldNum) {
//                    ImportTableDataEditor.this.listField.setItems(ImportTableDataEditor.this.fieldHeaders);
//                    ImportTableDataEditor.this.fieldList.get(colNum - 1).setShow(true);
//                    ImportTableDataEditor.this.listViewerColumn.setInput(ImportTableDataEditor.this.fieldList);
//                    if (colNum < ImportTableDataEditor.this.fieldHeaders.length) {
//                        ImportTableDataEditor.this.listField.remove(colNum, ImportTableDataEditor.this.fieldHeaders.length - 1);
//                    }
//                }
//                ImportTableDataEditor.this.parseOption.setColumnNum(colNum);
//                try {
//                    ImportTableDataEditor.this.refreshCsvPreviewTable();
//                } catch (Exception e1) {
//                    log.error(e1);
//                }
//
//                if (ImportTableDataEditor.this.autoMatchButton.getSelection()) {
//                    for (int i = 0; i < (ImportTableDataEditor.this.listColumn.getItems()).length && i < ImportTableDataEditor.this.comboViewerField.getCombo().getItemCount(); i++) {
//                        ColumnModel columnModel = (ColumnModel) ImportTableDataEditor.this.comboViewerField
//                                .getElementAt(i);
//                        String columnLabel = columnModel.getColumnName() + " (" +
//                                columnModel.getTypeName() + ")";
//                        ImportTableDataEditor.this.cmbField.setText(columnLabel);
//
//                        FieldModel fieldModel = ImportTableDataEditor.this.fieldList.get(i);
//                        fieldModel.setColumnModel(columnModel);
//
//                        String fieldType = DataImporter.dataTypeMap.get(columnModel
//                                .getTypeName().toLowerCase());
//                        if (fieldType != null) {
//                            fieldModel.setFieldType(fieldType);
//                        }
//                        ImportTableDataEditor.this.fieldMap.put(Integer.valueOf(i), fieldModel);
//                        ImportTableDataEditor.this.listViewerColumn.setInput(ImportTableDataEditor.this.fieldList);
//                        ImportTableDataEditor.this.listColumn.select(i);
//
//                        ImportTableDataEditor.this.listColumn.showSelection();
//
//                        ImportTableDataEditor.this.btnCSVImportToDb.setEnabled((ImportTableDataEditor.this.fieldMap.size() > 0));
//                        ImportTableDataEditor.this.refreshListColumn();
//                    }
//                } else {
//                    for (int i = 0; i < (ImportTableDataEditor.this.listColumn.getItems()).length && i < ImportTableDataEditor.this.comboViewerField.getCombo().getItemCount(); i++) {
//                        ImportTableDataEditor.this.listViewerColumn.setInput(ImportTableDataEditor.this.fieldList);
//                        ImportTableDataEditor.this.listColumn.select(i);
//
//                        ImportTableDataEditor.this.listColumn.showSelection();
//                        ImportTableDataEditor.this.btnCSVImportToDb.setEnabled(false);
//                        ImportTableDataEditor.this.cmbField.setText("");
//                        ImportTableDataEditor.this.refreshListColumn();
//                    }
//                }
//            }
//        });
//
//        Label lblRow = new Label(grpNormal, 0);
//        lblRow.setText(GBase8aMessages.import_properties_line_num);
//
//        this.spnRowNum = new Spinner(grpNormal, 2048);
//        this.spnRowNum.setLayoutData(new GridData(4, 16777216, true, false,
//                1, 1));
//        this.spnRowNum.addModifyListener(new ModifyListener() {
//            public void modifyText(ModifyEvent e) {
//                int rowNum = ImportTableDataEditor.this.spnRowNum.getSelection();
//                ImportTableDataEditor.this.parseOption.setRowNum(rowNum);
//            }
//        });
//
//        this.btnTitle = new Button(grpNormal, 32);
//        this.btnTitle.setLayoutData(new GridData(16384, 16777216, true, false,
//                2, 1));
//        this.btnTitle.setText(GBase8aMessages.import_properties_title);
//        this.btnTitle.setSelection(true);
//        this.btnTitle.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                super.widgetSelected(e);
//                ImportTableDataEditor.this.parseOption.setHasHeader(ImportTableDataEditor.this.btnTitle.getSelection());
//
//                try {
//                    ImportTableDataEditor.this.refreshCsvPreviewTable();
//                } catch (Exception exception) {
//                    MessageDialog.openWarning(ImportTableDataEditor.this.container.getShell(), GBase8aMessages.import_properties_tip,
//                            GBase8aMessages.import_properties_parse_error);
//                }
//
//                for (int i = 0; i < ImportTableDataEditor.this.fieldList.size(); i++) {
//                    FieldModel oldFieldModel = ImportTableDataEditor.this.fieldMap.get(Integer.valueOf(i));
//                    if (oldFieldModel != null) {
//                        ImportTableDataEditor.this.fieldList.get(i).setColumnModel(
//                                oldFieldModel.getColumnModel());
//                        oldFieldModel = ImportTableDataEditor.this.fieldList.get(i);
//                    }
//                }
//                ImportTableDataEditor.this.listViewerColumn.setInput(ImportTableDataEditor.this.fieldList);
//                ImportTableDataEditor.this.refreshListColumn();
//            }
//        });
//
//        Label lblCharset = new Label(grpNormal, 0);
//        lblCharset.setText(GBase8aMessages.import_properties_charset);
//
//        this.cmbCharset = new Combo(grpNormal, 0);
//        this.cmbCharset.setLayoutData(new GridData(4, 16777216, true,
//                false, 1, 1));
//        this.cmbCharset.setItems(IConstants.CHARSET_ARRAY);
//        this.cmbCharset.select(0);
//        this.cmbCharset.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                super.widgetSelected(e);
//                ImportTableDataEditor.this.parseOption.setCharset(ImportTableDataEditor.this.cmbCharset.getSelectionIndex());
//                try {
//                    ImportTableDataEditor.this.refreshCsvPreviewTable();
//                } catch (Exception exception) {
//                    MessageDialog.openWarning(ImportTableDataEditor.this.container.getShell(), GBase8aMessages.import_properties_tip,
//                            GBase8aMessages.import_properties_parse_error);
//                }
//
//                for (int i = 0; i < ImportTableDataEditor.this.fieldList.size(); i++) {
//                    FieldModel oldFieldModel = ImportTableDataEditor.this.fieldMap.get(Integer.valueOf(i));
//                    if (oldFieldModel != null) {
//                        ImportTableDataEditor.this.fieldList.get(i).setColumnModel(
//                                oldFieldModel.getColumnModel());
//                        oldFieldModel = ImportTableDataEditor.this.fieldList.get(i);
//                    }
//                }
//                ImportTableDataEditor.this.listViewerColumn.setInput(ImportTableDataEditor.this.fieldList);
//                ImportTableDataEditor.this.refreshListColumn();
//            }
//        });
//
//        this.listViewerField = new ListViewer(grpOption, 2816);
//
//        this.listField = this.listViewerField.getList();
//        this.listField.setLayoutData(new GridData(4, 4, true, false,
//                1, 1));
//        this.listViewerField.setLabelProvider(new LabelProvider() {
//            public String getText(Object element) {
//                FieldModel fieldModel = (FieldModel) element;
//                return fieldModel.getFieldName();
//            }
//        });
//        this.listViewerField.setContentProvider(new ArrayContentProvider() {
//            public Object[] getElements(Object inputElement) {
//                java.util.List<FieldModel> list = (java.util.List<FieldModel>) inputElement;
//                return list.toArray();
//            }
//        });
//
//        this.listField.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                super.widgetSelected(e);
//                int selectIndex = ImportTableDataEditor.this.listField.getSelectionIndex();
//                if (ImportTableDataEditor.this.dataParser.getRowSize() > 0) {
//                    ImportTableDataEditor.this.tablePreview.setSelection(selectIndex, 1, true);
//                    ImportTableDataEditor.this.listColumn.setSelection(selectIndex);
//                }
//            }
//        });
//
//        TabItem tbtmDatabase = new TabItem(tabFolderCSV, 0);
//        tbtmDatabase.setText(GBase8aMessages.import_properties_to_DB);
//
//        Composite cmpDatabase = new Composite(tabFolderCSV, 0);
//        tbtmDatabase.setControl(cmpDatabase);
//        cmpDatabase.setLayout(new GridLayout(1, false));
//
//        Group grpDbcmmn = new Group(cmpDatabase, 0);
//        grpDbcmmn.setLayout(new GridLayout(6, false));
//        grpDbcmmn.setLayoutData(new GridData(4, 16777216, true, false,
//                1, 1));
//        grpDbcmmn.setText(GBase8aMessages.import_properties_general);
//
//        Label lblVc = new Label(grpDbcmmn, 0);
//        lblVc.setLayoutData(new GridData(4, 16777216, false, false, 1,
//                1));
//        lblVc.setText(GBase8aMessages.import_properties_vc);
//
//        this.cmbVC = new Combo(grpDbcmmn, 8);
//        this.cmbVC.setLayoutData(new GridData(4, 16777216, true, false, 1,
//                1));
//
//        Label lblDb = new Label(grpDbcmmn, 0);
//        lblDb.setLayoutData(new GridData(4, 16777216, false, false, 1,
//                1));
//        lblDb.setText(GBase8aMessages.import_properties_DB);
//
//        this.cmbDb = new Combo(grpDbcmmn, 8);
//        this.cmbDb.setLayoutData(new GridData(4, 16777216, true, false, 1,
//                1));
//
//        Label lblTb = new Label(grpDbcmmn, 0);
//        lblTb.setLayoutData(new GridData(4, 16777216, false, false, 1,
//                1));
//        lblTb.setText(GBase8aMessages.import_properties_table);
//
//        this.cmbTb = new Combo(grpDbcmmn, 8);
//        this.cmbTb.setLayoutData(new GridData(4, 16777216, true, false, 1,
//                1));
//
//
//        Label lblCommit = new Label(grpDbcmmn, 0);
//        lblCommit.setLayoutData(new GridData(4, 16777216, false,
//                false, 1, 1));
//        lblCommit.setText("ÿ���ύ");
//        lblCommit.setVisible(false);
//
//
//        this.spnCommit = new Spinner(grpDbcmmn, 2048);
//        this.spnCommit.setLayoutData(new GridData(4, 16777216, true, false,
//                1, 1));
//        this.spnCommit.setVisible(false);
//        this.spnCommit.setEnabled(false);
//
//
//        Composite cmpCreateTable = new Composite(grpDbcmmn, 0);
//        cmpCreateTable.setLayoutData(new GridData(131072, 16777216, false,
//                false, 1, 1));
//        cmpCreateTable.setLayout(new GridLayout(5, false));
//
//        this.btnTruncate = new Button(cmpCreateTable, 32);
//        this.btnTruncate.setLayoutData(new GridData(131072, 16777216, false,
//                false, 1, 1));
//        this.btnTruncate.setText(GBase8aMessages.import_properties_clear);
//
//        this.autoMatchButton = new Button(cmpCreateTable, 32);
//        this.autoMatchButton.setLayoutData(new GridData(131072, 16777216, false,
//                false, 1, 1));
//        this.autoMatchButton.setText(GBase8aMessages.import_properties_auto_match);
//        this.autoMatchButton.setSelection(true);
//        this.autoMatchButton.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                if (ImportTableDataEditor.this.autoMatchButton.getSelection()) {
//
//                    for (int i = 0; i < (ImportTableDataEditor.this.listColumn.getItems()).length && i < ImportTableDataEditor.this.comboViewerField.getCombo().getItemCount(); i++) {
//                        ColumnModel columnModel = (ColumnModel) ImportTableDataEditor.this.comboViewerField
//                                .getElementAt(i);
//                        String columnLabel = columnModel.getColumnName() + " (" +
//                                columnModel.getTypeName() + ")";
//                        ImportTableDataEditor.this.cmbField.setText(columnLabel);
//
//                        FieldModel fieldModel = ImportTableDataEditor.this.fieldList.get(i);
//                        fieldModel.setColumnModel(columnModel);
//
//                        String fieldType = DataImporter.dataTypeMap.get(columnModel
//                                .getTypeName().toLowerCase());
//                        if (fieldType != null) {
//                            fieldModel.setFieldType(fieldType);
//                        }
//
//
//                        ImportTableDataEditor.this.fieldMap.put(Integer.valueOf(i), fieldModel);
//                        ImportTableDataEditor.this.listViewerColumn.setInput(ImportTableDataEditor.this.fieldList);
//                        ImportTableDataEditor.this.listColumn.select(i);
//
//                        ImportTableDataEditor.this.listColumn.showSelection();
//
//                        ImportTableDataEditor.this.btnCSVImportToDb.setEnabled((ImportTableDataEditor.this.fieldMap.size() > 0));
//                        ImportTableDataEditor.this.refreshListColumn();
//                    }
//                } else {
//
//                    for (int i = 0; i < (ImportTableDataEditor.this.listColumn.getItems()).length && i < ImportTableDataEditor.this.comboViewerField.getCombo().getItemCount(); i++) {
//
//
//                        ImportTableDataEditor.this.listViewerColumn.setInput(ImportTableDataEditor.this.fieldList);
//                        ImportTableDataEditor.this.listColumn.select(i);
//
//                        ImportTableDataEditor.this.listColumn.showSelection();
//                        ImportTableDataEditor.this.btnCSVImportToDb.setEnabled(false);
//                        ImportTableDataEditor.this.cmbField.setText("");
//                        ImportTableDataEditor.this.refreshListColumn();
//                    }
//                }
//            }
//        });
//
//        Group grpField = new Group(cmpDatabase, 0);
//        grpField.setLayout(new GridLayout(3, false));
//        grpField.setLayoutData(new GridData(4, 4, true, true, 1,
//                1));
//        grpField.setText(GBase8aMessages.import_properties_field);
//
//        this.listViewerColumn = new ListViewer(grpField, 2560);
//        this.listColumn = this.listViewerColumn.getList();
//        this.listColumn.setLayoutData(new GridData(4, 4, true, true,
//                1, 3));
//        this.listViewerColumn.setLabelProvider(new LabelProvider() {
//            public String getText(Object element) {
//                FieldModel fieldModel = (FieldModel) element;
//                String label = "";
//                if (fieldModel.getColumnModel() != null) {
//                    label = fieldModel.getFieldName() + " --> " +
//                            fieldModel.getColumnModel().getColumnName() +
//                            " (" + fieldModel.getColumnModel().getTypeName() +
//                            ")";
//                } else {
//                    label = fieldModel.getFieldName();
//                }
//                return label;
//            }
//        });
//
//        this.listViewerColumn.setContentProvider(new ArrayContentProvider() {
//            public Object[] getElements(Object inputElement) {
//                java.util.List<FieldModel> list = (java.util.List<FieldModel>) inputElement;
//                java.util.List<FieldModel> listToShow = new ArrayList<FieldModel>();
//                Iterator<FieldModel> iterator = list.iterator();
//                while (iterator
//                        .hasNext()) {
//                    FieldModel fieldModel = iterator.next();
//                    if (fieldModel.isShow()) {
//                        listToShow.add(fieldModel);
//                    }
//                }
//                return listToShow.toArray();
//            }
//        });
//
//        this.listColumn.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                super.widgetSelected(e);
//                int selectIndex = ImportTableDataEditor.this.listColumn.getSelectionIndex();
//                ImportTableDataEditor.this.cmbField.setEnabled((selectIndex >= 0));
//                ImportTableDataEditor.this.cmbFieldtype.setEnabled((selectIndex >= 0));
//                if (ImportTableDataEditor.this.dataParser.getRowSize() > 0) {
//                    ImportTableDataEditor.this.tablePreview.setSelection(selectIndex, 1, true);
//                    ImportTableDataEditor.this.listField.setSelection(selectIndex);
//                }
//
//                FieldModel fieldModelInMap = ImportTableDataEditor.this.fieldMap.get(Integer.valueOf(selectIndex));
//                ImportTableDataEditor.this.fieldList.get(selectIndex);
//                if (fieldModelInMap != null) {
//                    ColumnModel columnModel = fieldModelInMap.getColumnModel();
//                    String columnLabel = columnModel.getColumnName() + " (" +
//                            columnModel.getTypeName() + ")";
//                    ImportTableDataEditor.this.cmbField.setText(columnLabel);
//                    ImportTableDataEditor.this.cmbFieldtype.setText(fieldModelInMap.getFieldType());
//                    if (fieldModelInMap.getDateFormat() != null) {
//                        ImportTableDataEditor.this.cmbDateformat.setText(fieldModelInMap.getDateFormat()
//                                .getOraFormat());
//                    } else {
//
//                        ImportTableDataEditor.this.cmbDateformat.deselectAll();
//                    }
//
//                    if ("Date".equals(fieldModelInMap
//                            .getFieldType()) &&
//                            ImportTableDataEditor.this.cmbDateformat.getSelectionIndex() != -1) {
//                        ImportTableDataEditor.this.btnCSVImportToDb.setEnabled(true);
//                    }
//                } else {
//
//                    ImportTableDataEditor.this.cmbField.deselectAll();
//
//                    ImportTableDataEditor.this.cmbFieldtype.deselectAll();
//                    ImportTableDataEditor.this.cmbDateformat.deselectAll();
//                }
//                ImportTableDataEditor.this.cmbDateformat.setEnabled((selectIndex >= 0 &&
//                        "Date".equals(ImportTableDataEditor.this.cmbFieldtype
//                                .getText())));
//
//                ImportTableDataEditor.this.refreshListColumn();
//            }
//        });
//
//        Label lblField = new Label(grpField, 0);
//        lblField.setLayoutData(new GridData(131072, 16777216, false,
//                false, 1, 1));
//        lblField.setText(GBase8aMessages.import_properties_field);
//
//        this.comboViewerField = new ComboViewer(grpField, 0);
//        this.cmbField = this.comboViewerField.getCombo();
//        this.cmbField.setLayoutData(new GridData(4, 16777216, false, false,
//                1, 1));
//
//
//        this.comboViewerField.setLabelProvider(new LabelProvider() {
//            public String getText(Object element) {
//                ColumnModel columnModel = (ColumnModel) element;
//                return columnModel.getColumnName() + " (" +
//                        columnModel.getTypeName() + ")";
//            }
//        });
//        this.comboViewerField.setContentProvider(new ArrayContentProvider() {
//            public Object[] getElements(Object inputElement) {
//                ArrayList<ColumnModel> list = (ArrayList<ColumnModel>) inputElement;
//                return list.toArray();
//            }
//        });
//
//        this.cmbField.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                super.widgetSelected(e);
//                int listIndex = ImportTableDataEditor.this.listColumn.getSelectionIndex();
//                if (listIndex < 0)
//                    return;
//                ColumnModel columnModel = (ColumnModel) ImportTableDataEditor.this.comboViewerField
//                        .getElementAt(ImportTableDataEditor.this.cmbField.getSelectionIndex());
//
//                FieldModel fieldModel = ImportTableDataEditor.this.fieldList.get(listIndex);
//                fieldModel.setColumnModel(columnModel);
//                String fieldType = DataImporter.dataTypeMap.get(columnModel
//                        .getTypeName().toLowerCase());
//                if (fieldType != null) {
//                    fieldModel.setFieldType(fieldType);
//                    ImportTableDataEditor.this.cmbFieldtype.setText(fieldType);
//                }
//                ImportTableDataEditor.this.fieldMap.put(Integer.valueOf(listIndex), fieldModel);
//                ImportTableDataEditor.this.listViewerColumn.setInput(ImportTableDataEditor.this.fieldList);
//                ImportTableDataEditor.this.listColumn.select(listIndex);
//
//                ImportTableDataEditor.this.listColumn.showSelection();
//
//                ImportTableDataEditor.this.btnCSVImportToDb.setEnabled((ImportTableDataEditor.this.fieldMap.size() > 0));
//
//
//                ImportTableDataEditor.this.refreshListColumn();
//            }
//        });
//
//        this.cmbField.addModifyListener(new ModifyListener() {
//            public void modifyText(ModifyEvent e) {
//                int listIndex = ImportTableDataEditor.this.listColumn.getSelectionIndex();
//                if (listIndex < 0)
//                    return;
//                FieldModel fieldModelInList = ImportTableDataEditor.this.fieldList.get(listIndex);
//                FieldModel fieldModelInMap = ImportTableDataEditor.this.fieldMap.get(Integer.valueOf(listIndex));
//                if (fieldModelInMap == null) {
//                    return;
//                }
//                ColumnModel columnModel = fieldModelInMap.getColumnModel();
//
//                if (columnModel != null && ImportTableDataEditor.this.cmbField.getText().length() == 0) {
//
//
//                    fieldModelInList.setColumnModel(null);
//                    fieldModelInMap.setColumnModel(null);
//                    ImportTableDataEditor.this.fieldMap.remove(Integer.valueOf(listIndex));
//                    ImportTableDataEditor.this.listViewerColumn.setInput(ImportTableDataEditor.this.fieldList);
//                    ImportTableDataEditor.this.listColumn.select(listIndex);
//                    ImportTableDataEditor.this.cmbDateformat.setEnabled(false);
//                    ImportTableDataEditor.this.btnCSVImportToDb.setEnabled((ImportTableDataEditor.this.fieldMap.size() > 0));
//                    ImportTableDataEditor.this.refreshListColumn();
//                }
//            }
//        });
//
//        Label lblFiledtype = new Label(grpField, 0);
//        lblFiledtype.setLayoutData(new GridData(131072, 16777216, false,
//                false, 1, 1));
//        lblFiledtype.setText(GBase8aMessages.import_properties_field_type);
//        lblFiledtype.setVisible(false);
//        this.cmbFieldtype = new Combo(grpField, 8);
//
//        this.cmbFieldtype.setVisible(false);
//        this.cmbFieldtype.setLayoutData(new GridData(4, 16777216, false,
//                false, 1, 1));
//        this.cmbFieldtype.setItems(this.fieldTypes);
//
//
//        this.cmbFieldtype.addModifyListener(new ModifyListener() {
//            public void modifyText(ModifyEvent e) {
//                if (ImportTableDataEditor.this.cmbFieldtype.getSelectionIndex() == -1) {
//                    return;
//                }
//                int listIndex = ImportTableDataEditor.this.listColumn.getSelectionIndex();
//                if (listIndex == -1) {
//                    return;
//                }
//                FieldModel fieldModel = ImportTableDataEditor.this.fieldList.get(listIndex);
//                fieldModel.setFieldType(ImportTableDataEditor.this.cmbFieldtype.getText());
//
//                if (fieldModel.getColumnModel() == null) {
//                    ImportTableDataEditor.this.cmbFieldtype.deselectAll();
//                    ImportTableDataEditor.this.cmbDateformat.setEnabled(false);
//                    ImportTableDataEditor.this.cmbDateformat.deselectAll();
//
//                    return;
//                }
//
//                ImportTableDataEditor.this.listViewerColumn.setInput(ImportTableDataEditor.this.fieldList);
//                ImportTableDataEditor.this.listColumn.select(listIndex);
//                if (ImportTableDataEditor.this.cmbFieldtype.getSelectionIndex() != -1) {
//                    if ("Date".equals(ImportTableDataEditor.this.cmbFieldtype
//                            .getText())) {
//                        if (fieldModel.getDateFormat() != null) {
//                            ImportTableDataEditor.this.cmbDateformat.setText(fieldModel.getDateFormat()
//                                    .getOraFormat());
//
//                        }
//                    } else {
//
//                        ImportTableDataEditor.this.cmbDateformat.deselectAll();
//                    }
//                    ImportTableDataEditor.this.cmbDateformat.setEnabled("Date"
//                            .equals(ImportTableDataEditor.this.cmbFieldtype.getText()));
//                }
//                ImportTableDataEditor.this.refreshListColumn();
//            }
//        });
//
//        Label lblDateformat = new Label(grpField, 0);
//        lblDateformat.setLayoutData(new GridData(131072, 128, false,
//                false, 1, 1));
//        lblDateformat.setText(GBase8aMessages.import_properties_date_format);
//        lblDateformat.setVisible(false);
//        this.comboViewerDateformat = new ComboViewer(grpField, 8);
//        this.cmbDateformat = this.comboViewerDateformat.getCombo();
//        this.cmbDateformat.setLayoutData(new GridData(4, 128, true,
//                false, 1, 1));
//        this.cmbDateformat.setVisible(false);
//        this.comboViewerDateformat.setLabelProvider(new LabelProvider() {
//            public String getText(Object element) {
//                DateFormatModel model = (DateFormatModel) element;
//                return model.getOraFormat();
//            }
//        });
//        this.comboViewerDateformat.setContentProvider(new ArrayContentProvider() {
//            public Object[] getElements(Object inputElement) {
//                return ImportTableDataEditor.dateFormatList.toArray();
//            }
//        });
//        this.comboViewerDateformat.setInput(dateFormatList);
//
//        this.cmbDateformat.setEnabled(false);
//        this.cmbDateformat.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                super.widgetSelected(e);
//                int listIndex = ImportTableDataEditor.this.listColumn.getSelectionIndex();
//                FieldModel fieldModel = ImportTableDataEditor.this.fieldList.get(listIndex);
//                DateFormatModel dateFormatModel = (DateFormatModel) ImportTableDataEditor.this.comboViewerDateformat
//                        .getElementAt(ImportTableDataEditor.this.cmbDateformat.getSelectionIndex());
//                fieldModel.setDateFormat(dateFormatModel);
//                ImportTableDataEditor.this.refreshListColumn();
//            }
//        });
//
//        Group grpPreview = new Group(sashForm, 0);
//        grpPreview.setText(GBase8aMessages.import_properties_preview);
//        grpPreview.setLayout(new GridLayout(1, false));
//
//        this.tablePreview = new KTable(grpPreview, 2818);
//
//        this.tablePreview.setLayoutData(new GridData(4, 4, true, true,
//                1, 1));
//        this.tablePreviewModel = new TablePreviewModel();
//        this.tablePreview.setModel(this.tablePreviewModel);
//
//
//        sashForm.setWeights(3, 1);
//
//        Composite cmpCSVImportButton = new Composite(cmpCSV, 0);
//        cmpCSVImportButton.setLayoutData(new GridData(4, 16777216,
//                true, false, 1, 1));
//        cmpCSVImportButton.setLayout(new GridLayout(3, false));
//
//        this.btnCSVImportToDb = new Button(cmpCSVImportButton, 0);
//        this.btnCSVImportToDb.setText(GBase8aMessages.import_properties_import);
//        this.btnCSVImportToDb.setEnabled(false);
//        this.btnCSVImportToDb.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                super.widgetSelected(e);
//                ImportOption importOption = new ImportOption();
//                importOption.setClearTable(ImportTableDataEditor.this.btnTruncate.getSelection());
//                importOption.setRowNum(Integer.parseInt(ImportTableDataEditor.this.spnRowNum.getText()));
//                importOption.setVcName(ImportTableDataEditor.this.cmbVC.getText());
//                importOption.setDbName(ImportTableDataEditor.this.cmbDb.getText());
//                importOption.setTbName(ImportTableDataEditor.this.cmbTb.getText());
//                importOption.setDataFilePath(ImportTableDataEditor.this.filePathCSV);
//
//                IWorkbench workbench = PlatformUI.getWorkbench();
//                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
//                ImportTableDataEditor.this.getEditorInput();
//                DataImporter dataImporter = new DataImporter(window, ImportTableDataEditor.this.parseOption, ImportTableDataEditor.this.dataSource);
//
//
//                String new_tablename = "";
//                importOption.setNew_tablename(new_tablename);
//                HashMap<Integer, FieldModel> newFieldMap = new HashMap<>();
//
//                try {
//                    int colNum = ImportTableDataEditor.this.spnColNum.getSelection();
//                    if (colNum != 0) {
//                        for (int i = 0; i < colNum; i++) {
//                            newFieldMap.put(Integer.valueOf(i), ImportTableDataEditor.this.fieldMap.get(Integer.valueOf(i)));
//                        }
//                        ImportTableDataEditor.this.fieldMap = newFieldMap;
//                    }
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//
//                ImportTableDataProgress ictdp = new ImportTableDataProgress(
//                        dataImporter, ImportTableDataEditor.this.dataList, ImportTableDataEditor.this.fieldMap, importOption, true,
//                        ImportTableDataEditor.this.filePathCSV);
//                ProgressMonitorDialog pg = new ProgressMonitorDialog(ImportTableDataEditor.this.container
//                        .getShell());
//                try {
//                    pg.run(true, false, ictdp);
//
//                    if (ictdp.isSuccess()) {
//                        MessageDialog.openInformation(
//                                ImportTableDataEditor.this.container.getShell(),
//                                GBase8aMessages.import_properties_tip,
//                                GBase8aMessages.import_properties_import_complete +
//                                        "\n" +
//                                        GBase8aMessages.import_properties_import +
//                                        dataImporter.getImportedRowsNum() +
//                                        GBase8aMessages.import_properties_import_fail +
//                                        dataImporter.getErrorRowsNum() +
//                                        GBase8aMessages.import_properties_import_time +
//                                        dataImporter.getImportTime() + GBase8aMessages.import_properties_import_second);
//                        ImportTableDataEditor.this.txtCSVImportResult.setText(dataImporter
//                                .getImportResult());
//                    } else {
//                        MessageDialog.openError(ImportTableDataEditor.this.container.getShell(), GBase8aMessages.import_properties_tip,
//                                GBase8aMessages.import_properties_import_errormessage + ictdp.getStderrStr());
//                    }
//                } catch (InvocationTargetException | InterruptedException e1) {
//                    log.error(e1);
//                }
//            }
//        });
//
//
//        this.txtCSVImportResult = new Text(cmpCSVImportButton, 2056);
//
//        this.txtCSVImportResult.setLayoutData(new GridData(4, 16777216,
//                true, false, 1, 1));
//
//        initControl();
//        this.cmbVC.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                super.widgetSelected(e);
//                ImportTableDataEditor.this.vcName = ImportTableDataEditor.this.cmbVC.getText();
//                String[] dbs = null;
//
//                dbs = ImportTableDataEditor.this.getDBNames(ImportTableDataEditor.this.vcName);
//
//                ImportTableDataEditor.this.cmbDb.removeAll();
//                ImportTableDataEditor.this.cmbDb.setItems(dbs);
//                if (dbs.length > 0) {
//                    ImportTableDataEditor.this.cmbDb.select(0);
//                }
//            }
//        });
//
//
//        this.cmbDb.addModifyListener(new ModifyListener() {
//            public void modifyText(ModifyEvent e) {
//                ImportTableDataEditor.this.dbname = ImportTableDataEditor.this.cmbDb.getText();
//                java.util.List<String> list = ImportTableDataEditor.this.getTableNames(ImportTableDataEditor.this.vcName, ImportTableDataEditor.this.dbname);
//                ImportTableDataEditor.this.tables = new String[list.size()];
//                list.toArray(ImportTableDataEditor.this.tables);
//
//                ImportTableDataEditor.this.cmbTb.removeAll();
//                ImportTableDataEditor.this.cmbTb.clearSelection();
//                ImportTableDataEditor.this.cmbTb.setItems(ImportTableDataEditor.this.tables);
//                if (ImportTableDataEditor.this.tables.length > 0) {
//                    ImportTableDataEditor.this.cmbTb.select(0);
//                }
//            }
//        });
//
//        this.cmbTb.addModifyListener(new ModifyListener() {
//            public void modifyText(ModifyEvent e) {
//                ImportTableDataEditor.this.tableName = ImportTableDataEditor.this.cmbTb.getText();
//                java.util.List<ColumnModel> list_column = ImportTableDataEditor.this.getColumnFields(ImportTableDataEditor.this.vcName, ImportTableDataEditor.this.dbname, ImportTableDataEditor.this.tableName);
//                ImportTableDataEditor.this.comboViewerField.setInput(list_column);
//
//                ImportTableDataEditor.this.fieldMap.clear();
//
//                try {
//                    ImportTableDataEditor.this.refreshCsvPreviewTable();
//                } catch (Exception e1) {
//
//                    e1.printStackTrace();
//                }
//                ImportTableDataEditor.this.btnCSVImportToDb.setEnabled((ImportTableDataEditor.this.fieldMap.size() > 0));
//
//
//                if (ImportTableDataEditor.this.autoMatchButton.getSelection()) {
//                    for (int i = 0; i < (ImportTableDataEditor.this.listColumn.getItems()).length && i < ImportTableDataEditor.this.comboViewerField.getCombo().getItemCount(); i++) {
//                        ColumnModel columnModel = (ColumnModel) ImportTableDataEditor.this.comboViewerField
//                                .getElementAt(i);
//                        String columnLabel = columnModel.getColumnName() + " (" +
//                                columnModel.getTypeName() + ")";
//                        ImportTableDataEditor.this.cmbField.setText(columnLabel);
//
//                        FieldModel fieldModel = ImportTableDataEditor.this.fieldList.get(i);
//                        fieldModel.setColumnModel(columnModel);
//                        String fieldType = DataImporter.dataTypeMap.get(columnModel
//                                .getTypeName().toLowerCase());
//                        if (fieldType != null) {
//                            fieldModel.setFieldType(fieldType);
//                        }
//
//                        ImportTableDataEditor.this.fieldMap.put(Integer.valueOf(i), fieldModel);
//                        ImportTableDataEditor.this.listViewerColumn.setInput(ImportTableDataEditor.this.fieldList);
//                        ImportTableDataEditor.this.listColumn.select(i);
//
//                        ImportTableDataEditor.this.listColumn.showSelection();
//
//                        ImportTableDataEditor.this.btnCSVImportToDb.setEnabled((ImportTableDataEditor.this.fieldMap.size() > 0));
//                        ImportTableDataEditor.this.refreshListColumn();
//                    }
//                }
//            }
//        });
//    }
//
//    private boolean existTable(String dbName, String tableName) {
//
//        String sql = this.dbProcess.getSql(dbName);
//        DBCStatement dbStat = null;
//        try (JDBCStatement jDBCStatement = this.dbsession.prepareStatement(DBCStatementType.EXEC, sql, true, false, false)) {
//
//            if (jDBCStatement.executeStatement()) {
//                DBCResultSet dbResult = jDBCStatement.openResultSet();
//                while (dbResult.nextRow()) {
//                    String table = (String) dbResult.getAttributeValue(0);
//                    if (table.equalsIgnoreCase(tableName)) {
//                        return true;
//                    }
//                }
//            }
//        } catch (DBCException e) {
//            log.error(e);
//        }
//        return false;
//    }
//
//
//    private void initControl() {
//        getEditorInput();
//        java.util.List<String> list_vc = getVCNames();
//        String[] vcs = new String[list_vc.size()];
//        list_vc.toArray(vcs);
//        this.cmbVC.setItems(vcs);
//        this.cmbVC.select(list_vc.indexOf(this.vcName));
//
//
//        String[] catalogs = getDBNames(this.vcName);
//        for (int i = 0; i < catalogs.length; i++) {
//            if (this.dbname.equalsIgnoreCase(catalogs[i])) {
//                this.databaseIndex = i;
//            }
//        }
//        this.cmbDb.setItems(catalogs);
//        this.cmbDb.select(this.databaseIndex);
//
//        java.util.List<String> list_table = getTableNames(this.vcName, this.dbname);
//
//        String db = this.cmbDb.getText();
//
//        String[] tables = new String[list_table.size()];
//        list_table.toArray(tables);
//
//        int selectTableIndex = 0;
//        this.cmbTb.setItems(tables);
//        if (!"".equals(this.tableName)) {
//            selectTableIndex = list_table.indexOf(this.tableName);
//        }
//        if (tables.length > 0) {
//            this.cmbTb.select((selectTableIndex >= 0) ? selectTableIndex : 0);
//        }
//
//
//        String tb = this.cmbTb.getText();
//        java.util.List<ColumnModel> list_column = getColumnFields(this.vcName, db, tb);
//
//        this.comboViewerField.setInput(list_column);
//    }
//
//
//    private void refreshCsvPreviewTable() throws Exception {
//        this.parseOption.setPosition(0L);
//        this.parseOption.setStartLineNum(0);
//        this.parseOption.setEndLineNum(0);
//        if (this.filePathCSV == null || this.filePathCSV.equals("")) {
//            return;
//        }
//
//        this.dataList = this.dataParser.parseToList(this.filePathCSV, this.parseOption, 100, this.container);
//
//        int colNum = this.parseOption.getColumnNum();
//        int rowNum = (this.dataParser.getRowSize() > 100) ? 100 :
//                this.dataParser.getRowSize();
//
//        this.fieldList = new ArrayList<FieldModel>();
//        if (this.parseOption.isHasHeader()) {
//            this.fieldHeaders = this.dataParser.getHeaders();
//            for (int i = 0; i < this.fieldHeaders.length; i++) {
//                FieldModel fieldModel = new FieldModel();
//                fieldModel.setFieldIndex(i + 1);
//                fieldModel.setFieldName(this.fieldHeaders[i]);
//                this.fieldList.add(fieldModel);
//
//            }
//
//        } else {
//
//            this.fieldHeaders = new String[this.dataParser.getColSize()];
//            for (int i = 0; i < this.dataParser.getColSize(); i++) {
//                this.fieldHeaders[i] = "Field" + (i + 1);
//                FieldModel fieldModel = new FieldModel();
//                fieldModel.setFieldIndex(i + 1);
//                fieldModel.setFieldName(this.fieldHeaders[i]);
//                this.fieldList.add(fieldModel);
//            }
//        }
//
//
//        this.listViewerField.setInput(this.fieldList);
//        this.listViewerColumn.setInput(this.fieldList);
//
//        if (colNum < this.dataParser.getColSize()) {
//            this.listField.remove(colNum, this.dataParser.getColSize() - 1);
//        }
//        this.listColumn.setItems(this.listField.getItems());
//
//        this.spnColNum.setMaximum(this.dataParser.getColSize());
//        this.spnRowNum.setMaximum(this.dataParser.getRowSize());
//        this.spnRowNum.setSelection(this.spnRowNum.getMaximum());
//
//        this.tablePreviewModel = new TablePreviewModel(this.dataList, this.fieldHeaders,
//                rowNum, colNum);
//        this.tablePreview.setModel(this.tablePreviewModel);
//
//        try {
//            this.txtCSVFile.setText("");
//            if (!this.fileType.equalsIgnoreCase("XLS") && !this.fileType.equalsIgnoreCase("XLSX")) {
//                String filePreviewStr = readCsvFilePreviewToString();
//                this.txtCSVFile.setText(filePreviewStr);
//            }
//        } catch (Exception e1) {
//            log.error(e1);
//        }
//    }
//
//
//    private String readCsvFilePreviewToString() throws Exception {
//        String charset = IConstants.CHARSET_ARRAY[this.parseOption.getCharset()];
//        StringBuffer sb = new StringBuffer();
//        BufferedReader br = null;
//        try {
//            File file = new File(this.filePathCSV);
//            InputStreamReader isr = new InputStreamReader(new FileInputStream(file), charset);
//            br = new BufferedReader(isr);
//            String line = null;
//            int count = 0;
//            while ((line = br.readLine()) != null && count <= 100) {
//                count++;
//                sb.append(line).append("\n");
//            }
//        } catch (Exception e) {
//            throw e;
//        } finally {
//            if (br != null)
//                br.close();
//        }
//        return sb.toString();
//    }
//
//
//    public void setFocus() {
//        this.container.setFocus();
//        this.txtEnclose.setFocus();
//    }
//
//    public String getTitleToolTip() {
//        return getTitle();
//    }
//
//    private void refreshListColumn() {
//        if (this.parseOption.getColumnNum() < this.dataParser.getColSize()) {
//            try {
//                this.listColumn.remove(this.parseOption.getColumnNum(), this.dataParser.getColSize() - 1);
//            } catch (Exception e) {
//                log.error(e);
//            }
//        }
//    }
//
//    private void setOptionEnable(boolean flag) {
//        this.txtEnclose.setEnabled(flag);
//        this.txtFeildTerminate.setEnabled(flag);
//        this.txtLineTerminate.setEnabled(flag);
//        this.txtCSVFile.setEnabled(flag);
//    }
//
//
//    private java.util.List<String> getVCNames() {
//        java.util.List<String> vcs = new ArrayList<String>();
//        Collection<GBase8aVC> list_vc = this.dataSource.getVcs();
//        Iterator<GBase8aVC> it = list_vc.iterator();
//        while (it.hasNext()) {
//            vcs.add(it.next().getName());
//        }
//        return vcs;
//    }
//
//    private String[] getDBNames(String vcName) {
//        String[] dbs = null;
//        java.util.List<String> list_db = new ArrayList<String>();
//        GBase8aVC vc = new GBase8aVC(this.dataSource, vcName, null);
//        Collection<GBase8aCatalog> catalogs = vc.getCatalogs();
//        Iterator<GBase8aCatalog> it = catalogs.iterator();
//        while (it.hasNext()) {
//            list_db.add(it.next().getName());
//        }
//        dbs = new String[list_db.size()];
//        list_db.toArray(dbs);
//        return dbs;
//    }
//
//    private java.util.List<String> getTableNames(String vcName, String db) {
//        String sql = null;
//        ArrayList<String> list = new ArrayList<String>();
//        JDBCStatement dbStat = null;
//        if (this.dataSource.isVCCluster() && !this.dbProcess.isSystemDatabase(db)) {
//            sql = this.dbProcess.getSql(vcName, db);
//        } else {
//
//            sql = this.dbProcess.getSql(db);
//        }
//
//        try {
//            dbStat = this.dbsession.prepareStatement(DBCStatementType.EXEC, sql, true, false, false);
//
//            if (dbStat.executeStatement()) {
//                JDBCResultSet jDBCResultSet = dbStat.openResultSet();
//                while (jDBCResultSet.nextRow()) {
//                    String table = (String) jDBCResultSet.getAttributeValue(0);
//                    list.add(table);
//                }
//            }
//        } catch (DBCException e) {
//            MessageDialog.openError(this.container.getShell(), GBase8aMessages.import_properties_tip,
//                    e.getMessage());
//            e.printStackTrace();
//        } finally {
//            if (dbStat != null) {
//                dbStat.close();
//            }
//        }
//        return list;
//    }
//
//    private java.util.List<ColumnModel> getColumnFields(String vcName, String dbName, String tableName) {
//        java.util.List<ColumnModel> list = new ArrayList<ColumnModel>();
//        GBase8aCatalog catalog = null;
//        GBase8aTable table = null;
//        Collection<GBase8aTableColumn> columns = null;
//        GBase8aVC vc = new GBase8aVC(this.dataSource, vcName, null);
//        try {
//            catalog = vc.getChild(this.monitor, dbName);
//            if (catalog != null) {
//                table = (GBase8aTable) catalog.getChild(this.monitor, tableName);
//                if (table != null) {
//                    columns = table.getAttributes(this.monitor);
//                    Iterator<GBase8aTableColumn> it = columns.iterator();
//                    while (it.hasNext()) {
//                        GBase8aTableColumn tableColumn = it.next();
//                        ColumnModel columnModel = new ColumnModel();
//                        columnModel.setCatalogName(dbName);
//                        columnModel.setTableName(tableName);
//                        columnModel.setColumnName(tableColumn.getName());
//                        columnModel.setTypeName(tableColumn.getTypeName());
//                        columnModel.setColumnType(tableColumn.getTypeID());
//                        columnModel.setColumnSize(tableColumn.getPrecision());
//                        columnModel.setDecimalDigits(tableColumn.getScale());
//                        list.add(columnModel);
//                    }
//                }
//            }
//        } catch (Exception e) {
//
//            MessageDialog.openError(this.container.getShell(), GBase8aMessages.import_properties_tip,
//                    e.getMessage());
//            e.printStackTrace();
//        }
//        return list;
//    }
//}
