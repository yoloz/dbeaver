package org.jkiss.dbeaver.ext.gbase8a.model.plan;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.plan.DBCPlanNode;
import org.jkiss.dbeaver.model.impl.plan.AbstractExecutionPlan;
import org.jkiss.dbeaver.model.sql.SQLUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class GBase8aPlan extends AbstractExecutionPlan {

    private final GBase8aDataSource dataSource;
    private final String query;
    private List<DBCPlanNode> rootNodes;

    public GBase8aPlan(GBase8aDataSource dataSource, String query) throws DBCException {
        this.dataSource = dataSource;
        this.query = query;
    }

    public String getQueryString() {
        return this.query;
    }

    @Override
    public String getPlanQueryString() throws DBException {
        return "EXPLAIN EXTENDED " + query;
    }

    @Override
    public Object getPlanFeature(String feature) {
        return super.getPlanFeature(feature);
    }

    @Override
    public List<? extends DBCPlanNode> getPlanNodes(Map<String, Object> options) {
        return this.rootNodes;
    }

    public void explain(DBCSession session) throws DBCException {
        String plainQuery = SQLUtils.stripComments(SQLUtils.getDialectFromObject(dataSource), this.query).toUpperCase();
        if (!plainQuery.startsWith("SELECT")) {
            throw new DBCException("Only SELECT statements could produce execution plan");
        }
        if (plainQuery.contains(";")) {
            throw new DBCException("Please select one sql.");
        }
        JDBCSession connection = (JDBCSession) session;
        try (JDBCPreparedStatement dbStat = connection.prepareStatement(getPlanQueryString());
             JDBCResultSet dbResult = dbStat.executeQuery()) {
            this.rootNodes = new ArrayList<>();
            while (dbResult.next()) {
                GBase8aPlanNode node = new GBase8aPlanNode(null, dbResult);
                this.rootNodes.add(node);
            }
        } catch (SQLException | DBException e) {
            throw new DBCException("", e);
        }
    }
}
