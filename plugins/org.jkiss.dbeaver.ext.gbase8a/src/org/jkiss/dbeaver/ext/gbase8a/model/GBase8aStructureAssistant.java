package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCDataSource;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCStructureAssistant;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.struct.AbstractObjectReference;
import org.jkiss.dbeaver.model.impl.struct.RelationalObjectType;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSObjectReference;
import org.jkiss.dbeaver.model.struct.DBSObjectType;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;


public class GBase8aStructureAssistant extends JDBCStructureAssistant {

    private final GBase8aDataSource dataSource;

    public GBase8aStructureAssistant(GBase8aDataSource dataSource) {
        this.dataSource = dataSource;
    }


    protected JDBCDataSource getDataSource() {
        return this.dataSource;
    }


    public DBSObjectType[] getSupportedObjectTypes() {
        return new DBSObjectType[]{
                RelationalObjectType.TYPE_TABLE,
                RelationalObjectType.TYPE_CONSTRAINT,
                RelationalObjectType.TYPE_PROCEDURE,
                RelationalObjectType.TYPE_TABLE_COLUMN
        };
    }


    public DBSObjectType[] getHyperlinkObjectTypes() {
        return new DBSObjectType[]{
                RelationalObjectType.TYPE_TABLE,
                RelationalObjectType.TYPE_PROCEDURE
        };
    }


    public DBSObjectType[] getAutoCompleteObjectTypes() {
        return new DBSObjectType[]{
                RelationalObjectType.TYPE_TABLE,
                RelationalObjectType.TYPE_PROCEDURE
        };
    }

    @Override
    protected void findObjectsByMask(JDBCExecutionContext executionContext, JDBCSession session, DBSObjectType objectType, ObjectsSearchParams params, List list) throws DBException, SQLException {
        GBase8aCatalog catalog = (objectType instanceof GBase8aCatalog) ? (GBase8aCatalog) objectType : null;
        if (catalog == null) {
            catalog = this.dataSource.getDefaultObject();
        }
        int maxResults = params.getMaxResults();
        String objectNameMask = params.getMask();
        List<DBSObjectReference> references = (List<DBSObjectReference>)list;
        if (objectType == RelationalObjectType.TYPE_TABLE) {
            findTablesByMask(session, catalog, objectNameMask, maxResults, references);
        } else if (objectType == RelationalObjectType.TYPE_CONSTRAINT) {
            findConstraintsByMask(session, catalog, objectNameMask, maxResults, references);
        } else if (objectType == RelationalObjectType.TYPE_PROCEDURE) {
            findProceduresByMask(session, catalog, objectNameMask, maxResults, references);
        } else if (objectType == RelationalObjectType.TYPE_TABLE_COLUMN) {
            findTableColumnsByMask(session, catalog, objectNameMask, maxResults, references);
        }
    }

    private void findTablesByMask(JDBCSession session, @Nullable final GBase8aCatalog catalog, String tableNameMask, int maxResults, List<DBSObjectReference> objects) throws SQLException, DBException {
        DBRProgressMonitor monitor = session.getProgressMonitor();
        try (JDBCPreparedStatement dbStat = session.prepareStatement(
                "SELECT TABLE_SCHEMA,TABLE_NAME FROM information_schema.TABLES WHERE TABLE_NAME LIKE ? "
                        + ((catalog == null) ? "" : " AND TABLE_SCHEMA=?")
                        + " ORDER BY " + "TABLE_NAME" + " LIMIT " + maxResults)) {
            dbStat.setString(1, tableNameMask.toLowerCase(Locale.ENGLISH));
            if (catalog != null) {
                dbStat.setString(2, catalog.getName());
            }
            try (JDBCResultSet dbResult = dbStat.executeQuery()) {
                int tableNum = maxResults;
                while (dbResult.next() && tableNum-- > 0 && !monitor.isCanceled()) {
                    final String catalogName = JDBCUtils.safeGetString(dbResult, "TABLE_SCHEMA");
                    final String tableName = JDBCUtils.safeGetString(dbResult, "TABLE_NAME");
                    objects.add(new AbstractObjectReference(tableName, this.dataSource.getActiveVC().getCatalog(catalogName), null, GBase8aTableBase.class, RelationalObjectType.TYPE_TABLE) {
                        public DBSObject resolveObject(DBRProgressMonitor monitor) throws DBException {
                            GBase8aCatalog tableCatalog = (catalog != null) ? catalog : GBase8aStructureAssistant.this.dataSource.getActiveVC().getCatalog(catalogName);
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


    private void findProceduresByMask(JDBCSession session, @Nullable final GBase8aCatalog catalog, String procNameMask, int maxResults, List<DBSObjectReference> objects) throws SQLException, DBException {
        DBRProgressMonitor monitor = session.getProgressMonitor();
        try (JDBCPreparedStatement dbStat = session.prepareStatement(
                "SELECT ROUTINE_SCHEMA,ROUTINE_NAME FROM information_schema.ROUTINES WHERE trim(ROUTINE_NAME) LIKE ? "
                        + ((catalog == null) ? "" : " AND trim(ROUTINE_SCHEMA)=?")
                        + " ORDER BY " + "ROUTINE_NAME" + " LIMIT " + maxResults)) {
            dbStat.setString(1, procNameMask.toLowerCase(Locale.ENGLISH));
            if (catalog != null) {
                dbStat.setString(2, catalog.getName());
            }
            try (JDBCResultSet dbResult = dbStat.executeQuery()) {
                int tableNum = maxResults;
                while (dbResult.next() && tableNum-- > 0 && !monitor.isCanceled()) {
                    final String catalogName = JDBCUtils.safeGetString(dbResult, "ROUTINE_SCHEMA").trim();
                    final String procName = JDBCUtils.safeGetString(dbResult, "ROUTINE_NAME").trim();
                    objects.add(new AbstractObjectReference(procName, this.dataSource.getActiveVC().getCatalog(catalogName), null, GBase8aProcedure.class, RelationalObjectType.TYPE_PROCEDURE) {
                        public DBSObject resolveObject(DBRProgressMonitor monitor) throws DBException {
                            GBase8aCatalog procCatalog = (catalog != null) ? catalog : GBase8aStructureAssistant.this.dataSource.getActiveVC().getCatalog(catalogName);
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


    private void findConstraintsByMask(JDBCSession session, @Nullable final GBase8aCatalog catalog, String constrNameMask, int maxResults, List<DBSObjectReference> objects) throws SQLException, DBException {
        DBRProgressMonitor monitor = session.getProgressMonitor();
        try (JDBCPreparedStatement dbStat = session.prepareStatement(
                "SELECT TABLE_SCHEMA,TABLE_NAME,CONSTRAINT_NAME,CONSTRAINT_TYPE FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_NAME LIKE ? "
                        + ((catalog == null) ? "" : " AND TABLE_SCHEMA=?")
                        + " ORDER BY " + "CONSTRAINT_NAME" + " LIMIT " + maxResults)) {
            dbStat.setString(1, constrNameMask.toLowerCase(Locale.ENGLISH));
            if (catalog != null) {
                dbStat.setString(2, catalog.getName());
            }
            try (JDBCResultSet dbResult = dbStat.executeQuery()) {
                int tableNum = maxResults;
                while (dbResult.next() && tableNum-- > 0 && !monitor.isCanceled()) {
                    final String catalogName = JDBCUtils.safeGetString(dbResult, "TABLE_SCHEMA");
                    final String tableName = JDBCUtils.safeGetString(dbResult, "TABLE_NAME");
                    final String constrName = JDBCUtils.safeGetString(dbResult, "CONSTRAINT_NAME");
                    String constrType = JDBCUtils.safeGetString(dbResult, "CONSTRAINT_TYPE");
                    final boolean isFK = "FOREIGN KEY".equals(constrType);
                    objects.add(new AbstractObjectReference(constrName, this.dataSource.getActiveVC().getCatalog(catalogName), null, isFK ? GBase8aTableForeignKey.class : GBase8aTableConstraint.class, RelationalObjectType.TYPE_CONSTRAINT) {
                        public DBSObject resolveObject(DBRProgressMonitor monitor) throws DBException {
                            GBase8aTableConstraint gBase8aTableConstraint = null;
                            GBase8aCatalog tableCatalog = (catalog != null) ? catalog : GBase8aStructureAssistant.this.dataSource.getActiveVC().getCatalog(catalogName);
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


    private void findTableColumnsByMask(JDBCSession session, @Nullable final GBase8aCatalog catalog, String constrNameMask, int maxResults, List<DBSObjectReference> objects) throws SQLException, DBException {
        DBRProgressMonitor monitor = session.getProgressMonitor();
        try (JDBCPreparedStatement dbStat = session.prepareStatement(
                "SELECT TABLE_SCHEMA,TABLE_NAME,COLUMN_NAME FROM information_schema.COLUMNS WHERE COLUMN_NAME LIKE ? "
                        + ((catalog == null) ? "" : " AND TABLE_SCHEMA=?")
                        + " ORDER BY " + "COLUMN_NAME" + " LIMIT " + maxResults)) {
            dbStat.setString(1, constrNameMask.toLowerCase(Locale.ENGLISH));
            if (catalog != null) {
                dbStat.setString(2, catalog.getName());
            }
            try (JDBCResultSet dbResult = dbStat.executeQuery()) {
                int tableNum = maxResults;
                while (dbResult.next() && tableNum-- > 0 && !monitor.isCanceled()) {
                    final String catalogName = JDBCUtils.safeGetString(dbResult, "TABLE_SCHEMA");
                    final String tableName = JDBCUtils.safeGetString(dbResult, "TABLE_NAME");
                    final String columnName = JDBCUtils.safeGetString(dbResult, "COLUMN_NAME");
                    objects.add(new AbstractObjectReference(columnName, this.dataSource.getActiveVC().getCatalog(catalogName), null, GBase8aTableColumn.class, RelationalObjectType.TYPE_TABLE_COLUMN) {
                        @NotNull
                        public String getFullyQualifiedName(DBPEvaluationContext context) {
                            return String.valueOf(DBUtils.getQuotedIdentifier(GBase8aStructureAssistant.this.dataSource, catalogName)) +
                                    '.' +
                                    DBUtils.getQuotedIdentifier(GBase8aStructureAssistant.this.dataSource, tableName) +
                                    '.' +
                                    DBUtils.getQuotedIdentifier(GBase8aStructureAssistant.this.dataSource, columnName);
                        }

                        public DBSObject resolveObject(DBRProgressMonitor monitor) throws DBException {
                            GBase8aCatalog tableCatalog = (catalog != null) ? catalog : GBase8aStructureAssistant.this.dataSource.getActiveVC().getCatalog(catalogName);
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
}
