package com.blackviking.menorahfarms;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.ApplicationClass;
import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.HomeActivities.Dashboard;
import com.blackviking.menorahfarms.Models.UserModel;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
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
        userRef = db.getReference(Common.USERS_NODE);
        authed = db.getReference(Common.AUTHED_USERS_NODE);
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();



        /*---   WIDGETS   ---*/
        backButton = findViewById(R.id.backButton);
        registerFirstName = findViewById(R.id.registerFirstName);
        registerLastName = findViewById(R.id.registerLastName);
        signupButton = findViewById(R.id.signUpButton);


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
        signupButton.setOnClickListener(v -> {

            //show dialog
            mDialog = new SpotsDialog(Registration.this, "Writing Changes . . .");
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();

            //execute network check async task
            new CheckInternet(Registration.this, output -> {

                //check all cases
                if (output == 1){

                    saveChanges();

                } else

                if (output == 0){

                    //no internet
                    mDialog.dismiss();
                    Toast.makeText(this, "No internet access", Toast.LENGTH_SHORT).show();

                } else

                if (output == 2){

                    //no internet
                    mDialog.dismiss();
                    Toast.makeText(this, "Not connected to any network", Toast.LENGTH_LONG).show();

                }

            }).execute();

        });


        /*---    BACK   ---*/
        backButton.setOnClickListener(v -> resetSignIn());
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
                    .updateChildren(userMap)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()){

                            goToHome(currentUid);

                        } else {

                            mDialog.dismiss();
                            Toast.makeText(Registration.this, "Error occurred, please try again later.", Toast.LENGTH_LONG).show();

                        }

                    });

        }

    }

    private void goToHome(String currentUid) {

        //save user profile offline
        userRef.child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        UserModel user = dataSnapshot.getValue(UserModel.class);

                        ((ApplicationClass)(getApplicationContext())).setUser(user);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        //dismiss dialog
        mDialog.dismiss();

        //save user is locally
        Paper.book().write(Common.USER_ID, currentUid);

        //subscribe to notifications
        FirebaseMessaging.getInstance().subscribeToTopic(currentUid);
        FirebaseMessaging.getInstance().subscribeToTopic(Common.GENERAL_NOTIFY);

        //launch activity and clear back stack
        Intent homeIntent = new Intent(Registration.this, Dashboard.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();

    }

    private void resetSignIn() {

        mAuth.getCurrentUser().delete();
        userRef.child(currentUid).removeValue();
        authed.child(currentUid).removeValue();
        mAuth.signOut();
        finish();

    }

    @Override
    public void onBackPressed() {
        resetSignIn();
    }

}
