package org.jkiss.dbeaver.ext.gbase8a.ui.editors;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.ui.controls.PrivilegeTableControl;
import org.jkiss.dbeaver.ext.gbase8a.edit.GBase8aCommandGrantPrivilege;
import org.jkiss.dbeaver.ext.gbase8a.edit.UserPropertyHandler;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aGrant;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aPrivilege;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aUser;
import org.jkiss.dbeaver.model.edit.DBECommandReflector;
import org.jkiss.dbeaver.model.impl.edit.DBECommandAdapter;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.load.DatabaseLoadService;
import org.jkiss.dbeaver.ui.LoadingJob;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.controls.ProgressPageControl;
import org.jkiss.dbeaver.ui.editors.ControlPropertyCommandListener;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;


public class GBase8aUserEditorGeneral extends GBase8aUserEditorAbstract {
    public static final String DEF_PASSWORD_VALUE = "**********";
    private PageControl pageControl;
    private boolean isLoaded;
    private PrivilegeTableControl privTable;
    private boolean newUser;
    private Text userNameText;
    private Text hostText;
    private CommandListener commandlistener;

    public GBase8aUserEditorGeneral() {
    }

    public void createPartControl(Composite parent) {
        this.pageControl = new PageControl(parent);
        Composite container = UIUtils.createPlaceholder(this.pageControl, 1, 5);
        GridData gd = new GridData(1808);
        container.setLayoutData(gd);
        this.newUser = !this.getDatabaseObject().isPersisted();
        Composite loginGroup = UIUtils.createControlGroup(container, GBase8aMessages.editors_user_editor_general_group_login, 2, 32, 200);
        this.userNameText = UIUtils.createLabelText(loginGroup, GBase8aMessages.editors_user_editor_general_label_user_name, this.getDatabaseObject().getUserName());
        this.userNameText.setEditable(this.newUser);
        if (this.newUser) {
            ControlPropertyCommandListener.create(this, this.userNameText, UserPropertyHandler.NAME);
        }

        this.hostText = UIUtils.createLabelText(loginGroup, GBase8aMessages.editors_user_editor_general_label_host, this.getDatabaseObject().getHost());
        this.hostText.setEditable(this.newUser);
        if (this.newUser) {
            ControlPropertyCommandListener.create(this, this.hostText, UserPropertyHandler.HOST);
        }

        String password = this.newUser ? "" : "**********";
        Text passwordText = UIUtils.createLabelText(loginGroup, GBase8aMessages.editors_user_editor_general_label_password, password, 4196352);
        ControlPropertyCommandListener.create(this, passwordText, UserPropertyHandler.PASSWORD);
        Text confirmText = UIUtils.createLabelText(loginGroup, GBase8aMessages.editors_user_editor_general_label_confirm, password, 4196352);
        ControlPropertyCommandListener.create(this, confirmText, UserPropertyHandler.PASSWORD_CONFIRM);
        this.privTable = new PrivilegeTableControl(container, GBase8aMessages.editors_user_editor_general_control_dba_privileges);
        gd = new GridData(768);
        gd.horizontalSpan = 1;
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        gd.heightHint = 320;
        this.privTable.setLayoutData(gd);
        this.privTable.addListener(24, new Listener() {
            public void handleEvent(Event event) {
                final GBase8aPrivilege privilege = (GBase8aPrivilege) event.data;
                final boolean grant = event.detail == 1;
                GBase8aUserEditorGeneral.this.addChangeCommand(new GBase8aCommandGrantPrivilege(GBase8aUserEditorGeneral.this.getDatabaseObject(),
                        grant, null, null, null, privilege), new DBECommandReflector<GBase8aUser, GBase8aCommandGrantPrivilege>() {
                    public void redoCommand(GBase8aCommandGrantPrivilege GBaseDataCommandGrantPrivilege) {
                        if (!GBase8aUserEditorGeneral.this.privTable.isDisposed()) {
                            GBase8aUserEditorGeneral.this.privTable.checkPrivilege(privilege, grant);
                        }

                    }

                    public void undoCommand(GBase8aCommandGrantPrivilege GBaseDataCommandGrantPrivilege) {
                        if (!GBase8aUserEditorGeneral.this.privTable.isDisposed()) {
                            GBase8aUserEditorGeneral.this.privTable.checkPrivilege(privilege, !grant);
                        }

                    }
                });
            }
        });
        this.pageControl.createProgressPanel();
        this.commandlistener = new CommandListener();
        this.getEditorInput().getCommandContext().addCommandListener(this.commandlistener);
    }

    public void dispose() {
        if (this.commandlistener != null) {
            this.getEditorInput().getCommandContext().removeCommandListener(this.commandlistener);
        }

        super.dispose();
    }

    public void activatePart() {
        if (!this.isLoaded) {
            this.isLoaded = true;
            LoadingJob.createService(new DatabaseLoadService<List<GBase8aPrivilege>>(GBase8aMessages.editors_user_editor_general_service_load_catalog_privileges, this.getExecutionContext()) {
                public List<GBase8aPrivilege> evaluate(DBRProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        List<GBase8aPrivilege> privList = GBase8aUserEditorGeneral.this.getDatabaseObject().getDataSource().getPrivileges(monitor);
                        List<GBase8aPrivilege> gen_list = new ArrayList();
                        Iterator<GBase8aPrivilege> iterator = privList.iterator();

                        while (iterator.hasNext()) {
                            GBase8aPrivilege priv = iterator.next();
                            if (priv.getKind() == GBase8aPrivilege.Kind.ADMIN) {
                                if (!priv.getName().equalsIgnoreCase("proxy") && !priv.getName().toLowerCase().startsWith("replication")) {
                                    gen_list.add(priv);
                                }
                            } else if (priv.getName().equalsIgnoreCase("file")) {
                                gen_list.add(priv);
                            }
                        }

                        return gen_list;
                    } catch (DBException var6) {
                        DBException e = var6;
                        throw new InvocationTargetException(e);
                    }
                }
            }, this.pageControl.createLoadVisualizer()).schedule();
        }
    }

    protected PageControl getPageControl() {
        return this.pageControl;
    }

    protected void processGrants(List<GBase8aGrant> grants) {
        this.privTable.fillGrants(grants);
    }

    public RefreshResult refreshPart(Object source, boolean force) {
        return RefreshResult.IGNORED;
    }

    private class CommandListener extends DBECommandAdapter {
        private CommandListener() {
        }

        public void onSave() {
            if (GBase8aUserEditorGeneral.this.newUser && GBase8aUserEditorGeneral.this.getDatabaseObject().isPersisted()) {
                GBase8aUserEditorGeneral.this.newUser = false;
                UIUtils.asyncExec(() -> {
                    GBase8aUserEditorGeneral.this.userNameText.setEditable(false);
                    GBase8aUserEditorGeneral.this.hostText.setEditable(false);
                });
            }

        }
    }

    private class PageControl extends GBase8aUserEditorAbstract.UserPageControl {
        public PageControl(Composite parent) {
            super(parent);
        }

        public ProgressPageControl.ProgressVisualizer<List<GBase8aPrivilege>> createLoadVisualizer() {
            return new ProgressPageControl.ProgressVisualizer<>() {
                public void completeLoading(List<GBase8aPrivilege> privs) {
                    super.completeLoading(privs);
                    GBase8aUserEditorGeneral.this.privTable.fillPrivileges(privs);
                    GBase8aUserEditorGeneral.this.loadGrants();
                }
            };
        }
    }
}