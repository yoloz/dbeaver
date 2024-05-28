package org.jkiss.dbeaver.ext.gbase8a.model.plan;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.exec.plan.DBCPlan;
import org.jkiss.dbeaver.model.exec.plan.DBCPlanStyle;
import org.jkiss.dbeaver.model.exec.plan.DBCQueryPlanner;
import org.jkiss.dbeaver.model.exec.plan.DBCQueryPlannerConfiguration;
import org.jkiss.dbeaver.model.impl.plan.AbstractExecutionPlanSerializer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

/**
 * @author yolo
 */
public class GBase8aPlanAnalyser  implements DBCQueryPlanner {

    private GBase8aDataSource dataSource;

    public GBase8aPlanAnalyser(GBase8aDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public DBPDataSource getDataSource() {
        return dataSource;
    }

    @Override
    public DBCPlan planQueryExecution(DBCSession session, String query, DBCQueryPlannerConfiguration configuration) throws DBException {
        GBase8aPlan plan = new GBase8aPlan(this.dataSource, query);
        plan.explain(session);
        return plan;
    }

    @Override
    public DBCPlanStyle getPlanStyle() {
        return DBCPlanStyle.PLAN;
    }

}
