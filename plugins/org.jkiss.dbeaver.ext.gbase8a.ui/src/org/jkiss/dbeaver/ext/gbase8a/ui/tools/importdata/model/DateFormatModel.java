package org.jkiss.dbeaver.ext.gbase8a.ui.tools.importdata.model;


public class DateFormatModel {
    private String oraFormat;
    private String javaFormat;
    private String gcFormat;

    public DateFormatModel(String oraFormat, String javaFormat, String gcFormat) {
        this.oraFormat = oraFormat;
        this.javaFormat = javaFormat;
        this.gcFormat = gcFormat;
    }

    public String getOraFormat() {
        return this.oraFormat;
    }

    public void setOraFormat(String oraFormat) {
        this.oraFormat = oraFormat;
    }

    public String getJavaFormat() {
        return this.javaFormat;
    }

    public void setJavaFormat(String javaFormat) {
        this.javaFormat = javaFormat;
    }

    public String getGcFormat() {
        return this.gcFormat;
    }

    public void setGcFormat(String gcFormat) {
        this.gcFormat = gcFormat;
    }
}
