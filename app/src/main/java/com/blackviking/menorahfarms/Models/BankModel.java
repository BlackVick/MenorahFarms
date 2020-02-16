package com.blackviking.menorahfarms.Models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class BankModel {

    private String bankName;

    public BankModel() {
    }

    public BankModel(String bankName) {
        this.bankName = bankName;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
}
