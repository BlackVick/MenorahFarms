package com.blackviking.menorahfarms.AdminFragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.RunningCycleModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.ViewHolders.RunningCycleViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class RunningSponsorships extends Fragment {

    private RecyclerView runningCycleRecycler;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<RunningCycleModel, RunningCycleViewHolder> adapter;

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, sponsorshipRef, adminSponsorshipRef;

    public RunningSponsorships() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_running_sponsorships, container, false);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        sponsorshipRef = db.getReference("SponsoredFarms");
        adminSponsorshipRef = db.getReference("RunningCycles");


        /*---   WIDGETS   ---*/
        runningCycleRecycler = (RecyclerView)v.findViewById(R.id.runningCycleRecycler);


        loadRunningCycles();

        return v;
    }

    private void loadRunningCycles() {

        runningCycleRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        runningCycleRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<RunningCycleModel, RunningCycleViewHolder>(
                RunningCycleModel.class,
                R.layout.running_cycle_item,
                RunningCycleViewHolder.class,
                adminSponsorshipRef
        ) {
            @Override
            protected void populateViewHolder(final RunningCycleViewHolder viewHolder, RunningCycleModel model, int position) {

                userRef.child(model.getUserId())
                        .addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        String firstName = dataSnapshot.child("firstName").getValue().toString();
                                        String lastName = dataSnapshot.child("lastName").getValue().toString();

                                        viewHolder.cycleUserName.setText(firstName + " " + lastName);

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                }
                        );

                long returnToLong = Long.parseLong(model.getSponsorReturn());

                viewHolder.cycleFarmType.setText(model.getSponsoredFarmType());
                viewHolder.cycleRefNumber.setText("Ref: " + model.getSponsorRefNumber());
                viewHolder.cycleUnits.setText(model.getSponsoredUnits() + " Units");
                viewHolder.cycleAmountPaid.setText("Amount Paid: " + Common.convertToPrice(getContext(), model.getTotalAmountPaid()));
                viewHolder.cycleReturn.setText("Return: " + Common.convertToPrice(getContext(), returnToLong));
                viewHolder.cycleStartDate.setText(model.getCycleStartDate());
                viewHolder.cycleEndDate.setText(model.getCycleEndDate());

            }
        };
        runningCycleRecycler.setAdapter(adapter);

    }

}
