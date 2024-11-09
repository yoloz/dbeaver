package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aConstants;
import org.jkiss.dbeaver.model.DBPRefreshableObject;
import org.jkiss.dbeaver.model.DBPSystemObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCObjectCache;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSObjectContainer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GBase8aVC implements DBPRefreshableObject, DBPSystemObject, DBSObjectContainer { //, DBSObjectSelector {

    private static final Log log = Log.getLog(GBase8aVC.class);

    private final GBase8aDataSource dataSource;

    private final CatalogCache catalogCache = new CatalogCache() {
        @Override
        protected void detectCaseSensitivity(DBSObject object) {
            setCaseSensitive(!getDataSource().getSQLDialect().useCaseInsensitiveNameLookup());
        }
    };

    private List<GBase8aConsumerGroup> consumerGroups;
    private List<GBase8aResourcePool> resourcePools;
    private List<GBase8aResourcePlan> resourcePlans;
    private List<GBase8aResourceDirective> resourceDirectives;

    private final String name;
    //    private String activeCatalogName;
    private final String id;
    private String activeResourcePlanId;

    public GBase8aVC(GBase8aDataSource dataSource, String name, String id) throws DBException {
        this.dataSource = dataSource;
        this.name = name;
        this.id = id;
        this.catalogCache.getAllObjects(dataSource.getMonitor(), this);
//        JDBCSession session = DBUtils.openMetaSession(dataSource.getMonitor(), dataSource, "Load basic datasource metadata");
//        this.activeCatalogName = GBase8aUtils.determineCurrentDatabase(session);
//        this.activeResourcePlanId = GBase8aUtils.getActiveResourcePlanId(session, name, dataSource);
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getActiveResourcePlanId() {
        return activeResourcePlanId;
    }

    public CatalogCache getCatalogCache() {
        return catalogCache;
    }

    public Collection<GBase8aCatalog> getCatalogs() throws DBException {
        return catalogCache.getCachedObjects();
    }

    public GBase8aCatalog getCatalog(String name) {
        return catalogCache.getCachedObject(name);
    }

    public Collection<GBase8aConsumerGroup> getConsumerGroups(DBRProgressMonitor monitor) throws DBException {
        if (this.consumerGroups == null) {
            this.consumerGroups = loadConsumerGroup(monitor);
        }
        return this.consumerGroups;
    }

    public GBase8aConsumerGroup getConsumerGroup(DBRProgressMonitor monitor, String name) throws DBException {
        return DBUtils.findObject(getConsumerGroups(monitor), name, true);
    }

    private List<GBase8aConsumerGroup> loadConsumerGroup(DBRProgressMonitor monitor) throws DBException {
        GBase8aDataSource dataSource = getDataSource();
        String consumerGroupQuery = "select * from gbase.consumer_group where vc_id='"
                + getId()
                + "'";
        try (JDBCSession session = DBUtils.openMetaSession(monitor, this, "Load consumer group");
             JDBCPreparedStatement dbStat = session.prepareStatement(consumerGroupQuery);
             JDBCResultSet resultSet = dbStat.executeQuery()) {
            List<GBase8aConsumerGroup> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(new GBase8aConsumerGroup(dataSource, resultSet, this));
            }
            return list;
        } catch (SQLException ex) {
            throw new DBException("Load consumer group failed", ex);
        }
    }

    public Collection<GBase8aResourcePool> getResourcePools(DBRProgressMonitor monitor) throws DBException {
        if (this.resourcePools == null) {
            this.resourcePools = loadResourcePool(monitor);
        }
        return this.resourcePools;
    }

    public GBase8aResourcePool getResourcePool(DBRProgressMonitor monitor, String name) throws DBException {
        return DBUtils.findObject(getResourcePools(monitor), name, true);
    }

    private List<GBase8aResourcePool> loadResourcePool(DBRProgressMonitor monitor) throws DBException {
        GBase8aDataSource dataSource = getDataSource();
        String resourcePoolQuery = "select *,'' as parent_resource_pool_name from gbase.resource_pool where resource_pool_type=1 and vc_id='"
                + getId()
                + "' union select t1.*,t2.resource_pool_name as parent_resource_pool_name from gbase.resource_pool t1,gbase.resource_pool t2 "
                + "where t1.parent_resource_pool_id=t2.resource_pool_id and t1.resource_pool_type=0 and t1.vc_id='"
                + getId()
                + "'";
        try (JDBCSession session = DBUtils.openMetaSession(monitor, this, "Load resource pool");
             JDBCPreparedStatement dbStat = session.prepareStatement(resourcePoolQuery);
             JDBCResultSet resultSet = dbStat.executeQuery()) {
            List<GBase8aResourcePool> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(new GBase8aResourcePool(dataSource, resultSet, this));
            }
            return list;
        } catch (SQLException ex) {
            throw new DBException("Load resource pool failed", ex);
        }
    }

    public Collection<GBase8aResourceDirective> getResourceDirectives(DBRProgressMonitor monitor) throws DBException {
        if (this.resourceDirectives == null) {
            this.resourceDirectives = loadResourceDirective(monitor);
        }
        return this.resourceDirectives;
    }

    public GBase8aResourceDirective getResourceDirective(DBRProgressMonitor monitor, String name) throws DBException {
        return DBUtils.findObject(getResourceDirectives(monitor), name, true);
    }

    private List<GBase8aResourceDirective> loadResourceDirective(DBRProgressMonitor monitor) throws DBException {
        GBase8aDataSource dataSource = getDataSource();
        String resourceDirectiveQuery = "select d.resource_plan_directive_name,d.comments,p.resource_plan_name,c.consumer_group_name,o.resource_pool_name "
                + " from gbase.resource_plan_directive d, gbase.resource_plan p, gbase.consumer_group c, gbase.resource_pool o "
                + " where d.resource_plan_id = p.resource_plan_id and d.consumer_group_id = c.consumer_group_id "
                + " and d.resource_pool_id = o.resource_pool_id and d.vc_id='"
                + getId()
                + "'";
        try (JDBCSession session = DBUtils.openMetaSession(monitor, this, "Load resource directive");
             JDBCPreparedStatement dbStat = session.prepareStatement(resourceDirectiveQuery);
             JDBCResultSet resultSet = dbStat.executeQuery()) {
            List<GBase8aResourceDirective> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(new GBase8aResourceDirective(dataSource, resultSet, this));
            }
            return list;
        } catch (SQLException ex) {
            throw new DBException("Load resource directive failed", ex);
        }
    }

    public Collection<GBase8aResourcePlan> getResourcePlans(DBRProgressMonitor monitor) throws DBException {
        if (this.resourcePlans == null) {
            this.resourcePlans = loadResourcePlan(monitor);
        }
        return this.resourcePlans;
    }

    public GBase8aResourcePlan getResourcePlan(DBRProgressMonitor monitor, String name) throws DBException {
        return DBUtils.findObject(getResourcePlans(monitor), name, true);
    }

    private List<GBase8aResourcePlan> loadResourcePlan(DBRProgressMonitor monitor) throws DBException {
        GBase8aDataSource dataSource = getDataSource();
        String resourcePlanQuery = "select * from gbase.resource_plan where vc_id='"
                + getId()
                + "'";
        try (JDBCSession session = DBUtils.openMetaSession(monitor, this, "Load resource plan");
             JDBCPreparedStatement dbStat = session.prepareStatement(resourcePlanQuery);
             JDBCResultSet resultSet = dbStat.executeQuery()) {
            List<GBase8aResourcePlan> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(new GBase8aResourcePlan(dataSource, resultSet, this));
            }
            return list;
        } catch (SQLException ex) {
            throw new DBException("Load resource plan failed", ex);
        }
    }

    static String filterSysSchemaName() {
        return "'"
                + GBase8aConstants.INFO_SCHEMA_NAME
                + "','"
                + GBase8aConstants.GBASE8A_SCHEMA_NAME
                + "','"
                + GBase8aConstants.PERFORMANCE_SCHEMA
                + "','"
                + GBase8aConstants.GCLUSTERDB
                + "','"
                + GBase8aConstants.GCTEMDB
                + "'";
    }

    static class CatalogCache extends JDBCObjectCache<GBase8aVC, GBase8aCatalog> {

        protected JDBCStatement prepareObjectsStatement(@NotNull JDBCSession session, @NotNull GBase8aVC owner) throws SQLException {
            String catalogQuery = "SELECT * FROM "
                    + GBase8aConstants.META_TABLE_SCHEMATA
                    + " WHERE VC_NAME='"
                    + owner.getName()
                    + "' AND "
                    + GBase8aConstants.COL_SCHEMA_NAME
                    + " NOT IN ("
                    + filterSysSchemaName()
                    + ")";
//            DBSObjectFilter catalogFilters = owner.getDataSource().getContainer().getObjectFilter(GBase8aCatalog.class, null, false);
//            if (catalogFilters != null) {
//                JDBCUtils.appendFilterClause(catalogQuery, catalogFilters, "SCHEMA_NAME", true);
//            }
//            JDBCPreparedStatement dbStat = session.prepareStatement(catalogQuery.toString());
//            if (catalogFilters != null) {
//                JDBCUtils.setFilterParameters(dbStat, 1, catalogFilters);
//            }
            return session.prepareStatement(catalogQuery);
        }

        protected GBase8aCatalog fetchObject(@NotNull JDBCSession session, @NotNull GBase8aVC owner, @NotNull JDBCResultSet resultSet) {
            return new GBase8aCatalog(owner.getDataSource(), resultSet, null);
        }
    }

    public Collection<? extends GBase8aCatalog> getChildren(@NotNull DBRProgressMonitor monitor) throws DBException {
        return getCatalogs();
    }

    public GBase8aCatalog getChild(@NotNull DBRProgressMonitor monitor, @NotNull String childName) throws DBException {
        return getCatalog(childName);
    }

    @Override
    public Class<? extends DBSObject> getPrimaryChildType(DBRProgressMonitor monitor) throws DBException {
        return GBase8aCatalog.class;
    }

//    GBase8aTable findTable(DBRProgressMonitor monitor, String catalogName, String tableName) throws DBException {
//        return getDataSource().findTable(monitor, catalogName, tableName);
//    }

    @Override
    public String getDescription() {
        return null;
    }

    public DBSObject getParentObject() {
        return this.dataSource.getContainer();
    }

    public GBase8aDataSource getDataSource() {
        return this.dataSource;
    }

    public boolean isPersisted() {
        return false;
    }

    public boolean isSystem() {
        return false;
    }

    public DBSObject refreshObject(DBRProgressMonitor monitor) throws DBException {
//        this.activeCatalogName = null;
        this.catalogCache.clearCache();
        this.consumerGroups = null;
        this.resourcePlans = null;
        this.resourcePools = null;
        this.resourceDirectives = null;
        return this;
    }


    public void cacheStructure(DBRProgressMonitor monitor, int scope) throws DBException {
        monitor.subTask("Cache catalogs");
        this.catalogCache.getAllObjects(this.dataSource.getMonitor(), this);
        if ((scope & STRUCT_ATTRIBUTES) != 0) {
            monitor.subTask("Cache table columns");
//            getCatalogCache().loadChildren(monitor, this, null);
        }
    }

//    public GBase8aCatalog getDefaultObject() {
//        return getCatalog(this.activeCatalogName);
//    }

//    public void setDefaultObject(DBRProgressMonitor monitor, GBase8aExecutionContext executionContext, DBSObject object) throws DBException {
//        GBase8aCatalog oldSelectedEntity = getDefaultObject();
//        if (!(object instanceof GBase8aCatalog)) {
//            throw new DBException("Invalid object type: " + object);
//        }
//        this.dataSource.useDatabase(monitor, executionContext, (GBase8aCatalog) object);
//        this.activeCatalogName = object.getName();
//
//        if (oldSelectedEntity != null) {
//            DBUtils.fireObjectSelect(oldSelectedEntity, false, executionContext);
//        }
//        if (this.activeCatalogName != null) {
//            DBUtils.fireObjectSelect(object, true, executionContext);
//        }
//    }
}