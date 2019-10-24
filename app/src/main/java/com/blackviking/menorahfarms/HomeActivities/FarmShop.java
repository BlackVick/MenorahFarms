package com.blackviking.menorahfarms.HomeActivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.ApplicationClass;
import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.FarmshopFragments.TabsPager;
import com.blackviking.menorahfarms.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FarmShop extends AppCompatActivity {

    private LinearLayout dashboardSwitch, farmstoreSwitch, accountSwitch;
    private TextView dashboardText, farmstoreText, accountText;

    private RelativeLayout noInternetLayout;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TabsPager tabsPager;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference farmRef;
    private android.app.AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_shop);


        //Firebase
        farmRef = db.getReference("Farms");

        /*---   WIDGETS   ---*/
        dashboardSwitch = (LinearLayout)findViewById(R.id.dashboardLayout);
        farmstoreSwitch = (LinearLayout)findViewById(R.id.farmShopLayout);
        accountSwitch = (LinearLayout)findViewById(R.id.accountLayout);
        dashboardText = (TextView)findViewById(R.id.dashboardText);
        farmstoreText = (TextView)findViewById(R.id.farmShopText);
        accountText = (TextView)findViewById(R.id.accountText);
        noInternetLayout = findViewById(R.id.noInternetLayout);


        tabLayout = (TabLayout)findViewById(R.id.farmshopTabs);
        viewPager = (ViewPager)findViewById(R.id.farmshopViewPager);


        /*----------    TABS HANDLER   ----------*/
        tabsPager = new TabsPager(getSupportFragmentManager());



        /*---   BOTTOM NAV   ---*/
        dashboardText.setTextColor(getResources().getColor(R.color.black));
        farmstoreText.setTextColor(getResources().getColor(R.color.colorPrimary));
        accountText.setTextColor(getResources().getColor(R.color.black));


        //show loading dialog
        showLoadingDialog("Loading all farms . . .");

        //load farms into memory
        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

                //check all cases
                if (output == 1){

                    farmRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            //dismiss dialog
                            alertDialog.dismiss();

                            setFarms();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                            Toast.makeText(FarmShop.this, "cancelled", Toast.LENGTH_SHORT).show();

                        }
                    });

                } else

                if (output == 0){

                    //set layout
                    alertDialog.dismiss();
                    noInternetLayout.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.GONE);
                    viewPager.setVisibility(View.GONE);

                } else

                if (output == 2){

                    //set layout
                    alertDialog.dismiss();
                    noInternetLayout.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.GONE);
                    viewPager.setVisibility(View.GONE);

                }

            }
        }).execute();


        dashboardSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent dashboardIntent = new Intent(FarmShop.this, Dashboard.class);
                startActivity(dashboardIntent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });
        accountSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent accountIntent = new Intent(FarmShop.this, Account.class);
                startActivity(accountIntent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });

    }

    private void setFarms() {

        //setup fragments with tabs
        viewPager.setAdapter(tabsPager);
        tabLayout.setupWithViewPager(viewPager);

        //set layout
        noInternetLayout.setVisibility(View.GONE);
        tabLayout.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);

    }

    /*---   LOADING DIALOG   ---*/
    public void showLoadingDialog(String theMessage){

        alertDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.loading_dialog,null);

        final TextView loadingText = viewOptions.findViewById(R.id.loadingText);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        loadingText.setText(theMessage);

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });

        alertDialog.show();

    }
}
