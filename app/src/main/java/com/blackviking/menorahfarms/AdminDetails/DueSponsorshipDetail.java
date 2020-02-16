package com.blackviking.menorahfarms.AdminDetails;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.DueSponsorshipModel;
import com.blackviking.menorahfarms.Models.FarmModel;
import com.blackviking.menorahfarms.Models.SponsoredFarmModel;
import com.blackviking.menorahfarms.Models.UserModel;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Response;

public class DueSponsorshipDetail extends AppCompatActivity {

    private Button dueSettled;
    private ImageView backButton;
    private TextView dueUserName, dueUserEmail, dueUserBank, dueFarmType, dueUnitPrice, dueUnits, dueRoi,
            dueTotalPaid, dueDuration, dueStartDate, dueEndDate, dueTotalReturn, dueRefNumber;

    private String theDueUserName, theDueUserEmail, theDueUserBank, theDueFarmType, theDueUnitPrice, theDueUnits, theDueRoi,
            theDueTotalPaid, theDueDuration, theDueStartDate, theDueEndDate, theDueTotalReturn, theDueRefNumber;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, dueSponsorshipRef, notificationRef, adminHistoryRef,
            historyRef, sponsoredFarmsRef, adminSponsorshipRef, sponsoredFarmNotiRef, farmRef;
    private String dueSponsorshipId;
    private SponsoredFarmModel currentSponsorship;


    private APIService mService;
    private android.app.AlertDialog alertDialog;
    private android.app.AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_due_sponsorship_detail);


        /*---   INTENT DATA   ---*/
        dueSponsorshipId = getIntent().getStringExtra("DueSponsorshipId");


        /*---   FCM   ---*/
        mService = Common.getFCMService();


        /*---   FIREBASE   ---*/
        userRef = db.getReference(Common.USERS_NODE);
        dueSponsorshipRef = db.getReference(Common.DUE_SPONSORSHIPS_NODE);
        notificationRef = db.getReference(Common.NOTIFICATIONS_NODE);
        adminHistoryRef = db.getReference(Common.ADMIN_HISTORY_NODE);
        historyRef = db.getReference(Common.HISTORY_NODE);
        sponsoredFarmsRef = db.getReference(Common.SPONSORED_FARMS_NODE);
        adminSponsorshipRef = db.getReference(Common.RUNNING_CYCLE_NODE);
        sponsoredFarmNotiRef = db.getReference(Common.SPONSORED_FARMS_NOTIFICATION_NODE);
        farmRef = db.getReference(Common.FARM_NODE);


        /*---   WIDGETS   ---*/
        backButton = findViewById(R.id.backButton);
        dueSettled = findViewById(R.id.dueSettled);
        dueUserName = findViewById(R.id.dueUserName);
        dueUserEmail = findViewById(R.id.dueUserEmail);
        dueUserBank = findViewById(R.id.dueUserBank);
        dueFarmType = findViewById(R.id.dueFarmType);
        dueUnitPrice = findViewById(R.id.dueUnitPrice);
        dueUnits = findViewById(R.id.dueUnits);
        dueRoi = findViewById(R.id.dueRoi);
        dueTotalPaid = findViewById(R.id.dueTotalPaid);
        dueDuration = findViewById(R.id.dueDuration);
        dueStartDate = findViewById(R.id.dueStartDate);
        dueEndDate = findViewById(R.id.dueEndDate);
        dueTotalReturn = findViewById(R.id.dueTotalReturn);
        dueRefNumber = findViewById(R.id.dueRefNumber);


        backButton.setOnClickListener(v -> finish());


        //show loading dialog
        showLoadingDialog("Loading sponsorship details");

        //run network check
        new CheckInternet(DueSponsorshipDetail.this, output -> {

            //check all cases
            if (output == 1){

                dueSponsorshipRef.child(dueSponsorshipId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                DueSponsorshipModel currentDue = dataSnapshot.getValue(DueSponsorshipModel.class);

                                if (currentDue != null){

                                    loadAllDetails(currentDue.getUser(), currentDue.getSponsorshipId());

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            } else

            if (output == 0){

                //no internet
                alertDialog.dismiss();
                Toast.makeText(DueSponsorshipDetail.this, "No internet access", Toast.LENGTH_SHORT).show();

            } else

            if (output == 2){

                //no internet
                alertDialog.dismiss();
                Toast.makeText(DueSponsorshipDetail.this, "Not connected to any network", Toast.LENGTH_SHORT).show();

            }

        }).execute();

    }

    private void loadAllDetails(final String userId, final String sponsorshipId) {

        userRef.child(userId)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                UserModel currentUser = dataSnapshot.getValue(UserModel.class);

                                if (currentUser != null){

                                    theDueUserName = currentUser.getFirstName() + " " + currentUser.getLastName();
                                    theDueUserEmail = currentUser.getEmail();
                                    theDueUserBank = currentUser.getBank() + ", " + currentUser.getAccountNumber();

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

        sponsoredFarmsRef.child(userId)
                .child(sponsorshipId)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                currentSponsorship = dataSnapshot.getValue(SponsoredFarmModel.class);

                                if (currentSponsorship != null) {

                                    theDueFarmType = currentSponsorship.getSponsoredFarmType();
                                    theDueUnitPrice = currentSponsorship.getUnitPrice();
                                    theDueUnits = currentSponsorship.getSponsoredUnits();
                                    theDueRoi = currentSponsorship.getSponsoredFarmRoi();
                                    theDueTotalPaid = String.valueOf(currentSponsorship.getTotalAmountPaid());
                                    theDueDuration = currentSponsorship.getSponsorshipDuration();
                                    theDueStartDate = currentSponsorship.getCycleStartDate();
                                    theDueEndDate = currentSponsorship.getCycleEndDate();
                                    theDueTotalReturn = currentSponsorship.getSponsorReturn();
                                    theDueRefNumber = currentSponsorship.getSponsorRefNumber();

                                    setAllValues(userId, sponsorshipId, currentSponsorship);

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );


    }

    private void setAllValues(final String userId, final String sponsorshipId, final SponsoredFarmModel currentSponsorship) {

        //cancel loading screen
        alertDialog.dismiss();

        long unitPrice = Long.parseLong(theDueUnitPrice);
        long totalReturn = Long.parseLong(theDueTotalReturn);

        dueUserName.setText(theDueUserName);
        dueUserEmail.setText(theDueUserEmail);
        dueUserBank.setText(theDueUserBank);
        dueFarmType.setText(theDueFarmType);
        dueUnitPrice.setText(Common.convertToPrice(DueSponsorshipDetail.this, unitPrice));
        dueUnits.setText(theDueUnits);
        dueRoi.setText(theDueRoi + "%");
        dueTotalPaid.setText(Common.convertToPrice(DueSponsorshipDetail.this, currentSponsorship.getTotalAmountPaid()));
        dueDuration.setText(theDueDuration + " Months");
        dueStartDate.setText(theDueStartDate);
        dueEndDate.setText(theDueEndDate);
        dueTotalReturn.setText(Common.convertToPrice(DueSponsorshipDetail.this, totalReturn));
        dueRefNumber.setText(theDueRefNumber);

        dueSettled.setOnClickListener(v -> {

            //show loading dialog
            mDialog = new SpotsDialog(DueSponsorshipDetail.this, "Processing");
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();

            //execute network check async task
            new CheckInternet(DueSponsorshipDetail.this, output -> {

                //check all cases
                if (output == 1){

                    settleSponsorship(userId, sponsorshipId, currentSponsorship);

                } else

                if (output == 0){

                    //no internet
                    mDialog.dismiss();
                    Toast.makeText(DueSponsorshipDetail.this, "No internet access", Toast.LENGTH_SHORT).show();

                } else

                if (output == 2){

                    //no internet
                    mDialog.dismiss();
                    Toast.makeText(DueSponsorshipDetail.this, "Not connected to any network", Toast.LENGTH_SHORT).show();

                }

            }).execute();

        });

    }

    private void settleSponsorship(final String theUserId, final String theSponsorshipId, final SponsoredFarmModel currentSponsorship) {

        final Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String todayString = formatter.format(todayDate);


        final Map<String, Object> historyMap = new HashMap<>();
        historyMap.put("sponsorshipUser", theUserId);
        historyMap.put("sponsorReturn", currentSponsorship.getSponsorReturn());
        historyMap.put("cycleEndDate", currentSponsorship.getCycleEndDate());
        historyMap.put("cycleStartDate", currentSponsorship.getCycleStartDate());
        historyMap.put("sponsorRefNumber", currentSponsorship.getSponsorRefNumber());
        historyMap.put("unitPrice", currentSponsorship.getUnitPrice());
        historyMap.put("sponsoredUnits", currentSponsorship.getSponsoredUnits());
        historyMap.put("sponsoredFarmType", currentSponsorship.getSponsoredFarmType());
        historyMap.put("sponsoredFarmRoi", currentSponsorship.getSponsoredFarmRoi());
        historyMap.put("sponsorshipDuration", currentSponsorship.getSponsorshipDuration());
        historyMap.put("dateSettled", todayString);
        historyMap.put("totalAmountPaid", currentSponsorship.getTotalAmountPaid());
        historyMap.put("farmId", currentSponsorship.getFarmId());

        DatabaseReference pushRef = adminHistoryRef.push();
        final String pushId= pushRef.getKey();


        adminHistoryRef.child(pushId)
                .setValue(historyMap)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()){
                        historyRef.child(theUserId)
                                .child(pushId)
                                .setValue(historyMap)
                                .addOnCompleteListener(task1 -> {

                                    if (task1.isSuccessful()){

                                        removeFromCycle(currentSponsorship.getFarmId(), theSponsorshipId);

                                        removeNotification(currentSponsorship.getFarmId(), theUserId);
                                        sponsoredFarmsRef.child(theUserId)
                                                .child(theSponsorshipId)
                                                .removeValue()
                                                .addOnCompleteListener(task11 -> {

                                                    if (task11.isSuccessful()){

                                                        dueSponsorshipRef.child(dueSponsorshipId)
                                                                .removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task11) {

                                                                        if (task11.isSuccessful()){

                                                                            mDialog.dismiss();
                                                                            sendNotification(theUserId);
                                                                            Toast.makeText(DueSponsorshipDetail.this, "Sponsorship was successfully settled", Toast.LENGTH_LONG).show();

                                                                        }

                                                                    }
                                                                });

                                                    } else {

                                                        mDialog.dismiss();
                                                        Toast.makeText(DueSponsorshipDetail.this, "Error occurred", Toast.LENGTH_SHORT).show();

                                                    }

                                                });

                                    } else {

                                        mDialog.dismiss();
                                        Toast.makeText(DueSponsorshipDetail.this, "Error occurred", Toast.LENGTH_SHORT).show();

                                    }

                                });

                    } else {

                        mDialog.dismiss();
                        Toast.makeText(DueSponsorshipDetail.this, "Error occurred", Toast.LENGTH_SHORT).show();

                    }
                });


    }

    private void removeFromCycle(final String farmId, final String sponsorshipId) {

        farmRef.child(farmId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        FarmModel theFarm = dataSnapshot.getValue(FarmModel.class);

                        if (theFarm != null){

                            adminSponsorshipRef.child(theFarm.getFarmNotiId())
                                    .child(sponsorshipId)
                                    .removeValue();

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void removeNotification(String farmId, final String theUserId) {

        farmRef.child(farmId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        FarmModel theFarm = dataSnapshot.getValue(FarmModel.class);

                        if (theFarm != null){

                            sponsoredFarmNotiRef.child(theFarm.getFarmNotiId())
                                    .child(theUserId)
                                    .removeValue();

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    /*---   LOADING DIALOG   ---*/
    public void showLoadingDialog(String theMessage){

        alertDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.loading_dialog,null);

        final TextView loadingText = viewOptions.findViewById(R.id.loadingText);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        loadingText.setText(theMessage);

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });

        alertDialog.show();

    }

    private void sendNotification(final String theUserId) {

        final Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy  hh:mm");
        String todayString = formatter.format(todayDate);

        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("topic", "Sponsorship");
        notificationMap.put("message", "Your sponsorship with Reference Number " + theDueRefNumber + " has been settled.");
        notificationMap.put("time", todayString);


        notificationRef.child(theUserId)
                .push()
                .setValue(notificationMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Map<String, String> dataSend = new HashMap<>();
                        dataSend.put("title", "Sponsorship");
                        dataSend.put("message", "Your sponsorship with Reference Number " + theDueRefNumber + " has been settled.");
                        DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(theUserId).toString(), dataSend);

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
                });

        finish();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
