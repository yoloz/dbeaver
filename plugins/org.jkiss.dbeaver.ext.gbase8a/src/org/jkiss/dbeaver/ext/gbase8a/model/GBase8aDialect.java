package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aConstants;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCDatabaseMetaData;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCDataSource;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCSQLDialect;
import org.jkiss.dbeaver.model.impl.sql.BasicSQLDialect;
import org.jkiss.dbeaver.model.sql.SQLConstants;
import org.jkiss.dbeaver.model.sql.SQLDialectDDLExtension;
import org.jkiss.dbeaver.model.sql.SQLDialectSchemaController;
import org.jkiss.utils.ArrayUtils;


/**
 * GBase8a dialect
 */
public class GBase8aDialect extends JDBCSQLDialect implements SQLDialectSchemaController, SQLDialectDDLExtension {

    public static final String[] GBASE8A_NON_TRANSACTIONAL_KEYWORDS = (String[]) ArrayUtils.concatArrays(
            BasicSQLDialect.NON_TRANSACTIONAL_KEYWORDS, (Object[]) new String[]{
                    "USE", "SHOW", "CREATE", "ALTER", "DROP",
                    "EXPLAIN", "DESCRIBE", "DESC"
            });

    private int lowerCaseTableNames;

    public GBase8aDialect() {
        super("GBASE8A", "gbase8a");
    }

    public GBase8aDialect(String name, String id) {
        super(name, id);
    }

    public void initBaseDriverSettings(JDBCSession session, JDBCDataSource dataSource, JDBCDatabaseMetaData metaData) {
        super.initDriverSettings(session, dataSource, metaData);
        addTableQueryKeywords(SQLConstants.KEYWORD_EXPLAIN, "EXPLAIN", "DESCRIBE", "DESC");
//        Collections.addAll(this.tableQueryWords, new String[]{"EXPLAIN", "DESCRIBE", "DESC"});
    }

    @Override
    public void initDriverSettings(JDBCSession session, JDBCDataSource dataSource, JDBCDatabaseMetaData metaData) {
        initBaseDriverSettings(session, dataSource, metaData);

    }

    @Override
    public void afterDataSourceInitialization(@NotNull DBPDataSource dataSource) {
        this.lowerCaseTableNames = ((GBase8aDataSource) dataSource).getLowerCaseTableNames();
        this.setSupportsUnquotedMixedCase(lowerCaseTableNames != 2);
    }

    @Override
    public boolean useCaseInsensitiveNameLookup() {
        return lowerCaseTableNames != 0;
    }

    @Nullable
    public String getScriptDelimiterRedefiner() {
        return "DELIMITER";
    }

    public boolean supportsAliasInSelect() {
        return true;
    }


    public boolean supportsCommentQuery() {
        return true;
    }


    public String[] getSingleLineComments() {
        return new String[]{"-- ", "#"};
    }


    public String getTestSQL() {
        return "SELECT 1";
    }

    @NotNull
    public String[] getNonTransactionKeywords() {
        return GBASE8A_NON_TRANSACTIONAL_KEYWORDS;
    }

    @Override
    public String getAutoIncrementKeyword() {
        return "AUTO_INCREMENT";
    }

    @Override
    public boolean supportsCreateIfExists() {
        return false;
    }

    @Override
    public String getTimestampDataType() {
        return "TIMESTAMP";
    }

    @Override
    public String getBigIntegerType() {
        return "BIGINT";
    }

    @Override
    public String getClobDataType() {
        return "TEXT";
    }

    @Override
    public String getBlobDataType() {
        return "BLOB";
    }

    @Override
    public String getUuidDataType() {
        return "CHAR(36)";
    }

    @Override
    public String getBooleanDataType() {
        return "TINYINT(1)";
    }

    @Override
    public String getAlterColumnOperation() {
        return "MODIFY";
    }

    @Override
    public boolean supportsNoActionIndex() {
        return true;
    }

    @Override
    public boolean supportsAlterColumnSet() {
        return true;
    }

    @Override
    public boolean supportsAlterHasColumn() {
        return true;
    }

    @Override
    public String getSchemaExistQuery(String schemaName) {
        // and VC_NAME=?
        return "SELECT 1 FROM "
                + GBase8aConstants.META_TABLE_SCHEMATA
                + " WHERE "
                + GBase8aConstants.COL_SCHEMA_NAME
                + "='"
                + schemaName
                + "'";
    }

    @Override
    public String getCreateSchemaQuery(String schemaName) {
        return "CREATE DATABASE "
                + schemaName;
    }
}
