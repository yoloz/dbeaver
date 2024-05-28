package org.jkiss.dbeaver.ext.gbase8a.model;


public class GBase8aColumnModel {

    private String name;
    private String comment;
    private boolean hash;
    private String type;
    private String defValue;
    private String isNull;

    public GBase8aColumnModel(String name, String type) {
        this.name = name;
        this.type = type;
    }


    public GBase8aColumnModel(String name, String comment, boolean hash, String type, String defValue, String isNull) {
        this.name = name;
        this.comment = comment;
        this.hash = hash;
        this.type = type;
        this.defValue = defValue;
        this.isNull = isNull;
    }


    public GBase8aColumnModel(String name, String comment, String type, String isNull) {
        this.name = name;
        this.comment = comment;
        this.type = type;
        this.isNull = isNull;
    }

    public String getName() {
        return this.name;
    }

    public String getComment() {
        return this.comment;
    }

    public boolean isHash() {
        return this.hash;
    }

    public String getType() {
        return this.type;
    }

    public String getDefValue() {
        return this.defValue;
    }

    public String getIsNull() {
        return this.isNull;
    }


    public String toString() {
        return "ColumnModel [name=" +
                this.name +
                ", comment=" +
                this.comment +
                ", hash=" +
                this.hash +
                ", type=" +
                this.type +
                ", defValue=" +
                this.defValue +
                ", isNull=" +
                this.isNull +
                "]";
    }
}
