package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aConstants;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCDataSource;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCStructureAssistant;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.struct.AbstractObjectReference;
import org.jkiss.dbeaver.model.impl.struct.RelationalObjectType;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSObjectReference;
import org.jkiss.dbeaver.model.struct.DBSObjectType;
import org.jkiss.utils.CommonUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;


public class GBase8aStructureAssistant extends JDBCStructureAssistant<GBase8aExecutionContext> {

    private final GBase8aDataSource dataSource;

    public GBase8aStructureAssistant(GBase8aDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected JDBCDataSource getDataSource() {
        return this.dataSource;
    }

    @NotNull
    @Override
    public DBSObjectType[] getSupportedObjectTypes() {
        return new DBSObjectType[]{
                RelationalObjectType.TYPE_TABLE,
                RelationalObjectType.TYPE_CONSTRAINT,
                RelationalObjectType.TYPE_PROCEDURE,
                RelationalObjectType.TYPE_TABLE_COLUMN
        };
    }

    @NotNull
    @Override
    public DBSObjectType[] getHyperlinkObjectTypes() {
        return new DBSObjectType[]{
                RelationalObjectType.TYPE_TABLE,
                RelationalObjectType.TYPE_PROCEDURE
        };
    }

    @NotNull
    @Override
    public DBSObjectType[] getAutoCompleteObjectTypes() {
        return new DBSObjectType[]{
                RelationalObjectType.TYPE_TABLE,
                RelationalObjectType.TYPE_PROCEDURE
        };
    }

    @Override
    protected void findObjectsByMask(@NotNull GBase8aExecutionContext executionContext, @NotNull JDBCSession session,
                                     @NotNull DBSObjectType objectType, @NotNull ObjectsSearchParams params,
                                     @NotNull List<DBSObjectReference> references) throws DBException, SQLException {
        GBase8aCatalog catalog = (objectType instanceof GBase8aCatalog) ? (GBase8aCatalog) objectType : null;
        if (catalog == null) {
            catalog = executionContext.getContextDefaults().getDefaultCatalog();//this.dataSource.getDefaultObject();
        }
        if (objectType == RelationalObjectType.TYPE_TABLE) {
            findTablesByMask(session, catalog, params, references);
        } else if (objectType == RelationalObjectType.TYPE_CONSTRAINT) {
            findConstraintsByMask(session, catalog, params, references);
        } else if (objectType == RelationalObjectType.TYPE_PROCEDURE) {
            findProceduresByMask(session, catalog, params, references);
        } else if (objectType == RelationalObjectType.TYPE_TABLE_COLUMN) {
            findTableColumnsByMask(session, catalog, params, references);
        }
    }

    private void findTablesByMask(JDBCSession session, @Nullable final GBase8aCatalog catalog, @NotNull ObjectsSearchParams params,
                                  List<DBSObjectReference> objects) throws SQLException, DBException {
        DBRProgressMonitor monitor = session.getProgressMonitor();
        QueryParams queryParams = new QueryParams(
                GBase8aConstants.COL_TABLE_NAME,
                GBase8aConstants.COL_TABLE_SCHEMA + "," + GBase8aConstants.COL_TABLE_NAME,
                GBase8aConstants.META_TABLE_TABLES
        );
        if (params.isSearchInComments()) {
            queryParams.setCommentColumnName("TABLE_COMMENT");
        }
        if (catalog != null) {
            queryParams.setSchemaColumnName(GBase8aConstants.COL_TABLE_SCHEMA);
        }
        queryParams.setMaxResults(params.getMaxResults() - objects.size());
        queryParams.setCaseSensitive(params.isCaseSensitive());
        String sql = generateQuery(queryParams);
        try (JDBCPreparedStatement dbStat = session.prepareStatement(sql)) {
            fillParameters(dbStat, params, catalog, true, false);
            try (JDBCResultSet dbResult = dbStat.executeQuery()) {
                while (dbResult.next() && !monitor.isCanceled()) {
                    final String catalogName = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_TABLE_SCHEMA);
                    final String tableName = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_TABLE_NAME);
                    objects.add(new AbstractObjectReference<DBSObject>(tableName, dataSource.getCatalog(catalogName), null, GBase8aTableBase.class, RelationalObjectType.TYPE_TABLE) {
                        public DBSObject resolveObject(DBRProgressMonitor monitor) throws DBException {
                            if (CommonUtils.isEmpty(tableName)) {
                                throw new DBException("Table name not found in the metadata.");
                            }
                            GBase8aCatalog tableCatalog = (catalog != null) ?
                                    catalog : dataSource.getCatalog(catalogName);
                            if (tableCatalog == null) {
                                throw new DBException("Table catalog '" + catalogName + "' not found");
                            }
                            GBase8aTableBase table = tableCatalog.getTableCache().getObject(monitor, tableCatalog, tableName);
                            if (table == null) {
                                throw new DBException("Table '" + tableName + "' not found in catalog '" + catalogName + "'");
                            }
                            return table;
                        }
                    });
                }
            }
        }
    }


    private void findProceduresByMask(JDBCSession session, @Nullable final GBase8aCatalog catalog, @NotNull ObjectsSearchParams params,
                                      List<DBSObjectReference> objects) throws SQLException, DBException {
        DBRProgressMonitor monitor = session.getProgressMonitor();
//        try (JDBCPreparedStatement dbStat = session.prepareStatement(
//                "SELECT ROUTINE_SCHEMA,ROUTINE_NAME FROM information_schema.ROUTINES WHERE trim(ROUTINE_NAME) LIKE ? "
//                        + ((catalog == null) ? "" : " AND trim(ROUTINE_SCHEMA)=?")
//                        + " ORDER BY ROUTINE_NAME LIMIT "
//                        + maxResults)) {
//            dbStat.setString(1, procNameMask.toLowerCase(Locale.ENGLISH));
//            if (catalog != null) {
//                dbStat.setString(2, catalog.getName());
//            }
        QueryParams queryParams = new QueryParams(
                GBase8aConstants.COL_ROUTINE_NAME,
                GBase8aConstants.COL_ROUTINE_SCHEMA + "," + GBase8aConstants.COL_ROUTINE_NAME,
                GBase8aConstants.META_TABLE_ROUTINES
        );
        if (params.isSearchInComments()) {
            queryParams.setCommentColumnName("ROUTINE_COMMENT");
        }
        if (catalog != null) {
            queryParams.setSchemaColumnName(GBase8aConstants.COL_ROUTINE_SCHEMA);
        }
        queryParams.setMaxResults(params.getMaxResults() - objects.size());
        queryParams.setCaseSensitive(params.isCaseSensitive());
        if (params.isSearchInDefinitions()) {
            queryParams.setDefinitionColumnName(GBase8aConstants.COL_ROUTINE_DEFINITION);
        }
        String sql = generateQuery(queryParams);
        try (JDBCPreparedStatement dbStat = session.prepareStatement(sql)) {
            fillParameters(dbStat, params, catalog, true, true);
            try (JDBCResultSet dbResult = dbStat.executeQuery()) {
                while (dbResult.next() && !monitor.isCanceled()) {
                    final String catalogName = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_ROUTINE_SCHEMA);
                    final String procName = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_ROUTINE_NAME);
                    objects.add(new AbstractObjectReference<>(procName, dataSource.getCatalog(catalogName), null, GBase8aProcedure.class, RelationalObjectType.TYPE_PROCEDURE) {
                        public DBSObject resolveObject(DBRProgressMonitor monitor) throws DBException {
                            GBase8aCatalog procCatalog = (catalog != null) ? catalog : dataSource.getCatalog(catalogName);
                            if (procCatalog == null) {
                                throw new DBException("Procedure catalog '" + catalogName + "' not found");
                            }
                            GBase8aProcedure procedure = procCatalog.getProcedure(monitor, procName);
                            if (procedure == null) {
                                throw new DBException("Procedure '" + procName + "' not found in catalog '" + procCatalog.getName() + "'");
                            }
                            return procedure;
                        }
                    });
                }
            }
        }
    }


    private void findConstraintsByMask(JDBCSession session, @Nullable final GBase8aCatalog catalog, @NotNull ObjectsSearchParams params,
                                       List<DBSObjectReference> objects) throws SQLException, DBException {
        DBRProgressMonitor monitor = session.getProgressMonitor();
//        try (JDBCPreparedStatement dbStat = session.prepareStatement(
//                "SELECT TABLE_SCHEMA,TABLE_NAME,CONSTRAINT_NAME,CONSTRAINT_TYPE FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_NAME LIKE ? "
//                        + ((catalog == null) ? "" : " AND TABLE_SCHEMA=?")
//                        + " ORDER BY CONSTRAINT_NAME LIMIT "
//                        + maxResults)) {
//            dbStat.setString(1, constrNameMask.toLowerCase(Locale.ENGLISH));
//            if (catalog != null) {
//                dbStat.setString(2, catalog.getName());
//            }
        QueryParams queryParams = new QueryParams(
                GBase8aConstants.COL_CONSTRAINT_NAME,
                GBase8aConstants.COL_TABLE_SCHEMA + "," + GBase8aConstants.COL_TABLE_NAME + ","
                        + GBase8aConstants.COL_CONSTRAINT_NAME + "," + GBase8aConstants.COL_CONSTRAINT_TYPE,
                GBase8aConstants.META_TABLE_TABLE_CONSTRAINTS
        );
        if (catalog != null) {
            queryParams.setSchemaColumnName(GBase8aConstants.COL_TABLE_SCHEMA);
        }
        queryParams.setMaxResults(params.getMaxResults() - objects.size());
        queryParams.setCaseSensitive(params.isCaseSensitive());
        String sql = generateQuery(queryParams);
        try (JDBCPreparedStatement dbStat = session.prepareStatement(sql)) {
            fillParameters(dbStat, params, catalog, false, false);
            try (JDBCResultSet dbResult = dbStat.executeQuery()) {
                while (dbResult.next() && !monitor.isCanceled()) {
                    final String catalogName = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_TABLE_SCHEMA);
                    final String tableName = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_TABLE_NAME);
                    final String constrName = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_CONSTRAINT_NAME);
                    String constrType = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_CONSTRAINT_TYPE);
                    final boolean isFK = GBase8aConstants.CONSTRAINT_FOREIGN_KEY.equals(constrType);
                    objects.add(new AbstractObjectReference<>(constrName, dataSource.getCatalog(catalogName), null, isFK ? GBase8aTableForeignKey.class : GBase8aTableConstraint.class, RelationalObjectType.TYPE_CONSTRAINT) {
                        public DBSObject resolveObject(DBRProgressMonitor monitor) throws DBException {
                            GBase8aTableConstraint gBase8aTableConstraint = null;
                            GBase8aCatalog tableCatalog = (catalog != null) ? catalog : dataSource.getCatalog(catalogName);
                            if (tableCatalog == null) {
                                throw new DBException("Constraint catalog '" + catalogName + "' not found");
                            }
                            GBase8aTable table = tableCatalog.getTable(monitor, tableName);
                            if (table == null) {
                                throw new DBException("Constraint table '" + tableName + "' not found in catalog '" + tableCatalog.getName() + "'");
                            }
                            if (isFK) {
                                return table.getAssociation(monitor, constrName);
                            } else {
                                gBase8aTableConstraint = table.getConstraint(monitor, constrName);
                            }
                            if (gBase8aTableConstraint == null) {
                                throw new DBException("Constraint '" + constrName + "' not found in table '" + table.getFullyQualifiedName(DBPEvaluationContext.DDL) + "'");
                            }
                            return gBase8aTableConstraint;
                        }
                    });
                }
            }
        }
    }

    private void findTableColumnsByMask(JDBCSession session, @Nullable final GBase8aCatalog catalog, @NotNull ObjectsSearchParams params,
                                        List<DBSObjectReference> objects) throws SQLException, DBException {
        DBRProgressMonitor monitor = session.getProgressMonitor();
//        try (JDBCPreparedStatement dbStat = session.prepareStatement(
//                "SELECT TABLE_SCHEMA,TABLE_NAME,COLUMN_NAME FROM information_schema.COLUMNS WHERE COLUMN_NAME LIKE ? "
//                        + ((catalog == null) ? "" : " AND TABLE_SCHEMA=?")
//                        + " ORDER BY COLUMN_NAME LIMIT "
//                        + maxResults)) {
//            dbStat.setString(1, constrNameMask.toLowerCase(Locale.ENGLISH));
//            if (catalog != null) {
//                dbStat.setString(2, catalog.getName());
//            }
        QueryParams queryParams = new QueryParams(
                GBase8aConstants.COL_COLUMN_NAME,
                GBase8aConstants.COL_TABLE_SCHEMA + "," + GBase8aConstants.COL_TABLE_NAME + "," + GBase8aConstants.COL_COLUMN_NAME,
                GBase8aConstants.META_TABLE_COLUMNS
        );
        if (params.isSearchInComments()) {
            queryParams.setCommentColumnName("COLUMN_COMMENT");
        }
        if (catalog != null) {
            queryParams.setSchemaColumnName(GBase8aConstants.COL_TABLE_SCHEMA);
        }
        queryParams.setMaxResults(params.getMaxResults() - objects.size());
        queryParams.setCaseSensitive(params.isCaseSensitive());
//        if (params.isSearchInDefinitions()) {
//            queryParams.setDefinitionColumnName(MySQLConstants.COL_COLUMN_GENERATION_EXPRESSION);
//        }
        String sql = generateQuery(queryParams);
        try (JDBCPreparedStatement dbStat = session.prepareStatement(sql)) {
            fillParameters(dbStat, params, catalog, true, true);
            try (JDBCResultSet dbResult = dbStat.executeQuery()) {
                while (dbResult.next() && !monitor.isCanceled()) {
                    final String catalogName = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_TABLE_SCHEMA);
                    final String tableName = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_TABLE_NAME);
                    final String columnName = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_COLUMN_NAME);
                    objects.add(new AbstractObjectReference<>(columnName, dataSource.getCatalog(catalogName), null, GBase8aTableColumn.class, RelationalObjectType.TYPE_TABLE_COLUMN) {
                        @NotNull
                        public String getFullyQualifiedName(DBPEvaluationContext context) {
                            if (CommonUtils.isEmpty(catalogName) || CommonUtils.isEmpty(tableName) || CommonUtils.isEmpty(columnName)) {
                                log.debug("Can't find correct column metadata.");
                                return "";
                            }
                            return String.valueOf(DBUtils.getQuotedIdentifier(dataSource, catalogName)) +
                                    '.' +
                                    DBUtils.getQuotedIdentifier(dataSource, tableName) +
                                    '.' +
                                    DBUtils.getQuotedIdentifier(dataSource, columnName);
                        }

                        public DBSObject resolveObject(DBRProgressMonitor monitor) throws DBException {
                            GBase8aCatalog tableCatalog = (catalog != null) ? catalog : dataSource.getCatalog(catalogName);
                            if (tableCatalog == null) {
                                throw new DBException("Column catalog '" + catalogName + "' not found");
                            }
                            GBase8aTableBase table = tableCatalog.getTableCache().getObject(monitor, tableCatalog, tableName);
                            if (table == null) {
                                throw new DBException("Column table '" + tableName + "' not found in catalog '" + tableCatalog.getName() + "'");
                            }
                            GBase8aTableColumn column = table.getAttribute(monitor, columnName);
                            if (column == null) {
                                throw new DBException("Column '" + columnName + "' not found in table '" + table.getFullyQualifiedName(DBPEvaluationContext.DDL) + "'");
                            }
                            return column;
                        }
                    });
                }
            }
        }
    }

    private static String generateQuery(@NotNull QueryParams params) {
        StringBuilder sql = new StringBuilder("SELECT ").append(params.getSelect()).append(" FROM ").append(params.getFrom()).append(" WHERE ");
        String commentColumnName = params.getCommentColumnName();
        String definitionColumnName = params.getDefinitionColumnName();
        boolean addParentheses = commentColumnName != null || definitionColumnName != null;
        if (addParentheses) {
            sql.append("(");
        }
        boolean caseSensitive = params.isCaseSensitive();
        addNameWithLikeCondition(sql, params.getObjectNameColumn(), caseSensitive, false);
        if (!CommonUtils.isEmpty(commentColumnName)) {
            addNameWithLikeCondition(sql, commentColumnName, caseSensitive, true);
        }
        if (!CommonUtils.isEmpty(definitionColumnName)) {
            addNameWithLikeCondition(sql, definitionColumnName, caseSensitive, true);
        }
        if (addParentheses) {
            sql.append(") ");
        }
        if (!CommonUtils.isEmpty(params.getSchemaColumnName())) {
            sql.append("AND ").append(params.getSchemaColumnName()).append(" = ? ");
        }
        sql.append("ORDER BY ").append(params.getObjectNameColumn()).append(" LIMIT ").append(params.getMaxResults());
        return sql.toString();
    }

    private static void addNameWithLikeCondition(@NotNull StringBuilder sql, @NotNull String name, boolean caseSensitive, boolean addOR) {
        if (addOR) {
            sql.append(" OR ");
        }
        if (!caseSensitive) {
            sql.append("UPPER(");
        }
        sql.append(name);
        if (!caseSensitive) {
            sql.append(")");
        }
        sql.append(" LIKE ?");
    }

    private static void fillParameters(@NotNull JDBCPreparedStatement statement, @NotNull ObjectsSearchParams params,
                                       @Nullable GBase8aCatalog catalog, boolean hasCommentColumn, boolean hasDefinitionColumn) throws SQLException {
        String mask = params.isCaseSensitive() ? params.getMask() : params.getMask().toUpperCase(Locale.ENGLISH);
        statement.setString(1, mask);
        int idx = 2;
        if (params.isSearchInComments() && hasCommentColumn) {
            statement.setString(idx, mask);
            idx++;
        }
        if (params.isSearchInDefinitions() && hasDefinitionColumn) {
            statement.setString(idx, mask);
            idx++;
        }
        if (catalog != null) {
            statement.setString(idx, catalog.getName());
        }
    }

    private static final class QueryParams {
        @NotNull
        private final String objectNameColumn;

        @Nullable
        private String commentColumnName;

        @Nullable
        private String schemaColumnName;

        @NotNull
        private final String select;

        @NotNull
        private final String from;

        private int maxResults;

        @Nullable
        private String definitionColumnName;
        private boolean isCaseSensitive;

        private QueryParams(@NotNull String objectNameColumn, @NotNull String select, @NotNull String from) {
            this.objectNameColumn = objectNameColumn;
            this.select = select;
            this.from = from;
        }

        @NotNull
        private String getObjectNameColumn() {
            return objectNameColumn;
        }

        @Nullable
        private String getCommentColumnName() {
            return commentColumnName;
        }

        private void setCommentColumnName(@Nullable String commentColumnName) {
            this.commentColumnName = commentColumnName;
        }

        @Nullable
        private String getSchemaColumnName() {
            return schemaColumnName;
        }

        private void setSchemaColumnName(@Nullable String schemaColumnName) {
            this.schemaColumnName = schemaColumnName;
        }

        @NotNull
        public String getSelect() {
            return select;
        }

        @NotNull
        public String getFrom() {
            return from;
        }

        @Nullable
        private String getDefinitionColumnName() {
            return definitionColumnName;
        }

        private void setDefinitionColumnName(@Nullable String definitionColumnName) {
            this.definitionColumnName = definitionColumnName;
        }

        private int getMaxResults() {
            return maxResults;
        }

        private void setMaxResults(int maxResults) {
            this.maxResults = maxResults;
        }

        public boolean isCaseSensitive() {
            return isCaseSensitive;
        }

        public void setCaseSensitive(boolean caseSensitive) {
            isCaseSensitive = caseSensitive;
        }
    }

    @Override
    public boolean supportsSearchInCommentsFor(@NotNull DBSObjectType objectType) {
        return objectType == RelationalObjectType.TYPE_TABLE
                || objectType == RelationalObjectType.TYPE_PROCEDURE
                || objectType == RelationalObjectType.TYPE_TABLE_COLUMN;
    }

    @Override
    public boolean supportsSearchInDefinitionsFor(@NotNull DBSObjectType objectType) {
        return objectType == RelationalObjectType.TYPE_PROCEDURE || objectType == RelationalObjectType.TYPE_TABLE_COLUMN;
    }
}
