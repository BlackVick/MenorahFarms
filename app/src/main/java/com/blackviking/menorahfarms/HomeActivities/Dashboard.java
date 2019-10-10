package com.blackviking.menorahfarms.HomeActivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.AdminDash;
import com.blackviking.menorahfarms.CartAndHistory.Cart;
import com.blackviking.menorahfarms.Common.ApplicationClass;
import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.DashboardMenu.AccountManager;
import com.blackviking.menorahfarms.DashboardMenu.Faq;
import com.blackviking.menorahfarms.DashboardMenu.FarmUpdates;
import com.blackviking.menorahfarms.DashboardMenu.FollowedFarms;
import com.blackviking.menorahfarms.DashboardMenu.Notifications;
import com.blackviking.menorahfarms.DashboardMenu.SponsoredFarms;
import com.blackviking.menorahfarms.Models.FarmModel;
import com.blackviking.menorahfarms.Models.UserModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.Services.CheckForSponsorship;
import com.blackviking.menorahfarms.SignIn;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.net.InetAddress;
import java.net.UnknownHostException;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class Dashboard extends AppCompatActivity {

    private LinearLayout dashboardSwitch, farmstoreSwitch, accountSwitch;
    private TextView dashboardText, farmstoreText, accountText;

    private TextView welcome, sponsorCycle, totalReturnsText, nextEndOfCycleDate;
    private ImageView cartButton;
    private CircleImageView userAvatar;
    private RelativeLayout goToFarmstoreButton;
    private RelativeLayout sponsoredFarmsLayout, farmsToWatchLayout, farmUpdatesLayout, allFarmsLayout, projectManagerLayout, notificationLayout, faqLayout, adminLayout;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, sponsoredRef, farmRef;
    private String currentUid;
    private boolean isMonitorRunning;
    private UserModel paperUser;
    private android.app.AlertDialog alertDialog, alertDialogError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        sponsoredRef = db.getReference("SponsoredFarms");
        farmRef = db.getReference("Farms");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        dashboardSwitch = (LinearLayout)findViewById(R.id.dashboardLayout);
        farmstoreSwitch = (LinearLayout)findViewById(R.id.farmShopLayout);
        accountSwitch = (LinearLayout)findViewById(R.id.accountLayout);
        dashboardText = (TextView)findViewById(R.id.dashboardText);
        farmstoreText = (TextView)findViewById(R.id.farmShopText);
        accountText = (TextView)findViewById(R.id.accountText);


        sponsorCycle = (TextView)findViewById(R.id.userSponsorCycle);
        welcome = (TextView)findViewById(R.id.userWelcome);
        totalReturnsText = (TextView)findViewById(R.id.totalReturns);
        nextEndOfCycleDate = (TextView)findViewById(R.id.nextEndOfCycleDate);
        cartButton = (ImageView)findViewById(R.id.userCart);
        userAvatar = (CircleImageView)findViewById(R.id.userAvatar);
        goToFarmstoreButton = (RelativeLayout)findViewById(R.id.goToFarmstoreButton);
        sponsoredFarmsLayout = (RelativeLayout)findViewById(R.id.sponsoredFarmsLayout);
        farmsToWatchLayout = (RelativeLayout)findViewById(R.id.farmsToWatchLayout);
        farmUpdatesLayout = (RelativeLayout)findViewById(R.id.farmUpdatesLayout);
        allFarmsLayout = (RelativeLayout)findViewById(R.id.allFarmsLayout);
        projectManagerLayout = (RelativeLayout)findViewById(R.id.projectManagerLayout);
        notificationLayout = (RelativeLayout)findViewById(R.id.notificationLayout);
        faqLayout = (RelativeLayout)findViewById(R.id.faqLayout);
        adminLayout = (RelativeLayout)findViewById(R.id.adminLayout);


        /*---   BOTTOM NAV   ---*/
        dashboardText.setTextColor(getResources().getColor(R.color.colorPrimary));
        farmstoreText.setTextColor(getResources().getColor(R.color.black));
        accountText.setTextColor(getResources().getColor(R.color.black));

        farmstoreSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent farmstoreIntent = new Intent(Dashboard.this, FarmShop.class);
                startActivity(farmstoreIntent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });
        accountSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent accountIntent = new Intent(Dashboard.this, Account.class);
                startActivity(accountIntent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });


        /*---   CURRENT USER   ---*/
        //singletonUser = ((ApplicationClass)(getApplicationContext())).getUser();
        paperUser = Paper.book().read(Common.PAPER_USER);

        //show loading dialog
        showLoadingDialog("Fetching details . . .");

        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

                //check all cases
                if (output == 1){

                    //always update latest from server
                    userRef.child(currentUid)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    UserModel theUser = dataSnapshot.getValue(UserModel.class);
                                    ((ApplicationClass) (getApplicationContext())).setUser(theUser);

                                    paperUser = Paper.book().read(Common.PAPER_USER);

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                    if (paperUser == null) {

                        userRef.child(currentUid)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        UserModel theUser = dataSnapshot.getValue(UserModel.class);
                                        ((ApplicationClass) (getApplicationContext())).setUser(theUser);

                                        paperUser = Paper.book().read(Common.PAPER_USER);
                                        setUser(paperUser);
                                        setOtherDetails();

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                    } else {

                        setUser(paperUser);
                        setOtherDetails();

                    }

                } else

                if (output == 0){

                    if (paperUser != null) {

                        setUser(paperUser);
                        Toast.makeText(Dashboard.this, "No internet", Toast.LENGTH_SHORT).show();

                    } else {

                        alertDialog.dismiss();
                        showErrorDialog("Details could not be retrieved, please try again later.");

                    }

                } else

                if (output == 2){

                    if (paperUser != null) {

                        setUser(paperUser);
                        Toast.makeText(Dashboard.this, "Please connect to a network", Toast.LENGTH_SHORT).show();

                    } else {

                        alertDialog.dismiss();
                        showErrorDialog("Details could not be retrieved, please try again later.");

                    }

                }

            }
        }).execute();


        //check sponsorship monitor
        if (Paper.book().read(Common.isSponsorshipMonitorRunning) == null)
            Paper.book().write(Common.isSponsorshipMonitorRunning, false);
        isMonitorRunning = Paper.book().read(Common.isSponsorshipMonitorRunning);



        /*---   CART   ---*/
        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent cartIntent = new Intent(Dashboard.this, Cart.class);
                startActivity(cartIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });


        /*---   GO TO SHOP   ---*/
        goToFarmstoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent farmShopIntent = new Intent(Dashboard.this, FarmShop.class);
                startActivity(farmShopIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });


        /*---   MAIN MENU   ---*/
        sponsoredFarmsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent sponsoredFarmsIntent = new Intent(Dashboard.this, SponsoredFarms.class);
                startActivity(sponsoredFarmsIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });

        farmsToWatchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent followedFarmsIntent = new Intent(Dashboard.this, FollowedFarms.class);
                startActivity(followedFarmsIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });

        farmUpdatesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent farmUpdatesIntent = new Intent(Dashboard.this, FarmUpdates.class);
                startActivity(farmUpdatesIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });

        allFarmsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent farmShopIntent = new Intent(Dashboard.this, FarmShop.class);
                startActivity(farmShopIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });

        projectManagerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent projectManagerIntent = new Intent(Dashboard.this, AccountManager.class);
                startActivity(projectManagerIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });

        notificationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent notificationIntent = new Intent(Dashboard.this, Notifications.class);
                startActivity(notificationIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });

        faqLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent faqIntent = new Intent(Dashboard.this, Faq.class);
                startActivity(faqIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });

    }

    private void setOtherDetails() {

        /*---   SPONSORED CYCLE   ---*/
        sponsoredRef.child(currentUid)
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){

                                    long totalReturn = 0;
                                    int sponsorCount = (int) dataSnapshot.getChildrenCount();
                                    sponsorCycle.setText("Running Cycles : " + String.valueOf(sponsorCount));

                                    for (DataSnapshot user : dataSnapshot.getChildren()){

                                        long theReturn = Long.parseLong(user.child("sponsorReturn").getValue().toString());
                                        totalReturn = totalReturn + theReturn;

                                    }

                                    totalReturnsText.setText(Common.convertToPrice(Dashboard.this, totalReturn));



                                    if (!isMonitorRunning) {

                                        Intent checkSponsorship = new Intent(Dashboard.this, CheckForSponsorship.class);
                                        startService(checkSponsorship);

                                    }

                                } else {

                                    sponsorCycle.setText("Running Cycles : 0");
                                    totalReturnsText.setText("₦ 0.00");

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

        /*---   NEXT END OF CYCLE   ---*/
        sponsoredRef.child(currentUid)
                .limitToFirst(1)
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                for (DataSnapshot snap : dataSnapshot.getChildren()){

                                    String theKey = snap.getKey();

                                    if (dataSnapshot.exists()) {

                                        String date = dataSnapshot.child(theKey).child("cycleEndDate").getValue().toString();
                                        nextEndOfCycleDate.setText(date);

                                    } else {

                                        nextEndOfCycleDate.setText("Not Available");

                                    }

                                }



                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

    }

    private void setUser(final UserModel paperUser) {

        //close loading dialog
        alertDialog.dismiss();

        //welcome message
        welcome.setText("Hi, " + paperUser.getFirstName());

        //user profile pic
        if (!paperUser.getProfilePictureThumb().equalsIgnoreCase("")){

            Picasso.get()
                    .load(paperUser.getProfilePictureThumb())
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.profile)
                    .into(userAvatar, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(paperUser.getProfilePictureThumb())
                                    .placeholder(R.drawable.profile)
                                    .into(userAvatar);
                        }
                    });

        } else {

            userAvatar.setImageResource(R.drawable.profile);

        }

        //user type
        if (paperUser.getUserType().equalsIgnoreCase("Admin")){

            adminLayout.setVisibility(View.VISIBLE);
            adminLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent adminIntent = new Intent(Dashboard.this, AdminDash.class);
                    startActivity(adminIntent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
                }
            });

        } else if (paperUser.getUserType().equalsIgnoreCase("Banned")) {

            Paper.book().destroy();

            mAuth.signOut();
            ((ApplicationClass)(getApplicationContext())).resetUser();

            FirebaseMessaging.getInstance().unsubscribeFromTopic(currentUid);
            FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.GENERAL_NOTIFY);
            Intent signoutIntent = new Intent(Dashboard.this, SignIn.class);
            signoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(signoutIntent);
            finish();

        } else {

            adminLayout.setVisibility(View.INVISIBLE);
            adminLayout.setEnabled(false);

        }

    }

    /*---   LOADING DIALOG   ---*/
    public void showLoadingDialog(String theMessage){

        alertDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.loading_dialog,null);

        final TextView loadingText = viewOptions.findViewById(R.id.loadingText);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        loadingText.setText(theMessage);

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });

        alertDialog.show();

    }

    /*---   WARNING DIALOG   ---*/
    public void showErrorDialog(String theWarning){

        alertDialogError = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.dialog_layout,null);

        final TextView message = (TextView) viewOptions.findViewById(R.id.dialogMessage);
        final Button okButton = (Button) viewOptions.findViewById(R.id.dialogButton);

        alertDialogError.setView(viewOptions);

        alertDialogError.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialogError.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        message.setText(theWarning);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogError.dismiss();
            }
        });

        alertDialogError.show();

    }
}
