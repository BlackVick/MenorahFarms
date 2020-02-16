package com.blackviking.menorahfarms.AdminFragments;


import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.blackviking.menorahfarms.AdminDetails.DueSponsorshipDetail;
import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.Models.DueSponsorshipModel;
import com.blackviking.menorahfarms.Models.SponsoredFarmModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.ViewHolders.AdminViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class DueSponsorships extends Fragment {

    private RecyclerView dueSponsorshipRecycler;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference dueSponsorshipRef, sponsorshipRef;

    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<DueSponsorshipModel, AdminViewHolder> adapter;

    private RelativeLayout noInternetLayout;
    private LinearLayout emptyLayout;

    public DueSponsorships() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_due_sponsorships, container, false);


        /*---   FIREBASE   ---*/
        dueSponsorshipRef = db.getReference(Common.DUE_SPONSORSHIPS_NODE);
        sponsorshipRef = db.getReference(Common.SPONSORED_FARMS_NODE);


        /*---   WIDGETS   ---*/
        dueSponsorshipRecycler = v.findViewById(R.id.dueSponsorshipRecycler);
        noInternetLayout = v.findViewById(R.id.noInternetLayout);
        emptyLayout = v.findViewById(R.id.emptyLayout);


        //run network check
        new CheckInternet(getContext(), output -> {

            //check all cases
            if (output == 1){

                dueSponsorshipRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            noInternetLayout.setVisibility(View.GONE);
                            emptyLayout.setVisibility(View.GONE);
                            dueSponsorshipRecycler.setVisibility(View.VISIBLE);
                            loadDueSponsorships();

                        } else {

                            noInternetLayout.setVisibility(View.GONE);
                            emptyLayout.setVisibility(View.VISIBLE);
                            dueSponsorshipRecycler.setVisibility(View.GONE);

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
                dueSponsorshipRecycler.setVisibility(View.GONE);

            } else

            if (output == 2){

                //set layout
                noInternetLayout.setVisibility(View.VISIBLE);
                emptyLayout.setVisibility(View.GONE);
                dueSponsorshipRecycler.setVisibility(View.GONE);

            }

        }).execute();

        return v;
    }

    private void loadDueSponsorships() {

        dueSponsorshipRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        dueSponsorshipRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<DueSponsorshipModel, AdminViewHolder>(
                DueSponsorshipModel.class,
                R.layout.admin_item,
                AdminViewHolder.class,
                dueSponsorshipRef
        ) {
            @Override
            protected void populateViewHolder(final AdminViewHolder viewHolder, DueSponsorshipModel model, int position) {

                sponsorshipRef.child(model.getUser())
                        .child(model.getSponsorshipId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                SponsoredFarmModel currentSpons = dataSnapshot.getValue(SponsoredFarmModel.class);

                                if (currentSpons != null){

                                    viewHolder.itemIdentification.setText(currentSpons.getSponsorRefNumber());

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent intent = new Intent(getContext(), DueSponsorshipDetail.class);
                        intent.putExtra("DueSponsorshipId", adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                        startActivity(intent);
                    }
                });

            }
        };
        dueSponsorshipRecycler.setAdapter(adapter);

    }

}
