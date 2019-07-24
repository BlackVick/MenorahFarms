package com.blackviking.menorahfarms.DashboardMenu;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.blackviking.menorahfarms.R;

public class FarmUpdates extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_updates);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
