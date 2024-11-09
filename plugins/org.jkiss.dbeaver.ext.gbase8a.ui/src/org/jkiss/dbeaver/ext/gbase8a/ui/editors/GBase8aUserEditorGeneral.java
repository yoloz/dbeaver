/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2024 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ext.gbase8a.ui.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aGrant;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aPrivilege;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aUser;
import org.jkiss.dbeaver.ext.gbase8a.ui.controls.PrivilegeTableControl;
import org.jkiss.dbeaver.ext.gbase8a.ui.config.GBase8aCommandGrantPrivilege;
import org.jkiss.dbeaver.ext.gbase8a.ui.config.GBase8aUserManager;
import org.jkiss.dbeaver.ext.gbase8a.ui.config.UserPropertyHandler;
import org.jkiss.dbeaver.ext.gbase8a.ui.internal.GBase8aUIMessages;
import org.jkiss.dbeaver.model.edit.DBECommand;
import org.jkiss.dbeaver.model.edit.DBECommandContext;
import org.jkiss.dbeaver.model.edit.DBECommandReflector;
import org.jkiss.dbeaver.model.impl.edit.DBECommandAdapter;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.load.DatabaseLoadService;
import org.jkiss.dbeaver.ui.LoadingJob;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.editors.ControlPropertyCommandListener;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

/**
 * MySQLUserEditorGeneral
 */
public class GBase8aUserEditorGeneral extends GBase8aUserEditorAbstract {
    //static final Log log = Log.getLog(MySQLUserEditorGeneral.class);
    public static final String DEF_PASSWORD_VALUE = "**********"; //$NON-NLS-1$

    private PageControl pageControl;
    private boolean isLoaded;
    private PrivilegeTableControl privTable;
    private boolean newUser;
    private Text userNameText;
    private Text hostText;
    private CommandListener commandlistener;

    @Override
    public void createPartControl(Composite parent) {
        pageControl = new PageControl(parent);

        Composite container = UIUtils.createPlaceholder(pageControl, 2, 5);
        GridData gd = new GridData(GridData.FILL_BOTH);
        container.setLayoutData(gd);

        newUser = !getDatabaseObject().isPersisted();
        {
            Composite loginGroup = UIUtils.createControlGroup(container, GBase8aUIMessages.editors_user_editor_general_group_login, 2, GridData.FILL_HORIZONTAL, 0);

            userNameText = UIUtils.createLabelText(loginGroup, GBase8aUIMessages.editors_user_editor_general_label_user_name, getDatabaseObject().getUserName());
            userNameText.setEditable(newUser);
            if (newUser) {
                ControlPropertyCommandListener.create(this, userNameText, UserPropertyHandler.NAME);
            }

            hostText = UIUtils.createLabelText(loginGroup, GBase8aUIMessages.editors_user_editor_general_label_host, getDatabaseObject().getHost());
            hostText.setEditable(newUser);
            if (newUser) {
                ControlPropertyCommandListener.create(this, hostText, UserPropertyHandler.HOST);
            }

            String password = newUser ? "" : DEF_PASSWORD_VALUE; //$NON-NLS-1$
            Text passwordText = UIUtils.createLabelText(loginGroup, GBase8aUIMessages.editors_user_editor_general_label_password, password, SWT.BORDER | SWT.PASSWORD);
            ControlPropertyCommandListener.create(this, passwordText, UserPropertyHandler.PASSWORD);

            Text confirmText = UIUtils.createLabelText(loginGroup, GBase8aUIMessages.editors_user_editor_general_label_confirm, password, SWT.BORDER | SWT.PASSWORD);
            ControlPropertyCommandListener.create(this, confirmText, UserPropertyHandler.PASSWORD_CONFIRM);
        }

        {
            Composite limitsGroup = UIUtils.createControlGroup(container, GBase8aUIMessages.editors_user_editor_general_group_limits, 2, GridData.FILL_HORIZONTAL, 0);

            Spinner maxQueriesText = UIUtils.createLabelSpinner(limitsGroup, GBase8aUIMessages.editors_user_editor_general_spinner_max_queries, getDatabaseObject().getMaxQuestions(), 0, Integer.MAX_VALUE);
            ControlPropertyCommandListener.create(this, maxQueriesText, UserPropertyHandler.MAX_QUERIES);

            Spinner maxUpdatesText = UIUtils.createLabelSpinner(limitsGroup, GBase8aUIMessages.editors_user_editor_general_spinner_max_updates, getDatabaseObject().getMaxUpdates(), 0, Integer.MAX_VALUE);
            ControlPropertyCommandListener.create(this, maxUpdatesText, UserPropertyHandler.MAX_UPDATES);

            Spinner maxConnectionsText = UIUtils.createLabelSpinner(limitsGroup, GBase8aUIMessages.editors_user_editor_general_spinner_max_connections, getDatabaseObject().getMaxConnections(), 0, Integer.MAX_VALUE);
            ControlPropertyCommandListener.create(this, maxConnectionsText, UserPropertyHandler.MAX_CONNECTIONS);

            Spinner maxUserConnectionsText = UIUtils.createLabelSpinner(limitsGroup, GBase8aUIMessages.editors_user_editor_general_spinner_max_user_connections, getDatabaseObject().getMaxUserConnections(), 0, Integer.MAX_VALUE);
            ControlPropertyCommandListener.create(this, maxUserConnectionsText, UserPropertyHandler.MAX_USER_CONNECTIONS);
        }


        {
            privTable = new PrivilegeTableControl(container, GBase8aUIMessages.editors_user_editor_general_control_dba_privileges, true);
            gd = new GridData(GridData.FILL_BOTH);
            gd.horizontalSpan = 2;
            privTable.setLayoutData(gd);

            privTable.addListener(SWT.Modify, event -> {
                final GBase8aPrivilege privilege = (GBase8aPrivilege) event.data;
                final boolean grant = event.detail >= 1;
                final boolean withGrantOption = event.detail == 2;
                addChangeCommand(
                        new GBase8aCommandGrantPrivilege(
                                getDatabaseObject(),
                                grant,
                                withGrantOption,
                                null,
                                null,
                                privilege),
                        new DBECommandReflector<GBase8aUser, GBase8aCommandGrantPrivilege>() {
                            @Override
                            public void redoCommand(GBase8aCommandGrantPrivilege gBase8aCommandGrantPrivilege) {
                                if (!privTable.isDisposed()) {
                                    privTable.checkPrivilege(privilege, grant);
                                }
                            }

                            @Override
                            public void undoCommand(GBase8aCommandGrantPrivilege gBase8aCommandGrantPrivilege) {
                                if (!privTable.isDisposed()) {
                                    privTable.checkPrivilege(privilege, !grant);
                                }
                            }
                        });
            });

        }
        pageControl.createProgressPanel();

        commandlistener = new CommandListener();
        DBECommandContext context = getEditorInput().getCommandContext();
        if (context != null) {
            context.addCommandListener(commandlistener);
        }
    }

    @Override
    public void dispose() {
        DBECommandContext commandContext = getEditorInput().getCommandContext();
        if (commandlistener != null && commandContext != null) {
            commandContext.removeCommandListener(commandlistener);
        }
        super.dispose();
    }

    @Override
    public void activatePart() {
        if (isLoaded) {
            return;
        }
        isLoaded = true;
        LoadingJob.createService(
                        new DatabaseLoadService<>(GBase8aUIMessages.editors_user_editor_general_service_load_catalog_privileges, getExecutionContext()) {
                            @Override
                            public List<GBase8aPrivilege> evaluate(DBRProgressMonitor monitor) throws InvocationTargetException {
                                try {
                                    final List<GBase8aPrivilege> privList = getDatabaseObject().getDataSource().getPrivilegesByKind(monitor, GBase8aPrivilege.Kind.ADMIN);
                                    for (Iterator<GBase8aPrivilege> iterator = privList.iterator(); iterator.hasNext(); ) {
                                        GBase8aPrivilege priv = iterator.next();
                                        // Remove proxy (it is not singleton)
                                        if (priv.getName().equalsIgnoreCase("proxy")) {
                                            iterator.remove();
                                        }
                                    }
                                    return privList;
                                } catch (DBException e) {
                                    throw new InvocationTargetException(e);
                                }
                            }
                        },
                        pageControl.createLoadVisualizer())
                .schedule();
    }

    @Override
    protected PageControl getPageControl() {
        return pageControl;
    }

    @Override
    protected void processGrants(List<GBase8aGrant> grants) {
        privTable.fillGrants(grants);
    }

    @Override
    public RefreshResult refreshPart(Object source, boolean force) {
        // do nothing
        return RefreshResult.IGNORED;
    }

    private class PageControl extends UserPageControl {
        public PageControl(Composite parent) {
            super(parent);
        }

        public ProgressVisualizer<List<GBase8aPrivilege>> createLoadVisualizer() {
            return new ProgressVisualizer<List<GBase8aPrivilege>>() {
                @Override
                public void completeLoading(List<GBase8aPrivilege> privs) {
                    super.completeLoading(privs);
                    privTable.fillPrivileges(privs);
                    loadGrants();
                }
            };
        }

    }

    private class CommandListener extends DBECommandAdapter {
        @Override
        public void onSave() {
            if (newUser && getDatabaseObject().isPersisted()) {
                newUser = false;
                UIUtils.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        userNameText.setEditable(false);
                        hostText.setEditable(false);
                    }
                });
            }
        }

        @Override
        public void onCommandChange(DBECommand<?> command) {
            if (command instanceof GBase8aUserManager.CommandRenameUser) {
                GBase8aUserManager.CommandRenameUser mysqlCommand = (GBase8aUserManager.CommandRenameUser) command;
                setUsernameAndHost(mysqlCommand.getNewUserName(), mysqlCommand.getNewHost());
            }
        }

        @Override
        public void onReset() {
            GBase8aUser user = getDatabaseObject();
            setUsernameAndHost(user.getUserName(), user.getHost());
        }

        private void setUsernameAndHost(@NotNull String username, @NotNull String host) {
            UIUtils.asyncExec(() -> {
                if (!privTable.isDisposed()) {
                    userNameText.setText(username);
                    hostText.setText(host);
                }
            });
        }
    }
}
