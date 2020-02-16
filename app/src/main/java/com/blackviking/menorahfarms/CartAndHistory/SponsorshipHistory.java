package com.blackviking.menorahfarms.CartAndHistory;

import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.blackviking.menorahfarms.R;

public class SponsorshipHistory extends AppCompatActivity {

    private ImageView backButton;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private HistoryTabsPager tabsPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sponsorship_history);


        /*---   WIDGETS    ---*/
        backButton = findViewById(R.id.backButton);
        tabLayout = findViewById(R.id.historyTabs);
        viewPager = findViewById(R.id.historyViewPager);


        /*---   TABS   ---*/
        tabsPager = new HistoryTabsPager(getSupportFragmentManager());
        viewPager.setAdapter(tabsPager);
        tabLayout.setupWithViewPager(viewPager);


        backButton.setOnClickListener(v -> finish());


    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
