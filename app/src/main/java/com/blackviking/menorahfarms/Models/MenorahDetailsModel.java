package com.blackviking.menorahfarms.Models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class MenorahDetailsModel {

    private String account_name;
    private String account_number;
    private String bank;

    public MenorahDetailsModel() {
    }

    public MenorahDetailsModel(String account_name, String account_number, String bank) {
        this.account_name = account_name;
        this.account_number = account_number;
        this.bank = bank;
    }

    public String getAccount_name() {
        return account_name;
    }

    public void setAccount_name(String account_name) {
        this.account_name = account_name;
    }

    public String getAccount_number() {
        return account_number;
    }

    public void setAccount_number(String account_number) {
        this.account_number = account_number;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }
}
