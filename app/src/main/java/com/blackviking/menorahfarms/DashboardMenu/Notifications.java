package com.blackviking.menorahfarms.DashboardMenu;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.blackviking.menorahfarms.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Notifications extends AppCompatActivity {

    private ImageView backbutton;
    private LinearLayout emptyLayout;
    private RecyclerView notificationRecycler;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef, notificationRef;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        notificationRef = db.getReference("Notifications");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        backbutton = (ImageView)findViewById(R.id.backButton);
        emptyLayout = (LinearLayout)findViewById(R.id.emptyLayout);
        notificationRecycler = (RecyclerView)findViewById(R.id.notificationRecycler);


        /*---   CHECK EMPTY   ---*/
        notificationRef.child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            emptyLayout.setVisibility(View.GONE);
                            loadNotifications();

                        } else {

                            emptyLayout.setVisibility(View.VISIBLE);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void loadNotifications() {
    }
}
