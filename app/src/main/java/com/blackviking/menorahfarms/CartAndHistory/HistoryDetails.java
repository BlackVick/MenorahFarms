package com.blackviking.menorahfarms.CartAndHistory;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.HistoryModel;
import com.blackviking.menorahfarms.Models.UserModel;
import com.blackviking.menorahfarms.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class HistoryDetails extends AppCompatActivity {

    private ImageView backButton;
    private TextView dueUserName, dueUserEmail, dueUserBank, dueFarmType, dueUnitPrice, dueUnits, dueRoi,
            dueTotalPaid, dueDuration, dueStartDate, dueEndDate, dueTotalReturn, dueRefNumber;

    private String theUserName, theUserEmail, theUserBank, theFarmType, theDueUnitPrice, theDueUnits, theDueRoi,
            theDueDuration, theDueStartDate, theDueEndDate, theDueTotalReturn, theDueRefNumber, theDueTotalPaid;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference adminHistoryRef;
    private String userId, historyId;
    private HistoryModel currentSponsorship;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_details);


        /*---   INTENT DATA   ---*/
        historyId = getIntent().getStringExtra("HistoryId");


        /*---   FIREBASE   ---*/
        adminHistoryRef = db.getReference(Common.ADMIN_HISTORY_NODE);


        /*---   WIDGETS   ---*/
        backButton = findViewById(R.id.backButton);
        dueUserName = findViewById(R.id.historyUserName);
        dueUserEmail = findViewById(R.id.historyUserEmail);
        dueUserBank = findViewById(R.id.historyUserBank);
        dueFarmType = findViewById(R.id.historyFarmType);
        dueUnitPrice = findViewById(R.id.historyUnitPrice);
        dueUnits = findViewById(R.id.historyUnits);
        dueRoi = findViewById(R.id.historyRoi);
        dueTotalPaid = findViewById(R.id.historyTotalPaid);
        dueDuration = findViewById(R.id.historyDuration);
        dueStartDate = findViewById(R.id.historyStartDate);
        dueEndDate = findViewById(R.id.historyEndDate);
        dueTotalReturn = findViewById(R.id.historyTotalReturn);
        dueRefNumber = findViewById(R.id.historyRefNumber);


        //back
        backButton.setOnClickListener(v -> finish());


        //load history details
        loadAllDetails();

    }

    private void loadAllDetails() {

        UserModel currentUser = Paper.book().read(Common.PAPER_USER);

        theUserName = currentUser.getFirstName() + " " + currentUser.getLastName();
        theUserEmail = currentUser.getEmail();
        theUserBank = currentUser.getBank() + ", " + currentUser.getAccountNumber();

        adminHistoryRef.child(historyId)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                currentSponsorship = dataSnapshot.getValue(HistoryModel.class);

                                theFarmType = currentSponsorship.getSponsoredFarmType();
                                theDueUnitPrice = currentSponsorship.getUnitPrice();
                                theDueUnits = currentSponsorship.getSponsoredUnits();
                                theDueRoi = currentSponsorship.getSponsoredFarmRoi();
                                theDueTotalPaid = String.valueOf(currentSponsorship.getTotalAmountPaid());
                                theDueDuration = currentSponsorship.getSponsorshipDuration();
                                theDueStartDate = currentSponsorship.getCycleStartDate();
                                theDueEndDate = currentSponsorship.getCycleEndDate();
                                theDueTotalReturn = currentSponsorship.getSponsorReturn();
                                theDueRefNumber = currentSponsorship.getSponsorRefNumber();

                                setAllValues();

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

    }

    private void setAllValues() {

        long unitPrice = Long.parseLong(theDueUnitPrice);
        long totalReturn = Long.parseLong(theDueTotalReturn);

        dueUserName.setText(theUserName);
        dueUserEmail.setText(theUserEmail);
        dueUserBank.setText(theUserBank);
        dueFarmType.setText(theFarmType);
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
        finish();
    }
}
