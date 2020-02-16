package com.blackviking.menorahfarms.Models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class ProjectManagerModel {

    private String name;
    private String profilePicture;
    private String whatsapp;

    public ProjectManagerModel() {
    }

    public ProjectManagerModel(String name, String profilePicture, String whatsapp) {
        this.name = name;
        this.profilePicture = profilePicture;
        this.whatsapp = whatsapp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }
}
