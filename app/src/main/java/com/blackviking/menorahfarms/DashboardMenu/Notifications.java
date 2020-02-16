package com.blackviking.menorahfarms.DashboardMenu;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.paperdb.Paper;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
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
    private DatabaseReference notificationRef;
    private String currentUid;


    private android.app.AlertDialog alertDialog;
    private boolean isLoading = false;
    private RelativeLayout noInternetLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        //user
        currentUid = Paper.book().read(Common.USER_ID);


        /*---   FIREBASE   ---*/
        notificationRef = db.getReference(Common.NOTIFICATIONS_NODE);


        /*---   WIDGETS   ---*/
        backbutton = findViewById(R.id.backButton);
        emptyLayout = findViewById(R.id.emptyLayout);
        notificationRecycler = findViewById(R.id.notificationRecycler);
        noInternetLayout = findViewById(R.id.noInternetLayout);


        //show loading dialog
        showLoadingDialog("Loading notifications . . .");

        //execute network check async task
        new CheckInternet(this, output -> {

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

        }).execute();


        backbutton.setOnClickListener(v -> finish());

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
                notificationRef.child(currentUid).limitToLast(15)
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

        //loading
        isLoading = true;

        alertDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.loading_dialog,null);

        final TextView loadingText = viewOptions.findViewById(R.id.loadingText);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        loadingText.setText(theMessage);

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                isLoading = false;
            }
        });
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isLoading = false;
            }
        });

        alertDialog.show();

    }

    @Override
    public void onBackPressed() {
        if (isLoading){
            alertDialog.dismiss();
        }
        finish();
    }
}
