package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
//import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBPNamedObject2;
import org.jkiss.dbeaver.model.DBPRefreshableObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.meta.Association;
import org.jkiss.dbeaver.model.struct.DBStructUtils;
import org.jkiss.dbeaver.model.struct.cache.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCStructCache;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTable;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableColumn;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSEntity;
import org.jkiss.dbeaver.model.struct.DBSEntityAttribute;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.utils.CommonUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class GBase8aTableBase extends JDBCTable<GBase8aDataSource, GBase8aCatalog> implements DBSObject,DBPNamedObject2, DBPRefreshableObject, GBase8aSourceObject {

//    private static final Log log = Log.getLog(GBase8aTableBase.class);

    protected GBase8aTableBase(GBase8aCatalog catalog) {
        super(catalog, false);
    }

    protected GBase8aTableBase(DBRProgressMonitor monitor, GBase8aCatalog catalog, DBSEntity source) throws DBException {
        super(catalog, source, false);
        DBSObjectCache<GBase8aTableBase, GBase8aTableColumn> colCache = getContainer().getTableCache().getChildrenCache(this);
        for (DBSEntityAttribute srcColumn : CommonUtils.safeCollection(source.getAttributes(monitor))) {
            if (DBUtils.isHiddenObject(srcColumn)) {
                continue;
            }
            GBase8aTableColumn column = new GBase8aTableColumn(this, srcColumn);
            colCache.cacheObject(column);
        }
    }

    protected GBase8aTableBase(GBase8aCatalog catalog, ResultSet dbResult) {
        super(catalog, JDBCUtils.safeGetString(dbResult, 1), true);
    }

    public JDBCStructCache<GBase8aCatalog, ? extends JDBCTable, ? extends JDBCTableColumn> getCache() {
        return getContainer().getTableCache();
    }

    @NotNull
    public String getFullyQualifiedName(DBPEvaluationContext context) {
        if (getDataSource().isVCCluster() && !(getContainer() instanceof GBase8aSysCatalog)) {
            return DBUtils.getFullQualifiedName(getDataSource(), getDataSource().getVc(getContainer().getVcName()),
                    getContainer(),
                    this);
        }
        return DBUtils.getFullQualifiedName(getDataSource(), getContainer(),
                this);
    }

    @Association
    public List<GBase8aTableColumn> getAttributes(@NotNull DBRProgressMonitor monitor) throws DBException {
        List<GBase8aTableColumn> childColumns = getContainer().tableCache.getChildren(monitor, getContainer(), this);
        if (childColumns == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(childColumns);
//        Collections.sort(columns, DBUtils.orderComparator());
    }


    public GBase8aTableColumn getAttribute(@NotNull DBRProgressMonitor monitor, @NotNull String attributeName) throws DBException {
        return getContainer().tableCache.getChild(monitor, getContainer(), this, attributeName);
    }


    public List<GBase8aTableColumn> getCachedAttributes() {
        DBSObjectCache<GBase8aTableBase, GBase8aTableColumn> childrenCache = getContainer().tableCache.getChildrenCache(this);
        if (childrenCache != null) {
            return childrenCache.getCachedObjects();
        }
        return Collections.emptyList();
    }


    public DBSObject refreshObject(@NotNull DBRProgressMonitor monitor) throws DBException {
        return getContainer().tableCache.refreshObject(monitor, getContainer(), this);
    }


    public String getDDL(DBRProgressMonitor monitor, Map<String, Object> options) throws DBException {
        if (!isPersisted()) {
            return DBStructUtils.generateTableDDL(monitor, this, options, false);
        }
        try (JDBCSession session = DBUtils.openMetaSession(monitor, getDataSource(), "Retrieve table DDL")) {
            JDBCPreparedStatement dbStat = session.prepareStatement(
                    "SHOW CREATE " + (isView() ? "VIEW" : "TABLE") + " " + getFullyQualifiedName(DBPEvaluationContext.DDL));
            try (ResultSet dbResult = dbStat.executeQuery()) {
                if (dbResult.next()) {
                    if (isView()) {
                        return dbResult.getString("Create View");
                    } else {
                        return dbResult.getString("Create Table");
                    }
                } else {
                    return "DDL is not available";
                }
            }
        } catch (SQLException ex) {
            throw new DBException(ex, getDataSource());
        }
    }
}
