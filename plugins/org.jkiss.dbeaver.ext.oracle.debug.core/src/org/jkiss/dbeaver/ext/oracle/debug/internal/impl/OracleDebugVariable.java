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

import org.jkiss.dbeaver.debug.DBGVariable;
import org.jkiss.dbeaver.debug.DBGVariableType;

public class OracleDebugVariable implements DBGVariable<String> {

    private String name;
    private String val;
    private String dataType;
    private int lineNumber;
    private String kind;  //IN,OUT,IN_OUT,RETURN


    @Override
    public String getVal() {
        return val;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public DBGVariableType getType() {
        String type = dataType.toLowerCase();
        return switch (type) {
            case "number", "integer", "binary_integer" -> DBGVariableType.NUMBER;
            case "date", "timestamp" -> DBGVariableType.DATE;
            case "raw", "blob" -> DBGVariableType.BLOB;
            default -> DBGVariableType.TEXT;
        };
    }

    public OracleDebugVariable() {
        super();
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public void setValue(String text) {
        this.val = text;
    }

    public String getDataType() {
        return dataType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    @Override
    public String toString() {
        return "OracleDebugVariable{" +
                "name='" + name + '\'' +
                ", val='" + val + '\'' +
                ", dataType='" + dataType + '\'' +
                ", lineNumber=" + lineNumber +
                '}';
    }
}
