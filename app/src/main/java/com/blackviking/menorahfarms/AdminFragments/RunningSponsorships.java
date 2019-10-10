package com.blackviking.menorahfarms.AdminFragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.blackviking.menorahfarms.Common.CheckInternet;
import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Models.RunningCycleModel;
import com.blackviking.menorahfarms.Models.UserModel;
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

    private RelativeLayout noInternetLayout;
    private LinearLayout emptyLayout;

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
        noInternetLayout = v.findViewById(R.id.noInternetLayout);
        emptyLayout = v.findViewById(R.id.emptyLayout);


        //execute network check async task
        CheckInternet asyncTask = (CheckInternet) new CheckInternet(getContext(), new CheckInternet.AsyncResponse(){
            @Override
            public void processFinish(Integer output) {

                //check all cases
                if (output == 1){

                    adminSponsorshipRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()){

                                noInternetLayout.setVisibility(View.GONE);
                                emptyLayout.setVisibility(View.GONE);
                                runningCycleRecycler.setVisibility(View.VISIBLE);
                                loadRunningCycles();

                            } else {

                                noInternetLayout.setVisibility(View.GONE);
                                emptyLayout.setVisibility(View.VISIBLE);
                                runningCycleRecycler.setVisibility(View.GONE);

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
                    runningCycleRecycler.setVisibility(View.GONE);

                } else

                if (output == 2){

                    //set layout
                    noInternetLayout.setVisibility(View.VISIBLE);
                    emptyLayout.setVisibility(View.GONE);
                    runningCycleRecycler.setVisibility(View.GONE);

                }

            }
        }).execute();

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

                                        UserModel currentUser = dataSnapshot.getValue(UserModel.class);

                                        if (currentUser != null){

                                            viewHolder.cycleUserName.setText(currentUser.getFirstName() + " " + currentUser.getLastName());

                                        }

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
