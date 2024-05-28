//package org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata;
//
//import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
//import org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata.model.FieldModel;
//
//import java.io.File;
//import java.lang.reflect.InvocationTargetException;
//import java.util.List;
//import java.util.Map;
//
//import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.jface.operation.IRunnableWithProgress;
//import org.eclipse.osgi.util.NLS;
//
//
//public class ImportTableDataProgress implements IRunnableWithProgress {
//    private DataImporter csvDataImporter;
//    private List<String[]> data;
//    private Map<Integer, FieldModel> dataMap;
//    private ImportOption importOption;
//    private String filePath;
//    private boolean isToDb;
//    private boolean isSuccess = false;
//    private String stdoutStr = "";
//    private String stderrStr = "";
//
//
//    public ImportTableDataProgress(DataImporter csvDataImporter, List<String[]> data, Map<Integer, FieldModel> dataMap, ImportOption importOption, boolean isToDb, String filePath) {
//        this.csvDataImporter = csvDataImporter;
//        this.data = data;
//        this.dataMap = dataMap;
//        this.importOption = importOption;
//        this.filePath = filePath;
//        this.isToDb = isToDb;
//    }
//
//
//    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//        String fileType = this.filePath.substring(this.filePath.lastIndexOf(".") + 1, this.filePath.length());
//
//        String importMessage = GBase8aMessages.import_properties_import_now;
//        if (fileType.toUpperCase().equals("SHP")) {
//            File tempFile = new File(this.importOption.getDataFilePath());
//            String tabName = tempFile.getName().split("\\.")[0];
//            if (!this.importOption.getNew_tablename().isEmpty()) {
//                tabName = this.importOption.getNew_tablename();
//            }
//            importMessage = NLS.bind(GBase8aMessages.import_properties_import_table_now, tabName);
//        }
//        monitor.beginTask(importMessage, -1);
//        monitor.setTaskName(importMessage);
//
//        try {
//            if (this.isToDb) {
//
//                this.csvDataImporter.importDataToDb(this.data, this.dataMap, this.importOption);
//            } else {
//                this.csvDataImporter.importDataToFile(this.data, this.dataMap, this.importOption,
//                        this.filePath);
//            }
//
//
//            this.isSuccess = true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            this.isSuccess = false;
//            this.stderrStr = e.getMessage();
//        }
//
//        monitor.done();
//    }
//
//    public boolean isSuccess() {
//        return this.isSuccess;
//    }
//
//    public String getStdoutStr() {
//        return this.stdoutStr;
//    }
//
//    public String getStderrStr() {
//        return this.stderrStr;
//    }
//}
