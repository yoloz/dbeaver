//package org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata;
//
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.List;
//import javax.xml.parsers.SAXParser;
//import javax.xml.parsers.SAXParserFactory;
//
//import org.apache.commons.lang3.StringUtils;
//import org.apache.poi.openxml4j.opc.OPCPackage;
//import org.apache.poi.ss.usermodel.BuiltinFormats;
//import org.apache.poi.ss.usermodel.DataFormatter;
//import org.apache.poi.xssf.eventusermodel.XSSFReader;
//import org.apache.poi.xssf.model.SharedStringsTable;
//import org.apache.poi.xssf.model.StylesTable;
//import org.apache.poi.xssf.usermodel.XSSFCellStyle;
//import org.apache.poi.xssf.usermodel.XSSFRichTextString;
//import org.xml.sax.Attributes;
//import org.xml.sax.InputSource;
//import org.xml.sax.SAXException;
//import org.xml.sax.XMLReader;
//import org.xml.sax.helpers.DefaultHandler;
//
//
//public class ExcelXlsxReader  extends DefaultHandler {
//    private SharedStringsTable sst;
//    private String lastIndex;
//
//    enum CellDataType {
//        BOOL, ERROR, FORMULA, INLINESTR, SSTINDEX, NUMBER, DATE, NULL;
//    }
//
//
//    private String filePath = "";
//
//
//    private int sheetIndex = 0;
//
//
//    private String sheetName = "";
//
//
//    private int rowSize = 0;
//
//
//    private List<String> cellList = new ArrayList<String>();
//
//
//    private boolean flag = false;
//
//
//    private int curRow = 1;
//
//
//    private int curCol = 0;
//
//
//    private boolean isTElement;
//
//
//    private String exceptionMessage;
//
//
//    private CellDataType nextDataType = CellDataType.SSTINDEX;
//
//    private final DataFormatter formatter = new DataFormatter();
//
//
//    private short formatIndex;
//
//
//    private String formatString;
//
//
//    private String preRef = null;
//    private String ref = null;
//
//
//    private int curIndex;
//
//
//    private boolean nextIsString;
//
//
//    private String maxRef = null;
//
//    private ParseOption parseOption;
//    private String[] headers = null;
//    private List<String[]> rows = (List) new ArrayList<String>();
//
//
//    private StylesTable stylesTable;
//
//
//    public ExcelXlsxReader(ParseOption parseOption) {
//        this.parseOption = parseOption;
//    }
//
//
//    public int process(String filename) throws Exception {
//        this.filePath = filename;
//
//        OPCPackage pkg = OPCPackage.open(filename);
//
//        XSSFReader xssfReader = new XSSFReader(pkg);
//
//        this.stylesTable = xssfReader.getStylesTable();
//
//        SharedStringsTable sst = xssfReader.getSharedStringsTable();
//
//        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
//
//        SAXParser saxParser = saxFactory.newSAXParser();
//
//        XMLReader parser = saxParser.getXMLReader();
//
//        this.sst = sst;
//
//        parser.setContentHandler(this);
//
//        XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
//
//        while (sheets.hasNext()) {
//
//            this.curRow = 1;
//
//            this.sheetIndex++;
//
//            InputStream sheet = sheets.next();
//
//            this.sheetName = sheets.getSheetName();
//
//            InputSource sheetSource = new InputSource(sheet);
//
//            parser.parse(sheetSource);
//
//            sheet.close();
//        }
//
//
//        return this.rowSize;
//    }
//
//
//    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
//        if ("c".equals(name)) {
//
//
//            if (this.preRef == null) {
//
//                this.preRef = attributes.getValue("r");
//            } else {
//
//                this.preRef = this.ref;
//            }
//
//
//            this.ref = attributes.getValue("r");
//            this.curIndex = getColumnFromLetter(this.ref);
//
//
//            setNextDataType(attributes);
//
//            String cellType = attributes.getValue("t");
//            if (cellType != null && cellType.equals("s")) {
//                this.nextIsString = true;
//            } else {
//                this.nextIsString = false;
//            }
//        }
//
//
//        if ("t".equals(name)) {
//
//            this.isTElement = true;
//        } else {
//
//            this.isTElement = false;
//        }
//
//
//        this.lastIndex = "";
//    }
//
//
//    public void characters(char[] ch, int start, int length) throws SAXException {
//        this.lastIndex = String.valueOf(this.lastIndex) + new String(ch, start, length);
//    }
//
//
//    public void endElement(String uri, String localName, String name) throws SAXException {
//        if (this.nextIsString && StringUtils.isNotEmpty(this.lastIndex) && StringUtils.isNumeric(this.lastIndex)) {
//            int idx = Integer.parseInt(this.lastIndex);
//            this.lastIndex = (new XSSFRichTextString(this.sst.getEntryAt(idx))).toString();
//        }
//
//
//        if ("v".equals(name) || "t".equals(name)) {
//
//
//            String value = getDataValue(this.lastIndex.trim(), "");
//
//
//            if (!this.ref.equals(this.preRef)) {
//
//                int len = countNullCell(this.ref, this.preRef);
//
//                for (int j = 0; j < len; j++) {
//
//                    this.cellList.add(this.curCol, "");
//
//                    this.curCol++;
//                }
//            }
//
//
//            for (int i = this.curCol; i < this.curIndex - 1; i++) {
//                this.cellList.add(this.curCol, "");
//                this.curCol++;
//            }
//
//            this.cellList.add(this.curCol, value);
//
//            this.curCol++;
//
//
//            if (value != null && !"".equals(value)) {
//                this.flag = true;
//
//
//            }
//
//
//        } else if ("row".equals(name)) {
//
//
//            if (this.curRow == 1) {
//                this.maxRef = this.ref;
//            }
//
//
//            if (this.maxRef != null) {
//
//                int len = countNullCell(this.maxRef, this.ref);
//
//                for (int i = 0; i <= len; i++) {
//
//                    this.cellList.add(this.curCol, "");
//
//                    this.curCol++;
//                }
//            }
//
//
//            if (this.flag) {
//                if (this.parseOption.isHasHeader()) {
//                    if (this.headers == null) {
//                        this.headers = new String[this.cellList.size()];
//                        for (int i = 0; i < this.cellList.size(); i++) {
//                            this.headers[i] = this.cellList.get(i);
//                        }
//                    } else {
//                        String[] row = new String[this.headers.length];
//                        int colSize = this.headers.length;
//                        if (colSize > this.cellList.size()) {
//                            colSize = this.cellList.size();
//                        }
//                        for (int i = 0; i < colSize; i++) {
//                            String cell = this.cellList.get(i);
//                            if (cell != null) {
//                                cell = cell.trim();
//                            }
//                            row[i] = cell;
//                        }
//                        for (int j = colSize; j < this.headers.length; j++) {
//                            row[j] = "";
//                        }
//                        this.rows.add(row);
//                        this.rowSize++;
//                    }
//                } else {
//                    int colNum = 0;
//                    if (this.parseOption.getColumnNum() > 0) {
//                        colNum = this.parseOption.getColumnNum();
//                    } else {
//                        colNum = this.cellList.size();
//                    }
//                    String[] row = new String[colNum];
//                    int colSize = colNum;
//                    if (colSize > this.cellList.size()) {
//                        colSize = this.cellList.size();
//                    }
//                    for (int i = 0; i < colSize; i++) {
//                        row[i] = this.cellList.get(i);
//                    }
//                    for (int j = colSize; j < colNum; j++) {
//                        row[j] = "";
//                    }
//                    this.rows.add(row);
//                    this.rowSize++;
//                }
//            }
//
//
//            this.cellList.clear();
//
//            this.curRow++;
//
//            this.curCol = 0;
//
//            this.preRef = null;
//
//            this.ref = null;
//
//            this.flag = false;
//        }
//    }
//
//
//    public void setNextDataType(Attributes attributes) {
//        this.nextDataType = CellDataType.NUMBER;
//
//        this.formatIndex = -1;
//
//        this.formatString = null;
//
//        String cellType = attributes.getValue("t");
//
//        String cellStyleStr = attributes.getValue("s");
//
//        attributes.getValue("r");
//
//
//        if ("b".equals(cellType)) {
//
//            this.nextDataType = CellDataType.BOOL;
//        } else if ("e".equals(cellType)) {
//
//            this.nextDataType = CellDataType.ERROR;
//        } else if ("inlineStr".equals(cellType)) {
//
//            this.nextDataType = CellDataType.INLINESTR;
//        } else if ("s".equals(cellType)) {
//
//            this.nextDataType = CellDataType.SSTINDEX;
//        } else if ("str".equals(cellType)) {
//
//            this.nextDataType = CellDataType.FORMULA;
//        }
//
//
//        if (cellStyleStr != null) {
//
//            int styleIndex = Integer.parseInt(cellStyleStr);
//
//            XSSFCellStyle style = this.stylesTable.getStyleAt(styleIndex);
//
//            this.formatIndex = style.getDataFormat();
//
//            this.formatString = style.getDataFormatString();
//
//
//            if (this.formatString == null) {
//
//                this.nextDataType = CellDataType.NULL;
//
//                this.formatString = BuiltinFormats.getBuiltinFormat(this.formatIndex);
//            }
//
//
//            if (this.formatIndex == 14 || this.formatIndex == 31 || this.formatIndex == 57 || this.formatIndex == 58) {
//                this.nextDataType = CellDataType.DATE;
//                this.formatString = "yyyy-MM-dd hh:mm:ss";
//            } else if (this.formatIndex == 20 || this.formatIndex == 32) {
//                this.nextDataType = CellDataType.DATE;
//                this.formatString = "HH:mm";
//            }
//        }
//    }
//
//
//    public String getDataValue(String value, String thisStr) {
//        char first;
//        XSSFRichTextString rtsi;
//        String sstIndex;
//        switch (this.nextDataType) {
//
//
//            case null:
//                first = value.charAt(0);
//
//                thisStr = (first == '0') ? "FALSE" : "TRUE";
//
//
//                return thisStr;
//            case ERROR:
//                thisStr = "\"ERROR:" + value.toString() + '"';
//                return thisStr;
//            case FORMULA:
//                thisStr = String.valueOf('"') + value.toString() + '"';
//                return thisStr;
//            case INLINESTR:
//                rtsi = new XSSFRichTextString(value.toString());
//                thisStr = rtsi.toString();
//                rtsi = null;
//                return thisStr;
//            case SSTINDEX:
//                sstIndex = value.toString();
//                try {
//                    int idx = Integer.parseInt(sstIndex);
//                    XSSFRichTextString rtss = new XSSFRichTextString(this.sst.getEntryAt(idx));
//                    thisStr = rtss.toString();
//                    rtss = null;
//                } catch (NumberFormatException numberFormatException) {
//                    thisStr = value.toString();
//                }
//                return thisStr;
//            case NUMBER:
//                if (this.formatString != null) {
//                    thisStr = this.formatter.formatRawCellContents(Double.parseDouble(value), this.formatIndex, this.formatString).trim();
//                } else {
//                    thisStr = value;
//                }
//                thisStr = thisStr.replace("_", "").trim();
//                return thisStr;
//            case DATE:
//                thisStr = this.formatter.formatRawCellContents(Double.parseDouble(value), this.formatIndex, this.formatString);
//                thisStr = thisStr.replace("T", " ");
//                return thisStr;
//        }
//        thisStr = " ";
//        return thisStr;
//    }
//
//
//    public int countNullCell(String ref, String preRef) {
//        if (ref == null || preRef == null) {
//            return -1;
//        }
//
//        String xfd = ref.replaceAll("\\d+", "");
//
//        String xfd_1 = preRef.replaceAll("\\d+", "");
//
//
//        xfd = fillChar(xfd, 3, '@', true);
//
//        xfd_1 = fillChar(xfd_1, 3, '@', true);
//
//
//        char[] letter = xfd.toCharArray();
//
//        char[] letter_1 = xfd_1.toCharArray();
//
//        int res = (letter[0] - letter_1[0]) * 26 * 26 + (letter[1] - letter_1[1]) * 26 + letter[2] - letter_1[2];
//
//        return res - 1;
//    }
//
//
//    public String fillChar(String str, int len, char let, boolean isPre) {
//        int len_1 = str.length();
//
//        if (len_1 < len) {
//            if (isPre) {
//
//                for (int i = 0; i < len - len_1; i++) {
//                    str = String.valueOf(let) + str;
//
//                }
//            } else {
//
//                for (int i = 0; i < len - len_1; i++) {
//                    str = String.valueOf(str) + let;
//                }
//            }
//        }
//
//
//        return str;
//    }
//
//
//    private int getColumnFromLetter(String column) {
//        String c = column.toUpperCase().replaceAll("[0-9]", "");
//        int number = c.charAt(0) - 64;
//        return number;
//    }
//
//
//    public String getExceptionMessage() {
//        return this.exceptionMessage;
//    }
//
//
//    public String[] getHeaders() {
//        return this.headers;
//    }
//
//
//    public void setHeaders(String[] headers) {
//        this.headers = headers;
//    }
//
//
//    public List<String[]> getRows() {
//        return this.rows;
//    }
//
//
//    public void setRows(List<String[]> rows) {
//        this.rows = rows;
//    }
//}
