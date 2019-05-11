package com.mediatek.watchapp.online;

public class OnlineClockSkinXMLNode {
    private String customer;
    private String file;
    private String name;
    private String preview;
    private String skinid;
    private String type;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSkinId() {
        return this.skinid;
    }

    public void setSkinId(String skinid) {
        this.skinid = skinid;
    }

    public String getPreview() {
        return this.preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public String getCustomer() {
        return this.customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getClockTpye() {
        return this.type;
    }

    public void setClockTpye(String type) {
        this.type = type;
    }

    public String getFilePath() {
        return this.file;
    }

    public void setFilePath(String filePath) {
        this.file = filePath;
    }
}
