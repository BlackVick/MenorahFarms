package com.blackviking.menorahfarms.HomeActivities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.menorahfarms.About;
import com.blackviking.menorahfarms.AdminDash;
import com.blackviking.menorahfarms.CartAndHistory.Cart;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.DashboardMenu.AccountManager;
import com.blackviking.menorahfarms.DashboardMenu.Faq;
import com.blackviking.menorahfarms.DashboardMenu.FarmUpdates;
import com.blackviking.menorahfarms.DashboardMenu.FollowedFarms;
import com.blackviking.menorahfarms.DashboardMenu.Notifications;
import com.blackviking.menorahfarms.DashboardMenu.SponsoredFarms;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.Services.SponsorshipMonitor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import io.paperdb.Paper;

public class Dashboard extends AppCompatActivity {

    private LinearLayout homeSwitch, dashboardSwitch, farmstoreSwitch, accountSwitch;
    private TextView homeText, dashboardText, farmstoreText, accountText;

    private TextView welcome, sponsorCycle, totalReturnsText, nextEndOfCycleDate;
    private ImageView cartButton;
    private CircleImageView userAvatar;
    private RelativeLayout goToFarmstoreButton;
    private LinearLayout sponsoredFarmsLayout, farmsToWatchLayout, farmUpdatesLayout, allFarmsLayout, projectManagerLayout, notificationLayout, faqLayout, about, adminLayout;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, sponsoredRef;
    private String currentUid;
    private boolean isMonitorRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        sponsoredRef = db.getReference("SponsoredFarms");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        homeSwitch = (LinearLayout)findViewById(R.id.homeLayout);
        dashboardSwitch = (LinearLayout)findViewById(R.id.dashboardLayout);
        farmstoreSwitch = (LinearLayout)findViewById(R.id.farmShopLayout);
        accountSwitch = (LinearLayout)findViewById(R.id.accountLayout);
        homeText = (TextView)findViewById(R.id.homeText);
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
        sponsoredFarmsLayout = (LinearLayout)findViewById(R.id.sponsoredFarmsLayout);
        farmsToWatchLayout = (LinearLayout)findViewById(R.id.farmsToWatchLayout);
        farmUpdatesLayout = (LinearLayout)findViewById(R.id.farmUpdatesLayout);
        allFarmsLayout = (LinearLayout)findViewById(R.id.allFarmsLayout);
        projectManagerLayout = (LinearLayout)findViewById(R.id.projectManagerLayout);
        notificationLayout = (LinearLayout)findViewById(R.id.notificationLayout);
        faqLayout = (LinearLayout)findViewById(R.id.faqLayout);
        about = (LinearLayout)findViewById(R.id.aboutLayout);
        adminLayout = (LinearLayout)findViewById(R.id.adminLayout);


        /*---   BOTTOM NAV   ---*/
        homeText.setTextColor(getResources().getColor(R.color.black));
        dashboardText.setTextColor(getResources().getColor(R.color.colorPrimary));
        farmstoreText.setTextColor(getResources().getColor(R.color.black));
        accountText.setTextColor(getResources().getColor(R.color.black));

        homeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent homeIntent = new Intent(Dashboard.this, Home.class);
                startActivity(homeIntent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });
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
        userRef.child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String userFirstName = dataSnapshot.child("firstName").getValue().toString();
                        final String profilePicture = dataSnapshot.child("profilePictureThumb").getValue().toString();
                        final String profileType = dataSnapshot.child("userType").getValue().toString();

                        welcome.setText("Hi, "+userFirstName);

                        if (!profilePicture.equalsIgnoreCase("")){

                            Picasso.get()
                                    .load(profilePicture)
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.profile)
                                    .into(userAvatar, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Picasso.get()
                                                    .load(profilePicture)
                                                    .placeholder(R.drawable.profile)
                                                    .into(userAvatar);
                                        }
                                    });

                        } else {

                            userAvatar.setImageResource(R.drawable.profile);

                        }


                        if (profileType.equalsIgnoreCase("Admin")){

                            adminLayout.setVisibility(View.VISIBLE);
                            adminLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent adminIntent = new Intent(Dashboard.this, AdminDash.class);
                                    startActivity(adminIntent);
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
                                }
                            });

                        } else {

                            adminLayout.setVisibility(View.GONE);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        if (Paper.book().read(Common.isSponsorshipMonitorRunning) == null)
            Paper.book().write(Common.isSponsorshipMonitorRunning, false);
        isMonitorRunning = Paper.book().read(Common.isSponsorshipMonitorRunning);


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

                                        Intent sponsorshipMonitor = new Intent(Dashboard.this, SponsorshipMonitor.class);
                                        startService(sponsorshipMonitor);

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
                .addListenerForSingleValueEvent(
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

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent aboutIntent = new Intent(Dashboard.this, About.class);
                startActivity(aboutIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });
    }
}
