package com.blackviking.menorahfarms.CartAndHistory;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.blackviking.menorahfarms.Common.Common;
import com.blackviking.menorahfarms.Interface.ItemClickListener;
import com.blackviking.menorahfarms.Models.FarmModel;
import com.blackviking.menorahfarms.Models.HistoryModel;
import com.blackviking.menorahfarms.R;
import com.blackviking.menorahfarms.ViewHolders.HistoryViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryProjects extends Fragment {

    private RecyclerView historyRecycler;
    private LinearLayout emptyLayout;
    private LinearLayoutManager layoutManager;
    private FirebaseRecyclerAdapter<HistoryModel, HistoryViewHolder> adapter;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private DatabaseReference userRef, farmRef, sponsorshipRef, historyRef;

    private String currentUid;

    public HistoryProjects() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history_projects, container, false);


        /*---   FIREBASE   ---*/
        userRef = db.getReference("Users");
        farmRef = db.getReference("Farms");
        sponsorshipRef = db.getReference("SponsoredFarms");
        historyRef = db.getReference("History");
        if (mAuth.getCurrentUser() != null)
            currentUid = mAuth.getCurrentUser().getUid();


        /*---   WIDGET   ---*/
        emptyLayout = (LinearLayout)v.findViewById(R.id.emptyLayout);
        historyRecycler = (RecyclerView)v.findViewById(R.id.projectRecycler);


        /*---   CHECK   ---*/
        historyRef.child(currentUid)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){

                                    historyRecycler.setVisibility(View.VISIBLE);
                                    emptyLayout.setVisibility(View.GONE);
                                    loadHistory();

                                } else {

                                    historyRecycler.setVisibility(View.GONE);
                                    emptyLayout.setVisibility(View.VISIBLE);

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );


        return v;
    }

    private void loadHistory() {

        historyRecycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        historyRecycler.setLayoutManager(layoutManager);

        adapter = new FirebaseRecyclerAdapter<HistoryModel, HistoryViewHolder>(
                HistoryModel.class,
                R.layout.project_history_layout,
                HistoryViewHolder.class,
                historyRef.child(currentUid)
        ) {
            @Override
            protected void populateViewHolder(final HistoryViewHolder viewHolder, final HistoryModel model, int position) {

                farmRef.child(model.getFarmId())
                        .addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        FarmModel currentFarm = dataSnapshot.getValue(FarmModel.class);

                                        if (currentFarm != null){

                                            if (!currentFarm.getFarmImageThumb().equalsIgnoreCase("")){

                                                Picasso.get()
                                                        .load(currentFarm.getFarmImageThumb())
                                                        .into(viewHolder.historyProjectImage);

                                            }

                                            viewHolder.historyProjectDets.setText(currentFarm.getFarmName()
                                            + " yields a " + currentFarm.getFarmRoi() + "% return over a period of "
                                            + currentFarm.getSponsorDuration() + " months.");

                                            viewHolder.historyProjectNickName.setText(currentFarm.getFarmType());
                                            viewHolder.historyProjectLocation.setText(currentFarm.getFarmLocation()
                                            + "-" + model.getCycleStartDate());


                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                }
                        );

                viewHolder.historyProjectUnits.setText(model.getSponsoredUnits());
                viewHolder.historyProjectPrice.setText(Common.convertToPrice(getContext(), model.getTotalAmountPaid()));

                long returnToLong = Long.parseLong(model.getSponsorReturn());

                viewHolder.historyProjectReturn.setText(Common.convertToPrice(getContext(), returnToLong));

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        Intent historyDetailIntent = new Intent(getContext(), HistoryDetails.class);
                        historyDetailIntent.putExtra("HistoryId", adapter.getRef(viewHolder.getAdapterPosition()).getKey());
                        startActivity(historyDetailIntent);
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

                    }
                });

            }
        };
        historyRecycler.setAdapter(adapter);

    }

}
