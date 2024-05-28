package org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata;


public class ParseOption {
    public static final int DEFAULT_COLUMN_NUMBER = 0;
    public static final int DEFAULT_ROW_NUMBER = 0;
    public static final int DEFAULT_CHARSET = 0;
    public static final boolean DEFAULT_HAS_HEADER = true;
    public static final String DEFAULT_ENCLOSE = "";
    public static final String DEFAULT_FIELD_TERMINATE = ",";
    public static final String DEFAULT_LINE_TERMINATE = "\n";
    private int columnNum = 0;
    private int rowNum = 0;
    private int charset = 0;
    private boolean hasHeader = true;
    private int previewLine;
    private String enclose = "";
    private String fieldTerminate = ",";
    private String lineTerminate = "\n";
    private int startLineNum = 0;
    private int endLineNum = 0;
    private long position = 0L;
    private String oldDBName;
    private String shapeType = "";


    public int getColumnNum() {
        return this.columnNum;
    }

    public void setColumnNum(int columnNum) {
        this.columnNum = columnNum;
    }

    public int getRowNum() {
        return this.rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public int getCharset() {
        return this.charset;
    }

    public void setCharset(int charset) {
        this.charset = charset;
    }

    public boolean isHasHeader() {
        return this.hasHeader;
    }

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public int getPreviewLine() {
        return this.previewLine;
    }

    public void setPreviewLine(int previewLine) {
        this.previewLine = previewLine;
    }

    public String getEnclose() {
        return this.enclose;
    }

    public void setEnclose(String enclose) {
        this.enclose = enclose;
    }

    public String getFieldTerminate() {
        return this.fieldTerminate;
    }

    public void setFieldTerminate(String fieldTerminate) {
        this.fieldTerminate = fieldTerminate;
    }

    public String getLineTerminate() {
        return this.lineTerminate;
    }

    public void setLineTerminate(String lineTerminate) {
        this.lineTerminate = lineTerminate;
    }

    public int getStartLineNum() {
        return this.startLineNum;
    }

    public void setStartLineNum(int startLineNum) {
        this.startLineNum = startLineNum;
    }

    public int getEndLineNum() {
        return this.endLineNum;
    }

    public void setEndLineNum(int endLineNum) {
        this.endLineNum = endLineNum;
    }

    public long getPosition() {
        return this.position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public String getOldDBName() {
        return this.oldDBName;
    }

    public void setOldDBName(String oldDBName) {
        this.oldDBName = oldDBName;
    }

    public String getShapeType() {
        return this.shapeType;
    }

    public void setShapeType(String shapeType) {
        this.shapeType = shapeType;
    }
}
