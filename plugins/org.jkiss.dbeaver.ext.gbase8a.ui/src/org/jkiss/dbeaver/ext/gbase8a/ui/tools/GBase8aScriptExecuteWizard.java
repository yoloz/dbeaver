//package org.jkiss.dbeaver.ext.gbase8a.ui.tools;
//
//import org.jkiss.dbeaver.ext.gbase8a.GBase8aDataSourceProvider;
//import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aCatalog;
//import org.jkiss.dbeaver.model.connection.DBPClientHome;
//import org.jkiss.dbeaver.model.struct.DBSObject;
//import org.jkiss.dbeaver.utils.RuntimeUtils;
//import org.jkiss.dbeaver.tasks.ui.nativetool.AbstractNativeScriptExecuteWizard;
//
//import java.io.IOException;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.List;
//
//class GBase8aScriptExecuteWizard extends AbstractNativeScriptExecuteWizard<GBase8aCatalog, GBase8aCatalog> {
//    private LogLevel logLevel;
//    private boolean noBeep;
//    private boolean isImport;
//    private GBase8aScriptExecuteWizardPageSettings mainPage;
//
//    enum LogLevel {
//        Normal,
//        Verbose,
//        Debug;
//    }
//
//
//    public GBase8aScriptExecuteWizard(GBase8aCatalog catalog, boolean isImport) {
//        super(Collections.singleton(catalog), isImport ? GBase8aMessages.tools_script_execute_wizard_db_import : GBase8aMessages.tools_script_execute_wizard_execute_script);
//        this.isImport = isImport;
//        this.logLevel = LogLevel.Normal;
//        this.noBeep = true;
//        this.mainPage = new GBase8aScriptExecuteWizardPageSettings(this);
//    }
//
//
//    public LogLevel getLogLevel() {
//        return this.logLevel;
//    }
//
//
//    public void setLogLevel(LogLevel logLevel) {
//        this.logLevel = logLevel;
//    }
//
//
//    public boolean isImport() {
//        return this.isImport;
//    }
//
//
//    public boolean isVerbose() {
//        return !(this.logLevel != LogLevel.Verbose && this.logLevel != LogLevel.Debug);
//    }
//
//
//    public void addPages() {
//        addPage((IWizardPage) this.mainPage);
//        super.addPages();
//    }
//
//
//    public void fillProcessParameters(List<String> cmd, GBase8aCatalog arg) throws IOException {
//        String dumpPath = RuntimeUtils.getHomeBinary(getClientHome(), "bin", "gbase").getAbsolutePath();
//        cmd.add(dumpPath);
//        if (this.logLevel == LogLevel.Debug) {
//            cmd.add("--debug-info");
//        }
//        if (this.noBeep) {
//            cmd.add("--no-beep");
//        }
//    }
//
//
//    protected void setupProcessParameters(ProcessBuilder process) {
//        if (!CommonUtils.isEmpty(getToolUserPassword())) {
//            process.environment().put("GBASE_PWD", getToolUserPassword());
//        }
//    }
//
//
//    public GBase8aServerHome findServerHome(String clientHomeId) {
//        return GBase8aDataSourceProvider.getServerHome(clientHomeId);
//    }
//
//
//    public Collection<GBase8aCatalog> getRunInfo() {
//        return getDatabaseObjects();
//    }
//
//
//    protected List<String> getCommandLine(GBase8aCatalog arg) throws IOException {
//        List<String> cmd = GBase8aToolScript.getGBase8aToolCommandLine((AbstractToolWizard<DBSObject, GBase8aCatalog>) this, arg);
//        cmd.add(arg.getName());
//        return cmd;
//    }
//}
