package com.blackviking.menorahfarms.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.Common;
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

import io.paperdb.Paper;

public class SponsorshipMonitor extends Service {

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference sponsoredFarmRef, dueSponsorRef;
    String userId, endDate, todayString;

    public SponsorshipMonitor() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /*---   FIREBASE   ---*/
        sponsoredFarmRef = db.getReference("SponsoredFarms");
        dueSponsorRef = db.getReference("DueSponsorships");


        /*---   USER   ---*/
        userId = Paper.book().read(Common.USER_ID);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startMonitor();

        return START_STICKY;
    }

    private void startMonitor() {

        if (Common.isConnectedToInternet(getApplicationContext())){

            final Date todayDate = Calendar.getInstance().getTime();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            todayString = formatter.format(todayDate);

            sponsoredFarmRef.child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {

                                for (DataSnapshot snap : dataSnapshot.getChildren()) {

                                    endDate = snap.child("cycleEndDate").getValue().toString();

                                    if (todayString.equalsIgnoreCase(endDate)) {

                                        endSponsorship(snap.getKey());

                                    }

                                }

                            } else {

                                stopSelf();

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            repeatTomorrow();

        } else {

            retryNetwork();

        }

    }

    private void endSponsorship(String key) {



        Map<String, Object> dueSponsorshipMap = new HashMap<>();
        dueSponsorshipMap.put("user", userId);
        dueSponsorshipMap.put("sponsorshipId", key);
        dueSponsorshipMap.put("timeDue", ServerValue.TIMESTAMP);

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
                Toast.makeText(SponsorshipMonitor.this, "Something Happened", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void sendNotificationToAdmin() {
    }

    private void repeatTomorrow() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startMonitor();
            }
        }, 86400000);

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
}
