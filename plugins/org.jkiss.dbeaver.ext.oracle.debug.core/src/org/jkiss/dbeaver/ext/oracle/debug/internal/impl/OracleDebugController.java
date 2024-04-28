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

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.debug.DBGBaseController;
import org.jkiss.dbeaver.debug.DBGBreakpointDescriptor;
import org.jkiss.dbeaver.debug.DBGException;
import org.jkiss.dbeaver.ext.oracle.debug.OracleDebugConstants;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Map;

public class OracleDebugController extends DBGBaseController {

    private static final Log log = Log.getLog(OracleDebugController.class);

    public OracleDebugController(DBPDataSourceContainer dataSourceContainer, Map<String, Object> configuration) {
        super(dataSourceContainer, configuration);
    }

    @Override
    public OracleDebugSession createSession(DBRProgressMonitor monitor, Map<String, Object> configuration)
            throws DBGException {
        OracleDebugSession debugSession = null;
        try {
            debugSession = new OracleDebugSession(monitor, this);
            try (Statement statement = debugSession.targetSessionContext.getConnection(monitor).createStatement()) {
                statement.execute(OracleDebugConstants.SQL_SESSION_DEBUG);
            } catch (SQLException e) {
                throw new DBGException("SQL error", e);
            }
            String targetId;
            try (CallableStatement statement = debugSession.targetSessionContext.getConnection(monitor)
                    .prepareCall(OracleDebugConstants.SQL_QUERY_SESSION_ID)) {
                statement.registerOutParameter("retvar", Types.VARCHAR);
                statement.execute();
                targetId = statement.getString("retvar");
            } catch (SQLException e) {
                throw new DBGException("SQL error", e);
            }
            try (CallableStatement statement = debugSession.targetSessionContext.getConnection(monitor)
                    .prepareCall(OracleDebugConstants.SQL_DEBUG_ON)) {
                statement.execute();
            } catch (SQLException e) {
                throw new DBGException("SQL error", e);
            }
            debugSession.attach(monitor, targetId);
            log.debug("Attached target session.");
            debugSession.executeFunction(monitor, configuration);
            debugSession.debugSessionSynchronize(monitor);
            log.debug("Debug session synchronize success.");
            return debugSession;
        } catch (DBException e) {
            if (debugSession != null) {
                try {
                    debugSession.closeSession(monitor);
                } catch (Exception e1) {
                    log.error(e1);
                }
            }
            if (e instanceof DBGException) {
                throw (DBGException) e;
            }
            log.debug(String.format("Error attaching debug session %s", e.getMessage()));
            throw new DBGException("Error attaching debug session", e);
        }
    }

    @Override
    public DBGBreakpointDescriptor describeBreakpoint(Map<String, Object> attributes) {
        return OracleDebugBreakpointDescriptor.fromMap(attributes);
    }

}
