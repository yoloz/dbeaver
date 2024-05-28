package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCQueryTransformer;
import org.jkiss.dbeaver.model.exec.DBCStatement;
import org.jkiss.dbeaver.model.sql.SQLQuery;

import java.sql.SQLException;
import java.sql.Statement;


class QueryTransformerFetchAll implements DBCQueryTransformer {

    public void setParameters(Object... parameters) {
    }

    public String transformQueryString(SQLQuery query) throws DBCException {
        return query.getOriginalText();
    }


    public void transformStatement(DBCStatement statement, int parameterIndex) throws DBCException {
        try {
            ((Statement) statement).setFetchSize(-2147483648);
        } catch (SQLException e) {
            throw new DBCException("transformStatement", e);
        }
    }
}