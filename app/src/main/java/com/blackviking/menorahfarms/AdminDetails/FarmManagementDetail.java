package com.blackviking.menorahfarms.AdminDetails;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Models.FarmModel;
import com.blackviking.menorahfarms.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jcminarro.roundkornerlayout.RoundKornerRelativeLayout;
import com.squareup.picasso.Picasso;

public class FarmManagementDetail extends AppCompatActivity {

    private ImageView farmManageDetailImage, decreaseHourNumber, increaseHourNumber, backButton;
    private TextView farmManageDetailsType, farmManageDetailLocation, farmManageDetailROI,
                        hourNumber, farmManageDetailStatus;
    private Button activateFarm, confirmActivation;
    private RoundKornerRelativeLayout activateFarmLayout;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference farmRef;

    private String farmId;
    private int hourInt = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_management_detail);


        //intent data
        farmId = getIntent().getStringExtra("FarmId");


        //Firebase
        farmRef = db.getReference("Farms");


        //widgets
        farmManageDetailImage = findViewById(R.id.farmManageDetailImage);
        decreaseHourNumber = findViewById(R.id.decreaseHourNumber);
        increaseHourNumber = findViewById(R.id.increaseHourNumber);
        farmManageDetailsType = findViewById(R.id.farmManageDetailsType);
        farmManageDetailLocation = findViewById(R.id.farmManageDetailLocation);
        farmManageDetailROI = findViewById(R.id.farmManageDetailROI);
        hourNumber = findViewById(R.id.hourNumber);
        farmManageDetailStatus = findViewById(R.id.farmManageDetailStatus);
        activateFarm = findViewById(R.id.activateFarm);
        confirmActivation = findViewById(R.id.confirmActivation);
        activateFarmLayout = findViewById(R.id.activateFarmLayout);
        backButton = findViewById(R.id.backButton);


        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

                //check all cases
                if (output == 1){

                    loadFarmDetails(farmId);

                } else

                if (output == 0){

                    //set layout
                    Toast.makeText(FarmManagementDetail.this, "No internet access", Toast.LENGTH_SHORT).show();

                } else

                if (output == 2){

                    //set layout
                    Toast.makeText(FarmManagementDetail.this, "No network connection detected", Toast.LENGTH_SHORT).show();
                }

            }
        }).execute();


        //back
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void loadFarmDetails(final String farmid) {

        farmRef.child(farmid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        FarmModel currentFarm = dataSnapshot.getValue(FarmModel.class);

                        if (currentFarm != null){

                            setFarmDetails(currentFarm, farmid);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void setFarmDetails(FarmModel currentFarm, final String farmid) {

        if (!currentFarm.getFarmImageThumb().equalsIgnoreCase("")){

            Picasso.get()
                    .load(currentFarm.getFarmImageThumb())
                    .placeholder(R.drawable.menorah_placeholder)
                    .into(farmManageDetailImage);

        }

        farmManageDetailsType.setText(currentFarm.getFarmType());
        farmManageDetailLocation.setText(currentFarm.getFarmLocation());
        farmManageDetailROI.setText("Returns " + currentFarm.getFarmRoi() + "% in " + currentFarm.getSponsorDuration() + " months");
        farmManageDetailStatus.setText(currentFarm.getFarmState());



        if (currentFarm.getFarmState().equalsIgnoreCase("Now Selling")){

            activateFarmLayout.setVisibility(View.GONE);
            activateFarm.setVisibility(View.GONE);

        } else {

            activateFarmLayout.setVisibility(View.GONE);
            activateFarm.setVisibility(View.VISIBLE);

            activateFarm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    activateFarmLayout.setVisibility(View.VISIBLE);
                    activateFarm.setVisibility(View.GONE);

                }
            });


            //increase hour(s)
            hourNumber.setText(String.valueOf(hourInt));
            increaseHourNumber.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    hourInt++;
                    hourNumber.setText(String.valueOf(hourInt));

                }
            });

            //decrease hour(s)
            decreaseHourNumber.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (hourInt == 0){



                    } else {

                        hourInt --;
                        hourNumber.setText(String.valueOf(hourInt));

                    }

                }
            });


            //confirm activation
            confirmActivation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startTheCountdownService(farmid, hourInt);

                }
            });

        }

    }

    private void startTheCountdownService(String farmid, int hourInt) {
    }
}
