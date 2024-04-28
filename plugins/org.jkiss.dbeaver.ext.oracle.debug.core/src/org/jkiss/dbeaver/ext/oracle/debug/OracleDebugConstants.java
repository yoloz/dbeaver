/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2024 DBeaver Corp and others
 * Copyright (C) 2017-2018 Alexander Fedorov (alexander.fedorov@jkiss.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ext.oracle.debug;

public class OracleDebugConstants {

    public static final String ATTR_SCHEMA_NAME = "ORACLE.ATTR_SCHEMA_NAME"; //$NON-NLS-1$
    public static final String ATTR_FUNCTION_NAME = "ORACLE.ATTR_FUNCTION_NAME"; //$NON-NLS-1$
    public static final String ATTR_FUNCTION_ARGUMENTS = "ORACLE.ATTR_FUNCTION_PARAMETERS"; //$NON-NLS-1$

    public static final String DEBUG_TYPE_FUNCTION = "org.jkiss.dbeaver.ext.oracle.debug.function";

    public static final String SQL_ATTACH = """
            DECLARE
                debug_session_id VARCHAR2(255);
            BEGIN
                debug_session_id := :sessionId;
                SYS.DBMS_DEBUG.ATTACH_SESSION(debug_session_id);
            END;
            """;
    public static final String SQL_DEBUG_SYNCHRONIZE = """
            DECLARE
            	run_info SYS.DBMS_DEBUG.RUNTIME_INFO;
                info_requested BINARY_INTEGER;
                retval BINARY_INTEGER;
            BEGIN
                info_requested := SYS.DBMS_DEBUG.info_getStackDepth + SYS.DBMS_DEBUG.info_getLineInfo + SYS.DBMS_DEBUG.info_getBreakpoint + SYS.DBMS_DEBUG.info_getOerInfo;
            	retval := SYS.DBMS_DEBUG.SYNCHRONIZE(run_info, 0);
            	:1 := retval;
            	:2 := run_info.terminated;
            END;
            """;
    public static final String SQL_GET_SRC = """
            DECLARE
                source_buffer VARCHAR2(32767) := '';
                powner VARCHAR2(255) := '';
                pname VARCHAR2(255) := '';
            BEGIN
                powner := :owner;
                pname := :name;
            	FOR rec IN(SELECT text FROM sys.all_source WHERE owner = powner AND name = pname) LOOP
            		source_buffer := source_buffer || rec.text;
            	END LOOP;
            	:1 := source_buffer;
            END;
            """;
    public static final String SQL_GET_VARS = """
            DECLARE
                value_name VARCHAR2(100);
            	retval binary_integer;
            	value_buffer VARCHAR2(32767) := '';
            BEGIN
                value_name := :variable;
            	retval := SYS.DBMS_DEBUG.GET_VALUE(value_name, 0, value_buffer);
            	:1 := retval;
            	:2 := value_buffer;
            END;
            """;
    public static final String SQL_SET_VAR = """
            DECLARE
            	retval binary_integer;
            	assignment_statement VARCHAR2(32767) := '';
            BEGIN
                assignment_statement := :assignment;
            	retval := SYS.DBMS_DEBUG.SET_VALUE(0, assignment_statement);
            	:1 := retval;
            END;
            """;
    public static final String SQL_GET_STACK = """
            DECLARE
            	backtrace_table SYS.DBMS_DEBUG.BACKTRACE_TABLE;
            	i NUMBER;
            	string_buffer VARCHAR2(32767) := '';
            BEGIN
            	SYS.DBMS_DEBUG.PRINT_BACKTRACE(backtrace_table);
            	i := backtrace_table.first();
            	WHILE i IS NOT NULL LOOP
            	    string_buffer := backtrace_table(i).namespace || '<nav>' || backtrace_table(i).name || '<nav>' || backtrace_table(i).owner || '<nav>' || backtrace_table(i).line# || '<nav>' || backtrace_table(i).libunittype || '<nav_hr>' || string_buffer;
            	    i := backtrace_table.next(i);
            	END LOOP;
            	:1 := string_buffer;
            END;
            """;
    public static final String SQL_STEP_OVER = """
            DECLARE
                 run_info SYS.DBMS_DEBUG.RUNTIME_INFO;
                 info_requested BINARY_INTEGER;
                 retval BINARY_INTEGER;
            BEGIN
                info_requested := SYS.DBMS_DEBUG.info_getStackDepth + SYS.DBMS_DEBUG.info_getLineInfo + SYS.DBMS_DEBUG.info_getBreakpoint + SYS.DBMS_DEBUG.info_getOerInfo;
                retval := SYS.DBMS_DEBUG.CONTINUE(run_info, SYS.DBMS_DEBUG.break_next_line, info_requested);
                --IF run_info.reason = 8 THEN
                    --retval := SYS.DBMS_DEBUG.CONTINUE(run_info, 0, info_requested);
                --END IF;
                :1 := retval;
            	:2 := run_info.terminated;
            END;
            """;
    public static final String SQL_STEP_INTO = """
            DECLARE
                 run_info SYS.DBMS_DEBUG.RUNTIME_INFO;
                 info_requested BINARY_INTEGER;
                 retval BINARY_INTEGER;
            BEGIN
                info_requested := SYS.DBMS_DEBUG.info_getStackDepth + SYS.DBMS_DEBUG.info_getLineInfo + SYS.DBMS_DEBUG.info_getBreakpoint + SYS.DBMS_DEBUG.info_getOerInfo;
                retval := SYS.DBMS_DEBUG.CONTINUE(run_info, SYS.DBMS_DEBUG.break_any_call, info_requested);
                --IF run_info.reason = 8 THEN
                    --retval := SYS.DBMS_DEBUG.CONTINUE(run_info, 0, info_requested);
                --END IF;
                :1 := retval;
            	:2 := run_info.terminated;
            END;
            """;
    public static final String SQL_CONTINUE = """
            DECLARE
                 run_info SYS.DBMS_DEBUG.RUNTIME_INFO;
                 info_requested BINARY_INTEGER;
                 retval BINARY_INTEGER;
            BEGIN
                info_requested := SYS.DBMS_DEBUG.info_getStackDepth + SYS.DBMS_DEBUG.info_getLineInfo + SYS.DBMS_DEBUG.info_getBreakpoint + SYS.DBMS_DEBUG.info_getOerInfo;
                retval := SYS.DBMS_DEBUG.CONTINUE(run_info, 0, info_requested);
                :1 := retval;
            	:2 := run_info.terminated;
            END;
            """;
    public static final String SQL_STEP_RETURN = """
            DECLARE
                 run_info SYS.DBMS_DEBUG.RUNTIME_INFO;
                 info_requested BINARY_INTEGER;
                 retval BINARY_INTEGER;
            BEGIN
                info_requested := SYS.DBMS_DEBUG.info_getStackDepth + SYS.DBMS_DEBUG.info_getLineInfo + SYS.DBMS_DEBUG.info_getBreakpoint + SYS.DBMS_DEBUG.info_getOerInfo;
                retval := SYS.DBMS_DEBUG.CONTINUE(run_info, SYS.DBMS_DEBUG.break_any_return, info_requested);
                --IF run_info.reason = 8 THEN
                    --retval := SYS.DBMS_DEBUG.CONTINUE(run_info, 0, info_requested);
                --END IF;
                :1 := retval;
            	:2 := run_info.terminated;
            END;
            """;
    public static final String SQL_ABORT = """
            DECLARE
                 run_info SYS.DBMS_DEBUG.RUNTIME_INFO;
                 info_requested BINARY_INTEGER;
                 retval BINARY_INTEGER;
            BEGIN
                info_requested := SYS.DBMS_DEBUG.info_getStackDepth + SYS.DBMS_DEBUG.info_getLineInfo + SYS.DBMS_DEBUG.info_getBreakpoint + SYS.DBMS_DEBUG.info_getOerInfo;
                retval := SYS.DBMS_DEBUG.CONTINUE(run_info, SYS.DBMS_DEBUG.abort_execution, info_requested);
                retval := SYS.DBMS_DEBUG.CONTINUE(run_info, 0, 0);
            END;
            """;
    public static final String SQL_DETACH_SESSION = """
            BEGIN
                SYS.DBMS_DEBUG.DETACH_SESSION;
            END;
            """;
    public static final String SQL_SET_BREAKPOINT = """
            DECLARE
                varPROGRAM SYS.DBMS_DEBUG.PROGRAM_INFO;
                varLine BINARY_INTEGER;
                retval BINARY_INTEGER;
                pointnum BINARY_INTEGER;
            BEGIN
                varPROGRAM.Namespace := SYS.DBMS_DEBUG.Namespace_pkgspec_or_toplevel;
                varPROGRAM.NAME := :fucName;
                varPROGRAM.Owner := :owner;
                varPROGRAM.DBLink := null; --no dblink access
                varLine := :line;
                retval := SYS.DBMS_DEBUG.SET_BREAKPOINT(varPROGRAM, varLine, pointnum);
                :1 := retval;
                :2 := pointnum;
            END;
            """;
    public static final String SQL_DROP_BREAKPOINT = """
            DECLARE
                retval BINARY_INTEGER;
                pointnum BINARY_INTEGER;
            BEGIN
                pointnum := :pointNumber;
                retval := SYS.DBMS_DEBUG.DELETE_BREAKPOINT(pointnum);
                :1 := retval;
            END;
            """;

    public static final String SQL_QUERY_SESSION_ID = """
            BEGIN
                :retvar := SYS.DBMS_DEBUG.INITIALIZE(null, 0);
            END;
            """;

    public static final String SQL_SESSION_DEBUG = "ALTER SESSION SET PLSQL_DEBUG = true";
    public static final String SQL_DEBUG_ON = """
            BEGIN
                SYS.DBMS_DEBUG.DEBUG_ON(true,false);
            END;
            """;

    public static final String SQL_ARGUMENT = """
            SELECT * FROM SYS.ALL_ARGUMENTS WHERE
            	OWNER = ?
            	AND OBJECT_NAME = ?
            	AND DATA_LEVEL = 0
            	AND PACKAGE_NAME IS NULL
            ORDER BY POSITION
            """;

}
