package org.jkiss.dbeaver.ext.gbase8a.ui.controls;

import org.jkiss.dbeaver.ui.controls.PairListControl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;

public class PrivilegesPairList extends PairListControl<String> {
    public PrivilegesPairList(Composite parent) {
        super(parent, 0, "Available", "Granted");
    }

    public void setModel(Map<String, Boolean> privs) {
        List<String> availPrivs = new ArrayList<>();
        List<String> grantedPrivs = new ArrayList<>();
        for (Map.Entry<String, Boolean> priv : privs.entrySet()) {
            if (priv.getValue().booleanValue()) {
                grantedPrivs.add(priv.getKey());
                continue;
            }
            availPrivs.add(priv.getKey());
        }

        setModel(availPrivs, grantedPrivs);
    }
}
