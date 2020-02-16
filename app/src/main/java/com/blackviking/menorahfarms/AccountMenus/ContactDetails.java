package com.blackviking.menorahfarms.AccountMenus;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class ContactDetails extends AppCompatActivity {

    private MaterialEditText profileAddress, profileCity, profileState;
    private Button updateProfile;
    private ImageView backButton;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef;
    private String currentUid;
    private android.app.AlertDialog mDialog;
    private UserModel paperUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);


        /*---   FIREBASE   ---*/
        userRef = db.getReference(Common.USERS_NODE);
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();

        //paper user init
        paperUser = Paper.book().read(Common.PAPER_USER);


        /*---   WIDGETS   ---*/
        profileAddress = findViewById(R.id.profileAddress);
        profileCity = findViewById(R.id.profileCity);
        profileState = findViewById(R.id.profileState);
        backButton = findViewById(R.id.backButton);
        updateProfile = findViewById(R.id.updateProfileButton);

        //set current user info
        setUserInfo(paperUser);


        backButton.setOnClickListener(v -> finish());



        /*---   UPDATE   ---*/
        updateProfile.setOnClickListener(v -> {

            //show loading dialog
            mDialog = new SpotsDialog(ContactDetails.this, "Updating . . .");
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();

            //execute network check async task
            new CheckInternet(ContactDetails.this, output -> {

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

        profileAddress.setText(paperUser.getAddress());
        profileCity.setText(paperUser.getCity());
        profileState.setText(paperUser.getState());

    }

    private void updateChanges() {

        String theNewAddress = profileAddress.getText().toString().trim();
        String theNewCity = profileCity.getText().toString().trim();
        String theNewState = profileState.getText().toString().trim();


        //create map
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("address", theNewAddress);
        updateMap.put("city", theNewCity);
        updateMap.put("state", theNewState);

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

    @Override
    public void onBackPressed() {
        finish();
    }

}
