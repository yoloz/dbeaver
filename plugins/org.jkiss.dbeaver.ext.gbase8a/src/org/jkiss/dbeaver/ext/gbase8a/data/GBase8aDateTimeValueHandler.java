package org.jkiss.dbeaver.ext.gbase8a.data;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.data.DBDDisplayFormat;
import org.jkiss.dbeaver.model.data.DBDFormatSettings;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCResultSet;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.exec.DBCStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.impl.jdbc.data.handlers.JDBCDateTimeValueHandler;
import org.jkiss.dbeaver.model.messages.ModelMessages;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;

import java.sql.SQLException;
import java.util.Date;

/**
 * GBase8a datetime handler
 */
public class GBase8aDateTimeValueHandler extends JDBCDateTimeValueHandler {
    private static final Date ZERO_DATE = new Date(0L);
    private static final Date ZERO_TIMESTAMP = new Date(0L);

    private static final String ZERO_DATE_STRING = "0000-00-00";
    private static final String ZERO_TIMESTAMP_STRING = "0000-00-00 00:00:00";

    public GBase8aDateTimeValueHandler(DBDFormatSettings dbdFormatSettings) {
        super(dbdFormatSettings);
    }
//    public GBase8aDateTimeValueHandler(DBDDataFormatterProfile formatterProfile) {
//        super(formatterProfile);
//    }


    @Override
    public Object fetchValueObject(@NotNull DBCSession session, @NotNull DBCResultSet resultSet, @NotNull DBSTypedObject type, int index) throws DBCException {
        return super.fetchValueObject(session, resultSet, type, index);
    }

    @Override
    public void bindValueObject(@NotNull DBCSession session, @NotNull DBCStatement statement, @NotNull DBSTypedObject type, int index, @Nullable Object value) throws DBCException {
        if (value == ZERO_DATE || value == ZERO_TIMESTAMP) {
            try {
                JDBCPreparedStatement dbStat = (JDBCPreparedStatement) statement;
                if (value == ZERO_DATE) {
                    dbStat.setString(index + 1, "0000-00-00");
                } else {
                    dbStat.setString(index + 1, "0000-00-00 00:00:00");
                }
            } catch (SQLException e) {
                throw new DBCException(ModelMessages.model_jdbc_exception_could_not_bind_statement_parameter, e);
            }
        } else {
            super.bindValueObject(session, statement, type, index, value);
        }
    }


    @NotNull
    @Override
    public String getValueDisplayString(@NotNull DBSTypedObject column, Object value, @NotNull DBDDisplayFormat format) {
        if (value == ZERO_DATE) {
            return "0000-00-00";
        }
        if (value == ZERO_TIMESTAMP) {
            return "0000-00-00 00:00:00";
        }
        return super.getValueDisplayString(column, value, format);
    }

    @Override
    public Object getValueFromObject(@NotNull DBCSession session, @NotNull DBSTypedObject type, Object object, boolean copy, boolean validateValue) throws DBCException {
        if (object instanceof String) {
            if (type.getTypeID() == 91) {
                if (object.equals("0000-00-00")) {
                    return ZERO_DATE;
                }
                return super.getValueFromObject(session, type, object, copy, validateValue);
            }
            if (object.equals("0000-00-00 00:00:00")) {
                return ZERO_TIMESTAMP;
            }
        }
        return super.getValueFromObject(session, type, object, copy, validateValue);
    }
}