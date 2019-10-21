package com.blackviking.menorahfarms.AdminDetails;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.HomeActivities.Dashboard;
import com.blackviking.menorahfarms.Models.FarmModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.Services.AdminMonitorService;
import com.blackviking.menorahfarms.Services.SponsorshipMonitor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jcminarro.roundkornerlayout.RoundKornerRelativeLayout;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.paperdb.Paper;

public class FarmManagementDetail extends AppCompatActivity {

    private ImageView farmManageDetailImage, decreaseHourNumber, increaseHourNumber, backButton;
    private TextView farmManageDetailsType, farmManageDetailLocation, farmManageDetailROI,
                        hourNumber, farmManageDetailStatus;
    private Button activateFarm, confirmActivation, deactivateFarm;
    private RoundKornerRelativeLayout activateFarmLayout;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference farmRef, adminMonitorRef;

    private String farmId;
    private int hourInt = 0;

    private boolean isMonitorRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_management_detail);


        //intent data
        farmId = getIntent().getStringExtra("FarmId");


        //Firebase
        farmRef = db.getReference("Farms");
        adminMonitorRef = db.getReference("AdminMonitor");


        //check service run
        if (Paper.book().read(Common.isFarmServiceRunning) == null){

            Paper.book().write(Common.isFarmServiceRunning, false);
            isMonitorRunning = Paper.book().read(Common.isFarmServiceRunning);

        } else {

            isMonitorRunning = Paper.book().read(Common.isFarmServiceRunning);

        }


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
        deactivateFarm = findViewById(R.id.deactivateFarm);


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

    private void setFarmDetails(final FarmModel currentFarm, final String farmid) {

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
            deactivateFarm.setVisibility(View.VISIBLE);

            deactivateFarm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    deactivateTheFarm(currentFarm);

                }
            });

        } else {

            activateFarmLayout.setVisibility(View.GONE);
            activateFarm.setVisibility(View.VISIBLE);
            deactivateFarm.setVisibility(View.GONE);

            activateFarm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    activateFarmLayout.setVisibility(View.VISIBLE);
                    activateFarm.setVisibility(View.GONE);
                    deactivateFarm.setVisibility(View.GONE);

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

                    if (hourInt > 0) {
                        startTheCountdownService(farmid, hourInt, currentFarm);
                    }

                }
            });

        }

    }

    private void deactivateTheFarm(FarmModel currentFarm) {

        final Map<String, Object> deactivateFarmMap = new HashMap<>();
        deactivateFarmMap.put("farmState", "Sold Out");
        deactivateFarmMap.put("unitsAvailable", "");

        if (Paper.book().read(Common.isFarmServiceRunning)){

            Intent serviceIntent = new Intent(FarmManagementDetail.this, AdminMonitorService.class);
            stopService(serviceIntent);

        }

        farmRef.orderByChild("farmNotiId")
                .equalTo(currentFarm.getFarmNotiId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snap : dataSnapshot.getChildren()){

                            String theFarmId = snap.getKey();

                            farmRef.child(theFarmId)
                                    .updateChildren(deactivateFarmMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                activateFarmLayout.setVisibility(View.GONE);
                                                activateFarm.setVisibility(View.VISIBLE);
                                                deactivateFarm.setVisibility(View.GONE);




                                            } else {

                                                Toast.makeText(FarmManagementDetail.this, "Could not complete action", Toast.LENGTH_SHORT).show();

                                            }

                                        }
                                    });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void startTheCountdownService(final String farmid, final int hourInt, final FarmModel currentFarm) {

        //check start time
        /*final Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy  hh:mm");
        String currentTimeString = formatter.format(currentTime);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + hourInt);
        final Date endTime = calendar.getTime();
        String endTimeString = formatter.format(endTime);
*/
        final Map<String, Object> activateFarmMap = new HashMap<>();
        activateFarmMap.put("farmState", "Now Selling");
        activateFarmMap.put("unitsAvailable", "");

        farmRef.orderByChild("farmNotiId")
                .equalTo(currentFarm.getFarmNotiId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snap : dataSnapshot.getChildren()){

                            String theFarmId = snap.getKey();

                            farmRef.child(theFarmId)
                                    .updateChildren(activateFarmMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                activateFarmLayout.setVisibility(View.GONE);
                                                activateFarm.setVisibility(View.GONE);
                                                deactivateFarm.setVisibility(View.VISIBLE);


                                            } else {

                                                Toast.makeText(FarmManagementDetail.this, "Could not complete action", Toast.LENGTH_SHORT).show();

                                            }

                                        }
                                    });

                        }

                        Intent adminMonitorIntent = new Intent(FarmManagementDetail.this, AdminMonitorService.class);
                        adminMonitorIntent.putExtra("DurationHours", String.valueOf(hourInt));
                        adminMonitorIntent.putExtra("FarmType", currentFarm.getFarmNotiId());
                        adminMonitorIntent.putExtra("FarmId", farmid);
                        startService(adminMonitorIntent);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


    }
}
