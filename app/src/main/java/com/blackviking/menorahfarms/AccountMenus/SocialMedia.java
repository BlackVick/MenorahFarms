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

import com.blackviking.menorahfarms.Common.ApplicationClass;
import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.UserModel;
import com.blackviking.menorahfarms.R;
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

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class SocialMedia extends AppCompatActivity {

    private MaterialEditText profileFacebook, profileInstagram, profileTwitter, profileLinkedin;
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
        setContentView(R.layout.activity_social_media);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();

        //paper user init
        paperUser = Paper.book().read(Common.PAPER_USER);


        /*---   WIDGETS   ---*/
        profileFacebook = (MaterialEditText)findViewById(R.id.profileFacebook);
        profileInstagram = (MaterialEditText)findViewById(R.id.profileInstagram);
        profileTwitter = (MaterialEditText)findViewById(R.id.profileTwitter);
        profileLinkedin = (MaterialEditText)findViewById(R.id.profileLinkedIn);
        backButton = (ImageView)findViewById(R.id.backButton);
        updateProfile = (Button)findViewById(R.id.updateProfileButton);


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
                mDialog = new SpotsDialog(SocialMedia.this, "Updating . . .");
                mDialog.setCancelable(false);
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();

                //execute network check async task
                CheckInternet asyncTask = (CheckInternet) new CheckInternet(SocialMedia.this, new CheckInternet.AsyncResponse(){
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

        profileFacebook.setText(paperUser.getFacebook());
        profileInstagram.setText(paperUser.getInstagram());
        profileTwitter.setText(paperUser.getTwitter());
        profileLinkedin.setText(paperUser.getLinkedIn());

    }

    private void updateChanges() {

        UserModel thePaperUser = Paper.book().read(Common.PAPER_USER);

        String theNewFacebook = profileFacebook.getText().toString().trim();
        String theNewInstagram = profileInstagram.getText().toString().trim();
        String theNewTwitter = profileTwitter.getText().toString().trim();
        String theNewLinkedIn = profileLinkedin.getText().toString().trim();

        final UserModel updateUser = new UserModel(
                thePaperUser.getEmail(), thePaperUser.getFirstName(), thePaperUser.getLastName(),
                thePaperUser.getProfilePicture(), thePaperUser.getProfilePictureThumb(), thePaperUser.getSignUpMode(),
                theNewFacebook, theNewInstagram, theNewTwitter, theNewLinkedIn,
                thePaperUser.getUserType(), thePaperUser.getUserPackage(), thePaperUser.getPhone(), thePaperUser.getBirthday(),
                thePaperUser.getGender(), thePaperUser.getNationality(), thePaperUser.getAddress(), thePaperUser.getCity(),
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
