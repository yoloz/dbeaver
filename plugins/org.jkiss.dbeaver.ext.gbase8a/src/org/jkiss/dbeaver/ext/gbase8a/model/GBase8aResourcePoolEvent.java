package org.jkiss.dbeaver.ext.gbase8a.model;


public class GBase8aResourcePoolEvent {

    private String nodeName;
    private String vcName;
    private String ResourcePoolName;
    private String eventTime;
    private String taskId;
    private String statement;
    private String eventType;
    private String eventDesc;

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

    public String getResourcePoolName() {
        return this.ResourcePoolName;
    }

    public void setResourcePoolName(String resourcePoolName) {
        this.ResourcePoolName = resourcePoolName;
    }

    public String getEventTime() {
        return this.eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getTaskId() {
        return this.taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getStatement() {
        return this.statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public String getEventType() {
        return this.eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventDesc() {
        return this.eventDesc;
    }

    public void setEventDesc(String eventDesc) {
        this.eventDesc = eventDesc;
    }
}