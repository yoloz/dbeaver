package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;


public class GBase8aEvent extends GBase8aInformation implements GBase8aSourceObject {
    private static final String CAT_MAIN = "Main";
    private static final String CAT_DETAILS = "Details";
    private static final String CAT_STATS = "Statistics";
    private String name;
    private String definer;
    private String timeZone;
    private String eventBody;
    private String eventDefinition;
    private String eventType;
    private Date executeAt;
    private String intervalValue;
    private String intervalField;
    private String sqlMode;
    private Date starts;
    private Date ends;
    private String status;
    private String onCompletion;
    private Date created;
    private Date lastAltered;
    private Date lastExecuted;
    private String eventComment;
    private long originator;
    private GBase8aCharset characterSetClient;
    private GBase8aCollation collationConnection;
    private GBase8aCollation databaseCollation;

    public GBase8aEvent(GBase8aCatalog catalog, ResultSet dbResult) throws SQLException {
        super(catalog.getDataSource());
        loadInfo(dbResult);
    }


    private void loadInfo(ResultSet dbResult) throws SQLException {
        this.name = JDBCUtils.safeGetString(dbResult, "EVENT_NAME");
        this.definer = JDBCUtils.safeGetString(dbResult, "DEFINER");
        this.timeZone = JDBCUtils.safeGetString(dbResult, "TIME_ZONE");
        this.eventBody = JDBCUtils.safeGetString(dbResult, "EVENT_BODY");
        this.eventDefinition = JDBCUtils.safeGetString(dbResult, "EVENT_DEFINITION");
        this.eventType = JDBCUtils.safeGetString(dbResult, "EVENT_TYPE");
        this.executeAt = JDBCUtils.safeGetTimestamp(dbResult, "EXECUTE_AT");
        this.intervalValue = JDBCUtils.safeGetString(dbResult, "INTERVAL_VALUE");
        this.intervalField = JDBCUtils.safeGetString(dbResult, "INTERVAL_FIELD");
        this.sqlMode = JDBCUtils.safeGetString(dbResult, "SQL_MODE");
        this.starts = JDBCUtils.safeGetTimestamp(dbResult, "STARTS");
        this.ends = JDBCUtils.safeGetTimestamp(dbResult, "ENDS");
        this.status = JDBCUtils.safeGetString(dbResult, "STATUS");
        this.onCompletion = JDBCUtils.safeGetString(dbResult, "ON_COMPLETION");
        this.created = JDBCUtils.safeGetTimestamp(dbResult, "CREATED");
        this.lastAltered = JDBCUtils.safeGetTimestamp(dbResult, "LAST_ALTERED");
        this.lastExecuted = JDBCUtils.safeGetTimestamp(dbResult, "LAST_EXECUTED");
        this.eventComment = JDBCUtils.safeGetString(dbResult, "EVENT_COMMENT");
        this.originator = JDBCUtils.safeGetLong(dbResult, "ORIGINATOR");
        this.characterSetClient = getDataSource().getCharset(JDBCUtils.safeGetString(dbResult, "CHARACTER_SET_CLIENT"));
        this.collationConnection = getDataSource().getCollation(JDBCUtils.safeGetString(dbResult, "COLLATION_CONNECTION"));
        this.databaseCollation = getDataSource().getCollation(JDBCUtils.safeGetString(dbResult, "DATABASE_COLLATION"));
    }


    @Property(viewable = true, category = "Main", order = 1)
    @NotNull
    public String getName() {
        return this.name;
    }


    @Property(viewable = true, category = "Details", order = 100)
    @Nullable
    public String getDescription() {
        return this.eventComment;
    }

    @Property(viewable = true, category = "Main", order = 10)
    public String getEventType() {
        return this.eventType;
    }

    @Property(viewable = true, category = "Main", order = 11)
    public Date getExecuteAt() {
        return this.executeAt;
    }

    @Property(viewable = true, category = "Main", order = 12)
    public String getIntervalValue() {
        return this.intervalValue;
    }

    @Property(viewable = true, category = "Main", order = 13)
    public String getIntervalField() {
        return this.intervalField;
    }

    @Property(viewable = true, category = "Details", order = 14)
    public String getEventBody() {
        return this.eventBody;
    }

    @Property(category = "Details", order = 30)
    public String getDefiner() {
        return this.definer;
    }

    @Property(category = "Details", order = 31)
    public String getTimeZone() {
        return this.timeZone;
    }

    @Property(category = "Details", order = 32)
    public String getSqlMode() {
        return this.sqlMode;
    }

    @Property(category = "Details", order = 33)
    public Date getStarts() {
        return this.starts;
    }

    @Property(category = "Details", order = 34)
    public Date getEnds() {
        return this.ends;
    }

    @Property(category = "Statistics", order = 35)
    public String getStatus() {
        return this.status;
    }

    @Property(category = "Details", order = 36)
    public String getOnCompletion() {
        return this.onCompletion;
    }

    @Property(category = "Statistics", order = 37)
    public Date getCreated() {
        return this.created;
    }

    @Property(category = "Statistics", order = 38)
    public Date getLastAltered() {
        return this.lastAltered;
    }

    @Property(category = "Statistics", order = 39)
    public Date getLastExecuted() {
        return this.lastExecuted;
    }

    @Property(category = "Details", order = 40)
    public long getOriginator() {
        return this.originator;
    }

    @Property(category = "Details", order = 41)
    public GBase8aCharset getCharacterSetClient() {
        return this.characterSetClient;
    }

    @Property(category = "Details", order = 42)
    public GBase8aCollation getCollationConnection() {
        return this.collationConnection;
    }

    @Property(category = "Details", order = 43)
    public GBase8aCollation getDatabaseCollation() {
        return this.databaseCollation;
    }

    public void setObjectDefinitionText(String sourceText) throws DBException {
        this.eventDefinition = sourceText;
    }

    @Override
    public String getObjectDefinitionText(DBRProgressMonitor monitor, Map<String, Object> options) throws DBException {
        return this.eventDefinition;
    }
}
