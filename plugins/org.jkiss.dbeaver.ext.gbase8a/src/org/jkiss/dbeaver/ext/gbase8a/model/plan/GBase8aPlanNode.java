package org.jkiss.dbeaver.ext.gbase8a.model.plan;

import org.jkiss.dbeaver.model.exec.plan.DBCPlanNode;
import org.jkiss.dbeaver.model.exec.plan.DBCPlanNodeKind;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.plan.AbstractExecutionPlanNode;
import org.jkiss.dbeaver.model.meta.Property;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


public class GBase8aPlanNode extends AbstractExecutionPlanNode implements DBCPlanNode {

    private long id;
    private String selectType;
    private String table;
    private String type;
    private String possibleKeys;
    private String key;
    private String keyLength;
    private String ref;
    private long rowCount;
    private double filtered;
    private String extra;
    private GBase8aPlanNode parent;
    private List<GBase8aPlanNode> nested;

    public GBase8aPlanNode(GBase8aPlanNode parent, ResultSet dbResult) throws SQLException {
        this.parent = parent;
        this.id = JDBCUtils.safeGetLong(dbResult, "id");
        this.selectType = JDBCUtils.safeGetString(dbResult, "select_type");
        this.table = JDBCUtils.safeGetString(dbResult, "table");
        this.type = JDBCUtils.safeGetString(dbResult, "type");
        this.possibleKeys = JDBCUtils.safeGetString(dbResult, "possible_keys");
        this.key = JDBCUtils.safeGetString(dbResult, "key");
        this.keyLength = JDBCUtils.safeGetString(dbResult, "key_len");
        this.ref = JDBCUtils.safeGetString(dbResult, "ref");
        this.rowCount = JDBCUtils.safeGetLong(dbResult, "rows");
        this.filtered = JDBCUtils.safeGetDouble(dbResult, "filtered");
        this.extra = JDBCUtils.safeGetString(dbResult, "extra");
    }


    @Override
    public DBCPlanNodeKind getNodeKind() {
        if ("SIMPLE".equals(selectType)) {
            return DBCPlanNodeKind.SELECT;
        } else if ("JOIN".equals(selectType)) {
            return DBCPlanNodeKind.JOIN;
        } else if ("UNION".equals(selectType)) {
            return DBCPlanNodeKind.UNION;
        }
        return super.getNodeKind();
    }

    @Override
    public String getNodeName() {
        return table;
    }

    @Override
    public String getNodeType() {
        return selectType;
    }

    @Override
    public String getNodeCondition() {
        return "";
    }

    @Override
    public String getNodeDescription() {
        return ref;
    }

    public DBCPlanNode getParent() {
        return this.parent;
    }


    public List<GBase8aPlanNode> getNested() {
        return this.nested;
    }


    @Property(order = 0, viewable = true)
    public long getId() {
        return this.id;
    }


    @Property(order = 1, viewable = true)
    public String getSelectType() {
        return this.selectType;
    }


    @Property(order = 2, viewable = true)
    public String getTable() {
        return this.table;
    }


    @Property(order = 3, viewable = true)
    public String getType() {
        return this.type;
    }


    @Property(order = 4, viewable = true)
    public String getPossibleKeys() {
        return this.possibleKeys;
    }


    @Property(order = 5, viewable = true)
    public String getKey() {
        return this.key;
    }


    @Property(order = 6, viewable = true)
    public String getKeyLength() {
        return this.keyLength;
    }


    @Property(order = 7, viewable = true)
    public String getRef() {
        return this.ref;
    }


    @Property(order = 8, viewable = true)
    public long getRowCount() {
        return this.rowCount;
    }


    @Property(order = 9, viewable = true)
    public double getFiltered() {
        return this.filtered;
    }


    @Property(order = 10, viewable = true)
    public String getExtra() {
        return this.extra;
    }


    public String toString() {
        return String.valueOf(this.table) + " " + this.type + " " + this.key;
    }


    public String getValue() {
        return null;
    }
}
