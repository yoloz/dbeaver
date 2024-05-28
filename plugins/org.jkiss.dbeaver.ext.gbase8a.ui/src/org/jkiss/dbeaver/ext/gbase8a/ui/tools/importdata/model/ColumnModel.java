package org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata.model;


public class ColumnModel {
    private String catalogName;
    private String tableName;
    private String columnName;
    private int columnType;
    private String typeName;
    private int columnSize;
    private int decimalDigits;

    public String getCatalogName() {
        
        return this.catalogName;
    }

    public void setCatalogName(String catalogName) {
        
        this.catalogName = catalogName;
    }

    public String getTableName() {
        
        return this.tableName;
    }

    public void setTableName(String tableName) {
        
        this.tableName = tableName;
    }

    public String getColumnName() {
        
        return this.columnName;
    }

    public void setColumnName(String columnName) {
        
        this.columnName = columnName;
    }

    public int getColumnType() {
        
        return this.columnType;
    }

    public void setColumnType(int columnType) {
        
        this.columnType = columnType;
    }

    public String getTypeName() {
        
        return this.typeName;
    }

    public void setTypeName(String typeName) {
        
        this.typeName = typeName;
    }

    public int getColumnSize() {
        
        return this.columnSize;
    }

    public void setColumnSize(int columnSize) {
        
        this.columnSize = columnSize;
    }

    public int getDecimalDigits() {
        
        return this.decimalDigits;
    }

    public void setDecimalDigits(int decimalDigits) {
        
        this.decimalDigits = decimalDigits;
    }
}
