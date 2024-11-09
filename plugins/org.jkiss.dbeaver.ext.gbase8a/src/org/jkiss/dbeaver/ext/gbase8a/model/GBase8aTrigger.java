package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBDatabaseException;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBPRefreshableObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.struct.AbstractTrigger;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSActionTiming;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.rdb.DBSManipulationType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;


public class GBase8aTrigger extends AbstractTrigger implements GBase8aSourceObject, DBPRefreshableObject {

    private final GBase8aCatalog catalog;
    private final GBase8aTable table;
    private String body;
    private String charsetClient;
    private String sqlMode;

    public GBase8aTrigger(GBase8aCatalog catalog, GBase8aTable table, ResultSet dbResult) {
        super(JDBCUtils.safeGetString(dbResult, "Trigger"), null, true);
        this.catalog = catalog;
        this.table = table;

        setManipulationType(DBSManipulationType.getByName(JDBCUtils.safeGetString(dbResult, "Event")));
        setActionTiming(DBSActionTiming.getByName(JDBCUtils.safeGetString(dbResult, "Timing")));
        this.body = JDBCUtils.safeGetString(dbResult, "Statement");
        this.charsetClient = JDBCUtils.safeGetString(dbResult, "CHARACTER_SET_CLIENT");
        this.sqlMode = JDBCUtils.safeGetString(dbResult, "SQL_MODE");
    }


    public GBase8aTrigger(GBase8aCatalog catalog, GBase8aTable table, String name) {
        super(name, null, false);
        this.catalog = catalog;
        this.table = table;

        setActionTiming(DBSActionTiming.AFTER);
        setManipulationType(DBSManipulationType.INSERT);
        this.body = "";
    }

    public GBase8aTrigger(GBase8aCatalog catalog, GBase8aTable table, GBase8aTrigger source) {
        super(source.name, source.getDescription(), false);
        this.catalog = catalog;
        this.table = table;
        this.body = source.body;
        this.charsetClient = source.charsetClient;
        this.sqlMode = source.sqlMode;
    }

    @Property(viewable = true, order = 2, listProvider = TriggerTimingListProvider.class)
    public DBSActionTiming getActionTiming() {
        return super.getActionTiming();
    }

    @Property(viewable = true, order = 3, listProvider = TriggerTypeListProvider.class)
    public DBSManipulationType getManipulationType() {
        return super.getManipulationType();
    }

    public String getBody() {
        return this.body;
    }

    public GBase8aCatalog getCatalog() {
        return this.catalog;
    }

    @Override
    @Property(viewable = true, order = 4)
    public GBase8aTable getTable() {
        return this.table;
    }

    @Property(order = 5)
    public String getCharsetClient() {
        return this.charsetClient;
    }

    @Property(order = 6)
    public String getSqlMode() {
        return this.sqlMode;
    }

    public GBase8aTable getParentObject() {
        return this.table;
    }

    @NotNull
    @Override
    public GBase8aDataSource getDataSource() {
        return this.catalog.getDataSource();
    }

    @Override
    public void setObjectDefinitionText(String sourceText) {
        this.body = sourceText;
    }

    @Override
    public String getFullyQualifiedName(DBPEvaluationContext context) {
        return DBUtils.getFullQualifiedName(getDataSource(), this.catalog, this);
    }

    @Override
    @Property(hidden = true, editable = true, updatable = true, order = -1)
    public String getObjectDefinitionText(DBRProgressMonitor monitor, Map<String, Object> options) throws DBException {
        if (body == null) {
            try (JDBCSession session = DBUtils.openMetaSession(monitor, this, "Read trigger declaration");
                 JDBCPreparedStatement dbStat = session.prepareStatement("SHOW CREATE TRIGGER "
                         + getFullyQualifiedName(DBPEvaluationContext.DDL));
                 JDBCResultSet dbResult = dbStat.executeQuery()) {
                if (dbResult.next()) {
                    body = JDBCUtils.safeGetString(dbResult, "SQL Original Statement");
                } else {
                    body = "-- Trigger definition not found in catalog";
                }
            } catch (SQLException e) {
                body = "-- " + e.getMessage();
                throw new DBDatabaseException(e, getDataSource());
            }
        }
        return body;
    }

    @Override
    public DBSObject refreshObject(@NotNull DBRProgressMonitor monitor) throws DBException {
        return getCatalog().getTriggerCache().refreshObject(monitor, getCatalog(), this);
    }
}