package com.blackviking.menorahfarms.CartAndHistory;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.CartModel;
import com.blackviking.menorahfarms.Models.FarmModel;
import com.blackviking.menorahfarms.Models.MenorahDetailsModel;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import id.zelory.compressor.Compressor;
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
            notificationRef, sponsorshipDetailsRef, sponsorshipNotificationRef, bankRef, pendingSponsorRef;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference proofRef;

    //values
    private String currentuid;
    private String paymentReference;

    //data values
    private String currentFarmType = "", currentFarmRoi = "", currentUnitPrice = "",
            currentDuration = "", currentFarmId = "", currentCartKey = "";
    private String todayString = "", futureString = "";
    private long totalPrice = 0, currentTotalPayout = 0, displayPrice = 0;
    private int currentUnits = 0;
    private UserModel paperUser;

    //notification
    private APIService mService;

    //loading
    private android.app.AlertDialog loadingDialog;
    private boolean isLoading = false;
    private android.app.AlertDialog alertDialog, alertDialog2;

    //random farm manager
    private static final String[] CHARS = {"01", "02"};

    //for permissions
    private static final int VERIFY_PERMISSIONS_REQUEST = 757;
    private static final int GALLERY_REQUEST_CODE = 665;

    //image helper
    private Uri imageUri;
    private UploadTask proofUploadTask;
    private boolean isUploading = false;
    private String proofUrl = "";

    //transfer shit
    private TextView actionText;
    private RelativeLayout finishCheckout;
    private ProgressBar checkoutProgress;

    private String currentPushId;
    private String currentFarmNotiId;

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
        bankRef = db.getReference(Common.MENORAH_BANK_NODE);
        pendingSponsorRef = db.getReference(Common.PENDING_NODE);
        proofRef = storage.getReference("PAYMENT_PROOFS");
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
                                                        showPaymentDialog(adapter.getRef(viewHolder.getAdapterPosition()).getKey(), theFarmType, theFarmROI, theFarmUnitPrice, theFarmSponsorDuration, model.getTotalPayout(), model.getUnits(), model.getFarmId());
                                                    });

                                                    //cancel checkout
                                                    cancel.setOnClickListener(v1 -> alertDialog.cancel());

                                                    alertDialog.show();

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


    //show choice dialog
    private void showPaymentDialog(final String theCartKey, final String theFarmType, final String theFarmROI,
                                   final String theFarmUnitPrice, final String theFarmSponsorDuration, final long totalPayout,
                                   final int units, final String farmId) {
        alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.payment_gateway_choice,null);

        final ImageView onlinePick = viewOptions.findViewById(R.id.onlinePick);
        final ImageView transferPick = viewOptions.findViewById(R.id.transferPick);

        //dialog parameters
        alertDialog.setView(viewOptions);
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.y = 100; // bottom margin
        alertDialog.getWindow().setAttributes(layoutParams);

        //open camera
        onlinePick.setOnClickListener(v -> {
            alertDialog.dismiss();
            checkoutAndPay(theCartKey, theFarmType, theFarmROI,
            theFarmUnitPrice, theFarmSponsorDuration, totalPayout,
            units, farmId);
        });

        //open gallery
        transferPick.setOnClickListener(v -> {
            alertDialog.dismiss();
            openTransferDialog(theCartKey, theFarmType, theFarmROI,
                    theFarmUnitPrice, theFarmSponsorDuration, totalPayout,
                    units, farmId);
        });

        alertDialog.show();
    }




    //uploading transfer sturv
    private void openTransferDialog(String theCartKey, String theFarmType, String theFarmROI,
                                    String theFarmUnitPrice, String theFarmSponsorDuration,
                                    long totalPayout, int units, String farmId) {

        alertDialog2 = new android.app.AlertDialog.Builder(Cart.this, R.style.DialogTheme).create();
        LayoutInflater inflater = Cart.this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.add_payment_proof_layout, null);

        final TextView menorahBank = viewOptions.findViewById(R.id.menorahBank);
        final TextView menorahAccountName = viewOptions.findViewById(R.id.menorahAccountName);
        final TextView menorahAccountNumber = viewOptions.findViewById(R.id.menorahAccountNumber);
        final TextView amountRequed = viewOptions.findViewById(R.id.amountRequed);
        actionText = viewOptions.findViewById(R.id.actionText);
        finishCheckout = viewOptions.findViewById(R.id.finishCheckout);
        checkoutProgress = viewOptions.findViewById(R.id.checkoutProgress);

        alertDialog2.setView(viewOptions);

        alertDialog2.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //get price
        int thelongPrice = Integer.parseInt(theFarmUnitPrice);
        displayPrice = thelongPrice * units;
        amountRequed.setText(Common.convertToPrice(Cart.this, displayPrice));




        bankRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        MenorahDetailsModel theDets = dataSnapshot.getValue(MenorahDetailsModel.class);

                        if (theDets != null){

                            menorahAccountName.setText(theDets.getAccount_name());
                            menorahAccountNumber.setText(theDets.getAccount_number());
                            menorahBank.setText(theDets.getBank());

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );


        //upload btn
        finishCheckout.setOnClickListener(v -> {

            //loading
            finishCheckout.setEnabled(false);
            actionText.setVisibility(View.GONE);
            checkoutProgress.setVisibility(View.VISIBLE);


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
            sponsorshipMap.put("status", "pending");

            //pending sponsorship map
            Map<String, Object> adminPendingSponsorshipMap = new HashMap<>();
            adminPendingSponsorshipMap.put("sponsorReturn", String.valueOf(currentTotalPayout));
            adminPendingSponsorshipMap.put("cycleEndDate", futureString);
            adminPendingSponsorshipMap.put("cycleStartDate", todayString);
            adminPendingSponsorshipMap.put("sponsorRefNumber", paymentReference);
            adminPendingSponsorshipMap.put("unitPrice", currentUnitPrice);
            adminPendingSponsorshipMap.put("sponsoredUnits", String.valueOf(currentUnits));
            adminPendingSponsorshipMap.put("sponsoredFarmType", currentFarmType);
            adminPendingSponsorshipMap.put("sponsoredFarmRoi", currentFarmRoi);
            adminPendingSponsorshipMap.put("sponsorshipDuration", currentDuration);
            adminPendingSponsorshipMap.put("startPoint", ServerValue.TIMESTAMP);
            adminPendingSponsorshipMap.put("totalAmountPaid", totalPrice);
            adminPendingSponsorshipMap.put("farmId", currentFarmId);
            adminPendingSponsorshipMap.put("userId", currentuid);
            adminPendingSponsorshipMap.put("paymentProof", "");

            //push id
            DatabaseReference pushRef = sponsorshipRef.push();
            currentPushId = pushRef.getKey();

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

                                                                //value
                                                                currentFarmNotiId = laFarm.getFarmNotiId();

                                                                //add to pending
                                                                pendingSponsorRef.child(currentFarmNotiId)
                                                                        .child(currentPushId)
                                                                        .setValue(adminPendingSponsorshipMap)
                                                                        .addOnCompleteListener(task2 -> {

                                                                            if (task2.isSuccessful()){

                                                                                //add to user sponsorship list
                                                                                sponsorshipRef.child(currentuid)
                                                                                        .child(currentPushId)
                                                                                        .setValue(sponsorshipMap)
                                                                                        .addOnCompleteListener(task -> {

                                                                                            if (task.isSuccessful()){

                                                                                                //init upload
                                                                                                if (ContextCompat.checkSelfPermission(Cart.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

                                                                                                    Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                                                                                                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                                                                                    startActivityForResult(pickPhoto , GALLERY_REQUEST_CODE);

                                                                                                } else {

                                                                                                    ActivityCompat.requestPermissions(Cart.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, VERIFY_PERMISSIONS_REQUEST);

                                                                                                }

                                                                                            } else {

                                                                                                //stop loading
                                                                                                finishCheckout.setEnabled(true);
                                                                                                checkoutProgress.setVisibility(View.GONE);
                                                                                                actionText.setVisibility(View.VISIBLE);

                                                                                                Toast.makeText(Cart.this, "Unknown error occurred, please contact admin immediately", Toast.LENGTH_LONG).show();

                                                                                            }

                                                                                        });

                                                                            } else {

                                                                                //stop loading
                                                                                finishCheckout.setEnabled(true);
                                                                                checkoutProgress.setVisibility(View.GONE);
                                                                                actionText.setVisibility(View.VISIBLE);

                                                                                Toast.makeText(Cart.this, "Unknown error occurred, please contact admin immediately", Toast.LENGTH_LONG).show();

                                                                            }

                                                                        });

                                                            } else {

                                                                //stop loading
                                                                finishCheckout.setEnabled(true);
                                                                checkoutProgress.setVisibility(View.GONE);
                                                                actionText.setVisibility(View.VISIBLE);

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

                                            //stop loading
                                            finishCheckout.setEnabled(true);
                                            checkoutProgress.setVisibility(View.GONE);
                                            actionText.setVisibility(View.VISIBLE);

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

        });

        alertDialog2.show();

    }

    //permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length > 0) {

            if (requestCode == VERIFY_PERMISSIONS_REQUEST && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , GALLERY_REQUEST_CODE);

            } else {

                Toast.makeText(Cart.this, "Permissions Denied", Toast.LENGTH_SHORT).show();

            }

        } else {

            Toast.makeText(Cart.this, "Permissions Error", Toast.LENGTH_SHORT).show();

        }

    }






    //FlutterWave Payment Gateway
    private void checkoutAndPay(final String theCartKey, final String theFarmType, final String theFarmROI,
                                final String theFarmUnitPrice, final String theFarmSponsorDuration, final long totalPayout,
                                final int units, final String farmId) {

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

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK){

            if (data.getData() != null) {
                imageUri = data.getData();

                CropImage.activity(imageUri)
                        .start(Cart.this);
            }

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            final CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                //value
                isUploading = true;

                //get data
                Uri resultUri = result.getUri();

                //user id
                String currentUid = mAuth.getCurrentUser().getUid();

                //get file path
                File thumb_filepath = new File(resultUri.getPath());

                try {

                    //converting to bitmap
                    Bitmap thumb_bitmap = new Compressor(Cart.this)
                            .setMaxWidth(400)
                            .setMaxHeight(600)
                            .setQuality(70)
                            .compressToBitmap(thumb_filepath);

                    //compress file size and set format
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    final byte[] thumb_byte = baos.toByteArray();


                    //set file location and name in firebase
                    final StorageReference imageThumbRef = proofRef.child(currentPushId + ".jpg");

                    //start upload
                    proofUploadTask = imageThumbRef.putBytes(thumb_byte);

                    //get download link
                    Task<Uri> urlTask = proofUploadTask.continueWithTask(task -> {

                        if (!task.isSuccessful()) {

                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return imageThumbRef.getDownloadUrl();

                    }).addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            Uri downloadUri = task.getResult();

                            //link
                            proofUrl = downloadUri.toString();

                            //image map
                            Map<String, Object> imageProofMap = new HashMap<>();
                            imageProofMap.put("paymentProof", proofUrl);

                            pendingSponsorRef.child(currentFarmNotiId)
                                    .child(currentPushId)
                                    .updateChildren(imageProofMap);

                            //remove from cart
                            cartRef.child(currentuid).child(currentCartKey).removeValue();

                            //send notification to user
                            sendPendingNotification();

                            //stop loading
                            finishCheckout.setEnabled(true);
                            checkoutProgress.setVisibility(View.GONE);
                            actionText.setVisibility(View.VISIBLE);

                            //dialog
                            alertDialog2.dismiss();


                        } else {

                            //stop loading
                            finishCheckout.setEnabled(true);
                            checkoutProgress.setVisibility(View.GONE);
                            actionText.setVisibility(View.VISIBLE);

                            //value
                            isUploading = false;

                            //dialog
                            alertDialog2.dismiss();

                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else

            if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
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

    private void sendPendingNotification() {

        final Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy  hh:mm");
        String todayString = formatter.format(todayDate);

        //notification map
        final Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("topic", "Sponsorship Pending");
        notificationMap.put("message", "Your sponsorship has been processed pending approval. Please be patient as your sponsorship would be confirmed within 24 to 72 hours.");
        notificationMap.put("time", todayString);


        notificationRef.child(currentuid)
                .push()
                .setValue(notificationMap)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()){

                        Map<String, String> dataSend = new HashMap<>();
                        dataSend.put("title", "Sponsorship Start");
                        dataSend.put("message", "Your sponsorship has been processed pending approval. Please be patient as your sponsorship would be confirmed within 24 to 72 hours.");
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
        if (isUploading){
            proofUploadTask.cancel();
        }
        finish();
    }

}
