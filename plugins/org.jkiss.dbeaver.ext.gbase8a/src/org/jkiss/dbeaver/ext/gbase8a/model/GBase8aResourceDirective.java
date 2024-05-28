package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBIcon;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPImage;
import org.jkiss.dbeaver.model.DBPImageProvider;
import org.jkiss.dbeaver.model.DBPRefreshableObject;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.sql.SQLException;

public class GBase8aResourceDirective implements DBSObject, DBPRefreshableObject, DBPImageProvider {
    private static final Log log = Log.getLog(GBase8aResourceDirective.class);

    private GBase8aDataSource dataSource;

    private String name;

    private String plan_name;

    private String group_name;

    private String pool_name;

    private GBase8aVC gbase8aVC;

    private String comments;

    private boolean persisted;

    private boolean isInitialized = false;


    protected GBase8aResourceDirective(GBase8aDataSource dataSource, String name) {
        this.dataSource = dataSource;
        this.name = name;
    }


    public GBase8aResourceDirective(GBase8aDataSource dataSource, JDBCResultSet resultSet, GBase8aVC gbase8aVC) {
        this.dataSource = dataSource;
        this.gbase8aVC = gbase8aVC;

        if (resultSet != null) {
            this.persisted = true;
            try {
                this.name = resultSet.getString("resource_plan_directive_name");
                if (resultSet.getString("comments") != null) {
                    this.comments = resultSet.getString("comments");
                }
                if (resultSet.getString("resource_plan_name") != null) {
                    this.plan_name = resultSet.getString("resource_plan_name");
                }
                if (resultSet.getString("consumer_group_name") != null) {
                    this.group_name = resultSet.getString("consumer_group_name");
                }
                if (resultSet.getString("resource_pool_name") != null) {
                    this.pool_name = resultSet.getString("resource_pool_name");
                }
            } catch (SQLException e) {
                log.debug(e.getMessage());
            }
        } else {
            this.persisted = false;
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

    @Property(viewable = true, order = 2)
    public String getPlan_name() {
        return this.plan_name;
    }

    public void setPlan_name(String plan_name) {
        this.plan_name = plan_name;
    }

    @Property(viewable = true, order = 3)
    public String getGroup_name() {
        return this.group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    @Property(viewable = true, order = 4)
    public String getPool_name() {
        return this.pool_name;
    }

    public void setPool_name(String pool_name) {
        this.pool_name = pool_name;
    }

    @Property(viewable = true, order = 5)
    public String getComments() {
        return this.comments;
    }

    public void setComment(String comments) {
        this.comments = comments;
    }


    public boolean isPersisted() {
        return this.persisted;
    }


    public String getDescription() {
        return this.name;
    }


    public DBSObject getParentObject() {
        return (DBSObject) getDataSource().getContainer();
    }


    public DBPDataSource getDataSource() {
        return (DBPDataSource) this.dataSource;
    }

    public boolean isInitialized() {
        return this.isInitialized;
    }

    public void setInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }


    public DBSObject refreshObject(DBRProgressMonitor monitor) throws DBException {
        this.isInitialized = false;

        return this.gbase8aVC.getResourceDirective(this.name);
    }

    public DBPImage getObjectImage() {
        return (DBPImage) DBIcon.TREE_SERVER;
    }
}
