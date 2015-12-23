package org.zywx.wbpalmstar.plugin.uexfilemgr.vo;

import java.io.Serializable;

public class FileSizeDataVO implements Serializable{
    private static final long serialVersionUID = -417243106887692957L;
    private String id;
    private String path;
    private String unit = "B";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUnit() {
        return unit.toUpperCase();
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
