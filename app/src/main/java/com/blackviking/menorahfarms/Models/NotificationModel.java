package com.blackviking.menorahfarms.Models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class NotificationModel {

    private String topic;
    private String message;
    private String time;

    public NotificationModel() {
    }

    public NotificationModel(String topic, String message, String time) {
        this.topic = topic;
        this.message = message;
        this.time = time;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
