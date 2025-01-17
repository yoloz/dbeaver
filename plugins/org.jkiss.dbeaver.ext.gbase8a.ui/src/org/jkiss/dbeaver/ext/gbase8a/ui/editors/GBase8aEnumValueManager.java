/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2024 DBeaver Corp and others
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
package org.jkiss.dbeaver.ext.gbase8a.ui.editors;

import org.jkiss.dbeaver.ext.gbase8a.GBase8aConstants;
import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableColumn;
import org.jkiss.dbeaver.model.DBValueFormatting;
import org.jkiss.dbeaver.model.data.DBDDisplayFormat;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;
import org.jkiss.dbeaver.ui.data.IValueController;
import org.jkiss.dbeaver.ui.data.managers.EnumValueManager;
import org.jkiss.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * MySQL ENUM value manager
 */
public class GBase8aEnumValueManager extends EnumValueManager {
    public GBase8aEnumValueManager() {
    }

    @Override
    protected boolean isMultiValue(IValueController valueController) {
        return valueController.getValueType().getTypeName().equalsIgnoreCase(GBase8aConstants.TYPE_NAME_SET);
    }

    @Override
    protected List<String> getEnumValues(IValueController valueController) {
        DBSTypedObject valueType = valueController.getValueType();
        if (valueType instanceof GBase8aTableColumn) {
            return ((GBase8aTableColumn) valueType).getEnumValues();
        } else {
            return null;
        }
    }

    @Override
    protected List<String> getSetValues(IValueController valueController, Object value) {
        String setString = DBValueFormatting.getDefaultValueDisplayString(value, DBDDisplayFormat.UI);
        List<String> setValues = new ArrayList<>();
        if (!CommonUtils.isEmpty(setString)) {
            StringTokenizer st = new StringTokenizer(setString, ",");
            while (st.hasMoreTokens()) {
                setValues.add(st.nextToken());
            }
        }
        return setValues;
    }

}