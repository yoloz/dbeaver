package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBPNamedObject2;
import org.jkiss.dbeaver.model.DBPRefreshableObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
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

public abstract class GBase8aTableBase extends JDBCTable<GBase8aDataSource, GBase8aCatalog>
        implements DBPNamedObject2, DBPRefreshableObject, GBase8aSourceObject {

//    private static final Log log = Log.getLog(GBase8aTableBase.class);

    private boolean isPartition;

    protected GBase8aTableBase(GBase8aCatalog catalog) {
        super(catalog, false);
    }

    // Copy constructor
    protected GBase8aTableBase(DBRProgressMonitor monitor, GBase8aCatalog catalog, DBSEntity source) throws DBException {
        super(catalog, source, false);
        DBSObjectCache<GBase8aTableBase, GBase8aTableColumn> colCache = getContainer().getTableCache().getChildrenCache(this);
        // Copy columns
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

    public boolean isPartition() {
        return isPartition;
    }

    public void setPartition(boolean partition) {
        isPartition = partition;
    }

    @Override
    public JDBCStructCache<GBase8aCatalog, ? extends JDBCTable, ? extends JDBCTableColumn> getCache() {
        return getContainer().getTableCache();
    }

    @NotNull
    @Override
    public String getFullyQualifiedName(DBPEvaluationContext context) {
        String fullName = DBUtils.getFullQualifiedName(getDataSource(), getContainer(), this);
        if (!getContainer().isSystemCatalog()) {
            fullName = getDataSource().getVcName() + "." + fullName;
        }
        return fullName;
    }

    @Override
    public List<GBase8aTableColumn> getAttributes(@NotNull DBRProgressMonitor monitor) throws DBException {
        List<GBase8aTableColumn> childColumns = getContainer().getTableCache().getChildren(monitor, getContainer(), this);
        if (childColumns == null) {
            return Collections.emptyList();
        }
        List<GBase8aTableColumn> columns = new ArrayList<>(childColumns);
        columns.sort(DBUtils.orderComparator());
        return columns;
    }

    @Override
    public GBase8aTableColumn getAttribute(@NotNull DBRProgressMonitor monitor, @NotNull String attributeName) throws DBException {
        return getContainer().getTableCache().getChild(monitor, getContainer(), this, attributeName);
    }


    public List<GBase8aTableColumn> getCachedAttributes() {
        DBSObjectCache<GBase8aTableBase, GBase8aTableColumn> childrenCache = getContainer().getTableCache().getChildrenCache(this);
        if (childrenCache != null) {
            return childrenCache.getCachedObjects();
        }
        return Collections.emptyList();
    }

    @Override
    public DBSObject refreshObject(@NotNull DBRProgressMonitor monitor) throws DBException {
        return getContainer().getTableCache().refreshObject(monitor, getContainer(), this);
    }


    public String getDDL(DBRProgressMonitor monitor, Map<String, Object> options) throws DBException {
        if (!isPersisted()) {
            return DBStructUtils.generateTableDDL(monitor, this, options, false);
        }
        try (JDBCSession session = DBUtils.openMetaSession(monitor, this, "Retrieve table DDL");
             JDBCPreparedStatement dbStat = session.prepareStatement(
                     "SHOW CREATE " + (isView() ? "VIEW" : "TABLE")
                             + " "
                             + getFullyQualifiedName(DBPEvaluationContext.DDL));
             ResultSet dbResult = dbStat.executeQuery()) {
            if (dbResult.next()) {
                if (isView()) {
                    return dbResult.getString("Create View");
                } else {
                    return dbResult.getString("Create Table");
                }
            } else {
                return "DDL is not available";
            }
        } catch (SQLException ex) {
            throw new DBException("Retrieve table DDL failed", ex);
        }
    }
}
