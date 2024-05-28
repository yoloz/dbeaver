package org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata;


public class ImportOption {
    private boolean isClearTable;
    private boolean isCreateTable;
    private int rowNum;
    private String vcName;
    private String dbName;
    private String tbName;
    private String dataFilePath;
    private int commitNum = 10000;

    private String oldDbName;

    private String shapeType;

    private String new_tablename;

    public String getNew_tablename() {
        return this.new_tablename;
    }

    public void setNew_tablename(String new_tablename) {
        this.new_tablename = new_tablename;
    }

    public boolean isClearTable() {
        return this.isClearTable;
    }

    public void setClearTable(boolean isClearTable) {
        this.isClearTable = isClearTable;
    }

    public int getRowNum() {
        return this.rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public String getVcName() {
        return this.vcName;
    }

    public void setVcName(String vcName) {
        this.vcName = vcName;
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTbName() {
        return this.tbName;
    }

    public void setTbName(String tbName) {
        this.tbName = tbName;
    }

    public String getDataFilePath() {
        return this.dataFilePath;
    }

    public void setDataFilePath(String dataFilePath) {
        this.dataFilePath = dataFilePath;
    }

    public int getCommitNum() {
        return this.commitNum;
    }

    public void setCommitNum(int commitNum) {
        this.commitNum = commitNum;
    }

    public boolean isCreateTable() {
        return this.isCreateTable;
    }

    public void setCreateTable(boolean isCreateTable) {
        this.isCreateTable = isCreateTable;
    }

    public String getOldDbName() {
        return this.oldDbName;
    }

    public void setOldDbName(String oldDbName) {
        this.oldDbName = oldDbName;
    }

    public String getShapeType() {
        return this.shapeType;
    }

    public void setShapeType(String shapeType) {
        this.shapeType = shapeType;
    }
}
