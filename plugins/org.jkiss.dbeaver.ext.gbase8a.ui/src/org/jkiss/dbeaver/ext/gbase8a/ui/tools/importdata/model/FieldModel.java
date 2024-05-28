package org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata.model;


public class FieldModel {
    public static final String FIELD_TYPE_STRING = "String";
    public static final String FIELD_TYPE_NUMBER = "Number";
    public static final String FIELD_TYPE_DATE = "Date";
    public static final String FIELD_TYPE_BOOLEAN = "Boolean";
    private int fieldIndex;
    private String fieldName;
    private String fieldType = "String";

    private DateFormatModel dateFormat;

    private ColumnModel columnModel;

    private boolean isShow = true;

    public int getFieldIndex() {
        return this.fieldIndex;
    }

    public void setFieldIndex(int fieldIndex) {
        this.fieldIndex = fieldIndex;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return this.fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public DateFormatModel getDateFormat() {
        return this.dateFormat;
    }

    public void setDateFormat(DateFormatModel dateFormat) {
        this.dateFormat = dateFormat;
    }

    public ColumnModel getColumnModel() {
        return this.columnModel;
    }

    public void setColumnModel(ColumnModel columnModel) {
        this.columnModel = columnModel;
    }

    public boolean isShow() {
        return this.isShow;
    }

    public void setShow(boolean isShow) {
        this.isShow = isShow;
    }
}
