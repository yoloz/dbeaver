package org.jkiss.dbeaver.ext.gbase8a.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBIcon;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPImage;
import org.jkiss.dbeaver.model.DBPImageProvider;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.utils.CommonUtils;

import java.sql.SQLException;


public class GBase8aResourcePool implements DBSObject, DBPImageProvider {

    private static final Log log = Log.getLog(GBase8aResourcePool.class);

    private final GBase8aDataSource dataSource;
    private GBase8aVC gBase8aVC;

    private String name;
    private String baseOn;
    private String type;
    private long priority = 0L;
    private long cpu_percent = 0L;
    private long max_memory = 0L;
    private long max_temp_diskspace = 0L;
    private long max_disk_space = 0L;
    private long max_disk_writeio = 0L;
    private long max_disk_readio = 0L;
    private long max_activetask = 0L;
    private long task_max_parallel_degree = 0L;
    private long task_waiting_timeout = 0L;
    private long task_running_timeout = 0L;

    public GBase8aResourcePool(GBase8aDataSource dataSource, String name) {
        this.dataSource = dataSource;
        this.name = name;
    }


    public GBase8aResourcePool(GBase8aDataSource dataSource, JDBCResultSet resultSet, GBase8aVC gBase8aVC) {
        this.dataSource = dataSource;
        this.gBase8aVC = gBase8aVC;
        try {
            this.name = CommonUtils.trim(resultSet.getString("resource_pool_name"));
            if (resultSet.getLong("resource_pool_type") == 1L) {
                this.type = "static";
            } else {
                this.type = "dynamic";
            }
            this.baseOn = CommonUtils.trim(resultSet.getString("parent_resource_pool_name"));
            this.priority = resultSet.getLong("priority");
            this.cpu_percent = resultSet.getLong("cpu_percent");
            this.max_memory = resultSet.getLong("max_memory") / 1024L / 1024L;
            this.max_temp_diskspace = resultSet.getLong("max_tmp_table_space") / 1024L / 1024L;
            this.max_disk_space = resultSet.getLong("max_disk_space") / 1024L / 1024L;
            this.max_disk_writeio = resultSet.getLong("disk_write_bps") / 1024L / 1024L;
            this.max_disk_readio = resultSet.getLong("disk_read_bps") / 1024L / 1024L;
            this.max_activetask = resultSet.getLong("max_task_number");
            this.task_max_parallel_degree = resultSet.getLong("task_parallel");
            this.task_waiting_timeout = resultSet.getLong("waiting_timeout");
            this.task_running_timeout = resultSet.getLong("running_timeout");
        } catch (SQLException e) {
            log.debug(e.getMessage());
        }
    }


    public String getVcName() {
        return this.gBase8aVC.getName();
    }

    @Property(viewable = true, order = 1)
    @NotNull
    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Property(viewable = true, order = 3)
    public String getBaseOn() {
        return this.baseOn;
    }

    public void setBaseOn(String baseOn) {
        this.baseOn = baseOn;
    }

    @Property(viewable = true, order = 2)
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Property(viewable = true, order = 11)
    public long getPriority() {
        return this.priority;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    @Property(viewable = true, order = 12)
    public long getCpu_percent() {
        return this.cpu_percent;
    }

    public void setCpu_percent(long cpu_percent) {
        this.cpu_percent = cpu_percent;
    }

    @Property(viewable = true, order = 13)
    public long getMax_memory() {
        return this.max_memory;
    }

    public void setMax_memory(long max_memory) {
        this.max_memory = max_memory;
    }

    @Property(viewable = true, order = 14)
    public long getMax_temp_diskspace() {
        return this.max_temp_diskspace;
    }

    public void setMax_temp_diskspace(long max_temp_diskspace) {
        this.max_temp_diskspace = max_temp_diskspace;
    }

    @Property(viewable = true, order = 15)
    public long getMax_disk_space() {
        return this.max_disk_space;
    }

    public void setMax_disk_space(long max_disk_space) {
        this.max_disk_space = max_disk_space;
    }

    @Property(viewable = true, order = 16)
    public long getMax_disk_writeio() {
        return this.max_disk_writeio;
    }

    public void setMax_disk_writeio(long max_disk_writeio) {
        this.max_disk_writeio = max_disk_writeio;
    }

    @Property(viewable = true, order = 17)
    public long getMax_disk_readio() {
        return this.max_disk_readio;
    }

    public void setMax_disk_readio(long max_disk_readio) {
        this.max_disk_readio = max_disk_readio;
    }

    @Property(viewable = true, order = 18)
    public long getMax_activetask() {
        return this.max_activetask;
    }

    public void setMax_activetask(long max_activetask) {
        this.max_activetask = max_activetask;
    }

    @Property(viewable = true, order = 19)
    public long getTask_max_parallel_degree() {
        return this.task_max_parallel_degree;
    }

    public void setTask_max_parallel_degree(long task_max_parallel_degree) {
        this.task_max_parallel_degree = task_max_parallel_degree;
    }

    @Property(viewable = true, order = 20)
    public long getTask_waiting_timeout() {
        return this.task_waiting_timeout;
    }

    public void setTask_waiting_timeout(long task_waiting_timeout) {
        this.task_waiting_timeout = task_waiting_timeout;
    }

    @Property(viewable = true, order = 21)
    public long getTask_running_timeout() {
        return this.task_running_timeout;
    }

    public void setTask_running_timeout(long task_running_timeout) {
        this.task_running_timeout = task_running_timeout;
    }

    @Override
    public boolean isPersisted() {
        return true;
    }

    @Override
    public String getDescription() {
        return this.name;
    }

    @Override
    public DBSObject getParentObject() {
        return (DBSObject) getDataSource().getContainer();
    }

    @Override
    public DBPDataSource getDataSource() {
        return this.dataSource;
    }

    @Override
    public DBPImage getObjectImage() {
        if ("static".equals(this.type)) {
            return DBIcon.TREE_SERVER;
        }
        return DBIcon.TREE_SERVERS;
    }
}