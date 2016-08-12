package org.zywx.wbpalmstar.plugin.uexfilemgr.vo;

import java.io.Serializable;

public class ResultFileSizeVO implements Serializable{
    private static final long serialVersionUID = -4849172627309933109L;
    private String id;
    private int errorCode;
    private double data;
    private String unit;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public double getData() {
        return data;
    }

    public void setData(double data) {
        this.data = data;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
