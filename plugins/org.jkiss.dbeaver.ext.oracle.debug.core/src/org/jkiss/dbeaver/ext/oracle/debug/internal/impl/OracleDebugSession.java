/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2024 DBeaver Corp and others
 * Copyright (C) 2017-2018 Andrew Khitrin (ahitrin@gmail.com)
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

package org.jkiss.dbeaver.ext.oracle.debug.internal.impl;

import com.google.gson.reflect.TypeToken;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.debug.*;
import org.jkiss.dbeaver.debug.jdbc.DBGJDBCSession;
import org.jkiss.dbeaver.ext.oracle.debug.OracleDebugConstants;
import org.jkiss.dbeaver.ext.oracle.model.OracleDataSource;
import org.jkiss.dbeaver.model.DBInfoUtils;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCRemoteInstance;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.VoidProgressMonitor;
import org.jkiss.utils.CommonUtils;
import org.jkiss.utils.IOUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Typical scenario for debug session <br/>
 * <br/>
 * To debug server-side code, you must have two database sessions: one session
 * to run the code in debug mode (the target session), and a second session to
 * supervise the target session (the debug session). The target session becomes
 * available for debugging by making initializing calls with DBMS_DEBUG. This
 * marks the session so that the PL/SQL interpreter runs in debug mode and
 * generates debug events. As debug events are generated, they are posted from
 * the session. In most cases, debug events require return notification: the
 * interpreter pauses awaiting a reply. Meanwhile, the debug session must also
 * initialize itself using DBMS_DEBUG: This tells it which target session to
 * supervise. The debug session may then call entry points in DBMS_DEBUG to read
 * events that were posted from the target session and to communicate with the
 * target session.
 */
public class OracleDebugSession extends DBGJDBCSession {

    private static final Log log = Log.getLog(OracleDebugSession.class);

    private final JDBCExecutionContext debugSessionContext;
    protected final JDBCExecutionContext targetSessionContext;
    private OracleJDBCWorker targetSessionWorker;

    private boolean terminatedOrFinished;
    private boolean isAttached;
    private boolean synchronizeSuccess;

    private final DBRProgressMonitor voidMonitor = new VoidProgressMonitor();

    // 暂时手动初始化局部变量，一个frame默认初始化10个空白变量
    private final Map<Integer, Map<String, String>> variables;
    private final Pattern pattern;
    private int stackLevel = 0;

    OracleDebugSession(DBRProgressMonitor monitor, DBGBaseController controller) throws DBGException {
        super(controller);
        this.terminatedOrFinished = false;
        this.isAttached = false;
        this.synchronizeSuccess = false;
        this.variables = new HashMap<>();
        this.pattern = Pattern.compile("^\\$[0-9]+$");
        OracleDataSource dataSource = (OracleDataSource) controller.getDataSourceContainer().getDataSource();
        try {
            JDBCRemoteInstance instance = dataSource.getDefaultInstance();
            this.targetSessionContext = (JDBCExecutionContext) instance.openIsolatedContext(monitor, "target session",
                    null);
            log.debug("Target session created.");
            this.debugSessionContext = (JDBCExecutionContext) instance.openIsolatedContext(monitor, "debug session",
                    null);
            log.debug("Debug session created.");
        } catch (DBException e) {
            log.error(String.format("Error creating session %s", e.getMessage()));
            throw new DBGException(e, dataSource);
        }
    }

    @Override
    public JDBCExecutionContext getControllerConnection() {
        return debugSessionContext;
    }

    protected void fireEvent(DBGEvent event) {
        getController().fireEvent(event);
    }

    // DECLARE RESULT NUMBER; "CTR" NUMBER; BEGIN "CTR" := '3';
    // RESULT := "TEST"."LOOP_TESTER"("CTR" => "CTR");
    // DBMS_OUTPUT.PUT_LINE('Return'||' = '|| RESULT); END;
    private String assembleExecSql(String owner, String objectName, List<OracleDebugVariable> parameters) {
        StringBuilder builder = new StringBuilder();
        String output = null;
        // declare parameters
        if (parameters != null && !parameters.isEmpty()) {
            builder.append("DECLARE ");
            for (OracleDebugVariable variable : parameters) {
                String pname = variable.getName();
                String kind = variable.getKind();
                if ("RETURN".equalsIgnoreCase(kind)) { // output result
                    output = pname;
                } else {
                    pname = "\"" + pname + "\"";
                }
                String dataType = variable.getDataType();
                int index = dataType.indexOf("(");
                if (index > 0) {
                    dataType = dataType.substring(0, index);
                }
                dataType = switch (dataType) {
                    case "varchar2", "VARCHAR2" -> "VARCHAR2(32767)";
                    default -> dataType;
                };
                builder.append(pname).append(" ").append(dataType).append("; ");
            }
        }
        // body
        builder.append("BEGIN ");
        if (parameters != null && !parameters.isEmpty()) {
            for (OracleDebugVariable variable : parameters) {
                String kind = variable.getKind();
                if (!"RETURN".equalsIgnoreCase(kind)) {
                    builder.append("\"").append(variable.getName()).append("\"").append(" := ").append("'").append(variable.getVal())
                            .append("'; ");
                }
            }
        }
        if (output != null) {
            builder.append(output).append(" := ");
        }
        builder.append("\"").append(owner).append("\"").append(".").append("\"").append(objectName).append("\"(");
        if (parameters != null && !parameters.isEmpty()) {
            StringBuilder paramBuilder = new StringBuilder();
            for (OracleDebugVariable variable : parameters) {
                String pname = variable.getName();
                if (!"RETURN".equalsIgnoreCase(variable.getKind())) {
                    paramBuilder.append("\"").append(pname).append("\"").append(" => ").append("\"").append(pname)
                            .append("\",");
                }
            }
            builder.append(paramBuilder.substring(0, paramBuilder.length() - 1));
        }
        builder.append("); ");
        if (output != null) {
            builder.append("DBMS_OUTPUT.PUT_LINE('Return'||' = '|| ").append(output).append("); ");
        }
        builder.append("END;");
        return builder.toString();
    }

    /**
     * This method attach debug session to debug object (procedure) and wait forever
     * while target or any (depend on targetPID) session will run target procedure
     */
    public void attach(DBRProgressMonitor monitor, String sessionId) throws DBException {
        try (CallableStatement stmt = debugSessionContext.getConnection(new VoidProgressMonitor())
                .prepareCall(OracleDebugConstants.SQL_ATTACH)) {
            stmt.setString("sessionId", sessionId);
            stmt.execute();
            this.isAttached = true;
        } catch (SQLException e) {
            closeSession(monitor);
            throw new DBGException("SQL error", e);
        }
    }

    // execute function which wait for debug
    public void executeFunction(DBRProgressMonitor monitor, Map<String, Object> configuration) throws DBGException {
        String owner = CommonUtils.toString(configuration.get(OracleDebugConstants.ATTR_SCHEMA_NAME));
        String objectName = CommonUtils.toString(configuration.get(OracleDebugConstants.ATTR_FUNCTION_NAME));
        String listStr = CommonUtils.toString(configuration.get(OracleDebugConstants.ATTR_FUNCTION_ARGUMENTS), "[]");
        List<OracleDebugVariable> arguments = DBInfoUtils.SECRET_GSON.fromJson(listStr,
                new TypeToken<List<OracleDebugVariable>>() {
                }.getType());
        String sql_text = assembleExecSql(owner, objectName, arguments);
        String taskName = "Oracle Debug - target session execute debug object";
        DBGEvent begin = new DBGEvent(this, DBGEvent.RESUME, DBGEvent.MODEL_SPECIFIC);
        DBGEvent end = new DBGEvent(this, DBGEvent.SUSPEND, DBGEvent.MODEL_SPECIFIC);
        targetSessionWorker = new OracleJDBCWorker(this, taskName, sql_text, begin, end);
        targetSessionWorker.schedule();
        log.debug("Target session executed and hang.");
    }

    // debug session wait for target session event
    public void debugSessionSynchronize(DBRProgressMonitor monitor) throws DBGException {
        try (CallableStatement statement = debugSessionContext.getConnection(new VoidProgressMonitor())
                .prepareCall(OracleDebugConstants.SQL_DEBUG_SYNCHRONIZE)) {
            statement.registerOutParameter("1", Types.INTEGER);
            statement.registerOutParameter("2", Types.INTEGER);
            statement.execute();
            int result = statement.getInt("1");
            if (result == 0) {
                int terminated = statement.getInt("2");
                if (terminated == 1) {
                    this.terminatedOrFinished = true;
                    fireEvent(new DBGEvent(this, DBGEvent.TERMINATE, DBGEvent.MODEL_SPECIFIC));
                } else {
                    this.synchronizeSuccess = true;
                    fireEvent(new DBGEvent(this, DBGEvent.SUSPEND, DBGEvent.MODEL_SPECIFIC));
                }
            } else {
                throw new SQLException(OracleDebugException.getMessage(result));
            }
        } catch (SQLException e) {
            closeSession(monitor);
            throw new DBGException("SQL error", e);
        }
    }

    protected void doDetach(DBRProgressMonitor monitor) {
        String detachSQL = OracleDebugConstants.SQL_DETACH_SESSION;
        if (!terminatedOrFinished) {
            detachSQL = composeAbortCommand();
        }
        log.debug("Debug session detach.");
        try (CallableStatement dbStat = debugSessionContext.getConnection(voidMonitor).prepareCall(detachSQL)) {
            dbStat.execute();
        } catch (SQLException e) {
            log.error("Unable to detach debug session.", e);
        }
    }

    @Override
    public DBGSessionInfo getSessionInfo() {
        return null;
    }

    @Override
    public void addBreakpoint(DBRProgressMonitor monitor, DBGBreakpointDescriptor descriptor) throws DBGException {
        String sqlQuery = composeAddBreakpointCommand(descriptor);
        OracleDebugBreakpointDescriptor debugBreakpointDescriptor = (OracleDebugBreakpointDescriptor) descriptor;
        try (CallableStatement stmt = debugSessionContext.getConnection(new VoidProgressMonitor()).prepareCall(sqlQuery)) {
            stmt.setString("fucName", debugBreakpointDescriptor.getUnitName());
            stmt.setString("owner", debugBreakpointDescriptor.getUnitOwner());
            stmt.setInt("line", debugBreakpointDescriptor.getLineNum());
            stmt.registerOutParameter("1", Types.INTEGER);
            stmt.registerOutParameter("2", Types.INTEGER);
            stmt.execute();
            int result = stmt.getInt("1");
            if (result == 0) {
                int point = stmt.getInt("2");
                debugBreakpointDescriptor.setPointNum(point);
            } else {
                throw new SQLException(OracleDebugException.getMessage(result));
            }
        } catch (SQLException e) {
            throw new DBGException("SQL error", e);
        }
        breakpoints.add(descriptor);
    }

    protected String composeAddBreakpointCommand(DBGBreakpointDescriptor descriptor) {
        return OracleDebugConstants.SQL_SET_BREAKPOINT;
    }

    @Override
    public void removeBreakpoint(DBRProgressMonitor monitor, DBGBreakpointDescriptor bp) throws DBGException {
        if (breakpoints.isEmpty()) {
            return;
        }
        String sqlCommand = composeRemoveBreakpointCommand(bp);
        OracleDebugBreakpointDescriptor debugBreakpointDescriptor = (OracleDebugBreakpointDescriptor) bp;
        int pointNum = 0;
        for (DBGBreakpointDescriptor breakpoint : breakpoints) {
            OracleDebugBreakpointDescriptor breakpointDescriptor = (OracleDebugBreakpointDescriptor) breakpoint;
            if (breakpointDescriptor.getUnitName().equals(debugBreakpointDescriptor.getUnitName())
                    && breakpointDescriptor.getUnitOwner().equals(debugBreakpointDescriptor.getUnitOwner())
                    && breakpointDescriptor.getLineNum().equals(debugBreakpointDescriptor.getLineNum())) {
                pointNum = breakpointDescriptor.getPointNum();
                break;
            }
        }
        if (pointNum <= 0) {
            return;
        }
        try (CallableStatement stmt = debugSessionContext.getConnection(new VoidProgressMonitor()).prepareCall(sqlCommand)) {
            stmt.setInt("pointNumber", pointNum);
            stmt.registerOutParameter("1", Types.INTEGER);
            stmt.execute();
            int result = stmt.getInt("1");
            if (result == 0) {
                breakpoints.remove(bp);
            } else {
                throw new SQLException(OracleDebugException.getMessage(result));
            }
        } catch (SQLException e) {
            throw new DBGException("SQL error", e);
        }
    }

    protected String composeRemoveBreakpointCommand(DBGBreakpointDescriptor breakpointDescriptor) {
        return OracleDebugConstants.SQL_DROP_BREAKPOINT;
    }

    @Override
    public void execContinue() throws DBGException {
        DBGEvent begin = new DBGEvent(this, DBGEvent.RESUME, DBGEvent.RESUME);
        try (CallableStatement stmt = debugSessionContext.getConnection(voidMonitor)
                .prepareCall(OracleDebugConstants.SQL_CONTINUE)) {
            fireEvent(begin);
            stmt.registerOutParameter("1", Types.INTEGER);
            stmt.registerOutParameter("2", Types.INTEGER);
            stmt.execute();
            int result = stmt.getInt("1");
            if (result == 0) {
                int terminated = stmt.getInt("2");
                if (terminated == 1) {
                    this.terminatedOrFinished = true;
                    fireEvent(new DBGEvent(this, DBGEvent.TERMINATE, DBGEvent.STEP_END));
                } else {
                    fireEvent(new DBGEvent(this, DBGEvent.SUSPEND, DBGEvent.RESUME));
                }
            } else {
                throw new SQLException(OracleDebugException.getMessage(result));
            }
        } catch (SQLException e) {
            throw new DBGException("SQL error", e);
        }
    }

    @Override
    public void execStepInto() throws DBGException {
        try (CallableStatement stmt = debugSessionContext.getConnection(voidMonitor)
                .prepareCall(OracleDebugConstants.SQL_STEP_INTO)) {
            fireEvent(new DBGEvent(this, DBGEvent.RESUME, DBGEvent.STEP_INTO));
            stmt.registerOutParameter("1", Types.INTEGER);
            stmt.registerOutParameter("2", Types.INTEGER);
            stmt.execute();
            int result = stmt.getInt("1");
            if (result == 0) {
                int terminated = stmt.getInt("2");
                if (terminated == 1) {
                    this.terminatedOrFinished = true;
                    fireEvent(new DBGEvent(this, DBGEvent.TERMINATE, DBGEvent.STEP_END));
                } else {
                    fireEvent(new DBGEvent(this, DBGEvent.SUSPEND, DBGEvent.STEP_INTO));
                }
            } else {
                throw new SQLException(OracleDebugException.getMessage(result));
            }
        } catch (SQLException e) {
            throw new DBGException("SQL error", e);
        }
    }

    @Override
    public void execStepOver() throws DBGException {
        try (CallableStatement stmt = debugSessionContext.getConnection(voidMonitor)
                .prepareCall(OracleDebugConstants.SQL_STEP_OVER)) {
            fireEvent(new DBGEvent(this, DBGEvent.RESUME, DBGEvent.STEP_OVER));
            stmt.registerOutParameter("1", Types.INTEGER);
            stmt.registerOutParameter("2", Types.INTEGER);
            stmt.execute();
            int result = stmt.getInt("1");
            if (result == 0) {
                int terminated = stmt.getInt("2");
                if (terminated == 1) {
                    this.terminatedOrFinished = true;
                    fireEvent(new DBGEvent(this, DBGEvent.TERMINATE, DBGEvent.STEP_END));
                } else {
                    fireEvent(new DBGEvent(this, DBGEvent.SUSPEND, DBGEvent.STEP_OVER));
                }
            } else {
                throw new SQLException(OracleDebugException.getMessage(result));
            }
        } catch (SQLException e) {
            throw new DBGException("SQL error", e);
        }
    }

    @Override
    public void execStepReturn() throws DBGException {
        try (CallableStatement stmt = debugSessionContext.getConnection(voidMonitor)
                .prepareCall(OracleDebugConstants.SQL_STEP_RETURN)) {
            fireEvent(new DBGEvent(this, DBGEvent.RESUME, DBGEvent.STEP_RETURN));
            stmt.registerOutParameter("1", Types.INTEGER);
            stmt.registerOutParameter("2", Types.INTEGER);
            stmt.execute();
            int result = stmt.getInt("1");
            if (result == 0) {
                int terminated = stmt.getInt("2");
                if (terminated == 1) {
                    this.terminatedOrFinished = true;
                    fireEvent(new DBGEvent(this, DBGEvent.TERMINATE, DBGEvent.STEP_END));
                } else {
                    fireEvent(new DBGEvent(this, DBGEvent.SUSPEND, DBGEvent.STEP_RETURN));
                }
            } else {
                throw new SQLException(OracleDebugException.getMessage(result));
            }
        } catch (SQLException e) {
            throw new DBGException("SQL error", e);
        }
    }

    @Override
    public void resume() throws DBGException {
        execContinue();
    }

    @Override
    public void suspend() throws DBGException {
        throw new DBGException("Suspend not implemented");
    }

    protected String composeAbortCommand() {
        return OracleDebugConstants.SQL_ABORT;
    }

    @Override
    public List<DBGVariable<?>> getVariables(DBGStackFrame stack) throws DBGException {
        Map<String, String> variablesMap;
        if (stack != null) {
            variablesMap = selectFrame(stack.getLevel());
            try (CallableStatement statement = debugSessionContext.getConnection(voidMonitor).prepareCall(OracleDebugConstants.SQL_GET_VARS)) {
                for (String variable : variablesMap.keySet()) {
                    if (!this.pattern.matcher(variable).find()) {
                        statement.setString("variable", variable);
                        statement.registerOutParameter("1", Types.INTEGER);
                        statement.registerOutParameter("2", Types.VARCHAR);
                        statement.execute();
                        int result = statement.getInt("1");
                        if (result == 0) {
                            variablesMap.put(variable, statement.getString("2"));
                        } else if (result == 32) { // value is null
                            variablesMap.put(variable, "");
                        } else {
                            log.warn(variable + "=>:" + OracleDebugException.getMessage(result));
                        }
                    }
                }
            } catch (SQLException e) {
                throw new DBGException("SQL error", e);
            }
        } else {
            variablesMap = new HashMap<>();
        }
        //首先真实变量
        List<DBGVariable<?>> vars = new ArrayList<>(10);
        for (Map.Entry<String, String> entry : variablesMap.entrySet()) {
            String name = entry.getKey();
            if (!this.pattern.matcher(name).find()) {
                OracleDebugVariable debugVariable = new OracleDebugVariable();
                debugVariable.setName(name);
                debugVariable.setVal(entry.getValue());
                vars.add(debugVariable);
            }
        }
        //然后填充可使用变量
        for (int i = vars.size(); i < 10; i++) {
            OracleDebugVariable variable = new OracleDebugVariable();
            variable.setName("$" + i);
            variable.setVal("");
            vars.add(variable);
        }
        return vars;
    }

    @Override
    public void setVariableVal(DBGVariable<?> variable, Object value) throws DBGException {
        Map<String, String> variablesMap = this.variables.get(stackLevel);
        if (variablesMap == null) {
            return;
        }
        fireEvent(new DBGEvent(this, DBGEvent.RESUME, DBGEvent.MODEL_SPECIFIC));
        String key = variable.getName();
        //value不为空，输入框已做判断
        String[] array = String.valueOf(value).split(",");
        String variableName = key, variableVal = "";
        if (this.pattern.matcher(key).find()) {
            variablesMap.remove(key);
            variableName = array[0];
            if (array.length > 1) {
                variableVal = array[1];
            }
        } else {
            if (array.length == 1) {
                variableVal = array[0];
            } else {
                variablesMap.remove(key);
                variableName = array[0];
                variableVal = array[1];
            }
        }
        if (variableVal == null || variableVal.isEmpty() || variableVal.isBlank()) {
            variablesMap.put(variableName, "");
            fireEvent(new DBGEvent(this, DBGEvent.SUSPEND, DBGEvent.STEP_RETURN));
            return;
        }
        try (CallableStatement stmt = debugSessionContext.getConnection(new VoidProgressMonitor())
                .prepareCall(OracleDebugConstants.SQL_SET_VAR)) {
            if (variable instanceof OracleDebugVariable) {
                stmt.setString("assignment", variableName + " := " + variableVal + ";");
                stmt.registerOutParameter("1", Types.INTEGER);
                stmt.execute();
                int result = stmt.getInt("1");
                if (result == 0) {
                    variablesMap.put(variableName, variableVal);
                    fireEvent(new DBGEvent(this, DBGEvent.SUSPEND, DBGEvent.STEP_RETURN));
                    log.debug("set variable value[" + variableName + " := " + value + ";]");
                } else {
                    variablesMap.put(variableName, "");
                    fireEvent(new DBGEvent(this, DBGEvent.SUSPEND, DBGEvent.STEP_RETURN));
                    log.debug(OracleDebugException.getMessage(result));
                }
            } else {
                variablesMap.put(variableName, "");
                fireEvent(new DBGEvent(this, DBGEvent.SUSPEND, DBGEvent.STEP_RETURN));
                log.error("Incorrect variable class:" + variable.getClass().getName());
            }
        } catch (SQLException e) {
            log.error("Error setting var: " + e.getMessage());
            throw new DBGException("SQL error", e);
        }
    }

    @Override
    public List<DBGStackFrame> getStack() throws DBGException {
        List<DBGStackFrame> stack = new ArrayList<>(1);
        try (CallableStatement stmt = debugSessionContext.getConnection(new VoidProgressMonitor())
                .prepareCall(OracleDebugConstants.SQL_GET_STACK)) {
            stmt.registerOutParameter("1", Types.VARCHAR);
            stmt.execute();
            String bufferText = stmt.getString("1");
            String[] traces = bufferText.split("<nav_hr>");
            for (int i = 0; i < traces.length; i++) {
                String[] trace = traces[i].split("<nav>");
                OracleDebugStackFrame debugStackFrame = new OracleDebugStackFrame(CommonUtils.toInt(trace[0], 0),
                        trace[1], trace[2], CommonUtils.toInt(trace[3], 0), CommonUtils.toInt(trace[4], 0), i);
                stack.add(debugStackFrame);
            }
        } catch (SQLException e) {
            log.error("Error loading stack frame: " + e.getMessage());
            throw new DBGException("SQL error", e);
        }
        return stack;
    }

    @Override
    public String getSource(DBGStackFrame stack) throws DBGException {
        if (stack instanceof OracleDebugStackFrame debugStackFrame) {
            try (CallableStatement stmt = debugSessionContext.getConnection(new VoidProgressMonitor())
                    .prepareCall(OracleDebugConstants.SQL_GET_SRC)) {
                log.debug("Get stack frame [" + debugStackFrame.getOwner() + "." + debugStackFrame.getName() + "]source.");
                stmt.setString("owner", debugStackFrame.getOwner());
                stmt.setString("name", debugStackFrame.getName());
                stmt.registerOutParameter("1", Types.VARCHAR);
                stmt.execute();
                return stmt.getString("1");
            } catch (SQLException e) {
                log.error(String.format("Unable to get source %s", e.getMessage()));
                throw new DBGException("SQL error", e);
            }
        }
        String message = String.format("Unable to get source for stack %s", stack);
        throw new DBGException(message);
    }


    public Map<String, String> selectFrame(int frameNumber) throws DBGException {
        this.stackLevel = frameNumber;
        return this.variables.computeIfAbsent(frameNumber, v -> new HashMap<>());
    }

    @Override
    public Integer getSessionId() {
        return 0;
    }

    @Override
    public boolean canStepInto() {
        return !terminatedOrFinished;
    }

    @Override
    public boolean canStepOver() {
        return !terminatedOrFinished;
    }

    @Override
    public boolean canStepReturn() {
        return !terminatedOrFinished;
    }

    /**
     * Return true if debug session up and running on server
     *
     * @return boolean
     */
    public boolean isAttached() {
        return isAttached;
    }

    /**
     * Return true if session waiting target connection (on breakpoint, after step
     * or continue) in debug thread
     *
     * @return boolean
     */
    public boolean isDone() {
        return synchronizeSuccess;
    }

    @Override
    public void closeSession(DBRProgressMonitor monitor) throws DBGException {
        if (!isAttached()) {
            return;
        }
        log.debug("Closing session.");
        try {
            doDetach(monitor);
        } finally {
            if (targetSessionWorker != null) {
                targetSessionWorker.cancel();
                targetSessionWorker = null;
            }
            if (targetSessionContext != null) {
                IOUtils.close(targetSessionContext);
            }
            if (debugSessionContext != null) {
                IOUtils.close(debugSessionContext);
            }
        }
    }

}
