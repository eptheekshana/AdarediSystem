package com.bedsheet.orders.curfox.dto;

import java.time.LocalDateTime;

public class CurfoxTrackingEvent {

    private String statusName;
    private LocalDateTime dateTime;
    private String dateTimeAgo;
    private String userName;

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getDateTimeAgo() {
        return dateTimeAgo;
    }

    public void setDateTimeAgo(String dateTimeAgo) {
        this.dateTimeAgo = dateTimeAgo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
