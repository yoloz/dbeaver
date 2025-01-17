package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCAttribute;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureParameter;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureParameterKind;


public class GBase8aProcedureParameter extends JDBCAttribute implements DBSProcedureParameter, DBSTypedObject {

    private GBase8aProcedure procedure;
    private DBSProcedureParameterKind parameterKind;

    public GBase8aProcedureParameter(GBase8aProcedure procedure, String columnName, String typeName, int valueType,
                                     int ordinalPosition, long columnSize, int scale, int precision, boolean notNull,
                                     DBSProcedureParameterKind parameterKind) {
        super(columnName, typeName, valueType, ordinalPosition, columnSize, scale, precision, notNull, false);
        this.procedure = procedure;
        this.parameterKind = parameterKind;
    }


    @NotNull
    @Override
    public GBase8aDataSource getDataSource() {
        return this.procedure.getDataSource();
    }

    @Override
    public GBase8aProcedure getParentObject() {
        return this.procedure;
    }


    @NotNull
    @Override
    @Property(viewable = true, order = 10, name = "parameterKind")
    public DBSProcedureParameterKind getParameterKind() {
        return this.parameterKind;
    }


    @NotNull
    @Override
    public DBSTypedObject getParameterType() {
        return this;
    }
}
