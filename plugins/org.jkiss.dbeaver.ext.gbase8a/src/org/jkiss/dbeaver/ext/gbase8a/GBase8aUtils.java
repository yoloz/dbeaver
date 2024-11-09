package org.jkiss.dbeaver.ext.gbase8a;

import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aDataSource;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureType;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GBase8aUtils {

    private static final Log log = Log.getLog(GBase8aUtils.class);

    private static final String COLUMN_POSTFIX_PRIV = "_priv";
    private static final Map<String, Integer> typeMap = new HashMap();
//    public static final String COLUMN_POSTFIX_PRIV = "_priv";
//    public static String DATA_TYPE_BIGINT = "bigint";
//    public static String DATA_TYPE_BLOB = "blob";
//    public static String DATA_TYPE_CHAR = "char";
//    public static String DATA_TYPE_DATE = "date";
//    public static String DATA_TYPE_DATETIME = "datetime";
//    public static String DATA_TYPE_DECIMAL = "decimal";
//    public static String DATA_TYPE_DOUBLE = "double";
//    public static String DATA_TYPE_FLOAT = "float";
//    public static String DATA_TYPE_INT = "int";
//    public static String DATA_TYPE_INTEGER = "integer";
//    public static String DATA_TYPE_SMALLINT = "smallint";
//    public static String DATA_TYPE_TEXT = "text";
//    public static String DATA_TYPE_LONG_TEXT = "longtext";
//    public static String DATA_TYPE_TIME = "time";
//    public static String DATA_TYPE_TIMESTAMP = "timestamp";
//    public static String DATA_TYPE_TINYINT = "tinyint";
//    public static String DATA_TYPE_VARCHAR = "varchar";
//    public static String DATA_TYPE_NUMERIC = "numeric";
//    public static String DATA_TYPE_BINARY = "binary";
//    public static String DATA_TYPE_VARBINARY = "varbinary";
//    public static String DATA_TYPE_LONGBLOB = "longblob";

    static {
        typeMap.put("bit", -7);
        typeMap.put("bool", 16);
        typeMap.put("boolean", 16);
        typeMap.put("tinyint", -6);
        typeMap.put("smallint", 5);
        typeMap.put("mediumint", 4);
        typeMap.put("int", 4);
        typeMap.put("integer", 4);
        typeMap.put("int24", 4);
        typeMap.put("bigint", -5);
        typeMap.put("real", 8);
        typeMap.put("float", 6);
        typeMap.put("decimal", 3);
        typeMap.put("dec", 3);
        typeMap.put("numeric", 3);
        typeMap.put("double", 8);
        typeMap.put("double precision", 8);
        typeMap.put("char", 1);
        typeMap.put("varchar", 12);
        typeMap.put("date", 91);
        typeMap.put("time", 92);
        typeMap.put("year", 91);
        typeMap.put("timestamp", 93);
        typeMap.put("datetime", 93);
        typeMap.put("tinyblob", -2);
        typeMap.put("blob", -4);
        typeMap.put("mediumblob", -4);
        typeMap.put("longblob", -4);
        typeMap.put("tinytext", 12);
        typeMap.put("text", 12);
        typeMap.put("mediumtext", 12);
        typeMap.put("longtext", 12);
        typeMap.put("enum", 1);
        typeMap.put("set", 1);
        typeMap.put("geometry", -2);
        typeMap.put("binary", -2);
        typeMap.put("varbinary", -3);
    }

    public GBase8aUtils() {
    }

    public static int typeNameToValueType(String typeName) {
        Integer valueType = typeMap.get(typeName.toLowerCase(Locale.ENGLISH));
        return valueType == null ? 1111 : valueType;
    }


    public static String determineCurrentDatabase(JDBCSession session) throws DBCException {
        try (JDBCPreparedStatement dbStat = session.prepareStatement("SELECT DATABASE()")) {
            try (JDBCResultSet resultSet = dbStat.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString(1);
                }
            }
        } catch (SQLException e) {
            throw new DBCException("determineCurrentDatabase", e);
        }
        return null;
    }

    public static String getActiveResourcePlanId(JDBCSession session, String vcName, GBase8aDataSource dataSource) throws DBCException {
        try {
            String sql = "";
//            if (dataSource.isVCluster()) {
            sql = "select * from gbase.resource_config r,information_schema.vc i where i.id = r.vc_id and i.name='"
                    + vcName
                    + "'";
//            } else {
//                sql = "select * from gbase.resource_config ";
//            }
            try (JDBCPreparedStatement dbStat = session.prepareStatement(sql)) {
                try (JDBCResultSet resultSet = dbStat.executeQuery()) {
                    while (resultSet.next()) {
                        String configName = resultSet.getString("config_name");
                        if (configName != null && configName.equals("active_resource_plan_id")) {
                            String activeResourcePlanId = resultSet.getString("config_int_value");
                            if (activeResourcePlanId != null && !activeResourcePlanId.isEmpty()) {
                                return activeResourcePlanId;
                            }
                        }
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DBCException("getActiveResourcePlanId", e);
        }
    }

    public static boolean isHasDefaultDirective(JDBCSession session, String vcName, GBase8aDataSource dataSource, String planName) throws DBCException {
        try {
            String sql = "";
//            if (dataSource.isVCluster()) {
            sql = "select 1 from gbase.resource_plan p,gbase.resource_plan_directive d,information_schema.vc i ";
            sql = sql + " where d.resource_plan_id = p.resource_plan_id and consumer_group_id='1' ";
            sql = sql + " and  p.resource_plan_name = '" + planName + "' and p.vc_id = i.id and d.vc_id = i.id and i.name='" + vcName + "'";
//            } else {
//                sql = "select 1 from gbase.resource_plan p,gbase.resource_plan_directive d where d.resource_plan_id = p.resource_plan_id and consumer_group_id='1' and  p.resource_plan_name = '" + planName + "'";
//            }
            try (JDBCPreparedStatement dbStat = session.prepareStatement(sql)) {
                try (JDBCResultSet resultSet = dbStat.executeQuery()) {
                    if (!resultSet.next()) {
                        return false;
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            throw new DBCException("isHasDefaultDirective", e);
        }
    }

    public static boolean isLastDirective(JDBCSession session, String vcName, GBase8aDataSource dataSource, String directiveName) throws DBCException {
        try {
            String sql = "";
//            if (dataSource.isVCluster()) {
            sql = sql + "select count(1) directive_num from gbase.resource_plan_directive pd,information_schema.vc i where pd.resource_plan_id in";
            sql = sql + " (select resource_plan_id from gbase.resource_plan_directive d,information_schema.vc info ";
            sql = sql + " where info.id = d.vc_id and  d.resource_plan_directive_name = '" + directiveName + "' and info.name = '" + vcName + "')";
            sql = sql + " and  i.id = pd.vc_id and  i.name = '" + vcName + "'";
//            } else {
//                sql = sql + "select count(1) directive_num from gbase.resource_plan_directive pd where pd.resource_plan_id in";
//                sql = sql + " (select resource_plan_id from gbase.resource_plan_directive d where d.resource_plan_directive_name = '" + directiveName + "')";
//            }

            try (JDBCPreparedStatement dbStat = session.prepareStatement(sql)) {
                try (JDBCResultSet resultSet = dbStat.executeQuery()) {
                    if (resultSet.next()) {
                        String directive_num = resultSet.getString("directive_num");
                        if (Integer.valueOf(directive_num) <= 2) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            throw new DBCException("isLastDirective", e);
        }
    }

    public static String getDefaultDirectiveName(JDBCSession session, String vcName, GBase8aDataSource dataSource, String directiveName) throws DBCException {
        try {
            String sql = "";
//            if (dataSource.isVCluster()) {
            sql = sql + "select pd.resource_plan_directive_name from gbase.resource_plan_directive pd, information_schema.vc i where pd.consumer_group_id='1' and pd.resource_plan_id in";
            sql = sql + " (select resource_plan_id from gbase.resource_plan_directive d ,information_schema.vc info";
            sql = sql + " where info.id = d.vc_id and d.resource_plan_directive_name = '" + directiveName + "' and info.name = '" + vcName + "')";
            sql = sql + " and  i.id = pd.vc_id and  i.name = '" + vcName + "'";
//            } else {
//                sql = sql + "select pd.resource_plan_directive_name from gbase.resource_plan_directive pd where pd.consumer_group_id='1' and pd.resource_plan_id in";
//                sql = sql + " (select resource_plan_id from gbase.resource_plan_directive d where d.resource_plan_directive_name = '" + directiveName + "')";
//            }
            try (JDBCPreparedStatement dbStat = session.prepareStatement(sql)) {
                try (JDBCResultSet resultSet = dbStat.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("resource_plan_directive_name");
                    }
                }
            }
            return "";
        } catch (SQLException e) {
            throw new DBCException("getDefaultDirectiveName", e);
        }
    }

    public static String dealProcedureSqlDefiner(String sql, DBSProcedureType dbsProcedureType) {
        try {
            String startSql = "";
            String endSql = "";
            if (sql.contains("CREATE DEFINER")) {
                startSql = sql.substring(0, sql.indexOf("CREATE DEFINER"));
                endSql = sql.substring(sql.indexOf("CREATE DEFINER"));
                if (dbsProcedureType == DBSProcedureType.PROCEDURE) {
                    endSql = "CREATE " + endSql.substring(endSql.indexOf("PROCEDURE"));
                } else {
                    endSql = "CREATE " + endSql.substring(endSql.indexOf("FUNCTION"));
                }
                sql = startSql + endSql;
            }
        } catch (Exception var3) {
            log.error(var3);
        }
        return sql;
    }

    public static String dealProcedureSqlGualifiedName(String name, String sql, DBSProcedureType dbsProcedureType) {
        try {
            if (dbsProcedureType == DBSProcedureType.PROCEDURE) {
                sql = "CREATE PROCEDURE " + name + " " + sql.substring(sql.indexOf("("));
            } else {
                sql = "CREATE FUNCTION " + name + " " + sql.substring(sql.indexOf("("));
            }
        } catch (Exception var3) {
            log.error(var3);
        }
        return sql;
    }

    public static String dealFunctionSqlDefiner(String sql) {
        try {
            String startSql = "";
            String endSql = "";
            if (sql.indexOf("CREATE DEFINER") != -1) {
                startSql = sql.substring(0, sql.indexOf("CREATE DEFINER"));
                endSql = sql.substring(sql.indexOf("CREATE DEFINER"));
                endSql = "CREATE " + endSql.substring(endSql.indexOf("FUNCTION"));
                sql = startSql + endSql;
            }
        } catch (Exception var3) {
            log.error(var3);
        }
        return sql;
    }

    public static String dealFunctionSqlGualifiedName(String name, String sql) {
        try {
            sql = "CREATE FUNCTION " + name + " " + sql.substring(sql.indexOf("("));
        } catch (Exception var3) {
            log.error(var3);
        }
        return sql;
    }

    public static boolean isAlterUSerSupported(GBase8aDataSource dataSource) {
        return true;
    }
}
