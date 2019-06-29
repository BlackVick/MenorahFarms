package com.blackviking.menorahfarms;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.Common;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Arrays;

import dmax.dialog.SpotsDialog;

public class SignUp extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CallbackManager mCallbackManager;
    private ImageView facebookSignIn, googleSignIn;
    private MaterialEditText registerFirstName, registerLastName, registerEmail, registerPassword;
    private Button signupButton;
    private TextView loginLink;
    private int RC_SIGN_IN = 2;
    private GoogleApiClient mGoogleApiClient;
    private android.app.AlertDialog mDialog;
    private boolean isPasswordVisible = false;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");


        /*---   WIDGETS   ---*/
        facebookSignIn = (ImageView)findViewById(R.id.btn_fb_signUp);
        googleSignIn = (ImageView)findViewById(R.id.googleSignIn);
        registerFirstName = (MaterialEditText)findViewById(R.id.registerFirstName);
        registerLastName = (MaterialEditText)findViewById(R.id.registerLastName);
        registerEmail = (MaterialEditText)findViewById(R.id.registerEmail);
        registerPassword = (MaterialEditText)findViewById(R.id.registerPassword);
        signupButton = (Button)findViewById(R.id.signUpButton);
        loginLink = (TextView)findViewById(R.id.loginLink);



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

                    Common.showErrorDialog(SignUp.this, "No Internet Access !");

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
                    //signInWithGoogle();

                } else {

                    Common.showErrorDialog(SignUp.this, "No Internet Access !");

                }
            }
        });




    }

    private void handleFacebookAccessToken(AccessToken token) {
        Toast.makeText(this, "Token :"+token, Toast.LENGTH_SHORT).show();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(SignUp.this, "Sign In Successful", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignUp.this, ""+task.getException(), Toast.LENGTH_SHORT).show();
                            Toast.makeText(SignUp.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void updateUI(FirebaseUser user) {

        if (user != null){
            Toast.makeText(this, "Go To DashBoard", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
