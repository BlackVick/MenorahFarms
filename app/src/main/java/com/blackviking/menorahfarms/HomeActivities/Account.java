package com.blackviking.menorahfarms.HomeActivities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.AccountMenus.BankDetails;
import com.blackviking.menorahfarms.AccountMenus.ContactDetails;
import com.blackviking.menorahfarms.AccountMenus.NextOfKin;
import com.blackviking.menorahfarms.AccountMenus.PersonalDetails;
import com.blackviking.menorahfarms.AccountMenus.SocialMedia;
import com.blackviking.menorahfarms.AccountMenus.StudentDetails;
import com.blackviking.menorahfarms.BuildConfig;
import com.blackviking.menorahfarms.CartAndHistory.Cart;
import com.blackviking.menorahfarms.CartAndHistory.SponsorshipHistory;
import com.blackviking.menorahfarms.Common.ApplicationClass;
import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Common.Permissions;
import com.blackviking.menorahfarms.Models.UserModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.SignIn;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import id.zelory.compressor.Compressor;
import io.paperdb.Paper;

public class Account extends AppCompatActivity {

    private LinearLayout dashboardSwitch, farmstoreSwitch, accountSwitch;
    private TextView dashboardText, farmstoreText, accountText;

    private TextView userName, userEmail, profileProgressText, cartItemCount;
    private ImageView cartButton;
    private Button resetPassword;
    private CircleImageView userAvatar;
    private ProgressBar profileProgress;

    private ScrollView verifiedLayout;
    private RelativeLayout unverifiedLayout;
    private ImageView reloadPage;
    private Button resendActivationBtn;

    private RelativeLayout personalDetailsLayout, contactDetailsLayout,
            bankDetailsLayout, nextOfKinLayout, socialMediaLayout,
            studentProfileLayout, historyLayout, logOutLayout;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, cartRef;
    private String currentUid, loginType;

    //image upload
    private static final int VERIFY_PERMISSIONS_REQUEST = 757;
    private static final int CAMERA_REQUEST_CODE = 656;
    private static final int GALLERY_REQUEST_CODE = 665;
    private Uri imageUri;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference imageRef;
    private String imageUrl;
    private boolean isUploading = false;
    private UploadTask uploadTask;
    private ProgressBar imageProgress;

    //values
    private String theUserMail, theFirstName, theLastName, theProfilePicture;

    //profile completion
    private boolean isWarned;

    //loading
    private UserModel paperUser;
    private android.app.AlertDialog alertDialog;
    private boolean isLoading = false;


    //google
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);


        /*---   FIREBASE   ---*/
        userRef = db.getReference(Common.USERS_NODE);
        imageRef = storage.getReference("ProfileImages");
        cartRef = db.getReference(Common.CART_NODE);
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        dashboardSwitch = findViewById(R.id.dashboardLayout);
        farmstoreSwitch = findViewById(R.id.farmShopLayout);
        accountSwitch = findViewById(R.id.accountLayout);
        dashboardText = findViewById(R.id.dashboardText);
        farmstoreText = findViewById(R.id.farmShopText);
        accountText = findViewById(R.id.accountText);

        userName = findViewById(R.id.userFullName);
        userEmail = findViewById(R.id.userEmail);
        profileProgressText = findViewById(R.id.profileProgressText);
        cartButton = findViewById(R.id.cartButton);
        resetPassword = findViewById(R.id.changePasswordButton);
        resetPassword.setEnabled(false);
        userAvatar = findViewById(R.id.userAvatar);
        profileProgress = findViewById(R.id.profileProgress);

        personalDetailsLayout = findViewById(R.id.personalDetailsLayout);
        contactDetailsLayout = findViewById(R.id.contactDetailsLayout);
        bankDetailsLayout = findViewById(R.id.bankDetailsLayout);
        nextOfKinLayout = findViewById(R.id.nextOfKinLayout);
        socialMediaLayout = findViewById(R.id.socialMediaLayout);
        studentProfileLayout = findViewById(R.id.studentProfileLayout);
        historyLayout = findViewById(R.id.historyLayout);
        logOutLayout = findViewById(R.id.logOutLayout);

        verifiedLayout = findViewById(R.id.verifiedLayout);
        unverifiedLayout = findViewById(R.id.unverifiedLayout);
        reloadPage = findViewById(R.id.reloadPage);
        resendActivationBtn = findViewById(R.id.resendActivationBtn);

        cartItemCount = findViewById(R.id.cartItemCount);
        imageProgress = findViewById(R.id.imageProgress);



        /*---   GOOGLE INIT   ---*/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, connectionResult -> Toast.makeText(Account.this, "Unknown Error Occurred", Toast.LENGTH_SHORT).show())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        /*---   BOTTOM NAV   ---*/
        dashboardText.setTextColor(getResources().getColor(R.color.black));
        farmstoreText.setTextColor(getResources().getColor(R.color.black));
        accountText.setTextColor(getResources().getColor(R.color.colorPrimary));

        //dashboard
        dashboardSwitch.setOnClickListener(v -> {

            if (isLoading){
                alertDialog.dismiss();
            }

            Intent dashboardIntent = new Intent(Account.this, Dashboard.class);
            startActivity(dashboardIntent);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

        });

        //farmshop
        farmstoreSwitch.setOnClickListener(v -> {

            if (isLoading){
                alertDialog.dismiss();
            }

            Intent farmstoreIntent = new Intent(Account.this, FarmShop.class);
            startActivity(farmstoreIntent);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

        });

        //cart
        cartButton.setOnClickListener(v -> {

            Intent cartIntent = new Intent(Account.this, Cart.class);
            startActivity(cartIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

        });

        //personal details
        personalDetailsLayout.setOnClickListener(v -> {
            Intent personalIntent = new Intent(Account.this, PersonalDetails.class);
            startActivity(personalIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
        });

        //contact
        contactDetailsLayout.setOnClickListener(v -> {
            Intent conTactIntent = new Intent(Account.this, ContactDetails.class);
            startActivity(conTactIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
        });

        //financial
        bankDetailsLayout.setOnClickListener(v -> {
            Intent bankIntent = new Intent(Account.this, BankDetails.class);
            startActivity(bankIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
        });

        //next of kin
        nextOfKinLayout.setOnClickListener(v -> {
            Intent nextOfKinIntent = new Intent(Account.this, NextOfKin.class);
            startActivity(nextOfKinIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
        });

        //social media
        socialMediaLayout.setOnClickListener(v -> {
            Intent socialIntent = new Intent(Account.this, SocialMedia.class);
            startActivity(socialIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
        });

        //student dets
        studentProfileLayout.setOnClickListener(v -> {
            Intent studentIntent = new Intent(Account.this, StudentDetails.class);
            startActivity(studentIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
        });

        //history
        historyLayout.setOnClickListener(v -> {
            Intent historyIntent = new Intent(Account.this, SponsorshipHistory.class);
            startActivity(historyIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
        });

        //logout
        logOutLayout.setOnClickListener(v -> {

            if (paperUser.getSignUpMode().equalsIgnoreCase("Google")){

                //revoke access
                Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient);

                Paper.book().destroy();

                mAuth.signOut();
                ((ApplicationClass)(getApplicationContext())).resetUser();

                FirebaseMessaging.getInstance().unsubscribeFromTopic(currentUid);
                FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.GENERAL_NOTIFY);
                Intent signoutIntent = new Intent(Account.this, SignIn.class);
                signoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(signoutIntent);

                finish();

            } else {

                Paper.book().destroy();

                mAuth.signOut();
                ((ApplicationClass)(getApplicationContext())).resetUser();

                FirebaseMessaging.getInstance().unsubscribeFromTopic(currentUid);
                FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.GENERAL_NOTIFY);
                Intent signoutIntent = new Intent(Account.this, SignIn.class);
                signoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(signoutIntent);

                finish();

            }

        });

        //check mail verification
        if (mAuth.getCurrentUser().isEmailVerified()) {

            verifiedLayout.setVisibility(View.VISIBLE);
            unverifiedLayout.setVisibility(View.GONE);

        } else {

            verifiedLayout.setVisibility(View.GONE);
            unverifiedLayout.setVisibility(View.VISIBLE);

            resendActivationBtn.setOnClickListener(v -> mAuth.getCurrentUser().sendEmailVerification());

            reloadPage.setOnClickListener(v -> reloadThePage());

        }

        //load user
        setCurrentUser();

    }

    private void reloadThePage() {

        mAuth.getCurrentUser().reload().addOnCompleteListener(
                task -> {

                    if (task.isSuccessful()){

                        if (mAuth.getCurrentUser().isEmailVerified()){

                            verifiedLayout.setVisibility(View.VISIBLE);
                            unverifiedLayout.setVisibility(View.GONE);


                        } else {

                            verifiedLayout.setVisibility(View.GONE);
                            unverifiedLayout.setVisibility(View.VISIBLE);

                            //resend verification
                            resendActivationBtn.setOnClickListener(v -> mAuth.getCurrentUser().sendEmailVerification());

                            //reload page
                            reloadPage.setOnClickListener(v -> reloadThePage());

                        }

                    } else {

                        Toast.makeText(Account.this, "Error occurred", Toast.LENGTH_SHORT).show();

                    }

                }
        );

    }

    private void setCurrentUser() {

        //get local user
        paperUser = Paper.book().read(Common.PAPER_USER);

        theFirstName = paperUser.getFirstName();
        theLastName = paperUser.getLastName();
        theUserMail = paperUser.getEmail();
        theProfilePicture = paperUser.getProfilePictureThumb();
        loginType = paperUser.getSignUpMode();

        //set user details
        userName.setText(theFirstName + " " + theLastName);
        userEmail.setText(theUserMail);

        //avatar
        if (!theProfilePicture.equalsIgnoreCase("")) {

            Picasso.get()
                    .load(theProfilePicture)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.profile)
                    .into(userAvatar, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(theProfilePicture)
                                    .placeholder(R.drawable.profile)
                                    .into(userAvatar);
                        }
                    });


        } else {

            userAvatar.setImageResource(R.drawable.profile);

        }

        userAvatar.setOnClickListener(v -> {

            //check permissions
            if (ContextCompat.checkSelfPermission(Account.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(Account.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(Account.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

                showUploadDialog();

            } else {

                ActivityCompat.requestPermissions(Account.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, VERIFY_PERMISSIONS_REQUEST);

            }
        });

        //reset password check
        if (loginType.equalsIgnoreCase("Email")) {

            resetPassword.setVisibility(View.VISIBLE);
            resetPassword.setEnabled(true);

        } else {

            resetPassword.setVisibility(View.INVISIBLE);
            resetPassword.setEnabled(false);

        }


        //always cart item count
        cartRef.child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        int count = (int) dataSnapshot.getChildrenCount();

                        cartItemCount.setText(String.valueOf(count));

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        //set profile progress bar
        setProfileProgress(paperUser);

    }

    private void showUploadDialog() {
        alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.image_source_choice,null);

        final ImageView cameraPick = viewOptions.findViewById(R.id.cameraPick);
        final ImageView galleryPick = viewOptions.findViewById(R.id.galleryPick);

        //dialog parameters
        alertDialog.setView(viewOptions);
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.y = 100; // bottom margin
        alertDialog.getWindow().setAttributes(layoutParams);

        //open camera
        cameraPick.setOnClickListener(v -> {
            openCamera();
            alertDialog.dismiss();
        });

        //open gallery
        galleryPick.setOnClickListener(v -> {
            openGallery();
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    private void openGallery() {

        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , GALLERY_REQUEST_CODE);

    }

    private void openCamera() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file=getOutputMediaFile(1);
        imageUri = FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID + ".provider",
                file);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == VERIFY_PERMISSIONS_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                showUploadDialog();

            } else {

                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();

            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //image upload fix
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){

            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(Account.this);


        }

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK){

            if (data.getData() != null) {
                imageUri = data.getData();

                CropImage.activity(imageUri)
                        .setAspectRatio(1, 1)
                        .start(Account.this);
            }

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            final CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                //show loading
                imageProgress.setVisibility(View.VISIBLE);
                userAvatar.setEnabled(false);

                //value
                isUploading = true;

                //perform network check
                new CheckInternet(this, output -> {

                    //check all cases
                    if (output == 1){

                        //get data
                        Uri resultUri = result.getUri();

                        //get file path
                        File thumb_filepath = new File(resultUri.getPath());

                        try {

                            //converting to bitmap
                            Bitmap thumb_bitmap = new Compressor(Account.this)
                                    .setMaxWidth(500)
                                    .setMaxHeight(500)
                                    .setQuality(70)
                                    .compressToBitmap(thumb_filepath);

                            //compress file size and set format
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                            final byte[] thumb_byte = baos.toByteArray();


                            //set file location and name in firebase
                            final StorageReference imageThumbRef1 = imageRef.child(currentUid + ".jpg");

                            //start upload
                            uploadTask = imageThumbRef1.putBytes(thumb_byte);

                            //get download link
                            uploadTask.continueWithTask(task -> {

                                if (!task.isSuccessful()) {

                                    throw task.getException();
                                }

                                // Continue with the task to get the download URL
                                return imageThumbRef1.getDownloadUrl();

                            }).addOnCompleteListener(task -> {

                                if (task.isSuccessful()) {

                                    Uri downloadUri = task.getResult();

                                    //link
                                    imageUrl = downloadUri.toString();

                                    //update
                                    updateImage(imageUrl);

                                } else {

                                    Toast.makeText(Account.this, "FAILED", Toast.LENGTH_SHORT).show();

                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else

                    if (output == 0){

                        //stop loading
                        imageProgress.setVisibility(View.GONE);
                        userAvatar.setEnabled(true);

                        //value
                        isUploading = false;

                        //no internet
                        Toast.makeText(Account.this, "No internet access", Toast.LENGTH_SHORT).show();

                    } else

                    if (output == 2){

                        //stop loading
                        imageProgress.setVisibility(View.GONE);
                        userAvatar.setEnabled(true);

                        //value
                        isUploading = false;

                        //no network
                        Toast.makeText(Account.this, "Not connected to a network", Toast.LENGTH_SHORT).show();

                    }

                }).execute();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    private void updateImage(String imageUrl) {

        //create image map
        Map<String, Object> imageMap = new HashMap<>();
        imageMap.put("profilePicture", imageUrl);
        imageMap.put("profilePictureThumb", imageUrl);

        //update to db
        userRef.child(currentUid)
                .updateChildren(imageMap)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()){

                        //get user from server
                        userRef.child(currentUid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        UserModel updatedUser = dataSnapshot.getValue(UserModel.class);

                                        if (updatedUser != null){

                                            //update local data
                                            ((ApplicationClass)(getApplicationContext())).setUser(updatedUser);
                                            paperUser = updatedUser;
                                            setCurrentUser();

                                            //stop loading
                                            imageProgress.setVisibility(View.GONE);
                                            userAvatar.setEnabled(true);

                                            //value
                                            isUploading = false;

                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                    } else {

                        Toast.makeText(Account.this, "Update failed", Toast.LENGTH_SHORT).show();

                    }

                });
    }

    private File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Campus Rush");

        /**Create the storage directory if it does not exist*/
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }

        /**Create a media file name*/
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == 1){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".png");
        } else {
            return null;
        }

        return mediaFile;
    }

    private void setProfileProgress(UserModel paperUser) {

        String profileFirstName = paperUser.getFirstName();
        String profileLastName = paperUser.getLastName();
        String profileUserMail = paperUser.getEmail();
        String profilePix = paperUser.getProfilePictureThumb();
        String profileFacebook = paperUser.getFacebook();
        String profileInstagram = paperUser.getInstagram();
        String profileTwitter = paperUser.getTwitter();
        String profileLinkedIn = paperUser.getLinkedIn();
        String profilePhone = paperUser.getPhone();
        String profileBirthday = paperUser.getBirthday();
        String profileGender = paperUser.getGender();
        String profileNatinality = paperUser.getNationality();
        String profileAddress = paperUser.getAddress();
        String profileCity = paperUser.getCity();
        String profileState = paperUser.getState();
        String profileBank = paperUser.getBank();
        String profileAccountName = paperUser.getAccountName();
        String profileAccountNumber = paperUser.getAccountNumber();
        String profileKinName = paperUser.getKinName();
        String profileKinMail = paperUser.getKinEmail();
        String profileKinRelationship = paperUser.getKinRelationship();
        String profileKinPhone = paperUser.getKinPhone();
        String profileKinAddress = paperUser.getKinAddress();


        String[] theArray = {profileFirstName, profileLastName, profileUserMail, profilePix,
                profileFacebook, profileInstagram, profileTwitter, profileLinkedIn, profilePhone, profileBirthday, profileGender,
                profileNatinality, profileAddress, profileCity, profileState, profileBank, profileAccountName, profileAccountNumber,
                profileKinName, profileKinMail, profileKinRelationship, profileKinPhone, profileKinAddress};


        int profileProgressInt = 0;
        ArrayList<String> list = new ArrayList<>();
        int totalResult;

        int calcResult = 0;

        for (int i = 0; i < 23; i++) {

            if (!theArray[i].equalsIgnoreCase("")) {

                list.add(theArray[i]);

            }

        }

        calcResult = (list.size() * 100) / 23;


        profileProgressText.setText("Your profile is " + calcResult + "% complete.");

        Drawable draw = getResources().getDrawable(R.drawable.progress_drawable);
        profileProgress.setProgressDrawable(draw);
        profileProgress.setProgress(calcResult);

        //profile warning state
        if (Paper.book().read(Common.PROFILE_WARNING_COUNT) != null) {

            isWarned = Paper.book().read(Common.PROFILE_WARNING_COUNT);

            if (!isWarned) {

                if (calcResult < 75) {

                    Toast.makeText(this, "Complete profile to participate in farm sponsorships.", Toast.LENGTH_LONG).show();
                    Paper.book().write(Common.PROFILE_WARNING_COUNT, true);

                }

            }

        } else {

            Paper.book().write(Common.PROFILE_WARNING_COUNT, false);

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        setCurrentUser();
    }

    @Override
    public void onBackPressed() {
        if (isUploading){

            uploadTask.cancel();

        }
        if (isLoading){
            alertDialog.dismiss();
        }
        finish();
    }
}