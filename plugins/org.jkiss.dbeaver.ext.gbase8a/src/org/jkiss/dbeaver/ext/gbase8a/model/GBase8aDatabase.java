package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCDataSource;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCRemoteInstance;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;

/**
 * @author yolo
 */
public class GBase8aDatabase extends JDBCRemoteInstance {

    protected GBase8aDatabase(DBRProgressMonitor monitor,JDBCDataSource dataSource) throws DBException {
        super(monitor,dataSource,true);
    }
}
