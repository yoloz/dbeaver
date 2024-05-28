package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPScriptObject;
import org.jkiss.dbeaver.model.struct.DBSObject;

public interface GBase8aSourceObject extends DBPScriptObject, DBSObject {
    void setObjectDefinitionText(String paramString) throws DBException;
}
