//package org.jkiss.dbeaver.ext.gbase8a.ui.editors;
//
//import org.jkiss.dbeaver.ext.gbase8a.Activator;
//import org.jkiss.dbeaver.ext.gbase8a.GBase8aMessages;
//import org.jkiss.dbeaver.ext.gbase8a.log.FindDlg;
//import org.jkiss.dbeaver.ext.gbase8a.log.SWTResourceManager;
//import org.jkiss.dbeaver.ext.gbase8a.log.Server;
//import org.jkiss.dbeaver.ext.gbase8a.log.auditlog.AuditLogDataManager;
//import org.jkiss.dbeaver.ext.gbase8a.log.auditlog.AuditLogFilterDlg;
//import org.jkiss.dbeaver.ext.gbase8a.log.auditlog.ExportAuditLogProgress;
//import org.jkiss.dbeaver.ext.gbase8a.log.auditlog.QueryAuditLogProgress;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
//import org.jkiss.dbeaver.model.DBUtils;
//import org.jkiss.dbeaver.model.exec.DBCSession;
//import org.jkiss.dbeaver.model.impl.jdbc.exec.JDBCConnectionImpl;
//import org.jkiss.dbeaver.ui.editors.IDatabaseEditorInput;
//import org.jkiss.dbeaver.ui.editors.SinglePageDatabaseEditor;
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.FileReader;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.io.RandomAccessFile;
//import java.lang.reflect.InvocationTargetException;
//import java.sql.Date;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//import java.util.StringTokenizer;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.jface.dialogs.ProgressMonitorDialog;
//import org.eclipse.swt.events.ControlAdapter;
//import org.eclipse.swt.events.ControlEvent;
//import org.eclipse.swt.events.KeyEvent;
//import org.eclipse.swt.events.KeyListener;
//import org.eclipse.swt.events.MouseAdapter;
//import org.eclipse.swt.events.MouseEvent;
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
//
//public class GBase8aAuditLogEditor extends SinglePageDatabaseEditor<IDatabaseEditorInput> {
//    private GBase8aDataSource dataSource;
//    private static ArrayList<List<String>> tableDatas;
//    private Composite container;
//    private Table table;
//    private Label conditionLabel;
//    private Label dateLabel;
//    private Label userLabel;
//    private Label hostLabel;
//    private Label queryTimeLabel;
//    private Label rowsLabel;
//    private Label dbLabel;
//    private Label typeLabel;
//    private Text contentLabel;
//    private Label logFileLabel;
//    private Composite pageControl = null;
//    private ToolItem exportLogToolItem = null;
//    private ToolItem firstToolItem = null;
//    private ToolItem preToolItem = null;
//    private ToolItem nextToolItem = null;
//    private ToolItem lastToolItem = null;
//    private Text pageNumText = null;
//    private String exportFileName = null;
//    private int NUMBER_OF_COLUMNS = 8;
//    private String currentCommonLogFileName;
//    private long currentFilePointer = 0L;
//    private long sumOfBytes;
//    private long sumOfRows;
//    private int sumOfPages;
//    private int currentPage;
//    private int STEP = 100;
//    private int K = 1024;
//    private final int STEPF;
//    private int currentRowIndex;
//    private String beginDate;
//    private String endDate;
//    private String connType;
//    private String querytime;
//    private String logContent;
//    private String user;
//    private String host;
//    private String row;
//    private String db;
//    private boolean isFiltering;
//    private boolean filterConditionSaved;
//    private static final boolean UNLOAD = false;
//    private static final boolean LOADED = true;
//    private boolean isLoaded;
//    SelectionAdapter RefreshAction;
//
//    public GBase8aAuditLogEditor() {
//        this.STEPF = 10 * this.K;
//        this.currentRowIndex = 0;
//        this.beginDate = "";
//        this.endDate = "";
//        this.connType = "";
//        this.querytime = "";
//        this.logContent = "";
//        this.user = "";
//        this.host = "";
//        this.row = "";
//        this.db = "";
//        this.isFiltering = false;
//        this.filterConditionSaved = true;
//        this.isLoaded = false;
//        this.RefreshAction = new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                GBase8aAuditLogEditor.this.isLoaded = false;
//                GBase8aAuditLogEditor.this.clearFilterCondition();
//                GBase8aAuditLogEditor.this.showFilterCondition();
//                QueryAuditLogProgress progress = new QueryAuditLogProgress(GBase8aAuditLogEditor.this, true);
//                ProgressMonitorDialog pg = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
//
//                try {
//                    pg.run(true, true, progress);
//                } catch (InvocationTargetException var4) {
//                } catch (InterruptedException var5) {
//                }
//
//                GBase8aAuditLogEditor.this.loadJTableData();
//                GBase8aAuditLogEditor.this.setPageInfo();
//            }
//        };
//    }
//
//    public void refreshPart(Object source, boolean force) {
//    }
//
//    public void open() {
//        this.loadJTableData();
//        this.setPageInfo();
//    }
//
//    public void createPartControl(Composite parent) {
//        this.dataSource = (GBase8aDataSource)this.getExecutionContext().getDataSource();
//        DBCSession dbsession = DBUtils.openMetaSession(this.dataSource.getMonitor(), this.dataSource, "");
//        JDBCConnectionImpl jdbcci = (JDBCConnectionImpl)dbsession;
//
//        try {
//            AuditLogDataManager.setConnection(jdbcci.getOriginal());
//        } catch (SQLException var45) {
//            SQLException e2 = var45;
//            e2.printStackTrace();
//        }
//
//        AuditLogDataManager.setHostName(this.dataSource.getContainer().getConnectionConfiguration().getHostName());
//        this.container = new Composite(parent, 0);
//        this.container.setLayout(new GridLayout(1, false));
//        ToolBar toolBar = new ToolBar(this.container, 8519680);
//        GridData gridData_4 = new GridData(4, 16777216, false, false);
//        gridData_4.widthHint = 240;
//        toolBar.setLayoutData(gridData_4);
//        this.exportLogToolItem = new ToolItem(toolBar, 8);
//        this.exportLogToolItem.setImage(Activator.getImageDescriptor("icons/Export.png").createImage());
//        this.exportLogToolItem.setToolTipText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_export);
//        this.exportLogToolItem.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_export);
//        this.exportLogToolItem.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                if (GBase8aAuditLogEditor.this.isLoaded) {
//                    GBase8aAuditLogEditor.this.exportLogFromFile();
//                } else {
//                    GBase8aAuditLogEditor.this.exportLogFromDB();
//                }
//
//            }
//        });
//        ToolItem refreshToolItem = new ToolItem(toolBar, 8);
//        refreshToolItem.setImage(Activator.getImageDescriptor("icons/Refresh.png").createImage());
//        refreshToolItem.setToolTipText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_refresh);
//        refreshToolItem.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_refresh);
//        refreshToolItem.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                if (GBase8aAuditLogEditor.this.isLoaded) {
//                    GBase8aAuditLogEditor.this.refreshFromFile();
//                } else {
//                    GBase8aAuditLogEditor.this.refreshFromDB();
//                }
//
//            }
//        });
//        ToolItem filtrateToolItem = new ToolItem(toolBar, 8);
//        filtrateToolItem.setImage(Activator.getImageDescriptor("icons/Filter.png").createImage());
//        filtrateToolItem.setToolTipText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_filter);
//        filtrateToolItem.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_filter);
//        filtrateToolItem.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                if (GBase8aAuditLogEditor.this.isLoaded) {
//                    GBase8aAuditLogEditor.this.filterLogFromFile();
//                } else {
//                    GBase8aAuditLogEditor.this.filterLogFromDB();
//                }
//
//            }
//        });
//        ToolItem findToolItem = new ToolItem(toolBar, 8);
//        findToolItem.setImage(Activator.getImageDescriptor("icons/Search.png").createImage());
//        findToolItem.setToolTipText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_search);
//        findToolItem.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_search);
//        findToolItem.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                FindDlg fid = new FindDlg(Display.getDefault().getActiveShell(), GBase8aAuditLogEditor.this);
//                fid.open();
//            }
//        });
//        this.conditionLabel = new Label(this.container, 0);
//        GridData gridData_3 = new GridData(256);
//        gridData_3.horizontalIndent = 5;
//        this.conditionLabel.setLayoutData(gridData_3);
//        this.conditionLabel.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_filter_none);
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
//                TableItem item = GBase8aAuditLogEditor.this.table.getItem(GBase8aAuditLogEditor.this.table.getSelectionIndex());
//                GBase8aAuditLogEditor.this.dateLabel.setText(item.getText(0).trim());
//                GBase8aAuditLogEditor.this.userLabel.setText(item.getText(1).trim());
//                GBase8aAuditLogEditor.this.hostLabel.setText(item.getText(2).trim());
//                GBase8aAuditLogEditor.this.queryTimeLabel.setText(item.getText(3).trim());
//                GBase8aAuditLogEditor.this.rowsLabel.setText(item.getText(4).trim());
//                GBase8aAuditLogEditor.this.dbLabel.setText(item.getText(5).trim());
//                GBase8aAuditLogEditor.this.typeLabel.setText(item.getText(6).trim());
//                GBase8aAuditLogEditor.this.contentLabel.setText(item.getText(8).trim());
//                GBase8aAuditLogEditor.this.currentRowIndex = GBase8aAuditLogEditor.this.table.getSelectionIndex();
//            }
//
//            public void widgetDefaultSelected(SelectionEvent e) {
//            }
//        });
//        final TableColumn dateColumn = new TableColumn(this.table, 0);
//        dateColumn.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_time);
//        dateColumn.setAlignment(16777216);
//        final TableColumn userColumn = new TableColumn(this.table, 0);
//        userColumn.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_loginuser);
//        final TableColumn hostColumn = new TableColumn(this.table, 0);
//        hostColumn.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_userip);
//        final TableColumn queryTimeColumn = new TableColumn(this.table, 0);
//        queryTimeColumn.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_querytime);
//        final TableColumn rowsColumn = new TableColumn(this.table, 0);
//        rowsColumn.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_rows);
//        final TableColumn dbColumn = new TableColumn(this.table, 0);
//        dbColumn.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_database);
//        final TableColumn typeColumn = new TableColumn(this.table, 0);
//        typeColumn.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_contype);
//        final TableColumn statementColumn = new TableColumn(this.table, 0);
//        statementColumn.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_contents);
//        TableColumn statementColumnNotDisplay = new TableColumn(this.table, 0);
//        statementColumnNotDisplay.setWidth(0);
//        statementColumnNotDisplay.setResizable(false);
//        rowsColumn.pack();
//        dbColumn.pack();
//        queryTimeColumn.pack();
//        typeColumn.pack();
//        composite_2.addControlListener(new ControlAdapter() {
//            public void controlResized(ControlEvent e) {
//                Rectangle area = composite_2.getClientArea();
//                Point preferredSize = GBase8aAuditLogEditor.this.table.computeSize(-1, -1);
//                int width = area.width - 2 * GBase8aAuditLogEditor.this.table.getBorderWidth();
//                Point oldSize;
//                if (preferredSize.y > area.height + GBase8aAuditLogEditor.this.table.getHeaderHeight()) {
//                    oldSize = GBase8aAuditLogEditor.this.table.getVerticalBar().getSize();
//                    width -= oldSize.x;
//                }
//
//                oldSize = GBase8aAuditLogEditor.this.table.getSize();
//                if (oldSize.x > area.width) {
//                    dateColumn.pack();
//                    userColumn.pack();
//                    hostColumn.pack();
//                    statementColumn.setWidth(width - (dateColumn.getWidth() + userColumn.getWidth() + hostColumn.getWidth() + queryTimeColumn.getWidth() + rowsColumn.getWidth() + dbColumn.getWidth() + typeColumn.getWidth()));
//                    GBase8aAuditLogEditor.this.table.setSize(area.width, area.height);
//                } else {
//                    GBase8aAuditLogEditor.this.table.setSize(area.width, area.height);
//                    dateColumn.pack();
//                    userColumn.pack();
//                    hostColumn.pack();
//                    statementColumn.setWidth(width - (dateColumn.getWidth() + userColumn.getWidth() + hostColumn.getWidth() + queryTimeColumn.getWidth() + rowsColumn.getWidth() + dbColumn.getWidth() + typeColumn.getWidth()));
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
//        gridData_1.heightHint = 320;
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
//                GBase8aAuditLogEditor.this.currentPage = 1;
//                if (GBase8aAuditLogEditor.this.isLoaded) {
//                    GBase8aAuditLogEditor.this.currentFilePointer = GBase8aAuditLogEditor.this.sumOfBytes;
//                    GBase8aAuditLogEditor.this.loadJTableData(new File(GBase8aAuditLogEditor.this.currentCommonLogFileName));
//                    GBase8aAuditLogEditor.this.setPageInfo();
//                } else {
//                    AuditLogDataManager.setLimitNum(GBase8aAuditLogEditor.this.currentPage);
//                    QueryAuditLogProgress progress = new QueryAuditLogProgress(GBase8aAuditLogEditor.this, false);
//                    ProgressMonitorDialog pg = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
//
//                    try {
//                        pg.run(true, false, progress);
//                    } catch (InvocationTargetException var4) {
//                    } catch (InterruptedException var5) {
//                    }
//
//                    GBase8aAuditLogEditor.this.loadJTableData();
//                    GBase8aAuditLogEditor.this.setPageInfo();
//                }
//
//            }
//        });
//        this.preToolItem = new ToolItem(toolBar_1, 8);
//        this.preToolItem.setImage(Activator.getImageDescriptor("icons/pre.png").createImage());
//        this.preToolItem.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                GBase8aAuditLogEditor var10000 = GBase8aAuditLogEditor.this;
//                var10000.currentPage = var10000.currentPage - 1;
//                if (GBase8aAuditLogEditor.this.isLoaded) {
//                    GBase8aAuditLogEditor.this.currentFilePointer = GBase8aAuditLogEditor.this.sumOfBytes - (long)((GBase8aAuditLogEditor.this.currentPage - 1) * GBase8aAuditLogEditor.this.STEPF);
//                    GBase8aAuditLogEditor.this.loadJTableData(new File(GBase8aAuditLogEditor.this.currentCommonLogFileName));
//                    GBase8aAuditLogEditor.this.setPageInfo();
//                } else {
//                    AuditLogDataManager.setLimitNum(GBase8aAuditLogEditor.this.currentPage);
//                    QueryAuditLogProgress progress = new QueryAuditLogProgress(GBase8aAuditLogEditor.this, false);
//                    ProgressMonitorDialog pg = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
//
//                    try {
//                        pg.run(true, false, progress);
//                    } catch (InvocationTargetException var4) {
//                    } catch (InterruptedException var5) {
//                    }
//
//                    GBase8aAuditLogEditor.this.loadJTableData();
//                    GBase8aAuditLogEditor.this.setPageInfo();
//                }
//
//            }
//        });
//        this.pageNumText = new Text(this.pageControl, 16777216);
//        GridData gd = new GridData();
//        gd.widthHint = 90;
//        this.pageNumText.setLayoutData(gd);
//        this.pageNumText.addMouseListener(new MouseAdapter() {
//            public void mouseDown(MouseEvent e) {
//                GBase8aAuditLogEditor.this.pageNumText.selectAll();
//            }
//        });
//        this.pageNumText.setBackground(SWTResourceManager.getColor(255, 255, 255));
//        this.pageNumText.addKeyListener(new KeyListener() {
//            public void keyPressed(KeyEvent e) {
//                if (e.character == '\r') {
//                    boolean result = true;
//                    int page = 1;
//
//                    try {
//                        page = Integer.parseInt(GBase8aAuditLogEditor.this.pageNumText.getText().trim());
//                        if (page > GBase8aAuditLogEditor.this.sumOfPages) {
//                            page = GBase8aAuditLogEditor.this.sumOfPages;
//                        }
//
//                        if (page <= 0) {
//                            page = 1;
//                        }
//                    } catch (Exception var8) {
//                        result = false;
//                    }
//
//                    if (!result) {
//                        GBase8aAuditLogEditor.this.pageNumText.setText(Integer.toString(GBase8aAuditLogEditor.this.currentPage));
//                    } else {
//                        if (page == GBase8aAuditLogEditor.this.currentPage) {
//                            GBase8aAuditLogEditor.this.setPageInfo();
//                            GBase8aAuditLogEditor.this.table.setFocus();
//                            return;
//                        }
//
//                        GBase8aAuditLogEditor.this.currentPage = page;
//                        if (GBase8aAuditLogEditor.this.isLoaded) {
//                            GBase8aAuditLogEditor.this.loadJTableData();
//                            GBase8aAuditLogEditor.this.setPageInfo();
//                        } else {
//                            AuditLogDataManager.setLimitNum(GBase8aAuditLogEditor.this.currentPage);
//                            QueryAuditLogProgress progress = new QueryAuditLogProgress(GBase8aAuditLogEditor.this, false);
//                            ProgressMonitorDialog pg = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
//
//                            try {
//                                pg.run(true, false, progress);
//                            } catch (InvocationTargetException var6) {
//                            } catch (InterruptedException var7) {
//                            }
//
//                            GBase8aAuditLogEditor.this.loadJTableData();
//                            GBase8aAuditLogEditor.this.setPageInfo();
//                        }
//                    }
//
//                    GBase8aAuditLogEditor.this.table.setFocus();
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
//                GBase8aAuditLogEditor var10000 = GBase8aAuditLogEditor.this;
//                var10000.currentPage = var10000.currentPage + 1;
//                if (GBase8aAuditLogEditor.this.isLoaded) {
//                    GBase8aAuditLogEditor.this.currentFilePointer = GBase8aAuditLogEditor.this.sumOfBytes - (long)((GBase8aAuditLogEditor.this.currentPage - 1) * GBase8aAuditLogEditor.this.STEPF);
//                    GBase8aAuditLogEditor.this.loadJTableData(new File(GBase8aAuditLogEditor.this.currentCommonLogFileName));
//                    GBase8aAuditLogEditor.this.setPageInfo();
//                } else {
//                    AuditLogDataManager.setLimitNum(GBase8aAuditLogEditor.this.currentPage);
//                    QueryAuditLogProgress progress = new QueryAuditLogProgress(GBase8aAuditLogEditor.this, false);
//                    ProgressMonitorDialog pg = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
//
//                    try {
//                        pg.run(true, false, progress);
//                    } catch (InvocationTargetException var4) {
//                    } catch (InterruptedException var5) {
//                    }
//
//                    GBase8aAuditLogEditor.this.loadJTableData();
//                    GBase8aAuditLogEditor.this.setPageInfo();
//                }
//
//            }
//        });
//        this.lastToolItem = new ToolItem(toolBar_2, 8);
//        this.lastToolItem.setImage(Activator.getImageDescriptor("icons/last.png").createImage());
//        this.lastToolItem.addSelectionListener(new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                GBase8aAuditLogEditor.this.currentPage = GBase8aAuditLogEditor.this.sumOfPages;
//                if (GBase8aAuditLogEditor.this.isLoaded) {
//                    GBase8aAuditLogEditor.this.currentFilePointer = GBase8aAuditLogEditor.this.sumOfBytes - (long)((GBase8aAuditLogEditor.this.sumOfPages - 1) * GBase8aAuditLogEditor.this.STEPF);
//                    GBase8aAuditLogEditor.this.loadJTableData(new File(GBase8aAuditLogEditor.this.currentCommonLogFileName));
//                    GBase8aAuditLogEditor.this.setPageInfo();
//                } else {
//                    AuditLogDataManager.setLimitNum(GBase8aAuditLogEditor.this.currentPage);
//                    QueryAuditLogProgress progress = new QueryAuditLogProgress(GBase8aAuditLogEditor.this, false);
//                    ProgressMonitorDialog pg = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
//
//                    try {
//                        pg.run(true, false, progress);
//                    } catch (InvocationTargetException var4) {
//                    } catch (InterruptedException var5) {
//                    }
//
//                    GBase8aAuditLogEditor.this.loadJTableData();
//                    GBase8aAuditLogEditor.this.setPageInfo();
//                }
//
//            }
//        });
//        Label label_1 = new Label(composite, 0);
//        label_1.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_details);
//        new Label(composite, 0);
//        new Label(composite, 0);
//        new Label(composite, 0);
//        Label label_2 = new Label(composite, 0);
//        label_2.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_time);
//        this.dateLabel = new Label(composite, 0);
//        this.dateLabel.setLayoutData(new GridData(4, 16777216, true, false));
//        this.dateLabel.setText("");
//        Label label_user = new Label(composite, 0);
//        label_user.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_loginuser);
//        this.userLabel = new Label(composite, 0);
//        this.userLabel.setLayoutData(new GridData(4, 16777216, true, false));
//        this.userLabel.setText("");
//        Label label_host = new Label(composite, 0);
//        label_host.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_userip);
//        this.hostLabel = new Label(composite, 0);
//        this.hostLabel.setLayoutData(new GridData(4, 16777216, true, false));
//        this.hostLabel.setText("");
//        Label label_querytime = new Label(composite, 0);
//        label_querytime.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_querytime);
//        this.queryTimeLabel = new Label(composite, 0);
//        this.queryTimeLabel.setLayoutData(new GridData(4, 16777216, true, false));
//        this.queryTimeLabel.setText("");
//        Label label_rows = new Label(composite, 0);
//        label_rows.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_rows);
//        this.rowsLabel = new Label(composite, 0);
//        this.rowsLabel.setLayoutData(new GridData(4, 16777216, true, false));
//        this.rowsLabel.setText("");
//        Label label_db = new Label(composite, 0);
//        label_db.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_database);
//        this.dbLabel = new Label(composite, 0);
//        this.dbLabel.setLayoutData(new GridData(4, 16777216, true, false));
//        this.dbLabel.setText("");
//        Label label = new Label(composite, 0);
//        label.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_contype);
//        this.typeLabel = new Label(composite, 0);
//        this.typeLabel.setLayoutData(new GridData(4, 16777216, true, false));
//        this.typeLabel.setText("");
//        new Label(composite, 0);
//        new Label(composite, 0);
//        Label label_6 = new Label(composite, 0);
//        label_6.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_contents);
//        new Label(composite, 0);
//        this.contentLabel = new Text(composite, 2626);
//        GridData mulTextData = new GridData(4, 16777216, true, false, 2, 1);
//        if ((new Properties(System.getProperties())).getProperty("os.name").toLowerCase().indexOf("windows") < 0) {
//            mulTextData.heightHint = 40;
//        } else {
//            mulTextData.heightHint = 40;
//        }
//
//        this.contentLabel.setLayoutData(mulTextData);
//        this.contentLabel.setEditable(false);
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
//        this.logFileLabel.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_host + AuditLogDataManager.getHostName());
//        this.initInfo();
//
//        try {
//            this.createTableDataFromDB();
//        } catch (Exception var44) {
//            Exception e1 = var44;
//            e1.printStackTrace();
//        }
//
//        this.open();
//    }
//
//    public void initInfo() {
//        if (this.isLoaded) {
//            this.sumOfRows = (long)tableDatas.size();
//        } else {
//            String condition = AuditLogDataManager.getCondition();
//
//            try {
//                this.sumOfRows = AuditLogDataManager.getTableCount(condition);
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
//        AuditLogDataManager.setLimitNum(this.currentPage);
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
//            MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "����", "��־��ʽ����ȷ���\u07b7�������־��");
//        }
//
//        this.table.setSelection(0);
//        if (this.table.getItemCount() > 0) {
//            TableItem item = this.table.getItem(0);
//            this.dateLabel.setText(item.getText(0).trim());
//            this.userLabel.setText(item.getText(1).trim());
//            this.hostLabel.setText(item.getText(2).trim());
//            this.queryTimeLabel.setText(item.getText(3).trim());
//            this.rowsLabel.setText(item.getText(4).trim());
//            this.dbLabel.setText(item.getText(5).trim());
//            this.typeLabel.setText(item.getText(6).trim());
//            this.contentLabel.setText(item.getText(8).trim());
//        }
//
//        boolean hasData = this.table.getItemCount() > 0;
//        this.dateLabel.setText(hasData ? this.table.getItem(0).getText(0) : "");
//        this.userLabel.setText(hasData ? this.table.getItem(0).getText(1) : "");
//        this.hostLabel.setText(hasData ? this.table.getItem(0).getText(2) : "");
//        this.queryTimeLabel.setText(hasData ? this.table.getItem(0).getText(3) : "");
//        this.rowsLabel.setText(hasData ? this.table.getItem(0).getText(4) : "");
//        this.dbLabel.setText(hasData ? this.table.getItem(0).getText(5) : "");
//        this.typeLabel.setText(hasData ? this.table.getItem(0).getText(6) : "");
//        this.contentLabel.setText(hasData ? this.table.getItem(0).getText(8) : "");
//        this.setPageInfo();
//        this.table.redraw();
//    }
//
//    public void createTableDataFromDB() throws Exception {
//        String condition = AuditLogDataManager.getCondition();
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
//            AuditLogDataManager.getLimitedLogData(condition);
//        } catch (Exception var20) {
//            Exception e = var20;
//            throw e;
//        }
//
//        List dateList = AuditLogDataManager.getLimitedTimeDatas();
//        List queryTimeList = AuditLogDataManager.getLimitedQueryTimes();
//        List typeList = AuditLogDataManager.getLimitedConnType();
//        List sqlList = AuditLogDataManager.getLimitedLogContent();
//        List userList = AuditLogDataManager.getLimitedUser();
//        List hostList = AuditLogDataManager.getLimitedHost();
//        List rowList = AuditLogDataManager.getLimitedRows();
//        List dbList = AuditLogDataManager.getLimitedDbs();
//        tableDatas = new ArrayList();
//
//        for(int i = 0; i < dateList.size(); ++i) {
//            List<String> tempList = new ArrayList(this.NUMBER_OF_COLUMNS);
//            String dateString = dateList.get(i).toString().trim().substring(0, 19);
//            String queryTimeString = queryTimeList.get(i).toString().trim();
//            String typeString = typeList.get(i).toString().trim();
//            String sqlString = sqlList.get(i).toString().trim();
//            String userString = userList.get(i).toString().trim();
//            String hostString = hostList.get(i).toString().trim();
//            String rowString = rowList.get(i).toString().trim();
//            String dbString = dbList.get(i).toString().trim();
//            tempList.add(0, dateString);
//            tempList.add(1, userString);
//            tempList.add(2, hostString);
//            tempList.add(3, queryTimeString);
//            tempList.add(4, rowString);
//            tempList.add(5, dbString);
//            tempList.add(6, this.convertTypeOfLog(typeString));
//            tempList.add(7, sqlString);
//            tableDatas.add(tempList);
//        }
//
//        AuditLogDataManager.clearLimitedLogData();
//    }
//
//    private String convertTypeOfLog(String source) {
//        String target = "";
//        Map<String, String> typeOfLog = new HashMap();
//        typeOfLog.put("Connect", "����");
//        typeOfLog.put("Query", "��ѯ");
//        typeOfLog.put("Prepare", "\u05fc��");
//        typeOfLog.put("Execute", "ִ��");
//        typeOfLog.put("Quit", "�˳�");
//        if (typeOfLog.get(source) != null) {
//            target = ((String)typeOfLog.get(source)).toString();
//        } else {
//            target = source;
//        }
//
//        return target;
//    }
//
//    private boolean checkDate(String yymmdd) {
//        if (this.beginDate.equals("") && this.endDate.equals("")) {
//            return true;
//        } else {
//            String yyyymmddFormat = null;
//            if (yymmdd.length() == 19) {
//                yyyymmddFormat = yymmdd;
//            } else {
//                yyyymmddFormat = "20" + yymmdd.substring(0, 2) + "-" + yymmdd.substring(2, 4) + "-" + yymmdd.substring(4, 6);
//            }
//
//            Date d1 = Date.valueOf(yyyymmddFormat.substring(0, 10));
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
//    private boolean checkUser(String text) {
//        boolean result = false;
//        if (this.user.equals("") || text.indexOf(this.user) >= 0) {
//            result = true;
//        }
//
//        return result;
//    }
//
//    private boolean checkHost(String text) {
//        boolean result = false;
//        if (this.host.equals("") || text.indexOf(this.host) >= 0) {
//            result = true;
//        }
//
//        return result;
//    }
//
//    private boolean checkQuerytime(String querytime) {
//        boolean result = false;
//
//        try {
//            if (this.querytime.equals("") || querytime.indexOf(this.querytime) >= 0) {
//                result = true;
//            }
//
//            return result;
//        } catch (Exception var3) {
//            return false;
//        }
//    }
//
//    private boolean checkRows(String rows) {
//        boolean result = false;
//
//        try {
//            if (this.row.equals("") || rows.indexOf(this.row) >= 0) {
//                result = true;
//            }
//
//            return result;
//        } catch (Exception var3) {
//            return false;
//        }
//    }
//
//    private boolean checkDb(String db) {
//        boolean result = false;
//
//        try {
//            if (this.db.equals("") || db.indexOf(this.db) >= 0) {
//                result = true;
//            }
//
//            return result;
//        } catch (Exception var3) {
//            return false;
//        }
//    }
//
//    private boolean checkConnType(String type) {
//        boolean result = false;
//        if (this.connType.equals("") || this.convertTypeOfLog(type).indexOf(this.connType) >= 0) {
//            result = true;
//        }
//
//        return result;
//    }
//
//    private boolean checkLogContent(String text) {
//        boolean result = false;
//        if (this.logContent.equals("") || text.indexOf(this.logContent) >= 0) {
//            result = true;
//        }
//
//        return result;
//    }
//
//    public void exportLog(File file) throws Exception {
//        String condition = AuditLogDataManager.getCondition();
//        long pageSize = 10000L;
//        long count = 0L;
//
//        try {
//            count = AuditLogDataManager.getTableCount(condition);
//        } catch (Exception var24) {
//            Exception e = var24;
//            throw e;
//        }
//
//        if (count < pageSize) {
//            count = pageSize;
//        } else {
//            long m = count % pageSize;
//            if (m > 0L) {
//                count += pageSize - m;
//            }
//        }
//
//        OutputStreamWriter fileWriter = null;
//        BufferedWriter bufferedWriter = null;
//
//        try {
//            Exception limitStr;
//            try {
//                try {
//                    fileWriter = new OutputStreamWriter(new FileOutputStream(file));
//                    bufferedWriter = new BufferedWriter(fileWriter);
//                } catch (FileNotFoundException var23) {
//                    FileNotFoundException e = var23;
//                    throw e;
//                }
//
//                limitStr = null;
//
//                for(long i = 0L; i < count; i += pageSize) {
//                    String limitStr = " limit " + i + "," + pageSize;
//
//                    try {
//                        AuditLogDataManager.getLogData(condition, limitStr, bufferedWriter);
//                    } catch (Exception var22) {
//                        Exception e = var22;
//                        throw e;
//                    }
//                }
//            } catch (Exception var25) {
//                limitStr = var25;
//                throw limitStr;
//            }
//        } finally {
//            if (fileWriter != null) {
//                try {
//                    fileWriter.close();
//                } catch (IOException var21) {
//                }
//            }
//
//        }
//
//    }
//
//    private void initFileInfo() {
//        this.currentFilePointer = (new File(this.currentCommonLogFileName)).length();
//        this.sumOfBytes = (new File(this.currentCommonLogFileName)).length();
//        this.sumOfPages = (int)(this.currentFilePointer / (long)this.STEPF) + 1;
//        this.currentPage = 1;
//    }
//
//    private void loadJTableData(File file) {
//        this.table.deselectAll();
//        this.createTableData(file);
//        boolean hasData = this.table.getItemCount() > 0;
//        this.dateLabel.setText(hasData ? this.table.getItem(0).getText(0) : "");
//        this.userLabel.setText(hasData ? this.table.getItem(0).getText(1) : "");
//        this.hostLabel.setText(hasData ? this.table.getItem(0).getText(2) : "");
//        this.queryTimeLabel.setText(hasData ? this.table.getItem(0).getText(3) : "");
//        this.rowsLabel.setText(hasData ? this.table.getItem(0).getText(4) : "");
//        this.dbLabel.setText(hasData ? this.table.getItem(0).getText(5) : "");
//        this.typeLabel.setText(hasData ? this.table.getItem(0).getText(6) : "");
//        this.contentLabel.setText(hasData ? this.table.getItem(0).getText(7) : "");
//        this.setPageInfo();
//        this.table.redraw();
//    }
//
//    public void createTableData(File f) {
//        ArrayList tableData = null;
//        ArrayList commonLogFileData = null;
//        int numberOfRows = 0;
//
//        try {
//            numberOfRows = this.getCommonLogFileData(f).size();
//            commonLogFileData = this.getCommonLogFileData(f);
//        } catch (IOException var11) {
//        }
//
//        tableData = new ArrayList(numberOfRows);
//        this.table.clearAll();
//        this.table.removeAll();
//        int i = 0;
//
//        for(int j = numberOfRows - 1; j >= 0; --j) {
//            tableData.add(commonLogFileData.get(j));
//            TableItem item = new TableItem(this.table, 0, i);
//            item.setImage(Activator.getImageDescriptor("icons/rq.png").createImage());
//            ArrayList rowData = (ArrayList)commonLogFileData.get(j);
//            String[] text = new String[this.NUMBER_OF_COLUMNS];
//
//            for(int ii = 0; ii < this.NUMBER_OF_COLUMNS; ++ii) {
//                text[ii] = rowData.get(ii).toString();
//                item.setText(ii, text[ii]);
//            }
//
//            ++i;
//        }
//
//        this.table.setSelection(0);
//        if (this.table.getItemCount() > 0) {
//            TableItem item = this.table.getItem(0);
//            this.dateLabel.setText(item.getText(0).trim());
//            this.userLabel.setText(item.getText(1).trim());
//            this.hostLabel.setText(item.getText(2).trim());
//            this.queryTimeLabel.setText(item.getText(3).trim());
//            this.rowsLabel.setText(item.getText(4).trim());
//            this.dbLabel.setText(item.getText(5).trim());
//            this.typeLabel.setText(item.getText(6).trim());
//            this.contentLabel.setText(item.getText(7).trim());
//        }
//
//    }
//
//    private void setLocationFileInfo() {
//        File f = new File(this.currentCommonLogFileName);
//        if (f.exists()) {
//            this.logFileLabel.setText("�����־�ļ���" + this.currentCommonLogFileName);
//        } else {
//            this.logFileLabel.setText("�����־�ļ�" + this.currentCommonLogFileName + "�����ڣ�");
//        }
//
//    }
//
//    private ArrayList createRow(ArrayList a) {
//        ArrayList result = new ArrayList(this.NUMBER_OF_COLUMNS);
//        result.add(a.get(0));
//        result.add(a.get(1));
//        result.add(a.get(2));
//        result.add(a.get(3));
//        result.add(a.get(4));
//        result.add(a.get(5));
//        result.add(a.get(6));
//        result.add(a.get(7));
//        return result;
//    }
//
//    private ArrayList getCommonLogFileData(File file) throws IOException {
//        ArrayList data = new ArrayList();
//        ArrayList result = new ArrayList();
//        File commonLogFile;
//        if (file == null) {
//            commonLogFile = new File(Server.getCommonLogFileName());
//        } else {
//            commonLogFile = file;
//        }
//
//        this.setLocationFileInfo();
//        this.currentCommonLogFileName = commonLogFile.toString();
//        if (commonLogFile.exists()) {
//            RandomAccessFile br = new RandomAccessFile(commonLogFile, "r");
//            String dateString = "";
//            String userString = "";
//            String hostString = "";
//            String querytime = "";
//            String rows = "";
//            String db = "";
//            String typeString = "";
//            String sqlString = "";
//            String tempString = "";
//            long tempFilePointer = 0L;
//            tempFilePointer = this.currentFilePointer - (long)this.STEPF < 0L ? 0L : this.currentFilePointer - (long)this.STEPF;
//            br.seek(tempFilePointer);
//
//            String line;
//            for(line = br.readLine(); line != null && !this.isDateStarted(line); line = br.readLine()) {
//            }
//
//            while(line != null && tempFilePointer < this.currentFilePointer) {
//                try {
//                    while(line != null && !line.endsWith("started with:") && !line.startsWith("Tcp port:") && tempFilePointer < this.currentFilePointer) {
//                        if (line.startsWith("Time")) {
//                            line = br.readLine();
//                            tempFilePointer = br.getFilePointer();
//                        }
//
//                        result = new ArrayList(this.NUMBER_OF_COLUMNS);
//                        StringTokenizer tokenizer = new StringTokenizer(line);
//                        if (this.isDateStarted(line)) {
//                            dateString = "";
//                            tempString = tokenizer.nextToken();
//                            dateString = dateString + tempString;
//                            tempString = tokenizer.nextToken();
//                            dateString = dateString + " " + tempString;
//                        }
//
//                        userString = "";
//                        if (tokenizer.hasMoreTokens()) {
//                            userString = tokenizer.nextToken();
//                        }
//
//                        hostString = "";
//                        if (tokenizer.hasMoreTokens()) {
//                            hostString = tokenizer.nextToken();
//                        }
//
//                        querytime = "";
//                        if (tokenizer.hasMoreTokens()) {
//                            querytime = tokenizer.nextToken();
//                        }
//
//                        rows = "";
//                        if (tokenizer.hasMoreTokens()) {
//                            rows = tokenizer.nextToken();
//                        }
//
//                        db = "";
//                        if (tokenizer.hasMoreTokens()) {
//                            db = tokenizer.nextToken();
//                        }
//
//                        typeString = "";
//                        if (tokenizer.hasMoreTokens()) {
//                            typeString = tokenizer.nextToken();
//                        }
//
//                        int index = line.indexOf(typeString);
//                        sqlString = line.substring(index + typeString.length()).trim();
//                        result.add(0, dateString);
//                        result.add(1, userString);
//                        result.add(2, hostString);
//                        result.add(3, querytime);
//                        result.add(4, rows);
//                        result.add(5, db);
//                        result.add(6, typeString);
//                        result.add(7, sqlString);
//                        if (this.checkDate(result.get(0).toString().trim()) && this.checkUser(result.get(1).toString().trim()) && this.checkHost(result.get(2).toString().trim()) && this.checkQuerytime(result.get(3).toString().trim()) && this.checkRows(result.get(4).toString().trim()) && this.checkDb(result.get(5).toString().trim()) && this.checkConnType(result.get(6).toString().trim()) && this.checkLogContent(result.get(7).toString().trim())) {
//                            data.add(this.createRow(result));
//                        }
//
//                        for(int i = result.size() - 1; i > 0; --i) {
//                            result.remove(i);
//                        }
//
//                        if (line == null) {
//                            break;
//                        }
//
//                        line = br.readLine();
//                        tempFilePointer = br.getFilePointer();
//                    }
//
//                    if (line == null) {
//                        break;
//                    }
//
//                    line = br.readLine();
//                    tempFilePointer = br.getFilePointer();
//                } catch (Exception var21) {
//                    Exception e = var21;
//                    e.printStackTrace();
//                }
//            }
//
//            result.clear();
//            br.close();
//        }
//
//        return data;
//    }
//
//    private void exportLogFromFile() {
//        FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), 8192);
//        dialog.setFilterNames(new String[]{"*.log", "*.err"});
//        dialog.setFilterExtensions(new String[]{"*.log", "*.err", "*.*"});
//        dialog.setFilterPath(System.getProperty("user.dir"));
//        String filePath = dialog.open();
//        if (filePath != null) {
//            try {
//                BufferedReader in = new BufferedReader(new FileReader(this.exportFileName));
//                File file = new File(filePath);
//                if (file.exists()) {
//                    file.delete();
//                }
//
//                OutputStreamWriter fileWriter = null;
//
//                try {
//                    fileWriter = new OutputStreamWriter(new FileOutputStream(file));
//                } catch (FileNotFoundException var20) {
//                }
//
//                String date = "";
//                String user = "";
//                String host = "";
//                String querytime = "";
//                String rows = "";
//                String db = "";
//                String connType = "";
//                String logContent = "";
//                String tempBlock = "";
//
//                String s;
//                for(s = in.readLine(); s != null && !this.isDateStarted(s); s = in.readLine()) {
//                }
//
//                while(true) {
//                    while(s != null) {
//                        if (!s.endsWith("started with:") && !s.startsWith("Tcp port:")) {
//                            try {
//                                if (s.startsWith("Time")) {
//                                    tempBlock = s + "\n";
//                                    fileWriter.write(tempBlock);
//                                } else {
//                                    StringTokenizer tokenizer = new StringTokenizer(s);
//                                    if (this.isDateStarted(s)) {
//                                        date = "";
//                                        date = date + tokenizer.nextToken();
//                                        date = date + tokenizer.nextToken();
//                                    }
//
//                                    user = tokenizer.nextToken();
//                                    host = tokenizer.nextToken();
//                                    querytime = tokenizer.nextToken();
//                                    rows = tokenizer.nextToken();
//                                    db = tokenizer.nextToken();
//                                    connType = tokenizer.nextToken();
//                                    int index = s.indexOf(connType);
//                                    logContent = s.substring(index + connType.length()).trim();
//                                    if (this.isExportChecked(date, user, host, querytime, rows, db, connType, logContent)) {
//                                        tempBlock = tempBlock + s + "\n";
//                                        fileWriter.write(tempBlock);
//                                    }
//                                }
//                            } catch (Exception var19) {
//                            }
//
//                            tempBlock = "";
//                            s = in.readLine();
//                        } else {
//                            s = in.readLine();
//                        }
//                    }
//
//                    try {
//                        fileWriter.close();
//                    } catch (IOException var18) {
//                    }
//                    break;
//                }
//            } catch (IOException var21) {
//                MessageDialog.openError((Shell)null, "����", "���ļ��Ĺ���з������");
//            }
//        }
//
//    }
//
//    private void exportLogFromDB() {
//        FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), 8192);
//        dialog.setFilterNames(new String[]{"*.log", "*.err"});
//        dialog.setFilterExtensions(new String[]{"*.log", "*.err", "*.*"});
//        dialog.setFilterPath(System.getProperty("user.dir"));
//        String filePath = dialog.open();
//        if (filePath != null) {
//            File file = new File(filePath);
//            if (file.exists()) {
//                boolean config = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "��ʾ", filePath + " �Ѿ����ڣ�\n�Ƿ��滻��");
//                if (!config) {
//                    return;
//                }
//
//                file.delete();
//                ExportAuditLogProgress progress = new ExportAuditLogProgress(this, file);
//                ProgressMonitorDialog pg = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
//
//                try {
//                    pg.run(true, true, progress);
//                } catch (InvocationTargetException var9) {
//                } catch (InterruptedException var10) {
//                }
//
//                if (progress.getErrorMessage() != null) {
//                    MessageDialog.openError(Display.getCurrent().getActiveShell(), "����", "������־���ִ���");
//                }
//            } else {
//                ExportAuditLogProgress progress = new ExportAuditLogProgress(this, file);
//                ProgressMonitorDialog pg = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
//
//                try {
//                    pg.run(true, true, progress);
//                } catch (InvocationTargetException var7) {
//                } catch (InterruptedException var8) {
//                }
//
//                if (progress.getErrorMessage() != null) {
//                    MessageDialog.openError(Display.getCurrent().getActiveShell(), "����", "������־���ִ���");
//                }
//            }
//        }
//
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
//    public boolean isExportChecked(String date, String user, String host, String querytime, String rows, String db, String connType, String content) {
//        if (!this.checkDate(date)) {
//            return false;
//        } else if (!this.checkUser(user)) {
//            return false;
//        } else if (!this.checkHost(host)) {
//            return false;
//        } else if (!this.checkQuerytime(querytime)) {
//            return false;
//        } else if (!this.checkRows(rows)) {
//            return false;
//        } else if (!this.checkDb(db)) {
//            return false;
//        } else if (!this.checkConnType(connType)) {
//            return false;
//        } else {
//            return this.checkLogContent(content);
//        }
//    }
//
//    private void refreshFromDB() {
//        this.clearFilterCondition();
//        this.showFilterCondition();
//        QueryAuditLogProgress progress = new QueryAuditLogProgress(this, true);
//        ProgressMonitorDialog pg = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
//
//        try {
//            pg.run(true, true, progress);
//        } catch (InvocationTargetException var3) {
//        } catch (InterruptedException var4) {
//        }
//
//        this.loadJTableData();
//        this.setPageInfo();
//    }
//
//    private void refreshFromFile() {
//        this.clearFilterCondition();
//        this.showFilterCondition();
//        this.initFileInfo();
//        this.loadJTableData(new File(this.currentCommonLogFileName));
//    }
//
//    private void filterLogFromFile() {
//        this.clearFilterCondition();
//        AuditLogFilterDlg f = new AuditLogFilterDlg(Display.getDefault().getActiveShell(), this);
//        if (f.open() == 0) {
//            this.isFiltering = true;
//            this.beginDate = f.getBeginDate();
//            this.endDate = f.getEndDate();
//            this.user = f.getUser();
//            this.host = f.getHost();
//            this.querytime = f.getQuerytime();
//            this.connType = f.getConnType();
//            this.row = f.getRows();
//            this.db = f.getDb();
//            this.logContent = f.getLogContent();
//            this.showFilterCondition();
//            this.loadJTableData(new File(this.currentCommonLogFileName));
//            if (!f.getSave()) {
//                this.filterConditionSaved = false;
//            } else {
//                this.filterConditionSaved = true;
//            }
//
//            this.setPageInfo();
//        }
//
//    }
//
//    private void filterLogFromDB() {
//        this.clearFilterCondition();
//        AuditLogFilterDlg f = new AuditLogFilterDlg(Display.getDefault().getActiveShell(), this);
//        if (f.open() == 0) {
//            this.isFiltering = true;
//            if (AuditLogDataManager.getBeginDate().length() > 0) {
//                try {
//                    this.beginDate = AuditLogDataManager.getBeginDate();
//                } catch (Exception var7) {
//                }
//            } else {
//                this.beginDate = "";
//            }
//
//            if (AuditLogDataManager.getEndDate().length() > 0) {
//                try {
//                    this.endDate = AuditLogDataManager.getEndDate().split("\\s")[0];
//                } catch (Exception var6) {
//                }
//            } else {
//                this.endDate = "";
//            }
//
//            this.user = AuditLogDataManager.getUser();
//            this.host = AuditLogDataManager.getHost();
//            this.querytime = AuditLogDataManager.getQueryTime();
//            this.row = AuditLogDataManager.getRow();
//            this.db = AuditLogDataManager.getDb();
//            this.connType = AuditLogDataManager.getConnType();
//            this.logContent = AuditLogDataManager.getLogContent();
//            this.showFilterCondition();
//            QueryAuditLogProgress progress = new QueryAuditLogProgress(this, true);
//            ProgressMonitorDialog pg = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
//
//            try {
//                pg.run(true, true, progress);
//            } catch (InvocationTargetException var4) {
//            } catch (InterruptedException var5) {
//            }
//
//            this.loadJTableData();
//            this.setPageInfo();
//            if (!f.getSave()) {
//                this.filterConditionSaved = false;
//            } else {
//                this.filterConditionSaved = true;
//            }
//        }
//
//    }
//
//    private void clearFilterCondition() {
//        if (!this.filterConditionSaved) {
//            this.connType = "";
//            this.beginDate = "";
//            this.endDate = "";
//            this.logContent = "";
//            this.querytime = "";
//            this.user = "";
//            this.host = "";
//            this.row = "";
//            this.db = "";
//            AuditLogDataManager.setConnType("");
//            AuditLogDataManager.setBeginDate("");
//            AuditLogDataManager.setEndDate("");
//            AuditLogDataManager.setLogContent("");
//            AuditLogDataManager.setQueryTime("");
//            AuditLogDataManager.setUser("");
//            AuditLogDataManager.setHost("");
//            AuditLogDataManager.setRow("");
//            AuditLogDataManager.setDb("");
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
//        if (!this.user.equals("")) {
//            if (this.user.length() > 8) {
//                temp = this.user.substring(0, 6) + "��";
//            } else {
//                temp = this.user;
//            }
//
//            s = s + GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_loginuser + "'" + temp + "'��";
//            fs = s;
//        }
//
//        if (!this.host.equals("")) {
//            if (this.host.length() > 8) {
//                temp = this.host.substring(0, 6) + "��";
//            } else {
//                temp = this.host;
//            }
//
//            s = s + GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_userip + "'" + temp + "'��";
//            fs = fs + GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_userip + "'" + this.host + "'��";
//        }
//
//        if (!this.querytime.equals("")) {
//            String qTime = this.querytime;
//            s = s + GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_querytime + "'" + qTime + "'��";
//            fs = fs + GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_querytime + "'" + qTime + "'��";
//        }
//
//        if (!this.row.equals("")) {
//            temp = this.row;
//            s = s + GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_rows + "'" + temp + "'��";
//            fs = fs + GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_rows + "'" + temp + "'��";
//        }
//
//        if (!this.db.equals("")) {
//            temp = this.db;
//            s = s + GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_database + "'" + temp + "'��";
//            fs = fs + GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_database + "'" + temp + "'��";
//        }
//
//        if (!this.connType.equals("")) {
//            if (this.connType.length() > 8) {
//                temp = this.connType.substring(0, 6) + "��";
//            } else {
//                temp = this.connType;
//            }
//
//            s = s + GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_contype + "'" + temp + "'��";
//            fs = fs + GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_table_contype + "'" + this.connType + "'��";
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
//        if (s.length() > 50) {
//            s = s.substring(0, s.lastIndexOf("��")) + "��";
//        }
//
//        if (s.equals("")) {
//            this.conditionLabel.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_filter_none);
//        } else {
//            this.conditionLabel.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_filter_condition + (s.length() > 72 ? s.substring(0, 70) + "..." : s.substring(0, s.length() - 1) + "��"));
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
//    public void search(FindDlg jDialog) {
//        int i = false;
//        boolean condition = true;
//        String searchWord = jDialog.getLogContent();
//        String logContent = "";
//        int rowCount = this.table.getItemCount();
//        int i;
//        if (jDialog.isDownDirect()) {
//            if (this.currentRowIndex >= rowCount) {
//                MessageDialog.openInformation((Shell)null, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_find_message, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_find_none);
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
//                MessageDialog.openInformation((Shell)null, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_find_message, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_find_none);
//                return;
//            }
//        }
//
//        if (!jDialog.isDownDirect()) {
//            if (this.currentRowIndex <= -1) {
//                MessageDialog.openInformation((Shell)null, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_find_message, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_find_none);
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
//                MessageDialog.openInformation((Shell)null, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_find_message, GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_find_none);
//                return;
//            }
//        }
//
//    }
//
//    public String getQuerytime() {
//        return this.querytime;
//    }
//
//    public String getEventType() {
//        return this.connType;
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
//    public String getUser() {
//        return this.user;
//    }
//
//    public String getHost() {
//        return this.host;
//    }
//
//    public String getRow() {
//        return this.row;
//    }
//
//    public String getDb() {
//        return this.db;
//    }
//
//    public void setFocus() {
//        this.container.setFocus();
//    }
//}
