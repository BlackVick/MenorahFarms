package com.blackviking.menorahfarms.AdminFragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.BankModel;
import com.blackviking.menorahfarms.Notification.APIService;
import com.blackviking.menorahfarms.Notification.DataMessage;
import com.blackviking.menorahfarms.Notification.MyResponse;
import com.blackviking.menorahfarms.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdminNotify extends Fragment {

    private EditText notificationTopic, notificationMessage, userEmail;
    private Button sendBroadcastBtn;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference notificationRef, userRef, farmRef;
    private APIService mService;

    private Spinner topicSpinner;
    private RadioGroup announcementStyle;
    private String selectedTopic = "";


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
        farmRef = db.getReference("Farms");


        /*---   FCM   ---*/
        mService = Common.getFCMService();


        /*---   WIDGET   ---*/
        notificationTopic = (EditText)v.findViewById(R.id.notificationTopic);
        notificationMessage = (EditText)v.findViewById(R.id.notificationMessage);
        userEmail = (EditText)v.findViewById(R.id.userEmail);
        sendBroadcastBtn = (Button)v.findViewById(R.id.sendNotificationBtn);
        topicSpinner = (Spinner)v.findViewById(R.id.topicSpinner);
        announcementStyle = (RadioGroup)v.findViewById(R.id.announcementStyle);


        /*---   BANK SPINNER   ---*/
        final List<String> bankList = new ArrayList<>();
        bankList.add(0, "Topic");

        farmRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot child : dataSnapshot.getChildren()){

                    bankList.add(child.getKey());

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ArrayAdapter<String> dataAdapterGender;
        dataAdapterGender = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, bankList);
        dataAdapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        topicSpinner.setAdapter(dataAdapterGender);
        topicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).equals("Topic")){

                    selectedTopic = parent.getItemAtPosition(position).toString();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        announcementStyle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId == R.id.single){

                    topicSpinner.setVisibility(View.GONE);
                    userEmail.setVisibility(View.VISIBLE);
                    sendBroadcastBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            checkSingleStatus();
                        }
                    });

                } else if (checkedId == R.id.all){

                    topicSpinner.setVisibility(View.VISIBLE);
                    userEmail.setVisibility(View.GONE);
                    sendBroadcastBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            checkTopicStatus();
                        }
                    });

                }

            }
        });

        sendBroadcastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStatus();
            }
        });
        
        return v;
    }

    private void checkTopicStatus() {

        final String theTopic = notificationTopic.getText().toString().trim();
        final String theMessage = notificationMessage.getText().toString().trim();

        if (Common.isConnectedToInternet(getContext())) {

            if (TextUtils.isEmpty(theTopic)) {

                notificationTopic.requestFocus();
                notificationTopic.setError("Topic Can Not Be Empty !");

            } else if (TextUtils.isEmpty(theMessage)) {

                notificationMessage.requestFocus();
                notificationMessage.setError("Message Can Not Be Empty !");

            } else if (TextUtils.isEmpty(selectedTopic)) {

                Common.showErrorDialog(getContext(), "Select A Topic", getActivity());

            } else {

                userRef.addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        for (DataSnapshot snap : dataSnapshot.getChildren()){

                                            String theKey = snap.getKey();
                                            sendTopicBroadcast(theTopic, theMessage, theKey);

                                        }

                                        Map<String, String> dataSend = new HashMap<>();
                                        dataSend.put("title", "Menorah Farms");
                                        dataSend.put("message", theMessage);
                                        DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(selectedTopic).toString(), dataSend);

                                        mService.sendNotification(dataMessage)
                                                .enqueue(new retrofit2.Callback<MyResponse>() {
                                                    @Override
                                                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                                    }

                                                    @Override
                                                    public void onFailure(Call<MyResponse> call, Throwable t) {
                                                    }
                                                });

                                        Toast.makeText(getContext(), "Done", Toast.LENGTH_SHORT).show();
                                        notificationTopic.setText("");
                                        notificationMessage.setText("");

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                }
                        );

            }

        } else {

            Common.showErrorDialog(getContext(), "No Internet Access !", getActivity());

        }

    }

    private void sendTopicBroadcast(String theTopic, String theMessage, String theKey) {

        final Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy  hh:mm");
        String todayString = formatter.format(todayDate);

        final Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("topic", theTopic);
        notificationMap.put("message", theMessage);
        notificationMap.put("time", todayString);

        notificationRef.child(theKey)
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

    private void checkSingleStatus() {

        final String theEmail = userEmail.getText().toString().trim().toLowerCase();
        final String theTopic = notificationTopic.getText().toString().trim();
        final String theMessage = notificationMessage.getText().toString().trim();

        if (Common.isConnectedToInternet(getContext())) {

            if (TextUtils.isEmpty(theTopic)) {

                notificationTopic.requestFocus();
                notificationTopic.setError("Topic Can Not Be Empty !");

            } else if (TextUtils.isEmpty(theMessage)) {

                notificationMessage.requestFocus();
                notificationMessage.setError("Message Can Not Be Empty !");

            } else if (TextUtils.isEmpty(theEmail)) {

                userEmail.requestFocus();
                userEmail.setError("Who ?");

            } else {

                userRef.orderByChild("email")
                        .equalTo(theEmail)
                        .addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists()) {

                                            for (DataSnapshot snap : dataSnapshot.getChildren()) {

                                                String userKey = snap.getKey();
                                                sendSingleBroadcast(theTopic, theMessage, theEmail, userKey);
                                            }

                                        } else {

                                            Toast.makeText(getContext(), "No such user", Toast.LENGTH_SHORT).show();

                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                }
                        );

            }

        } else {

            Common.showErrorDialog(getContext(), "No Internet Access !", getActivity());

        }

    }

    private void sendSingleBroadcast(String theTopic, final String theMessage, String theEmail, final String userKey) {

        final Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy  hh:mm");
        String todayString = formatter.format(todayDate);

        final Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("topic", theTopic);
        notificationMap.put("message", theMessage);
        notificationMap.put("time", todayString);

        notificationRef.child(userKey)
                .push()
                .setValue(notificationMap)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Map<String, String> dataSend = new HashMap<>();
                                dataSend.put("title", "Menorah Farms");
                                dataSend.put("message", theMessage);
                                DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(userKey).toString(), dataSend);

                                mService.sendNotification(dataMessage)
                                        .enqueue(new retrofit2.Callback<MyResponse>() {
                                            @Override
                                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                            }

                                            @Override
                                            public void onFailure(Call<MyResponse> call, Throwable t) {
                                            }
                                        });

                                notificationTopic.setText("");
                                notificationMessage.setText("");
                                userEmail.setText("");
                                Toast.makeText(getContext(), "Done", Toast.LENGTH_SHORT).show();

                            }
                        }
                ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void checkStatus() {

        String theTopic = notificationTopic.getText().toString().trim();
        String theMessage = notificationMessage.getText().toString().trim();

        if (Common.isConnectedToInternet(getContext())) {

            if (TextUtils.isEmpty(theTopic)){

                notificationTopic.requestFocus();
                notificationTopic.setError("Topic Can Not Be Empty !");

            } else if (TextUtils.isEmpty(theMessage)){

                notificationMessage.requestFocus();
                notificationMessage.setError("Message Can Not Be Empty !");

            } else {

                sendBroadcast(theTopic, theMessage);

            }

        } else {

            Common.showErrorDialog(getContext(), "No Internet Access !", getActivity());

        }

    }

    private void sendBroadcast(String theTopic, final String theMessage) {

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

                        DatabaseReference pushRef = notificationRef.push();
                        String pushId = pushRef.getKey();

                        for (DataSnapshot snap : dataSnapshot.getChildren()){

                            final String theUserIds = snap.getKey();

                            notificationRef.child(theUserIds)
                                    .child(pushId)
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

                        Map<String, String> dataSend = new HashMap<>();
                        dataSend.put("title", "Menorah Farms");
                        dataSend.put("message", theMessage);
                        DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(Common.GENERAL_NOTIFY).toString(), dataSend);

                        mService.sendNotification(dataMessage)
                                .enqueue(new retrofit2.Callback<MyResponse>() {
                                    @Override
                                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                    }

                                    @Override
                                    public void onFailure(Call<MyResponse> call, Throwable t) {
                                    }
                                });

                        Toast.makeText(getContext(), "Done", Toast.LENGTH_SHORT).show();
                        notificationTopic.setText("");
                        notificationMessage.setText("");

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

    }

}
