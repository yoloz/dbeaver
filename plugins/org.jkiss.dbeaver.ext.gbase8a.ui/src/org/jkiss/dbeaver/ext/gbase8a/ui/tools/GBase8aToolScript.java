//package org.jkiss.dbeaver.ext.gbase8a.ui.tools;
//
//import org.jkiss.dbeaver.DBException;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
//import org.jkiss.dbeaver.model.connection.DBPConnectionConfiguration;
//import org.jkiss.dbeaver.model.struct.DBSObject;
//import org.jkiss.dbeaver.tasks.ui.nativetool.NativeToolWizardDialog;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//
//import org.eclipse.jface.wizard.IWizard;
//import org.eclipse.ui.IWorkbenchPart;
//import org.eclipse.ui.IWorkbenchWindow;
//import org.jkiss.utils.CommonUtils;
//
//
//public class GBase8aToolScript implements IExternalTool {
//
//    public void execute(IWorkbenchWindow window, IWorkbenchPart activePart, Collection<DBSObject> objects) throws DBException {
//        for (DBSObject object : objects) {
//            if (object instanceof GBase8aCatalog) {
//                NativeToolWizardDialog dialog = new NativeToolWizardDialog (
//                        window,
//                        new GBase8aScriptExecuteWizard((GBase8aCatalog) object, false));
//                dialog.open();
//            }
//        }
//    }
//
//
//    public static <BASE_OBJECT extends DBSObject, PROCESS_ARG> List<String> getGBase8aToolCommandLine(AbstractToolWizard<BASE_OBJECT, PROCESS_ARG> toolWizard, PROCESS_ARG arg) throws IOException {
//        List<String> cmd = new ArrayList<String>();
//        toolWizard.fillProcessParameters(cmd, arg);
//
//        if (toolWizard.isVerbose()) {
//            cmd.add("-v");
//        }
//        DBPConnectionConfiguration connectionInfo = toolWizard.getConnectionInfo();
//        cmd.add("--host=" + connectionInfo.getHostName());
//        if (!CommonU
//        tils.isEmpty(connectionInfo.getHostPort())) {
//            cmd.add("--port=" + connectionInfo.getHostPort());
//        }
//        cmd.add("-u");
//        cmd.add(toolWizard.getToolUserName());
//
//
//        return cmd;
//    }
//}
