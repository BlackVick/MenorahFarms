package com.blackviking.menorahfarms;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class SignUp extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    CallbackManager mCallbackManager;
    LoginButton loginButton;
    ImageView facebookSignIn, googleSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Toast.makeText(SignUp.this, ""+loginResult, Toast.LENGTH_SHORT).show();
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

        facebookSignIn = (ImageView)findViewById(R.id.btn_fb_login);
        facebookSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(SignUp.this, Arrays.asList("email", "public_profile"));
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
