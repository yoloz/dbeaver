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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.session.GBase8aSessionManager;
import org.jkiss.dbeaver.ext.gbase8a.ui.internal.GBase8aUIMessages;
import org.jkiss.dbeaver.model.admin.sessions.DBAServerSession;
import org.jkiss.dbeaver.model.admin.sessions.DBAServerSessionManager;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.ui.ActionUtils;
import org.jkiss.dbeaver.ui.DBeaverIcons;
import org.jkiss.dbeaver.ui.UIIcon;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.views.session.AbstractSessionEditor;
import org.jkiss.dbeaver.ui.views.session.SessionManagerViewer;
import org.jkiss.utils.CommonUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MySQLSessionEditor
 */
public class GBase8aSessionEditor extends AbstractSessionEditor {
    private static final Log log = Log.getLog(GBase8aSessionEditor.class);

    private KillSessionAction killSessionAction;
    private KillSessionAction terminateQueryAction;

    @Override
    public void createEditorControl(Composite parent) {
        killSessionAction = new KillSessionAction(false);
        terminateQueryAction = new KillSessionAction(true);
        super.createEditorControl(parent);
    }

    @Override
    protected SessionManagerViewer createSessionViewer(DBCExecutionContext executionContext, Composite parent) {
        final GBase8aDataSource dataSource = (GBase8aDataSource) executionContext.getDataSource();
        return new SessionManagerViewer<>(this, parent, new GBase8aSessionManager(dataSource)) {
            private boolean hideSleeping;

            @Override
            protected void contributeToToolbar(DBAServerSessionManager sessionManager, IContributionManager contributionManager) {
                contributionManager.add(killSessionAction);
                contributionManager.add(terminateQueryAction);
                contributionManager.add(new Separator());

                contributionManager.add(ActionUtils.makeActionContribution(
                        new Action(GBase8aUIMessages.editors_session_hide_sleeping_text, Action.AS_CHECK_BOX) {
                            {
                                setToolTipText(GBase8aUIMessages.editors_session_hide_sleeping_tip);
                                setImageDescriptor(DBeaverIcons.getImageDescriptor(UIIcon.HIDE_ALL_DETAILS));
                                setChecked(hideSleeping);
                            }

                            @Override
                            public void run() {
                                hideSleeping = isChecked();
                                refreshPart(GBase8aSessionEditor.this, true);
                            }
                        }, true));

//                if (dataSource.supportsSysSchema()) {
//                    contributionManager.add(ActionUtils.makeActionContribution(
//                        new Action(GBase8aUIMessages.editors_session_show_performance_text, Action.AS_CHECK_BOX) {
//                            {
//                                setToolTipText(GBase8aUIMessages.editors_session_show_performance_tip);
//                                setImageDescriptor(DBeaverIcons.getImageDescriptor(UIIcon.SHOW_ALL_DETAILS));
//                                setChecked(showPerformance);
//                            }
//
//                            @Override
//                            public void run() {
//                                showPerformance = isChecked();
//                                refreshPart(GBase8aSessionEditor.this, true);
//                            }
//                        }, true));
//                }

                contributionManager.add(new Separator());
            }

            @Override
            protected void onSessionSelect(DBAServerSession session) {
                super.onSessionSelect(session);
                killSessionAction.setEnabled(session != null);
                terminateQueryAction.setEnabled(session != null && !CommonUtils.isEmpty(session.getActiveQuery()));
            }

            @Override
            public Map<String, Object> getSessionOptions() {
                Map<String, Object> options = new HashMap<>();
                if (hideSleeping) {
                    options.put(GBase8aSessionManager.OPTION_HIDE_SLEEPING, true);
                }
                return options;
            }

            @Override
            protected void loadSettings(IDialogSettings settings) {
                hideSleeping = CommonUtils.toBoolean(settings.get("hideSleeping"));
                super.loadSettings(settings);
            }

            @Override
            protected void saveSettings(IDialogSettings settings) {
                super.saveSettings(settings);
                settings.put("hideSleeping", hideSleeping);
            }
        };
    }

    private class KillSessionAction extends Action {
        private boolean killQuery;

        public KillSessionAction(boolean killQuery) {
            super(
                    killQuery ? GBase8aUIMessages.editors_session_editor_action_terminate_Query : GBase8aUIMessages.editors_session_editor_action_kill_Session,
                    killQuery ?
                            UIUtils.getShardImageDescriptor(ISharedImages.IMG_ELCL_STOP) :
                            DBeaverIcons.getImageDescriptor(UIIcon.SQL_DISCONNECT));
            this.killQuery = killQuery;
        }

        @Override
        public void run() {
            final List<DBAServerSession> sessions = getSessionsViewer().getSelectedSessions();
            if (sessions != null && UIUtils.confirmAction(getSite().getShell(),
                    this.getText(),
                    NLS.bind(GBase8aUIMessages.editors_session_editor_confirm, getText(), sessions))) {
                getSessionsViewer().alterSessions(
                        sessions,
                        Collections.singletonMap(GBase8aSessionManager.PROP_KILL_QUERY, killQuery));
            }
        }
    }

}