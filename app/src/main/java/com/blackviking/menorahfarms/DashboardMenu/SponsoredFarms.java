package com.blackviking.menorahfarms.DashboardMenu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Common.GetTimeAgo;
import com.blackviking.menorahfarms.HomeActivities.FarmShop;
import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.Models.SponsoredFarmModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.Sponsorship.SponsorshipDetails;
import com.blackviking.menorahfarms.ViewHolders.SponsoredFarmViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SponsoredFarms extends AppCompatActivity {

    private ImageView backButton;
    private RelativeLayout goToFarmstoreButton;
    private LinearLayout emptyLayout;
    private RecyclerView sponsoredFarmsRecycler;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference userRef, sponsorshipRef;
    private String currentUid;

    private FirebaseRecyclerAdapter<SponsoredFarmModel, SponsoredFarmViewHolder> adapter;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sponsored_farms);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        sponsorshipRef = db.getReference("SponsoredFarms");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        backButton = (ImageView)findViewById(R.id.backButton);
        goToFarmstoreButton = (RelativeLayout)findViewById(R.id.goToFarmstoreButton);
        emptyLayout = (LinearLayout)findViewById(R.id.emptyLayout);
        sponsoredFarmsRecycler = (RecyclerView)findViewById(R.id.sponsoredFarmsRecycler);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        checkForSponsorship();

    }

    private void checkForSponsorship() {

        sponsorshipRef.child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            loadSponsorships();
                            emptyLayout.setVisibility(View.GONE);

                        } else {

                            emptyLayout.setVisibility(View.VISIBLE);
                            goToFarmstoreButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent farmShopIntent = new Intent(SponsoredFarms.this, FarmShop.class);
                                    startActivity(farmShopIntent);
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

    private void loadSponsorships() {

        sponsoredFarmsRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        sponsoredFarmsRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<SponsoredFarmModel, SponsoredFarmViewHolder>(
                SponsoredFarmModel.class,
                R.layout.sponsored_farm_item,
                SponsoredFarmViewHolder.class,
                sponsorshipRef.child(currentUid)
        ) {
            @Override
            protected void populateViewHolder(final SponsoredFarmViewHolder viewHolder, SponsoredFarmModel model, int position) {

                /*---   GET TIME AGO ALGORITHM   ---*/
                GetTimeAgo getTimeAgo = new GetTimeAgo();
                long lastTime = model.getStartPoint();
                final String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                viewHolder.sponsoredFarmDate.setText(lastSeenTime);

                /*---   PRICE   ---*/
                long priceToLong = Long.parseLong(model.getSponsoredUnitPrice());

                viewHolder.sponsoredFarmType.setText(model.getSponsoredFarmType());
                viewHolder.sponsoredFarmPriceUnit.setText(Common.convertToPrice(SponsoredFarms.this, priceToLong) + " x " + model.getSponsoredUnits() + " units.");

                viewHolder.sponsoredFarmRefNumber.setText(model.getSponsorRefNumber());
                viewHolder.sponsoredFarmROI.setText(model.getSponsoredFarmRoi() + "% returns in " + model.getSponsorshipDuration() + " months.");


                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent sponsorshipDetail = new Intent(SponsoredFarms.this, SponsorshipDetails.class);
                        sponsorshipDetail.putExtra("SponsorshipId", adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                        startActivity(sponsorshipDetail);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
                    }
                });
            }
        };
        sponsoredFarmsRecycler.setAdapter(adapter);

    }
}
