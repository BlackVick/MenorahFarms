package com.blackviking.menorahfarms;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Arrays;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {

    private MaterialEditText loginEmail, loginPassword;
    private Button loginButton;
    private ImageView googleSignIn, facebookSignIn, showPassword;
    private TextView registerLink, recoverPassword;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef;
    private CallbackManager mCallbackManager;
    private int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    private android.app.AlertDialog mDialog;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");


        /*---   WIDGETS   ---*/
        loginEmail = (MaterialEditText)findViewById(R.id.loginEmail);
        loginPassword = (MaterialEditText)findViewById(R.id.loginPassword);
        loginButton = (Button)findViewById(R.id.loginButton);
        googleSignIn = (ImageView)findViewById(R.id.googleSignIn);
        facebookSignIn = (ImageView)findViewById(R.id.facebookSignIn);
        showPassword = (ImageView)findViewById(R.id.showPassword);
        registerLink = (TextView)findViewById(R.id.registerLink);
        recoverPassword = (TextView)findViewById(R.id.recoverPassword);


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
                        Toast.makeText(SignIn.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(SignIn.this, ""+exception, Toast.LENGTH_SHORT).show();
                    }
                });

        facebookSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getBaseContext())) {

                    mDialog = new SpotsDialog(SignIn.this, "Processing . . .");
                    mDialog.setCancelable(false);
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                    LoginManager.getInstance().logInWithReadPermissions(SignIn.this, Arrays.asList("email", "public_profile"));

                } else {

                    Common.showErrorDialog(SignIn.this, "No Internet Access !");

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
                        Toast.makeText(SignIn.this, "Unknown Error Occurred", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getBaseContext())) {

                    mDialog = new SpotsDialog(SignIn.this, "Processing . . .");
                    mDialog.setCancelable(false);
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                    signInWithGoogle();

                } else {

                    Common.showErrorDialog(SignIn.this, "No Internet Access !");

                }
            }
        });



        /*---   EMAIL SIGN IN   ---*/
        /*---   SIGN IN   ---*/
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())) {

                    mDialog = new SpotsDialog(SignIn.this, "Processing . . .");
                    mDialog.setCancelable(false);
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                    signInUserWithEmail();
                    loginPassword.onEditorAction(EditorInfo.IME_ACTION_DONE);

                } else {

                    Common.showErrorDialog(SignIn.this, "No Internet Access !");

                }
            }
        });



        /*---   RESET PASSWORD   ---*/
        recoverPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.isConnectedToInternet(getBaseContext())) {

                    mDialog = new SpotsDialog(SignIn.this, "Processing . . .");
                    mDialog.setCancelable(false);
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                    resetThePassword();

                } else {
                    Common.showErrorDialog(SignIn.this, "No Internet Access !");
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

                        }
                    }
                }
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Common.showErrorDialog(SignIn.this, "Sign in failed, please check provided details and try again later");
                mDialog.dismiss();
            }
        });

    }

    private void handleSignInResponse() {

        if (mAuth.getCurrentUser() != null) {

            /*currentUid = mAuth.getCurrentUser().getUid();

            progressBar.setVisibility(View.GONE);

            *//*---   LOCAL   ---*//*
            Paper.book().write(Common.USER_ID, currentUid);

            FirebaseMessaging.getInstance().subscribeToTopic(currentUid);
            Paper.book().write(Common.NOTIFICATION_STATE, "true");

            Paper.book().write(Common.isSubServiceRunning, false);
            Intent intent = new Intent(getApplicationContext(), CheckSubStatusService.class);
            startService(intent);

            FirebaseMessaging.getInstance().subscribeToTopic(Common.FEED_NOTIFICATION_TOPIC+currentUid);
            Paper.book().write(Common.MY_FEED_NOTIFICATION_STATE, "true");

            Intent signInIntent = new Intent(Login.this, Home.class);
            startActivity(signInIntent);
            finish();
            overridePendingTransition(R.anim.slide_left, R.anim.slide_left);*/

            updateUI(mAuth.getCurrentUser());

        } else {

            Common.showErrorDialog(SignIn.this, "Process Failed");
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

                                Common.showErrorDialog(SignIn.this, "Password Reset Instructions Have Been Sent To Your Mail !");
                                mDialog.dismiss();

                            } else {

                                Common.showErrorDialog(SignIn.this, "Password Reset Failed !");
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

    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(SignIn.this, "Sign In Successful", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignIn.this, ""+task.getException(), Toast.LENGTH_SHORT).show();
                            Toast.makeText(SignIn.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
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

                Toast.makeText(this, "Google Sign In Failed", Toast.LENGTH_SHORT).show();

            }

        } else {

            mCallbackManager.onActivityResult(requestCode, resultCode, data);

        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);

                        } else {

                            Toast.makeText(SignIn.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();

                        }

                    }
                });
    }

    private void updateUI(FirebaseUser user) {

        if (user != null){
            Toast.makeText(this, "Go To DashBoard", Toast.LENGTH_SHORT).show();
        }

    }
}
