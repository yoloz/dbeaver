//package org.jkiss.dbeaver.ext.gbase8a.ui.tools;
//
//import org.jkiss.dbeaver.DBException;
//import org.jkiss.dbeaver.model.struct.DBSObject;
//
//import java.util.Collection;
//
//import org.eclipse.jface.wizard.IWizard;
//import org.eclipse.ui.IWorkbenchPart;
//import org.eclipse.ui.IWorkbenchWindow;
//import org.jkiss.dbeaver.tasks.ui.nativetool.NativeToolWizardDialog;
//
//
//public class GBase8aToolExport implements IExternalTool {
//    public void execute(IWorkbenchWindow window, IWorkbenchPart activePart, Collection<DBSObject> objects) throws DBException {
//        NativeToolWizardDialog dialog = new NativeToolWizardDialog (
//                window,
//                (IWizard) new GBase8aExportWizard(objects));
//        dialog.open();
//    }
//}
