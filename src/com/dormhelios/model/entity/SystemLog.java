package com.dormhelios.model.entity;

/**
 * Entity representing a record in the system_logs table
 */
public class SystemLog {
    private String name;
    private String value;

    public SystemLog() {}

    public SystemLog(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}