/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2017 Andrew Khitrin (ahitrin@gmail.com)
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

package org.jkiss.dbeaver.postgresql.pldbg.control;

import java.util.List;

import org.jkiss.dbeaver.postgresql.pldbg.DebugException;
import org.jkiss.dbeaver.postgresql.pldbg.DebugObject;
import org.jkiss.dbeaver.postgresql.pldbg.DebugSession;
import org.jkiss.dbeaver.postgresql.pldbg.SessionInfo;
import org.jkiss.dbeaver.postgresql.pldbg.impl.DebugSessionPostgres;

public interface DebugManager<SESSIONID,OBJECTID> {
	SessionInfo<SESSIONID> getCurrent() throws DebugException;
    List<? extends SessionInfo<SESSIONID>> getSessions() throws DebugException;
    DebugSession<? extends SessionInfo<SESSIONID>,? extends DebugObject<OBJECTID>> getDebugSession(SESSIONID id) throws DebugException;
    List<DebugSession<?,?>> getDebugSessions() throws DebugException;
    void terminateSession(SESSIONID id); 
    DebugSession<? extends SessionInfo<SESSIONID>,? extends DebugObject<OBJECTID>> createDebugSession() throws DebugException;
    boolean isSessionExists(SESSIONID id);
    List<? extends DebugObject<OBJECTID>> getObjects(String ownerCtx, String nameCtx) throws DebugException;
}
