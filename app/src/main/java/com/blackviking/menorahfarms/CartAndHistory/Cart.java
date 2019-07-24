package com.blackviking.menorahfarms.CartAndHistory;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.CartModel;
import com.blackviking.menorahfarms.Notification.APIService;
import com.blackviking.menorahfarms.Notification.DataMessage;
import com.blackviking.menorahfarms.Notification.MyResponse;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.ViewHolders.CartViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class Cart extends AppCompatActivity {

    private ImageView backButton;
    private LinearLayout emptyLayout;
    private RecyclerView cartRecycler;
    private FirebaseRecyclerAdapter<CartModel, CartViewHolder> adapter;
    private LinearLayoutManager layoutManager;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference cartRef, userRef, farmRef, sponsorshipRef, payHistoryRef, termsRef, notificationRef;
    private String currentuid;
    private String userFirstName, userLastName, userEmail, paymentReference;

    private String currentFarmType = "", currentFarmRoi = "", currentUnitPrice = "", currentDuration = "", currentFarmId = "", currentCartKey = "";
    private String todayString = "", futureString = "";
    private long totalPrice = 0, currentTotalPayout = 0;
    private int currentUnits = 0;
    private String publicKey = "FLWPUBK-bb6cf62f9e07f4b7f1028699eaa58873-X";
    private String encryptionKey = "0b17c443fa8aba1186a42910";
    private APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);


        /*---   FIREBASE   ---*/
        cartRef = db.getReference("Carts");
        userRef = db.getReference("Users");
        farmRef = db.getReference("Farms");
        sponsorshipRef = db.getReference("SponsoredFarms");
        payHistoryRef = db.getReference("TransactionHistory");
        termsRef = db.getReference("TermsAndConditions");
        notificationRef = db.getReference("Notifications");
        if (mAuth.getCurrentUser() != null)
            currentuid = mAuth.getCurrentUser().getUid();


        /*---   FCM   ---*/
        mService = Common.getFCMService();


        /*---   WIDGETS   ---*/
        backButton = (ImageView)findViewById(R.id.backButton);
        emptyLayout = (LinearLayout)findViewById(R.id.emptyLayout);
        cartRecycler = (RecyclerView)findViewById(R.id.cartRecycler);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        /*---   CHECK IF USER CART EMPTY   ---*/
        cartRef.child(currentuid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            emptyLayout.setVisibility(View.GONE);
                            loadCart();

                        } else {

                            emptyLayout.setVisibility(View.VISIBLE);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        /*---   USER INFO   ---*/
        userRef.child(currentuid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        userFirstName = dataSnapshot.child("firstName").getValue().toString();
                        userLastName = dataSnapshot.child("lastName").getValue().toString();
                        userEmail = dataSnapshot.child("email").getValue().toString();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void loadCart() {

        cartRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        cartRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<CartModel, CartViewHolder>(
                CartModel.class,
                R.layout.cart_item,
                CartViewHolder.class,
                cartRef.child(currentuid)
        ) {
            @Override
            protected void populateViewHolder(final CartViewHolder viewHolder, final CartModel model, int position) {

                viewHolder.cartItemUnits.setText("X " + String.valueOf(model.getUnits()));

                farmRef.child(model.getFarmId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                final String theFarmType = dataSnapshot.child("farmType").getValue().toString();
                                String theFarmLocation = dataSnapshot.child("farmLocation").getValue().toString();
                                final String theFarmROI = dataSnapshot.child("farmRoi").getValue().toString();
                                final String theFarmUnitPrice = dataSnapshot.child("pricePerUnit").getValue().toString();
                                final String theFarmSponsorDuration = dataSnapshot.child("sponsorDuration").getValue().toString();
                                final String theFarmImage = dataSnapshot.child("farmImageThumb").getValue().toString();

                                long priceToLong = Long.parseLong(theFarmUnitPrice);

                                viewHolder.cartItemType.setText(theFarmType);
                                viewHolder.cartItemLocation.setText(theFarmLocation);
                                viewHolder.cartItemROI.setText("Return on investment: " + theFarmROI + "%");
                                viewHolder.cartItemDuration.setText("Duration: " + theFarmSponsorDuration + " Months");
                                viewHolder.cartItemPrice.setText(Common.convertToPrice(Cart.this, priceToLong));

                                if (!theFarmImage.equalsIgnoreCase("")){

                                    Picasso.get()
                                            .load(theFarmImage)
                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                            .placeholder(R.drawable.menorah_placeholder)
                                            .into(viewHolder.cartItemImage, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    Picasso.get()
                                                            .load(theFarmImage)
                                                            .placeholder(R.drawable.menorah_placeholder)
                                                            .into(viewHolder.cartItemImage);
                                                }

                                            });

                                } else {

                                    viewHolder.cartItemImage.setImageResource(R.drawable.menorah_placeholder);

                                }


                                viewHolder.removeFromCart.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (Common.isConnectedToInternet(getBaseContext())) {
                                            openDeleteDialog(adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                                        } else {
                                            Common.showErrorDialog(Cart.this, "No Internet Access !", Cart.this);
                                        }
                                    }
                                });


                                viewHolder.checkout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (Common.isConnectedToInternet(getBaseContext())) {

                                            final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(Cart.this).create();
                                            LayoutInflater inflater = Cart.this.getLayoutInflater();
                                            View viewOptions = inflater.inflate(R.layout.terms_layout,null);

                                            final TextView termsText = (TextView) viewOptions.findViewById(R.id.termsText);
                                            final Button cancel = (Button) viewOptions.findViewById(R.id.cancelCheckout);
                                            final Button proceed = (Button) viewOptions.findViewById(R.id.proceedCheckout);

                                            alertDialog.setView(viewOptions);

                                            alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
                                            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                                            termsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    String theTerms = dataSnapshot.child("terms").getValue().toString();

                                                    termsText.setText(theTerms);

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                            proceed.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    alertDialog.dismiss();
                                                    checkoutAndPay(adapter.getRef(viewHolder.getAdapterPosition()).getKey(), theFarmType, theFarmROI, theFarmUnitPrice, theFarmSponsorDuration, model.getTotalPayout(), model.getUnits(), model.getFarmId());
                                                }
                                            });

                                            cancel.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    alertDialog.cancel();
                                                }
                                            });

                                            alertDialog.show();

                                        } else {
                                            Common.showErrorDialog(Cart.this, "No Internet Access !", Cart.this);
                                        }
                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            }
        };
        cartRecycler.setAdapter(adapter);

    }

    private void checkoutAndPay(String theCartKey, final String theFarmType, String theFarmROI, String theFarmUnitPrice, String theFarmSponsorDuration, long totalPayout, final int units, String farmId) {


        /*---   GLOBAL SET   ---*/
        currentFarmType = theFarmType;
        currentFarmRoi = theFarmROI;
        currentUnitPrice = theFarmUnitPrice;
        currentDuration = theFarmSponsorDuration;
        currentFarmId = farmId;
        currentTotalPayout = totalPayout;
        currentUnits = units;
        currentCartKey = theCartKey;


        paymentReference = userEmail + "_BVS_" + System.currentTimeMillis();

        /*---   PRICE TO PAY   ---*/
        int thePrice = Integer.parseInt(theFarmUnitPrice);
        totalPrice = thePrice * units;


        /*---   DURATION   ---*/
        int theDuration = Integer.parseInt(theFarmSponsorDuration);
        int theDays = 30 * theDuration;

        Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        todayString = formatter.format(todayDate);

        Calendar cal = Calendar.getInstance();
        cal.setTime(todayDate);
        cal.add(Calendar.DATE, theDays);
        Date futureDate = cal.getTime();
        SimpleDateFormat formatterFuture = new SimpleDateFormat("dd-MM-yyyy");
        futureString = formatterFuture.format(futureDate);


        farmRef.child(farmId)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                String unitsAvailable = dataSnapshot.child("unitsAvailable").getValue().toString();

                                int unitsLeft = Integer.parseInt(unitsAvailable);

                                if (units <= unitsLeft){

                                    /*---   PAYMENT   ---*/
                                    new RavePayManager(Cart.this).setAmount(totalPrice)
                                            .setCountry("NG")
                                            .setCurrency("NGN")
                                            .setEmail(userEmail)
                                            .setfName(userFirstName)
                                            .setlName(userLastName)
                                            .setNarration("Sponsorship Payment For " + String.valueOf(units) + " Units Of " + theFarmType + " Farm, On This Day " + todayString + ".")
                                            .setPublicKey(publicKey)
                                            .setEncryptionKey(encryptionKey)
                                            .setTxRef(paymentReference)
                                            .acceptAccountPayments(false)
                                            .acceptCardPayments(true)
                                            .onStagingEnv(false)
                                            .isPreAuth(true)
                                            .shouldDisplayFee(true)
                                            .showStagingLabel(false)
                                            .initialize();

                                } else {

                                    Common.showErrorDialog(Cart.this, "Units Left For Sale On Farm Can Not Serve Your Request. Please Go Back To The Farm Store To Make Other Requests", Cart.this);

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            String message = data.getStringExtra("response");

            if (resultCode == RavePayActivity.RESULT_SUCCESS) {

                pushToDb();

            } else if (resultCode == RavePayActivity.RESULT_ERROR) {

                Toast.makeText(this, "ERROR " + message, Toast.LENGTH_SHORT).show();

            } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {

                Toast.makeText(this, "CANCELLED " + message, Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void pushToDb() {


        farmRef.child(currentFarmId)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                String unitsAvailable = dataSnapshot.child("unitsAvailable").getValue().toString();

                                int unitsLeft = Integer.parseInt(unitsAvailable);

                                int remainingUnit = unitsLeft - currentUnits;

                                farmRef.child(currentFarmId).child("unitsAvailable").setValue(String.valueOf(remainingUnit));

                                if (remainingUnit == 0){
                                    farmRef.child(currentFarmId).child("farmState").setValue("Sold Out");
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );


        Map<String, Object> sponsorshipMap = new HashMap<>();
        sponsorshipMap.put("sponsorReturn", String.valueOf(currentTotalPayout));
        sponsorshipMap.put("cycleEndDate", futureString);
        sponsorshipMap.put("cycleStartDate", todayString);
        sponsorshipMap.put("sponsorRefNumber", paymentReference);
        sponsorshipMap.put("unitPrice", currentUnitPrice);
        sponsorshipMap.put("sponsoredUnits", String.valueOf(currentUnits));
        sponsorshipMap.put("sponsoredFarmType", currentFarmType);
        sponsorshipMap.put("sponsoredFarmRoi", currentFarmRoi);
        sponsorshipMap.put("sponsorshipDuration", currentDuration);
        sponsorshipMap.put("startPoint", ServerValue.TIMESTAMP);
        sponsorshipMap.put("totalAmountPaid", totalPrice);
        sponsorshipMap.put("farmId", currentFarmId);

        final Map<String, Object> logMap = new HashMap<>();
        logMap.put("userName", userFirstName + " " + userLastName);
        logMap.put("userEmail", userEmail);
        logMap.put("paymentRef", paymentReference);
        logMap.put("paymentDate", todayString);
        logMap.put("farmSponsored", currentFarmId);

        sponsorshipRef.child(currentuid)
                .push()
                .setValue(sponsorshipMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        payHistoryRef.child(currentuid)
                                .push()
                                .setValue(logMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        cartRef.child(currentuid).child(currentCartKey).removeValue();
                                        sendNotification();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });

    }

    private void sendNotification() {

        final Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy  hh:mm");
        String todayString = formatter.format(todayDate);

        final Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("topic", "Sponsorship Start");
        notificationMap.put("message", "You have successfully sponsored a farm, feel free to look around for more sponsorship opportunities.");
        notificationMap.put("time", todayString);

        notificationRef.child(currentuid)
                .push()
                .setValue(notificationMap)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Map<String, String> dataSend = new HashMap<>();
                                dataSend.put("title", "Sponsorship Start");
                                dataSend.put("message", "You have successfully sponsored a farm, feel free to look around for more sponsorship opportunities.");
                                DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(currentuid).toString(), dataSend);

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
                        }
                ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void openDeleteDialog(final String key) {

        AlertDialog alertDialog = new AlertDialog.Builder(Cart.this)
                .setTitle("Delete Update !")
                .setIcon(R.drawable.ic_remove_from_cart)
                .setMessage("Are You Sure You Want To Remove This Item From Your Cart?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        cartRef.child(currentuid)
                                .child(key)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Toast.makeText(Cart.this, "Item Removed From cart !", Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.show();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
