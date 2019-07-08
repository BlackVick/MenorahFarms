package com.blackviking.menorahfarms;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FarmDetails extends AppCompatActivity {

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef, farmRef;
    private String currentUid, userType, farmId;

    private ImageView backButton, farmImage;
    private TextView farmType, unitsLeft, farmLocation, farmROI, unitPrice, totalROI, totalDuration, totalPay;
    private ElegantNumberButton unitNumber;
    private RelativeLayout followFarmBtn, addToCartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_details);


        /*---   INTENT DATA   ---*/
        farmId = getIntent().getStringExtra("FarmId");


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        farmRef = db.getReference("Farms");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        backButton = (ImageView)findViewById(R.id.backButton);
        farmImage = (ImageView)findViewById(R.id.farmDetailImage);
        farmType = (TextView)findViewById(R.id.farmDetailsType);
        unitsLeft = (TextView)findViewById(R.id.farmDetailsUnitsLeft);
        farmLocation = (TextView)findViewById(R.id.farmDetailLocation);
        farmROI = (TextView)findViewById(R.id.farmDetailROI);
        unitPrice = (TextView)findViewById(R.id.farmDetailUnitPrice);
        totalROI = (TextView)findViewById(R.id.totalRoiAndPrice);
        totalDuration = (TextView)findViewById(R.id.totalDuration);
        totalPay = (TextView)findViewById(R.id.totalPayback);
        unitNumber = (ElegantNumberButton)findViewById(R.id.farmDetailNumberButton);
        followFarmBtn = (RelativeLayout)findViewById(R.id.followFarmButton);
        addToCartBtn = (RelativeLayout)findViewById(R.id.addToCartButton);


        loadCurrentFarm();


    }

    private void loadCurrentFarm() {

        farmRef.child(farmId)
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                String theFarmType = dataSnapshot.child("farmType").getValue().toString();
                                String theFarmLocation = dataSnapshot.child("farmLocation").getValue().toString();
                                String theFarmROI = dataSnapshot.child("farmRoi").getValue().toString();
                                String theFarmUnitPrice = dataSnapshot.child("pricePerUnit").getValue().toString();
                                String theFarmSponsorDuration = dataSnapshot.child("sponsorDuration").getValue().toString();
                                String theFarmUnitsLeft = dataSnapshot.child("unitsAvailable").getValue().toString();



                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

    }
}
