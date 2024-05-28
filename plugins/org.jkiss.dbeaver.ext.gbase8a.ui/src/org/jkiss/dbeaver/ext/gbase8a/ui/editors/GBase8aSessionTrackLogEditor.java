//package org.jkiss.dbeaver.ext.gbase8a.ui.editors;
//
//import ch.ethz.ssh2.Connection;
//import ch.ethz.ssh2.Session;
//import ch.ethz.ssh2.StreamGobbler;
//import org.jkiss.dbeaver.ext.gbase8a.Activator;
//import org.jkiss.dbeaver.ext.gbase8a.GBase8aMessages;
//import org.jkiss.dbeaver.ext.gbase8a.log.FindDlg;
//import org.jkiss.dbeaver.ext.gbase8a.log.LogProperties;
//import org.jkiss.dbeaver.ext.gbase8a.log.OpenUserInfoDialog;
//import org.jkiss.dbeaver.ext.gbase8a.log.sessiontracklog.QuerySessionTrackLogProgress;
//import org.jkiss.dbeaver.ext.gbase8a.log.sessiontracklog.SessionTrackLogDataManager;
//import org.jkiss.dbeaver.ext.gbase8a.log.sessiontracklog.ShowSessionLogDlg;
//import org.jkiss.dbeaver.ext.gbase8a.log.sessiontracklog.TraceLogFile;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
//import org.jkiss.dbeaver.model.DBUtils;
//import org.jkiss.dbeaver.model.exec.DBCSession;
//import org.jkiss.dbeaver.model.impl.jdbc.exec.JDBCConnectionImpl;
//import org.jkiss.dbeaver.ui.editors.IDatabaseEditorInput;
//import org.jkiss.dbeaver.ui.editors.SinglePageDatabaseEditor;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.lang.reflect.InvocationTargetException;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.sql.Date;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.StringTokenizer;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.jface.dialogs.ProgressMonitorDialog;
//import org.eclipse.swt.events.ControlAdapter;
//import org.eclipse.swt.events.ControlEvent;
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
//import org.eclipse.swt.widgets.Listener;
//import org.eclipse.swt.widgets.Shell;
//import org.eclipse.swt.widgets.Table;
//import org.eclipse.swt.widgets.TableColumn;
//import org.eclipse.swt.widgets.TableItem;
//import org.eclipse.swt.widgets.ToolBar;
//import org.eclipse.swt.widgets.ToolItem;
//
//public class GBase8aSessionTrackLogEditor extends SinglePageDatabaseEditor<IDatabaseEditorInput> {
//    private GBase8aDataSource dataSource;
//    private static ArrayList<List<String>> tableDatas;
//    private Composite container;
//    private Table table;
//    private Label logFileLabel;
//    private int NUMBER_OF_COLUMNS = 8;
//    private long sumOfRows;
//    private int sumOfPages;
//    private int currentPage;
//    private int STEP = 100;
//    private int currentRowIndex = 0;
//    private static final boolean UNLOAD = false;
//    private static final boolean LOADED = true;
//    private boolean isLoaded = false;
//    private String userName;
//    private String password;
//    private List<TraceLogFile> logFileList;
//    SelectionAdapter RefreshAction = new SelectionAdapter() {
//        public void widgetSelected(SelectionEvent e) {
//            GBase8aSessionTrackLogEditor.this.isLoaded = false;
//            QuerySessionTrackLogProgress progress = new QuerySessionTrackLogProgress(GBase8aSessionTrackLogEditor.this, true);
//            ProgressMonitorDialog pg = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
//
//            try {
//                pg.run(true, true, progress);
//            } catch (InvocationTargetException var4) {
//            } catch (InterruptedException var5) {
//            }
//
//            GBase8aSessionTrackLogEditor.this.loadJTableData();
//        }
//    };
//
//    public GBase8aSessionTrackLogEditor() {
//    }
//
//    public void refreshPart(Object source, boolean force) {
//    }
//
//    public void open() {
//        this.loadJTableData();
//    }
//
//    public void createPartControl(Composite parent) {
//        this.dataSource = (GBase8aDataSource)this.getExecutionContext().getDataSource();
//        DBCSession dbsession = DBUtils.openMetaSession(this.dataSource.getMonitor(), this.dataSource, "");
//        JDBCConnectionImpl jdbcci = (JDBCConnectionImpl)dbsession;
//
//        try {
//            SessionTrackLogDataManager.setConnection(jdbcci.getOriginal());
//        } catch (SQLException var17) {
//            SQLException e2 = var17;
//            e2.printStackTrace();
//        }
//
//        SessionTrackLogDataManager.setHostName(this.dataSource.getContainer().getConnectionConfiguration().getHostName());
//        this.container = new Composite(parent, 0);
//        this.container.setLayout(new GridLayout(1, false));
//        ToolBar toolBar = new ToolBar(this.container, 8519680);
//        GridData gridData_4 = new GridData(4, 16777216, false, false);
//        gridData_4.widthHint = 240;
//        toolBar.setLayoutData(gridData_4);
//        ToolItem findToolItem = new ToolItem(toolBar, 8);
//        findToolItem.setImage(Activator.getImageDescriptor("icons/Search.png").createImage());
//        findToolItem.setToolTipText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_search);
//        findToolItem.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_search);
//        findToolItem.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                FindDlg fid = new FindDlg(Display.getDefault().getActiveShell(), GBase8aSessionTrackLogEditor.this);
//                fid.open();
//            }
//        });
//        final Composite composite_2 = new Composite(this.container, 2048);
//        composite_2.setLayoutData(new GridData(4, 4, true, true));
//        GridLayout gridLayout_3 = new GridLayout();
//        gridLayout_3.marginWidth = 0;
//        gridLayout_3.marginHeight = 0;
//        composite_2.setLayout(gridLayout_3);
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
//        this.table.addSelectionListener(new SelectionListener() {
//            public void widgetSelected(SelectionEvent e) {
//                GBase8aSessionTrackLogEditor.this.currentRowIndex = GBase8aSessionTrackLogEditor.this.table.getSelectionIndex();
//            }
//
//            public void widgetDefaultSelected(SelectionEvent e) {
//                TableItem item = GBase8aSessionTrackLogEditor.this.table.getItem(GBase8aSessionTrackLogEditor.this.table.getSelectionIndex());
//                String fileName = item.getText(0).trim();
//                String filePath = item.getText(2).trim();
//                ShowSessionLogDlg ssLogDlg = new ShowSessionLogDlg(GBase8aSessionTrackLogEditor.this.container.getShell(), fileName, filePath, GBase8aSessionTrackLogEditor.this.dataSource);
//                ssLogDlg.open();
//                URL url = null;
//
//                try {
//                    url = new URL(System.getProperty("osgi.install.area"));
//                    String location = url.getPath() + File.separator + "sessionlogs" + File.separator;
//                    File file = new File(location + fileName);
//                    if (file.exists()) {
//                        file.delete();
//                    }
//                } catch (MalformedURLException var9) {
//                    MalformedURLException e1 = var9;
//                    e1.printStackTrace();
//                }
//
//            }
//        });
//        final TableColumn fileNameColumn = new TableColumn(this.table, 0);
//        fileNameColumn.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_sessiontracklog_file_name);
//        fileNameColumn.setAlignment(16777216);
//        final TableColumn fileSizeColumn = new TableColumn(this.table, 0);
//        fileSizeColumn.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_sessiontracklog_file_size);
//        final TableColumn filePathColumn = new TableColumn(this.table, 0);
//        filePathColumn.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_sessiontracklog_file_path);
//        composite_2.addControlListener(new ControlAdapter() {
//            public void controlResized(ControlEvent e) {
//                Rectangle area = composite_2.getClientArea();
//                Point preferredSize = GBase8aSessionTrackLogEditor.this.table.computeSize(-1, -1);
//                int var10000 = area.width;
//                GBase8aSessionTrackLogEditor.this.table.getBorderWidth();
//                if (preferredSize.y > area.height + GBase8aSessionTrackLogEditor.this.table.getHeaderHeight()) {
//                    Point vBarSize = GBase8aSessionTrackLogEditor.this.table.getVerticalBar().getSize();
//                    var10000 = vBarSize.x;
//                }
//
//                fileNameColumn.pack();
//                fileSizeColumn.pack();
//                filePathColumn.pack();
//                GBase8aSessionTrackLogEditor.this.table.setSize(area.width, area.height);
//            }
//        });
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
//        this.logFileLabel.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_host + SessionTrackLogDataManager.getHostName());
//        this.createTableDataFromList();
//        this.loadJTableData();
//    }
//
//    public void initInfo() {
//        if (this.isLoaded) {
//            this.sumOfRows = (long)tableDatas.size();
//        } else {
//            String condition = SessionTrackLogDataManager.getCondition();
//
//            try {
//                this.sumOfRows = SessionTrackLogDataManager.getTableCount(condition);
//            } catch (Exception var2) {
//            }
//        }
//
//        if (this.sumOfRows % (long)this.STEP == 0L) {
//            this.sumOfPages = (int)(this.sumOfRows / (long)this.STEP);
//        } else {
//            this.sumOfPages = (int)(this.sumOfRows / (long)this.STEP) + 1;
//        }
//
//        this.currentPage = 1;
//        SessionTrackLogDataManager.setLimitNum(this.currentPage);
//    }
//
//    private void loadJTableData() {
//        this.table.deselectAll();
//        this.table.clearAll();
//        this.table.removeAll();
//        int size = tableDatas.size();
//        if (size > 0) {
//            for(int i = 0; i < size; ++i) {
//                Object obj = tableDatas.get(i);
//                if (obj instanceof ArrayList) {
//                    ArrayList temp = (ArrayList)obj;
//                    TableItem item = new TableItem(this.table, 0, i);
//                    item.setImage(Activator.getImageDescriptor("icons/rq.png").createImage());
//                    item.setText(0, temp.get(0).toString());
//                    item.setText(1, temp.get(1).toString());
//                    item.setText(2, temp.get(2).toString());
//                }
//            }
//        } else if (this.isLoaded) {
//            MessageDialog.openWarning(Display.getCurrent().getActiveShell(), GBase8aMessages.message_warning_title, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_error_load_log);
//        }
//
//        this.table.setSelection(0);
//        this.table.getItemCount();
//        this.table.redraw();
//    }
//
//    public boolean isDateStarted(String line) {
//        boolean result = true;
//        if (line.equals("")) {
//            return false;
//        } else {
//            StringTokenizer tokenizer = new StringTokenizer(line);
//            String tempString = "";
//            if (tokenizer.hasMoreTokens()) {
//                tempString = tokenizer.nextToken();
//            }
//
//            if (tempString.length() != 6 && tempString.length() != 8 && tempString.length() != 10) {
//                result = false;
//            } else if (tempString.length() != 6 && tempString.length() != 8) {
//                try {
//                    Date.valueOf(tempString);
//                } catch (Exception var6) {
//                    result = false;
//                }
//            } else {
//                try {
//                    new Integer(tempString);
//                    String yyyymmddFormat = "20" + line.substring(0, 2) + "-" + line.substring(2, 4) + "-" + line.substring(4, 6);
//                    Date.valueOf(yyyymmddFormat);
//                } catch (Exception var7) {
//                    result = false;
//                }
//            }
//
//            return result;
//        }
//    }
//
//    public void createTableDataFromList() {
//        this.logFileList = new ArrayList();
//        int var10000;
//        if (this.currentPage != this.sumOfPages) {
//            var10000 = this.STEP;
//        } else if (this.sumOfRows % (long)this.STEP == 0L) {
//            var10000 = this.STEP;
//        } else {
//            var10000 = (int)this.sumOfRows % this.STEP;
//        }
//
//        tableDatas = new ArrayList();
//        String host = this.dataSource.getContainer().getConnectionConfiguration().getHostName();
//        Connection conn = new Connection(host);
//        this.userName = LogProperties.getProperty("cluster_sys_username");
//        this.password = LogProperties.getProperty("cluster_sys_password");
//
//        try {
//            conn.connect();
//            boolean flag = conn.authenticateWithPassword(this.userName, this.password);
//            if (flag) {
//                String logFileNames = this.runRemoteCommand("ls -ogGS /opt/gcluster/log/gcluster/*.trc", host, this.userName, this.password);
//                String[] logFiles = logFileNames.split("<br>");
//
//                for(int i = 0; i < logFiles.length; ++i) {
//                    String logFile = logFiles[i];
//                    if (logFile != null && logFile.length() > 0) {
//                        String[] logPropertis = logFile.split("\\s+");
//                        new TraceLogFile();
//                        String fileFullPath = logPropertis[logPropertis.length - 1];
//                        String fileName = fileFullPath.substring(fileFullPath.lastIndexOf("/") + 1);
//                        String filePath = fileFullPath.substring(0, fileFullPath.lastIndexOf("/") + 1);
//                        String fileSize = logPropertis[2];
//                        List<String> dataList = new ArrayList();
//                        dataList.add(0, fileName);
//                        dataList.add(1, fileSize);
//                        dataList.add(2, filePath);
//                        tableDatas.add(dataList);
//                    }
//                }
//            } else {
//                OpenUserInfoDialog openUserInfoDialog = new OpenUserInfoDialog(Display.getCurrent().getActiveShell());
//                if (openUserInfoDialog.open() == 0) {
//                    this.createTableDataFromList();
//                    return;
//                }
//            }
//        } catch (IOException var14) {
//            var14.printStackTrace();
//            MessageDialog.openError(this.container.getShell(), GBase8aMessages.message_error_title, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_error_open_file);
//        } catch (Exception var15) {
//            Exception e = var15;
//            e.printStackTrace();
//        }
//
//    }
//
//    public String runRemoteCommand(String cmd, String host, String user, String password) throws Exception {
//        StringBuffer buf = new StringBuffer(1000);
//        String rt = "-1";
//
//        try {
//            Connection con = new Connection(host);
//            con.connect();
//            boolean result = con.authenticateWithPassword(user, password);
//            if (!result) {
//                String msg = "��¼Զ�̷�����ʧ��";
//                throw new Exception(msg);
//            } else {
//                Session session = con.openSession();
//                session.execCommand(cmd);
//                InputStream stdout = new StreamGobbler(session.getStdout());
//                BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
//                String line = "";
//
//                while((line = br.readLine()) != null) {
//                    buf.append(line + "<br>");
//                }
//
//                rt = buf.toString();
//                br.close();
//                session.close();
//                con.close();
//                return rt;
//            }
//        } catch (IOException var13) {
//            IOException e = var13;
//            throw new Exception(e.getMessage(), e);
//        }
//    }
//
//    public void search(FindDlg jDialog) {
//        int i = false;
//        boolean condition = true;
//        String searchWord = jDialog.getLogContent();
//        String logContent = "";
//        int rowCount = this.table.getItemCount();
//        int i;
//        if (jDialog.isDownDirect()) {
//            if (this.currentRowIndex >= rowCount) {
//                MessageDialog.openInformation((Shell)null, GBase8aMessages.message_information_title, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_information_search_complete);
//                this.currentRowIndex = rowCount;
//                return;
//            }
//
//            if (this.currentRowIndex < this.table.getSelectionIndex()) {
//                this.currentRowIndex = this.table.getSelectionIndex();
//            }
//
//            for(i = this.currentRowIndex; i < rowCount; ++i) {
//                logContent = this.table.getItem(i).getText(this.NUMBER_OF_COLUMNS - 1);
//                if (jDialog.isCaseSense()) {
//                    if (!jDialog.isWordMatch()) {
//                        condition = logContent.indexOf(searchWord.trim()) != -1;
//                    } else {
//                        condition = logContent.indexOf(" " + searchWord.trim() + " ") != -1 || logContent.trim().indexOf(searchWord.trim() + " ") == 0 || logContent.trim().equals(searchWord.trim());
//                    }
//                } else if (!jDialog.isWordMatch()) {
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
//                MessageDialog.openInformation((Shell)null, GBase8aMessages.message_information_title, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_information_search_complete);
//                return;
//            }
//        }
//
//        if (!jDialog.isDownDirect()) {
//            if (this.currentRowIndex <= -1) {
//                MessageDialog.openInformation((Shell)null, GBase8aMessages.message_information_title, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_information_search_complete);
//                this.currentRowIndex = 0;
//                return;
//            }
//
//            if (this.currentRowIndex > this.table.getSelectionIndex()) {
//                this.currentRowIndex = this.table.getSelectionIndex();
//            }
//
//            for(i = this.currentRowIndex; i >= 0; --i) {
//                logContent = this.table.getItem(i).getText(this.NUMBER_OF_COLUMNS - 1);
//                if (jDialog.isCaseSense()) {
//                    if (!jDialog.isWordMatch()) {
//                        condition = logContent.indexOf(searchWord.trim()) != -1;
//                    } else {
//                        condition = logContent.indexOf(" " + searchWord.trim() + " ") != -1 || logContent.trim().indexOf(searchWord.trim() + " ") == 0 || logContent.trim().equals(searchWord.trim());
//                    }
//                } else if (!jDialog.isWordMatch()) {
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
//                MessageDialog.openInformation((Shell)null, GBase8aMessages.message_information_title, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_information_search_complete);
//                return;
//            }
//        }
//
//    }
//
//    public void setFocus() {
//        this.container.setFocus();
//    }
//}
