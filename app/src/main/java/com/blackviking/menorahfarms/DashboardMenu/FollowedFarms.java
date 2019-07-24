package com.blackviking.menorahfarms.DashboardMenu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.blackviking.menorahfarms.CartAndHistory.Cart;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.FarmDetails;
import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.Models.CartModel;
import com.blackviking.menorahfarms.Models.FollowedFarmModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.ViewHolders.CartViewHolder;
import com.blackviking.menorahfarms.ViewHolders.FollowedFarmViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    private DatabaseReference followedFarmRef, userRef, farmRef;
    private String currentuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followed_farms);


        /*---   FIREBASE   ---*/
        followedFarmRef = db.getReference("FollowedFarms");
        userRef = db.getReference("Users");
        farmRef = db.getReference("Farms");
        if (mAuth.getCurrentUser() != null)
            currentuid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        backButton = (ImageView)findViewById(R.id.backButton);
        emptyLayout = (LinearLayout)findViewById(R.id.emptyLayout);
        followedFarmRecycler = (RecyclerView)findViewById(R.id.followedFarmsRecycler);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        /*---   CHECK IF USER CART EMPTY   ---*/
        followedFarmRef.child(currentuid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            emptyLayout.setVisibility(View.GONE);
                            loadFollowedFarms();

                        } else {

                            emptyLayout.setVisibility(View.VISIBLE);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void loadFollowedFarms() {

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

                                String theFarmType = dataSnapshot.child("farmType").getValue().toString();
                                final String theFarmROI = dataSnapshot.child("farmRoi").getValue().toString();
                                final String theFarmUnitPrice = dataSnapshot.child("pricePerUnit").getValue().toString();
                                String theFarmSponsorDuration = dataSnapshot.child("sponsorDuration").getValue().toString();
                                final String theFarmImage = dataSnapshot.child("farmImageThumb").getValue().toString();
                                final String theFarmState = dataSnapshot.child("farmState").getValue().toString();

                                long priceToLong = Long.parseLong(theFarmUnitPrice);

                                viewHolder.followedFarmType.setText(theFarmType);
                                viewHolder.followedFarmROI.setText(theFarmROI + "% returns in " + theFarmSponsorDuration + " months");
                                viewHolder.followedFarmState.setText(theFarmState);
                                viewHolder.followedFarmPrice.setText(Common.convertToPrice(FollowedFarms.this, priceToLong));

                                if (!theFarmImage.equalsIgnoreCase("")){

                                    Picasso.get()
                                            .load(theFarmImage)
                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                            .placeholder(R.drawable.menorah_placeholder)
                                            .into(viewHolder.followedFarmImage, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    Picasso.get()
                                                            .load(theFarmImage)
                                                            .placeholder(R.drawable.menorah_placeholder)
                                                            .into(viewHolder.followedFarmImage);
                                                }
                                            });

                                } else {

                                    viewHolder.followedFarmImage.setImageResource(R.drawable.menorah_placeholder);

                                }


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

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            }
        };
        followedFarmRecycler.setAdapter(adapter);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
