package com.example.histoquiz.model;

public class Friend {
    protected String UID;
    protected String name;

    public Friend(String name, String UID){
        this.name = name;
        this.UID = UID;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
