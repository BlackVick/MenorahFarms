package com.blackviking.menorahfarms.AccountMenus;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class SocialMedia extends AppCompatActivity {

    private MaterialEditText profileFacebook, profileInstagram, profileTwitter, profileLinkedin;
    private Button updateProfile;
    private ImageView backButton;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef;
    private String currentUid;
    private android.app.AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_media);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        profileFacebook = (MaterialEditText)findViewById(R.id.profileFacebook);
        profileInstagram = (MaterialEditText)findViewById(R.id.profileInstagram);
        profileTwitter = (MaterialEditText)findViewById(R.id.profileTwitter);
        profileLinkedin = (MaterialEditText)findViewById(R.id.profileLinkedIn);
        backButton = (ImageView)findViewById(R.id.backButton);
        updateProfile = (Button)findViewById(R.id.updateProfileButton);


        /*---   CURRENT USER   ---*/
        userRef.child(currentUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String theFacebook = dataSnapshot.child("facebook").getValue().toString();
                        String theInstagram = dataSnapshot.child("instagram").getValue().toString();
                        String theTwitter = dataSnapshot.child("twitter").getValue().toString();
                        String theLinkedIn = dataSnapshot.child("linkedIn").getValue().toString();

                        profileFacebook.setText(theFacebook);
                        profileInstagram.setText(theInstagram);
                        profileTwitter.setText(theTwitter);
                        profileLinkedin.setText(theLinkedIn);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



        /*---   UPDATE   ---*/
        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())) {
                    updateChanges();
                } else {
                    Common.showErrorDialog(SocialMedia.this, "No Internet Access !");
                }
            }
        });
    }

    private void updateChanges() {

        mDialog = new SpotsDialog(SocialMedia.this, "Updating . . .");
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        String theNewFacebook = profileFacebook.getText().toString().trim();
        String theNewInstagram = profileInstagram.getText().toString().trim();
        String theNewTwitter = profileTwitter.getText().toString().trim();
        String theNewLinkedIn = profileLinkedin.getText().toString().trim();


        final Map<String, Object> userMap = new HashMap<>();
        userMap.put("facebook", theNewFacebook);
        userMap.put("instagram", theNewInstagram);
        userMap.put("twitter", theNewTwitter);
        userMap.put("linkedIn", theNewLinkedIn);


        userRef.child(currentUid)
                .updateChildren(userMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        mDialog.dismiss();
                        finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }
}
