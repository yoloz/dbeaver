package org.jkiss.dbeaver.ext.gbase8a.ui;

import org.jkiss.dbeaver.ext.gbase8a.GBase8aQueryTokenizer;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aIEClusterObject;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;

public final class GBase8aIEUtil {
    public static final String CLUSTER_TYPE_TABLE = "TABLE";
    public static final String CLUSTER_TYPE_VIEW = "VIEW";
    public static final String CLUSTER_TYPE_PROCEDUCE = "PROCEDURE";
    public static final String CLUSTER_TYPE_FUNCTION = "FUNCTION";
    public static final String CODING_UTF_8 = "UTF-8";
    public static final String CODING_GBK = "GBK";
    public static final String PATH_REGEX_WIN = "(\\b[a-zA-Z]:)\\\\((?:[^\\\\/:*?\"<>|\r\n]+\\\\)*)([^\\\\/:*?\"<>|\r\n]*)";
    public static final String PATH_REGEX_UNIX = "((/+[a-zA-Z0-9._-]+(/?[a-zA-Z0-9._-]+)*/?))";

    private GBase8aIEUtil() throws Exception {
        throw new Exception("The class can not be instantiated.");
    }

    public static void getClusterTableNames(String selDBName, JDBCSession session, List<GBase8aIEClusterObject> clusterObjectList) throws Exception {
        try {
            Statement stmt = session.getOriginal().createStatement();
            String sql = "/**studio-data-export*/show distribution tables";
            ResultSet rs = stmt.executeQuery(sql);
            List<GBase8aIEClusterObject> tableList = new ArrayList();
            while (rs.next()) {
                String dbName = rs.getString("dbName").trim().toLowerCase();
                String tableName = rs.getString("tbName").trim().toLowerCase();
                if (dbName.equalsIgnoreCase(selDBName)) {
                    GBase8aIEClusterObject clusterObject = new GBase8aIEClusterObject();
                    clusterObject.setObjectName(tableName);
                    clusterObject.setType("TABLE");
                    tableList.add(clusterObject);
                }
            }

            GBase8aIEClusterObject[] tables = new GBase8aIEClusterObject[tableList.size()];
            tables = tableList.toArray(tables);
            Arrays.sort(tables);
            int i = 0;

            for (int len = tables.length; i < len; ++i) {
                clusterObjectList.add(tables[i]);
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            throw e;
        }
    }

    public static void getClusterViewNames(String selDBName, JDBCSession session, List<GBase8aIEClusterObject> clusterObjectList) throws Exception {
        String[] viewTypes = new String[]{"VIEW"};
        try (JDBCResultSet dbResult = session.getMetaData().getTables(selDBName, selDBName, "%", viewTypes)) {
            while (dbResult.next()) {
                String type = JDBCUtils.safeGetString(dbResult, "TABLE_TYPE");
                String name = JDBCUtils.safeGetString(dbResult, "TABLE_NAME");
                if (type.equalsIgnoreCase("VIEW")) {
                    GBase8aIEClusterObject clusterObject = new GBase8aIEClusterObject();
                    clusterObject.setObjectName(name);
                    clusterObject.setType("VIEW");
                    clusterObjectList.add(clusterObject);
                }
            }
        }

    }

    public static void getClusterProcedureNames(String selDBName, JDBCSession session, List<GBase8aIEClusterObject> clusterObjectList) throws Exception {
        PreparedStatement pstmt = null;
        ResultSet res = null;
        String strSQL = "/**studio-data-export*/SELECT routine_name FROM information_schema.routines WHERE routine_type = 'PROCEDURE' AND routine_schema = ?";

        try {
            session.getOriginal().setCatalog(selDBName);
            pstmt = session.getOriginal().prepareStatement(strSQL);
            pstmt.setString(1, selDBName);
            res = pstmt.executeQuery();

            while (res.next()) {
                String name = (new String(res.getBytes("routine_name"), "UTF8")).trim();
                GBase8aIEClusterObject clusterObject = new GBase8aIEClusterObject();
                clusterObject.setObjectName(name);
                clusterObject.setType("PROCEDURE");
                clusterObjectList.add(clusterObject);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException var16) {
                }
            }

            if (res != null) {
                try {
                    res.close();
                } catch (SQLException var15) {
                }
            }

        }

    }

    public static void getClusterFunctionNames(String selDBName, JDBCSession session, List<GBase8aIEClusterObject> clusterObjectList) throws Exception {
        String strSQL = "/**studio-data-export*/SELECT routine_name,routine_type FROM information_schema.routines WHERE (routine_type = 'FUNCTION' OR routine_type = 'FUNC_EXP') AND routine_schema = ?";
        try (PreparedStatement pstmt = session.getOriginal().prepareStatement(strSQL)) {
            pstmt.setString(1, selDBName);
            try (ResultSet res = pstmt.executeQuery()) {
                while (res.next()) {
                    String procedureName = (new String(res.getBytes("routine_name"), "UTF8")).trim();
                    procedureName = procedureName.trim();
                    res.getString("routine_type");
                    GBase8aIEClusterObject clusterObject = new GBase8aIEClusterObject();
                    clusterObject.setObjectName(procedureName);
                    clusterObject.setType("FUNCTION");
                    clusterObjectList.add(clusterObject);
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static void writeFile(String text, String filePath, String coding) throws IOException {

        try (FileOutputStream fos = new FileOutputStream(filePath); OutputStreamWriter osw = new OutputStreamWriter(fos, coding)) {
            osw.write(text);
            osw.flush();
        } catch (IOException e) {
            throw e;
        }

    }

    public static void appendWriteFile(String text, String filePath, String coding) throws IOException {

        try (FileOutputStream fos = new FileOutputStream(filePath, true); OutputStreamWriter osw = new OutputStreamWriter(fos, coding)) {
            osw.write(text);
            osw.flush();
        } catch (IOException e) {
            throw e;
        }

    }

    public static void showErrorMessageBox(String message, Composite container) {
        MessageBox errorBox = new MessageBox(container.getShell(), 33);
        errorBox.setText("Error");
        errorBox.setMessage(message);
        errorBox.open();
    }

    public static String genScriptOfTable(String databaseName, String tableName, JDBCSession session) throws Exception {
        String tableSQL = null;
        String strSQL = "/**studio-data-export*/SHOW CREATE TABLE `" + databaseName + "`.`" + tableName + "`;";
        try (Statement pstmt = session.getOriginal().createStatement(); ResultSet res = pstmt.executeQuery(strSQL)) {
            while (res.next()) {
                tableSQL = res.getString(2);
                if (tableSQL.indexOf("TABLESPACE") != -1) {
                    Pattern pattern = Pattern.compile("[^\\']*\\'[^\\']*\\'");
                    Matcher matcher = pattern.matcher(tableSQL.substring(tableSQL.indexOf("TABLESPACE"), tableSQL.length()));
                    if (matcher.find()) {
                        tableSQL = tableSQL.replace(matcher.group(), "");
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        }

        return tableSQL;
    }

    public static String genFullScriptOfTable(String databaseName, String tableName, JDBCSession session) throws Exception {
        String tableSQL = null;
        Statement pstmt = null;
        ResultSet res = null;
        String strSQL = "/**studio-data-export*/SHOW CREATE TABLE `" + databaseName + "`.`" + tableName + "`;";

        try {
            pstmt = session.getOriginal().createStatement();

            for (res = pstmt.executeQuery(strSQL); res.next(); tableSQL = res.getString(2)) {
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException var16) {
                }
            }

            if (res != null) {
                try {
                    res.close();
                } catch (SQLException var15) {
                }
            }

        }

        return tableSQL;
    }

    public static String genScriptOfView(String databaseName, String viewName, JDBCSession session) throws Exception {
        String tableSQL = null;
        Statement pstmt = null;
        ResultSet res = null;
        String strSQL = "/**studio-data-export*/SHOW CREATE VIEW `" + databaseName + "`.`" + viewName + "`;";

        try {
            Connection conn = session.getOriginal();
            conn.setCatalog(databaseName);
            pstmt = conn.createStatement();

            for (res = pstmt.executeQuery(strSQL); res.next(); tableSQL = res.getString(2)) {
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException var16) {
                }
            }

            if (res != null) {
                try {
                    res.close();
                } catch (SQLException var15) {
                }
            }

        }

        return tableSQL;
    }

    public static String genScriptOfProcedure(String databaseName, String procedureName, JDBCSession session) throws Exception {
        String createSQL = null;
        Statement statement = null;
        ResultSet res = null;
        String queryDelimiter = ";";
        String delimiter = "$$";
        String qualifiedName = "`" + procedureName + "`";
        String alterSQL = "DROP Procedure IF EXISTS " + qualifiedName + " " + queryDelimiter + "\n\n";

        try {
            Exception e;
            try {
                e = null;
                String strSQL = "/**studio-data-export*/SHOW CREATE PROCEDURE `" + databaseName + "`.`" + procedureName + "`";
                statement = session.getOriginal().createStatement();
                res = statement.executeQuery(strSQL);
                if (res.next()) {
                    createSQL = res.getString(3);
                    createSQL = alterSQL + "DELIMITER " + delimiter + "\n\n" + createSQL + " " + delimiter + "\n\n";
                }
            } catch (Exception var21) {
                e = var21;
                throw e;
            }
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException var20) {
                }
            }

            if (res != null) {
                try {
                    res.close();
                } catch (SQLException var19) {
                }
            }

        }

        return createSQL;
    }

    public static String genScriptOfFunction(String databaseName, String procedureName, JDBCSession session) throws Exception {
        String createSQL = null;
        Statement statement = null;
        ResultSet res = null;

        try {
            Exception e;
            try {
                e = null;
                String ctype = "function";
                String strSQL = "/**studio-data-export*/";
                if (ctype.equalsIgnoreCase("func_exp")) {
                    strSQL = "SHOW CREATE FUNCTION_EXP";
                } else {
                    strSQL = "SHOW CREATE " + ctype;
                }

                strSQL = strSQL.concat(" ").concat("`").concat(databaseName).concat("`").concat(".").concat("`").concat(procedureName).concat("`");
                statement = session.getOriginal().createStatement();
                res = statement.executeQuery(strSQL);
                if (res.next()) {
                    createSQL = res.getString(3);
                    if (ctype.equalsIgnoreCase("func_exp")) {
                        createSQL = createSQL.replaceFirst("\\s+DEFINER=`[\\w.]+`@`[\\w%.]+`+", "");
                        createSQL = createSQL.replaceFirst("\\s+CHARSET\\s+[\\w]+", "");
                    }

                    createSQL = "DELIMITER |\n\n" + createSQL + " |";
                }
            } catch (Exception var18) {
                e = var18;
                throw e;
            }
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException var17) {
                }
            }

            if (res != null) {
                try {
                    res.close();
                } catch (SQLException var16) {
                }
            }

        }

        return createSQL;
    }

    public static BigInteger getTableCount(String databaseName, String tableName, JDBCSession session) throws Exception {
        Statement pstmt = null;
        ResultSet rs = null;
        String strSQL = "/**studio-data-export*/select count(*) from `" + databaseName + "`.`" + tableName + "`;";

        BigInteger var8;
        try {
            pstmt = session.getOriginal().createStatement();
            rs = pstmt.executeQuery(strSQL);
            if (!rs.next()) {
                return new BigInteger("0");
            }

            var8 = new BigInteger(rs.getString(1));
        } catch (Exception e) {
            throw e;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException var18) {
                }
            }

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException var17) {
                }
            }

        }

        return var8;
    }

    public static BigInteger getMaxAllowedPacket(JDBCSession session, Composite container) {
        Statement pstmt = null;
        ResultSet rs = null;
        String strSQL = "/**studio-data-export*/show variables like 'max_allowed_packet' ";

        BigInteger var7;
        try {
            pstmt = session.getOriginal().createStatement();
            rs = pstmt.executeQuery(strSQL);
            if (!rs.next()) {
                return new BigInteger("0");
            }

            var7 = new BigInteger(rs.getString(2));
        } catch (Exception e) {
            showErrorMessageBox(e.toString(), container);
            return new BigInteger("0");
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException var19) {
                }
            }

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException var18) {
                }
            }

        }

        return var7;
    }

    public static boolean isExcludedByFilter(String name, String[] filterExpressions) {
        if (filterExpressions != null && filterExpressions.length != 0) {
            for (String filterExpression : filterExpressions) {
                String regex = filterExpression.trim();
                regex = replaceChar(regex, '?', ".");
                regex = replaceChar(regex, '*', ".*");
                if (!regex.isEmpty() && name.matches(regex)) {
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public static void execUpdate(JDBCSession session, String script, String catalog) throws Exception {
        String multiCommentRegex = "(?ms)(/\\*)([^+].*?)(\\*/)";
        String semicolon = "&semicolon&";
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = session.getOriginal();
            conn.setCatalog(catalog);
            stmt = conn.createStatement();

            String querySql;
            for (GBase8aQueryTokenizer qt = new GBase8aQueryTokenizer(script, ";", "", "--", multiCommentRegex); qt.hasQuery(); stmt.execute(querySql)) {
                querySql = qt.nextQuery();
                if (querySql.contains(semicolon)) {
                    querySql = querySql.replaceAll(semicolon, ";");
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
            }

        }

    }

    public static boolean isTableExist(String databaseName, String tableName, JDBCSession session) throws Exception {
        boolean flag = false;
        int count = 0;
        String strSQL = "/**studio-data-export*/select count(1) from information_schema.tables where table_schema='" + databaseName + "' and table_name='" + tableName + "'";

        try (Statement pstmt = session.getOriginal().createStatement(); ResultSet res = pstmt.executeQuery(strSQL)) {
            if (res.next()) {
                count = res.getInt(1);
            }

            if (count > 0) {
                flag = true;
            }
        } catch (Exception e) {
            throw e;
        }

        return flag;
    }

    public static boolean isObjectExist(String databaseName, String objName, JDBCSession session, String objType) throws Exception {
        boolean flag = false;
        int count = 0;
        Statement pstmt = null;
        ResultSet res = null;
        String strSQL = "/**studio-data-export*/";
        if ("TABLE".equals(objType)) {
            strSQL = "select count(1) from information_schema.tables where table_schema='" + databaseName + "' and table_name='" + objName + "' and table_type='BASE TABLE'";
        } else if ("VIEW".equals(objType)) {
            strSQL = "select count(1) from information_schema.tables where table_schema='" + databaseName + "' and table_name='" + objName + "' and table_type='VIEW'";
        } else if ("FUNCTION".equals(objType)) {
            strSQL = "select count(1) from information_schema.routines where routine_schema='" + databaseName + "' and routine_name='" + objName + "' and routine_type='FUNCTION'";
        } else if ("PROCEDURE".equals(objType)) {
            strSQL = "select count(1) from information_schema.routines where routine_schema='" + databaseName + "' and routine_name='" + objName + "' and routine_type='PROCEDURE'";
        }

        try {
            pstmt = session.getOriginal().createStatement();
            res = pstmt.executeQuery(strSQL);
            if (res.next()) {
                count = res.getInt(1);
            }

            if (count > 0) {
                flag = true;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException var18) {
                }
            }

            if (res != null) {
                try {
                    res.close();
                } catch (SQLException var17) {
                }
            }

        }

        return flag;
    }

    public static boolean isFilePathValid(String path) {
        boolean flag = false;
        String regex;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            regex = "(\\b[a-zA-Z]:)\\\\((?:[^\\\\/:*?\"<>|\r\n]+\\\\)*)([^\\\\/:*?\"<>|\r\n]*)";
        } else {
            regex = "((/+[a-zA-Z0-9._-]+(/?[a-zA-Z0-9._-]+)*/?))";
        }

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(path);
        flag = m.matches();
        return flag;
    }

    public static String replaceChar(String inputString, char replaceFrom, String replaceTo) {
        if (inputString != null && !inputString.isEmpty()) {
            StringBuffer buffer = new StringBuffer();
            char[] input = inputString.toCharArray();

            for (char c : input) {
                if (c == replaceFrom) {
                    buffer.append(replaceTo);
                } else {
                    buffer.append(c);
                }
            }

            return buffer.toString();
        } else {
            return inputString;
        }
    }
}
