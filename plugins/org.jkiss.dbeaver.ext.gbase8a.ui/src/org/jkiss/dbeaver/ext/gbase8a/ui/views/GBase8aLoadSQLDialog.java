//package org.jkiss.dbeaver.ext.gbase8a.ui.views;
//
//import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
//import org.jkiss.dbeaver.model.DBUtils;
//import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
//import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
//import org.jkiss.dbeaver.ui.UIUtils;
//import org.jkiss.dbeaver.ui.dialogs.BaseDialog;
//
//import java.sql.SQLException;
//
//import org.eclipse.swt.events.ModifyEvent;
//import org.eclipse.swt.events.ModifyListener;
//import org.eclipse.swt.graphics.Point;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Group;
//import org.eclipse.swt.widgets.MessageBox;
//import org.eclipse.swt.widgets.Shell;
//import org.eclipse.swt.widgets.Text;
//
//
//public class GBase8aLoadSQLDialog extends BaseDialog {
//    private final GBase8aDataSource dataSource;
//    private String sql;
//    private Text resultText;
//    private final String catalogName;
//    private final String vcName;
//    private Button btnOk;
//
//    public GBase8aLoadSQLDialog(Shell parentShell, GBase8aDataSource dataSource, String vcName, String catalogName) {
//        super(parentShell, GBase8aMessages.dialog_load_sql_name, null);
//        this.dataSource = dataSource;
//        this.vcName = vcName;
//        this.catalogName = catalogName;
//    }
//
//
//    protected Composite createDialogArea(Composite parent) {
//        Composite composite = super.createDialogArea(parent);
//
//        Composite group = new Composite(composite, 0);
//        group.setLayout(new GridLayout(1, false));
//
//        Group sql_group = new Group(group, 0);
//        sql_group.setText(GBase8aMessages.dialog_load_sql_sqlgroup_text);
//        sql_group.setLayout(new GridLayout(1, false));
//
//        final Text nameText = new Text(sql_group, 2818);
//        nameText.addModifyListener(new ModifyListener() {
//            public void modifyText(ModifyEvent e) {
//                GBase8aLoadSQLDialog.this.sql = nameText.getText();
//                GBase8aLoadSQLDialog.this.getButton(0).setEnabled(!GBase8aLoadSQLDialog.this.sql.isEmpty());
//            }
//        });
//
//        GridData gd_nameText = new GridData(16384, 16777216, false, false, 1, 1);
//        gd_nameText.widthHint = 300;
//        gd_nameText.heightHint = 100;
//        nameText.setLayoutData(gd_nameText);
//
//        Group result_group = new Group(group, 0);
//        result_group.setText(GBase8aMessages.dialog_load_sql_resultgroup_text);
//        result_group.setLayout(new GridLayout(1, false));
//
//        this.resultText = new Text(result_group, 2);
//        this.resultText.setEnabled(false);
//        GridData gd_resultText = new GridData(16384, 16777216, false, false, 1, 1);
//        gd_resultText.widthHint = 322;
//        gd_resultText.heightHint = 100;
//        this.resultText.setLayoutData(gd_resultText);
//
//        return composite;
//    }
//
//    protected Point getInitialSize() {
//        return new Point(380, 400);
//    }
//
//
//    protected void createButtonsForButtonBar(Composite parent) {
//        this.btnOk = createButton(parent, 0, GBase8aMessages.dialog_load_ok_button, true);
//        this.btnOk.setEnabled(false);
//        createButton(parent, 1, GBase8aMessages.dialog_load_cancel_button, true);
//    }
//
//    protected void buttonPressed(int buttonId) {
//        if (buttonId == 0) {
//            if (!this.sql.isEmpty()) {
//
//                long taskId = -1L;
//                long skipCount = -1L;
//                long updateCount = -1L;
//                try(JDBCSession dbsession = DBUtils.openMetaSession(this.dataSource.getMonitor(), this.dataSource, "load sql");
//                    JDBCStatement stmt = dbsession.createStatement()) {
//                    if (this.dataSource.isVCCluster()) {
//                        stmt.execute("use vc " + this.vcName);
//                    }
//                    if (this.catalogName != null) {
//                        stmt.execute("use " + this.catalogName);
//                    }
//                    stmt.execute(this.sql);
//                    updateCount = stmt.getLongUpdateCount();
//                    taskId = stmt.getLoadTaskID();
//                    skipCount = stmt.getSkippedLines();
//                    skipCount = (skipCount < 0L) ? 0L : skipCount;
//
//                    String message = "Task ID : " + taskId +
//                            "\n" +
//                            GBase8aMessages.dialog_load_sql_update_rows +
//                            updateCount +
//                            "\n" +
//                            GBase8aMessages.dialog_load_sql_skip_rows +
//                            skipCount;
//                    this.resultText.setText(message);
//                } catch (Exception e) {
//                    MessageBox box = new MessageBox(UIUtils.getActiveWorkbenchShell(), 1);
//                    box.setMessage("Error");
//                    box.setMessage(e.getMessage());
//                    box.open();
//                } finally {
//                    try {
//                        String defaultdb = this.dataSource.getActiveVC().getDefaultObject().getName();
//                        stmt.execute("use " + defaultdb);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                    try {
//                        if (stmt != null) {
//                            stmt.close();
//                        }
//                        dbsession.close();
//                    } catch (SQLException sQLException) {
//                    }
//                }
//            }
//        }
//        if (buttonId == 1)
//            close();
//    }
//}