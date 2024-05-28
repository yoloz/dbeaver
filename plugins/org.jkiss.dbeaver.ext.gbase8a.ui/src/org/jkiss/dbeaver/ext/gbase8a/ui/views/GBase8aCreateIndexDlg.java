package org.jkiss.dbeaver.ext.gbase8a.ui.views;

import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aColumnModel;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;


public class GBase8aCreateIndexDlg extends Dialog {
    private Table tblColumn;
    private CheckboxTableViewer tableViewer;
    private Text txtIndexName;
    private Button btnOk;
    private Button btnGlobal;
    private Spinner spinner;
    private String indexName;
    private final String vcName;
    private final String database;
    private final String tablename;
    private final GBase8aDataSource datasource;
    private final ArrayList<GBase8aColumnModel> columns = new ArrayList<GBase8aColumnModel>();
    private final ArrayList<String> indexes = new ArrayList<String>();
    private GBase8aColumnModel crntColumn = null;

    private final TreeViewer treeViewer;

    private final DBRProgressMonitor monitor;

    public GBase8aCreateIndexDlg(Shell parentShell, String vcName, String database, String tablename, GBase8aDataSource datasource, TreeViewer treeViewer, DBRProgressMonitor monitor) {
        super(parentShell);
        setShellStyle(1168);
        this.vcName = vcName;
        this.database = database;
        this.tablename = tablename;
        this.datasource = datasource;
        this.treeViewer = treeViewer;

        this.monitor = monitor;
    }


    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        GridLayout gl_area = new GridLayout(1, false);
        gl_area.marginWidth = 0;
        gl_area.marginHeight = 0;
        area.setLayout(gl_area);
        Composite container = new Composite(area, 0);
        container.setLayoutData(new GridData(4, 4, true, true, 1,
                1));
        container.setLayout(new GridLayout(1, false));

        Composite composite = new Composite(container, 0);
        composite.setLayoutData(new GridData(4, 4, true, false,
                1, 1));
        composite.setLayout(new GridLayout(2, false));

        Label lblIndexName = new Label(composite, 0);
        lblIndexName.setText(GBase8aMessages.dialog_create_index_name);

        this.txtIndexName = new Text(composite, 2048);
        this.txtIndexName.setLayoutData(new GridData(4, 4, true,
                false, 1, 1));

        this.txtIndexName.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                GBase8aCreateIndexDlg.this.btnOk.setEnabled(GBase8aCreateIndexDlg.this.check());
            }
        });

        this.tableViewer = CheckboxTableViewer.newCheckList(container, 34816);

        this.tblColumn = this.tableViewer.getTable();
        this.tblColumn.setLinesVisible(true);
        this.tblColumn.setHeaderVisible(true);
        this.tblColumn.setLayoutData(new GridData(4, 4, true, true, 1,
                1));

        TableColumn tblclmnColumn = new TableColumn(this.tblColumn, 0);
        tblclmnColumn.setWidth(200);
        tblclmnColumn.setText(GBase8aMessages.dialog_create_index_column);

        TableColumn tblclmnType = new TableColumn(this.tblColumn, 0);
        tblclmnType.setWidth(200);
        tblclmnType.setText(GBase8aMessages.dialog_create_index_type);

        Composite compositeGlobal = new Composite(container, 0);
        compositeGlobal.setLayoutData(new GridData(4, 4, true,
                false, 1, 1));
        compositeGlobal.setLayout(new GridLayout(2, false));

        this.btnGlobal = new Button(compositeGlobal, 16);
        this.btnGlobal.setLayoutData(new GridData(16384, 16777216, true, false,
                1, 1));
        this.btnGlobal.setText(GBase8aMessages.dialog_create_index_global);
        this.btnGlobal.setSelection(true);


        Composite compositeBlockSize = new Composite(container, 0);
        compositeBlockSize.setLayout(new GridLayout(2, false));
        compositeBlockSize.setLayoutData(new GridData(4, 4, true,
                false, 1, 1));

        Label lblBlockSize = new Label(compositeBlockSize, 0);
        lblBlockSize.setText(GBase8aMessages.dialog_create_index_datablock_size);
        lblBlockSize.setLayoutData(new GridData(131072, 16777216, false,
                false, 1, 1));

        this.spinner = new Spinner(compositeBlockSize, 2056);
        this.spinner.setIncrement(4096);
        this.spinner.setPageIncrement(4096);
        this.spinner.setMaximum(32768);
        this.spinner.setMinimum(4096);
        this.spinner.setLayoutData(new GridData(4, 4, true, false, 1,
                1));

        this.tableViewer.setLabelProvider(new ITableLabelProvider() {
            public void removeListener(ILabelProviderListener listener) {
            }

            public boolean isLabelProperty(Object element, String property) {
                return false;
            }


            public void dispose() {
            }


            public void addListener(ILabelProviderListener listener) {
            }

            public String getColumnText(Object element, int columnIndex) {
                GBase8aColumnModel value = (GBase8aColumnModel) element;
                if (columnIndex == 0)
                    return value.getName();
                if (columnIndex == 1) {
                    return value.getType();
                }
                return null;
            }

            public Image getColumnImage(Object element, int columnIndex) {
                return null;
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


        initColumns();

        this.tableViewer.setInput(this.columns);


        this.tableViewer.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                if ((GBase8aCreateIndexDlg.this.tableViewer.getCheckedElements()).length > 1) {
                    GBase8aCreateIndexDlg.this.tableViewer.setChecked(GBase8aCreateIndexDlg.this.crntColumn, false);
                }
                GBase8aCreateIndexDlg.this.crntColumn = (GBase8aColumnModel) event.getElement();
                GBase8aCreateIndexDlg.this.btnOk.setEnabled(GBase8aCreateIndexDlg.this.check());
            }
        });

        this.btnGlobal.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                GBase8aCreateIndexDlg.this.spinner.setEnabled(GBase8aCreateIndexDlg.this.btnGlobal.getSelection());
            }
        });
        return area;
    }

    private void initColumns() {
        try (JDBCSession dbsession = DBUtils.openMetaSession(this.monitor, this.datasource, "s1");
             JDBCStatement stmt = dbsession.createStatement()) {
            String sql = "show index from `" + this.database + "`.`" + this.tablename + "` from `" + this.database + "` where index_type != 'fulltext'";
            if (this.vcName != null && !this.vcName.equalsIgnoreCase("Default")) {
                sql = "show index from `" + this.vcName + "`.`" + this.database + "`.`" + this.tablename + "` from `" + this.vcName + "`.`" + this.database + "` where index_type != 'fulltext'";
            }
            try (JDBCResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    this.indexes.add(rs.getString("Column_name").trim());
                }
            }
            sql = "show columns from `" + this.database + "`.`" + this.tablename + "` from `" + this.database + "`";
            if (this.vcName != null && !this.vcName.equalsIgnoreCase("Default")) {
                sql = "show columns from `" + this.vcName + "`.`" + this.database + "`.`" + this.tablename + "` from `" + this.vcName + "`.`" + this.database + "`";
            }
            try (JDBCResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    boolean isUsed = false;
                    for (String indexname : this.indexes) {
                        if (indexname.equalsIgnoreCase(rs.getString(1))) {
                            isUsed = true;
                            break;
                        }
                    }
                    if (!isUsed) {
                        GBase8aColumnModel columnModel = new GBase8aColumnModel(rs.getString(1), rs.getString(2));
                        this.columns.add(columnModel);
                    }
                }
            }
        } catch (Exception e) {
            MessageDialog.openWarning(Display.getCurrent().getActiveShell(),
                    GBase8aMessages.dialog_create_index_information, GBase8aMessages.dialog_create_index_create_fail + e.getMessage());
        }
    }

    protected void buttonPressed(int buttonId) {
        if (buttonId == 0) {
            String indexName = this.txtIndexName.getText().trim();
            if (!indexName.matches("\\w+")) {
                MessageDialog.openWarning(
                        Display.getCurrent().getActiveShell(), GBase8aMessages.dialog_create_index_information,
                        GBase8aMessages.dialog_create_index_error_two);
                this.txtIndexName.setFocus();
                return;
            }
            if (indexName.matches("\\d+\\w*")) {
                MessageDialog.openWarning(
                        Display.getCurrent().getActiveShell(), GBase8aMessages.dialog_create_index_information,
                        GBase8aMessages.dialog_create_index_error_one);
                this.txtIndexName.setFocus();

                return;
            }
            GBase8aColumnModel column = (GBase8aColumnModel) this.tableViewer.getCheckedElements()[0];
            String sql = "create index `" + indexName + "` on `" + this.database + "`.`" +
                    this.tablename + "` (`" + column.getName() + "`) " +
                    "key_block_size = " + this.spinner.getDigits() +
                    " USING HASH " + (
                    this.btnGlobal.getSelection() ? "GLOBAL" : "LOCAL");
            if (this.vcName != null && !this.vcName.equalsIgnoreCase("Default")) {
                sql = "create index `" + indexName + "` on `" + this.vcName + "`.`" + this.database + "`.`" +
                        this.tablename + "` (`" + column.getName() + "`) " +
                        "key_block_size = " + this.spinner.getDigits() +
                        " USING HASH " + (
                        this.btnGlobal.getSelection() ? "GLOBAL" : "LOCAL");
            }

            try (JDBCSession dbsession = DBUtils.openMetaSession(this.monitor, this.datasource, "s2");
                 JDBCStatement stmt = dbsession.createStatement()) {
                stmt.execute(sql);
                setIndexName(indexName);
                MessageDialog.openInformation(Display.getCurrent()
                        .getActiveShell(), GBase8aMessages.dialog_create_index_information, GBase8aMessages.dialog_create_index_create_sussess);
            } catch (Exception e) {
                setIndexName(null);
                MessageDialog.openError(Display.getCurrent().getActiveShell(),
                        GBase8aMessages.dialog_create_index_information, GBase8aMessages.dialog_create_index_create_fail + e.getMessage());
            }
            close();
        } else if (buttonId == 1) {
            close();
        }
    }

    private boolean check() {
        boolean flag = false;
        String indexName = this.txtIndexName.getText();
        if (!indexName.isEmpty() && (this.tableViewer.getCheckedElements()).length > 0) {
            flag = true;
        }
        return flag;
    }

    protected void createButtonsForButtonBar(Composite parent) {
        this.btnOk = createButton(parent, 0, GBase8aMessages.dialog_create_index_confirm, true);
        this.btnOk.setEnabled(false);
        createButton(parent, 1, GBase8aMessages.dialog_create_index_cancel, false);
    }

    protected Point getInitialSize() {
        return new Point(500, 400);
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(GBase8aMessages.dialog_create_index_title + this.database + "." + this.tablename);
    }

    static class CreateIndexProgress implements IRunnableWithProgress {
        private final Connection connection;

        private final String sql;

        private String errmsg = "";

        private boolean isSuccess = true;

        public CreateIndexProgress(Connection connection, String sql) {
            this.connection = connection;
            this.sql = sql;
        }

        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            monitor.beginTask("create index...", -1);
            monitor.setTaskName("create index...");
            try {
                Statement stmt = this.connection.createStatement();
                stmt.execute(this.sql);
                stmt.close();
            } catch (Exception ex) {
                this.errmsg = ex.getMessage();
                this.isSuccess = false;
            }

            monitor.done();
        }

        public String getErrmsg() {
            return this.errmsg;
        }

        public boolean isSuccess() {
            return this.isSuccess;
        }
    }

    public String getIndexName() {
        return this.indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
}