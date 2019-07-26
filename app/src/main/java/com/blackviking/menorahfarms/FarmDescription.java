package com.blackviking.menorahfarms;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.menorahfarms.Common.Common;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FarmDescription extends AppCompatActivity {

    private ImageView backButton;
    private TextView farmDescUnits, farmDescROI, farmDescDuration, farmDescPrice, farmDescLocation, farmDescTerms;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference farmRef, termsRef;
    private String farmId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_description);


        /*---   LOCAL INTENT   ---*/
        farmId = getIntent().getStringExtra("FarmId");


        /*---   FIREBASE   ---*/
        farmRef = db.getReference("Farms");
        termsRef = db.getReference("TermsAndConditions");


        /*---   WIDGETS   ---*/
        backButton = (ImageView)findViewById(R.id.backButton);
        farmDescUnits = (TextView)findViewById(R.id.farmDescUnits);
        farmDescROI = (TextView)findViewById(R.id.farmDescROI);
        farmDescDuration = (TextView)findViewById(R.id.farmDescDuration);
        farmDescPrice = (TextView)findViewById(R.id.farmDescPrice);
        farmDescLocation = (TextView)findViewById(R.id.farmDescLocation);
        farmDescTerms = (TextView)findViewById(R.id.farmDescTerms);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loadCurrentFarm();
    }

    private void loadCurrentFarm() {

        farmRef.child(farmId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String theUnits = dataSnapshot.child("unitsAvailable").getValue().toString();
                        String theROI = dataSnapshot.child("farmRoi").getValue().toString();
                        String theDuration = dataSnapshot.child("sponsorDuration").getValue().toString();
                        String thePrice = dataSnapshot.child("pricePerUnit").getValue().toString();
                        String theLocation = dataSnapshot.child("farmLocation").getValue().toString();
                        String theFarmDescription = dataSnapshot.child("farmDescription").getValue().toString();

                        farmDescDuration.setText(theDuration + " Months");
                        farmDescLocation.setText(theLocation);
                        farmDescROI.setText(theROI + "%");
                        farmDescUnits.setText(theUnits);
                        farmDescTerms.setText(theFarmDescription);

                        long priceToLong = Long.parseLong(thePrice);
                        farmDescPrice.setText(Common.convertToPrice(FarmDescription.this, priceToLong));

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
