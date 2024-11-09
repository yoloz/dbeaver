package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBIcon;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPImage;
import org.jkiss.dbeaver.model.DBPImageProvider;
import org.jkiss.dbeaver.model.DBPRefreshableObject;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSObject;

import java.sql.SQLException;

public class GBase8aConsumerGroup implements DBSObject, DBPRefreshableObject, DBPImageProvider {
    private static final Log log = Log.getLog(GBase8aConsumerGroup.class);

    private final GBase8aDataSource dataSource;

    private String name;

    private String groupId;

    private GBase8aVC gbase8aVC;

    private String comment;

    private String userNames = "";

    private boolean persisted;

    private boolean isInitialized = false;

    protected GBase8aConsumerGroup(GBase8aDataSource dataSource, String name) {
        this.dataSource = dataSource;
        this.name = name;
    }


    public GBase8aConsumerGroup(GBase8aDataSource dataSource, JDBCResultSet resultSet, GBase8aVC gbase8aVC) {
        this.dataSource = dataSource;
        this.gbase8aVC = gbase8aVC;

        JDBCResultSet dbResult = null;
        if (resultSet != null) {
            this.persisted = true;
            try {
                this.name = resultSet.getString("consumer_group_name");
                if (resultSet.getString("comment") != null) {
                    this.comment = resultSet.getString("comment");
                }

                if (resultSet.getString("consumer_group_id") != null) {
                    this.groupId = resultSet.getString("consumer_group_id");
                    try (JDBCSession dbsession = DBUtils.openMetaSession(this.dataSource.getMonitor(), dataSource, "query consumer group users");
                         JDBCStatement dbStat = dbsession.createStatement()
                    ) {
                        dbResult = dbStat.executeQuery("select user_name from gbase.consumer_group_user where consumer_group_id=" + this.groupId);
                        while (dbResult.next()) {
                            String userName = dbResult.getString("user_name");
                            this.userNames = this.userNames + userName + ",";
                        }
                    } catch (SQLException | DBCException e) {
                        log.error(e);
                    }
                    if (!this.userNames.isEmpty()) {
                        this.userNames = this.userNames.substring(0, this.userNames.length() - 1);
                    }
                }
            } catch (SQLException e) {
                log.error(e);
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

    @Property(viewable = true, order = 3)
    public String getUserNames() {
        return this.userNames;
    }

    public void setUserNames(String userNames) {
        this.userNames = userNames;
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
        return this.gbase8aVC.getConsumerGroup(monitor, this.name);
    }

    public DBPImage getObjectImage() {
//        return (DBPImage) DBIcon.TREE_TABLE_COPY;
        return DBIcon.TREE_USER_GROUP;
    }
}
