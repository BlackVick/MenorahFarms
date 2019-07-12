package com.blackviking.menorahfarms;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.Common;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class FarmDetails extends AppCompatActivity {

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef, farmRef, followedRef, cartRef;
    private String currentUid, userType, farmId;

    private ImageView backButton, farmImage;
    private TextView farmType, unitsLeft, farmLocation, farmROI, unitPrice, totalROI, totalDuration, totalPay;
    private ImageView decreaseUnitNumber, increaseUnitNumber;
    private TextView unitNumber;
    private RelativeLayout followFarmBtn, addToCartBtn;

    private int unitNumberText = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_details);


        /*---   INTENT DATA   ---*/
        farmId = getIntent().getStringExtra("FarmId");


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        farmRef = db.getReference("Farms");
        followedRef = db.getReference("FollowedFarms");
        cartRef = db.getReference("Carts");
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
        followFarmBtn = (RelativeLayout)findViewById(R.id.followFarmButton);
        addToCartBtn = (RelativeLayout)findViewById(R.id.addToCartButton);
        decreaseUnitNumber = (ImageView)findViewById(R.id.decreaseUnitNumber);
        increaseUnitNumber = (ImageView)findViewById(R.id.increaseUnitNumber);
        unitNumber = (TextView)findViewById(R.id.unitNumber);
        unitNumber.setText(String.valueOf(unitNumberText));


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
                                final String theFarmROI = dataSnapshot.child("farmRoi").getValue().toString();
                                final String theFarmUnitPrice = dataSnapshot.child("pricePerUnit").getValue().toString();
                                String theFarmSponsorDuration = dataSnapshot.child("sponsorDuration").getValue().toString();
                                String theFarmUnitsLeft = dataSnapshot.child("unitsAvailable").getValue().toString();


                                farmType.setText(theFarmType);
                                unitsLeft.setText(theFarmUnitsLeft);
                                farmLocation.setText(theFarmLocation);
                                farmROI.setText("Returns " + theFarmROI + "% in " + theFarmSponsorDuration + " months.");

                                long priceToLong = Long.parseLong(theFarmUnitPrice);

                                unitPrice.setText(Common.convertToPrice(FarmDetails.this, priceToLong));

                                totalROI.setText("Return on investment (" + theFarmROI + "%) - " + Common.convertToPrice(FarmDetails.this, priceToLong));
                                totalDuration.setText("Total payout after " + theFarmSponsorDuration + " months");

                                /*---   CALCULATION   ---*/
                                long theFixedPricePerUnit = Long.parseLong(theFarmUnitPrice);
                                int theFixedRoi = Integer.parseInt(theFarmROI);

                                /*---   CALCULATION   ---*/
                                long theCalculatedPrice = theFixedPricePerUnit * unitNumberText;
                                long totalCalculation = theCalculatedPrice * theFixedRoi / 100;
                                long totalResult = totalCalculation + theCalculatedPrice;

                                totalPay.setText(Common.convertToPrice(FarmDetails.this, totalResult));


                                /*---   UNIT NUMBERS   ---*/
                                increaseUnitNumber.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        unitNumberText ++;
                                        unitNumber.setText(String.valueOf(unitNumberText));

                                        calculateChanges(unitNumberText, theFarmUnitPrice, theFarmROI);
                                    }
                                });

                                decreaseUnitNumber.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        if (unitNumberText == 1){



                                        } else {

                                            unitNumberText --;
                                            unitNumber.setText(String.valueOf(unitNumberText));

                                            calculateChanges(unitNumberText, theFarmUnitPrice, theFarmROI);

                                        }

                                    }
                                });


                                followFarmBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        followFarm();

                                    }
                                });

                                addToCartBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        addToCart(unitNumberText, theFarmUnitPrice, theFarmROI);

                                    }
                                });


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

    }

    private void addToCart(int unitNumberText, String theFarmUnitPrice, String theFarmROI) {

        long theFixedPricePerUnit = Long.parseLong(theFarmUnitPrice);
        int theFixedRoi = Integer.parseInt(theFarmROI);

        /*---   CALCULATION   ---*/
        long theCalculatedPrice = theFixedPricePerUnit * unitNumberText;
        long totalCalculation = theCalculatedPrice * theFixedRoi / 100;
        long totalResult = totalCalculation + theCalculatedPrice;

        Map<String, Object> cartMap = new HashMap<>();
        cartMap.put("totalPrice", theCalculatedPrice);
        cartMap.put("totalPayout", totalResult);
        cartMap.put("farmId", farmId);
        cartMap.put("units", unitNumberText);

        cartRef.child(currentUid)
                .push()
                .setValue(cartMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void followFarm() {

        followedRef.child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child(farmId).exists()){

                            Common.showErrorDialog(FarmDetails.this, "You are already following this farm !");

                        } else {

                            final long date = System.currentTimeMillis();
                            final SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yy");
                            final String dateString = sdf.format(date);

                            Map<String, Object> followedMap = new HashMap<>();
                            followedMap.put("dateFollowed", dateString);

                            followedRef.child(currentUid)
                                    .child(farmId)
                                    .setValue(followedMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(FarmDetails.this, "Followed", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void calculateChanges(int unitNumberText, String theFarmUnitPrice, String theFarmRoi) {

        long theFixedPricePerUnit = Long.parseLong(theFarmUnitPrice);
        int theFixedRoi = Integer.parseInt(theFarmRoi);

        /*---   CALCULATION   ---*/
        long theCalculatedPrice = theFixedPricePerUnit * unitNumberText;
        long totalCalculation = theCalculatedPrice * theFixedRoi / 100;
        long totalResult = totalCalculation + theCalculatedPrice;

        totalPay.setText(Common.convertToPrice(FarmDetails.this, totalResult));
    }
}
