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

import com.blackviking.menorahfarms.AdminDetails.PendingSponsorshipDetail;
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
    private DatabaseReference cartRef, userRef, farmRef, sponsorshipRef, payHistoryRef, termsRef, runningCycleRef,
            notificationRef;
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
    private boolean isLoading = false;
    private UserModel paperUser;

    //random farm manager
    private static final Random random = new Random();
    private static final String[] CHARS = {"01", "02"};

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
        runningCycleRef = db.getReference("RunningCycles");
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

                                final FarmModel currentFarm = dataSnapshot.getValue(FarmModel.class);

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

                                                            //check
                                                            sponsorshipRef.child(currentuid)
                                                                    .orderByChild("farmId")
                                                                    .equalTo(model.getFarmId())
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

                                                                                if (theCount < 100 && model.getUnits() <= spaceRemaining){

                                                                                    final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(Cart.this, R.style.DialogTheme).create();
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
                                                                                            //transferCheckout(theFarmState, adapter.getRef(viewHolder.getAdapterPosition()).getKey(), theFarmType, theFarmROI, theFarmUnitPrice, theFarmSponsorDuration, model.getTotalPayout(), model.getUnits(), model.getFarmId());
                                                                                            checkoutAndPay(theFarmState, adapter.getRef(viewHolder.getAdapterPosition()).getKey(), theFarmType, theFarmROI, theFarmUnitPrice, theFarmSponsorDuration, model.getTotalPayout(), model.getUnits(), model.getFarmId());
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

                                                                                } else {

                                                                                    showErrorDialog("You can not exceed sponsorship limit for this package");

                                                                                }

                                                                            } else

                                                                            if (currentFarm.getPackagedType().equalsIgnoreCase("Student")){

                                                                                int spaceRemaining = 10 - theCount;

                                                                                if (theCount < 10 && model.getUnits() <= spaceRemaining){

                                                                                    final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(Cart.this, R.style.DialogTheme).create();
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
                                                                                            //transferCheckout(theFarmState, adapter.getRef(viewHolder.getAdapterPosition()).getKey(), theFarmType, theFarmROI, theFarmUnitPrice, theFarmSponsorDuration, model.getTotalPayout(), model.getUnits(), model.getFarmId());
                                                                                            checkoutAndPay(theFarmState, adapter.getRef(viewHolder.getAdapterPosition()).getKey(), theFarmType, theFarmROI, theFarmUnitPrice, theFarmSponsorDuration, model.getTotalPayout(), model.getUnits(), model.getFarmId());
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

                                                                                } else {

                                                                                    showErrorDialog("You can not exceed sponsorship limit for this package");

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



    //FlutterWave Payment Gateway
    private void checkoutAndPay(final String theFarmState, final String theCartKey, final String theFarmType, final String theFarmROI, final String theFarmUnitPrice, final String theFarmSponsorDuration, final long totalPayout, final int units, final String farmId) {

        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

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
                            .addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            FarmModel laFarm = dataSnapshot.getValue(FarmModel.class);

                                            if (laFarm != null){

                                                if (laFarm.getFarmState().equalsIgnoreCase("Now Selling")){

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
                                                            .withTheme(R.style.FlutterPayTheme)
                                                            .initialize();

                                                } else {

                                                    //loading
                                                    showErrorDialog("Farm is sold out.");

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

                //execute network check async task
                CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
                    @Override
                    public void processFinish(Integer output) {

                        //check all cases
                        if (output == 1){

                            pushToDb();

                        } else

                        if (output == 0){

                            loadingDialog.dismiss();
                            showErrorDialog("No internet access");

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
        }

    }





//    //Transfer Payment Gateway
//    private void transferCheckout(final String theFarmState, final String key, final String theFarmType, final String theFarmROI, final String theFarmUnitPrice, final String theFarmSponsorDuration, final long totalPayout, final int units, final String farmId) {
//
//        final android.app.AlertDialog alertDialog2 = new android.app.AlertDialog.Builder(Cart.this, R.style.DialogTheme).create();
//        LayoutInflater inflater = Cart.this.getLayoutInflater();
//        View viewOptions = inflater.inflate(R.layout.add_payment_proof_layout, null);
//
//        menorahBank = viewOptions.findViewById(R.id.menorahBank);
//        menorahAccountName = viewOptions.findViewById(R.id.menorahAccountName);
//        menorahAccountNumber = viewOptions.findViewById(R.id.menorahAccountNumber);
//        actionText = viewOptions.findViewById(R.id.actionText);
//        finishCheckout = viewOptions.findViewById(R.id.finishCheckout);
//        checkoutProgress = viewOptions.findViewById(R.id.checkoutProgress);
//
//        alertDialog2.setView(viewOptions);
//
//        alertDialog2.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
//        alertDialog2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//
//
//        //fill bank info
//        //execute network check async task
//        CheckInternet asyncTask = (CheckInternet) new CheckInternet(Cart.this, new CheckInternet.AsyncResponse() {
//            @Override
//            public void processFinish(Integer output) {
//
//                //check all cases
//                if (output == 1) {
//
//                    menorahBankRef.addListenerForSingleValueEvent(
//                            new ValueEventListener() {
//                                @Override
//                                public void onDataChange(DataSnapshot dataSnapshot) {
//
//                                    MenorahDetailsModel theDets = dataSnapshot.getValue(MenorahDetailsModel.class);
//
//                                    if (theDets != null){
//
//                                        menorahAccountName.setText(theDets.getAccount_name());
//                                        menorahAccountNumber.setText(theDets.getAccount_number());
//                                        menorahBank.setText(theDets.getBank());
//
//                                    }
//
//                                }
//
//                                @Override
//                                public void onCancelled(DatabaseError databaseError) {
//
//                                }
//                            }
//                    );
//
//                } else if (output == 0) {
//
//                    Toast.makeText(Cart.this, "No internet access", Toast.LENGTH_SHORT).show();
//
//                } else if (output == 2) {
//
//                    Toast.makeText(Cart.this, "No network access", Toast.LENGTH_SHORT).show();
//
//                }
//
//            }
//        }).execute();
//
//
//        //upload btn
//        finishCheckout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (imageUri == null && actionText.getText().toString().equalsIgnoreCase("UPLOAD EVIDENCE OF PAYMENT")) {
//
//                    if (checkPermissionsArray(Permissions.PERMISSIONS)){
//
//                        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
//                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                        startActivityForResult(pickPhoto, GALLERY_REQUEST_CODE);
//
//
//                    } else {
//
//                        verifyPermissions(Permissions.PERMISSIONS);
//
//                    }
//
//                } else if (imageUri != null && originalImageUrl != null
//                            && !originalImageUrl.equals("") && actionText.getText().toString().equalsIgnoreCase("FINISH")){
//
//                    finishTransferCheckout(alertDialog2, theFarmState, key, theFarmType, theFarmROI, theFarmUnitPrice, theFarmSponsorDuration, totalPayout, units, farmId);
//
//                }
//
//            }
//        });
//
//        alertDialog2.show();
//
//    }
//
//    private void finishTransferCheckout(final android.app.AlertDialog alertDialog2, final String theFarmState, final String key, final String theFarmType, final String theFarmROI, final String theFarmUnitPrice, final String theFarmSponsorDuration, final long totalPayout, final int units, final String farmId) {
//
//        //loading
//        actionText.setVisibility(View.GONE);
//        checkoutProgress.setVisibility(View.VISIBLE);
//
//        //execute network check async task
//        CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
//            @Override
//            public void processFinish(Integer output) {
//
//                //check all cases
//                if (output == 1){
//
//                    final UserModel user1 = Paper.book().read(Common.PAPER_USER);
//
//                    /*---   GLOBAL SET   ---*/
//                    currentFarmType = theFarmType;
//                    currentFarmRoi = theFarmROI;
//                    currentUnitPrice = theFarmUnitPrice;
//                    currentDuration = theFarmSponsorDuration;
//                    currentFarmId = farmId;
//                    currentTotalPayout = totalPayout;
//                    currentUnits = units;
//                    currentCartKey = key;
//
//
//                    char firstInit = user1.getFirstName().charAt(0);
//                    char lastInit = user1.getLastName().charAt(0);
//
//                    String initials = String.valueOf(firstInit) + String.valueOf(lastInit);
//
//                    paymentReference = initials + "-MF-" + System.currentTimeMillis();
//
//                    /*---   PRICE TO PAY   ---*/
//                    int thePrice = Integer.parseInt(theFarmUnitPrice);
//                    totalPrice = thePrice * units;
//
//
//                    /*---   DURATION   ---*/
//                    int theDuration = Integer.parseInt(theFarmSponsorDuration);
//                    int theDays = 30 * theDuration;
//
//                    Date todayDate = Calendar.getInstance().getTime();
//                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
//                    todayString = formatter.format(todayDate);
//
//                    Calendar cal = Calendar.getInstance();
//                    cal.setTime(todayDate);
//                    cal.add(Calendar.DATE, theDays);
//                    Date futureDate = cal.getTime();
//                    SimpleDateFormat formatterFuture = new SimpleDateFormat("dd-MM-yyyy");
//                    futureString = formatterFuture.format(futureDate);
//
//
//                    if (theFarmState.equalsIgnoreCase("Now Selling")){
//
//                        pushToDb(alertDialog2);
//
//                    } else {
//
//                        //loading
//                        actionText.setVisibility(View.VISIBLE);
//                        checkoutProgress.setVisibility(View.GONE);
//                        showErrorDialog("Sorry, this farm is sold out.");
//
//                    }
//
//
//                } else
//
//                if (output == 0){
//
//                    //loading
//                    actionText.setVisibility(View.VISIBLE);
//                    checkoutProgress.setVisibility(View.GONE);
//                    Toast.makeText(Cart.this, "No internet access", Toast.LENGTH_SHORT).show();
//
//                } else
//
//                if (output == 2){
//
//                    //loading
//                    actionText.setVisibility(View.VISIBLE);
//                    checkoutProgress.setVisibility(View.GONE);
//                    Toast.makeText(Cart.this, "No network connection", Toast.LENGTH_SHORT).show();
//
//                }
//
//            }
//        }).execute();
//
//    }



    //Test Gateway
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



//    //Permissions
//    private void verifyPermissions(String[] permissions) {
//
//        ActivityCompat.requestPermissions(
//                this,
//                permissions,
//                VERIFY_PERMISSIONS_REQUEST
//        );
//    }
//
//    private boolean checkPermissionsArray(String[] permissions) {
//
//        for (int i = 0; i < permissions.length; i++){
//
//            String check = permissions[i];
//            if (!checkPermissions(check)){
//                return false;
//            }
//
//        }
//        return true;
//    }
//
//    private boolean checkPermissions(String permission) {
//
//        int permissionRequest = ActivityCompat.checkSelfPermission(this, permission);
//
//        if (permissionRequest != PackageManager.PERMISSION_GRANTED){
//
//            return false;
//        } else {
//
//            return true;
//        }
//    }



    //add to database
    private void pushToDb(/*final android.app.AlertDialog alertDialog2*/) {

        //get user
        final UserModel user2 = Paper.book().read(Common.PAPER_USER);

        //assign farm manager
        if (paperUser.getAccountManager().equalsIgnoreCase("")) {
            userRef.child(currentuid).child("accountManager").setValue(CHARS[new Random().nextInt(CHARS.length)]);
        }




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

        //pending sponsorship map
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

        //admin
        final Map<String, Object> logMap = new HashMap<>();
        logMap.put("userName", user2.getFirstName() + " " + user2.getLastName());
        logMap.put("userEmail", user2.getEmail());
        logMap.put("paymentRef", paymentReference);
        logMap.put("paymentDate", todayString);
        logMap.put("farmSponsored", currentFarmId);
        logMap.put("paymentProof", "");

        DatabaseReference pushRef = sponsorshipRef.push();
        String pushId = pushRef.getKey();

        //add to running sponsorships cycle
        addToRunningCycle(pushId, currentFarmId, runningCycleMap);

        //add to user sponsorship list
        sponsorshipRef.child(currentuid)
                .child(pushId)
                .setValue(sponsorshipMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            cartRef.child(currentuid).child(currentCartKey).removeValue();
                            sendApprovalNotification();

                            payHistoryRef.child(currentuid)
                                    .push()
                                    .setValue(logMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                loadingDialog.dismiss();

                                            } else {

                                                showErrorDialog("Unknown error occurred, please contact admin immediately");

                                            }

                                        }
                                    });

                        } else {

                            showErrorDialog("Unknown error occurred, please contact admin immediately");

                        }

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

                            runningCycleRef.child(theFarm.getFarmNotiId())
                                    .child(pushId)
                                    .setValue(runningCycleMap);

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
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

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
        if (isLoading){
            loadingDialog.dismiss();
        }
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
}
