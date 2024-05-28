//package org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata;
//
//import org.jkiss.dbeaver.ext.gbase8a.data.GBase8aMessages;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDBProcess;
//import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
//import org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata.model.ColumnModel;
//import org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata.model.DateFormatModel;
//import org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata.model.FieldModel;
//import org.jkiss.dbeaver.model.DBUtils;
//import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
//import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
//import org.jkiss.dbeaver.model.runtime.VoidProgressMonitor;
//import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.ui.IWorkbenchWindow;
//
//
//public class DataImporter {
//    private final IWorkbenchWindow window;
//    private final ParseOption parseOption;
//    public static final HashMap<String, String> dataTypeMap = new HashMap<String, String>();
//
//    static {
//        dataTypeMap.put("bigint",
//                "Number");
//        dataTypeMap.put("bit",
//                "Number");
//        dataTypeMap.put("tinyint",
//                "Number");
//        dataTypeMap.put("int",
//                "Number");
//        dataTypeMap.put("smallint",
//                "Number");
//        dataTypeMap.put("decimal",
//                "Number");
//        dataTypeMap.put("float",
//                "Number");
//        dataTypeMap.put("double",
//                "Number");
//
//        dataTypeMap.put("char",
//                "String");
//        dataTypeMap.put("varchar",
//                "String");
//        dataTypeMap.put("text",
//                "String");
//        dataTypeMap.put("longtext",
//                "String");
//        dataTypeMap.put("blob",
//                "String");
//        dataTypeMap.put("blob uri",
//                "String");
//
//        dataTypeMap.put("date",
//                "Date");
//        dataTypeMap.put("datetime",
//                "Date");
//        dataTypeMap.put("time",
//                "Date");
//        dataTypeMap.put("timestamp",
//                "Date");
//        dataTypeMap.put("year",
//                "Date");
//    }
//
//    private final String logFileExtName = ".log";
//
//    private int importedRowsNum = 0;
//    private int errorRowsNum = 0;
//
//    private double importTime;
//
//    private String importResult;
//    private final ArrayList<String> columnList = new ArrayList<String>();
//
//    private boolean checkFlag = true;
//
//    private boolean continueFlag = true;
//
//    private String sql;
//    private final GBase8aDataSource dataSource;
//    private final String oldDBName;
////    private GBase8aDBProcess dbProcess;
//
//    public DataImporter(IWorkbenchWindow window, ParseOption parseOption, GBase8aDataSource dataSource) {
//        this.window = window;
//        this.parseOption = parseOption;
//        this.dataSource = dataSource;
//        this.oldDBName = parseOption.getOldDBName();
////        this.dbProcess = dataSource.getDbProcess();
//    }
//
//
//    public void importDataToDb(List<String[]> data, Map dataMap, ImportOption importOption) throws Exception {
//        String dataFilePath = importOption.getDataFilePath();
//        String logFilePath = dataFilePath + ".log";
//
//        this.importedRowsNum = 0;
//        this.errorRowsNum = 0;
//        this.columnList.clear();
//
//        JDBCSession dbsession = DBUtils.openUtilSession(new VoidProgressMonitor(), this.dataSource, "importDataToDb");
//        BufferedWriter bw = null;
//        String vc = null;
//        String db = null;
//        String tb = null;
//        GBase8aDBProcess dbProcess = this.dataSource.getDbProcess();
//        try(JDBCStatement stmt = dbsession.createStatement()) {
//            vc = importOption.getVcName();
//            db = importOption.getDbName();
//            tb = importOption.getTbName();
//
//            if (this.dataSource.isVCCluster() && !dbProcess.isSystemDatabase(db)) {
//                dbProcess.switchVC(dbsession, vc);
//            }
//            dbProcess.switchDB(dbsession, db);
//            this.sql = "";
//
//            Date beforeImpDate = new Date();
//
//            File logFile = new File(logFilePath);
//
//            if (logFile.exists()) {
//                logFile.delete();
//            } else {
//                logFile.createNewFile();
//            }
//            bw = new BufferedWriter(new FileWriter(logFile));
//            if (importOption.isClearTable()) {
//                if (this.dataSource.isVCCluster() && !dbProcess.isSystemDatabase(db)) {
//                    this.sql = dbProcess.getTruncateSql(vc, db, tb);
//                } else {
//                    this.sql = dbProcess.getTruncateSql(db, tb);
//                }
//                stmt.execute(this.sql);
//                this.sql = "";
//            }
//            int commitNum = importOption.getCommitNum();
//            int rowNum = importOption.getRowNum();
//            String sqlHead = null;
//            if (this.dataSource.isVCCluster() && !dbProcess.isSystemDatabase(db)) {
//                sqlHead = dbProcess.getImportSqlHeader(vc, db, tb);
//            } else {
//                sqlHead = dbProcess.getImportSqlHeader(db, tb);
//            }
//            String preparesql = getPreparedColumnsSQL(data, dataMap, importOption, rowNum, sqlHead);
//            try (JDBCPreparedStatement ps = dbsession.prepareStatement(preparesql)){
//                DataParser csvDataParser = new DataParser();
//                this.parseOption.setPosition(0L);
//                this.parseOption.setStartLineNum(0);
//                this.parseOption.setEndLineNum(0);
//                boolean allStopFlag = false;
//                long insert_num = 0L;
//                for (int times = 0; times < Math.floor(((10000 + rowNum) / 10000)) && insert_num < rowNum; times++) {
//                    this.parseOption.setStartLineNum(this.parseOption.getEndLineNum());
//                    this.parseOption.setEndLineNum(this.parseOption.getEndLineNum() + 10000);
//                    data = csvDataParser.dataToList(dataFilePath, this.parseOption);
//                    for (int i = 0; i < data.size() && insert_num < rowNum; i++) {
//                        insert_num++;
//                        String[] line = data.get(i);
//                        this.checkFlag = true;
//                        this.continueFlag = true;
//                        this.columnList.clear();
//                        int param_pos = 0;
//                        for (int j = 0; j < line.length; j++) {
//                            FieldModel fieldModel = (FieldModel) dataMap.get(Integer.valueOf(j));
//                            if (fieldModel != null) {
//                                param_pos++;
//                                ColumnModel columnModel = fieldModel.getColumnModel();
//                                String typeName = columnModel.getTypeName().trim()
//                                        .toLowerCase();
//                                columnModel.getColumnSize();
//                                if (this.columnList.contains(columnModel.getColumnName())) {
//                                    this.checkFlag = false;
//                                } else {
//                                    this.columnList.add(columnModel.getColumnName());
//                                }
//                                String fieldType = fieldModel.getFieldType();
//                                if (line[j] != null && !fieldType.equals("String") && line[j].isEmpty()) {
//                                    line[j] = null;
//                                }
//                                if (line[j] != null && !line[j].equalsIgnoreCase("null")) {
//                                    if (fieldType.equals("String")) {
//                                        ps.setString(param_pos, line[j]);
//                                    } else if (fieldType.equals("Date")) {
//                                        ps.setString(param_pos, line[j]);
//                                    } else if (fieldType.equals("Boolean")) {
//                                        String value = line[j];
//                                        if (value.equalsIgnoreCase("true")) {
//                                            value = "t";
//                                        } else if (value.equalsIgnoreCase("false")) {
//                                            value = "f";
//                                        }
//                                        ps.setString(param_pos, value);
//                                    } else {
//                                        BigDecimal bd = null;
//                                        try {
//                                            bd = new BigDecimal(line[j].trim());
//                                            String str = bd.toPlainString();
//                                            if (!dataTypeMap.get(typeName).equals( "Number")) {
//                                                this.checkFlag = false;
//                                            } else if ("int".equalsIgnoreCase(typeName)) {
//                                                try {
//                                                    int value = Integer.parseInt(str);
//                                                    if (value < -2147483647 ||
//                                                            value > Integer.MAX_VALUE) {
//                                                        this.checkFlag = false;
//                                                    }
//                                                } catch (Exception exception) {
//                                                    this.checkFlag = false;
//                                                }
//                                            } else if ("tinyint".equalsIgnoreCase(typeName)) {
//                                                try {
//                                                    byte value = Byte.parseByte(str);
//                                                    if (value < -127 ||value > Byte.MAX_VALUE) {
//                                                        this.checkFlag = false;
//                                                    }
//                                                } catch (Exception exception) {
//                                                    this.checkFlag = false;
//                                                }
//                                            } else if ("bigint".equalsIgnoreCase(typeName)) {
//                                                try {
//                                                    long value = Long.parseLong(str);
//                                                    if (value < Long.MIN_VALUE ||value > Long.MAX_VALUE) {
//                                                        this.checkFlag = false;
//                                                    }
//                                                } catch (Exception exception) {
//                                                    this.checkFlag = false;
//                                                }
//                                            } else if ("smallint".equalsIgnoreCase(typeName)) {
//                                                try {
//                                                    short value = Short.parseShort(str);
//                                                    if (value < -32767 || value > Short.MAX_VALUE) {
//                                                        this.checkFlag = false;
//                                                    }
//                                                } catch (Exception exception) {
//                                                    this.checkFlag = false;
//                                                }
//                                            } else if ("decimal".equalsIgnoreCase(typeName)) {
//                                                try {
//                                                    int size = columnModel.getColumnSize();
//                                                    int digits = columnModel.getDecimalDigits();
//                                                    BigDecimal value = new BigDecimal(line[j]);
//                                                    int precsion = value.precision();
//                                                    int scale = value.scale();
//                                                    if (precsion > size ||scale > digits || precsion - scale > size -digits) {
//                                                        this.checkFlag = false;
//                                                    }
//                                                } catch (Exception exception) {
//                                                    this.checkFlag = false;
//                                                }
//                                            } else if ("float".equalsIgnoreCase(typeName)) {
//                                                try {
//                                                    Float.parseFloat(str);
//                                                } catch (Exception exception) {
//                                                    this.checkFlag = false;
//                                                }
//                                            } else if ("double".equalsIgnoreCase(typeName)) {
//                                                try {
//                                                    Double.parseDouble(str);
//                                                } catch (Exception exception) {
//                                                    this.checkFlag = false;
//                                                }
//                                            }
//                                            ps.setString(param_pos, line[j]);
//                                        } catch (Exception exception) {
//                                            this.checkFlag = false;
//                                        }
//                                    }
//                                } else {
//                                    ps.setObject(param_pos, null);
//                                }
//                            }
//                        }
//
//
//                        if (!this.checkFlag) {
//                            this.errorRowsNum++;
//                            bw.write(this.sql);
//                            bw.newLine();
//                            bw.flush();
//                            this.window.getWorkbench().getDisplay().syncExec(new Runnable() {
//                                public void run() {
//                                    DataImporter.this.continueFlag = MessageDialog.openQuestion(DataImporter.this.window
//                                            .getWorkbench().getDisplay()
//                                            .getActiveShell(), GBase8aMessages.import_properties_tip, DataImporter.this.sql +
//                                            "\n" +
//                                            GBase8aMessages.import_properties_error_coninues);
//                                }
//                            });
//                            if (!this.continueFlag) {
//                                allStopFlag = true;
//                                break;
//                            }
//                        } else {
//                            this.importedRowsNum++;
//                            ps.addBatch();
//                            if (i % commitNum == 0) {
//                                ps.executeBatch();
//                                ps.clearBatch();
//                            }
//                        }
//                    }
//                    ps.executeBatch();
//                    if (allStopFlag) {
//                        break;
//                    }
//                }
//            }
//            Date affterImpDate = new Date();
//            this.importTime = (affterImpDate.getTime() - beforeImpDate
//                    .getTime()) / 1000.0D;
//            this.importResult = this.importedRowsNum + " records imported in " +
//                    this.importTime + " seconds (" + this.errorRowsNum + " failed).";
//            bw.write("\n" + this.importResult);
//        } catch (Exception e) {
//            throw e;
//        } finally {
//            try {
//                if (bw != null) {
//                    bw.flush();
//                    bw.close();
//                }
//            } catch (IOException iOException) {
//            }
//            dbProcess.switchDB(dbsession, this.oldDBName);
//        }
//    }
//
//
//    private String getPreparedColumnsSQL(List<String[]> data, Map dataMap, ImportOption importOption, int rowNum, String sqlHead) throws Exception {
//        int i = 0;
//        if (i < data.size()) {
//            String[] line = data.get(i);
//            StringBuffer columnSb = new StringBuffer();
//            StringBuffer valueSb = new StringBuffer();
//
//            for (int j = 0; j < line.length; j++) {
//                FieldModel fieldModel = (FieldModel) dataMap.get(Integer.valueOf(j));
//                if (fieldModel != null) {
//                    if (valueSb.length() != 0) {
//                        valueSb.append(",");
//                        columnSb.append(",");
//                    }
//                    ColumnModel columnModel = fieldModel.getColumnModel();
//                    columnSb.append(columnModel.getColumnName());
//
//                    valueSb.append("?");
//                }
//            }
//
//            String sb = sqlHead + " (" + columnSb +
//                    ") \nvalues (" + valueSb + ")";
//
//            return sb;
//        }
//
//        return null;
//    }
//
//
//    public void importDataToFile(List<String[]> data, Map dataMap, ImportOption importOption, String filePath) throws Exception {
//        try {
//            this.importedRowsNum = 0;
//            this.errorRowsNum = 0;
//
//            String dataFilePath = importOption.getDataFilePath();
//
//            Date beforeImpDate = new Date();
//
//            File file = new File(filePath);
//            if (file.exists()) {
//                file.delete();
//            } else {
//                file.createNewFile();
//            }
//
//            FileOutputStream fos = new FileOutputStream(file);
//            String charset = IConstants.CHARSET_ARRAY[this.parseOption.getCharset()];
//
//            StringBuffer sb = new StringBuffer();
//
//
//            if (importOption.isClearTable()) {
//                this.sql = "truncate " + importOption.getDbName() + ":" +
//                        importOption.getTbName() + ";\n\n";
//                sb.append(this.sql);
//            }
//
//            String sqlHead = "insert into " + importOption.getDbName() + ":" +
//                    importOption.getTbName();
//
//            int rowNum = importOption.getRowNum();
//
//
//            if (rowNum >= this.parseOption.getPreviewLine()) {
//                DataParser csvDataParser = new DataParser();
//                data = csvDataParser.dataToList(dataFilePath, this.parseOption);
//            }
//
//            for (int i = 0; i < rowNum; i++) {
//                String[] line = data.get(i);
//                StringBuffer columnSb = new StringBuffer();
//                StringBuffer valueSb = new StringBuffer();
//                for (int j = 0; j < line.length; j++) {
//                    FieldModel fieldModel = (FieldModel) dataMap.get(Integer.valueOf(j));
//                    if (fieldModel != null) {
//                        if (valueSb.length() != 0) {
//                            valueSb.append(",");
//                            columnSb.append(",");
//                        }
//                        if (line[j] != null) {
//                            ColumnModel columnModel = fieldModel
//                                    .getColumnModel();
//
//
//                            columnSb.append(columnModel.getColumnName());
//
//                            String fieldType = fieldModel.getFieldType();
//                            if (fieldType.equals("String")) {
//                                if (line[j].contains("'")) {
//                                    line[j] = line[j].replaceAll("'", "''");
//                                }
//                                valueSb.append("'").append(line[j]).append("'");
//                            } else if (fieldType
//                                    .equals("Date")) {
//
//
//                                if (fieldModel.getDateFormat() == null) {
//
//
//                                    fieldModel
//                                            .setDateFormat(new DateFormatModel(
//                                                    "", "", ""));
//                                }
//
//                                valueSb.append("TO_DATE('")
//                                        .append(line[j])
//                                        .append("','")
//                                        .append(fieldModel.getDateFormat()
//                                                .getGcFormat()).append("')");
//                            } else {
//                                valueSb.append(line[j]);
//                            }
//                        } else {
//
//                            valueSb.append("NULL");
//                        }
//                    }
//                }
//
//                sb.append(sqlHead).append(" (").append(columnSb)
//                        .append(") \nvalues (").append(valueSb)
//                        .append(");\n\n");
//                this.importedRowsNum++;
//            }
//
//            sb.append("commit;\n");
//
//            fos.write(sb.toString().getBytes(charset));
//            fos.flush();
//            fos.close();
//
//            Date affterImpDate = new Date();
//
//            this.importTime = (affterImpDate.getTime() - beforeImpDate
//                    .getTime()) / 1000.0D;
//
//            this.importResult = this.importedRowsNum + " records imported in " +
//                    this.importTime + " seconds (" + this.errorRowsNum + " failed).";
//        } catch (Exception e) {
//
//            throw e;
//        }
//    }
//
//
//    public int getImportedRowsNum() {
//        return this.importedRowsNum;
//    }
//
//    public int getErrorRowsNum() {
//        return this.errorRowsNum;
//    }
//
//    public String getImportResult() {
//        return this.importResult;
//    }
//
//    public double getImportTime() {
//        return this.importTime;
//    }
//}
