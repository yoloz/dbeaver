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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.debug.*;
import org.jkiss.dbeaver.debug.core.DebugUtils;
import org.jkiss.dbeaver.debug.jdbc.DBGJDBCSession;
import org.jkiss.dbeaver.ext.oracle.debug.OracleDebugConstants;
import org.jkiss.dbeaver.ext.oracle.debug.core.OracleSqlDebugCore;
import org.jkiss.dbeaver.ext.oracle.debug.internal.OracleDebugCoreMessages;
import org.jkiss.dbeaver.ext.oracle.model.OracleConstants;
import org.jkiss.dbeaver.ext.oracle.model.OracleDataSource;
import org.jkiss.dbeaver.ext.oracle.model.OracleExecutionContext;
import org.jkiss.dbeaver.ext.oracle.model.OracleProcedureArgument;
import org.jkiss.dbeaver.ext.oracle.model.OracleProcedurePackaged;
import org.jkiss.dbeaver.ext.oracle.model.OracleProcedureStandalone;
import org.jkiss.dbeaver.ext.oracle.model.OracleSchema;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.exec.DBCExecutionPurpose;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCCallableStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCDataSource;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCDataSourceInfo;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCRemoteInstance;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.runtime.AbstractJob;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.VoidProgressMonitor;
import org.jkiss.dbeaver.model.struct.rdb.DBSProcedureType;
import org.jkiss.utils.CommonUtils;
import org.jkiss.utils.IOUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Typical scenario for debug session <br/>
 * <br/>
 * 0. create session (now it can only attached to target Procedure)<br/>
 * <br/>
 * 1. attach to target this method attaches to a debugging target and listening
 * on the given port - waiting for run procedure in other session(s) debugger
 * client should invoke this function after creation also created implicit
 * breakpoint for target procedure, after this call debug session in
 * <b>WAITING</b> state - isDone returns false and is isWaiting returns
 * true<br/>
 * <br/>
 * 2. when target procedure will called debug session implicit breakpoint will
 * be reached and session goes in state <b>READY</b> (isDone - true, isWaiting -
 * true) in this state possible to call getStack, getVariables, setVariables,
 * setBreakpoint or execStepXXX\continue<br/>
 * <br/>
 * 3. when execStepXXX or continue will called session goes in <b>WAITING</b>
 * state until next breakpoint or end of procedure will be reached <br/>
 */
public class OracleDebugSession extends DBGJDBCSession {
	private final JDBCExecutionContext controllerConnection;

	private int functionOid = -1;
	private String sessionId;

	private OracleDebugAttachKind attachKind = OracleDebugAttachKind.UNKNOWN;
	private DBGSessionInfo sessionInfo;

	private volatile Job localWorkerJob = null;

	private OracleDebugBreakpointDescriptor bpGlobal;

	private static final int LOCAL_WAIT_MS = 500; // 0.5 sec

	private static final int LOCAL_TIMEOT_MS = 50 * (2 * LOCAL_WAIT_MS); // 50 sec

	private static final String MAGIC_PORT = "PLDBGBREAK";

	private static final String SQL_CHECK_PLUGIN = "select 'Server version: ' || serverversionstr || '.\nProxy API version: ' ||  proxyapiver from pldbg_get_proxy_info()";

	private static final String SQL_ATTACH = "select pldbg_wait_for_target(?sessionid)";
//	private static final String SQL_ATTACH_TO_PORT = "select pldbg_attach_to_port(?portnumber)";
	private static final String SQL_ATTACH_TO_SESSION = "CALL DBMS_DEBUG.ATTACH_SESSION('?sessionid')";
	private static final String SQL_PREPARE_SLOT = " select pldbg_oid_debug(?objectid)";
	private static final String SQL_LISTEN = "select pldbg_create_listener() as sessionid";
	private static final String SQL_GET_SRC = "select pldbg_get_source(?sessionid,?oid)";
	private static final String SQL_GET_VARS = "select * from pldbg_get_variables(?sessionid)";
	private static final String SQL_SET_VAR = "select pldbg_deposit_value(?,?,?,?)";
	private static final String SQL_GET_STACK = "select * from pldbg_get_stack(?sessionid)";
	private static final String SQL_SELECT_FRAME = "select * from pldbg_select_frame(?sessionid,?frameno)";
	private static final String SQL_STEP_OVER = "select pldbg_step_over(?sessionid)";
	private static final String SQL_STEP_INTO = "select pldbg_step_into(?sessionid)";
	private static final String SQL_CONTINUE = "select pldbg_continue(?sessionid)";
	private static final String SQL_ABORT = "select pldbg_abort_target(?sessionid)";
	private static final String SQL_SET_GLOBAL_BREAKPOINT = "select pldbg_set_global_breakpoint(?sessionid, ?obj, ?line, ?target)";
	private static final String SQL_SET_BREAKPOINT = "select pldbg_set_breakpoint(?sessionid, ?obj, ?line)";
	private static final String SQL_DROP_BREAKPOINT = "select pldbg_drop_breakpoint(?sessionid, ?obj, ?line)";

	private static final String SQL_CURRENT_SESSION = "SELECT pid,usename,application_name,state,query\n"
			+ "FROM pg_stat_activity WHERE pid = pg_backend_pid()"; //$NON-NLS-1$

	private static final Log log = Log.getLog(OracleDebugSession.class);

	/**
	 * Create session with two description after creation session need to be
	 * attached to oracle procedure by attach method
	 */
	OracleDebugSession(DBRProgressMonitor monitor, DBGBaseController controller) throws DBGException {
		super(controller);
		log.debug("Creating controller session.");

		OracleDataSource dataSource = (OracleDataSource) controller.getDataSourceContainer().getDataSource();
		try {
			log.debug("Controller session creating.");
			JDBCRemoteInstance instance;
//			if (isGlobalSession(controller.getDebugConfiguration())) {
			instance = dataSource.getDefaultInstance();
//			} else {
//				OracleProcedureStandalone function = OracleSqlDebugCore.resolveFunction(monitor,
//						controller.getDataSourceContainer(), controller.getDebugConfiguration());
//				instance = function.getSchema();
//			}
			this.controllerConnection = (JDBCExecutionContext) instance.openIsolatedContext(monitor,
					"Debug controller session", null);

			log.debug("Debug controller session created.");
			JDBCDataSource src = this.controllerConnection.getDataSource();
			if (src instanceof OracleDataSource) {
				OracleDataSource oracleSrc = (OracleDataSource) src;
//				log.debug(String.format("Active user %s", schema.getUser().getName()));
//				log.debug(String.format("Active schema %s", schema.getName()));
				if (oracleSrc.getInfo() instanceof JDBCDataSourceInfo) {
					JDBCDataSourceInfo JDBCinfo = (JDBCDataSourceInfo) oracleSrc.getInfo();
					log.debug("------------DATABASE DRIVER INFO---------------");
					log.debug(String.format("Database Product Name %s", JDBCinfo.getDatabaseProductName()));
					log.debug(String.format("Database Product Version %s", JDBCinfo.getDatabaseProductVersion()));
					log.debug(String.format("Database Version %s", JDBCinfo.getDatabaseVersion()));
					log.debug(String.format("Driver Name %s", JDBCinfo.getDriverName()));
					log.debug(String.format("Driver Version %s", JDBCinfo.getDriverVersion()));
					log.debug("-----------------------------------------------");
				} else {
					log.debug("No additional Driver info");
				}
			} else {
				log.debug("Unknown Driver version");
			}

		} catch (DBException e) {
			log.debug(String.format("Error creating debug session %s", e.getMessage()));
			throw new DBGException(e, dataSource);
		}
	}

	@Override
	public JDBCExecutionContext getControllerConnection() {
		return controllerConnection;
	}

	@NotNull
	protected Job runLocalProc(@NotNull JDBCExecutionContext connection, @NotNull OracleProcedurePackaged function,
			@NotNull List<String> paramValues, @NotNull String name,
			@NotNull CompletableFuture<JDBCCallableStatement> asyncStatement) throws DBGException {
		List<OracleProcedureArgument> parameters = Collections.emptyList();// function.getParameters(mointor);
		log.debug("Run local proc");
		if (parameters.size() != paramValues.size()) {
			String unmatched = "Parameter value count (" + paramValues.size()
					+ ") doesn't match actual function parameters (" + parameters.size() + ")";
			log.debug(unmatched);
			throw new DBGException(unmatched);
		}
		Job job = new AbstractJob(name) {
			@Override
			protected IStatus run(DBRProgressMonitor monitor) {
				try (JDBCSession session = connection.openSession(monitor, DBCExecutionPurpose.USER,
						"Run SQL command")) {
					JDBCCallableStatement statement = null;
					try {
						StringBuilder query = new StringBuilder();
						if (function.getProcedureType() == DBSProcedureType.PROCEDURE) {
							query.append("{ CALL ");
						} else {
							query.append("SELECT ");
						}
						query.append(function.getFullyQualifiedName(DBPEvaluationContext.DML)).append("(");
						for (int i = 0; i < parameters.size(); i++) {
							if (i > 0) {
								query.append(",");
							}
							String paramValue = paramValues.get(i);
							if (CommonUtils.isEmpty(paramValue)) {
								throw new DBGException(
										NLS.bind(OracleDebugCoreMessages.OracleDebugCore_parameters_not_set_message,
												parameters.get(i).getName()));
							}
							switch (parameters.get(i).getDataKind()) {
							case NUMERIC:
								if (!CommonUtils.isNumber(paramValue)) {
									throw new DBGException(NLS.bind(
											OracleDebugCoreMessages.OracleDebugCore_parameter_type_not_fit_message,
											new Object[] { parameters.get(i).getName(), "numeric", paramValue }));
								}
								break;
							case BOOLEAN:
								if (!paramValue.equalsIgnoreCase("true") && !paramValue.equalsIgnoreCase("false")) {
									throw new DBGException(NLS.bind(
											OracleDebugCoreMessages.OracleDebugCore_parameter_type_not_fit_message,
											new Object[] { parameters.get(i).getName(), "boolean", paramValue }));
								}
								break;
							default:
								break;
							}
							query.append(paramValue);
						}
						query.append(")");
						if (function.getProcedureType() == DBSProcedureType.PROCEDURE) {
							query.append(" }");
						}

						log.debug(String.format("Prepared local call %s", query));
						statement = session.prepareCall(query.toString());

						/*
						 * for (int i = 0; i < parameters.size(); i++) { PostgreProcedureParameter
						 * parameter = parameters.get(i); String paramValue = paramValues.get(i);
						 * DBDValueHandler valueHandler = DBUtils.findValueHandler(session, parameter);
						 * valueHandler.bindValueObject(session, localStatement, parameter, i,
						 * paramValue); }
						 */
						asyncStatement.complete(statement);
						statement.execute();
						// And Now His Watch Is Ended
						log.debug("Local statement executed (ANHWIE)");
						fireEvent(new DBGEvent(this, DBGEvent.RESUME, DBGEvent.STEP_RETURN));
					} catch (Exception e) {
						log.debug("Error execute local statement: " + e.getMessage());
						if (!asyncStatement.isDone()) {
							asyncStatement.completeExceptionally(e);
							return Status.CANCEL_STATUS;
						} else {
							String sqlState = e instanceof SQLException ? ((SQLException) e).getSQLState() : null;
//							if (!OracleConstants.EC_QUERY_CANCELED.equals(sqlState)) {
							log.error(name, e);
							return DebugUtils.newErrorStatus(name, e);
//							}
						}
					} finally {
						try {
							if (statement != null) {
								statement.close();
							}
						} catch (Exception e1) {
							log.debug("Error clearing local statment");
							log.error(e1);
						}
						connection.close();

						fireEvent(new DBGEvent(this, DBGEvent.TERMINATE, DBGEvent.CLIENT_REQUEST));
					}
				}
				log.debug("Local statement executed");
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return job;
	}

	private void attachLocal(DBRProgressMonitor monitor, OracleProcedureStandalone function, List<String> parameters)
			throws DBGException {
		try {
			JDBCExecutionContext connection = (JDBCExecutionContext) controllerConnection.getOwnerInstance()
					.openIsolatedContext(monitor, "Debug process session", null);
			log.debug("Attaching locally....");
			try (JDBCSession session = connection.openSession(monitor, DBCExecutionPurpose.UTIL, "Attach to session")) {
				try (Statement stmt = session.createStatement()) {
					ResultSet resultSet = stmt.executeQuery("SELECT SYS.DBMS_DEBUG.Initialize(null,0) FROM dual");
					if (resultSet.next()) {
						sessionId = resultSet.getString(1);
					} else {
						throw new SQLException("Error while get session");
					}
				}
			} catch (SQLException e) {
				log.debug("Error while attaching to session");
				throw new DBGException("Error attaching to session", e);
			}
			log.debug(String.format("Attached local session ID = %s", sessionId));
			getController().fireEvent(new DBGEvent(this, DBGEvent.SUSPEND, DBGEvent.MODEL_SPECIFIC));
		} catch (DBException e) {
			throw new DBGException("Error opening debug session", e);
		}

	}

	/**
	 * This method attach debug session to debug object (procedure) and wait forever
	 * while target or any (depend on targetPID) session will run target procedure
	 */
	public void attach(DBRProgressMonitor monitor, Map<String, Object> configuration) throws DBException {
//		if (!checkDebugPlugin(monitor)) {
//			throw new DBGException("PostgreSQL debug plugin is not installed on the server.\n"
//					+ "Refer to this WIKI article for installation instructions:\n"
//					+ "https://github.com/dbeaver/dbeaver/wiki/PGDebugger#installation");
//		}

		log.debug("Attaching...");

		functionOid = CommonUtils.toInt(configuration.get(OracleDebugConstants.ATTR_FUNCTION_OID));
		log.debug(String.format("Function OID %d", functionOid));
		attachKind = OracleDebugAttachKind.LOCAL;
		OracleProcedureStandalone function = OracleSqlDebugCore.resolveFunction(monitor,
				controllerConnection.getDataSource().getContainer(), configuration);
		List<String> parameterValues = (List<String>) configuration.get(OracleDebugConstants.ATTR_FUNCTION_PARAMETERS);
		attachLocal(monitor, function, parameterValues);
		log.debug("Local attached");
	}

	private boolean isGlobalSession(Map<String, Object> configuration) {
		return OracleDebugConstants.ATTACH_KIND_GLOBAL
				.equals(String.valueOf(configuration.get(OracleDebugConstants.ATTR_ATTACH_KIND)));
	}

	private boolean checkDebugPlugin(DBRProgressMonitor monitor) {
		try (JDBCSession session = getControllerConnection().openSession(monitor, DBCExecutionPurpose.UTIL,
				"Check debug plugin installation")) {
			String version = JDBCUtils.executeQuery(session, SQL_CHECK_PLUGIN);
			log.debug("Debug plugin is installed:\n" + version);
			return true;
		} catch (Exception e) {
			log.debug("Debug plugin not installed: " + e.getMessage());
			return false;
		}
	}

	private void detachLocal(DBRProgressMonitor monitor, JDBCExecutionContext connection) throws DBGException {
		if (localWorkerJob == null || Status.OK_STATUS.equals(localWorkerJob.getResult())) {
			// Execution already terminated
			return;
		}
		try (JDBCSession session = connection.openSession(monitor, DBCExecutionPurpose.UTIL, "Abort local session")) {
			JDBCUtils.executeQuery(session, composeAbortCommand());
			log.debug("Local detached");
		} catch (SQLException e) {
			log.debug("Unable to abort local session");
			log.error("Unable to abort local target", e);
		}
	}

	private void detachGlobal(DBRProgressMonitor monitor) throws DBGException {
		removeBreakpoint(monitor, bpGlobal);

		try (JDBCSession session = getControllerConnection().openSession(monitor, DBCExecutionPurpose.UTIL,
				"Abort global session")) {
			JDBCUtils.executeQuery(session, composeAbortCommand());
			log.debug("Global deattached");
		} catch (SQLException e) {
			log.error("Unable to abort global target", e);
		}
	}

	protected void doDetach(DBRProgressMonitor monitor) throws DBGException {
		switch (attachKind) {
		case GLOBAL:
			detachGlobal(monitor);
			break;
		case LOCAL:
			detachLocal(monitor, getControllerConnection());
			break;
		default:
			break;
		}
	}

	@Override
	public DBGSessionInfo getSessionInfo() {
		return sessionInfo;
	}

	protected String composeAddBreakpointCommand(DBGBreakpointDescriptor descriptor) {
		OracleDebugBreakpointDescriptor bp = (OracleDebugBreakpointDescriptor) descriptor;
		String sqlPattern = attachKind == OracleDebugAttachKind.GLOBAL ? SQL_SET_GLOBAL_BREAKPOINT : SQL_SET_BREAKPOINT;
		long lineNumber = bp.isOnStart() ? -1 : bp.getLineNo();
		log.debug(String.format("Adding breakpoint to line #%d", lineNumber));
		return sqlPattern.replaceAll("\\?sessionid", String.valueOf(getSessionId()))
				.replaceAll("\\?obj", String.valueOf(functionOid)).replaceAll("\\?line", String.valueOf(lineNumber))
				.replaceAll("\\?target", bp.isAll() ? "null" : String.valueOf(bp.getTargetId()));
	}

	protected String composeRemoveBreakpointCommand(DBGBreakpointDescriptor breakpointDescriptor) {
		OracleDebugBreakpointDescriptor bp = (OracleDebugBreakpointDescriptor) breakpointDescriptor;
		return SQL_DROP_BREAKPOINT.replaceAll("\\?sessionid", String.valueOf(getSessionId()))
				.replaceAll("\\?obj", String.valueOf(functionOid))
				.replaceAll("\\?line", bp.isOnStart() ? "-1" : String.valueOf(bp.getLineNo()));
	}

	@Override
	public void execContinue() throws DBGException {
		log.debug("try continue for");
		execStep(SQL_CONTINUE, " continue for ", DBGEvent.RESUME);
		log.debug("continue for realized");
	}

	@Override
	public void execStepInto() throws DBGException {
		log.debug("try step into");
		execStep(SQL_STEP_INTO, " step into for ", DBGEvent.STEP_INTO);
		log.debug("step into realized");
	}

	@Override
	public void execStepOver() throws DBGException {
		log.debug("try step over");
		execStep(SQL_STEP_OVER, " step over for ", DBGEvent.STEP_OVER);
		log.debug("step over realized");
	}

	@Override
	public void execStepReturn() throws DBGException {
		log.debug("Exec return not implemented");
		throw new DBGException("Exec return not implemented");
	}

	@Override
	public void resume() throws DBGException {
		log.debug("try continue execution");
		execContinue();
		log.debug("continue execution realized");
	}

	@Override
	public void suspend() throws DBGException {
		throw new DBGException("Suspend not implemented");
	}

	/**
	 * Execute step SQL command asynchronously, set debug session name to
	 * [sessionID] name [managerPID]
	 *
	 * @param commandPattern - SQL command for execute step
	 * @param nameParameter  - session 'name' part
	 */
	public void execStep(String commandPattern, String nameParameter, int eventDetail) throws DBGException {
		String sql = commandPattern.replaceAll("\\?sessionid", String.valueOf(sessionId));
		String taskName = String.valueOf(sessionId) + nameParameter + sessionInfo.getID();
		DBGEvent begin = new DBGEvent(this, DBGEvent.RESUME, eventDetail);
		DBGEvent end = new DBGEvent(this, DBGEvent.SUSPEND, eventDetail);
		runAsync(sql, taskName, begin, end);
	}

	protected String composeAbortCommand() {
		return SQL_ABORT.replaceAll("\\?sessionid", String.valueOf(sessionId));
	}

	@Override
	public List<DBGVariable<?>> getVariables(DBGStackFrame stack) throws DBGException {
		if (stack != null) {
			selectFrame(stack.getLevel());
		}

		log.debug("Get vars values");
		List<DBGVariable<?>> vars = new ArrayList<>();

		String sql = SQL_GET_VARS.replaceAll("\\?sessionid", String.valueOf(sessionId));
		try (JDBCSession session = getControllerConnection().openSession(new VoidProgressMonitor(),
				DBCExecutionPurpose.UTIL, "Read debug variables")) {
			try (Statement stmt = session.createStatement()) {
				try (ResultSet rs = stmt.executeQuery(sql)) {

					while (rs.next()) {
						String name = rs.getString("name");
						String varclass = rs.getString("varclass");
						int linenumber = rs.getInt("linenumber");
						boolean isunique = rs.getBoolean("isunique");
						boolean isconst = rs.getBoolean("isconst");
						boolean isnotnull = rs.getBoolean("isnotnull");
						int dtype = rs.getInt("dtype");
						String value = rs.getString("value");
						OracleDebugVariable var = new OracleDebugVariable(name, varclass, linenumber, isunique, isconst,
								isnotnull, dtype, value);
						vars.add(var);
					}

				}
			}
		} catch (SQLException e) {
			log.debug("Error getting vars: " + e.getMessage());
			throw new DBGException("SQL error", e);
		}

		log.debug(String.format("Return %d var(s)", vars.size()));
		return vars;

	}

	@Override
	public void setVariableVal(DBGVariable<?> variable, Object value) throws DBGException {
		log.debug("Set var value");
		try (JDBCSession session = getControllerConnection().openSession(new VoidProgressMonitor(),
				DBCExecutionPurpose.UTIL, "Set debug variable")) {
			try (PreparedStatement stmt = session.prepareStatement(SQL_SET_VAR)) {

				if (variable instanceof OracleDebugVariable) {

					if (value instanceof String) {

						OracleDebugVariable var = (OracleDebugVariable) variable;

						stmt.setString(1, sessionId);
						stmt.setString(2, var.getName());
						stmt.setInt(3, var.getLineNumber());
						stmt.setString(4, (String) value);

						stmt.execute();
						log.debug("Var value set");
					} else {
						log.debug("Incorrect variable value class");
						throw new DBGException("Incorrect variable value class");
					}

				} else {
					log.debug("Incorrect variable class");
					throw new DBGException("Incorrect variable class");
				}
			}

		} catch (SQLException e) {
			log.debug("Error setting var: " + e.getMessage());
			throw new DBGException("SQL error", e);
		}
	}

	@Override
	public List<DBGStackFrame> getStack() throws DBGException {
		List<DBGStackFrame> stack = new ArrayList<>(1);
		log.debug("Get stack");

		String sql = SQL_GET_STACK.replaceAll("\\?sessionid", String.valueOf(getSessionId()));
		try (JDBCSession session = getControllerConnection().openSession(new VoidProgressMonitor(),
				DBCExecutionPurpose.UTIL, "Get debug stack frame")) {
			try (Statement stmt = session.createStatement()) {
				try (ResultSet rs = stmt.executeQuery(sql)) {
					while (rs.next()) {
						int level = rs.getInt("level");
						String targetname = rs.getString("targetname");
						int func = rs.getInt("func");
						int linenumber = rs.getInt("linenumber");
						String args = rs.getString("args");
						OracleDebugStackFrame frame = new OracleDebugStackFrame(level, targetname, func, linenumber,
								args);
						stack.add(frame);
					}

				}
			}
		} catch (SQLException e) {
			log.debug("Error loading stack frame: " + e.getMessage());
			throw new DBGException("SQL error", e);
		}
		log.debug(String.format("Return %d stack frame(s)", stack.size()));
		return stack;
	}

	@Override
	public String getSource(DBGStackFrame stack) throws DBGException {
		log.debug("Get source");
		if (stack instanceof OracleDebugStackFrame) {
			OracleDebugStackFrame postgreStack = (OracleDebugStackFrame) stack;
			String src = getSource(postgreStack.getOid());
			log.debug(String.format("Return %d src char(s)", src.length()));
			return src;
		}
		String message = String.format("Unable to get source for stack %s", stack);
		throw new DBGException(message);
	}

	/**
	 * Return source for func OID in debug session
	 *
	 * @return String
	 */

	public String getSource(int OID) throws DBGException {
		log.debug("Get source for func OID in debug session");
		String sql = SQL_GET_SRC.replaceAll("\\?sessionid", String.valueOf(sessionId)).replaceAll("\\?oid",
				String.valueOf(OID));
		try (JDBCSession session = getControllerConnection().openSession(new VoidProgressMonitor(),
				DBCExecutionPurpose.UTIL, "Get session source")) {
			try (Statement stmt = session.createStatement()) {
				try (ResultSet rs = stmt.executeQuery(sql)) {
					if (rs.next()) {
						String src = rs.getString(1);
						log.debug(String.format("Return %d src char(s)", src.length()));
						return src;
					}
					return null;
				}
			}
		} catch (SQLException e) {
			log.debug(String.format("Unable to get source for OID %s", e.getMessage()));
			throw new DBGException("SQL error", e);
		}
	}

	/**
	 * This function changes the debugger focus to the indicated frame (in the call
	 * stack). Whenever the target stops (at a breakpoint or as the result of a
	 * step/into or step/over), the debugger changes focus to most deeply nested
	 * function in the call stack (because that's the function that's executing).
	 * <p>
	 * You can change the debugger focus to other stack frames - once you do that,
	 * you can examine the source code for that frame, the variable values in that
	 * frame, and the breakpoints in that target.
	 * <p>
	 * The debugger focus remains on the selected frame until you change it or the
	 * target stops at another breakpoint.
	 */

	public void selectFrame(int frameNumber) throws DBGException {
		log.debug("Select frame");
		String sql = SQL_SELECT_FRAME.replaceAll("\\?sessionid", String.valueOf(sessionId)).replaceAll("\\?frameno",
				String.valueOf(frameNumber));

		try (JDBCSession session = getControllerConnection().openSession(new VoidProgressMonitor(),
				DBCExecutionPurpose.UTIL, "Select debug frame")) {
			try (Statement stmt = session.createStatement()) {
				try (ResultSet rs = stmt.executeQuery(sql)) {
					if (!rs.next()) {
						log.debug("Unable to select frame");
						throw new DBGException("Unable to select frame");
					}

					log.debug("Frame selected");

				}
			}
		} catch (SQLException e) {
			log.debug(String.format("Unable to select frame %s", e.getMessage()));
			throw new DBGException("SQL error", e);
		}
	}

	@Override
	public String toString() {
		return "PostgreDebugSession " + (isWaiting() ? "WAITING" : "READY") + " [sessionId=" + sessionId
				+ ", breakpoints=" + getBreakpoints() + "targetId=(" + sessionInfo.getID() + ") Session=("
				+ sessionInfo.toString() + ") " + "]";
	}

	@Override
	public String getSessionId() {
		return sessionId;
	}

	@Override
	public boolean canStepInto() {
		return true;
	}

	@Override
	public boolean canStepOver() {
		return true;
	}

	@Override
	public boolean canStepReturn() {
		return false;
	}

	/**
	 * Return true if debug session up and running on server
	 *
	 * @return boolean
	 */
	public boolean isAttached() {
		return sessionId != null;
	}

	/**
	 * Return true if session waiting target connection (on breakpoint, after step
	 * or continue) in debug thread
	 *
	 * @return boolean
	 */
	public boolean isDone() {
		switch (attachKind) {
		case GLOBAL:
			return workerJob == null || workerJob.isFinished();
		case LOCAL:
			return sessionId != null;
		default:
			return false;
		}
	}

	@Override
	public void closeSession(DBRProgressMonitor monitor) throws DBGException {
		if (!isAttached()) {
			return;
		}
		log.debug("Closing session.");
		try {
			super.closeSession(monitor);
			log.debug("Session closed.");
		} finally {
			if (controllerConnection != null) {
				IOUtils.close(controllerConnection);
			}
		}
	}
}
