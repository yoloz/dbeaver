package org.jkiss.dbeaver.ext.gbase8a.ui.editors;

import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.ext.gbase8a.model.session.GBase8aSessionManager;
import org.jkiss.dbeaver.model.admin.sessions.DBAServerSession;
import org.jkiss.dbeaver.model.admin.sessions.DBAServerSessionManager;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.ui.DBeaverIcons;
import org.jkiss.dbeaver.ui.UIIcon;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.views.session.AbstractSessionEditor;
import org.jkiss.dbeaver.ui.views.session.SessionManagerViewer;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.jkiss.utils.CommonUtils;


public class GBase8aSessionEditor extends AbstractSessionEditor {
    private static final Log log = Log.getLog(GBase8aSessionEditor.class);

    private KillSessionAction killSessionAction;
    private KillSessionAction terminateQueryAction;

    @Override
    public void createEditorControl(Composite parent) {
        this.killSessionAction = new KillSessionAction(false);
        this.terminateQueryAction = new KillSessionAction(true);
        super.createEditorControl(parent);
    }

    protected SessionManagerViewer createSessionViewer(DBCExecutionContext executionContext, Composite parent) {
        return new SessionManagerViewer(this, parent, new GBase8aSessionManager((GBase8aDataSource) executionContext.getDataSource())) {
            protected void contributeToToolbar(DBAServerSessionManager sessionManager, IContributionManager contributionManager) {
                contributionManager.add(GBase8aSessionEditor.this.killSessionAction);
                contributionManager.add(GBase8aSessionEditor.this.terminateQueryAction);
                contributionManager.add(new Separator());
            }

            protected void onSessionSelect(DBAServerSession session) {
                super.onSessionSelect(session);
                GBase8aSessionEditor.this.killSessionAction.setEnabled((session != null));
                GBase8aSessionEditor.this.terminateQueryAction.setEnabled((session != null && !CommonUtils.isEmpty(session.getActiveQuery())));
            }
        };
    }

    private class KillSessionAction extends Action {
        private final boolean killQuery;

        public KillSessionAction(boolean killQuery) {
            super(killQuery ? GBase8aMessages.editors_session_editor_action_terminate_Query : GBase8aMessages.editors_session_editor_action_kill_Session,
                    DBeaverIcons.getImageDescriptor(killQuery ? UIIcon.REJECT : UIIcon.SQL_DISCONNECT));
            this.killQuery = killQuery;
        }


        public void run() {
            List<DBAServerSession> sessions = getSessionsViewer().getSelectedSessions();
            if (sessions != null && UIUtils.confirmAction(GBase8aSessionEditor.this.getSite().getShell(),
                    getText(),
                    NLS.bind(GBase8aMessages.editors_session_editor_confirm, getText(), sessions))) {
                getSessionsViewer().alterSessions(
                        sessions,
                        Collections.singletonMap("killQuery", this.killQuery));
            }
        }
    }
}
