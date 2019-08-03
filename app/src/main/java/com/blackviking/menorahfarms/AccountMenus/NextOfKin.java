package com.blackviking.menorahfarms.AccountMenus;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

public class NextOfKin extends AppCompatActivity {

    private MaterialEditText kinName, kinEmail, kinRelationship, kinPhone, kinAddress;
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
        setContentView(R.layout.activity_next_of_kin);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        kinName = (MaterialEditText)findViewById(R.id.kinName);
        kinEmail = (MaterialEditText)findViewById(R.id.kinEmail);
        kinRelationship = (MaterialEditText)findViewById(R.id.kinRelationship);
        kinPhone = (MaterialEditText)findViewById(R.id.kinPhone);
        kinAddress = (MaterialEditText)findViewById(R.id.kinAddress);
        backButton = (ImageView)findViewById(R.id.backButton);
        updateProfile = (Button)findViewById(R.id.updateProfileButton);


        /*---   CURRENT USER   ---*/
        userRef.child(currentUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String theName = dataSnapshot.child("kinName").getValue().toString();
                        String theEmail= dataSnapshot.child("kinEmail").getValue().toString();
                        String theRelationship = dataSnapshot.child("kinRelationship").getValue().toString();
                        String thePhone = dataSnapshot.child("kinPhone").getValue().toString();
                        String theAddress = dataSnapshot.child("kinAddress").getValue().toString();

                        kinAddress.setText(theAddress);
                        kinName.setText(theName);
                        kinRelationship.setText(theRelationship);
                        kinPhone.setText(thePhone);
                        kinEmail.setText(theEmail);

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
                    showErrorDialog("No Internet Access !");
                }
            }
        });
    }

    private void updateChanges() {

        mDialog = new SpotsDialog(NextOfKin.this, "Updating . . .");
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        String theNewName = kinName.getText().toString().trim();
        String theNewEmail = kinEmail.getText().toString().trim();
        String theNewRelationship = kinRelationship.getText().toString().trim();
        String theNewPhone = kinPhone.getText().toString().trim();
        String theNewAddress = kinAddress.getText().toString().trim();


        final Map<String, Object> userMap = new HashMap<>();
        userMap.put("kinName", theNewName);
        userMap.put("kinEmail", theNewEmail);
        userMap.put("kinRelationship", theNewRelationship);
        userMap.put("kinPhone", theNewPhone);
        userMap.put("kinAddress", theNewAddress);


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
}
