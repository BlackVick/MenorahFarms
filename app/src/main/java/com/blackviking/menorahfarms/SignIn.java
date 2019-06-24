package com.blackviking.menorahfarms;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SignIn extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private Button loginButton;
    private ImageView googleSignIn, facebookSignIn;
    private TextView registerLink;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);


        /*---   WIDGETS   ---*/
        loginEmail = (EditText)findViewById(R.id.loginEmail);
        loginPassword = (EditText)findViewById(R.id.loginPassword);
        loginButton = (Button)findViewById(R.id.loginButton);
        googleSignIn = (ImageView)findViewById(R.id.googleSignIn);
        facebookSignIn = (ImageView)findViewById(R.id.facebookSignIn);
        registerLink = (TextView)findViewById(R.id.registerLink);

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(SignIn.this, SignUp.class);
                startActivity(registerIntent);
            }
        });
    }
}
