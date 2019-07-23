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
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Common.Permissions;
import com.blackviking.menorahfarms.Models.UserModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.SignIn;
import com.facebook.login.LoginManager;
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
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import id.zelory.compressor.Compressor;
import io.paperdb.Paper;

public class Account extends AppCompatActivity {

    private LinearLayout homeSwitch, dashboardSwitch, farmstoreSwitch, accountSwitch;
    private TextView homeText, dashboardText, farmstoreText, accountText;

    private TextView userName, userEmail, profileProgressText;
    private ImageView cartButton;
    private Button resetPassword;
    private CircleImageView userAvatar;
    private ProgressBar profileProgress;

    private ScrollView verifiedLayout;
    private RelativeLayout unverifiedLayout;
    private ImageView reloadPage;
    private Button resendActivationBtn;

    private LinearLayout personalDetailsLayout, contactDetailsLayout, bankDetailsLayout, nextOfKinLayout, socialMediaLayout, studentProfileLayout, historyLayout, logOutLayout;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        imageRef = storage.getReference("ProfileImages");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        homeSwitch = (LinearLayout)findViewById(R.id.homeLayout);
        dashboardSwitch = (LinearLayout)findViewById(R.id.dashboardLayout);
        farmstoreSwitch = (LinearLayout)findViewById(R.id.farmShopLayout);
        accountSwitch = (LinearLayout)findViewById(R.id.accountLayout);
        homeText = (TextView)findViewById(R.id.homeText);
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

        personalDetailsLayout = (LinearLayout)findViewById(R.id.personalDetailsLayout);
        contactDetailsLayout = (LinearLayout)findViewById(R.id.contactDetailsLayout);
        bankDetailsLayout = (LinearLayout)findViewById(R.id.bankDetailsLayout);
        nextOfKinLayout = (LinearLayout)findViewById(R.id.nextOfKinLayout);
        socialMediaLayout = (LinearLayout)findViewById(R.id.socialMediaLayout);
        studentProfileLayout = (LinearLayout)findViewById(R.id.studentProfileLayout);
        historyLayout = (LinearLayout)findViewById(R.id.historyLayout);
        logOutLayout = (LinearLayout)findViewById(R.id.logOutLayout);

        verifiedLayout = (ScrollView)findViewById(R.id.verifiedLayout);
        unverifiedLayout = (RelativeLayout)findViewById(R.id.unverifiedLayout);
        reloadPage = (ImageView)findViewById(R.id.reloadPage);
        resendActivationBtn = (Button)findViewById(R.id.resendActivationBtn);


        /*---   BOTTOM NAV   ---*/
        homeText.setTextColor(getResources().getColor(R.color.black));
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
        homeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent homeIntent = new Intent(Account.this, Home.class);
                startActivity(homeIntent);
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

                if (loginType.equalsIgnoreCase("Facebook")){

                    Paper.book().destroy();

                    mAuth.signOut();
                    LoginManager.getInstance().logOut();

                    FirebaseMessaging.getInstance().unsubscribeFromTopic(currentUid);
                    Intent signoutIntent = new Intent(Account.this, SignIn.class);
                    signoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(signoutIntent);
                    finish();

                } else {

                    Paper.book().destroy();

                    mAuth.signOut();

                    FirebaseMessaging.getInstance().unsubscribeFromTopic(currentUid);

                    Intent signoutIntent = new Intent(Account.this, SignIn.class);
                    signoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(signoutIntent);
                    finish();

                }

            }
        });

        /*---   PERMISSIONS HANDLER   ---*/
        if (checkPermissionsArray(Permissions.PERMISSIONS)){


        } else {

            verifyPermissions(Permissions.PERMISSIONS);

        }


        /*---   CURRENT USER   ---*/
        setCurrentUser();

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


    }

    private void reloadThePage() {

        mAuth.getCurrentUser().reload().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

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

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void setCurrentUser() {

        userRef.child(currentUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        UserModel currentUser = dataSnapshot.getValue(UserModel.class);

                        if (currentUser != null) {
                            theFirstName = currentUser.getFirstName();
                            theLastName = currentUser.getLastName();
                            theUserMail = currentUser.getEmail();
                            theProfilePicture = currentUser.getProfilePictureThumb();
                            loginType = currentUser.getSignUpMode();

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
                                    showUploadDialog();
                                }
                            });

                            if (loginType.equalsIgnoreCase("Email")) {

                                resetPassword.setVisibility(View.VISIBLE);
                                resetPassword.setEnabled(true);

                            } else {

                                resetPassword.setVisibility(View.INVISIBLE);
                                resetPassword.setEnabled(false);

                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        setProfileProgress();


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

        cameraPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getBaseContext())){

                    openCamera();

                }else {

                    Common.showErrorDialog(Account.this, "No Internet Access !", Account.this);
                }
                alertDialog.dismiss();

            }
        });

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

        final long date = System.currentTimeMillis();

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
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                if (Common.isConnectedToInternet(getBaseContext())) {

                    mDialog = new SpotsDialog(this, "Upload In Progress . . .");
                    mDialog.setCancelable(false);
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();

                    Uri resultUri = result.getUri();
                    String imgURI = resultUri.toString();

                    final long date = System.currentTimeMillis();
                    final String dateShitFmt = String.valueOf(date);

                    File thumb_filepath = new File(resultUri.getPath());


                    try {
                        Bitmap thumb_bitmap = new Compressor(this)
                                .setMaxWidth(450)
                                .setMaxHeight(450)
                                .setQuality(70)
                                .compressToBitmap(thumb_filepath);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                        final byte[] thumb_byte = baos.toByteArray();

                        final StorageReference imageRef1 = imageRef.child("FullImages").child(dateShitFmt + ".jpg");

                        final StorageReference imageThumbRef1 = imageRef.child("Thumbnails").child(dateShitFmt + ".jpg");

                        final UploadTask originalUpload = imageRef1.putFile(resultUri);

                        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                imageUri = null;
                                originalUpload.cancel();
                            }
                        });

                        originalUpload.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {

                                    originalImageUrl = task.getResult().getDownloadUrl().toString();
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
                                                updateImage(originalImageUrl, thumbDownloadUrl);

                                            } else {
                                                Toast.makeText(Account.this, "Upload Failed. Please Try Again", Toast.LENGTH_SHORT).show();
                                                mDialog.dismiss();
                                                imageUri = null;
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });

                                } else {

                                    Toast.makeText(Account.this, "Upload Failed. Please Try Again", Toast.LENGTH_SHORT).show();
                                    mDialog.dismiss();
                                    imageUri = null;

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {

                    Common.showErrorDialog(Account.this, "No Internet Access ! Please, try again later.", Account.this);

                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    private void updateImage(String originalImageUrl, String thumbDownloadUrl) {

        Map<String, Object> profilePicMap = new HashMap<>();
        profilePicMap.put("profilePicture", originalImageUrl);
        profilePicMap.put("profilePictureThumb", thumbDownloadUrl);

        userRef.child(currentUid)
                .updateChildren(profilePicMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

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

    private void setProfileProgress() {

        userRef.child(currentUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String profileFirstName = dataSnapshot.child("firstName").getValue().toString();
                        String profileLastName = dataSnapshot.child("lastName").getValue().toString();
                        String profileUserMail = dataSnapshot.child("email").getValue().toString();
                        String profilePix = dataSnapshot.child("profilePictureThumb").getValue().toString();
                        String profileFacebook = dataSnapshot.child("facebook").getValue().toString();
                        String profileInstagram = dataSnapshot.child("instagram").getValue().toString();
                        String profileTwitter = dataSnapshot.child("twitter").getValue().toString();
                        String profileLinkedIn = dataSnapshot.child("linkedIn").getValue().toString();
                        String profilePhone = dataSnapshot.child("phone").getValue().toString();
                        String profileBirthday = dataSnapshot.child("birthday").getValue().toString();
                        String profileGender = dataSnapshot.child("gender").getValue().toString();
                        String profileNatinality = dataSnapshot.child("nationality").getValue().toString();
                        String profileAddress = dataSnapshot.child("address").getValue().toString();
                        String profileCity = dataSnapshot.child("city").getValue().toString();
                        String profileState = dataSnapshot.child("state").getValue().toString();
                        String profileBank = dataSnapshot.child("bank").getValue().toString();
                        String profileAccountName = dataSnapshot.child("accountName").getValue().toString();
                        String profileAccountNumber = dataSnapshot.child("accountNumber").getValue().toString();
                        String profileKinName = dataSnapshot.child("kinName").getValue().toString();
                        String profileKinMail = dataSnapshot.child("kinEmail").getValue().toString();
                        String profileKinRelationship = dataSnapshot.child("kinRelationship").getValue().toString();
                        String profileKinPhone = dataSnapshot.child("kinPhone").getValue().toString();
                        String profileKinAddress = dataSnapshot.child("kinAddress").getValue().toString();


                        String[] theArray = {profileFirstName, profileLastName, profileUserMail, profilePix,
                                profileFacebook, profileInstagram, profileTwitter, profileLinkedIn, profilePhone, profileBirthday, profileGender,
                                profileNatinality, profileAddress, profileCity, profileState, profileBank, profileAccountName, profileAccountNumber,
                                profileKinName, profileKinMail, profileKinRelationship, profileKinPhone, profileKinAddress};


                        int profileProgressInt = 0;
                        ArrayList<String> list = new ArrayList<>();
                        int totalResult;
                        
                        int calcResult = 0;

                        for (int i = 0; i < 23; i++){

                            if (!theArray[i].equalsIgnoreCase("")) {

                                list.add(theArray[i]);

                            }

                        }

                        calcResult = (list.size() * 100) / 23;


                        profileProgressText.setText("Your profile is " + String.valueOf(calcResult) + "% complete.");

                        Drawable draw = getResources().getDrawable(R.drawable.progress_drawable);
                        profileProgress.setProgressDrawable(draw);
                        profileProgress.setProgress(calcResult);

                        if (calcResult < 70){

                            Common.showErrorDialog(Account.this, "We advise that users complete their profile by providing all required details, so we can serve you better. \n\nThank you", Account.this);

                        }





                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });













    }
}
