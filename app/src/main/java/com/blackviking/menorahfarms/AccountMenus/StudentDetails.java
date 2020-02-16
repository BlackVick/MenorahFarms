package com.blackviking.menorahfarms.AccountMenus;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.BuildConfig;
import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Common.Permissions;
import com.blackviking.menorahfarms.HomeActivities.Account;
import com.blackviking.menorahfarms.Models.StudentModel;
import com.blackviking.menorahfarms.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import id.zelory.compressor.Compressor;

public class StudentDetails extends AppCompatActivity {

    private MaterialEditText schoolName, schoolDepartment;
    private Button updateProfile;
    private ImageView backButton, studentId;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference schoolRef;
    private String currentUid;
    private ProgressBar imageProgress;
    private android.app.AlertDialog mDialog;

    private static final int VERIFY_PERMISSIONS_REQUEST = 757;
    private static final int CAMERA_REQUEST_CODE = 656;
    private static final int GALLERY_REQUEST_CODE = 665;
    private Uri imageUri;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference imageRef;
    private String imageUrl;
    private boolean isUploading = false;
    private UploadTask uploadTask;

    //loading
    private android.app.AlertDialog alertDialog;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_details);


        /*---   FIREBASE   ---*/
        schoolRef = db.getReference(Common.STUDENT_DETAILS_NODE);
        imageRef = storage.getReference("StudentIdImages");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();
        
        
        /*---   WIDGET   ---*/
        schoolName = findViewById(R.id.studentSchoolName);
        schoolDepartment = findViewById(R.id.studentDepartment);
        backButton = findViewById(R.id.backButton);
        studentId = findViewById(R.id.studentIdCard);
        updateProfile = findViewById(R.id.updateProfileButton);
        imageProgress = findViewById(R.id.imageProgress);


        //back
        backButton.setOnClickListener(v -> {
            if (isLoading){
                alertDialog.dismiss();
            }
            if (isUploading){

                uploadTask.cancel();

            }
            finish();
        });


        //id click
        studentId.setOnClickListener(v -> {

            //check permissions
            if (ContextCompat.checkSelfPermission(StudentDetails.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(StudentDetails.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(StudentDetails.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

                showUploadDialog();

            } else {

                ActivityCompat.requestPermissions(StudentDetails.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, VERIFY_PERMISSIONS_REQUEST);

            }

        });


        //load current
        loadUserDetails(currentUid);

    }

    private void loadUserDetails(String currentUid) {

        //show loading
        showLoadingDialog("Loading student details");

        //run network check
        new CheckInternet(StudentDetails.this, output -> {

            //check all cases
            if (output == 1){

                schoolRef.child(currentUid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {

                                    final StudentModel currentStudent = dataSnapshot.getValue(StudentModel.class);

                                    if (currentStudent != null){

                                        //clear loading dialog
                                        alertDialog.dismiss();

                                        //set school name and details
                                        schoolName.setText(currentStudent.getSchoolName());
                                        schoolDepartment.setText(currentStudent.getDepartment());

                                        //id image
                                        if (!currentStudent.getStudentIdThumb().equalsIgnoreCase("")){

                                            Picasso.get()
                                                    .load(currentStudent.getStudentIdThumb())
                                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                                    .placeholder(R.drawable.menorah_placeholder)
                                                    .into(studentId, new Callback() {
                                                        @Override
                                                        public void onSuccess() {

                                                        }

                                                        @Override
                                                        public void onError(Exception e) {
                                                            Picasso.get()
                                                                    .load(currentStudent.getStudentIdThumb())
                                                                    .placeholder(R.drawable.menorah_placeholder)
                                                                    .into(studentId);
                                                        }
                                                    });

                                            //update
                                            updateProfile.setOnClickListener(v -> updateChanges(currentStudent.getStudentIdThumb()));

                                        }



                                    }

                                } else {

                                    //dismiss dialog
                                    alertDialog.dismiss();

                                    //update
                                    updateProfile.setOnClickListener(v -> updateChanges(""));

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            } else

            if (output == 0){

                //no internet
                alertDialog.dismiss();
                Toast.makeText(StudentDetails.this, "No internet access", Toast.LENGTH_SHORT).show();

            } else

            if (output == 2){

                //no internet
                alertDialog.dismiss();
                Toast.makeText(StudentDetails.this, "Not connected to any network", Toast.LENGTH_LONG).show();

            }

        }).execute();

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

        //camera
        cameraPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openCamera();
                alertDialog.dismiss();

            }
        });

        //gallery
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

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){

            CropImage.activity(imageUri)
                    .start(StudentDetails.this);


        }

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK){

            if (data.getData() != null) {
                imageUri = data.getData();

                CropImage.activity(imageUri)
                        .start(StudentDetails.this);
            }

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            final CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                //show loading
                imageProgress.setVisibility(View.VISIBLE);
                studentId.setEnabled(false);

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
                            Bitmap thumb_bitmap = new Compressor(StudentDetails.this)
                                    .setMaxWidth(500)
                                    .setMaxHeight(500)
                                    .setQuality(70)
                                    .compressToBitmap(thumb_filepath);

                            //compress file size and set format
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                            final byte[] thumb_byte = baos.toByteArray();


                            //set file location and name in firebase
                            final StorageReference imageThumbRef1 = imageRef.child("Thumbnails").child(currentUid + ".jpg");

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

                                    //stop loading
                                    imageProgress.setVisibility(View.GONE);
                                    studentId.setEnabled(true);

                                    //value
                                    isUploading = false;

                                    //update
                                    setImage(imageUrl, studentId);

                                } else {

                                    Toast.makeText(StudentDetails.this, "FAILED", Toast.LENGTH_SHORT).show();

                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else

                    if (output == 0){

                        //stop loading
                        imageProgress.setVisibility(View.GONE);
                        studentId.setEnabled(true);

                        //value
                        isUploading = false;

                        //no internet
                        Toast.makeText(StudentDetails.this, "No internet access", Toast.LENGTH_SHORT).show();

                    } else

                    if (output == 2){

                        //stop loading
                        imageProgress.setVisibility(View.GONE);
                        studentId.setEnabled(true);

                        //value
                        isUploading = false;

                        //no network
                        Toast.makeText(StudentDetails.this, "Not connected to a network", Toast.LENGTH_SHORT).show();

                    }

                }).execute();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    private void setImage(String imgUrl, ImageView image){

        ImageLoader loader = ImageLoader.getInstance();

        loader.init(ImageLoaderConfiguration.createDefault(this));

        loader.displayImage(imgUrl, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });

    }

    private File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Menorah Farms");

        //Create the storage directory if it does not exist*/
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }

        //Create a media file name
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

    private void updateChanges(String currentImageUrl) {

        //get strings
        String theNewSchoolName = schoolName.getText().toString().trim();
        String theNewDepartment = schoolDepartment.getText().toString().trim();

        //image url link
        if (TextUtils.isEmpty(imageUrl)){

            imageUrl = currentImageUrl;

        }


        //run checks
        if (TextUtils.isEmpty(theNewSchoolName)){

            //show error
            schoolName.requestFocus();
            schoolName.setError("Required");

        } else

        if (TextUtils.isEmpty(theNewDepartment)) {

            //show error
            schoolDepartment.requestFocus();
            schoolDepartment.setError("Required");

        } else

        if (TextUtils.isEmpty(imageUrl)) {

            //show error
            Toast.makeText(this, "Please provide valid ID card image", Toast.LENGTH_LONG).show();

        } else {

            //show loading
            mDialog = new SpotsDialog(StudentDetails.this, "Updating . . .");
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();

            //run network check
            new CheckInternet(StudentDetails.this, output -> {

                //check all cases
                if (output == 1){

                    final Map<String, Object> userMap = new HashMap<>();
                    userMap.put("schoolName", theNewSchoolName);
                    userMap.put("department", theNewDepartment);
                    userMap.put("studentId", imageUrl);
                    userMap.put("studentIdThumb", imageUrl);
                    userMap.put("approval", "pending");

                    schoolRef.child(currentUid)
                            .setValue(userMap)
                            .addOnCompleteListener(task -> {

                                if (task.isSuccessful()){

                                    mDialog.dismiss();
                                    finish();

                                } else {

                                    Toast.makeText(this, "UPDATE FAILED", Toast.LENGTH_SHORT).show();

                                }

                            });

                } else

                if (output == 0){

                    //no internet
                    mDialog.dismiss();
                    Toast.makeText(StudentDetails.this, "No internet access", Toast.LENGTH_SHORT).show();

                } else

                if (output == 2){

                    //no internet
                    mDialog.dismiss();
                    Toast.makeText(StudentDetails.this, "Not connected to any network", Toast.LENGTH_LONG).show();

                }

            }).execute();

        }

    }



    /*---   LOADING DIALOG   ---*/
    public void showLoadingDialog(String theMessage){

        //loading
        isLoading = true;

        alertDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.loading_dialog,null);

        final TextView loadingText = viewOptions.findViewById(R.id.loadingText);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        loadingText.setText(theMessage);

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                isLoading = false;
            }
        });
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isLoading = false;
            }
        });

        alertDialog.show();

    }

    @Override
    public void onBackPressed() {
        if (isLoading){
            alertDialog.dismiss();
        }
        if (isUploading){

            uploadTask.cancel();

        }
        finish();
    }

}
