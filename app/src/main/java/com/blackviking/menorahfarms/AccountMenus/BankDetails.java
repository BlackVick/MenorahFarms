package com.blackviking.menorahfarms.AccountMenus;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.ApplicationClass;
import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.BankModel;
import com.blackviking.menorahfarms.Models.UserModel;
import com.blackviking.menorahfarms.R;
import com.google.android.gms.tasks.OnCompleteListener;
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
        userRef = db.getReference(Common.USERS_NODE);
        bankRef = db.getReference(Common.BANKS_NODE);
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        //paper user init
        paperUser = Paper.book().read(Common.PAPER_USER);


        /*---   WIDGETS   ---*/
        profileAccountName = findViewById(R.id.profileAccountName);
        profileAccountNumber = findViewById(R.id.profileAccountNumber);
        profileBankName = findViewById(R.id.profileBankName);
        profileBankName.setEnabled(false);
        profileBank =  findViewById(R.id.bankSpinner);
        backButton = findViewById(R.id.backButton);
        updateProfile = findViewById(R.id.updateProfileButton);

        //set current user info
        setUserInfo(paperUser);


        /*---   BANK SPINNER   ---*/
        final List<String> bankList = new ArrayList<>();
        bankList.add(0, "Bank");

        final ArrayAdapter<String> dataAdapterGender;
        dataAdapterGender = new ArrayAdapter(this, android.R.layout.simple_spinner_item, bankList);
        dataAdapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //get bank lists
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

        //set list click listener
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

        //back
        backButton.setOnClickListener(v -> finish());



        /*---   UPDATE   ---*/
        updateProfile.setOnClickListener(v -> {

            //show loading dialog
            mDialog = new SpotsDialog(BankDetails.this, "Updating . . .");
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();

            //run network check
            new CheckInternet(BankDetails.this, output -> {

                //check all cases
                if (output == 1){

                    updateChanges();

                } else

                if (output == 0){

                    //no internet
                    mDialog.dismiss();
                    Toast.makeText(this, "No internet access", Toast.LENGTH_SHORT).show();

                } else

                if (output == 2){

                    //no internet
                    mDialog.dismiss();
                    Toast.makeText(this, "Not connected to any network", Toast.LENGTH_SHORT).show();

                }

            }).execute();

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
            Toast.makeText(this, "Please Enter Account Holder Name", Toast.LENGTH_LONG).show();

        } else if (TextUtils.isEmpty(theNewAccountNumber)){

            mDialog.dismiss();
            Toast.makeText(this, "Please Enter Account Number", Toast.LENGTH_LONG).show();

        } else if (selectedBank.equalsIgnoreCase("")){

            mDialog.dismiss();
            Toast.makeText(this, "Please Select A Bank", Toast.LENGTH_LONG).show();

        } else if (selectedBank.equalsIgnoreCase("Bank")){

            mDialog.dismiss();
            Toast.makeText(this, "Please Select A Valid Bank", Toast.LENGTH_LONG).show();

        } else {


            //create map
            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("bank", selectedBank);
            updateMap.put("accountName", theNewAccountName);
            updateMap.put("accountNumber", theNewAccountNumber);

            //push
            userRef.child(currentUid)
                    .updateChildren(updateMap)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()){

                            userRef.child(currentUid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            UserModel updatedUser = dataSnapshot.getValue(UserModel.class);

                                            if (updatedUser != null){

                                                ((ApplicationClass)(getApplicationContext())).setUser(updatedUser);
                                                mDialog.dismiss();
                                                finish();

                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                        } else {

                            Toast.makeText(this, "Error occurred, please try again later", Toast.LENGTH_LONG).show();

                        }

                    });

        }

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}