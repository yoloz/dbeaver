package org.jkiss.dbeaver.ext.gbase8a.ui.tools;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.util.Collection;

/**
 * @author yolo
 */
public interface IExternalTool {
    void execute(IWorkbenchWindow paramIWorkbenchWindow, IWorkbenchPart paramIWorkbenchPart, Collection<DBSObject> paramCollection) throws DBException;
}
