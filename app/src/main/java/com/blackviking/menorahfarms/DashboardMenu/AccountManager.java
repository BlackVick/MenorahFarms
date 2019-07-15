package com.blackviking.menorahfarms.DashboardMenu;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountManager extends AppCompatActivity {

    private ImageView backButton;
    private CircleImageView accountManagerAvatar;
    private TextView accountManagerName, accountManagerAddress;
    private Button callManagerButton, whatsappManagerButton;
    private RelativeLayout notEmptyLayout;
    private LinearLayout emptyLayout;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, accountManagerRef;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_manager);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        accountManagerRef = db.getReference("AccountManagers");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        backButton = (ImageView)findViewById(R.id.backButton);
        accountManagerAvatar = (CircleImageView) findViewById(R.id.accountManagerAvatar);
        accountManagerName = (TextView) findViewById(R.id.accountManagerName);
        accountManagerAddress = (TextView)findViewById(R.id.accountManagerAddress);
        callManagerButton = (Button) findViewById(R.id.callManagerButton);
        whatsappManagerButton = (Button)findViewById(R.id.whatsappManagerButton);
        notEmptyLayout = (RelativeLayout) findViewById(R.id.notEmptyLayout);
        emptyLayout = (LinearLayout) findViewById(R.id.emptyLayout);


        /*---   CURRENT USER   ---*/
        userRef.child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String theManager = dataSnapshot.child("accountManager").getValue().toString();

                        if (!theManager.equalsIgnoreCase("")){

                            notEmptyLayout.setVisibility(View.VISIBLE);
                            emptyLayout.setVisibility(View.GONE);
                            loadAccountManager(theManager);

                        } else {

                            notEmptyLayout.setVisibility(View.GONE);
                            emptyLayout.setVisibility(View.VISIBLE);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadAccountManager(String theManager) {

        accountManagerRef.child(theManager)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String theName = dataSnapshot.child("name").getValue().toString();
                        String theAddress = dataSnapshot.child("address").getValue().toString();
                        final String theProfilePic = dataSnapshot.child("profilePicture").getValue().toString();
                        final String thePhone = dataSnapshot.child("phone").getValue().toString();
                        final String theWhatsapp = dataSnapshot.child("whatsapp").getValue().toString();

                        accountManagerName.setText(theName);
                        accountManagerAddress.setText(theAddress);

                        if (!theProfilePic.equalsIgnoreCase("")){

                            Picasso.get()
                                    .load(theProfilePic)
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.profile)
                                    .into(accountManagerAvatar, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Picasso.get()
                                                    .load(theProfilePic)
                                                    .placeholder(R.drawable.profile)
                                                    .into(accountManagerAvatar);
                                        }
                                    });

                        }

                        callManagerButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse("tel:"+thePhone));
                                startActivity(intent);
                            }
                        });

                        whatsappManagerButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                String whatsappUrl = "https://api.whatsapp.com/send?phone=+"+theWhatsapp;

                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(whatsappUrl));
                                startActivity(i);

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }
}
