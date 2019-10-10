package com.blackviking.menorahfarms.DashboardMenu;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blackviking.menorahfarms.AccountMenus.StudentDetails;
import com.blackviking.menorahfarms.CartAndHistory.Cart;
import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.FarmDetails;
import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.Models.CartModel;
import com.blackviking.menorahfarms.Models.FarmModel;
import com.blackviking.menorahfarms.Models.FollowedFarmModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.ViewHolders.CartViewHolder;
import com.blackviking.menorahfarms.ViewHolders.FollowedFarmViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class FollowedFarms extends AppCompatActivity {

    private ImageView backButton;
    private LinearLayout emptyLayout;
    private RecyclerView followedFarmRecycler;
    private FirebaseRecyclerAdapter<FollowedFarmModel, FollowedFarmViewHolder> adapter;
    private LinearLayoutManager layoutManager;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference followedFarmRef, userRef, farmRef, followedFarmNotiRef;
    private String currentuid;

    private android.app.AlertDialog alertDialog;
    private RelativeLayout noInternetLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followed_farms);


        /*---   FIREBASE   ---*/
        followedFarmRef = db.getReference("FollowedFarms");
        userRef = db.getReference("Users");
        farmRef = db.getReference("Farms");
        followedFarmNotiRef = db.getReference("FollowedFarmsNotification");
        if (mAuth.getCurrentUser() != null)
            currentuid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        backButton = (ImageView)findViewById(R.id.backButton);
        emptyLayout = (LinearLayout)findViewById(R.id.emptyLayout);
        followedFarmRecycler = (RecyclerView)findViewById(R.id.followedFarmsRecycler);
        noInternetLayout = findViewById(R.id.noInternetLayout);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        //show loading dialog
        showLoadingDialog("Loading followed farms . . .");

        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(this, new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

                //check all cases
                if (output == 1){

                    /*---   CHECK IF USER CART EMPTY   ---*/
                    followedFarmRef.child(currentuid)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.exists()){

                                        emptyLayout.setVisibility(View.GONE);
                                        noInternetLayout.setVisibility(View.GONE);
                                        followedFarmRecycler.setVisibility(View.VISIBLE);
                                        loadFollowedFarms();

                                    } else {

                                        alertDialog.dismiss();
                                        emptyLayout.setVisibility(View.VISIBLE);
                                        noInternetLayout.setVisibility(View.GONE);
                                        followedFarmRecycler.setVisibility(View.GONE);

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
                    followedFarmRecycler.setVisibility(View.GONE);
                    emptyLayout.setVisibility(View.GONE);

                } else

                if (output == 2){

                    //set layout
                    alertDialog.dismiss();
                    noInternetLayout.setVisibility(View.VISIBLE);
                    followedFarmRecycler.setVisibility(View.GONE);
                    emptyLayout.setVisibility(View.GONE);

                }

            }
        }).execute();

    }

    private void loadFollowedFarms() {

        //close dialog
        alertDialog.dismiss();

        followedFarmRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        followedFarmRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<FollowedFarmModel, FollowedFarmViewHolder>(
                FollowedFarmModel.class,
                R.layout.followed_farm_item,
                FollowedFarmViewHolder.class,
                followedFarmRef.child(currentuid)
        ) {
            @Override
            protected void populateViewHolder(final FollowedFarmViewHolder viewHolder, FollowedFarmModel model, int position) {

                String theKey = adapter.getRef(viewHolder.getAdapterPosition()).getKey();

                farmRef.child(theKey)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                final FarmModel currentFarm = dataSnapshot.getValue(FarmModel.class);

                                if (currentFarm != null){

                                    long priceToLong = Long.parseLong(currentFarm.getPricePerUnit());

                                    viewHolder.followedFarmType.setText(currentFarm.getFarmType());
                                    viewHolder.followedFarmROI.setText(currentFarm.getFarmRoi() + "% returns in " + currentFarm.getSponsorDuration() + " months");
                                    viewHolder.followedFarmState.setText(currentFarm.getFarmState());
                                    viewHolder.followedFarmPrice.setText(Common.convertToPrice(FollowedFarms.this, priceToLong));

                                    if (!currentFarm.getFarmImageThumb().equalsIgnoreCase("")){

                                        Picasso.get()
                                                .load(currentFarm.getFarmImageThumb())
                                                .networkPolicy(NetworkPolicy.OFFLINE)
                                                .placeholder(R.drawable.menorah_placeholder)
                                                .into(viewHolder.followedFarmImage, new Callback() {
                                                    @Override
                                                    public void onSuccess() {

                                                    }

                                                    @Override
                                                    public void onError(Exception e) {
                                                        Picasso.get()
                                                                .load(currentFarm.getFarmImageThumb())
                                                                .placeholder(R.drawable.menorah_placeholder)
                                                                .into(viewHolder.followedFarmImage);
                                                    }
                                                });

                                    } else {

                                        viewHolder.followedFarmImage.setImageResource(R.drawable.menorah_placeholder);

                                    }


                                    viewHolder.unfollowButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            openConfirmationText(adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                                        }
                                    });


                                    viewHolder.setItemClickListener(new ItemClickListener() {
                                        @Override
                                        public void onClick(View view, int position, boolean isLongClick) {
                                            Intent farmDetailIntent = new Intent(FollowedFarms.this, FarmDetails.class);
                                            farmDetailIntent.putExtra("FarmId", adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                                            startActivity(farmDetailIntent);
                                            overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
                                        }
                                    });

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            }
        };
        followedFarmRecycler.setAdapter(adapter);

    }

    private void openConfirmationText(final String key) {

        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
        LayoutInflater inflater = FollowedFarms.this.getLayoutInflater();
        View viewOptions = inflater.inflate(R.layout.unfollow_layout,null);

        final Button cancel = (Button) viewOptions.findViewById(R.id.cancelAcada);
        final Button proceed = (Button) viewOptions.findViewById(R.id.proceedAcada);

        alertDialog.setView(viewOptions);

        alertDialog.getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //execute network check async task
                CheckInternet asyncTask = (CheckInternet) new CheckInternet(FollowedFarms.this, new CheckInternet.AsyncResponse(){
                    @Override
                    public void processFinish(Integer output) {

                        //check all cases
                        if (output == 1){

                            followedFarmRef.child(currentuid)
                                    .child(key)
                                    .removeValue()
                                    .addOnCompleteListener(
                                            new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    alertDialog.dismiss();

                                                    farmRef.child(key)
                                                            .addListenerForSingleValueEvent(
                                                                    new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                                                            String theNotiId = dataSnapshot.child("farmNotiId").getValue().toString();

                                                                            //unsubscribe to notification
                                                                            removeNotification(theNotiId);

                                                                            Toast.makeText(FollowedFarms.this, "Farm un-followed", Toast.LENGTH_SHORT).show();

                                                                        }

                                                                        @Override
                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                        }
                                                                    }
                                                            );

                                                }
                                            }
                                    );

                        } else

                        if (output == 0){

                            Toast.makeText(FollowedFarms.this, "No internet access", Toast.LENGTH_SHORT).show();

                        } else

                        if (output == 2){

                            Toast.makeText(FollowedFarms.this, "No network detected", Toast.LENGTH_SHORT).show();

                        }

                    }
                }).execute();


            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });

        alertDialog.show();

    }

    private void removeNotification(String theNotiId) {

        followedFarmNotiRef.child(theNotiId)
                .child(currentuid)
                .removeValue();

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
