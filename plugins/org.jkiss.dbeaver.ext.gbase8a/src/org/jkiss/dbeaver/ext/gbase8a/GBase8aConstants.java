package org.jkiss.dbeaver.ext.gbase8a;

import org.jkiss.dbeaver.ext.gbase8a.GBase8aMessages;
import org.jkiss.dbeaver.model.struct.rdb.DBSIndexType;

import java.util.HashMap;


public class GBase8aConstants {
    public static final int DEFAULT_PORT = 5258;
    public static final String DEFAULT_HOST = "localhost";
    public static final String DRIVER_ID_GBASE8A = "gbase8a";
    public static final String HANDLER_SSL = "gbase8a_ssl";
    public static final String PROP_REQUIRE_SSL = "ssl.require";
    public static final String PROP_VERIFY_SERVER_SERT = "ssl.verify.server";
    public static final String PROP_SSL_CIPHER_SUITES = "ssl.cipher.suites";
    public static final String PROP_SSL_PUBLIC_KEY_RETRIEVE = "ssl.public.key.retrieve";
    public static final String PROP_SSL_CLIENT_CERT = "ssl.client.cert";
    public static final String PROP_SSL_CLIENT_KEY = "ssl.client.key";
    public static final String PROP_SSL_CA_CERT = "ssl.ca.cert";
    public static final String PROP_SSL_DEBUG = "ssl.debug";
    public static final String PROP_VC_NAME = "vcName";
    public static final String[] TABLE_TYPES = new String[]{"TABLE", "VIEW", "LOCAL TEMPORARY"};

    public static final String INFO_SCHEMA_NAME = "information_schema";

    public static final String GBASE8A_SCHEMA_NAME = "gbase";

    public static final String PERFORMANCE_SCHEMA = "performance_schema";

    public static final String GCLUSTERDB = "gclusterdb";

    public static final String GCTEMDB = "gctmpdb";

    public static final String META_TABLE_ENGINES = "information_schema.ENGINES";

    public static final String META_TABLE_SCHEMATA = "information_schema.SCHEMATA";

    public static final String META_TABLE_TABLES = "information_schema.TABLES";

    public static final String META_TABLE_ROUTINES = "information_schema.ROUTINES";

    public static final String META_TABLE_TRIGGERS = "information_schema.TRIGGERS";

    public static final String META_TABLE_COLUMNS = "information_schema.COLUMNS";

    public static final String META_TABLE_TABLE_CONSTRAINTS = "information_schema.TABLE_CONSTRAINTS";

    public static final String META_TABLE_KEY_COLUMN_USAGE = "information_schema.KEY_COLUMN_USAGE";

    public static final String META_TABLE_STATISTICS = "information_schema.STATISTICS";

    public static final String META_TABLE_PARTITIONS = "information_schema.PARTITIONS";

    public static final String META_TABLE_VIEWS = "information_schema.VIEWS";
    public static final String COL_ENGINE_NAME = "ENGINE";
    public static final String COL_ENGINE_SUPPORT = "SUPPORT";
    public static final String COL_ENGINE_DESCRIPTION = "COMMENT";
    public static final String COL_ENGINE_SUPPORT_TXN = "TRANSACTIONS";
    public static final String COL_ENGINE_SUPPORT_XA = "XA";
    public static final String COL_ENGINE_SUPPORT_SAVEPOINTS = "SAVEPOINTS";
    public static final String COL_CATALOG_NAME = "CATALOG_NAME";
    public static final String COL_SCHEMA_NAME = "SCHEMA_NAME";
    public static final String COL_DEFAULT_CHARACTER_SET_NAME = "DEFAULT_CHARACTER_SET_NAME";
    public static final String COL_DEFAULT_COLLATION_NAME = "DEFAULT_COLLATION_NAME";
    public static final String COL_SQL_PATH = "SQL_PATH";
    public static final String COL_TABLE_SCHEMA = "TABLE_SCHEMA";
    public static final String COL_TABLE_NAME = "TABLE_NAME";
    public static final String COL_VC_NAME = "TABLE_VC";
    public static final String COL_TABLE_TYPE = "TABLE_TYPE";
    public static final String COL_ENGINE = "ENGINE";
    public static final String COL_VERSION = "VERSION";
    public static final String COL_TABLE_ROWS = "ROWS";
    public static final String COL_CREATE_USRE = "USER";
    public static final String COL_TABLE_COMMENT = "TABLE_COMMENT";
    public static final String COL_TABLE_LIMIT_SIZE = "LIMIT_STORAGE_SIZE";
    public static final String COL_COLUMNS_NAME = "COLUMNS_NAME";
    public static final String COL_ORDINAL_POSITION = "ORDINAL_POSITION";
    public static final String COL_CREATE_TIME = "CREATE_TIME";
    public static final String COL_UPDATE_TIME = "UPDATE_TIME";
    public static final String COL_CHECK_TIME = "CHECK_TIME";
    public static final String COL_COLLATION = "COLLATION";
    public static final String COL_COLLATION_NAME = "COLLATION_NAME";
    public static final String COL_NULLABLE = "NULLABLE";
    public static final String COL_SUB_PART = "SUB_PART";
    public static final String COL_AVG_ROW_LENGTH = "AVG_ROW_LENGTH";
    public static final String COL_DATA_LENGTH = "DATA_LENGTH";
    public static final String COL_INDEX_NAME = "INDEX_NAME";
    public static final String COL_INDEX_TYPE = "INDEX_TYPE";
    public static final String COL_SEQ_IN_INDEX = "SEQ_IN_INDEX";
    public static final String COL_NON_UNIQUE = "NON_UNIQUE";
    public static final String COL_COMMENT = "COMMENT";
    public static final String COL_COLUMN_NAME = "COLUMN_NAME";
    public static final String COL_COLUMN_KEY = "COLUMN_KEY";
    public static final String COL_DATA_TYPE = "DATA_TYPE";
    public static final String COL_CHARACTER_MAXIMUM_LENGTH = "CHARACTER_MAXIMUM_LENGTH";
    public static final String COL_CHARACTER_OCTET_LENGTH = "CHARACTER_OCTET_LENGTH";
    public static final String COL_NUMERIC_PRECISION = "NUMERIC_PRECISION";
    public static final String COL_NUMERIC_SCALE = "NUMERIC_SCALE";
    public static final String COL_COLUMN_DEFAULT = "COLUMN_DEFAULT";
    public static final String COL_IS_NULLABLE = "IS_NULLABLE";
    public static final String COL_IS_UPDATABLE = "IS_UPDATABLE";
    public static final String COL_COLUMN_COMMENT = "COLUMN_COMMENT";
    public static final String COL_COLUMN_HASH = "COLUMN_HASH";
    public static final String COL_COLUMN_EXTRA = "EXTRA";
    public static final String COL_COLUMN_TYPE = "COLUMN_TYPE";
    public static final String COL_ROUTINE_SCHEMA = "ROUTINE_SCHEMA";
    public static final String COL_ROUTINE_NAME = "ROUTINE_NAME";
    public static final String COL_ROUTINE_VC = "ROUTINE_VC";
    public static final String COL_ROUTINE_TYPE = "ROUTINE_TYPE";
    public static final String COL_DTD_IDENTIFIER = "DTD_IDENTIFIER";
    public static final String COL_ROUTINE_BODY = "ROUTINE_BODY";
    public static final String COL_ROUTINE_DEFINITION = "ROUTINE_DEFINITION";
    public static final String COL_EXTERNAL_NAME = "EXTERNAL_NAME";
    public static final String COL_EXTERNAL_LANGUAGE = "EXTERNAL_LANGUAGE";
    public static final String COL_PARAMETER_STYLE = "PARAMETER_STYLE";
    public static final String COL_IS_DETERMINISTIC = "IS_DETERMINISTIC";
    public static final String COL_SQL_DATA_ACCESS = "SQL_DATA_ACCESS";
    public static final String COL_SECURITY_TYPE = "SECURITY_TYPE";
    public static final String COL_ROUTINE_COMMENT = "ROUTINE_COMMENT";
    public static final String COL_DEFINER = "DEFINER";
    public static final String COL_CHARACTER_SET_CLIENT = "CHARACTER_SET_CLIENT";
    public static final String COL_TRIGGER_SCHEMA = "TRIGGER_SCHEMA";
    public static final String COL_TRIGGER_NAME = "TRIGGER_NAME";
    public static final String COL_TRIGGER_EVENT_MANIPULATION = "EVENT_MANIPULATION";
    public static final String COL_TRIGGER_EVENT_OBJECT_SCHEMA = "EVENT_OBJECT_SCHEMA";
    public static final String COL_TRIGGER_EVENT_OBJECT_TABLE = "EVENT_OBJECT_TABLE";
    public static final String COL_TRIGGER_ACTION_ORDER = "ACTION_ORDER";
    public static final String COL_TRIGGER_ACTION_CONDITION = "ACTION_CONDITION";
    public static final String COL_TRIGGER_ACTION_STATEMENT = "ACTION_STATEMENT";
    public static final String COL_TRIGGER_ACTION_ORIENTATION = "ACTION_ORIENTATION";
    public static final String COL_TRIGGER_ACTION_TIMING = "ACTION_TIMING";
    public static final String COL_TRIGGER_SQL_MODE = "SQL_MODE";
    public static final String COL_TRIGGER_DEFINER = "DEFINER";
    public static final String COL_TRIGGER_CHARACTER_SET_CLIENT = "CHARACTER_SET_CLIENT";
    public static final String COL_TRIGGER_COLLATION_CONNECTION = "COLLATION_CONNECTION";
    public static final String COL_TRIGGER_DATABASE_COLLATION = "DATABASE_COLLATION";
    public static final String COL_CONSTRAINT_NAME = "CONSTRAINT_NAME";
    public static final String COL_CONSTRAINT_TYPE = "CONSTRAINT_TYPE";
    public static final String CONSTRAINT_FOREIGN_KEY = "FOREIGN KEY";
    public static final String CONSTRAINT_PRIMARY_KEY = "PRIMARY KEY";
    public static final String CONSTRAINT_UNIQUE = "UNIQUE";
    public static final String INDEX_PRIMARY = "PRIMARY";
    public static final String TYPE_NAME_ENUM = "enum";
    public static final String TYPE_NAME_SET = "set";
    public static final DBSIndexType INDEX_TYPE_BTREE = new DBSIndexType("BTREE", "BTree");
    public static final DBSIndexType INDEX_TYPE_FULLTEXT = new DBSIndexType("FULLTEXT", "Full Text");
    public static final DBSIndexType INDEX_TYPE_HASH = new DBSIndexType("HASH", "Hash");
    public static final DBSIndexType INDEX_TYPE_RTREE = new DBSIndexType("RTREE", "RTree");

    public static final String COL_CHARSET = "CHARSET";

    public static final String COL_DESCRIPTION = "DESCRIPTION";

    public static final String COL_MAX_LEN = "MAXLEN";

    public static final String COL_ID = "ID";

    public static final String COL_DEFAULT = "DEFAULT";

    public static final String COL_COMPILED = "COMPILED";

    public static final String COL_SORT_LENGTH = "SORTLEN";

    public static final String COL_PARTITION_NAME = "PARTITION_NAME";

    public static final String COL_SUBPARTITION_NAME = "SUBPARTITION_NAME";

    public static final String COL_PARTITION_ORDINAL_POSITION = "PARTITION_ORDINAL_POSITION";

    public static final String COL_SUBPARTITION_ORDINAL_POSITION = "SUBPARTITION_ORDINAL_POSITION";

    public static final String COL_PARTITION_METHOD = "PARTITION_METHOD";

    public static final String COL_SUBPARTITION_METHOD = "SUBPARTITION_METHOD";
    public static final String COL_PARTITION_EXPRESSION = "PARTITION_EXPRESSION";
    public static final String COL_SUBPARTITION_EXPRESSION = "SUBPARTITION_EXPRESSION";
    public static final String COL_PARTITION_DESCRIPTION = "PARTITION_DESCRIPTION";
    public static final String COL_PARTITION_COMMENT = "PARTITION_COMMENT";
    public static final String PARTITION_METHOD_RANGE = "RANGE";
    public static final String PARTITION_METHOD_LIST = "LIST";
    public static final String PARTITION_METHOD_HASH = "HASH";
    public static final String PARTITION_METHOD_KEY = "KEY";
    public static final String PARTITION_METHOD_LINEAR_HASH = "LINEAR HASH";
    public static final String PARTITION_METHOD_LINEAR_KEY = "LINEAR KEY";
    public static final String COL_MAX_DATA_LENGTH = "MAX_DATA_LENGTH";
    public static final String COL_INDEX_LENGTH = "INDEX_LENGTH";
    public static final String COL_NODEGROUP = "NODEGROUP";
    public static final String COL_DATA_FREE = "DATA_FREE";
    public static final String COL_CHECKSUM = "CHECKSUM";
    public static final String COL_CHECK_OPTION = "CHECK_OPTION";
    public static final String COL_VIEW_DEFINITION = "VIEW_DEFINITION";
    public static final String TYPE_VARCHAR = "varchar";
    public static final String BIN_FOLDER = "bin";
    public static final String ENV_VARIABLE_GBASE8A_PWD = "GBASE_PWD";
    public static final String CLUSTER_SYS_USERNAME = "cluster_sys_username";
    public static final String CLUSTER_SYS_PASSWORD = "cluster_sys_password";
    public static final String ANALYSIS_TIME_INTERVAL = "analysis_time_interval";
    public static final String SSL_KEYSTORE = "javax.net.ssl.keyStore";
    public static final String SSL_KEYSTOREPASSWORD = "javax.net.ssl.keyStorePassword";
    public static final String SSL_TRUSTSTORE = "javax.net.ssl.trustStore";
    public static final String SSL_TRUSTSTOREPASSWORD = "javax.net.ssl.trustStorePassword";
    public static final String RESOURCE_POOL_TYPE_STATIC = "static";
    public static final String RESOURCE_POOL_TYPE_DYNAMIC = "dynamic";
    public static final String RESOURCE_DIRECTIVE_DEFAULT_NAME = "_default";
    public static final String[] TYPES = new String[]{"BIGINT", "BLOB", "CHAR", "DATE",
            "DATETIME", "DECIMAL", "DOUBLE", "FLOAT", "INT", "INTEGER",
            "SMALLINT", "TEXT", "LONGTEXT", "TIME", "TIMESTAMP", "TINYINT", "VARCHAR", "BINARY", "VARBINARY", "LONGBLOB", "NUMERIC"};
    public static final String[] TF = new String[]{GBase8aMessages.constants_true, GBase8aMessages.constants_false};

    public static HashMap<String, Integer> TYPES_INDEX = new HashMap<String, Integer>();

    public static final String PRIVILEGE_GRANT_OPTION_NAME = "Grant option";

    public static HashMap<String, Integer> getTYPES_INDEX() {
        TYPES_INDEX.put("BIGINT", Integer.valueOf(1));
        TYPES_INDEX.put("BLOB", Integer.valueOf(1));
        TYPES_INDEX.put("CHAR", Integer.valueOf(2));
        TYPES_INDEX.put("DATE", Integer.valueOf(1));
        TYPES_INDEX.put("DATETIME", Integer.valueOf(1));
        TYPES_INDEX.put("DECIMAL", Integer.valueOf(3));
        TYPES_INDEX.put("DOUBLE", Integer.valueOf(3));
        TYPES_INDEX.put("FLOAT", Integer.valueOf(3));
        TYPES_INDEX.put("INT", Integer.valueOf(1));
        TYPES_INDEX.put("INTEGER", Integer.valueOf(1));
        TYPES_INDEX.put("SMALLINT", Integer.valueOf(1));
        TYPES_INDEX.put("TEXT", Integer.valueOf(1));
        TYPES_INDEX.put("LONGTEXT", Integer.valueOf(1));
        TYPES_INDEX.put("TIME", Integer.valueOf(1));
        TYPES_INDEX.put("TIMESTAMP", Integer.valueOf(1));
        TYPES_INDEX.put("TINYINT", Integer.valueOf(1));
        TYPES_INDEX.put("VARCHAR", Integer.valueOf(2));
        TYPES_INDEX.put("LONGBLOB", Integer.valueOf(1));
        TYPES_INDEX.put("BINARY", Integer.valueOf(1));
        TYPES_INDEX.put("VARBINARY", Integer.valueOf(2));
        TYPES_INDEX.put("NUMERIC", Integer.valueOf(3));
        return TYPES_INDEX;
    }
}
