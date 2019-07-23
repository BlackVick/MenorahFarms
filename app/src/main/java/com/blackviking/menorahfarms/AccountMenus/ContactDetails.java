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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class ContactDetails extends AppCompatActivity {

    private MaterialEditText profileAddress, profileCity, profileState;
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
        setContentView(R.layout.activity_contact_details);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        profileAddress = (MaterialEditText)findViewById(R.id.profileAddress);
        profileCity = (MaterialEditText)findViewById(R.id.profileCity);
        profileState = (MaterialEditText)findViewById(R.id.profileState);
        backButton = (ImageView)findViewById(R.id.backButton);
        updateProfile = (Button)findViewById(R.id.updateProfileButton);


        /*---   CURRENT USER   ---*/
        userRef.child(currentUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String theAddress = dataSnapshot.child("address").getValue().toString();
                        String theCity = dataSnapshot.child("city").getValue().toString();
                        String theState = dataSnapshot.child("state").getValue().toString();

                        profileAddress.setText(theAddress);
                        profileCity.setText(theCity);
                        profileState.setText(theState);

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
                    Common.showErrorDialog(ContactDetails.this, "No Internet Access !", ContactDetails.this);
                }
            }
        });
    }

    private void updateChanges() {

        mDialog = new SpotsDialog(ContactDetails.this, "Updating . . .");
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        String theNewAddress = profileAddress.getText().toString().trim();
        String theNewCity = profileCity.getText().toString().trim();
        String theNewState = profileState.getText().toString().trim();


        final Map<String, Object> userMap = new HashMap<>();
        userMap.put("address", theNewAddress);
        userMap.put("city", theNewCity);
        userMap.put("state", theNewState);


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
