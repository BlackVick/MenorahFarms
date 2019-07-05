package com.blackviking.menorahfarms.HomeActivities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blackviking.menorahfarms.R;
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

public class Account extends AppCompatActivity {

    private LinearLayout homeSwitch, dashboardSwitch, farmstoreSwitch, accountSwitch;
    private TextView homeText, dashboardText, farmstoreText, accountText;

    private TextView userName, userEmail, profileProgressText;
    private ImageView cartButton, backButton;
    private Button resetPassword;
    private CircleImageView userAvatar;
    private ProgressBar profileProgress;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
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

        userName = (TextView)findViewById(R.id.userFullName);
        userEmail = (TextView)findViewById(R.id.userEmail);
        profileProgressText = (TextView)findViewById(R.id.profileProgressText);
        backButton = (ImageView)findViewById(R.id.backButton);
        cartButton = (ImageView)findViewById(R.id.cartButton);
        resetPassword = (Button)findViewById(R.id.changePasswordButton);
        resetPassword.setEnabled(false);
        userAvatar = (CircleImageView)findViewById(R.id.userAvatar);
        profileProgress = (ProgressBar)findViewById(R.id.profileProgress);


        /*---   BOTTOM NAV   ---*/
        homeSwitch.setBackgroundResource(R.drawable.white_backround);
        homeText.setTextColor(getResources().getColor(R.color.black));
        dashboardSwitch.setBackgroundResource(R.drawable.white_backround);
        dashboardText.setTextColor(getResources().getColor(R.color.black));
        farmstoreSwitch.setBackgroundResource(R.drawable.white_backround);
        farmstoreText.setTextColor(getResources().getColor(R.color.black));
        accountSwitch.setBackgroundResource(R.drawable.off_white_backround);
        accountText.setTextColor(getResources().getColor(R.color.colorPrimary));


        dashboardSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent dashboardIntent = new Intent(Account.this, Dashboard.class);
                startActivity(dashboardIntent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });
        farmstoreSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent farmstoreIntent = new Intent(Account.this, FarmShop.class);
                startActivity(farmstoreIntent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });
        homeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent homeIntent = new Intent(Account.this, Home.class);
                startActivity(homeIntent);
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
                        String userLastName = dataSnapshot.child("lastName").getValue().toString();
                        final String profilePicture = dataSnapshot.child("profilePictureThumb").getValue().toString();
                        final String theUserEmail = dataSnapshot.child("email").getValue().toString();
                        final String loginType = dataSnapshot.child("signUpMode").getValue().toString();

                        userName.setText(userFirstName + " " + userLastName);
                        userEmail.setText(theUserEmail);

                        if (!profilePicture.equalsIgnoreCase("")){

                            Picasso.with(getBaseContext())
                                    .load(profilePicture)
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.profile)
                                    .into(userAvatar, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(getBaseContext())
                                                    .load(profilePicture)
                                                    .placeholder(R.drawable.profile)
                                                    .into(userAvatar);
                                        }
                                    });

                        } else {

                            userAvatar.setImageResource(R.drawable.profile);

                        }

                        if (loginType.equalsIgnoreCase("Email")){

                            resetPassword.setVisibility(View.VISIBLE);
                            resetPassword.setEnabled(true);

                        } else {

                            resetPassword.setVisibility(View.INVISIBLE);
                            resetPassword.setEnabled(false);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        setProfileProgress();

    }

    private void setProfileProgress() {

        Drawable draw = getResources().getDrawable(R.drawable.progress_drawable);
        profileProgress.setProgressDrawable(draw);
        profileProgress.setProgress(40);

    }
}
