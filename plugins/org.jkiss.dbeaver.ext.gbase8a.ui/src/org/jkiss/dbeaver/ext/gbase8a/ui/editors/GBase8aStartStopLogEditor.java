//package org.jkiss.dbeaver.ext.gbase8a.ui.editors;
//
//import ch.ethz.ssh2.Connection;
//import ch.ethz.ssh2.SCPClient;
//import org.jkiss.dbeaver.ext.gbase8a.Activator;
//import org.jkiss.dbeaver.ext.gbase8a.GBase8aMessages;
//import org.jkiss.dbeaver.ext.gbase8a.log.FindDlg;
//import org.jkiss.dbeaver.ext.gbase8a.log.LogProperties;
//import org.jkiss.dbeaver.ext.gbase8a.log.OpenUserInfoDialog;
//import org.jkiss.dbeaver.ext.gbase8a.log.auditlog.QueryStartStopLogProgress;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
//import org.jkiss.dbeaver.model.DBPDataSource;
//import org.jkiss.dbeaver.model.DBUtils;
//import org.jkiss.dbeaver.model.exec.DBCSession;
//import org.jkiss.dbeaver.model.impl.jdbc.exec.JDBCConnectionImpl;
//import org.jkiss.dbeaver.ui.editors.IDatabaseEditorInput;
//import org.jkiss.dbeaver.ui.editors.SinglePageDatabaseEditor;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.lang.reflect.InvocationTargetException;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.sql.Date;
//import java.sql.ResultSet;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.StringTokenizer;
//
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.jface.dialogs.ProgressMonitorDialog;
//import org.eclipse.jface.operation.IRunnableWithProgress;
//import org.eclipse.swt.events.ControlAdapter;
//import org.eclipse.swt.events.ControlEvent;
//import org.eclipse.swt.events.ControlListener;
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
//import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.Layout;
//import org.eclipse.swt.widgets.Listener;
//import org.eclipse.swt.widgets.Table;
//import org.eclipse.swt.widgets.TableColumn;
//import org.eclipse.swt.widgets.TableItem;
//import org.eclipse.swt.widgets.ToolBar;
//import org.eclipse.swt.widgets.ToolItem;
//
//
//public class GBase8aStartStopLogEditor extends SinglePageDatabaseEditor<IDatabaseEditorInput> {
//    private Composite container;
//    private static ArrayList<List<String>> tableDatas;
//    private Table table;
//    private Label dateLabel;
//    private Label contentLabel;
//    private Label logFileLabel;
//    private String lastSelectedFile = null;
//
//    private int NUMBER_OF_COLUMNS = 2;
//
//
//    private String currentCommonLogFileName;
//
//
//    private long currentFilePointer = 0L;
//    private long sumOfBytes;
//    private long sumOfRows;
//    private int sumOfPages;
//    private int currentPage;
//    private int STEP = 100;
//
//    private int K = 1024;
//    private final int STEPF = 10 * this.K;
//
//
//    private int currentRowIndex = 0;
//
//    private int connectionId = -1;
//
//    private static final boolean UNLOAD = false;
//
//    private static final boolean LOADED = true;
//
//    private boolean isLoaded = false;
//
//    private static GBase8aDataSource dataSource;
//    private static List<String> limitedDate = new ArrayList<String>();
//    private static List<String> limitedContent = new ArrayList<String>();
//    private static JDBCConnectionImpl jdbcci = null;
//
//
//    private static final String logFileName = "express.log";
//
//
//    public void refreshPart(Object source, boolean force) {
//    }
//
//
//    public void open() {
//        loadJTableData();
//    }
//
//
//    public void createPartControl(Composite parent) {
//        dataSource = (GBase8aDataSource) getExecutionContext().getDataSource();
//
//        DBCSession dbsession = DBUtils.openMetaSession(dataSource.getMonitor(), (DBPDataSource) dataSource, "");
//        jdbcci = (JDBCConnectionImpl) dbsession;
//
//        this.container = new Composite(parent, 0);
//        this.container.setLayout((Layout) new GridLayout(1, false));
//
//        ToolBar toolBar = new ToolBar(this.container, 8519680);
//        GridData gridData_4 = new GridData(4, 16777216, false, false);
//        gridData_4.widthHint = 240;
//        toolBar.setLayoutData(gridData_4);
//
//
//        ToolItem refreshToolItem = new ToolItem(toolBar, 8);
//        refreshToolItem.setImage(Activator.getImageDescriptor("icons/Refresh.png").createImage());
//        refreshToolItem.setToolTipText(GBase8aMessages.gbase8a_log_audilog_refresh);
//        refreshToolItem.setText(GBase8aMessages.gbase8a_log_audilog_refresh);
//        refreshToolItem.addSelectionListener((SelectionListener) new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                GBase8aStartStopLogEditor.this.refreshFromDB();
//            }
//        });
//
//
//        ToolItem findToolItem = new ToolItem(toolBar, 8);
//        findToolItem.setImage(Activator.getImageDescriptor("icons/Search.png").createImage());
//        findToolItem.setToolTipText(GBase8aMessages.gbase8a_log_audilog_search);
//        findToolItem.setText(GBase8aMessages.gbase8a_log_audilog_search);
//        findToolItem.addSelectionListener((SelectionListener) new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                FindDlg fid = new FindDlg(Display.getDefault().getActiveShell(), GBase8aStartStopLogEditor.this);
//                fid.open();
//            }
//        });
//
//
//        final Composite composite_2 = new Composite(this.container, 2048);
//        composite_2.setLayoutData(new GridData(4, 4, true, true));
//        GridLayout gridLayout_3 = new GridLayout();
//        gridLayout_3.marginWidth = 0;
//        gridLayout_3.marginHeight = 0;
//        composite_2.setLayout((Layout) gridLayout_3);
//
//        this.table = new Table(composite_2, 65536);
//        this.table.setLinesVisible(true);
//        this.table.setHeaderVisible(true);
//        GridData gridData = new GridData(4, 4, true, true);
//        this.table.setLayoutData(gridData);
//        this.table.addListener(41, new Listener() {
//            public void handleEvent(Event e) {
//                e.height = 19;
//            }
//        });
//
//        this.table.addSelectionListener(new SelectionListener() {
//            public void widgetSelected(SelectionEvent e) {
//                TableItem item = GBase8aStartStopLogEditor.this.table.getItem(GBase8aStartStopLogEditor.this.table.getSelectionIndex());
//
//
//                GBase8aStartStopLogEditor.this.dateLabel.setText(item.getText(0).trim());
//
//
//                GBase8aStartStopLogEditor.this.contentLabel.setText(item.getText(1).trim());
//
//                GBase8aStartStopLogEditor.this.currentRowIndex = GBase8aStartStopLogEditor.this.table.getSelectionIndex();
//            }
//
//            public void widgetDefaultSelected(SelectionEvent e) {
//            }
//        });
//        final TableColumn dateColumn = new TableColumn(this.table, 0);
//        dateColumn.setText(GBase8aMessages.gbase8a_log_sslog_date);
//        dateColumn.setAlignment(16777216);
//
//        final TableColumn contentColumn = new TableColumn(this.table, 0);
//        contentColumn.setText(GBase8aMessages.gbase8a_log_sslog_operate_content);
//
//
//        composite_2.addControlListener((ControlListener) new ControlAdapter() {
//            public void controlResized(ControlEvent e) {
//                Rectangle area = composite_2.getClientArea();
//                Point preferredSize = GBase8aStartStopLogEditor.this.table.computeSize(-1, -1);
//                GBase8aStartStopLogEditor.this.table.getBorderWidth();
//                if (preferredSize.y > area.height + GBase8aStartStopLogEditor.this.table.getHeaderHeight()) {
//
//                    Point vBarSize = GBase8aStartStopLogEditor.this.table.getVerticalBar().getSize();
//                }
//
//                Point oldSize = GBase8aStartStopLogEditor.this.table.getSize();
//                if (oldSize.x > area.width) {
//                    dateColumn.pack();
//                    contentColumn.pack();
//
//                    GBase8aStartStopLogEditor.this.table.setSize(area.width, area.height);
//
//                } else {
//
//                    dateColumn.pack();
//                    contentColumn.pack();
//
//                    GBase8aStartStopLogEditor.this.table.setSize(area.width, area.height);
//                }
//            }
//        });
//
//        Composite composite = new Composite(this.container, 0);
//        GridLayout gridLayout_1 = new GridLayout();
//        gridLayout_1.verticalSpacing = 0;
//        gridLayout_1.horizontalSpacing = 60;
//        gridLayout_1.numColumns = 2;
//        composite.setLayout((Layout) gridLayout_1);
//        GridData gridData_1 = new GridData(4, 4, true, false);
//        gridData_1.heightHint = 220;
//        composite.setLayoutData(gridData_1);
//
//        Label label_1 = new Label(composite, 0);
//        label_1.setText(GBase8aMessages.gbase8a_log_sslog_detail_content);
//
//
//        Label label_2 = new Label(composite, 0);
//        label_2.setText(GBase8aMessages.gbase8a_log_sslog_date);
//
//        this.dateLabel = new Label(composite, 0);
//        this.dateLabel.setLayoutData(new GridData(4, 16777216, true, false));
//        this.dateLabel.setText("");
//
//        Label label_5 = new Label(composite, 0);
//        label_5.setText(GBase8aMessages.gbase8a_log_sslog_operate_content);
//
//        this.contentLabel = new Label(composite, 0);
//        this.contentLabel.setLayoutData(new GridData(4, 16777216, true, false));
//        this.contentLabel.setText("");
//
//
//        Composite composite_1 = new Composite(this.container, 0);
//        GridData gridData_2 = new GridData(4, 16777216, true, false);
//        gridData_2.heightHint = 35;
//        composite_1.setLayoutData(gridData_2);
//        GridLayout gridLayout_2 = new GridLayout();
//        gridLayout_2.numColumns = 1;
//        composite_1.setLayout((Layout) gridLayout_2);
//        Label line = new Label(composite_1, 258);
//        line.setLayoutData(new GridData(4, 16777216, true, false));
//        this.logFileLabel = new Label(composite_1, 0);
//        this.logFileLabel.setLayoutData(new GridData(4, 16777216, true, false));
//        this.logFileLabel.setText(String.valueOf(GBase8aMessages.gbase8a_log_sslog_server) + dataSource.getContainer().getConnectionConfiguration().getHostName());
//
//
//        initInfo();
//
//
//        try {
//            createTableDataFromDB();
//        } catch (Exception e1) {
//            e1.printStackTrace();
//        }
//
//
//        open();
//    }
//
//
//    public void initInfo() {
//    }
//
//
//    private void loadJTableData() {
//        this.table.deselectAll();
//
//        this.table.clearAll();
//        this.table.removeAll();
//
//        int size = tableDatas.size();
//
//        if (size > 0) {
//            for (int i = 0; i < size; i++) {
//                Object<String> obj = (Object<String>) tableDatas.get(i);
//                if (obj instanceof ArrayList) {
//                    ArrayList<E> temp = (ArrayList) obj;
//
//                    TableItem item = new TableItem(this.table, 0, i);
//                    item.setImage(Activator.getImageDescriptor("icons/rq.png").createImage());
//
//                    item.setText(0, temp.get(0).toString());
//                    item.setText(1, temp.get(1).toString());
//                }
//
//            }
//
//        } else if (this.isLoaded) {
//            MessageDialog.openWarning(
//                    Display.getCurrent().getActiveShell(), GBase8aMessages.gbase8a_log_errorlog_warning,
//                    GBase8aMessages.gbase8a_log_sslog_format_error);
//        }
//
//
//        this.table.setSelection(0);
//        if (this.table.getItemCount() > 0) {
//            TableItem item = this.table.getItem(0);
//            this.dateLabel.setText(item.getText(0).trim());
//            this.contentLabel.setText(item.getText(1).trim());
//        }
//
//        boolean hasData = (this.table.getItemCount() > 0);
//
//        this.dateLabel.setText(hasData ? this.table.getItem(0).getText(0) : "");
//        this.contentLabel.setText(hasData ? this.table.getItem(0).getText(1) : "");
//
//        this.table.redraw();
//    }
//
//
//    public void createTableDataFromDB() {
//        if (this.currentPage != this.sumOfPages) {
//
//
//        } else if (this.sumOfRows % this.STEP == 0L) {
//
//        } else {
//            (int) this.sumOfRows % this.STEP;
//        }
//
//
//        try {
//            getLimitedLogData();
//        } catch (Exception exception) {
//        }
//
//        List<String> dateList = limitedDate;
//        List<String> contentList = limitedContent;
//
//        tableDatas = new ArrayList<List<String>>();
//
//        for (int i = 0; i < dateList.size(); i++) {
//
//            List<String> tempList = new ArrayList<String>(this.NUMBER_OF_COLUMNS);
//
//            String dateString = dateList.get(i).toString().trim();
//            String contentString = contentList.get(i).toString().trim();
//
//            tempList.add(0, dateString);
//            tempList.add(1, contentString);
//
//            tableDatas.add(tempList);
//        }
//
//
//        limitedDate.clear();
//        limitedContent.clear();
//    }
//
//
//    public boolean isDateStarted(String line) {
//        boolean result = true;
//        if (line.equals("")) return false;
//        StringTokenizer tokenizer = new StringTokenizer(line);
//        String tempString = "";
//        if (tokenizer.hasMoreTokens()) tempString = tokenizer.nextToken();
//        if (tempString.length() != 6 && tempString.length() != 8 && tempString.length() != 10) {
//            result = false;
//        } else if (tempString.length() == 6 || tempString.length() == 8) {
//            try {
//                String yyyymmddFormat = "20" + line.substring(0, 2) + "-" + line.substring(2, 4) + "-" + line.substring(4, 6);
//                Date.valueOf(yyyymmddFormat);
//            } catch (Exception exception) {
//                result = false;
//            }
//        } else {
//            try {
//                Date.valueOf(tempString);
//            } catch (Exception exception) {
//                result = false;
//            }
//        }
//
//
//        return result;
//    }
//
//
//    public boolean isExportChecked(String date, String userHost, String connectionId, String eventType, String content) {
//        return true;
//    }
//
//
//    SelectionAdapter RefreshAction = new SelectionAdapter() {
//        public void widgetSelected(SelectionEvent e) {
//            GBase8aStartStopLogEditor.this.isLoaded = false;
//
//            QueryStartStopLogProgress progress = new QueryStartStopLogProgress(GBase8aStartStopLogEditor.this, true);
//            ProgressMonitorDialog pg = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
//
//            try {
//                pg.run(true, true, (IRunnableWithProgress) progress);
//            } catch (InvocationTargetException invocationTargetException) {
//
//            } catch (InterruptedException interruptedException) {
//            }
//
//
//            GBase8aStartStopLogEditor.this.loadJTableData();
//        }
//    };
//
//
//    private void refreshFromDB() {
//        QueryStartStopLogProgress progress = new QueryStartStopLogProgress(this, true);
//        ProgressMonitorDialog pg = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
//
//        try {
//            pg.run(true, true, (IRunnableWithProgress) progress);
//        } catch (InvocationTargetException invocationTargetException) {
//
//        } catch (InterruptedException interruptedException) {
//        }
//
//
//        loadJTableData();
//    }
//
//
//    public void search(FindDlg jDialog) {
//        int i = 0;
//        boolean condition = true;
//        String searchWord = jDialog.getLogContent();
//        String logContent = "";
//        int rowCount = this.table.getItemCount();
//
//
//        if (jDialog.isDownDirect()) {
//            if (this.currentRowIndex >= rowCount) {
//                MessageDialog.openInformation(null, GBase8aMessages.message_information_title, GBase8aMessages.gbase8a_log_sslog_search_none);
//                this.currentRowIndex = rowCount;
//                return;
//            }
//            if (this.currentRowIndex < this.table.getSelectionIndex())
//                this.currentRowIndex = this.table.getSelectionIndex();
//            for (i = this.currentRowIndex; i < rowCount; i++) {
//
//                logContent = this.table.getItem(i).getText(this.NUMBER_OF_COLUMNS - 1);
//                if (jDialog.isCaseSense()) {
//                    if (jDialog.isWordMatch()) {
//                        condition = !(logContent.indexOf(" " + searchWord.trim() + " ") == -1 &&
//                                logContent.trim().indexOf(String.valueOf(searchWord.trim()) + " ") != 0 &&
//                                !logContent.trim().equals(searchWord.trim()));
//                    } else {
//                        condition = (logContent.indexOf(searchWord.trim()) != -1);
//                    }
//
//                } else if (jDialog.isWordMatch()) {
//                    condition = !(logContent.toLowerCase().indexOf(" " + searchWord.trim().toLowerCase() + " ") == -1 &&
//                            logContent.trim().toLowerCase().indexOf(String.valueOf(searchWord.trim().toLowerCase()) + " ") != 0 &&
//                            !logContent.trim().equalsIgnoreCase(searchWord.trim()));
//                } else {
//                    condition = (logContent.toLowerCase().indexOf(searchWord.trim().toLowerCase()) != -1);
//                }
//
//
//                if (condition) {
//                    this.table.setSelection(i);
//                    this.currentRowIndex = i + 1;
//                    break;
//                }
//            }
//            if (i >= rowCount) {
//                MessageDialog.openInformation(null, GBase8aMessages.message_information_title, GBase8aMessages.gbase8a_log_sslog_search_none);
//
//                return;
//            }
//        }
//
//        if (!jDialog.isDownDirect()) {
//            if (this.currentRowIndex <= -1) {
//                MessageDialog.openInformation(null, GBase8aMessages.message_information_title, GBase8aMessages.gbase8a_log_sslog_search_none);
//                this.currentRowIndex = 0;
//                return;
//            }
//            if (this.currentRowIndex > this.table.getSelectionIndex())
//                this.currentRowIndex = this.table.getSelectionIndex();
//            for (i = this.currentRowIndex; i >= 0; i--) {
//                logContent = this.table.getItem(i).getText(this.NUMBER_OF_COLUMNS - 1);
//                if (jDialog.isCaseSense()) {
//                    if (jDialog.isWordMatch()) {
//                        condition = !(logContent.indexOf(" " + searchWord.trim() + " ") == -1 &&
//                                logContent.trim().indexOf(String.valueOf(searchWord.trim()) + " ") != 0 &&
//                                !logContent.trim().equals(searchWord.trim()));
//                    } else {
//                        condition = (logContent.indexOf(searchWord.trim()) != -1);
//                    }
//
//                } else if (jDialog.isWordMatch()) {
//                    condition = !(logContent.toLowerCase().indexOf(" " + searchWord.trim().toLowerCase() + " ") == -1 &&
//                            logContent.trim().toLowerCase().indexOf(String.valueOf(searchWord.trim().toLowerCase()) + " ") != 0 &&
//                            !logContent.trim().equalsIgnoreCase(searchWord.trim()));
//                } else {
//                    condition = (logContent.toLowerCase().indexOf(searchWord.trim().toLowerCase()) != -1);
//                }
//
//                if (condition) {
//                    this.table.setSelection(i);
//                    this.currentRowIndex = i - 1;
//                    break;
//                }
//            }
//            if (i <= -1) {
//                MessageDialog.openInformation(null, GBase8aMessages.message_information_title, GBase8aMessages.gbase8a_log_sslog_search_none);
//                return;
//            }
//        }
//    }
//
//    public static void getLimitedLogData() {
//        File file;
//        Connection conn = null;
//        URL url = null;
//        try {
//            url = new URL(System.getProperty("osgi.install.area"));
//        } catch (MalformedURLException e) {
//
//            log.error(e);
//        }
//        String launcher_path = url.getPath();
//
//        try {
//            LogProperties.reload();
//
//            String userName = LogProperties.getProperty("cluster_sys_username");
//            String password = LogProperties.getProperty("cluster_sys_password");
//
//            conn = new Connection(dataSource.getContainer().getConnectionConfiguration().getHostName());
//
//            conn.connect();
//            boolean flag = conn.authenticateWithPassword(userName, password);
//            if (flag) {
//                String sql = "SHOW VARIABLES LIKE 'log_error'";
//                Statement stmt = jdbcci.getOriginal().createStatement();
//                ResultSet rs = stmt.executeQuery(sql);
//
//                String logFilePath = "";
//                if (rs.next()) {
//                    logFilePath = rs.getString(2);
//                }
//
//
//                File file1 = new File(String.valueOf(launcher_path) + "express.log");
//                if (file1.exists()) {
//                    file1.delete();
//                    file1.createNewFile();
//                } else {
//                    file1.createNewFile();
//                }
//
//                logFilePath = String.valueOf(logFilePath.substring(0, logFilePath.lastIndexOf('/') + 1)) + "express.log";
//
//                SCPClient scpClient = new SCPClient(conn);
//                scpClient.get(logFilePath, launcher_path);
//
//                BufferedReader reader = null;
//                reader = new BufferedReader(new FileReader(String.valueOf(launcher_path) + "express.log"));
//                String tempString = null;
//
//                while ((tempString = reader.readLine()) != null) {
//                    if (tempString.contains("Express Engine Started") || tempString.contains("Express Engine Shutdown")) {
//                        String[] str = tempString.split(" ");
//                        limitedDate.add(String.valueOf(str[0]) + " " + str[1]);
//                        String content = "";
//                        for (int i = 2; i < str.length; i++) {
//                            content = String.valueOf(content) + " " + str[i];
//                        }
//                        limitedContent.add(content);
//                    }
//
//                }
//            } else {
//
//                OpenUserInfoDialog openUserInfoDialog = new OpenUserInfoDialog(Display.getCurrent().getActiveShell());
//                if (openUserInfoDialog.open() == 0) {
//
//
//                    getLimitedLogData();
//
//
//                    return;
//                }
//            }
//        } catch (MalformedURLException e) {
//            log.error(e);
//        } catch (Exception e) {
//            log.error(e);
//        } finally {
//            if (conn != null) conn.close();
//            File file1 = new File(String.valueOf(launcher_path) + "express.log");
//            if (file1.exists()) {
//                file1.delete();
//            }
//        }
//    }
//
//
//    public void setFocus() {
//        this.container.setFocus();
//    }
//}
//
//
///* Location:              /data/yolo/GBaseDataStudio_9.5.2.0_build21_Windows_x86_64/GBaseDataStudio/plugins/cn.gbase.studio.ext.gbase8a_1.0.0.jar!/cn/gbase/studio/ext/gbase8a/editors/GBase8aStartStopLogEditor.class
// * Java compiler version: 6 (50.0)
// * JD-Core Version:       1.1.3
// */