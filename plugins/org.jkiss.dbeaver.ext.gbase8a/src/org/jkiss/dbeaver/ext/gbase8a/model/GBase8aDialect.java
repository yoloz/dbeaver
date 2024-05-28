package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCDatabaseMetaData;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCDataSource;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCSQLDialect;
import org.jkiss.dbeaver.model.impl.sql.BasicSQLDialect;
import org.jkiss.dbeaver.model.sql.SQLConstants;
import org.jkiss.dbeaver.model.sql.SQLDialect;
import org.jkiss.utils.ArrayUtils;


class GBase8aDialect extends JDBCSQLDialect {
    public static final String[] GBASE8A_NON_TRANSACTIONAL_KEYWORDS = (String[]) ArrayUtils.concatArrays(
            BasicSQLDialect.NON_TRANSACTIONAL_KEYWORDS, (Object[]) new String[]{
                    "USE", "SHOW", "CREATE", "ALTER", "DROP",
                    "EXPLAIN", "DESCRIBE", "DESC"
            });

    public GBase8aDialect() {
        super("GBASE8A", "gbase8a");
    }

    public void initBaseDriverSettings(JDBCSession session, JDBCDataSource dataSource, JDBCDatabaseMetaData metaData) {
        super.initDriverSettings(session, dataSource, metaData);
        addTableQueryKeywords(SQLConstants.KEYWORD_EXPLAIN, "EXPLAIN", "DESCRIBE", "DESC");
//        Collections.addAll(this.tableQueryWords, new String[]{"EXPLAIN", "DESCRIBE", "DESC"});
    }

    @Override
    public void initDriverSettings(JDBCSession session, JDBCDataSource dataSource, JDBCDatabaseMetaData metaData) {
        initBaseDriverSettings(session, dataSource, metaData);

    }

    @Nullable
    public String getScriptDelimiterRedefiner() {
        return "DELIMITER";
    }


    @NotNull
    public SQLDialect.MultiValueInsertMode getMultiValueInsertMode() {
        return SQLDialect.MultiValueInsertMode.GROUP_ROWS;
    }


    public boolean supportsAliasInSelect() {
        return true;
    }


    public boolean supportsCommentQuery() {
        return true;
    }


    public String[] getSingleLineComments() {
        return new String[]{"-- ", "#"};
    }


    public String getTestSQL() {
        return "SELECT 1";
    }

    @NotNull
    public String[] getNonTransactionKeywords() {
        return GBASE8A_NON_TRANSACTIONAL_KEYWORDS;
    }
}
