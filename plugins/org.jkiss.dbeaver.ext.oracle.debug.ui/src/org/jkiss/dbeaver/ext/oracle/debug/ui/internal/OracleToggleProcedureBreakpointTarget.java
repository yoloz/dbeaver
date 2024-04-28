/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2024 DBeaver Corp and others
 * Copyright (C) 2017-2018 Alexander Fedorov (alexander.fedorov@jkiss.org)
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

package org.jkiss.dbeaver.ext.oracle.debug.ui.internal;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension2;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.jkiss.dbeaver.debug.DBGConstants;
import org.jkiss.dbeaver.debug.core.breakpoints.DatabaseLineBreakpoint;
import org.jkiss.dbeaver.debug.core.breakpoints.IDatabaseBreakpoint;
import org.jkiss.dbeaver.debug.ui.DebugUI;
import org.jkiss.dbeaver.ext.oracle.debug.internal.impl.OracleDebugBreakpointDescriptor;
import org.jkiss.dbeaver.ext.oracle.model.OracleProcedureStandalone;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.navigator.DBNDatabaseNode;
import org.jkiss.dbeaver.model.runtime.VoidProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.runtime.DBWorkbench;
import org.jkiss.dbeaver.utils.GeneralUtils;

public class OracleToggleProcedureBreakpointTarget implements IToggleBreakpointsTargetExtension2 {

    @Override
    public void toggleBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
        toggleLineBreakpoints(part, selection);
    }

    @Override
    public boolean canToggleBreakpoints(IWorkbenchPart part, ISelection selection) {
        return canToggleLineBreakpoints(part, selection);
    }

    @Override
    public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
        IEditorPart editorPart = (IEditorPart) part;
        IResource resource = extractResource(editorPart, selection);
        if (resource == null) {
            return;
        }
        DBSObject databaseObject = DebugUI.extractDatabaseObject(editorPart);
        if (databaseObject == null) {
            return;
        }
        DBNDatabaseNode node = DBWorkbench.getPlatform().getNavigatorModel().getNodeByObject(new VoidProgressMonitor(), databaseObject, false);
        if (node == null) {
            return;
        }
        String nodeItemPath = node.getNodeUri();

        ITextSelection textSelection = (ITextSelection) selection;
        int lineNumber = textSelection.getStartLine();
        if (lineNumber == 0) {
            return;
        }
        IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager()
                .getBreakpoints(DBGConstants.MODEL_IDENTIFIER_DATABASE);
        for (IBreakpoint breakpoint : breakpoints) {
            if (breakpoint instanceof IDatabaseBreakpoint databaseBreakpoint) {
                if (nodeItemPath.equals(databaseBreakpoint.getNodePath())) {
                    if (((ILineBreakpoint) breakpoint).getLineNumber() == (lineNumber + 1)) {
                        DebugUITools.deleteBreakpoints(new IBreakpoint[]{breakpoint}, part.getSite().getShell(), null);
                        return;
                    }
                }
            }
        }
        int charstart = -1, charend = -1;
        OracleProcedureStandalone procedureStandalone = GeneralUtils.adapt(databaseObject, OracleProcedureStandalone.class);
        if (procedureStandalone == null) {
            throw new CoreException(GeneralUtils.makeErrorStatus(
                    "Oracle Object '" + DBUtils.getObjectFullName(databaseObject, DBPEvaluationContext.UI) + "' doesn't support breakpoints"));
        }
        OracleDebugBreakpointDescriptor descriptor = new OracleDebugBreakpointDescriptor(procedureStandalone.getName(), procedureStandalone.getSchema().getName(), lineNumber + 1);
        // create line breakpoint (doc line numbers start at 0)
        new DatabaseLineBreakpoint(
                databaseObject, node, resource,
                descriptor,
                lineNumber + 1, charstart, charend, true);
    }

    protected IResource extractResource(IEditorPart part, ISelection selection) {
        DBSObject databaseObject = DebugUI.extractDatabaseObject(part);
        return resolveWorkspaceResource(databaseObject);
    }

    public static IResource resolveWorkspaceResource(DBSObject dbsObject) {
        DBNDatabaseNode node = DBWorkbench.getPlatform().getNavigatorModel().getNodeByObject(dbsObject);
        return node == null || node.getOwnerProject() == null ? null : node.getOwnerProject().getEclipseProject();
    }

    @Override
    public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
        return true;
    }

    @Override
    public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
        // nothing by default
    }

    @Override
    public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
        return false;
    }

    @Override
    public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
        // nothing by default
    }

    @Override
    public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
        return false;
    }

    @Override
    public void toggleBreakpointsWithEvent(IWorkbenchPart part, ISelection selection, Event event)
            throws CoreException {
        // nothing by default
    }

    @Override
    public boolean canToggleBreakpointsWithEvent(IWorkbenchPart part, ISelection selection, Event event) {
        return false;
    }

}
