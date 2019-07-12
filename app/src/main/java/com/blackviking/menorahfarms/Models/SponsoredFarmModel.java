package com.blackviking.menorahfarms.Models;

public class SponsoredFarmModel {

    private String sponsorReturn;
    private String cycleEndDate;
    private String cycleStartDate;
    private String sponsorRefNumber;
    private String unitPrice;
    private String sponsoredUnits;
    private String sponsoredUnitPrice;
    private String sponsoredFarmType;
    private String sponsoredFarmRoi;
    private String sponsorshipDuration;
    private long startPoint;

    public SponsoredFarmModel() {
    }

    public SponsoredFarmModel(String sponsorReturn, String cycleEndDate, String cycleStartDate, String sponsorRefNumber, String unitPrice, String sponsoredUnits, String sponsoredUnitPrice, String sponsoredFarmType, String sponsoredFarmRoi, String sponsorshipDuration, long startPoint) {
        this.sponsorReturn = sponsorReturn;
        this.cycleEndDate = cycleEndDate;
        this.cycleStartDate = cycleStartDate;
        this.sponsorRefNumber = sponsorRefNumber;
        this.unitPrice = unitPrice;
        this.sponsoredUnits = sponsoredUnits;
        this.sponsoredUnitPrice = sponsoredUnitPrice;
        this.sponsoredFarmType = sponsoredFarmType;
        this.sponsoredFarmRoi = sponsoredFarmRoi;
        this.sponsorshipDuration = sponsorshipDuration;
        this.startPoint = startPoint;
    }

    public String getSponsorReturn() {
        return sponsorReturn;
    }

    public void setSponsorReturn(String sponsorReturn) {
        this.sponsorReturn = sponsorReturn;
    }

    public String getCycleEndDate() {
        return cycleEndDate;
    }

    public void setCycleEndDate(String cycleEndDate) {
        this.cycleEndDate = cycleEndDate;
    }

    public String getCycleStartDate() {
        return cycleStartDate;
    }

    public void setCycleStartDate(String cycleStartDate) {
        this.cycleStartDate = cycleStartDate;
    }

    public String getSponsorRefNumber() {
        return sponsorRefNumber;
    }

    public void setSponsorRefNumber(String sponsorRefNumber) {
        this.sponsorRefNumber = sponsorRefNumber;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(String unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getSponsoredUnits() {
        return sponsoredUnits;
    }

    public void setSponsoredUnits(String sponsoredUnits) {
        this.sponsoredUnits = sponsoredUnits;
    }

    public String getSponsoredUnitPrice() {
        return sponsoredUnitPrice;
    }

    public void setSponsoredUnitPrice(String sponsoredUnitPrice) {
        this.sponsoredUnitPrice = sponsoredUnitPrice;
    }

    public String getSponsoredFarmType() {
        return sponsoredFarmType;
    }

    public void setSponsoredFarmType(String sponsoredFarmType) {
        this.sponsoredFarmType = sponsoredFarmType;
    }

    public String getSponsoredFarmRoi() {
        return sponsoredFarmRoi;
    }

    public void setSponsoredFarmRoi(String sponsoredFarmRoi) {
        this.sponsoredFarmRoi = sponsoredFarmRoi;
    }

    public String getSponsorshipDuration() {
        return sponsorshipDuration;
    }

    public void setSponsorshipDuration(String sponsorshipDuration) {
        this.sponsorshipDuration = sponsorshipDuration;
    }

    public long getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(long startPoint) {
        this.startPoint = startPoint;
    }
}
