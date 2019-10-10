package com.blackviking.menorahfarms.AccountMenus;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.blackviking.menorahfarms.Common.ApplicationClass;
import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.BankModel;
import com.blackviking.menorahfarms.Models.UserModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.Registration;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import io.paperdb.Paper;

public class BankDetails extends AppCompatActivity {

    private MaterialEditText profileBankName, profileAccountName, profileAccountNumber;
    private Spinner profileBank;
    private Button updateProfile;
    private ImageView backButton;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef, bankRef;
    private String currentUid;
    private android.app.AlertDialog mDialog;
    private String selectedBank = "";
    private UserModel paperUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_details);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        bankRef = db.getReference("Banks");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        //paper user init
        paperUser = Paper.book().read(Common.PAPER_USER);


        /*---   WIDGETS   ---*/
        profileAccountName = (MaterialEditText)findViewById(R.id.profileAccountName);
        profileAccountNumber = (MaterialEditText)findViewById(R.id.profileAccountNumber);
        profileBankName = (MaterialEditText)findViewById(R.id.profileBankName);
        profileBankName.setEnabled(false);
        profileBank = (Spinner) findViewById(R.id.bankSpinner);
        backButton = (ImageView)findViewById(R.id.backButton);
        updateProfile = (Button)findViewById(R.id.updateProfileButton);

        //set current user info
        setUserInfo(paperUser);


        /*---   BANK SPINNER   ---*/
        final List<String> bankList = new ArrayList<>();
        bankList.add(0, "Bank");

        final ArrayAdapter<String> dataAdapterGender;
        dataAdapterGender = new ArrayAdapter(this, android.R.layout.simple_spinner_item, bankList);
        dataAdapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        bankRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot child : dataSnapshot.getChildren()){

                    BankModel bankModel = child.getValue(BankModel.class);
                    bankList.add(bankModel.getBankName());

                }

                profileBank.setAdapter(dataAdapterGender);
                dataAdapterGender.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        profileBank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).equals("Bank")){

                    selectedBank = parent.getItemAtPosition(position).toString();
                    profileBankName.setText(selectedBank);

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

                //show loading dialog
                mDialog = new SpotsDialog(BankDetails.this, "Updating . . .");
                mDialog.setCancelable(false);
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();

                //execute network check async task
                CheckInternet asyncTask = (CheckInternet) new CheckInternet(BankDetails.this, new CheckInternet.AsyncResponse(){
                    @Override
                    public void processFinish(Integer output) {

                        //check all cases
                        if (output == 1){

                            updateChanges();

                        } else

                        if (output == 0){

                            //no internet
                            mDialog.dismiss();
                            showErrorDialog("No internet access");

                        } else

                        if (output == 2){

                            //no internet
                            mDialog.dismiss();
                            showErrorDialog("Not connected to any network");

                        }

                    }
                }).execute();

            }
        });
    }

    private void setUserInfo(UserModel paperUser) {

        profileAccountName.setText(paperUser.getAccountName());
        profileAccountNumber.setText(paperUser.getAccountNumber());
        profileBankName.setText(paperUser.getBank());

    }

    private void updateChanges() {

        UserModel thePaperUser = Paper.book().read(Common.PAPER_USER);

        String theNewAccountName = profileAccountName.getText().toString().trim();
        String theNewAccountNumber = profileAccountNumber.getText().toString().trim();


        if (TextUtils.isEmpty(theNewAccountName)){

            mDialog.dismiss();
            showErrorDialog("Please Enter Account Holder Name");

        } else if (TextUtils.isEmpty(theNewAccountNumber)){

            mDialog.dismiss();
            showErrorDialog("Please Enter Account Number");

        } else if (selectedBank.equalsIgnoreCase("")){

            mDialog.dismiss();
            showErrorDialog("Please Select A Bank");

        } else if (selectedBank.equalsIgnoreCase("Bank")){

            mDialog.dismiss();
            showErrorDialog("Please Select A Valid Bank");

        } else {

            final UserModel updateUser = new UserModel(
                    thePaperUser.getEmail(), thePaperUser.getFirstName(), thePaperUser.getLastName(),
                    thePaperUser.getProfilePicture(), thePaperUser.getProfilePictureThumb(), thePaperUser.getSignUpMode(),
                    thePaperUser.getFacebook(), thePaperUser.getInstagram(), thePaperUser.getTwitter(), thePaperUser.getLinkedIn(),
                    thePaperUser.getUserType(), thePaperUser.getUserPackage(), thePaperUser.getPhone(), thePaperUser.getBirthday(),
                    thePaperUser.getGender(), thePaperUser.getNationality(), thePaperUser.getAddress(), thePaperUser.getCity(),
                    thePaperUser.getState(), selectedBank, theNewAccountName, theNewAccountNumber,
                    thePaperUser.getKinName(), thePaperUser.getKinEmail(), thePaperUser.getKinRelationship(), thePaperUser.getKinPhone(),
                    thePaperUser.getKinAddress(), thePaperUser.getAccountManager()
            );

            userRef.child(currentUid)
                    .setValue(updateUser)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                ((ApplicationClass)(getApplicationContext())).setUser(updateUser);
                                mDialog.dismiss();
                                finish();

                            } else {

                                showErrorDialog("Error occurred, please try again later");

                            }

                        }
                    });

        }

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