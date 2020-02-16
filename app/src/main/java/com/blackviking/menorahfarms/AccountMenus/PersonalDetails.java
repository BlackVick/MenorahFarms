package com.blackviking.menorahfarms.AccountMenus;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.ApplicationClass;
import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
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
        userRef = db.getReference(Common.USERS_NODE);
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();

        //paper user init
        paperUser = Paper.book().read(Common.PAPER_USER);

        /*---   WIDGETS   ---*/
        profileName = findViewById(R.id.profileName);
        profileName.setEnabled(false);
        profileMail = findViewById(R.id.profileEmail);
        profileMail.setEnabled(false);
        profileType = findViewById(R.id.profileType);
        profileType.setEnabled(false);
        profilePhone = findViewById(R.id.profilePhone);
        profileBirthday = findViewById(R.id.profileBirthday);
        changeProfileBirthday = findViewById(R.id.changeProfileBirthday);
        backButton = findViewById(R.id.backButton);
        profileGender = findViewById(R.id.profileGender);
        profileNationality = findViewById(R.id.profileNationality);
        updateProfile = findViewById(R.id.updateProfileButton);


        /*---   DATE PICKER   ---*/
        final DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        };

        changeProfileBirthday.setOnClickListener(v -> new DatePickerDialog(PersonalDetails.this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show());


        //set current user info
        setUserInfo(paperUser);


        backButton.setOnClickListener(v -> finish());
        
        /*---   UPDATE   ---*/
        updateProfile.setOnClickListener(v -> {
            //show loading dialog
            mDialog = new SpotsDialog(PersonalDetails.this, "Updating . . .");
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();

            //run network check
            new CheckInternet(PersonalDetails.this, output -> {

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

        profileName.setText(paperUser.getFirstName() + " " + paperUser.getLastName());
        profileMail.setText(paperUser.getEmail());
        profileType.setText(paperUser.getSignUpMode());
        profilePhone.setText(paperUser.getPhone());
        profileBirthday.setText(paperUser.getBirthday());
        profileGender.setText(paperUser.getGender());
        profileNationality.setText(paperUser.getNationality());

    }

    private void updateChanges() {

        String theNewPhone = profilePhone.getText().toString().trim();
        String theNewBirthday = profileBirthday.getText().toString().trim();
        String theNewGender = profileGender.getText().toString().trim();
        String theNewNationality = profileNationality.getText().toString().trim();


        //create map
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("phone", theNewPhone);
        updateMap.put("birthday", theNewBirthday);
        updateMap.put("gender", theNewGender);
        updateMap.put("nationality", theNewNationality);

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

    private void updateLabel() {
        String myFormat = "dd/MMM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        profileBirthday.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
