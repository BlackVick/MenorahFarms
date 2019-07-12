package com.blackviking.menorahfarms.Models;

public class FollowedFarmModel {

    private String dateFollowed;

    public FollowedFarmModel() {
    }

    public FollowedFarmModel(String dateFollowed) {
        this.dateFollowed = dateFollowed;
    }

    public String getDateFollowed() {
        return dateFollowed;
    }

    public void setDateFollowed(String dateFollowed) {
        this.dateFollowed = dateFollowed;
    }
}
