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

import org.jkiss.dbeaver.debug.DBGStackFrame;

public class OracleDebugStackFrame implements DBGStackFrame {

    // The following fields are used when setting a breakpoint
    private final Integer namespace;
    private final String name;
    private final String owner;
    //    private final String dblink;
    private final Integer line;
    // Read-only fields (set by Probe when doing a stack backtrace)
    private final Integer libunittype;
    //    private final String entrypointname;
    private final int level;

    public OracleDebugStackFrame(int namespace, String name, String owner, int lineNo, int libunittype, int level) {
        super();
        this.namespace = namespace;
        this.name = name;
        this.owner = owner;
        this.line = lineNo;
        this.libunittype = libunittype;
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public Object getSourceIdentifier() {
        return name;
    }

    @Override
    public int getLineNumber() {
        return line;
    }

    @Override
    public String getName() {
        return name;
    }

    public Integer getNamespace() {
        return namespace;
    }

    public String getOwner() {
        return owner;
    }

    public Integer getLibunittype() {
        return libunittype;
    }

    @Override
    public String toString() {
        return "OracleDebugStackFrame{" +
                "namespace=" + namespace +
                ", name='" + name + '\'' +
                ", owner='" + owner + '\'' +
                ", line=" + line +
                ", libunittype=" + libunittype +
                '}';
    }
}
