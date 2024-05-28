package org.jkiss.dbeaver.ext.gbase8a.ui.views;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aConstants;
import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aUtils;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aPartition;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTable;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableColumn;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableSpace;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.dialogs.BaseDialog;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class GBase8aTableDialog extends BaseDialog {

    Log log = Log.getLog(GBase8aTableDialog.class);

    private final GBase8aDataSource dataSource;
    private final DBRProgressMonitor monitor;
    private Text textDescription;
    private Text textLimitSize;
    private Text textTableName;
    private Combo comboCharset;
    private Button btnUseTableCompress;
    private Combo comboNumberCompress;
    private Combo comboStringCompress;
    private TableViewer tableViewer;
    private Table table;
    private final String[] charsets = new String[]{"utf8", "gbk"};
    private final String[] tablespaces = new String[0];
    private final String[] numCompresses = new String[]{"0", "1", "5"};
    private final String[] stringCompresses = new String[]{"0", "3", "5"};
    private Button btnReplicatedTable;
    private Button btnNocopyTable;
    private Combo comboHashColumn;
    private Combo comboTableSpace;
    private final HashMap<TableItem, TableItemControls> tableControls = new HashMap();
    private static final int NAME_INDEX = 1;
    private static final int TYPE_INDEX = 2;
    private static final int LENGTH_INDEX = 3;
    private static final int IS_NOT_NULL_INDEX = 4;
    private static final int DEFAULT_INDEX = 5;
    private static final int PRECISION_INDEX = 6;
    private static final int SCALE_INDEX = 7;
    private static final int COMMENT_INDEX = 8;
    private static final int HASH_INDEX = 9;
    private static final int MAXZINT = Integer.MAX_VALUE;
    private MenuManager menu;
    private MenuManager menu_p;
    private GBase8aTable gbase8aTable;
    private List<GBase8aTableColumn> list_columns;
    private List<String> list_hash_columns;
    private List<GBase8aPartition> list_partition;
    private final boolean isNew;
    private boolean isSubPartition;
    private Text textName;
    private CCombo comboDataType;
    private Text textLength;
    private CCombo comboNotNull;
    private Text textDefault;
    private Text textPrecision;
    private Text textScale;
    private Text textComment;
    private CCombo comboHash;
    private Button btnIsPartiton;
    private Combo comboType;
    private Group grpRange;
    private GridData gd_grpRange;
    private Text textRangeColumn;
    private Button btnIsSubPartition;
    private TableViewer tvRange;
    private Table tbRange;
    private Text textRangeName;
    private Text textRangeValue;
    private Text textSubPartitionName;
    private Group grpSubPartition;
    private GridData gd_grpSubPartition;
    private Button btnSubLinear;
    private Combo comboSubType;
    private Text textSubPartitionColumn;
    private Text textSubCount;
    private Group grpHash;
    private GridData gd_grpHash;
    private Button btnLinear;
    private Text textHashColumn;
    private Text textCount;
    private TableViewer tvHash;
    private Table tbHash;
    private Text textHashName;
    private final String[] partitionType = new String[]{"RANGE", "LIST", "HASH", "KEY"};
    private final String[] subPartitionType = new String[]{"HASH", "KEY"};
    private Action addRangeAction;
    private Action delRangeAction;
    private Action addHashAction;
    private Action delHashAction;

    public GBase8aTableDialog(Shell parentShell, DBRProgressMonitor monitor, GBase8aDataSource dataSource, boolean isNew) {
        super(parentShell, GBase8aMessages.dialog_create_table_title, null);
        this.setShellStyle(67680 | getDefaultOrientation());
        this.monitor = monitor;
        this.dataSource = dataSource;
        this.isNew = isNew;
        String title;
        if (isNew) {
            title = GBase8aMessages.dialog_create_table_title;
        } else {
            title = GBase8aMessages.dialog_modify_table_title;
        }

        this.setTitle(title);
    }

    protected Composite createDialogArea(Composite parent) {
        Composite composite = super.createDialogArea(parent);
        this.menu = new MenuManager();
        this.menu.add(new AddColumnAction());
        this.menu.add(new DeleteColumnAction());
        TabFolder tabFolder = new TabFolder(composite, 0);
        tabFolder.setLayoutData(new GridData(4, 4, true, true, 1, 1));
        TabItem tbtmTableProperties = new TabItem(tabFolder, 0);
        tbtmTableProperties.setText(GBase8aMessages.dialog_create_table_tab_table_property);
        Composite composite_1 = new Composite(tabFolder, 0);
        tbtmTableProperties.setControl(composite_1);
        composite_1.setLayout(new GridLayout(3, false));
        Label lblTableName = new Label(composite_1, 0);
        GridData gd_lblTableName = new GridData(4, 16777216, false, false, 1, 1);
        gd_lblTableName.widthHint = 130;
        lblTableName.setLayoutData(gd_lblTableName);
        lblTableName.setText(GBase8aMessages.dialog_create_table_name);
        this.textTableName = new Text(composite_1, 2048);
        this.textTableName.setLayoutData(new GridData(4, 16777216, true, false, 2, 1));
        this.textTableName.setTextLimit(56);
        Label lblDescription = new Label(composite_1, 0);
        lblDescription.setLayoutData(new GridData(4, 16777216, false, false, 1, 1));
        lblDescription.setText(GBase8aMessages.dialog_create_table_description);
        this.textDescription = new Text(composite_1, 2048);
        this.textDescription.setLayoutData(new GridData(4, 16777216, true, false, 2, 1));
        Label lblTableLimitSize = new Label(composite_1, 0);
        lblTableLimitSize.setLayoutData(new GridData(4, 16777216, false, false, 1, 1));
        lblTableLimitSize.setText(GBase8aMessages.dialog_create_table_limit_size);
        this.textLimitSize = new Text(composite_1, 2048);
        this.textLimitSize.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        Label lblLimitSizeUnit = new Label(composite_1, 0);
        lblLimitSizeUnit.setText(GBase8aMessages.dialog_create_table_limit_size_unit);
        Label lblCharset = new Label(composite_1, 0);
        lblCharset.setLayoutData(new GridData(4, 16777216, false, false, 1, 1));
        lblCharset.setText(GBase8aMessages.dialog_create_table_charset);
        this.comboCharset = new Combo(composite_1, 8);
        this.comboCharset.setLayoutData(new GridData(4, 16777216, true, false, 2, 1));
        this.comboCharset.setItems(this.charsets);
        this.btnUseTableCompress = new Button(composite_1, 32);
        this.btnUseTableCompress.setText(GBase8aMessages.dialog_create_table_use_table_compress);
        this.btnUseTableCompress.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                GBase8aTableDialog.this.comboNumberCompress.setEnabled(GBase8aTableDialog.this.btnUseTableCompress.getSelection());
                GBase8aTableDialog.this.comboStringCompress.setEnabled(GBase8aTableDialog.this.btnUseTableCompress.getSelection());
            }
        });
        new Label(composite_1, 0);
        new Label(composite_1, 0);
        Group grpCompress = new Group(composite_1, 0);
        grpCompress.setText(GBase8aMessages.dialog_create_table_compress_type);
        grpCompress.setLayout(new GridLayout(2, false));
        grpCompress.setLayoutData(new GridData(4, 16777216, false, false, 3, 1));
        Label lblNumberCompress = new Label(grpCompress, 0);
        GridData gd_lblNumberCompress = new GridData(131072, 16777216, false, false, 1, 1);
        gd_lblNumberCompress.widthHint = 143;
        lblNumberCompress.setLayoutData(gd_lblNumberCompress);
        lblNumberCompress.setText(GBase8aMessages.dialog_create_table_number_compress);
        this.comboNumberCompress = new Combo(grpCompress, 8);
        this.comboNumberCompress.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        this.comboNumberCompress.setItems(this.numCompresses);
        Label lblStringCompress = new Label(grpCompress, 0);
        lblStringCompress.setLayoutData(new GridData(4, 16777216, false, false, 1, 1));
        lblStringCompress.setText(GBase8aMessages.dialog_create_table_string_compress);
        this.comboStringCompress = new Combo(grpCompress, 8);
        this.comboStringCompress.setLayoutData(new GridData(4, 16777216, true, false, 1, 1));
        this.comboStringCompress.setItems(this.stringCompresses);
        Group grpTableType = new Group(composite_1, 0);
        grpTableType.setLayout(new GridLayout(2, false));
        grpTableType.setLayoutData(new GridData(4, 4, false, false, 3, 1));
        grpTableType.setText(GBase8aMessages.dialog_create_table_type);
        this.btnReplicatedTable = new Button(grpTableType, 32);
        GridData gd_btnCopyTable = new GridData(4, 4, false, false, 1, 1);
        gd_btnCopyTable.widthHint = 136;
        this.btnReplicatedTable.setLayoutData(gd_btnCopyTable);
        this.btnReplicatedTable.setText(GBase8aMessages.dialog_create_table_replicated);
        this.btnReplicatedTable.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                GBase8aTableDialog.this.btnNocopyTable.setEnabled(!GBase8aTableDialog.this.btnReplicatedTable.getSelection());
                GBase8aTableDialog.this.comboHashColumn.setEnabled(!GBase8aTableDialog.this.btnReplicatedTable.getSelection());
            }
        });
        new Label(grpTableType, 0);
        this.btnNocopyTable = new Button(grpTableType, 32);
        this.btnNocopyTable.setLayoutData(new GridData(4, 4, false, false, 1, 1));
        this.btnNocopyTable.setText(GBase8aMessages.dialog_create_table_nocopies);
        new Label(grpTableType, 0);
        new Label(composite_1, 0);
        new Label(composite_1, 0);
        new Label(composite_1, 0);
        TabItem tbtmColumnProperties = new TabItem(tabFolder, 0);
        tbtmColumnProperties.setText(GBase8aMessages.dialog_create_table_tab_column_property);
        Composite composite_2 = new Composite(tabFolder, 0);
        tbtmColumnProperties.setControl(composite_2);
        composite_2.setLayout(new GridLayout(1, false));
        this.tableViewer = new TableViewer(composite_2, 68354);
        this.table = this.tableViewer.getTable();
        this.table.setLayoutData(new GridData(4, 4, true, true, 1, 1));
        this.table.setHeaderVisible(true);
        this.table.setLinesVisible(true);
        this.table.setMenu(this.menu.createContextMenu(this.table));
        this.table.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent e) {
                GBase8aTableDialog.this.addColumn();
            }
        });
        this.tableViewer.setColumnProperties(new String[]{"", "colName", "dataType", "length", "notNull", "default", "precision", "scale", "comment", "hashColumn"});
        CellEditor[] cellEditors = new CellEditor[10];
        cellEditors[1] = new TextCellEditor(this.table);
        this.textName = (Text) cellEditors[1].getControl();
        cellEditors[2] = new ComboBoxCellEditor(this.table, GBase8aConstants.TYPES, 520);
        this.comboDataType = (CCombo) cellEditors[2].getControl();
        this.comboDataType.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                String typeName = GBase8aTableDialog.this.comboDataType.getText().toUpperCase();
                int selectIndex = GBase8aTableDialog.this.table.getSelectionIndex();
                TableItem item = GBase8aTableDialog.this.table.getItem(selectIndex);
                GBase8aTableColumn getc = (GBase8aTableColumn) item.getData();
                try {
                    getc.setTypeName(typeName);
                } catch (DBException ignore) {
                }
                switch (GBase8aConstants.TYPES_INDEX.get(typeName)) {
                    case 1:
                        if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_BIGINT)) {
                            getc.setMaxLength(19L);
                        } else if (!typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_INT) && !typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_INTEGER)) {
                            if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_SMALLINT)) {
                                getc.setMaxLength(3L);
                            } else {
                                getc.setMaxLength(0L);
                            }
                        } else {
                            getc.setMaxLength(10L);
                        }

                        getc.setPrecision(0);
                        getc.setScale(0);
                        break;
                    case 2:
                        if (!typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_CHAR) && !typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_VARCHAR) && !typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_VARBINARY)) {
                            getc.setMaxLength(0L);
                        } else {
                            getc.setMaxLength(50L);
                        }

                        getc.setPrecision(0);
                        getc.setScale(0);
                        break;
                    case 3:
                        if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_DECIMAL)) {
                            getc.setPrecision(10);
                            getc.setScale(0);
                        } else if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_FLOAT)) {
                            getc.setPrecision(12);
                            getc.setScale(0);
                        } else if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_DOUBLE)) {
                            getc.setPrecision(22);
                            getc.setScale(0);
                        } else if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_NUMERIC)) {
                            getc.setPrecision(22);
                            getc.setScale(0);
                        } else {
                            getc.setPrecision(0);
                            getc.setScale(0);
                        }

                        getc.setMaxLength(0L);
                }

                GBase8aTableDialog.this.tableViewer.refresh();
            }
        });
        cellEditors[3] = new TextCellEditor(this.table);
        this.textLength = (Text) cellEditors[3].getControl();
        this.textLength.addVerifyListener(new VerifyListener() {
            public void verifyText(VerifyEvent e) {
                String inStr = e.text;
                if (e.keyCode != 8 && e.keyCode != 127) {
                    e.doit = GBase8aTableDialog.this.checkDigit(inStr);
                } else {
                    e.doit = true;
                }

            }
        });
        cellEditors[4] = new ComboBoxCellEditor(this.table, GBase8aConstants.TF, 520);
        this.comboNotNull = (CCombo) cellEditors[4].getControl();
        cellEditors[5] = new TextCellEditor(this.table);
        this.textDefault = (Text) cellEditors[5].getControl();
        cellEditors[6] = new TextCellEditor(this.table);
        this.textPrecision = (Text) cellEditors[6].getControl();
        this.textPrecision.addVerifyListener(new VerifyListener() {
            public void verifyText(VerifyEvent e) {
                String inStr = e.text;
                if (e.keyCode != 8 && e.keyCode != 127) {
                    e.doit = GBase8aTableDialog.this.checkDigit(inStr);
                } else {
                    e.doit = true;
                }

            }
        });
        cellEditors[7] = new TextCellEditor(this.table);
        this.textScale = (Text) cellEditors[7].getControl();
        this.textScale.addVerifyListener(new VerifyListener() {
            public void verifyText(VerifyEvent e) {
                String inStr = e.text;
                if (e.keyCode != 8 && e.keyCode != 127) {
                    e.doit = GBase8aTableDialog.this.checkDigit(inStr);
                } else {
                    e.doit = true;
                }

            }
        });
        cellEditors[8] = new TextCellEditor(this.table);
        this.textComment = (Text) cellEditors[8].getControl();
        cellEditors[9] = new ComboBoxCellEditor(this.table, GBase8aConstants.TF, 520);
        this.comboHash = (CCombo) cellEditors[9].getControl();
        this.tableViewer.setCellEditors(cellEditors);
        this.tableViewer.setCellModifier(new ColumnCellModifier(this.tableViewer));
        TableColumn blankColumn = new TableColumn(this.table, 0);
        blankColumn.setWidth(20);
        blankColumn.setText("");
        TableColumn tblclmnName = new TableColumn(this.table, 0);
        tblclmnName.setWidth(100);
        tblclmnName.setText(GBase8aMessages.dialog_create_table_column_name);
        TableColumn tblclmnDataType = new TableColumn(this.table, 0);
        tblclmnDataType.setWidth(100);
        tblclmnDataType.setText(GBase8aMessages.dialog_create_table_column_data_type);
        TableColumn tblclmnLength = new TableColumn(this.table, 0);
        tblclmnLength.setWidth(100);
        tblclmnLength.setText(GBase8aMessages.dialog_create_table_column_length);
        TableColumn tblclmnIsNull = new TableColumn(this.table, 0);
        tblclmnIsNull.setWidth(100);
        tblclmnIsNull.setText(GBase8aMessages.dialog_create_table_column_is_not_null);
        TableColumn tblclmnDefault = new TableColumn(this.table, 0);
        tblclmnDefault.setWidth(100);
        tblclmnDefault.setText(GBase8aMessages.dialog_create_table_column_default);
        TableColumn tblclmnPrecision = new TableColumn(this.table, 0);
        tblclmnPrecision.setWidth(100);
        tblclmnPrecision.setText(GBase8aMessages.dialog_create_table_column_precision);
        TableColumn tblclmnScale = new TableColumn(this.table, 0);
        tblclmnScale.setWidth(100);
        tblclmnScale.setText(GBase8aMessages.dialog_create_table_column_scale);
        TableColumn tblclmnComment = new TableColumn(this.table, 0);
        tblclmnComment.setWidth(100);
        tblclmnComment.setText(GBase8aMessages.dialog_create_table_column_comment);
        TableColumn tblclmnHash = new TableColumn(this.table, 0);
        tblclmnHash.setWidth(100);
        tblclmnHash.setText(GBase8aMessages.dialog_create_table_column_hash_column);
        TableColumn hideColumn = new TableColumn(this.table, 0);
        hideColumn.setWidth(0);
        hideColumn.setText("persisted");
        this.tableViewer.setLabelProvider(new ITableLabelProvider() {
            public void addListener(ILabelProviderListener listener) {
            }

            public void dispose() {
            }

            public boolean isLabelProperty(Object element, String property) {
                return false;
            }

            public void removeListener(ILabelProviderListener listener) {
            }

            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }

            public String getColumnText(Object element, int columnIndex) {
                GBase8aTableColumn column = (GBase8aTableColumn) element;
                String typeName = column.getTypeName().toUpperCase();
                int typeIndex = GBase8aConstants.getTYPES_INDEX().get(column.getTypeName().toUpperCase());
                switch (columnIndex) {
                    case 0:
                        return "";
                    case 1:
                        return column.getName() == null ? "" : column.getName();
                    case 2:
                        return typeName;
                    case 3:
                        long length = column.getMaxLength();
                        switch (typeIndex) {
                            case 1:
                                if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_BIGINT)) {
                                    return String.valueOf(length);
                                } else {
                                    if (!typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_INT) && !typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_INTEGER)) {
                                        if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_SMALLINT)) {
                                            return String.valueOf(length);
                                        }

                                        return "";
                                    }

                                    return String.valueOf(length);
                                }
                            case 2:
                                if (!typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_CHAR) && !typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_VARCHAR) && !typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_VARBINARY)) {
                                    return "";
                                }

                                return String.valueOf(length);
                            case 3:
                                return "";
                            default:
                                return "";
                        }
                    case 4:
                        return column.isRequired() ? GBase8aMessages.constants_true : GBase8aMessages.constants_false;
                    case 5:
                        return column.getDefaultValue() == null ? "" : column.getDefaultValue();
                    case 6:
                        int precision = column.getPrecision();
                        switch (typeIndex) {
                            case 1:
                            case 2:
                                return "";
                            case 3:
                                if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_DECIMAL)) {
                                    return String.valueOf(precision);
                                } else if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_FLOAT)) {
                                    return String.valueOf(precision);
                                } else if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_DOUBLE)) {
                                    return String.valueOf(precision);
                                } else {
                                    if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_NUMERIC)) {
                                        return String.valueOf(precision);
                                    }

                                    return "";
                                }
                            default:
                                return "";
                        }
                    case 7:
                        int scale = column.getScale();
                        switch (typeIndex) {
                            case 1:
                            case 2:
                                return "";
                            case 3:
                                if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_DECIMAL)) {
                                    return String.valueOf(scale);
                                } else if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_FLOAT)) {
                                    return String.valueOf(scale);
                                } else if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_DOUBLE)) {
                                    return String.valueOf(scale);
                                } else {
                                    if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_NUMERIC)) {
                                        return String.valueOf(scale);
                                    }

                                    return "";
                                }
                            default:
                                return "";
                        }
                    case 8:
                        return column.getComment() == null ? "" : column.getComment();
                    case 9:
                        return column.isHashColumn() ? GBase8aMessages.constants_true : GBase8aMessages.constants_false;
                    case 10:
                        return String.valueOf(column.isPersisted());
                    default:
                        return null;
                }
            }
        });
        this.tableViewer.setContentProvider(new IStructuredContentProvider() {
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

            public void dispose() {
            }

            public Object[] getElements(Object inputElement) {
                return ((ArrayList) inputElement).toArray();
            }
        });
        this.init();
        this.tableViewer.setUseHashlookup(true);
        this.tableViewer.setInput(this.list_columns);
        return composite;
    }

    private boolean checkDigit(String s) {
        if (s.length() > 0) {
            try {
                Integer.parseInt(s);
                return true;
            } catch (NumberFormatException var2) {
                return false;
            }
        } else {
            return false;
        }
    }

    private void init() {
        if (this.isNew) {
            this.textTableName.setText("");
            this.textDescription.setText("");
            this.textLimitSize.setText("0");
            this.comboCharset.select(0);
            this.btnUseTableCompress.setSelection(false);
            this.comboNumberCompress.select(0);
            this.comboNumberCompress.setEnabled(false);
            this.comboStringCompress.select(0);
            this.comboStringCompress.setEnabled(false);
            this.btnReplicatedTable.setSelection(false);
            this.btnNocopyTable.setSelection(false);
            this.list_columns = new ArrayList();
        } else {
            try {
                GBase8aTable.AdditionalInfo addInfo = this.gbase8aTable.getAdditionalInfo(this.monitor);
                this.textTableName.setText(this.gbase8aTable.getName());
                this.textTableName.setEnabled(false);
                this.textDescription.setText(addInfo.getDescription());
                this.textLimitSize.setText(addInfo.getTableLimitSize());
                this.textLimitSize.setEnabled(false);
                this.comboCharset.setText(addInfo.getCharset());
                this.comboCharset.setEnabled(false);
                this.btnUseTableCompress.setSelection(addInfo.isBtnIsCompress());
                this.comboNumberCompress.setEnabled(addInfo.isBtnIsCompress());
                this.comboStringCompress.setEnabled(addInfo.isBtnIsCompress());
                this.comboNumberCompress.setText(addInfo.getCompressNum());
                this.comboStringCompress.setText(addInfo.getCompressString());
                this.btnReplicatedTable.setSelection(addInfo.isRepliacte());
                this.btnReplicatedTable.setEnabled(false);
                this.btnNocopyTable.setSelection(addInfo.isNocopies());
                this.btnNocopyTable.setEnabled(false);
                Collection<GBase8aTableColumn> collection_column = this.gbase8aTable.getAttributes(this.monitor);
                this.list_columns = new ArrayList();
                this.list_columns.addAll(collection_column);
            } catch (DBException e) {
                log.error(e);
            }
        }

    }

    protected void buttonPressed(int buttonId) {
        if (buttonId == 0) {
            try {
                GBase8aTable.AdditionalInfo additionalInfo = this.gbase8aTable.getAdditionalInfo(this.monitor);
                if (this.isNew) {
                    if (this.textTableName.getText().trim().equals("")) {
                        MessageDialog.openError(this.getShell(), GBase8aMessages.message_error_title, GBase8aMessages.dialog_create_table_error_table_name);
                        return;
                    }

                    this.gbase8aTable.setName(this.textTableName.getText().trim());
                    String limitSize = this.textLimitSize.getText().trim();
                    if (!limitSize.equals("") && !limitSize.equals("0")) {
                        additionalInfo.setTableLimitSize(limitSize);
                    }

                    additionalInfo.setCharset(this.dataSource.getCharset(this.comboCharset.getText().trim()));
                    additionalInfo.setRepliacte(this.btnReplicatedTable.getSelection());

                    additionalInfo.setNocopies(this.btnNocopyTable.getSelection());
                }

                additionalInfo.setDescription(this.textDescription.getText().trim());
                if (this.btnUseTableCompress.getSelection()) {
                    additionalInfo.setBtnIsCompress(true);
                    additionalInfo.setCompressNum(this.comboNumberCompress.getText().trim());
                    additionalInfo.setCompressString(this.comboStringCompress.getText().trim());
                } else {
                    additionalInfo.setBtnIsCompress(false);
                    additionalInfo.setCompressNum("0");
                    additionalInfo.setCompressString("0");
                }

                TableItem[] items = this.table.getItems();
                if (items.length == 0) {
                    MessageDialog.openError(this.getShell(), GBase8aMessages.message_error_title, GBase8aMessages.dialog_create_table_error_no_column);
                    return;
                }

                if (!this.getColumns()) {
                    MessageDialog.openError(this.getShell(), GBase8aMessages.message_error_title, GBase8aMessages.dialog_create_table_error_column_name);
                    return;
                }

                if (!this.list_hash_columns.isEmpty()) {
                    additionalInfo.setHashCols(this.list_hash_columns);
                }

                if (!this.executeSql()) {
                    return;
                }
            } catch (DBCException e) {
                log.error(e);
            }
        }

        super.buttonPressed(buttonId);
    }

    public GBase8aTable getGbase8aTable() {
        return this.gbase8aTable;
    }

    public void setGbase8aTable(GBase8aTable gbase8aTable) {
        this.gbase8aTable = gbase8aTable;
    }

    private String[] getTableSpaceNames() {
        String[] names = null;
        Collection<GBase8aTableSpace> spaces = null;

        try {
            spaces = this.gbase8aTable.getContainer().getTablespaces(this.monitor);
            names = new String[spaces.size() + 1];
            names[0] = GBase8aMessages.dialog_create_table_hash_column_choose;
            Iterator<GBase8aTableSpace> it = spaces.iterator();

            GBase8aTableSpace space;
            for (int i = 0; it.hasNext(); names[i] = space.getName()) {
                ++i;
                space = it.next();
            }
        } catch (DBException var6) {
        }

        return names;
    }

    private String[] getHashColumnNames() {
        String[] names = new String[]{GBase8aMessages.dialog_create_table_hash_column_choose};
        TableItem[] items = this.table.getItems();
        if (items.length > 0) {
            names = new String[items.length + 1];
            names[0] = GBase8aMessages.dialog_create_table_hash_column_choose;

            for (int i = 0; i < items.length; ++i) {
                String name = items[i].getText(1).trim();
                if (name != null && !"".equals(name)) {
                    names[i + 1] = name;
                }
            }
        }

        return names;
    }

    private boolean getColumns() {
        Map<String, String> map = new HashMap();
        TableItem[] items = this.table.getItems();
        if (items.length > 0) {
            this.list_columns = new ArrayList();
            this.list_columns.clear();
            this.list_hash_columns = new ArrayList();
            this.list_hash_columns.clear();
            TableItem[] var6 = items;
            int var5 = items.length;

            for (int var4 = 0; var4 < var5; ++var4) {
                TableItem item = var6[var4];
                GBase8aTableColumn gbase8aTableColumn = new GBase8aTableColumn(this.gbase8aTable);
                String name = item.getText(1).trim();
                if (!this.check(name)) {
                    return false;
                }
                if (map.containsKey(name)) {
                    return false;
                }
                gbase8aTableColumn.setName(name);
                map.put(name, null);
                try {
                    gbase8aTableColumn.setTypeName(item.getText(2));
                } catch (DBException ignore) {
                }
                String length = item.getText(3).trim();
                gbase8aTableColumn.setMaxLength("".equals(length) ? 0L : Long.parseLong(length));
                String isNotNull = item.getText(4);
                gbase8aTableColumn.setRequired(GBase8aMessages.constants_true.equalsIgnoreCase(isNotNull));
                String defaultValue = item.getText(5).trim();
                gbase8aTableColumn.setDefaultValue(defaultValue);
                String scale = item.getText(7).trim();
                gbase8aTableColumn.setScale("".equals(scale) ? 0 : Integer.parseInt(scale));
                String precision = item.getText(6).trim();
                gbase8aTableColumn.setPrecision("".equals(precision) ? 0 : Integer.parseInt(precision));
                String comment = item.getText(8).trim();
                gbase8aTableColumn.setComment(comment);
                String isHash = item.getText(9);
                gbase8aTableColumn.setHashColumn(GBase8aMessages.constants_true.equalsIgnoreCase(isHash));
                if (GBase8aMessages.constants_true.equalsIgnoreCase(isHash)) {
                    this.list_hash_columns.add(name);
                }

                gbase8aTableColumn.setPersisted(Boolean.valueOf(item.getText(10)));
                this.list_columns.add(gbase8aTableColumn);
            }
        }

        return true;
    }

    private String prepareCreateSql() throws DBException {
        StringBuffer sb = new StringBuffer();
        String vcName = null;
        sb.append("CREATE TABLE `");
        if (this.gbase8aTable.getDataSource().isVCCluster()) {
            vcName = this.gbase8aTable.getContainer().getVcName();
            sb.append(vcName);
            sb.append("`.`");
        }

        sb.append(this.gbase8aTable.getContainer().getName());
        sb.append("`.`");
        sb.append(this.gbase8aTable.getName());
        sb.append("` (");

        for (GBase8aTableColumn listColumn : this.list_columns) {
            GBase8aTableColumn column = listColumn;
            sb.append("`");
            sb.append(column.getName());
            sb.append("` ");
            sb.append(column.getTypeName());
            String sLower = column.getTypeName().toLowerCase();
            boolean bNumeric = column.getScale() > 0 || column.getPrecision() > 0;

            if (sLower.indexOf("varchar") == -1 && sLower.indexOf("char") == -1 && sLower.indexOf("varbinary") == -1) {
                if (bNumeric) {
                    sb.append("(");
                    sb.append(column.getPrecision());
                    sb.append(",");
                    sb.append(column.getScale());
                    sb.append(")");
                }
            } else {
                sb.append("(");
                sb.append(column.getMaxLength());
                sb.append(")");
            }

            if (column.isRequired()) {
                sb.append(" not null ");
            }

            if (column.getDefaultValue() != null && !column.getDefaultValue().equals("")) {
                sb.append(" default ");
                if (sLower.equalsIgnoreCase("timestamp") && column.getDefaultValue().equalsIgnoreCase("CURRENT_TIMESTAMP")) {
                    sb.append(" ");
                } else {
                    sb.append("'");
                }

                sb.append(column.getDefaultValue());
                if (sLower.equalsIgnoreCase("timestamp") && column.getDefaultValue().equalsIgnoreCase("CURRENT_TIMESTAMP")) {
                    sb.append(" ");
                } else {
                    sb.append("'");
                }
            }

            if (!column.getComment().isEmpty()) {
                sb.append(" comment ");
                sb.append("'");
                sb.append(column.getComment());
                sb.append("'");
            }

            sb.append(",");
        }

        sb.delete(sb.length() - 1, sb.length());
        sb.append(")");
        GBase8aTable.AdditionalInfo addInfo = this.gbase8aTable.getAdditionalInfo(this.monitor);
        if (addInfo.isBtnIsCompress()) {
            sb.append(" compress(").append(addInfo.getCompressNum()).append(",").append(addInfo.getCompressString()).append(")");
        }

        if (addInfo.getTableLimitSize() != null && addInfo.getTableLimitSize().length() > 0 && !addInfo.getTableLimitSize().equals("0")) {
            sb.append(" ").append("limit_storage_size=" + addInfo.getTableLimitSize());
        }

        if (addInfo.isRepliacte()) {
            sb.append(" ");
            sb.append("REPLICATED");
        }

        if (addInfo.isNocopies()) {
            sb.append(" nocopies");
        }

        if (addInfo.getHashCols() != null && !addInfo.getHashCols().isEmpty()) {
            sb.append(" distributed by (");
            Iterator var9 = addInfo.getHashCols().iterator();

            while (var9.hasNext()) {
                String col = (String) var9.next();
                sb.append("'");
                sb.append(col);
                sb.append("',");
            }

            sb.delete(sb.lastIndexOf(","), sb.length());
            sb.append(")");
        }

        if (addInfo.getCharset().equalsIgnoreCase("gbk")) {
            sb.append(" CHARSET=gbk");
        }

        if (!addInfo.getDescription().isEmpty()) {
            sb.append(" ");
            sb.append("COMMENT=");
            sb.append("'");
            sb.append(addInfo.getDescription());
            sb.append("'");
        }

        return sb.toString();
    }

    private String prepareAlterSql() throws DBException {
        StringBuffer sb = new StringBuffer();
        String head = "ALTER TABLE `";
        if (this.gbase8aTable.getDataSource().isVCCluster()) {
            String vcName = this.gbase8aTable.getContainer().getVcName();
            head = head + vcName + "`.`";
        }

        head = head + this.gbase8aTable.getContainer().getName() + "`.`" + this.gbase8aTable.getName() + "` ";
        GBase8aTable.AdditionalInfo addInfo = this.gbase8aTable.getAdditionalInfo(this.monitor);
        if (!addInfo.getDescription().equalsIgnoreCase(addInfo.getOldDescription())) {
            sb.append(head);
            sb.append("comment '");
            sb.append(addInfo.getDescription());
            sb.append("';");
        }

        if (addInfo.isBtnIsCompress() && (!addInfo.getOldCompressNum().equals(addInfo.getCompressNum()) || !addInfo.getOldCompressString().equals(addInfo.getCompressString()))) {
            sb.append(head);
            sb.append("Alter compress(");
            sb.append(addInfo.getCompressNum());
            sb.append(",");
            sb.append(addInfo.getCompressString());
            sb.append(");");
        }

        Collection<GBase8aTableColumn> oldColumn_list = this.gbase8aTable.getAttributes(this.monitor);
        int newSize = this.list_columns.size();
        List<GBase8aTableColumn> list_contain = new ArrayList();
        new StringBuffer();
        StringBuffer addBuf = new StringBuffer();

        int oldSize;
        for (oldSize = 0; oldSize < newSize; ++oldSize) {
            GBase8aTableColumn column = this.list_columns.get(oldSize);
            if (oldColumn_list.contains(column)) {
                list_contain.add(column);
            } else {
                addBuf.append(head);
                addBuf.append("add column ");
                addBuf.append("`");
                addBuf.append(column.getName());
                addBuf.append("` ");
                addBuf.append(column.getTypeName());
                String sLower = column.getTypeName().toLowerCase();
                boolean bNumeric = column.getScale() > 0 || column.getPrecision() > 0;

                if (sLower.indexOf("varchar") == -1 && sLower.indexOf("char") == -1 && sLower.indexOf("varbinary") == -1) {
                    if (bNumeric) {
                        addBuf.append("(");
                        addBuf.append(column.getPrecision());
                        addBuf.append(",");
                        addBuf.append(column.getScale());
                        addBuf.append(")");
                    }
                } else {
                    addBuf.append("(");
                    addBuf.append(column.getMaxLength());
                    addBuf.append(")");
                }

                if (column.isRequired()) {
                    addBuf.append(" not null ");
                }

                if (column.getDefaultValue() != null && !column.getDefaultValue().equals("")) {
                    addBuf.append(" default ");
                    if (sLower.equalsIgnoreCase("timestamp") && column.getDefaultValue().equalsIgnoreCase("CURRENT_TIMESTAMP")) {
                        addBuf.append(" ");
                    } else {
                        addBuf.append("'");
                    }

                    addBuf.append(column.getDefaultValue());
                    if (sLower.equalsIgnoreCase("timestamp") && column.getDefaultValue().equalsIgnoreCase("CURRENT_TIMESTAMP")) {
                        addBuf.append(" ");
                    } else {
                        addBuf.append("'");
                    }
                }

                if (!column.getComment().isEmpty()) {
                    addBuf.append(" comment ");
                    addBuf.append("'");
                    addBuf.append(column.getComment());
                    addBuf.append("'");
                }

                addBuf.append(";");
            }
        }

        oldColumn_list.removeAll(list_contain);
        oldSize = oldColumn_list.size();
        if (oldSize > 0) {
            sb.append(head);

            for (GBase8aTableColumn column : oldColumn_list) {
                sb.append("drop column ");
                sb.append("`");
                sb.append(column.getName());
                sb.append("`");
                sb.append(",");
            }

            sb.delete(sb.length() - 1, sb.length());
            sb.append(";");
        }

        sb.append(addBuf);
        return sb.toString();
    }

    private boolean executeSql() {
        try (JDBCSession dbsession = DBUtils.openMetaSession(this.monitor, this.dataSource, GBase8aMessages.message_query_sql);
             JDBCStatement stmt = dbsession.createStatement()) {
            String sql;
            if (this.isNew) {
                sql = this.prepareCreateSql();
            } else {
                sql = this.prepareAlterSql();
            }
            if (!sql.isEmpty()) {
                String[] sqls = sql.split(";");
                if (!UIUtils.confirmAction(GBase8aMessages.dialog_create_table_confirm_sql, sql)) {
                    return false;
                }
                for (String s : sqls) {
                    stmt.execute(s);
                }
            } else {
                UIUtils.showMessageBox(null, GBase8aMessages.message_information_title, GBase8aMessages.dialog_create_table_none, 2);
                return true;
            }
        } catch (SQLException | DBException var23) {
            UIUtils.showMessageBox(null, GBase8aMessages.message_error_title, var23.getMessage(), 1);
            return false;
        }
        UIUtils.showMessageBox(null, GBase8aMessages.message_information_title, GBase8aMessages.dialog_create_table_ok, 2);
        return true;
    }

    protected boolean check(String name) {
        char[] sybol = new char[]{'`', ';', '%', '\\', '\'', '"', '.', '*', '/', ':', '?', '<', '>', '|', '{', '}', '&', '$', '!', '@', '#', '(', ')', '^', '~', '[', ']', ',', '+', '='};
        if (name != null && !"".equals(name)) {
            for (int i = 0; i < name.length(); ++i) {
                char cc = name.charAt(i);
                if (cc < ' ' || cc >= 128 && (cc < 19968 || cc > '龻')) {
                    return false;
                }
                for (int j = 0; j < sybol.length; ++j) {
                    if (cc == sybol[j]) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private void addColumn() {
        GBase8aTableColumn getc = new GBase8aTableColumn(this.gbase8aTable);
        getc.setName("column" + this.getNewColumnNum());
        try {
            getc.setTypeName(GBase8aConstants.TYPES[16]);
        } catch (DBException ignore) {
        }
        getc.setMaxLength(20L);
        getc.setRequired(false);
        getc.setHashColumn(false);
        if (!this.getColumns()) {
            MessageDialog.openError(this.getShell(), GBase8aMessages.message_error_title, GBase8aMessages.dialog_create_table_error_column_name);
        } else {
            this.list_columns.add(getc);
            this.tableViewer.setInput(this.list_columns);
            this.tableViewer.refresh();
        }
    }

    private Integer getNewColumnNum() {
        int columnNum = 1;
        TableItem[] items = this.table.getItems();
        if (items.length > 0) {
            TableItem[] var6 = items;
            int var5 = items.length;

            for (int var4 = 0; var4 < var5; ++var4) {
                TableItem item = var6[var4];
                String name = item.getText(1).trim();
                if (name.startsWith("column")) {
                    try {
                        if (Integer.valueOf(name.substring(6)) >= columnNum) {
                            columnNum = Integer.valueOf(name.substring(6)) + 1;
                        }
                    } catch (Exception var8) {
                    }
                }
            }
        }
        return columnNum;
    }

    private void addPartitionColumn(TableViewer tv, Table tb) {
        GBase8aPartition getp = new GBase8aPartition(this.gbase8aTable, null, this.gbase8aTable.getName());
        getp.setName("p" + this.getNewPartitionNameNum(tb));
        this.list_partition.add(getp);
        tv.setInput(this.list_partition);
        tv.refresh();
    }

    private Integer getNewPartitionNameNum(Table table) {
        int columnNum = 0;
        TableItem[] items = table.getItems();
        if (items.length > 0) {
            TableItem[] var7 = items;
            int var6 = items.length;
            for (int var5 = 0; var5 < var6; ++var5) {
                TableItem item = var7[var5];
                String name = item.getText(1).trim();
                if (name.startsWith("p")) {
                    try {
                        if (Integer.valueOf(name.substring(1)) >= columnNum) {
                            columnNum = Integer.valueOf(name.substring(1)) + 1;
                        }
                    } catch (Exception var9) {
                    }
                }
            }
        }
        return columnNum;
    }

    private void deletePartitionColumn(TableViewer tv, Table tb, boolean isRange) {
        TableItem[] items = tb.getSelection();
        if (items != null && items.length != 0) {
            TableItem[] var8 = items;
            int var7 = items.length;
            for (int var6 = 0; var6 < var7; ++var6) {
                TableItem selectItem = var8[var6];
                for (int i = 0; i < tb.getItemCount(); ++i) {
                    TableItem item = tb.getItem(i);
                    if (selectItem.equals(item)) {
                        GBase8aPartition getc = (GBase8aPartition) item.getData();
                        this.list_partition.remove(getc);
                        tb.remove(i);
                        tb.pack();
                        if (isRange) {
                            tb.setBounds(8, 210, 925, 121);
                        } else {
                            tb.setBounds(8, 100, 925, 121);
                        }
                    }
                }
            }
        } else {
            UIUtils.showMessageBox(null, GBase8aMessages.message_error_title, GBase8aMessages.dialog_create_table_error_select_column, 1);
        }
    }

    private List<GBase8aPartition> getPartitons() throws DBException {
        List<GBase8aPartition> list_p = null;
        Collection<GBase8aPartition> collection_partition = this.gbase8aTable.getPartitions(this.monitor);
        Map<String, GBase8aPartition> partitionMap = new LinkedHashMap();
        String partitionName = null;
        if (collection_partition != null) {
            list_p = new ArrayList();
            Iterator<GBase8aPartition> it = collection_partition.iterator();
            while (it.hasNext()) {
                GBase8aPartition partition = it.next();
                if (partition.getParent() == null) {
                    list_p.add(partition);
                } else {
                    partitionName = partition.getName();
                    GBase8aPartition parentPartition = partitionMap.get(partitionName);
                    if (parentPartition == null) {
                        partitionMap.put(partitionName, partition.getParent());
                    }
                }
            }
        }
        list_p.addAll(partitionMap.values());
        return list_p;
    }

    public void setList_columns(List<GBase8aTableColumn> list_columns) {
        this.list_columns = list_columns;
    }

    protected Point getInitialSize() {
        return new Point(980, 550);
    }

    class AddColumnAction extends Action {
        public AddColumnAction() {
            this.setText(GBase8aMessages.dialog_create_table_column_add);
        }

        public void run() {
            GBase8aTableDialog.this.addColumn();
        }
    }

    class AddHashPartitionAction extends Action {
        public AddHashPartitionAction() {
            this.setText(GBase8aMessages.dialog_create_table_partition_add);
        }

        public void run() {
            GBase8aTableDialog.this.addPartitionColumn(GBase8aTableDialog.this.tvHash, GBase8aTableDialog.this.tbHash);
        }
    }

    class AddRangePartitionAction extends Action {
        public AddRangePartitionAction() {
            this.setText(GBase8aMessages.dialog_create_table_partition_add);
        }

        public void run() {
            GBase8aTableDialog.this.addPartitionColumn(GBase8aTableDialog.this.tvRange, GBase8aTableDialog.this.tbRange);
        }
    }

    class ColumnCellModifier implements ICellModifier {
        private final TableViewer tv;

        public ColumnCellModifier(TableViewer tv) {
            this.tv = tv;
        }

        public boolean canModify(Object element, String property) {
            GBase8aTableColumn getc = (GBase8aTableColumn) element;
            if (getc.isPersisted()) {
                return false;
            }
            String typeName = getc.getTypeName().toUpperCase();
            switch (GBase8aConstants.getTYPES_INDEX().get(typeName).intValue()) {
                case 1:
                    if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_BIGINT)) {
                        if ("scale".equals(property) || "precision".equals(property) || "length".equals(property))
                            return false;
                    } else if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_INT) || typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_INTEGER)) {
                        if ("scale".equals(property) || "precision".equals(property) || "length".equals(property))
                            return false;
                    } else if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_SMALLINT)) {
                        if ("scale".equals(property) || "precision".equals(property) || "length".equals(property))
                            return false;
                    } else if ("scale".equals(property) || "precision".equals(property) || "length".equals(property)) {
                        return false;
                    }
                case 2:
                    if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_CHAR) || typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_VARCHAR)) {
                        if ("scale".equals(property) || "precision".equals(property))
                            return false;
                        if ("length".equals(property))
                            return true;
                    } else if ("scale".equals(property) || "precision".equals(property) || "length".equals(property)) {
                        return false;
                    }
                case 3:
                    if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_DECIMAL)) {
                        if ("length".equals(property)) {
                            return false;
                        }
                        if ("scale".equals(property) || "precision".equals(property)) {
                            return true;
                        }
                        break;
                    }
                    if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_FLOAT)) {
                        if ("length".equals(property)) {
                            return false;
                        }
                        if ("scale".equals(property) || "precision".equals(property)) {
                            return true;
                        }
                        break;
                    }
                    if (typeName.equalsIgnoreCase(GBase8aUtils.DATA_TYPE_DOUBLE)) {
                        if ("length".equals(property)) {
                            return false;
                        }
                        if ("scale".equals(property) || "precision".equals(property)) {
                            return true;
                        }
                        break;
                    }
                    if ("scale".equals(property) || "precision".equals(property) || "length".equals(property)) {
                        return false;
                    }
                    break;
            }
            return GBase8aTableDialog.this.isNew || !"hashColumn".equals(property);
        }

        public Object getValue(Object element, String property) {
            GBase8aTableColumn getc = (GBase8aTableColumn) element;
            if (getc != null) {
                if ("colName".equals(property)) {
                    return getc.getName();
                }
                if ("dataType".equals(property)) {
                    for (int i = 0; i < GBase8aConstants.TYPES.length; ++i) {
                        if (getc.getTypeName().equalsIgnoreCase(GBase8aConstants.TYPES[i])) {
                            return i;
                        }
                    }
                    return -1;
                }
                if ("length".equals(property)) {
                    return String.valueOf(getc.getMaxLength());
                }
                if ("notNull".equals(property)) {
                    return getc.isRequired() ? 0 : 1;
                }
                if ("default".equals(property)) {
                    return getc.getDefaultValue() == null ? "" : getc.getDefaultValue();
                }
                if ("precision".equals(property)) {
                    return String.valueOf(getc.getPrecision());
                }
                if ("scale".equals(property)) {
                    return String.valueOf(getc.getScale());
                }
                if ("comment".equals(property)) {
                    return getc.getComment() == null ? "" : getc.getComment();
                }
                if ("hashColumn".equals(property)) {
                    return getc.isHashColumn() ? 0 : 1;
                }
            }
            return null;
        }

        public void modify(Object element, String property, Object value) {
            if (element instanceof TableItem tix) {
                try {
                    this.processModify(tix, property, value);
                } catch (DBException ignore) {
                }
            } else if (element instanceof Table table) {
                int index = table.getSelectionIndex();
                TableItem ti = table.getItem(index);
                try {
                    this.processModify(ti, property, value);
                } catch (DBException ignore) {
                }
            }
        }

        private void processModify(TableItem ti, String property, Object value) throws DBException {
            if (ti != null) {
                GBase8aTableColumn getc = (GBase8aTableColumn) ti.getData();
                if (getc != null) {
                    if ("colName".equals(property)) {
                        String oldName = getc.getName();
                        int selectRow = -1;
                        int i;
                        for (i = 0; i < GBase8aTableDialog.this.table.getItemCount(); i++) {
                            TableItem item = GBase8aTableDialog.this.table.getItem(i);
                            GBase8aTableColumn itemc = (GBase8aTableColumn) item.getData();
                            if (itemc.getName().equalsIgnoreCase(oldName)) {
                                selectRow = i;
                                break;
                            }
                        }
                        if (value == null || "".equals(value)) {
                            if (UIUtils.confirmAction(GBase8aMessages.message_information_title, GBase8aMessages.dialog_create_table_error_no_column_name) &&
                                    selectRow >= 0) {
                                for (i = 0; i < GBase8aTableDialog.this.table.getItemCount(); i++) {
                                    TableItem item = GBase8aTableDialog.this.table.getItem(i);
                                    if (ti.equals(item)) {
                                        GBase8aTableDialog.this.list_columns.remove(getc);
                                        GBase8aTableDialog.this.table.remove(i);
                                        GBase8aTableDialog.this.table.pack();
                                        GBase8aTableDialog.this.table.setBounds(5, 5, 1041, 347);
                                    }
                                }
                            }
                        } else {
                            int count = 0;
                            for (int j = 0; j < GBase8aTableDialog.this.table.getItemCount(); j++) {
                                TableItem item = GBase8aTableDialog.this.table.getItem(j);
                                GBase8aTableColumn itemc = (GBase8aTableColumn) item.getData();
                                if (itemc.getName().equalsIgnoreCase((String) value) &&
                                        j != selectRow)
                                    count++;
                            }
                            if (count > 0) {
                                MessageDialog.openError(GBase8aTableDialog.this.getShell(), GBase8aMessages.message_error_title, GBase8aMessages.dialog_create_table_error_column_name_exist);
                                return;
                            }
                            if (!GBase8aTableDialog.this.check((String) value)) {
                                MessageDialog.openError(GBase8aTableDialog.this.getShell(), GBase8aMessages.message_error_title, GBase8aMessages.dialog_create_table_error_column_name);
                                return;
                            }
                            getc.setName((String) value);
                        }
                    }
                    if ("dataType".equals(property) && value != null && !"".equals(value)) {
                        Integer index = (Integer) value;
                        String typeName = GBase8aConstants.TYPES[index.intValue()];
                        getc.setTypeName(typeName);
                    }
                    if ("length".equals(property)) {
                        Long columnLength = null;
                        if (value != null && !"".equals(value)) {
                            columnLength = Long.valueOf(Long.parseLong((String) value));
                            String typeName = getc.getTypeName();
                            if (GBase8aUtils.DATA_TYPE_CHAR.equalsIgnoreCase(typeName) && (
                                    columnLength.longValue() > 255L || columnLength.longValue() < 1L)) {
                                MessageDialog.openError(GBase8aTableDialog.this.getShell(), GBase8aMessages.message_error_title, GBase8aMessages.dialog_create_table_error_column_char_length);
                                return;
                            }
                            if (GBase8aUtils.DATA_TYPE_VARCHAR.equalsIgnoreCase(typeName) && (
                                    columnLength.longValue() > 10922L || columnLength.longValue() < 1L)) {
                                MessageDialog.openError(GBase8aTableDialog.this.getShell(), GBase8aMessages.message_error_title, GBase8aMessages.dialog_create_table_error_column_varchar_length);
                                return;
                            }
                            getc.setMaxLength(columnLength.longValue());
                        }
                    }
                    if ("notNull".equals(property) && value != null && !"".equals(value)) {
                        Integer index = (Integer) value;
                        getc.setRequired((index.intValue() == 0));
                    }
                    if ("default".equals(property)) {
                        if (value != null && !"".equals(value)) {
                            getc.setDefaultValue((String) value);
                        } else {
                            getc.setDefaultValue("");
                        }
                    }
                    if ("precision".equals(property) && value != null && !"".equals(value)) {
                        int columnPrecesion = 0;
                        if (value != null && !"".equals(value)) {
                            columnPrecesion = Integer.parseInt((String) value);
                            getc.setPrecision(columnPrecesion);
                        }
                    }
                    if ("scale".equals(property) && value != null && !"".equals(value)) {
                        int columnScale = 0;
                        if (value != null && !"".equals(value)) {
                            columnScale = Integer.parseInt((String) value);
                            getc.setScale(columnScale);
                        }
                    }
                    if ("comment".equals(property)) {
                        if (value != null && !"".equals(value)) {
                            getc.setComment((String) value);
                        } else {
                            getc.setComment("");
                        }
                    }
                    if ("hashColumn".equals(property) && value != null && !"".equals(value)) {
                        Integer index = (Integer) value;
                        getc.setHashColumn((index.intValue() == 0));
                    }
                    GBase8aTableDialog.this.tableViewer.update(getc, null);
                    GBase8aTableDialog.this.tableViewer.refresh();
                }
            }
        }
    }

    class DeleteColumnAction extends Action {
        public DeleteColumnAction() {
            this.setText(GBase8aMessages.dialog_create_table_column_delete);
        }

        public void run() {
            TableItem[] items = GBase8aTableDialog.this.table.getSelection();
            if (items != null && items.length != 0) {
                TableItem[] var5 = items;
                int var4 = items.length;
                for (int var3 = 0; var3 < var4; ++var3) {
                    TableItem selectItem = var5[var3];
                    for (int i = 0; i < GBase8aTableDialog.this.table.getItemCount(); ++i) {
                        TableItem item = GBase8aTableDialog.this.table.getItem(i);
                        if (selectItem.equals(item)) {
                            GBase8aTableColumn getc = (GBase8aTableColumn) item.getData();
                            GBase8aTableDialog.this.list_columns.remove(getc);
                            GBase8aTableDialog.this.table.remove(i);
                            GBase8aTableDialog.this.table.redraw();
                        }
                    }
                }
            } else {
                UIUtils.showMessageBox(null, GBase8aMessages.message_error_title, GBase8aMessages.dialog_create_table_error_select_column, 1);
            }
        }
    }

    class DeleteHashPartitionAction extends Action {
        public DeleteHashPartitionAction() {
            this.setText(GBase8aMessages.dialog_create_table_partition_delete);
        }

        public void run() {
            GBase8aTableDialog.this.deletePartitionColumn(GBase8aTableDialog.this.tvHash, GBase8aTableDialog.this.tbHash, false);
        }
    }

    class DeleteRangePartitionAction extends Action {
        public DeleteRangePartitionAction() {
            this.setText(GBase8aMessages.dialog_create_table_partition_delete);
        }

        public void run() {
            GBase8aTableDialog.this.deletePartitionColumn(GBase8aTableDialog.this.tvRange, GBase8aTableDialog.this.tbRange, true);
        }
    }

    class PartitionCellModifier implements ICellModifier {
        private TableViewer tv;

        private Table tb;

        public PartitionCellModifier(TableViewer tv, Table tb) {
            this.tv = tv;
            this.tb = tb;
        }

        public boolean canModify(Object element, String property) {
            GBase8aPartition getp = (GBase8aPartition) element;
            if (getp.isPersisted())
                return false;
            if (!GBase8aTableDialog.this.isSubPartition && "subPartitionName".equals(property))
                return false;
            return true;
        }

        public Object getValue(Object element, String property) {
            GBase8aPartition getp = (GBase8aPartition) element;
            if (getp != null) {
                if ("partitionName".equals(property))
                    return getp.getName();
                if ("partitionValue".equals(property))
                    return (getp.getDescription() == null) ? "" : getp.getDescription();
                if ("subPartitionName".equals(property)) {
                    List<GBase8aPartition> subPartiton_list = getp.getSubPartitions();
                    String subNameStr = "";
                    if (subPartiton_list != null && !subPartiton_list.isEmpty()) {
                        for (GBase8aPartition subPartition : subPartiton_list) {
                            subNameStr = String.valueOf(subNameStr) + subPartition.getSubName() + ",";
                            subPartition.setParent(getp);
                        }
                        if (!subNameStr.isEmpty())
                            subNameStr = subNameStr.substring(0, subNameStr.length() - 1);
                        return subNameStr;
                    }
                    return "";
                }
            }
            return null;
        }

        public void modify(Object element, String property, Object value) {
            if (element instanceof TableItem) {
                TableItem ti = (TableItem) element;
                processModify(ti, property, value);
            } else if (element instanceof Table) {
                Table table = (Table) element;
                int index = table.getSelectionIndex();
                TableItem ti = table.getItem(index);
                processModify(ti, property, value);
            }
        }

        private void processModify(TableItem ti, String property, Object value) {
            if (ti != null) {
                GBase8aPartition getp = (GBase8aPartition) ti.getData();
                if (getp != null) {
                    if ("partitionName".equals(property)) {
                        String oldName = getp.getName();
                        int selectRow = -1;
                        int i;
                        for (i = 0; i < this.tb.getItemCount(); i++) {
                            TableItem item = this.tb.getItem(i);
                            GBase8aPartition itemp = (GBase8aPartition) item.getData();
                            if (itemp.getName().equalsIgnoreCase(oldName)) {
                                selectRow = i;
                                break;
                            }
                        }
                        if (value == null || "".equals(value)) {
                            if (UIUtils.confirmAction(GBase8aMessages.message_information_title, GBase8aMessages.dialog_create_table_error_no_column_name) &&
                                    selectRow >= 0)
                                for (i = 0; i < this.tb.getItemCount(); i++) {
                                    TableItem item = this.tb.getItem(i);
                                    if (ti.equals(item)) {
                                        GBase8aTableDialog.this.list_partition.remove(getp);
                                        this.tb.remove(i);
                                        this.tb.pack();
                                        this.tb.setBounds(5, 5, 1041, 147);
                                    }
                                }
                        } else {
                            int count = 0;
                            for (int j = 0; j < this.tb.getItemCount(); j++) {
                                TableItem item = this.tb.getItem(j);
                                GBase8aPartition itemp = (GBase8aPartition) item.getData();
                                if (itemp.getName().equalsIgnoreCase((String) value) &&
                                        j != selectRow)
                                    count++;
                            }
                            if (count > 0) {
                                MessageDialog.openError(GBase8aTableDialog.this.getShell(), GBase8aMessages.message_error_title, GBase8aMessages.dialog_create_table_error_column_name_exist);
                                return;
                            }
                            if (!GBase8aTableDialog.this.check((String) value)) {
                                MessageDialog.openError(GBase8aTableDialog.this.getShell(), GBase8aMessages.message_error_title, GBase8aMessages.dialog_create_table_error_column_name);
                                return;
                            }
                            getp.setName((String) value);
                        }
                    }
                    if ("partitionValue".equals(property))
                        if (value != null && !"".equals(value)) {
                            getp.setDescription((String) value);
                        } else {
                            getp.setDescription("");
                        }
                    if ("subPartitionName".equals(property) &&
                            value != null && !"".equals(value)) {
                        String[] subNames = ((String) value).split(",");
                        List<String> names = new ArrayList<String>(Arrays.asList(subNames));
                        List<GBase8aPartition> list_subPartition = getp.getSubPartitions();
                        if (list_subPartition != null) {
                            list_subPartition = new ArrayList<GBase8aPartition>();
                            getp.setSubPartitions(list_subPartition);
                        }
                        for (String name : names) {
                            GBase8aPartition newSubPartition = new GBase8aPartition(GBase8aTableDialog.this.gbase8aTable, getp, name);
                            getp.addSubPartitions(newSubPartition);
                        }
                    }
                    this.tv.update(getp, null);
                    this.tv.refresh();
                }
            }
        }
    }

}
