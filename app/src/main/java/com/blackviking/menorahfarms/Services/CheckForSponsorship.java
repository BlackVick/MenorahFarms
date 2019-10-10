package com.blackviking.menorahfarms.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.FarmDetails;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class CheckForSponsorship extends Service {

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference sponsoredFarmRef;
    private String sponsorshipStatus, userId;

    public CheckForSponsorship() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /*---   FIREBASE   ---*/
        sponsoredFarmRef = db.getReference("SponsoredFarms");


        /*---   LOCAL   ---*/
        userId = Paper.book().read(Common.USER_ID);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        startCheck();

        return START_STICKY;

    }

    private void startCheck() {

        /*---   LOAD   ---*/
        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

                //check all cases
                if (output == 1){

                    /*---   SUBSCRIPTION LOT   ---*/
                    sponsoredFarmRef.child(userId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.exists()){

                                        Intent intent = new Intent(getApplicationContext(), SponsorshipMonitor.class);
                                        startService(intent);

                                        stopSelf();

                                    } else {

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

    private void retryNetwork() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startCheck();
            }
        }, 1800000);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
