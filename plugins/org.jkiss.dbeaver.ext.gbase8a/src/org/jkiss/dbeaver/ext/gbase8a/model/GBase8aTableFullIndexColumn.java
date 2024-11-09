package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.impl.struct.AbstractTableIndexColumn;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableIndexColumn;

import java.util.Collections;

public class GBase8aTableFullIndexColumn extends AbstractTableIndexColumn {

    private final Log log = Log.getLog(GBase8aTableFullIndexColumn.class);

    private final GBase8aTableFullIndex index;
    private GBase8aTableColumn tableColumn;
    private final int ordinalPosition;
    private final boolean ascending;
    private boolean nullable;
    private String subPart;
    private String path;


    public GBase8aTableFullIndexColumn(GBase8aTableFullIndex index, GBase8aTableColumn tableColumn, int ordinalPosition,
                                       boolean ascending, boolean nullable, String subPart) {
        this.index = index;
        String createSQL = "";
        try {
            createSQL = index.getTable().getDDL(getDataSource().getMonitor(), Collections.emptyMap());
            if (!createSQL.contains("INDEX_DATA_PATH")) {
                this.path = "";
            } else {
                String path = "FULLTEXT \"" + index.getName() + "\" " + "(\"" +
                        tableColumn.getName() + "\")INDEX_DATA_PATH='";
                if (createSQL.contains(path)) {
                    String tempSQL = createSQL.substring(createSQL.indexOf(path) + path.length());
                    this.path = tempSQL.substring(0, tempSQL.indexOf("'"));
                }
            }
        } catch (DBException e) {
           log.error(e);
        }
        this.tableColumn = tableColumn;
        this.ordinalPosition = ordinalPosition;
        this.ascending = ascending;
        this.nullable = nullable;
        this.subPart = subPart;
    }

    GBase8aTableFullIndexColumn(DBRProgressMonitor monitor, GBase8aTableFullIndex toIndex, DBSTableIndexColumn source) throws DBException {
        this.index = toIndex;
        if (source.getTableColumn() != null) {
            this.tableColumn = toIndex.getTable().getAttribute(monitor, source.getTableColumn().getName());
        }
        this.ordinalPosition = source.getOrdinalPosition();
        this.ascending = source.isAscending();
        if (source instanceof GBase8aTableFullIndexColumn) {
            this.nullable = ((GBase8aTableFullIndexColumn) source).nullable;
            this.subPart = ((GBase8aTableFullIndexColumn) source).subPart;
        }
    }


    @NotNull
    @Override
    public GBase8aTableFullIndex getIndex() {
        return this.index;
    }

    @Property(viewable = true, order = 1)
    @NotNull
    @Override
    public String getName() {
        return this.tableColumn.getName();
    }

    @Nullable
    @Override
    @Property(viewable = true, order = 2)
    public GBase8aTableColumn getTableColumn() {
        return this.tableColumn;
    }

    @Property(viewable = false, order = 3)
    public int getOrdinalPosition() {
        return this.ordinalPosition;
    }


    @Property(viewable = true, order = 4)
    @Override
    public boolean isAscending() {
        return this.ascending;
    }


    @Property(viewable = true, order = 5)
    public boolean isNullable() {
        return this.nullable;
    }

    @Property(viewable = true, order = 6)
    public String getSubPart() {
        return this.subPart;
    }

    @Nullable
    @Override
    public String getDescription() {
        return tableColumn.getDescription();
    }

    @Override
    public GBase8aTableFullIndex getParentObject() {
        return index;
    }

    @NotNull
    @Override
    public GBase8aDataSource getDataSource() {
        return this.index.getDataSource();
    }
}
