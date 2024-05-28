//package org.jkiss.dbeaver.ext.gbase8a.ui.editors;
//
//import ch.ethz.ssh2.SCPClient;
//import org.jkiss.dbeaver.ext.gbase8a.Activator;
//import org.jkiss.dbeaver.ext.gbase8a.GBase8aMessages;
//import org.jkiss.dbeaver.ext.gbase8a.log.FindDlg;
//import org.jkiss.dbeaver.ext.gbase8a.log.LogProperties;
//import org.jkiss.dbeaver.ext.gbase8a.log.OpenUserInfoDialog;
//import org.jkiss.dbeaver.ext.gbase8a.log.SWTResourceManager;
//import org.jkiss.dbeaver.ext.gbase8a.log.errorlog.ErrorLogFilterDlg;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
//import org.jkiss.dbeaver.model.DBUtils;
//import org.jkiss.dbeaver.model.exec.DBCSession;
//import org.jkiss.dbeaver.model.impl.jdbc.exec.JDBCConnectionImpl;
//import org.jkiss.dbeaver.ui.editors.IDatabaseEditorInput;
//import org.jkiss.dbeaver.ui.editors.SinglePageDatabaseEditor;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.FileReader;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.io.RandomAccessFile;
//import java.net.URL;
//import java.sql.Connection;
//import java.sql.Date;
//import java.sql.ResultSet;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.StringTokenizer;
//import org.eclipse.core.runtime.FileLocator;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.swt.events.ControlAdapter;
//import org.eclipse.swt.events.ControlEvent;
//import org.eclipse.swt.events.KeyEvent;
//import org.eclipse.swt.events.KeyListener;
//import org.eclipse.swt.events.SelectionAdapter;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.events.SelectionListener;
//import org.eclipse.swt.graphics.Point;
//import org.eclipse.swt.graphics.Rectangle;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Event;
//import org.eclipse.swt.widgets.FileDialog;
//import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.Listener;
//import org.eclipse.swt.widgets.Shell;
//import org.eclipse.swt.widgets.Table;
//import org.eclipse.swt.widgets.TableColumn;
//import org.eclipse.swt.widgets.TableItem;
//import org.eclipse.swt.widgets.Text;
//import org.eclipse.swt.widgets.ToolBar;
//import org.eclipse.swt.widgets.ToolItem;
//import org.osgi.framework.Bundle;
//
//public class GBase8aErrorLogEditor extends SinglePageDatabaseEditor<IDatabaseEditorInput> {
//    private GBase8aDataSource dataSource;
//    private Composite container;
//    private Table table;
//    private int COLUMNCOUNT = 3;
//    private Label conditionLabel;
//    private Label dateLabel;
//    private Label eventTypeLabel;
//    private Text contentLabel;
//    private Label logFileLabel = null;
//    private Composite pageControl = null;
//    private ToolItem firstToolItem = null;
//    private ToolItem preToolItem = null;
//    private ToolItem nextToolItem = null;
//    private ToolItem lastToolItem = null;
//    private Text pageNumText = null;
//    private boolean isFiltering = false;
//    private boolean filterConditionSaved = true;
//    private boolean loadFlag = false;
//    private String host;
//    private String lastSelectedFile = null;
//    private String exportFileName = null;
//    public String currentSysLogFileName;
//    private long currentFilePointer = 0L;
//    private long sumOfBytes;
//    private int sumOfPages;
//    private int sumOfLogs;
//    private int currentPage;
//    private int K = 1024;
//    private int STEP = 50;
//    private Map<Integer, Long> pagePointerMap = new HashMap();
//    private String beginDate = "";
//    private String endDate = "";
//    private String logContent = "";
//    private String logType = "";
//    private int currentRowIndex = 0;
//    private String userName;
//    private String password;
//    SelectionAdapter LoadLogAction = new SelectionAdapter() {
//        public void widgetSelected(SelectionEvent e) {
//            FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), 4096);
//            dialog.setFilterNames(new String[]{"*.log", "*.err"});
//            dialog.setFilterExtensions(new String[]{"*.log", "*.err", "*.*"});
//            if (GBase8aErrorLogEditor.this.lastSelectedFile == null) {
//                dialog.setFilterPath(System.getProperty("user.dir"));
//            } else {
//                dialog.setFilterPath(GBase8aErrorLogEditor.this.lastSelectedFile);
//            }
//
//            String filePath = dialog.open();
//            if (filePath != null) {
//                GBase8aErrorLogEditor.this.clearFilterCondition();
//                GBase8aErrorLogEditor.this.showFilterCondition();
//                GBase8aErrorLogEditor.this.isFiltering = false;
//                GBase8aErrorLogEditor.this.lastSelectedFile = filePath;
//                GBase8aErrorLogEditor.this.currentSysLogFileName = filePath;
//                GBase8aErrorLogEditor.this.exportFileName = filePath;
//                GBase8aErrorLogEditor.this.initFileInfo();
//                GBase8aErrorLogEditor.this.loadFlag = true;
//                GBase8aErrorLogEditor.this.loadJTableData(new File(filePath));
//                GBase8aErrorLogEditor.this.table.redraw();
//            }
//
//            GBase8aErrorLogEditor.this.setPageInfo();
//        }
//    };
//    SelectionAdapter ExportLogAction = new SelectionAdapter() {
//        public void widgetSelected(SelectionEvent e) {
//            FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), 8192);
//            dialog.setFilterNames(new String[]{"*.log", "*.err"});
//            dialog.setFilterExtensions(new String[]{"*.log", "*.err"});
//            dialog.setFilterPath(System.getProperty("user.dir"));
//            String filePath = dialog.open();
//            if (filePath != null) {
//                try {
//                    BufferedReader in = new BufferedReader(new FileReader(GBase8aErrorLogEditor.this.exportFileName));
//                    File file = new File(filePath);
//                    if (file.exists()) {
//                        file.delete();
//                    }
//
//                    OutputStreamWriter fileWriter = null;
//
//                    try {
//                        fileWriter = new OutputStreamWriter(new FileOutputStream(file));
//                    } catch (FileNotFoundException var11) {
//                    }
//
//                    String tempBlock = "";
//
//                    for(String s = in.readLine(); s != null; tempBlock = "") {
//                        tempBlock = tempBlock + s + "\n";
//                        s = in.readLine();
//                        if (s != null) {
//                            while(!GBase8aErrorLogEditor.this.isDateStarted(s)) {
//                                tempBlock = tempBlock + s + "\n";
//                                s = in.readLine();
//                                if (s == null) {
//                                    break;
//                                }
//                            }
//                        }
//
//                        try {
//                            if (GBase8aErrorLogEditor.this.isExportChecked(tempBlock)) {
//                                fileWriter.write(tempBlock);
//                            }
//                        } catch (IOException var10) {
//                        }
//                    }
//
//                    try {
//                        fileWriter.close();
//                    } catch (IOException var9) {
//                        System.out.println("close file error");
//                    }
//                } catch (Exception var12) {
//                    MessageDialog.openError((Shell)null, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_error, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_open_error);
//                }
//            }
//
//        }
//    };
//    SelectionAdapter FilterAction = new SelectionAdapter() {
//        public void widgetSelected(SelectionEvent e) {
//            GBase8aErrorLogEditor.this.clearFilterCondition();
//            ErrorLogFilterDlg elfd = new ErrorLogFilterDlg(Display.getDefault().getActiveShell(), GBase8aErrorLogEditor.this);
//            if (elfd.open() == 0) {
//                GBase8aErrorLogEditor.this.isFiltering = true;
//                GBase8aErrorLogEditor.this.beginDate = elfd.getBeginDate();
//                GBase8aErrorLogEditor.this.endDate = elfd.getEndDate();
//                GBase8aErrorLogEditor.this.logContent = elfd.getLogContent();
//                GBase8aErrorLogEditor.this.logType = elfd.getLogType();
//                GBase8aErrorLogEditor.this.showFilterCondition();
//                GBase8aErrorLogEditor.this.initFileInfo();
//                GBase8aErrorLogEditor.this.loadJTableData(new File(GBase8aErrorLogEditor.this.currentSysLogFileName));
//                if (!elfd.getSave()) {
//                    GBase8aErrorLogEditor.this.filterConditionSaved = false;
//                } else {
//                    GBase8aErrorLogEditor.this.filterConditionSaved = true;
//                }
//            }
//
//        }
//    };
//
//    public GBase8aErrorLogEditor() {
//    }
//
//    public void refreshPart(Object source, boolean force) {
//    }
//
//    public void createPartControl(Composite parent) {
//        this.dataSource = (GBase8aDataSource)this.getExecutionContext().getDataSource();
//        String location = getBoundlePath(Activator.getDefault().getBundle());
//        this.host = this.dataSource.getContainer().getConnectionConfiguration().getHostName();
//        this.currentSysLogFileName = location + "system.log";
//        this.userName = LogProperties.getProperty("cluster_sys_username");
//        this.password = LogProperties.getProperty("cluster_sys_password");
//        DBCSession dbsession = DBUtils.openMetaSession(this.dataSource.getMonitor(), this.dataSource, "");
//        JDBCConnectionImpl jdbcci = (JDBCConnectionImpl)dbsession;
//        Connection con = null;
//
//        try {
//            con = jdbcci.getOriginal();
//            ch.ethz.ssh2.Connection conn = new ch.ethz.ssh2.Connection(this.host);
//            conn.connect();
//            boolean flag = conn.authenticateWithPassword(this.userName, this.password);
//            if (flag) {
//                String sql = "SHOW VARIABLES LIKE 'log_error'";
//                Statement stmt = con.createStatement();
//                ResultSet rs = stmt.executeQuery(sql);
//                String logFilePath = "";
//                if (rs.next()) {
//                    logFilePath = rs.getString(2);
//                }
//
//                SCPClient scpClient = new SCPClient(conn);
//                scpClient.get(logFilePath, location);
//            } else {
//                OpenUserInfoDialog openUserInfoDialog = new OpenUserInfoDialog(Display.getCurrent().getActiveShell());
//                if (openUserInfoDialog.open() == 0) {
//                    this.createPartControl(parent);
//                    return;
//                }
//            }
//
//            conn.close();
//        } catch (Exception var32) {
//            Exception e2 = var32;
//            e2.printStackTrace();
//            MessageDialog.openError(parent.getShell(), GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_error, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_open_error);
//            return;
//        }
//
//        this.initFileInfo();
//        this.container = new Composite(parent, 0);
//        this.container.setLayout(new GridLayout(1, false));
//        ToolBar toolBar = new ToolBar(this.container, 8519680);
//        GridData gridData_4 = new GridData(4, 16777216, false, false);
//        gridData_4.widthHint = 240;
//        toolBar.setLayoutData(gridData_4);
//        ToolItem filtrateToolItem = new ToolItem(toolBar, 8);
//        filtrateToolItem.setImage(Activator.getImageDescriptor("icons/Filter.png").createImage());
//        filtrateToolItem.setToolTipText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_filter);
//        filtrateToolItem.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_filter);
//        filtrateToolItem.addSelectionListener(this.FilterAction);
//        ToolItem findToolItem = new ToolItem(toolBar, 8);
//        findToolItem.setImage(Activator.getImageDescriptor("icons/Search.png").createImage());
//        findToolItem.setToolTipText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_search);
//        findToolItem.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_search);
//        findToolItem.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                FindDlg fid = new FindDlg(GBase8aErrorLogEditor.this.container.getShell(), GBase8aErrorLogEditor.this);
//                fid.open();
//            }
//        });
//        this.conditionLabel = new Label(this.container, 0);
//        GridData gridData_3 = new GridData(256);
//        gridData_3.horizontalIndent = 5;
//        gridData_3.minimumWidth = 80;
//        this.conditionLabel.setLayoutData(gridData_3);
//        this.conditionLabel.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_filter_none);
//        final Composite composite_2 = new Composite(this.container, 2048);
//        composite_2.setLayoutData(new GridData(4, 4, true, true));
//        GridLayout gridLayout_3 = new GridLayout();
//        gridLayout_3.marginWidth = 0;
//        gridLayout_3.marginHeight = 0;
//        composite_2.setLayout(gridLayout_3);
//        this.table = new Table(composite_2, 65540);
//        this.table.setLinesVisible(true);
//        this.table.setHeaderVisible(true);
//        this.table.setLayoutData(new GridData(4, 4, true, true));
//        this.table.addListener(41, new Listener() {
//            public void handleEvent(Event e) {
//                e.height = 20;
//            }
//        });
//        final TableColumn dateColumn = new TableColumn(this.table, 16384);
//        dateColumn.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_time);
//        final TableColumn eventTypeColumn = new TableColumn(this.table, 16384);
//        eventTypeColumn.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_eventtype);
//        final TableColumn contentColumn = new TableColumn(this.table, 16384);
//        contentColumn.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_contents);
//        this.table.addSelectionListener(new SelectionListener() {
//            public void widgetSelected(SelectionEvent e) {
//                TableItem item = GBase8aErrorLogEditor.this.table.getItem(GBase8aErrorLogEditor.this.table.getSelectionIndex());
//                GBase8aErrorLogEditor.this.dateLabel.setText(item.getText(0).trim());
//                GBase8aErrorLogEditor.this.eventTypeLabel.setText(item.getText(1).trim());
//                GBase8aErrorLogEditor.this.contentLabel.setText(item.getText(2).trim());
//                GBase8aErrorLogEditor.this.currentRowIndex = GBase8aErrorLogEditor.this.table.getSelectionIndex();
//            }
//
//            public void widgetDefaultSelected(SelectionEvent e) {
//            }
//        });
//        composite_2.addControlListener(new ControlAdapter() {
//            public void controlResized(ControlEvent e) {
//                Rectangle area = composite_2.getClientArea();
//                Point preferredSize = GBase8aErrorLogEditor.this.table.computeSize(-1, -1);
//                int width = area.width - 2 * GBase8aErrorLogEditor.this.table.getBorderWidth();
//                Point oldSize;
//                if (preferredSize.y > area.height + GBase8aErrorLogEditor.this.table.getHeaderHeight()) {
//                    oldSize = GBase8aErrorLogEditor.this.table.getVerticalBar().getSize();
//                    width -= oldSize.x;
//                }
//
//                oldSize = GBase8aErrorLogEditor.this.table.getSize();
//                if (oldSize.x > area.width) {
//                    dateColumn.setWidth(width / 5);
//                    eventTypeColumn.setWidth(width / 6);
//                    contentColumn.setWidth(width - dateColumn.getWidth() - eventTypeColumn.getWidth());
//                    GBase8aErrorLogEditor.this.table.setSize(area.width, area.height);
//                } else {
//                    GBase8aErrorLogEditor.this.table.setSize(area.width, area.height);
//                    dateColumn.setWidth(width / 5);
//                    eventTypeColumn.setWidth(width / 6);
//                    contentColumn.setWidth(width - dateColumn.getWidth() - eventTypeColumn.getWidth());
//                }
//
//            }
//        });
//        Composite composite = new Composite(this.container, 0);
//        GridLayout gridLayout_1 = new GridLayout();
//        gridLayout_1.verticalSpacing = 0;
//        gridLayout_1.horizontalSpacing = 60;
//        gridLayout_1.numColumns = 2;
//        composite.setLayout(gridLayout_1);
//        GridData gridData_1 = new GridData(4, 4, true, false);
//        gridData_1.heightHint = 220;
//        composite.setLayoutData(gridData_1);
//        new Label(composite, 0);
//        this.pageControl = new Composite(composite, 0);
//        this.pageControl.setLayoutData(new GridData(131072, 16777216, false, false));
//        GridLayout gl = new GridLayout();
//        gl.numColumns = 3;
//        this.pageControl.setLayout(gl);
//        ToolBar toolBar_1 = new ToolBar(this.pageControl, 8388608);
//        this.firstToolItem = new ToolItem(toolBar_1, 8);
//        this.firstToolItem.setImage(Activator.getImageDescriptor("icons/first.png").createImage());
//        this.firstToolItem.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                GBase8aErrorLogEditor.this.currentPage = 1;
//                GBase8aErrorLogEditor.this.currentFilePointer = (Long)GBase8aErrorLogEditor.this.pagePointerMap.get(GBase8aErrorLogEditor.this.currentPage);
//                GBase8aErrorLogEditor.this.loadJTableData(new File(GBase8aErrorLogEditor.this.currentSysLogFileName));
//            }
//        });
//        this.preToolItem = new ToolItem(toolBar_1, 8);
//        this.preToolItem.setImage(Activator.getImageDescriptor("icons/pre.png").createImage());
//        this.preToolItem.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                try {
//                    GBase8aErrorLogEditor var10000 = GBase8aErrorLogEditor.this;
//                    var10000.currentPage = var10000.currentPage - 1;
//                    GBase8aErrorLogEditor.this.currentFilePointer = (Long)GBase8aErrorLogEditor.this.pagePointerMap.get(GBase8aErrorLogEditor.this.currentPage);
//                    GBase8aErrorLogEditor.this.loadJTableData(new File(GBase8aErrorLogEditor.this.currentSysLogFileName));
//                } catch (Exception var3) {
//                    Exception e1 = var3;
//                    e1.printStackTrace();
//                }
//
//            }
//        });
//        this.pageNumText = new Text(this.pageControl, 16777216);
//        GridData gd = new GridData();
//        gd.widthHint = 90;
//        this.pageNumText.setLayoutData(gd);
//        this.pageNumText.setBackground(SWTResourceManager.getColor(255, 255, 255));
//        this.pageNumText.setText("    1    ");
//        this.pageNumText.addKeyListener(new KeyListener() {
//            public void keyPressed(KeyEvent e) {
//                if (e.character == '\r') {
//                    boolean result = true;
//                    int page = 1;
//
//                    try {
//                        page = Integer.parseInt(GBase8aErrorLogEditor.this.pageNumText.getText().trim());
//                        if (page > GBase8aErrorLogEditor.this.sumOfPages) {
//                            page = GBase8aErrorLogEditor.this.sumOfPages;
//                        }
//
//                        if (page <= 0) {
//                            page = 1;
//                        }
//                    } catch (Exception var4) {
//                        result = false;
//                    }
//
//                    if (!result) {
//                        GBase8aErrorLogEditor.this.pageNumText.setText(Integer.toString(GBase8aErrorLogEditor.this.currentPage));
//                    } else {
//                        if (page == GBase8aErrorLogEditor.this.currentPage) {
//                            GBase8aErrorLogEditor.this.setPageInfo();
//                            GBase8aErrorLogEditor.this.table.setFocus();
//                            return;
//                        }
//
//                        GBase8aErrorLogEditor.this.currentPage = page;
//                        GBase8aErrorLogEditor.this.currentFilePointer = (Long)GBase8aErrorLogEditor.this.pagePointerMap.get(GBase8aErrorLogEditor.this.currentPage);
//                        GBase8aErrorLogEditor.this.loadJTableData(new File(GBase8aErrorLogEditor.this.currentSysLogFileName));
//                    }
//
//                    GBase8aErrorLogEditor.this.table.setFocus();
//                }
//
//            }
//
//            public void keyReleased(KeyEvent e) {
//            }
//        });
//        ToolBar toolBar_2 = new ToolBar(this.pageControl, 8388608);
//        this.nextToolItem = new ToolItem(toolBar_2, 8);
//        this.nextToolItem.setImage(Activator.getImageDescriptor("icons/next.png").createImage());
//        this.nextToolItem.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                try {
//                    GBase8aErrorLogEditor var10000 = GBase8aErrorLogEditor.this;
//                    var10000.currentPage = var10000.currentPage + 1;
//                    GBase8aErrorLogEditor.this.currentFilePointer = (Long)GBase8aErrorLogEditor.this.pagePointerMap.get(GBase8aErrorLogEditor.this.currentPage);
//                    GBase8aErrorLogEditor.this.loadJTableData(new File(GBase8aErrorLogEditor.this.currentSysLogFileName));
//                } catch (Exception var3) {
//                    Exception e1 = var3;
//                    e1.printStackTrace();
//                }
//
//            }
//        });
//        this.lastToolItem = new ToolItem(toolBar_2, 8);
//        this.lastToolItem.setImage(Activator.getImageDescriptor("icons/last.png").createImage());
//        this.lastToolItem.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                GBase8aErrorLogEditor.this.currentPage = GBase8aErrorLogEditor.this.sumOfPages;
//                GBase8aErrorLogEditor.this.currentFilePointer = (Long)GBase8aErrorLogEditor.this.pagePointerMap.get(GBase8aErrorLogEditor.this.currentPage);
//                GBase8aErrorLogEditor.this.loadJTableData(new File(GBase8aErrorLogEditor.this.currentSysLogFileName));
//            }
//        });
//        Label label_1 = new Label(composite, 0);
//        label_1.setLayoutData(new GridData(4, 16777216, false, false, 1, 1));
//        label_1.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_details);
//        new Label(composite, 0);
//        new Label(composite, 0);
//        new Label(composite, 0);
//        Label label_2 = new Label(composite, 0);
//        label_2.setLayoutData(new GridData(4, 16777216, false, false, 1, 1));
//        label_2.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_time);
//        this.dateLabel = new Label(composite, 0);
//        this.dateLabel.setLayoutData(new GridData(4, 16777216, true, false));
//        this.dateLabel.setText("");
//        Label label_3 = new Label(composite, 0);
//        label_3.setLayoutData(new GridData(4, 16777216, false, false, 1, 1));
//        label_3.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_eventtype);
//        this.eventTypeLabel = new Label(composite, 0);
//        this.eventTypeLabel.setLayoutData(new GridData(4, 16777216, true, false));
//        this.eventTypeLabel.setText("");
//        new Label(composite, 0);
//        new Label(composite, 0);
//        Label label = new Label(composite, 0);
//        label.setLayoutData(new GridData(4, 16777216, false, false, 1, 1));
//        label.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_contents);
//        new Label(composite, 0);
//        this.contentLabel = new Text(composite, 2626);
//        GridData mulTextData = new GridData(4, 16777216, true, false, 2, 1);
//        mulTextData.heightHint = 40;
//        this.contentLabel.setLayoutData(mulTextData);
//        this.contentLabel.setEditable(false);
//        this.contentLabel.setTextLimit(50);
//        this.contentLabel.setBackground(this.container.getBackground());
//        this.contentLabel.setText("");
//        Composite composite_1 = new Composite(this.container, 0);
//        GridData gridData_2 = new GridData(4, 16777216, true, false);
//        gridData_2.heightHint = 35;
//        composite_1.setLayoutData(gridData_2);
//        GridLayout gridLayout_2 = new GridLayout();
//        gridLayout_2.numColumns = 1;
//        composite_1.setLayout(gridLayout_2);
//        Label line = new Label(composite_1, 258);
//        line.setLayoutData(new GridData(4, 16777216, true, false));
//        this.logFileLabel = new Label(composite_1, 0);
//        this.logFileLabel.setLayoutData(new GridData(4, 16777216, true, false));
//        this.logFileLabel.setText("");
//        this.open();
//    }
//
//    public Object open() {
//        this.createTableData((File)null);
//        this.setPageInfo();
//        return "";
//    }
//
//    private void initFileInfo() {
//        this.currentFilePointer = 0L;
//        this.scanFile();
//        this.sumOfPages = this.sumOfLogs / this.STEP + 1;
//        this.currentPage = 1;
//    }
//
//    private void scanFile() {
//        File file = new File(this.currentSysLogFileName);
//        String newLine = "";
//        StringBuffer line = new StringBuffer();
//        RandomAccessFile br = null;
//        if (file.exists()) {
//            try {
//                String regexDatetime = "(\\d{6})(\\s)+(\\d{1,2}):{1}(\\d{2}):{1}(\\d{2})";
//                br = new RandomAccessFile(file, "r");
//                this.sumOfLogs = 0;
//                this.pagePointerMap.put(1, 0L);
//
//                do {
//                    newLine = br.readLine();
//                    if (newLine == null && line.length() == 0) {
//                        return;
//                    }
//
//                    if (newLine == null && line.length() > 0) {
//                        ++this.sumOfLogs;
//                        if ((this.sumOfLogs - this.STEP) % this.STEP == 1) {
//                            this.pagePointerMap.put(this.sumOfLogs / this.STEP + 1, br.getFilePointer());
//                        }
//                    } else if (newLine.length() > 15 && newLine.substring(0, 15).matches(regexDatetime) && line.length() > 0) {
//                        ++this.sumOfLogs;
//                        if ((this.sumOfLogs - this.STEP) % this.STEP == 1) {
//                            this.pagePointerMap.put(this.sumOfLogs / this.STEP + 1, br.getFilePointer());
//                        }
//
//                        line.replace(0, line.length(), "");
//                        line.append(newLine);
//                    } else {
//                        if (line.length() > 0) {
//                            line.append('\n');
//                        }
//
//                        line.append(newLine);
//                    }
//                } while(newLine != null);
//            } catch (Exception var15) {
//                Exception e = var15;
//                e.printStackTrace();
//            } finally {
//                if (br != null) {
//                    try {
//                        br.close();
//                    } catch (IOException var14) {
//                        IOException e = var14;
//                        e.printStackTrace();
//                    }
//                }
//
//            }
//        }
//
//    }
//
//    private void setPageInfo() {
//        if (this.sumOfPages > 1 && (this.table.getItemCount() != 0 || this.isFiltering)) {
//            this.pageControl.setVisible(true);
//            if (this.currentPage == 1) {
//                this.firstToolItem.setEnabled(false);
//                this.preToolItem.setEnabled(false);
//                this.nextToolItem.setEnabled(true);
//                this.lastToolItem.setEnabled(true);
//            } else if (this.currentPage == this.sumOfPages) {
//                this.firstToolItem.setEnabled(true);
//                this.preToolItem.setEnabled(true);
//                this.nextToolItem.setEnabled(false);
//                this.lastToolItem.setEnabled(false);
//            } else if (1 < this.currentPage && this.currentPage < this.sumOfPages) {
//                this.firstToolItem.setEnabled(true);
//                this.preToolItem.setEnabled(true);
//                this.nextToolItem.setEnabled(true);
//                this.lastToolItem.setEnabled(true);
//            }
//
//            this.pageNumText.setText("  " + this.currentPage + "/" + this.sumOfPages + "  ");
//        } else {
//            this.pageControl.setVisible(false);
//        }
//    }
//
//    private void setLocationFileInfo() {
//        if (this.loadFlag) {
//            File f = new File(this.currentSysLogFileName);
//            if (f.exists()) {
//                this.logFileLabel.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_error_log + this.currentSysLogFileName);
//            }
//        } else {
//            this.logFileLabel.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_server + this.host);
//        }
//
//    }
//
//    private void loadJTableData(File file) {
//        this.table.deselectAll();
//        this.createTableData(file);
//        boolean hasData = this.table.getItemCount() > 0;
//        this.currentRowIndex = 0;
//        this.dateLabel.setText(hasData ? this.table.getItem(0).getText(0) : "");
//        this.eventTypeLabel.setText(hasData ? this.table.getItem(0).getText(1) : "");
//        this.contentLabel.setText(hasData ? this.table.getItem(0).getText(2) : "");
//        this.setPageInfo();
//        this.table.redraw();
//    }
//
//    private void createTableData(File f) {
//        ArrayList tableData = null;
//        ArrayList SysLogFileData = null;
//        int numberOfRows = 0;
//
//        try {
//            SysLogFileData = this.getSysLogFileData(f);
//            numberOfRows = SysLogFileData.size();
//        } catch (IOException var10) {
//        }
//
//        tableData = new ArrayList(numberOfRows);
//        this.table.clearAll();
//        this.table.removeAll();
//
//        for(int i = 0; i <= numberOfRows - 1; ++i) {
//            TableItem item = null;
//            tableData.add(SysLogFileData.get(i));
//            item = new TableItem(this.table, 0, i);
//            item.setImage(Activator.getImageDescriptor("icons/rq.png").createImage());
//            ArrayList rowData = (ArrayList)SysLogFileData.get(i);
//            String[] text = new String[this.COLUMNCOUNT];
//
//            for(int ii = 0; ii < this.COLUMNCOUNT; ++ii) {
//                text[ii] = rowData.get(ii).toString();
//                item.setText(ii, text[ii]);
//            }
//        }
//
//        this.table.setSelection(0);
//        if (this.table.getItemCount() > 0) {
//            this.dateLabel.setText(this.table.getItem(0).getText(0));
//            this.eventTypeLabel.setText(this.table.getItem(0).getText(1));
//            this.contentLabel.setText(this.table.getItem(0).getText(2));
//        }
//
//    }
//
//    private ArrayList getSysLogFileData(File file) throws IOException {
//        ArrayList data = new ArrayList();
//        File sysLogFile;
//        if (file == null) {
//            sysLogFile = new File(this.currentSysLogFileName);
//        } else {
//            sysLogFile = file;
//        }
//
//        this.currentSysLogFileName = sysLogFile.toString();
//        this.setLocationFileInfo();
//        if (sysLogFile.exists()) {
//            RandomAccessFile br = new RandomAccessFile(sysLogFile, "r");
//            StringTokenizer tokenizer = null;
//            int numOfToken = true;
//            String dateString = "";
//            String logContentString = "";
//            long tempFilePointer = 0L;
//            tempFilePointer = this.currentFilePointer;
//            br.seek(tempFilePointer);
//            String line = br.readLine();
//            int count = 0;
//
//            while(line != null && count < this.STEP) {
//                try {
//                    ArrayList result = new ArrayList(this.table.getColumnCount());
//                    if (line.indexOf("Version") < 0 && !line.equals("")) {
//                        tokenizer = new StringTokenizer(line);
//                        logContentString = "";
//                        dateString = "";
//                        int numOfToken = -1;
//
//                        while(tokenizer.hasMoreTokens() && numOfToken < 3) {
//                            String tempString = tokenizer.nextToken();
//                            ++numOfToken;
//                            boolean isDate = true;
//                            if (numOfToken == 0) {
//                                if (tempString.length() != 6 && tempString.length() != 8) {
//                                    line = br.readLine();
//                                    tempFilePointer = br.getFilePointer();
//                                    break;
//                                }
//
//                                try {
//                                    new Integer(tempString);
//                                } catch (Exception var17) {
//                                    isDate = false;
//                                }
//
//                                if (!isDate) {
//                                    line = br.readLine();
//                                    tempFilePointer = br.getFilePointer();
//                                    break;
//                                }
//                            }
//
//                            if (numOfToken != 0 && numOfToken != 1) {
//                                if (numOfToken == 2) {
//                                    result.add(0, dateString.trim());
//                                    result.add(1, tempString);
//                                } else {
//                                    int arg0 = line.indexOf(tempString);
//                                    logContentString = line.substring(arg0, line.length());
//                                }
//                            } else {
//                                dateString = dateString + tempString + " ";
//                            }
//                        }
//
//                        if (result.size() == 2) {
//                            line = br.readLine();
//                            result.add(2, logContentString);
//                            if (result.size() >= 2) {
//                                if (result.get(1).toString().startsWith("GsDB:")) {
//                                    String s2 = result.get(2).toString();
//                                    if (s2.startsWith("Started")) {
//                                        result.set(1, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_startservice);
//                                        result.set(2, s2.substring(8, s2.length()));
//                                    } else if (s2.startsWith("Starting shutdown")) {
//                                        result.set(1, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_stopservice);
//                                    } else {
//                                        result.set(1, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_tip);
//                                    }
//                                }
//
//                                if (this.checkDate(result.get(0).toString()) && this.checkLogType(this.convertTypeOfLog(result.get(1).toString())) && this.checkLogText(result.get(2).toString())) {
//                                    data.add(this.createRow(result));
//                                    ++count;
//                                }
//                            }
//                        } else {
//                            line = br.readLine();
//                            tempFilePointer = br.getFilePointer();
//                        }
//
//                        result.clear();
//                    } else {
//                        line = br.readLine();
//                        tempFilePointer = br.getFilePointer();
//                    }
//                } catch (Exception var18) {
//                    Exception e = var18;
//                    e.printStackTrace();
//                }
//            }
//
//            br.close();
//        }
//
//        return data;
//    }
//
//    public boolean isDateStarted(String line) {
//        boolean result = true;
//        if (line.equals("")) {
//            return false;
//        } else {
//            StringTokenizer tokenizer = new StringTokenizer(line);
//            String tempString = tokenizer.nextToken();
//            if (tempString.length() != 6 && tempString.length() != 8) {
//                result = false;
//            } else {
//                try {
//                    new Integer(tempString);
//                    String yyyymmddFormat = "20" + line.substring(0, 2) + "-" + line.substring(2, 4) + "-" + line.substring(4, 6);
//                    Date.valueOf(yyyymmddFormat);
//                } catch (Exception var6) {
//                    result = false;
//                }
//            }
//
//            return result;
//        }
//    }
//
//    private boolean checkDate(String yymmdd) {
//        if (this.beginDate.equals("") && this.endDate.equals("")) {
//            return true;
//        } else {
//            String yyyymmddFormat = "20" + yymmdd.substring(0, 2) + "-" + yymmdd.substring(2, 4) + "-" + yymmdd.substring(4, 6);
//            Date d1 = Date.valueOf(yyyymmddFormat);
//            Date eDate;
//            if (this.beginDate.equals("")) {
//                eDate = Date.valueOf(this.endDate);
//                return d1.compareTo(eDate) <= 0;
//            } else {
//                Date bDate;
//                if (this.endDate.equals("")) {
//                    bDate = Date.valueOf(this.beginDate);
//                    return d1.compareTo(bDate) >= 0;
//                } else {
//                    bDate = Date.valueOf(this.beginDate);
//                    eDate = Date.valueOf(this.endDate);
//                    return d1.compareTo(bDate) >= 0 && d1.compareTo(eDate) <= 0;
//                }
//            }
//        }
//    }
//
//    private boolean checkLogType(String logTypeValue) {
//        if (this.logType.equals("")) {
//            return true;
//        } else {
//            return logTypeValue.indexOf(this.logType) >= 0;
//        }
//    }
//
//    private boolean checkLogText(String text) {
//        if (this.logContent.equals("")) {
//            return true;
//        } else {
//            return text.indexOf(this.logContent) >= 0;
//        }
//    }
//
//    private String convertTypeOfLog(String source) {
//        String target = "";
//        Map typeOfLog = new HashMap();
//        typeOfLog.put("[Note]", GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_tip);
//        typeOfLog.put("[Warning]", GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_warning);
//        typeOfLog.put("[ERROR]", GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_error);
//        if (typeOfLog.get(source) != null) {
//            target = typeOfLog.get(source).toString();
//        } else {
//            target = source;
//        }
//
//        return target;
//    }
//
//    private ArrayList createRow(ArrayList a) {
//        ArrayList result = new ArrayList(this.table.getColumnCount());
//        result.add(a.get(0));
//        result.add(this.convertTypeOfLog(a.get(1).toString()));
//        result.add(a.get(2));
//        return result;
//    }
//
//    public boolean isExportChecked(String logLine) {
//        if (logLine.equals("")) {
//            return true;
//        } else if (!this.isDateStarted(logLine)) {
//            return false;
//        } else {
//            String line = logLine;
//            StringTokenizer tokenizer = new StringTokenizer(line);
//            int numOfToken = -1;
//            String dateString = "";
//            ArrayList result = new ArrayList(this.COLUMNCOUNT);
//
//            while(tokenizer.hasMoreTokens() && numOfToken < 3) {
//                String tempString = tokenizer.nextToken();
//                ++numOfToken;
//                if (numOfToken != 0 && numOfToken != 1) {
//                    if (numOfToken == 2) {
//                        result.add(0, dateString.trim());
//                        result.add(1, tempString);
//                    } else {
//                        int arg0 = line.indexOf(tempString);
//                        result.add(2, line.substring(arg0, line.length()));
//                    }
//                } else {
//                    dateString = dateString + tempString + " ";
//                }
//            }
//
//            if (result.size() >= 2) {
//                if (result.get(1).toString().startsWith("GsDB:")) {
//                    String s2 = result.get(2).toString();
//                    if (s2.startsWith("Started")) {
//                        result.set(1, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_startservice);
//                        result.set(2, s2.substring(8, s2.length()));
//                    } else if (s2.startsWith("Starting shutdown")) {
//                        result.set(1, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_startservice);
//                    } else {
//                        result.set(1, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_tip);
//                    }
//                }
//
//                if (!this.checkDate(result.get(0).toString())) {
//                    return false;
//                }
//
//                if (!this.checkLogType(this.convertTypeOfLog(result.get(1).toString()))) {
//                    return false;
//                }
//
//                if (!this.checkLogText(result.get(2).toString())) {
//                    return false;
//                }
//            }
//
//            return true;
//        }
//    }
//
//    private void clearFilterCondition() {
//        if (!this.filterConditionSaved) {
//            this.beginDate = "";
//            this.endDate = "";
//            this.logContent = "";
//            this.logType = "";
//        }
//
//    }
//
//    private void showFilterCondition() {
//        String s = "";
//        String temp = "";
//        String fs = "";
//        if (!this.beginDate.equals("")) {
//            s = s + GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_filter_begindate + "'" + this.beginDate + "'��";
//            fs = s;
//        }
//
//        if (!this.endDate.equals("")) {
//            s = s + GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_filter_enddate + "'" + this.endDate + "'��";
//            fs = s;
//        }
//
//        if (!this.logType.equals("")) {
//            if (this.logType.length() > 8) {
//                temp = this.logType.substring(0, 6) + "��";
//            } else {
//                temp = this.logType;
//            }
//
//            s = s + GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_eventtype + "'" + temp + "'��";
//            fs = fs + GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_eventtype + "'" + this.logType + "'��";
//        }
//
//        if (!this.logContent.equals("")) {
//            if (this.logContent.length() > 8) {
//                temp = this.logContent.substring(0, 6) + "��";
//            } else {
//                temp = this.logContent;
//            }
//
//            s = s + GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_contents + "'" + temp + "'��";
//            fs = fs + GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_contents + "'" + this.logContent + "'��";
//        }
//
//        if (s.equals("")) {
//            this.conditionLabel.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_filter_none);
//        } else {
//            this.conditionLabel.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_filter + ":" + (s.length() > 72 ? s.substring(0, 70) + "..." : s.substring(0, s.length() - 1) + "��"));
//            fs = fs.substring(0, fs.lastIndexOf("��"));
//            StringBuffer sb = new StringBuffer(fs);
//            int strLength = fs.length();
//
//            for(int i = 150; i < strLength; i += 151) {
//                sb.insert(i, "\n");
//            }
//
//            this.conditionLabel.setToolTipText(sb.toString());
//        }
//
//    }
//
//    public void search(FindDlg dialog) {
//        int i = false;
//        boolean condition = true;
//        String searchWord = dialog.getLogContent();
//        String logContent = "";
//        int rowCount = this.table.getItemCount();
//        int i;
//        if (dialog.isDownDirect()) {
//            if (this.currentRowIndex >= rowCount) {
//                MessageDialog.openInformation((Shell)null, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_message, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_find_none);
//                this.currentRowIndex = rowCount;
//                return;
//            }
//
//            if (this.currentRowIndex < this.table.getSelectionIndex()) {
//                this.currentRowIndex = this.table.getSelectionIndex();
//            }
//
//            for(i = this.currentRowIndex; i < rowCount; ++i) {
//                logContent = this.table.getItem(i).getText(this.COLUMNCOUNT - 1);
//                if (dialog.isCaseSense()) {
//                    if (!dialog.isWordMatch()) {
//                        condition = logContent.indexOf(searchWord.trim()) != -1;
//                    } else {
//                        condition = logContent.indexOf(" " + searchWord.trim() + " ") != -1 || logContent.trim().indexOf(searchWord.trim() + " ") == 0 || logContent.trim().equals(searchWord.trim());
//                    }
//                } else if (!dialog.isWordMatch()) {
//                    condition = logContent.toLowerCase().indexOf(searchWord.trim().toLowerCase()) != -1;
//                } else {
//                    condition = logContent.toLowerCase().indexOf(" " + searchWord.trim().toLowerCase() + " ") != -1 || logContent.trim().toLowerCase().indexOf(searchWord.trim().toLowerCase() + " ") == 0 || logContent.trim().equalsIgnoreCase(searchWord.trim());
//                }
//
//                if (condition) {
//                    this.table.setSelection(i);
//                    this.currentRowIndex = i + 1;
//                    break;
//                }
//            }
//
//            if (i >= rowCount) {
//                MessageDialog.openInformation((Shell)null, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_message, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_find_none);
//                return;
//            }
//        }
//
//        if (!dialog.isDownDirect()) {
//            if (this.currentRowIndex <= -1) {
//                MessageDialog.openInformation((Shell)null, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_message, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_find_none);
//                this.currentRowIndex = 0;
//                return;
//            }
//
//            if (this.currentRowIndex > this.table.getSelectionIndex()) {
//                this.currentRowIndex = this.table.getSelectionIndex();
//            }
//
//            for(i = this.currentRowIndex; i >= 0; --i) {
//                logContent = this.table.getItem(i).getText(this.COLUMNCOUNT - 1);
//                if (dialog.isCaseSense()) {
//                    if (!dialog.isWordMatch()) {
//                        condition = logContent.indexOf(searchWord.trim()) != -1;
//                    } else {
//                        condition = logContent.indexOf(" " + searchWord.trim() + " ") != -1 || logContent.trim().indexOf(searchWord.trim() + " ") == 0 || logContent.trim().equals(searchWord.trim());
//                    }
//                } else if (!dialog.isWordMatch()) {
//                    condition = logContent.toLowerCase().indexOf(searchWord.trim().toLowerCase()) != -1;
//                } else {
//                    condition = logContent.toLowerCase().indexOf(" " + searchWord.trim().toLowerCase() + " ") != -1 || logContent.trim().toLowerCase().indexOf(searchWord.trim().toLowerCase() + " ") == 0 || logContent.trim().equalsIgnoreCase(searchWord.trim());
//                }
//
//                if (condition) {
//                    this.table.setSelection(i);
//                    this.currentRowIndex = i - 1;
//                    break;
//                }
//            }
//
//            if (i <= -1) {
//                MessageDialog.openInformation((Shell)null, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_message, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_errorlog_find_none);
//                return;
//            }
//        }
//
//    }
//
//    public String getBeginDate() {
//        return this.beginDate;
//    }
//
//    public String getEndDate() {
//        return this.endDate;
//    }
//
//    public String getLogContent() {
//        return this.logContent;
//    }
//
//    public String getLogType() {
//        return this.logType;
//    }
//
//    public void setFocus() {
//        this.container.setFocus();
//    }
//
//    private static final String getBoundlePath(Bundle bundle) {
//        URL url = null;
//
//        try {
//            url = FileLocator.toFileURL(Activator.getDefault().getBundle().getEntry("/"));
//        } catch (IOException var3) {
//            IOException e = var3;
//            e.printStackTrace();
//        }
//
//        return url == null ? null : url.getPath();
//    }
//}
