package com.blackviking.menorahfarms;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.Common.ApplicationClass;
import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.HomeActivities.Dashboard;
import com.blackviking.menorahfarms.Models.UserModel;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {

    private MaterialEditText loginEmail, loginPassword;
    private Button loginButton;
    private ImageView googleSignIn, appleSignIn, showPassword;
    private TextView registerLink, recoverPassword;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, authed;
    private int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleApiClient;
    private android.app.AlertDialog mDialog;
    private boolean isPasswordVisible = false;
    private String currentUid;
    private OAuthProvider.Builder provider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        //firebase
        userRef = db.getReference(Common.USERS_NODE);
        authed = db.getReference(Common.AUTHED_USERS_NODE);
        provider = OAuthProvider.newBuilder("apple.com");


        //widgets
        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        googleSignIn = findViewById(R.id.googleSignIn);
        appleSignIn = findViewById(R.id.appleSignIn);
        showPassword = findViewById(R.id.showPassword);
        registerLink = findViewById(R.id.registerLink);
        recoverPassword = findViewById(R.id.recoverPassword);

        //init ui
        initializeUI();

    }

    private void initializeUI() {

        //init google api
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, connectionResult -> Toast.makeText(SignIn.this, "Unable to connect to google network", Toast.LENGTH_LONG).show())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //google sign in
        googleSignIn.setOnClickListener(v -> {

            //show dialog
            mDialog = new SpotsDialog(SignIn.this, "Please Wait . . .");
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();

            //execute network check async task
            new CheckInternet(SignIn.this, output -> {

                //check all cases
                if (output == 1){

                    signInWithGoogle();

                } else

                if (output == 0){

                    //no internet
                    mDialog.dismiss();
                    Toast.makeText(SignIn.this, "No internet access", Toast.LENGTH_SHORT).show();

                } else

                if (output == 2){

                    //no internet
                    mDialog.dismiss();
                    Toast.makeText(SignIn.this, "Not connected to any network", Toast.LENGTH_SHORT).show();

                }

            }).execute();

        });

        //email sign in
        loginButton.setOnClickListener(v -> {

            //show dialog
            mDialog = new SpotsDialog(SignIn.this, "Please Wait . . .");
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();

            //execute network check async task
            new CheckInternet(SignIn.this, output -> {

                //check all cases
                if (output == 1){

                    signInUserWithEmail();
                    loginPassword.onEditorAction(EditorInfo.IME_ACTION_DONE);

                } else

                if (output == 0){

                    //no internet
                    mDialog.dismiss();
                    Toast.makeText(SignIn.this, "No internet access", Toast.LENGTH_SHORT).show();

                } else

                if (output == 2){

                    //no internet
                    mDialog.dismiss();
                    Toast.makeText(SignIn.this, "Not connected to any network", Toast.LENGTH_SHORT).show();

                }

            }).execute();

        });

        //apple sign in
        appleSignIn.setOnClickListener(view -> {

            //init apple sign in process
            signInWithApple();

        });

        //reset password
        recoverPassword.setOnClickListener(v -> {

            //show dialog
            mDialog = new SpotsDialog(SignIn.this, "Please Wait . . .");
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();

            //run network check
            new CheckInternet(SignIn.this, output -> {

                //check all cases
                if (output == 1){

                    resetThePassword();

                } else

                if (output == 0){

                    //no internet
                    mDialog.dismiss();
                    Toast.makeText(SignIn.this, "No internet access", Toast.LENGTH_SHORT).show();

                } else

                if (output == 2){

                    //no internet
                    mDialog.dismiss();
                    Toast.makeText(SignIn.this, "Not connected to any network", Toast.LENGTH_LONG).show();

                }

            }).execute();

        });

        //sign up link
        registerLink.setOnClickListener(v -> {
            Intent registerIntent = new Intent(SignIn.this, SignUp.class);
            startActivity(registerIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        //password visibility
        showPassword.setOnClickListener(v -> {

            if (!isPasswordVisible){

                isPasswordVisible = true;
                loginPassword.setTransformationMethod(null);
                showPassword.setImageResource(R.drawable.ic_invisible_password);

            } else {

                isPasswordVisible = false;
                showPassword.setImageResource(R.drawable.ic_visible_password);
                loginPassword.setTransformationMethod(new PasswordTransformationMethod());

            }


        });

    }

    private void signInWithApple() {

        Task<AuthResult> pending = mAuth.getPendingAuthResult();
        if (pending != null) {

            pending.addOnSuccessListener(authResult -> {

                //get user
                FirebaseUser user = authResult.getUser();
                loadAppleSignIn(user);

            }).addOnFailureListener(e -> {

                //apple auth failed
                Toast.makeText(SignIn.this, "Apple auth Failed", Toast.LENGTH_LONG).show();

            });

        } else {

            //init auth
            mAuth.startActivityForSignInWithProvider(this, provider.build())
                    .addOnSuccessListener(authResult -> {

                        // Sign-in successful!
                        FirebaseUser user = authResult.getUser();
                        loadAppleSignIn(user);

                    })
                    .addOnFailureListener(e -> {

                                //apple auth failed
                                Toast.makeText(SignIn.this, "Apple auth Failed", Toast.LENGTH_LONG).show();

                            });

        }





    }

    private void loadAppleSignIn(FirebaseUser user) {

        authed.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(user.getUid()).exists()){

                    updateUI(user);

                } else {

                    authed.child(user.getUid())
                            .child("userEmail")
                            .setValue(user.getEmail())
                            .addOnCompleteListener(task1 -> {

                                if (task1.isSuccessful()){

                                    registerAppleUser(user);

                                } else {

                                    //show error
                                    Toast.makeText(SignIn.this, "Registration unsuccessful. Please try again later.", Toast.LENGTH_SHORT).show();

                                    //dismiss dialog
                                    mDialog.dismiss();

                                    //remove auth
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

    }

    private void registerAppleUser(FirebaseUser user) {

        currentUid = user.getUid();

        //name separation
        String[] parts = user.getDisplayName().split("\\s+");
        final String theFirstName = parts[0];
        final String theLastName = parts[1];
        final String theEmail = user.getEmail();

        final Map<String, Object> newUserMap = new HashMap<>();
        newUserMap.put("email", theEmail);
        newUserMap.put("firstName", theFirstName);
        newUserMap.put("lastName", theLastName);
        newUserMap.put("profilePicture", "");
        newUserMap.put("profilePictureThumb", "");
        newUserMap.put("signUpMode", "Apple");
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
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()){

                        updateUIRegister(user);

                    } else {

                        //show error
                        Toast.makeText(SignIn.this, "Something happened. Please try again later.", Toast.LENGTH_LONG).show();

                        //dismiss dialog
                        mDialog.dismiss();

                        //undo auth and remove from db
                        if (mAuth.getCurrentUser() != null) {

                            mAuth.getCurrentUser().delete();
                            authed.child(currentUid).removeValue();
                            mAuth.signOut();

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
                task -> {
                    if (task.isSuccessful()){

                        handleSignInResponse();

                    } else {

                        Toast.makeText(this, "Email does not exist or has been used in a different sign in method.", Toast.LENGTH_LONG).show();

                    }
                }
        );

    }

    private void handleSignInResponse() {

        if (mAuth.getCurrentUser() != null) {

            updateUI(mAuth.getCurrentUser());

        } else {

            Toast.makeText(this, "Process Failed", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            mDialog.dismiss();

        }

    }

    private void resetThePassword() {

        String theResetMail = loginEmail.getText().toString().trim();

        if (TextUtils.isEmpty(theResetMail) || !isValidEmail(theResetMail)){

            loginEmail.setError("Please provide e-mail");
            loginEmail.requestFocus();
            mDialog.dismiss();

        } else {

            mAuth.sendPasswordResetEmail(theResetMail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){

                            Toast.makeText(this, "Reset instructions have been sent to your mail!", Toast.LENGTH_LONG).show();
                            mDialog.dismiss();

                        } else {

                            Toast.makeText(this, "Password reset failed !", Toast.LENGTH_SHORT).show();
                            mDialog.dismiss();

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
                .addOnCompleteListener(this, task -> {
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
                                            .setValue(user.getEmail())
                                            .addOnCompleteListener(task1 -> {

                                                if (task1.isSuccessful()){

                                                    registerGoogleUser(user);

                                                } else {

                                                    //show error
                                                    Toast.makeText(SignIn.this, "Registration unsuccessful. Please try again later.", Toast.LENGTH_SHORT).show();

                                                    //dismiss dialog
                                                    mDialog.dismiss();

                                                    //remove auth
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

                        //dismiss dialog
                        mDialog.dismiss();

                        //remove auth
                        mAuth.signOut();

                        //show error
                        Toast.makeText(SignIn.this, "Sign in failed. Please try again later.", Toast.LENGTH_SHORT).show();

                    }

                });
    }

    private void registerGoogleUser(final FirebaseUser user) {

        currentUid = user.getUid();

        //name separation
        String[] parts = user.getDisplayName().split("\\s+");
        final String theFirstName = parts[0];
        final String theLastName = parts[1];
        final String theEmail = user.getEmail();

        final Map<String, Object> newUserMap = new HashMap<>();
        newUserMap.put("email", theEmail);
        newUserMap.put("firstName", theFirstName);
        newUserMap.put("lastName", theLastName);
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
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()){

                        updateUIRegister(user);

                    } else {

                        //show error
                        Toast.makeText(SignIn.this, "Something happened. Please try again later.", Toast.LENGTH_LONG).show();

                        //dismiss dialog
                        mDialog.dismiss();

                        //undo auth and remove from db
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
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();

            }

        }
    }

    private void updateUIRegister (FirebaseUser user) {

        if (user != null){

            mDialog.dismiss();
            Intent finishRegIntent = new Intent(SignIn.this, Registration.class);
            startActivity(finishRegIntent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();

        }

    }

    private void updateUI(FirebaseUser user) {

        if (user != null){

            //current uid
            String currentUid = user.getUid();

            //get user profile from db
            userRef.child(currentUid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            //save user profile offline
                            UserModel theUser = dataSnapshot.getValue(UserModel.class);
                            ((ApplicationClass)(getApplicationContext())).setUser(theUser);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            //dismiss dialog
            mDialog.dismiss();

            //save user id offline
            Paper.book().write(Common.USER_ID, currentUid);

            //subscribe to notifications
            FirebaseMessaging.getInstance().subscribeToTopic(currentUid);
            FirebaseMessaging.getInstance().subscribeToTopic(Common.GENERAL_NOTIFY);

            Intent goToHome = new Intent(SignIn.this, Dashboard.class);
            goToHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(goToHome);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }

    }

}
