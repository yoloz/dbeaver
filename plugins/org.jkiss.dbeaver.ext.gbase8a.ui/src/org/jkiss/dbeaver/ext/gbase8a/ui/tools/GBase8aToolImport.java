//package org.jkiss.dbeaver.ext.gbase8a.ui.tools;
//
//import org.jkiss.dbeaver.DBException;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
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
//public class GBase8aToolImport implements IExternalTool {
//    public void execute(IWorkbenchWindow window, IWorkbenchPart activePart, Collection<DBSObject> objects) throws DBException {
//        for (DBSObject object : objects) {
//            if (object instanceof GBase8aCatalog) {
//                NativeToolWizardDialog dialog = new NativeToolWizardDialog (
//                        window,
//                        (IWizard) new GBase8aScriptExecuteWizard((GBase8aCatalog) object, true));
//                dialog.open();
//            }
//        }
//    }
//}
