package com.blackviking.menorahfarms;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.HomeActivities.Dashboard;
import com.blackviking.menorahfarms.HomeActivities.Home;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {

    private MaterialEditText loginEmail, loginPassword;
    private Button loginButton;
    private ImageView googleSignIn, showPassword;
    private TextView registerLink, recoverPassword;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, authed;
    private int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    private android.app.AlertDialog mDialog;
    private boolean isPasswordVisible = false;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        authed = db.getReference("AuthedUsers");


        /*---   WIDGETS   ---*/
        loginEmail = (MaterialEditText)findViewById(R.id.loginEmail);
        loginPassword = (MaterialEditText)findViewById(R.id.loginPassword);
        loginButton = (Button)findViewById(R.id.loginButton);
        googleSignIn = (ImageView)findViewById(R.id.googleSignIn);
        showPassword = (ImageView)findViewById(R.id.showPassword);
        registerLink = (TextView)findViewById(R.id.registerLink);
        recoverPassword = (TextView)findViewById(R.id.recoverPassword);

        /*---   GOOGLE INIT   ---*/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(SignIn.this, "Unknown Error Occurred", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getBaseContext())) {

                    mDialog = new SpotsDialog(SignIn.this, "Please Wait . . .");
                    mDialog.setCancelable(false);
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                    signInWithGoogle();

                } else {

                    showErrorDialog("No Internet Access !");

                }
            }
        });



        /*---   EMAIL SIGN IN   ---*/
        /*---   SIGN IN   ---*/
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())) {

                    mDialog = new SpotsDialog(SignIn.this, "Please Wait . . .");
                    mDialog.setCancelable(false);
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                    signInUserWithEmail();
                    loginPassword.onEditorAction(EditorInfo.IME_ACTION_DONE);

                } else {

                    showErrorDialog("No Internet Access !");

                }
            }
        });



        /*---   RESET PASSWORD   ---*/
        recoverPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())) {

                    mDialog = new SpotsDialog(SignIn.this, "Please Wait . . .");
                    mDialog.setCancelable(false);
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                    resetThePassword();

                } else {
                    showErrorDialog("No Internet Access !");
                }
            }
        });



        /*---   SIGN UP   ---*/
        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(SignIn.this, SignUp.class);
                startActivity(registerIntent);
            }
        });



        /*---   PASSWORD VISIBILITY   ---*/
        showPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isPasswordVisible){

                    isPasswordVisible = true;
                    loginPassword.setTransformationMethod(null);
                    showPassword.setImageResource(R.drawable.ic_invisible_password);

                } else {

                    isPasswordVisible = false;
                    showPassword.setImageResource(R.drawable.ic_visible_password);
                    loginPassword.setTransformationMethod(new PasswordTransformationMethod());

                }


            }
        });
    }

    private void signInUserWithEmail() {

        String theEmail = loginEmail.getText().toString().trim();
        String thePassword = loginPassword.getText().toString();

        if (TextUtils.isEmpty(theEmail) || !isValidEmail(theEmail)){

            loginEmail.setError("Provide A Valid E-Mail Address");
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .playOn(loginButton);
            mDialog.dismiss();
            loginEmail.requestFocus();

        } else if (TextUtils.isEmpty(thePassword)){

            loginPassword.setError("Provide Your Password");
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .playOn(loginButton);
            mDialog.dismiss();
            loginPassword.requestFocus();

        } else {

            signInWithEmail(theEmail, thePassword);

        }

    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null)
            return false;

        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private void signInWithEmail(String theEmail, String thePassword) {

        mAuth.signInWithEmailAndPassword(theEmail, thePassword).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            handleSignInResponse();

                        } else {

                            showErrorDialog("Email does not exist or has been used in a different sign in method.");

                        }
                    }
                }
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showErrorDialog("Sign in failed, please check provided details and try again later");
                mDialog.dismiss();
            }
        });

    }

    private void handleSignInResponse() {

        if (mAuth.getCurrentUser() != null) {

            updateUI(mAuth.getCurrentUser());

        } else {

           showErrorDialog("Process Failed");
            mAuth.getCurrentUser().delete();
            mAuth.signOut();
            mDialog.dismiss();

        }

    }

    private void resetThePassword() {

        String theResetMail = loginEmail.getText().toString().trim();

        if (TextUtils.isEmpty(theResetMail) || !isValidEmail(theResetMail)){

            loginEmail.setError("Please Provide The E-Mail You Want A Password Reset For.");
            loginEmail.requestFocus();
            mDialog.dismiss();

        } else {

            mAuth.sendPasswordResetEmail(theResetMail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                showErrorDialog("Password Reset Instructions Have Been Sent To Your Mail !");
                                mDialog.dismiss();

                            } else {

                                showErrorDialog("Password Reset Failed !");
                                mDialog.dismiss();

                            }
                        }
                    });

        }
    }

    private void signInWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            final FirebaseUser user = mAuth.getCurrentUser();

                            authed.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.child(user.getUid()).exists()){

                                        updateUI(user);

                                    } else {

                                        authed.child(user.getUid())
                                                .child("userEmail")
                                                .setValue(user.getEmail()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                registerGoogleUser(user);

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                showErrorDialog("Authentication Could Not Be Registered. Please Try Again Later.");
                                                mDialog.dismiss();
                                                if (mAuth.getCurrentUser() != null){

                                                    mAuth.getCurrentUser().delete();
                                                    mAuth.signOut();

                                                }
                                            }
                                        });

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        } else {

                            mDialog.dismiss();
                            mAuth.signOut();
                            showErrorDialog("Authentication Failed. Mail may already exist through another sign in method. Please confirm and try again.");

                        }

                    }
                });
    }

    private void registerGoogleUser(final FirebaseUser user) {

        currentUid = user.getUid();

        final Map<String, Object> newUserMap = new HashMap<>();
        newUserMap.put("email", user.getEmail());
        newUserMap.put("firstName", user.getDisplayName());
        newUserMap.put("lastName", user.getDisplayName());
        newUserMap.put("profilePicture", "");
        newUserMap.put("profilePictureThumb", "");
        newUserMap.put("signUpMode", "Google");
        newUserMap.put("facebook", "");
        newUserMap.put("instagram", "");
        newUserMap.put("twitter", "");
        newUserMap.put("userType", "User");
        newUserMap.put("userPackage", "Worker");
        newUserMap.put("phone", "");
        newUserMap.put("birthday", "");
        newUserMap.put("gender", "");
        newUserMap.put("nationality", "");
        newUserMap.put("address", "");
        newUserMap.put("city", "");
        newUserMap.put("state", "");
        newUserMap.put("bank", "");
        newUserMap.put("accountName", "");
        newUserMap.put("accountNumber", "");
        newUserMap.put("kinName", "");
        newUserMap.put("kinEmail", "");
        newUserMap.put("kinRelationship", "");
        newUserMap.put("kinPhone", "");
        newUserMap.put("kinAddress", "");
        newUserMap.put("linkedIn", "");
        newUserMap.put("accountManager", "");

        userRef.child(currentUid)
                .setValue(newUserMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        updateUIRegister(user);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                showErrorDialog("Something Went Wrong. Please Try Again Later.");
                mDialog.dismiss();
                if (mAuth.getCurrentUser() != null) {

                    mAuth.getCurrentUser().delete();
                    authed.child(currentUid).removeValue();
                    mAuth.signOut();

                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()){

                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

            } else {

                mDialog.dismiss();
                Toast.makeText(this, "Google Sign In Failed", Toast.LENGTH_SHORT).show();

            }

        }
    }

    private void updateUIRegister (FirebaseUser user) {

        if (user != null){

            mDialog.dismiss();
            Intent finishRegIntent = new Intent(SignIn.this, Registration.class);
            startActivity(finishRegIntent);
            finish();

        }

    }

    private void updateUI(FirebaseUser user) {

        if (user != null){

            mDialog.dismiss();
            String currentUid = user.getUid();

            Paper.book().write(Common.USER_ID, currentUid);

            FirebaseMessaging.getInstance().subscribeToTopic(currentUid);
            FirebaseMessaging.getInstance().subscribeToTopic(Common.GENERAL_NOTIFY);

            Intent goToHome = new Intent(SignIn.this, Dashboard.class);
            goToHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(goToHome);
            finish();
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
