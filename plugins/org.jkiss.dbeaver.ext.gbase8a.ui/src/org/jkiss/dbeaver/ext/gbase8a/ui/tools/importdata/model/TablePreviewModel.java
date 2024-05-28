//package org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata.model;
//
//import de.kupzog.ktable.KTableCellEditor;
//import de.kupzog.ktable.KTableCellRenderer;
//import de.kupzog.ktable.KTableDefaultModel;
//import de.kupzog.ktable.renderers.FixedCellRenderer;
//import de.kupzog.ktable.renderers.TextCellRenderer;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class TablePreviewModel extends KTableDefaultModel {
//    private final FixedCellRenderer m_fixedRenderer = new FixedCellRenderer(
//            32);
//
//    private final TextCellRenderer m_textRenderer = new TextCellRenderer(
//            32);
//    private String[] headers = new String[]{""};
//
//    private List<String[]> list = (List) new ArrayList<String>(0);
//
//    private int rowSize = 0;
//    private int colSize = 0;
//
//    public TablePreviewModel() {
//        initialize();
//    }
//
//
//    public TablePreviewModel(List<String[]> list, String[] headers, int rowSize, int colSize) {
//        this.list = list;
//        this.headers = headers;
//        this.rowSize = rowSize;
//        this.colSize = colSize;
//        initialize();
//    }
//
//
//    public int getFixedHeaderRowCount() {
//        return 1;
//    }
//
//    public int getFixedSelectableRowCount() {
//        return 0;
//    }
//
//    public int getFixedHeaderColumnCount() {
//        return 0;
//    }
//
//    public int getFixedSelectableColumnCount() {
//        return 0;
//    }
//
//    public boolean isColumnResizable(int col) {
//        return true;
//    }
//
//    public boolean isRowResizable(int row) {
//        return false;
//    }
//
//    public int getRowHeightMinimum() {
//        return 18;
//    }
//
//    public int getInitialColumnWidth(int column) {
//        return 90;
//    }
//
//    public int getInitialRowHeight(int row) {
//        return 22;
//    }
//
//    public Object doGetContentAt(int col, int row) {
//        Object obj;
//        if (row == 0) {
//            return this.headers[col];
//        }
//        if (row > this.list.size()) {
//            return "null";
//        }
//        String[] rowContent = this.list.get(row - 1);
//
//        if (col >= rowContent.length) {
//            obj = "NULL";
//        } else {
//            obj = ((String[]) this.list.get(row - 1))[col];
//            if (obj == null) {
//                obj = "NULL";
//            }
//        }
//        return obj;
//    }
//
//
//    public KTableCellEditor doGetCellEditor(int col, int row) {
//        return null;
//    }
//
//    public void doSetContentAt(int col, int row, Object value) {
//    }
//
//    public KTableCellRenderer doGetCellRenderer(int col, int row) {
//        if (isFixedCell(col, row)) {
//            return this.m_fixedRenderer;
//        }
//        return this.m_textRenderer;
//    }
//
//    public int doGetRowCount() {
//        return this.rowSize + getFixedRowCount();
//    }
//
//    public int doGetColumnCount() {
//        return this.colSize + getFixedColumnCount();
//    }
//}