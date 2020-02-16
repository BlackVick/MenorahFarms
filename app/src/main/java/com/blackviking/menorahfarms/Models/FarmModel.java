package com.blackviking.menorahfarms.Models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class FarmModel {

    private String farmName;
    private String farmLocation;
    private String farmType;
    private String pricePerUnit;
    private String farmRoi;
    private String sponsorDuration;
    private String farmState;
    private String farmImage;
    private String farmImageThumb;
    private String unitsAvailable;
    private String unitsSold;
    private String packaged;
    private String packagedType;
    private String projectManager;
    private String farmDescription;
    private String farmNotiId;

    public FarmModel() {
    }

    public FarmModel(String farmName, String farmLocation, String farmType, String pricePerUnit, String farmRoi, String sponsorDuration, String farmState, String farmImage, String farmImageThumb, String unitsAvailable, String unitsSold, String packaged, String packagedType, String projectManager, String farmDescription, String farmNotiId) {
        this.farmName = farmName;
        this.farmLocation = farmLocation;
        this.farmType = farmType;
        this.pricePerUnit = pricePerUnit;
        this.farmRoi = farmRoi;
        this.sponsorDuration = sponsorDuration;
        this.farmState = farmState;
        this.farmImage = farmImage;
        this.farmImageThumb = farmImageThumb;
        this.unitsAvailable = unitsAvailable;
        this.unitsSold = unitsSold;
        this.packaged = packaged;
        this.packagedType = packagedType;
        this.projectManager = projectManager;
        this.farmDescription = farmDescription;
        this.farmNotiId = farmNotiId;
    }


    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }

    public String getFarmLocation() {
        return farmLocation;
    }

    public void setFarmLocation(String farmLocation) {
        this.farmLocation = farmLocation;
    }

    public String getFarmType() {
        return farmType;
    }

    public void setFarmType(String farmType) {
        this.farmType = farmType;
    }

    public String getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(String pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public String getFarmRoi() {
        return farmRoi;
    }

    public void setFarmRoi(String farmRoi) {
        this.farmRoi = farmRoi;
    }

    public String getSponsorDuration() {
        return sponsorDuration;
    }

    public void setSponsorDuration(String sponsorDuration) {
        this.sponsorDuration = sponsorDuration;
    }

    public String getFarmState() {
        return farmState;
    }

    public void setFarmState(String farmState) {
        this.farmState = farmState;
    }

    public String getFarmImage() {
        return farmImage;
    }

    public void setFarmImage(String farmImage) {
        this.farmImage = farmImage;
    }

    public String getFarmImageThumb() {
        return farmImageThumb;
    }

    public void setFarmImageThumb(String farmImageThumb) {
        this.farmImageThumb = farmImageThumb;
    }

    public String getUnitsAvailable() {
        return unitsAvailable;
    }

    public void setUnitsAvailable(String unitsAvailable) {
        this.unitsAvailable = unitsAvailable;
    }

    public String getUnitsSold() {
        return unitsSold;
    }

    public void setUnitsSold(String unitsSold) {
        this.unitsSold = unitsSold;
    }

    public String getPackaged() {
        return packaged;
    }

    public void setPackaged(String packaged) {
        this.packaged = packaged;
    }

    public String getPackagedType() {
        return packagedType;
    }

    public void setPackagedType(String packagedType) {
        this.packagedType = packagedType;
    }

    public String getProjectManager() {
        return projectManager;
    }

    public void setProjectManager(String projectManager) {
        this.projectManager = projectManager;
    }

    public String getFarmDescription() {
        return farmDescription;
    }

    public void setFarmDescription(String farmDescription) {
        this.farmDescription = farmDescription;
    }

    public String getFarmNotiId() {
        return farmNotiId;
    }

    public void setFarmNotiId(String farmNotiId) {
        this.farmNotiId = farmNotiId;
    }
}
