package com.blackviking.menorahfarms;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blackviking.menorahfarms.HomeFragments.AccountFragment;
import com.blackviking.menorahfarms.HomeFragments.DashboardFragment;
import com.blackviking.menorahfarms.HomeFragments.FarmshopFragment;
import com.blackviking.menorahfarms.HomeFragments.HomeFragment;

public class Home extends AppCompatActivity {

    private LinearLayout homeSwitch, dashboardSwitch, farmstoreSwitch, accountSwitch;
    private TextView homeText, dashboardText, farmstoreText, accountText;
    private FrameLayout frame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        /*---   WIDGETS   ---*/
        homeSwitch = (LinearLayout)findViewById(R.id.homeLayout);
        dashboardSwitch = (LinearLayout)findViewById(R.id.dashboardLayout);
        farmstoreSwitch = (LinearLayout)findViewById(R.id.farmShopLayout);
        accountSwitch = (LinearLayout)findViewById(R.id.accountLayout);
        homeText = (TextView)findViewById(R.id.homeText);
        dashboardText = (TextView)findViewById(R.id.dashboardText);
        farmstoreText = (TextView)findViewById(R.id.farmShopText);
        accountText = (TextView)findViewById(R.id.accountText);
        frame = (FrameLayout)findViewById(R.id.frame);


        /*---   FRAGMENTS   ---*/
        final HomeFragment homeFragment = new HomeFragment();
        final DashboardFragment dashboardFragment = new DashboardFragment();
        final FarmshopFragment farmshopFragment = new FarmshopFragment();
        final AccountFragment accountFragment = new AccountFragment();





        /*---   BOTTOM NAV CONTROL   ---*/
        homeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                homeSwitch.setBackgroundResource(R.drawable.off_white_backround);
                homeText.setTextColor(getResources().getColor(R.color.colorPrimary));
                dashboardSwitch.setBackgroundResource(R.drawable.white_backround);
                dashboardText.setTextColor(getResources().getColor(R.color.black));
                farmstoreSwitch.setBackgroundResource(R.drawable.white_backround);
                farmstoreText.setTextColor(getResources().getColor(R.color.black));
                accountSwitch.setBackgroundResource(R.drawable.white_backround);
                accountText.setTextColor(getResources().getColor(R.color.black));

                setFragment(homeFragment);

            }
        });
        dashboardSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                homeSwitch.setBackgroundResource(R.drawable.white_backround);
                homeText.setTextColor(getResources().getColor(R.color.black));
                dashboardSwitch.setBackgroundResource(R.drawable.off_white_backround);
                dashboardText.setTextColor(getResources().getColor(R.color.colorPrimary));
                farmstoreSwitch.setBackgroundResource(R.drawable.white_backround);
                farmstoreText.setTextColor(getResources().getColor(R.color.black));
                accountSwitch.setBackgroundResource(R.drawable.white_backround);
                accountText.setTextColor(getResources().getColor(R.color.black));

                setFragment(dashboardFragment);

            }
        });
        farmstoreSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                homeSwitch.setBackgroundResource(R.drawable.white_backround);
                homeText.setTextColor(getResources().getColor(R.color.black));
                dashboardSwitch.setBackgroundResource(R.drawable.white_backround);
                dashboardText.setTextColor(getResources().getColor(R.color.black));
                farmstoreSwitch.setBackgroundResource(R.drawable.off_white_backround);
                farmstoreText.setTextColor(getResources().getColor(R.color.colorPrimary));
                accountSwitch.setBackgroundResource(R.drawable.white_backround);
                accountText.setTextColor(getResources().getColor(R.color.black));

                setFragment(farmshopFragment);

            }
        });
        accountSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                homeSwitch.setBackgroundResource(R.drawable.white_backround);
                homeText.setTextColor(getResources().getColor(R.color.black));
                dashboardSwitch.setBackgroundResource(R.drawable.white_backround);
                dashboardText.setTextColor(getResources().getColor(R.color.black));
                farmstoreSwitch.setBackgroundResource(R.drawable.white_backround);
                farmstoreText.setTextColor(getResources().getColor(R.color.black));
                accountSwitch.setBackgroundResource(R.drawable.off_white_backround);
                accountText.setTextColor(getResources().getColor(R.color.colorPrimary));

                setFragment(accountFragment);

            }
        });


        setBaseFragment(dashboardFragment);
    }

    private void setBaseFragment(DashboardFragment dashboardFragment) {

        setFragment(dashboardFragment);

        homeSwitch.setBackgroundResource(R.drawable.white_backround);
        homeText.setTextColor(getResources().getColor(R.color.black));
        dashboardSwitch.setBackgroundResource(R.drawable.off_white_backround);
        dashboardText.setTextColor(getResources().getColor(R.color.colorPrimary));
        farmstoreSwitch.setBackgroundResource(R.drawable.white_backround);
        farmstoreText.setTextColor(getResources().getColor(R.color.black));
        accountSwitch.setBackgroundResource(R.drawable.white_backround);
        accountText.setTextColor(getResources().getColor(R.color.black));
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }
}
