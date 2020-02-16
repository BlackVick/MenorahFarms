package com.blackviking.menorahfarms.DashboardMenu;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Common.GetTimeAgo;
import com.blackviking.menorahfarms.HomeActivities.FarmShop;
import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.Models.FarmModel;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class SponsoredFarms extends AppCompatActivity {

    private ImageView backButton;
    private RelativeLayout goToFarmstoreButton;
    private LinearLayout emptyLayout;
    private RecyclerView sponsoredFarmsRecycler;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference sponsorshipRef, farmRef;
    private String currentUid;

    private FirebaseRecyclerAdapter<SponsoredFarmModel, SponsoredFarmViewHolder> adapter;
    private LinearLayoutManager layoutManager;

    private android.app.AlertDialog alertDialog;
    private boolean isLoading = false;
    private RelativeLayout noInternetLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sponsored_farms);


        /*---   FIREBASE   ---*/
        farmRef = db.getReference(Common.FARM_NODE);
        sponsorshipRef = db.getReference(Common.SPONSORED_FARMS_NODE);
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGETS   ---*/
        backButton = findViewById(R.id.backButton);
        goToFarmstoreButton = findViewById(R.id.goToFarmstoreButton);
        emptyLayout = findViewById(R.id.emptyLayout);
        sponsoredFarmsRecycler = findViewById(R.id.sponsoredFarmsRecycler);
        noInternetLayout = findViewById(R.id.noInternetLayout);


        //show loading dialog
        showLoadingDialog("Loading sponsorships . . .");

        //run network check
        new CheckInternet(this, output -> {

            //check all cases
            if (output == 1){

                checkForSponsorship();

            } else

            if (output == 0){

                //set layout
                alertDialog.dismiss();
                noInternetLayout.setVisibility(View.VISIBLE);
                sponsoredFarmsRecycler.setVisibility(View.GONE);
                emptyLayout.setVisibility(View.GONE);

            } else

            if (output == 2){

                //set layout
                alertDialog.dismiss();
                noInternetLayout.setVisibility(View.VISIBLE);
                sponsoredFarmsRecycler.setVisibility(View.GONE);
                emptyLayout.setVisibility(View.GONE);

            }

        }).execute();

        backButton.setOnClickListener(v -> finish());

    }

    private void checkForSponsorship() {

        sponsorshipRef.child(currentUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            loadSponsorships();
                            noInternetLayout.setVisibility(View.GONE);
                            sponsoredFarmsRecycler.setVisibility(View.VISIBLE);
                            emptyLayout.setVisibility(View.GONE);

                        } else {

                            alertDialog.dismiss();
                            noInternetLayout.setVisibility(View.GONE);
                            sponsoredFarmsRecycler.setVisibility(View.GONE);
                            emptyLayout.setVisibility(View.VISIBLE);
                            goToFarmstoreButton.setOnClickListener(v -> {
                                Intent farmShopIntent = new Intent(SponsoredFarms.this, FarmShop.class);
                                startActivity(farmShopIntent);
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_in);
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void loadSponsorships() {

        //remove dialog
        alertDialog.dismiss();


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


                /*---   IMAGE   ---*/
                farmRef.child(model.getFarmId())
                        .addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        final FarmModel currentFarm = dataSnapshot.getValue(FarmModel.class);

                                        if (currentFarm != null){

                                            if (!currentFarm.getFarmImageThumb().equalsIgnoreCase("")){

                                                Picasso.get()
                                                        .load(currentFarm.getFarmImageThumb())
                                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                                        .placeholder(R.drawable.menorah_placeholder)
                                                        .into(viewHolder.sponsoredFarmImage, new Callback() {
                                                            @Override
                                                            public void onSuccess() {

                                                            }

                                                            @Override
                                                            public void onError(Exception e) {
                                                                Picasso.get()
                                                                        .load(currentFarm.getFarmImageThumb())
                                                                        .placeholder(R.drawable.menorah_placeholder)
                                                                        .into(viewHolder.sponsoredFarmImage);
                                                            }
                                                        });

                                            } else {

                                                viewHolder.sponsoredFarmImage.setImageResource(R.drawable.menorah_placeholder);

                                            }

                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                }
                        );

                /*---   PRICE   ---*/
                long priceToLong = Long.parseLong(model.getUnitPrice());

                viewHolder.sponsoredFarmType.setText(model.getSponsoredFarmType());
                viewHolder.sponsoredFarmPriceUnit.setText(Common.convertToPrice(SponsoredFarms.this, priceToLong) + " x " + model.getSponsoredUnits() + " units.");

                viewHolder.sponsoredFarmRefNumber.setText(model.getSponsorRefNumber());
                viewHolder.sponsoredFarmROI.setText(model.getSponsoredFarmRoi() + "% returns in " + model.getSponsorshipDuration() + " months.");

                if (model.getStatus().equalsIgnoreCase("processing")){

                    viewHolder.processing.setVisibility(View.VISIBLE);
                    viewHolder.pending.setVisibility(View.GONE);

                } else if (model.getStatus().equalsIgnoreCase("pending")){

                    viewHolder.processing.setVisibility(View.GONE);
                    viewHolder.pending.setVisibility(View.VISIBLE);

                } else {

                    viewHolder.processing.setVisibility(View.GONE);
                    viewHolder.pending.setVisibility(View.GONE);

                }

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
