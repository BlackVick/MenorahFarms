package com.blackviking.menorahfarms.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.blackviking.menorahfarms.AdminDetails.FarmManagementDetail;
import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.paperdb.Paper;

import static com.blackviking.menorahfarms.Common.ApplicationClass.CHANNEL_2_ID;

public class AdminMonitorService extends Service {

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference farmRef;

    /*---   TIMED TEST   ---*/
    private CountDownTimer mCountDownTimer;
    private long mTimeLeftInMillis;
    private long mEndTime;
    private long totalTimeInSeconds;

    private int remainingHours;

    private String farmType, farmId;

    public AdminMonitorService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Firebase
        farmRef = db.getReference("Farms");

        //intent data
        farmType = intent.getStringExtra("FarmType");
        farmId = intent.getStringExtra("FarmId");
        String activeDurationString = intent.getStringExtra("DurationHours");

        //paper
        Paper.book().write(Common.isFarmServiceRunning, true);

        //hours in integer
        int activeDuration = Integer.parseInt(activeDurationString);

        //total number of seconds
        totalTimeInSeconds = activeDuration * 60 * 60;

        //duration in millis
        mTimeLeftInMillis = TimeUnit.SECONDS.toMillis(totalTimeInSeconds);


        //timer
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;

        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 60000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;

                //update
                updateCountDown(farmType, mTimeLeftInMillis);
            }

            @Override
            public void onFinish() {

                mCountDownTimer.cancel();
                endFarmActivation(farmType);

            }
        }.start();


        //notification
        Intent farmDetailIntent = new Intent(this, FarmManagementDetail.class);
        farmDetailIntent.putExtra("FarmId", farmId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, farmDetailIntent, 0);


        Notification notification = new NotificationCompat.Builder(this, CHANNEL_2_ID)
                .setContentTitle("Farm Management")
                .setContentText(farmType + " is currently active for sponsorship")
                .setSmallIcon(R.drawable.ic_admin_notification)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(9, notification);

        return START_NOT_STICKY;
    }

    private void endFarmActivation(final String farmType) {

        //cancel timer
        mCountDownTimer.cancel();

        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

                //check all cases
                if (output == 1){

                    farmRef.orderByChild("farmNotiId")
                            .equalTo(farmType)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    for (DataSnapshot snap : dataSnapshot.getChildren()){

                                        //farm id
                                        String farmId = snap.getKey();

                                        //deactivate map
                                        final Map<String, Object> deactivateFarmMap = new HashMap<>();
                                        deactivateFarmMap.put("farmState", "Sold Out");
                                        deactivateFarmMap.put("unitsAvailable", "");

                                        farmRef.child(farmId)
                                                .updateChildren(deactivateFarmMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()){



                                                        }

                                                    }
                                                });

                                    }

                                    Paper.book().write(Common.isFarmServiceRunning, false);
                                    stopSelf();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                } else

                if (output == 0){

                    retryFinish(farmType);

                } else

                if (output == 2){

                    retryFinish(farmType);

                }

            }
        }).execute();

    }

    private void retryFinish(final String farmType) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                endFarmActivation(farmType);
            }
        }, 300000);

    }

    private void updateCountDown(final String farmType, final long mTimeLeftInMillis) {

        //convert to seconds
        long millisToSecs = mTimeLeftInMillis / 1000;

        //remaining time
        final int hoursLeft = (int) (millisToSecs / 60) / 60;
        final int minutes = (int) (millisToSecs / 60) % 60;

        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

                //check all cases
                if (output == 1){

                    farmRef.orderByChild("farmNotiId")
                            .equalTo(farmType)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    for (DataSnapshot snap : dataSnapshot.getChildren()){

                                        String farmId = snap.getKey();

                                        farmRef.child(farmId)
                                                .child("unitsAvailable")
                                                .setValue(String.valueOf(hoursLeft) + " hours, " + String.valueOf(minutes) + " minutes left !")
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()){



                                                        }

                                                    }
                                                });

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                } else

                if (output == 0){

                    retry(farmType);

                } else

                if (output == 2){

                    retry(farmType);

                }

            }
        }).execute();

    }

    /*private void repeatUpdate(final String farmType) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateCountDown(farmType, mTimeLeftInMillis);
            }
        }, 60000);

    }*/

    private void retry(final String farmType) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateCountDown(farmType, mTimeLeftInMillis);
            }
        }, 600000);

    }

    //24 hours = 86400000
    //6 hours = 21600000
    //2 hours = 7200000
    //30 minutes = 1800000
    //10 minutes = 600000
    //20 minutes = 1200000
    //5 minutes = 300000
    //1 minute = 60000

    @Override
    public void onDestroy() {
        super.onDestroy();
        Paper.book().write(Common.isFarmServiceRunning, false);
        mCountDownTimer.cancel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
