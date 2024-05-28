package org.jkiss.dbeaver.ext.gbase8a.edit;

import org.jkiss.dbeaver.ext.gbase8a.model.GBase8aUser;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.edit.prop.DBEPropertyHandler;
import org.jkiss.dbeaver.model.edit.prop.DBEPropertyReflector;
import org.jkiss.utils.CommonUtils;

public enum UserPropertyHandler implements DBEPropertyHandler<GBase8aUser>, DBEPropertyReflector<GBase8aUser> {
    NAME,
    HOST,
    PASSWORD,
    PASSWORD_CONFIRM,
    MAX_QUERIES,
    MAX_UPDATES,
    MAX_CONNECTIONS,
    MAX_USER_CONNECTIONS;


    public String getId() {
        return name();
    }


    public GBase8aCommandChangeUser createCompositeCommand(GBase8aUser object) {
        return new GBase8aCommandChangeUser(object);
    }


    public void reflectValueChange(GBase8aUser object, Object oldValue, Object newValue) {
        if (this == NAME || this == HOST) {
            if (this == NAME) {
                object.setUserName(CommonUtils.toString(newValue));
            } else {
                object.setHost(CommonUtils.toString(newValue));
            }
            DBUtils.fireObjectUpdate(object);
        }
    }
}
