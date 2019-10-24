package com.blackviking.menorahfarms.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.FarmModel;
import com.blackviking.menorahfarms.Notification.APIService;
import com.blackviking.menorahfarms.Notification.DataMessage;
import com.blackviking.menorahfarms.Notification.MyResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Response;

public class SponsorshipMonitor extends Service {

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference sponsoredFarmRef, dueSponsorRef, notificationRef, userRef, runningCycleRef, farmRef;
    String userId, startDateString, endDate, todayString;
    APIService mService;


    private Date startDate, todayDate, endDateDate;
    private long totalSponsorshipDays, daysUsed;
    private long sponsorDayDiff, currentDayDiff;
    private SimpleDateFormat sdfStartDate, sdfToday, sdfStopDayDay;

    public SponsorshipMonitor() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /*---   FIREBASE   ---*/
        sponsoredFarmRef = db.getReference("SponsoredFarms");
        dueSponsorRef = db.getReference("DueSponsorships");
        notificationRef = db.getReference("Notifications");
        userRef = db.getReference("Users");
        runningCycleRef = db.getReference("RunningCycles");
        farmRef = db.getReference("Farms");


        /*---   FCM   ---*/
        mService = Common.getFCMService();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startMonitor();

        return START_STICKY;
    }

    private void startMonitor() {

        /*---   USER   ---*/
        userId = Paper.book().read(Common.USER_ID);

        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

                //check all cases
                if (output == 1){

                    sponsoredFarmRef.child(userId)
                            .orderByChild("status")
                            .equalTo("sponsoring")
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.exists()) {

                                        Paper.book().write(Common.isSponsorshipMonitorRunning, true);

                                        for (DataSnapshot snap : dataSnapshot.getChildren()) {

                                            endDate = snap.child("cycleEndDate").getValue().toString();
                                            startDateString = snap.child("cycleStartDate").getValue().toString();
                                            String farmId = snap.child("farmId").getValue().toString();

                                            calculateDays(endDate, startDateString, snap.getKey(), farmId);

                                        }

                                        repeatTomorrow();

                                    } else {

                                        Paper.book().write(Common.isSponsorshipMonitorRunning, false);
                                        stopSelf();

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                } else

                if (output == 0){

                    retryNetwork();

                } else

                if (output == 2){

                    retryNetwork();

                }

            }
        }).execute();

    }

    private void calculateDays(String endDate, String startDateString, String key, String farmId) {

        todayDate = Calendar.getInstance().getTime();
        sdfToday = new SimpleDateFormat("dd-MM-yyyy");
        todayString = sdfToday.format(todayDate);

        /*---   GET START DATE   ---*/
        sdfStartDate = new SimpleDateFormat("dd-MM-yyyy");
        try {
            startDate = sdfStartDate.parse(startDateString);

            /*---   GET STOP DATE   ---*/
            sdfStopDayDay = new SimpleDateFormat("dd-MM-yyyy");
            try {
                endDateDate = sdfStopDayDay.parse(endDate);

                /*---   CALCULATE   ---*/
                sponsorDayDiff = endDateDate.getTime() - startDate.getTime();
                totalSponsorshipDays = TimeUnit.DAYS.convert(sponsorDayDiff, TimeUnit.MILLISECONDS);


                currentDayDiff = todayDate.getTime() - startDate.getTime();
                daysUsed = TimeUnit.DAYS.convert(currentDayDiff, TimeUnit.MILLISECONDS);

                if (daysUsed >= totalSponsorshipDays) {

                    endSponsorship(key, farmId);

                }

            } catch (Exception e){

            }

        } catch (Exception e){

        }

    }

    private void endSponsorship(String key, String farmId) {


        //remove from running cycle
        farmRef.child(farmId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        FarmModel theFarm = dataSnapshot.getValue(FarmModel.class);

                        if (theFarm != null){

                            runningCycleRef.child(theFarm.getFarmNotiId())
                                    .child(userId)
                                    .removeValue();

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        Map<String, Object> dueSponsorshipMap = new HashMap<>();
        dueSponsorshipMap.put("user", userId);
        dueSponsorshipMap.put("sponsorshipId", key);
        dueSponsorshipMap.put("timeDue", ServerValue.TIMESTAMP);


        //switch sponsorship to processing
        sponsoredFarmRef.child(userId)
                .child(key)
                .child("status")
                .setValue("processing");


        //add sponsorship to due
        dueSponsorRef.push()
                .setValue(dueSponsorshipMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sendNotificationToAdmin();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });


    }

    private void sendNotificationToAdmin() {

        final Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy  hh:mm");
        String todayString = formatter.format(todayDate);

        final Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("topic", "Sponsorship End");
        notificationMap.put("message", "Congratulations! You have reached the end of a sponsorship cycle. Your full return payment will be made into your bank account at the end of the month as stated in the terms and conditions.");
        notificationMap.put("time", todayString);

        notificationRef.child(userId)
                .push()
                .setValue(notificationMap)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Map<String, String> dataSend = new HashMap<>();
                                dataSend.put("title", "Sponsorship End");
                                dataSend.put("message", "Congratulations! You have reached the end of a sponsorship cycle. Your full return payment will be made into your bank account at the end of the month as stated in the terms and condition.");
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

                                sendNotificationToRealAdmin();

                            }
                        }
                ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void sendNotificationToRealAdmin() {

        userRef.orderByChild("userType")
                .equalTo("Admin")
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                for (DataSnapshot snap : dataSnapshot.getChildren()){

                                    String theUserIds = snap.getKey();

                                    Map<String, String> dataSend = new HashMap<>();
                                    dataSend.put("title", "Admin");
                                    dataSend.put("message", "A Cycle Just Ended. Tend to customer");
                                    DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(theUserIds).toString(), dataSend);

                                    mService.sendNotification(dataMessage)
                                            .enqueue(new retrofit2.Callback<MyResponse>() {
                                                @Override
                                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                                }

                                                @Override
                                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                                }
                                            });

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

    }

    private void repeatTomorrow() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startMonitor();
            }
        }, 21600000);

    }

    private void retryNetwork() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startMonitor();
            }
        }, 1800000);

    }

    //24 hours = 86400000
    //6 hours = 21600000
    //2 hours = 7200000
    //30 minutes = 1800000

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Paper.book().write(Common.isSponsorshipMonitorRunning, false);
    }
}
