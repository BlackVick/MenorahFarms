package com.blackviking.menorahfarms.AdminDetails;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.PendingSponsorshipModel;
import com.blackviking.menorahfarms.Notification.APIService;
import com.blackviking.menorahfarms.Notification.DataMessage;
import com.blackviking.menorahfarms.Notification.MyResponse;
import com.blackviking.menorahfarms.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class PendingSponsorshipDetail extends AppCompatActivity {

    //widgets
    private ImageView proofImage, backButton, openInGallery;
    private RelativeLayout denySponsorship, approveSponsorship;

    //firebase
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, pendingSponsorshipRef, sponsorshipRef, notificationRef,
                                runningCycleRef, sponsorshipNotificationRef;

    //sponsorship
    private String currentSponsorId, currentSponsorshipGroup;
    private PendingSponsorshipModel currentSponsorship;

    //notification
    private APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_sponsorship_detail);


        //intent data
        currentSponsorId = getIntent().getStringExtra("PendingSponsorshipId");
        currentSponsorshipGroup = getIntent().getStringExtra("SponsorshipGroup");


        //firebase
        userRef = db.getReference("Users");
        sponsorshipRef = db.getReference("SponsoredFarms");
        notificationRef = db.getReference("Notifications");
        runningCycleRef = db.getReference("RunningCycles");
        sponsorshipNotificationRef = db.getReference("SponsoredFarmsNotification");
        pendingSponsorshipRef = db.getReference("PendingSponsorships");


        //FCM
        mService = Common.getFCMService();


        //widgets
        proofImage = findViewById(R.id.proofImage);
        backButton = findViewById(R.id.backButton);
        openInGallery = findViewById(R.id.openInGallery);
        denySponsorship = findViewById(R.id.denySponsorship);
        approveSponsorship = findViewById(R.id.approveSponsorship);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

                //check all cases
                if (output == 1){

                    loadContent(currentSponsorId, currentSponsorshipGroup);

                } else

                if (output == 0){

                    //set layout
                    Toast.makeText(PendingSponsorshipDetail.this, "No internet access", Toast.LENGTH_SHORT).show();

                } else

                if (output == 2){

                    //set layout
                    Toast.makeText(PendingSponsorshipDetail.this, "No network detected", Toast.LENGTH_SHORT).show();

                }

            }
        }).execute();

    }

    private void loadContent(final String currentSponsorId, final String currentSponsorshipGroup) {

        pendingSponsorshipRef
                .child(currentSponsorshipGroup)
                .child(currentSponsorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        currentSponsorship = dataSnapshot.getValue(PendingSponsorshipModel.class);

                        if (currentSponsorship != null){

                            if (!currentSponsorship.getPaymentProof().equalsIgnoreCase("")){

                                Picasso.get()
                                        .load(currentSponsorship.getPaymentProof())
                                        .into(proofImage);

                            }


                            //open in gallery
                            openInGallery.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Uri uri =  Uri.parse(currentSponsorship.getPaymentProof());

                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setDataAndType(uri, "image/*");
                                    startActivity(intent);
                                }
                            });


                            //deny sponsorship
                            denySponsorship.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    //execute network check async task
                                    CheckInternet asyncTask = (CheckInternet) new CheckInternet(PendingSponsorshipDetail.this, new CheckInternet.AsyncResponse(){
                                        @Override
                                        public void processFinish(Integer output) {

                                            //check all cases
                                            if (output == 1){

                                                denyTheSponsorship(currentSponsorId, currentSponsorshipGroup, currentSponsorship);

                                            } else

                                            if (output == 0){

                                                //set layout
                                                Toast.makeText(PendingSponsorshipDetail.this, "No internet access", Toast.LENGTH_SHORT).show();

                                            } else

                                            if (output == 2){

                                                //set layout
                                                Toast.makeText(PendingSponsorshipDetail.this, "No network detected", Toast.LENGTH_SHORT).show();

                                            }

                                        }
                                    }).execute();

                                }
                            });


                            //approve sponsorship
                            approveSponsorship.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    //execute network check async task
                                    CheckInternet asyncTask = (CheckInternet) new CheckInternet(PendingSponsorshipDetail.this, new CheckInternet.AsyncResponse(){
                                        @Override
                                        public void processFinish(Integer output) {

                                            //check all cases
                                            if (output == 1){

                                                approveTheSponsorship(currentSponsorId, currentSponsorshipGroup, currentSponsorship);

                                            } else

                                            if (output == 0){

                                                //set layout
                                                Toast.makeText(PendingSponsorshipDetail.this, "No internet access", Toast.LENGTH_SHORT).show();

                                            } else

                                            if (output == 2){

                                                //set layout
                                                Toast.makeText(PendingSponsorshipDetail.this, "No network detected", Toast.LENGTH_SHORT).show();

                                            }

                                        }
                                    }).execute();

                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void approveTheSponsorship(final String currentSponsorId, final String currentSponsorshipGroup, final PendingSponsorshipModel currentSponsorship) {

        //approve sponsorship
        sponsorshipRef.child(currentSponsorship.getUserId())
                .child(currentSponsorId)
                .child("status")
                .setValue("sponsoring")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            sponsorshipNotificationRef.child(currentSponsorshipGroup)
                                    .child(currentSponsorship.getUserId())
                                    .child("timestamp")
                                    .setValue(ServerValue.TIMESTAMP);

                            addToRunningCycle(currentSponsorId, currentSponsorshipGroup, currentSponsorship);

                        } else {

                            Toast.makeText(PendingSponsorshipDetail.this, "Error Occurred", Toast.LENGTH_SHORT).show();

                        }

                    }
                });

    }

    private void addToRunningCycle(final String currentSponsorId, final String currentSponsorshipGroup, final PendingSponsorshipModel currentSponsorship) {

        //admin running cycle map
        Map<String, Object> adminSponsorshipMap = new HashMap<>();
        adminSponsorshipMap.put("sponsorReturn", currentSponsorship.getSponsorReturn());
        adminSponsorshipMap.put("cycleEndDate", currentSponsorship.getCycleEndDate());
        adminSponsorshipMap.put("cycleStartDate", currentSponsorship.getCycleStartDate());
        adminSponsorshipMap.put("sponsorRefNumber", currentSponsorship.getSponsorRefNumber());
        adminSponsorshipMap.put("unitPrice", currentSponsorship.getUnitPrice());
        adminSponsorshipMap.put("sponsoredUnits", currentSponsorship.getSponsoredUnits());
        adminSponsorshipMap.put("sponsoredFarmType", currentSponsorship.getSponsoredFarmType());
        adminSponsorshipMap.put("sponsoredFarmRoi", currentSponsorship.getSponsoredFarmRoi());
        adminSponsorshipMap.put("sponsorshipDuration", currentSponsorship.getSponsorshipDuration());
        adminSponsorshipMap.put("startPoint", ServerValue.TIMESTAMP);
        adminSponsorshipMap.put("totalAmountPaid", currentSponsorship.getTotalAmountPaid());
        adminSponsorshipMap.put("farmId", currentSponsorship.getFarmId());
        adminSponsorshipMap.put("userId", currentSponsorship.getUserId());

        runningCycleRef.child(currentSponsorshipGroup)
                .child(currentSponsorId)
                .setValue(adminSponsorshipMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            sendApprovalNotification(currentSponsorship.getUserId(), currentSponsorId, currentSponsorshipGroup);

                        }

                    }
                });

    }

    private void sendApprovalNotification(final String userId, final String currentSponsorId, final String currentSponsorshipGroup) {

        final Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy  hh:mm");
        String todayString = formatter.format(todayDate);

        //notification map
        final Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("topic", "Sponsorship Approved");
        notificationMap.put("message", "Your sponsorship has been approved. You can monitor your sponsored farm from the Sponsored Farms page through your dashboard.");
        notificationMap.put("time", todayString);


        notificationRef.child(userId)
                .push()
                .setValue(notificationMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            Map<String, String> dataSend = new HashMap<>();
                            dataSend.put("title", "Sponsorship Start");
                            dataSend.put("message", "Your sponsorship has been approved. You can monitor your sponsored farm from the Sponsored Farms page through your dashboard.");
                            DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(userId).toString(), dataSend);

                            mService.sendNotification(dataMessage)
                                    .enqueue(new retrofit2.Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {
                                        }
                                    });

                            //remove from pending ref
                            pendingSponsorshipRef
                                    .child(currentSponsorshipGroup)
                                    .child(currentSponsorId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                Toast.makeText(PendingSponsorshipDetail.this, "Approval successful", Toast.LENGTH_SHORT).show();
                                                finish();

                                            }

                                        }
                                    });

                        } else {

                            Toast.makeText(PendingSponsorshipDetail.this, "Error occurred while sending notification", Toast.LENGTH_SHORT).show();

                        }

                    }
                });

    }

    private void denyTheSponsorship(final String currentSponsorId, final String currentSponsorshipGroup, final PendingSponsorshipModel currentSponsorship) {

        //remove sponsorship ref
        sponsorshipRef.child(currentSponsorship.getUserId())
                .child(currentSponsorId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            sendDenialNotification(currentSponsorship.getUserId(), currentSponsorId, currentSponsorshipGroup);

                        } else {

                            Toast.makeText(PendingSponsorshipDetail.this, "Error Occurred", Toast.LENGTH_SHORT).show();

                        }

                    }
                });

    }

    private void sendDenialNotification(final String userId, final String currentSponsorId, final String currentSponsorshipGroup) {

        final Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy  hh:mm");
        String todayString = formatter.format(todayDate);

        //notification map
        final Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("topic", "Sponsorship Denied");
        notificationMap.put("message", "Your sponsorship was denied due to invalid proof of payment. If this is wrong, please contact admin.");
        notificationMap.put("time", todayString);

        notificationRef.child(userId)
                .push()
                .setValue(notificationMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            Map<String, String> dataSend = new HashMap<>();
                            dataSend.put("title", "Sponsorship");
                            dataSend.put("message", "Your sponsorship was denied due to invalid proof of payment. If this is wrong, please contact admin.");
                            DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(userId).toString(), dataSend);

                            mService.sendNotification(dataMessage)
                                    .enqueue(new retrofit2.Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {
                                        }
                                    });

                            //remove from pending ref
                            pendingSponsorshipRef
                                    .child(currentSponsorshipGroup)
                                    .child(currentSponsorId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                Toast.makeText(PendingSponsorshipDetail.this, "Denial successful", Toast.LENGTH_SHORT).show();
                                                finish();

                                            }

                                        }
                                    });

                        } else {

                            Toast.makeText(PendingSponsorshipDetail.this, "Error occurred while sending notification", Toast.LENGTH_SHORT).show();

                        }

                    }
                });

    }

}
