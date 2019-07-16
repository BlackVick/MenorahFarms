package com.blackviking.menorahfarms.AdminDetails;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.SponsoredFarmModel;
import com.blackviking.menorahfarms.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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

public class DueSponsorshipDetail extends AppCompatActivity {

    private Button dueSettled;
    private ImageView backButton;
    private TextView dueUserName, dueUserEmail, dueUserBank, dueFarmType, dueUnitPrice, dueUnits, dueRoi,
            dueTotalPaid, dueDuration, dueStartDate, dueEndDate, dueTotalReturn, dueRefNumber;

    private String theDueUserName, theDueUserEmail, theDueUserBank, theDueFarmType, theDueUnitPrice, theDueUnits, theDueRoi,
            theDueTotalPaid, theDueDuration, theDueStartDate, theDueEndDate, theDueTotalReturn, theDueRefNumber;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, dueSponsorshipRef, notificationRef, adminHistoryRef, historyRef, sponsoredFarmsRef;
    private String userId, sponsorshipId, dueSponsorshipId;
    private SponsoredFarmModel currentSponsorship;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_due_sponsorship_detail);


        /*---   INTENT DATA   ---*/
        dueSponsorshipId = getIntent().getStringExtra("DueSponsorshipId");


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        dueSponsorshipRef = db.getReference("DueSponsorships");
        notificationRef = db.getReference("Notifications");
        adminHistoryRef = db.getReference("AdminHistory");
        historyRef = db.getReference("History");
        sponsoredFarmsRef = db.getReference("SponsoredFarms");


        /*---   WIDGETS   ---*/
        backButton = (ImageView)findViewById(R.id.backButton);
        dueSettled = (Button)findViewById(R.id.dueSettled);
        dueUserName = (TextView)findViewById(R.id.dueUserName);
        dueUserEmail = (TextView)findViewById(R.id.dueUserEmail);
        dueUserBank = (TextView)findViewById(R.id.dueUserBank);
        dueFarmType = (TextView)findViewById(R.id.dueFarmType);
        dueUnitPrice = (TextView)findViewById(R.id.dueUnitPrice);
        dueUnits = (TextView)findViewById(R.id.dueUnits);
        dueRoi = (TextView)findViewById(R.id.dueRoi);
        dueTotalPaid = (TextView)findViewById(R.id.dueTotalPaid);
        dueDuration = (TextView)findViewById(R.id.dueDuration);
        dueStartDate = (TextView)findViewById(R.id.dueStartDate);
        dueEndDate = (TextView)findViewById(R.id.dueEndDate);
        dueTotalReturn = (TextView)findViewById(R.id.dueTotalReturn);
        dueRefNumber = (TextView)findViewById(R.id.dueRefNumber);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        dueSponsorshipRef.child(dueSponsorshipId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        userId = dataSnapshot.child("user").getValue().toString();
                        sponsorshipId = dataSnapshot.child("sponsorshipId").getValue().toString();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        loadAllDetails();

    }

    private void loadAllDetails() {

        userRef.child(userId)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                String firstName = dataSnapshot.child("firstName").getValue().toString();
                                String lastName = dataSnapshot.child("lastName").getValue().toString();
                                String bankName = dataSnapshot.child("bank").getValue().toString();
                                String accountNumber = dataSnapshot.child("accountNumber").getValue().toString();

                                theDueUserName = firstName + " " + lastName;
                                theDueUserEmail = dataSnapshot.child("email").getValue().toString();
                                theDueUserBank = bankName + ", " + accountNumber;


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

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

        setAllValues();
    }

    private void setAllValues() {

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

        dueSettled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                settleSponsorship();

            }
        });

    }

    private void settleSponsorship() {

        final Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String todayString = formatter.format(todayDate);


        final Map<String, Object> historyMap = new HashMap<>();
        historyMap.put("sponsorshipUser", userId);
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

        adminHistoryRef.push()
                .setValue(historyMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        historyRef.child(userId)
                                .push()
                                .setValue(historyMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        sponsoredFarmsRef.child(userId)
                                                .child(sponsorshipId)
                                                .removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        sendNotification();
                                                    }
                                                });

                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void sendNotification() {
    }
}