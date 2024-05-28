package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.struct.AbstractTrigger;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSActionTiming;
import org.jkiss.dbeaver.model.struct.rdb.DBSManipulationType;

import java.sql.ResultSet;
import java.util.Map;


public class GBase8aTrigger extends AbstractTrigger implements GBase8aSourceObject {

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


    public String getBody() {
        return this.body;
    }

    public GBase8aCatalog getCatalog() {
        return this.catalog;
    }


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
    public GBase8aDataSource getDataSource() {
        return this.catalog.getDataSource();
    }


    public void setObjectDefinitionText(String sourceText) {
        this.body = sourceText;
    }


    public String getFullyQualifiedName(DBPEvaluationContext context) {
        return DBUtils.getFullQualifiedName(getDataSource(), this.catalog,
                this);
    }

    @Override
    public String getObjectDefinitionText(DBRProgressMonitor monitor, Map<String, Object> options) throws DBException {
        return getBody();
    }
}