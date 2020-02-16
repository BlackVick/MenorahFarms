package com.blackviking.menorahfarms.Models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class SponsorshipDetailsModel {

    private String sponsored_farm;
    private int units_sold;
    private int units_available;
    private int total_units;

    public SponsorshipDetailsModel() {
    }

    public SponsorshipDetailsModel(String sponsored_farm, int units_sold, int units_available, int total_units) {
        this.sponsored_farm = sponsored_farm;
        this.units_sold = units_sold;
        this.units_available = units_available;
        this.total_units = total_units;
    }

    public String getSponsored_farm() {
        return sponsored_farm;
    }

    public void setSponsored_farm(String sponsored_farm) {
        this.sponsored_farm = sponsored_farm;
    }

    public int getUnits_sold() {
        return units_sold;
    }

    public void setUnits_sold(int units_sold) {
        this.units_sold = units_sold;
    }

    public int getUnits_available() {
        return units_available;
    }

    public void setUnits_available(int units_available) {
        this.units_available = units_available;
    }

    public int getTotal_units() {
        return total_units;
    }

    public void setTotal_units(int total_units) {
        this.total_units = total_units;
    }
}
