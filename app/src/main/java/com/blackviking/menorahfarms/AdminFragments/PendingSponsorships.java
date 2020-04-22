package com.blackviking.menorahfarms.AdminFragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blackviking.menorahfarms.AdminDetails.PendingSponsorshipDetail;
import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.Models.PendingSponsorshipModel;
import com.blackviking.menorahfarms.Models.RunningCycleModel;
import com.blackviking.menorahfarms.Models.UserModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.ViewHolders.AdminViewHolder;
import com.blackviking.menorahfarms.ViewHolders.PendingSponsorshipViewHolder;
import com.blackviking.menorahfarms.ViewHolders.RunningCycleViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PendingSponsorships extends Fragment {

    private RecyclerView pendingRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<PendingSponsorshipModel, AdminViewHolder> adapter;
    private FirebaseRecyclerAdapter<PendingSponsorshipModel, PendingSponsorshipViewHolder> adapter2;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, pendSponsorshipRef;

    private RelativeLayout noInternetLayout;
    private LinearLayout emptyLayout, controlLayout;
    private ImageView backButton;
    private TextView groupName;

    public PendingSponsorships() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pending_sponsorships, container, false);

        /*---   FIREBASE   ---*/
        userRef = db.getReference(Common.USERS_NODE);
        pendSponsorshipRef = db.getReference(Common.PENDING_NODE);


        /*---   WIDGETS   ---*/
        pendingRecycler = v.findViewById(R.id.pendingRecycler);
        noInternetLayout = v.findViewById(R.id.noInternetLayout);
        emptyLayout = v.findViewById(R.id.emptyLayout);
        controlLayout = v.findViewById(R.id.controlLayout);
        backButton = v.findViewById(R.id.backButton);
        groupName = v.findViewById(R.id.groupName);

        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(getContext(), new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

                //check all cases
                if (output == 1){

                    pendSponsorshipRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()){

                                noInternetLayout.setVisibility(View.GONE);
                                emptyLayout.setVisibility(View.GONE);
                                pendingRecycler.setVisibility(View.VISIBLE);
                                loadPendingSponsorships();

                            } else {

                                noInternetLayout.setVisibility(View.GONE);
                                emptyLayout.setVisibility(View.VISIBLE);
                                pendingRecycler.setVisibility(View.GONE);

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
                    pendingRecycler.setVisibility(View.GONE);

                } else

                if (output == 2){

                    //set layout
                    noInternetLayout.setVisibility(View.VISIBLE);
                    emptyLayout.setVisibility(View.GONE);
                    pendingRecycler.setVisibility(View.GONE);

                }

            }
        }).execute();

        return v;

    }

    private void loadPendingSponsorships() {

        //setLayout
        controlLayout.setVisibility(View.GONE);

        pendingRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        pendingRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<PendingSponsorshipModel, AdminViewHolder>(
                PendingSponsorshipModel.class,
                R.layout.admin_item,
                AdminViewHolder.class,
                pendSponsorshipRef
        ) {
            @Override
            protected void populateViewHolder(final AdminViewHolder viewHolder, PendingSponsorshipModel model, int position) {

                viewHolder.itemIdentification.setText(adapter.getRef(viewHolder.getAdapterPosition()).getKey());


                viewHolder.setItemClickListener((view, position1, isLongClick) ->
                        loadPendSponsorshipList(adapter.getRef(viewHolder.getAdapterPosition()).getKey())
                );
            }
        };
        pendingRecycler.setAdapter(adapter);

    }

    private void loadPendSponsorshipList(String key) {

        //setLayout
        controlLayout.setVisibility(View.VISIBLE);
        groupName.setText(key);

        //back
        backButton.setOnClickListener(view -> loadPendingSponsorships());

        pendingRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        pendingRecycler.setLayoutManager(layoutManager);

        adapter2 = new FirebaseRecyclerAdapter<PendingSponsorshipModel, PendingSponsorshipViewHolder>(
                PendingSponsorshipModel.class,
                R.layout.pending_sponsorship_item,
                PendingSponsorshipViewHolder.class,
                pendSponsorshipRef.child(key)
        ) {
            @Override
            protected void populateViewHolder(final PendingSponsorshipViewHolder viewHolder, PendingSponsorshipModel model, int position) {

                //get item id
                String sponsorshipId = adapter2.getRef(viewHolder.getAdapterPosition()).getKey();

                //load view
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
                        detailIntent.putExtra("PendingSponsorshipId", sponsorshipId);
                        detailIntent.putExtra("SponsorshipGroup", key);
                        startActivity(detailIntent);
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

                    }
                });


            }
        };
        pendingRecycler.setAdapter(adapter2);

    }
}
