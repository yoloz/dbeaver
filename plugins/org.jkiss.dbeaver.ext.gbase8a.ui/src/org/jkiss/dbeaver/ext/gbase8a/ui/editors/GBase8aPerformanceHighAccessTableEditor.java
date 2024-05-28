//package org.jkiss.dbeaver.ext.gbase8a.ui.editors;
//
//import org.jkiss.dbeaver.ext.gbase8a.Activator;
//import org.jkiss.dbeaver.ext.gbase8a.GBase8aMessages;
//import org.jkiss.dbeaver.ext.gbase8a.log.analysislog.AnalysisTableManager;
//import org.jkiss.dbeaver.ext.gbase8a.log.analysislog.LogAnalysis;
//import org.jkiss.dbeaver.ext.gbase8a.log.analysislog.QueryItemManager;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
//import org.jkiss.dbeaver.model.DBUtils;
//import org.jkiss.dbeaver.model.exec.DBCSession;
//import org.jkiss.dbeaver.model.impl.jdbc.exec.JDBCConnectionImpl;
//import org.jkiss.dbeaver.ui.editors.IDatabaseEditorInput;
//import org.jkiss.dbeaver.ui.editors.SinglePageDatabaseEditor;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Iterator;
//import java.util.Map;
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
//import org.eclipse.swt.graphics.Point;
//import org.eclipse.swt.graphics.Rectangle;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Event;
//import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.Listener;
//import org.eclipse.swt.widgets.Table;
//import org.eclipse.swt.widgets.TableColumn;
//import org.eclipse.swt.widgets.TableItem;
//import org.eclipse.swt.widgets.ToolBar;
//import org.eclipse.swt.widgets.ToolItem;
//
//public class GBase8aPerformanceHighAccessTableEditor extends SinglePageDatabaseEditor<IDatabaseEditorInput> {
//  private GBase8aDataSource dataSource;
//  private Composite container;
//  private ToolItem exportItem = null;
//  private ToolItem statisticItem = null;
//  private Table table;
//  private CDateTime cdtStartTime;
//  private CDateTime cdtEndTime;
//  private Connection conn;
//  private QueryItemManager qim;
//  private Map<String, AnalysisTableManager> map_table;
//  private int topUser = 0;
//
//  public GBase8aPerformanceHighAccessTableEditor() {
//  }
//
//  public Connection getConn() {
//    return this.conn;
//  }
//
//  public void setConn(Connection conn) {
//    this.conn = conn;
//  }
//
//  public Map<String, AnalysisTableManager> getMap_table() {
//    return this.map_table;
//  }
//
//  public void setMap_table(Map<String, AnalysisTableManager> map_table) {
//    this.map_table = map_table;
//  }
//
//  public void refreshPart(Object source, boolean force) {
//  }
//
//  public void createPartControl(Composite parent) {
//    this.dataSource = (GBase8aDataSource)this.getExecutionContext().getDataSource();
//    DBCSession dbsession = DBUtils.openMetaSession(this.dataSource.getMonitor(), this.dataSource, "");
//    JDBCConnectionImpl jdbcci = (JDBCConnectionImpl)dbsession;
//
//    try {
//      this.conn = jdbcci.getOriginal();
//    } catch (SQLException var29) {
//      SQLException e2 = var29;
//      e2.printStackTrace();
//    }
//
//    this.container = new Composite(parent, 0);
//    this.container.setLayout(new GridLayout(1, false));
//    ToolBar toolBar = new ToolBar(this.container, 8519680);
//    GridData gridData_4 = new GridData(4, 16777216, false, false);
//    gridData_4.widthHint = 240;
//    toolBar.setLayoutData(gridData_4);
//    this.statisticItem = new ToolItem(toolBar, 8);
//    this.statisticItem.setImage(Activator.getImageDescriptor("icons/Search.png").createImage());
//    this.statisticItem.setToolTipText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_search);
//    this.statisticItem.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_search);
//    this.statisticItem.addSelectionListener(new SelectionAdapter() {
//      public void widgetSelected(SelectionEvent e) {
//        LogAnalysis.setConnection(GBase8aPerformanceHighAccessTableEditor.this.conn);
//        GBase8aPerformanceHighAccessTableEditor.this.qim = new QueryItemManager();
//        GBase8aPerformanceHighAccessTableEditor.this.qim.setStartTime(GBase8aPerformanceHighAccessTableEditor.this.cdtStartTime.getText());
//        GBase8aPerformanceHighAccessTableEditor.this.qim.setEndTime(GBase8aPerformanceHighAccessTableEditor.this.cdtEndTime.getText());
//        if (LogAnalysis.checkedDate(Display.getCurrent().getActiveShell(), GBase8aPerformanceHighAccessTableEditor.this.qim)) {
//          OpenSessionTrackLogProgress progress = GBase8aPerformanceHighAccessTableEditor.this.new OpenSessionTrackLogProgress();
//          ProgressMonitorDialog pg = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
//
//          try {
//            pg.run(true, false, progress);
//          } catch (Exception var5) {
//            Exception e1 = var5;
//            MessageDialog.openError(Display.getCurrent().getActiveShell(), GBase8aMessages.message_error_title, GBase8aMessages.editors_stastics_error);
//            e1.printStackTrace();
//            return;
//          }
//
//          if (progress.getErrorMessage() != null) {
//            MessageDialog.openError(Display.getCurrent().getActiveShell(), GBase8aMessages.message_error_title, GBase8aMessages.editors_access_log_table_error);
//            return;
//          }
//
//          GBase8aPerformanceHighAccessTableEditor.this.refreshTable(GBase8aPerformanceHighAccessTableEditor.this.getMap_table());
//        }
//
//      }
//    });
//    this.exportItem = new ToolItem(toolBar, 8);
//    this.exportItem.setImage(Activator.getImageDescriptor("icons/Export.png").createImage());
//    this.exportItem.setToolTipText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_export);
//    this.exportItem.setText(GBase8aMessages.cn_gbase_studio_ext_gbase8a_log_audilog_export);
//    this.exportItem.addSelectionListener(new SelectionAdapter() {
//      public void widgetSelected(SelectionEvent e) {
//        LogAnalysis.export(Display.getCurrent().getActiveShell(), GBase8aPerformanceHighAccessTableEditor.this.table.getColumns(), GBase8aPerformanceHighAccessTableEditor.this.table.getItems());
//      }
//    });
//    Composite composite_2 = new Composite(this.container, 0);
//    GridLayout gridLayout_2 = new GridLayout();
//    gridLayout_2.numColumns = 4;
//    composite_2.setLayout(gridLayout_2);
//    Label lblStartTime = new Label(composite_2, 0);
//    GridData gd_lblStartTime = new GridData(4, 16777216, false, false, 1, 1);
//    gd_lblStartTime.widthHint = 200;
//    lblStartTime.setLayoutData(gd_lblStartTime);
//    lblStartTime.setText(GBase8aMessages.editors_performance_start_time);
//    this.cdtStartTime = new CDateTime(composite_2, 12582914);
//    GridData gd_cdtStartTime = new GridData(4, 16777216, false, false, 1, 1);
//    gd_cdtStartTime.widthHint = 407;
//    this.cdtStartTime.setLayoutData(gd_cdtStartTime);
//    this.cdtStartTime.setPattern("yyyy-MM-dd HH:mm:ss");
//    Label lblEndTime = new Label(composite_2, 0);
//    GridData gd_lblEndTime = new GridData(4, 16777216, false, false, 1, 1);
//    gd_lblEndTime.widthHint = 225;
//    lblEndTime.setLayoutData(gd_lblEndTime);
//    lblEndTime.setText(GBase8aMessages.editors_performance_end_time);
//    this.cdtEndTime = new CDateTime(composite_2, 12582914);
//    GridData gd_cdtEndTime = new GridData(4, 16777216, false, false, 1, 1);
//    gd_cdtEndTime.widthHint = 402;
//    this.cdtEndTime.setLayoutData(gd_cdtEndTime);
//    this.cdtEndTime.setPattern("yyyy-MM-dd HH:mm:ss");
//    Date date = new Date();
//    Calendar calendar = Calendar.getInstance();
//    calendar.setTime(date);
//    calendar.add(5, -7);
//    this.cdtStartTime.setSelection(calendar.getTime());
//    this.cdtEndTime.setSelection(date);
//    Composite composite = new Composite(this.container, 2048);
//    GridData gd_composite = new GridData(4, 4, false, false, 1, 1);
//    composite.setLayoutData(gd_composite);
//    GridLayout gl_composite = new GridLayout(1, true);
//    composite.setLayout(gl_composite);
//    TableViewer tv = new TableViewer(composite, 68352);
//    this.table = tv.getTable();
//    GridData gd_table_1 = new GridData(4, 4, true, false, 1, 2);
//    gd_table_1.heightHint = 535;
//    gd_table_1.widthHint = 660;
//    this.table.setLayoutData(gd_table_1);
//    this.table.setHeaderVisible(true);
//    this.table.setLinesVisible(true);
//    this.table.addMouseTrackListener(new MouseTrackListener() {
//      public void mouseHover(MouseEvent e) {
//        TableItem[] items = GBase8aPerformanceHighAccessTableEditor.this.table.getItems();
//        Point pt = new Point(e.x, e.y);
//        StringBuffer sb = new StringBuffer();
//
//        for(int i = 0; i < items.length; ++i) {
//          for(int j = 0; j < GBase8aPerformanceHighAccessTableEditor.this.table.getColumnCount(); ++j) {
//            Rectangle rect = items[i].getBounds(j);
//            if (rect.contains(pt)) {
//              String itemtext = items[i].getText(j);
//              if (itemtext.length() > 100) {
//                int n;
//                for(n = 0; n < itemtext.length() - 100; n += 100) {
//                  sb.append(itemtext.substring(n, n + 100));
//                  sb.append("\n");
//                }
//
//                sb.append(itemtext.substring(n));
//              } else {
//                sb.append(itemtext);
//              }
//
//              GBase8aPerformanceHighAccessTableEditor.this.table.setToolTipText(sb.toString());
//            }
//          }
//        }
//
//      }
//
//      public void mouseExit(MouseEvent e) {
//      }
//
//      public void mouseEnter(MouseEvent e) {
//      }
//    });
//    Listener paintListener = new Listener() {
//      public void handleEvent(Event event) {
//        TableItem item;
//        String text;
//        Point size;
//        switch (event.type) {
//          case 40:
//            event.detail &= -17;
//            break;
//          case 41:
//            item = (TableItem)event.item;
//            text = this.getText(item, event.index);
//            size = event.gc.textExtent(text);
//            event.width = size.x;
//            event.height = Math.max(event.height, size.y);
//            break;
//          case 42:
//            item = (TableItem)event.item;
//            text = this.getText(item, event.index);
//            size = event.gc.textExtent(text);
//            int offset2 = event.index == 0 ? Math.max(0, (event.height - size.y) / 2) : 0;
//            event.gc.drawText(text, event.x, event.y + offset2, true);
//        }
//
//      }
//
//      String getText(TableItem item, int column) {
//        return item.getText(column);
//      }
//    };
//    this.table.addListener(41, paintListener);
//    this.table.addListener(42, paintListener);
//    this.table.addListener(40, paintListener);
//    TableColumn tblclmnId = new TableColumn(this.table, 0);
//    tblclmnId.setResizable(false);
//    tblclmnId.setText("id");
//    TableColumn tableColumn = new TableColumn(this.table, 0);
//    tableColumn.setWidth(100);
//    tableColumn.setText(GBase8aMessages.editors_performance_max_resulst_seq);
//    TableColumn tblclmnSql = new TableColumn(this.table, 0);
//    tblclmnSql.setWidth(123);
//    tblclmnSql.setText(GBase8aMessages.editors_performance_table_name);
//    TableColumn tableColumn_4 = new TableColumn(this.table, 0);
//    tableColumn_4.setWidth(144);
//    tableColumn_4.setText(GBase8aMessages.editors_performance_table_database);
//    TableColumn tableColumn_3 = new TableColumn(this.table, 0);
//    tableColumn_3.setWidth(124);
//    tableColumn_3.setText(GBase8aMessages.editors_performance_table_request_time);
//    TableColumn tableColumn_1 = new TableColumn(this.table, 0);
//    tableColumn_1.setWidth(88);
//    tableColumn_1.setText(GBase8aMessages.editors_performance_table_visit_user);
//    TableColumn tblclmnip = new TableColumn(this.table, 0);
//    tblclmnip.setWidth(108);
//    tblclmnip.setText(GBase8aMessages.editors_performance_table_visit_request);
//    new Label(composite, 0);
//    new Label(composite, 0);
//  }
//
//  private void refreshTable(Map<String, AnalysisTableManager> map) {
//    TableItem[] var5;
//    int var4 = (var5 = this.table.getItems()).length;
//
//    for(int var3 = 0; var3 < var4; ++var3) {
//      TableItem item = var5[var3];
//      item.dispose();
//    }
//
//    this.table.clearAll();
//    this.topUser = 0;
//    if (map != null && !map.isEmpty()) {
//      int i = 1;
//
//      for(Iterator var14 = map.entrySet().iterator(); var14.hasNext(); ++i) {
//        Map.Entry<String, AnalysisTableManager> entry = (Map.Entry)var14.next();
//        AnalysisTableManager atm = (AnalysisTableManager)entry.getValue();
//        TableItem item = new TableItem(this.table, 0);
//        item.setText(1, Integer.toString(i));
//        if (atm.getTableName() == null) {
//          item.setText(2, "");
//        } else {
//          item.setText(2, atm.getTableName());
//        }
//
//        if (atm.getDbName() == null) {
//          item.setText(3, "");
//        } else {
//          item.setText(3, atm.getDbName());
//        }
//
//        item.setText(4, Long.toString(atm.getAccessTimes()));
//        Map<String, String> accessUser = atm.getAccessUser();
//        StringBuffer name = new StringBuffer();
//        StringBuffer per = new StringBuffer();
//        this.topUser = 0;
//        Iterator var11 = accessUser.keySet().iterator();
//
//        String nameStr;
//        while(var11.hasNext()) {
//          nameStr = (String)var11.next();
//          if (this.topUser == 10) {
//            return;
//          }
//
//          ++this.topUser;
//          name.append(nameStr);
//          name.append("\n");
//          per.append((String)accessUser.get(nameStr));
//          per.append("%\n");
//        }
//
//        nameStr = name.toString();
//        if (nameStr.isEmpty()) {
//          item.setText(5, nameStr);
//        } else {
//          item.setText(5, nameStr.substring(0, nameStr.length() - 1));
//        }
//
//        String perStr = per.toString();
//        if (perStr.isEmpty()) {
//          item.setText(6, perStr);
//        } else {
//          item.setText(6, perStr.substring(0, perStr.length() - 1));
//        }
//      }
//
//      this.table.update();
//    }
//
//  }
//
//  public void setFocus() {
//    this.container.setFocus();
//  }
//
//  class OpenSessionTrackLogProgress implements IRunnableWithProgress {
//    private String errorMessage;
//
//    OpenSessionTrackLogProgress() {
//    }
//
//    public String getErrorMessage() {
//      return this.errorMessage;
//    }
//
//    public void run(IProgressMonitor monitor) {
//      monitor.setTaskName("User behaviour gather");
//      monitor.beginTask(GBase8aMessages.editors_performance_counting, -1);
//      ParseLogFilesThread parseLogFilesThread = GBase8aPerformanceHighAccessTableEditor.this.new ParseLogFilesThread();
//      parseLogFilesThread.start();
//
//      while(parseLogFilesThread.isAlive()) {
//        if (monitor.isCanceled()) {
//          parseLogFilesThread.interrupt();
//          break;
//        }
//
//        try {
//          Thread.sleep(100L);
//        } catch (InterruptedException var4) {
//          InterruptedException e = var4;
//          e.printStackTrace();
//        }
//      }
//
//      this.errorMessage = parseLogFilesThread.getErrorMessage();
//      monitor.done();
//    }
//  }
//
//  private class ParseLogFilesThread extends Thread {
//    private String errorMessage = null;
//
//    public ParseLogFilesThread() {
//      String threadName = "Thread [" + this.getId() + "]";
//      this.setName(threadName);
//    }
//
//    public void run() {
//      try {
//        GBase8aPerformanceHighAccessTableEditor.this.setMap_table(LogAnalysis.getPerformanceHighAccessTable(GBase8aPerformanceHighAccessTableEditor.this.qim));
//      } catch (SQLException var2) {
//        SQLException e = var2;
//        this.errorMessage = e.getMessage();
//        e.printStackTrace();
//      }
//
//    }
//
//    public String getErrorMessage() {
//      return this.errorMessage;
//    }
//  }
//}
