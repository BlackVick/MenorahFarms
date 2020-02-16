package com.blackviking.menorahfarms.Models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class DueSponsorshipModel {

    private String user;
    private String sponsorshipId;
    private Long timeDue;

    public DueSponsorshipModel() {
    }

    public DueSponsorshipModel(String user, String sponsorshipId, Long timeDue) {
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

    public Long getTimeDue() {
        return timeDue;
    }

    public void setTimeDue(Long timeDue) {
        this.timeDue = timeDue;
    }
}
