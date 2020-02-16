package com.blackviking.menorahfarms.Models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class NewFarmModel {

    private String farm_description;
    private String farm_image;
    private String farm_location;
    private String farm_name;
    private int farm_roi;
    private String farm_status;
    private String farm_type;
    private String gateway_duration;
    private long student_price;
    private long worker_price;
    private int sponsor_duration;
    private int units_available;
    private int units_sold;

    public NewFarmModel() {
    }

    public NewFarmModel(String farm_description, String farm_image, String farm_location, String farm_name, int farm_roi, String farm_status, String farm_type, String gateway_duration, long student_price, long worker_price, int sponsor_duration, int units_available, int units_sold) {
        this.farm_description = farm_description;
        this.farm_image = farm_image;
        this.farm_location = farm_location;
        this.farm_name = farm_name;
        this.farm_roi = farm_roi;
        this.farm_status = farm_status;
        this.farm_type = farm_type;
        this.gateway_duration = gateway_duration;
        this.student_price = student_price;
        this.worker_price = worker_price;
        this.sponsor_duration = sponsor_duration;
        this.units_available = units_available;
        this.units_sold = units_sold;
    }

    public String getFarm_description() {
        return farm_description;
    }

    public void setFarm_description(String farm_description) {
        this.farm_description = farm_description;
    }

    public String getFarm_image() {
        return farm_image;
    }

    public void setFarm_image(String farm_image) {
        this.farm_image = farm_image;
    }

    public String getFarm_location() {
        return farm_location;
    }

    public void setFarm_location(String farm_location) {
        this.farm_location = farm_location;
    }

    public String getFarm_name() {
        return farm_name;
    }

    public void setFarm_name(String farm_name) {
        this.farm_name = farm_name;
    }

    public int getFarm_roi() {
        return farm_roi;
    }

    public void setFarm_roi(int farm_roi) {
        this.farm_roi = farm_roi;
    }

    public String getFarm_status() {
        return farm_status;
    }

    public void setFarm_status(String farm_status) {
        this.farm_status = farm_status;
    }

    public String getFarm_type() {
        return farm_type;
    }

    public void setFarm_type(String farm_type) {
        this.farm_type = farm_type;
    }

    public String getGateway_duration() {
        return gateway_duration;
    }

    public void setGateway_duration(String gateway_duration) {
        this.gateway_duration = gateway_duration;
    }

    public long getStudent_price() {
        return student_price;
    }

    public void setStudent_price(long student_price) {
        this.student_price = student_price;
    }

    public long getWorker_price() {
        return worker_price;
    }

    public void setWorker_price(long worker_price) {
        this.worker_price = worker_price;
    }

    public int getSponsor_duration() {
        return sponsor_duration;
    }

    public void setSponsor_duration(int sponsor_duration) {
        this.sponsor_duration = sponsor_duration;
    }

    public int getUnits_available() {
        return units_available;
    }

    public void setUnits_available(int units_available) {
        this.units_available = units_available;
    }

    public int getUnits_sold() {
        return units_sold;
    }

    public void setUnits_sold(int units_sold) {
        this.units_sold = units_sold;
    }
}
