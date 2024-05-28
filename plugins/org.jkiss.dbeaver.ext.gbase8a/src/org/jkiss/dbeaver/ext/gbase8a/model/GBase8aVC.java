package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.ext.gbase8a.GBase8aUtils;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPRefreshableObject;
import org.jkiss.dbeaver.model.DBPSystemObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCObjectCache;
import org.jkiss.dbeaver.model.meta.Association;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSObjectContainer;
import org.jkiss.dbeaver.model.struct.DBSObjectFilter;
//import cn.gbase.studio.model.struct.DBSObjectSelector;
import org.jkiss.utils.CommonUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GBase8aVC implements DBPRefreshableObject, DBPSystemObject, DBSObjectContainer { //, DBSObjectSelector {

    private static final Log log = Log.getLog(GBase8aVC.class);

    private final GBase8aDataSource dataSource;

    private final CatalogCache catalogCache = new CatalogCache();

    private final ConsumerGroupCache consumerGroupCache = new ConsumerGroupCache();

    private final ResourcePoolCache resourcePoolCache = new ResourcePoolCache();

    private final ResourcePlanCache resourcePlanCache = new ResourcePlanCache();

    private final ResourceDirectiveCache resourceDirectiveCache = new ResourceDirectiveCache();

    private String name;

    private String activeCatalogName;

    private String id;

    private String activeResourcePlanId;

    public GBase8aVC(GBase8aDataSource dataSource, String name, String id) {
        this.dataSource = dataSource;
        this.name = name;
        this.id = id;

        try {
            this.catalogCache.getAllObjects(this.dataSource.getMonitor(), this);
            this.consumerGroupCache.getAllObjects(this.dataSource.getMonitor(), this);
            this.resourcePoolCache.getAllObjects(this.dataSource.getMonitor(), this);
            this.resourcePlanCache.getAllObjects(this.dataSource.getMonitor(), this);
            this.resourceDirectiveCache.getAllObjects(this.dataSource.getMonitor(), this);

            JDBCSession session = DBUtils.openMetaSession(this.dataSource.getMonitor(), dataSource, "Load basic datasource metadata");
            this.activeCatalogName = GBase8aUtils.determineCurrentDatabase(session);
            this.activeResourcePlanId = GBase8aUtils.getActiveResourcePlanId(session, name, dataSource);
        } catch (DBException e) {
            log.debug(e.getMessage());
        }
    }

    @Property(viewable = true, order = 1)
    @NotNull
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActiveResourcePlanId() {
        return this.activeResourcePlanId;
    }

    public CatalogCache getCatalogCache() {
        return this.catalogCache;
    }

    public GBase8aCatalog getCatalog(String name) {
        return this.catalogCache.getCachedObject(name);
    }

    @Association
    public Collection<GBase8aCatalog> getCatalogs(DBRProgressMonitor monitor) {
        Collection<GBase8aCatalog> catalogs = this.catalogCache.getCachedObjects();
        for (GBase8aCatalog catalog : catalogs) {
            log.debug("************" + catalog.getVcName() + "." + catalog.getName());
        }
        log.debug("getCatalogs==========");
        List<GBase8aCatalog> userCatalogs = new ArrayList<>();
        for (GBase8aCatalog catalog : catalogs) {
            if (!"information_schema".equalsIgnoreCase(catalog.getName()) &&
                    !"gbase".equalsIgnoreCase(catalog.getName()) &&
                    !"performance_schema".equalsIgnoreCase(catalog.getName()) &&
                    !"gclusterdb".equalsIgnoreCase(catalog.getName()) &&
                    !"gctmpdb".equalsIgnoreCase(catalog.getName())) {
                userCatalogs.add(catalog);
            }
        }
        return userCatalogs;
    }

    @Association
    public Collection<GBase8aConsumerGroup> getConsumerGroups(DBRProgressMonitor monitor) {
        return this.consumerGroupCache.getCachedObjects();
    }

    public GBase8aConsumerGroup getConsumerGroup(String name) {
        return this.consumerGroupCache.getCachedObject(name);
    }

    @Association
    public Collection<GBase8aResourcePool> getResourcePools(DBRProgressMonitor monitor) {
        return this.resourcePoolCache.getCachedObjects();
    }

    @Association
    public Collection<GBase8aResourceDirective> getResourceDirectives(DBRProgressMonitor monitor) {
        return this.resourceDirectiveCache.getCachedObjects();
    }

    public GBase8aResourceDirective getResourceDirective(String name) {
        return this.resourceDirectiveCache.getCachedObject(name);
    }

    @Association
    public Collection<GBase8aResourcePlan> getResourcePlans(DBRProgressMonitor monitor) {
        return this.resourcePlanCache.getCachedObjects();
    }

    public GBase8aResourcePlan getResourcePlan(String name) {
        return this.resourcePlanCache.getCachedObject(name);
    }

    static class CatalogCache extends JDBCObjectCache<GBase8aVC, GBase8aCatalog> {

        protected JDBCStatement prepareObjectsStatement(@NotNull JDBCSession session, @NotNull GBase8aVC owner) throws SQLException {
            GBase8aDataSource dataSource = (GBase8aDataSource) owner.getDataSource();

            StringBuilder catalogQuery = new StringBuilder();
            if (dataSource.isVCCluster()) {
                catalogQuery = catalogQuery.append("SELECT * FROM information_schema.SCHEMATA where vc_name='").append(owner.getName()).append("'");
            } else {
                catalogQuery = catalogQuery.append("SELECT * FROM information_schema.SCHEMATA");
            }

            DBSObjectFilter catalogFilters = owner.getDataSource().getContainer().getObjectFilter(GBase8aCatalog.class, null, false);
            if (catalogFilters != null) {
                JDBCUtils.appendFilterClause(catalogQuery, catalogFilters, "SCHEMA_NAME", true);
            }
            JDBCPreparedStatement dbStat = session.prepareStatement(catalogQuery.toString());
            if (catalogFilters != null) {
                JDBCUtils.setFilterParameters(dbStat, 1, catalogFilters);
            }
            return dbStat;
        }

        protected GBase8aCatalog fetchObject(@NotNull JDBCSession session, @NotNull GBase8aVC owner, @NotNull JDBCResultSet resultSet) throws SQLException, DBException {
            return new GBase8aCatalog((GBase8aDataSource) owner.getDataSource(), resultSet, owner);
        }
    }

    static class ConsumerGroupCache extends JDBCObjectCache<GBase8aVC, GBase8aConsumerGroup> {
        protected JDBCStatement prepareObjectsStatement(@NotNull JDBCSession session, @NotNull GBase8aVC owner) throws SQLException {
            GBase8aDataSource dataSource = (GBase8aDataSource) owner.getDataSource();
            StringBuilder consumerGroupQuery = new StringBuilder();
            if (dataSource.isVCCluster()) {
                consumerGroupQuery = consumerGroupQuery.append("select * from gbase.consumer_group where vc_id='" + owner.getId() + "'");
            } else {
                consumerGroupQuery = consumerGroupQuery.append("select * from gbase.consumer_group ");
            }
            JDBCPreparedStatement dbStat = session.prepareStatement(consumerGroupQuery.toString());
            return dbStat;
        }

        protected GBase8aConsumerGroup fetchObject(@NotNull JDBCSession session, @NotNull GBase8aVC owner, @NotNull JDBCResultSet resultSet) throws SQLException, DBException {
            return new GBase8aConsumerGroup((GBase8aDataSource) owner.getDataSource(), resultSet, owner);
        }
    }


    static class ResourcePlanCache
            extends JDBCObjectCache<GBase8aVC, GBase8aResourcePlan> {
        protected JDBCStatement prepareObjectsStatement(@NotNull JDBCSession session, @NotNull GBase8aVC owner) throws SQLException {
            GBase8aDataSource dataSource = (GBase8aDataSource) owner.getDataSource();

            StringBuilder consumerGroupQuery = new StringBuilder();
            if (dataSource.isVCCluster()) {
                consumerGroupQuery = consumerGroupQuery.append("select * from gbase.resource_plan where vc_id='" + owner.getId() + "'");
            } else {
                consumerGroupQuery = consumerGroupQuery.append("select * from gbase.resource_plan ");
            }

            JDBCPreparedStatement dbStat = session.prepareStatement(consumerGroupQuery.toString());

            return dbStat;
        }


        protected GBase8aResourcePlan fetchObject(@NotNull JDBCSession session, @NotNull GBase8aVC owner, @NotNull JDBCResultSet resultSet) throws SQLException, DBException {
            return new GBase8aResourcePlan((GBase8aDataSource) owner.getDataSource(), resultSet, owner);
        }
    }


    static class ResourceDirectiveCache
            extends JDBCObjectCache<GBase8aVC, GBase8aResourceDirective> {
        protected JDBCStatement prepareObjectsStatement(@NotNull JDBCSession session, @NotNull GBase8aVC owner) throws SQLException {
            GBase8aDataSource dataSource = (GBase8aDataSource) owner.getDataSource();

            StringBuilder consumerGroupQuery = new StringBuilder();
            if (dataSource.isVCCluster()) {
                consumerGroupQuery.append("select d.resource_plan_directive_name,d.comments,p.resource_plan_name,c.consumer_group_name,o.resource_pool_name ");
                consumerGroupQuery.append("from gbase.resource_plan_directive d, gbase.resource_plan p, gbase.consumer_group c, gbase.resource_pool o ");
                consumerGroupQuery.append("where d.resource_plan_id = p.resource_plan_id and d.consumer_group_id = c.consumer_group_id and d.resource_pool_id = o.resource_pool_id ");
                consumerGroupQuery.append("and d.vc_id='" + owner.getId() + "'");
            } else {
                consumerGroupQuery.append("select d.resource_plan_directive_name,d.comments,p.resource_plan_name,c.consumer_group_name,o.resource_pool_name ");
                consumerGroupQuery.append("from gbase.resource_plan_directive d, gbase.resource_plan p, gbase.consumer_group c, gbase.resource_pool o ");
                consumerGroupQuery.append("where d.resource_plan_id = p.resource_plan_id and d.consumer_group_id = c.consumer_group_id and d.resource_pool_id = o.resource_pool_id ");
            }

            JDBCPreparedStatement dbStat = session.prepareStatement(consumerGroupQuery.toString());

            return dbStat;
        }

        protected GBase8aResourceDirective fetchObject(@NotNull JDBCSession session, @NotNull GBase8aVC owner, @NotNull JDBCResultSet resultSet) throws SQLException, DBException {
            return new GBase8aResourceDirective((GBase8aDataSource) owner.getDataSource(), resultSet, owner);
        }
    }

    static class ResourcePoolCache
            extends JDBCObjectCache<GBase8aVC, GBase8aResourcePool> {
        protected JDBCStatement prepareObjectsStatement(@NotNull JDBCSession session, @NotNull GBase8aVC owner) throws SQLException {
            GBase8aDataSource dataSource = (GBase8aDataSource) owner.getDataSource();

            StringBuilder resourcePoolQuery = new StringBuilder();
            if (dataSource.isVCCluster()) {
                resourcePoolQuery = resourcePoolQuery.append("select *,'' as parent_resource_pool_name from gbase.resource_pool where resource_pool_type=1 and vc_id='").append(owner.getId()).append("'")
                        .append(" union ")
                        .append(" select t1.*,t2.resource_pool_name as parent_resource_pool_name from gbase.resource_pool t1,gbase.resource_pool t2 ")
                        .append(" where t1.parent_resource_pool_id=t2.resource_pool_id and t1.resource_pool_type=0  and t1.vc_id='").append(owner.getId()).append("'");
            } else {
                resourcePoolQuery = resourcePoolQuery.append("select *,'' as parent_resource_pool_name from gbase.resource_pool where resource_pool_type=1")
                        .append(" union ")
                        .append(" select t1.*,t2.resource_pool_name as parent_resource_pool_name from gbase.resource_pool t1,gbase.resource_pool t2")
                        .append(" where t1.parent_resource_pool_id=t2.resource_pool_id and t1.resource_pool_type=0");
            }

            JDBCPreparedStatement dbStat = session.prepareStatement(resourcePoolQuery.toString());
            return dbStat;
        }

        protected GBase8aResourcePool fetchObject(@NotNull JDBCSession session, @NotNull GBase8aVC owner, @NotNull JDBCResultSet resultSet) throws SQLException, DBException {
            return new GBase8aResourcePool((GBase8aDataSource) owner.getDataSource(), resultSet, owner.getName());
        }
    }

    public Collection<? extends GBase8aCatalog> getChildren(@NotNull DBRProgressMonitor monitor) throws DBException {
        return getCatalogs(monitor);
    }

    public GBase8aCatalog getChild(@NotNull DBRProgressMonitor monitor, @NotNull String childName) throws DBException {
        return getCatalog(childName);
    }

    @Override
    public Class<? extends DBSObject> getPrimaryChildType(DBRProgressMonitor monitor) throws DBException {
        return GBase8aCatalog.class;
    }

    GBase8aTable findTable(DBRProgressMonitor monitor, String catalogName, String tableName) throws DBException {
        if (CommonUtils.isEmpty(catalogName)) {
            return null;
        }
        GBase8aCatalog catalog = getCatalog(catalogName);
        if (catalog == null) {
            log.error("Catalog " + catalogName + " not found");
            return null;
        }
        return catalog.getTable(monitor, tableName);
    }

    public String getDescription() {
        return null;
    }

    public DBSObject getParentObject() {
        return this.dataSource.getContainer();
    }

    public DBPDataSource getDataSource() {
        return this.dataSource;
    }

    public boolean isPersisted() {
        return false;
    }

    public boolean isSystem() {
        return false;
    }

    public DBSObject refreshObject(DBRProgressMonitor monitor) throws DBException {
        this.activeCatalogName = null;
        this.catalogCache.clearCache();
        this.consumerGroupCache.clearCache();
        this.resourcePoolCache.clearCache();
        this.resourcePlanCache.clearCache();
        this.resourceDirectiveCache.clearCache();
        return this;
    }


    public void cacheStructure(DBRProgressMonitor monitor, int scope) throws DBException {
        monitor.subTask("Cache catalogs");
        this.catalogCache.getAllObjects(this.dataSource.getMonitor(), this);
    }

    public boolean supporterd() {
        return true;
    }

    public boolean supportsDefaultChange() {
        return true;
    }


    public GBase8aCatalog getDefaultObject() {
        return getCatalog(this.activeCatalogName);
    }

    public void setDefaultObject(DBRProgressMonitor monitor, GBase8aExecutionContext executionContext, DBSObject object) throws DBException {
        GBase8aCatalog oldSelectedEntity = getDefaultObject();
        if (!(object instanceof GBase8aCatalog)) {
            throw new DBException("Invalid object type: " + object);
        }
        this.dataSource.useDatabase(monitor, executionContext, (GBase8aCatalog) object);
        this.activeCatalogName = object.getName();

        if (oldSelectedEntity != null) {
            DBUtils.fireObjectSelect(oldSelectedEntity, false, executionContext);
        }
        if (this.activeCatalogName != null) {
            DBUtils.fireObjectSelect(object, true, executionContext);
        }
    }
}