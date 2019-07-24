package com.blackviking.menorahfarms.Sponsorship;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.SponsoredFarmModel;
import com.blackviking.menorahfarms.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SponsorshipDetails extends AppCompatActivity {

    private ImageView backButton;
    private TextView sponsoredFarmType, sponsoredFarmUnitPrice, sponsoredFarmUnits, sponsoredFarmROI,
            sponsoredFarmDuration, sponsoredFarmStartDate, sponsoredFarmEndDate, sponsoredFarmTotalReturn,
            sponsoredFarmRefNumber, sponsoredFarmTotalPrice;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference farmref, sponsoredFarmRef;
    private String currentUid, sponsorshipId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sponsorship_details);


        /*---   INTENT DATA   ---*/
        sponsorshipId = getIntent().getStringExtra("SponsorshipId");


        /*---   FIREBASE   ---*/
        farmref = db.getReference("Farms");
        sponsoredFarmRef = db.getReference("SponsoredFarms");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        backButton = (ImageView)findViewById(R.id.backButton);
        sponsoredFarmType = (TextView)findViewById(R.id.sponsoredFarmType);
        sponsoredFarmUnitPrice = (TextView)findViewById(R.id.sponsoredFarmUnitPrice);
        sponsoredFarmUnits = (TextView)findViewById(R.id.sponsoredFarmUnits);
        sponsoredFarmROI = (TextView)findViewById(R.id.sponsoredFarmROI);
        sponsoredFarmDuration = (TextView)findViewById(R.id.sponsoredFarmDuration);
        sponsoredFarmStartDate = (TextView)findViewById(R.id.sponsoredFarmStartDate);
        sponsoredFarmEndDate = (TextView)findViewById(R.id.sponsoredFarmEndDate);
        sponsoredFarmTotalReturn = (TextView)findViewById(R.id.sponsoredFarmTotalReturn);
        sponsoredFarmRefNumber = (TextView)findViewById(R.id.sponsoredFarmRefNumber);
        sponsoredFarmTotalPrice = (TextView)findViewById(R.id.sponsoredFarmTotalPrice);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        loadSponsorshipDetails();
    }

    private void loadSponsorshipDetails() {

        sponsoredFarmRef.child(currentUid)
                .child(sponsorshipId)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                SponsoredFarmModel currentSponsorship = dataSnapshot.getValue(SponsoredFarmModel.class);

                                sponsoredFarmType.setText(currentSponsorship.getSponsoredFarmType());
                                sponsoredFarmUnits.setText(currentSponsorship.getSponsoredUnits());
                                sponsoredFarmROI.setText(currentSponsorship.getSponsoredFarmRoi() + "%");
                                sponsoredFarmDuration.setText(currentSponsorship.getSponsorshipDuration() + " Months");
                                sponsoredFarmStartDate.setText(currentSponsorship.getCycleStartDate());
                                sponsoredFarmEndDate.setText(currentSponsorship.getCycleEndDate());
                                sponsoredFarmRefNumber.setText(currentSponsorship.getSponsorRefNumber());


                                long unitPriceToLong = Long.parseLong(currentSponsorship.getUnitPrice());
                                long totalReturnToLong = Long.parseLong(currentSponsorship.getSponsorReturn());


                                sponsoredFarmUnitPrice.setText(Common.convertToPrice(SponsorshipDetails.this, unitPriceToLong));
                                sponsoredFarmTotalReturn.setText(Common.convertToPrice(SponsorshipDetails.this, totalReturnToLong));
                                sponsoredFarmTotalPrice.setText(Common.convertToPrice(SponsorshipDetails.this, currentSponsorship.getTotalAmountPaid()));




                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
