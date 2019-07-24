package com.blackviking.menorahfarms.CartAndHistory;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.menorahfarms.AdminDetails.DueSponsorshipDetail;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.HistoryModel;
import com.blackviking.menorahfarms.Models.SponsoredFarmModel;
import com.blackviking.menorahfarms.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HistoryDetails extends AppCompatActivity {

    private ImageView backButton;
    private TextView dueUserName, dueUserEmail, dueUserBank, dueFarmType, dueUnitPrice, dueUnits, dueRoi,
            dueTotalPaid, dueDuration, dueStartDate, dueEndDate, dueTotalReturn, dueRefNumber;

    private String theDueUserName, theDueUserEmail, theDueUserBank, theDueFarmType, theDueUnitPrice, theDueUnits, theDueRoi,
            theDueTotalPaid, theDueDuration, theDueStartDate, theDueEndDate, theDueTotalReturn, theDueRefNumber;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference  userRef, adminHistoryRef, historyRef, sponsoredFarmsRef;
    private String userId, sponsorshipId, historyId;
    private HistoryModel currentSponsorship;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_details);


        /*---   INTENT DATA   ---*/
        historyId = getIntent().getStringExtra("HistoryId");


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        adminHistoryRef = db.getReference("AdminHistory");
        historyRef = db.getReference("History");
        sponsoredFarmsRef = db.getReference("SponsoredFarms");


        /*---   WIDGETS   ---*/
        backButton = (ImageView)findViewById(R.id.backButton);
        dueUserName = (TextView)findViewById(R.id.historyUserName);
        dueUserEmail = (TextView)findViewById(R.id.historyUserEmail);
        dueUserBank = (TextView)findViewById(R.id.historyUserBank);
        dueFarmType = (TextView)findViewById(R.id.historyFarmType);
        dueUnitPrice = (TextView)findViewById(R.id.historyUnitPrice);
        dueUnits = (TextView)findViewById(R.id.historyUnits);
        dueRoi = (TextView)findViewById(R.id.historyRoi);
        dueTotalPaid = (TextView)findViewById(R.id.historyTotalPaid);
        dueDuration = (TextView)findViewById(R.id.historyDuration);
        dueStartDate = (TextView)findViewById(R.id.historyStartDate);
        dueEndDate = (TextView)findViewById(R.id.historyEndDate);
        dueTotalReturn = (TextView)findViewById(R.id.historyTotalReturn);
        dueRefNumber = (TextView)findViewById(R.id.historyRefNumber);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        adminHistoryRef.child(historyId)
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

        adminHistoryRef.child(historyId)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                currentSponsorship = dataSnapshot.getValue(HistoryModel.class);

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
        dueUnitPrice.setText(Common.convertToPrice(HistoryDetails.this, unitPrice));
        dueUnits.setText(theDueUnits);
        dueRoi.setText(theDueRoi + "%");
        dueTotalPaid.setText(Common.convertToPrice(HistoryDetails.this, currentSponsorship.getTotalAmountPaid()));
        dueDuration.setText(theDueDuration + " Months");
        dueStartDate.setText(theDueStartDate);
        dueEndDate.setText(theDueEndDate);
        dueTotalReturn.setText(Common.convertToPrice(HistoryDetails.this, totalReturn));
        dueRefNumber.setText(theDueRefNumber);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
