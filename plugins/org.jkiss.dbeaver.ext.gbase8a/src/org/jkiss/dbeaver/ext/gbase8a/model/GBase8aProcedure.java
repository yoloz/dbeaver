package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aConstants;
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
import org.jkiss.utils.CommonUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

public class GBase8aProcedure extends AbstractProcedure<GBase8aDataSource, GBase8aCatalog> implements GBase8aSourceObject, DBPRefreshableObject {

    private static final Log log = Log.getLog(GBase8aProcedure.class);

    private DBSProcedureType procedureType;
    private String resultType;
    private String bodyType;
    private boolean deterministic;
    private transient String clientBody;
    private String charset;

    public GBase8aProcedure(GBase8aCatalog catalog) {
        super(catalog, false);
        this.procedureType = DBSProcedureType.PROCEDURE;
        this.bodyType = "SQL";
        this.resultType = "";
        this.deterministic = false;
    }

    public GBase8aProcedure(GBase8aCatalog catalog, ResultSet dbResult) {
        super(catalog, true);
        loadInfo(dbResult);
    }

    private void loadInfo(ResultSet dbResult) {
        setName(CommonUtils.trim(JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_ROUTINE_NAME)));
        setDescription(JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_ROUTINE_COMMENT));
        String procType = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_ROUTINE_TYPE);
        try {
            this.procedureType = procType == null ? DBSProcedureType.PROCEDURE : DBSProcedureType.valueOf(procType.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            log.debug("Unsupported procedure type: " + procType);
            this.procedureType = DBSProcedureType.PROCEDURE;
        }
        this.resultType = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_DTD_IDENTIFIER);
        this.bodyType = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_ROUTINE_BODY);
        this.charset = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_CHARACTER_SET_CLIENT);
        this.deterministic = JDBCUtils.safeGetBoolean(dbResult, GBase8aConstants.COL_IS_DETERMINISTIC, "YES");
        this.description = JDBCUtils.safeGetString(dbResult, GBase8aConstants.COL_ROUTINE_COMMENT);
    }

    @Override
    @Property(order = 2)
    public DBSProcedureType getProcedureType() {
        return procedureType;
    }

    public void setProcedureType(DBSProcedureType procedureType) {
        this.procedureType = procedureType;
    }

    @Property(order = 3)
    public String getResultType() {
        return resultType;
    }

    @Property(order = 4)
    public String getBodyType() {
        return bodyType;
    }

    @Property(editable = true, updatable = true, order = 5)
    public boolean isDeterministic() {
        return deterministic;
    }

    @Property(hidden = true, editable = true, updatable = true, order = -1)
    public String getDeclaration(DBRProgressMonitor monitor) throws DBException {
        if (this.clientBody == null) {
            if (!this.persisted) {
                this.clientBody =
                        "CREATE " + getProcedureType().name() + " " + getFullyQualifiedName(DBPEvaluationContext.DDL)
                                + "()" + GeneralUtils.getDefaultLineSeparator()
                                + ((this.procedureType == DBSProcedureType.FUNCTION) ? ("RETURNS INT" + GeneralUtils.getDefaultLineSeparator()) : "")
                                + "BEGIN" + GeneralUtils.getDefaultLineSeparator() +
                                "END";
            } else {
                try (JDBCSession session = DBUtils.openMetaSession(monitor, getDataSource(), "Read procedure declaration");
                     JDBCPreparedStatement dbStat = session.prepareStatement("SHOW CREATE "
                             + getProcedureType().name()
                             + " "
                             + getFullyQualifiedName(DBPEvaluationContext.DDL));
                     JDBCResultSet dbResult = dbStat.executeQuery()) {
                    if (dbResult.next()) {
                        this.clientBody = JDBCUtils.safeGetString(dbResult, (getProcedureType() == DBSProcedureType.PROCEDURE) ? "Create Procedure" : "Create Function");
                        if (this.clientBody == null) {
                            this.clientBody = "";
                        } else {
                            this.clientBody = normalizeCreateStatement(this.clientBody);
                        }
                        this.clientBody = GBase8aUtils.dealProcedureSqlDefiner(this.clientBody, getProcedureType());
                        this.clientBody = GBase8aUtils.dealProcedureSqlGualifiedName(getFullyQualifiedName(DBPEvaluationContext.DDL), this.clientBody, getProcedureType());
                    } else {
                        this.clientBody = "";
                    }
                } catch (SQLException e) {
                    this.clientBody = e.getMessage();
                    throw new DBException("Read procedure declaration failed", e);
                }
            }
        }
        return this.clientBody;
    }

    private String normalizeCreateStatement(String createDDL) {
        String procType = getProcedureType().name();
        int divPos = createDDL.indexOf(procType + " `");
        if (divPos != -1) {
            return createDDL.substring(0, divPos)
                    + procType
                    + " `"
                    + getContainer().getName()
                    + "`."
                    + createDDL.substring(divPos + procType.length() + 1);
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

    @Nullable
    @Override
    public Collection<GBase8aProcedureParameter> getParameters(@NotNull DBRProgressMonitor monitor) throws DBException {
        return getContainer().getProceduresCache().getChildren(monitor, getContainer(), this);
    }

    @NotNull
    @Override
    public String getFullyQualifiedName(DBPEvaluationContext context) {
        String fullName = DBUtils.getFullQualifiedName(getDataSource(), getContainer(), this);
        if (!CommonUtils.isEmpty(getDataSource().getVcName())) {
            fullName = getDataSource().getVcName() + "." + fullName;
        }
        return fullName;
    }

    @Override
    @Property(hidden = true, editable = true, updatable = true, order = -1)
    public String getObjectDefinitionText(DBRProgressMonitor monitor, Map<String, Object> options) throws DBException {
        return getDeclaration(monitor);
    }

    @Override
    public void setObjectDefinitionText(String sourceText) throws DBException {
        setDeclaration(sourceText);
    }

    @Override
    public DBSObject refreshObject(@NotNull DBRProgressMonitor monitor) throws DBException {
        return getContainer().getProceduresCache().refreshObject(monitor, getContainer(), this);
    }

    @Override
    public String toString() {
        return procedureType.name() + " " + getName();
    }
}
