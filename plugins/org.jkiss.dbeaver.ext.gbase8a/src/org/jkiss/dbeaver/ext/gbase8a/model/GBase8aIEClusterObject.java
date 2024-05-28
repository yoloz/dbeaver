package org.jkiss.dbeaver.ext.gbase8a.model;


public class GBase8aIEClusterObject implements Comparable<Object> {

    private String objectName;
    private String type;

    public String getObjectName() {
        return this.objectName;
    }


    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }


    public String getType() {
        return this.type;
    }


    public void setType(String type) {
        this.type = type;
    }


    public int compareTo(Object o) {
        GBase8aIEClusterObject other = (GBase8aIEClusterObject) o;
        return this.objectName.compareTo(other.objectName);
    }
}
