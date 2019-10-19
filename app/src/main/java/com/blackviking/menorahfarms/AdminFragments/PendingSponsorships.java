package com.blackviking.menorahfarms.AdminFragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.menorahfarms.AdminDetails.PendingSponsorshipDetail;
import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.Models.PendingSponsorshipModel;
import com.blackviking.menorahfarms.Models.UserModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.ViewHolders.AdminViewHolder;
import com.blackviking.menorahfarms.ViewHolders.PendingSponsorshipViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class PendingSponsorships extends Fragment {

    private RecyclerView pendingCycleRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<PendingSponsorshipModel, PendingSponsorshipViewHolder> adapter;
    private FirebaseRecyclerAdapter<PendingSponsorshipModel, AdminViewHolder> adapter2;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, pendingSponsorshipRef;

    private RelativeLayout noInternetLayout, controlLayout;
    private LinearLayout emptyLayout;
    private ImageView backButton;
    private TextView pendingGroup;

    public PendingSponsorships() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pending_sponsorships, container, false);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        pendingSponsorshipRef = db.getReference("SponsoredFarms");


        /*---   WIDGETS   ---*/
        pendingCycleRecycler = (RecyclerView)v.findViewById(R.id.pendingCycleRecycler);
        noInternetLayout = v.findViewById(R.id.noInternetLayout);
        emptyLayout = v.findViewById(R.id.emptyLayout);
        controlLayout = v.findViewById(R.id.controlLayout);
        backButton = v.findViewById(R.id.backButton);
        pendingGroup = v.findViewById(R.id.pendingGroup);


        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(getContext(), new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

                //check all cases
                if (output == 1){

                    pendingSponsorshipRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()){

                                noInternetLayout.setVisibility(View.GONE);
                                emptyLayout.setVisibility(View.GONE);
                                pendingCycleRecycler.setVisibility(View.VISIBLE);
                                loadPendingSponsorshipGroup();

                            } else {

                                noInternetLayout.setVisibility(View.GONE);
                                emptyLayout.setVisibility(View.VISIBLE);
                                pendingCycleRecycler.setVisibility(View.GONE);

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else

                if (output == 0){

                    //set layout
                    noInternetLayout.setVisibility(View.VISIBLE);
                    emptyLayout.setVisibility(View.GONE);
                    pendingCycleRecycler.setVisibility(View.GONE);

                } else

                if (output == 2){

                    //set layout
                    noInternetLayout.setVisibility(View.VISIBLE);
                    emptyLayout.setVisibility(View.GONE);
                    pendingCycleRecycler.setVisibility(View.GONE);

                }

            }
        }).execute();


        return v;
    }

    private void loadPendingSponsorshipGroup() {

        //setLayout
        controlLayout.setVisibility(View.GONE);

        pendingCycleRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        pendingCycleRecycler.setLayoutManager(layoutManager);

        adapter2 = new FirebaseRecyclerAdapter<PendingSponsorshipModel, AdminViewHolder>(
                PendingSponsorshipModel.class,
                R.layout.admin_item,
                AdminViewHolder.class,
                pendingSponsorshipRef
        ) {
            @Override
            protected void populateViewHolder(final AdminViewHolder viewHolder, PendingSponsorshipModel model, int position) {

                viewHolder.itemIdentification.setText(adapter2.getRef(viewHolder.getAdapterPosition()).getKey());


                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        loadPendingSponsorships(adapter2.getRef(viewHolder.getAdapterPosition()).getKey());
                    }
                });
            }
        };
        pendingCycleRecycler.setAdapter(adapter2);

    }

    private void loadPendingSponsorships(final String key) {

        //set layout
        controlLayout.setVisibility(View.VISIBLE);
        pendingGroup.setText(key);

        pendingCycleRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        pendingCycleRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<PendingSponsorshipModel, PendingSponsorshipViewHolder>(
                PendingSponsorshipModel.class,
                R.layout.running_cycle_item,
                PendingSponsorshipViewHolder.class,
                pendingSponsorshipRef.child(key)
        ) {
            @Override
            protected void populateViewHolder(final PendingSponsorshipViewHolder viewHolder, PendingSponsorshipModel model, int position) {

                //current sponsorship
                final String currentSponsorshipId = adapter.getRef(viewHolder.getAdapterPosition()).getKey();

                //user info
                userRef.child(model.getUserId())
                        .addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        UserModel currentUser = dataSnapshot.getValue(UserModel.class);

                                        if (currentUser != null){

                                            viewHolder.userName.setText(currentUser.getFirstName() + " " + currentUser.getLastName());

                                            if (!currentUser.getProfilePictureThumb().equalsIgnoreCase("")){

                                                Picasso.get()
                                                        .load(currentUser.getProfilePictureThumb())
                                                        .placeholder(R.drawable.profile)
                                                        .into(viewHolder.userImg);

                                            } else {

                                                viewHolder.userImg.setImageResource(R.drawable.profile);

                                            }
                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                }
                        );

                //sponsorship info
                viewHolder.userSponsorRef.setText(model.getSponsorRefNumber());
                viewHolder.userAmountPaid.setText("Amount Paid: " + Common.convertToPrice(getContext(), model.getTotalAmountPaid()));

                //confirm in dialog
                viewHolder.confimPending.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent detailIntent = new Intent(getContext(), PendingSponsorshipDetail.class);
                        detailIntent.putExtra("PendingSponsorshipId", currentSponsorshipId);
                        detailIntent.putExtra("SponsorshipGroup", key);
                        startActivity(detailIntent);
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

                    }
                });

            }
        };
        pendingCycleRecycler.setAdapter(adapter);


        //back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPendingSponsorshipGroup();
            }
        });

    }

}
