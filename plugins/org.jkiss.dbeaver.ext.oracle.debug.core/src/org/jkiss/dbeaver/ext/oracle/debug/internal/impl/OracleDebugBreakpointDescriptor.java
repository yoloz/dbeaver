/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2024 DBeaver Corp and others
 * Copyright (C) 2017-2018 Andrew Khitrin (ahitrin@gmail.com)
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

import org.eclipse.core.resources.IMarker;
import org.jkiss.dbeaver.debug.DBGBreakpointDescriptor;
import org.jkiss.utils.CommonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Oracle breakpoint. It contains Oracle-specific info for IDatabaseBreakpoint
 * <br/>
 * status:
 * 1:breakpoint_status_active;
 * 4:breakpoint_status_disabled;
 * 8:breakpoint_status_remote;
 */
public class OracleDebugBreakpointDescriptor implements DBGBreakpointDescriptor {

    private String unitName;           // Name of the program unit
    private String unitOwner;          // Owner of the program unit
    private String dblink;         // Database link, if remote
    private final Integer lineNum;          // Line number
    private Integer libunittype;   //NULL, unless this is a nested procedure or function
    private Integer pointStatus;
    private int pointNum;

    public OracleDebugBreakpointDescriptor(String unitName, String unitOwner, Integer lineNum) {
        this.unitName = unitName;
        this.unitOwner = unitOwner;
        this.lineNum = lineNum;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public void setUnitOwner(String unitOwner) {
        this.unitOwner = unitOwner;
    }

    public String getUnitName() {
        return unitName;
    }

    public String getUnitOwner() {
        return unitOwner;
    }

    public String getDblink() {
        return dblink;
    }

    public Integer getLineNum() {
        return lineNum;
    }

    public Integer getLibunittype() {
        return libunittype;
    }

    public Integer getPointStatus() {
        return pointStatus;
    }

    public int getPointNum() {
        return pointNum;
    }

    public void setPointNum(int pointNum) {
        this.pointNum = pointNum;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("unitName", getUnitName());
        map.put("unitOwner", getUnitOwner());
        map.put("dblink", getDblink());
        map.put("lineNum", getLineNum());
        map.put("libunittype", getLibunittype());
        map.put("pointStatus", getPointStatus());
        map.put("pointNum", getPointNum());
        return map;
    }

    public static DBGBreakpointDescriptor fromMap(Map<String, Object> attributes) {
        String name = CommonUtils.toString(attributes.get("unitName"));
        String owner = CommonUtils.toString(attributes.get("unitOwner"));
        int line = CommonUtils.toInt(attributes.get(IMarker.LINE_NUMBER));
        return new OracleDebugBreakpointDescriptor(name, owner, line);
    }

    @Override
    public String toString() {
        return "OracleDebugBreakpointDescriptor{" +
                "unitName='" + unitName + '\'' +
                ", unitOwner='" + unitOwner + '\'' +
                ", dblink='" + dblink + '\'' +
                ", lineNum=" + lineNum +
                ", libunittype=" + libunittype +
                ", pointStatus=" + pointStatus +
                ", pointNum=" + pointNum +
                '}';
    }
}
