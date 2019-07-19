package com.blackviking.menorahfarms.HomeActivities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.AccountMenus.BankDetails;
import com.blackviking.menorahfarms.AccountMenus.ContactDetails;
import com.blackviking.menorahfarms.AccountMenus.NextOfKin;
import com.blackviking.menorahfarms.AccountMenus.PersonalDetails;
import com.blackviking.menorahfarms.AccountMenus.SocialMedia;
import com.blackviking.menorahfarms.AccountMenus.StudentDetails;
import com.blackviking.menorahfarms.CartAndHistory.Cart;
import com.blackviking.menorahfarms.CartAndHistory.SponsorshipHistory;
import com.blackviking.menorahfarms.Models.UserModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.SignIn;
import com.facebook.login.LoginManager;
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

public class Account extends AppCompatActivity {

    private LinearLayout homeSwitch, dashboardSwitch, farmstoreSwitch, accountSwitch;
    private TextView homeText, dashboardText, farmstoreText, accountText;

    private TextView userName, userEmail; //profileProgressText;
    private ImageView cartButton;
    private Button resetPassword;
    private CircleImageView userAvatar;
    //private ProgressBar profileProgress;

    private LinearLayout verifiedLayout;
    private RelativeLayout unverifiedLayout;

    private LinearLayout personalDetailsLayout, contactDetailsLayout, bankDetailsLayout, nextOfKinLayout, socialMediaLayout, studentProfileLayout, historyLayout, logOutLayout;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef;
    private String currentUid, loginType;


    private String theUserMail, theFirstName, theLastName, theProfilePicture,
    theFacebook, theInstagram, theTwitter, theLinkedIn, thePhone, theBirthday, theGender,
    theNationality, theAddress, theCity, theState, theBank, theAccountName, theAccountNumber,
    theKinMail, theKinName, theKinrelationship, theKinPhone, theKinAddress;

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
        //profileProgressText = (TextView)findViewById(R.id.profileProgressText);
        cartButton = (ImageView)findViewById(R.id.cartButton);
        resetPassword = (Button)findViewById(R.id.changePasswordButton);
        resetPassword.setEnabled(false);
        userAvatar = (CircleImageView)findViewById(R.id.userAvatar);
        //profileProgress = (ProgressBar)findViewById(R.id.profileProgress);

        personalDetailsLayout = (LinearLayout)findViewById(R.id.personalDetailsLayout);
        contactDetailsLayout = (LinearLayout)findViewById(R.id.contactDetailsLayout);
        bankDetailsLayout = (LinearLayout)findViewById(R.id.bankDetailsLayout);
        nextOfKinLayout = (LinearLayout)findViewById(R.id.nextOfKinLayout);
        socialMediaLayout = (LinearLayout)findViewById(R.id.socialMediaLayout);
        studentProfileLayout = (LinearLayout)findViewById(R.id.studentProfileLayout);
        historyLayout = (LinearLayout)findViewById(R.id.historyLayout);
        logOutLayout = (LinearLayout)findViewById(R.id.logOutLayout);

        verifiedLayout = (LinearLayout)findViewById(R.id.verifiedLayout);
        unverifiedLayout = (RelativeLayout)findViewById(R.id.unverifiedLayout);


        /*---   BOTTOM NAV   ---*/
        homeText.setTextColor(getResources().getColor(R.color.black));
        dashboardText.setTextColor(getResources().getColor(R.color.black));
        farmstoreText.setTextColor(getResources().getColor(R.color.black));
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

        setCurrentUser();


        /*---   CART   ---*/
        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent cartIntent = new Intent(Account.this, Cart.class);
                startActivity(cartIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });


        personalDetailsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent personalIntent = new Intent(Account.this, PersonalDetails.class);
                startActivity(personalIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
            }
        });

        contactDetailsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent conTactIntent = new Intent(Account.this, ContactDetails.class);
                startActivity(conTactIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
            }
        });

        bankDetailsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bankIntent = new Intent(Account.this, BankDetails.class);
                startActivity(bankIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
            }
        });

        nextOfKinLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextOfKinIntent = new Intent(Account.this, NextOfKin.class);
                startActivity(nextOfKinIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
            }
        });

        socialMediaLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent socialIntent = new Intent(Account.this, SocialMedia.class);
                startActivity(socialIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
            }
        });

        studentProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent studentIntent = new Intent(Account.this, StudentDetails.class);
                startActivity(studentIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
            }
        });

        historyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent historyIntent = new Intent(Account.this, SponsorshipHistory.class);
                startActivity(historyIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
            }
        });

        logOutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (loginType.equalsIgnoreCase("Facebook")){

                    Paper.book().destroy();

                    mAuth.signOut();
                    LoginManager.getInstance().logOut();

                    Intent signoutIntent = new Intent(Account.this, SignIn.class);
                    signoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(signoutIntent);
                    finish();

                } else {

                    Paper.book().destroy();

                    mAuth.signOut();

                    Intent signoutIntent = new Intent(Account.this, SignIn.class);
                    signoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(signoutIntent);
                    finish();

                }

            }
        });






    }

    private void setCurrentUser() {

        userRef.child(currentUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        UserModel currentUser = dataSnapshot.getValue(UserModel.class);

                        if (currentUser != null) {
                            theFirstName = currentUser.getFirstName();
                            theLastName = currentUser.getLastName();
                            theUserMail = currentUser.getEmail();
                            theProfilePicture = currentUser.getProfilePictureThumb();
                            theFacebook = currentUser.getFacebook();
                            theInstagram = currentUser.getInstagram();
                            theTwitter = currentUser.getTwitter();
                            theLinkedIn = currentUser.getLinkedIn();
                            thePhone = currentUser.getPhone();
                            theBirthday = currentUser.getBirthday();
                            theGender = currentUser.getGender();
                            theNationality = currentUser.getNationality();
                            theAddress = currentUser.getAddress();
                            theCity = currentUser.getCity();
                            theState = currentUser.getState();
                            theBank = currentUser.getBank();
                            theAccountName = currentUser.getAccountName();
                            theAccountNumber = currentUser.getAccountNumber();
                            theKinMail = currentUser.getKinEmail();
                            theKinName = currentUser.getKinName();
                            theKinrelationship = currentUser.getKinRelationship();
                            theKinPhone = currentUser.getKinPhone();
                            theKinAddress = currentUser.getKinAddress();
                            loginType = currentUser.getSignUpMode();

                            userName.setText(theFirstName + " " + theLastName);
                            userEmail.setText(theUserMail);

                            if (!theProfilePicture.equalsIgnoreCase("")) {

                                Picasso.get()
                                        .load(theProfilePicture)
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .placeholder(R.drawable.profile)
                                        .into(userAvatar, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                Picasso.get()
                                                        .load(theProfilePicture)
                                                        .placeholder(R.drawable.profile)
                                                        .into(userAvatar);
                                            }
                                        });

                            } else {

                                userAvatar.setImageResource(R.drawable.profile);

                            }

                            if (loginType.equalsIgnoreCase("Email")) {

                                resetPassword.setVisibility(View.VISIBLE);
                                resetPassword.setEnabled(true);

                            } else {

                                resetPassword.setVisibility(View.INVISIBLE);
                                resetPassword.setEnabled(false);

                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        //setProfileProgress();


    }

    private void setProfileProgress() {

        String[] theArray = {theUserMail, theFirstName, theLastName, theProfilePicture,
                theFacebook, theInstagram, theTwitter, theLinkedIn, thePhone, theBirthday, theGender,
                theNationality, theAddress, theCity, theState, theBank, theAccountName, theAccountNumber,
                theKinMail, theKinName, theKinrelationship, theKinPhone, theKinAddress};

        int profileProgressInt = 0;

        if (!theUserMail.equalsIgnoreCase("")){

            profileProgressInt++;

        }

        if (!TextUtils.isEmpty(theFirstName)){

            profileProgressInt++;

        }

        if (!TextUtils.isEmpty(theLastName)){

            profileProgressInt++;

        }

        if (!TextUtils.isEmpty(theProfilePicture)){

            profileProgressInt++;

        }

        if (!TextUtils.isEmpty(theFacebook)){

            profileProgressInt++;

        }

        if (!TextUtils.isEmpty(theInstagram)){

            profileProgressInt++;

        }

        if (!TextUtils.isEmpty(theTwitter)){

            profileProgressInt++;

        }

        if (!TextUtils.isEmpty(theLinkedIn)){

            profileProgressInt++;

        }

        if (!TextUtils.isEmpty(thePhone)){

            profileProgressInt++;

        }

        if (!TextUtils.isEmpty(theBirthday)){

            profileProgressInt++;

        }

        if (!TextUtils.isEmpty(theGender)){

            profileProgressInt++;

        }

        if (!TextUtils.isEmpty(theNationality)){

            profileProgressInt++;

        }

        if (!TextUtils.isEmpty(theAddress)){

            profileProgressInt++;

        }

        if (!TextUtils.isEmpty(theCity)){

            profileProgressInt++;

        }

        if (!TextUtils.isEmpty(theState)){

            profileProgressInt++;

        }

        if (!TextUtils.isEmpty(theBank)){

            profileProgressInt++;

        }

        if (!TextUtils.isEmpty(theAccountName)){

            profileProgressInt++;

        }

        if (!TextUtils.isEmpty(theAccountNumber)){

            profileProgressInt++;

        }

        if (!TextUtils.isEmpty(theKinMail)){

            profileProgressInt++;

        }

        if (!TextUtils.isEmpty(theKinName)){

            profileProgressInt++;

        }

        if (!TextUtils.isEmpty(theKinrelationship)){

            profileProgressInt++;

        }

        if (!TextUtils.isEmpty(theKinPhone)){

            profileProgressInt++;

        }

        if (!TextUtils.isEmpty(theKinAddress)){

            profileProgressInt++;

        } else {

            /*int calcdProgress = profileProgressInt / theArray.length;
            int percentedTotal = calcdProgress * 100;

            profileProgressText.setText("Your profile is " + String.valueOf(percentedTotal) + "% complete.");

            Drawable draw = getResources().getDrawable(R.drawable.progress_drawable);
            profileProgress.setProgressDrawable(draw);
            profileProgress.setProgress(percentedTotal);*/

        }


        /*for (int i = 0; i < theArray.length; i++){

            Toast.makeText(this, ""+theArray[i], Toast.LENGTH_SHORT).show();

            *//*if (!TextUtils.isEmpty(theArray[i]))
                profileProgressInt++;



            *//*

        }*/



    }
}
