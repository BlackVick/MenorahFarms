package com.blackviking.menorahfarms;

import android.content.DialogInterface;
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

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.FarmModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class FarmDetails extends AppCompatActivity {

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef, farmRef, followedRef, cartRef, followedFarmNotiRef, sponsorshipRef;
    private String currentUid, userType, farmId, farmNotiId;

    private ImageView backButton, farmImage;
    private TextView farmType, unitsLeft, farmLocation, farmROI, unitPrice, totalROI, totalDuration, totalPay;
    private ImageView decreaseUnitNumber, increaseUnitNumber;
    private TextView unitNumber, farmDescription;
    private RelativeLayout followFarmBtn, addToCartBtn, followedFarmButton;
    private LinearLayout farmNumber;

    private int unitNumberText = 1;

    private android.app.AlertDialog alertDialog;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farm_details);


        /*---   INTENT DATA   ---*/
        farmId = getIntent().getStringExtra("FarmId");


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        farmRef = db.getReference("Farms");
        sponsorshipRef = db.getReference("SponsoredFarms");
        followedRef = db.getReference("FollowedFarms");
        cartRef = db.getReference("Carts");
        followedFarmNotiRef = db.getReference("FollowedFarmsNotification");
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


        //show loading dialog
        showLoadingDialog("Loading farm details . . .");

        //execute network check async task
        new CheckInternet(this, new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

                //check all cases
                if (output == 1){

                    loadCurrentFarm();

                } else

                if (output == 0){

                    //set layout
                    alertDialog.dismiss();
                    Toast.makeText(FarmDetails.this, "No internet connection", Toast.LENGTH_SHORT).show();

                } else

                if (output == 2){

                    //set layout
                    alertDialog.dismiss();
                    Toast.makeText(FarmDetails.this, "No network detected", Toast.LENGTH_SHORT).show();

                }

            }
        }).execute();


    }

    private void checkFollowedFarmFollowed() {

        //remove dialog
        alertDialog.dismiss();

        followedRef.child(currentUid)
                .child(farmId)
                .addValueEventListener(
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

    }

    private void loadCurrentFarm() {

        farmRef.child(farmId)
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                final FarmModel currentFarm = dataSnapshot.getValue(FarmModel.class);

                                if (currentFarm != null){

                                    String theFarmType = currentFarm.getFarmType();
                                    String theFarmLocation = currentFarm.getFarmLocation();
                                    final String theFarmROI = currentFarm.getFarmRoi();
                                    final String theFarmUnitPrice = currentFarm.getPricePerUnit();
                                    String theFarmSponsorDuration = currentFarm.getSponsorDuration();
                                    final String theFarmImage = currentFarm.getFarmImageThumb();
                                    final String theFarmState = currentFarm.getFarmState();
                                    farmNotiId = currentFarm.getFarmNotiId();

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
                                    unitsLeft.setText(currentFarm.getUnitsAvailable());
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

                                    if (!theFarmState.equalsIgnoreCase("Now Selling")){

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

                                            if (currentFarm.getPackagedType().equalsIgnoreCase("Student")){

                                                if (unitNumberText < 10) {
                                                    unitNumberText++;
                                                    unitNumber.setText(String.valueOf(unitNumberText));

                                                    calculateChanges(unitNumberText, theFarmUnitPrice, theFarmROI);

                                                } else {

                                                    showErrorDialog("You can not exceed sponsorship limit for this package.");

                                                }

                                            }

                                            if (currentFarm.getPackagedType().equalsIgnoreCase("Worker")){

                                                if (unitNumberText < 100) {
                                                    unitNumberText++;
                                                    unitNumber.setText(String.valueOf(unitNumberText));

                                                    calculateChanges(unitNumberText, theFarmUnitPrice, theFarmROI);

                                                } else {

                                                    showErrorDialog("You can not exceed sponsorship limit for this package");

                                                }

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

                                    //follow check
                                    checkFollowedFarmFollowed();


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

                                            //execute network check async task
                                            CheckInternet asyncTask = (CheckInternet) new CheckInternet(FarmDetails.this, new CheckInternet.AsyncResponse(){
                                                @Override
                                                public void processFinish(Integer output) {

                                                    //check all cases
                                                    if (output == 1){


                                                        sponsorshipRef.child(currentUid)
                                                                .orderByChild("farmId")
                                                                .equalTo(farmId)
                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                                        int theCount = 0;

                                                                        for (DataSnapshot snap : dataSnapshot.getChildren()){

                                                                            int newCount = Integer.parseInt(snap.child("sponsoredUnits").getValue().toString());

                                                                            theCount = theCount + newCount;

                                                                        }

                                                                        if (currentFarm.getPackagedType().equalsIgnoreCase("Worker")){

                                                                            int spaceRemaining = 100 - theCount;

                                                                            if (theCount < 100 && unitNumberText <= spaceRemaining){

                                                                                addToCart(unitNumberText, theFarmUnitPrice, theFarmROI);

                                                                            } else {

                                                                                showErrorDialog("You can not exceed sponsorship limit of 100 for this package");

                                                                            }

                                                                        } else

                                                                        if (currentFarm.getPackagedType().equalsIgnoreCase("Student")){

                                                                            int spaceRemaining = 10 - theCount;

                                                                            if (theCount < 10 && unitNumberText <= spaceRemaining){

                                                                                addToCart(unitNumberText, theFarmUnitPrice, theFarmROI);

                                                                            } else {

                                                                                showErrorDialog("You can not exceed sponsorship limit of 10 for this package");

                                                                            }

                                                                        }



                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });

                                                    } else

                                                    if (output == 0){

                                                        //set layout
                                                        alertDialog.dismiss();
                                                        Toast.makeText(FarmDetails.this, "No internet connection", Toast.LENGTH_SHORT).show();

                                                    } else

                                                    if (output == 2){

                                                        //set layout
                                                        alertDialog.dismiss();
                                                        Toast.makeText(FarmDetails.this, "No network detected", Toast.LENGTH_SHORT).show();

                                                    }

                                                }
                                            }).execute();

                                        }
                                    });

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

    }

    private void addToCart(final int unitNumberText, final String theFarmUnitPrice, final String theFarmROI) {

        if (Common.checkKYC(FarmDetails.this).equalsIgnoreCase("Profile Complete")) {

            cartRef.child(currentUid)
                    .orderByChild("farmId")
                    .equalTo(farmId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (!dataSnapshot.exists()) {

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
                                                Toast.makeText(FarmDetails.this, "Added to cart", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });

                            } else {

                                showErrorDialog("This farm has already been added to your cart.");

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

        } else {

            showErrorDialog(Common.checkKYC(FarmDetails.this));

        }

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

                        subscribeToNotification(farmId);

                        followFarmBtn.setVisibility(View.GONE);
                        followedFarmButton.setVisibility(View.VISIBLE);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void subscribeToNotification(String farmId) {

        farmRef.child(farmId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        FarmModel theFarm = dataSnapshot.getValue(FarmModel.class);

                        if (theFarm != null){

                            followedFarmNotiRef.child(theFarm.getFarmNotiId())
                                    .child(currentUid)
                                    .child("timestamp")
                                    .setValue(ServerValue.TIMESTAMP);

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

        unitPrice.setText(Common.convertToPrice(FarmDetails.this, theCalculatedPrice));
        totalPay.setText(Common.convertToPrice(FarmDetails.this, totalResult));
    }

    @Override
    public void onBackPressed() {
        if (isLoading){
            alertDialog.dismiss();
        }
        finish();
    }

    /*---   LOADING DIALOG   ---*/
    public void showLoadingDialog(String theMessage){

        //loading
        isLoading = true;

        alertDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.loading_dialog,null);

        final TextView loadingText = viewOptions.findViewById(R.id.loadingText);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        loadingText.setText(theMessage);

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                isLoading = false;
            }
        });
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isLoading = false;
            }
        });

        alertDialog.show();

    }

    /*---   WARNING DIALOG   ---*/
    public void showErrorDialog(String theWarning){

        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.dialog_layout,null);

        final TextView message = (TextView) viewOptions.findViewById(R.id.dialogMessage);
        final Button okButton = (Button) viewOptions.findViewById(R.id.dialogButton);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        message.setText(theWarning);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }
}
