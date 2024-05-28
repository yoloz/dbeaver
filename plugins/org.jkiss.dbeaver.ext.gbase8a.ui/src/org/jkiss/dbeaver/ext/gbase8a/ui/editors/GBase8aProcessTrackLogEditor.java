//package org.jkiss.dbeaver.ext.gbase8a.ui.editors;
//
//import org.jkiss.dbeaver.ext.gbase8a.Activator;
//import org.jkiss.dbeaver.ext.gbase8a.GBase8aMessages;
//import org.jkiss.dbeaver.ext.gbase8a.log.FindDlg;
//import org.jkiss.dbeaver.ext.gbase8a.log.processtracklog.ProcessTrackLogDataManager;
//import org.jkiss.dbeaver.ext.gbase8a.log.processtracklog.QueryProcessTrackLogProgress;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
//import org.jkiss.dbeaver.model.DBUtils;
//import org.jkiss.dbeaver.model.exec.DBCSession;
//import org.jkiss.dbeaver.model.impl.jdbc.exec.JDBCConnectionImpl;
//import org.jkiss.dbeaver.ui.editors.IDatabaseEditorInput;
//import org.jkiss.dbeaver.ui.editors.SinglePageDatabaseEditor;
//import java.lang.reflect.InvocationTargetException;
//import java.sql.Date;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Properties;
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
//import org.eclipse.swt.widgets.Text;
//import org.eclipse.swt.widgets.ToolBar;
//import org.eclipse.swt.widgets.ToolItem;
//
//public class GBase8aProcessTrackLogEditor extends SinglePageDatabaseEditor<IDatabaseEditorInput> {
//    private GBase8aDataSource dataSource;
//    private static ArrayList<List<String>> tableDatas;
//    private Composite container;
//    private Table table;
//    private Label idLabel;
//    private Label userLabel;
//    private Label hostLabel;
//    private Label dbLabel;
//    private Label commandLabel;
//    private Label timeLabel;
//    private Label stateLabel;
//    private Text infoLabel;
//    private Label logFileLabel;
//    private int NUMBER_OF_COLUMNS = 8;
//    private long sumOfRows;
//    private int sumOfPages;
//    private int currentPage;
//    private int STEP = 100;
//    private int K = 1024;
//    private int currentRowIndex = 0;
//    private static final boolean UNLOAD = false;
//    private static final boolean LOADED = true;
//    private boolean isLoaded = false;
//    SelectionAdapter RefreshAction = new SelectionAdapter() {
//        public void widgetSelected(SelectionEvent e) {
//            GBase8aProcessTrackLogEditor.this.isLoaded = false;
//            QueryProcessTrackLogProgress progress = new QueryProcessTrackLogProgress(GBase8aProcessTrackLogEditor.this, true);
//            ProgressMonitorDialog pg = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
//
//            try {
//                pg.run(true, true, progress);
//            } catch (InvocationTargetException var4) {
//            } catch (InterruptedException var5) {
//            }
//
//            GBase8aProcessTrackLogEditor.this.loadJTableData();
//        }
//    };
//
//    public GBase8aProcessTrackLogEditor() {
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
//            ProcessTrackLogDataManager.setConnection(jdbcci.getOriginal());
//        } catch (SQLException var38) {
//            SQLException e2 = var38;
//            e2.printStackTrace();
//        }
//
//        ProcessTrackLogDataManager.setHostName(this.dataSource.getContainer().getConnectionConfiguration().getHostName());
//        this.container = new Composite(parent, 0);
//        this.container.setLayout(new GridLayout(1, false));
//        ToolBar toolBar = new ToolBar(this.container, 8519680);
//        GridData gridData_4 = new GridData(4, 16777216, false, false);
//        gridData_4.widthHint = 240;
//        toolBar.setLayoutData(gridData_4);
//        ToolItem refreshToolItem = new ToolItem(toolBar, 8);
//        refreshToolItem.setImage(Activator.getImageDescriptor("icons/Refresh.png").createImage());
//        refreshToolItem.setToolTipText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_refresh);
//        refreshToolItem.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_refresh);
//        refreshToolItem.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                GBase8aProcessTrackLogEditor.this.refreshFromDB();
//            }
//        });
//        ToolItem findToolItem = new ToolItem(toolBar, 8);
//        findToolItem.setImage(Activator.getImageDescriptor("icons/Search.png").createImage());
//        findToolItem.setToolTipText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_search);
//        findToolItem.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_search);
//        findToolItem.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                FindDlg fid = new FindDlg(Display.getDefault().getActiveShell(), GBase8aProcessTrackLogEditor.this);
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
//                TableItem item = GBase8aProcessTrackLogEditor.this.table.getItem(GBase8aProcessTrackLogEditor.this.table.getSelectionIndex());
//                GBase8aProcessTrackLogEditor.this.idLabel.setText(item.getText(0).trim());
//                GBase8aProcessTrackLogEditor.this.userLabel.setText(item.getText(1).trim());
//                GBase8aProcessTrackLogEditor.this.hostLabel.setText(item.getText(2).trim());
//                GBase8aProcessTrackLogEditor.this.dbLabel.setText(item.getText(3).trim());
//                GBase8aProcessTrackLogEditor.this.commandLabel.setText(item.getText(4).trim());
//                GBase8aProcessTrackLogEditor.this.timeLabel.setText(item.getText(5).trim());
//                GBase8aProcessTrackLogEditor.this.stateLabel.setText(item.getText(6).trim());
//                GBase8aProcessTrackLogEditor.this.infoLabel.setText(item.getText(7).trim());
//                GBase8aProcessTrackLogEditor.this.currentRowIndex = GBase8aProcessTrackLogEditor.this.table.getSelectionIndex();
//            }
//
//            public void widgetDefaultSelected(SelectionEvent e) {
//            }
//        });
//        final TableColumn idColumn = new TableColumn(this.table, 0);
//        idColumn.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_processtracklog_table_sessionid);
//        idColumn.setAlignment(16777216);
//        final TableColumn userColumn = new TableColumn(this.table, 0);
//        userColumn.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_processtracklog_table_user);
//        final TableColumn hostColumn = new TableColumn(this.table, 0);
//        hostColumn.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_processtracklog_table_host);
//        final TableColumn dbColumn = new TableColumn(this.table, 0);
//        dbColumn.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_processtracklog_table_database);
//        final TableColumn commandColumn = new TableColumn(this.table, 0);
//        commandColumn.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_processtracklog_table_command);
//        final TableColumn timeColumn = new TableColumn(this.table, 0);
//        timeColumn.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_processtracklog_table_time);
//        final TableColumn stateColumn = new TableColumn(this.table, 0);
//        stateColumn.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_processtracklog_table_state);
//        final TableColumn infoColumn = new TableColumn(this.table, 0);
//        infoColumn.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_processtracklog_table_information);
//        composite_2.addControlListener(new ControlAdapter() {
//            public void controlResized(ControlEvent e) {
//                Rectangle area = composite_2.getClientArea();
//                Point preferredSize = GBase8aProcessTrackLogEditor.this.table.computeSize(-1, -1);
//                int var10000 = area.width;
//                GBase8aProcessTrackLogEditor.this.table.getBorderWidth();
//                if (preferredSize.y > area.height + GBase8aProcessTrackLogEditor.this.table.getHeaderHeight()) {
//                    Point vBarSize = GBase8aProcessTrackLogEditor.this.table.getVerticalBar().getSize();
//                    var10000 = vBarSize.x;
//                }
//
//                idColumn.pack();
//                userColumn.pack();
//                hostColumn.pack();
//                commandColumn.pack();
//                timeColumn.pack();
//                stateColumn.pack();
//                dbColumn.pack();
//                infoColumn.pack();
//                GBase8aProcessTrackLogEditor.this.table.setSize(area.width, area.height);
//            }
//        });
//        Composite composite = new Composite(this.container, 0);
//        GridLayout gridLayout_1 = new GridLayout();
//        gridLayout_1.verticalSpacing = 0;
//        gridLayout_1.horizontalSpacing = 60;
//        gridLayout_1.numColumns = 2;
//        composite.setLayout(gridLayout_1);
//        GridData gridData_1 = new GridData(4, 4, true, false);
//        gridData_1.heightHint = 320;
//        composite.setLayoutData(gridData_1);
//        Label label_1 = new Label(composite, 0);
//        label_1.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_details);
//        new Label(composite, 0);
//        new Label(composite, 0);
//        new Label(composite, 0);
//        new Label(composite, 0);
//        new Label(composite, 0);
//        Label label_2 = new Label(composite, 0);
//        label_2.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_processtracklog_table_sessionid);
//        this.idLabel = new Label(composite, 0);
//        this.idLabel.setLayoutData(new GridData(4, 16777216, true, false));
//        this.idLabel.setText("");
//        Label label_sql = new Label(composite, 0);
//        label_sql.setAlignment(16384);
//        label_sql.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_processtracklog_table_user);
//        this.userLabel = new Label(composite, 0);
//        this.userLabel.setLayoutData(new GridData(4, 16777216, true, false));
//        this.userLabel.setText("");
//        Label label_3 = new Label(composite, 0);
//        label_3.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_processtracklog_table_host);
//        this.hostLabel = new Label(composite, 0);
//        this.hostLabel.setLayoutData(new GridData(4, 16777216, true, false));
//        this.hostLabel.setText("");
//        Label label_4 = new Label(composite, 0);
//        label_4.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_processtracklog_table_database);
//        this.dbLabel = new Label(composite, 0);
//        this.dbLabel.setLayoutData(new GridData(4, 16777216, true, false));
//        this.dbLabel.setText("");
//        Label label_5 = new Label(composite, 0);
//        label_5.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_processtracklog_table_command);
//        this.commandLabel = new Label(composite, 0);
//        this.commandLabel.setLayoutData(new GridData(4, 16777216, true, false));
//        this.commandLabel.setText("");
//        Label label_6 = new Label(composite, 0);
//        label_6.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_processtracklog_table_time);
//        this.timeLabel = new Label(composite, 0);
//        this.timeLabel.setLayoutData(new GridData(4, 16777216, true, false));
//        this.timeLabel.setText("");
//        Label label_7 = new Label(composite, 0);
//        label_7.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_processtracklog_table_state);
//        this.stateLabel = new Label(composite, 0);
//        this.stateLabel.setLayoutData(new GridData(4, 16777216, true, false));
//        this.stateLabel.setText("");
//        Label label_8 = new Label(composite, 0);
//        label_8.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_processtracklog_table_information);
//        new Label(composite, 0);
//        new Label(composite, 0);
//        this.infoLabel = new Text(composite, 2626);
//        GridData mulTextData = new GridData(4, 16777216, true, false, 2, 1);
//        if ((new Properties(System.getProperties())).getProperty("os.name").toLowerCase().indexOf("windows") < 0) {
//            mulTextData.heightHint = 40;
//        } else {
//            mulTextData.heightHint = 40;
//        }
//
//        this.infoLabel.setLayoutData(mulTextData);
//        this.infoLabel.setEditable(false);
//        this.infoLabel.setBackground(this.container.getBackground());
//        this.infoLabel.setText("");
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
//        this.logFileLabel.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_host + ProcessTrackLogDataManager.getHostName());
//        this.initInfo();
//
//        try {
//            this.createTableDataFromDB();
//        } catch (Exception var37) {
//            Exception e1 = var37;
//            e1.printStackTrace();
//        }
//
//        this.open();
//    }
//
//    public void initInfo() {
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
//                    item.setText(3, temp.get(3).toString());
//                    item.setText(4, temp.get(4).toString());
//                    item.setText(5, temp.get(5).toString());
//                    item.setText(6, temp.get(6).toString());
//                    item.setText(7, temp.get(7).toString());
//                    String display = temp.get(7).toString();
//                    if (display.length() > 100) {
//                        display = display.substring(0, 99) + "...";
//                    }
//
//                    item.setText(7, display);
//                    item.setText(8, temp.get(7).toString());
//                }
//            }
//        } else if (this.isLoaded) {
//            MessageDialog.openWarning(Display.getCurrent().getActiveShell(), GBase8aMessages.message_warning_title, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_error_load_log);
//        }
//
//        this.table.setSelection(0);
//        if (this.table.getItemCount() > 0) {
//            TableItem item = this.table.getItem(0);
//            this.idLabel.setText(item.getText(0).trim());
//            this.userLabel.setText(item.getText(1).trim());
//            this.hostLabel.setText(item.getText(2).trim());
//            this.dbLabel.setText(item.getText(3).trim());
//            this.commandLabel.setText(item.getText(4).trim());
//            this.timeLabel.setText(item.getText(5).trim());
//            this.stateLabel.setText(item.getText(6).trim());
//            this.infoLabel.setText(item.getText(7).trim());
//        }
//
//        boolean hasData = this.table.getItemCount() > 0;
//        this.idLabel.setText(hasData ? this.table.getItem(0).getText(0) : "");
//        this.userLabel.setText(hasData ? this.table.getItem(0).getText(1) : "");
//        this.hostLabel.setText(hasData ? this.table.getItem(0).getText(2) : "");
//        this.dbLabel.setText(hasData ? this.table.getItem(0).getText(3) : "");
//        this.commandLabel.setText(hasData ? this.table.getItem(0).getText(4) : "");
//        this.timeLabel.setText(hasData ? this.table.getItem(0).getText(5) : "");
//        this.stateLabel.setText(hasData ? this.table.getItem(0).getText(6) : "");
//        this.infoLabel.setText(hasData ? this.table.getItem(0).getText(7) : "");
//        this.table.redraw();
//    }
//
//    public void createTableDataFromDB() {
//        int var10000;
//        if (this.currentPage != this.sumOfPages) {
//            var10000 = this.STEP;
//        } else if (this.sumOfRows % (long)this.STEP == 0L) {
//            var10000 = this.STEP;
//        } else {
//            var10000 = (int)this.sumOfRows % this.STEP;
//        }
//
//        try {
//            ProcessTrackLogDataManager.getLimitedLogData();
//        } catch (Exception var19) {
//        }
//
//        List idList = ProcessTrackLogDataManager.getLimitedId();
//        List userList = ProcessTrackLogDataManager.getLimitedUser();
//        List hostList = ProcessTrackLogDataManager.getLimitedHost();
//        List dbList = ProcessTrackLogDataManager.getLimitedDb();
//        List commandList = ProcessTrackLogDataManager.getLimitedCommand();
//        List timeList = ProcessTrackLogDataManager.getLimitedTime();
//        List stateList = ProcessTrackLogDataManager.getLimitedState();
//        List infoList = ProcessTrackLogDataManager.getLimitedInfo();
//        tableDatas = new ArrayList();
//
//        for(int i = 0; i < idList.size(); ++i) {
//            List<String> tempList = new ArrayList(this.NUMBER_OF_COLUMNS);
//            String idString = idList.get(i).toString().trim();
//            String userString = userList.get(i).toString().trim();
//            String hostString = hostList.get(i).toString().trim();
//            String dbString = dbList.get(i).toString().trim();
//            String commandString = commandList.get(i).toString().trim();
//            String timeString = timeList.get(i).toString().trim();
//            String stateString = stateList.get(i).toString().trim();
//            String infoString = infoList.get(i).toString().trim();
//            tempList.add(0, idString);
//            tempList.add(1, userString);
//            tempList.add(2, hostString);
//            tempList.add(3, dbString);
//            tempList.add(4, commandString);
//            tempList.add(5, timeString);
//            tempList.add(6, stateString);
//            tempList.add(7, infoString);
//            tableDatas.add(tempList);
//        }
//
//        ProcessTrackLogDataManager.clearLimitedLogData();
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
//    private void refreshFromDB() {
//        QueryProcessTrackLogProgress progress = new QueryProcessTrackLogProgress(this, true);
//        ProgressMonitorDialog pg = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
//
//        try {
//            pg.run(true, true, progress);
//        } catch (InvocationTargetException var3) {
//        } catch (InterruptedException var4) {
//        }
//
//        this.loadJTableData();
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
