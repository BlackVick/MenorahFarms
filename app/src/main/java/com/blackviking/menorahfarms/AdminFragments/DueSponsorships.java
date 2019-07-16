package com.blackviking.menorahfarms.AdminFragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blackviking.menorahfarms.AdminDetails.DueSponsorshipDetail;
import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.Models.DueSponsorshipModel;
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

    public DueSponsorships() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_due_sponsorships, container, false);


        /*---   FIREBASE   ---*/
        dueSponsorshipRef = db.getReference("DueSponsorships");
        sponsorshipRef = db.getReference("SponsoredFarms");


        /*---   WIDGETS   ---*/
        dueSponsorshipRecycler = (RecyclerView)v.findViewById(R.id.dueSponsorshipRecycler);


        loadDueSponsorships();

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

                sponsorshipRef.child(model.getSponsorshipId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                String theRef = dataSnapshot.child("sponsorRefNumber").getValue().toString();

                                viewHolder.itemIdentification.setText(theRef);

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
