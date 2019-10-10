package com.blackviking.menorahfarms.DashboardMenu;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Models.NotificationModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.ViewHolders.NotificationViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<NotificationModel, NotificationViewHolder> adapter;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef, notificationRef;
    private String currentUid;


    private android.app.AlertDialog alertDialog;
    private RelativeLayout noInternetLayout;

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
        noInternetLayout = findViewById(R.id.noInternetLayout);


        //show loading dialog
        showLoadingDialog("Loading notifications . . .");

        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

                //check all cases
                if (output == 1){

                    notificationRef.child(currentUid)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.exists()){

                                        emptyLayout.setVisibility(View.GONE);
                                        noInternetLayout.setVisibility(View.GONE);
                                        notificationRecycler.setVisibility(View.VISIBLE);
                                        loadNotifications();

                                    } else {

                                        alertDialog.dismiss();
                                        emptyLayout.setVisibility(View.VISIBLE);
                                        noInternetLayout.setVisibility(View.GONE);
                                        notificationRecycler.setVisibility(View.GONE);

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                } else

                if (output == 0){

                    //set layout
                    alertDialog.dismiss();
                    noInternetLayout.setVisibility(View.VISIBLE);
                    notificationRecycler.setVisibility(View.GONE);
                    emptyLayout.setVisibility(View.GONE);

                } else

                if (output == 2){

                    //set layout
                    alertDialog.dismiss();
                    noInternetLayout.setVisibility(View.VISIBLE);
                    notificationRecycler.setVisibility(View.GONE);
                    emptyLayout.setVisibility(View.GONE);

                }

            }
        }).execute();


        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void loadNotifications() {

        //cancel dialog
        alertDialog.dismiss();

        notificationRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        notificationRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<NotificationModel, NotificationViewHolder>(
                NotificationModel.class,
                R.layout.naotification_item,
                NotificationViewHolder.class,
                notificationRef.child(currentUid)
        ) {
            @Override
            protected void populateViewHolder(NotificationViewHolder viewHolder, NotificationModel model, int position) {

                viewHolder.notificationTopic.setText(model.getTopic());
                viewHolder.notificationMessage.setText(model.getMessage());
                viewHolder.notificationTime.setText(model.getTime());

            }
        };
        notificationRecycler.setAdapter(adapter);

    }

    /*---   LOADING DIALOG   ---*/
    public void showLoadingDialog(String theMessage){

        alertDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.loading_dialog,null);

        final TextView loadingText = viewOptions.findViewById(R.id.loadingText);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        loadingText.setText(theMessage);

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });

        alertDialog.show();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
