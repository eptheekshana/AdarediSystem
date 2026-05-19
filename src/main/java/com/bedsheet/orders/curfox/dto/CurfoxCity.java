package com.bedsheet.orders.curfox.dto;

public class CurfoxCity {

    private String id;
    private String name;
    private String stateName;

    public CurfoxCity() {
    }

    public CurfoxCity(String id, String name, String stateName) {
        this.id = id;
        this.name = name;
        this.stateName = stateName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }
}
