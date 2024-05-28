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

public class GBase8aResourcePlan implements DBSObject, DBPRefreshableObject, DBPImageProvider {
    private static final Log log = Log.getLog(GBase8aResourcePlan.class);

    private final GBase8aDataSource dataSource;

    private String name;

    private GBase8aVC gbase8aVC;

    private String comment;

    private boolean persisted;

    private boolean isInitialized = false;

    private String resourcePlanId;

    protected GBase8aResourcePlan(GBase8aDataSource dataSource, String name) {
        this.dataSource = dataSource;
        this.name = name;
    }


    public GBase8aResourcePlan(GBase8aDataSource dataSource, JDBCResultSet resultSet, GBase8aVC gbase8aVC) {
        this.dataSource = dataSource;
        this.gbase8aVC = gbase8aVC;

        if (resultSet != null) {
            this.persisted = true;
            try {
                this.name = resultSet.getString("resource_plan_name");
                if (resultSet.getString("comment") != null) {
                    this.comment = resultSet.getString("comment");
                }
                this.resourcePlanId = resultSet.getString("resource_plan_id");
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
    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


    public boolean isPersisted() {
        return this.persisted;
    }


    public String getDescription() {
        return this.name;
    }


    public DBSObject getParentObject() {
        return getDataSource().getContainer();
    }


    public DBPDataSource getDataSource() {
        return this.dataSource;
    }

    public boolean isInitialized() {
        return this.isInitialized;
    }

    public void setInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }


    public DBSObject refreshObject(DBRProgressMonitor monitor) throws DBException {
        this.isInitialized = false;

        return this.gbase8aVC.getResourcePlan(this.name);
    }


    public DBPImage getObjectImage() {
        if (this.resourcePlanId.equals(this.gbase8aVC.getActiveResourcePlanId())) {
            return DBIcon.TREE_LOCKED;
        }
        return DBIcon.TREE_EVENT;
    }
}
