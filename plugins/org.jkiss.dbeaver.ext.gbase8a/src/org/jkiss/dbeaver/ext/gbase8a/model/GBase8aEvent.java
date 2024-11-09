package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBConstants;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBPQualifiedObject;
import org.jkiss.dbeaver.model.DBPSaveableObject;
import org.jkiss.dbeaver.model.DBPScriptObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.meta.PropertyLength;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.sql.SQLUtils;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.utils.CommonUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


public class GBase8aEvent implements GBase8aSourceObject, DBPSaveableObject, DBPQualifiedObject {

    private static final String CAT_MAIN = "Main";
    private static final String CAT_DETAILS = "Details";
    private static final String CAT_STATS = "Statistics";

    private GBase8aCatalog catalog;
    private boolean persisted;
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

    private transient String eventFullDefinitionText;

    public GBase8aEvent(GBase8aCatalog catalog, ResultSet dbResult) throws SQLException {
        this.catalog =catalog;
        this.persisted = true;
        loadInfo(dbResult);
    }
    public GBase8aEvent(GBase8aCatalog catalog, String name) {
        this.catalog = catalog;
        this.name = name;
        this.persisted = false;
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

    @Override
    public GBase8aDataSource getDataSource() {
        return catalog.getDataSource();
    }

    @Override
    public DBSObject getParentObject() {
        return catalog;
    }


    @NotNull
    @Override
    @Property(viewable = true, order = 1)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isPersisted() {
        return persisted;
    }

    @Override
    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }

    @Nullable
    @Override
    @Property(viewable = true, length = PropertyLength.MULTILINE, category = CAT_DETAILS, order = 100)
    public String getDescription() {
        return this.eventComment;
    }

    @Property(viewable = true, order = 10)
    public String getEventType() {
        return this.eventType;
    }

    @Property(viewable = true, order = 11)
    public Date getExecuteAt() {
        return this.executeAt;
    }

    @Property(viewable = true, order = 12)
    public String getIntervalValue() {
        return this.intervalValue;
    }

    @Property(viewable = true, order = 13)
    public String getIntervalField() {
        return this.intervalField;
    }

    @Property(viewable = true, category = CAT_DETAILS, order = 14)
    public String getEventBody() {
        return this.eventBody;
    }

    @Property(category = CAT_DETAILS, order = 30)
    public String getDefiner() {
        return this.definer;
    }

    @Property(category = CAT_DETAILS, order = 31)
    public String getTimeZone() {
        return this.timeZone;
    }

    @Property(category = CAT_DETAILS, order = 32)
    public String getSqlMode() {
        return this.sqlMode;
    }

    @Property(category = CAT_DETAILS, order = 33)
    public Date getStarts() {
        return this.starts;
    }

    @Property(category = CAT_DETAILS, order = 34)
    public Date getEnds() {
        return this.ends;
    }

    @Property(category = DBConstants.CAT_STATISTICS, order = 35)
    public String getStatus() {
        return this.status;
    }

    @Property(category = CAT_DETAILS, order = 36)
    public String getOnCompletion() {
        return this.onCompletion;
    }

    @Property(category = DBConstants.CAT_STATISTICS, order = 37)
    public Date getCreated() {
        return this.created;
    }

    @Property(category = DBConstants.CAT_STATISTICS, order = 38)
    public Date getLastAltered() {
        return this.lastAltered;
    }

    @Property(category = DBConstants.CAT_STATISTICS, order = 39)
    public Date getLastExecuted() {
        return this.lastExecuted;
    }

    @Property(category = CAT_DETAILS, order = 40)
    public long getOriginator() {
        return this.originator;
    }

    @Property(category = CAT_DETAILS, order = 41)
    public GBase8aCharset getCharacterSetClient() {
        return this.characterSetClient;
    }

    @Property(category = CAT_DETAILS, order = 42)
    public GBase8aCollation getCollationConnection() {
        return this.collationConnection;
    }

    @Property(category = CAT_DETAILS, order = 43)
    public GBase8aCollation getDatabaseCollation() {
        return this.databaseCollation;
    }

    @Override
    @Property(hidden = true, editable = true, updatable = true, order = -1)
    public String getObjectDefinitionText(DBRProgressMonitor monitor, Map<String, Object> options) throws DBException {
        if (eventFullDefinitionText != null) {
            return eventFullDefinitionText;
        }
        DateFormat dateFormat = new SimpleDateFormat(DBConstants.DEFAULT_TIMESTAMP_FORMAT);
        StringBuilder sql = new StringBuilder();
        sql.append(CommonUtils.getOption(options, DBPScriptObject.OPTION_OBJECT_ALTER) ? "ALTER" : "CREATE");
        sql.append(" EVENT ").append(DBUtils.getQuotedIdentifier(this)).append("\n");
        if (intervalValue != null && intervalField != null) {
            sql.append("ON SCHEDULE EVERY ").append(intervalValue).append(" ").append(intervalField).append("\n");
        } else if (executeAt != null) {
            sql.append("ON SCHEDULE AT '").append(dateFormat.format(executeAt)).append("'\n");
        } else {
            sql.append("ON SCHEDULE AT CURRENT_TIMESTAMP\n");
        }
        if (starts != null) {
            sql.append("STARTS '").append(dateFormat.format(starts)).append("'\n");
        }
        if (ends != null) {
            sql.append("ENDS '").append(dateFormat.format(ends)).append("'\n");
        }
        if (!CommonUtils.isEmpty(onCompletion)) {
            sql.append("ON COMPLETION ").append(onCompletion).append("\n");
        }
        sql.append(
                "ENABLED".equals(status) ? "ENABLE" :
                        "DISABLED".equals(status) ? "DISABLE" : "DISABLE ON SLAVE"
        ).append("\n");

        if (!CommonUtils.isEmpty(eventComment)) {
            sql.append("COMMENT '").append(SQLUtils.escapeString(getDataSource(), eventComment)).append("'\n");
        }
        sql.append("DO ").append(eventDefinition);
        return sql.toString();
    }

    public void setObjectDefinitionText(String sourceText) throws DBException {
        this.eventFullDefinitionText = sourceText;
    }

    public void setEventDefinition(String eventDefinition) {
        this.eventDefinition = eventDefinition;
    }

    public GBase8aCatalog getCatalog() {
        return catalog;
    }

    @NotNull
    @Override
    public String getFullyQualifiedName(DBPEvaluationContext context) {
        return DBUtils.getFullQualifiedName(getDataSource(),
                catalog,
                this);
    }

}
