// package org.jkiss.dbeaver.ext.gbase8a.ui.tools;
// import org.jkiss.code.NotNull;
// import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
// import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableBase;
// import org.jkiss.dbeaver.model.preferences.DBPPreferenceStore;
// import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
// import org.jkiss.dbeaver.model.struct.DBSObject;
// import org.jkiss.dbeaver.tasks.ui.nativetool.AbstractNativeExportWizard;
// import org.jkiss.dbeaver.ui.UIUtils;
// import org.jkiss.dbeaver.ui.dialogs.DialogUtils;
// import org.jkiss.dbeaver.utils.GeneralUtils;
// import org.jkiss.dbeaver.utils.RuntimeUtils;
// import java.io.BufferedWriter;
// import java.io.File;
// import java.io.FileOutputStream;
// import java.io.IOException;
// import java.io.InputStream;
// import java.io.LineNumberReader;
// import java.io.OutputStream;
// import java.io.OutputStreamWriter;
// import java.text.NumberFormat;
// import java.util.Collection;
// import java.util.Iterator;
// import java.util.List;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;
// import org.eclipse.jface.viewers.IStructuredSelection;
// import org.eclipse.jface.wizard.IWizardPage;
// import org.eclipse.ui.IExportWizard;
// import org.eclipse.ui.IWorkbench;
// import org.jkiss.utils.CommonUtils;
//
// class GBase8aExportWizard extends AbstractNativeExportWizard<GBase8aDatabaseExportInfo> implements IExportWizard {
//   DumpMethod method;
//   boolean noCreateStatements;
//
//   public enum DumpMethod {
//     ONLINE,
//     LOCK_ALL_TABLES,
//     NORMAL;
//   }
//
//   boolean addDropStatements = true;
//
//   boolean disableKeys = true;
//
//   boolean extendedInserts = true;
//   boolean dumpEvents;
//   boolean comments;
//   boolean removeDefiner;
//   boolean binariesInHex;
//   boolean showViews;
//   public List<GBase8aDatabaseExportInfo> objects = new ArrayList<GBase8aDatabaseExportInfo>();
//
//   private GBase8aExportWizardPageObjects objectsPage;
//   private GBase8aExportWizardPageSettings settingsPage;
//
//   public GBase8aExportWizard(Collection<DBSObject> objects) {
//     super(objects, GBase8aMessages.tools_db_export_wizard_task_name);
//     this.method = DumpMethod.NORMAL;
//     this.outputFolder = new File(DialogUtils.getCurDialogFolder());
//
//     DBPPreferenceStore store = GBase8aActivator.getDefault().getPreferenceStore()();
//     this.outputFilePattern = store.getString("GBase8a.export.outputFilePattern");
//     if (CommonUtils.isEmpty(this.outputFilePattern)) {
//       this.outputFilePattern = "dump-${database}-${timestamp}.sql";
//     }
//     this.noCreateStatements = CommonUtils.getBoolean(store.getString("GBase8a.export.noCreateStatements"), false);
//     this.addDropStatements = CommonUtils.getBoolean(store.getString("GBase8a.export.addDropStatements"), true);
//     this.disableKeys = CommonUtils.getBoolean(store.getString("GBase8a.export.disableKeys"), true);
//     this.extendedInserts = CommonUtils.getBoolean(store.getString("GBase8a.export.extendedInserts"), true);
//     this.dumpEvents = CommonUtils.getBoolean(store.getString("GBase8a.export.dumpEvents"), false);
//     this.comments = CommonUtils.getBoolean(store.getString("GBase8a.export.comments"), false);
//     this.removeDefiner = CommonUtils.getBoolean(store.getString("GBase8a.export.removeDefiner"), false);
//     this.binariesInHex = CommonUtils.getBoolean(store.getString("GBase8a.export.binariesInHex"), false);
//     this.showViews = CommonUtils.getBoolean(store.getString("GBase8a.export.showViews"), false);
//   }
//
//
//   public void init(IWorkbench workbench, IStructuredSelection selection) {
//     super.init(workbench, selection);
//     this.objectsPage = new GBase8aExportWizardPageObjects(this);
//     this.settingsPage = new GBase8aExportWizardPageSettings(this);
//   }
//
//
//   public void addPages() {
//     addPage((IWizardPage)this.objectsPage);
//     addPage((IWizardPage)this.settingsPage);
//     super.addPages();
//   }
//
//
//   public IWizardPage getNextPage(IWizardPage page) {
//     if (page == this.settingsPage) {
//       return null;
//     }
//     return super.getNextPage(page);
//   }
//
//
//   public IWizardPage getPreviousPage(IWizardPage page) {
//     if (page == this.logPage) {
//       return (IWizardPage)this.settingsPage;
//     }
//     return super.getPreviousPage(page);
//   }
//
//
//   public void onSuccess() {
//     UIUtils.showMessageBox(
//         getShell(),
//         GBase8aMessages.tools_db_export_wizard_title,
//         CommonUtils.truncateString(NLS.bind(GBase8aMessages.tools_db_export_wizard_message_export_completed, getObjectsName()), 255),
//         2);
//     UIUtils.launchProgram(this.outputFolder.getAbsolutePath());
//   }
//
//
//
//   public void fillProcessParameters(List<String> cmd, GBase8aDatabaseExportInfo arg) throws IOException {
//     File dumpBinary = RuntimeUtils.getHomeBinary(getClientHome(), "bin", "gbasedump");
//     String dumpPath = dumpBinary.getAbsolutePath();
//     cmd.add(dumpPath);
//     switch (this.method) {
//       case null:
//         cmd.add("--lock-all-tables");
//         break;
//       case ONLINE:
//         cmd.add("--single-transaction");
//         break;
//     }
//
//     if (this.noCreateStatements) {
//       cmd.add("--no-create-info");
//     }
//     else if (CommonUtils.isEmpty(arg.getTables())) {
//       cmd.add("--routines");
//     }
//
//     if (this.addDropStatements) cmd.add("--add-drop-table");
//     if (this.disableKeys) cmd.add("--disable-keys");
//     if (this.extendedInserts) {
//       cmd.add("--extended-insert");
//     } else {
//       cmd.add("--skip-extended-insert");
//     }
//     if (this.binariesInHex) {
//       cmd.add("--hex-blob");
//     }
//     if (this.dumpEvents) cmd.add("--events");
//     if (this.comments) cmd.add("--comments");
//
//   }
//
//   protected void setupProcessParameters(ProcessBuilder process) {
//     if (!CommonUtils.isEmpty(getToolUserPassword())) {
//       process.environment().put("GBASE_PWD", getToolUserPassword());
//     }
//   }
//
//
//   public boolean performFinish() {
//     this.objectsPage.saveState();
//
//     DBPPreferenceStore store = GBase8aActivator.getDefault().getPreferenceStore()();
//     store.setValue("GBase8a.export.outputFilePattern", this.outputFilePattern);
//     store.setValue("GBase8a.export.noCreateStatements", this.noCreateStatements);
//     store.setValue("GBase8a.export.addDropStatements", this.addDropStatements);
//     store.setValue("GBase8a.export.disableKeys", this.disableKeys);
//     store.setValue("GBase8a.export.extendedInserts", this.extendedInserts);
//     store.setValue("GBase8a.export.dumpEvents", this.dumpEvents);
//     store.setValue("GBase8a.export.comments", this.comments);
//     store.setValue("GBase8a.export.removeDefiner", this.removeDefiner);
//     store.setValue("GBase8a.export.binariesInHex", this.binariesInHex);
//     store.setValue("GBase8a.export.showViews", this.showViews);
//
//     return super.performFinish();
//   }
//
//
//
//   public GBase8aServerHome findServerHome(String clientHomeId) {
//     return GBase8aDataSourceProvider.getServerHome(clientHomeId);
//   }
//
//
//   public Collection<GBase8aDatabaseExportInfo> getRunInfo() {
//     return this.objects;
//   }
//
//
//
//   protected List<String> getCommandLine(GBase8aDatabaseExportInfo arg) throws IOException {
//     List<String> cmd = GBase8aToolScript.getGBase8aToolCommandLine((AbstractToolWizard<DBSObject, GBase8aDatabaseExportInfo>)this, arg);
//     if (!this.objects.isEmpty())
//     {
//       if (!CommonUtils.isEmpty(arg.getTables())) {
//         cmd.add(arg.getDatabase().getName());
//         for (GBase8aTableBase table : arg.getTables()) {
//           cmd.add(table.getName());
//         }
//       } else {
//         cmd.add(arg.getDatabase().getName());
//       }
//     }
//     return cmd;
//   }
//
//
//
//   public boolean isVerbose() {
//     return true;
//   }
//
//
//
//   protected void startProcessHandler(DBRProgressMonitor monitor, final GBase8aDatabaseExportInfo arg, ProcessBuilder processBuilder, Process process) {
//     super.startProcessHandler(monitor, arg, processBuilder, process);
//
//     String outFileName = GeneralUtils.replaceVariables(this.outputFilePattern, new GeneralUtils.IVariableResolver()
//         {
//           public String get(String name) {
//             if (name.equals("database"))
//               return arg.getDatabase().getName();
//             if (name.equals("host"))
//               return arg.getDatabase().getDataSource().getContainer().getConnectionConfiguration().getHostName();
//             if (name.equals("table")) {
//               Iterator<GBase8aTableBase> iterator = (arg.getTables() == null) ? null : arg.getTables().iterator();
//               if (iterator != null && iterator.hasNext()) {
//                 return ((GBase8aTableBase)iterator.next()).getName();
//               }
//               return "null";
//             }
//             if (name.equals("timestamp")) {
//               return RuntimeUtils.getCurrentTimeStamp();
//             }
//             System.getProperty(name);
//
//             return null;
//           }
//         });
//
//     File outFile = new File(this.outputFolder, outFileName);
//     boolean isFiltering = this.removeDefiner;
//     Thread job = isFiltering ?
//       (Thread)new DumpFilterJob(monitor, process.getInputStream(), outFile) :
//       (Thread)new AbstractToolWizard.DumpCopierJob((AbstractToolWizard)this, monitor, GBase8aMessages.tools_db_export_wizard_monitor_export_db, process.getInputStream(), outFile);
//     job.start();
//   }
//
//   private static Pattern DEFINER_PATTER = Pattern.compile("DEFINER\\s*=\\s*`[^*]*`@`[0-9a-z\\-_\\.%]*`", 2);
//
//   class DumpFilterJob
//     extends AbstractToolWizard<DBSObject, GBase8aDatabaseExportInfo>.DumpJob {
//     protected DumpFilterJob(DBRProgressMonitor monitor, InputStream stream, File outFile) {
//       super((AbstractToolWizard)GBase8aExportWizard.this, GBase8aMessages.tools_db_export_wizard_job_dump_log_reader, monitor, stream, outFile);
//     }
//
//
//     public void runDump() throws IOException {
//       this.monitor.beginTask(GBase8aMessages.tools_db_export_wizard_monitor_export_db, 100);
//       long prevStatusUpdateTime = 0L;
//       try {
//         NumberFormat numberFormat = NumberFormat.getInstance();
//
//         LineNumberReader reader = new LineNumberReader(new InputStreamReader(this.input, "UTF-8"));
//         OutputStream output = new FileOutputStream(this.outFile);
//         try {
//           BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
//           while (true) {
//             String line = reader.readLine();
//             if (line == null) {
//               break;
//             }
//             Matcher matcher = GBase8aExportWizard.DEFINER_PATTER.matcher(line);
//             if (matcher.find()) {
//               line = matcher.replaceFirst("");
//             }
//             long currentTime = System.currentTimeMillis();
//             if (currentTime - prevStatusUpdateTime > 300L) {
//               this.monitor.subTask("Saved " + numberFormat.format(reader.getLineNumber()) + " lines");
//               prevStatusUpdateTime = currentTime;
//             }
//             line = filterLine(line);
//             writer.write(line);
//             writer.newLine();
//           }
//           writer.flush();
//         } finally {
//           output.close();
//         }
//       } finally {
//
//         this.monitor.done();
//       }
//     }
//
//     @NotNull
//     private String filterLine(@NotNull String line) {
//       return line;
//     }
//   }
// }
