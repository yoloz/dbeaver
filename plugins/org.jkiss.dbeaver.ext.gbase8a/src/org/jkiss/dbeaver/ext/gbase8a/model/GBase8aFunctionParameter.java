//package org.jkiss.dbeaver.ext.gbase8a.model;
//
//import org.jkiss.code.NotNull;
//import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCAttribute;
//import org.jkiss.dbeaver.model.meta.Property;
//import org.jkiss.dbeaver.model.struct.DBSTypedObject;
//import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureParameter;
//import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureParameterKind;
//
//
//public class GBase8aFunctionParameter extends JDBCAttribute implements DBSProcedureParameter, DBSTypedObject {
//
//    private GBase8aFunction function;
//    private DBSProcedureParameterKind parameterKind;
//
//    public GBase8aFunctionParameter(GBase8aFunction function, String columnName, String typeName, int valueType,
//                                    int ordinalPosition, long columnSize, int scale, int precision, boolean notNull,
//                                    DBSProcedureParameterKind parameterKind) {
//        super(columnName, typeName, valueType, ordinalPosition, columnSize, scale, precision, notNull, false);
//        this.function = function;
//        this.parameterKind = parameterKind;
//    }
//
//
//    @NotNull
//    @Override
//    public GBase8aDataSource getDataSource() {
//        return this.function.getDataSource();
//    }
//
//    @Override
//    public GBase8aFunction getParentObject() {
//        return this.function;
//    }
//
//
//    @NotNull
//    @Override
//    @Property(viewable = true, order = 10, name = "parameterKind")
//    public DBSProcedureParameterKind getParameterKind() {
//        return this.parameterKind;
//    }
//
//
//    @NotNull
//    @Override
//    public DBSTypedObject getParameterType() {
//        return this;
//    }
//}
