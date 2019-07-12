package com.blackviking.menorahfarms.Models;

public class CartModel {

    private String farmId;
    private long totalPrice;
    private long totalPayout;
    private int units;

    public CartModel() {
    }

    public CartModel(String farmId, long totalPrice, long totalPayout, int units) {
        this.farmId = farmId;
        this.totalPrice = totalPrice;
        this.totalPayout = totalPayout;
        this.units = units;
    }

    public String getFarmId() {
        return farmId;
    }

    public void setFarmId(String farmId) {
        this.farmId = farmId;
    }

    public long getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(long totalPrice) {
        this.totalPrice = totalPrice;
    }

    public long getTotalPayout() {
        return totalPayout;
    }

    public void setTotalPayout(long totalPayout) {
        this.totalPayout = totalPayout;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }
}
