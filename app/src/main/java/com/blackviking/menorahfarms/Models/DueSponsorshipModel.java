package com.blackviking.menorahfarms.Models;

public class DueSponsorshipModel {

    private String user;
    private String sponsorshipId;
    private String timeDue;

    public DueSponsorshipModel() {
    }

    public DueSponsorshipModel(String user, String sponsorshipId, String timeDue) {
        this.user = user;
        this.sponsorshipId = sponsorshipId;
        this.timeDue = timeDue;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSponsorshipId() {
        return sponsorshipId;
    }

    public void setSponsorshipId(String sponsorshipId) {
        this.sponsorshipId = sponsorshipId;
    }

    public String getTimeDue() {
        return timeDue;
    }

    public void setTimeDue(String timeDue) {
        this.timeDue = timeDue;
    }
}
