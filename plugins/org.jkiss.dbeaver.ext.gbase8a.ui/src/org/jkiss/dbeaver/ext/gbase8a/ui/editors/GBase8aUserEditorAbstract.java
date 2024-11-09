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

import org.eclipse.jface.action.IContributionManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aGrant;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aUser;
import org.jkiss.dbeaver.ext.gbase8a.ui.internal.GBase8aUIMessages;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.load.DatabaseLoadService;
import org.jkiss.dbeaver.ui.LoadingJob;
import org.jkiss.dbeaver.ui.controls.ObjectEditorPageControl;
import org.jkiss.dbeaver.ui.editors.AbstractDatabaseObjectEditor;
import org.jkiss.dbeaver.ui.editors.DatabaseEditorUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * GBase8aUserEditorAbstract
 */
public abstract class GBase8aUserEditorAbstract extends AbstractDatabaseObjectEditor<GBase8aUser> {

    void loadGrants() {
        LoadingJob.createService(new DatabaseLoadService<>(GBase8aUIMessages.editors_user_editor_abstract_load_grants,
                                         getDatabaseObject().getDataSource()) {
                                     @Override
                                     public List<GBase8aGrant> evaluate(DBRProgressMonitor monitor) throws InvocationTargetException {
                                         try {
                                             return getDatabaseObject().getGrants(monitor);
                                         } catch (DBException e) {
                                             throw new InvocationTargetException(e);
                                         }
                                     }
                                 },
                getPageControl().createGrantsLoadVisualizer()).schedule();
    }

    @Override
    public void setFocus() {
        if (getPageControl() != null) {
            getPageControl().setFocus();
        }
    }

    protected abstract UserPageControl getPageControl();

    protected abstract void processGrants(List<GBase8aGrant> grants);

    protected class UserPageControl extends ObjectEditorPageControl {
        public UserPageControl(Composite parent) {
            super(parent, SWT.NONE, GBase8aUserEditorAbstract.this);
        }

        public ProgressVisualizer<List<GBase8aGrant>> createGrantsLoadVisualizer() {
            return new ProgressVisualizer<>() {
                @Override
                public void completeLoading(List<GBase8aGrant> grants) {
                    super.completeLoading(grants);
                    processGrants(grants);
                }
            };
        }

        @Override
        public void fillCustomActions(IContributionManager contributionManager) {
            super.fillCustomActions(contributionManager);
            DatabaseEditorUtils.contributeStandardEditorActions(getSite(), contributionManager);
        }
    }

}