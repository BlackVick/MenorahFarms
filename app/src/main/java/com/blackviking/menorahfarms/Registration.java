package com.blackviking.menorahfarms;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.HomeActivities.Dashboard;
import com.blackviking.menorahfarms.HomeActivities.Home;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class Registration extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private ImageView backButton;
    private MaterialEditText registerFirstName, registerLastName;
    private Button signupButton;
    private android.app.AlertDialog mDialog;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, authed;
    private String currentUid, signUpType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        authed = db.getReference("AuthedUsers");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();



        /*---   WIDGETS   ---*/
        backButton = (ImageView)findViewById(R.id.backButton);
        registerFirstName = (MaterialEditText)findViewById(R.id.registerFirstName);
        registerLastName = (MaterialEditText)findViewById(R.id.registerLastName);
        signupButton = (Button)findViewById(R.id.signUpButton);


        /*---   CURRENT USER   ---*/
        userRef.child(currentUid)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                String serverFirstName = dataSnapshot.child("firstName").getValue().toString();
                                String serverLastName = dataSnapshot.child("lastName").getValue().toString();
                                signUpType = dataSnapshot.child("signUpMode").getValue().toString();

                                registerFirstName.setText(serverFirstName);
                                registerLastName.setText(serverLastName);


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );


        /*---   SAVE CHANGES   ---*/
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getBaseContext())) {

                    mDialog = new SpotsDialog(Registration.this, "Writing Changes . . .");
                    mDialog.setCancelable(false);
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                    saveChanges();

                } else {

                    Common.showErrorDialog(Registration.this, "No Internet Access !");

                }
            }
        });


        /*---    BACK   ---*/
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetSignIn();
            }
        });
    }

    private void saveChanges() {

        String theFirstName = registerFirstName.getText().toString().trim();
        String theLastName = registerLastName.getText().toString().trim();

        if (TextUtils.isEmpty(theFirstName)){

            registerFirstName.setError("Provide Your First Name");
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .playOn(signupButton);
            mDialog.dismiss();
            registerFirstName.requestFocus();

        } else if (TextUtils.isEmpty(theLastName)){

            registerLastName.setError("Provide Your Last Name");
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .playOn(signupButton);
            mDialog.dismiss();
            registerLastName.requestFocus();

        } else {

            final Map<String, Object> userMap = new HashMap<>();
            userMap.put("firstName", theFirstName);
            userMap.put("lastName", theLastName);

            userRef.child(currentUid)
                    .updateChildren(userMap).addOnSuccessListener(
                    new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            goToHome(currentUid);

                        }
                    }
            ).addOnFailureListener(
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Common.showErrorDialog(Registration.this, "Error Occurred, Please Try Again Later.");
                        }
                    }
            );

        }

    }

    private void goToHome(String currentUid) {

        mDialog.dismiss();

        Paper.book().write(Common.USER_ID, currentUid);

        FirebaseMessaging.getInstance().subscribeToTopic(currentUid);

        Intent homeIntent = new Intent(Registration.this, Dashboard.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
        finish();

    }

    private void resetSignIn() {

        if (signUpType.equalsIgnoreCase("Facebook")){

            userRef.child(currentUid).removeValue();
            authed.child(currentUid).removeValue();
            LoginManager.getInstance().logOut();
            mAuth.signOut();
            mAuth.getCurrentUser().delete();
            finish();

        } else {

            userRef.child(currentUid).removeValue();
            authed.child(currentUid).removeValue();
            mAuth.signOut();
            mAuth.getCurrentUser().delete();
            finish();

        }

    }

    @Override
    public void onBackPressed() {
        resetSignIn();
    }
}
