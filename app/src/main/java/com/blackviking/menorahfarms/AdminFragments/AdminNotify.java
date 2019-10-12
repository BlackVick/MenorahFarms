package com.blackviking.menorahfarms.AdminFragments;


import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.BankModel;
import com.blackviking.menorahfarms.Models.FarmModel;
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

    private EditText followedTopic, followedMessage, sponsoredTopic, sponsoredMessage;
    private Button sendFollowedNotiBtn, sendSponsoredNotiBtn;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference notificationRef, userRef, farmRef, followedFarmNotiRef, sponsoredFarmNotiRef;
    private APIService mService;

    private Spinner followedSpinner, sponsoredSpinner;
    private RadioGroup notificationStyle;
    private String selectedFollowedGroup = "", selectedSponsoredGroup = "";

    private FloatingActionButton broadcastToAllFab;
    private RelativeLayout directionLayout, followedFarmLayout, sponsoredFarmLayout;

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
        followedFarmNotiRef = db.getReference("FollowedFarmsNotification");
        sponsoredFarmNotiRef = db.getReference("SponsoredFarmsNotification");


        /*---   FCM   ---*/
        mService = Common.getFCMService();


        /*---   WIDGET   ---*/
        followedTopic = v.findViewById(R.id.followedTopic);
        followedMessage = v.findViewById(R.id.followedMessage);
        sponsoredTopic = v.findViewById(R.id.sponsoredTopic);
        sponsoredMessage = v.findViewById(R.id.sponsoredMessage);
        sendFollowedNotiBtn = v.findViewById(R.id.sendFollowedNotiBtn);
        sendSponsoredNotiBtn = v.findViewById(R.id.sendSponsoredNotiBtn);
        followedSpinner = v.findViewById(R.id.followedSpinner);
        sponsoredSpinner = v.findViewById(R.id.sponsoredSpinner);
        notificationStyle = v.findViewById(R.id.notificationStyle);
        broadcastToAllFab = v.findViewById(R.id.broadcastToAllFab);
        directionLayout = v.findViewById(R.id.directionLayout);
        followedFarmLayout = v.findViewById(R.id.followedFarmLayout);
        sponsoredFarmLayout = v.findViewById(R.id.sponsoredFarmLayout);


        //notification style
        notificationStyle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){

                    case R.id.followedFarmsNoti:
                        loadFollowedFarms();
                        break;

                    case R.id.sponsoredFarmsNoti:
                        loadSponsoredFarms();
                        break;

                }
            }
        });


        broadcastToAllFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBroadcastDialog();
            }
        });
        
        return v;
    }

    //FOLLOWED FARMS
    private void loadFollowedFarms() {

        //set layout
        directionLayout.setVisibility(View.GONE);
        followedFarmLayout.setVisibility(View.VISIBLE);
        sponsoredFarmLayout.setVisibility(View.GONE);


        //set followed farm list
        final List<String> followedFarmList = new ArrayList<>();
        followedFarmList.add(0, "Followed Farm");

        final ArrayAdapter<String> dataAdapterFollowedFarm;
        dataAdapterFollowedFarm = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, followedFarmList);
        dataAdapterFollowedFarm.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(getContext(), new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

                //check all cases
                if (output == 1){

                    followedFarmNotiRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot child : dataSnapshot.getChildren()){

                                followedFarmList.add(child.getKey());

                            }

                            followedSpinner.setAdapter(dataAdapterFollowedFarm);
                            dataAdapterFollowedFarm.notifyDataSetChanged();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

            }
        }).execute();

        followedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).equals("Followed Farm")){

                    selectedFollowedGroup = parent.getItemAtPosition(position).toString();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //validate
        sendFollowedNotiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkFollowedFarmsNotiParam();
            }
        });

    }

    private void checkFollowedFarmsNotiParam() {

        final String theTopic = followedTopic.getText().toString().trim();
        final String theMessage = followedMessage.getText().toString().trim();


        //validate fields
        if (TextUtils.isEmpty(theTopic)) {

            followedTopic.requestFocus();
            followedTopic.setError("Field required !");

        } else if (TextUtils.isEmpty(theMessage)) {

            followedMessage.requestFocus();
            followedMessage.setError("Field required !");

        } else if (TextUtils.isEmpty(selectedFollowedGroup)) {

            showErrorDialog("Select A Farm");

        } else {

            //execute network check async task
            CheckInternet asyncTask = (CheckInternet) new CheckInternet(getContext(), new CheckInternet.AsyncResponse(){
                @Override
                public void processFinish(Integer output) {

                    //check all cases
                    if (output == 1){

                        followedFarmNotiRef
                                .child(selectedFollowedGroup)
                                .addListenerForSingleValueEvent(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                for (DataSnapshot snap : dataSnapshot.getChildren()){

                                                    String theKey = snap.getKey();
                                                    sendFollowedFarmNoti(theTopic, theMessage, theKey);

                                                }

                                                Toast.makeText(getContext(), "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                                                followedTopic.setText("");
                                                followedMessage.setText("");

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        }
                                );

                    } else

                    if (output == 0){

                        //set layout
                        showErrorDialog("Failed due to lack of internet access");

                    } else

                    if (output == 2){

                        //set layout
                        showErrorDialog("Failed due to lack of network connection");

                    }

                }
            }).execute();

        }

    }

    private void sendFollowedFarmNoti(String theTopic, final String theMessage, final String theKey) {

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
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            Map<String, String> dataSend = new HashMap<>();
                            dataSend.put("title", "Followed Farms");
                            dataSend.put("message", theMessage);
                            DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(theKey).toString(), dataSend);

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
                });

    }


    //SPONSORED FARMS
    private void loadSponsoredFarms() {

        directionLayout.setVisibility(View.GONE);
        followedFarmLayout.setVisibility(View.GONE);
        sponsoredFarmLayout.setVisibility(View.VISIBLE);

        //set followed farm list
        final List<String> sponsoredFarmList = new ArrayList<>();
        sponsoredFarmList.add(0, "Sponsored Farm");

        final ArrayAdapter<String> dataAdapterSponsoredFarm;
        dataAdapterSponsoredFarm = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, sponsoredFarmList);
        dataAdapterSponsoredFarm.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(getContext(), new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

                //check all cases
                if (output == 1){

                    sponsoredFarmNotiRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot child : dataSnapshot.getChildren()){

                                sponsoredFarmList.add(child.getKey());

                            }

                            sponsoredSpinner.setAdapter(dataAdapterSponsoredFarm);
                            dataAdapterSponsoredFarm.notifyDataSetChanged();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

            }
        }).execute();

        sponsoredSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).equals("Sponsored Farm")){

                    selectedSponsoredGroup = parent.getItemAtPosition(position).toString();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //validate
        sendSponsoredNotiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkSponsoredFarmsNotiParam();
            }
        });

    }

    private void checkSponsoredFarmsNotiParam() {

        final String theTopic = sponsoredTopic.getText().toString().trim();
        final String theMessage = sponsoredMessage.getText().toString().trim();


        //validate fields
        if (TextUtils.isEmpty(theTopic)) {

            sponsoredTopic.requestFocus();
            sponsoredTopic.setError("Field required !");

        } else if (TextUtils.isEmpty(theMessage)) {

            sponsoredMessage.requestFocus();
            sponsoredMessage.setError("Field required !");

        } else if (TextUtils.isEmpty(selectedSponsoredGroup)) {

            showErrorDialog("Select A Farm");

        } else {

            //execute network check async task
            CheckInternet asyncTask = (CheckInternet) new CheckInternet(getContext(), new CheckInternet.AsyncResponse(){
                @Override
                public void processFinish(Integer output) {

                    //check all cases
                    if (output == 1){

                        sponsoredFarmNotiRef
                                .child(selectedSponsoredGroup)
                                .addListenerForSingleValueEvent(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                for (DataSnapshot snap : dataSnapshot.getChildren()){

                                                    String theKey = snap.getKey();
                                                    sendSponsoredFarmNoti(theTopic, theMessage, theKey);

                                                }

                                                Toast.makeText(getContext(), "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                                                sponsoredTopic.setText("");
                                                sponsoredMessage.setText("");

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        }
                                );

                    } else

                    if (output == 0){

                        //set layout
                        showErrorDialog("Failed due to lack of internet access");

                    } else

                    if (output == 2){

                        //set layout
                        showErrorDialog("Failed due to lack of network connection");

                    }

                }
            }).execute();

        }

    }

    private void sendSponsoredFarmNoti(String theTopic, final String theMessage, final String theKey) {

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
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            Map<String, String> dataSend = new HashMap<>();
                            dataSend.put("title", "Sponsored Farms");
                            dataSend.put("message", theMessage);
                            DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(theKey).toString(), dataSend);

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
                });

    }


    //BROADCAST TO ALL
    private void openBroadcastDialog() {
        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.admin_broadcast_layout,null);

        final EditText broadcastTopic = viewOptions.findViewById(R.id.broadcastTopic);
        final EditText broadcastMessage = viewOptions.findViewById(R.id.broadcastMessage);
        final Button sendBroadcastBtn = viewOptions.findViewById(R.id.sendBroadcastBtn);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        sendBroadcastBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkBroadcastNotiParam(alertDialog, broadcastTopic, broadcastMessage);

            }
        });


        alertDialog.show();
    }

    private void checkBroadcastNotiParam(final AlertDialog alertDialog, EditText broadcastTopic, EditText broadcastMessage) {

        final String theTopic = broadcastTopic.getText().toString().trim();
        final String theMessage = broadcastMessage.getText().toString().trim();


        //validate fields
        if (TextUtils.isEmpty(theTopic)) {

            broadcastTopic.requestFocus();
            broadcastTopic.setError("Field required !");

        } else if (TextUtils.isEmpty(theMessage)) {

            broadcastMessage.requestFocus();
            broadcastMessage.setError("Field required !");

        } else {

            //execute network check async task
            CheckInternet asyncTask = (CheckInternet) new CheckInternet(getContext(), new CheckInternet.AsyncResponse(){
                @Override
                public void processFinish(Integer output) {

                    //check all cases
                    if (output == 1){

                        userRef.addListenerForSingleValueEvent(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                for (DataSnapshot snap : dataSnapshot.getChildren()){

                                                    String theKey = snap.getKey();
                                                    sendBroadcastNoti(theTopic, theMessage, theKey);

                                                }

                                                Toast.makeText(getContext(), "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                                                alertDialog.dismiss();

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        }
                                );

                    } else

                    if (output == 0){

                        //set layout
                        showErrorDialog("Failed due to lack of internet access");

                    } else

                    if (output == 2){

                        //set layout
                        showErrorDialog("Failed due to lack of network connection");

                    }

                }
            }).execute();

        }

    }

    private void sendBroadcastNoti(String theTopic, final String theMessage, final String theKey) {

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
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            Map<String, String> dataSend = new HashMap<>();
                            dataSend.put("title", "Menorah Farms");
                            dataSend.put("message", theMessage);
                            DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(theKey).toString(), dataSend);

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
                });

    }


    /*---   WARNING DIALOG   ---*/
    public void showErrorDialog(String theWarning){

        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.dialog_layout,null);

        final TextView message = (TextView) viewOptions.findViewById(R.id.dialogMessage);
        final Button okButton = (Button) viewOptions.findViewById(R.id.dialogButton);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        message.setText(theWarning);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }
}
