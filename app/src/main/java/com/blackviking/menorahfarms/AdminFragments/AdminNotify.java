package com.blackviking.menorahfarms.AdminFragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.blackviking.menorahfarms.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdminNotify extends Fragment {

    private EditText notificationTopic, notificationMessage;
    private Button sendBroadcastBtn;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference notificationRef, userRef;


    public AdminNotify() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_notify, container, false);

        
        /*---   FIREBASE   ---*/
        notificationRef = db.getReference("Notifications");
        userRef = db.getReference("Users");


        /*---   WIDGET   ---*/
        notificationTopic = (EditText)v.findViewById(R.id.notificationTopic);
        notificationMessage = (EditText)v.findViewById(R.id.notificationMessage);
        sendBroadcastBtn = (Button)v.findViewById(R.id.sendNotificationBtn);
        

        sendBroadcastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStatus();
            }
        });
        
        return v;
    }

    private void checkStatus() {

        String theTopic = notificationTopic.getText().toString().trim();
        String theMessage = notificationMessage.getText().toString().trim();

        if (TextUtils.isEmpty(theTopic)){

            notificationTopic.requestFocus();
            notificationTopic.setError("Topic Can Not Be Empty !");

        } else if (TextUtils.isEmpty(theMessage)){

            notificationMessage.requestFocus();
            notificationMessage.setError("Message Can Not Be Empty !");

        } else {

            sendBroadcast(theTopic, theMessage);

        }
    }

    private void sendBroadcast(String theTopic, String theMessage) {

        final Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy  hh:mm");
        String todayString = formatter.format(todayDate);

        final Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("topic", theTopic);
        notificationMap.put("message", theMessage);
        notificationMap.put("time", todayString);

        userRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snap : dataSnapshot.getChildren()){

                            String theUserIds = snap.getKey();

                            notificationRef.child(theUserIds)
                                    .push()
                                    .setValue(notificationMap)
                                    .addOnSuccessListener(
                                    new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                        }
                                    }
                            ).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });

                        }

                        sendNotification();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

    }

    private void sendNotification() {
    }

}
