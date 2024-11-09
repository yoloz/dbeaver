package org.jkiss.dbeaver.ext.gbase8a.model;


public class GBase8aResourcePoolUsage {

    private String nodeName;
    private String vcName;
    private String ResourcePoolname;
    private String priority;
    private String running_tasks;
    private String waiting_tasks;
    private String cpu_usage;
    private String mem_usage;
    private String disk_usage;
    private String disk_writeio;
    private String disk_readio;
    private String servied_tasks;
    private String waiting_avg_time;
    private String running_avg_time;
    private String sample_time;

    public String getNodeName() {
        return this.nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getVcName() {
        return this.vcName;
    }

    public void setVcName(String vcName) {
        this.vcName = vcName;
    }

    public String getResourcePoolname() {
        return this.ResourcePoolname;
    }

    public void setResourcePoolname(String resourcePoolname) {
        this.ResourcePoolname = resourcePoolname;
    }

    public String getPriority() {
        return this.priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getRunning_tasks() {
        return this.running_tasks;
    }

    public void setRunning_tasks(String running_tasks) {
        this.running_tasks = running_tasks;
    }

    public String getWaiting_tasks() {
        return this.waiting_tasks;
    }

    public void setWaiting_tasks(String waiting_tasks) {
        this.waiting_tasks = waiting_tasks;
    }

    public String getCpu_usage() {
        return this.cpu_usage;
    }

    public void setCpu_usage(String cpu_usage) {
        this.cpu_usage = cpu_usage;
    }

    public String getMem_usage() {
        return this.mem_usage;
    }

    public void setMem_usage(String mem_usage) {
        this.mem_usage = mem_usage;
    }

    public String getDisk_usage() {
        return this.disk_usage;
    }

    public void setDisk_usage(String disk_usage) {
        this.disk_usage = disk_usage;
    }

    public String getDisk_writeio() {
        return this.disk_writeio;
    }

    public void setDisk_writeio(String disk_writeio) {
        this.disk_writeio = disk_writeio;
    }

    public String getDisk_readio() {
        return this.disk_readio;
    }

    public void setDisk_readio(String disk_readio) {
        this.disk_readio = disk_readio;
    }

    public String getServied_tasks() {
        return this.servied_tasks;
    }

    public void setServied_tasks(String servied_tasks) {
        this.servied_tasks = servied_tasks;
    }

    public String getWaiting_avg_time() {
        return this.waiting_avg_time;
    }

    public void setWaiting_avg_time(String waiting_avg_time) {
        this.waiting_avg_time = waiting_avg_time;
    }

    public String getRunning_avg_time() {
        return this.running_avg_time;
    }

    public void setRunning_avg_time(String running_avg_time) {
        this.running_avg_time = running_avg_time;
    }

    public String getSample_time() {
        return this.sample_time;
    }

    public void setSample_time(String sample_time) {
        this.sample_time = sample_time;
    }
}
