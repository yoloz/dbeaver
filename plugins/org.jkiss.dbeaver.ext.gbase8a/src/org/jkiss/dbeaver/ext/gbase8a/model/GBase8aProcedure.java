package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aUtils;
import org.jkiss.dbeaver.model.DBPDataKind;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBPRefreshableObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.struct.AbstractProcedure;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureType;
import org.jkiss.dbeaver.utils.GeneralUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

public class GBase8aProcedure extends AbstractProcedure<GBase8aDataSource, GBase8aCatalog> implements GBase8aSourceObject, DBPRefreshableObject {

    private DBSProcedureType procedureType;
    private transient String clientBody;
    private String charset;

    public GBase8aProcedure(GBase8aCatalog catalog) {
        super(catalog, false);
        this.procedureType = DBSProcedureType.PROCEDURE;
    }

    public GBase8aProcedure(GBase8aCatalog catalog, ResultSet dbResult) {
        super(catalog, true);
        loadInfo(dbResult);
    }

    private void loadInfo(ResultSet dbResult) {
        setName(JDBCUtils.safeGetString(dbResult, "ROUTINE_NAME"));
        setDescription(JDBCUtils.safeGetString(dbResult, "ROUTINE_COMMENT"));
        String procType = JDBCUtils.safeGetString(dbResult, "ROUTINE_TYPE");
        this.procedureType = (procType == null) ? DBSProcedureType.PROCEDURE : DBSProcedureType.valueOf(procType.toUpperCase(Locale.ENGLISH));
        this.charset = JDBCUtils.safeGetString(dbResult, "CHARACTER_SET_CLIENT");
        this.description = JDBCUtils.safeGetString(dbResult, "ROUTINE_COMMENT");
    }

    @Property(order = 2)
    public DBSProcedureType getProcedureType() {
        return this.procedureType;
    }

    public void setProcedureType(DBSProcedureType procedureType) {
        this.procedureType = procedureType;
    }

    @Property(hidden = true, editable = true, updatable = true, order = -1)
    public String getDeclaration(DBRProgressMonitor monitor) throws DBException {
        if (this.clientBody == null) {
            if (!this.persisted) {
                this.clientBody =
                        "CREATE " + getProcedureType().name() + " " + getFullyQualifiedName(DBPEvaluationContext.DDL)
                                + "()" + GeneralUtils.getDefaultLineSeparator()
                                + ((this.procedureType == DBSProcedureType.FUNCTION) ? ("RETURNS INT" + GeneralUtils.getDefaultLineSeparator()) : "")
                                + "BEGIN" + GeneralUtils.getDefaultLineSeparator() + GeneralUtils.getDefaultLineSeparator() +
                                "END";
            } else {
                try (JDBCSession session = DBUtils.openMetaSession(monitor, getDataSource(), "Read procedure declaration");
                     JDBCPreparedStatement dbStat = session.prepareStatement("SHOW CREATE " + getProcedureType().name() + " " + getFullyQualifiedName(DBPEvaluationContext.DDL));
                     JDBCResultSet dbResult = dbStat.executeQuery()) {
                    if (dbResult.next()) {
                        this.clientBody = JDBCUtils.safeGetString(dbResult, (getProcedureType() == DBSProcedureType.PROCEDURE) ? "Create Procedure" : "Create Function");
                        if (this.clientBody == null) {
                            this.clientBody = "";
                        } else {
                            this.clientBody = normalizeCreateStatement(this.clientBody);
                        }

                        this.clientBody = GBase8aUtils.dealProcedureSqlDefiner(this.clientBody);

                        this.clientBody = GBase8aUtils.dealProcedureSqlGualifiedName(getFullyQualifiedName(DBPEvaluationContext.DDL), this.clientBody);
                    } else {

                        this.clientBody = "";
                    }
                } catch (SQLException e) {
                    this.clientBody = e.getMessage();
                    throw new DBException(e, getDataSource());
                }
            }
        }
        return this.clientBody;
    }

    private String normalizeCreateStatement(String createDDL) {
        String procType = getProcedureType().name();
        int divPos = createDDL.indexOf(procType + " `");
        if (divPos != -1) {
            return createDDL.substring(0, divPos) + procType +
                    " `" + getContainer().getName() + "`." +
                    createDDL.substring(divPos + procType.length() + 1);
        }
        return createDDL;
    }

    private static void appendParameterType(StringBuilder cb, GBase8aProcedureParameter column) {
        cb.append(column.getTypeName());
        if (column.getDataKind() == DBPDataKind.STRING && column.getMaxLength() > 0L) {
            cb.append('(').append(column.getMaxLength()).append(')');
        }
    }

    public String getDeclaration() {
        return this.clientBody;
    }

    public void setDeclaration(String clientBody) {
        this.clientBody = clientBody;
    }

    public String getCharset() {
        return this.charset;
    }

    public Collection<GBase8aProcedureParameter> getParameters(DBRProgressMonitor monitor) throws DBException {
        return getContainer().proceduresCache.getChildren(monitor, getContainer(), this);
    }

    @NotNull
    public String getFullyQualifiedName(DBPEvaluationContext context) {
        String fullName = DBUtils.getFullQualifiedName(getDataSource(), getContainer(), this);
        if (getDataSource().isVCCluster()) {
            String vcName = getContainer().getVcName();
            if (vcName != null && !vcName.isEmpty() && !"default".equalsIgnoreCase(vcName)) {
                fullName = getContainer().getVcName() + "." + fullName;
            }
        }
        return fullName;
    }

    public void setObjectDefinitionText(String sourceText) throws DBException {
        setDeclaration(sourceText);
    }

    public DBSObject refreshObject(@NotNull DBRProgressMonitor monitor) throws DBException {
        return getContainer().proceduresCache.refreshObject(monitor, getContainer(), this);
    }

    @Override
    public String getObjectDefinitionText(DBRProgressMonitor monitor, Map<String, Object> options) throws DBException {
        return getDeclaration(monitor);
    }
}
