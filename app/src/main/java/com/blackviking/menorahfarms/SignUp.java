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
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class SignUp extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CallbackManager mCallbackManager;
    private ImageView facebookSignIn, googleSignIn, showPassword, backButton;
    private MaterialEditText registerFirstName, registerLastName, registerEmail, registerPassword;
    private Button signupButton;
    private TextView loginLink;
    private int RC_SIGN_IN = 2;
    private GoogleApiClient mGoogleApiClient;
    private android.app.AlertDialog mDialog;
    private boolean isPasswordVisible = false;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, authed;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        authed = db.getReference("AuthedUsers");



        /*---   WIDGETS   ---*/
        facebookSignIn = (ImageView)findViewById(R.id.btn_fb_signUp);
        googleSignIn = (ImageView)findViewById(R.id.googleSignUp);
        showPassword = (ImageView)findViewById(R.id.showPassword);
        backButton = (ImageView)findViewById(R.id.backButton);
        registerFirstName = (MaterialEditText)findViewById(R.id.registerFirstName);
        registerLastName = (MaterialEditText)findViewById(R.id.registerLastName);
        registerEmail = (MaterialEditText)findViewById(R.id.registerEmail);
        registerPassword = (MaterialEditText)findViewById(R.id.registerPassword);
        signupButton = (Button)findViewById(R.id.signUpButton);
        loginLink = (TextView)findViewById(R.id.loginLink);


        /*---    BACK   ---*/
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        /*---   FACEBOOK INIT   ---*/
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handleFacebookAccessToken(loginResult.getAccessToken());

                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(SignUp.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(SignUp.this, ""+exception, Toast.LENGTH_SHORT).show();
                    }
                });


        facebookSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())) {

                    mDialog = new SpotsDialog(SignUp.this, "Processing . . .");
                    mDialog.setCancelable(false);
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                    LoginManager.getInstance().logInWithReadPermissions(SignUp.this, Arrays.asList("email", "public_profile"));

                } else {

                    showErrorDialog("No Internet Access !");

                }
            }
        });



        /*---   GOOGLE INIT   ---*/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(SignUp.this, "Unknown Error Occurred", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getBaseContext())) {

                    mDialog = new SpotsDialog(SignUp.this, "Processing . . .");
                    mDialog.setCancelable(false);
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                    signInWithGoogle();

                } else {

                    showErrorDialog("No Internet Access !");

                }
            }
        });


        /*---   EMAIL INIT   ---*/
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())) {

                    mDialog = new SpotsDialog(SignUp.this, "Processing . . .");
                    mDialog.setCancelable(false);
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                    signUpUserWithEmail();
                    registerPassword.onEditorAction(EditorInfo.IME_ACTION_DONE);

                } else {

                    showErrorDialog("No Internet Access !");

                }
            }
        });


        /*---   LOGIN   ---*/
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        /*---   PASSWORD VISIBILITY   ---*/
        showPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isPasswordVisible){

                    isPasswordVisible = true;
                    registerPassword.setTransformationMethod(null);
                    showPassword.setImageResource(R.drawable.ic_invisible_password);

                } else {

                    isPasswordVisible = false;
                    showPassword.setImageResource(R.drawable.ic_visible_password);
                    registerPassword.setTransformationMethod(new PasswordTransformationMethod());

                }


            }
        });


    }

    private void signUpUserWithEmail() {

        String theFirstName = registerFirstName.getText().toString().trim();
        String theLastName = registerLastName.getText().toString().trim();
        String theEmail = registerEmail.getText().toString().trim();
        String thePassword = registerPassword.getText().toString();

        if (TextUtils.isEmpty(theEmail) || !isValidEmail(theEmail)){

            registerEmail.setError("Provide A Valid E-Mail Address");
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .playOn(signupButton);
            mDialog.dismiss();
            registerEmail.requestFocus();

        } else if (TextUtils.isEmpty(thePassword)){

            registerPassword.setError("Provide Your Password");
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .playOn(signupButton);
            mDialog.dismiss();
            registerPassword.requestFocus();

        } else if (thePassword.length() < 6){

            registerPassword.setError("Password Too Weak");
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .playOn(signupButton);
            mDialog.dismiss();
            registerPassword.requestFocus();

        } else if (TextUtils.isEmpty(theFirstName)){

            registerFirstName.setError("Provide Your First Name");
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .playOn(signupButton);
            mDialog.dismiss();
            registerFirstName.requestFocus();

        } else if (TextUtils.isEmpty(theLastName)){

            registerLastName.setError("Provide Your Last Name");
            YoYo.with(Techniques.Shake)
                    .duration(500)
                    .playOn(signupButton);
            mDialog.dismiss();
            registerLastName.requestFocus();

        } else {

            signInWithEmail(theEmail, thePassword, theFirstName, theLastName);

        }

    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null)
            return false;

        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private void signInWithEmail(final String theEmail, String thePassword, final String theFirstName, final String theLastName) {

        mAuth.createUserWithEmailAndPassword(theEmail, thePassword).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            mAuth.getCurrentUser().sendEmailVerification();

                            currentUid = mAuth.getCurrentUser().getUid();
                            authed.child(currentUid)
                                    .child("userEmail")
                                    .setValue(theEmail).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    handleSignInResponse(theEmail, theFirstName, theLastName);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    showErrorDialog("Authentication Could Not Be Registered. Please Try Again Later.");
                                    if (mAuth.getCurrentUser() != null){

                                        mDialog.dismiss();
                                        mAuth.signOut();


                                    }
                                }
                            });

                        } else {

                            mDialog.dismiss();
                            showErrorDialog("Error Occurred While Signing Up With Email. Provided Mail May Already Exist.");

                        }
                    }
                }
        );

    }

    private void handleSignInResponse(String theEmail, String theFirstName, String theLastName) {

        if (mAuth.getCurrentUser() != null) {

            currentUid = mAuth.getCurrentUser().getUid();

            final Map<String, Object> newUserMap = new HashMap<>();
            newUserMap.put("email", theEmail);
            newUserMap.put("firstName", theFirstName);
            newUserMap.put("lastName", theLastName);
            newUserMap.put("profilePicture", "");
            newUserMap.put("profilePictureThumb", "");
            newUserMap.put("signUpMode", "Email");
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

                            updateUI(mAuth.getCurrentUser());

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    showErrorDialog("Something Went Wrong. Please Try Again Later.");
                    if (mAuth.getCurrentUser() != null){

                        mDialog.dismiss();
                        authed.child(currentUid).removeValue();
                        mAuth.signOut();

                    }

                }
            });

        } else {

            showErrorDialog("Process Failed");
            mDialog.dismiss();

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

                            if (user != null) {

                                mAuth.getCurrentUser().sendEmailVerification();

                                currentUid = user.getUid();

                                authed.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.child(currentUid).exists()){

                                            mDialog.dismiss();
                                            showErrorDialog("A User Has Already Registered With This Account, Log In Instead");
                                            mAuth.signOut();

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
                                                    if (mAuth.getCurrentUser() != null){

                                                        mDialog.dismiss();
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

                                showErrorDialog("Error Occurred, Please try Again Later");
                                mDialog.dismiss();

                            }

                        } else {

                            mDialog.dismiss();
                            showErrorDialog("Authentication Failed. Please Try Again Later");

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

                        updateUI(user);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                showErrorDialog("Something Went Wrong. Please Try Again Later.");
                if (mAuth.getCurrentUser() != null) {

                    mDialog.dismiss();
                    authed.child(currentUid).removeValue();
                    mAuth.signOut();

                }

            }
        });

    }

    private void handleFacebookAccessToken(final AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            final FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {

                                mAuth.getCurrentUser().sendEmailVerification();

                                currentUid = user.getUid();

                                authed.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.child(currentUid).exists()){

                                            mDialog.dismiss();
                                            showErrorDialog("A User Has Already Registered With This Email, Try Signing In");
                                            mAuth.signOut();
                                            LoginManager.getInstance().logOut();

                                        } else {

                                            authed.child(user.getUid())
                                                    .child("userEmail")
                                                    .setValue(user.getEmail()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    registerFacebookUser(user, token);

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                    showErrorDialog("Authentication Could Not Be Registered. Please Try Again Later.");
                                                    if (mAuth.getCurrentUser() != null){

                                                        mDialog.dismiss();
                                                        mAuth.signOut();
                                                        LoginManager.getInstance().logOut();

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

                                showErrorDialog("Error Occurred, Please try Again Later");
                                mDialog.dismiss();

                            }

                        } else {

                            mDialog.dismiss();
                            Toast.makeText(SignUp.this, ""+task.getException(), Toast.LENGTH_SHORT).show();
                            Toast.makeText(SignUp.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void registerFacebookUser(final FirebaseUser user, AccessToken token) {

        currentUid = user.getUid();

        final Map<String, Object> newUserMap = new HashMap<>();
        newUserMap.put("email", user.getEmail());
        newUserMap.put("firstName", user.getDisplayName());
        newUserMap.put("lastName", user.getDisplayName());
        newUserMap.put("profilePicture", "");
        newUserMap.put("profilePictureThumb", "");
        newUserMap.put("signUpMode", "Facebook");
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

                        updateUI(user);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                showErrorDialog("Something Went Wrong. Please Try Again Later.");
                if (mAuth.getCurrentUser() != null) {

                    mDialog.dismiss();
                    authed.child(currentUid).removeValue();
                    mAuth.signOut();
                    LoginManager.getInstance().logOut();

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

        } else {


            mCallbackManager.onActivityResult(requestCode, resultCode, data);

        }
    }

    private void updateUI(FirebaseUser user) {

        if (user != null){

            mDialog.dismiss();
            Intent finishRegIntent = new Intent(SignUp.this, Registration.class);
            startActivity(finishRegIntent);
            finish();

        }

    }

    @Override
    public void onBackPressed() {
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
