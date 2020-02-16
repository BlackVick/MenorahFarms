package com.blackviking.menorahfarms.AdminFragments;


import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Notification.APIService;
import com.blackviking.menorahfarms.Notification.DataMessage;
import com.blackviking.menorahfarms.Notification.MyResponse;
import com.blackviking.menorahfarms.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;

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

    //widgets
    private EditText followedTopic, followedMessage, sponsoredTopic, sponsoredMessage;
    private Button sendFollowedNotiBtn, sendSponsoredNotiBtn;
    private FloatingActionButton broadcastToAllFab;
    private RelativeLayout directionLayout, followedFarmLayout, sponsoredFarmLayout;

    //firebase
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference notificationRef, userRef, farmRef, followedFarmNotiRef, sponsoredFarmNotiRef;
    private FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
    private APIService mService;

    //controller
    private Spinner followedSpinner, sponsoredSpinner;
    private RadioGroup notificationStyle;
    private String selectedFollowedGroup = "", selectedSponsoredGroup = "";


    public AdminNotify() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_notify, container, false);

        
        /*---   FIREBASE   ---*/
        notificationRef = db.getReference(Common.NOTIFICATIONS_NODE);
        userRef = db.getReference(Common.USERS_NODE);
        farmRef = db.getReference(Common.FARM_NODE);
        followedFarmNotiRef = db.getReference(Common.FOLLOWED_FARMS_NOTIFICATION_NODE);
        sponsoredFarmNotiRef = db.getReference(Common.SPONSORED_FARMS_NOTIFICATION_NODE);


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
        notificationStyle.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId){

                case R.id.followedFarmsNoti:
                    loadFollowedFarms();
                    break;

                case R.id.sponsoredFarmsNoti:
                    loadSponsoredFarms();
                    break;

            }
        });


        broadcastToAllFab.setOnClickListener(v1 -> openBroadcastDialog());
        
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

        //run network check
        new CheckInternet(getContext(), output -> {

            //check all cases
            if (output == 1){

                followedFarmNotiRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
        sendFollowedNotiBtn.setOnClickListener(v -> checkFollowedFarmsNotiParam());

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

            Toast.makeText(getContext(), "Select A Farm", Toast.LENGTH_SHORT).show();

        } else {

            //execute network check async task
            new CheckInternet(getContext(), output -> {

                //check all cases
                if (output == 1){

                    sendNotificationToFollowedFarms(selectedFollowedGroup, theTopic, theMessage)
                            .addOnCompleteListener(task -> {

                                if (!task.isSuccessful()) {

                                    Exception e = task.getException();
                                    if (e instanceof FirebaseFunctionsException) {
                                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                        FirebaseFunctionsException.Code code = ffe.getCode();
                                        Object details = ffe.getDetails();

                                    }

                                }

                                Toast.makeText(getContext(), "DONE", Toast.LENGTH_SHORT).show();
                                followedTopic.setText("");
                                followedMessage.setText("");

                            });

                } else

                if (output == 0){

                    //set layout
                    Toast.makeText(getContext(), "No internet access", Toast.LENGTH_SHORT).show();

                } else

                if (output == 2){

                    //set layout
                    Toast.makeText(getContext(), "Not connected to a network", Toast.LENGTH_SHORT).show();

                }

            }).execute();

        }

    }

    private Task<String> sendNotificationToFollowedFarms(final String theFarm, final String theTopic, final String theMessage) {

        //get today String
        final Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String todayString = formatter.format(todayDate);

        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("farm", theFarm);
        data.put("topic", theTopic);
        data.put("message", theMessage);
        data.put("time", todayString);

        return mFunctions
                .getHttpsCallable("sendNotificationToFollowedFarms")
                .call(data)
                .continueWith(task -> {
                    // This continuation runs on either success or failure, but if the task
                    // has failed then getResult() will throw an Exception which will be
                    // propagated down.
                    String result = (String) task.getResult().getData();
                    Log.e("SendFollowedNoti", result);

                    return result;
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

        //run network check
        new CheckInternet(getContext(), output -> {

            //check all cases
            if (output == 1){

                sponsoredFarmNotiRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
        sendSponsoredNotiBtn.setOnClickListener(v -> checkSponsoredFarmsNotiParam());

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

            Toast.makeText(getContext(), "Select A Farm", Toast.LENGTH_SHORT).show();

        } else {

            //execute network check async task
            new CheckInternet(getContext(), output -> {

                //check all cases
                if (output == 1){

                    sendNotificationToFarmSponsors(selectedSponsoredGroup, theTopic, theMessage)
                            .addOnCompleteListener(task -> {

                                if (!task.isSuccessful()) {

                                    Exception e = task.getException();
                                    if (e instanceof FirebaseFunctionsException) {
                                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                        FirebaseFunctionsException.Code code = ffe.getCode();
                                        Object details = ffe.getDetails();

                                    }

                                }

                                Toast.makeText(getContext(), "DONE", Toast.LENGTH_SHORT).show();
                                sponsoredTopic.setText("");
                                sponsoredMessage.setText("");

                            });

                } else

                if (output == 0){

                    //set layout
                    Toast.makeText(getContext(), "No internet access", Toast.LENGTH_SHORT).show();

                } else

                if (output == 2){

                    //set layout
                    Toast.makeText(getContext(), "No network access", Toast.LENGTH_SHORT).show();

                }

            }).execute();

        }

    }

    private Task<String> sendNotificationToFarmSponsors(final String theFarm, final String theTopic, final String theMessage) {

        //get today String
        final Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String todayString = formatter.format(todayDate);

        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("farm", theFarm);
        data.put("topic", theTopic);
        data.put("message", theMessage);
        data.put("time", todayString);

        return mFunctions
                .getHttpsCallable("sendNotificationToFarmSponsors")
                .call(data)
                .continueWith(task -> {
                    // This continuation runs on either success or failure, but if the task
                    // has failed then getResult() will throw an Exception which will be
                    // propagated down.
                    String result = (String) task.getResult().getData();
                    Log.e("SendSponsoredNoti", result);

                    return result;
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

        sendBroadcastBtn.setOnClickListener(v -> checkBroadcastNotiParam(alertDialog, broadcastTopic, broadcastMessage));


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

            //run network check
            new CheckInternet(getContext(), output -> {

                //check all cases
                if (output == 1){

                    sendNotificationToAll(theTopic, theMessage)
                            .addOnCompleteListener(task -> {

                                if (!task.isSuccessful()) {

                                    Exception e = task.getException();
                                    if (e instanceof FirebaseFunctionsException) {
                                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                        FirebaseFunctionsException.Code code = ffe.getCode();
                                        Object details = ffe.getDetails();

                                    }

                                }

                                Toast.makeText(getContext(), "DONE", Toast.LENGTH_SHORT).show();
                                alertDialog.dismiss();

                            });

                } else

                if (output == 0){

                    //set layout
                    Toast.makeText(getContext(), "No internet access", Toast.LENGTH_SHORT).show();

                } else

                if (output == 2){

                    //set layout
                    Toast.makeText(getContext(), "No network access", Toast.LENGTH_SHORT).show();

                }

            }).execute();

        }

    }

    private Task<String> sendNotificationToAll(final String theTopic, final String theMessage) {

        //get today String
        final Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String todayString = formatter.format(todayDate);

        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("topic", theTopic);
        data.put("message", theMessage);
        data.put("time", todayString);

        return mFunctions
                .getHttpsCallable("sendNotificationToAll")
                .call(data)
                .continueWith(task -> {
                    // This continuation runs on either success or failure, but if the task
                    // has failed then getResult() will throw an Exception which will be
                    // propagated down.
                    String result = (String) task.getResult().getData();
                    Log.e("SendNotiToAll", result);

                    return result;
                });
    }

}
