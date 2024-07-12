//package org.jkiss.dbeaver.ext.gbase8a.ui.editors;
//
//import org.jkiss.dbeaver.ext.gbase8a.Activator;
//import org.jkiss.dbeaver.ext.gbase8a.GBase8aMessages;
//import org.jkiss.dbeaver.ext.gbase8a.log.analysislog.AnalysisSQLManager;
//import org.jkiss.dbeaver.ext.gbase8a.log.analysislog.LogAnalysis;
//import org.jkiss.dbeaver.ext.gbase8a.log.analysislog.QueryItemManager;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
//import org.jkiss.dbeaver.model.DBPDataSource;
//import org.jkiss.dbeaver.model.DBUtils;
//import org.jkiss.dbeaver.model.exec.DBCSession;
//import org.jkiss.dbeaver.model.impl.jdbc.exec.JDBCConnectionImpl;
//import org.jkiss.dbeaver.ui.editors.IDatabaseEditorInput;
//import org.jkiss.dbeaver.ui.editors.SinglePageDatabaseEditor;
//
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Map;
//
//import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.jface.dialogs.ProgressMonitorDialog;
//import org.eclipse.jface.operation.IRunnableWithProgress;
//import org.eclipse.jface.viewers.TableViewer;
//import org.eclipse.nebula.widgets.cdatetime.CDateTime;
//import org.eclipse.swt.events.MouseEvent;
//import org.eclipse.swt.events.MouseTrackListener;
//import org.eclipse.swt.events.SelectionAdapter;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.events.SelectionListener;
//import org.eclipse.swt.graphics.Point;
//import org.eclipse.swt.graphics.Rectangle;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.Layout;
//import org.eclipse.swt.widgets.Table;
//import org.eclipse.swt.widgets.TableColumn;
//import org.eclipse.swt.widgets.TableItem;
//import org.eclipse.swt.widgets.ToolBar;
//import org.eclipse.swt.widgets.ToolItem;
//
//
//public class GBase8aPerformanceMaxResultSQLEditor extends SinglePageDatabaseEditor<IDatabaseEditorInput> {
//    private GBase8aDataSource dataSource;
//    private Composite container;
//    private ToolItem exportItem = null;
//    private ToolItem statisticItem = null;
//    private Table table;
//    private CDateTime cdtStartTime;
//    private CDateTime cdtEndTime;
//    private Connection conn;
//    private QueryItemManager qim;
//    private Map<String, AnalysisSQLManager> map_sql;
//
//    public Connection getConn() {
//        return this.conn;
//    }
//
//    public void setConn(Connection conn) {
//        this.conn = conn;
//    }
//
//    public Map<String, AnalysisSQLManager> getMap_sql() {
//        return this.map_sql;
//    }
//
//    public void setMap_sql(Map<String, AnalysisSQLManager> map_sql) {
//        this.map_sql = map_sql;
//    }
//
//
//    public void refreshPart(Object source, boolean force) {
//    }
//
//
//    public void createPartControl(Composite parent) {
//        this.dataSource = (GBase8aDataSource) getExecutionContext().getDataSource();
//
//        DBCSession dbsession = DBUtils.openMetaSession(this.dataSource.getMonitor(), (DBPDataSource) this.dataSource, "");
//        JDBCConnectionImpl jdbcci = (JDBCConnectionImpl) dbsession;
//        try {
//            this.conn = jdbcci.getOriginal();
//        } catch (SQLException e2) {
//
//            e2.printStackTrace();
//        }
//
//        this.container = new Composite(parent, 0);
//        this.container.setLayout((Layout) new GridLayout(1, false));
//
//        ToolBar toolBar = new ToolBar(this.container, 8519680);
//        GridData gridData_4 = new GridData(4, 16777216, false, false);
//        gridData_4.widthHint = 240;
//        toolBar.setLayoutData(gridData_4);
//
//        this.statisticItem = new ToolItem(toolBar, 8);
//        this.statisticItem.setImage(Activator.getImageDescriptor("icons/Search.png").createImage());
//        this.statisticItem.setToolTipText(GBase8aMessages.gbase8a_log_audilog_search);
//        this.statisticItem.setText(GBase8aMessages.gbase8a_log_audilog_search);
//        this.statisticItem.addSelectionListener((SelectionListener) new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                LogAnalysis.setConnection(GBase8aPerformanceMaxResultSQLEditor.this.conn);
//                GBase8aPerformanceMaxResultSQLEditor.this.qim = new QueryItemManager();
//                GBase8aPerformanceMaxResultSQLEditor.this.qim.setStartTime(GBase8aPerformanceMaxResultSQLEditor.this.cdtStartTime.getText());
//                GBase8aPerformanceMaxResultSQLEditor.this.qim.setEndTime(GBase8aPerformanceMaxResultSQLEditor.this.cdtEndTime.getText());
//                if (LogAnalysis.checkedDate(Display.getCurrent().getActiveShell(), GBase8aPerformanceMaxResultSQLEditor.this.qim)) {
//                    OpenSessionTrackLogProgress progress = new OpenSessionTrackLogProgress();
//                    ProgressMonitorDialog pg = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
//                    try {
//                        pg.run(true, false, progress);
//                    } catch (Exception e1) {
//                        MessageDialog.openError(Display.getCurrent().getActiveShell(), GBase8aMessages.message_error_title, GBase8aMessages.editors_stastics_error);
//                        e1.printStackTrace();
//
//                        return;
//                    }
//                    if (progress.getErrorMessage() != null) {
//                        MessageDialog.openError(Display.getCurrent().getActiveShell(), GBase8aMessages.message_error_title, GBase8aMessages.editors_access_log_table_error);
//
//                        return;
//                    }
//                    GBase8aPerformanceMaxResultSQLEditor.this.refreshTable(GBase8aPerformanceMaxResultSQLEditor.this.getMap_sql());
//                }
//            }
//        });
//
//        this.exportItem = new ToolItem(toolBar, 8);
//        this.exportItem.setImage(Activator.getImageDescriptor("icons/Export.png").createImage());
//        this.exportItem.setToolTipText(GBase8aMessages.gbase8a_log_audilog_export);
//        this.exportItem.setText(GBase8aMessages.gbase8a_log_audilog_export);
//        this.exportItem.addSelectionListener((SelectionListener) new SelectionAdapter() {
//            public void widgetSelected(SelectionEvent e) {
//                LogAnalysis.export(Display.getCurrent().getActiveShell(), GBase8aPerformanceMaxResultSQLEditor.this.table.getColumns(), GBase8aPerformanceMaxResultSQLEditor.this.table.getItems());
//            }
//        });
//
//        Composite composite_2 = new Composite(this.container, 0);
//        GridLayout gridLayout_2 = new GridLayout();
//        gridLayout_2.numColumns = 4;
//        composite_2.setLayout((Layout) gridLayout_2);
//
//        Label lblStartTime = new Label(composite_2, 0);
//        GridData gd_lblStartTime = new GridData(4, 16777216, false, false, 1, 1);
//        gd_lblStartTime.widthHint = 200;
//        lblStartTime.setLayoutData(gd_lblStartTime);
//        lblStartTime.setText(GBase8aMessages.editors_performance_start_time);
//
//        this.cdtStartTime = new CDateTime(composite_2, 12582914);
//        GridData gd_cdtStartTime = new GridData(4, 16777216, false, false, 1, 1);
//        gd_cdtStartTime.widthHint = 425;
//        this.cdtStartTime.setLayoutData(gd_cdtStartTime);
//        this.cdtStartTime.setPattern("yyyy-MM-dd HH:mm:ss");
//
//
//        Label lblEndTime = new Label(composite_2, 0);
//        GridData gd_lblEndTime = new GridData(4, 16777216, false, false, 1, 1);
//        gd_lblEndTime.widthHint = 225;
//        lblEndTime.setLayoutData(gd_lblEndTime);
//        lblEndTime.setText(GBase8aMessages.editors_performance_end_time);
//
//        this.cdtEndTime = new CDateTime(composite_2, 12582914);
//        GridData gd_cdtEndTime = new GridData(4, 16777216, false, false, 1, 1);
//        gd_cdtEndTime.widthHint = 402;
//        this.cdtEndTime.setLayoutData(gd_cdtEndTime);
//        this.cdtEndTime.setPattern("yyyy-MM-dd HH:mm:ss");
//
//
//        Date date = new Date();
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        calendar.add(5, -7);
//        this.cdtStartTime.setSelection(calendar.getTime());
//        this.cdtEndTime.setSelection(date);
//
//
//        Composite composite = new Composite(this.container, 2048);
//        GridData gd_composite = new GridData(4, 4, false, false, 1, 1);
//        composite.setLayoutData(gd_composite);
//        GridLayout gl_composite = new GridLayout(1, true);
//        composite.setLayout((Layout) gl_composite);
//
//        TableViewer tv = new TableViewer(composite, 68352);
//        this.table = tv.getTable();
//
//        GridData gd_table_1 = new GridData(4, 4, true, false, 1, 2);
//        gd_table_1.heightHint = 535;
//        gd_table_1.widthHint = 600;
//
//        this.table.setLayoutData(gd_table_1);
//        this.table.setHeaderVisible(true);
//        this.table.setLinesVisible(true);
//
//
//        this.table.addMouseTrackListener(new MouseTrackListener() {
//
//            public void mouseHover(MouseEvent e) {
//                TableItem[] items = GBase8aPerformanceMaxResultSQLEditor.this.table.getItems();
//                Point pt = new Point(e.x, e.y);
//                StringBuffer sb = new StringBuffer();
//                for (int i = 0; i < items.length; i++) {
//                    for (int j = 0; j < GBase8aPerformanceMaxResultSQLEditor.this.table.getColumnCount(); j++) {
//                        Rectangle rect = items[i].getBounds(j);
//                        if (rect.contains(pt)) {
//                            String itemtext = items[i].getText(j);
//                            if (itemtext.length() > 100) {
//                                int n;
//                                for (n = 0; n < itemtext.length() - 100; n += 100) {
//                                    sb.append(itemtext.substring(n, n + 100));
//                                    sb.append("\n");
//                                }
//                                sb.append(itemtext.substring(n));
//                            } else {
//                                sb.append(itemtext);
//                            }
//                            GBase8aPerformanceMaxResultSQLEditor.this.table.setToolTipText(sb.toString());
//                        }
//                    }
//                }
//            }
//
//
//            public void mouseExit(MouseEvent e) {
//            }
//
//
//            public void mouseEnter(MouseEvent e) {
//            }
//        });
//        TableColumn tblclmnId = new TableColumn(this.table, 0);
//        tblclmnId.setResizable(false);
//        tblclmnId.setText("id");
//
//        TableColumn tableColumn = new TableColumn(this.table, 0);
//        tableColumn.setWidth(100);
//        tableColumn.setText(GBase8aMessages.editors_performance_max_resulst_seq);
//
//
//        TableColumn tblclmnSql = new TableColumn(this.table, 0);
//        tblclmnSql.setWidth(400);
//        tblclmnSql.setText(GBase8aMessages.editors_performance_max_resulst_sql);
//
//
//        TableColumn tableColumn_4 = new TableColumn(this.table, 0);
//        tableColumn_4.setWidth(150);
//        tableColumn_4.setText(GBase8aMessages.editors_performance_max_resulst_query_time);
//
//
//        TableColumn tableColumn_3 = new TableColumn(this.table, 0);
//        tableColumn_3.setWidth(150);
//        tableColumn_3.setText(GBase8aMessages.editors_performance_max_resulst_count);
//
//
//        TableColumn tableColumn_1 = new TableColumn(this.table, 0);
//        tableColumn_1.setWidth(100);
//        tableColumn_1.setText(GBase8aMessages.editors_performance_max_resulst_login_user);
//
//
//        TableColumn tblclmnip = new TableColumn(this.table, 0);
//        tblclmnip.setWidth(200);
//        tblclmnip.setText(GBase8aMessages.editors_performance_max_resulst_user_ip);
//    }
//
//
//    private void refreshTable(Map<String, AnalysisSQLManager> map) {
//        byte b;
//        int i;
//        TableItem[] arrayOfTableItem;
//        for (i = (arrayOfTableItem = this.table.getItems()).length, b = 0; b < i; ) {
//            TableItem item = arrayOfTableItem[b];
//            item.dispose();
//            b++;
//        }
//
//        this.table.clearAll();
//        if (map != null && !map.isEmpty()) {
//            int j = 1;
//            for (Map.Entry<String, AnalysisSQLManager> entry : map.entrySet()) {
//                AnalysisSQLManager asm = entry.getValue();
//                TableItem item = new TableItem(this.table, 0);
//                item.setText(1, Integer.toString(j));
//                if (asm.getSql() == null) {
//                    item.setText(2, "");
//                } else {
//                    item.setText(2, asm.getSql());
//                }
//                item.setText(3, Long.toString(asm.getQueryTime().longValue()));
//                item.setText(4, Long.toString(asm.getRows().longValue()));
//                if (asm.getUserName() == null) {
//                    item.setText(5, "");
//                } else {
//                    item.setText(5, asm.getUserName());
//                }
//                if (asm.getIp() == null) {
//                    item.setText(6, "");
//                } else {
//                    item.setText(6, asm.getIp());
//                }
//                j++;
//            }
//            this.table.update();
//        }
//    }
//
//
//    class OpenSessionTrackLogProgress
//            implements IRunnableWithProgress {
//        private String errorMessage;
//
//
//        public String getErrorMessage() {
//            return this.errorMessage;
//        }
//
//        public void run(IProgressMonitor monitor) {
//            monitor.setTaskName("User behaviour gather");
//            monitor.beginTask(GBase8aMessages.editors_performance_counting, -1);
//
//            ParseLogFilesThread parseLogFilesThread = new ParseLogFilesThread();
//            parseLogFilesThread.start();
//
//            while (parseLogFilesThread.isAlive()) {
//                if (monitor.isCanceled()) {
//                    parseLogFilesThread.interrupt();
//                    break;
//                }
//                try {
//                    Thread.sleep(100L);
//                } catch (InterruptedException e) {
//                    log.error(e);
//                }
//            }
//            this.errorMessage = parseLogFilesThread.getErrorMessage();
//            monitor.done();
//        }
//    }
//
//    private class ParseLogFilesThread extends Thread {
//        private String errorMessage = null;
//
//        public ParseLogFilesThread() {
//            String threadName = "Thread [" + getId() + "]";
//            setName(threadName);
//        }
//
//
//        public void run() {
//            try {
//                GBase8aPerformanceMaxResultSQLEditor.this.setMap_sql(LogAnalysis.getPerformanceMaxResultSQL(GBase8aPerformanceMaxResultSQLEditor.this.qim));
//            } catch (SQLException e) {
//
//                this.errorMessage = e.getMessage();
//                log.error(e);
//            }
//        }
//
//        public String getErrorMessage() {
//            return this.errorMessage;
//        }
//    }
//
//    public void setFocus() {
//        this.container.setFocus();
//    }
//}
//
//
///* Location:              /data/yolo/GBaseDataStudio_9.5.2.0_build21_Windows_x86_64/GBaseDataStudio/plugins/cn.gbase.studio.ext.gbase8a_1.0.0.jar!/cn/gbase/studio/ext/gbase8a/editors/GBase8aPerformanceMaxResultSQLEditor.class
// * Java compiler version: 6 (50.0)
// * JD-Core Version:       1.1.3
// */