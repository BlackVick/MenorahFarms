package com.blackviking.menorahfarms.CartAndHistory;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.blackviking.menorahfarms.Models.CartModel;
import com.blackviking.menorahfarms.Models.FarmModel;
import com.blackviking.menorahfarms.Models.SponsorshipDetailsModel;
import com.blackviking.menorahfarms.Models.UserModel;
import com.blackviking.menorahfarms.Notification.APIService;
import com.blackviking.menorahfarms.Notification.DataMessage;
import com.blackviking.menorahfarms.Notification.MyResponse;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.ViewHolders.CartViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;
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
import java.util.Random;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Response;

public class Cart extends AppCompatActivity {

    //widget
    private ImageView backButton;
    private LinearLayout emptyLayout;
    private RecyclerView cartRecycler;
    private FirebaseRecyclerAdapter<CartModel, CartViewHolder> adapter;
    private LinearLayoutManager layoutManager;
    private RelativeLayout noInternetLayout;

    //firebase
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference cartRef, userRef, farmRef, sponsorshipRef, payHistoryRef, termsRef, runningCycleRef,
            notificationRef, sponsorshipDetailsRef, sponsorshipNotificationRef;

    //values
    private String currentuid;
    private String paymentReference;

    //data values
    private String currentFarmType = "", currentFarmRoi = "", currentUnitPrice = "",
            currentDuration = "", currentFarmId = "", currentCartKey = "";
    private String todayString = "", futureString = "";
    private long totalPrice = 0, currentTotalPayout = 0;
    private int currentUnits = 0;
    private UserModel paperUser;

    //notification
    private APIService mService;

    //loading
    private android.app.AlertDialog loadingDialog;
    private boolean isLoading = false;

    //random farm manager
    private static final String[] CHARS = {"01", "02"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);


        /*---   FIREBASE   ---*/
        cartRef = db.getReference(Common.CART_NODE);
        userRef = db.getReference(Common.USERS_NODE);
        farmRef = db.getReference(Common.FARM_NODE);
        sponsorshipRef = db.getReference(Common.SPONSORED_FARMS_NODE);
        sponsorshipNotificationRef = db.getReference(Common.SPONSORED_FARMS_NOTIFICATION_NODE);
        payHistoryRef = db.getReference(Common.TRANSACTION_NODE);
        termsRef = db.getReference(Common.TERMS_AND_CONDITIONS_NODE);
        notificationRef = db.getReference(Common.NOTIFICATIONS_NODE);
        runningCycleRef = db.getReference(Common.RUNNING_CYCLE_NODE);
        sponsorshipDetailsRef = db.getReference(Common.SPONSORSHIP_DETAILS_NODE);
        if (mAuth.getCurrentUser() != null)
            currentuid = mAuth.getCurrentUser().getUid();


        //set paper user
        paperUser = Paper.book().read(Common.PAPER_USER);


        /*---   FCM   ---*/
        mService = Common.getFCMService();


        /*---   WIDGETS   ---*/
        backButton = findViewById(R.id.backButton);
        emptyLayout = findViewById(R.id.emptyLayout);
        cartRecycler = findViewById(R.id.cartRecycler);
        noInternetLayout = findViewById(R.id.noInternetLayout);


        //show loading dialog
        showLoadingDialog("Loading cart items . . .");


        //run network check
        new CheckInternet(this, output -> {

            //check all cases
            if (output == 1){

                /*---   CHECK IF USER CART EMPTY   ---*/
                cartRef.child(currentuid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){

                                    cartRecycler.setVisibility(View.VISIBLE);
                                    emptyLayout.setVisibility(View.GONE);
                                    noInternetLayout.setVisibility(View.GONE);
                                    loadCart();

                                } else {

                                    loadingDialog.dismiss();
                                    cartRecycler.setVisibility(View.GONE);
                                    emptyLayout.setVisibility(View.VISIBLE);
                                    noInternetLayout.setVisibility(View.GONE);

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            } else

            if (output == 0){

                //set layout
                loadingDialog.dismiss();
                noInternetLayout.setVisibility(View.VISIBLE);
                cartRecycler.setVisibility(View.GONE);
                emptyLayout.setVisibility(View.GONE);

            } else

            if (output == 2){

                //set layout
                loadingDialog.dismiss();
                noInternetLayout.setVisibility(View.VISIBLE);
                cartRecycler.setVisibility(View.GONE);
                emptyLayout.setVisibility(View.GONE);

            }

        }).execute();


        backButton.setOnClickListener(v -> finish());

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

                loadingDialog.dismiss();
                viewHolder.cartItemUnits.setText("X " + model.getUnits());

                farmRef.child(model.getFarmId())
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                final FarmModel currentFarm = dataSnapshot.getValue(FarmModel.class);

                                if (currentFarm != null){

                                    final String theFarmType = currentFarm.getFarmType();
                                    final String theFarmLocation = currentFarm.getFarmLocation();
                                    final String theFarmROI = currentFarm.getFarmRoi();
                                    final String theFarmUnitPrice = currentFarm.getPricePerUnit();
                                    final String theFarmSponsorDuration = currentFarm.getSponsorDuration();
                                    final String theFarmImage = currentFarm.getFarmImageThumb();
                                    final String theFarmState = currentFarm.getFarmState();

                                    long priceToLong = Long.parseLong(theFarmUnitPrice);

                                    //farm details
                                    viewHolder.cartItemType.setText(theFarmType);
                                    viewHolder.cartItemLocation.setText(theFarmLocation);
                                    viewHolder.cartItemROI.setText("Return on investment: " + theFarmROI + "%");
                                    viewHolder.cartItemDuration.setText("Duration: " + theFarmSponsorDuration + " Months");
                                    viewHolder.cartItemPrice.setText(Common.convertToPrice(Cart.this, priceToLong));

                                    //farm image
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

                                    //remove cart item
                                    viewHolder.removeFromCart.setOnClickListener(v -> openDeleteDialog(adapter.getRef(viewHolder.getAdapterPosition()).getKey()));

                                    //checkout
                                    viewHolder.checkout.setOnClickListener(v -> {

                                        if (Common.checkKYC(Cart.this).equalsIgnoreCase("Profile Complete")) {

                                            //run network check
                                            new CheckInternet(Cart.this, output -> {

                                                //check all cases
                                                if (output == 1) {

                                                    //check for user sponsorship limit
                                                    sponsorshipRef.child(currentuid)
                                                            .orderByChild("farmId")
                                                            .equalTo(model.getFarmId())
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot1) {

                                                                    int theCount = 0;

                                                                    for (DataSnapshot snap : dataSnapshot1.getChildren()){

                                                                        int newCount = Integer.parseInt(snap.child("sponsoredUnits").getValue().toString());

                                                                        theCount = theCount + newCount;

                                                                    }

                                                                    if (currentFarm.getPackagedType().equalsIgnoreCase("Worker")){

                                                                        int spaceRemaining = 100 - theCount;

                                                                        if (theCount < 100 && model.getUnits() <= spaceRemaining){

                                                                            final AlertDialog alertDialog = new AlertDialog.Builder(Cart.this, R.style.DialogTheme).create();
                                                                            LayoutInflater inflater = Cart.this.getLayoutInflater();
                                                                            View viewOptions = inflater.inflate(R.layout.terms_layout, null);

                                                                            final TextView termsText = viewOptions.findViewById(R.id.termsText);
                                                                            final Button cancel = viewOptions.findViewById(R.id.cancelCheckout);
                                                                            final Button proceed = viewOptions.findViewById(R.id.proceedCheckout);

                                                                            alertDialog.setView(viewOptions);

                                                                            alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
                                                                            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                                                                            //get term from server
                                                                            termsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot dataSnapshot1) {

                                                                                    String theTerms = dataSnapshot1.child("terms").getValue().toString();

                                                                                    termsText.setText(theTerms);

                                                                                }

                                                                                @Override
                                                                                public void onCancelled(DatabaseError databaseError) {

                                                                                }
                                                                            });

                                                                            //proceed to checkout
                                                                            proceed.setOnClickListener(v1 -> {
                                                                                alertDialog.dismiss();
                                                                                checkoutAndPay(adapter.getRef(viewHolder.getAdapterPosition()).getKey(), theFarmType, theFarmROI, theFarmUnitPrice, theFarmSponsorDuration, model.getTotalPayout(), model.getUnits(), model.getFarmId());
                                                                            });

                                                                            //cancel checkout
                                                                            cancel.setOnClickListener(v1 -> alertDialog.cancel());

                                                                            alertDialog.show();

                                                                        } else {

                                                                            Toast.makeText(Cart.this, "Limit exceeded!", Toast.LENGTH_LONG).show();

                                                                        }

                                                                    } else

                                                                    if (currentFarm.getPackagedType().equalsIgnoreCase("Student")){

                                                                        int spaceRemaining = 10 - theCount;

                                                                        if (theCount < 10 && model.getUnits() <= spaceRemaining){

                                                                            final AlertDialog alertDialog = new AlertDialog.Builder(Cart.this, R.style.DialogTheme).create();
                                                                            LayoutInflater inflater = Cart.this.getLayoutInflater();
                                                                            View viewOptions = inflater.inflate(R.layout.terms_layout, null);

                                                                            final TextView termsText = viewOptions.findViewById(R.id.termsText);
                                                                            final Button cancel = viewOptions.findViewById(R.id.cancelCheckout);
                                                                            final Button proceed = viewOptions.findViewById(R.id.proceedCheckout);

                                                                            alertDialog.setView(viewOptions);

                                                                            alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
                                                                            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                                                                            //get term and condition for the sponsorship
                                                                            termsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot dataSnapshot1) {

                                                                                    String theTerms = dataSnapshot1.child("terms").getValue().toString();

                                                                                    termsText.setText(theTerms);

                                                                                }

                                                                                @Override
                                                                                public void onCancelled(DatabaseError databaseError) {

                                                                                }
                                                                            });

                                                                            //proceed to checkout
                                                                            proceed.setOnClickListener(v1 -> {
                                                                                alertDialog.dismiss();
                                                                                checkoutAndPay(adapter.getRef(viewHolder.getAdapterPosition()).getKey(), theFarmType, theFarmROI, theFarmUnitPrice, theFarmSponsorDuration, model.getTotalPayout(), model.getUnits(), model.getFarmId());
                                                                            });

                                                                            //cancel checkout
                                                                            cancel.setOnClickListener(v1 -> alertDialog.cancel());

                                                                            alertDialog.show();

                                                                        } else {

                                                                            Toast.makeText(Cart.this, "Limit exceeded!", Toast.LENGTH_LONG).show();

                                                                        }

                                                                    }



                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {

                                                                }
                                                            });

                                                } else if (output == 0) {

                                                    Toast.makeText(Cart.this, "No internet access", Toast.LENGTH_SHORT).show();

                                                } else if (output == 2) {

                                                    Toast.makeText(Cart.this, "No network access", Toast.LENGTH_SHORT).show();

                                                }

                                            }).execute();

                                        } else {

                                            Toast.makeText(Cart.this, Common.checkKYC(Cart.this), Toast.LENGTH_LONG).show();

                                        }


                                    });

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            }
        };
        cartRecycler.setAdapter(adapter);

    }



    //FlutterWave Payment Gateway
    private void checkoutAndPay(final String theCartKey, final String theFarmType, final String theFarmROI, final String theFarmUnitPrice, final String theFarmSponsorDuration, final long totalPayout, final int units, final String farmId) {

        //run network check
        new CheckInternet(this, output -> {

            //check all cases
            if (output == 1){

                //get user
                final UserModel user1 = Paper.book().read(Common.PAPER_USER);

                /*---   GLOBAL SET   ---*/
                currentFarmType = theFarmType;
                currentFarmRoi = theFarmROI;
                currentUnitPrice = theFarmUnitPrice;
                currentDuration = theFarmSponsorDuration;
                currentFarmId = farmId;
                currentTotalPayout = totalPayout;
                currentUnits = units;
                currentCartKey = theCartKey;


                //create initials from name
                char firstInit = user1.getFirstName().charAt(0);
                char lastInit = user1.getLastName().charAt(0);
                String initials = String.valueOf(firstInit) + String.valueOf(lastInit);

                //get reference
                paymentReference = initials + "-MF-" + System.currentTimeMillis();

                //get price
                int thePrice = Integer.parseInt(theFarmUnitPrice);
                totalPrice = thePrice * units;


                //duration
                int theDuration = Integer.parseInt(theFarmSponsorDuration);
                int theDays = 30 * theDuration;

                //get today date
                Date todayDate = Calendar.getInstance().getTime();
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                todayString = formatter.format(todayDate);

                //get end date
                Calendar cal = Calendar.getInstance();
                cal.setTime(todayDate);
                cal.add(Calendar.DATE, theDays);
                Date futureDate = cal.getTime();
                SimpleDateFormat formatterFuture = new SimpleDateFormat("dd-MM-yyyy");
                futureString = formatterFuture.format(futureDate);


                //get farm state
                farmRef.child(farmId)
                        .addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        FarmModel laFarm = dataSnapshot.getValue(FarmModel.class);

                                        if (laFarm != null){

                                            if (laFarm.getFarmState().equalsIgnoreCase("Now Selling")){

                                                sponsorshipDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                        SponsorshipDetailsModel theDetails = dataSnapshot.getValue(SponsorshipDetailsModel.class);

                                                        if (theDetails != null){

                                                            if (theDetails.getSponsored_farm().equalsIgnoreCase(laFarm.getFarmNotiId())){

                                                                if (units <= theDetails.getUnits_available()){

                                                                    //initiate flutterwave payment
                                                                    new RavePayManager(Cart.this).setAmount(totalPrice)
                                                                            .setCountry("NG")
                                                                            .setCurrency("NGN")
                                                                            .setEmail(user1.getEmail())
                                                                            .setfName(user1.getFirstName())
                                                                            .setlName(user1.getLastName())
                                                                            .setNarration("Sponsorship Payment For " + units + " Units Of " + theFarmType + " Farm, On This Day " + todayString + ".")
                                                                            .setPublicKey(Common.flutterWavePublicKey)
                                                                            .setEncryptionKey(Common.flutterWaveEncryptionKey)
                                                                            .setTxRef(paymentReference)
                                                                            .acceptAccountPayments(false)
                                                                            .acceptCardPayments(true)
                                                                            .onStagingEnv(false)
                                                                            .isPreAuth(true)
                                                                            .shouldDisplayFee(true)
                                                                            .showStagingLabel(false)
                                                                            .withTheme(R.style.FlutterPayTheme)
                                                                            .initialize();

                                                                } else {

                                                                    //show error
                                                                    Toast.makeText(Cart.this, "Remaining units can't serve your request.", Toast.LENGTH_LONG).show();

                                                                }

                                                            }

                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });

                                            } else {

                                                //loading
                                                Toast.makeText(Cart.this, "Farm is sold out!", Toast.LENGTH_LONG).show();

                                            }

                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                }
                        );

            } else

            if (output == 0){

                Toast.makeText(Cart.this, "No internet access", Toast.LENGTH_SHORT).show();

            } else

            if (output == 2){

                Toast.makeText(Cart.this, "No network connection", Toast.LENGTH_SHORT).show();

            }

        }).execute();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            String message = data.getStringExtra("response");

            if (resultCode == RavePayActivity.RESULT_SUCCESS) {

                //show loading dialog
                showLoadingDialog("Authorizing payment . . .");

                //run network check
                new CheckInternet(this, output -> {

                    //check all cases
                    if (output == 1){

                        pushToDb();

                    } else

                    if (output == 0){

                        loadingDialog.dismiss();
                        Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show();

                    } else

                    if (output == 2){

                        loadingDialog.dismiss();
                        Toast.makeText(this, "Not connected to any network", Toast.LENGTH_LONG).show();

                    }

                }).execute();

            } else if (resultCode == RavePayActivity.RESULT_ERROR) {

                Toast.makeText(this, "Transaction failed", Toast.LENGTH_SHORT).show();

            } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {

                Toast.makeText(this, "Transaction cancelled", Toast.LENGTH_SHORT).show();

            }
        }

    }




    //Test Gateway
    private void testPay(String theCartKey, final String theFarmType, String theFarmROI, String theFarmUnitPrice, String theFarmSponsorDuration, long totalPayout, final int units, String farmId) {

        /*---   GLOBAL SET   ---*/
        currentFarmType = theFarmType;
        currentFarmRoi = theFarmROI;
        currentUnitPrice = theFarmUnitPrice;
        currentDuration = theFarmSponsorDuration;
        currentFarmId = farmId;
        currentTotalPayout = totalPayout;
        currentUnits = units;
        currentCartKey = theCartKey;


        final UserModel user1 = Paper.book().read(Common.PAPER_USER);

        char firstInit = user1.getFirstName().charAt(0);
        char lastInit = user1.getLastName().charAt(0);

        String initials = String.valueOf(firstInit) + String.valueOf(lastInit);

        paymentReference = initials + "-MF-" + System.currentTimeMillis();

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

        //pushToDb();

    }




    //add to database
    private void pushToDb() {

        //get user
        final UserModel user2 = Paper.book().read(Common.PAPER_USER);

        //assign farm manager
        if (paperUser.getAccountManager().equalsIgnoreCase("")) {
            userRef.child(currentuid).child("accountManager").setValue(CHARS[new Random().nextInt(CHARS.length)]);
        }

        //do calculations for details
        sponsorshipDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                SponsorshipDetailsModel spnsDets = dataSnapshot.getValue(SponsorshipDetailsModel.class);

                int uLeft = spnsDets.getUnits_available() - currentUnits;
                int uSold = spnsDets.getUnits_sold() + currentUnits;

                //update to db
                Map<String, Object> updMap = new HashMap<>();
                updMap.put("units_available", uLeft);
                updMap.put("units_sold", uSold);
                sponsorshipDetailsRef.updateChildren(updMap);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //user personal map
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
        sponsorshipMap.put("status", "sponsoring");

        //running cycle map
        Map<String, Object> runningCycleMap = new HashMap<>();
        runningCycleMap.put("sponsorReturn", String.valueOf(currentTotalPayout));
        runningCycleMap.put("cycleEndDate", futureString);
        runningCycleMap.put("cycleStartDate", todayString);
        runningCycleMap.put("sponsorRefNumber", paymentReference);
        runningCycleMap.put("unitPrice", currentUnitPrice);
        runningCycleMap.put("sponsoredUnits", String.valueOf(currentUnits));
        runningCycleMap.put("sponsoredFarmType", currentFarmType);
        runningCycleMap.put("sponsoredFarmRoi", currentFarmRoi);
        runningCycleMap.put("sponsorshipDuration", currentDuration);
        runningCycleMap.put("startPoint", ServerValue.TIMESTAMP);
        runningCycleMap.put("totalAmountPaid", totalPrice);
        runningCycleMap.put("farmId", currentFarmId);
        runningCycleMap.put("userId", currentuid);

        //transactional log
        final Map<String, Object> logMap = new HashMap<>();
        logMap.put("userName", user2.getFirstName() + " " + user2.getLastName());
        logMap.put("userEmail", user2.getEmail());
        logMap.put("paymentRef", paymentReference);
        logMap.put("paymentDate", todayString);
        logMap.put("farmSponsored", currentFarmId);
        logMap.put("paymentProof", "");

        //create general key from fb
        DatabaseReference pushRef = sponsorshipRef.push();
        String pushId = pushRef.getKey();

        //add to running sponsorships cycle
        addToRunningCycle(pushId, currentFarmId, runningCycleMap);

        //add to user sponsorship list
        sponsorshipRef.child(currentuid)
                .child(pushId)
                .setValue(sponsorshipMap)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()){

                        //remove from cart
                        cartRef.child(currentuid).child(currentCartKey).removeValue();

                        //send notification to user
                        sendApprovalNotification();

                        //add transaction log
                        payHistoryRef.child(currentuid)
                                .push()
                                .setValue(logMap);

                        //dismiss loading
                        loadingDialog.dismiss();

                    } else {

                        Toast.makeText(this, "Unknown error occurred, please contact admin immediately", Toast.LENGTH_LONG).show();

                    }

                });

    }

    private void addToRunningCycle(final String pushId, String currentFarmId, final Map<String, Object> runningCycleMap) {

        farmRef.child(currentFarmId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        FarmModel theFarm = dataSnapshot.getValue(FarmModel.class);

                        if (theFarm != null){

                            //add to running cycles
                            runningCycleRef.child(theFarm.getFarmNotiId())
                                    .child(pushId)
                                    .setValue(runningCycleMap);

                            //add to notification group
                            sponsorshipNotificationRef.child(theFarm.getFarmNotiId())
                                    .child(currentuid)
                                    .child("timestamp")
                                    .setValue(ServerValue.TIMESTAMP);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void sendApprovalNotification() {

        final Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy  hh:mm");
        String todayString = formatter.format(todayDate);

        //notification map
        final Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("topic", "Sponsorship Approved");
        notificationMap.put("message", "Your sponsorship has been approved. You can monitor your sponsored farm from the Sponsored Farms page through your dashboard.");
        notificationMap.put("time", todayString);


        notificationRef.child(currentuid)
                .push()
                .setValue(notificationMap)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()){

                        Map<String, String> dataSend = new HashMap<>();
                        dataSend.put("title", "Sponsorship Start");
                        dataSend.put("message", "Your sponsorship has been approved. You can monitor your sponsored farm from the Sponsored Farms page through your dashboard.");
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

                        //notification to admin
                        sendAlertToAdmin();

                    } else {

                        Toast.makeText(Cart.this, "Error occurred while sending notification", Toast.LENGTH_SHORT).show();

                    }

                });

    }

    private void sendAlertToAdmin() {

        userRef.orderByChild("userType")
                .equalTo("Admin")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                for (DataSnapshot snap : dataSnapshot.getChildren()){

                                    final String adminKey = snap.getKey();

                                    Map<String, String> dataSend = new HashMap<>();
                                    dataSend.put("title", "Sponsorship Start");
                                    dataSend.put("message", "A new user just sponsored a farm.");
                                    DataMessage dataMessage = new DataMessage(new StringBuilder("/topics/").append(adminKey).toString(), dataSend);

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

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

    }




    private void openDeleteDialog(final String key) {

        AlertDialog alertDialog = new AlertDialog.Builder(Cart.this)
                .setTitle("Delete Update !")
                .setIcon(R.drawable.ic_remove_from_cart)
                .setMessage("Are You Sure You Want To Remove This Item From Your Cart?")
                .setPositiveButton("YES", (dialog, which) -> {

                    //run network check
                    new CheckInternet(Cart.this, output -> {

                        //check all cases
                        if (output == 1){

                            cartRef.child(currentuid)
                                    .child(key)
                                    .removeValue()
                                    .addOnCompleteListener(task -> {

                                        if (task.isSuccessful()){

                                            Toast.makeText(Cart.this, "Item Removed From cart !", Toast.LENGTH_SHORT).show();

                                        } else {

                                            Toast.makeText(Cart.this, "Error occurred", Toast.LENGTH_SHORT).show();

                                        }

                                    });

                        } else

                        if (output == 0){

                            Toast.makeText(Cart.this, "Error occurred", Toast.LENGTH_SHORT).show();

                        } else

                        if (output == 2){

                            Toast.makeText(Cart.this, "Error occurred", Toast.LENGTH_SHORT).show();

                        }

                    }).execute();

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

    public void showLoadingDialog(String theMessage){

        //loading
        isLoading = true;

        loadingDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.loading_dialog,null);

        final TextView loadingText = viewOptions.findViewById(R.id.loadingText);

        loadingDialog.setView(viewOptions);

        loadingDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        loadingText.setText(theMessage);

        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                isLoading = false;
            }
        });
        loadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isLoading = false;
            }
        });

        loadingDialog.show();

    }

    @Override
    public void onBackPressed() {
        if (isLoading){
            loadingDialog.dismiss();
        }
        finish();
    }
}
