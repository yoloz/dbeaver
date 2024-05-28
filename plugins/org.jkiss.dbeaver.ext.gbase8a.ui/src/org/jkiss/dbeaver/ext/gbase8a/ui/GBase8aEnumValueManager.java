package org.jkiss.dbeaver.ext.gbase8a.ui;

import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aTableColumn;
import org.jkiss.dbeaver.model.DBValueFormatting;
import org.jkiss.dbeaver.model.data.DBDDisplayFormat;
import org.jkiss.dbeaver.ui.data.IValueController;
import org.jkiss.dbeaver.ui.data.managers.EnumValueManager;
import org.jkiss.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class GBase8aEnumValueManager extends EnumValueManager {
    protected boolean isMultiValue(IValueController valueController) {
        return valueController.getValueType().getTypeName().equalsIgnoreCase("set");
    }

    protected List<String> getEnumValues(IValueController valueController) {
        return ((GBase8aTableColumn) valueController.getValueType()).getEnumValues();
    }

    protected List<String> getSetValues(IValueController valueController, Object value) {
        String setString = DBValueFormatting.getDefaultValueDisplayString(value, DBDDisplayFormat.UI);
        List<String> setValues = new ArrayList<String>();
        if (!CommonUtils.isEmpty(setString)) {
            StringTokenizer st = new StringTokenizer(setString, ",");
            while (st.hasMoreTokens())
                setValues.add(st.nextToken());
        }
        return setValues;
    }
}