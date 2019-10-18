package com.blackviking.menorahfarms.CartAndHistory;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Common.Permissions;
import com.blackviking.menorahfarms.FarmDetails;
import com.blackviking.menorahfarms.HomeActivities.Account;
import com.blackviking.menorahfarms.HomeActivities.FarmShop;
import com.blackviking.menorahfarms.Models.CartModel;
import com.blackviking.menorahfarms.Models.FarmModel;
import com.blackviking.menorahfarms.Models.MenorahDetailsModel;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import dmax.dialog.SpotsDialog;
import id.zelory.compressor.Compressor;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Response;

public class Cart extends AppCompatActivity {

    private ImageView backButton;
    private LinearLayout emptyLayout;
    private RecyclerView cartRecycler;
    private FirebaseRecyclerAdapter<CartModel, CartViewHolder> adapter;
    private LinearLayoutManager layoutManager;
    private RelativeLayout noInternetLayout;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference cartRef, userRef, farmRef, sponsorshipRef, payHistoryRef, termsRef, menorahBankRef, pendingSponsorshipRef,
            notificationRef, adminSponsorRef, sponsoredFarmNotiRef;
    private String currentuid;
    private String paymentReference;

    private String currentFarmType = "", currentFarmRoi = "", currentUnitPrice = "", currentDuration = "", currentFarmId = "", currentCartKey = "";
    private String todayString = "", futureString = "";
    private long totalPrice = 0, currentTotalPayout = 0;
    private int currentUnits = 0;
    private String publicKey = "FLWPUBK-bb6cf62f9e07f4b7f1028699eaa58873-X";
    private String encryptionKey = "0b17c443fa8aba1186a42910";
    private APIService mService;

    private android.app.AlertDialog loadingDialog, mDialog;
    private UserModel paperUser;

    //random farm manager
    private static final Random random = new Random();
    private static final String CHARS = "12";


    //dialog widgets
    private TextView menorahBank, menorahAccountName, menorahAccountNumber, actionText;
    private RelativeLayout finishCheckout;
    private ProgressBar checkoutProgress;


    //image
    private static final int VERIFY_PERMISSIONS_REQUEST = 757;
    private static final int GALLERY_REQUEST_CODE = 265;
    private Uri imageUri;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference imageRef;
    private String originalImageUrl;

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
        adminSponsorRef = db.getReference("RunningCycles");
        sponsoredFarmNotiRef = db.getReference("SponsoredFarmsNotification");
        menorahBankRef = db.getReference("MenorahBankDetails");
        imageRef = storage.getReference("PaymentProofs");
        pendingSponsorshipRef = db.getReference("PendingSponsorships");
        if (mAuth.getCurrentUser() != null)
            currentuid = mAuth.getCurrentUser().getUid();


        //set paper user
        paperUser = Paper.book().read(Common.PAPER_USER);


        /*---   FCM   ---*/
        mService = Common.getFCMService();


        /*---   WIDGETS   ---*/
        backButton = (ImageView)findViewById(R.id.backButton);
        emptyLayout = (LinearLayout)findViewById(R.id.emptyLayout);
        cartRecycler = (RecyclerView)findViewById(R.id.cartRecycler);
        noInternetLayout = findViewById(R.id.noInternetLayout);


        //show loading dialog
        showLoadingDialog("Loading cart items . . .");


        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

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

            }
        }).execute();


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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

                loadingDialog.dismiss();
                viewHolder.cartItemUnits.setText("X " + model.getUnits());

                farmRef.child(model.getFarmId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                FarmModel currentFarm = dataSnapshot.getValue(FarmModel.class);

                                if (currentFarm != null){

                                    final String theFarmType = currentFarm.getFarmType();
                                    String theFarmLocation = currentFarm.getFarmLocation();
                                    final String theFarmROI = currentFarm.getFarmRoi();
                                    final String theFarmUnitPrice = currentFarm.getPricePerUnit();
                                    final String theFarmSponsorDuration = currentFarm.getSponsorDuration();
                                    final String theFarmImage = currentFarm.getFarmImageThumb();
                                    final String theFarmState = currentFarm.getFarmState();

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
                                            openDeleteDialog(adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                                        }
                                    });


                                    viewHolder.checkout.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            if (Common.checkKYC(Cart.this).equalsIgnoreCase("Profile Complete")) {

                                                //execute network check async task
                                                CheckInternet asyncTask = (CheckInternet) new CheckInternet(Cart.this, new CheckInternet.AsyncResponse() {
                                                    @Override
                                                    public void processFinish(Integer output) {

                                                        //check all cases
                                                        if (output == 1) {

                                                            final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(Cart.this).create();
                                                            LayoutInflater inflater = Cart.this.getLayoutInflater();
                                                            View viewOptions = inflater.inflate(R.layout.terms_layout, null);

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
                                                                    transferCheckout(theFarmState, adapter.getRef(viewHolder.getAdapterPosition()).getKey(), theFarmType, theFarmROI, theFarmUnitPrice, theFarmSponsorDuration, model.getTotalPayout(), model.getUnits(), model.getFarmId());
                                                                    //checkoutAndPay(theFarmState, adapter.getRef(viewHolder.getAdapterPosition()).getKey(), theFarmType, theFarmROI, theFarmUnitPrice, theFarmSponsorDuration, model.getTotalPayout(), model.getUnits(), model.getFarmId());
                                                                    //testPay(adapter.getRef(viewHolder.getAdapterPosition()).getKey(), theFarmType, theFarmROI, theFarmUnitPrice, theFarmSponsorDuration, model.getTotalPayout(), model.getUnits(), model.getFarmId());
                                                                }
                                                            });

                                                            cancel.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    alertDialog.cancel();
                                                                }
                                                            });

                                                            alertDialog.show();

                                                        } else if (output == 0) {

                                                            Toast.makeText(Cart.this, "No internet access", Toast.LENGTH_SHORT).show();

                                                        } else if (output == 2) {

                                                            Toast.makeText(Cart.this, "No network access", Toast.LENGTH_SHORT).show();

                                                        }

                                                    }
                                                }).execute();

                                            } else {

                                                showErrorDialog(Common.checkKYC(Cart.this));

                                            }


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

    private void transferCheckout(final String theFarmState, final String key, final String theFarmType, final String theFarmROI, final String theFarmUnitPrice, final String theFarmSponsorDuration, final long totalPayout, final int units, final String farmId) {

        final android.app.AlertDialog alertDialog2 = new android.app.AlertDialog.Builder(Cart.this).create();
        LayoutInflater inflater = Cart.this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.add_payment_proof_layout, null);

        menorahBank = viewOptions.findViewById(R.id.menorahBank);
        menorahAccountName = viewOptions.findViewById(R.id.menorahAccountName);
        menorahAccountNumber = viewOptions.findViewById(R.id.menorahAccountNumber);
        actionText = viewOptions.findViewById(R.id.actionText);
        finishCheckout = viewOptions.findViewById(R.id.finishCheckout);
        checkoutProgress = viewOptions.findViewById(R.id.checkoutProgress);

        alertDialog2.setView(viewOptions);

        alertDialog2.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        //fill bank info
        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(Cart.this, new CheckInternet.AsyncResponse() {
            @Override
            public void processFinish(Integer output) {

                //check all cases
                if (output == 1) {

                    menorahBankRef.addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    MenorahDetailsModel theDets = dataSnapshot.getValue(MenorahDetailsModel.class);

                                    if (theDets != null){

                                        menorahAccountName.setText("Account Name: " + theDets.getAccount_name());
                                        menorahAccountNumber.setText("Account No: " + theDets.getAccount_number());
                                        menorahBank.setText("Bank: " + theDets.getBank());

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            }
                    );

                } else if (output == 0) {

                    Toast.makeText(Cart.this, "No internet access", Toast.LENGTH_SHORT).show();

                } else if (output == 2) {

                    Toast.makeText(Cart.this, "No network access", Toast.LENGTH_SHORT).show();

                }

            }
        }).execute();


        //upload btn
        finishCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (imageUri == null && actionText.getText().toString().equalsIgnoreCase("UPLOAD")) {

                    if (checkPermissionsArray(Permissions.PERMISSIONS)){

                        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhoto, GALLERY_REQUEST_CODE);


                    } else {

                        verifyPermissions(Permissions.PERMISSIONS);

                    }

                } else if (imageUri != null && originalImageUrl != null
                            && !originalImageUrl.equals("") && actionText.getText().toString().equalsIgnoreCase("FINISH")){

                    finishTransferCheckout(alertDialog2, theFarmState, key, theFarmType, theFarmROI, theFarmUnitPrice, theFarmSponsorDuration, totalPayout, units, farmId);

                }

            }
        });

        alertDialog2.show();

    }

    private void finishTransferCheckout(final android.app.AlertDialog alertDialog2, final String theFarmState, final String key, final String theFarmType, final String theFarmROI, final String theFarmUnitPrice, final String theFarmSponsorDuration, final long totalPayout, final int units, final String farmId) {

        //loading
        actionText.setVisibility(View.GONE);
        checkoutProgress.setVisibility(View.VISIBLE);

        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

                //check all cases
                if (output == 1){

                    final UserModel user1 = Paper.book().read(Common.PAPER_USER);

                    /*---   GLOBAL SET   ---*/
                    currentFarmType = theFarmType;
                    currentFarmRoi = theFarmROI;
                    currentUnitPrice = theFarmUnitPrice;
                    currentDuration = theFarmSponsorDuration;
                    currentFarmId = farmId;
                    currentTotalPayout = totalPayout;
                    currentUnits = units;
                    currentCartKey = key;


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


                    if (theFarmState.equalsIgnoreCase("Now Selling")){

                        pushToDb(alertDialog2);

                    } else {

                        //loading
                        actionText.setVisibility(View.VISIBLE);
                        checkoutProgress.setVisibility(View.GONE);
                        showErrorDialog("Sorry, this farm is sold out.");

                    }


                } else

                if (output == 0){

                    //loading
                    actionText.setVisibility(View.VISIBLE);
                    checkoutProgress.setVisibility(View.GONE);
                    Toast.makeText(Cart.this, "No internet access", Toast.LENGTH_SHORT).show();

                } else

                if (output == 2){

                    //loading
                    actionText.setVisibility(View.VISIBLE);
                    checkoutProgress.setVisibility(View.GONE);
                    Toast.makeText(Cart.this, "No network connection", Toast.LENGTH_SHORT).show();

                }

            }
        }).execute();

    }

    private void testPay(String theFarmState, String theCartKey, final String theFarmType, String theFarmROI, String theFarmUnitPrice, String theFarmSponsorDuration, long totalPayout, final int units, String farmId) {

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

    private void checkoutAndPay(final String theFarmState, final String theCartKey, final String theFarmType, final String theFarmROI, final String theFarmUnitPrice, final String theFarmSponsorDuration, final long totalPayout, final int units, final String farmId) {

        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

                //check all cases
                if (output == 1){

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


                    farmRef.child(farmId)
                            .addValueEventListener(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            String unitsAvailable = dataSnapshot.child("unitsAvailable").getValue().toString();

                                            int unitsLeft = Integer.parseInt(unitsAvailable);

                                            if (units < unitsLeft || units == unitsLeft){

                                                if (theFarmState.equalsIgnoreCase("Now Selling")){

                                                    /*---   PAYMENT   ---*/
                                                    new RavePayManager(Cart.this).setAmount(totalPrice)
                                                            .setCountry("NG")
                                                            .setCurrency("NGN")
                                                            .setEmail(user1.getEmail())
                                                            .setfName(user1.getFirstName())
                                                            .setlName(user1.getLastName())
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

                                                    showErrorDialog("Sorry, this farm is sold out.");

                                                }

                                            } else {

                                                showErrorDialog("Units Left For Sale On Farm Can Not Serve Your Request. Please Go Back To The Farm Store To Make Other Requests");

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

            }
        }).execute();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            String message = data.getStringExtra("response");

            if (resultCode == RavePayActivity.RESULT_SUCCESS) {

                //show loading dialog
                showLoadingDialog("Authorizing sponsorship . . .");

                //execute network check async task
                CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
                    @Override
                    public void processFinish(Integer output) {

                        //check all cases
                        if (output == 1){

                            //pushToDb();

                        } else

                        if (output == 0){

                            loadingDialog.dismiss();
                            showErrorDialog("Error occurred due to lack of internet access");

                        } else

                        if (output == 2){

                            loadingDialog.dismiss();
                            showErrorDialog("You are not connected to any network");

                        }

                    }
                }).execute();

            } else if (resultCode == RavePayActivity.RESULT_ERROR) {

                Toast.makeText(this, "Transaction failed", Toast.LENGTH_SHORT).show();

            } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {

                Toast.makeText(this, "Transaction cancelled", Toast.LENGTH_SHORT).show();

            }
        } else

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

                //btn animation
                actionText.setVisibility(View.GONE);
                checkoutProgress.setVisibility(View.VISIBLE);


                //execute network check async task
                CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
                    @Override
                    public void processFinish(Integer output) {

                        //check all cases
                        if (output == 1){

                            Uri resultUri = result.getUri();

                            File thumb_filepath = new File(resultUri.getPath());

                            try {
                                Bitmap thumb_bitmap = new Compressor(Cart.this)
                                        .setMaxWidth(500)
                                        .setMaxHeight(700)
                                        .setQuality(75)
                                        .compressToBitmap(thumb_filepath);

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                                final byte[] thumb_byte = baos.toByteArray();

                                final StorageReference imageThumbRef1 = imageRef.child(currentuid).child(System.currentTimeMillis() + ".jpg");

                                final UploadTask uploadTask = imageThumbRef1.putBytes(thumb_byte);


                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                        originalImageUrl = thumb_task.getResult().getDownloadUrl().toString();

                                        if (thumb_task.isSuccessful()) {

                                            actionText.setText("FINISH");
                                            actionText.setCompoundDrawablesRelative(getResources().getDrawable(R.drawable.ic_followed), null, null, null);
                                            actionText.setVisibility(View.VISIBLE);
                                            checkoutProgress.setVisibility(View.GONE);

                                        } else {
                                            Toast.makeText(Cart.this, "Upload Failed. Please Try Again", Toast.LENGTH_LONG).show();
                                            imageUri = null;
                                            originalImageUrl = null;
                                        }
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } else

                        if (output == 0){

                            //no internet
                            imageUri = null;
                            originalImageUrl = null;
                            Toast.makeText(Cart.this, "No internet", Toast.LENGTH_SHORT).show();

                        } else

                        if (output == 2){

                            //no network
                            imageUri = null;
                            originalImageUrl = null;
                            Toast.makeText(Cart.this, "Not connected to a network", Toast.LENGTH_SHORT).show();

                        }

                    }
                }).execute();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void verifyPermissions(String[] permissions) {

        ActivityCompat.requestPermissions(
                this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
    }

    private boolean checkPermissionsArray(String[] permissions) {

        for (int i = 0; i < permissions.length; i++){

            String check = permissions[i];
            if (!checkPermissions(check)){
                return false;
            }

        }
        return true;
    }

    private boolean checkPermissions(String permission) {

        int permissionRequest = ActivityCompat.checkSelfPermission(this, permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED){

            return false;
        } else {

            return true;
        }
    }

    private void pushToDb(final android.app.AlertDialog alertDialog2) {

        //assign farm manager
        userRef.child(currentuid).child("accountManager").setValue(setAdminToken(1));

        final UserModel user2 = Paper.book().read(Common.PAPER_USER);


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

        //admin running cycle map
        Map<String, Object> adminSponsorshipMap = new HashMap<>();
        adminSponsorshipMap.put("sponsorReturn", String.valueOf(currentTotalPayout));
        adminSponsorshipMap.put("cycleEndDate", futureString);
        adminSponsorshipMap.put("cycleStartDate", todayString);
        adminSponsorshipMap.put("sponsorRefNumber", paymentReference);
        adminSponsorshipMap.put("unitPrice", currentUnitPrice);
        adminSponsorshipMap.put("sponsoredUnits", String.valueOf(currentUnits));
        adminSponsorshipMap.put("sponsoredFarmType", currentFarmType);
        adminSponsorshipMap.put("sponsoredFarmRoi", currentFarmRoi);
        adminSponsorshipMap.put("sponsorshipDuration", currentDuration);
        adminSponsorshipMap.put("startPoint", ServerValue.TIMESTAMP);
        adminSponsorshipMap.put("totalAmountPaid", totalPrice);
        adminSponsorshipMap.put("farmId", currentFarmId);
        adminSponsorshipMap.put("userId", currentuid);

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
        adminPendingSponsorshipMap.put("paymentProof", originalImageUrl);

        //admin
        final Map<String, Object> logMap = new HashMap<>();
        logMap.put("userName", user2.getFirstName() + " " + user2.getLastName());
        logMap.put("userEmail", user2.getEmail());
        logMap.put("paymentRef", paymentReference);
        logMap.put("paymentDate", todayString);
        logMap.put("farmSponsored", currentFarmId);
        logMap.put("paymentProof", originalImageUrl);

        DatabaseReference pushRef = adminSponsorRef.push();
        String pushId = pushRef.getKey();

        //subscribe to notification
        subscribeToNnotification(currentFarmId);

        //add to running cycle
        addToRunningCycle(pushId, adminSponsorshipMap, currentFarmId, adminPendingSponsorshipMap);

        sponsorshipRef.child(currentuid)
                .child(pushId)
                .setValue(sponsorshipMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            payHistoryRef.child(currentuid)
                                    .push()
                                    .setValue(logMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                //dialog dismiss
                                                alertDialog2.dismiss();

                                                cartRef.child(currentuid).child(currentCartKey).removeValue();
                                                sendNotification();

                                            } else {

                                                //loading
                                                actionText.setVisibility(View.VISIBLE);
                                                checkoutProgress.setVisibility(View.GONE);
                                                showErrorDialog("Unknown error occurred, please contact admin immediately");

                                            }

                                        }
                                    });

                        } else {

                            //loading
                            actionText.setVisibility(View.VISIBLE);
                            checkoutProgress.setVisibility(View.GONE);
                            showErrorDialog("Unknown error occurred, please contact admin immediately");

                        }

                    }
                });

    }

    private void addToRunningCycle(final String pushId, final Map<String, Object> adminSponsorshipMap, String currentFarmId, final Map<String, Object> adminPendingSponsorshipMap) {

        farmRef.child(currentFarmId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        FarmModel theFarm = dataSnapshot.getValue(FarmModel.class);

                        if (theFarm != null){

                            adminSponsorRef.child(theFarm.getFarmNotiId())
                                    .child(pushId)
                                    .setValue(adminSponsorshipMap);

                            pendingSponsorshipRef.child(theFarm.getFarmNotiId())
                                    .child(pushId)
                                    .setValue(adminPendingSponsorshipMap);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void subscribeToNnotification(String currentFarmId) {

        farmRef.child(currentFarmId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        FarmModel theFarm = dataSnapshot.getValue(FarmModel.class);

                        if (theFarm != null){

                            sponsoredFarmNotiRef.child(theFarm.getFarmNotiId())
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

    private void sendNotification() {

        final Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy  hh:mm");
        String todayString = formatter.format(todayDate);

        final Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("topic", "Sponsorship Start");
        notificationMap.put("message", "Your payment has been received successfully and your sponsorship cycle will start shortly. You can monitor your sponsored farm from the Sponsored Farms page through your dashboard.");
        notificationMap.put("time", todayString);


        //notification to admin
        sendAlertToAdmin();
        /*final Map<String, Object> adminNotificationMap = new HashMap<>();
        adminNotificationMap.put("topic", "Sponsorship Start");
        adminNotificationMap.put("message", "New Sponsorship Alert.");
        adminNotificationMap.put("time", todayString);

        userRef.orderByChild("userType")
                .equalTo("Admin")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                for (DataSnapshot snap : dataSnapshot.getChildren()){

                                    final String adminKey = snap.getKey();

                                    notificationRef.child(adminKey)
                                            .push()
                                            .setValue(adminNotificationMap)
                                            .addOnSuccessListener(
                                                    new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

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
                                            ).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );*/

        notificationRef.child(currentuid)
                .push()
                .setValue(notificationMap)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Map<String, String> dataSend = new HashMap<>();
                                dataSend.put("title", "Sponsorship Start");
                                dataSend.put("message", "Your payment has been received successfully and your sponsorship cycle will start shortly. You can monitor your sponsored farm from the Sponsored Farms page through your dashboard.");
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

    public static String setAdminToken(int length) {
        StringBuilder token = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            token.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return token.toString();
    }

    private void openDeleteDialog(final String key) {

        AlertDialog alertDialog = new AlertDialog.Builder(Cart.this)
                .setTitle("Delete Update !")
                .setIcon(R.drawable.ic_remove_from_cart)
                .setMessage("Are You Sure You Want To Remove This Item From Your Cart?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //execute network check async task
                        CheckInternet asyncTask = (CheckInternet) new CheckInternet(Cart.this, new CheckInternet.AsyncResponse(){
                            @Override
                            public void processFinish(Integer output) {

                                //check all cases
                                if (output == 1){

                                    cartRef.child(currentuid)
                                            .child(key)
                                            .removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()){

                                                        Toast.makeText(Cart.this, "Item Removed From cart !", Toast.LENGTH_SHORT).show();

                                                    } else {

                                                        Toast.makeText(Cart.this, "Error occurred", Toast.LENGTH_SHORT).show();

                                                    }

                                                }
                                            });

                                } else

                                if (output == 0){

                                    Toast.makeText(Cart.this, "Error occurred", Toast.LENGTH_SHORT).show();

                                } else

                                if (output == 2){

                                    Toast.makeText(Cart.this, "Error occurred", Toast.LENGTH_SHORT).show();

                                }

                            }
                        }).execute();

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

    /*---   LOADING DIALOG   ---*/
    public void showLoadingDialog(String theMessage){

        loadingDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.loading_dialog,null);

        final TextView loadingText = viewOptions.findViewById(R.id.loadingText);

        loadingDialog.setView(viewOptions);

        loadingDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        loadingText.setText(theMessage);

        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });

        loadingDialog.show();

    }
}
