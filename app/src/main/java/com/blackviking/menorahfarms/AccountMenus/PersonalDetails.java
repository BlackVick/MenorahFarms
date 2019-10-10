package com.blackviking.menorahfarms.AccountMenus;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.blackviking.menorahfarms.Common.ApplicationClass;
import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.UserModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.SignUp;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class PersonalDetails extends AppCompatActivity {

    private MaterialEditText profileName, profileMail, profileType, profilePhone, profileBirthday, profileGender, profileNationality;
    private Button updateProfile;
    private ImageView changeProfileBirthday, backButton;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef;
    private String currentUid;
    private final Calendar myCalendar = Calendar.getInstance();
    private android.app.AlertDialog mDialog;
    private UserModel paperUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_details);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();

        //paper user init
        paperUser = Paper.book().read(Common.PAPER_USER);

        /*---   WIDGETS   ---*/
        profileName = (MaterialEditText)findViewById(R.id.profileName);
        profileName.setEnabled(false);
        profileMail = (MaterialEditText)findViewById(R.id.profileEmail);
        profileMail.setEnabled(false);
        profileType = (MaterialEditText)findViewById(R.id.profileType);
        profileType.setEnabled(false);
        profilePhone = (MaterialEditText)findViewById(R.id.profilePhone);
        profileBirthday = (MaterialEditText)findViewById(R.id.profileBirthday);
        changeProfileBirthday = (ImageView)findViewById(R.id.changeProfileBirthday);
        backButton = (ImageView)findViewById(R.id.backButton);
        profileGender = (MaterialEditText)findViewById(R.id.profileGender);
        profileNationality = (MaterialEditText)findViewById(R.id.profileNationality);
        updateProfile = (Button)findViewById(R.id.updateProfileButton);


        /*---   DATE PICKER   ---*/
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        changeProfileBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(PersonalDetails.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        //set current user info
        setUserInfo(paperUser);


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
                mDialog = new SpotsDialog(PersonalDetails.this, "Updating . . .");
                mDialog.setCancelable(false);
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();

                //execute network check async task
                CheckInternet asyncTask = (CheckInternet) new CheckInternet(PersonalDetails.this, new CheckInternet.AsyncResponse(){
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

        profileName.setText(paperUser.getFirstName() + " " + paperUser.getLastName());
        profileMail.setText(paperUser.getEmail());
        profileType.setText(paperUser.getSignUpMode());
        profilePhone.setText(paperUser.getPhone());
        profileBirthday.setText(paperUser.getBirthday());
        profileGender.setText(paperUser.getGender());
        profileNationality.setText(paperUser.getNationality());

    }

    private void updateChanges() {

        UserModel thePaperUser = Paper.book().read(Common.PAPER_USER);

        String theNewPhone = profilePhone.getText().toString().trim();
        String theNewBirthday = profileBirthday.getText().toString().trim();
        String theNewGender = profileGender.getText().toString().trim();
        String theNewNationality = profileNationality.getText().toString().trim();


        final UserModel updateUser = new UserModel(
                thePaperUser.getEmail(), thePaperUser.getFirstName(), thePaperUser.getLastName(),
                thePaperUser.getProfilePicture(), thePaperUser.getProfilePictureThumb(), thePaperUser.getSignUpMode(),
                thePaperUser.getFacebook(), thePaperUser.getInstagram(), thePaperUser.getTwitter(), thePaperUser.getLinkedIn(),
                thePaperUser.getUserType(), thePaperUser.getUserPackage(), theNewPhone, theNewBirthday,
                theNewGender, theNewNationality, thePaperUser.getAddress(), thePaperUser.getCity(),
                thePaperUser.getState(), thePaperUser.getBank(), thePaperUser.getAccountName(), thePaperUser.getAccountNumber(),
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

    private void updateLabel() {
        String myFormat = "dd/MMM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        profileBirthday.setText(sdf.format(myCalendar.getTime()));
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
