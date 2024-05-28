package org.jkiss.dbeaver.ext.gbase8a.ui.editors;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aGrant;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aUser;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.load.DatabaseLoadService;
import org.jkiss.dbeaver.ui.LoadingJob;
import org.jkiss.dbeaver.ui.controls.ObjectEditorPageControl;
import org.jkiss.dbeaver.ui.controls.ProgressPageControl;
import org.jkiss.dbeaver.ui.editors.AbstractDatabaseObjectEditor;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.swt.widgets.Composite;


public abstract class GBase8aUserEditorAbstract extends AbstractDatabaseObjectEditor<GBase8aUser> {

    void loadGrants() {
        LoadingJob.createService(new DatabaseLoadService<>(GBase8aMessages.editors_user_editor_abstract_load_grants,
                this.getDatabaseObject().getDataSource()) {
            public List<GBase8aGrant> evaluate(DBRProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                try {
                    return GBase8aUserEditorAbstract.this.getDatabaseObject().getGrants(monitor);
                } catch (DBException e) {
                    throw new InvocationTargetException(e);
                }
            }
        }, this.getPageControl().createGrantsLoadVisualizer()).schedule();
    }

    public void setFocus() {
        if (this.getPageControl() != null) {
            this.getPageControl().setFocus();
        }

    }

    protected abstract UserPageControl getPageControl();

    protected abstract void processGrants(List<GBase8aGrant> var1);

    protected class UserPageControl extends ObjectEditorPageControl {
        public UserPageControl(Composite parent) {
            super(parent, 0, GBase8aUserEditorAbstract.this);
        }

        public ProgressPageControl.ProgressVisualizer<List<GBase8aGrant>> createGrantsLoadVisualizer() {
            return new ProgressPageControl.ProgressVisualizer<>() {
                public void completeLoading(List<GBase8aGrant> grants) {
                    super.completeLoading(grants);
                    GBase8aUserEditorAbstract.this.processGrants(grants);
                }
            };
        }
    }
}
