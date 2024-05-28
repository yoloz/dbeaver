package org.jkiss.dbeaver.ext.gbase8a.ui.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aConsumerGroup;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.ui.dialogs.BaseDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.graphics.Image;

public class GBase8aModifyConsumerGroupDialog extends BaseDialog {

    private static final Log log = Log.getLog(GBase8aModifyConsumerGroupDialog.class);
    private final GBase8aDataSource dataSource;
    private String name;
    private String comment;
    private final String vcName;
    private final String userNames;
    private final GBase8aConsumerGroup consumerGroup;
    private TableViewer noUsersAddedTableViewer;
    private TableViewer usersAddedTableViewer;
    private Table partitionsTable;
    private Table coalescentTable;
    private Button moveLeftButton;
    private Button moveRightButton;
    private final List<User> noUsersAddedList = new ArrayList<>();
    private final List<User> usersAddedList = new ArrayList<>();
    private final List<String> allUserList = new ArrayList<>();
    private final Map<String,String> oldUsersAddMap = new HashMap<>();
    private final Map<String,String> oldNoUsersAddedMap = new HashMap<>();
    private Text nameText;
    private Text commentText;

    public GBase8aModifyConsumerGroupDialog(Shell parentShell, GBase8aDataSource dataSource, GBase8aConsumerGroup group, String vcName) {
        super(parentShell, GBase8aMessages.dialog_modify_Consumer_dialog_name, null);
        this.dataSource = dataSource;
        this.vcName = vcName;
        this.consumerGroup = group;
        this.userNames = group.getUserNames();
        getAllUserList();
        initUsersInfor();
    }

    private void getAllUserList() {
        try (JDBCSession session = DBUtils.openMetaSession(this.dataSource.getMonitor(), this.dataSource, "query all users");
             JDBCStatement dbStat = session.createStatement();
             JDBCResultSet dbResult = dbStat.executeQuery("SELECT * FROM gbase.user ORDER BY user")) {
            while (dbResult.next()) {
                String userName = dbResult.getString("user").trim();
                this.allUserList.add(userName);
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    private void initUsersInfor() {
        String[] users = this.userNames.split(",");
        byte b;
        int j;
        String[] arrayOfString1;
        for (j = (arrayOfString1 = users).length, b = 0; b < j; ) {
            String userName = arrayOfString1[b];
            this.oldUsersAddMap.put(userName, userName);
            b++;
        }
        for (String userName : this.allUserList) {
            User user = new User();
            user.setUserName(userName.trim());
            if (this.oldUsersAddMap.containsKey(userName)) {
                this.usersAddedList.add(user);
            } else {
                this.noUsersAddedList.add(user);
                this.oldNoUsersAddedMap.put(userName, userName);
            }
        }
    }


    protected Composite createDialogArea(Composite parent) {
        Composite composite = super.createDialogArea(parent);
        Composite contener = new Composite(composite, 0);
        contener.setLayout(new GridLayout(2, false));
        Label vcNameLabel = new Label(contener, 0);
        vcNameLabel.setText(GBase8aMessages.dialog_connection_vc_name);

        Text vcNameText = new Text(contener, 0);
        GridData gd_vcNameText = new GridData(16384, 16777216, false, false, 1, 1);
        gd_vcNameText.widthHint = 160;
        vcNameText.setLayoutData(gd_vcNameText);
        vcNameText.setText(this.vcName);
        vcNameText.setEnabled(false);

        Label nameLabel = new Label(contener, 0);
        nameLabel.setText(GBase8aMessages.dialog_create_Consumer_Group_name);

        this.nameText = new Text(contener, 2048);
        GridData gd_nameText = new GridData(16384, 16777216, false, false, 1, 1);
        gd_nameText.widthHint = 160;
        this.nameText.setLayoutData(gd_nameText);
        this.nameText.setText(this.consumerGroup.getName());

        Label commentLabel = new Label(contener, 0);
        commentLabel.setText(GBase8aMessages.dialog_create_Consumer_Group_comment);

        this.commentText = new Text(contener, 2048);
        GridData gd_commentText = new GridData(16384, 16777216, false, false, 1, 1);
        gd_commentText.widthHint = 160;
        this.commentText.setLayoutData(gd_commentText);
        this.commentText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                GBase8aModifyConsumerGroupDialog.this.comment = GBase8aModifyConsumerGroupDialog.this.commentText.getText();
            }
        });

        if (this.consumerGroup.getComment() != null) {
            this.commentText.setText(this.consumerGroup.getComment());
        }

        Group group = new Group(contener, 0);
        GridData gd_userContener = new GridData(16384, 16777216, false, false, 2, 2);
        group.setLayoutData(gd_userContener);
        group.setBounds(10, 42, 635, 263);

        Label label = new Label(group, 0);
        label.setText(GBase8aMessages.dialog_modify_Consumer_dialog_no_users_added);
        label.setBounds(10, 24, 120, 15);

        this.noUsersAddedTableViewer = new TableViewer(group, 67586);
        this.noUsersAddedTableViewer.setLabelProvider(new PartitionsTableLabelProvider());

        this.noUsersAddedTableViewer.setContentProvider(new PartitionsContentProvider());
        this.coalescentTable = this.noUsersAddedTableViewer.getTable();
        this.coalescentTable.setHeaderVisible(true);
        this.coalescentTable.setBounds(10, 44, 200, 209);
        this.noUsersAddedTableViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent e) {
                GBase8aModifyConsumerGroupDialog.this.moveRight();
            }
        });

        TableColumn eNameColumn = new TableColumn(this.coalescentTable, 0);
        eNameColumn.setWidth(100);
        eNameColumn.setText(GBase8aMessages.dialog_connection_user_name);

        this.noUsersAddedTableViewer.setInput(this.noUsersAddedList);


        Label label_1 = new Label(group, 0);
        label_1.setBounds(300, 24, 120, 15);
        label_1.setText(GBase8aMessages.dialog_modify_Consumer_dialog_users_added);

        this.usersAddedTableViewer = new TableViewer(group, 67586);
        this.usersAddedTableViewer.setLabelProvider(new PartitionsTableLabelProvider());
        this.usersAddedTableViewer.setContentProvider(new PartitionsContentProvider());
        this.partitionsTable = this.usersAddedTableViewer.getTable();
        this.partitionsTable.setHeaderVisible(true);
        this.partitionsTable.setBounds(300, 44, 200, 209);
        this.usersAddedTableViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent e) {
                GBase8aModifyConsumerGroupDialog.this.moveLeft();
            }
        });
        TableColumn mNameColumn = new TableColumn(this.partitionsTable, 0);
        mNameColumn.setWidth(100);
        mNameColumn.setText(GBase8aMessages.dialog_connection_user_name);

        this.usersAddedTableViewer.setInput(this.usersAddedList);

        this.moveRightButton = new Button(group, 0);
        this.moveRightButton.setText(GBase8aMessages.dialog_modify_Consumer_dialog_right);
        this.moveRightButton.setBounds(224, 87, 65, 25);
        this.moveRightButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                GBase8aModifyConsumerGroupDialog.this.moveRight();
            }
        });

        this.moveLeftButton = new Button(group, 0);
        this.moveLeftButton.setBounds(224, 148, 65, 25);
        this.moveLeftButton.setText(GBase8aMessages.dialog_modify_Consumer_dialog_left);
        this.moveLeftButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                GBase8aModifyConsumerGroupDialog.this.moveLeft();
            }
        });

        return composite;
    }


    protected void buttonPressed(int buttonId) {
        if (buttonId == 0) {
            try (JDBCSession dbsession = DBUtils.openMetaSession(this.dataSource.getMonitor(), this.dataSource, "modify consumer group");
            JDBCStatement stmt = dbsession.createStatement()){
                this.name = this.nameText.getText().trim();
                if (!this.name.equals(this.consumerGroup.getName())) {
                    String alterNameSql = "alter consumer group ";
                    if (this.dataSource.isVCCluster()) {
                        alterNameSql = alterNameSql + this.vcName + ".";
                    }
                    alterNameSql = alterNameSql + this.consumerGroup.getName() + " rename to " + this.name;
                    stmt.execute(alterNameSql);
                }
                this.comment = this.commentText.getText().trim();
                if (!this.comment.isEmpty()) {
                    String alterCommentSQL = "alter consumer group ";
                    if (this.dataSource.isVCCluster()) {
                        alterCommentSQL = alterCommentSQL + this.vcName + ".";
                    }
                    alterCommentSQL = alterCommentSQL + this.name + " comment='" + this.comment + "'";
                    stmt.execute(alterCommentSQL);
                }
                int i, size;
                for (i = 0, size = this.usersAddedList.size(); i < size; i++) {
                    User user = this.usersAddedList.get(i);
                    if (!this.oldUsersAddMap.containsKey(user.getUserName())) {
                        String alterAddUserSQL = "alter consumer group ";
                        if (this.dataSource.isVCCluster()) {
                            alterAddUserSQL = alterAddUserSQL + this.vcName + ".";
                        }
                        alterAddUserSQL = alterAddUserSQL + this.name + " add user " + user.getUserName();
                        stmt.execute(alterAddUserSQL);
                    }
                }
                for (i = 0, size = this.noUsersAddedList.size(); i < size; i++) {
                    User user = this.noUsersAddedList.get(i);
                    if (!this.oldNoUsersAddedMap.containsKey(user.getUserName())) {
                        String alterRemoveUserSQL = "alter consumer group ";
                        if (this.dataSource.isVCCluster()) {
                            alterRemoveUserSQL = alterRemoveUserSQL + this.vcName + ".";
                        }
                        alterRemoveUserSQL = alterRemoveUserSQL + this.name + " remove user " + user.getUserName();
                        stmt.execute(alterRemoveUserSQL);
                    }
                }
                MessageDialog.openInformation(Display.getCurrent()
                        .getActiveShell(), GBase8aMessages.dialog_modify_Consumer_dialog_name, GBase8aMessages.dialog_modify_Consumer_dialog_success);

                close();
            } catch (Exception e) {
                MessageDialog.openError(Display.getCurrent().getActiveShell(),
                        GBase8aMessages.dialog_modify_Consumer_dialog_name, GBase8aMessages.dialog_modify_Consumer_dialog_failed + e.getMessage());
            }
        }
        super.buttonPressed(buttonId);
    }

    protected Point getInitialSize() {
        return new Point(550, 480);
    }

    public String getName() {
        return this.name;
    }

    public String getComment() {
        return this.comment;
    }


    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
    }

    private void moveRight() {
        IStructuredSelection sel = (IStructuredSelection) this.noUsersAddedTableViewer.getSelection();
        for (int i = 0; i < sel.size(); i++) {
            if (sel.toList().get(i) != null) {
                User vp = (User) sel.toList().get(i);
                this.noUsersAddedList.remove(vp);
                this.usersAddedList.add(vp);
                refreshUI();
                this.usersAddedTableViewer.setSelection(new StructuredSelection(vp));
                this.usersAddedTableViewer.getTable().setFocus();
            }
        }
    }

    private void moveLeft() {
        IStructuredSelection sel = (IStructuredSelection) this.usersAddedTableViewer.getSelection();
        for (int i = 0; i < sel.size(); i++) {
            if (sel.toList().get(i) != null) {
                User vp = (User) sel.toList().get(i);
                this.noUsersAddedList.add(vp);
                this.usersAddedList.remove(vp);
                refreshUI();
                this.noUsersAddedTableViewer.setSelection(new StructuredSelection(vp));
                this.noUsersAddedTableViewer.getTable().setFocus();
            }
        }
    }

    private void refreshUI() {
        this.noUsersAddedTableViewer.refresh();
        this.usersAddedTableViewer.refresh();
    }

    static class PartitionsTableLabelProvider extends LabelProvider implements ITableLabelProvider {
        public String getColumnText(Object element, int columnIndex) {
            User user = (User) element;
            if (columnIndex == 0) {
                return user.getUserName();
            }
            return "";
        }

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }

    static class PartitionsContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            return ((List<?>) inputElement).toArray();
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    static class User {
        private String userName;

        public String getUserName() {
            return this.userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }
}