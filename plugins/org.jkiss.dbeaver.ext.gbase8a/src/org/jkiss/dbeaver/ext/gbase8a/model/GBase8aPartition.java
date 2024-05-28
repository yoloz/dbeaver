package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableObject;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class GBase8aPartition extends JDBCTableObject<GBase8aTable> {
    private GBase8aPartition parent;
    private List<GBase8aPartition> subPartitions;
    private int position;
    private String method;
    private String expression;
    private String description;
    private long tableRows;
    private long avgRowLength;
    private long dataLength;
    private long maxDataLength;
    private long indexLength;
    private long dataFree;
    private Date createTime;
    private Date updateTime;
    private Date checkTime;
    private long checksum;
    private String comment;
    private String nodegroup;
    private int count;

    protected GBase8aPartition(GBase8aTable gBase8aTable, GBase8aPartition parent, String name, ResultSet dbResult) {
        super(gBase8aTable, name, true);
        this.parent = parent;
        if (parent != null) {
            parent.addSubPartitions(this);
        }
        this.position = JDBCUtils.safeGetInt(dbResult,
                (parent == null) ?
                        "PARTITION_ORDINAL_POSITION" :
                        "SUBPARTITION_ORDINAL_POSITION");
        this.method = JDBCUtils.safeGetString(dbResult,
                (parent == null) ?
                        "PARTITION_METHOD" :
                        "SUBPARTITION_METHOD");
        this.expression = JDBCUtils.safeGetString(dbResult,
                (parent == null) ?
                        "PARTITION_EXPRESSION" :
                        "SUBPARTITION_EXPRESSION");
        this.description = JDBCUtils.safeGetString(dbResult, "PARTITION_DESCRIPTION");
        this.tableRows = JDBCUtils.safeGetLong(dbResult, "ROWS");
        this.avgRowLength = JDBCUtils.safeGetLong(dbResult, "AVG_ROW_LENGTH");
        this.dataLength = JDBCUtils.safeGetLong(dbResult, "DATA_LENGTH");
        this.maxDataLength = JDBCUtils.safeGetLong(dbResult, "MAX_DATA_LENGTH");
        this.indexLength = JDBCUtils.safeGetLong(dbResult, "INDEX_LENGTH");
        this.dataFree = JDBCUtils.safeGetLong(dbResult, "DATA_FREE");
        this.createTime = JDBCUtils.safeGetTimestamp(dbResult, "CREATE_TIME");
        this.updateTime = JDBCUtils.safeGetTimestamp(dbResult, "UPDATE_TIME");
        this.checkTime = JDBCUtils.safeGetTimestamp(dbResult, "CHECK_TIME");
        this.checksum = JDBCUtils.safeGetLong(dbResult, "CHECKSUM");
        this.comment = JDBCUtils.safeGetString(dbResult, "PARTITION_COMMENT");
        this.nodegroup = JDBCUtils.safeGetString(dbResult, "NODEGROUP");
    }


    public GBase8aPartition(GBase8aTable table, GBase8aPartition parent, String name) {
        super(table, name, false);
        this.parent = parent;
    }


    protected GBase8aPartition(DBRProgressMonitor monitor, GBase8aTable table, GBase8aPartition source) {
        super(table, source.getName(), false);
        this.position = source.position;
        this.method = source.method;
        this.expression = source.expression;
        this.description = source.description;
        this.comment = source.comment;
        this.nodegroup = source.nodegroup;
    }


    public void addSubPartitions(GBase8aPartition partition) {
        if (this.subPartitions == null) {
            this.subPartitions = new ArrayList<GBase8aPartition>();
        }
        this.subPartitions.add(partition);
    }


    public GBase8aPartition getParent() {
        return this.parent;
    }


    public List<GBase8aPartition> getSubPartitions() {
        return this.subPartitions;
    }

    public void setSubPartitions(List<GBase8aPartition> subPartitions) {
        this.subPartitions = subPartitions;
    }


    @NotNull
    public GBase8aDataSource getDataSource() {
        return (GBase8aDataSource) ((GBase8aTable) getTable()).getDataSource();
    }


    @Property(viewable = true, order = 3)
    @NotNull
    public String getName() {
        if (this.parent == null) {
            return super.getName();
        }
        return this.parent.getName();
    }


    @Property(viewable = true, order = 4)
    public String getMethod() {
        if (this.parent == null) {
            return this.method;
        }
        return this.parent.getMethod();
    }


    @Property(viewable = true, order = 5)
    public String getExpression() {
        if (this.parent == null) {
            return this.expression;
        }
        return this.parent.getExpression();
    }


    @Property(viewable = true, order = 6)
    @Nullable
    public String getDescription() {
        if (this.parent == null) {
            return this.description;
        }
        return this.parent.getDescription();
    }


    @Property(viewable = true, order = 7)
    public int getPosition() {
        if (this.parent == null) {
            return this.position;
        }
        return this.parent.getPosition();
    }


    @Property(viewable = true, order = 8)
    public String getSubName() {
        if (this.parent == null) {
            return null;
        }
        return super.getName();
    }


    @Property(viewable = true, order = 9)
    public String getSubMethod() {
        if (this.parent == null) {
            return null;
        }
        return this.method;
    }


    @Property(viewable = true, order = 10)
    public String getSubExpression() {
        if (this.parent == null) {
            return null;
        }
        return this.expression;
    }


    @Property(viewable = true, order = 11)
    public String getSubDescription() {
        if (this.parent == null) {
            return null;
        }
        return this.description;
    }


    @Property(viewable = true, order = 12)
    public int getSubPosition() {
        if (this.parent == null) {
            return 0;
        }
        return this.position;
    }


    @Property(viewable = true, order = 20)
    public long getTableRows() {
        return this.tableRows;
    }


    @Property(viewable = true, order = 21)
    public long getAvgRowLength() {
        return this.avgRowLength;
    }


    @Property(viewable = true, order = 22)
    public long getDataLength() {
        return this.dataLength;
    }


    @Property(viewable = true, order = 23)
    public long getMaxDataLength() {
        return this.maxDataLength;
    }


    @Property(viewable = true, order = 24)
    public long getIndexLength() {
        return this.indexLength;
    }


    @Property(viewable = true, order = 25)
    public long getDataFree() {
        return this.dataFree;
    }


    @Property(viewable = false, order = 26)
    public Date getCreateTime() {
        return this.createTime;
    }


    @Property(viewable = false, order = 27)
    public Date getUpdateTime() {
        return this.updateTime;
    }


    @Property(viewable = false, order = 28)
    public Date getCheckTime() {
        return this.checkTime;
    }


    @Property(viewable = true, order = 29)
    public long getChecksum() {
        return this.checksum;
    }


    @Property(viewable = true, order = 30)
    public String getComment() {
        return this.comment;
    }


    @Property(viewable = true, order = 31)
    public String getNodegroup() {
        return this.nodegroup;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public void setParent(GBase8aPartition parent) {
        this.parent = parent;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTableRows(long tableRows) {
        this.tableRows = tableRows;
    }

    public void setAvgRowLength(long avgRowLength) {
        this.avgRowLength = avgRowLength;
    }

    public void setDataLength(long dataLength) {
        this.dataLength = dataLength;
    }

    public void setMaxDataLength(long maxDataLength) {
        this.maxDataLength = maxDataLength;
    }

    public void setIndexLength(long indexLength) {
        this.indexLength = indexLength;
    }

    public void setDataFree(long dataFree) {
        this.dataFree = dataFree;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public void setCheckTime(Date checkTime) {
        this.checkTime = checkTime;
    }

    public void setChecksum(long checksum) {
        this.checksum = checksum;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setNodegroup(String nodegroup) {
        this.nodegroup = nodegroup;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }


    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof GBase8aPartition) {
            GBase8aPartition partition = (GBase8aPartition) obj;
            if (partition.getName() == null || partition.getName().isEmpty() || !partition.getName().equalsIgnoreCase(getName())) {
                return false;
            }
            if (("RANGE".equalsIgnoreCase(partition.getMethod()) || "LIST".equalsIgnoreCase(partition.getMethod())) && (
                    partition.getDescription() == null || partition.getDescription().isEmpty() || !partition.getDescription().equalsIgnoreCase(this.description))) {
                return false;
            }
        }

        return true;
    }
}
