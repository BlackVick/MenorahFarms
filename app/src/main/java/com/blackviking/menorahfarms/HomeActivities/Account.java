package com.blackviking.menorahfarms.HomeActivities;

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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.blackviking.menorahfarms.Models.FarmModel;
import com.blackviking.menorahfarms.Models.UserModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.SignIn;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
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
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import id.zelory.compressor.Compressor;
import io.paperdb.Paper;

public class Account extends AppCompatActivity {

    private LinearLayout dashboardSwitch, farmstoreSwitch, accountSwitch;
    private TextView dashboardText, farmstoreText, accountText;

    private TextView userName, userEmail, profileProgressText;
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
    private DatabaseReference userRef, farmRef;
    private String currentUid, loginType;

    private android.app.AlertDialog mDialog;

    private static final int VERIFY_PERMISSIONS_REQUEST = 757;
    private static final int CAMERA_REQUEST_CODE = 656;
    private static final int GALLERY_REQUEST_CODE = 665;
    private Uri imageUri;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference imageRef;
    private String originalImageUrl, thumbDownloadUrl;

    private String theUserMail, theFirstName, theLastName, theProfilePicture;

    private boolean isWarned;


    private UserModel paperUser;
    private android.app.AlertDialog alertDialog, alertDialogError;


    //google
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        imageRef = storage.getReference("ProfileImages");
        farmRef = db.getReference("Farms");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        //get paper user
        paperUser = Paper.book().read(Common.PAPER_USER);


        /*---   WIDGETS   ---*/
        dashboardSwitch = (LinearLayout)findViewById(R.id.dashboardLayout);
        farmstoreSwitch = (LinearLayout)findViewById(R.id.farmShopLayout);
        accountSwitch = (LinearLayout)findViewById(R.id.accountLayout);
        dashboardText = (TextView)findViewById(R.id.dashboardText);
        farmstoreText = (TextView)findViewById(R.id.farmShopText);
        accountText = (TextView)findViewById(R.id.accountText);

        userName = (TextView)findViewById(R.id.userFullName);
        userEmail = (TextView)findViewById(R.id.userEmail);
        profileProgressText = (TextView)findViewById(R.id.profileProgressText);
        cartButton = (ImageView)findViewById(R.id.cartButton);
        resetPassword = (Button)findViewById(R.id.changePasswordButton);
        resetPassword.setEnabled(false);
        userAvatar = (CircleImageView)findViewById(R.id.userAvatar);
        profileProgress = (ProgressBar)findViewById(R.id.profileProgress);

        personalDetailsLayout = (RelativeLayout)findViewById(R.id.personalDetailsLayout);
        contactDetailsLayout = (RelativeLayout)findViewById(R.id.contactDetailsLayout);
        bankDetailsLayout = (RelativeLayout)findViewById(R.id.bankDetailsLayout);
        nextOfKinLayout = (RelativeLayout)findViewById(R.id.nextOfKinLayout);
        socialMediaLayout = (RelativeLayout)findViewById(R.id.socialMediaLayout);
        studentProfileLayout = (RelativeLayout)findViewById(R.id.studentProfileLayout);
        historyLayout = (RelativeLayout)findViewById(R.id.historyLayout);
        logOutLayout = (RelativeLayout)findViewById(R.id.logOutLayout);

        verifiedLayout = (ScrollView)findViewById(R.id.verifiedLayout);
        unverifiedLayout = (RelativeLayout)findViewById(R.id.unverifiedLayout);
        reloadPage = (ImageView)findViewById(R.id.reloadPage);
        resendActivationBtn = (Button)findViewById(R.id.resendActivationBtn);



        /*---   GOOGLE INIT   ---*/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(Account.this, "Unknown Error Occurred", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();



        /*---   BOTTOM NAV   ---*/
        dashboardText.setTextColor(getResources().getColor(R.color.black));
        farmstoreText.setTextColor(getResources().getColor(R.color.black));
        accountText.setTextColor(getResources().getColor(R.color.colorPrimary));


        dashboardSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent dashboardIntent = new Intent(Account.this, Dashboard.class);
                startActivity(dashboardIntent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });
        farmstoreSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent farmstoreIntent = new Intent(Account.this, FarmShop.class);
                startActivity(farmstoreIntent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });


        /*---   CART   ---*/
        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent cartIntent = new Intent(Account.this, Cart.class);
                startActivity(cartIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

            }
        });


        personalDetailsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent personalIntent = new Intent(Account.this, PersonalDetails.class);
                startActivity(personalIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
            }
        });

        contactDetailsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent conTactIntent = new Intent(Account.this, ContactDetails.class);
                startActivity(conTactIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
            }
        });

        bankDetailsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bankIntent = new Intent(Account.this, BankDetails.class);
                startActivity(bankIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
            }
        });

        nextOfKinLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextOfKinIntent = new Intent(Account.this, NextOfKin.class);
                startActivity(nextOfKinIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
            }
        });

        socialMediaLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent socialIntent = new Intent(Account.this, SocialMedia.class);
                startActivity(socialIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
            }
        });

        studentProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent studentIntent = new Intent(Account.this, StudentDetails.class);
                startActivity(studentIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
            }
        });

        historyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent historyIntent = new Intent(Account.this, SponsorshipHistory.class);
                startActivity(historyIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
            }
        });

        logOutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

            }
        });

        //check mail verification
        if (mAuth.getCurrentUser().isEmailVerified()) {

            verifiedLayout.setVisibility(View.VISIBLE);
            unverifiedLayout.setVisibility(View.GONE);

        } else {

            verifiedLayout.setVisibility(View.GONE);
            unverifiedLayout.setVisibility(View.VISIBLE);

            resendActivationBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuth.getCurrentUser().sendEmailVerification();
                }
            });

            reloadPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reloadThePage();
                }
            });

        }

        /*---   CURRENT USER   ---*/
        setCurrentUser(paperUser);

    }

    private void reloadThePage() {

        mAuth.getCurrentUser().reload().addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            if (mAuth.getCurrentUser().isEmailVerified()){

                                verifiedLayout.setVisibility(View.VISIBLE);
                                unverifiedLayout.setVisibility(View.GONE);

                            } else {

                                verifiedLayout.setVisibility(View.GONE);
                                unverifiedLayout.setVisibility(View.VISIBLE);

                                resendActivationBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mAuth.getCurrentUser().sendEmailVerification();
                                    }
                                });

                                reloadPage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        reloadThePage();
                                    }
                                });

                            }

                        } else {

                            Toast.makeText(Account.this, "Error occurred", Toast.LENGTH_SHORT).show();

                        }

                    }
                }
        );

    }

    private void setCurrentUser(UserModel paperUserr) {

        theFirstName = paperUserr.getFirstName();
        theLastName = paperUserr.getLastName();
        theUserMail = paperUserr.getEmail();
        theProfilePicture = paperUserr.getProfilePictureThumb();
        loginType = paperUserr.getSignUpMode();

        userName.setText(theFirstName + " " + theLastName);
        userEmail.setText(theUserMail);

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

        userAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*---   PERMISSIONS HANDLER   ---*/
                if (checkPermissionsArray(Permissions.PERMISSIONS)){

                    showUploadDialog();

                } else {

                    verifyPermissions(Permissions.PERMISSIONS);

                }
            }
        });

        if (loginType.equalsIgnoreCase("Email")) {

            resetPassword.setVisibility(View.VISIBLE);
            resetPassword.setEnabled(true);

        } else {

            resetPassword.setVisibility(View.INVISIBLE);
            resetPassword.setEnabled(false);

        }

        //set profile progress bar
        setProfileProgress(paperUserr);

    }

    private void showUploadDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.image_source_choice,null);

        final ImageView cameraPick = (ImageView) viewOptions.findViewById(R.id.cameraPick);
        final ImageView galleryPick = (ImageView) viewOptions.findViewById(R.id.galleryPick);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        //layoutParams.x = 100; // left margin
        layoutParams.y = 100; // bottom margin
        alertDialog.getWindow().setAttributes(layoutParams);

        //open camera
        cameraPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
                alertDialog.dismiss();
            }
        });

        //open gallery
        galleryPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
                alertDialog.dismiss();
            }
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

    private void verifyPermissions(String[] permissions) {

        ActivityCompat.requestPermissions(
                this,
                permissions,
                VERIFY_PERMISSIONS_REQUEST
        );
    }

    private boolean checkPermissionsArray(String[] permissions) {

        for (int i = 0; i < permissions.length; i++){

            String check = permissions[i];
            if (!checkPermissions(check)){
                return false;
            }

        }
        return true;
    }

    private boolean checkPermissions(String permission) {

        int permissionRequest = ActivityCompat.checkSelfPermission(this, permission);

        if (permissionRequest != PackageManager.PERMISSION_GRANTED){

            return false;
        } else {

            return true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){

            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
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

                //show dialog
                mDialog = new SpotsDialog(Account.this, "Upload In Progress . . .");
                mDialog.setCancelable(false);
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();

                //execute network check async task
                CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
                    @Override
                    public void processFinish(Integer output) {

                        //check all cases
                        if (output == 1){

                            Uri resultUri = result.getUri();

                            File thumb_filepath = new File(resultUri.getPath());

                            try {
                                Bitmap thumb_bitmap = new Compressor(Account.this)
                                        .setMaxWidth(500)
                                        .setMaxHeight(500)
                                        .setQuality(75)
                                        .compressToBitmap(thumb_filepath);

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                                final byte[] thumb_byte = baos.toByteArray();

                                final StorageReference imageThumbRef1 = imageRef.child("Thumbnails").child(currentUid + ".jpg");

                                final UploadTask uploadTask = imageThumbRef1.putBytes(thumb_byte);

                                mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        imageUri = null;
                                        uploadTask.cancel();
                                    }
                                });

                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                        thumbDownloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                        if (thumb_task.isSuccessful()) {

                                            mDialog.dismiss();
                                            updateImage(thumbDownloadUrl, thumbDownloadUrl);

                                        } else {
                                            Toast.makeText(Account.this, "Upload Failed. Please Try Again", Toast.LENGTH_SHORT).show();
                                            mDialog.dismiss();
                                            imageUri = null;
                                            originalImageUrl = null;
                                            thumbDownloadUrl = null;
                                        }
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } else

                        if (output == 0){

                            //no internet
                            mDialog.dismiss();
                            imageUri = null;
                            originalImageUrl = null;
                            thumbDownloadUrl = null;
                            Toast.makeText(Account.this, "No internet", Toast.LENGTH_SHORT).show();

                        } else

                        if (output == 2){

                            //no network
                            mDialog.dismiss();
                            imageUri = null;
                            originalImageUrl = null;
                            thumbDownloadUrl = null;
                            Toast.makeText(Account.this, "Not connected to a network", Toast.LENGTH_SHORT).show();

                        }

                    }
                }).execute();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    private void updateImage(String originalImageUrl, String thumbDownloadUrl) {

        UserModel thePaperUser = Paper.book().read(Common.PAPER_USER);

        final UserModel updateUser = new UserModel(
                thePaperUser.getEmail(), thePaperUser.getFirstName(), thePaperUser.getLastName(),
                originalImageUrl, thumbDownloadUrl, thePaperUser.getSignUpMode(),
                thePaperUser.getFacebook(), thePaperUser.getInstagram(), thePaperUser.getTwitter(), thePaperUser.getLinkedIn(),
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
                            paperUser = Paper.book().read(Common.PAPER_USER);
                            setCurrentUser(paperUser);

                        } else {

                            Toast.makeText(Account.this, "Update failed", Toast.LENGTH_SHORT).show();

                        }

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

    private void setProfileProgress(UserModel paperUserr) {

        String profileFirstName = paperUserr.getFirstName();
        String profileLastName = paperUserr.getLastName();
        String profileUserMail = paperUserr.getEmail();
        String profilePix = paperUserr.getProfilePictureThumb();
        String profileFacebook = paperUserr.getFacebook();
        String profileInstagram = paperUserr.getInstagram();
        String profileTwitter = paperUserr.getTwitter();
        String profileLinkedIn = paperUserr.getLinkedIn();
        String profilePhone = paperUserr.getPhone();
        String profileBirthday = paperUserr.getBirthday();
        String profileGender = paperUserr.getGender();
        String profileNatinality = paperUserr.getNationality();
        String profileAddress = paperUserr.getAddress();
        String profileCity = paperUserr.getCity();
        String profileState = paperUserr.getState();
        String profileBank = paperUserr.getBank();
        String profileAccountName = paperUserr.getAccountName();
        String profileAccountNumber = paperUserr.getAccountNumber();
        String profileKinName = paperUserr.getKinName();
        String profileKinMail = paperUserr.getKinEmail();
        String profileKinRelationship = paperUserr.getKinRelationship();
        String profileKinPhone = paperUserr.getKinPhone();
        String profileKinAddress = paperUserr.getKinAddress();


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

        if (Paper.book().read(Common.PROFILE_WARNING_COUNT) != null) {

            isWarned = Paper.book().read(Common.PROFILE_WARNING_COUNT);

            if (!isWarned) {

                if (calcResult < 75) {

                    showErrorDialog("You are advised to complete your profile to enable you participate in farm sponsorships. \n\nThank you");
                    Paper.book().write(Common.PROFILE_WARNING_COUNT, true);

                }

            }

        } else {

            Paper.book().write(Common.PROFILE_WARNING_COUNT, false);

        }

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