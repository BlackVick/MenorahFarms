package com.blackviking.menorahfarms.AccountMenus;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.BankModel;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class BankDetails extends AppCompatActivity {

    private MaterialEditText profileAccountName, profileAccountNumber;
    private Spinner profileBank;
    private Button updateProfile;
    private ImageView backButton;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef, bankRef;
    private String currentUid;
    private android.app.AlertDialog mDialog;
    private String selectedBank = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_details);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        bankRef = db.getReference("Banks");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        profileAccountName = (MaterialEditText)findViewById(R.id.profileAccountName);
        profileAccountNumber = (MaterialEditText)findViewById(R.id.profileAccountNumber);
        profileBank = (Spinner) findViewById(R.id.bankSpinner);
        backButton = (ImageView)findViewById(R.id.backButton);
        updateProfile = (Button)findViewById(R.id.updateProfileButton);


        /*---   CURRENT USER   ---*/
        userRef.child(currentUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String theBank = dataSnapshot.child("bank").getValue().toString();
                        String theAccountName = dataSnapshot.child("accountName").getValue().toString();
                        String theAccountNumber = dataSnapshot.child("accountNumber").getValue().toString();

                        profileAccountName.setText(theAccountName);
                        profileAccountNumber.setText(theAccountNumber);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        /*---   BANK SPINNER   ---*/
        final List<String> bankList = new ArrayList<>();
        bankList.add(0, "Bank");

        bankRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot child : dataSnapshot.getChildren()){

                    BankModel bankModel = child.getValue(BankModel.class);
                    bankList.add(bankModel.getBankName());

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ArrayAdapter<String> dataAdapterGender;
        dataAdapterGender = new ArrayAdapter(this, android.R.layout.simple_spinner_item, bankList);
        dataAdapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profileBank.setAdapter(dataAdapterGender);
        profileBank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).equals("Bank")){

                    selectedBank = parent.getItemAtPosition(position).toString();

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
                    Common.showErrorDialog(BankDetails.this, "No Internet Access !", BankDetails.this);
                }
            }
        });
    }

    private void updateChanges() {

        mDialog = new SpotsDialog(BankDetails.this, "Updating . . .");
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        String theNewAccountName = profileAccountName.getText().toString().trim();
        String theNewAccountNumber = profileAccountNumber.getText().toString().trim();


        if (TextUtils.isEmpty(theNewAccountName)){

            mDialog.dismiss();
            Common.showErrorDialog(BankDetails.this, "Please Enter Account Holder Name", BankDetails.this);

        } else if (TextUtils.isEmpty(theNewAccountNumber)){

            mDialog.dismiss();
            Common.showErrorDialog(BankDetails.this, "Please Enter Account Number", BankDetails.this);

        } else if (selectedBank.equalsIgnoreCase("")){

            mDialog.dismiss();
            Common.showErrorDialog(BankDetails.this, "Please Select A Bank", BankDetails.this);

        } else if (selectedBank.equalsIgnoreCase("Bank")){

            mDialog.dismiss();
            Common.showErrorDialog(BankDetails.this, "Please Select A Valid Bank", BankDetails.this);

        } else {

            final Map<String, Object> userMap = new HashMap<>();
            userMap.put("bank", selectedBank);
            userMap.put("accountName", theNewAccountName);
            userMap.put("accountNumber", theNewAccountNumber);


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
}
