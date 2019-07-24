package com.blackviking.menorahfarms;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

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
    private TextView unitNumber, farmDescription;
    private RelativeLayout followFarmBtn, addToCartBtn, followedFarmButton;
    private LinearLayout farmNumber;

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
        farmDescription = (TextView)findViewById(R.id.farmDescription);
        followFarmBtn = (RelativeLayout)findViewById(R.id.followFarmButton);
        addToCartBtn = (RelativeLayout)findViewById(R.id.addToCartButton);
        followedFarmButton = (RelativeLayout)findViewById(R.id.followedFarmButton);
        decreaseUnitNumber = (ImageView)findViewById(R.id.decreaseUnitNumber);
        increaseUnitNumber = (ImageView)findViewById(R.id.increaseUnitNumber);
        unitNumber = (TextView)findViewById(R.id.unitNumber);
        unitNumber.setText(String.valueOf(unitNumberText));
        farmNumber = (LinearLayout)findViewById(R.id.farmNumber);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        followedRef.child(currentUid)
                .child(farmId)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){

                                    followedFarmButton.setVisibility(View.VISIBLE);
                                    followFarmBtn.setVisibility(View.GONE);

                                } else {

                                    followedFarmButton.setVisibility(View.GONE);
                                    followFarmBtn.setVisibility(View.VISIBLE);

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );


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
                                final String theFarmImage = dataSnapshot.child("farmImage").getValue().toString();
                                final String theFarmState = dataSnapshot.child("farmState").getValue().toString();

                                if (theFarmState.equalsIgnoreCase("Now Selling")){

                                    addToCartBtn.setVisibility(View.VISIBLE);
                                    farmNumber.setVisibility(View.VISIBLE);

                                } else {

                                    addToCartBtn.setVisibility(View.GONE);
                                    farmNumber.setVisibility(View.GONE);

                                }

                                if (!theFarmImage.equalsIgnoreCase("")){

                                    Picasso.get()
                                            .load(theFarmImage)
                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                            .placeholder(R.drawable.menorah_placeholder)
                                            .into(farmImage, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    Picasso.get()
                                                            .load(theFarmImage)
                                                            .placeholder(R.drawable.menorah_placeholder)
                                                            .into(farmImage);
                                                }
                                            });

                                }


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

                                final int unitsAvail = Integer.parseInt(theFarmUnitsLeft);

                                if (unitsAvail == 0){

                                    addToCartBtn.setVisibility(View.GONE);
                                    farmNumber.setVisibility(View.GONE);

                                } else {

                                    addToCartBtn.setVisibility(View.VISIBLE);
                                    farmNumber.setVisibility(View.VISIBLE);

                                }

                                /*---   UNIT NUMBERS   ---*/
                                increaseUnitNumber.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        if (unitNumberText < unitsAvail) {
                                            unitNumberText++;
                                            unitNumber.setText(String.valueOf(unitNumberText));

                                            calculateChanges(unitNumberText, theFarmUnitPrice, theFarmROI);

                                        }
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


                                farmDescription.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent farmDescIntent = new Intent(FarmDetails.this, FarmDescription.class);
                                        farmDescIntent.putExtra("FarmId", farmId);
                                        startActivity(farmDescIntent);
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
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

                                        if (Common.isConnectedToInternet(getBaseContext())) {

                                            addToCart(unitNumberText, theFarmUnitPrice, theFarmROI);

                                        } else {

                                            Common.showErrorDialog(FarmDetails.this, "No Internet Access", FarmDetails.this);

                                        }

                                    }
                                });


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

    }

    private void addToCart(final int unitNumberText, final String theFarmUnitPrice, final String theFarmROI) {

        cartRef.child(currentUid)
                .orderByChild("farmId")
                .equalTo(farmId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (!dataSnapshot.exists()){

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
                                            showAffirmDialog();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });

                        } else {

                            Common.showErrorDialog(FarmDetails.this, "This farm has already been added to your cart.", FarmDetails.this);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void showAffirmDialog() {

        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(FarmDetails.this).create();
        LayoutInflater inflater = FarmDetails.this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.dialog_layout,null);

        final TextView message = (TextView) viewOptions.findViewById(R.id.dialogMessage);
        final Button okButton = (Button) viewOptions.findViewById(R.id.dialogButton);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        message.setText("Added to cart successfully");

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                finish();
            }
        });

        alertDialog.show();

    }

    private void followFarm() {

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

                        followFarmBtn.setVisibility(View.GONE);
                        followedFarmButton.setVisibility(View.VISIBLE);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

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

        unitPrice.setText(Common.convertToPrice(FarmDetails.this, theCalculatedPrice));
        totalPay.setText(Common.convertToPrice(FarmDetails.this, totalResult));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
